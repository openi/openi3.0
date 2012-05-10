package org.openi.web.rest;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.openi.datasource.Datasource;
import org.openi.datasource.DatasourceType;
import org.openi.service.DatasourceService;
import org.springframework.stereotype.Component;

/**
 * Rest resource for olap datasource information
 * 
 * @author SUJEN
 * 
 */
@Component
@Path("/openianalytics/api/datasourceResource")
public class DatasourceResource {

	/**
	 * 
	 * @return
	 */
	@GET
	@Produces({"application/json"})
	@Path("datasources")
	public Collection<Datasource> getAllDatasources() {
		return this.dsService.getAllDatasources();
	}
	
	/**
	 * 
	 * @param datasourceType
	 * @return
	 */
	@GET
	@Produces({"application/json"})
	@Path("/datasources/{datasourceType}/")
	public Collection<Datasource> getDatasources(@PathParam("datasourceType") String datasourceType) {
		if(datasourceType.equals(DatasourceType.XMLA.toString()))
			return this.dsService.getDatasources(DatasourceType.XMLA);
		else
			return this.dsService.getDatasources(DatasourceType.MONDRIAN);
	}
	
	private DatasourceService dsService;

	public DatasourceService getDsService() {
		return dsService;
	}

	public void setDsService(DatasourceService dsService) {
		this.dsService = dsService;
	}
}
