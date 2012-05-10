package org.openi.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.openi.analysis.Analysis;
import org.openi.chart.EnhancedChartComponent;
import org.openi.chart.EnhancedChartFactory;
import org.openi.navigator.Navigator;
import org.openi.navigator.hierarchy.AbstractCategory;
import org.openi.navigator.hierarchy.HierarchyItem;
import org.openi.navigator.hierarchy.HierarchyNavigator;
import org.openi.navigator.member.MemberNavigator;
import org.openi.service.exception.ServiceException;
import org.openi.util.file.FileUtils;
import org.openi.util.wcf.WCFUtils;
import org.openi.wcf.component.WCFComponentType;
import org.openi.wcf.component.WCFRender;
import org.openi.web.rest.WCFComponentResource;
import org.w3c.dom.Document;

import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * Service Layer class, to be utilized by {@link WCFComponentResource}
 * 
 * @author SUJEN
 * 
 */
public class WCFComponentService {

	private static final Logger logger = Logger
			.getLogger(WCFComponentService.class);

	public String getComponentHTML(String componentType, String pivotID,
			RequestContext context) throws Exception {
		String componentHTML = "";
		if (componentType == null || componentType.equals("")
				|| pivotID == null || pivotID.equals(""))
			componentHTML = "";
		if (componentType.equals(WCFComponentType.TABLE.toString())) {
			componentHTML = getTableComponentHTML(pivotID, context);
		} else if (componentType.equals(WCFComponentType.DRILLTHROUGHTABLE
				.toString())) {
			componentHTML = getDTTableComponentHTML(pivotID, context);
		} else if (componentType.equals(WCFComponentType.CHART.toString())) {
			//componentHTML = getChartComponentHTML(pivotID, context);
		} else if (componentType.equals(WCFComponentType.NAVIGATOR.toString())) {
			componentHTML = getNavComponentHTML(pivotID, context);
		} else if (componentType.equals(WCFComponentType.MEMBERNAVIGATOR
				.toString())) {
			componentHTML = getMemNavComponentHTML(pivotID, context);
		} else if (componentType.equals(WCFComponentType.CHARTPROPERTIESFORM
				.toString())) {
			componentHTML = getChartFormComponentHTML(pivotID, context);
		} else if (componentType
				.equals(WCFComponentType.MDXEDITFORM.toString())) {
			componentHTML = getMDXEditFormComponentHTML(pivotID, context);
		} else if (componentType.equals(WCFComponentType.PRINTFORM.toString())) {
			componentHTML = getPrintFormComponentHTML(pivotID, context);
		} else if (componentType.equals(WCFComponentType.SORTFORM.toString())) {
			componentHTML = getSortFormComponentHTML(pivotID, context);
		}
		return componentHTML;
	}

	private String getSortFormComponentHTML(String pivotID,
			RequestContext context) throws Exception {
		String compID = "sortForm" + pivotID;
		String compString = WCFUtils.componentToHTMLString(new WCFRender(
				WCFUtils.getWCFComponentXSLUri(WCFComponentType.SORTFORM),
				true, compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering sort form component");
		return compString;
	}

	private String getPrintFormComponentHTML(String pivotID,
			RequestContext context) throws Exception {
		String compID = "printForm" + pivotID;
		String compString = WCFUtils.componentToHTMLString(new WCFRender(
				WCFUtils.getWCFComponentXSLUri(WCFComponentType.PRINTFORM),
				true, compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering print form component");
		return compString;
	}

	private String getMDXEditFormComponentHTML(String pivotID,
			RequestContext context) throws Exception {
		String compID = "mdxEditForm" + pivotID;
		String compString = WCFUtils.componentToHTMLString(new WCFRender(
				WCFUtils.getWCFComponentXSLUri(WCFComponentType.MDXEDITFORM),
				true, compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering mdx edit form component");
		return compString;
	}

	private String getChartFormComponentHTML(String pivotID,
			RequestContext context) throws Exception {
		String compID = "chartForm" + pivotID;
		String compString = WCFUtils
				.componentToHTMLString(
						new WCFRender(
								WCFUtils.getWCFComponentXSLUri(WCFComponentType.CHARTPROPERTIESFORM),
								true, compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering chart properties form component");
		return compString;
	}

	/**
	 * returns html for Navigator identified by 'xmlaNav' + pivotID
	 * 
	 * @param pivotID
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private String getNavComponentHTML(String pivotID, RequestContext context)
			throws Exception {
		String compID = "xmlaNav" + pivotID;
		String compString = WCFUtils.componentToHTMLString(new WCFRender(
				WCFUtils.getWCFComponentXSLUri(WCFComponentType.NAVIGATOR),
				true, compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering navigator component");
		return compString;
	}

	private String getMemNavComponentHTML(String pivotID, RequestContext context)
			throws Exception {
		String compID = "xmlaNav" + pivotID + ".membernav";
		String compString = WCFUtils
				.componentToHTMLString(
						new WCFRender(
								WCFUtils.getWCFComponentXSLUri(WCFComponentType.MEMBERNAVIGATOR),
								true, compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering member navigator component");
		return compString;
	}

	/**
	 * 
	 * @param pivotID
	 * @param chartType 
	 * @param chartHeight 
	 * @param chartWidth 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public String getChartComponentHTML(String pivotID, int chartWidth, int chartHeight, int chartType, RequestContext context)
			throws Exception {
		String compID = "chart" + pivotID;
		EnhancedChartComponent chartComponent = (EnhancedChartComponent) (context.getSession().getAttribute(compID));
		if(chartComponent == null)
			throw new Exception("chart component not loaded properly");
		Map loadedAnalysis = (Map) context.getSession().getAttribute(
			"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalysis.get(pivotID);
		analysis.setChartType(chartType);
		analysis.setChartHeight(chartHeight);
		analysis.setChartWidth(chartWidth);
		
		chartComponent.setChartType(chartType);
		chartComponent.setChartHeight(chartHeight);
		chartComponent.setChartWidth(chartWidth);
		
		chartComponent.setLegendPosition(analysis.getLegendPosition());
		chartComponent.setShowLegend(analysis.isShowLegend());
		chartComponent.setLegendFontName(analysis.getLegendFontName());
		chartComponent.setLegendFontStyle(analysis.getLegendFontStyle());
		chartComponent.setLegendFontSize(analysis.getLegendFontSize());

		chartComponent.setShowSlicer(analysis.isShowSlicer());
		chartComponent.setSlicerPosition(analysis.getSlicerPosition());
		chartComponent.setSlicerFontName(analysis.getSlicerFontName());
		chartComponent.setSlicerFontStyle(analysis.getSlicerFontStyle());
		chartComponent.setSlicerFontSize(analysis.getSlicerFontSize());

		chartComponent.setChartTitle(analysis.getChartTitle());
		chartComponent.setFontName(analysis.getFontName());
		chartComponent.setFontStyle(analysis.getFontStyle());
		chartComponent.setFontSize(analysis.getFontSize());

		chartComponent.setHorizAxisLabel(analysis.getHorizAxisLabel());
		chartComponent.setVertAxisLabel(analysis.getVertAxisLabel());
		
		chartComponent.setAxisFontName(analysis.getAxisFontName());
		chartComponent.setAxisFontStyle(analysis.getAxisFontStyle());
		chartComponent.setAxisFontSize(analysis.getAxisFontSize());
		
		chartComponent.setAxisTickFontName(analysis.getAxisTickFontName());
		chartComponent.setAxisTickFontStyle(analysis.getAxisTickFontStyle());
		chartComponent.setAxisTickFontSize(analysis.getAxisTickFontSize());
		
		String compString = WCFUtils.componentToHTMLString(new WCFRender(
				WCFUtils.getWCFComponentXSLUri(WCFComponentType.CHART), true,
				compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering table component");
		return compString;
	}

	/**
	 * returns html for Table identified by 'table' + pivotID
	 * 
	 * @param pivotID
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private String getTableComponentHTML(String pivotID, RequestContext context)
			throws Exception {
		String compID = "table" + pivotID;
		String compString = WCFUtils.componentToHTMLString(new WCFRender(
				WCFUtils.getWCFComponentXSLUri(WCFComponentType.TABLE), true,
				compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering table component");
		return compString;
	}

	private String getDTTableComponentHTML(String pivotID,
			RequestContext context) throws Exception {
		String compID = "xmlaQuery" + pivotID + ".drillthroughtable";
		String compString = WCFUtils
				.componentToHTMLString(
						new WCFRender(
								WCFUtils.getWCFComponentXSLUri(WCFComponentType.DRILLTHROUGHTABLE),
								true, compID), context, pivotID);
		if (logger.isInfoEnabled())
			logger.info("rendering drillthrough table component");
		return compString;
	}

	public void loadMemberNavigator(String hierItemID, String pivotID,
			RequestContext context) throws Exception {
		String compID = "xmlaNav" + pivotID;
		Navigator navigator = (Navigator) context.getSession().getAttribute(
				compID);
		if (navigator == null)
			throw new Exception(
					"heirarchy navigator component identified by \"" + compID
							+ "\" is not found");
		HierarchyNavigator hierNav = navigator.getHierarchyNav();
		HierarchyItem hierItem = hierNav.findHierarchyItemByID(hierItemID);
		AbstractCategory category = hierNav.findItemCategory(hierItem);
		category.itemClicked(context, hierItem);
		MemberNavigator memNav = navigator.getMemberNav();
		context.getSession().setAttribute(memNav.getId(), memNav);
		// hierNav.getHierarchyItemClickHandler().itemClicked(context, item,
		// selection, allowChangeOrder);
	}

	public File getChartComponentFile(String pivotID, int chartWidth,
			int chartHeight, int chartType, RequestContext context)
			throws Exception {
		/*
		Map loadedAnalysis = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalysis.get(pivotID);
		analysis.setChartType(chartType);
		analysis.setChartHeight(chartHeight);
		analysis.setChartWidth(chartWidth);
		OlapModel olapModel = (OlapModel) context.getSession().getAttribute(
				"xmlaQuery" + pivotID);
		File tmpDir = FileUtils.createTempDir();

		String chartFilename = constructChartFilename(
				analysis.getAnalysisTitle(), chartWidth, chartHeight);
		File chartFile = new File(tmpDir, chartFilename);
		createChart(chartFile, analysis, olapModel, chartWidth, chartHeight,
				context.getRequest().getLocale(), null);
		return chartFile;*/
		EnhancedChartComponent chartComponent = (EnhancedChartComponent) (context.getSession().getAttribute("chart" + pivotID));
		if(chartComponent == null)
			throw new Exception("chart component not loaded properly");
		Map loadedAnalysis = (Map) context.getSession().getAttribute(
			"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalysis.get(pivotID);
		analysis.setChartType(chartType);
		analysis.setChartHeight(chartHeight);
		analysis.setChartWidth(chartWidth);
		
		chartComponent.setChartType(chartType);
		chartComponent.setChartHeight(chartHeight);
		chartComponent.setChartWidth(chartWidth);
		
		chartComponent.setLegendPosition(analysis.getLegendPosition());
		chartComponent.setShowLegend(analysis.isShowLegend());
		chartComponent.setLegendFontName(analysis.getLegendFontName());
		chartComponent.setLegendFontStyle(analysis.getLegendFontStyle());
		chartComponent.setLegendFontSize(analysis.getLegendFontSize());

		chartComponent.setShowSlicer(analysis.isShowSlicer());
		chartComponent.setSlicerPosition(analysis.getSlicerPosition());
		chartComponent.setSlicerFontName(analysis.getSlicerFontName());
		chartComponent.setSlicerFontStyle(analysis.getSlicerFontStyle());
		chartComponent.setSlicerFontSize(analysis.getSlicerFontSize());

		chartComponent.setChartTitle(analysis.getChartTitle());
		chartComponent.setFontName(analysis.getFontName());
		chartComponent.setFontStyle(analysis.getFontStyle());
		chartComponent.setFontSize(analysis.getFontSize());

		chartComponent.setHorizAxisLabel(analysis.getHorizAxisLabel());
		chartComponent.setVertAxisLabel(analysis.getVertAxisLabel());
		
		chartComponent.setAxisFontName(analysis.getAxisFontName());
		chartComponent.setAxisFontStyle(analysis.getAxisFontStyle());
		chartComponent.setAxisFontSize(analysis.getAxisFontSize());
		
		chartComponent.setAxisTickFontName(analysis.getAxisTickFontName());
		chartComponent.setAxisTickFontStyle(analysis.getAxisTickFontStyle());
		chartComponent.setAxisTickFontSize(analysis.getAxisTickFontSize());
		
		Document doc = chartComponent.render(context);
		if(doc == null)
			throw new Exception("chart component not loaded properly");
		
		//ignore the document, not returning the chart html, so need to transform chart component doc into html, using chart.xsl 
		
		String chartFilename = chartComponent.getFilename();
		return new File(System.getProperty("java.io.tmpdir"), chartFilename);
	}

	private static String tempFilePrefix = "dashboardchart-";

	private static String constructChartFilename(String filename, int width,
			int height) {
		String name = tempFilePrefix + "_" + filename.replace(".", "_") + "_"
				+ width + "_" + height + System.currentTimeMillis() + ".png";
		return name;
	}

	/**
	 * uses EnhancedChartFactory to create chart based on analysis and olapmodel
	 * instances
	 * 
	 * @param chartFile
	 * @param analysis
	 * @param olapModel
	 * @param width
	 * @param height
	 * @param locale
	 * @param urlGenerator
	 * @throws IOException
	 * @throws OlapException
	 * @throws FileNotFoundException
	 * @throws ServiceException
	 * @throws Exception
	 */
	private void createChart(File chartFile, Analysis analysis,
			OlapModel olapModel, int width, int height, Locale locale,
			CategoryURLGenerator urlGenerator) throws FileNotFoundException,
			OlapException, IOException {
		OutputStream out = new FileOutputStream(chartFile);
		EnhancedChartFactory.createChart(out, analysis, olapModel, width,
				height, locale, null, null, "", true);
		out.close();
	}
}
