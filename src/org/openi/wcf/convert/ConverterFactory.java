package org.openi.wcf.convert;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.openi.util.wcf.ObjectFactory;
import org.xml.sax.SAXException;

import com.tonbeller.wcf.convert.Converter;
import com.tonbeller.wcf.format.Formatter;
import com.tonbeller.wcf.utils.SoftException;

/**
 * @author av
 * @author SUJEN
 */
public class ConverterFactory {
  private ConverterFactory() {
  }
  private static Logger logger = Logger.getLogger(ConverterFactory.class);

  /**
   * returns a new instance w/o caching
   */
  public static Converter instance(Formatter formatter) {
    URL configXml = ConverterFactory.class.getResource("config.xml");
    return instance(formatter, configXml);
  }

  public static Converter instance(Formatter formatter, URL configXml) {
    try {
      URL rulesXml = ConverterFactory.class.getResource("rules.xml");
      Converter conv = (Converter) ObjectFactory.instance(rulesXml, configXml);
      conv.setFormatter(formatter);
      return conv;
    } catch (SAXException e) {
      logger.error("?", e);
      throw new SoftException(e);
    } catch (IOException e) {
      logger.error("?", e);
      throw new SoftException(e);
    }
  }

}
