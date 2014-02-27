package org.openi.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openi.analysis.Analysis;
import org.openi.analysis.AnalysisHelper;
import org.openi.analysis.SlicerValue;
import org.openi.analysis.export.AnalysisExporter;
import org.openi.analysis.export.ExporterFactory;
import org.openi.chart.EnhancedChartComponent;
import org.openi.pentaho.plugin.PluginConstants;
import org.openi.table.DrillExpandPositionUI;
import org.openi.table.DrillReplaceUI;
import org.openi.table.DrillThroughUI;
import org.openi.table.SortRankUI;
import org.openi.table.TableComponentTag;
import org.openi.util.file.FileUtils;
import org.openi.util.plugin.PluginUtils;
import org.openi.util.serialize.XMLBeanHelper;
import org.openi.util.wcf.WCFUtils;
import org.openi.util.xml.XmlUtils;
import org.openi.wcf.component.WCFComponentType;
import org.openi.web.rest.AnalysisResource;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.w3c.dom.Document;

import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.print.PrintComponent;
import com.tonbeller.jpivot.table.TableComponent;
import com.tonbeller.jpivot.table.navi.AxisStyleUI;
import com.tonbeller.jpivot.table.navi.NonEmptyUI;
import com.tonbeller.jpivot.table.navi.SwapAxesUI;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * Service Layer class, to be utilized by {@link AnalysisResource}
 * 
 * @author SUJEN
 * 
 */
public class AnalysisService {

	private static Logger logger = Logger.getLogger(AnalysisService.class);

	/**
	 * 
	 * @param pivotID
	 * @param exportType
	 * @param context
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	public File getReportFile(String pivotID, int exportType,
			RequestContext context) throws Exception {
		Map loadedAnalysis = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalysis.get(pivotID);

		File tmpDir = FileUtils.createTempDir();

		String chartFilename = (analysis.getAnalysisTitle() == null || analysis
				.getAnalysisTitle().equals("")) ? "analysis-report" : analysis
				.getAnalysisTitle().replaceAll(" ", "_");
		File exportRptFile = new File(tmpDir, chartFilename);

		AnalysisExporter exporter = ExporterFactory.getExporter(exportType);

		exporter.export(analysis, pivotID, exportRptFile, context);
		return exportRptFile;
	}

	/**
	 * 
	 * @param pivotID
	 * @param showLegend
	 * @param legendPosition
	 * @param legendFontSize
	 * @param legendFontStyle
	 * @param legendFontFamily
	 * @param vertAxisLabel
	 * @param horizAxisLabel
	 * @param subTitle
	 * @param slicerPosition
	 * @param showSlicer
	 * @param vertAxisLabel2
	 * @param horizAxisLabel2
	 * @param chartTitleFontSize
	 * @param chartTitleFontStyle
	 * @param slicerFontSize
	 * @param slicerFontStyle
	 * @param axisTickLabelFontSize
	 * @param axisTickLabelFontStyle
	 * @param axisTickLabelFontFamily
	 * @param axisLabelFontSize
	 * @param axisLabelFontStyle
	 * @param axisLabelFontFamily
	 * @param context
	 * @throws Exception
	 */
	public void applyChartProperties(String pivotID, boolean showLegend,
			int legendPosition, String legendFontFamily, int legendFontStyle,
			int legendFontSize, boolean showSlicer, int slicerPosition,
			String slicerFontFamily, int slicerFontStyle, int slicerFontSize,
			String subTitle, String chartTitleFontFamily,
			int chartTitleFontStyle, int chartTitleFontSize,
			String horizAxisLabel, String vertAxisLabel,
			String axisLabelFontFamily, int axisLabelFontStyle,
			int axisLabelFontSize, String axisTickLabelFontFamily,
			int axisTickLabelFontStyle, int axisTickLabelFontSize,
			RequestContext context) throws Exception {
		Map loadedAnalysis = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalysis.get(pivotID);
		if (analysis == null) {
			throw new Exception("analysis not loaded properly");
		}
		analysis.setLegendPosition(legendPosition);
		analysis.setShowLegend(showLegend);
		analysis.setLegendFontName(legendFontFamily);
		analysis.setLegendFontStyle(legendFontStyle);
		analysis.setLegendFontSize(legendFontSize);

		analysis.setShowSlicer(showSlicer);
		analysis.setSlicerPosition(slicerPosition);
		analysis.setSlicerFontName(slicerFontFamily);
		analysis.setSlicerFontStyle(slicerFontStyle);
		analysis.setSlicerFontSize(slicerFontSize);

		analysis.setChartTitle(subTitle);
		analysis.setFontName(chartTitleFontFamily);
		analysis.setFontStyle(chartTitleFontStyle);
		analysis.setFontSize(chartTitleFontSize);

		analysis.setHorizAxisLabel(horizAxisLabel);
		analysis.setVertAxisLabel(vertAxisLabel);
		
		analysis.setAxisFontName(axisLabelFontFamily);
		analysis.setAxisFontStyle(axisLabelFontStyle);
		analysis.setAxisFontSize(axisLabelFontSize);
		
		analysis.setAxisTickFontName(axisTickLabelFontFamily);
		analysis.setAxisTickFontStyle(axisTickLabelFontStyle);
		analysis.setAxisTickFontSize(axisTickLabelFontSize);
		
	}

	/**
	 * 
	 * @param pivotID
	 * @param reportTitle
	 * @param pageOrientation
	 * @param pageSize
	 * @param pageHeight
	 * @param pageWidth
	 * @param applyTableWidth
	 * @param tableWidth
	 * @param chartOnSeparatePage
	 * @param context
	 * @throws Exception
	 */
	public void applyPrintSettings(String pivotID, String reportTitle,
			String pageOrientation, String pageSize, double pageHeight,
			double pageWidth, boolean applyTableWidth, double tableWidth,
			boolean chartOnSeparatePage, RequestContext context)
			throws Exception {
		PrintComponent printComp = (PrintComponent) context.getSession()
				.getAttribute("print" + pivotID);
		if (printComp == null) {
			throw new Exception("print component identified by " + "print"
					+ pivotID + " not found");
		}
		printComp.setReportTitle(reportTitle);
		printComp.setPageOrientation(pageOrientation);
		printComp.setPaperType(pageSize);
		printComp.setPageHeight(pageHeight);
		printComp.setPageWidth(pageWidth);
		printComp.setSetTableWidth(applyTableWidth);
		printComp.setTableWidth(tableWidth);
		printComp.setChartPageBreak(chartOnSeparatePage);
	}

	/**
	 * 
	 * @param pivotID
	 * @param doShow
	 * @param context
	 * @throws Exception
	 */
	public void showHideTable(String pivotID, boolean doShow,
			RequestContext context) throws Exception {
		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		if (analysis == null) {
			throw new Exception("analysis not loaded properly");
		}
		analysis.setShowTable(doShow);
	}

	/**
	 * 
	 * @param pivotID
	 * @param doShow
	 * @param context
	 * @throws Exception
	 */
	public void showHideChart(String pivotID, boolean doShow,
			RequestContext context) throws Exception {
		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		if (analysis == null) {
			throw new Exception("analysis not loaded properly");
		}
		analysis.setShowChart(doShow);
	}

	/**
	 * 
	 * @param pivotID
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public String getSlicerValue(String pivotID, RequestContext context)
			throws Exception {
		OlapModel olapModel = (OlapModel) context.getSession().getAttribute(
				"xmlaQuery" + pivotID);
		if (olapModel == null) {
			throw new Exception("olap model identified by " + "xmlaQuery"
					+ pivotID + " not found");
		}
		return AnalysisHelper.buildSlicerAsString(olapModel);
	}
	
	/**
	 * 
	 * @param pivotID
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public List<SlicerValue> getSlicerValueObjects(String pivotID,
			RequestContext context) throws Exception {
		OlapModel olapModel = (OlapModel) context.getSession().getAttribute(
				"xmlaQuery" + pivotID);
		if (olapModel == null) {
			throw new Exception("olap model identified by " + "xmlaQuery"
					+ pivotID + " not found");
		}
		return AnalysisHelper.buildSlicerAsObjects(olapModel);
	}

	/**
	 * 
	 * @param pivotID
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public String printAnalysisReport(String pivotID, RequestContext context)
			throws Exception {
		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		if (analysis == null) {
			throw new Exception("analysis not loaded properly");
		}

		String tableHTML = "";
		String chartHTML = "";

		if (analysis.isShowTable()) {
			OlapModel olapModel = (OlapModel) context.getSession()
					.getAttribute("xmlaQuery" + pivotID);
			if (olapModel == null) {
				throw new Exception("olap model identified by " + "xmlaQuery"
						+ pivotID + " not found");
			}

			TableComponentTag tag = new TableComponentTag();
			tag.setQuery("xmlaQuery" + pivotID);
			TableComponent comp = null;
			comp = (TableComponent) tag.createComponent(context);
			comp.initialize(context);

			((SwapAxesUI) comp.getExtensions().get(SwapAxesUI.ID))
					.setButtonPressed(analysis.isSwapAxes());
			((NonEmptyUI) comp.getExtensions().get(NonEmptyUI.ID))
					.setButtonPressed(analysis.isShowNonEmpty());
			((AxisStyleUI) comp.getExtensions().get(AxisStyleUI.ID))
					.setLevelStyle(analysis.isLevelStyle());
			((DrillReplaceUI) comp.getExtensions().get(DrillReplaceUI.ID))
					.setEnabled(false);
			((DrillThroughUI) (comp.getExtensions().get(DrillThroughUI.ID)))
					.setEnabled(false);
			((DrillExpandPositionUI) (comp.getExtensions()
					.get(DrillExpandPositionUI.ID))).setEnabled(false);
			((SortRankUI) (comp.getExtensions().get(SortRankUI.ID)))
					.setEnabled(false);

			Transformer transformer = XmlUtils.getTransformer(
					new File(WCFUtils
							.getWCFComponentXSLUri(WCFComponentType.TABLE)),
					true);
			Document document = WCFUtils.componentToDocument(comp, context);
			DOMSource source = new DOMSource(document);
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			transformer.transform(source, result);

			tableHTML = sw.toString();

		}

		String host = context.getRequest().getServerName();
		int port = context.getRequest().getServerPort();
		String location = context.getRequest().getContextPath();
		String scheme = context.getRequest().getScheme();
		String url = scheme + "://" + host + ":" + port + location;

		if (analysis.isShowChart()) {
			String chartResourceURL = url
					+ "/plugin/openi/api/wcfCompResource/";
			chartResourceURL += "wcfChartComp";
			chartResourceURL += "?inline=true&amp;pivotID=" + pivotID + "&chartWidth="
					+ analysis.getChartWidth() + "&chartHeight="
					+ analysis.getChartHeight() + "&chartType="
					+ analysis.getChartType();

			chartHTML = "<img src=\"" + chartResourceURL + "\" />";
			/*EnhancedChartComponent chartComp = (EnhancedChartComponent) context.getSession()
					.getAttribute("chart" + pivotID);
			if (chartComp == null) {
				throw new Exception("Chart Component identified by " + "chart"
						+ pivotID + " not found");
			}
			Transformer transformer = XmlUtils.getTransformer(
					new File(WCFUtils
							.getWCFComponentXSLUri(WCFComponentType.CHART)),
					true);
			Document document = WCFUtils.componentToDocument(chartComp, context);
			DOMSource source = new DOMSource(document);
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			transformer.transform(source, result);

			chartHTML = sw.toString();*/
		}

		File viewFile = new File(PluginUtils.getPluginDir(),
				"ui/views/print_analysis_view.html");
		FileReader fr = new FileReader(viewFile);
		BufferedReader br = new BufferedReader(fr);
		String inputLine = "";
		StringBuilder html = new StringBuilder();
		while ((inputLine = br.readLine()) != null) {
			html.append(inputLine);
		}
		String jsScript = "<script language=\"javascript\">\r\njQuery(document).ready(function(){\r\n";
		jsScript += "jQuery(\"#table-container\").empty().html('" + tableHTML
				+ "');\r\n" + "jQuery(\"#chart-container\").empty().html('"
				+ chartHTML + "');\r\n"
				+ "jQuery(\"#title-bar\").empty().html('"
				+ analysis.getAnalysisTitle() + "');" + "window.print();";
		jsScript += "\r\n});\r\n</script>";
		int htmlBodyIndex = html.indexOf("</body>");
		html.insert(htmlBodyIndex, jsScript);
		String htmlStr = html.toString();
		htmlStr = htmlStr.replaceAll("PARAM_contextPath", url);
		return htmlStr;
	}

	/**
	 * 
	 * @param pivotID
	 * @param filename
	 * @param solution
	 * @param path
	 * @param type
	 * @param overwrite
	 * @param context
	 * @throws Exception
	 */
	public void saveAnalysisReport(String pivotID, String fileName,
			String solution, String path, String type, boolean overwrite,
			RequestContext context) throws Exception {
		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		if (analysis == null) {
			throw new Exception("analysis not loaded properly");
		}

		if(!fileName.endsWith("." + PluginConstants.PLUGIN_CONTENT_TYPE)) {
			analysis.setAnalysisTitle(fileName);
			fileName += "." + PluginConstants.PLUGIN_CONTENT_TYPE;
		}
		else
			analysis.setAnalysisTitle(fileName.substring(0, fileName.indexOf("." + PluginConstants.PLUGIN_CONTENT_TYPE)));
		OlapModel olapModel = (OlapModel) context.getSession().getAttribute(
				"xmlaQuery" + pivotID);
		analysis.setMdxQuery(AnalysisHelper.getMDXFromOlapModel(olapModel));

		//ISolutionRepository repository = PentahoSystem.get(
			//	ISolutionRepository.class, PentahoSessionHolder.getSession());
		
		
		RepositoryFile analysisFile = null;
		IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class);
		if (repository == null) {
			throw new NullPointerException("Access to Repository has failed");
		}
		
	     
		XMLBeanHelper xmlBeanHelper = new XMLBeanHelper();
		if(!path.endsWith("." + PluginConstants.PLUGIN_CONTENT_TYPE))
			analysisFile = repository.getFile(path + '/' + fileName);
		else
			analysisFile = repository.getFile(path);
		if (analysisFile != null) {
			analysisFile = repository.updateFile(analysisFile, new SimpleRepositoryFileData(
									new ByteArrayInputStream(xmlBeanHelper.beanToXMLString(analysis).getBytes()),
									LocaleHelper.getSystemEncoding(),
									"application/xml"),
							"Update to existing file");
		} else {
			
			logger.info("Creating new File: " + fileName + " at " + path);
	        
			RepositoryFile parentFile = repository.getFile(path);
			
			logger.info("New File;s Parent " + parentFile.getPath());
			
			analysisFile = new RepositoryFile.Builder(fileName)
					.title(RepositoryFile.ROOT_LOCALE, fileName)
					.description(RepositoryFile.ROOT_LOCALE, fileName).build();
			analysisFile = repository.createFile(parentFile.getId(), analysisFile,
							new SimpleRepositoryFileData(
									new ByteArrayInputStream(xmlBeanHelper.beanToXMLString(analysis).getBytes()),
									LocaleHelper.getSystemEncoding(),
									"application/xml"),
							"New OpenI File Created..");
			
		}
		

		/*String pentahoSolutionRoot = PentahoSystem.getApplicationContext()
				.getSolutionPath("");
		String analysisFilePath = pentahoSolutionRoot + "/"
				+ ActionInfo.buildSolutionPath(solution, path, filename);
		logger.info(analysisFilePath);

		XMLBeanHelper xmlBeanHelper = new XMLBeanHelper();
		
		StringBuffer buf = new StringBuffer(ISolutionRepository.SEPARATOR);
		int result = repository.publish(pentahoSolutionRoot, solution + "/" + path, filename, xmlBeanHelper.beanToXMLString(analysis).getBytes(), overwrite); 
		
		if(result != 3 && result != 0)
			throw new Exception("Error in adding file: Please confirm you have appropriate directory permissions");
	    
		repository.resetRepository(); */
		//xmlBeanHelper.beanToXMLFile(new File(analysisFilePath), analysis);

	}

}
