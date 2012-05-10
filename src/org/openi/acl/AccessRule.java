package org.openi.acl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openi.acl.AccessController.AccessRuleApplicableItemType;

/**
 * 
 * @author SUJEN
 *
 */
public class AccessRule implements Serializable {
	private String applyBy;
	private List<String> applyTo = new ArrayList<String>();
	private AccessRuleApplicableItemType ruleApplicableItemType;
	private List<String> ruleApplicableItems = new ArrayList<String>();
	private boolean access = true;

	public String getApplyBy() {
		return applyBy;
	}

	public void setApplyBy(String applyBy) {
		this.applyBy = applyBy;
	}

	public List<String> getApplyTo() {
		return applyTo;
	}

	public void setApplyTo(List<String> applyTo) {
		this.applyTo = applyTo;
	}

	public AccessRuleApplicableItemType getAccessRuleItemType() {
		return ruleApplicableItemType;
	}

	public void setAccessRuleItemType(
			AccessRuleApplicableItemType accessRuleItemType) {
		this.ruleApplicableItemType = accessRuleItemType;
	}

	public List<String> getRuleApplicableItems() {
		return ruleApplicableItems;
	}

	public void setRuleApplicableItems(List<String> ruleApplicableItems) {
		this.ruleApplicableItems = ruleApplicableItems;
	}

	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}
}
