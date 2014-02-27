package org.openi.util.serialize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openi.acl.AccessController;
import org.openi.analysis.Analysis;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;

/**
 * 
 * @author SUJEN
 * 
 */
public class XMLBeanHelper {

	private static Logger logger = LogManager.getLogger(XMLBeanHelper.class);

	XStream binder;

	public XMLBeanHelper() {
		binder = new XStream();
	}

	public String beanToXMLString(Object bean) {
		return this.binder.toXML(bean);
	}

	public synchronized void beanToXMLFile(File xmlFile, Object bean)
			throws SerializationException {
		if (!xmlFile.exists()) {
			File parentDir = xmlFile.getParentFile();

			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
		}

		try {
			FileWriter writer = new FileWriter(xmlFile);
			saveBean(writer, bean);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new SerializationException(
					"could not write bean to xml file '" + xmlFile.getName(), e);
		} finally {
		}
	}

	public synchronized void saveBean(Writer writer, Object bean) {
		this.binder.toXML(bean, writer);
	}

	public Object xmlFileToBean(File xmlFile) throws SerializationException {
		if (!xmlFile.exists()) {
			throw new SerializationException(new FileNotFoundException(
					"could not find file " + xmlFile.getName()));
		}

		FileReader reader = null;
		try {
			reader = new FileReader(xmlFile);
		} catch (FileNotFoundException e) {
			throw new SerializationException(
					"could not open reader for bean file '" + xmlFile.getName()
							+ "'", e);
		}
		Object obj = null;

		try {
			obj = this.binder.fromXML(reader);
		} catch (ConversionException e) {
			throw new SerializationException(
					"Trouble restoring bean, caught ConversionException: ", e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				logger.warn(e);
			}
		}

		return obj;
	}
	
	public Object xmlStreamToBean(InputStream xmlStream) throws SerializationException {
		Object obj = null;

		try {
			obj = this.binder.fromXML(xmlStream);
		} catch (ConversionException e) {
			throw new SerializationException(
					"Trouble restoring bean, caught ConversionException: ", e);
		} finally {
			try {
				xmlStream.close();
			} catch (IOException e) {
				logger.warn(e);
			}
		}

		return obj;
	}

	public Object xmlStringToBean(String xmlString) {
		return this.binder.fromXML(xmlString);
	}

	public static void main(String args[]) {
		XMLBeanHelper beanHelper = new XMLBeanHelper();
		System.out.println(beanHelper.beanToXMLString(new Analysis()));
	}
}
