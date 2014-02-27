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
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.FormatHandler;
import com.tonbeller.wcf.format.Formatter;
import com.tonbeller.wcf.ui.EditCtrl;
import com.tonbeller.wcf.ui.TypedCtrl;
import com.tonbeller.wcf.utils.XoplonNS;

/**
 * converts user input for a Edit control
 * @see com.tonbeller.wcf.ui.Edit
 * @author av
 */
public class EditCtrlConverter extends NodeConverterBase {
  private static Logger logger = Logger.getLogger(EditCtrlConverter.class);

  /**
   * updates a bean property as well as the value attribute of element
   */
  public void convert(Formatter formatter, Map param,  Map fileSource, Element element, Object bean)
    throws FormatException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    // disabled = true? return
    if (TypedCtrl.isDisabled(element))
      return;

    String id = EditCtrl.getId(element);
    String[] inputValue = (String[]) param.get(id);

    // input available
    if (inputValue != null && inputValue.length > 0) {

      XoplonNS.removeAttribute(element, "error");

      // parse input
      String formatString = EditCtrl.getFormatString(element);
      try {

        checkRequired(formatter.getLocale(), element, inputValue[0].trim().length() == 0);

        String type = EditCtrl.getType(element);
        FormatHandler handler = formatter.getHandler(type);
        if (handler == null)
          throw new FormatException("no handler found for type: " + type);

        Object newValue = handler.parse(inputValue[0], formatString);
        String strValue = handler.format(newValue, formatString);
        EditCtrl.setValue(element, strValue);

        String model = EditCtrl.getModelReference(element);
        if (bean != null && model.length() > 0) {
          PropertyUtils.setProperty(bean, model, newValue);
        }

      } catch (IllegalAccessException e) {
        logger.info("exception caught", e);
        XoplonNS.setAttribute(element, "error", e.getMessage());
        XoplonNS.setAttribute(element, "value", inputValue[0]);
        throw e;
      } catch (NoSuchMethodException e) {
        logger.info("exception caught", e);
        XoplonNS.setAttribute(element, "error", e.getMessage());
        XoplonNS.setAttribute(element, "value", inputValue[0]);
        throw e;
      } catch (InvocationTargetException e) {
        logger.info("exception caught", e);
        XoplonNS.setAttribute(element, "error", e.getMessage());
        XoplonNS.setAttribute(element, "value", inputValue[0]);
        throw e;
      } catch (FormatException e) {
        logger.info("invalid user input: " + e.getMessage());
        XoplonNS.setAttribute(element, "error", e.getMessage());
        XoplonNS.setAttribute(element, "value", inputValue[0]);
        throw e;
      }
    }
  }

  /**
   * initializes the value attribute of element from a bean property.
   */
  public void convert(Formatter formatter, Object bean, Element element)
    throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    try {
      String model = EditCtrl.getModelReference(element);
      if (model.length() == 0)
        return;

      String type = EditCtrl.getType(element);
      FormatHandler handler = formatter.getHandler(type);
      if (handler == null)
        throw new FormatException("no handler found for type: " + type);

      Object value = PropertyUtils.getProperty(bean, model);
      String pattern = EditCtrl.getFormatString(element);
      String strValue = handler.format(value, pattern);
      EditCtrl.setValue(element, strValue);

    } catch (IllegalAccessException e) {
      XoplonNS.setAttribute(element, "error", e.getMessage());
      throw e;
    } catch (NoSuchMethodException e) {
      XoplonNS.setAttribute(element, "error", e.getMessage());
      throw e;
    } catch (InvocationTargetException e) {
      XoplonNS.setAttribute(element, "error", e.getMessage());
      throw e;
    } catch (FormatException e) {
      XoplonNS.setAttribute(element, "error", e.getMessage());
      throw e;
    }
  }

}
