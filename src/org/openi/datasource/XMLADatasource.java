package org.openi.datasource;

/**
 * POJO representing an XMLA datasource
 * contians required datasource properties for making xmla connections
 * 
 * @author SUJEN
 * 
 */
public class XMLADatasource extends Datasource {

	private String name;
	private String serverURL;
	private String datasourceName;
	private String username;
	private String password;
	private String catalog;
	
	public XMLADatasource() {
	}

	public XMLADatasource(String name, String serverURL, String datasourceName, String username, String catalog) {
		this.name = name;
		this.serverURL = serverURL;
		this.datasourceName = datasourceName;
		this.username = username;
		this.catalog = catalog;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
	
	public String getDatasourceName() {
		return datasourceName;
	}

	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
