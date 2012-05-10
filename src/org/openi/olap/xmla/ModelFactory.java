package org.openi.olap.xmla;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.tonbeller.jpivot.core.Model;

/**
 * creates a model instance from an xml description. Example
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="utf-8"?&gt;
 * &lt;model id="m1" class="com.tonbeller.jpivot.core.ModelSupport"&gt;
 *   &lt;extension id="f1" class="com.tonbeller.jpivot.core.ExtensionSupport"/&gt;
 *   &lt;extension id="f2" class="com.tonbeller.jpivot.core.ExtensionSupport"/&gt;
 * &lt;/model&gt;
 * </pre>
 * 
 * @author av
 * @author SUJEN
 */

public class ModelFactory {

	/**
	 * singleton
	 */
	private ModelFactory() {
	}

	/**
	 * not for external use. Has to be public for the commons digester to access
	 * it.
	 */
	public static class ModelHolder {
		private Model model;

		public void setModel(Model model) {
			this.model = model;
		}

		public Model getModel() {
			return model;
		}
	}

	/**
	 * creates a model from an xml configuration file
	 * 
	 * @param url
	 *            url of model configuration file
	 * @return Model
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Model instance(URL url) throws SAXException, IOException {
		Digester digester = new Digester();
		digester.setValidating(false);

		/**
		 * Use same context class loader, so set to true if set to false, will
		 * use WebAppClassLoader, throws ClassNotFoundException, as the openi
		 * plugin related classes are not under webapps/../WEB-INF
		 */
		digester.setUseContextClassLoader(true);

		ModelHolder root = new ModelHolder();
		digester.push(root);

		digester.addObjectCreate("model", "missing \"class\" attribute",
				"class");
		digester.addSetProperties("model");
		digester.addSetNext("model", "setModel");

		digester.addObjectCreate("model/extension",
				"missing \"class\" attribute", "class");
		digester.addSetProperties("model/extension");
		digester.addSetNext("model/extension", "addExtension");

		//InputSource is = new InputSource(url.toExternalForm());
		digester.parse(url.openStream());
		//digester.parse(is);
		return root.getModel();
	}

}
