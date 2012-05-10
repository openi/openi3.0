package org.openi.analysis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Domain/Model Class for an Analysis
 * 
 * @author SUJEN
 * 
 */
public class Analysis implements Serializable, Cloneable {

	private String analysisTitle = "AnalysisTitle";
	private String mdxQuery = null;
	private String dataSourceName = null;
	private String dsType = "XMLA";
	private String colorPaletteName = null;
	private double foregroundAlpha = 1.0; // default to no transparency
	private boolean showPareto = false;
	private boolean showChart = true;
	private boolean showTable = true;
	private String chartTitle = "";
	private int chartType = 1; // vertical bar
	private int chartHeight = 300;
	private int chartWidth = 500;
	private String fontName = "Verdana";
	private int fontStyle = 1; // bold
	private int fontSize = 18;
	private boolean useChartSize = false; // chart size based on saved width and
											// height.
	// legend
	private boolean showLegend = true;
	private String legendFontName = "Verdana";
	private int legendFontStyle = 0; // plain
	private int legendFontSize = 11;
	private int legendPosition = 2; // right side of chart

	// slicer
	private boolean showSlicer = true;
	private int slicerPosition = 1; // top
	private int slicerAlignment = 4; // center
	private String slicerFontName = "Verdana";
	private int slicerFontStyle = 0; // plain
	private int slicerFontSize = 11;

	// axes
	private String axisFontName = "Verdana";
	private int axisFontStyle = 0; // plain
	private int axisFontSize = 11;
	private String horizAxisLabel = "";
	private String vertAxisLabel = "";
	private String axisTickFontName = "Verdana";
	private int axisTickFontStyle = 0; // plain
	private int axisTickFontSize = 11;
	private boolean drillThroughEnabled = false;
	private boolean chartDrillThroughEnabled = false;
	private boolean drillReplaceEnabled = false;
	private boolean drillPositionEnabled = true;

	private int tickLabelRotate = 30; // 30 degree
	private int bgColorR = 255;
	private int bgColorG = 255;
	private int bgColorB = 255;

	// swap axis
	private transient boolean swapAxes = false;
	// show non empty
	private boolean showNonEmpty = false;
	// show parents
	private boolean levelStyle = false;

	// hide spans
	private boolean hideSpans = false;
	private boolean showCustomLabels = false;

	// added for drillthrough
	private String drillthroughSQL = "";
	private String drillthroughDatasource = "";
	private String description;

	private Map<String, String> mdxQueryMap;

	private Set<String> owners = null;

	// view only mode feature
	private boolean viewOnlyMode = false;

	private boolean suppressCrowdedLabels = true;

	public boolean isSuppressCrowdedLabels() {
		return suppressCrowdedLabels;
	}

	public void setSuppressCrowdedLabels(boolean suppressCrowdedLabels) {
		this.suppressCrowdedLabels = suppressCrowdedLabels;
	}

	public Analysis() {
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Analysis cloned = new Analysis();
		cloned.analysisTitle = this.analysisTitle;
		cloned.axisFontName = this.axisFontName;
		cloned.axisFontSize = this.axisFontSize;
		cloned.axisFontStyle = this.axisFontStyle;
		cloned.axisTickFontName = this.axisTickFontName;
		cloned.axisTickFontSize = this.axisTickFontSize;
		cloned.axisTickFontStyle = this.axisTickFontStyle;
		cloned.bgColorB = this.bgColorB;
		cloned.bgColorG = this.bgColorG;
		cloned.bgColorR = this.bgColorR;
		cloned.chartHeight = this.chartHeight;
		cloned.chartTitle = this.chartTitle;
		cloned.chartType = this.chartType;
		cloned.chartWidth = this.chartWidth;
		cloned.colorPaletteName = this.colorPaletteName;
		cloned.dataSourceName = this.dataSourceName;
		cloned.description = this.description;
		cloned.drillPositionEnabled = this.drillPositionEnabled;
		cloned.drillReplaceEnabled = this.drillReplaceEnabled;
		cloned.drillthroughDatasource = this.drillthroughDatasource;
		cloned.drillThroughEnabled = this.drillThroughEnabled;
		cloned.drillthroughSQL = this.drillthroughSQL;
		cloned.fontName = this.fontName;
		cloned.fontSize = this.fontSize;
		cloned.fontStyle = this.fontStyle;
		cloned.foregroundAlpha = this.foregroundAlpha;
		cloned.hideSpans = this.hideSpans;
		cloned.horizAxisLabel = this.horizAxisLabel;
		cloned.legendFontName = this.legendFontName;
		cloned.legendFontSize = this.legendFontSize;
		cloned.legendFontStyle = this.legendFontStyle;
		cloned.legendPosition = this.legendPosition;
		cloned.levelStyle = this.levelStyle;
		cloned.mdxQuery = this.mdxQuery;
		cloned.showChart = this.showChart;
		cloned.showLegend = this.showLegend;
		cloned.showPareto = this.showPareto;
		cloned.showSlicer = this.showSlicer;
		cloned.showTable = this.showTable;
		cloned.slicerAlignment = this.slicerAlignment;
		cloned.slicerFontName = this.slicerFontName;
		cloned.slicerFontSize = this.slicerFontSize;
		cloned.slicerFontStyle = this.slicerFontStyle;
		cloned.slicerPosition = this.slicerPosition;
		cloned.tickLabelRotate = this.tickLabelRotate;
		cloned.useChartSize = this.useChartSize;
		cloned.vertAxisLabel = this.vertAxisLabel;
		cloned.showCustomLabels = this.showCustomLabels;
		cloned.suppressCrowdedLabels = this.suppressCrowdedLabels;
		// cloned.mdxQueryMap = new HashMap<String, String>(this.mdxQueryMap);
		return cloned;
	}

	/**
	 * 
	 * @return String
	 */
	public String getAnalysisTitle() {
		return analysisTitle;
	}

	/**
	 * 
	 * @return String
	 */
	public String getAxisFontName() {
		return axisFontName;
	}

	/**
	 * 
	 * @return int
	 */
	public int getAxisFontSize() {
		return axisFontSize;
	}

	/**
	 * 
	 * @return int
	 */
	public int getAxisFontStyle() {
		return axisFontStyle;
	}

	/*
	 * public String getCatalog() { return catalog; }
	 */

	/**
	 * 
	 * @return int
	 */
	public int getChartHeight() {
		return chartHeight;
	}

	/**
	 * 
	 * @return String
	 */
	public String getChartTitle() {
		return chartTitle;
	}

	/**
	 * 
	 * @return int
	 */
	public int getChartType() {
		return chartType;
	}

	/**
	 * 
	 * @return int
	 */
	public int getChartWidth() {
		return chartWidth;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isDrillThroughEnabled() {
		return drillThroughEnabled;
	}

	/**
	 * 
	 * @return String
	 */
	public String getFontName() {
		return fontName;
	}

	/**
	 * 
	 * @return int
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * 
	 * @return int
	 */
	public int getFontStyle() {
		return fontStyle;
	}

	/**
	 * 
	 * @return String
	 */
	public String getHorizAxisLabel() {
		return horizAxisLabel;
	}

	/**
	 * 
	 * @return String
	 */
	public String getLegendFontName() {
		return legendFontName;
	}

	/**
	 * 
	 * @return int
	 */
	public int getLegendFontSize() {
		return legendFontSize;
	}

	/**
	 * 
	 * @return int
	 */
	public int getLegendFontStyle() {
		return legendFontStyle;
	}

	/**
	 * 
	 * @return int
	 */
	public int getLegendPosition() {
		return legendPosition;
	}

	/**
	 * 
	 * @return String
	 */
	public String getMdxQuery() {
		return mdxQuery;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isShowLegend() {
		return showLegend;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isShowSlicer() {
		return showSlicer;
	}

	/**
	 * 
	 * @return int
	 */
	public int getSlicerAlignment() {
		return slicerAlignment;
	}

	/**
	 * 
	 * @return String
	 */
	public String getSlicerFontName() {
		return slicerFontName;
	}

	/**
	 * 
	 * @return int
	 */
	public int getSlicerFontSize() {
		return slicerFontSize;
	}

	/**
	 * 
	 * @return int
	 */
	public int getSlicerFontStyle() {
		return slicerFontStyle;
	}

	/**
	 * 
	 * @return int
	 */
	public int getSlicerPosition() {
		return slicerPosition;
	}

	/**
	 * 
	 * @return int
	 */
	public int getTickLabelRotate() {
		return tickLabelRotate;
	}

	/**
	 * 
	 * @return String
	 */
	public String getVertAxisLabel() {
		return vertAxisLabel;
	}

	/**
	 * 
	 * @return String
	 */
	public String getDataSourceName() {
		return dataSourceName;
	}

	/*
	 * public String getXmlaUri() { return xmlaUri; }
	 */

	/**
	 * 
	 * @param analysisTitle
	 *            String
	 */
	public void setAnalysisTitle(String analysisTitle) {
		this.analysisTitle = analysisTitle;
	}

	/**
	 * 
	 * @param axisFontName
	 *            String
	 */
	public void setAxisFontName(String axisFontName) {
		this.axisFontName = axisFontName;
	}

	/**
	 * 
	 * @param axisFontSize
	 *            int
	 */
	public void setAxisFontSize(int axisFontSize) {
		this.axisFontSize = axisFontSize;
	}

	/**
	 * 
	 * @param axisFontStyle
	 *            int
	 */
	public void setAxisFontStyle(int axisFontStyle) {
		this.axisFontStyle = axisFontStyle;
	}

	/*
	 * public void setCatalog(String catalog) { this.catalog = catalog; }
	 */

	/**
	 * 
	 * @param chartHeight
	 *            int
	 */
	public void setChartHeight(int chartHeight) {
		this.chartHeight = chartHeight;
	}

	/**
	 * 
	 * @param chartTitle
	 *            String
	 */
	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}

	/**
	 * 
	 * @param chartType
	 *            int
	 */
	public void setChartType(int chartType) {
		this.chartType = chartType;
	}

	/**
	 * 
	 * @param chartWidth
	 *            int
	 */
	public void setChartWidth(int chartWidth) {
		this.chartWidth = chartWidth;
	}

	/**
	 * 
	 * @param drillThroughEnabled
	 *            boolean
	 */
	public void setDrillThroughEnabled(boolean drillThroughEnabled) {
		this.drillThroughEnabled = drillThroughEnabled;
	}

	/**
	 * 
	 * @param fontName
	 *            String
	 */
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	/**
	 * 
	 * @param fontSize
	 *            int
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * 
	 * @param fontStyle
	 *            int
	 */
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
	}

	/**
	 * 
	 * @param horizAxisLabel
	 *            String
	 */
	public void setHorizAxisLabel(String horizAxisLabel) {
		this.horizAxisLabel = horizAxisLabel;
	}

	/**
	 * 
	 * @param legendFontName
	 *            String
	 */
	public void setLegendFontName(String legendFontName) {
		this.legendFontName = legendFontName;
	}

	/**
	 * 
	 * @param legendFontSize
	 *            int
	 */
	public void setLegendFontSize(int legendFontSize) {
		this.legendFontSize = legendFontSize;
	}

	/**
	 * 
	 * @param legendFontStyle
	 *            int
	 */
	public void setLegendFontStyle(int legendFontStyle) {
		this.legendFontStyle = legendFontStyle;
	}

	/**
	 * 
	 * @param legendPosition
	 *            int
	 */
	public void setLegendPosition(int legendPosition) {
		this.legendPosition = legendPosition;
	}

	/**
	 * 
	 * @param mdxQuery
	 *            String
	 */
	public void setMdxQuery(String mdxQuery) {
		this.mdxQuery = mdxQuery;
	}

	/**
	 * 
	 * @param showLegend
	 *            boolean
	 */
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * 
	 * @param showSlicer
	 *            boolean
	 */
	public void setShowSlicer(boolean showSlicer) {
		this.showSlicer = showSlicer;
	}

	/**
	 * 
	 * @param slicerAlignment
	 *            int
	 */
	public void setSlicerAlignment(int slicerAlignment) {
		this.slicerAlignment = slicerAlignment;
	}

	/**
	 * 
	 * @param slicerFontName
	 *            String
	 */
	public void setSlicerFontName(String slicerFontName) {
		this.slicerFontName = slicerFontName;
	}

	/**
	 * 
	 * @param slicerFontSize
	 *            int
	 */
	public void setSlicerFontSize(int slicerFontSize) {
		this.slicerFontSize = slicerFontSize;
	}

	/**
	 * 
	 * @param slicerFontStyle
	 *            int
	 */
	public void setSlicerFontStyle(int slicerFontStyle) {
		this.slicerFontStyle = slicerFontStyle;
	}

	/**
	 * 
	 * @param slicerPosition
	 *            int
	 */
	public void setSlicerPosition(int slicerPosition) {
		this.slicerPosition = slicerPosition;
	}

	/**
	 * 
	 * @param tickLabelRotate
	 *            int
	 */
	public void setTickLabelRotate(int tickLabelRotate) {
		this.tickLabelRotate = tickLabelRotate;
	}

	/**
	 * 
	 * @param vertAxisLabel
	 *            String
	 */
	public void setVertAxisLabel(String vertAxisLabel) {
		this.vertAxisLabel = vertAxisLabel;
	}

	/**
	 * 
	 * @param dataSource
	 *            String
	 */
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	/*
	 * public void setXmlaUri(String xmlaUri) { this.xmlaUri = xmlaUri; }
	 */

	/**
	 * @return Returns the colorPaletteName.
	 */
	public String getColorPaletteName() {
		return colorPaletteName;
	}

	/**
	 * @param colorPaletteName
	 *            The colorPaletteName to set.
	 */
	public void setColorPaletteName(String colorPaletteName) {
		this.colorPaletteName = colorPaletteName;
	}

	/**
	 * @return Returns the showPareto.
	 */
	public boolean getShowPareto() {
		return showPareto;
	}

	/**
	 * 
	 * @return String
	 */
	public String getAxisTickFontName() {
		return axisTickFontName;
	}

	/**
	 * 
	 * @return int
	 */
	public int getAxisTickFontSize() {
		return axisTickFontSize;
	}

	/**
	 * 
	 * @return int
	 */
	public int getAxisTickFontStyle() {
		return axisTickFontStyle;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isShowChart() {
		return showChart;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isShowTable() {
		return showTable;
	}

	/**
	 * 
	 * @return double
	 */
	public double getForegroundAlpha() {
		return foregroundAlpha;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isUseChartSize() {
		return useChartSize;
	}

	/**
	 * 
	 * @return int
	 */
	public int getBgColorB() {
		return bgColorB;
	}

	/**
	 * 
	 * @return int
	 */
	public int getBgColorG() {
		return bgColorG;
	}

	/**
	 * 
	 * @return int
	 */
	public int getBgColorR() {
		return bgColorR;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isHideSpans() {
		return hideSpans;
	}

	/**
	 * 
	 * @return boolean
	 */
	public boolean isLevelStyle() {
		return levelStyle;
	}

	public String getDrillthroughSQL() {
		return drillthroughSQL;
	}

	public String getDrillthroughDatasource() {
		return drillthroughDatasource;
	}

	/**
	 * @param showPareto
	 *            The showPareto to set.
	 */
	public void setShowPareto(boolean showPareto) {
		this.showPareto = showPareto;
	}

	/**
	 * 
	 * @param axisTickFontName
	 *            String
	 */
	public void setAxisTickFontName(String axisTickFontName) {
		this.axisTickFontName = axisTickFontName;
	}

	/**
	 * 
	 * @param axisTickFontSize
	 *            int
	 */
	public void setAxisTickFontSize(int axisTickFontSize) {
		this.axisTickFontSize = axisTickFontSize;
	}

	/**
	 * 
	 * @param axisTickFontStyle
	 *            int
	 */
	public void setAxisTickFontStyle(int axisTickFontStyle) {
		this.axisTickFontStyle = axisTickFontStyle;
	}

	/**
	 * 
	 * @param showChart
	 *            boolean
	 */
	public void setShowChart(boolean showChart) {
		this.showChart = showChart;
	}

	/**
	 * 
	 * @param showTable
	 *            boolean
	 */
	public void setShowTable(boolean showTable) {
		this.showTable = showTable;
	}

	/**
	 * 
	 * @param foregroundAlpha
	 *            double
	 */
	public void setForegroundAlpha(double foregroundAlpha) {
		this.foregroundAlpha = foregroundAlpha;
	}

	/**
	 * 
	 * @param useChartSize
	 *            boolean
	 */
	public void setUseChartSize(boolean useChartSize) {
		this.useChartSize = useChartSize;
	}

	/**
	 * 
	 * @param bgColorB
	 *            int
	 */
	public void setBgColorB(int bgColorB) {
		this.bgColorB = bgColorB;
	}

	/**
	 * 
	 * @param bgColorG
	 *            int
	 */
	public void setBgColorG(int bgColorG) {
		this.bgColorG = bgColorG;
	}

	/**
	 * 
	 * @param bgColorR
	 *            int
	 */
	public void setBgColorR(int bgColorR) {
		this.bgColorR = bgColorR;
	}

	/**
	 * 
	 * @param hideSpans
	 *            boolean
	 */
	public void setHideSpans(boolean hideSpans) {
		this.hideSpans = hideSpans;
	}

	/**
	 * 
	 * @param levelStyle
	 *            boolean
	 */
	public void setLevelStyle(boolean levelStyle) {
		this.levelStyle = levelStyle;
	}

	public void setDrillthroughSQL(String drillthroughSQL) {
		this.drillthroughSQL = drillthroughSQL;
	}

	public void setDrillthroughDatasource(String drillthroughDatasource) {
		this.drillthroughDatasource = drillthroughDatasource;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDrillReplaceEnabled() {
		return drillReplaceEnabled;
	}

	public void setDrillReplaceEnabled(boolean drillReplaceEnabled) {
		this.drillReplaceEnabled = drillReplaceEnabled;
	}

	public boolean isDrillPositionEnabled() {
		return drillPositionEnabled;
	}

	public void setDrillPositionEnabled(boolean drillPositionEnabled) {
		this.drillPositionEnabled = drillPositionEnabled;
	}

	public Map<String, String> getMdxQueryMap() {
		if (mdxQueryMap == null) {
			mdxQueryMap = new HashMap<String, String>();
		}

		return mdxQueryMap;
	}

	public void setMdxQueryMap(Map<String, String> mdxQueryMap) {
		this.mdxQueryMap = mdxQueryMap;
	}

	public Set<String> getOwners() {
		if (this.owners == null) {
			this.owners = new HashSet<String>();
		}
		return owners;
	}

	public void setOwners(Set<String> owners) {
		this.owners = owners;
	}

	public boolean isSwapAxes() {
		return swapAxes;
	}

	public void setSwapAxes(boolean swapAxes) {
		this.swapAxes = swapAxes;
	}

	public boolean isShowNonEmpty() {
		return showNonEmpty;
	}

	public void setShowNonEmpty(boolean showNonEmpty) {
		this.showNonEmpty = showNonEmpty;
	}

	public boolean isChartDrillThroughEnabled() {
		return chartDrillThroughEnabled;
	}

	public void setChartDrillThroughEnabled(boolean chartDrillThroughEnabled) {
		this.chartDrillThroughEnabled = chartDrillThroughEnabled;
	}

	public boolean isShowCustomLabels() {
		return showCustomLabels;
	}

	public void setShowCustomLabels(boolean showCustomLabels) {
		this.showCustomLabels = showCustomLabels;
	}

	public boolean isViewOnlyMode() {
		return viewOnlyMode;
	}

	public void setViewOnlyMode(boolean viewOnlyMode) {
		this.viewOnlyMode = viewOnlyMode;
	}

	public String getDsType() {
		return dsType;
	}

	public void setDsType(String dsType) {
		this.dsType = dsType;
	}

}
