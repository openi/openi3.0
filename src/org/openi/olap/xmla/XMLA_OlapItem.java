package org.openi.olap.xmla;

import java.util.HashMap;
import java.util.Map;

import com.tonbeller.jpivot.olap.model.OlapItem;

/**
 * XMLA Olap Item
 * @author SUJEN
 */
public class XMLA_OlapItem implements OlapItem {

	private int type;
	private Map propMap = new HashMap();
	private String caption = null;
	private String name;
	private String uniqueName;

	/**
	 * c'tor
	 * 
	 * @param type
	 */
	public XMLA_OlapItem(int type) {
		this.type = type;
	}

	/**
	 * @return type of item
	 */
	public int getType() {
		return type;
	}

	/**
	 * Label is the string to be externally displayed
	 * 
	 * @return label
	 */
	public String getLabel() {
		if (caption != null)
			return caption;
		else
			return name;
	}

	/**
	 * @return the unique name
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @return caption (can be null)
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param propName
	 *            name of the property to be retrieved
	 * @return
	 */
	public String getProperty(String propName) {
		return (String) propMap.get(propName);
	}

	/**
	 * any OlapItem contains a map of properties, key and value of type String
	 * 
	 * @return properties property map
	 */
	public Map getProperties() {
		return propMap;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param caption
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @param propName
	 *            property name
	 * @param value
	 *            property value
	 */
	public void setProperty(String propName, String value) {
		propMap.put(propName, value);
	}

	/**
	 * @param string
	 */
	public void setUniqueName(String string) {
		uniqueName = string;
		if (name == null)
			name = string;
	}

} // XMLA_OlapItem
