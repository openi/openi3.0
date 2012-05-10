package org.openi.olap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mondrian.olap.Dimension;

import org.apache.log4j.Logger;
import org.openi.datasource.Datasource;
import org.openi.datasource.DatasourceType;
import org.openi.datasource.MondrianDatasource;
import org.openi.datasource.XMLADatasource;
import org.openi.util.olap.MondrianHelper;
import org.openi.util.olap.XMLAUtils;

import com.tonbeller.jpivot.olap.model.OlapException;

/**
 * helper class for EDA feature
 * 
 * @author SUJEN
 * 
 */
public class CubeDataExplorer {

	private static Logger logger = Logger.getLogger(CubeDataExplorer.class);

	private DatasourceType dsType;
	private Datasource datasource;
	private String cube;
	private String selectedMeasuresList = "";

	public CubeDataExplorer(DatasourceType dsType, Datasource datasource,
			String cube, String selectedMeasures) {
		this.dsType = dsType;
		this.datasource = datasource;
		this.cube = cube;
		this.selectedMeasuresList = selectedMeasures;
		init();
	}

	private void init() {
		populateDimensionsList();
		populateMeasuresList();
	}

	private List<String> dimensionsList = null;

	private List<String> measuresList = null;
	
	public List<String> getDimensionsList() {
		return dimensionsList;
	}

	public void setDimensionsList(List<String> dimensionsList) {
		this.dimensionsList = dimensionsList;
	}

	public List<String> getMeasuresList() {
		return measuresList;
	}

	public void setMeasuresList(List<String> measuresList) {
		this.measuresList = measuresList;
	}

	@SuppressWarnings("unchecked")
	private void populateMeasuresList() {
		if(logger.isInfoEnabled())
			logger.info("Loading measures list for the cube : " + this.cube);
		
		if(this.selectedMeasuresList != null && !this.selectedMeasuresList.equals("")) {
			if(measuresList == null)
				measuresList = new ArrayList();
			String selectedMeasures[] = this.selectedMeasuresList.split(",");
			for(int i = 0; i < selectedMeasures.length; i++) {
				this.measuresList.add(selectedMeasures[i]);
			}
		}
		else {
			try {
				if (this.dsType == DatasourceType.XMLA)
					measuresList = XMLAUtils.discoverMeasures(
							(XMLADatasource) this.datasource, cube);
				else if (this.dsType == DatasourceType.MONDRIAN) {
					Dimension measureDim = null;
					if(dimObjects.containsKey("[Measures]"))
						measureDim = dimObjects.get("[Measures]");
					else
						measureDim = dimObjects.get("Measures");
					measuresList = MondrianHelper.discoverMeasures(
							(MondrianDatasource) this.datasource, cube, measureDim);
				}
			} catch (Exception e) {
				logger.error(
						"Couldn't populate measures list of the selected cube "
								+ cube, e);
			}
		}
	}

	private Map<String, Dimension> dimObjects = new HashMap<String, Dimension>();
	@SuppressWarnings("unchecked")
	private void populateDimensionsList() {
		if(logger.isInfoEnabled())
			logger.info("Loading dimensions list for the cube : " + this.cube);
		try {
			if (this.dsType == DatasourceType.XMLA)
				dimensionsList = XMLAUtils.getDimensionList(
						(XMLADatasource) this.datasource, cube);
			else if (this.dsType == DatasourceType.MONDRIAN) {
				dimensionsList = new ArrayList<String>();
				Dimension[] dims = MondrianHelper.getDimensionList((MondrianDatasource) this.datasource, cube);
				for(int i = 0; i < dims.length; i++) {
					dimensionsList.add(dims[i].getName());
					dimObjects.put(dims[i].getName(), dims[i]);
				}
			}
		} catch (Exception e) {
			logger.error(
					"Couldn't populate dimensions list of the selected cube "
							+ cube, e);
		}
	}

	/**
	 * generates meaures by dimension queries map key => string
	 * "measure by dimension" value => mdx query for data retrieval
	 * 
	 * @return
	 * @throws OlapException
	 * @throws UnsupportedEncodingException
	 */
	public Map<String, String> generateMeasureByDimQueries()
			throws OlapException, UnsupportedEncodingException {
		long start = System.currentTimeMillis();
		Map<String, String> mdxQueriesMap = new HashMap<String, String>();
		if (dimensionsList != null && measuresList != null) {
			Iterator dimensionsItr = this.dimensionsList.iterator();
			Iterator measuresItr = this.measuresList.iterator();
			while (dimensionsItr.hasNext()) {
				String dimensionName = (String) dimensionsItr.next();
				if (!"measures".equalsIgnoreCase(dimensionName)) {
					if (this.dsType == DatasourceType.XMLA)
						mdxQueriesMap.put("by " + dimensionName, XMLAUtils
								.createDefaultMdx((XMLADatasource) datasource,
										cube, dimensionName, measuresList));
					else if (this.dsType == DatasourceType.MONDRIAN)
						mdxQueriesMap.put("by " + dimensionName, MondrianHelper.getInitialQuery((MondrianDatasource) datasource, cube, dimObjects.get(dimensionName), measuresList));
				}
			}
		}
		logger.debug("MDX generated for all dimensions in: "
				+ (System.currentTimeMillis() - start) + "ms");
		return mdxQueriesMap;
	}

	public DatasourceType getDsType() {
		return dsType;
	}

	public void setDsType(DatasourceType dsType) {
		this.dsType = dsType;
	}

	public Datasource getDatasource() {
		return datasource;
	}

	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	public String getCube() {
		return cube;
	}

	public void setCube(String cube) {
		this.cube = cube;
	}

}
