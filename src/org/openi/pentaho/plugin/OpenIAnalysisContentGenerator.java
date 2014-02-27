package org.openi.pentaho.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.dom4j.Document;
import org.openi.analysis.Analysis;
import org.openi.analysis.AnalysisHelper;
import org.openi.datasource.Datasource;
import org.openi.datasource.DatasourceType;
import org.openi.datasource.MondrianDatasource;
import org.openi.datasource.XMLADatasource;
import org.openi.olap.CubeDataExplorer;
import org.openi.service.DatasourceService;
import org.openi.util.plugin.PluginUtils;
import org.openi.util.plugin.ViewParamResolver;
import org.openi.util.serialize.XMLBeanHelper;
import org.openi.util.wcf.WCFUtils;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;
import org.pentaho.platform.engine.services.solution.SimpleParameterSetter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.springframework.context.ConfigurableApplicationContext;

import com.tonbeller.wcf.controller.Controller;
import com.tonbeller.wcf.controller.MultiPartEnabledRequest;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.WcfController;

/**
 * content generator for openi-pentaho plugin
 * 
 * @author SUJEN
 * 
 */
public class OpenIAnalysisContentGenerator extends SimpleContentGenerator {

	private static final Log logger = LogFactory
			.getLog(OpenIAnalysisContentGenerator.class);

	private static final String TEXT_HTML = "text/html"; //$NON-NLS-1$
	private IParameterProvider requestParameters;
	private IParameterProvider pathParameters;
	private String path = null;
	private String contentType = null;
	private String actionType = null;

	private static final String RENDER_OPENI_ANALYSIS = "/RenderOAnalysis";
	public static final String PLUGIN_NAME = "openi";
	private static final String MIME_HTML = "text/html";
	public String RELATIVE_URL;

	public Map<String, IParameterProvider> getParameterProviders() {
		return this.parameterProviders;
	}

	@SuppressWarnings("unchecked")
	private IParameterProvider getRequestParameters() {
		if (this.requestParameters != null) {
			return this.requestParameters;
		}

		if (this.parameterProviders == null) {
			return new SimpleParameterProvider();
		}

		IParameterProvider requestParams = this.parameterProviders
				.get("request"); //$NON-NLS-1$
		SimpleParameterSetter parameters = new SimpleParameterSetter();
		Iterator requestParamIterator = requestParams.getParameterNames();
		while (requestParamIterator.hasNext()) {
			String param = (String) requestParamIterator.next();
			parameters.setParameter(param, requestParams.getParameter(param));
		}
		this.requestParameters = parameters;
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public IParameterProvider getPathParameters() {
		if (this.pathParameters != null) {
			return this.pathParameters;
		}

		IParameterProvider pathParams = this.parameterProviders.get("path"); //$NON-NLS-1$
		SimpleParameterSetter parameters = new SimpleParameterSetter();
		Iterator pathParamIterator = pathParams.getParameterNames();
		while (pathParamIterator.hasNext()) {
			String param = (String) pathParamIterator.next();
			parameters.setParameter(param, pathParams.getParameter(param));
		}

		this.pathParameters = parameters;
		return parameters;
	}

	public String getMimeType() {
		IParameterProvider requestParams = getRequestParameters();
		IParameterProvider pathParams = getPathParameters();

		if ((requestParams != null)
				&& (requestParams.getStringParameter("contentType", null) != null)) { //$NON-NLS-1$
			contentType = requestParams.getStringParameter(
					"contentType", TEXT_HTML); //$NON-NLS-1$
		} else if ((pathParams != null)
				&& (pathParams.getStringParameter("contentType", null) != null)) { //$NON-NLS-1$
			contentType = pathParams.getStringParameter(
					"contentType", TEXT_HTML); //$NON-NLS-1$
		}
		return contentType;
	}

	@Override
	public void createContent() throws Exception {

		IParameterProvider requestParams = getRequestParameters();
		IParameterProvider pathParams = getPathParameters();

		if ((requestParams != null)
				&& (requestParams.getStringParameter("path", null) != null)) //$NON-NLS-1$
		{
			path = URLDecoder.decode(
					requestParams.getStringParameter("path", ""), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if ((pathParams != null)
				&& (pathParams.getStringParameter("path", null) != null)) { //$NON-NLS-1$
			path = URLDecoder.decode(
					pathParams.getStringParameter("path", ""), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		if ((requestParams != null)
				&& (requestParams.getStringParameter("contentType", null) != null)) { //$NON-NLS-1$
			contentType = requestParams.getStringParameter(
					"contentType", TEXT_HTML); //$NON-NLS-1$
		} else if ((pathParams != null)
				&& (pathParams.getStringParameter("contentType", null) != null)) { //$NON-NLS-1$
			contentType = pathParams.getStringParameter(
					"contentType", TEXT_HTML); //$NON-NLS-1$
		}

		if ((requestParams != null)
				&& (requestParams.getStringParameter("actionType", null) != null)) { //$NON-NLS-1$
			actionType = requestParams.getStringParameter(
					"actionType", TEXT_HTML); //$NON-NLS-1$
		} else if ((pathParams != null)
				&& (pathParams.getStringParameter("actionType", null) != null)) { //$NON-NLS-1$
			actionType = pathParams.getStringParameter("actionType", TEXT_HTML); //$NON-NLS-1$
		}

		if (actionType != null && actionType.equals("new")) {
			String datasourceType = requestParams.getStringParameter(
					"datasourceType", null);
			String datasource = requestParams.getStringParameter("datasource",
					null);
			String cube = requestParams.getStringParameter("cube", null);
			String mdxQuery = requestParams.getStringParameter("mdx", null);
			String selectedMeasures = requestParams.getStringParameter(
					"selectedMeasures", null);
			if (datasourceType == null || datasourceType.equals("")
					|| datasource == null || datasource.equals("")
					|| cube == null || cube.equals("")) {
			} else {
				Analysis newAnalysis = new Analysis();
				newAnalysis.setAnalysisTitle(cube);
				newAnalysis.setDataSourceName(datasource);
				newAnalysis.setDsType(datasourceType);
				DatasourceType dsType = (datasourceType
						.equals(DatasourceType.XMLA.toString())) ? DatasourceType.XMLA
						: DatasourceType.MONDRIAN;
				ConfigurableApplicationContext appContext = PluginUtils
						.getSpringBeanFactory();
				DatasourceService dsService = (DatasourceService) appContext
						.getBean("dsService");
				if (mdxQuery != null && !mdxQuery.equals(""))
					newAnalysis.setMdxQuery(mdxQuery);
				else
					newAnalysis.setMdxQuery(AnalysisHelper.generateDefaultMdx(
							dsType,
							dsService.getDatasource(datasource, dsType), cube,
							selectedMeasures));
				if (newAnalysis == null) {
					logger.error("Error while generating new analysis");
					throw new NullPointerException(
							"Error while generating new analysis");
				}

				currentAnalysis = newAnalysis;

				HttpServletRequest request = (HttpServletRequest) parameterProviders
						.get("path").getParameter("httprequest");
				HttpServletResponse response = (HttpServletResponse) parameterProviders
						.get("path").getParameter("httpresponse");
				if (logger.isInfoEnabled())
					logger.info("Loading new analysis");
				loadAnalysis(newAnalysis, request, response);

			}

		} else if (actionType != null && actionType.equals("view")) {
			if (path != null && path.length() > 0) {
				IUnifiedRepository repository = PentahoSystem.get(
						IUnifiedRepository.class, null);
				RepositoryFile repoFile = repository.getFile(path);

				HttpServletRequest httpRequest = (HttpServletRequest) pathParams
						.getParameter("httprequest"); //$NON-NLS-1$

				HttpServletResponse httpResponse = (HttpServletResponse) pathParams
						.getParameter("httpresponse"); //$NON-NLS-1$

				if (repoFile != null) {

					Document document = null;
					SimpleRepositoryFileData data = repository.getDataForRead(
							repoFile.getId(), SimpleRepositoryFileData.class);
					if (data != null) {
						
						XMLBeanHelper xmlBeanHelper = new XMLBeanHelper();
						Analysis analysis = (Analysis) xmlBeanHelper.xmlStreamToBean(data.getInputStream());
						
						if(logger.isDebugEnabled())
							logger.debug("Analysis File Content: " + xmlBeanHelper.beanToXMLString(analysis));
						
						if (analysis == null) {
							logger.error("Error retrieving analysis file from solution repository");
							throw new NullPointerException(
									"Error retrieving analysis file from solution repository");
						}

						currentAnalysis = analysis;

						if (logger.isInfoEnabled())
							logger.info("Loading Analysis from " + path);
						loadAnalysis(analysis, httpRequest, httpResponse);
					}

				}

			}
		}
		try {

			IContentItem contentItem = outputHandler.getOutputContentItem(
					"response", "content", instanceId, getMimeType()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			if (contentItem == null) {
				logger.error("content item is null"); //$NON-NLS-1$
				throw new NullPointerException("content item is null"); //$NON-NLS-1$
			}

			OutputStream out = contentItem.getOutputStream(null);
			createContent(out);

		} catch (Exception e) {
			logger.error("Error loading solution file", e);
			throw new Exception("Error loading solution file", e);
		}

	}

	@Override
	public void createContent(OutputStream out) throws Exception {
		try {
			StringBuilder html = new StringBuilder();
			final IPluginManager pluginManager = (IPluginManager) PentahoSystem
					.get(IPluginManager.class,
							PentahoSessionHolder.getSession());
			final PluginClassLoader pluginClassloader = (PluginClassLoader) pluginManager
					.getClassLoader(PLUGIN_NAME);
			File pluginDir = pluginClassloader.getPluginDir();

			IParameterProvider requestParams = parameterProviders
					.get(IParameterProvider.SCOPE_REQUEST);
			if (requestParams == null) {
				logger.error("Parameter provider is null");
				throw new NullPointerException("Parameter provider is null");
			}

			String action = requestParams.getStringParameter("action", null);
			String actionType = requestParams.getStringParameter("actionType",
					null);
			String datasourceType = requestParams.getStringParameter(
					"datasourceType", null);
			String datasource = requestParams.getStringParameter("datasource",
					null);
			String cube = requestParams.getStringParameter("cube", null);
			String selectedMeasures = requestParams.getStringParameter(
					"selectedMeasures", null);
			String htmlStr = "";
			File viewFile = null;

			if (actionType != null
					&& actionType.equals("new")
					&& (datasourceType == null || datasourceType.equals("")
							|| datasource == null || datasource.equals("")
							|| cube == null || cube.equals(""))) {
				viewFile = new File(pluginDir,
						"ui/views/datasource_selection_view.html");
				FileReader fr = new FileReader(viewFile);
				BufferedReader br = new BufferedReader(fr);
				String inputLine;

				while ((inputLine = br.readLine()) != null) {
					html.append(inputLine);
				}
				ConfigurableApplicationContext appContext = PluginUtils
						.getSpringBeanFactory();
				DatasourceService dsService = (DatasourceService) appContext
						.getBean("dsService");
				List<Datasource> xmlaDatasources = dsService
						.getDatasources(DatasourceType.XMLA);
				List<Datasource> mondrianDatasources = dsService
						.getDatasources(DatasourceType.MONDRIAN);

				Iterator xmlaDatasourcesItr = xmlaDatasources.iterator();
				Iterator mondrianDatasourcesItr = mondrianDatasources
						.iterator();

				String jsScript = "<script language=\"javascript\">\r\nfunction populateDatasources(){\r\n";
				jsScript += "jQuery(\"#select-datasource-type\").change(function() {\r\n";

				jsScript += "jQuery(\"#select-datasource\").find(\"option\").remove();";
				jsScript += "jQuery(\"#select-cube\").find(\"option\").remove();";

				jsScript += "jQuery(\"#select-datasource\").append(jQuery(\"<option></option>\").val(\"Select Catalog\").html(\"Select Catalog\"));";
				jsScript += "jQuery(\"#select-cube\").append(jQuery(\"<option></option>\").val(\"Select Cube\").html(\"Select Cube\"));";

				jsScript += "var selectedDatasourceType = jQuery(this).val()\r\n";
				jsScript += "if(selectedDatasourceType == \"XMLA\") {\r\n";

				while (xmlaDatasourcesItr.hasNext()) {
					XMLADatasource xmlaDS = (XMLADatasource) xmlaDatasourcesItr
							.next();
					jsScript += "jQuery(\"#select-datasource\").append(jQuery(\"<option></option>\").val("
							+ "\""
							+ xmlaDS.getName()
							+ "\""
							+ ").html("
							+ "\""
							+ xmlaDS.getName() + "\"));";
				}

				jsScript += "} else if(selectedDatasourceType == \"MONDRIAN\") {\r\n";
				while (mondrianDatasourcesItr.hasNext()) {
					MondrianDatasource mondrianDS = (MondrianDatasource) mondrianDatasourcesItr
							.next();
					jsScript += "jQuery(\"#select-datasource\").append(jQuery(\"<option></option>\").val("
							+ "\""
							+ mondrianDS.getName()
							+ "\""
							+ ").html("
							+ "\"" + mondrianDS.getName() + "\"));";
				}

				jsScript += "}";
				jsScript += "jQuery.uniform.update(\"#select-datasource\");";
				jsScript += "jQuery.uniform.update(\"#select-cube\");";
				jsScript += "});\r\n";

				jsScript += "\r\n}\r\n</script>";
				int htmlBodyIndex = html.indexOf("</body>");
				html.insert(htmlBodyIndex, jsScript);
				htmlStr = html.toString();
			} else if (actionType != null && actionType.equals("explore")) {
				if (datasourceType == null || datasourceType.equals("")
						|| datasource == null || datasource.equals("")
						|| cube == null || cube.equals("")) {
				} else {
					viewFile = new File(pluginDir,
							"ui/views/explore_data_view.html");
					FileReader fr = new FileReader(viewFile);
					BufferedReader br = new BufferedReader(fr);
					String inputLine;

					while ((inputLine = br.readLine()) != null) {
						html.append(inputLine);
					}
					DatasourceType dsType = (datasourceType
							.equals(DatasourceType.XMLA.toString())) ? DatasourceType.XMLA
							: DatasourceType.MONDRIAN;
					ConfigurableApplicationContext appContext = PluginUtils
							.getSpringBeanFactory();
					DatasourceService dsService = (DatasourceService) appContext
							.getBean("dsService");

					CubeDataExplorer dataExplorer = new CubeDataExplorer(
							dsType,
							dsService.getDatasource(datasource, dsType), cube,
							selectedMeasures);
					Map queriesMap = dataExplorer.generateMeasureByDimQueries();

					String jsScript = "<script language=\"javascript\">\r\n jQuery(document).ready(function(){var edaWidgets = [];\r\n";
					if (queriesMap != null) {
						int count = 0;
						Iterator keyItr = queriesMap.keySet().iterator();
						while (keyItr.hasNext()) {
							String key = (String) keyItr.next();
							jsScript += "edaWidgets[" + count
									+ "] = new EdaWidget('" + key + "', '"
									+ datasourceType + "', '" + datasource
									+ "', '" + cube + "', '"
									+ queriesMap.get(key) + "');\r\n";
							count++;
						}
					}

					jsScript += "Eda.loadEDAWidgets(edaWidgets);\r\n});\r\n</script>";
					int htmlBodyIndex = html.indexOf("</body>");
					html.insert(htmlBodyIndex, jsScript);
					htmlStr = html.toString();

				}
			} else {
				if (currentAnalysis != null && currAnalysisPivotID != null) {
					logger.info("Loading Analysis View from the location: "
							+ pluginDir.getPath()
							+ "/ui/views/analysis_view.html");

					viewFile = new File(pluginDir,
							"ui/views/analysis_view.html");

					FileReader fr = new FileReader(viewFile);
					BufferedReader br = new BufferedReader(fr);
					String inputLine;

					while ((inputLine = br.readLine()) != null) {
						html.append(inputLine);
					}

					htmlStr = html.toString();
					htmlStr = new ViewParamResolver().resolveViewParams(
							htmlStr, currentAnalysis, currAnalysisPivotID);

				}
			}
			out.write(htmlStr.getBytes(LocaleHelper.getSystemEncoding()));

		} catch (Exception e) {
			throw new Exception("Error creating content", e);
		}

	}

	private void loadAnalysis(Analysis analysis, HttpServletRequest request,
			HttpServletResponse response) {
		// String pivotID = UUIDUtil.getUUIDAsString();
		MultiPartEnabledRequest mprequest = new MultiPartEnabledRequest(
				(HttpServletRequest) request);
		HttpSession mpsession = mprequest.getSession(true);
		MDC.put("SessionID", mpsession.getId());
		String cpath = mprequest.getContextPath();
		mprequest.setAttribute("context", cpath);
		RequestContext wcfcontext = WCFUtils.getRequestContext(request,
				response);

		Controller controller = WcfController.instance(request.getSession());

		try {
			controller.request(wcfcontext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String pivotID = UUIDUtil.getUUIDAsString();
		currAnalysisPivotID = pivotID;

		Map<String, String> map = new HashMap<String, String>();
		map.put("pivotId", pivotID);
		request.setAttribute("com.tonbeller.wcf.component.RendererParameters",
				map);
		addAnalysisToSession(currAnalysisPivotID, analysis,
				PentahoSessionHolder.getSession());
		AnalysisHelper.loadAnalysis(analysis, wcfcontext, currAnalysisPivotID);

	}

	private void addAnalysisToSession(String pivotID, Analysis analysis,
			IPentahoSession session) {
		Map<String, Analysis> loadedAnalyses = (Map) session
				.getAttribute("loadedAnalyses");
		if (loadedAnalyses == null) {
			loadedAnalyses = new HashMap<String, Analysis>();
			userSession.setAttribute("loadedAnalyses", loadedAnalyses);
		}
		loadedAnalyses.put(pivotID, analysis);
	}

	public Log getLogger() {
		return LogFactory.getLog(OpenIAnalysisContentGenerator.class);
	}

	private String currAnalysisPivotID;

	private Analysis currentAnalysis;

}
