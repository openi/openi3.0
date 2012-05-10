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
 * Sort Buttons
 * 
 * @author av
 * @author SUJEN
 */
public class SortRankUI extends TableComponentExtensionSupport implements
		ModelChangeListener {

	public static final String ID = "sortRank";

	Dispatcher dispatcher = new DispatcherSupport();
	private boolean triState = true;
	Resources resources;
	SortRank extension;
	boolean renderActions;

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
		resources = context.getResources(SortRankUI.class);
	}

	public void startBuild(RequestContext context) {
		super.startBuild(context);
		renderActions = RendererParameters.isRenderActions(context);
		if (renderActions)
			dispatcher.clear();
	}

	private Map sortHandlers = new HashMap();
	
	public Map getSortHandlers() {
		return sortHandlers;
	}

	public void setSortHandlers(Map sortHandlers) {
		this.sortHandlers = sortHandlers;
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

			// this node is sortable!
			Element sort = table.insert("sort", parent);
			String id = DomUtils.randomId();
			sort.setAttribute("id", id);
			if (triState) {
				SortHandler3 sortHandler = new SortHandler3(
						position, axis);
				sortHandlers.put(id, sortHandler);
				dispatcher.addRequestListener(id, null, sortHandler);
			}
			else {
				SortHandler2 sortHandler = new SortHandler2(
						position, axis);
				sortHandlers.put(id, sortHandler);
				dispatcher.addRequestListener(id, null, sortHandler);
			}
			String title = "";
			switch (extension.getSortMode()) {
				case 1:
					title = "Hierarchy Ascending";
					break;
				case 2:
					title = "Hierarchy Descending";
					break;
				case 3:
					title = "Break Hierarchy Ascending";
					break;
				case 4:
					title = "Break Hierarchy Descending";
					break;
				case 5:
					title = "Top Count";
					break;
				case 6:
					title = "Bottom Count";

			}
			/*sort.setAttribute("title",
					resources.getString("sort.mode." + extension.getSortMode()));*/
			sort.setAttribute("title", title);
			if (!extension.isSorting()) {
				sort.setAttribute("mode", "sort-natural");
				/*
				 * sort.setAttribute("title",
				 * resources.getString("sort.mode.natural"));
				 */
				sort.setAttribute("title", "Natural Order");
			} else if (extension.isCurrentSorting((Position) position
					.getRootDecoree())) {
				if (isAscending())
					sort.setAttribute("mode", "sort-current-up");
				else
					sort.setAttribute("mode", "sort-current-down");
			} else {
				if (isAscending())
					sort.setAttribute("mode", "sort-other-up");
				else
					sort.setAttribute("mode", "sort-other-down");
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

	/*
	 * tristate sort handler: ascending -&gt; descending -&gt; disabled
	 */
	private class SortHandler3 implements RequestListener {
		Position position;
		Axis axis;

		SortHandler3(Position position, Axis axis) {
			this.position = (Position) position.getRootDecoree();
			this.axis = (Axis) axis.getRootDecoree();
		}

		// tristate: asc -> desc -> disabled
		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);

			// disabled -> asc
			if (!extension.isSorting()) {
				extension.setSorting(true);
				extension.sort(axis, position);
				return;
			}

			// clicked on another measure -> activate that measure
			if (!extension.isCurrentSorting(position)) {
				extension.sort(axis, position);
				return;
			}

			// asc -> desc
			if (isAscending() || isTopBottomCount()) {
				flipAscending();
				extension.sort(axis, position);
				return;
			}

			// desc -> disabled
			flipAscending();
			extension.setSorting(false);
		}
	}

	/**
	 * two state sort handler: asc &gt; desc
	 */
	private class SortHandler2 implements RequestListener {
		Position position;
		Axis axis;

		SortHandler2(Position position, Axis axis) {
			this.position = (Position) position.getRootDecoree();
			this.axis = (Axis) axis.getRootDecoree();
		}

		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);
			if (extension.isCurrentSorting(position))
				flipAscending();
			extension.sort(axis, position);
		}
	}

	public boolean isAvailable() {
		SortRank ext = getExtension();
		return ext != null && !(ext instanceof DummySortRank);
	}

	boolean isTopBottomCount() {
		int mode = extension.getSortMode();
		return mode == SortRank.TOPCOUNT || mode == SortRank.BOTTOMCOUNT;
	}

	boolean isAscending() {
		int mode = extension.getSortMode();
		return mode == SortRank.ASC || mode == SortRank.BASC
				|| mode == SortRank.BOTTOMCOUNT;
	}

	void flipAscending() {
		int mode = extension.getSortMode();
		switch (mode) {
		case SortRank.ASC:
			mode = SortRank.DESC;
			break;
		case SortRank.DESC:
			mode = SortRank.ASC;
			break;
		case SortRank.BASC:
			mode = SortRank.BDESC;
			break;
		case SortRank.BDESC:
			mode = SortRank.BASC;
			break;
		case SortRank.TOPCOUNT:
			mode = SortRank.BOTTOMCOUNT;
			break;
		case SortRank.BOTTOMCOUNT:
			mode = SortRank.TOPCOUNT;
			break;
		}
		extension.setSortMode(mode);
	}

	/*
	 * ------------------------ properties for settings form
	 * --------------------
	 */

	/**
	 * @see com.tonbeller.jpivot.olap.navi.SortRank
	 */
	public int getSortMode() {
		return extension.getSortMode();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.SortRank
	 */
	public int getTopBottomCount() {
		return extension.getTopBottomCount();
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
	 * @see com.tonbeller.jpivot.olap.navi.SortRank
	 */
	public void setTopBottomCount(int topBottomCount) {
		extension.setTopBottomCount(topBottomCount);
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

	/* ----------------------------------------------------------------- */

	/**
	 * for easier GUI, user chooses a radio button out of "Keep Hierarchy",
	 * "Break Hierarchy" and "Topcount"
	 */
	public boolean isBreakHierarchy() {
		switch (extension.getSortMode()) {
		case SortRank.BASC:
		case SortRank.BDESC:
			return true;
		default:
			return false;
		}
	}

	public void setBreakHierarchy(boolean b) {
		if (b)
			setSortMode(SortRank.BDESC);
	}

	public boolean isKeepHierarchy() {
		switch (extension.getSortMode()) {
		case SortRank.ASC:
		case SortRank.DESC:
			return true;
		default:
			return false;
		}
	}

	public void setKeepHierarchy(boolean b) {
		if (b)
			setSortMode(SortRank.DESC);
	}

	public boolean isRanking() {
		switch (extension.getSortMode()) {
		case SortRank.TOPCOUNT:
		case SortRank.BOTTOMCOUNT:
			return true;
		default:
			return false;
		}
	}

	public void setRanking(boolean b) {
		if (b)
			setSortMode(SortRank.TOPCOUNT);
	}

	@Override
	public Object getBookmarkState(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
