package org.openi.olap.xmla;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.log4j.Logger;

import com.tonbeller.wcf.utils.SoftException;

/**
 * static SOAP utility methods
 * @author SUJEN
 */
public class SoapUtil {

	public static final Logger logger = Logger.getLogger(SoapUtil.class);

	/**
	 * add a list of Restrictions/Properties ...
	 */
	public static void addParameterList(SOAPEnvelope envelope,
			SOAPElement eParent, String typeName, String listName, Map params)
			throws SOAPException {
		Name nPara = envelope.createName(typeName);
		SOAPElement eType = eParent.addChildElement(nPara);
		nPara = envelope.createName(listName);
		SOAPElement eList = eType.addChildElement(nPara);
		if (params == null)
			return;
		Set keys = params.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String tag = (String) it.next();
			String value = (String) params.get(tag);
			nPara = envelope.createName(tag);
			SOAPElement eTag = eList.addChildElement(nPara);
			eTag.addTextNode(value);
		}
	}

	/**
	 * print reply to output to System.out
	 */
	public static void printReply(SOAPMessage reply) throws SOAPException {
		// Document source, do a transform.
		System.out.println("Reply:");
		SOAPPart sp = reply.getSOAPPart();
		SOAPEnvelope envelope = sp.getEnvelope();
		SOAPBody body = envelope.getBody();
		Iterator itBody = body.getChildElements();
		while (itBody.hasNext()) {
			SOAPElement element = (SOAPElement) itBody.next();
			printElement(element);
		}
		System.out.println();
	}

	/**
	 * recursively print element
	 * 
	 * @param el
	 */
	static private void printElement(SOAPElement el) {

		System.out.println(el.getElementName() + el.getValue());
		Iterator itAtt = el.getAllAttributes();
		if (itAtt.hasNext()) {
			System.out.print("<" + el.getElementName());
			while (itAtt.hasNext()) {
				SOAPElement att = (SOAPElement) itAtt.next();
				System.out.print(" " + att.getElementName() + "="
						+ att.getValue());
			}
			System.out.println(">");
		} else {
			System.out.println("<" + el.getElementName() + ">");
		}

		System.out.println(el.getValue());
		System.out.println("</" + el.getElementName() + ">");

		Iterator it = el.getChildElements();
		while (it.hasNext()) {
			SOAPElement element = (SOAPElement) it.next();
			printElement(element);
		}
	}

	/**
	 * run Discover request
	 */
	public static SOAPMessage createDiscoverMsg(String dataSource,
			String catalog, String request, Map restrictMap)
			throws SOAPException {
		MessageFactory mf = MessageFactory.newInstance();
		SOAPMessage msg = mf.createMessage();

		MimeHeaders mh = msg.getMimeHeaders();
		mh.setHeader("SOAPAction",
				"\"urn:schemas-microsoft-com:xml-analysis:Discover\"");

		SOAPPart soapPart = msg.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPBody body = envelope.getBody();
		Name nDiscover = envelope.createName("Discover", "",
				"urn:schemas-microsoft-com:xml-analysis");

		SOAPElement eDiscover = body.addChildElement(nDiscover);

		// add the parameters

		// <RequestType>request</RequestType>
		Name nPara = envelope.createName("RequestType");
		SOAPElement eRequestType = eDiscover.addChildElement(nPara);
		eRequestType.addTextNode(request);
		SoapUtil.addParameterList(envelope, eDiscover, "Restrictions",
				"RestrictionList", restrictMap);

		// <Properties>
		// <PropertyList>
		// <DataSourceInfo>Provider=MSOLAP;Data Source=local</DataSourceInfo>
		// <Catalog>Foodmart 2000</Catalog>
		// <Format>Tabular</Format>
		// <Content>SchemaData</Content>
		// </PropertyList>
		// </Properties>
		HashMap pHash = new HashMap();
		pHash.put("DataSourceInfo", dataSource);
		pHash.put("Catalog", catalog);

		pHash.put("Format", "Tabular");
		pHash.put("Content", "SchemaData");
		SoapUtil.addParameterList(envelope, eDiscover, "Properties",
				"PropertyList", pHash);

		msg.saveChanges();
		return msg;
	}

	/**
	 * check SOAP reply for Error, return fault Code
	 * 
	 * @param reply
	 *            the message to check
	 * @param aReturn
	 *            ArrayList containing faultcode,faultstring,faultactor
	 */
	public static boolean soapFault(SOAPMessage reply, String[] faults)
			throws SOAPException {
		SOAPPart sp = reply.getSOAPPart();
		SOAPEnvelope envelope = sp.getEnvelope();
		SOAPBody body = envelope.getBody();
		if (!body.hasFault())
			return false;
		SOAPFault fault = body.getFault();

		faults[0] = fault.getFaultCode();
		faults[1] = fault.getFaultString();
		faults[2] = fault.getFaultActor();

		return true;

	}

	public static URL addUserPassword(URL url, String user, String password) {
		try {
			if (user != null && user.length() > 0) {
				String newUri = url.getProtocol() + "://" + user;
				if (password != null && password.length() > 0) {
					newUri += ":" + password;
				}
				newUri += "@" + url.getHost() + ":" + url.getPort()
						+ url.getPath();
				return new URL(newUri);
			}
			return url;
		} catch (MalformedURLException e) {
			logger.error("?", e);
			throw new SoftException(e);
		}
	}

} // End SoapUtil
