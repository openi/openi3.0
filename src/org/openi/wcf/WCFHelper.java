package org.openi.wcf;

import org.apache.log4j.Logger;
import org.openi.analysis.Analysis;
import org.openi.chart.EnhancedChartComponent;
import org.openi.chart.EnhancedChartComponentTag;
import org.openi.datasource.DatasourceType;
import org.openi.datasource.MondrianDatasource;
import org.openi.datasource.XMLADatasource;
import org.openi.olap.mondrian.MondrianOlapModelTag;
import org.openi.olap.xmla.XmlaQueryTag;
import org.openi.service.DatasourceService;
import org.openi.service.exception.ServiceException;
import org.openi.table.DrillExpandPositionUI;
import org.openi.table.DrillReplaceUI;
import org.openi.table.DrillThroughUI;
import org.openi.table.TableComponentTag;
import org.openi.util.olap.MondrianHelper;
import org.openi.util.plugin.PluginUtils;
import org.openi.wcf.form.FormComponentTag;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.springframework.context.ConfigurableApplicationContext;

import org.openi.navigator.Navigator;
import org.openi.navigator.NavigatorTag;

import com.tonbeller.jpivot.print.PrintComponent;
import com.tonbeller.jpivot.table.TableComponent;
import com.tonbeller.jpivot.table.navi.AxisStyleUI;
import com.tonbeller.jpivot.table.navi.NonEmptyUI;
import com.tonbeller.jpivot.table.navi.SwapAxesUI;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * responsible for initializing and loading the wcf components into the session
 * components are loaded with PivotID as identifier session attribute name for
 * table component is table + pivotID session attribute name for navigator
 * component is chart + pivotID and so on separate UUUID based PivotID is used
 * for every analysis loaded
 * 
 * @author SUJEN
 */
public class WCFHelper {

	private static Logger logger = Logger.getLogger(WCFHelper.class);

	private IPentahoSession session = PentahoSessionHolder.getSession();
	private RequestContext context = null;
	private String pivotID = null;

	public WCFHelper(RequestContext context, String pivotID) {
		this.session = PentahoHttpSessionHelper.getPentahoSession(context
				.getRequest());
		this.context = context;
		this.pivotID = pivotID;
	}

	/**
	 * initialized Olap Model, xmla model or mondrian model based on datasource
	 * Type saved in Analysis definition file
	 * 
	 * @param analysis
	 * @throws Exception
	 */
	public void initOlapModel(Analysis analysis) throws Exception {
		ConfigurableApplicationContext appContext = PluginUtils
				.getSpringBeanFactory();
		DatasourceService dsService = (DatasourceService) appContext
				.getBean("dsService");
		if (dsService == null)
			throw new ServiceException("unable to load datasource service");

		if (analysis.getDsType().equals("XMLA")) {

			XmlaQueryTag queryTag = new XmlaQueryTag();
			XMLADatasource datasource = (XMLADatasource) dsService
					.getDatasource(analysis.getDataSourceName(),
							DatasourceType.XMLA);
			if (logger.isInfoEnabled())
				logger.info("Initializing xmla olap model with datasource name: "
						+ datasource.getDatasourceName()
						+ ", server url: "
						+ datasource.getServerURL()
						+ ", catalog: "
						+ datasource.getCatalog()
						+ " and MDX Query: "
						+ analysis.getMdxQuery());

			queryTag.setMdxQuery(analysis.getMdxQuery());
			queryTag.setUri(datasource.getServerURL());
			queryTag.setDataSource(datasource.getDatasourceName());
			queryTag.setCatalog(datasource.getCatalog());
			queryTag.setUser(datasource.getUsername());
			queryTag.setPassword(datasource.getPassword());
			queryTag.setId("xmlaQuery" + pivotID);

			try {
				queryTag.init(context);
			} catch (Exception e) {
				logger.error(
						"couldn't initialize olap model, no olap components will be loaded into the session",
						e);
			}
		} else if (analysis.getDsType().equals("MONDRIAN")) {

			MondrianOlapModelTag mondrianOlapModelTag = new MondrianOlapModelTag();
			MondrianDatasource mondrianDS = (MondrianDatasource) dsService
					.getDatasource(analysis.getDataSourceName(),
							DatasourceType.MONDRIAN);
			MondrianCatalog selectedCatalog = mondrianDS.getMondrianCatalog();
			
			String jndiDS = selectedCatalog.getEffectiveDataSource().getJndi();

			String catalogUri = "file://"
					+ PentahoSystem.getApplicationContext().getSolutionPath(
							selectedCatalog.getDefinition().substring(
									("solution:/").length() - 1));
			/*String catalogUri = selectedCatalog.getDefinition().replaceAll("solution:", "file:" + PentahoSystem.getApplicationContext().getSolutionPath("") );*/
			
			String role = MondrianHelper.doMondrianRoleMapping(catalogUri);
			

			if (logger.isInfoEnabled()) {
				logger.info("CatalogURI : " + catalogUri);
				logger.info("Datasource : " + jndiDS);
				logger.info("Role : " + role);
				logger.info("MDX Query : " + analysis.getMdxQuery());
				logger.info("Initializing mondrian olap model");
			}

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
			mondrianOlapModelTag.setId("xmlaQuery" + pivotID);
			mondrianOlapModelTag.setCatalogUri(catalogUri);
			mondrianOlapModelTag.setDataSource(jndiDS);
			mondrianOlapModelTag.setRole(role);
			mondrianOlapModelTag.setDynResolver(dynResolver);
			mondrianOlapModelTag.setMdxQuery(analysis.getMdxQuery());
			mondrianOlapModelTag.init(context);

		}
	}

	/**
	 * loads Chart into session, Chart component id: chart + pivotID
	 * 
	 * @param analysis
	 * @throws Exception
	 */
	public void loadChart(Analysis analysis) throws Exception {
		EnhancedChartComponent chart = (EnhancedChartComponent) session
				.getAttribute("chart" + pivotID);
		if (chart == null) {
			RequestContext context = getRequestContext();
			EnhancedChartComponentTag chartTag = new EnhancedChartComponentTag();
			chartTag.setQuery("xmlaQuery" + pivotID);
			chartTag.setId("chart" + pivotID);
			try {
				chart = (EnhancedChartComponent) chartTag
						.createComponent(context);
				chart.initialize(context);
				if (logger.isInfoEnabled())
					logger.info("loading chart component with id " + "chart"
							+ pivotID + " into session");
				session.setAttribute("chart" + pivotID, chart);
			} catch (Exception e) {
				throw new Exception("Could not create chart 'chart" + pivotID
						+ "'", e);
			}
		}
		
		if(chart != null) {
			chart.setChartType(analysis.getChartType());
			chart.setChartHeight(analysis.getChartHeight());
			chart.setChartWidth(analysis.getChartWidth());
			
			chart.setLegendPosition(analysis.getLegendPosition());
			chart.setShowLegend(analysis.isShowLegend());
			chart.setLegendFontName(analysis.getLegendFontName());
			chart.setLegendFontStyle(analysis.getLegendFontStyle());
			chart.setLegendFontSize(analysis.getLegendFontSize());

			chart.setShowSlicer(analysis.isShowSlicer());
			chart.setSlicerPosition(analysis.getSlicerPosition());
			chart.setSlicerFontName(analysis.getSlicerFontName());
			chart.setSlicerFontStyle(analysis.getSlicerFontStyle());
			chart.setSlicerFontSize(analysis.getSlicerFontSize());

			chart.setChartTitle(analysis.getChartTitle());
			chart.setFontName(analysis.getFontName());
			chart.setFontStyle(analysis.getFontStyle());
			chart.setFontSize(analysis.getFontSize());

			chart.setHorizAxisLabel(analysis.getHorizAxisLabel());
			chart.setVertAxisLabel(analysis.getVertAxisLabel());
			
			chart.setAxisFontName(analysis.getAxisFontName());
			chart.setAxisFontStyle(analysis.getAxisFontStyle());
			chart.setAxisFontSize(analysis.getAxisFontSize());
			
			chart.setAxisTickFontName(analysis.getAxisTickFontName());
			chart.setAxisTickFontStyle(analysis.getAxisTickFontStyle());
			chart.setAxisTickFontSize(analysis.getAxisTickFontSize());
		}
	}

	/**
	 * loads table component into session, table component id: table + pivotID
	 * 
	 * @param analysis
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void loadTable(Analysis analysis) throws Exception {
		TableComponent comp = (TableComponent) session.getAttribute("table"
				+ pivotID);
		if (comp == null) {
			RequestContext context = getRequestContext();
			TableComponentTag tag = new TableComponentTag();
			tag.setQuery("xmlaQuery" + pivotID);
			tag.setId("table" + pivotID);
			try {
				comp = (TableComponent) tag.createComponent(context);

				comp.initialize(context);
				if (logger.isInfoEnabled())
					logger.info("loading table component with id " + "table"
							+ pivotID + " into session");
				session.setAttribute("table" + pivotID, comp);
			} catch (Exception e) {
				throw new Exception("Could not create table 'table" + pivotID
						+ "'", e);
			}
		}
		((SwapAxesUI) comp.getExtensions().get(SwapAxesUI.ID))
				.setButtonPressed(analysis.isSwapAxes());
		((NonEmptyUI) comp.getExtensions().get(NonEmptyUI.ID))
				.setButtonPressed(analysis.isShowNonEmpty());
		((AxisStyleUI) comp.getExtensions().get(AxisStyleUI.ID))
				.setLevelStyle(analysis.isLevelStyle());
		((DrillReplaceUI) comp.getExtensions().get(DrillReplaceUI.ID))
				.setEnabled(analysis.isDrillReplaceEnabled());
		((DrillThroughUI) (comp.getExtensions().get(DrillThroughUI.ID)))
				.setEnabled(analysis.isDrillThroughEnabled());
		((DrillExpandPositionUI) (comp.getExtensions()
				.get(DrillExpandPositionUI.ID))).setEnabled(analysis
				.isDrillPositionEnabled());
	}

	public void loadDrillthroughTable() throws Exception {
		Component comp = (Component) session.getAttribute("xmlaQuery" + pivotID
				+ ".drillthroughtable");
		if (comp == null) {
			RequestContext context = getRequestContext();
			com.tonbeller.wcf.table.TableComponentTag tag = new com.tonbeller.wcf.table.TableComponentTag();
			tag.setId("xmlaQuery" + pivotID + ".drillthroughtable");
			tag.setVisible(false);
			tag.setSelmode("none");
			tag.setEditable(true);
			try {
				comp = tag.createComponent(context);
				comp.initialize(context);
				session.setAttribute("xmlaQuery" + pivotID
						+ ".drillthroughtable", comp);
			} catch (Exception e) {
				throw new Exception(
						"Could not create drillthrough table 'drillthroughtable' ",
						e);
			}
		}
		comp.setVisible(false);
	}

	/**
	 * 
	 * @param id
	 * @param xmlaQuery
	 * @throws Exception
	 */
	public void loadNavigator(String id, String xmlaQuery) throws Exception {
		Navigator nav = (Navigator) session.getAttribute("xmlaNav" + pivotID);
		if (nav == null) {
			try {
				NavigatorTag navTag = new NavigatorTag();
				navTag.setId(id);
				navTag.setQuery(xmlaQuery);
				nav = (Navigator) navTag.createComponent(getRequestContext());
				nav.initialize(getRequestContext());
				if (logger.isInfoEnabled())
					logger.info("loading navigator component with id "
							+ "xmlaNav" + pivotID + " into session");
				session.setAttribute("xmlaNav" + pivotID, nav);
			} catch (Exception e) {
				throw new Exception("Could not create olap navigator 'xmlaNav"
						+ pivotID + "'", e);
			}
		}
		nav.setVisible(true);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void loadPrint() throws Exception {
		PrintComponent comp = (PrintComponent) session.getAttribute("print"
				+ pivotID);
		if (comp == null) {
			try {
				comp = new PrintComponent("print" + pivotID,
						getRequestContext());
				comp.initialize(getRequestContext());
				comp.setVisible(false);
				if (logger.isInfoEnabled())
					logger.info("loading print component with id " + "print"
							+ pivotID + " into session");
				session.setAttribute("print" + pivotID, comp);
			} catch (Exception e) {
				throw new Exception(
						"Could not initialize print component 'print" + pivotID
								+ "'", e);
			}
		}
	}

	public void loadForm(String id, String xmlUri, String model, boolean visible)
			throws Exception {
		Component component = (Component) session.getAttribute(id);
		if (component == null) {
			FormComponentTag formTag = new FormComponentTag();
			formTag.setXmlUri(xmlUri);
			formTag.setModel(model);
			formTag.setId(id);
			formTag.setVisible(visible);
			try {
				component = formTag.createComponent(getRequestContext());
				component.initialize(getRequestContext());
				component.setVisible(visible);
				session.setAttribute(id, component);
			} catch (Exception e) {
				throw new Exception("Could not create form '" + id + "'", e);
			}
		}
	}

	public RequestContext getRequestContext() {
		return this.context;
	}

	/**
	 * public String getPivotID(RequestContext context) { String pivotID = null;
	 * HttpServletRequest request = context.getRequest(); if (request != null &&
	 * request.getParameter("pivotID") != null) { pivotID =
	 * request.getParameter("pivotID"); } else { pivotID =
	 * UUIDUtil.getUUIDAsString(); } return pivotID; }
	 **/

	public void loadChartForm() throws Exception {
		loadForm("chartForm" + this.pivotID, this.chartFormDefinition, "chart"
				+ this.pivotID, true);
	}

	public void loadSortForm() throws Exception {
		loadForm("sortForm" + this.pivotID, this.sortFormDefinition, "table"
				+ this.pivotID, true);
	}

	public void loadMDXEditForm() throws Exception {
		loadForm("mdxEditForm" + this.pivotID, this.mdxEditorFormDefinition,
				"xmlaQuery" + this.pivotID, true);
	}

	public void loadPrintForm() throws Exception {
		loadForm("printForm" + this.pivotID,
				this.printPropertiesFormDefinition, "print" + this.pivotID,
				true);
	}

	// WCF form definition file
	private String chartFormDefinition = PluginUtils.getPluginDir()
			+ "/ui/resources/jpivot/chart/chartpropertiesform.xml";
	private String mdxEditorFormDefinition = PluginUtils.getPluginDir()
			+ "/ui/resources/jpivot/table/mdxedit.xml";
	private String sortFormDefinition = PluginUtils.getPluginDir()
			+ "/ui/resources/jpivot/table/sortform.xml";
	private String printPropertiesFormDefinition = PluginUtils.getPluginDir()
			+ "/ui/resources/jpivot/print/printpropertiesform.xml";

}
