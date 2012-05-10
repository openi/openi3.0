package org.openi.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openi.analysis.Analysis;
import org.openi.analysis.AnalysisHelper;
import org.openi.navigator.Navigator;
import org.openi.service.exception.ServiceException;
import org.openi.table.DrillExpandMemberUI;
import org.openi.table.DrillExpandPositionUI;
import org.openi.table.DrillExpandUI;
import org.openi.table.DrillReplaceUI;
import org.openi.table.DrillThroughUI;
import org.openi.table.SortRankUI;
import org.openi.util.file.FileUtils;
import org.openi.web.rest.QueryResource;

import org.openi.navigator.hierarchy.AbstractCategory;
import org.openi.navigator.hierarchy.HierarchyItem;
import org.openi.navigator.hierarchy.HierarchyNavigator;
import org.openi.navigator.member.MemberNavigator;
import org.openi.navigator.member.SelectionMgr.SelectionHandler;
import org.openi.olap.drillthrough.DrillthroughHelper;
import org.openi.olap.mondrian.MondrianMember;
import org.openi.olap.xmla.XMLA_Member;

import org.w3c.dom.Element;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.navi.MdxQuery;
import com.tonbeller.jpivot.table.TableComponent;
import com.tonbeller.jpivot.table.navi.AxisStyleUI;
import com.tonbeller.jpivot.table.navi.NonEmptyUI;
import com.tonbeller.jpivot.table.navi.SwapAxesUI;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestListener;
import com.tonbeller.wcf.selection.SelectionModel;
import com.tonbeller.wcf.table.ITableComponent;
import com.tonbeller.wcf.ui.RadioButton;

/**
 * service layer class to be utilized by {@link QueryResource} for handling olap
 * query actions like swapAxes, drillExpand and collapse, hide empty rows/cols,
 * drill through etc.
 * 
 * @author SUJEN
 * 
 */
public class OlapQueryService {

	private static Logger logger = Logger.getLogger(OlapQueryService.class);

	/**
	 * 
	 * @param pivotID
	 * @param doSwap
	 * @param context
	 * @throws Exception
	 */
	public void swapAxes(String pivotID, boolean doSwap, RequestContext context)
			throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("MDX before swap: "
					+ AnalysisHelper.getMDXFromOlapModel(tableComp
							.getOlapModel()));
		}

		((SwapAxesUI) tableComp.getExtensions().get(SwapAxesUI.ID))
				.setButtonPressed(doSwap);

		if (logger.isDebugEnabled()) {
			logger.debug("MDX after swap: "
					+ AnalysisHelper.getMDXFromOlapModel(tableComp
							.getOlapModel()));
		}

	}

	/**
	 * 
	 * @param pivotID
	 * @param doHide
	 * @param context
	 * @throws Exception
	 */
	public void hideEmptyRowsCols(String pivotID, boolean doHide,
			RequestContext context) throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}

		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		analysis.setShowNonEmpty(doHide);
		((NonEmptyUI) tableComp.getExtensions().get(NonEmptyUI.ID))
				.setButtonPressed(doHide);

	}

	/**
	 * 
	 * @param pivotID
	 * @param doSet
	 * @param context
	 * @throws Exception
	 */
	public void setAxisStyle(String pivotID, boolean doSet,
			RequestContext context) throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}

		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		analysis.setLevelStyle(doSet);

		((AxisStyleUI) tableComp.getExtensions().get(AxisStyleUI.ID))
				.setLevelStyle(doSet);
	}

	/**
	 * 
	 * @param pivotID
	 * @param doShow
	 * @param context
	 * @throws Exception
	 */
	public void showHideHierarchy(String pivotID, boolean doShow,
			RequestContext context) throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}

		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		analysis.setDrillPositionEnabled(doShow);

		DrillExpandPositionUI drillExpandPositionUI = (DrillExpandPositionUI) tableComp
				.getExtensions().get(DrillExpandPositionUI.ID);
		drillExpandPositionUI.setEnabled(doShow);
	}

	/**
	 * 
	 * @param pivotID
	 * @param enableDrillThrough
	 * @param context
	 * @throws Exception
	 */
	public void enableDisableDrillthrough(String pivotID,
			boolean enableDrillThrough, RequestContext context)
			throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}

		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		analysis.setDrillThroughEnabled(enableDrillThrough);

		DrillThroughUI drillThroughUI = (DrillThroughUI) tableComp
				.getExtensions().get(DrillThroughUI.ID);
		drillThroughUI.setEnabled(enableDrillThrough);
	}

	/**
	 * 
	 * @param pivotID
	 * @param enableReplace
	 * @param context
	 * @throws Exception
	 */
	public void enableDisableReplace(String pivotID, boolean enableReplace,
			RequestContext context) throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}

		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		analysis.setDrillReplaceEnabled(enableReplace);

		DrillReplaceUI drillReplaceUI = (DrillReplaceUI) tableComp
				.getExtensions().get(DrillReplaceUI.ID);
		drillReplaceUI.setEnabled(enableReplace);
	}

	/**
	 * 
	 * @param pivotID
	 * @param targetAxis
	 * @param hierarchyName
	 * @param position
	 * @param context
	 * @throws Exception
	 */
	public void moveItem(String pivotID, String targetAxis,
			String hierarchyName, int position, RequestContext context)
			throws Exception {
		String navCompID = "xmlaNav" + pivotID;
		Navigator navComp = (Navigator) (context.getSession()
				.getAttribute(navCompID));
		if (navComp == null) {
			throw new Exception("Navigator component identified by \""
					+ navCompID + "\" is not found");
		}
		HierarchyNavigator hierNav = navComp.getHierarchyNav();
		HierarchyItem hierItem = hierNav.findHierarchyItem(hierarchyName);
		AbstractCategory ac = hierNav.findCategoryByName(targetAxis.substring(
				0, targetAxis.indexOf("-category")));
		boolean status = navComp.getHierarchyNav().moveHierarchyItem(hierItem,
				ac, position);
		if (status)
			navComp.getHierarchyNav().getAcceptHandler().request(context);
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param requestContext
	 * @throws Exception
	 */
	public void drillExpandCollapse(String pivotID, String elementID,
			RequestContext requestContext) throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (requestContext
				.getSession().getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}
		DrillExpandUI drillExpandPositionUI = (DrillExpandPositionUI) tableComp
				.getExtensions().get(DrillExpandPositionUI.ID);
		DrillExpandUI drillExpandMemberUI = (DrillExpandMemberUI) tableComp
				.getExtensions().get(DrillExpandMemberUI.ID);
		DrillReplaceUI drillReplaceUI = (DrillReplaceUI) tableComp
				.getExtensions().get(DrillReplaceUI.ID);

		Map expandPositionHandlers = drillExpandPositionUI.getExpandHandlers();
		Map collapsePositionHandlers = drillExpandPositionUI
				.getCollapseHandlers();

		Map expandMemberHandlers = drillExpandMemberUI.getExpandHandlers();
		Map collapseMemberHandlers = drillExpandMemberUI.getCollapseHandlers();

		Map replaceExpandHandlers = drillReplaceUI.getExpandHandlers();
		Map replaceCollapseHandlers = drillReplaceUI.getCollapseHandlers();

		if (expandPositionHandlers != null
				&& expandPositionHandlers.get(elementID) != null) {
			RequestListener expandHandler = (RequestListener) expandPositionHandlers
					.get(elementID);
			expandHandler.request(requestContext);
		} else if (collapsePositionHandlers != null
				&& collapsePositionHandlers.get(elementID) != null) {
			RequestListener collapseHandler = (RequestListener) collapsePositionHandlers
					.get(elementID);
			collapseHandler.request(requestContext);
		} else if (expandMemberHandlers != null
				&& expandMemberHandlers.get(elementID) != null) {
			RequestListener expandHandler = (RequestListener) expandMemberHandlers
					.get(elementID);
			expandHandler.request(requestContext);
		} else if (collapseMemberHandlers != null
				&& collapseMemberHandlers.get(elementID) != null) {
			RequestListener collapseHandler = (RequestListener) collapseMemberHandlers
					.get(elementID);
			collapseHandler.request(requestContext);
		} else if (replaceExpandHandlers != null
				&& replaceExpandHandlers.get(elementID) != null) {
			RequestListener expandHandler = (RequestListener) replaceExpandHandlers
					.get(elementID);
			expandHandler.request(requestContext);
		} else if (replaceCollapseHandlers != null
				&& replaceCollapseHandlers.get(elementID) != null) {
			RequestListener collapseHandler = (RequestListener) replaceCollapseHandlers
					.get(elementID);
			collapseHandler.request(requestContext);
		}
	}

	/*
	 * public void drillThrough(String pivotID, String elementID, RequestContext
	 * requestContext) throws Exception { String tableCompID = "table" +
	 * pivotID; TableComponent tableComp = (TableComponent) (requestContext
	 * .getSession().getAttribute(tableCompID)); if (tableComp == null) {
	 * logger.error("Error while doing drillThrough"); return; } DrillThroughUI
	 * drillThroughUI = (DrillThroughUI) tableComp
	 * .getExtensions().get(DrillThroughUI.ID); Map drillThroughHandlers =
	 * drillThroughUI.getDrillThroughHandlers(); Iterator itr =
	 * drillThroughHandlers.keySet().iterator(); if (drillThroughHandlers !=
	 * null && drillThroughHandlers.get(elementID) != null) { RequestListener
	 * drillThroughHandler = (RequestListener) drillThroughHandlers
	 * .get(elementID); drillThroughHandler.request(requestContext); if
	 * (logger.isInfoEnabled()) logger.info("drill through handler requested");
	 * }
	 * 
	 * }
	 */

	public File drillThrough(String pivotID, String elementID,
			RequestContext context) throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}
		DrillThroughUI drillThroughUI = (DrillThroughUI) tableComp
				.getExtensions().get(DrillThroughUI.ID);
		if (drillThroughUI == null) {
			logger.error("Drill Through Extension is disabled");
			return null;
		}
		Map drillThroughHandlers = drillThroughUI.getDrillThroughHandlers();
		Iterator itr = drillThroughHandlers.keySet().iterator();
		if (drillThroughHandlers != null
				&& drillThroughHandlers.get(elementID) != null) {
			RequestListener drillThroughHandler = (RequestListener) drillThroughHandlers
					.get(elementID);
			drillThroughHandler.request(context);
			if (logger.isInfoEnabled())
				logger.info("drill through handler requested");
		}

		File tmpDir = FileUtils.createTempDir();
		String dtFilename = "drillthrough-result.csv";
		File dtResultFile = new File(tmpDir, dtFilename);
		OutputStream out = new FileOutputStream(dtResultFile);
		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);

		final String drillTableRef = tableComp.getOlapModel().getID()
				+ ".drillthroughtable";
		ITableComponent dtTableComp = (ITableComponent) context.getSession()
				.getAttribute(drillTableRef);

		if (dtTableComp == null) {
			logger.error("Drillthrough table component is not loaded properly in the session");
			return null;
		}
		DrillthroughHelper.drillthroughTableModelToCSV(dtTableComp.getModel(),
				out);
		return dtResultFile;
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param requestContext
	 * @throws Exception
	 */
	public void sort(String pivotID, String elementID, RequestContext context)
			throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}
		SortRankUI sortRankUI = (SortRankUI) tableComp.getExtensions().get(
				SortRankUI.ID);
		Map sortHandlers = sortRankUI.getSortHandlers();
		if (sortHandlers != null && sortHandlers.get(elementID) != null) {
			RequestListener sortHandler = (RequestListener) sortHandlers
					.get(elementID);

			sortHandler.request(context);

		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param context
	 * @throws Exception
	 */
	public void expandMemberTree(String pivotID, String elementID,
			RequestContext context) throws Exception {
		MemberNavigator memberNav = (MemberNavigator) context.getSession()
				.getAttribute("xmlaNav" + pivotID + ".membernav");
		if (memberNav == null) {
			throw new Exception("member navigator component identified by \""
					+ "xmlaNav" + pivotID + ".membernav" + "\" is not found");
		}
		Map expandHanlders = memberNav.getExpandHandlers();
		Iterator itr = expandHanlders.keySet().iterator();
		if (expandHanlders != null && expandHanlders.get(elementID) != null) {
			RequestListener expandHandler = (RequestListener) expandHanlders
					.get(elementID);
			expandHandler.request(context);
		}

	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param context
	 * @throws Exception
	 */
	public void collapseMemberTree(String pivotID, String elementID,
			RequestContext context) throws Exception {
		MemberNavigator memberNav = (MemberNavigator) context.getSession()
				.getAttribute("xmlaNav" + pivotID + ".membernav");
		if (memberNav == null) {
			throw new Exception("member navigator component identified by \""
					+ "xmlaNav" + pivotID + ".membernav" + "\" is not found");
		}
		Map collapseHandlers = memberNav.getCollapseHandlers();
		Iterator itr = collapseHandlers.keySet().iterator();
		if (collapseHandlers != null && collapseHandlers.get(elementID) != null) {
			RequestListener collapseHandler = (RequestListener) collapseHandlers
					.get(elementID);
			collapseHandler.request(context);
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param context
	 * @throws Exception
	 */
	public void selectDeselectMember(String pivotID, String elementID,
			RequestContext context) throws Exception {
		MemberNavigator memberNav = (MemberNavigator) context.getSession()
				.getAttribute("xmlaNav" + pivotID + ".membernav");
		if (memberNav == null)
			throw new Exception("member navigator component identified by \""
					+ "xmlaNav" + pivotID + ".membernav" + "\" is not found");
		if (memberNav.getSelectionModel().getMode() == SelectionModel.SINGLE_SELECTION_BUTTON) {
			Map singleSelectHandlers = memberNav.getSelectionMgr()
					.getSingleSelectHandlers();
			if (singleSelectHandlers != null
					&& singleSelectHandlers.get(elementID) != null) {
				RequestListener singleSelectHandler = (RequestListener) singleSelectHandlers
						.get(elementID);
				singleSelectHandler.request(context);
			}

		} else if (memberNav.getSelectionModel().getMode() == SelectionModel.MULTIPLE_SELECTION_BUTTON) {
			Map multipleSelectHandlers = memberNav.getSelectionMgr()
					.getMultipleSelectHandlers();
			if (multipleSelectHandlers != null
					&& multipleSelectHandlers.get(elementID) != null) {
				RequestListener multipleSelectHandler = (RequestListener) multipleSelectHandlers
						.get(elementID);
				multipleSelectHandler.request(context);
			}
		} else {
			List selectionHandlers = memberNav.getSelectionMgr()
					.getSelectionHandlers();
			Iterator itr = selectionHandlers.iterator();
			while (itr.hasNext()) {
				SelectionHandler selectHandler = (SelectionHandler) itr.next();
				Element elem = (Element) selectHandler.getElem();
				if (RadioButton.getId(elem).equals(elementID)) {
					selectHandler.validate(context);
					break;
				}
			}
		}

	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param context
	 * @throws ServiceException
	 * @throws Exception
	 */
	public void applyMemberSelection(String pivotID, String elementID,
			RequestContext context) throws Exception {
		String navCompID = "xmlaNav" + pivotID;
		Navigator navComp = (Navigator) (context.getSession()
				.getAttribute(navCompID));
		if (navComp == null)
			throw new Exception(
					"heirarchy navigator component identified by \""
							+ navCompID + "\" is not found");
		MemberNavigator memberNav = navComp.getMemberNav();
		RequestListener okHandler = memberNav.getOkHandler();
		okHandler.request(context);
		navComp.getHierarchyNav().getAcceptHandler().request(context);

	}

	/**
	 * 
	 * @param pivotID
	 * @param elementID
	 * @param context
	 * @throws ServiceException
	 * @throws Exception
	 */
	public void revertMemberSelection(String pivotID, String elementID,
			RequestContext context) throws Exception {
		String navCompID = "xmlaNav" + pivotID;
		Navigator navComp = (Navigator) (context.getSession()
				.getAttribute(navCompID));
		if (navComp == null)
			throw new Exception(
					"heirarchy navigator component identified by \""
							+ navCompID + "\" is not found");
		MemberNavigator memberNav = navComp.getMemberNav();
		RequestListener cancelHandler = memberNav.getCancelHandler();
		cancelHandler.request(context);
		navComp.getHierarchyNav().getRevertHandler().request(context);
	}

	/**
	 * 
	 * @param pivotID
	 * @param slicerUniqueName
	 * @param requestContext
	 */
	public void removeSlicerSelection(String pivotID, String dimensionName,
			String hierarchyName, String levelName, String memberName,
			String uniqueName, RequestContext context) throws Exception {
		String navCompID = "xmlaNav" + pivotID;
		Navigator navComp = (Navigator) (context.getSession()
				.getAttribute(navCompID));
		if (navComp == null) {
			throw new Exception("Navigator component identified by \""
					+ navCompID + "\" is not found");
		}
		HierarchyNavigator hierNav = navComp.getHierarchyNav();
		HierarchyItem hierItem = hierNav.findHierarchyItem(hierarchyName);
		List slicerSelection = hierItem.getSlicerSelection();
		List updatedSlicerSelection = new ArrayList();
		if (slicerSelection != null && slicerSelection.size() > 0) {
			Iterator selectionItr = slicerSelection.iterator();
			while (selectionItr.hasNext()) {
				Member slicerMember = (Member) selectionItr.next();
				String slicerMemberUniqueName = "";
				if (slicerMember instanceof XMLA_Member)
					slicerMemberUniqueName = ((XMLA_Member) slicerMember)
							.getUniqueName();
				else if (slicerMember instanceof MondrianMember)
					slicerMemberUniqueName = ((MondrianMember) slicerMember)
							.getUniqueName();
				logger.info("slicerMemberUniqueName = " + slicerMemberUniqueName);
				logger.info("uniqueName = " + uniqueName);
				if (!slicerMemberUniqueName.equals(uniqueName))
					updatedSlicerSelection.add(slicerMember);
			}
			hierItem.setSlicerSelection(updatedSlicerSelection);
			navComp.getHierarchyNav().getAcceptHandler().request(context);
		}
	}

	/**
	 * 
	 * @param pivotID
	 * @param newMDXQuery
	 * @param context
	 * @throws ServiceException
	 * @throws Exception
	 */
	public void applyMDX(String pivotID, String newMDXQuery,
			RequestContext context) throws Exception {
		Map loadedAnalyses = (Map) context.getSession().getAttribute(
				"loadedAnalyses");
		Analysis analysis = (Analysis) loadedAnalyses.get(pivotID);
		analysis.setMdxQuery(newMDXQuery);

		OlapModel olapModel = (OlapModel) context.getSession().getAttribute(
				"xmlaQuery" + pivotID);
		if (olapModel == null)
			throw new Exception("olap model identified by \"" + "xmlaQuery"
					+ pivotID + "\" is not found");
		if (logger.isDebugEnabled()) {
			logger.debug("MDX before applying new mdx: "
					+ AnalysisHelper.getMDXFromOlapModel(olapModel));
		}
		MdxQuery mdxQueryModel = (MdxQuery) olapModel.getExtension("mdxQuery");
		mdxQueryModel.setMdxQuery(newMDXQuery);
	}

	/**
	 * 
	 * @param pivotID
	 * @param sortMode
	 * @param rowsCount
	 * @param showProps
	 * @param requestContext
	 * @throws ServiceException
	 * @throws Exception
	 */
	public void applySortProperties(String pivotID, int sortMode,
			int rowsCount, boolean showProps, RequestContext requestContext)
			throws Exception {
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (requestContext
				.getSession().getAttribute(tableCompID));
		if (tableComp == null) {
			throw new Exception("table component identified by \""
					+ tableCompID + "\" is not found");
		}
		SortRankUI sortRankUI = (SortRankUI) tableComp.getExtensions().get(
				SortRankUI.ID);
		sortRankUI.setTopBottomCount(rowsCount);
		sortRankUI.setSortMode(sortMode);
		tableComp.getRowAxisBuilder().getAxisConfig().getPropertyConfig()
				.setShowProperties(showProps);
	}
}
