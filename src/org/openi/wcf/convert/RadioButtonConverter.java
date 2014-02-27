/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 *
 * 
 */
package org.openi.wcf.convert;

import java.util.Map;

import org.w3c.dom.Element;

import com.tonbeller.wcf.ui.RadioButton;

/**
 * Created on 14.11.2002
 * 
 * @author av
 */
public class RadioButtonConverter extends BooleanConverter {

  /**
   * @see com.tonbeller.wcf.convert.BooleanConverter#isSelected(Element, Map)
   */
  public int isSelected(Element elem, Map params) {

    // disabled = true? return
    if (RadioButton.isDisabled(elem))
      return UNKNOWN;

    // was the form submitted at all?
    String id = RadioButton.getId(elem);
    Object inputAvailable = params.get(id + ".valid");
    if (inputAvailable == null)
      return UNKNOWN;
    
    String groupId = RadioButton.getGroupId(elem);
    String[] values = (String[])params.get(groupId);
    if (values == null)
      return FALSE;
    // values.length should be 1, but there may be other booleans with the same name
    for (int i = 0; i < values.length; i++)
      if (values[i].equals(id))
        return TRUE;
    return FALSE;
    
  }

}
