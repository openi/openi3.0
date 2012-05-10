package org.openi.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openi.acl.AccessController;
import org.openi.acl.AccessRuleApplicableItem;
import org.openi.acl.AccessRuleApplicableItemFilter;
import org.openi.acl.AccessController.AccessRuleApplicableItemType;
import org.openi.acl.AccessDeniedException;
import org.openi.datasource.Datasource;
import org.openi.pentaho.plugin.DataSourceManager;
import org.openi.datasource.DatasourceType;

/**
 * service layer class to be find the appropriate datasource for making olap
 * connection and initiliazing xmla/mondrian model
 * 
 * @author SUJEN
 * 
 */
public class DatasourceService {

	private DataSourceManager dsManager;

	public DataSourceManager getDsManager() {
		return dsManager;
	}

	public void setDsManager(DataSourceManager dsManager) {
		this.dsManager = dsManager;
	}

	private AccessController accessController;

	public AccessController getAccessController() {
		return accessController;
	}

	public void setAccessController(AccessController accessController) {
		this.accessController = accessController;
	}

	/**
	 * 
	 * @param datasourceName
	 * @param dsType
	 * @return Datasource
	 * @throws AccessDeniedException
	 */
	public Datasource getDatasource(String datasourceName, DatasourceType dsType)
			throws AccessDeniedException {
		Datasource ds = null;
		if (dsType == DatasourceType.MONDRIAN)
			ds = (Datasource) dsManager.getMondrianDatasources().get(
					datasourceName);
		else if (dsType == DatasourceType.XMLA)
			ds = (Datasource) dsManager.getXmlaDatasources()
					.get(datasourceName);
		if (!accessController.isItemAccesible(
				AccessRuleApplicableItemType.DATASOURCE, ds))
			throw new AccessDeniedException("Access denied for datasource "
					+ datasourceName);
		return ds;
	}

	/**
	 * 
	 * @param dsType
	 * @return
	 */
	public List<Datasource> getDatasources(DatasourceType dsType) {
		List<Datasource> datasources = new ArrayList();
		if (dsType == DatasourceType.MONDRIAN) {
			Map mondrianDatasources = dsManager.getMondrianDatasources();
			if (mondrianDatasources != null) {
				Iterator itr = mondrianDatasources.keySet().iterator();
				while (itr.hasNext()) {
					Datasource ds = (Datasource) mondrianDatasources.get(itr
							.next());
					datasources.add(ds);
				}
			}
		} else if (dsType == DatasourceType.XMLA) {
			Map xmlaDatasources = dsManager.getXmlaDatasources();
			if (xmlaDatasources != null) {
				Iterator itr = xmlaDatasources.keySet().iterator();
				while (itr.hasNext()) {
					Datasource ds = (Datasource) xmlaDatasources
							.get(itr.next());
					datasources.add(ds);
				}
			}
		}
		return filterByACL(AccessRuleApplicableItemType.DATASOURCE, datasources);
	}

	/**
	 * 
	 * @param dsType
	 * @return
	 */
	public List<Datasource> getAllDatasources() {
		List<Datasource> datasources = new ArrayList();
		Map mondrianDatasources = dsManager.getMondrianDatasources();
		if (mondrianDatasources != null) {
			Iterator itr = mondrianDatasources.keySet().iterator();
			while (itr.hasNext()) {
				datasources
						.add((Datasource) mondrianDatasources.get(itr.next()));
			}
		}
		Map xmlaDatasources = dsManager.getXmlaDatasources();
		if (xmlaDatasources != null) {
			Iterator itr = xmlaDatasources.keySet().iterator();
			while (itr.hasNext()) {
				datasources.add((Datasource) xmlaDatasources.get(itr.next()));
			}
		}
		return datasources;
	}

	private List<Datasource> filterByACL(
			AccessRuleApplicableItemType ruleapplicableItemType,
			List<Datasource> datasources) {
		List filteredItems = new ArrayList();
		if (datasources != null) {
			Iterator<Datasource> itr = datasources.iterator();
			while (itr.hasNext()) {
				AccessRuleApplicableItem currItem = itr.next();
				if (new AccessRuleApplicableItemFilter() {
					@Override
					public boolean isItemAccessible(
							AccessRuleApplicableItemType itemType,
							AccessRuleApplicableItem item) {
						return accessController.isItemAccesible(itemType, item);
					}
				}.isItemAccessible(ruleapplicableItemType, currItem))
					filteredItems.add(currItem);
			}
		}
		return filteredItems;
	}
}
