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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.w3c.dom.Element;

import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.FormatHandler;
import com.tonbeller.wcf.format.Formatter;
import com.tonbeller.wcf.ui.Item;
import com.tonbeller.wcf.ui.SelectMultiple;

/**
 * sets an array bean property. The values of all selected items are collected in an
 * array and the bean property is set to that array. If no items are selected, the
 * bean property will contain an empty array.
 * <p>
 * An items value is the value attribute of the elected item. 
 * For type conversion the type, modelReference and formatString attributes
 * will be taken from the items parent (e.g. the listBox).
 * 
 * @author av
 */
public class SelectMultipleConverter extends SelectConverterBase {

  /**
   * @see com.tonbeller.wcf.convert.SelectConverterBase#updateModelReference(Formatter, Map, Element, Object)
   */
  protected void updateModelReference(Formatter fmt, Element elem, Object bean)
    throws FormatException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    String model = SelectMultiple.getModelReference(elem);
    if (model.length() == 0)
      return;
    
    String type = SelectMultiple.getType(elem);
    String formatString = SelectMultiple.getFormatString(elem);
    FormatHandler parser = fmt.getHandler(type);
    if (parser == null)
      throw new FormatException("no handler found for type: " + type);
    
    List items = SelectMultiple.getSelectedItems(elem);
    checkRequired(fmt.getLocale(), elem, items.size() == 0);
    List values = new ArrayList();
    
    for (Iterator it = items.iterator(); it.hasNext();) {
      Element item = (Element)it.next();
      String valueString = Item.getValue(item);
      Object value = parser.parse(valueString, formatString);
      values.add(value);
    }
    
    PropertyUtils.setProperty(bean, model, parser.toNativeArray(values));
  }


}
