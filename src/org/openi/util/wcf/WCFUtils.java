package org.openi.util.wcf;

import org.apache.log4j.Logger;
import org.openi.util.file.FileUtils;
import org.openi.util.plugin.PluginUtils;
import org.openi.util.xml.XmlUtils;
import org.openi.wcf.component.WCFComponentType;
import org.openi.wcf.component.WCFRender;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tonbeller.wcf.component.Renderable;
import com.tonbeller.wcf.component.RoleExprHolder;
import com.tonbeller.wcf.component.Visible;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestContextFactoryFinder;
import com.tonbeller.wcf.expr.ExprUtils;
import com.tonbeller.wcf.token.RequestToken;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * 
 * @author SUJEN
 * 
 */
public class WCFUtils {

	private static Logger logger = Logger.getLogger(WCFUtils.class);

	public static String componentToXMLString(WCFRender wcfRender,
			RequestContext context, String compId) throws TransformerFactoryConfigurationError, Exception {
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		Renderable comp = getComp(wcfRender, context);
		if (comp == null)
			return "";
		Document document = componentToDocument(comp, context);
		Map params = createPredefinedParameters(context, compId,
				wcfRender.getRef());
		setXmlParameters(document, params);
		DOMSource source = new DOMSource(document);
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);

		transformer.transform(source, result);
		sw.flush();
		return sw.toString();
	}

	public static String componentToHTMLString(WCFRender wcfRender,
			RequestContext context, String compId) throws Exception {
		// Renderable comp = getComp(wcfRender, context);
		Renderable comp = (Renderable) (context.getSession()
				.getAttribute((String) wcfRender.getRef()));
		if (comp == null)
			return "";
		Map params = createPredefinedParameters(context, compId,
				wcfRender.getRef());
		Transformer transformer = XmlUtils.getTransformer(new File(
				(String) wcfRender.getXslUri()), (Boolean) wcfRender
				.isXslCache());
		setXslParameters(context, transformer, params);
		Document document = componentToDocument(comp, context);
		setXmlParameters(document, params);
		DOMSource source = new DOMSource(document);
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		transformer.transform(source, result);
		return sw.toString();
	}

	public static Document componentToDocument(Renderable comp,
			RequestContext context) throws Exception {
		return comp.render(context);
	}

	private static void setXmlParameters(Document document, Map parameters) {
		Element root = document.getDocumentElement();
		if (root != null && "xform".equals(root.getNodeName()))
			setXmlParameters(document.getChildNodes(), parameters);
	}

	private static void setXmlParameters(NodeList list, Map parameters) {
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Node n = list.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element x = (Element) list.item(i);

			if ("param".equals(x.getNodeName())) {
				String paramName = x.getAttribute("name");
				String attrName = x.getAttribute("attr");
				String value = (String) parameters.get(paramName);
				Element parent = (Element) x.getParentNode();
				if (value == null || value.length() == 0)
					DomUtils.removeAttribute(parent, attrName);
				else
					parent.setAttribute(attrName, value);
			} else {
				setXmlParameters(x.getChildNodes(), parameters);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Map createPredefinedParameters(RequestContext context,
			String id, Object ref) throws MalformedURLException {
		String renderId = id;
		Map parameters = new HashMap();
		if (renderId == null || renderId.length() == 0)
			renderId = ExprUtils.getBeanName((String) ref);
		parameters.put("renderId", renderId);
		parameters.put("context", context.getRequest().getContextPath());
		// Some FOP-PDF versions require a complete URL, not a path
		parameters.put("contextUrl", createContextURLValue(context));

		// if there, add token to control page flow
		RequestToken tok = RequestToken.instance(context.getSession());
		if (tok != null) {
			parameters.put("token",
					tok.getHttpParameterName() + "=" + tok.getToken());
		}

		return parameters;
	}

	private static String createContextURLValue(RequestContext context) throws MalformedURLException {

		if (context.getRequest() == null
				|| context.getRequest().getRequestURL() == null) {
			return "UNDEFINED";
		}

		URL url = new URL((context.getRequest()).getRequestURL().toString());

		StringBuffer c = new StringBuffer();
		c.append(url.getProtocol());
		c.append("://");
		c.append(url.getHost());
		if (url.getPort() != 80) {
			c.append(":");
			c.append(url.getPort());
		}
		c.append(context.getRequest().getContextPath());

		return c.toString();
	}

	private static Renderable getComp(WCFRender wcfRender,
			RequestContext context) throws Exception {
		Object compRef = wcfRender.getRef();
		Object x = context.getModelReference((String) compRef);
		if (x == null || !(x instanceof Renderable))
			throw new Exception("component \"" + compRef + "\" not found");
		if (!(x instanceof Renderable))
			throw new Exception("component \"" + compRef
					+ "\" is not Renderable: " + x.getClass());
		Renderable comp = (Renderable) x;

		if (comp instanceof Visible && !((Visible) comp).isVisible())
			return null;

		if (comp instanceof RoleExprHolder) {
			String roleExpr = ((RoleExprHolder) comp).getRoleExpr();
			if (!context.isUserInRole(roleExpr))
				return null;
		}
		return comp;
	}

	private static void setXslParameters(RequestContext context,
			Transformer transformer, Map parameters) {
		for (Iterator it = parameters.keySet().iterator(); it.hasNext();) {
			String name = (String) it.next();
			Object value = parameters.get(name);
			transformer.setParameter(name, value);
		}
	}

	public static RequestContext getRequestContext(HttpServletRequest request,
			HttpServletResponse response) {
		return RequestContextFactoryFinder.createContext(request, response,
				true);
	}

	public static String getWCFComponentXSLUri(WCFComponentType componentType) {
		File xslUriFile = null;
		if (componentType == WCFComponentType.TABLE)
			xslUriFile = new File(PluginUtils.getPluginDir(),
					"ui/resources/jpivot/table/mdxtable.xsl");
		else if (componentType == WCFComponentType.CHART)
			xslUriFile = new File(PluginUtils.getPluginDir(),
					"ui/resources/jpivot/chart/chart.xsl");
		else if (componentType == WCFComponentType.DRILLTHROUGHTABLE)
			xslUriFile = new File(PluginUtils.getPluginDir(),
					"ui/resources/jpivot/table/dttable.xsl");
		else if (componentType == WCFComponentType.NAVIGATOR)
			xslUriFile = new File(PluginUtils.getPluginDir(),
					"ui/resources/jpivot/navi/navigator.xsl");
		else if (componentType == WCFComponentType.MEMBERNAVIGATOR)
			xslUriFile = new File(PluginUtils.getPluginDir(),
					"ui/resources/jpivot/navi/xtree.xsl");
		else if (componentType == WCFComponentType.MDXEDITFORM
				|| componentType == WCFComponentType.PRINTFORM
				|| componentType == WCFComponentType.CHARTPROPERTIESFORM
				|| componentType == WCFComponentType.SORTFORM)
			xslUriFile = new File(PluginUtils.getPluginDir(),
					"ui/resources/wcf/wcf.xsl");
		String xslUri = xslUriFile.getAbsolutePath();
		return FileUtils.applyUnixFileSeperator(xslUri);
	}
}
