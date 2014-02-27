package org.openi.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.openi.acl.AccessDeniedException;
import org.openi.analysis.Analysis;
import org.openi.chart.EnhancedChartFactory;
import org.openi.datasource.Datasource;
import org.openi.datasource.DatasourceType;
import org.openi.datasource.MondrianDatasource;
import org.openi.datasource.XMLADatasource;
import org.openi.olap.mondrian.MondrianModel;
import org.openi.olap.mondrian.MondrianModelFactory;
import org.openi.olap.mondrian.MondrianOlapModelTag;
import org.openi.olap.xmla.ModelFactory;
import org.openi.olap.xmla.XMLA_Model;
import org.openi.olap.xmla.XmlaQueryTag;
import org.openi.util.file.FileUtils;
import org.openi.util.olap.MondrianHelper;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.xml.sax.SAXException;

import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * 
 * @author SUJEN
 * 
 */
public class ExploreDataService {

	private static Logger logger = Logger.getLogger(ExploreDataService.class);

	/**
	 * 
	 * @param edaWidgetContentQuery
	 * @param edaWidgetTitle
	 * @param edaWidgetCntWidth
	 * @param edaWidgetCntHeight
	 * @param datasourceType
	 * @param datasourceName
	 * @param cubeName
	 * @param requestContext
	 * @return
	 * @throws FileNotFoundException
	 * @throws OlapException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ObjectFactoryException
	 * @throws PentahoAccessControlException
	 * @throws DatasourceServiceException
	 * @throws AccessDeniedException
	 * @throws DBDatasourceServiceException 
	 */
	public File getEdaWidgetContent(String edaWidgetContentQuery,
			String edaWidgetTitle, int edaWidgetCntWidth,
			int edaWidgetCntHeight, String datasourceType,
			String datasourceName, String cubeName,
			RequestContext requestContext) throws FileNotFoundException,
			OlapException, IOException, SAXException, ObjectFactoryException, PentahoAccessControlException, AccessDeniedException, DBDatasourceServiceException {
		File tmpDir = FileUtils.createTempDir();

		Analysis analysis = new Analysis();
		analysis.setAnalysisTitle(edaWidgetTitle);
		analysis.setDataSourceName(datasourceName);
		analysis.setDsType(datasourceType);
		analysis.setMdxQuery(edaWidgetContentQuery);

		DatasourceType dsType = datasourceType.equals(DatasourceType.XMLA
				.toString()) ? DatasourceType.XMLA : DatasourceType.MONDRIAN;
		Datasource datasource = dsService.getDatasource(datasourceName, dsType);

		String chartFilename = constructChartFilename(edaWidgetTitle,
				edaWidgetCntWidth, edaWidgetCntHeight);
		File chartFile = new File(tmpDir, chartFilename);
			OutputStream out = new FileOutputStream(chartFile);
			OlapModel olapModel = null;
			olapModel = getOlapModel(dsType, datasource,
						edaWidgetContentQuery);
			EnhancedChartFactory.createChart(out, analysis, olapModel,
					edaWidgetCntWidth, edaWidgetCntHeight, requestContext
							.getRequest().getLocale(), null, null, "", true);
			out.close();
		return chartFile;
	}

	private static String tempFilePrefix = "edaChart-";

	private static String constructChartFilename(String filename, int width,
			int height) {
		String name = tempFilePrefix + "_" + filename.replace(" ", "_") + "_"
				+ width + "_" + height + System.currentTimeMillis() + ".png";
		return name;
	}

	private void createChart(File chartFile, Analysis analysis,
			OlapModel olapModel, int width, int height, Locale locale,
			CategoryURLGenerator urlGenerator) throws OlapException,
			IOException {
		OutputStream out = new FileOutputStream(chartFile);
		EnhancedChartFactory.createChart(out, analysis, olapModel, width,
				height, locale, null, null, "", true);
		out.close();
	}

	private DatasourceService dsService;

	public DatasourceService getDsService() {
		return dsService;
	}

	public void setDsService(DatasourceService dsService) {
		this.dsService = dsService;
	}

	public synchronized OlapModel getOlapModel(DatasourceType dsType, Datasource datasource,
			String mdxQuery) throws SAXException, IOException, OlapException, ObjectFactoryException, PentahoAccessControlException, DBDatasourceServiceException {
		OlapModel olapModel = null;
		if(dsType == DatasourceType.XMLA) {
			URL confUrl = XmlaQueryTag.class.getResource("config.xml");
			XMLA_Model xmlaModel = (XMLA_Model) ModelFactory.instance(confUrl);
			XMLADatasource xmlsDS = (XMLADatasource) datasource;
			xmlaModel.setMdxQuery(mdxQuery);
			xmlaModel.setUri(xmlsDS.getServerURL());
			xmlaModel.setCatalog(xmlsDS.getCatalog());
			xmlaModel.setDataSource(xmlsDS.getDatasourceName());
			xmlaModel.setUser(URLEncoder.encode(xmlsDS.getUsername(), "UTF-8"));
			xmlaModel.setPassword(URLEncoder.encode(xmlsDS.getPassword(), "UTF-8"));
			xmlaModel.initialize();
			olapModel = xmlaModel;
		}
		else if(dsType == DatasourceType.MONDRIAN){
			MondrianDatasource mondrianDS = (MondrianDatasource) datasource;
			MondrianCatalog selectedCatalog = mondrianDS.getMondrianCatalog();
			
			String jndiDS = selectedCatalog.getJndi();

			/*String catalogUri = "file://"
					+ PentahoSystem.getApplicationContext().getSolutionPath(
							selectedCatalog.getDefinition().substring(
									("solution:/").length() - 1)); */
			/*String catalogUri = selectedCatalog.getDefinition().replaceAll("solution:", "file:" + PentahoSystem.getApplicationContext().getSolutionPath("") );*/
			
			String catalogUri = selectedCatalog.getDefinition();
			
			String role = MondrianHelper.doMondrianRoleMapping(catalogUri);
			
			String dsInfo = selectedCatalog.getDataSourceInfo();
			String[] dsInfoSplits = dsInfo.split(";");
			String dynResolver = "";
			for(int i = 0; i < dsInfoSplits.length; i++) {
				if(dsInfoSplits[i].startsWith("DynamicSchemaProcessor")) {
					String dsInfoSplit = dsInfoSplits[i];
					dynResolver = dsInfoSplit.split("=")[1];
					break;
				}
			}
			
			MondrianModelFactory.Config cfg = new MondrianModelFactory.Config();
			//URL schemaUrl = new URL(catalogUri);
			cfg.setMdxQuery(mdxQuery);
			//cfg.setSchemaUrl("\"" + schemaUrl.toExternalForm() + "\"");
			cfg.setSchemaUrl("\"" + catalogUri + "\"");
			cfg.setDataSource(jndiDS);
			cfg.setRole(role);
			cfg.setDynResolver(dynResolver);
			
		/*	IDatasourceService dsService = PentahoSystem
					.getObjectFactory().get(IDatasourceService.class, null);
			cfg.setExternalDataSource(dsService.getDataSource(jndiDS)); */
			
			IDBDatasourceService datasourceService = PentahoSystem.getObjectFactory().get(IDBDatasourceService.class, null);
			cfg.setExternalDataSource(datasourceService.getDataSource(jndiDS));

			URL confUrl = MondrianOlapModelTag.class.getResource("config.xml");
			MondrianModel mm = MondrianModelFactory.instance(confUrl, cfg);
			olapModel = (OlapModel) mm.getTopDecorator();
			olapModel.initialize();
		}

		return olapModel;
	}

}
