package org.openi.util.plugin;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openi.analysis.Analysis;
import org.openi.util.StringUtils;

/**
 * 
 * @author SUJEN
 * 
 */
public class ViewParamResolver {

	private static Logger logger = Logger.getLogger(ViewParamResolver.class);

	public static final String VIEW_PARAM_REGEX = "\\[PARAM_[a-zA-Z]+\\]";

	/**
	 * resolves all the parameters in the analysis view, such as PivotID, etc.
	 * @param viewHTMLString
	 * @param params
	 * @return
	 */
	public String resolveViewParams(String viewHTMLString, Analysis analysis, String pivotID) {
		if (logger.isInfoEnabled())
			logger.info("resolving parameters in the analysis views");
		long startTime = System.currentTimeMillis();
		
		loadViewParams(analysis);

		if(!this.viewParams.containsKey("PARAM_pivotID"))
			this.viewParams.put("PARAM_pivotID", pivotID);
		
		String newString = StringUtils.replaceMatchingParams(viewHTMLString, this.viewParams);
		
		if (logger.isInfoEnabled()) {
			logger.info("view parameters resolved "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
		return newString;
	}
	
	private Map<String, String> viewParams = new HashMap<String, String>();
	
	/**
	 * TO DOs: Figure out another approach to resolve analysis view parameters
	 * need to get rid of this sort of hardcoding approach
	 * 
	 * @param analysis
	 */
	private void loadViewParams(Analysis analysis) {
		if(!this.viewParams.containsKey("PARAM_analysisTitle"))
			this.viewParams.put("PARAM_analysisTitle", analysis.getAnalysisTitle());
		
		if(!this.viewParams.containsKey("PARAM_doSwapAxes"))
			this.viewParams.put("PARAM_doSwapAxes", (analysis.isSwapAxes() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_hideEmptyRowsCols"))
			this.viewParams.put("PARAM_hideEmptyRowsCols", (analysis.isShowNonEmpty() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_setAxisStyle"))
			this.viewParams.put("PARAM_setAxisStyle", (analysis.isLevelStyle() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_showTable"))
			this.viewParams.put("PARAM_showTable", (analysis.isShowTable() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_showChart"))
			this.viewParams.put("PARAM_showChart", (analysis.isShowChart() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_showHideHierarchy"))
			this.viewParams.put("PARAM_showHideHierarchy", (analysis.isDrillPositionEnabled() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_dataReport"))
			this.viewParams.put("PARAM_dataReport", (analysis.isDrillThroughEnabled() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_doReplace"))
			this.viewParams.put("PARAM_doReplace", (analysis.isDrillReplaceEnabled() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_dataReport"))
			this.viewParams.put("PARAM_dataReport", (analysis.isDrillThroughEnabled() == true) ? "checked" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_1"))
			this.viewParams.put("PARAM_chartType_1", (analysis.getChartType() == 1) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_5"))
			this.viewParams.put("PARAM_chartType_5", (analysis.getChartType() == 5) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_3"))
			this.viewParams.put("PARAM_chartType_3", (analysis.getChartType() == 3) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_7"))
			this.viewParams.put("PARAM_chartType_7", (analysis.getChartType() == 7) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_9"))
			this.viewParams.put("PARAM_chartType_9", (analysis.getChartType() == 9) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_1_1"))
			this.viewParams.put("PARAM_chartType_1_1", (analysis.getChartType() == 11) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_1_2"))
			this.viewParams.put("PARAM_chartType_1_2", (analysis.getChartType() == 12) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_1_3"))
			this.viewParams.put("PARAM_chartType_1_3", (analysis.getChartType() == 13) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_1_4"))
			this.viewParams.put("PARAM_chartType_1_4", (analysis.getChartType() == 14) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_1_5"))
			this.viewParams.put("PARAM_chartType_1_5", (analysis.getChartType() == 15) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_chartType_1_6"))
			this.viewParams.put("PARAM_chartType_1_6", (analysis.getChartType() == 16) ? "selected" : "");
		
		if(!this.viewParams.containsKey("PARAM_MDX"))
			this.viewParams.put("PARAM_MDX", analysis.getMdxQuery());
		
	}
	
}
