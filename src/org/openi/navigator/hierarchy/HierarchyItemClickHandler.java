package org.openi.navigator.hierarchy;

import org.openi.navigator.member.MemberSelectionModel;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * called when the user clicks on a HierarchyItem
 * 
 * @author av
 * @author SUJEN
 * 
 */
public interface HierarchyItemClickHandler {
  void itemClicked(RequestContext context, HierarchyItem item, MemberSelectionModel selection, boolean allowChangeOrder);
}
