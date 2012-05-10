package org.openi.olap.xmla;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tonbeller.jpivot.olap.mdxparse.CompoundId;
import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.ExpVisitor;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.Visitor;
import com.tonbeller.jpivot.olap.query.MDXLevel;
import com.tonbeller.jpivot.util.StringUtil;

/**
 * Level Implementation for XMLA
 * @author SUJEN
 */
public class XMLA_Level implements Level, MDXLevel, Exp {

	private String dimUniqueName;
	private String hierUniqueName;
	private String name;
	private String uniqueName;
	private String caption;
	private int number;
	private int dimType;
	private int cardinality;
	private int type;
	private int customRollupSettings;
	private int uniqueSettings;
	private boolean isVisible;
	private String orderingProperty;
	private int dbType;
	private String nameSqlColumnName;
	private String keySqlColumnName;
	private String uniqueNameSqlColumnName;

	private XMLA_Level childLevel = null;
	private XMLA_Level parentLevel = null;

	private Hierarchy hierarchy = null;

	private List aMembers = null;
	private XMLA_Model model;

	private Map props = new HashMap(); // member properties , not SAP

	/**
	 * c'tor
	 * 
	 * @param model
	 */
	public XMLA_Level(XMLA_Model model) {
		this.model = model;
	}

	/**
	 * 
	 * @param other
	 * @return boolean
	 */
	public boolean isEqual(XMLA_Level other) {
		return this.uniqueName.equals(other.getUniqueName());
	}

	public void setMembers(List mList) {
		aMembers = mList;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Visitable#accept(Visitor)
	 */
	public void accept(Visitor visitor) {
		visitor.visitLevel(this);
	}

	public Object getRootDecoree() {
		return this;
	}

	/**
	 * Returns the caption as Label
	 * 
	 * @return String
	 */
	public String getLabel() {
		return caption;
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
	 * Returns the customRollupSettings.
	 * 
	 * @return int
	 */
	public int getCustomRollupSettings() {
		return customRollupSettings;
	}

	/**
	 * Returns the dbType.
	 * 
	 * @return int
	 */
	public int getDbType() {
		return dbType;
	}

	/**
	 * Returns the dimType.
	 * 
	 * @return int
	 */
	public int getDimType() {
		return dimType;
	}

	/**
	 * Returns the dimUniqueName.
	 * 
	 * @return String
	 */
	public String getDimUniqueName() {
		return dimUniqueName;
	}

	/**
	 * Returns the hier.
	 * 
	 * @return Hierarchy
	 */
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	/**
	 * Returns the hierUniqueName.
	 * 
	 * @return String
	 */
	public String getHierUniqueName() {
		return hierUniqueName;
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
	 * Returns the keySqlColumnName.
	 * 
	 * @return String
	 */
	public String getKeySqlColumnName() {
		return keySqlColumnName;
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
	 * Returns the nameSqlColumnName.
	 * 
	 * @return String
	 */
	public String getNameSqlColumnName() {
		return nameSqlColumnName;
	}

	/*
	 * return depth ( = 0 for root level)
	 * 
	 * @see com.tonbeller.jpivot.olap.model.MDXLevel#getDepth()
	 */
	public int getDepth() {
		return number;
	}

	/**
	 * Returns the orderingProperty.
	 * 
	 * @return String
	 */
	public String getOrderingProperty() {
		return orderingProperty;
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
	 * Returns the uniqueNameSqlColumnName.
	 * 
	 * @return String
	 */
	public String getUniqueNameSqlColumnName() {
		return uniqueNameSqlColumnName;
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
	 * Sets the customRollupSettings.
	 * 
	 * @param customRollupSettings
	 *            The customRollupSettings to set
	 */
	public void setCustomRollupSettings(int customRollupSettings) {
		this.customRollupSettings = customRollupSettings;
	}

	/**
	 * Sets the dbType.
	 * 
	 * @param dbType
	 *            The dbType to set
	 */
	public void setDbType(int dbType) {
		this.dbType = dbType;
	}

	/**
	 * Sets the dimType.
	 * 
	 * @param dimType
	 *            The dimType to set
	 */
	public void setDimType(int dimType) {
		this.dimType = dimType;
	}

	/**
	 * Sets the dimUniqueName.
	 * 
	 * @param dimUniqueName
	 *            The dimUniqueName to set
	 */
	public void setDimUniqueName(String dimUniqueName) {
		this.dimUniqueName = dimUniqueName;
	}

	/**
	 * Sets the hier.
	 * 
	 * @param hier
	 *            The hier to set
	 */
	public void setHierarchy(Hierarchy hier) {
		this.hierarchy = hier;
	}

	/**
	 * Sets the hierUniqueName.
	 * 
	 * @param hierUniqueName
	 *            The hierUniqueName to set
	 */
	public void setHierUniqueName(String hierUniqueName) {
		this.hierUniqueName = hierUniqueName;
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
	 * Sets the keySqlColumnName.
	 * 
	 * @param keySqlColumnName
	 *            The keySqlColumnName to set
	 */
	public void setKeySqlColumnName(String keySqlColumnName) {
		this.keySqlColumnName = keySqlColumnName;
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
	 * Sets the nameSqlColumnName.
	 * 
	 * @param nameSqlColumnName
	 *            The nameSqlColumnName to set
	 */
	public void setNameSqlColumnName(String nameSqlColumnName) {
		this.nameSqlColumnName = nameSqlColumnName;
	}

	/**
	 * Sets the number.
	 * 
	 * @param number
	 *            The number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Sets the orderingProperty.
	 * 
	 * @param orderingProperty
	 *            The orderingProperty to set
	 */
	public void setOrderingProperty(String orderingProperty) {
		this.orderingProperty = orderingProperty;
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
	 * Sets the uniqueNameSqlColumnName.
	 * 
	 * @param uniqueNameSqlColumnName
	 *            The uniqueNameSqlColumnName to set
	 */
	public void setUniqueNameSqlColumnName(String uniqueNameSqlColumnName) {
		this.uniqueNameSqlColumnName = uniqueNameSqlColumnName;
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
	 * Returns the childLevel.
	 * 
	 * @return XMLA_Level
	 */
	public XMLA_Level getChildLevel() {
		return childLevel;
	}

	/**
	 * Returns the parentLevel.
	 * 
	 * @return XMLA_Level
	 */
	public XMLA_Level getParentLevel() {
		return parentLevel;
	}

	/**
	 * Sets the childLevel.
	 * 
	 * @param childLevel
	 *            The childLevel to set
	 */
	public void setChildLevel(XMLA_Level childLevel) {
		this.childLevel = childLevel;
	}

	/**
	 * Sets the parentLevel.
	 * 
	 * @param parentLevel
	 *            The parentLevel to set
	 */
	public void setParentLevel(XMLA_Level parentLevel) {
		this.parentLevel = parentLevel;
	}

	/**
	 * get the members of this level
	 */
	public XMLA_Member[] getMembers() throws OlapException {
		// potentially killer function
		if (aMembers != null)
			return (XMLA_Member[]) aMembers.toArray(new XMLA_Member[0]);
		model.completeLevel(this); // get the level's members
		return (XMLA_Member[]) aMembers.toArray(new XMLA_Member[0]);
	}

	/**
	 * @return the unique name
	 */
	public String toMdx() {
		return this.uniqueName;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.query.MDXLevel#isAll()
	 * @return true if Hierarchy has "All" member and level is top level
	 */
	public boolean isAll() {
		if (this.getParentLevel() != null)
			return false;
		if (((XMLA_Hierarchy) this.getHierarchy()).getAllMember() != null)
			return true;
		return false;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.query.MDXLevel#hasChildLevel()
	 */
	public boolean hasChildLevel() {
		return (this.getChildLevel() != null);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#clone() probably not needed
	 */
	public Object clone() {
		String[] nameParts = StringUtil.splitUniqueName(uniqueName);
		CompoundId clone = new CompoundId(nameParts[0], false);
		for (int i = 1; i < nameParts.length; i++) {
			clone.append(nameParts[i], false);
		}
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
		visitor.visitLevel(this);
	}

} // End XMLA_Level
