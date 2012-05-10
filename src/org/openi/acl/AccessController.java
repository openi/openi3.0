package org.openi.acl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.openi.util.plugin.PluginUtils;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;
import org.xml.sax.EntityResolver;

/**
 * 
 * @author SUJEN
 * 
 */
public class AccessController {

	private static Logger logger = Logger.getLogger(AccessController.class);

	private List<AccessRule> accessRules = new ArrayList<AccessRule>();

	public AccessController() {
		loadAccessRules();
	}

	private void loadAccessRules() {
		EntityResolver loader = new PentahoEntityResolver();
		Document aclXMLDoc = null;
		try {
			aclXMLDoc = XmlDom4JHelper.getDocFromFile(getACLXML(), loader);
			String aclXMLString = aclXMLDoc.asXML();
			aclXMLString = aclXMLString
					.replace("solution:", "file:"
							+ PentahoSystem.getApplicationContext()
									.getSolutionPath(""));
			aclXMLDoc = XmlDom4JHelper.getDocFromString(aclXMLString, loader);
			List<Node> ruleNodes = aclXMLDoc.selectNodes("/openiacl/rule");
			for (Node ruleNode : ruleNodes) {
				AccessRule accessRule = new AccessRule();

				String applyBy = "";
				String applyTo = "";
				String applicableItemType = "";
				String applicableItem = "";
				String access = "";

				Element ruleElement = (Element) ruleNode;
				List<Attribute> list = ruleElement.attributes();
				for (Attribute attribute : list) {
					String attrName = attribute.getName();
					if ("applyBy".equals(attrName)) {
						applyBy = attribute.getStringValue();
					} else if ("applyTo".equals(attrName)) {
						applyTo = attribute.getStringValue();
					} else if ("applicableItemType".equals(attrName)) {
						applicableItemType = attribute.getStringValue();
					} else if ("access".equals(attrName)) {
						access = attribute.getStringValue();
					}
				}

				accessRule.setApplyBy(applyBy.trim());
				List applyToList = accessRule.getApplyTo();

				String[] strArr = applyTo.split(",");
				for (String str : strArr) {
					applyToList.add(str.trim());
				}

				accessRule
						.setAccessRuleItemType(AccessRuleApplicableItemType.DATASOURCE);
				if (access.trim().equals("false"))
					accessRule.setAccess(false);
				else
					accessRule.setAccess(true);

				List applicableItems = accessRule.getRuleApplicableItems();

				int itemIndex = 0;
				List<Node> itemNodes = ruleNode
						.selectNodes("applicable-items-list/item-identifier");
				for (Node itemNode : itemNodes) {
					Element itemElem = (Element) itemNode;
					String itemIdentifier = itemElem.getStringValue();
					applicableItems.add(itemIdentifier.trim());
					itemIndex++;
				}
				// accessRule.setItems(items);
				accessRules.add(accessRule);
			}

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private File getACLXML() {
		File xmlFile = new File(PluginUtils.getPluginDir(),
				"acl/acl-config.xml");
		return xmlFile;
	}

	public boolean isItemAccesible(
			AccessRuleApplicableItemType accessRuleApplicableItemType,
			AccessRuleApplicableItem accessRuleItem) {
		boolean accessible = true;
		List<AccessRule> applicableRules = getApplicableAccessRules(
				accessRuleApplicableItemType, accessRuleItem);
		Iterator applicableRuleseItr = applicableRules.iterator();
		while (applicableRuleseItr.hasNext()) {
			AccessRule accessRule = (AccessRule) applicableRuleseItr.next();
			if (accessRule != null) {
				String accessRuleItemStr = accessRuleItem.toString();
				if (isInAccessRuleApplicableItemsList(accessRuleItemStr,
						accessRule))
					accessible = accessRule.isAccess();

			}
		}
		return accessible;
	}

	private boolean isInAccessRuleApplicableItemsList(String item,
			AccessRule accessRule) {
		boolean inList = false;
		List<String> accessRuleApplicableItems = accessRule
				.getRuleApplicableItems();
		Iterator<String> itr = accessRuleApplicableItems.iterator();
		while (itr.hasNext()) {
			String applicableItem = itr.next();
			Pattern pattern = Pattern.compile(applicableItem,
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(item);
			if (matcher.matches()) {
				inList = true;
				break;
			}
		}
		return inList;
	}

	private boolean isInAccessRuleApplyToList(String item, AccessRule accessRule) {
		boolean inList = false;
		List<String> accessRuleApplyToList = accessRule.getApplyTo();
		Iterator<String> itr = accessRuleApplyToList.iterator();
		while (itr.hasNext()) {
			String applyTo = itr.next();
			Pattern pattern = Pattern
					.compile(applyTo, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(item);
			if (matcher.matches()) {
				inList = true;
				break;
			}
		}
		return inList;
	}

	private List<AccessRule> getApplicableAccessRules(
			AccessRuleApplicableItemType accessRuleItemType,
			Object accessRuleItem) {
		String loggedInUser = getUserFromSession();
		GrantedAuthority[] authorities = getGrantedAuthorities();
		List<AccessRule> applicationAccessRules = new ArrayList<AccessRule>();

		Iterator accessRulesIterator = accessRules.iterator();
		while (accessRulesIterator.hasNext()) {
			AccessRule accesRule = (AccessRule) accessRulesIterator.next();
			String applyBy = accesRule.getApplyBy();
			List applyTo = accesRule.getApplyTo();

			if (applyBy.equalsIgnoreCase("ROLE")) {
				if (authorities != null) {
					for (int i = 0; i < authorities.length; i++) {
						String authority = authorities[i].getAuthority();
						if (isInAccessRuleApplyToList(authority, accesRule)) {
							applicationAccessRules.add(accesRule);
							break;
						}
					}

				}
			} else if (applyBy.equalsIgnoreCase("USER")) {
				if (isInAccessRuleApplyToList(loggedInUser, accesRule))
					applicationAccessRules.add(accesRule);
			}
		}

		return applicationAccessRules;
	}

	public enum AccessRuleApplicableItemType {
		DATASOURCE
	}

	private String getUserFromSession() {
		String loggedInUserName = "";
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		if (userDetails != null) {
			loggedInUserName = userDetails.getUsername();
		}
		return loggedInUserName;
	}

	private GrantedAuthority[] getGrantedAuthorities() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		return userDetails.getAuthorities();
	}
}
