package org.openi.olap.xmla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.tonbeller.jpivot.olap.mdxparse.CompoundId;
import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.ExpVisitor;
import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Visitor;
import com.tonbeller.jpivot.olap.query.MDXElement;

/**
 * XMLA Dimension
 * 
 * @author SUJEN
 * 
 */
public class XMLA_Dimension implements Dimension, Exp, MDXElement {

	private String name;
	private String uniqueName;
	private String caption;
	private int ordinal;
	private int type;
	private int cardinality;
	private String defaultHier;
	private boolean isVirtual;
	private boolean isReadWrite;
	private int uniqueSettings;
	private boolean isVisible;
	private String description;

	private ArrayList aHierarchies = new ArrayList();

	private Map props = new HashMap(); // member properties , SAP

	// dimension types (not documented?)
	public static final int MD_DIMTYPE_TIME = 1;
	public static final int MD_DIMTYPE_MEASURE = 2;
	public static final int MD_DIMTYPE_OTHER = 3;

	/**
	 * @see com.tonbeller.jpivot.olap.model.Dimension#isMeasure()
	 */
	public boolean isMeasure() {
		return type == MD_DIMTYPE_MEASURE;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Dimension#isTime()
	 */
	public boolean isTime() {
		return type == MD_DIMTYPE_TIME;
	}

	public String getLabel() {
		return caption;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Dimension#getHierarchies()
	 */
	public Hierarchy[] getHierarchies() {
		return (Hierarchy[]) aHierarchies.toArray(new XMLA_Hierarchy[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Visitable#accept(Visitor)
	 */
	public void accept(Visitor visitor) {
		visitor.visitDimension(this);
	}

	public Object getRootDecoree() {
		return this;
	}

	/**
	 * Returns the caption.
	 * 
	 * @return String
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * Returns the cardinality.
	 * 
	 * @return int
	 */
	public int getCardinality() {
		return cardinality;
	}

	/**
	 * Returns the defaultHier.
	 * 
	 * @return String
	 */
	public String getDefaultHier() {
		return defaultHier;
	}

	/**
	 * Returns the isReadWrite.
	 * 
	 * @return boolean
	 */
	public boolean isReadWrite() {
		return isReadWrite;
	}

	/**
	 * Returns the isVirtual.
	 * 
	 * @return boolean
	 */
	public boolean isVirtual() {
		return isVirtual;
	}

	/**
	 * Returns the isVisible.
	 * 
	 * @return boolean
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the ordinal.
	 * 
	 * @return int
	 */
	public int getOrdinal() {
		return ordinal;
	}

	/**
	 * Returns the type.
	 * 
	 * @return int
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the uniqueName.
	 * 
	 * @return String
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * Returns the uniqueSettings.
	 * 
	 * @return int
	 */
	public int getUniqueSettings() {
		return uniqueSettings;
	}

	/**
	 * Sets the caption.
	 * 
	 * @param caption
	 *            The caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * Sets the cardinality.
	 * 
	 * @param cardinality
	 *            The cardinality to set
	 */
	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * Sets the defaultHier.
	 * 
	 * @param defaultHier
	 *            The defaultHier to set
	 */
	public void setDefaultHier(String defaultHier) {
		this.defaultHier = defaultHier;
	}

	/**
	 * Sets the isReadWrite.
	 * 
	 * @param isReadWrite
	 *            The isReadWrite to set
	 */
	public void setReadWrite(boolean isReadWrite) {
		this.isReadWrite = isReadWrite;
	}

	/**
	 * Sets the isVirtual.
	 * 
	 * @param isVirtual
	 *            The isVirtual to set
	 */
	public void setVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}

	/**
	 * Sets the isVisible.
	 * 
	 * @param isVisible
	 *            The isVisible to set
	 */
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the ordinal.
	 * 
	 * @param ordinal
	 *            The ordinal to set
	 */
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            The type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Sets the uniqueName.
	 * 
	 * @param uniqueName
	 *            The uniqueName to set
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	/**
	 * Sets the uniqueSettings.
	 * 
	 * @param uniqueSettings
	 *            The uniqueSettings to set
	 */
	public void setUniqueSettings(int uniqueSettings) {
		this.uniqueSettings = uniqueSettings;
	}

	/**
	 * add hierarchy for this dimension
	 * 
	 * @param hier
	 */
	public void addHier(Hierarchy hier) {
		aHierarchies.add(hier);
	}

	/**
	 * @return the unique name
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#toMdx()
	 */
	public String toMdx() {
		return uniqueName;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#clone() probably not needed
	 */
	public Object clone() {
		String str = uniqueName.substring(1, uniqueName.length() - 1);
		String[] nameParts = new String[] { str };

		CompoundId clone = new CompoundId(nameParts[0], false);
		return clone;
	}

	/**
	 * @return
	 */
	public Map getProps() {
		return props;
	}

	/**
	 * add a property
	 */
	public void addProp(XMLA_MemberProp prop) {
		if (!props.containsKey(prop.getXmlTag())) {
			props.put(prop.getXmlTag(), prop);
		}
	}

	/**
	 * retrieve a property
	 */
	public XMLA_MemberProp getProp(String xmlTag) {
		return (XMLA_MemberProp) props.get(xmlTag);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitDimension(this);
	}

} // XMLA_Dimension
