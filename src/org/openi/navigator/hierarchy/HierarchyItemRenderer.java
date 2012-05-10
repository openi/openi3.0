package org.openi.navigator.hierarchy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.wcf.catedit.Category;
import com.tonbeller.wcf.catedit.DefaultItemElementRenderer;
import com.tonbeller.wcf.catedit.Item;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * renders a Hierarchy 
 * @author av
 * @author SUJEN
 */
public class HierarchyItemRenderer extends DefaultItemElementRenderer {

  public Element render(RequestContext context, Document factory, Category cat, Item item) {
    Element elem = super.render(context, factory, cat, item);
    HierarchyItem hi = (HierarchyItem)item;
    if (hi.isClickable()) {
      elem.setAttribute("id", hi.getId());
      if(((AbstractCategory) cat).isSlicer())
    		  elem.setAttribute("type", "Slicer");
      else if(((AbstractCategory) cat) instanceof AxisCategory && ((AbstractCategory) cat).getName().equals("Rows"))
		  elem.setAttribute("type", "Row");
      else if(((AbstractCategory) cat) instanceof AxisCategory && ((AbstractCategory) cat).getName().equals("Columns"))
		  elem.setAttribute("type", "Column");
    }

    if (!hi.getSlicerSelection().isEmpty()) {
      Member m = (Member)hi.getSlicerSelection().get(0);
      Element e = DomUtils.appendElement(elem, "slicer-value");
      e.setAttribute("label", m.getLabel());
      e.setAttribute("level", m.getLevel().getLabel());
    }
    
    return elem;
    
  }
}
