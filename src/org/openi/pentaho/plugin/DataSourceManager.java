package org.openi.pentaho.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openi.datasource.Datasource;
import org.openi.datasource.DatasourceType;
import org.openi.datasource.MondrianDatasource;
import org.openi.datasource.XMLADatasource;
import org.openi.util.olap.MondrianHelper;
import org.openi.util.plugin.PluginUtils;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;

/**
 * loads the xmla and mondrian datasources into map when it's loaded
 * 
 * @author SUJEN
 * 
 */
public class DataSourceManager {

	private static Logger logger = Logger.getLogger(DataSourceManager.class);
	
	Map<String, MondrianDatasource> mondrianDatasources = new HashMap<String, MondrianDatasource>();
	Map<String, XMLADatasource> xmlaDatasources = new HashMap<String, XMLADatasource>();
	
	public Map<String, MondrianDatasource> getMondrianDatasources() {
		if(this.mondrianDatasources == null || this.mondrianDatasources.size() <= 0)
			loadMondrianDS();
		return this.mondrianDatasources;
	}

	public void setMondrianDatasources(
			Map<String, MondrianDatasource> mondrianDatasources) {
		this.mondrianDatasources = mondrianDatasources;
	}

	public Map<String, XMLADatasource> getXmlaDatasources() {
		if(this.xmlaDatasources == null || this.xmlaDatasources.size() <= 0)
			loadXMLADS();
		return this.xmlaDatasources;
	}

	public void setXmlaDatasources(Map<String, XMLADatasource> xmlaDatasources) {
		this.xmlaDatasources = xmlaDatasources;
	}

	public DataSourceManager() {
		//loadDS();
	}

	private void loadDS() {
		loadMondrianDS();
		loadXMLADS();
	}

	private void loadMondrianDS() {
		mondrianDatasources.clear();
		/*
		EntityResolver loader = new PentahoEntityResolver();
		Document doc = null;
		try {
			doc = XmlDom4JHelper.getDocFromFile(getDatasourcesXML(DatasourceType.MONDRIAN), loader);
			String modified = doc.asXML();
			modified = modified
					.replace("solution:", "file:"
							+ PentahoSystem.getApplicationContext()
									.getSolutionPath(""));
			doc = XmlDom4JHelper.getDocFromString(modified, loader);

			List<Node> nodes = doc.selectNodes("/DataSources/DataSource/Catalogs/Catalog"); //$NON-NLS-1$
			int nr = 0;
			for (Node node : nodes) {
				nr++;
				
				MondrianDatasource mondrianDS = new MondrianDatasource();
				String name = "";
				Element e = (Element) node;
				List<Attribute> list = e.attributes();
				for (Attribute attribute : list) {
					String aname = attribute.getName();
					if ("name".equals(aname)) {
						name = attribute.getStringValue();
					}
				}

				mondrianDS.setName(name);
				
				Node ds = node.selectSingleNode("DataSourceInfo");
				Node cat = node.selectSingleNode("Definition");
				
				String dataSourceInfo = ds.getStringValue();
				String definition = cat.getStringValue();
				mondrianDS.setCatalogUri(definition);
				addDatasource(DatasourceType.MONDRIAN, mondrianDS);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		*/
		
		IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, PentahoSessionHolder.getSession());
		List<MondrianCatalog> mondrianCatalogs = mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true);
		
		if(mondrianCatalogs != null) {
			Iterator<MondrianCatalog> catalogsItr = mondrianCatalogs.iterator();
			while(catalogsItr.hasNext()) {
				MondrianCatalog catalog = catalogsItr.next();
				addDatasource(DatasourceType.MONDRIAN, new MondrianDatasource(catalog.getName(), catalog));
			}
			
		}
	}

	private void loadXMLADS() {
		xmlaDatasources.clear();
		EntityResolver loader = new PentahoEntityResolver();
		Document doc = null;
		try {
			doc = XmlDom4JHelper.getDocFromFile(getDatasourcesXML(DatasourceType.XMLA), loader);
			String modified = doc.asXML();
			modified = modified
					.replace("solution:", "file:"
							+ PentahoSystem.getApplicationContext()
									.getSolutionPath(""));
			doc = XmlDom4JHelper.getDocFromString(modified, loader);

			List<Node> nodes = doc.selectNodes("/DataSources/DataSource");
			for (Node node : nodes) {
				XMLADatasource xmlaDatasource = new XMLADatasource();
				String name = "";
				Element e = (Element) node;
				List<Attribute> list = e.attributes();
				for (Attribute attribute : list) {
					String aname = attribute.getName();
					if ("name".equals(aname)) {
						name = attribute.getStringValue();
					}
				}

				xmlaDatasource.setName(name);
				
				Node serverURL = node.selectSingleNode("Server");
				//Node datasourceName = node.selectSingleNode("DatasourceName");
				Node catalog = node.selectSingleNode("Catalog");
				Node username = node.selectSingleNode("Username");
				Node password = node.selectSingleNode("Password");
				
				xmlaDatasource.setServerURL(serverURL.getStringValue());
				//xmlaDatasource.setDatasourceName(datasourceName.getStringValue());
				xmlaDatasource.setCatalog(catalog.getStringValue());
				xmlaDatasource.setUsername(username.getStringValue());
				xmlaDatasource.setPassword(password.getStringValue());
				
				addDatasource(DatasourceType.XMLA, xmlaDatasource);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addDatasource(DatasourceType dsType, Datasource ds) {
		if (dsType == DatasourceType.MONDRIAN) {
			addMondrianDS((MondrianDatasource) ds);
		} else if (dsType == DatasourceType.XMLA) {
			addXMLADS((XMLADatasource) ds);
		}
	}

	public void addXMLADS(XMLADatasource ds) {
		if (!xmlaDatasources.containsKey(ds.getName()))
			xmlaDatasources.put(ds.getName(), ds);
	}

	public void addMondrianDS(MondrianDatasource ds) {
		if (!mondrianDatasources.containsKey(ds.getName()))
			mondrianDatasources.put(ds.getName(), ds);
	}
	
	private File getDatasourcesXML(DatasourceType dsType) {
		File xmlFile = null;
		if (dsType == DatasourceType.MONDRIAN) {
			xmlFile = new File(PentahoSystem.getApplicationContext()
					.getSolutionPath("system/olap/datasources.xml"));
			if(logger.isInfoEnabled())
				logger.info("Loaded MONDRIAN datasources ");
		} else if (dsType == DatasourceType.XMLA) {
			xmlFile = new File(PluginUtils.getPluginDir(), "olap/datasources.xml");
			if(logger.isInfoEnabled())
				logger.info("Loaded XMLA datasources ");
		}
		return xmlFile;
	}

}
