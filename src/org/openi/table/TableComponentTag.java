package org.openi.table;

import java.net.URL;

import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.component.ComponentTag;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * creates a TableComponentImpl
 * 
 * @author av
 * @author SUJEN
 */
public class TableComponentTag extends ComponentTag {
	String query;
	String configXml = null;

	/**
	 * creates a TableComponentImpl
	 */
	public Component createComponent(RequestContext context) throws Exception {
		OlapModel olapModel = (OlapModel) context.getModelReference(query);
		if (olapModel == null)
			throw new Exception("query \"" + query + "\" not found");
		URL configUrl = getClass().getResource("config.xml");

		return TableComponentFactory.instance(id, configUrl, olapModel);

	}

	/**
	 * Returns the configXml.
	 * 
	 * @return String
	 */
	public String getConfigXml() {
		return configXml;
	}

	/**
	 * Sets the configXml.
	 * 
	 * @param configXml
	 *            The configXml to set
	 */
	public void setConfigXml(String configXml) {
		this.configXml = configXml;
	}

	/**
	 * Returns the query.
	 * 
	 * @return String
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query.
	 * 
	 * @param query
	 *            The query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

}
