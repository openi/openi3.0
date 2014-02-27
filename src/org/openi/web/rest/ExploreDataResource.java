package org.openi.web.rest;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.openi.Resources;
import org.openi.service.ExploreDataService;
import org.openi.util.wcf.WCFUtils;
import org.openi.web.rest.exception.RestResourceException;
import org.springframework.stereotype.Component;

/**
 * 
 * @author SUJEN
 * 
 */
@Component
@Path("/openi/api/exploreDataResource")
public class ExploreDataResource {

	private static Logger logger = Logger.getLogger(ExploreDataResource.class);
	
	//ExploreDataResource Exception Messages
	public static final String EDA_ERROR = Resources.getString("EDA_ERROR");
		
	/**
	 * 
	 * @param edaWidgetContentQuery
	 * @param edaWidgetTitle
	 * @param edaWidgetCntWidth
	 * @param edaWidgetCntHeight
	 * @param datasource
	 * @param cube
	 * @param request
	 * @param response
	 * @return
	 */
	@GET
	@Path("edaWidgetContent")
	@Produces("image/png")
	public Response getEdaWidgetContent(
			@QueryParam("edaWidgetTitle") String edaWidgetTitle,
			@QueryParam("datasourceType") String datasourceType,
			@QueryParam("datasourceName") String datasourceName,
			@QueryParam("cubeName") String cubeName,
			@QueryParam("edaWidgetContentQuery") String edaWidgetContentQuery,
			@QueryParam("edaWidgetCntWidth") int edaWidgetCntWidth,
			@QueryParam("edaWidgetCntHeight") int edaWidgetCntHeight,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled()) {
			logger.info("request for eda widget content");
			logger.info("content query : " + edaWidgetContentQuery);
		}

		ResponseBuilder respBuilder = null;
		Response resp = null;
		try {
			File chartCompFile = this.exploreDataService.getEdaWidgetContent(
					edaWidgetContentQuery, edaWidgetTitle, edaWidgetCntWidth,
					edaWidgetCntHeight, datasourceType, datasourceName,
					cubeName, WCFUtils.getRequestContext(request, response));
			respBuilder = Response.ok((Object) chartCompFile);
			respBuilder.header("Content-Disposition", "inline; filename=chart.png");
			resp = respBuilder.build();
			return resp;
		} catch (Exception e) {
			logger.error(EDA_ERROR, e);
			throw new RestResourceException(EDA_ERROR + "\r\n" + e.getStackTrace());
		}
	}

	private ExploreDataService exploreDataService;

	public ExploreDataService getExploreDataService() {
		return exploreDataService;
	}

	public void setExploreDataService(ExploreDataService exploreDataService) {
		this.exploreDataService = exploreDataService;
	}

}
