package org.openi.navigator.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openi.navigator.member.MemberSelectionModel;
import com.tonbeller.jpivot.olap.model.Hierarchy;
//import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapUtils;
import com.tonbeller.jpivot.olap.navi.ChangeSlicer;
import com.tonbeller.jpivot.olap.query.MDXElement;
import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.catedit.Item;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.selection.SelectionModel;

/**
 * @author av
 * @author SUJEN
 * 
 */
class SlicerCategory extends AbstractCategory {
	private Logger logger = Logger.getLogger(SlicerCategory.class);
	
	public SlicerCategory(HierarchyNavigator navi, String name, String icon)
			throws OlapException {
		super(navi, name, icon);

		/*
		 * Take active hierarchies instead of active dimensions, to remember the
		 * slicer on a hierarchy that is not the default (first)
		 */
		Set slicerHiers = OlapUtils.getSlicerHierarchies(navi.getOlapModel());
		for (Iterator it = slicerHiers.iterator(); it.hasNext();) {
			Hierarchy hier = (Hierarchy) it.next();
			HierarchyItem hi = new HierarchyItem(this, hier);
			items.add(hi);
		}

		Collections.sort(items);
	}

	/**
	 * calls HierarchyNavigator.itemClicked with the appropriate selection model
	 */
	public void itemClicked(RequestContext context, HierarchyItem item) {
		// create a selection model
		MemberSelectionModel selection = new MemberSelectionModel();
		if (navi.getSlicerExtension() == null)
			selection.setMode(SelectionModel.NO_SELECTION);
		else {
			//selection.setMode(SelectionModel.SINGLE_SELECTION_BUTTON);
			selection.setMode(SelectionModel.MULTIPLE_SELECTION_BUTTON);
		}

		selection.setOrderedSelection(item.getSlicerSelection());
		navi.itemClicked(context, item, selection, false);
	}

	void setSelection(HierarchyItem item, Collection selection) {
		item.setSlicerSelection(selection);
	}

	public boolean isOrderSignificant() {
		return false;
	}

	void prepareApplyChanges() {
	}

	void applyChanges() {
		if (!isDirty())
			return;
		setDirty(false);

		ChangeSlicer slicerExtension = navi.getSlicerExtension();
		if (slicerExtension == null)
			return;

		List memberList = new ArrayList();
		for (Iterator it = items.iterator(); it.hasNext();) {
			HierarchyItem hi = (HierarchyItem) it.next();
			memberList.addAll(hi.getSlicerSelection());
		}
		
		Member[] memberArr = (Member[]) memberList
				.toArray(new Member[memberList.size()]);
		slicerExtension.setSlicer(memberArr);
	}

	public boolean isEmptyAllowed() {
		return true;
	}

	String validateSelection(HierarchyItem item, Collection selection) {
		/*if (selection.size() > 1) {
			Resources res = getNavigator().getRes();
			return res.getString("selection.mustSelectOneOrLess");
		}*/
		return null;
	}

	/**
	 * adds an item and sorts the list
	 */
	public void addItem(Item item) {
		super.addItem(item);
		Collections.sort(items);
	}

	public boolean isSlicer() {
		return true;
	}

}
