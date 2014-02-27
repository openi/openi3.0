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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.w3c.dom.Element;

import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.Formatter;
import com.tonbeller.wcf.ui.Item;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * @author av
 * Converter for RadioButton and CheckBox.
 */

public abstract class BooleanConverter extends NodeConverterBase {
  public static final int UNKNOWN = 1;
  public static final int TRUE = 2;
  public static final int FALSE = 3;

  /**
   * sets the selected attribute of the DOM element. If a modelReference
   * is specified, it must point to a boolean bean-property that will be updated.
   *
   * @param fmt Formatter for i18n string-object conversion
   * @param params parameters of http request
   * @param elem the target element.
   * @param bean the target bean
   */
  public void convert(Formatter fmt, Map params, Map fileParams, Element elem, Object bean)
    throws FormatException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    int state = isSelected(elem, params);
    if (state == UNKNOWN)
      return;

    // was the checkbox checked?
    Boolean value = new Boolean(state == TRUE);

    // set into elem and bean
    DomUtils.removeAttribute(elem, "error");
    Item.setSelected(elem, value.booleanValue());

    // update bean
    String modelReference = Item.getModelReference(elem);
    if (bean != null && modelReference.length() > 0)
      PropertyUtils.setProperty(bean, Item.getModelReference(elem), value);
  }

  /**
   * evaluates the http paramters. Does not evaluate the selected attribute of the DOM Element.
   * @return one of TRUE, FALSE, UNKNOWN
   */
  public abstract int isSelected(Element elem, Map params);

  /**
   * sets the selected attribute of the checkbox from the bean
   */
  public void convert(Formatter fmt, Object bean, Element elem)
    throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    String modelReference = Item.getModelReference(elem);
    if (bean == null || modelReference.length() == 0)
      return;

    Boolean value = (Boolean) PropertyUtils.getProperty(bean, Item.getModelReference(elem));
    boolean b = (value == null) ? false : value.booleanValue();
    Item.setSelected(elem, b);
  }

}
