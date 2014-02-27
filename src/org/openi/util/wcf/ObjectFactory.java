package org.openi.util.wcf;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.SAXException;

/**
 * Creates Object instances via Digester xml rules. For more info see <a
 * href="http://jakarta.apache.org/commons/digester.html">Commons Digester</a>.
 * 
 * @author av
 * @author SUJEN
 */
public class ObjectFactory {

	/**
	 * not for external use. Has to be public for the commons digester to access
	 * it.
	 */
	public static class ObjectHolder {
		private Object object;

		public void setObject(Object object) {
			this.object = object;
		}

		public Object getObject() {
			return object;
		}
	}

	private ObjectFactory() {
	}

	public static Object instance(URL rulesXml, URL configXml)
			throws SAXException, IOException {

		Digester digester = DigesterLoader.createDigester(rulesXml);
		digester.setValidating(false);

		/**
		 * Use same context class loader, so set to true if set to false, will
		 * use WebAppClassLoader, throws ClassNotFoundException, as the openi
		 * plugin related classes are not under webapps/../WEB-INF
		 */
		//digester.setUseContextClassLoader(true);
		digester.setClassLoader(ObjectFactory.class.getClassLoader());

		ObjectHolder root = new ObjectHolder();
		digester.push(root);

		digester.parse(configXml.openStream());
		return root.getObject();
	}

}
