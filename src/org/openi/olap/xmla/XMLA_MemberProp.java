package org.openi.olap.xmla;

import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Level;

/**
 * a member property in xmla
 * @author SUJEN
 */
public class XMLA_MemberProp {

	private String caption;
	private Level level;
	private Dimension dimension;
	private String name;
	private String xmlTag;

	private boolean sap = false;

	/**
	 * C'tor NOT SAP
	 */
	public XMLA_MemberProp(String name, String caption, Level level) {
		this.name = name;
		this.caption = caption;
		this.level = level;
		this.sap = false;
		this.dimension = level.getHierarchy().getDimension();
		// replace special characters
		xmlTag = escapeSpecialChars(name);

	}

	/**
	 * C'tor SAP
	 */
	public XMLA_MemberProp(String name, String caption, Dimension dimension) {
		this.name = name;
		this.caption = caption;
		this.level = null;
		this.sap = true;
		this.dimension = dimension;
		// replace special characters (Microsoft)
		xmlTag = escapeSpecialChars(name);

		// SAP: replace enclosing brackets
		int len = xmlTag.length();
		if (xmlTag.charAt(0) == '[' && xmlTag.charAt(len - 1) == ']')
			xmlTag = xmlTag.substring(1, len - 1); // remove brackets
		// SAP XML tag always starts with "_"
		if (xmlTag.charAt(0) != '_')
			xmlTag = "_" + xmlTag;
	}

	/**
	 * @return
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getXmlTag() {
		return xmlTag;
	}

	/**
	 * escape special characters
	 */
	static final char[] special = { ' ', '<', '>' };

	private String escapeSpecialChars(String str) {

		StringBuffer sb = new StringBuffer();
		Outer: for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			for (int j = 0; j < special.length; j++) {
				if (c == special[j]) {
					sb.append("_x");
					String x = Integer.toHexString(c);
					int k = 4 - x.length();
					/*
					 * if (k > 0) sb.append("0000".substring(0, k));
					 */
					for (int m = 0; m < k; m++)
						sb.append('0');
					sb.append(x);
					sb.append("_");
					continue Outer;
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * @return
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @return
	 */
	public Dimension getDimension() {
		return dimension;
	}

} // XMLA_MemberProp
