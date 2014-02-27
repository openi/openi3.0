package org.openi.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final Log logger = LogFactory
			.getLog(OlapDiscoverService.class);

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
	public List<String> discoverCubes(String dsType, String dsName)
			throws UnsupportedEncodingException,
			AccessDeniedException, OlapException {
		List<String> cubes = null;
		if (dsType.equals(DatasourceType.XMLA.toString())) {
			XMLADatasource xmlaDS = (XMLADatasource) this.dsService
					.getDatasource(dsName, DatasourceType.XMLA);
			try {
				cubes = XMLAUtils.getCubesList(xmlaDS);
			} catch (OlapException e) {
				logger.error(e);
				throw new OlapException(e);
			}
		} else if (dsType.equals(DatasourceType.MONDRIAN.toString())) {
			cubes = MondrianHelper
					.getCubesList((MondrianDatasource) this.dsService
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
	public List<String> discoverMeasures(String datasourceType,
			String datasource, String cube) throws AccessDeniedException {
		DatasourceType dsType = null;
		if (datasourceType.equals(DatasourceType.XMLA.toString()))
			dsType = DatasourceType.XMLA;
		else
			dsType = DatasourceType.MONDRIAN;
		CubeDataExplorer cubeDataExplorer = new CubeDataExplorer(dsType,
				this.dsService.getDatasource(datasource, dsType), cube, "");
		List measuresList = cubeDataExplorer.getMeasuresList();
		if (measuresList == null)
			measuresList = new ArrayList();
		return measuresList;
	}
}
