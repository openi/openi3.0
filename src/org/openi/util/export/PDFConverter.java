package org.openi.util.export;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;
import org.apache.log4j.Logger;
import org.openi.pentaho.plugin.PluginConstants;
import org.openi.util.plugin.PluginUtils;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Utility class to convert the data formats like DOM, XML into PDF using Apache
 * FOP library
 * 
 * @author SUJEN
 * 
 */
public class PDFConverter {

	private static Logger logger = Logger.getLogger(PDFConverter.class);

	//private static FopFactory fopFactory = FopFactory.newInstance();

	/**
	 * converts document dom object to pdf file into specified location
	 * 
	 * @param doc
	 * @param pdfFile
	 * @throws FileNotFoundException
	 */
	public static void convertDOM2PDF(Document doc, File pdfFile)
			throws FileNotFoundException {
		convertDOM2PDF(doc, new java.io.FileOutputStream(pdfFile));
	}

	/**
	 * 
	 * @param doc
	 * @param out
	 */
	public static void convertDOM2PDF(Document doc, OutputStream out) {
		/*try {
			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
			out = new java.io.BufferedOutputStream(out);

			try {
				// Construct fop with desired output format and output stream
				Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF,
						foUserAgent, out);

				// Setup Identity Transformer
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer(); // identity
																	// transformer

				// Setup input for XSLT transformation
				Source src = new DOMSource(doc);

				// Resulting SAX events (the generated FO) must be piped through
				// to FOP
				Result res = new SAXResult(fop.getDefaultHandler());

				// Start XSLT transformation and FOP processing
				transformer.transform(src, res);
			} finally {
				out.close();
			}

		} catch (Exception e) {
			logger.error("Error whle converting DOM to PDF", e);
		}*/
	}

	/**
	 * converts document dom object to pdf file in the java.io.tmpdir
	 * 
	 * @param doc
	 * @return
	 */
	public static File convertDOM2PDF(Document doc) {
		return null;
	}

	/**
	 * Converts an FO file to a PDF file using FOP
	 * 
	 * @param fo
	 *            the FO file
	 * @param pdf
	 *            the target PDF file
	 * @throws IOException
	 *             In case of an I/O problem
	 * @throws FOPException
	 *             In case of a FOP problem
	 */
	public static void convertFO2PDF(File fo, File pdf) throws IOException,
			FOPException {
		convertFO2PDF(new FileInputStream(fo), new FileOutputStream(pdf));
	}

	/**
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void convertFO2PDF(InputStream in, OutputStream out) throws IOException {
		/*
		try {
			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
			out = new BufferedOutputStream(out);
			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent,
					out);
			Source src = new StreamSource(in);
			Result res = new SAXResult(fop.getDefaultHandler());

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(src, res);

			// Result processing
			FormattingResults foResults = fop.getResults();
			java.util.List pageSequences = foResults.getPageSequences();
			for (java.util.Iterator it = pageSequences.iterator(); it.hasNext();) {
				PageSequenceResults pageSequenceResults = (PageSequenceResults) it
						.next();
			}

		} catch (Exception e) {
			logger.error("Error whle converting FO to PDF", e);
		} finally {
			out.close();
		}*/
	}

	/**
	 * writes an FO file to an outputstream
	 * 
	 * @param fo
	 *            the FO file
	 * @param target
	 *            outputstream
	 * @throws IOException
	 *             In case of an I/O problem
	 */
	public static void convertFO2PDF(File fo, OutputStream out)
			throws IOException {
		InputStream in = new FileInputStream(fo);
		convertFO2PDF(in, out);
	}
	
	public static void convertFO2PDF(ByteArrayInputStream bain,
			ByteArrayOutputStream baout) throws IOException, FOPException,
			TransformerException {
		FopFactory fopFactory = FopFactory.newInstance();
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, baout);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		Source src = new StreamSource(bain);
		Result res = new SAXResult(fop.getDefaultHandler());
		transformer.transform(src, res);
	}
		
		
	
	public static void main(String args[]) {
		PDFConverter converter = new PDFConverter();
		URL foURL = converter.getClass().getResource("table_fo.xml");
		File pdfOutputFile = new File("C:\\Users\\SUJEN\\Desktop\\test.pdf");
		try {
			Source src = new StreamSource(foURL.openStream());
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(src, result);
			sw.flush();
			ByteArrayInputStream bain = new ByteArrayInputStream(sw.toString()
					.getBytes("UTF-8"));
			OutputStream out = new FileOutputStream(pdfOutputFile);
			ByteArrayOutputStream baout = new ByteArrayOutputStream(
					16384);
			PDFConverter.convertFO2PDF(bain, baout);
			final byte[] content = baout.toByteArray();
			out.write(content);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FOPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
