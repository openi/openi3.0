package org.openi.navigator.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapUtils;
import com.tonbeller.jpivot.olap.navi.ChangeSlicer;
import com.tonbeller.jpivot.olap.navi.PlaceMembersOnAxes;
import com.tonbeller.wcf.catedit.Item;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestListener;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * Wraps a hierarchy for HierarchyNavigator. Contains two selections, one
 * multiple selection for axis view and another one, single selection for slicer
 * view. If the item is moved around between axis and slicer category it does
 * not forget its selection.
 * 
 * @author av
 * @author SUJEN
 * 
 */
public class HierarchyItem implements Item, RequestListener, Comparable {

	private static final Logger logger = Logger.getLogger(HierarchyItem.class);

	// the GUI component
	private HierarchyNavigator navigator;

	// one of rows, columns or slicer
	private AbstractCategory category;

	// the hierarchy this item is representing
	private Hierarchy hierarchy;

	// the selection in case this is contained in axis category
	private List axisSelection;
	private boolean axisSelectionDirty;

	// the selection in case this is contained in slicer category
	private List slicerSelection;
	private boolean slicerSelectionDirty;

	// list of (calculated) members that are about to be deleted in this
	// hierarchy
	private List deleted = new ArrayList();

	// some expression to be placed on the axis instead of a hierarchy or
	// selection
	private Object expression;

	private String id = DomUtils.randomId();

	private Dimension dimension;

	public String getId() {
		return id;
	}

	public HierarchyItem(AbstractCategory category, Hierarchy hierarchy) {
		this.category = category;
		this.hierarchy = hierarchy;
		this.dimension = hierarchy.getDimension();
		this.navigator = category.getNavigator();
		navigator.getTempDispatcher().addRequestListener(id, null, this);
	}

	void initializeSlicerSelection() {
		ChangeSlicer slicerExtension = navigator.getSlicerExtension();
		slicerSelection = new ArrayList();
		if (slicerExtension != null) {
			Member[] members = slicerExtension.getSlicer();
			loop: for (int i = 0; i < members.length; i++) {
				if (members[i].getLevel().getHierarchy().equals(hierarchy)) {
					slicerSelection.add(members[i]);
					break loop;
				}
			}
		}
	}

	void initializeAxisSelection() {
		PlaceMembersOnAxes memberExtension = navigator.getMemberExtension();
		axisSelection = new ArrayList();
		if (memberExtension != null) {
			List members = memberExtension.findVisibleMembers(hierarchy);
			axisSelection.addAll(members);
		}
	}

	private void clear() {
		axisSelection = null;
		slicerSelection = null;
		deleted.clear();
		expression = null;
	}

	/**
	 * this item has been moved from one category to another. e.g., this
	 * hierarchy has been moved from rows to filters.
	 */
	public void setCategory(AbstractCategory category) {
		this.category = category;
	}

	public AbstractCategory getCategory() {
		return category;
	}

	/**
	 * Returns the current hierarchy.
	 * 
	 * @return Hierarchy
	 */
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	/**
	 * returns the Dimension of this HierarchyItem
	 */
	public Dimension getDimension() {
		return dimension;
	}

	public String getLabel() {
		return hierarchy.getLabel();
	}

	/**
	 * called when the user clicks on this item.
	 */
	public void request(RequestContext context) throws Exception {
		category.itemClicked(context, this);
	}

	/**
	 * Returns the axisSelection.
	 * 
	 * @return Set
	 */
	public List getAxisSelection() {
		if (axisSelection == null)
			initializeAxisSelection();
		return axisSelection;
	}

	/**
	 * Returns the slicerSelection.
	 * 
	 * @return Set
	 */
	public List getSlicerSelection() {
		if (slicerSelection == null)
			initializeSlicerSelection();
		return slicerSelection;
	}

	/**
	 * Sets the axisSelection.
	 * 
	 * @param axisSelection
	 *            The axisSelection to set
	 */
	public void setAxisSelection(Collection selection) {
		if (selection.equals(axisSelection)) {
			// Nothing has changed, just return
			return;
		}
		clear();
		updateHierarchy(selection);
		if (axisSelection == null)
			axisSelection = new ArrayList();
		else
			axisSelection.clear();
		axisSelection.addAll(selection);
		axisSelectionDirty = true;
		category.setDirty(true);
		expression = null;
	}

	/**
	 * Sets the slicerSelection.
	 * 
	 * @param slicerSelection
	 *            The slicerSelection to set
	 */
	public void setSlicerSelection(Collection selection) {
		clear();
		updateHierarchy(selection);
		if (slicerSelection == null)
			slicerSelection = new ArrayList();
		else
			slicerSelection.clear();
		slicerSelection.addAll(selection);
		slicerSelectionDirty = true;
		category.setDirty(true);
		expression = null;
	}

	private void updateHierarchy(Collection selection) {
		if (selection == null || selection.isEmpty())
			hierarchy = dimension.getHierarchies()[0];
		else {
			Member m = (Member) selection.iterator().next();
			hierarchy = m.getLevel().getHierarchy();
			if (!hierarchy.getDimension().equals(dimension))
				logger.error("invalid dimension in " + hierarchy.getLabel());
		}

	}

	/**
	 * Returns the axisSelectionDirty.
	 * 
	 * @return boolean
	 */
	public boolean isAxisSelectionDirty() {
		return axisSelectionDirty;
	}

	/**
	 * Returns the slicerSelectionDirty.
	 * 
	 * @return boolean
	 */
	public boolean isSlicerSelectionDirty() {
		return slicerSelectionDirty;
	}

	/**
	 * Sets the axisSelectionDirty.
	 * 
	 * @param axisSelectionDirty
	 *            The axisSelectionDirty to set
	 */
	public void setAxisSelectionDirty(boolean axisSelectionDirty) {
		this.axisSelectionDirty = axisSelectionDirty;
	}

	/**
	 * Sets the slicerSelectionDirty.
	 * 
	 * @param slicerSelectionDirty
	 *            The slicerSelectionDirty to set
	 */
	public void setSlicerSelectionDirty(boolean slicerSelectionDirty) {
		this.slicerSelectionDirty = slicerSelectionDirty;
	}

	public void setSelection(Collection selection) {
		category.setSelection(this, selection);
	}

	/**
	 * validates the selection.
	 * 
	 * @param selection
	 * @return null on success, error message on error
	 */
	public String validateSelection(Collection selection) {
		return category.validateSelection(this, selection);
	}

	/**
	 * lexical compare for GUI lists
	 */
	public int compareTo(Object arg) {
		HierarchyItem that = (HierarchyItem) arg;
		return this.hierarchy.getLabel().compareTo(
				that.getHierarchy().getLabel());
	}

	/**
	 * removes deleted members from the selections.
	 */
	public void removeFromSelection(Set deleted) {
		if (axisSelection != null)
			axisSelection.removeAll(deleted);
		if (slicerSelection != null)
			slicerSelection.removeAll(deleted);
	}

	/**
	 * the collection of members that shall be deleted from the query. (i.e.
	 * remove calculated members)
	 */
	public Collection getDeleted() {
		return deleted;
	}

	/**
	 * the collection of members that shall be deleted from the query. (i.e.
	 * remove calculated members)
	 */
	public void setDeleted(Collection c) {
		deleted.clear();
		deleted.addAll(c);
	}

	public Object getExpression() {
		return expression;
	}

	public void setExpression(Object object) {
		clear();
		expression = object;
		category.setDirty(true);
	}

	public boolean isMovable() {
		return !OlapUtils.isSingleRecord(hierarchy);
	}

	public boolean isClickable() {
		return navigator.getHierarchyItemClickHandler() != null;
	}

}
