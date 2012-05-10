package org.openi.util.olap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import mondrian.olap.Connection;
import mondrian.olap.Cube;
import mondrian.olap.Dimension;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.Schema;
import mondrian.olap.Util;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.util.Pair;

import org.apache.log4j.Logger;
import org.openi.datasource.MondrianDatasource;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IConnectionUserRoleMapper;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;

public class MondrianHelper {

	private static Logger logger = Logger.getLogger(MondrianHelper.class);

	/**
	 * 
	 * @return MondrianCatalog
	 */
	public static List<MondrianCatalog> listAvailableMondrianCatalogs() {
		IMondrianCatalogService mondrianCatalogService = PentahoSystem
				.get(IMondrianCatalogService.class,
						"IMondrianCatalogService", PentahoSessionHolder.getSession());
		return mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true);
	}
	
	/**
	 * 
	 * @param mondrian datasource
	 * @return
	 */
	public static List<String> getCubesList(MondrianDatasource mondrianDS) {
		List<String> cubeList = new ArrayList<String>();
		MondrianCatalog catalog = mondrianDS.getMondrianCatalog();
		for (MondrianCube cube : catalog.getSchema().getCubes()) {
			cubeList.add(cube.getName());
		}
		return cubeList;
	}

	/**
	 * 
	 * @param mondrianDS
	 * @param cubeName
	 * @return
	 * @throws ObjectFactoryException
	 */
	public static Dimension[] getDimensionList(MondrianDatasource mondrianDS,
			String cubeName) throws ObjectFactoryException {
		MondrianCatalog catalog = mondrianDS.getMondrianCatalog();
		IDatasourceService datasourceService = PentahoSystem.getObjectFactory()
				.get(IDatasourceService.class, null);
		String jndiDS = datasourceService.getDSUnboundName(catalog
				.getEffectiveDataSource().getJndi());

		Properties properties = new Properties();
		properties.put(RolapConnectionProperties.Catalog.name(),
				catalog.getDefinition());
		properties.put(RolapConnectionProperties.Provider.name(), "mondrian");
		properties.put(RolapConnectionProperties.PoolNeeded.name(), "false");
		properties.put(RolapConnectionProperties.DataSource.name(), jndiDS);

		Util.PropertyList connectProperties = Util
				.parseConnectString(catalog.getDataSourceInfo());
		Iterator<Pair<String, String>> iter = connectProperties.iterator();
		while (iter.hasNext()) {
			Pair<String, String> pair = iter.next();
			if (!properties.containsKey(pair.getKey()))
				properties.put(pair.getKey(), pair.getValue());
		}

		MDXConnection mdxConnection = (MDXConnection) PentahoConnectionFactory
				.getConnection(IPentahoConnection.MDX_DATASOURCE, properties,
						PentahoSessionHolder.getSession(), null);
		// mdxConnection.setProperties( properties );
		Connection connection = mdxConnection.getConnection();
		if (connection == null) {
			logger.error("Error getting mondrian connection object");
			return null;
		}

		Schema schema = connection.getSchema();
		if (schema == null) {
			logger.error("Error getting mondrian schema from mondrian connection object");
			return null;
		}

		Cube cubes[] = schema.getCubes();
		if ((cubes == null) || (cubes.length == 0)) {
			logger.error("Error getting cubes list from the mondrian schema");
			return null;
		}

		Cube cube = null;
		if (cubes.length == 1) {
			cube = cubes[0];
		} else {
			for (Cube element : cubes) {
				if (element.getName().equals(cubeName)) {
					cube = element;
					break;
				}
			}
		}

		Dimension[] dims = cube.getDimensions();
		return dims;
	}

	/**
	 * 
	 * @param datasource
	 * @param cubeName
	 * @param dim
	 * @param measuresList
	 * @return
	 */
	public static String getInitialQuery(MondrianDatasource datasource,
			String cubeName, Dimension dim, List<String> measuresList) {
		String dimName = dim.getName();
		String mdxQuery = "";
		if (!"Measures".equalsIgnoreCase(dimName)) {
			Hierarchy[] hiers = dim.getHierarchies();
			if (hiers != null && hiers.length > 0) {
				Hierarchy hier = ((Hierarchy) hiers[0]);
				String hierName = hier.getName();
				if (hierName.startsWith("[") && hierName.endsWith("]")) {
					hierName = "." + hierName;
				} else {
					hierName = ".[" + hierName + "]";
				}
				if (measuresList == null || measuresList.size() < 1)
					mdxQuery = "SELECT {[Measures].DefaultMember} on columns, {"
							+ dim
							+ hierName
							+ ".DefaultMember} on rows FROM ["
							+ cubeName + "]";
				else {
					String measuresMDX = "";
					Iterator<String> measuresItr = measuresList.iterator();
					while (measuresItr.hasNext()) {
						measuresMDX += "[Measures]";
						String measure = measuresItr.next();
						if (measure.startsWith("[") && measure.endsWith("]")) {
							measuresMDX += "." + measure;
						} else {
							measuresMDX += ".[" + measure + "]";
						}
						if (measuresItr.hasNext())
							measuresMDX += ",";
					}
					mdxQuery = "SELECT {" + measuresMDX + "} on columns, {"
							+ dim + ".Children} on rows FROM [" + cubeName
							+ "]";
				}
			}
			logger.info("Default query generated as :" + mdxQuery);
		}
		return mdxQuery;
	}

	/**
	 * 
	 * @param datasource
	 * @param cube
	 * @param measureDim
	 * @return
	 */
	public static List<String> discoverMeasures(MondrianDatasource datasource,
			String cube, Dimension measureDim) {
		List<String> measuresList = new ArrayList<String>();
		if (measureDim.isMeasures()) {
			Schema schema = measureDim.getSchema();
			Hierarchy measureHier = measureDim.getHierarchy();
			List<Member> measures = schema.getSchemaReader().getLevelMembers(
					measureHier.getLevels()[0], false);
			Iterator<Member> measuresItr = measures.iterator();
			while (measuresItr.hasNext()) {
				Member mem = measuresItr.next();
				if (mem.isMeasure() && !mem.isHidden()
						&& !mem.getName().equalsIgnoreCase("Fact Count"))
					measuresList.add(mem.getName());
			}

		}
		return measuresList;
	}

	/**
	 * 
	 * @param catalogUri
	 * @return
	 * @throws PentahoAccessControlException
	 */
	public static String doMondrianRoleMapping(String catalogUri)
			throws PentahoAccessControlException {
		String role = "";
		if (PentahoSystem.getObjectFactory().objectDefined(
				MDXConnection.MDX_CONNECTION_MAPPER_KEY)) {
			IConnectionUserRoleMapper mondrianUserRoleMapper = PentahoSystem
					.get(IConnectionUserRoleMapper.class,
							MDXConnection.MDX_CONNECTION_MAPPER_KEY, null);
			if (mondrianUserRoleMapper != null) {
				// Do role mapping
				String[] validMondrianRolesForUser = mondrianUserRoleMapper
						.mapConnectionRoles(PentahoSessionHolder.getSession(),
								catalogUri);
				if ((validMondrianRolesForUser != null)
						&& (validMondrianRolesForUser.length > 0)) {
					StringBuffer buff = new StringBuffer();
					String aRole = null;
					for (int i = 0; i < validMondrianRolesForUser.length; i++) {
						aRole = validMondrianRolesForUser[i];
						if (i > 0) {
							buff.append(",");
						}
						buff.append(aRole.replaceAll(",", ",,"));
					}
					role = buff.toString();
				}
			}
		}
		return role;
	}
	
}
