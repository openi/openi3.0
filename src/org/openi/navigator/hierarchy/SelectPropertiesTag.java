package org.openi.navigator.hierarchy;

import javax.servlet.jsp.JspException;

import com.tonbeller.jpivot.table.TableComponent;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.component.ComponentTag;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * creates a SelectPropertiesTag
 *
 * @author wawan
 * @author SUJEN
 * 
 */

public class SelectPropertiesTag extends ComponentTag {
	String table;

	/**
	 * creates the select properties component
	 */
	public Component createComponent(RequestContext context) throws JspException {
		TableComponent tableComponent = (TableComponent) context.getModelReference(table);
		if (tableComponent == null)
			throw new JspException("table \"" + table + "\" not found");
		return new SelectProperties(getId(), null, tableComponent);
	}

	/**
	 * Returns the query.
	 *
	 * @return String
	 */
	public String getTable() {
		return table;
	}

	/**
	 * Sets the query.
	 *
	 * @param query
	 *            The query to set
	 */
	public void setTable(String query) {
		this.table = query;
	}

}
