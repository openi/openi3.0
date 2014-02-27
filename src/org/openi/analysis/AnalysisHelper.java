package org.openi.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mondrian.olap.Dimension;

import org.apache.log4j.Logger;
import org.openi.datasource.Datasource;
import org.openi.datasource.DatasourceType;
import org.openi.datasource.MondrianDatasource;
import org.openi.datasource.XMLADatasource;
import org.openi.util.olap.MondrianHelper;
import org.openi.util.olap.XMLAUtils;
import org.openi.wcf.WCFHelper;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.navi.ChangeSlicer;
import com.tonbeller.jpivot.olap.navi.MdxQuery;
import com.tonbeller.jpivot.olap.query.MDXElement;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * helper class for an analysis
 * 
 * @author SUJEN
 * 
 */
public class AnalysisHelper {

	private static Logger logger = Logger.getLogger(AnalysisHelper.class);

	/**
	 * 
	 * @param analysis
	 * @param context
	 * @param pivotID
	 */
	public static void loadAnalysis(Analysis analysis, RequestContext context,
			String pivotID) {
		long startTime = System.currentTimeMillis();
		if (logger.isInfoEnabled())
			logger.info("loading analysis components for analysis: "
					+ analysis.getAnalysisTitle());
		loadAnalysisInternal(analysis, context, pivotID);
		if (logger.isInfoEnabled()) {
			logger.info("analysis load process took "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

	/**
	 * 
	 * @param analysis
	 * @param context
	 * @param pivotID
	 */
	private static void loadAnalysisInternal(Analysis analysis,
			RequestContext context, String pivotID) {
		WCFHelper wcfHelper = new WCFHelper(context, pivotID);
		try {
			wcfHelper.initOlapModel(analysis);
			wcfHelper.loadTable(analysis);
			wcfHelper.loadChart(analysis);
			wcfHelper.loadNavigator("xmlaNav" + pivotID, "xmlaQuery" + pivotID);
			wcfHelper.loadPrint();
			wcfHelper.loadDrillthroughTable();
			wcfHelper.loadPrintForm();
			wcfHelper.loadSortForm();
			wcfHelper.loadChartForm();
			wcfHelper.loadMDXEditForm();
		} catch (Exception e) {
			logger.error("Error while loading analysis wcf components", e);
		}
	}

	/**
	 * get mdx query from the olap model
	 * 
	 * @param olapModel
	 * @return MDX query
	 */
	public static String getMDXFromOlapModel(OlapModel olapModel) {
		String mdxString = "";
		MdxQuery mdxQueryModel = (MdxQuery) olapModel.getExtension("mdxQuery");
		if (mdxQueryModel != null)
			mdxString = mdxQueryModel.getMdxQuery();

		if (logger.isInfoEnabled())
			logger.info("MDX from the olap model : " + mdxString);

		return mdxString;
	}

	/**
	 * builds slicer value from the olapmodel Format: Slicer: Dimension -> Level
	 * = Member1, Member2 ....
	 * 
	 * @param olapModel
	 * @return Selected slicers
	 */
	public static String buildSlicerAsString(OlapModel olapModel) {
		StringBuffer slicer = new StringBuffer();

		ChangeSlicer changeSlicer = (ChangeSlicer) olapModel
				.getExtension("changeSlicer");

		Member[] members = changeSlicer.getSlicer();

		for (int i = 0; i < members.length; i++) {
			slicer.append(members[i].getLevel().getHierarchy().getDimension()
					.getLabel()
					+ "->" + members[i].getLevel().getLabel());

			slicer.append("=");
			slicer.append(members[i].getLabel());

			if (i < (members.length - 1)) {
				slicer.append(", ");
			}
		}

		if (slicer.length() != 0)
			slicer.insert(0, "Slicer: ");

		return slicer.toString();
	}
	
	/**
	 * 
	 * @param olapModel
	 * @return
	 */
	public static List<SlicerValue> buildSlicerAsObjects(OlapModel olapModel) {
		List<SlicerValue> slicerValueObjects = new ArrayList<SlicerValue>();

		ChangeSlicer changeSlicer = (ChangeSlicer) olapModel
				.getExtension("changeSlicer");

		Member[] members = changeSlicer.getSlicer();

		for (int i = 0; i < members.length; i++) {
			String dimensionName = members[i].getLevel().getHierarchy().getDimension().getLabel();
			String hierarchyName = members[i].getLevel().getHierarchy().getLabel();
			String levelName = members[i].getLevel().getLabel();
			String memberName = members[i].getLabel();
			String uniqueName = ((MDXElement) members[i]).getUniqueName(); 
			SlicerValue slicerValueObj = new SlicerValue(dimensionName, hierarchyName, levelName, memberName, uniqueName);
			slicerValueObjects.add(slicerValueObj);
		}
		
		return slicerValueObjects;
	}

	public static String generateDefaultMdx(DatasourceType dsType,
			Datasource datasource, String cube, String selectedMeasures)
			throws Exception {
		String mdxQuery = "";
		List measuresList = new ArrayList();
		if (selectedMeasures != null && !selectedMeasures.equals("")) {
			String measures[] = selectedMeasures.split(",");
			for (int i = 0; i < measures.length; i++) {
				measuresList.add(measures[i]);
			}
		}
		if (dsType == DatasourceType.XMLA) {
			List<String> dims = XMLAUtils.getDimensionList(
					(XMLADatasource) datasource, cube);
			for (String dim : dims) {
				if (!"Measures".equalsIgnoreCase(dim)) {
					List<String> hiers = XMLAUtils.discoverHier(
							(XMLADatasource) datasource, cube, dim);
					String hierName = "";
					if (hiers != null && hiers.size() > 1) {
						hierName = hiers.get(0);
						if (hierName.startsWith("[") && hierName.endsWith("]")) {
							hierName = "." + hierName;
						} else {
							hierName = ".[" + hierName + "]";
						}
					}

					if (measuresList == null || measuresList.size() < 1)
						mdxQuery = "SELECT {[Measures].DefaultMember} on columns, {["
								+ dim
								+ "]"
								+ hierName
								+ ".DefaultMember} on rows FROM [" + cube + "]";
					else {
						String measuresMDX = "";
						Iterator<String> measuresItr = measuresList.iterator();
						while (measuresItr.hasNext()) {
							measuresMDX += "[Measures]";
							String measure = measuresItr.next();
							if (measure.startsWith("[")
									&& measure.endsWith("]")) {
								measuresMDX += "." + measure;
							} else {
								measuresMDX += ".[" + measure + "]";
							}
							if (measuresItr.hasNext())
								measuresMDX += ",";
						}
						mdxQuery = "SELECT {" + measuresMDX
								+ "} on columns, {[" + dim + "]" + hierName
								+ ".DefaultMember} on rows FROM [" + cube + "]";
					}

					logger.debug("Default query generated as :" + mdxQuery);
					break;
				}
			}
		} else {
			MondrianDatasource mondrianDS = (MondrianDatasource) datasource;
			Dimension[] dims = MondrianHelper
					.getDimensionList(mondrianDS, cube);
			Dimension selectedDim = null;
			for (int i = 0; i < dims.length; i++) {
				logger.info("Dimenesion[" + i + "] : " + dims[i].getName());
				if (!"Measures".equalsIgnoreCase(dims[i].getName())) {
					selectedDim = dims[i];
					break;
				}
			}
			mdxQuery = MondrianHelper.getInitialQuery(mondrianDS, cube,
					selectedDim, measuresList);
		}

		return mdxQuery;
	}

}
