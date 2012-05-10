package org.openi.util.plugin;

import java.io.File;

import org.apache.log4j.Logger;
import org.openi.pentaho.plugin.PluginConstants;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Utility class for plugin
 * 
 * @author SUJEN
 * 
 */
public class PluginUtils {

	private static Logger logger = Logger.getLogger(PluginUtils.class);

	final static IPluginManager pluginManager = (IPluginManager) PentahoSystem
			.get(IPluginManager.class, PentahoSessionHolder.getSession());

	/**
	 * returns the directory path of this plugin
	 * 
	 * @return
	 */
	public static String getPluginDir() {

		final PluginClassLoader pluginClassloader = (PluginClassLoader) pluginManager
				.getClassLoader(PluginConstants.PLUGIN_NAME);
		File pluginDir = pluginClassloader.getPluginDir();
		return pluginDir.getAbsolutePath();
	}

	/**
	 * prepares Spring bean factory from the plugin.spring.xml file
	 * 
	 * @return ConfigurableApplicationContext Spring Application Context
	 */
	public static ConfigurableApplicationContext getSpringBeanFactory() {
		final PluginClassLoader loader = (PluginClassLoader) pluginManager
				.getClassLoader(PluginConstants.PLUGIN_NAME);
		File f = new File(loader.getPluginDir(), "plugin.spring.xml");
		if (f.exists()) {
			logger.debug("Found plugin spring file @ " + f.getAbsolutePath());
			ConfigurableApplicationContext context = new FileSystemXmlApplicationContext(
					"file:" + f.getAbsolutePath()) {
				@Override
				protected void initBeanDefinitionReader(
						XmlBeanDefinitionReader beanDefinitionReader) {

					beanDefinitionReader.setBeanClassLoader(loader);
				}

				@Override
				protected void prepareBeanFactory(
						ConfigurableListableBeanFactory clBeanFactory) {
					super.prepareBeanFactory(clBeanFactory);
					clBeanFactory.setBeanClassLoader(loader);
				}

				/**
				 * Critically important to override this and return the desired
				 * CL
				 **/
				@Override
				public ClassLoader getClassLoader() {
					return loader;
				}
			};
			return context;
		}
		throw new IllegalStateException("no plugin.spring.xml file found"); 
	}
}
