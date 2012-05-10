package org.openi.olap.mondrian;

import java.io.Serializable;

/**
 * Java Bean object to hold the state of MondrianDrillEx. Used for serialization
 * of MondrianModel via MondrianMemento. Referenced by MondrianQuaxBean.
 * 
 * Used by old memento (MDX Generation version 2) HHTASK: remove, when old
 * Bookmarks are replaced
 */

public class MondrianDrillExBean implements Serializable {
	String[] pathMembers;

	/**
	 * Get pathMembers.
	 * 
	 * @return pathMembers
	 */
	public String[] getPathMembers() {
		return pathMembers;
	}

	/**
	 * Set pathMembers.
	 * 
	 * @param pathMembers
	 *            String array of unique names
	 */
	public void setPathMembers(String[] pathMembers) {
		this.pathMembers = pathMembers;
	}

} // End MondrianDrillExBean
