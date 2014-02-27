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

import org.w3c.dom.Element;

import com.tonbeller.wcf.convert.ConvertException;
import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.Formatter;

/**
 *
 * @author av
 */
public interface NodeConverter {

  /**
   * converts http parameters in requestSource and updates the domTarget and beanTarget.
   * the domTarget also contains metadata, e.g. the type and formatString attributes.
   * @param fmt Formatter to be used to parse and format user input
   * @param requestSource a map containing http parameters. key = String, value = String[]
   * @param domTarget a DOM tree containing UI elements like TextField etc including the following
   * attributes:
   * <ul>
   * <li><code>type</code> the data type, i.e. the id to find the formatter
   * <li><code>value</code> the formatted value will be written to this attribute
   * <li><code>formatString</code> optional, overwrites the default formatString of the FormatHandler
   * <li><code>modelReference</code> optional, if present the attribute name of the bean property.
   * a jakarta-commons/bean-utils path is supported
   * <li><code>id</code> unique id. Used to identify DOM elements from HTTP Parameters
   * </ul>
   * @param
   */
  void convert(Formatter fmt, Map requestSource, Map fileSource, Element domTarget, Object beanTarget)
    throws ConvertException, FormatException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;

  /**
   * updates the domTarget according to the current property values of beanSource
   */
  void convert(Formatter fmt, Object beanSource, Element domTarget)
    throws ConvertException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;

  /**
   * return the element name that this handler will handle
   */
  String getElementName();
  void setElementName(String elemName);


}
