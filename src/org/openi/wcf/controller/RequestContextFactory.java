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
package org.openi.wcf.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.convert.Converter;
import com.tonbeller.wcf.format.Formatter;

/**
 * creates a RequestContext from request / response.
 * SPI interface only. Use RequestContextFactoryFinder to create a context.
 * 
 * @see RequestContextFactoryFinder#createContext(HttpServletRequest, HttpServletResponse, boolean)
 * @see RequestContext#instance()
 * 
 * @author av
 * @author SUJEN
 */
public interface RequestContextFactory {

  /**
   * create a new context
   */
  RequestContext createContext(HttpServletRequest request, HttpServletResponse response);
  
  Formatter getFormatter();
  Converter getConverter();
  Locale getLocale();
  Resources getResources();

  /** @deprecated */
  void setLocale(Locale locale);
  void setLocale(HttpServletRequest session, Locale locale);

  String getRemoteUser();
  String getRemoteDomain();

}
