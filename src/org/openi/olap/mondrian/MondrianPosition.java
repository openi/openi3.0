package org.openi.olap.mondrian;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tonbeller.jpivot.olap.query.PositionBase;

/**
 * MondrianPosition is an adapter class for the Mondrian Position.
 */
public class MondrianPosition extends PositionBase {

	mondrian.olap.Position monPosition;
	MondrianModel model;
	private int iAxis; // Axis ordinal for result axis

	/**
	 * Constructor create the array of members
	 * 
	 * @param monPosition
	 *            corresponding Mondrian Position
	 * @param model
	 *            MondrianModel
	 */
	MondrianPosition(mondrian.olap.Position monPosition, int iAxis,
			MondrianModel model) {
		super();
		this.monPosition = monPosition;
		this.model = model;
		this.iAxis = iAxis;
		// extract the members
		List l = new ArrayList();
		Iterator mit = monPosition.iterator();
		while (mit.hasNext()) {
			mondrian.olap.Member monMember = (mondrian.olap.Member) mit.next();
			l.add(model.lookupMemberByUName(monMember.getUniqueName()));
		}
		members = (MondrianMember[]) l.toArray(new MondrianMember[l.size()]);
	}

	/**
	 * get the Mondrian Members for this Axis Position
	 * 
	 * @return Array of Mondrian members mondrian.olap.Member[] getMonMembers()
	 *         { this is not used anywhere return monPosition.getMembers(); }
	 */

	/**
	 * Returns the iAxis.
	 * 
	 * @return int
	 */
	int getAxis() {
		return iAxis;
	}

} // MondrianPosition
