package org.openi.web.rest;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.openi.Resources;
import org.openi.service.WCFComponentService;
import org.openi.util.wcf.WCFUtils;
import org.openi.wcf.component.WCFComponentType;
import org.openi.web.rest.exception.RestResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Rest resource for wcf components like TABLE, 
 * CHART, NAVIGATOR etc.
 * 
 * @author SUJEN
 * 
 */
@Component
@Path("/openi/api/wcfCompResource")
public class WCFComponentResource {

	private static Logger logger = Logger.getLogger(WCFComponentResource.class);
	
	//WCFComponentResource Exception Messages
	public static final String GET_COMPONENT_HTML_ERROR = Resources.getString("GET_COMPONENT_HTML_ERROR");
	public static final String GET_MEMNAV_COMPONENT_HTML_ERROR = Resources.getString("GET_MEMNAV_COMPONENT_HTML_ERROR");
	public static final String GET_CHART_COMPONENT_ERROR = Resources.getString("GET_CHART_COMPONENT_ERROR");

	/**
	 * 
	 * @param componentType
	 * @param pivotID
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Path("wcfComp")
	public String getComponentHTML(
			@QueryParam("componentType") String componentType,
			@QueryParam("pivotID") String pivotID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("component HTML request for component "
					+ componentType + " with pivot ID: " + pivotID);

		try {
			return this.componentService.getComponentHTML(componentType, pivotID,
					WCFUtils.getRequestContext(request, response));
		} catch (Exception e) {
			logger.error(GET_COMPONENT_HTML_ERROR, e);
			throw new RestResourceException(GET_COMPONENT_HTML_ERROR + "\r\n" + e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param componentType
	 * @param pivotID
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Path("wcfMemNavComp")
	public String getMemberNavComponentHTML(
			@QueryParam("hierItemID") String hierItemID,
			@QueryParam("pivotID") String pivotID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("component HTML request for member navigator component with pivot ID: " + pivotID);
		try {
			this.componentService.loadMemberNavigator(hierItemID, pivotID, WCFUtils.getRequestContext(request, response));
			return this.componentService.getComponentHTML(WCFComponentType.MEMBERNAVIGATOR.toString(), pivotID,
					WCFUtils.getRequestContext(request, response));
		} catch (Exception e) {
			logger.error(GET_MEMNAV_COMPONENT_HTML_ERROR, e);
			throw new RestResourceException(GET_MEMNAV_COMPONENT_HTML_ERROR + "\r\n" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param chartWidth
	 * @param chartHeight
	 * @param chartType
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Path("wcfChartComp")
	@Produces("image/png")
	public Response getChartComponent(@QueryParam("pivotID") String pivotID,
			@DefaultValue("800") @QueryParam("chartWidth") int chartWidth,
			@DefaultValue("350") @QueryParam("chartHeight") int chartHeight,
			@DefaultValue("3") @QueryParam("chartType") int chartType,
			@DefaultValue("true") @QueryParam("inline") String inline,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {

		if (logger.isInfoEnabled())
			logger.info("chart component HTML request for component with pivot ID: " + pivotID);
		
		ResponseBuilder resp = null;
		try {
			File chartCompFile = this.componentService.getChartComponentFile(
					pivotID, chartWidth, chartHeight, chartType,
					WCFUtils.getRequestContext(request, response));
			resp = Response.ok((Object) chartCompFile);
			resp.header("Content-Disposition",
					(inline.equals("true")) ? "inline" : "attachment" + "; filename=chart.png");
			return resp.build();
		} catch (Exception e) {
			logger.error(GET_CHART_COMPONENT_ERROR, e);
			throw new RestResourceException(GET_CHART_COMPONENT_ERROR + "\r\n" + e.getMessage());
		}

	}
	
	public String getChartComponentHTML(@QueryParam("pivotID") String pivotID,
			@DefaultValue("800") @QueryParam("chartWidth") int chartWidth,
			@DefaultValue("350") @QueryParam("chartHeight") int chartHeight,
			@DefaultValue("3") @QueryParam("chartType") int chartType,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("component HTML request for chart component with pivot ID: " + pivotID);

		try {
			return this.componentService.getChartComponentHTML(pivotID, chartWidth, chartHeight, chartType,
					WCFUtils.getRequestContext(request, response));
		} catch (Exception e) {
			logger.error(GET_COMPONENT_HTML_ERROR, e);
			throw new RestResourceException(GET_COMPONENT_HTML_ERROR + "\r\n" + e.getMessage());
		}
	}

	private WCFComponentService componentService;

	public WCFComponentService getComponentService() {
		return componentService;
	}

	@Autowired
	public void setComponentService(WCFComponentService componentService) {
		this.componentService = componentService;
	}
}
