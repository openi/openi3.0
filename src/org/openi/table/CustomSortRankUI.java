package org.openi.table;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.tonbeller.jpivot.core.Model;
import com.tonbeller.jpivot.core.ModelChangeEvent;
import com.tonbeller.jpivot.core.ModelChangeListener;
import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.Position;
import com.tonbeller.jpivot.olap.navi.SortRank;
import com.tonbeller.jpivot.table.AxisBuilder;
import com.tonbeller.jpivot.table.SpanBuilder;
import com.tonbeller.jpivot.table.SpanBuilderDecorator;
import com.tonbeller.jpivot.table.TableComponent;
import com.tonbeller.jpivot.table.TableComponentExtensionSupport;
import com.tonbeller.jpivot.table.navi.AxisStyleUI;
import com.tonbeller.jpivot.table.span.Span;
import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.component.RendererParameters;
import com.tonbeller.wcf.controller.Dispatcher;
import com.tonbeller.wcf.controller.DispatcherSupport;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestListener;
import com.tonbeller.wcf.scroller.Scroller;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * @author SUJEN
 */
public class CustomSortRankUI extends TableComponentExtensionSupport implements
		ModelChangeListener {

	public static final String ID = "customSort";

	Dispatcher dispatcher = new DispatcherSupport();
	private boolean triState = true;
	Resources resources;
	SortRank extension;
	boolean renderActions;
	
	private Map sortAscHandlers = new HashMap();
	private Map sortDescHandlers = new HashMap();
	private Map naturalSortHandlers = new HashMap();

	class DummySortRank implements SortRank {
		public boolean isSorting() {
			return false;
		}

		public void setSorting(boolean enabled) {
		}

		public boolean isSortable(Position position) {
			return false;
		}

		public boolean isCurrentSorting(Position position) {
			return false;
		}

		public int getSortMode() {
			return SortRank.ASC;
		}

		public void setSortMode(int mode) {
		}

		public int getTopBottomCount() {
			return 10;
		}

		public void setTopBottomCount(int topBottomCount) {
		}

		public void sort(Axis membersToSort, Position position)
				throws OlapException {
		}

		public void setModel(Model model) {
		}

		public String getId() {
			return SortRank.ID;
		}

		public Model decorate(Model modelToDecorate) {
			return modelToDecorate;
		}

		/**
		 * Notification after model initialization is complete
		 */
		public void modelInitialized() {
			// no action
		}

	}

	public String getId() {
		return ID;
	}

	public void initialize(RequestContext context, TableComponent table)
			throws Exception {
		super.initialize(context, table);
		table.getOlapModel().addModelChangeListener(this);

		extension = getExtension();

		// extend the controller
		table.getDispatcher().addRequestListener(null, null, dispatcher);

		// add some decorators
		AxisBuilder cab = table.getColumnAxisBuilder();
		DomDecorator chr = new DomDecorator(cab.getSpanBuilder());
		cab.setSpanBuilder(chr);
		resources = context.getResources(CustomSortRankUI.class);
	}

	public void startBuild(RequestContext context) {
		super.startBuild(context);
		renderActions = RendererParameters.isRenderActions(context);
		if (renderActions)
			dispatcher.clear();
	}

	/**
	 * adds the sort button elements to the dom tree
	 */
	class DomDecorator extends SpanBuilderDecorator {
		DomDecorator(SpanBuilder delegate) {
			super(delegate);
		}

		public Element build(SBContext sbctx, Span span, boolean even) {
			Element parent = super.build(sbctx, span, even);

			// this extension is disabled
			if (!isEnabled() || !renderActions)
				return parent;

			// natural sorting is shown w/o buttons
			if (!triState && !extension.isSorting())
				return parent;

			// only member can be sorted
			if (!span.isMember())
				return parent;

			// test if member is the on the innermost hierarchy
			Member member = span.getMember();
			Position position = span.getPosition();
			if (!isCandidate(position, member))
				return parent;

			// find the axis to sort by. its "the other" axis
			Axis axis = span.getAxis();
			Axis[] axes = table.getResult().getAxes();
			if (axes.length < 2)
				return parent;
			if (axes[0].getRootDecoree().equals(axis.getRootDecoree()))
				axis = axes[1];
			else
				axis = axes[0];
			
			if (triState) {
				parent.setAttribute("sortable", "true");
				String elemID = parent.getAttribute("id");
				if(elemID == null || elemID.equals("")) {
					elemID = DomUtils.randomId();
					parent.setAttribute("id", elemID);
				}
				String sortAscElemID = elemID + "_sortAsc";
				String sortDescElemID = elemID + "_sortDesc";
				String naturalSortElemID = elemID + "_naturalSort";

				SortAscHandler sortAscHandler = new SortAscHandler(
						position, axis);
				sortAscHandlers.put(sortAscElemID, sortAscHandler);
				dispatcher.addRequestListener(sortAscElemID, null, sortAscHandler);
				
				SortDescHandler sortDescHandler = new SortDescHandler(
						position, axis);
				sortDescHandlers.put(sortDescElemID, sortDescHandler);
				dispatcher.addRequestListener(sortDescElemID, null, sortDescHandler);
				
				NaturalSortHandler naturalSortHandler = new NaturalSortHandler(
						position, axis);
				naturalSortHandlers.put(naturalSortElemID, naturalSortHandler);
				dispatcher.addRequestListener(naturalSortElemID, null, naturalSortHandler);
			}
			
			return parent;
		}

		/**
		 * true if member is at the innermost position and accepted by the
		 * extension
		 */
		boolean isCandidate(Position position, Member member) {
			Member[] members = position.getMembers();
			if (!member.equals(members[members.length - 1]))
				return false;
			return extension.isSortable((Position) position.getRootDecoree());
		}

		@Override
		public Object getBookmarkState(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private class SortAscHandler implements RequestListener {
		Position position;
		Axis axis;

		SortAscHandler(Position position, Axis axis) {
			this.position = (Position) position.getRootDecoree();
			this.axis = (Axis) axis.getRootDecoree();
		}

		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);
			if (!extension.isSorting()) {
				extension.setSorting(true);
			}
			setSortMode(SortRank.ASC);
			extension.sort(axis, position);
		}
	}
	
	private class SortDescHandler implements RequestListener {
		Position position;
		Axis axis;

		SortDescHandler(Position position, Axis axis) {
			this.position = (Position) position.getRootDecoree();
			this.axis = (Axis) axis.getRootDecoree();
		}

		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);
			if (!extension.isSorting()) {
				extension.setSorting(true);
			}
			setSortMode(SortRank.DESC);
			extension.sort(axis, position);
		}
	}

	private class NaturalSortHandler implements RequestListener {
		Position position;
		Axis axis;

		NaturalSortHandler(Position position, Axis axis) {
			this.position = (Position) position.getRootDecoree();
			this.axis = (Axis) axis.getRootDecoree();
		}

		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);
			extension.setSorting(false);
		}
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.SortRank
	 */
	public boolean isSorting() {
		return extension.isSorting();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.SortRank
	 */
	public void setSorting(boolean enabled) {
		extension.setSorting(enabled);
	}

	/**
	 * sets the sort mode and the level style
	 * 
	 * @see com.tonbeller.jpivot.olap.navi.SortRank
	 */
	public void setSortMode(int mode) {
		extension.setSortMode(mode);
		AxisStyleUI asu = (AxisStyleUI) table.getExtensions().get(
				AxisStyleUI.ID);
		if (asu == null)
			return;
		if (mode == SortRank.ASC || mode == SortRank.DESC)
			asu.setLevelStyle(false);
		else
			asu.setLevelStyle(true);
	}

	/**
	 * Returns the triState.
	 * 
	 * @return boolean
	 */
	public boolean isTriState() {
		return triState;
	}

	/**
	 * Sets the triState.
	 * 
	 * @param triState
	 *            The triState to set
	 */
	public void setTriState(boolean triState) {
		this.triState = triState;
	}

	private SortRank getExtension() {
		SortRank ext = (SortRank) table.getOlapModel()
				.getExtension(SortRank.ID);
		if (ext == null)
			ext = new DummySortRank();
		return ext;
	}

	public void modelChanged(ModelChangeEvent e) {
	}

	public void structureChanged(ModelChangeEvent e) {
		extension = getExtension();
		dispatcher.clear();
	}

	@Override
	public Object getBookmarkState(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
