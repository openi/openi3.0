/*
 * The MIT License
 * 
 * Copyright (c) 2011, Aaron Phillips
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.openi.pentaho.plugin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * This should only be used by a plugin in the plugin.spring.xml file to
 * initialize a Jersey. The presence of this servlet in the spring file will
 * make it possible to write JAX-RS POJOs in your plugin.
 * 
 * @author Aaron Phillips
 * @author SUJEN
 */
public class JAXRSPluginServlet extends SpringServlet implements
		ApplicationContextAware {

	private static final long serialVersionUID = 457538570048660945L;

	private ApplicationContext applicationContext;

	private static final Log logger = LogFactory
			.getLog(JAXRSPluginServlet.class);

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		if(logger.isDebugEnabled())
			logger.debug("setting the application context in JaxRSPluginSerlvet");
		this.applicationContext = applicationContext;
	}

	@Override
	protected ConfigurableApplicationContext getContext() {
		return (ConfigurableApplicationContext) applicationContext;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("servicing request for resource " + request.getPathInfo()); //$NON-NLS-1$
		super.service(request, response);
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		super.service(req, res);
	}

	@Override
	protected void initiate(ResourceConfig rc, WebApplication wa) {
		if (logger.isDebugEnabled()) {
			rc.getFeatures().put(ResourceConfig.FEATURE_TRACE, true);
			rc.getFeatures()
					.put(ResourceConfig.FEATURE_TRACE_PER_REQUEST, true);
		}
		super.initiate(rc, wa);
	}

}
