package org.openi.pentaho.plugin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.web.servlet.JAXRSPluginServlet;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;

public class ExtendedJAXRSPluginServlet extends JAXRSPluginServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void initiate(ResourceConfig rc, WebApplication wa) {

		rc.getClasses().add(JacksonJaxbJsonProvider.class);
//	    rc.getContainerResponseFilters().add(new GZIPContentEncodingFilter());
		super.initiate(rc, wa);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader altLoader = this.getClass().getClassLoader();

		try {
			//temporarily swap out the context classloader to an alternate classloader if 
			//the targetBean has been loaded by one other than the context classloader.
			//This is necessary, so the classes can do a Class.forName and find the service 
			//class specified in the request
			if (altLoader != origLoader) {
				Thread.currentThread().setContextClassLoader(altLoader);
			}
			super.service(request, response);
		} finally {
			//reset the context classloader if necessary
			if ((altLoader != origLoader) && origLoader != null) {
				Thread.currentThread().setContextClassLoader(origLoader);
			}
		}
	}


	}
