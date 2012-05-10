package org.openi.olap.mondrian;

import java.io.Serializable;

import com.tonbeller.jpivot.olap.query.*;

/**
 * Java Bean object to hold the state of MondrianQuax. Used for serialization of
 * MondrianModel via MondrianMemento. Referenced by MondrianMemento.
 * 
 */
public class MondrianQuaxBean extends QuaxBean implements Serializable {

	// old stuff
	MondrianMemberSetBean[] memberSets = null; // Member sets, used in Qubon
												// mode
	MondrianDrillExBean[] drillExes; // List of drill expansions, used in normal
										// mode

	// HHTASK : support Non Empty

	/**
	 * Set drillExes.
	 * 
	 * @param drillExes
	 *            Used by old memento (MDX Generation version 2) HHTASK: remove,
	 *            when old Bookmarks are replaced
	 */
	public void setDrillExes(MondrianDrillExBean[] drillExes) {
		this.drillExes = drillExes;
	}

	/**
	 * Get drillExes.
	 * 
	 * @return drillExes Used by old memento (MDX Generation version 2) HHTASK:
	 *         remove, when old Bookmarks are replaced
	 */
	public MondrianDrillExBean[] getDrillExes() {
		if (drillExes != null)
			return drillExes;
		else
			return new MondrianDrillExBean[0];
	}

	/**
	 * Get memberSets.
	 * 
	 * @return memberSets Used by old memento (MDX Generation version 2) HHTASK:
	 *         remove, when old Bookmarks are replaced
	 */
	public MondrianMemberSetBean[] getMemberSets() {
		if (memberSets != null)
			return memberSets;
		else
			return new MondrianMemberSetBean[0];
	}

	/**
	 * Set memberSets.
	 * 
	 * @param memberSets
	 *            Used by old memento (MDX Generation version 2) HHTASK: remove,
	 *            when old Bookmarks are replaced
	 */
	public void setMemberSets(MondrianMemberSetBean[] memberSets) {
		this.memberSets = memberSets;
	}

} // End MondrianQuaxBean
