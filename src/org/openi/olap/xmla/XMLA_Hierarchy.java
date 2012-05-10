package org.openi.olap.xmla;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.mdxparse.CompoundId;
import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.ExpVisitor;
import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.Visitor;
import com.tonbeller.jpivot.olap.query.MDXElement;
import com.tonbeller.jpivot.util.StringUtil;

/**
 * Hierarchy for XMLA
 * @author SUJEN
 * 
 */
public class XMLA_Hierarchy implements Hierarchy, Exp, MDXElement {

	static Logger logger = Logger.getLogger(XMLA_Hierarchy.class);

	private String dimUniqueName;
	private String uniqueName;
	private String caption;
	private int dimType;
	private int cardinality;
	private String defaultMember;
	private String allMember;
	private XMLA_Model model;

	// could not find documentation on the following "defines"
	// probably NOT supported by SAP
	public static final int STRUCTURE_FULLYBALANCED = 0;
	public static final int STRUCTURE_RAGGEDBALANCED = 1; // ??
	public static final int STRUCTURE_UNBALANCED = 2; // ?? example:
														// Foodmart.HR.Employee
	private int structure = 0;

	private boolean isVirtual;
	private boolean isReadWrite;
	private int dimUniqueSettings;
	private boolean isDimVisible;
	private int ordinal;
	private boolean isDimShared;

	private boolean isMembersGotten = false;

	private Dimension dimension = null;

	private ArrayList aLevels = new ArrayList();

	public XMLA_Hierarchy(XMLA_Model model) {
		this.model = model;
	}

	/**
	 * Returns the allMember.
	 * 
	 * @return String
	 */
	public String getAllMemberName() {
		return allMember;
	}

	/**
	 * Returns the allMember.
	 * 
	 * @return Member
	 */
	public Member getAllMember() {
		if (allMember == null)
			return null;
		Member mAll = model.lookupMemberByUName(allMember);
		if (mAll != null)
			return mAll;
		try {
			// the all member was not retrieved yet
			model.retrieveMember(allMember);
		} catch (OlapException e) {
			// should not occur
			logger.error("could not retrieve member " + allMember, e);
		}
		return model.lookupMemberByUName(allMember);
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
	 * Returns the defaultMember.
	 * 
	 * @return String
	 */
	public String getDefaultMember() {
		return defaultMember;
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
	 * Returns the dimUniqueSettings.
	 * 
	 * @return int
	 */
	public int getDimUniqueSettings() {
		return dimUniqueSettings;
	}

	/**
	 * Returns the isDimShared.
	 * 
	 * @return boolean
	 */
	public boolean isDimShared() {
		return isDimShared;
	}

	/**
	 * Returns the isDimVisible.
	 * 
	 * @return boolean
	 */
	public boolean isDimVisible() {
		return isDimVisible;
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
	 * Returns the ordinal.
	 * 
	 * @return int
	 */
	public int getOrdinal() {
		return ordinal;
	}

	/**
	 * Returns the structure.
	 * 
	 * @return int
	 */
	public int getStructure() {
		return structure;
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
	 * Sets the allMember.
	 * 
	 * @param allMember
	 *            The allMember to set
	 */
	public void setAllMember(String allMember) {
		this.allMember = allMember;
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
	 * Sets the defaultMember.
	 * 
	 * @param defaultMember
	 *            The defaultMember to set
	 */
	public void setDefaultMember(String defaultMember) {
		this.defaultMember = defaultMember;
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
	 * Sets the dimUniqueSettings.
	 * 
	 * @param dimUniqueSettings
	 *            The dimUniqueSettings to set
	 */
	public void setDimUniqueSettings(int dimUniqueSettings) {
		this.dimUniqueSettings = dimUniqueSettings;
	}

	/**
	 * Sets the isDimShared.
	 * 
	 * @param isDimShared
	 *            The isDimShared to set
	 */
	public void setDimShared(boolean isDimShared) {
		this.isDimShared = isDimShared;
	}

	/**
	 * Sets the isDimVisible.
	 * 
	 * @param isDimVisible
	 *            The isDimVisible to set
	 */
	public void setDimVisible(boolean isDimVisible) {
		this.isDimVisible = isDimVisible;
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
	 * Sets the ordinal.
	 * 
	 * @param ordinal
	 *            The ordinal to set
	 */
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	/**
	 * Sets the structure.
	 * 
	 * @param structure
	 *            The structure to set
	 */
	public void setStructure(int structure) {
		this.structure = structure;
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
	 * @see com.tonbeller.jpivot.olap.model.Hierarchy#getDimension()
	 */
	public Dimension getDimension() {
		return dimension;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Hierarchy#getLevels()
	 */
	public Level[] getLevels() {
		return (Level[]) aLevels.toArray(new XMLA_Level[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Displayable#getLabel()
	 */
	public String getLabel() {
		return caption;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Visitable#accept(Visitor)
	 */
	public void accept(Visitor visitor) {
		visitor.visitHierarchy(this);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Decorator#getRootDecoree()
	 */
	public Object getRootDecoree() {
		return this;
	}

	/**
	 * Sets the dimension.
	 * 
	 * @param dimension
	 *            The dimension to set
	 */
	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}

	/**
	 * add level for this hierarchy
	 * 
	 * @param lev
	 */
	public void addLevel(Level lev) {
		aLevels.add(lev);
	}

	/**
	 * 
	 * @param other
	 * @return boolean
	 */
	public boolean isEqual(XMLA_Hierarchy other) {
		return (this.getUniqueName().equals(other.getUniqueName()));
	}

	/**
	 * Returns the isMembersGotten.
	 * 
	 * @return boolean
	 */
	protected boolean isMembersGotten() {
		return isMembersGotten;
	}

	/**
	 * Sets the isMembersGotten.
	 * 
	 * @param isMembersGotten
	 *            The isMembersGotten to set
	 */
	protected void setMembersGotten(boolean isMembersGotten) {
		this.isMembersGotten = isMembersGotten;
	}

	/**
	 * @return the unique name
	 */
	public String toMdx() {
		return this.uniqueName;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#clone() probably not needed
	 *      any more
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
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitHierarchy(this);
	}

	public boolean hasAll() {
		return allMember != null;
	}
} // End XMLA_Hierarchy
