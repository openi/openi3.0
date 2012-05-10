package org.openi.olap.mondrian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.Position;
import com.tonbeller.jpivot.olap.model.Result;
import com.tonbeller.jpivot.olap.navi.PlaceMembersOnAxes;
import com.tonbeller.jpivot.olap.query.Quax;

/**
 * 
 * @author hh
 */
public class MondrianPlaceMembers extends MondrianPlaceHierarchies implements
		PlaceMembersOnAxes {

	/**
	 * Constructor for MondrianPlaceMembers.
	 */
	public MondrianPlaceMembers() {
		setId(PlaceMembersOnAxes.ID);
	}

	/**
	 * return List of Mondrian members
	 * 
	 * @see com.tonbeller.jpivot.olap.navi.PlaceMembersOnAxes#createMemberExpression(List)
	 */
	public Object createMemberExpression(List members) {
		ArrayList memberList = new ArrayList();
		for (Iterator iter = members.iterator(); iter.hasNext();) {
			MondrianMember mem = (MondrianMember) iter.next();
			memberList.add(mem.getMonMember());
		}
		return memberList;
	}

	/**
	 * find all members of an hierarchy
	 * 
	 * @see com.tonbeller.jpivot.olap.navi.PlaceMembersOnAxes#findVisibleMembers(Hierarchy)
	 */
	public List findVisibleMembers(Hierarchy hier) {
		List memberList = null;

		MondrianModel model = (MondrianModel) getModel();
		MondrianQueryAdapter adapter = (MondrianQueryAdapter) model
				.getQueryAdapter();

		// find the Quax for this hier
		Quax quax = adapter.findQuax(hier.getDimension());
		if (quax == null)
			return Collections.EMPTY_LIST; // should not occur

		int iDim = quax.dimIdx(hier.getDimension());

		// use result
		// problem: if NON EMPTY is on the axis then a member, which is excluded
		// by Non Empty,
		// will not be visible.
		// It would be possible to add it (again) to the axis, which must be
		// avoided

		Result res = null;
		memberList = new ArrayList();
		try {
			res = model.getResult();
		} catch (OlapException e) {
			e.printStackTrace();
			logger.error("findVisibleMembers: unexpected failure of getResult",
					e);
			return Collections.EMPTY_LIST;
		}

		// locate the appropriate result axis
		int iAx = quax.getOrdinal();
		if (adapter.isSwapAxes())
			iAx = (iAx + 1) % 2;
		Axis axis = res.getAxes()[iAx];
		List positions = axis.getPositions();
		for (Iterator iter = positions.iterator(); iter.hasNext();) {
			Position pos = (Position) iter.next();
			Member[] members = pos.getMembers();
			MondrianMember mem = (MondrianMember) members[iDim];
			if (mem != null && !memberList.contains(mem))
				memberList.add(mem);
		}

		return memberList;
	}

} // End MondrianPlaceMembers
