package org.openi.wcf.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.log4j.Logger;
import org.openi.wcf.convert.ConverterFactory;
import org.openi.wcf.format.FormatterFactory;

import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.tbutils.res.ResourcesFactory;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.convert.Converter;
import com.tonbeller.wcf.format.Formatter;

/**
 * @author av
 * @author SUJEN
 */
public class RequestContextFactoryImpl implements RequestContextFactory {
  Formatter formatter;
  Converter converter;
  Locale locale;
  Resources resources;

  String remoteUser;
  String remoteDomain;

  private static Logger logger = Logger.getLogger(RequestContextFactoryImpl.class);

  public RequestContext createContext(HttpServletRequest request, HttpServletResponse response) {
    initialize(request);
    return new RequestContextImpl(this, request, response);
  }

  protected void initialize(HttpServletRequest request) {
    if (locale == null) {
      locale = (Locale) request.getSession().getAttribute("userLocale");
      if (locale == null) {
          locale = ResourcesFactory.instance().getFixedLocale();
      }
      if (locale == null)
        locale = request.getLocale();
      if (locale == null)
        locale = Locale.getDefault();
      formatter = FormatterFactory.instance(locale);
      converter = ConverterFactory.instance(formatter);
      resources = Resources.instance(locale, getClass());

      String rUser = request.getRemoteUser();
      if (rUser != null) {
        int slash = rUser.indexOf('/');
        if (slash >= 0) {
          remoteDomain = rUser.substring(0, slash);
          remoteUser = rUser.substring(slash + 1);
        } else {
          remoteUser = rUser;
          remoteDomain = null;
        }
        if (remoteUser != null)
          remoteUser = remoteUser.trim();
        if (remoteDomain != null)
          remoteDomain = remoteDomain.trim();
      }

    }
  }

  /**
   * change the locale
   * @param locale the new locale or null to determine 
   * the locale from the next http request
   * @deprecated 
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
    if (locale != null) {
      formatter = FormatterFactory.instance(locale);
      converter = ConverterFactory.instance(formatter);
      resources = Resources.instance(locale, getClass());
    }
  }

  /**
   * change the locale including the Locale for JSTL &gt;fmt:message&gt; tags
   * 
   * @param locale the new locale
   * @param session current session
   */
  public void setLocale(HttpServletRequest request, Locale locale) {
    this.locale = locale;
    if (locale != null) {
      formatter = FormatterFactory.instance(locale);
      converter = ConverterFactory.instance(formatter);
      resources = Resources.instance(locale, getClass());
      if (logger.isInfoEnabled())
        logger.info("setting locale to " + locale);
      Config.set(request, Config.FMT_LOCALE, locale);
    }
  }

  public Converter getConverter() {
    return converter;
  }

  public Formatter getFormatter() {
    return formatter;
  }

  public Locale getLocale() {
    return locale;
  }

  public Resources getResources() {
    return resources;
  }

  public String getRemoteDomain() {
    return remoteDomain;
  }

  public String getRemoteUser() {
    return remoteUser;
  }

}