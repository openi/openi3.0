package org.openi.acl;

import org.openi.acl.AccessController.AccessRuleApplicableItemType;

/**
 * 
 * @author SUJEN
 *
 */
public interface AccessRuleApplicableItemFilter {
	public boolean isItemAccessible(AccessRuleApplicableItemType itemType, AccessRuleApplicableItem item);
}
