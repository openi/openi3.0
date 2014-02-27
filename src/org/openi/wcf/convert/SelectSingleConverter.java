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

import org.apache.commons.beanutils.PropertyUtils;
import org.w3c.dom.Element;

import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.FormatHandler;
import com.tonbeller.wcf.format.Formatter;
import com.tonbeller.wcf.ui.Item;
import com.tonbeller.wcf.ui.SelectSingle;

/**
 * sets a scalar bean property to the value of the selected item. If no item
 * is selected, nothing will be done. 
 * 
 * <p>
 * An items value is the value attribute of the elected item. 
 * For type conversion the type, modelReference and formatString attributes
 * will be taken from the items parent (e.g. the listBox).
 *
 * @author av
 */
public class SelectSingleConverter extends SelectConverterBase {

  protected void updateModelReference(Formatter fmt, Element elem, Object bean) throws FormatException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    String model = SelectSingle.getModelReference(elem);
    if (model.length() == 0)
      return;
    
    String type = SelectSingle.getType(elem);
    String formatString = SelectSingle.getFormatString(elem);
    FormatHandler parser = fmt.getHandler(type);
    if (parser == null)
      throw new FormatException("no handler found for type: " + type);
    
    Element item = SelectSingle.getSelectedItem(elem);
    if (item == null)
      return;

    String valueString = Item.getValue(item);
    Object value = parser.parse(valueString, formatString);
    PropertyUtils.setProperty(bean, model, value);
  }


}
