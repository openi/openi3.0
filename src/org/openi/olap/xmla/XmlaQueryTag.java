package org.openi.olap.xmla;

import com.tonbeller.jpivot.core.Model;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.navi.ClickableExtension;
import com.tonbeller.jpivot.olap.navi.ClickableExtensionImpl;
import com.tonbeller.jpivot.table.ClickableMember;
import com.tonbeller.jpivot.tags.OlapModelProxy;
import com.tonbeller.wcf.controller.RequestContext;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author SUJEN
 * 
 */
public class XmlaQueryTag extends XMLA_OlapModelTag {
	private static Logger logger = Logger.getLogger(XmlaQueryTag.class);
	private List clickables;
	private String user = null;
	private String password = null;
	private String mdxQuery = null;
	private String queryName;
	private boolean stackMode;

	public void addClickable(ClickableMember clickable) {
		clickables.add(clickable);
	}

	protected OlapModel getOlapModel(RequestContext context)
			throws SAXException, IOException, OlapException {
		URL url;

		if (getConfig() == null) {
			url = getClass().getResource("config.xml");
		} else {
			url = pageContext.getServletContext().getResource(getConfig());
		}

		// let Digester create a model from config input
		// the config input stream MUST refer to the XMLA_Model class
		// <model class="org.openi.olap.xmla.XMLA_Model"> is required
		Model model = ModelFactory.instance(url);

		if (!(model instanceof XMLA_Model)) {
			throw new com.tonbeller.jpivot.olap.model.OlapException(
					"invalid class attribute for model tag, resource="
							+ getConfig());
		}

		XMLA_Model xm = (XMLA_Model) model;
		xm.setUri(getUri());
		xm.setDataSource(getDataSource());
		xm.setCatalog(getCatalog());
		// xm.setMdxQuery(getBodyContent().getString());
		xm.setUser(URLEncoder.encode(user, "UTF-8"));
		xm.setPassword(URLEncoder.encode(password, "UTF-8"));
		xm.setMdxQuery(mdxQuery);

		return xm;
	}

	public void init(RequestContext context) throws Exception {
		long start = System.currentTimeMillis();
		clickables = new ArrayList();

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

		ext.setClickables(clickables);

		OlapModelProxy omp = OlapModelProxy.instance(id, context.getSession(),
				stackMode);

		if (queryName != null) {
			omp.initializeAndShow(queryName, om);
		} else {
			omp.initializeAndShow(om);
		}

		logger.info("init completed in: "
				+ (System.currentTimeMillis() - start) + " ms");
	}

	public String getMdxQuery() {
		return this.mdxQuery;
	}

	public void setMdxQuery(String mdxquery) {
		this.mdxQuery = mdxquery;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public void setStackMode(boolean stackMode) {
		this.stackMode = stackMode;
	}
}
