package org.openi.wcf.format;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.format.Formatter;
import com.tonbeller.wcf.utils.SoftException;

/**
 * @author av
 * @author SUJEN
 */
public class FormatterFactory {
	private static Logger logger = Logger.getLogger(FormatterFactory.class);

	private FormatterFactory() {
	}

	/**
	 * returns a new instance
	 */
	public static Formatter instance(Locale locale) {
		URL defaultXml = FormatterFactory.class.getResource("config.xml");
		Formatter formatter = new Formatter();
		fillFormatter(formatter, locale, defaultXml);
		String s = Resources.instance().getOptionalString(
				"wcf.formatter.config.xml", null);
		if (s != null) {
			URL analyseXml = Formatter.class.getResource(s);
			if (analyseXml == null) {
				analyseXml = defaultXml;
			}
			fillFormatter(formatter, locale, analyseXml);
		}

		return formatter;
	}

	private static void fillFormatter(Formatter formatter, Locale locale,
			URL configXml) {

		if (locale == null)
			locale = Locale.getDefault();

		URL rulesXml = Formatter.class.getResource("rules.xml");
		Digester digester = DigesterLoader.createDigester(rulesXml);

		digester.setValidating(false);
		//digester.setUseContextClassLoader(true);
		digester.setClassLoader(FormatterFactory.class.getClassLoader());

		digester.push(formatter);
		try {
			digester.parse(new InputSource(configXml.toExternalForm()));
		} catch (IOException e) {
			logger.error("exception caught", e);
			throw new SoftException(e);
		} catch (SAXException e) {
			logger.error("exception caught", e);
			throw new SoftException(e);
		}
		formatter.setLocale(locale);
	}

}
