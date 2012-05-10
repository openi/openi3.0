package org.openi.navigator;

import javax.servlet.jsp.JspException;

import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.component.ComponentTag;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * 
 * @author av
 * @author SUJEN
 */
public class NavigatorTag extends ComponentTag {

  String query;

  /**
   * creates the navigator component
   */
  public Component createComponent(RequestContext context) throws JspException {
    OlapModel olapModel = (OlapModel) context.getModelReference(query);
    if (olapModel == null)
      throw new JspException("query \"" + query + "\" not found");
    return new Navigator(getId(), null, olapModel);
  }

  /**
   * Returns the query.
   * @return String
   */
  public String getQuery() {
    return query;
  }

  /**
   * Sets the query.
   * @param query The query to set
   */
  public void setQuery(String query) {
    this.query = query;
  }

}
