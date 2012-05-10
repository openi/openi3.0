package org.openi.olap.mondrian;

import java.util.EventListener;

/**
 * @param quax
 *            the Quax being changed
 * @param source
 *            the initiator object of the change
 * @param changedMemberSet
 *            true if the member set was changed by the navigator
 */
public interface MondrianQuaxChangeListener extends EventListener {
	void quaxChanged(MondrianQuax quax, Object source, boolean changedMemberSet);
}
