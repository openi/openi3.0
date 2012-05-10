package org.openi.navigator.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.navi.MemberDeleter;
import com.tonbeller.wcf.catedit.Category;
import com.tonbeller.wcf.catedit.Item;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * AxisCategory or SlicerCategory. Contains a list of HierInfo's. Represents a
 * Query Axis, either visible (row, column) or slicer.
 * 
 * @author av
 * @author SUJEN
 * 
 */
public abstract class AbstractCategory implements Category {
	HierarchyNavigator navi;
	String name;
	String icon;
	boolean dirty;
	List items = new ArrayList();

	/**
	 * ctor for visible axis
	 */
	public AbstractCategory(HierarchyNavigator navi, String name, String icon) {
		this.navi = navi;
		this.name = name;
		this.icon = icon;
	}

	/**
	 * @see com.tonbeller.jpivot.core.Displayable#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the items.
	 * 
	 * @return List
	 */
	public List getItems() {
		return Collections.unmodifiableList(items);
	}

	public void addItem(Item item) {
		HierarchyItem hi = (HierarchyItem) item;
		// append the hierarchy at the last position,
		// this makes opening a node of the new hierarchy cheaper
		// because there is nothing right of it.
		// items.add(0, hi);
		items.add(hi);
		hi.setCategory(this);
		setDirty(true);
	}

	public void changeOrder(List items) {
		this.items.clear();
		this.items.addAll(items);
		setDirty(true);
	}

	public void removeItem(Item item) {
		items.remove(item);
		setDirty(true);
	}

	/**
	 * @see com.tonbeller.wcf.catedit.Category#getIcon()
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Returns the dirty.
	 * 
	 * @return boolean
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Sets the dirty to true
	 * 
	 * @param dirty
	 *            The dirty to set
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		if (dirty)
			navi.setEditing(true);
	}

	HierarchyNavigator getNavigator() {
		return navi;
	}

	/**
	 * user has clicked on the hyperlink for item
	 */
	public abstract void itemClicked(RequestContext context, HierarchyItem item);

	/**
	 * simulates an itemClick for the hierarchy
	 */
	public HierarchyItem findItemFor(Hierarchy hier) {
		Iterator it = items.iterator();
		while (it.hasNext()) {
			HierarchyItem item = (HierarchyItem) it.next();
			if (hier.equals(item.getHierarchy())) {
				return item;
			}
		}
		return null;
	}

	/**
	 * called before applyChanges to query the needed MemberExpressions
	 */
	abstract void prepareApplyChanges();

	/**
	 * called after prepareApplyChanges to apply the MemberExpressions
	 */
	abstract void applyChanges();

	abstract void setSelection(HierarchyItem item, Collection selection);

	abstract String validateSelection(HierarchyItem item, Collection selection);

	public abstract boolean isSlicer();

	/**
	 * removes the deleted items from the model
	 */
	public void deleteDeleted() {
		MemberDeleter md = navi.getDeleterExtension();
		if (md == null)
			return;
		Iterator it = items.iterator();
		while (it.hasNext()) {
			HierarchyItem item = (HierarchyItem) it.next();
			for (Iterator mi = item.getDeleted().iterator(); mi.hasNext();) {
				md.delete((Member) mi.next());
			}
		}
	}

}
