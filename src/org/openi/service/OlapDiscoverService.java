package org.openi.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openi.acl.AccessDeniedException;
import org.openi.datasource.DatasourceType;
import org.openi.datasource.MondrianDatasource;
import org.openi.datasource.XMLADatasource;
import org.openi.olap.CubeDataExplorer;
import org.openi.service.exception.ServiceException;
import org.openi.util.olap.MondrianHelper;
import org.openi.util.olap.XMLAUtils;

import com.tonbeller.jpivot.olap.model.OlapException;

/**
 * 
 * @author SUJEN
 * 
 */
public class OlapDiscoverService {

	/**
	 * 
	 * @param dsType
	 * @param dsName
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws OlapException 
	 * @throws AccessDeniedException 
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public List<String> discoverCubes(String dsType, String dsName) throws OlapException, UnsupportedEncodingException, AccessDeniedException {
		List<String> cubes = null;
		if (dsType.equals(DatasourceType.XMLA.toString())) {
			cubes = XMLAUtils.getCubesList((XMLADatasource) this.dsService
					.getDatasource(dsName, DatasourceType.XMLA));
		}
		else if(dsType.equals(DatasourceType.MONDRIAN.toString())) {
			cubes = MondrianHelper.getCubesList((MondrianDatasource) this.dsService
					.getDatasource(dsName, DatasourceType.MONDRIAN));
		}
		return cubes;
	}

	private DatasourceService dsService;

	public DatasourceService getDsService() {
		return dsService;
	}

	public void setDsService(DatasourceService dsService) {
		this.dsService = dsService;
	}

	/**
	 * 
	 * @param datasourceType
	 * @param datasource
	 * @param cube
	 * @return
	 * @throws AccessDeniedException
	 */
	public Collection<String> discoverMeasures(String datasourceType,
			String datasource, String cube) throws AccessDeniedException {
		DatasourceType dsType = null;
		if (datasourceType.equals(DatasourceType.XMLA.toString())) 
			dsType = DatasourceType.XMLA;
		else
			dsType = DatasourceType.MONDRIAN;
		CubeDataExplorer cubeDataExplorer = new CubeDataExplorer(dsType, this.dsService.getDatasource(datasource, dsType), cube, "");
		List measuresList = cubeDataExplorer.getMeasuresList();
		if(measuresList == null)
			measuresList = new ArrayList();
		return measuresList;
	}
}
