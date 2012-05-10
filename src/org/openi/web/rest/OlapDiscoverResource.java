package org.openi.web.rest;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.openi.Resources;
import org.openi.service.OlapDiscoverService;
import org.openi.web.rest.exception.RestResourceException;
import org.springframework.stereotype.Component;

/**
 * @author SUJEN
 * 
 */
@Component
@Path("/openianalytics/api/discoverResource")
public class OlapDiscoverResource {

	private static Logger logger = Logger.getLogger(OlapDiscoverResource.class);

	//OlapDiscoverResource Exception Messages
	public static final String DISCOVER_CUBES_ERROR = Resources.getString("DISCOVER_CUBES_ERROR");
	public static final String DISCOVER_MEASURES_ERROR = Resources.getString("DISCOVER_MEASURES_ERROR");
	
	/**
	 * 
	 * @param datasourceType
	 * @return
	 */
	@GET
	@Produces({ "application/json" })
	@Path("/{datasourceType}/datasources/{datasource}/cubes/")
	public Collection<String> discoverCubes(
			@PathParam("datasourceType") String datasourceType,
			@PathParam("datasource") String datasource) {
		try {
			return this.olapDiscoverService.discoverCubes(datasourceType,
					datasource);
		} catch (Exception e) {
			throw new RestResourceException(DISCOVER_CUBES_ERROR + "\r\n" + e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param datasourceType
	 * @param datasource
	 * @param cube
	 * @return
	 */
	@GET
	@Produces({ "application/json" })
	@Path("/{datasourceType}/datasources/{datasource}/{cube}/measures/")
	public Collection<String> discoverMeasures(
			@PathParam("datasourceType") String datasourceType,
			@PathParam("datasource") String datasource,
			@PathParam("cube") String cube) {
		try {
			return this.olapDiscoverService.discoverMeasures(datasourceType,
					datasource, cube);
		} catch (Exception e) {
			throw new RestResourceException(DISCOVER_MEASURES_ERROR + "\r\n" + e.getMessage());
		}
	}

	private OlapDiscoverService olapDiscoverService;

	public OlapDiscoverService getOlapDiscoverService() {
		return olapDiscoverService;
	}

	public void setOlapDiscoverService(OlapDiscoverService olapDiscoverService) {
		this.olapDiscoverService = olapDiscoverService;
	}

}
