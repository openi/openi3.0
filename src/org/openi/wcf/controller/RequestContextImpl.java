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
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.controller.MultiPartEnabledRequest;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.convert.Converter;
import com.tonbeller.wcf.expr.ExprContext;
import com.tonbeller.wcf.expr.ExprUtils;
import com.tonbeller.wcf.format.Formatter;
/**
 * @author av
 * @author SUJEN
 */
public class RequestContextImpl extends RequestContext implements ExprContext {
  protected RequestContextFactory rcf;
  protected HttpServletRequest request;
  protected HttpServletResponse response;
  protected static Logger logger = Logger.getLogger(RequestContextImpl.class);

  public RequestContextImpl(RequestContextFactory rcf, HttpServletRequest request,
      HttpServletResponse response) {
    this.rcf = rcf;
    this.request = request;
    this.response = response;
  }

  public void invalidate() {
    super.invalidate();
    this.rcf = null;
    this.request = null;
    this.response = null;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public ServletContext getServletContext() {
    return getSession().getServletContext();
  }

  public HttpSession getSession() {
    return request.getSession(true);
  }

  public Locale getLocale() {
    return rcf.getLocale();
  }

  public Formatter getFormatter() {
    return rcf.getFormatter();
  }

  public Converter getConverter() {
    return rcf.getConverter();
  }

  public Map getParameters() {
    return request.getParameterMap();
  }

  public String[] getParameters(String name) {
    return request.getParameterValues(name);
  }

  public String getParameter(String name) {
    return request.getParameter(name);
  }

  public Object getModelReference(String expr) {
    if (expr == null || expr.length() == 0)
      return null;
    FacesContext fc = FacesContext.getCurrentInstance();
    if (fc != null && (expr.startsWith("#{") || expr.startsWith("#("))) {
      ValueBinding vb = fc.getApplication().createValueBinding(expr);
      return vb.getValue(fc);
    }
    // ExprUtils.checkExpr(expr);
    return ExprUtils.getModelReference(this, expr);
  }

  public void setModelReference(String expr, Object value) {
    FacesContext fc = FacesContext.getCurrentInstance();
    if (fc != null && (expr.startsWith("#{") || expr.startsWith("#("))) {
      ValueBinding vb = fc.getApplication().createValueBinding(expr);
      vb.setValue(fc, value);
    } else {
      // ExprUtils.checkExpr(expr);
      ExprUtils.setModelReference(this, expr, value);
    }
  }

  /**
   * same as JSP PageContext.findAttribute() except it does not search in page
   * scope.
   */
  public Object findBean(String name) {
    Object bean = request.getAttribute(name);
    if (bean != null)
      return bean;
    HttpSession session = getSession();
    bean = session.getAttribute(name);
    if (bean != null)
      return bean;
    ServletContext context = getServletContext();
    bean = context.getAttribute(name);
    if (bean != null)
      return bean;
    return null;
  }

  public void setBean(String name, Object bean) {
    HttpSession session = getSession();
    if (bean == null)
      session.removeAttribute(name);
    else
      session.setAttribute(name, bean);
  }

  public boolean isUserInRole(String roleExpr) {
    if (roleExpr == null || roleExpr.length() == 0)
      return true;
    // evaluate modelReference
    String ref = (String) getModelReference(roleExpr);
    if (ref != null)
      roleExpr = ref;
    boolean success = true;
    if (roleExpr.startsWith("!")) {
      roleExpr = roleExpr.substring(1);
      success = false;
    }

    // root has all roles
    if (isAdmin())
      return success;

    // evaluate mapping
    String mappedRole = getMappedRole(roleExpr);
    if (mappedRole != null)
      roleExpr = mappedRole;
    // test environment?
    if (request == null)
      return !success;
    // loop over roles in comma/space separated list
    StringTokenizer st = new StringTokenizer(roleExpr, ", ", false);
    while (st.hasMoreTokens()) {
      if (internalIsUserInRole(st.nextToken()))
        return success;
    }
    return !success;
  }

  /**
   * allows derived classes to use another authentification framework.
   * Roles may be looked up in a database for example.
   *
   * @param role the role to test
   */
  protected boolean internalIsUserInRole(String role) {
    return request.isUserInRole(role);
  }

  /**
   * maps a role to another role. The application may define fine grained,
   * virtual roles for buttons, components etc. These roles may be mapped to a
   * few real roles like "user", "expert" etc.
   * <p />
   *
   * @param role
   *          name of the virtual fine grained role, e.g. "mayPressButton23role"
   * @return a real role, e.g. "expertUser" to allow only experts to press
   *         button23 or null, if no such mapping exists and the role is defined
   *         elsewhere. May return a comma separated list of roles, e.g.
   *         "expertUser,adminUser" to allow access to any of the roles.
   */
  protected String getMappedRole(String role) {
    try {
      return rcf.getResources().getString("role." + role);
    } catch (MissingResourceException e) {
      return null;
    }
  }

  public Resources getResources() {
    return Resources.instance(getLocale());
  }

  public Resources getResources(String bundleName) {
    return Resources.instance(getLocale(), bundleName);
  }

  public Resources getResources(Class clasz) {
    return Resources.instance(getLocale(), clasz);
  }

  public String getRemoteUser() {
    return rcf.getRemoteUser();
  }

  public String getRemoteDomain() {
    return rcf.getRemoteDomain();
  }

  public boolean isAdmin() {
    return false;
  }

  public void setLocale(Locale locale) {
    rcf.setLocale(request, locale);
  }

  public Map getFileParameters() {
    if(request instanceof MultiPartEnabledRequest)
      return ((MultiPartEnabledRequest)request).getFileParameterMap();

    return null;
  }
}