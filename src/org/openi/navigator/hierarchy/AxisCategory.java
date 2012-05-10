package org.openi.navigator.hierarchy;

import java.util.Collection;
import java.util.Iterator;

import org.openi.Resources;
import org.openi.navigator.member.MemberSelectionModel;
import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.navi.PlaceHierarchiesOnAxes;
import com.tonbeller.jpivot.olap.navi.PlaceMembersOnAxes;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.selection.SelectionModel;

/**
 * represents a visible query axis.
 * 
 * @author av
 * @author SUJEN
 */
class AxisCategory extends AbstractCategory {

	// AVTASK the axis may have changed if a ModelChangeEvent was received.
	// This means, that we are calling the navigation code with an invalid
	// axis object that is not part of the current result.
	Axis axis;

	/**
	 * ctor for visible axis
	 */
	public AxisCategory(HierarchyNavigator navi, Axis axis, String name,
			String icon) {
		super(navi, name, icon);
		this.axis = axis;

		Hierarchy[] hiers = axis.getHierarchies();
		for (int i = 0; i < hiers.length; i++) {
			Hierarchy hier = hiers[i];
			HierarchyItem hi = new HierarchyItem(this, hier);
			items.add(hi);
		}
	}

	/**
	 * calls HierarchyNavigator.hierarchyItemClicked with the appropriate
	 * selection model
	 */
	public void itemClicked(RequestContext context, HierarchyItem item) {
		// create a selection model
		MemberSelectionModel selection = new MemberSelectionModel();
		if (navi.getMemberExtension() == null)
			selection.setMode(SelectionModel.NO_SELECTION);
		else
			selection.setMode(SelectionModel.MULTIPLE_SELECTION_BUTTON);

		selection.setOrderedSelection(item.getAxisSelection());
		navi.itemClicked(context, item, selection, true);
	}

	void setSelection(HierarchyItem item, Collection selection) {
		item.setAxisSelection(selection);
	}

	String validateSelection(HierarchyItem item, Collection selection) {
		if (selection.size() < 1)
			return Resources.getString("selection.mustSelectOneOrMore");
		Hierarchy hier = null;
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Member m = (Member) it.next();
			Hierarchy mh = m.getLevel().getHierarchy();
			if (hier == null)
				hier = mh;
			else {
				if (!hier.equals(mh))
					return Resources.getString("selection.multipleHierarchies");
			}
		}
		return null;
	}

	/**
	 * returns true;
	 */
	public boolean isOrderSignificant() {
		return true;
	}

	Object[] memberExpressions;

	void prepareApplyChanges() {
		if (!isDirty())
			return;
		setDirty(false);

		PlaceHierarchiesOnAxes hierExtension = navi.getHierarchyExtension();
		PlaceMembersOnAxes memberExtension = navi.getMemberExtension();
		if (hierExtension == null)
			return;

		memberExpressions = new Object[getItems().size()];
		Iterator it = getItems().iterator();
		for (int i = 0; i < memberExpressions.length; i++) {
			HierarchyItem hi = (HierarchyItem) it.next();
			if (hi.getExpression() != null)
				memberExpressions[i] = hi.getExpression();
			else if (hi.isAxisSelectionDirty() && memberExtension != null)
				memberExpressions[i] = memberExtension
						.createMemberExpression(hi.getAxisSelection());
			else
				memberExpressions[i] = hierExtension.createMemberExpression(hi
						.getHierarchy());
		}
	}

	void applyChanges() {
		PlaceHierarchiesOnAxes hierExtension = navi.getHierarchyExtension();
		if (hierExtension == null)
			return;
		if (memberExpressions != null) {
			hierExtension.setQueryAxis((Axis) axis.getRootDecoree(),
					memberExpressions);
			memberExpressions = null;
		}
	}

	public boolean isEmptyAllowed() {
		return false;
	}

	public boolean isSlicer() {
		return false;
	}

}
