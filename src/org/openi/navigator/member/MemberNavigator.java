package org.openi.navigator.member;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.tonbeller.jpivot.core.ModelChangeEvent;
import com.tonbeller.jpivot.core.ModelChangeListener;
import com.tonbeller.jpivot.olap.model.Displayable;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.model.OlapUtils;
import com.tonbeller.jpivot.olap.navi.MemberDeleter;
import com.tonbeller.jpivot.olap.navi.MemberTree;
import com.tonbeller.jpivot.ui.Available;
import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.controller.Dispatcher;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestListener;
import com.tonbeller.wcf.scroller.Scroller;
import com.tonbeller.wcf.selection.SelectionModel;
import com.tonbeller.wcf.tree.CachingTreeModelDecorator;
import com.tonbeller.wcf.tree.DecoratedTreeModel;
import com.tonbeller.wcf.tree.DefaultDeleteNodeModel;
import com.tonbeller.wcf.tree.DefaultLabelProvider;
import com.tonbeller.wcf.tree.DefaultNodeRenderer;
import com.tonbeller.wcf.tree.DeleteNodeModel;
import com.tonbeller.wcf.tree.EnumBoundedTreeModelDecorator;
import com.tonbeller.wcf.tree.GroupingTreeModelDecorator;
import com.tonbeller.wcf.tree.LabelProvider;
import com.tonbeller.wcf.tree.MutableTreeModelDecorator;
import com.tonbeller.wcf.tree.TreeModel;
import com.tonbeller.wcf.ui.Button;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * GUI for choosing members. User of this class must call setHierarchy or
 * setHierarchies before rendering.
 * 
 * @author SUJEN
 */
public class MemberNavigator extends TreeComponent implements
		ModelChangeListener, Available {

	public static final String MEMBER_NAVIGATOR_LAZY_FETCH_CHILDREN = "MemberNavigator.lazyFetchChildren";
	public static final String MEMBER_NAVIGATOR_EXPAND_SELECTED = "MemberNavigator.expandSelected";
	public static final String MEMBER_NAVIGATOR_INITIAL_GROUPING = "MemberNavigator.initialGrouping";
	public static final String MEMBER_NAVIGATOR_GROUPING_MEMBER_COUNT = "MemberNavigator.groupingMemberCount";

	private OlapModel olapModel;
	private String title;
	private RequestListener okHandler;

	public RequestListener getOkHandler() {
		return okHandler;
	}

	public void setOkHandler(RequestListener okHandler) {
		this.okHandler = okHandler;
	}

	public RequestListener getCancelHandler() {
		return cancelHandler;
	}

	public void setCancelHandler(RequestListener cancelHandler) {
		this.cancelHandler = cancelHandler;
	}

	private RequestListener cancelHandler;
	private boolean showSelectNoneButton;
	private String okButtonId;
	private String cancelButtonId;
	private String selectVisibleButtonId;
	private String selectNoneButtonId;
	private String enableGroupingButtonId;
	private String disableGroupingButtonId;
	// contains a Tree (value) for a HierarchyArray (key)
	private Map models = new HashMap();
	private Resources resources;

	private int groupingMemberCount = 12;
	private boolean initialGrouping = false;
	private boolean expandSelected = true;
	private boolean lazyFetchChildren = false;

	/**
	 * defines equals/hashCode for an array of hierarchies. Two arrays are equal
	 * if and only if all their hierarchies are equal.
	 * 
	 * @author av
	 */
	static class HierarchyArray {

		private Hierarchy[] hiers;

		public HierarchyArray(Hierarchy[] hiers) {
			this.hiers = hiers;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof HierarchyArray))
				return false;
			HierarchyArray that = (HierarchyArray) obj;
			if (this.hiers.length != that.hiers.length)
				return false;
			for (int i = 0; i < hiers.length; i++)
				if (!this.hiers[i].equals(that.hiers[i]))
					return false;
			return true;
		}

		public Hierarchy[] getHierarchies() {
			return hiers;
		}

		public String getLabel() {
			if (hiers.length == 0)
				return ""; //$NON-NLS-1$
			if (hiers.length == 1)
				return hiers[0].getLabel();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < hiers.length; i++) {
				if (i > 0)
					sb.append(" / "); //$NON-NLS-1$
				sb.append(hiers[i].getLabel());
			}
			return sb.toString();
		}

		public int hashCode() {
			int code = 27;
			for (int i = 0; i < hiers.length; i++) {
				code ^= hiers[i].hashCode();
			}
			return code;
		}

		public void setHierarchies(Hierarchy[] hiers) {
			this.hiers = hiers;
		}

	}

	class SelectVisibleHandler implements RequestListener {
		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);
			validate(context);
			selectVisible();
		}
	}

	class SelectNoneHandler implements RequestListener {
		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);
			validate(context);
			getSelectionModel().clear();
		}
	}

	class SetGroupingHandler implements RequestListener {
		boolean grouping;

		SetGroupingHandler(boolean grouping) {
			this.grouping = grouping;
		}

		public void request(RequestContext context) throws Exception {
			validate(context);
			setGrouping(grouping);
		}
	}

	class MutableMemberTreeModelDecorator extends MutableTreeModelDecorator {
		public MutableMemberTreeModelDecorator(TreeModel decoree) {
			super(decoree);
		}

		public MutableMemberTreeModelDecorator(TreeModel decoree,
				Comparator comp) {
			super(decoree, comp);
		}

		public boolean mayMove(Object scope, Object node) {
			// non-members are virtual groups
			if (!(node instanceof Member)) {
				return false;
			}
			return super.mayMove(scope, node);
		}
	}

	private static Logger logger = Logger.getLogger(MemberNavigator.class);

	private DeleteNodeModel deleteModel = new DefaultDeleteNodeModel() {
		public boolean isDeletable(Object node) {
			if (olapModel == null)
				return false;
			MemberDeleter md = (MemberDeleter) olapModel
					.getExtension(MemberDeleter.ID);
			if (md == null)
				return false;
			if (node instanceof Member)
				return md.isDeletable((Member) node);
			return false;
		}
	};

	public MemberNavigator(String id, Component parent, OlapModel olapModel,
			RequestListener okHandler, RequestListener cancelHandler) {
		super(id, parent);
		logger.info("creating instance: " + this);

		this.olapModel = olapModel;
		if (olapModel != null)
			olapModel.addModelChangeListener(this);

		this.okHandler = okHandler;
		this.cancelHandler = cancelHandler;
		setNodeRenderer(new DefaultNodeRenderer(labelProvider));
		if (id == null)
			id = DomUtils.randomId();
		okButtonId = id + ".ok";
		cancelButtonId = id + ".cancel";
		selectVisibleButtonId = id + ".selectVisible";
		selectNoneButtonId = id + ".selectNone";
		enableGroupingButtonId = id + ".enableGrouping";
		disableGroupingButtonId = id + ".disableGrouping";
		setSelectionModel(new MemberSelectionModel());
		setDeleteNodeModel(deleteModel);
		// move nodes via Cut/Paste GUI
		setCutPasteMode(true);
	}

	/**
	 * crerates a MemberNavigator that is not connected to a query. Before
	 * callin setHierarchies() the client must call setOlapModel().
	 */
	public MemberNavigator(String id, Component parent,
			RequestListener okHandler, RequestListener cancelHandler) {
		this(id, parent, null, okHandler, cancelHandler);
	}

	public void setOlapModel(OlapModel newOlapModel) {
		if (olapModel != null)
			olapModel.removeModelChangeListener(this);
		olapModel = newOlapModel;
		if (olapModel != null)
			olapModel.addModelChangeListener(this);
		models.clear();
	}

	/**
	 * Returns the title.
	 * 
	 * @return String
	 */
	public String getTitle() {
		return title;
	}

	public void initialize(RequestContext context) throws Exception {
		super.initialize(context);

		Dispatcher disp = super.getDispatcher();
		disp.addRequestListener(selectVisibleButtonId, null,
				new SelectVisibleHandler());
		disp.addRequestListener(selectNoneButtonId, null,
				new SelectNoneHandler());
		disp.addRequestListener(enableGroupingButtonId, null,
				new SetGroupingHandler(true));
		disp.addRequestListener(disableGroupingButtonId, null,
				new SetGroupingHandler(false));
		disp.addRequestListener(okButtonId, null, okHandler);
		disp.addRequestListener(cancelButtonId, null, cancelHandler);
		resources = context.getResources(MemberNavigator.class);

		groupingMemberCount = resources.getOptionalInteger(
				MEMBER_NAVIGATOR_GROUPING_MEMBER_COUNT, groupingMemberCount);
		initialGrouping = resources.getOptionalBoolean(
				MEMBER_NAVIGATOR_INITIAL_GROUPING, initialGrouping);
		expandSelected = resources.getOptionalBoolean(
				MEMBER_NAVIGATOR_EXPAND_SELECTED, expandSelected);
		lazyFetchChildren = resources.getOptionalBoolean(
				MEMBER_NAVIGATOR_LAZY_FETCH_CHILDREN, lazyFetchChildren);

		// test environment?
		String s = context.getRequest().getParameter(
				MEMBER_NAVIGATOR_LAZY_FETCH_CHILDREN);
		if (s != null)
			lazyFetchChildren = Boolean.valueOf(s).booleanValue();
	}

	public Element render(RequestContext context, Document factory)
			throws Exception {
		if (getModel() == null)
			throw new IllegalStateException(
					"must call MemberNavigator.setHierarchy() before rendering");
		Element elem = super.render(context, factory);
		elem.setAttribute("title", title);
		elem.setAttribute("closeId", cancelButtonId);
		renderButtons(elem, factory);
		setError(null);
		return elem;
	}

	private void renderButtons(Element parent, Document factory) {
		Element buttons = factory.createElement("buttons");
		parent.appendChild(buttons);
		appendSelectButton(buttons);
		appendGroupingButton(buttons);
		// Button.addButton(buttons, okButtonId,
		// resources.getString("MemberNavigator.ok.title"));
		// Button.addButton(buttons, cancelButtonId,
		// resources.getString("MemberNavigator.cancel.title"));
		Button.addButton(buttons, okButtonId, "Ok");
		Button.addButton(buttons, cancelButtonId, "Cancel");
	}

	private void appendGroupingButton(Element buttons) {
		if (isGrouping()) {
			// String label =
			// resources.getString("MemberNavigator.disableGrouping.title");
			String label = "Flat";
			Button.addButton(buttons, disableGroupingButtonId, label);
		} else {
			// String label =
			// resources.getString("MemberNavigator.enableGrouping.title");
			String label = "Group";
			Button.addButton(buttons, enableGroupingButtonId, label);
		}
	}

	private void appendSelectButton(Element buttons) {
		// in multiple selection mode append toggle-button "select all" /
		// "select none"
		if (getSelectionModel().getMode() == SelectionModel.MULTIPLE_SELECTION) {
			if (getSelectionModel().isEmpty()) {
				// String label =
				// resources.getString("MemberNavigator.selectVisible.title");
				String label = "All";
				Button.addButton(buttons, selectVisibleButtonId, label);
			} else {
				// String label =
				// resources.getString("MemberNavigator.selectNone.title");
				String label = "None";
				Button.addButton(buttons, selectNoneButtonId, label);
			}
		}
		// in single selection mode optionally append "select none"
		else if (showSelectNoneButton) {
			// String label =
			// resources.getString("MemberNavigator.selectNone.title");
			String label = "None";
			Button.addButton(buttons, selectNoneButtonId, label);
		}
	}

	public static LabelProvider labelProvider = new DefaultLabelProvider() {
		public String getLabel(Object node) {
			if (node instanceof Displayable)
				return ((Displayable) node).getLabel();
			return super.getLabel(node);
		}
	};

	private TreeModelAdapter.OverflowListener overflowListener = new TreeModelAdapter.OverflowListener() {
		public void overflowOccured() {
			// setError(resources.getString("MemberNavigator.overflowOccurred"));
			setError("Too Many Members");
		}
	};

	/**
	 * sets the hierarchies for the members to choose from. Sets a default
	 * title.
	 * 
	 * @param hierarchies
	 * @param allowChangeOrder
	 */
	public void setHierarchies(Hierarchy[] hierarchies, boolean allowChangeOrder) {
		if (!isAvailable())
			return;
		HierarchyArray hierarchyArray = new HierarchyArray(hierarchies);
		TreeModel model = (TreeModel) models.get(hierarchyArray);
		if (model == null) {
			// build the decorator chain
			Locale locale = resources.getLocale();
			MemberTree memberTree = (MemberTree) olapModel
					.getExtension(MemberTree.ID);
			TreeModelAdapter modelAdapter = new TreeModelAdapter(hierarchies,
					memberTree, locale);
			modelAdapter.setOverflowListener(overflowListener);

			// the singleRecord level can not be opened because it contains
			// too many children
			for (int i = 0; i < hierarchies.length; i++) {
				if (OlapUtils.isSingleRecord(hierarchies[i])) {
					Level[] levels = hierarchies[i].getLevels();
					modelAdapter.setNoChildrenLevel(levels[0]);
				}
			}

			model = new CachingTreeModelDecorator(modelAdapter);
			if (lazyFetchChildren)
				model = new EnumBoundedTreeModelDecorator(model);

			// insert virtual groups for Non-Measures hierarchies
			if (hierarchies.length > 0
					&& !hierarchies[0].getDimension().isMeasure()) {
				model = new GroupingTreeModelDecorator(labelProvider, model,
						initialGrouping ? groupingMemberCount : 0);
			}

			// let the user change node position
			model = new MutableMemberTreeModelDecorator(model);

			// create and store the tree
			models.put(hierarchyArray, model);
		}

		super.setModel(model);
		MutableTreeModelDecorator mutableModel = (MutableTreeModelDecorator) findModel(MutableTreeModelDecorator.class);
		if (mutableModel != null) {
			mutableModel.setEnableChangeOrder(allowChangeOrder);
			super.setChangeOrderModel(mutableModel);
		}
		this.title = hierarchyArray.getLabel();
	}

	public void setHierarchies(Hierarchy[] hiers, boolean allowChangeOrder,
			MemberSelectionModel selection, Collection deleted) {
		setHierarchies(hiers, allowChangeOrder);
		if (lazyFetchChildren) {
			EnumBoundedTreeModelDecorator boundedModel = (EnumBoundedTreeModelDecorator) findModel(EnumBoundedTreeModelDecorator.class);
			// boundedModel.setVisible(selection.getOrderedSelection());
			boundedModel.setVisible(selection.getSelection());
			super.setBounding(boundedModel);
		}
		setSelectionModel(selection);
		if (expandSelected)
			expandSelected(false);
		Set set = getDeleteNodeModel().getDeleted();
		set.clear();
		set.addAll(deleted);
	}

	/**
	 * sets the hierarchy of members to choose from. Sets a default title.
	 */
	public void setHierarchy(Hierarchy hierarchy, boolean allowChangeOrder) {
		long startTime = System.currentTimeMillis();
		setHierarchies(new Hierarchy[] { hierarchy }, allowChangeOrder);
		logger.info("time taken to set hierachy of members in ms"
				+ (System.currentTimeMillis() - startTime));
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            The title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public void modelChanged(ModelChangeEvent e) {
		for (Iterator it = models.values().iterator(); it.hasNext();) {
			TreeModel model = (TreeModel) it.next();
			TreeModelAdapter tma = (TreeModelAdapter) findModel(model,
					TreeModelAdapter.class);
			tma.modelChanged();
		}
	}

	public void structureChanged(ModelChangeEvent e) {
		// clear all references
		logger.info("cleaning up");
		models.clear();
		super.setModel(null);
		super.getSelectionModel().clear();
	}

	/**
	 * shows "Select None" button in single selection mode
	 */
	public boolean isShowSelectNoneButton() {
		return showSelectNoneButton;
	}

	/**
	 * shows "Select None" button in single selection mode
	 */
	public void setShowSelectNoneButton(boolean b) {
		showSelectNoneButton = b;
	}

	/**
	 * Number of members of a non-selectable pseudo level
	 * 
	 * @return Returns the groupingMemberCount.
	 */
	public int getGroupingMemberCount() {
		return groupingMemberCount;
	}

	/**
	 * Number of members of a non-selectable pseudo level
	 * 
	 * @param groupingMemberCount
	 *            The groupingMemberCount to set.
	 */
	public void setGroupingMemberCount(int groupingMemberCount) {
		this.groupingMemberCount = groupingMemberCount;
	}

	public boolean isGrouping() {
		GroupingTreeModelDecorator groupingTreeModel = (GroupingTreeModelDecorator) findModel(GroupingTreeModelDecorator.class);
		if (groupingTreeModel != null)
			return groupingTreeModel.getLimit() > 0;
		return false;
	}

	public void setGrouping(boolean b) {
		GroupingTreeModelDecorator groupingTreeModel = (GroupingTreeModelDecorator) findModel(GroupingTreeModelDecorator.class);
		if (groupingTreeModel != null)
			groupingTreeModel.setLimit(b ? groupingMemberCount : 0);
	}

	private TreeModel findModel(Class clazz) {
		return findModel(getModel(), clazz);
	}

	private TreeModel findModel(TreeModel tm, Class clazz) {
		while (true) {
			if (tm == null)
				return null;
			if (clazz.isAssignableFrom(tm.getClass()))
				return tm;
			if (!(tm instanceof DecoratedTreeModel))
				return null;
			tm = ((DecoratedTreeModel) tm).getDecoree();
		}
	}

	/**
	 * returns true if the OlapModel supports all extensions that are required
	 * to use the MemberNavigator
	 */
	public boolean isAvailable() {
		return olapModel.getExtension(MemberTree.ID) != null;
	}

	public void setVisible(boolean b) {
		if (!isAvailable())
			b = false;
		super.setVisible(b);
	}

	public boolean isExpandSelected() {
		return expandSelected;
	}

	public void setExpandSelected(boolean expandSelected) {
		this.expandSelected = expandSelected;
	}

	public boolean isLazyFetchChildren() {
		return lazyFetchChildren;
	}

	public void setLazyFetchChildren(boolean lazyFetchChildren) {
		this.lazyFetchChildren = lazyFetchChildren;
	}

}