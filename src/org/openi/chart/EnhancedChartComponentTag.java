package org.openi.chart;

import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.component.ComponentTag;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * creates a ChartComponent
 * 
 * @author SUJEN
 */
public class EnhancedChartComponentTag extends ComponentTag {
	String query;
	String baseDisplayURL;
	String controllerURL;

	/**
	 * creates a ChartComponent
	 */
	public Component createComponent(RequestContext context) throws Exception {
		OlapModel model = (OlapModel) context.getModelReference(getQuery());
		if (model == null)
			throw new Exception("component \"" + getQuery() + "\" not found");
		return new EnhancedChartComponent(id, null, query, baseDisplayURL,
				controllerURL, context);
	}

	/**
	 * Returns the query attribute (actually a reference to an Olap Model)
	 * 
	 * @return String
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query attribute (actually a reference to an Olap Model)
	 * 
	 * @param ref
	 *            The ref to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Returns the baseDisplayURL
	 * 
	 * @return String
	 */
	public String getBaseDisplayURL() {
		return baseDisplayURL;
	}

	/**
	 * Sets the baseDisplayURL
	 * 
	 * @param baseDisplayURL
	 *            The baseDisplayURL to set
	 */
	public void setBaseDisplayURL(String baseDisplayURL) {
		this.baseDisplayURL = baseDisplayURL;
	}

	public String getControllerURL() {
		return controllerURL;
	}

	public void setControllerURL(String controllerURL) {
		this.controllerURL = controllerURL;
	}

}
