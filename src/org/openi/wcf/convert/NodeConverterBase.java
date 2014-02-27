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

import java.util.Locale;
import java.util.ResourceBundle;

import org.w3c.dom.Element;

import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.format.FormatException;


/**
 * @author andreas
 */
public abstract class NodeConverterBase implements NodeConverter {
  String elementName;

  /**
   * Returns the elementName.
   * @return String
   */
  public String getElementName() {
    return elementName;
  }

  /**
   * Sets the elementName.
   * @param elementName The elementName to set
   */
  public void setElementName(String elementName) {
    this.elementName = elementName;
  }
  /** 
   * throws a format exception if the
   */
  public void checkRequired(Locale locale, Element elem, boolean empty) {
    if (!"true".equals(elem.getAttribute("required")))
      return;
    if (empty) {
      Resources res = Resources.instance();
      if (res != null) {
          throw new FormatException(res.getString("jsp.wcf.input.required"));
      }
      else {
          ResourceBundle rb = ResourceBundle.getBundle("com.tonbeller.wcf.convert.messages", locale);
          throw new FormatException(rb.getString("jsp.wcf.input.required"));
      }
    }
  }

}
