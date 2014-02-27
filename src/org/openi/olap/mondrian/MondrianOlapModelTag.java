package org.openi.olap.mondrian;

import java.net.URL;

import org.openi.olap.OlapModelTag;
import org.openi.olap.mondrian.MondrianModelFactory.Config;
import org.pentaho.platform.api.data.IDBDatasourceService;
//import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.navi.ClickableExtension;
import com.tonbeller.jpivot.olap.navi.ClickableExtensionImpl;
import com.tonbeller.jpivot.tags.OlapModelProxy;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * 
 * @author SUJEN
 * 
 */
public class MondrianOlapModelTag extends OlapModelTag {

	String dataSource;
	String jdbcDriver;
	String jdbcUser;
	String jdbcPassword;
	String jdbcUrl;
	String catalogUri;
	String config;
	String role;
	String dynResolver;
	String dynLocale;
	String connectionPooling;
	String dataSourceChangeListener;

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

	public String getDataSourceChangeListener() {
		return dataSourceChangeListener;
	}

	public void setDataSourceChangeListener(String dataSourceChangeListener) {
		this.dataSourceChangeListener = dataSourceChangeListener;
	}

	@Override
	protected OlapModel getOlapModel(RequestContext context) throws Exception {
		MondrianModelFactory.Config cfg = new MondrianModelFactory.Config();
		/*URL schemaUrl;
		if (catalogUri.startsWith("/"))
			schemaUrl = pageContext.getServletContext().getResource(catalogUri);
		else
			schemaUrl = new URL(catalogUri);
		if (schemaUrl == null)
			throw new Exception("could not find Catalog \"" + catalogUri + "\""); */

		// cfg.setMdxQuery(getBodyContent().getString());
		cfg.setMdxQuery(getMdxQuery());
		// Add the schema URL. Enclose the value in quotes to permit
		// schema URLs that include things like ;jsessionid values.
		//cfg.setSchemaUrl("\"" + schemaUrl.toExternalForm() + "\"");
		cfg.setSchemaUrl("\"" + catalogUri + "\"");
		cfg.setJdbcUrl(jdbcUrl);
		cfg.setJdbcDriver(jdbcDriver);
		cfg.setJdbcUser(jdbcUser);
		cfg.setJdbcPassword(jdbcPassword);
		cfg.setDataSource(dataSource);
		cfg.setRole(role);
		cfg.setDynResolver(dynResolver);
		cfg.setDynLocale(dynLocale);
		cfg.setConnectionPooling(connectionPooling);
		cfg.setDataSourceChangeListener(dataSourceChangeListener);
		//IDatasourceService datasourceService = PentahoSystem
			//	.getObjectFactory().get(IDatasourceService.class, null);
		
		IDBDatasourceService datasourceService = PentahoSystem.getObjectFactory().get(IDBDatasourceService.class, null);
		
		cfg.setExternalDataSource(datasourceService.getDataSource(dataSource));
		allowOverride(context, cfg);

		URL url;
		if (config == null)
			url = getDefaultConfig();
		else
			url = pageContext.getServletContext().getResource(config);

		MondrianModel mm = MondrianModelFactory.instance(url, cfg);
		OlapModel om = (OlapModel) mm.getTopDecorator();
		om.setLocale(context.getLocale());
		om.setServletContext(context.getSession().getServletContext());
		return om;
	}

	public void init(RequestContext context) throws Exception {
		long start = System.currentTimeMillis();
		// clickables = new ArrayList();

		OlapModel om = getOlapModel(context);
		om = (OlapModel) om.getTopDecorator();
		om.setLocale(context.getLocale());
		om.setID(id);

		ClickableExtension ext = (ClickableExtension) om
				.getExtension(ClickableExtension.ID);

		if (ext == null) {
			ext = new ClickableExtensionImpl();
			om.addExtension(ext);
		}

		// ext.setClickables(clickables);

		/*
		 * OlapModelProxy omp = OlapModelProxy.instance(id,
		 * context.getSession(), stackMode);
		 */

		OlapModelProxy omp = OlapModelProxy.instance(id, context.getSession());

		omp.initializeAndShow(om);

	}

	protected void allowOverride(RequestContext context, Config cfg) {
		cfg.allowOverride(context);
	}

	protected URL getDefaultConfig() {
		return getClass().getResource("config.xml");
	}

	private String mdxQuery;

	public String getMdxQuery() {
		return mdxQuery;
	}

	public void setMdxQuery(String mdxQuery) {
		this.mdxQuery = mdxQuery;
	}

}
