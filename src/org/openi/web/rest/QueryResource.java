package org.openi.web.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
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
import org.openi.service.OlapQueryService;
import org.openi.util.wcf.WCFUtils;
import org.openi.web.rest.exception.RestResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Rest resource for olap actions like swapAxes, drillThrough etc.
 * 
 * @author SUJEN
 * 
 */
@Component
@Path("/openi/api/queryResource")
public class QueryResource {

	private static Logger logger = Logger.getLogger(QueryResource.class);

	// QueryResource Exception Messages
	public static final String SWAPAXES_ERROR = Resources
			.getString("SWAPAXES_ERROR");
	public static final String HIDE_EMPTY_ROWS_COLS_ERROR = Resources
			.getString("HIDE_EMPTY_ROWS_COLS_ERROR");
	public static final String SET_AXISSTYLE_ERROR = Resources
			.getString("SET_AXISSTYLE_ERROR");
	public static final String SHOW_HIDE_HIERARCHY_ERROR = Resources
			.getString("SHOW_HIDE_HIERARCHY_ERROR");
	public static final String ENABLE_DISABLE_DRILLTHROUGH_ERROR = Resources
			.getString("ENABLE_DISABLE_DRILLTHROUGH_ERROR");
	public static final String ENABLE_DISABLE_REPLACE_ERROR = Resources
			.getString("ENABLE_DISABLE_REPLACE_ERROR");
	public static final String MOVE_HIER_ITEM_ERROR = Resources
			.getString("MOVE_HIER_ITEM_ERROR");
	public static final String DRILL_EXPAND_COLLAPSE_ERROR = Resources
			.getString("DRILL_EXPAND_COLLAPSE_ERROR");
	public static final String DRILLTRHOUGH_ERROR = Resources
			.getString("DRILLTRHOUGH_ERROR");
	public static final String SORT_ERROR = Resources.getString("SORT_ERROR");
	public static final String EXPAND_MEMBER_TREE_ERROR = Resources
			.getString("EXPAND_MEMBER_TREE_ERROR");
	public static final String COLLAPSE_MEMBER_TREE_ERROR = Resources
			.getString("COLLAPSE_MEMBER_TREE_ERROR");
	public static final String MEMBER_SELECTION_ERROR = Resources
			.getString("MEMBER_SELECTION_ERROR");
	public static final String APPLY_MEMBER_SELECTION_ERROR = Resources
			.getString("APPLY_MEMBER_SELECTION_ERROR");
	public static final String REVERT_MEMBER_SELECTION_ERROR = Resources
			.getString("REVERT_MEMBER_SELECTION_ERROR");
	public static final String REMOVE_SLICER_SELECTION_ERROR = Resources
			.getString("REMOVE_SLICER_SELECTION_ERROR");
	public static final String APPLY_MDX_ERROR = Resources
			.getString("APPLY_MDX_ERROR");
	public static final String APPLY_SORT_PROPERTIES_ERROR = Resources
			.getString("APPLY_SORT_PROPERTIES_ERROR");

	/**
	 * 
	 * @param pivotID
	 * @param doSwap
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("swapaxes")
	public Status swapAxes(@FormParam("pivotID") String pivotID,
			@FormParam("doSwap") boolean doSwap,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do SwapAxes on olap model with pivot ID: "
					+ pivotID + " :: doSwapAxes = " + doSwap);
		try {
			olapQueryService.swapAxes(pivotID, doSwap,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(SWAPAXES_ERROR, e);
			throw new RestResourceException(SWAPAXES_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param doHide
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("hideEmptyRowsCols")
	public Status hideEmptyRowsCols(@FormParam("pivotID") String pivotID,
			@FormParam("doHide") boolean doHide,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do hideEmptyRowsCols on olap model with pivot ID: "
					+ pivotID + " :: hideEmptyRowsCols = " + doHide);
		try {
			olapQueryService.hideEmptyRowsCols(pivotID, doHide,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(HIDE_EMPTY_ROWS_COLS_ERROR, e);
			throw new RestResourceException(HIDE_EMPTY_ROWS_COLS_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param doSet
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("setAxisStyle")
	public Status setAxisStyle(@FormParam("pivotID") String pivotID,
			@FormParam("doSet") boolean doSet,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do setAxisStyle on olap model with pivot ID: "
					+ pivotID + " :: setAxisStyle = " + doSet);
		try {
			olapQueryService.setAxisStyle(pivotID, doSet,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(SET_AXISSTYLE_ERROR, e);
			throw new RestResourceException(SET_AXISSTYLE_ERROR + "\r\n"
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
	@Path("showHideHierarchy")
	public Status showHideHierarchy(@FormParam("pivotID") String pivotID,
			@FormParam("doShow") boolean doShow,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do showHideHierarchy on olap model with pivot ID: "
					+ pivotID + " :: setHierarchy = " + doShow);
		try {
			olapQueryService.showHideHierarchy(pivotID, doShow,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(SHOW_HIDE_HIERARCHY_ERROR, e);
			throw new RestResourceException(SHOW_HIDE_HIERARCHY_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param enableDrillThrough
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("enableDisableDrillthrough")
	public Status enableDisableDrillthrough(
			@FormParam("pivotID") String pivotID,
			@FormParam("enableDrillThrough") boolean enableDrillThrough,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do enableDisableDrillthrough on olap model with pivot ID: "
					+ pivotID
					+ " :: enableDisableDrillthrough = "
					+ enableDrillThrough);
		try {
			olapQueryService.enableDisableDrillthrough(pivotID,
					enableDrillThrough,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(ENABLE_DISABLE_DRILLTHROUGH_ERROR, e);
			throw new RestResourceException(ENABLE_DISABLE_DRILLTHROUGH_ERROR
					+ "\r\n" + e.getMessage());
		}
	}

	@POST
	@Produces({ "application/json" })
	@Path("enableDisableReplace")
	public Status enableDisableReplace(@FormParam("pivotID") String pivotID,
			@FormParam("enableReplace") boolean enableReplace,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do enableDisableReplace on olap model with pivot ID: "
					+ pivotID + " :: enableReplace = " + enableReplace);
		try {
			olapQueryService.enableDisableReplace(pivotID, enableReplace,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(ENABLE_DISABLE_REPLACE_ERROR, e);
			throw new RestResourceException(ENABLE_DISABLE_REPLACE_ERROR
					+ "\r\n" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param targetAxis
	 * @param hierarchyName
	 * @param position
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("/{pivotID}/targetAxis/{targetAxis}/hierarchyItem/{hierarchyName}")
	public Status moveItem(@PathParam("pivotID") String pivotID,
			@PathParam("targetAxis") String targetAxis,
			@PathParam("hierarchyName") String hierarchyName,
			@FormParam("position") int position,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {

		if (logger.isInfoEnabled())
			logger.info("olap query request to do move hierarchy item on navigator with pivot ID: "
					+ pivotID
					+ " to "
					+ targetAxis
					+ " at position "
					+ position);
		try {
			olapQueryService.moveItem(pivotID, targetAxis, hierarchyName,
					position, WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(MOVE_HIER_ITEM_ERROR, e);
			throw new RestResourceException(MOVE_HIER_ITEM_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("drillExpandCollapse")
	public Status drillExpandCollapse(@FormParam("pivotID") String pivotID,
			@FormParam("elementID") String elementID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do drillExpandCollapse :: wcfID = "
					+ elementID);
		request.setAttribute(elementID, "");
		try {
			olapQueryService.drillExpandCollapse(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(DRILL_EXPAND_COLLAPSE_ERROR, e);
			throw new RestResourceException(DRILL_EXPAND_COLLAPSE_ERROR
					+ "\r\n" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param request
	 * @param response
	 * @return
	 */
	/*
	 * @POST
	 * 
	 * @Produces({ "application/json" })
	 * 
	 * @Path("drillThrough") public Status drillThrough(@FormParam("pivotID")
	 * String pivotID,
	 * 
	 * @FormParam("elementID") String elementID, final @Context
	 * HttpServletRequest request,
	 * 
	 * @Context HttpServletResponse response) { if (logger.isInfoEnabled())
	 * logger.info("olap query request to do drillthrough :: wcfID = " +
	 * elementID); request.setAttribute(elementID, ""); try {
	 * olapQueryService.drillThrough(pivotID, elementID,
	 * WCFUtils.getRequestContext(request, response)); } catch (Exception e) {
	 * logger.error("Error while doing drillExpandCollapse", e); return
	 * Status.INTERNAL_SERVER_ERROR; } return Status.OK; }
	 */

	@GET
	@Produces({ "application/csv" })
	@Path("drillThrough")
	public Response drillThrough(@QueryParam("pivotID") String pivotID,
			@QueryParam("elementID") String elementID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to do drillthrough :: wcfID = "
					+ elementID);
		request.setAttribute(elementID, "");

		ResponseBuilder resp = null;
		try {
			File dtCSVFile = olapQueryService.drillThrough(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));
			resp = Response.ok((Object) dtCSVFile,
					new MimetypesFileTypeMap().getContentType(dtCSVFile));
			resp.header("Content-Disposition",
					"attachment; filename=dt-report.csv");
			return resp.build();

		} catch (Exception e) {
			logger.error(DRILLTRHOUGH_ERROR, e);
			throw new RestResourceException(DRILLTRHOUGH_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	@GET
	@Produces({ "application/json" })
	@Path("drillThroughSend")
	public Status drillThroughSend(@QueryParam("pivotID") String pivotID,
			@QueryParam("elementID") String elementID,
			@QueryParam("webServiceURL") String webServiceURL,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {

		if (logger.isInfoEnabled())
			logger.info("olap query request to do drillthroughSend :: wcfID = "
					+ elementID + " to Web Service URL: " + webServiceURL);
		request.setAttribute(elementID, "");

		// ResponseBuilder resp = null;
		try {
			File dtCSVFile = olapQueryService.drillThrough(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));

			InputStream stream = new FileInputStream(dtCSVFile);
			FormDataMultiPart part = new FormDataMultiPart().field("file",
					stream, MediaType.TEXT_PLAIN_TYPE);

			WebResource resource = Client.create().resource(webServiceURL);
			String res = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE)
					.post(String.class, part);

			return Status.OK;

		} catch (Exception e) {
			logger.error(DRILLTRHOUGH_ERROR, e);
			throw new RestResourceException(DRILLTRHOUGH_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	public static void main(String[] args) {
		QueryResource qr = new QueryResource();

		Status s = qr.drillThroughSend(null, null,
				"http://localhost:8090/Tests/rest/hello", null, null);

	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("sort")
	public Status sort(@FormParam("pivotID") String pivotID,
			@FormParam("elementID") String elementID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request do sort :: wcfID = " + elementID);
		request.setAttribute(elementID, "");
		try {
			olapQueryService.sort(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(SORT_ERROR, e);
			throw new RestResourceException(SORT_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("expandMemberTree")
	public Status expandMemberTree(@FormParam("pivotID") String pivotID,
			@FormParam("elementID") String elementID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to expand Member Tree :: wcfID = "
					+ elementID);
		request.setAttribute(elementID, "");
		try {
			olapQueryService.expandMemberTree(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(EXPAND_MEMBER_TREE_ERROR, e);
			throw new RestResourceException(EXPAND_MEMBER_TREE_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	@POST
	@Produces({ "application/json" })
	@Path("collapseMemberTree")
	public Status collapseMemberTree(@FormParam("pivotID") String pivotID,
			@FormParam("elementID") String elementID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to collapse Member Tree :: wcfID = "
					+ elementID);
		request.setAttribute(elementID, "");
		try {
			olapQueryService.collapseMemberTree(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(COLLAPSE_MEMBER_TREE_ERROR, e);
			throw new RestResourceException(COLLAPSE_MEMBER_TREE_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	@POST
	@Produces({ "application/json" })
	@Path("selectDeselectMember")
	public Status selectDeselectMember(@FormParam("pivotID") String pivotID,
			@FormParam("elementID") String elementID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to select/deselect Member :: wcfID = "
					+ elementID);
		request.setAttribute(elementID, "");
		try {
			olapQueryService.selectDeselectMember(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(MEMBER_SELECTION_ERROR, e);
			throw new RestResourceException(MEMBER_SELECTION_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	@POST
	@Produces({ "application/json" })
	@Path("applyMemberSelection")
	public Status applyMemberSelection(@FormParam("pivotID") String pivotID,
			@FormParam("elementID") String elementID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to apply member selection :: wcfID = "
					+ elementID);
		request.setAttribute(elementID, "");
		try {
			olapQueryService.applyMemberSelection(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(APPLY_MEMBER_SELECTION_ERROR, e);
			throw new RestResourceException(APPLY_MEMBER_SELECTION_ERROR
					+ "\r\n" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("revertMemberSelection")
	public Status revertMemberSelection(@FormParam("pivotID") String pivotID,
			@FormParam("elementID") String elementID,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to revert member selection :: wcfID = "
					+ elementID);
		request.setAttribute(elementID, "");
		try {
			olapQueryService.revertMemberSelection(pivotID, elementID,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(REVERT_MEMBER_SELECTION_ERROR, e);
			throw new RestResourceException(REVERT_MEMBER_SELECTION_ERROR
					+ "\r\n" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("removeSlicerSelection")
	public Status removeSlicerSelection(@FormParam("pivotID") String pivotID,
			@FormParam("dimensionName") String dimensionName,
			@FormParam("hierarchyName") String hierarchyName,
			@FormParam("levelName") String levelName,
			@FormParam("memberName") String memberName,
			@FormParam("slicerUniqueName") String slicerUniqueName,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to remove selected slicer member selection :: uniqueName = "
					+ slicerUniqueName);
		try {
			olapQueryService.removeSlicerSelection(pivotID, dimensionName,
					hierarchyName, levelName, memberName, slicerUniqueName,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(REMOVE_SLICER_SELECTION_ERROR, e);
			throw new RestResourceException(REMOVE_SLICER_SELECTION_ERROR
					+ "\r\n" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("applyMDX")
	public Status applyMDX(@FormParam("pivotID") String pivotID,
			@FormParam("newMDXQuery") String newMDXQuery,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		if (logger.isInfoEnabled())
			logger.info("olap query request to apply new mdx on olap model with pivot ID: "
					+ pivotID);
		try {
			olapQueryService.applyMDX(pivotID, newMDXQuery,
					WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(APPLY_MDX_ERROR, e);
			throw new RestResourceException(APPLY_MDX_ERROR + "\r\n"
					+ e.getMessage());
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param sortMode
	 * @param rowsCount
	 * @param showProps
	 * @param request
	 * @param response
	 * @return
	 */
	@POST
	@Produces({ "application/json" })
	@Path("applySortProperties")
	public Status applySortProperties(@FormParam("pivotID") String pivotID,
			@FormParam("sortMode") int sortMode,
			@FormParam("rowsCount") int rowsCount,
			@FormParam("showProps") boolean showProps,
			final @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		logger.info(pivotID + " " + sortMode + " " + rowsCount + " "
				+ showProps);
		if (logger.isInfoEnabled())
			logger.info("olap query request to apply sort properties on olap model with pivot ID: "
					+ pivotID);
		try {
			olapQueryService.applySortProperties(pivotID, sortMode, rowsCount,
					showProps, WCFUtils.getRequestContext(request, response));
			return Status.OK;
		} catch (Exception e) {
			logger.error(APPLY_SORT_PROPERTIES_ERROR, e);
			throw new RestResourceException(APPLY_SORT_PROPERTIES_ERROR
					+ "\r\n" + e.getMessage());
		}
	}

	private OlapQueryService olapQueryService;

	public OlapQueryService getOlapQueryService() {
		return olapQueryService;
	}

	@Autowired
	public void setOlapQueryService(OlapQueryService olapQueryService) {
		this.olapQueryService = olapQueryService;
	}

}
