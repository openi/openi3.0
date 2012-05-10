package org.openi.datasource;

import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

/**
 * POJO representing mondrian datasource contians required datsource properties
 * for making direct mondrian connection
 * 
 * @author SUJEN
 * 
 */
public class MondrianDatasource extends Datasource {

	private String name;
	private MondrianCatalog mondrianCatalog;
	
	public MondrianDatasource(String name, MondrianCatalog mondrianCatalog) {
		this.name = name;
		this.mondrianCatalog = mondrianCatalog;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/*
	private String dataSource;
	private String jdbcDriver;
	private String jdbcUser;
	private String jdbcPassword;
	private String jdbcUrl;
	private String catalogUri;
	private String config;
	private String role;
	private String dynResolver;
	private String dynLocale;
	private String connectionPooling;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getJdbcUser() {
		return jdbcUser;
	}

	public void setJdbcUser(String jdbcUser) {
		this.jdbcUser = jdbcUser;
	}

	public String getJdbcPassword() {
		return jdbcPassword;
	}

	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getCatalogUri() {
		return catalogUri;
	}

	public void setCatalogUri(String catalogUri) {
		this.catalogUri = catalogUri;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDynResolver() {
		return dynResolver;
	}

	public void setDynResolver(String dynResolver) {
		this.dynResolver = dynResolver;
	}

	public String getDynLocale() {
		return dynLocale;
	}

	public void setDynLocale(String dynLocale) {
		this.dynLocale = dynLocale;
	}

	public String getConnectionPooling() {
		return connectionPooling;
	}

	public void setConnectionPooling(String connectionPooling) {
		this.connectionPooling = connectionPooling;
	}
	
	@Override
	public String toString() {
		return name;
	}*/
	

	public MondrianCatalog getMondrianCatalog() {
		return mondrianCatalog;
	}

	public void setMondrianCatalog(MondrianCatalog mondrianCatalog) {
		this.mondrianCatalog = mondrianCatalog;
	}

	@Override
	public String toString() {
		return name;
	}
}
