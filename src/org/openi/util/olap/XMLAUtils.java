package org.openi.util.olap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openi.datasource.Datasource;
import org.openi.datasource.XMLADatasource;

import com.tonbeller.jpivot.core.Extension;
import com.tonbeller.jpivot.core.Model;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapItem;
import com.tonbeller.jpivot.olap.model.OlapModel;

import org.openi.olap.xmla.ModelFactory;
import org.openi.olap.xmla.XMLA_ChangeSlicer;
import org.openi.olap.xmla.XMLA_MdxQuery;
import org.openi.olap.xmla.XMLA_MemberTree;
import org.openi.olap.xmla.XMLA_Model;
import org.openi.olap.xmla.XMLA_SOAP;
import org.openi.olap.xmla.XMLA_SortRank;
import org.openi.olap.xmla.XMLA_SwapAxes;
import org.openi.olap.xmla.XmlaQueryTag;
import org.xml.sax.SAXException;

public class XMLAUtils {

	private static final Log logger = LogFactory.getLog(XMLAUtils.class);

	@SuppressWarnings("unchecked")
	public static List getCubesList(XMLADatasource datasource)
			throws OlapException, UnsupportedEncodingException {
		/*
		 * XMLA_SOAP xmlaSOAP = new XMLA_SOAP(datasource.getServerURL(),
		 * URLEncoder.encode(datasource.getUsername(), "UTF-8"),
		 * URLEncoder.encode(datasource.getPassword(), "UTF-8"),
		 * datasource.getDatasourceName());
		 */
		/*
		 * XMLA_SOAP xmlaSOAP = new XMLA_SOAP(datasource.getServerURL(),
		 * URLEncoder.encode(datasource.getUsername(), "UTF-8"),
		 * URLEncoder.encode(datasource.getPassword(), "UTF-8"),
		 * datasource.getDatasourceName());
		 */
		XMLA_SOAP xmlaSOAP = new XMLA_SOAP(datasource.getServerURL(),
				URLEncoder.encode(datasource.getUsername(), "UTF-8"),
				URLEncoder.encode(datasource.getPassword(), "UTF-8"));
		List cubeList = getCubesWithoutPerspectives(datasource.getCatalog(),
				xmlaSOAP);
		return cubeList;
	}

	/**
	 * SSAS 2005 uses concept of Perspectives
	 * (http://msdn2.microsoft.com/en-us/library/ms167223.aspx) for a cube.
	 * Discover Cube SOAP request retrieves perspectives along with cubes. Since
	 * dimensions used in a cube may not available in a perspective for the
	 * cube, when a perspective is selected from the list of cubes in new
	 * analysis, some members/levels are not found and exception is thrown. This
	 * method filter perspectives.
	 * 
	 * @param catalog
	 *            String
	 * @param olap
	 *            XMLA_SOAP
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	private static List getCubesWithoutPerspectives(String catalog,
			XMLA_SOAP xmlaSOAP) throws OlapException {

		List cubesWithoutPerspectives = null;

		if (xmlaSOAP != null) {
			Iterator iterator = xmlaSOAP.discoverCube(catalog).iterator();
			cubesWithoutPerspectives = new ArrayList();

			while (iterator.hasNext()) {
				OlapItem item = (OlapItem) iterator.next();

				// perspective has BASE_CUBE_NAME property
				if (item.getProperty("BASE_CUBE_NAME") != null
						&& !"".equalsIgnoreCase(item
								.getProperty("BASE_CUBE_NAME"))) {
					continue;
				}

				cubesWithoutPerspectives.add(item.getName());
			}
		}

		return cubesWithoutPerspectives;
	}

	/**
	 * Generates an MDX statement from the cube param, and the dimension param.
	 * sample result: SELECT {[Measures].DefaultMember} on columns,
	 * {[Category].Children} on rows FROM [Budget]
	 * 
	 * If your dimension has multiple hierarchies (SSAS 2005), we use first
	 * discovered hierarchy: SELECT {[Measures].DefaultMember} on columns,
	 * {[Product Class].[Product Category].Children
	 * 
	 * And a variation is if you want to pass in a specific list of measures,
	 * the result would look like this: SELECT {[Measures].[Gross Weight],
	 * [Measures].[Net Weight]} on columns, {[Product Class].[Product
	 * Category].Children} on rows FROM [Foodmart 2005]
	 * 
	 * @param datasource
	 * @param cube
	 * @param dimension
	 * @param measures
	 *            if you want the mdx to use a specific list of measures use
	 *            this param otherwise, you will get [Measures].DefaultMember
	 * @return
	 * @throws OlapException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public static String createDefaultMdx(XMLADatasource datasource,
			String cube, String dimension, List measures) throws OlapException,
			UnsupportedEncodingException {
		String firstHier = "";
		// String mdxQuery = "";
		String columnsClause = "";
		String rowsClause = "";
		String fromClause = "";

		// build the columnsClause (measures)
		if (measures != null && measures.size() > 0) {
			Iterator itMeas = measures.iterator();
			columnsClause = "{";
			while (itMeas.hasNext()) {
				columnsClause += "[Measures].[" + (String) itMeas.next() + "]";

				if (itMeas.hasNext()) {
					columnsClause += ", ";
				}
			}

			columnsClause += "}";
		} else {
			columnsClause = "{[Measures].DefaultMember}";
		}

		// build the columns (dimensions) clause
		// make sure the dimension input is not the measures
		if ("Measures".equalsIgnoreCase(dimension)) {
			throw new OlapException(
					"Cannot autocreate MDX, dimension passed in is the measures");
		}

		List hiers = discoverHier(datasource, cube, dimension);
		// if there are any hierarchies in this dimension, get the first one
		if ((hiers != null) && (hiers.size() > 1)) {
			firstHier = (String) hiers.get(0);

			if (firstHier.startsWith("[") && firstHier.endsWith("]")) {
				firstHier = "." + firstHier;
			} else {
				firstHier = ".[" + firstHier + "]";
			}
		}

		rowsClause = "{[" + dimension + "]" + firstHier + ".Children}";
		/*
		 * mdxQuery = "SELECT {[Measures].DefaultMember} on columns, {[" +
		 * dimension + "]" + firstHier + ".Children} on rows FROM [" + cube +
		 * "]"; logger.debug("MDX generated as :" + mdxQuery);
		 */

		// build from clause
		fromClause = "[" + cube + "]";

		String mdxQuery = "SELECT " + columnsClause + " on columns, "
				+ rowsClause + " on rows FROM " + fromClause;

		logger.debug("MDX generated as: " + mdxQuery);
		return mdxQuery;
	}

	@SuppressWarnings("unchecked")
	public static List getDimensionList(XMLADatasource datasource,
			String cubeName) throws OlapException, UnsupportedEncodingException {
		List dimensions = new ArrayList();
		logger.info("get dimensions list from " + datasource.getServerURL()
				+ " for cube " + cubeName);
		/*XMLA_SOAP soap = new XMLA_SOAP(datasource.getServerURL(),
				URLEncoder.encode(datasource.getUsername(), "UTF-8"),
				URLEncoder.encode(datasource.getPassword(), "UTF-8"),
				datasource.getDatasourceName());*/
		
		XMLA_SOAP soap = new XMLA_SOAP(datasource.getServerURL(),
				URLEncoder.encode(datasource.getUsername(), "UTF-8"),
				URLEncoder.encode(datasource.getPassword(), "UTF-8"));
		Iterator olapItems = soap
				.discoverDim(datasource.getCatalog(), cubeName).iterator();
		while (olapItems.hasNext()) {
			OlapItem item = (OlapItem) olapItems.next();
			dimensions.add(item.getName());
		}

		return dimensions;
	}

	public static List<String> discoverHier(XMLADatasource datasource,
			String cube, String dimension) throws OlapException,
			UnsupportedEncodingException {
		/*
		 * XMLA_SOAP soap = new XMLA_SOAP(datasource.getServerURL(),
		 * URLEncoder.encode(datasource.getUsername(), "UTF-8"),
		 * URLEncoder.encode(datasource.getPassword(), "UTF-8"),
		 * datasource.getDatasourceName());
		 */

		XMLA_SOAP soap = new XMLA_SOAP(datasource.getServerURL(),
				URLEncoder.encode(datasource.getUsername(), "UTF-8"),
				URLEncoder.encode(datasource.getPassword(), "UTF-8"));

		List hierarchies = new LinkedList();
		Iterator hiers = soap.discoverHier(datasource.getCatalog(), cube,
				"[" + dimension + "]").iterator();

		while ((hiers != null) && hiers.hasNext()) {
			OlapItem currentHierarchy = (OlapItem) hiers.next();
			String parsedName = currentHierarchy.getName()
					.replaceAll("\\[", "");
			parsedName = parsedName.replaceAll("\\]", "");
			hierarchies.add(parsedName);
		}
		return hierarchies;
	}

	@SuppressWarnings("unchecked")
	public static List discoverMeasures(XMLADatasource datasource, String cube)
			throws OlapException, UnsupportedEncodingException {
		List measures = new LinkedList();

		/*
		 * XMLA_SOAP xmlaSoap = new XMLA_SOAP(datasource.getServerURL(),
		 * URLEncoder.encode(datasource.getUsername(), "UTF-8"),
		 * URLEncoder.encode(datasource.getPassword(), "UTF-8"),
		 * datasource.getDatasourceName());
		 */

		XMLA_SOAP xmlaSoap = new XMLA_SOAP(datasource.getServerURL(),
				URLEncoder.encode(datasource.getUsername(), "UTF-8"),
				URLEncoder.encode(datasource.getPassword(), "UTF-8"));

		Iterator olapItems = xmlaSoap.discoverMem(datasource.getCatalog(),
				cube, "[Measures]", null, null).iterator();
		while (olapItems.hasNext()) {
			OlapItem item = (OlapItem) olapItems.next();
			measures.add(item.getName());
		}

		return measures;
	}

	/**
	 * 
	 * @param datasource
	 * @param mdxQuery
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws OlapException
	 */
	public static XMLA_Model getOlapModel(XMLADatasource datasource,
			String mdxQuery) throws SAXException, IOException, OlapException {
		URL confUrl = XmlaQueryTag.class.getResource("config.xml");
		XMLA_Model model = (XMLA_Model) ModelFactory.instance(confUrl);
		model.setMdxQuery(mdxQuery);
		model.setUri(datasource.getServerURL());
		model.setCatalog(datasource.getCatalog());
		model.setDataSource(datasource.getDatasourceName());
		model.setUser(URLEncoder.encode(datasource.getUsername(), "UTF-8"));
		model.setPassword(URLEncoder.encode(datasource.getPassword(), "UTF-8"));
		logger.info("test message");
		model.initialize();
		return model;
	}
}
