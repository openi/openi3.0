package org.openi.web.rest;

import java.io.File;
import java.util.Collection;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.openi.Resources;
import org.openi.analysis.SlicerValue;
import org.openi.analysis.export.ExporterFactory;
import org.openi.service.AnalysisService;
import org.openi.util.wcf.WCFUtils;
import org.openi.web.rest.exception.RestResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Rest resource for analysis related actions like export etc.
 * 
 * @author SUJEN
 * 
 */
@Component
@Path("/openianalytics/api/analysisResource")
@Produces("application/*")
public class AnalysisResource {

	private static Logger logger = Logger.getLogger(AnalysisResource.class);

	// AnalysisResource Exception Messages
	public static final String EXPORT_REPORT_ERROR = Resources
			.getString("EXPORT_REPORT_ERROR");
	public static final String PRINT_REPORT_ERROR = Resources
			.getString("PRINT_REPORT_ERROR");
	public static final String APPLY_CHART_PROPS_ERROR = Resources
			.getString("APPLY_CHART_PROPS_ERROR");
	public static final String APPLY_PRINT_SETTINGS_ERROR = Resources
			.getString("APPLY_PRINT_SETTINGS_ERROR");
	public static final String SHOW_HIDE_TABLE_ERROR = Resources
			.getString("SHOW_HIDE_TABLE_ERROR");
	public static final String SHOW_HIDE_CHART_ERROR = Resources
			.getString("SHOW_HIDE_CHART_ERROR");
	public static final String SLICER_VALUE_ERROR = Resources
			.getString("SLICER_VALUE_ERROR");
	public static final String SAVE_ANALYSIS_ERROR = Resources
			.getString("SAVE_ANALYSIS_ERROR");

	/**
	 * 
	 * @param pivotID
	 * @param exportType
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Path("exportAnalysisReport")
	public Response exportAnalysisReport(@QueryParam("pivotID") String pivotID,
			@DefaultValue("0") @QueryParam("exportType") int exportType,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {

		if (logger.isInfoEnabled())
			logger.info("request to export openi analysis report :: format = "
					+ exportType);
		ResponseBuilder resp = null;
		try {
			File reportFile = this.analysisService.getReportFile(pivotID,
					exportType, WCFUtils.getRequestContext(request, response));
			resp = Response.ok((Object) reportFile,
					new MimetypesFileTypeMap().getContentType(reportFile));

			if (exportType == ExporterFactory.PDF)
				resp.header("Content-Disposition",
						"attachment; filename=analysis-report.pdf");
			else if (exportType == ExporterFactory.XLS)
				resp.header("Content-Disposition",
						"attachment; filename=analysis-report.xls");
			return resp.build();

		} catch (Exception e) {
			throw new RestResourceException(EXPORT_REPORT_ERROR + "\r\n"
					+ e.getMessage());
		}

	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("printAnalysisReport")
	public String printAnalysisReport(@QueryParam("pivotID") String pivotID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		try {
			return this.analysisService.printAnalysisReport(pivotID,
					WCFUtils.getRequestContext(request, response));
		} catch (Exception e) {
			throw new RestResourceException(PRINT_REPORT_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param showLegend
	 * @param legendPosition
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("applyChartProperties")
	public Status applyChartProperties(@FormParam("pivotID") String pivotID,
			@FormParam("showLegend") boolean showLegend,
			@FormParam("legendFontFamily") String legendFontFamily,
			@FormParam("legendFontStyle") int legendFontStyle,
			@FormParam("legendFontSize") int legendFontSize,
			@FormParam("legendPosition") int legendPosition,
			@FormParam("showSlicer") boolean showSlicer,
			@FormParam("slicerPosition") int slicerPosition,
			@FormParam("slicerFontFamily") String slicerFontFamily,
			@FormParam("slicerFontStyle") int slicerFontStyle,
			@FormParam("slicerFontSize") int slicerFontSize,
			@FormParam("subTitle") String subTitle,
			@FormParam("chartTitleFontFamily") String chartTitleFontFamily,
			@FormParam("chartTitleFontStyle") int chartTitleFontStyle,
			@FormParam("chartTitleFontSize") int chartTitleFontSize,
			@FormParam("horizAxisLabel") String horizAxisLabel,
			@FormParam("vertAxisLabel") String vertAxisLabel,
			@FormParam("axisLabelFontFamily") String axisLabelFontFamily,
			@FormParam("axisLabelFontStyle") int axisLabelFontStyle,
			@FormParam("axisLabelFontSize") int axisLabelFontSize,
			@FormParam("axisTickLabelFontFamily") String axisTickLabelFontFamily,
			@FormParam("axisTickLabelFontStyle") int axisTickLabelFontStyle,
			@FormParam("axisTickLabelFontSize") int axisTickLabelFontSize,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("request to apply chart properties with pivot ID: "
					+ pivotID + ":: showLegend = " + showLegend
					+ ", legendPosition = " + legendPosition);
		try {
			analysisService.applyChartProperties(pivotID, showLegend,
					legendPosition, legendFontFamily, legendFontStyle,
					legendFontSize, showSlicer, slicerPosition,
					slicerFontFamily, slicerFontStyle, slicerFontSize,
					subTitle, chartTitleFontFamily, chartTitleFontStyle,
					chartTitleFontSize, horizAxisLabel, vertAxisLabel,
					axisLabelFontFamily, axisLabelFontStyle, axisLabelFontSize,
					axisTickLabelFontFamily, axisTickLabelFontStyle, axisTickLabelFontSize,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			throw new RestResourceException(APPLY_CHART_PROPS_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	@POST
	@Produces({ "application/json" })
	@Path("applyPrintSettings")
	public Status applyPrintSettings(@FormParam("pivotID") String pivotID,
			@FormParam("reportTitle") String reportTitle,
			@FormParam("pageOrientation") String pageOrientation,
			@FormParam("pageSize") String pageSize,
			@FormParam("pageHeight") double pageHeight,
			@FormParam("pageWidth") double pageWidth,
			@FormParam("applyTableWidth") boolean applyTableWidth,
			@FormParam("tableWidth") double tableWidth,
			@FormParam("chartOnSeparatePage") boolean chartOnSeparatePage,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("request to apply print properties with pivot ID: "
					+ pivotID);
		try {
			analysisService.applyPrintSettings(pivotID, reportTitle,
					pageOrientation, pageSize, pageHeight, pageWidth,
					applyTableWidth, tableWidth, chartOnSeparatePage,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			throw new RestResourceException(APPLY_PRINT_SETTINGS_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param doShow
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("showHideTable")
	public Status showHideTable(@FormParam("pivotID") String pivotID,
			@FormParam("doShow") boolean doShow,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do showHideTable on olap model with pivot ID: "
					+ pivotID + " :: showTable = " + doShow);
		try {
			analysisService.showHideTable(pivotID, doShow,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			throw new RestResourceException(SHOW_HIDE_TABLE_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param doShow
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("showHideChart")
	public Status showHideChart(@FormParam("pivotID") String pivotID,
			@FormParam("doShow") boolean doShow,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do showHideChart on olap model with pivot ID: "
					+ pivotID + " :: showChart = " + doShow);
		try {
			analysisService.showHideChart(pivotID, doShow,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			throw new RestResourceException(SHOW_HIDE_CHART_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Path("slicerValue")
	public String getSlicerValue(@QueryParam("pivotID") String pivotID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("request for slicer value with pivot ID: " + pivotID);

		try {
			return this.analysisService.getSlicerValue(pivotID,
					WCFUtils.getRequestContext(request, response));
		} catch (Exception e) {
			throw new RestResourceException(SLICER_VALUE_ERROR + "\r\n"
					+ e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param pivotID
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Produces({"application/json"})
	@Path("slicerValueObjects")
	public Collection<SlicerValue> getSlicerValueObjects(@QueryParam("pivotID") String pivotID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("request for slicer value objects with pivot ID: " + pivotID);

		try {
			return this.analysisService.getSlicerValueObjects(pivotID,
					WCFUtils.getRequestContext(request, response));
		} catch (Exception e) {
			throw new RestResourceException(SLICER_VALUE_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	@POST
	@Produces({ "application/json" })
	@Path("saveAnalysisReport")
	public Status saveAnalysisReport(@FormParam("pivotID") String pivotID,
			@FormParam("filename") String filename,
			@FormParam("solution") String solution,
			@FormParam("path") String path, @FormParam("type") String type,
			@FormParam("overwrite") boolean overwrite,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("request to save the analysis with pivot ID: "
					+ pivotID);
		try {
			analysisService.saveAnalysisReport(pivotID, filename, solution,
					path, type, overwrite,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			throw new RestResourceException(SAVE_ANALYSIS_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	private AnalysisService analysisService;

	public AnalysisService getAnalysisService() {
		return analysisService;
	}

	@Autowired
	public void setAnalysisService(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

}
