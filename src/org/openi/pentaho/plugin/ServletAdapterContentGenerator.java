package org.openi.pentaho.plugin;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.modules.jersey.EnunciateJerseyServletContainer;
import org.openi.util.plugin.PluginUtils;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.wrapper.SavedRequestAwareWrapper;

/**
 * 
 * @author SUJEN
 * 
 */
@SuppressWarnings("serial")
public class ServletAdapterContentGenerator extends BaseContentGenerator {

	private static final Log logger = LogFactory
			.getLog(ServletAdapterContentGenerator.class);

	private IPluginManager pm = PentahoSystem.get(IPluginManager.class);

	private static ConfigurableApplicationContext appContext;

	// private static JAXRSPluginServlet servlet;
	private static EnunciateJerseyServletContainer servlet;

	public ServletAdapterContentGenerator() throws ServletException {
		final ClassLoader origLoader = Thread.currentThread()
				.getContextClassLoader();
		final PluginClassLoader tempLoader = (PluginClassLoader) pm
				.getClassLoader(PluginConstants.PLUGIN_NAME);
		try {
			Thread.currentThread().setContextClassLoader(tempLoader);

			if (appContext == null) {
				appContext = PluginUtils.getSpringBeanFactory();
				// servlet = (JAXRSPluginServlet)
				// appContext.getBean("jaxrsPluginServlet");
				servlet = (EnunciateJerseyServletContainer) appContext
						.getBean("enunciatePluginServlet");
				servlet.init(new MutableServletConfig(
						"ServletAdapterContentGenerator"));
			}
		} finally {
			Thread.currentThread().setContextClassLoader(origLoader);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void createContent() throws Exception {
		Object requestOrWrapper = this.parameterProviders.get("path")
				.getParameter("httprequest");
		HttpServletRequest request = null;
		if (requestOrWrapper instanceof SavedRequestAwareWrapper) {
			request = (HttpServletRequest) ((SavedRequestAwareWrapper) requestOrWrapper)
					.getRequest();
		} else {
			request = (HttpServletRequest) requestOrWrapper;
		}
		HttpServletResponse response = (HttpServletResponse) this.parameterProviders
				.get("path").getParameter("httpresponse");

		final ClassLoader origLoader = Thread.currentThread()
				.getContextClassLoader();
		final PluginClassLoader tempLoader = (PluginClassLoader) pm
				.getClassLoader(PluginConstants.PLUGIN_NAME);
		try {
			Thread.currentThread().setContextClassLoader(tempLoader);
			servlet.service(request, response);
		} finally {
			Thread.currentThread().setContextClassLoader(origLoader);
		}
	}

	@Override
	public Log getLogger() {
		return logger;
	}

}
