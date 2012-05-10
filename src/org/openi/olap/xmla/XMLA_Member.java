package org.openi.olap.xmla;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.mdxparse.CompoundId;
import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.ExpVisitor;
import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.Property;
import com.tonbeller.jpivot.olap.model.Visitor;
import com.tonbeller.jpivot.olap.query.MDXMember;
import com.tonbeller.jpivot.util.StringUtil;

/**
 * Member Implementation for XMLA
 * @author SUJEN
 */
public class XMLA_Member implements Member, MDXMember, Exp {

	static Logger logger = Logger.getLogger(XMLA_Member.class);

	private XMLA_Model model;

	private long ordinal;
	private String name;
	private int type;
	private String caption;
	private long childrenCardinality = -1;
	private long parentLevel;
	private String uniqueName;
	private String parentUniqueName = null;
	private String key;
	private boolean isPlaceHolderMember;
	private boolean isDataMember;
	private long displayInfo = -1;
	private String dimUName = null;
	// dimension unique name for calculated members

	private XMLA_Level level;
	private XMLA_Member parent = null;
	private ArrayList aChildren = null;

	// private Property[] properties = null;
	private List properties = new ArrayList();

	private boolean isCalculated; // for a formula member

	private boolean complete = false; // completely initialized
	private boolean propsOk = false;
	private boolean childrenOk = false;
	private boolean parentOk = false;

	/**
	 * c'tor
	 * 
	 * @param uName
	 * @param caption
	 * @param levUname
	 * @param levelNumber
	 * @param hierUName
	 * @param lev
	 */
	public XMLA_Member(XMLA_Model model, String uName, String caption,
			XMLA_Level lev, boolean isCalculated) {

		if (!(model.lookupMemberByUName(uName) == null)) {
			// error - should never occur
			logger.fatal("cannot create member doubly " + uName);
			throw new IllegalArgumentException("cannot create member doubly "
					+ uName);
		}

		this.model = model;
		this.uniqueName = uName;
		this.caption = caption;
		this.level = lev;
		this.isCalculated = isCalculated;

		logger.debug("<init>: uName - " + uName + ", level - " + lev
				+ ", isCalculated - " + isCalculated);

		model.addMember(this);

		if (model.isMicrosoft() || isCalculated) {
			parentUniqueName = StringUtil.parentFromUName(uName);
			if (parentUniqueName == null) {
				parent = null;
				parentOk = true;
				logger.debug("parentUniqueName from uName: " + uName
						+ " == null");
			} else {
				parent = (XMLA_Member) model
						.lookupMemberByUName(parentUniqueName);
				logger.debug("lookupMemberByUName(" + parentUniqueName + "): "
						+ parent);
				if (parent != null)
					parentOk = true;
			}
		}

		// SAP does not return a level name for a calculated measure
		// another bug?
		// we need (?) a level, so try to get it
		// a normal member (not calculated) should always have a level
		if (level == null && isCalculated) {
			// get the dimension from the unique name
			String dimUname = StringUtil.dimFromUName(uniqueName);
			XMLA_Dimension dim = model.lookupDimByUName(dimUname);
			logger.debug("looked up dimension name: " + uniqueName + " = "
					+ dim);
			if (dim != null) {
				if (dim.isMeasure()) {
					// assign measures level
					Hierarchy[] hiers = dim.getHierarchies();
					Level[] levs = hiers[0].getLevels();
					level = (XMLA_Level) levs[0];
					logger.debug("isMeasure: " + level);
				} else {
					// normal dimension
					// if it is NOT SAP (no parent supported) and
					// if the unique name contains a parent - get the level from
					// parent
					XMLA_Member pm = null;
					if (model.isMicrosoft() && parentUniqueName != null) {
						pm = (XMLA_Member) model
								.lookupMemberByUName(parentUniqueName);
						if (pm != null)
							level = ((XMLA_Level) pm.getLevel())
									.getChildLevel();
						logger.debug("normal dimension: " + level);
					}
					if (level == null) {
						// don't know how to find level
						// try default hierarchy - top level
						Hierarchy hier = null;
						if (pm != null) {
							hier = pm.getHierarchy();
							logger.debug("hierarchy from member: " + hier);
						} else {
							String hierUname = dim.getDefaultHier();
							hier = model.lookupHierByUName(hierUname);
							logger.debug("hierarchy from DefaultHier: " + hier);
						}
						if (hier != null) {
							logger.debug("trying default hierarchy: " + hier);
							// take the level with number 0
							Level[] levs = hier.getLevels();
							for (int i = 0; i < levs.length; i++) {
								if (((XMLA_Level) levs[i]).getDepth() == 0) {
									level = (XMLA_Level) levs[i];
									break;
								}
							} // for levs
						}
					}
				} // else
			} // if dim != null
		} // if level == null

		// won't survive without a level
		// better crash here than later
		if (level == null)
			throw new IllegalArgumentException("Member " + uName
					+ " Level=null");

		if (level.getChildLevel() == null || isCalculated) {
			childrenCardinality = 0;
			childrenOk = true;
		}
	}

	/**
	 * 
	 * @param other
	 * @return boolean
	 */
	public boolean isEqual(Member otherM) {
		XMLA_Member other = (XMLA_Member) otherM;
		return this.uniqueName.equals(other.uniqueName);
	}

	/**
	 * determine, whether this member is descendant of other member
	 * 
	 * @param other
	 * @return boolean
	 */
	public boolean isChildOf(Member otherM) throws OlapException {
		XMLA_Member other = (XMLA_Member) otherM;
		// if not the same hierarchy, say no
		XMLA_Hierarchy thisHier = (XMLA_Hierarchy) level.getHierarchy();
		XMLA_Hierarchy otherHier = (XMLA_Hierarchy) other.getLevel()
				.getHierarchy();
		if (!thisHier.isEqual(otherHier))
			return false;
		// cannot be a child, if the level is not higher
		long otherLevelNumber = ((XMLA_Level) other.getLevel()).getDepth();
		if (!(level.getDepth() > otherLevelNumber))
			return false;

		// go up parent chain
		XMLA_Member m = this;
		while (m.level.getDepth() > otherLevelNumber) {
			m = (XMLA_Member) m.getParent();
		}

		if (m.isEqual(other))
			return true;

		return false;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Visitable#accept(Visitor)
	 */
	public void accept(Visitor visitor) {
		visitor.visitMember(this);
	}

	public Object getRootDecoree() {
		return this;
	}

	/**
	 * Returns the caption as Label.
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
	 * Returns the childrenCardinality.
	 * 
	 * @return int
	 */
	public long getChildrenCardinality() {
		return childrenCardinality;
	}

	/**
	 * Returns the isDataMember.
	 * 
	 * @return boolean
	 */
	public boolean isDataMember() {
		return isDataMember;
	}

	/**
	 * Returns the isPlaceHolderMember.
	 * 
	 * @return boolean
	 */
	public boolean isPlaceHolderMember() {
		return isPlaceHolderMember;
	}

	/**
	 * Returns the key.
	 * 
	 * @return String
	 */
	public String getKey() {
		return key;
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
	public long getOrdinal() {
		return ordinal;
	}

	/**
	 * Returns the parentLevel.
	 * 
	 * @return int
	 */
	public long getParentLevel() {
		return parentLevel;
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
	 * set all attributes, which were not set in the c'tor
	 * 
	 * @param name
	 * @param type
	 * @param ordinal
	 * @param parentUniqueName
	 * @param childrenCardinality
	 * @param parentLevel
	 * @param isDataMember
	 * @param isPlaceHolderMember
	 * @param key
	 */
	public void complete(String name, int type, long ordinal,
			String parentUniqueName, long childrenCardinality,
			long parentLevel, boolean isDataMember,
			boolean isPlaceHolderMember, String key) {
		this.childrenCardinality = childrenCardinality;
		this.isDataMember = isDataMember;
		this.isPlaceHolderMember = isPlaceHolderMember;
		this.key = key;
		this.name = name;
		this.ordinal = ordinal;
		this.parentLevel = parentLevel;
		this.type = type;
		this.parentUniqueName = parentUniqueName;
		complete = true;
	}

	/**
	 * Returns the parentUniqueName.
	 * 
	 * @return String
	 */
	public String getParentUniqueName() {
		return parentUniqueName;
	}

	/**
	 * Returns the level.
	 * 
	 * @return XMLA_Level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * set the Level
	 */
	public void setLevel(XMLA_Level level) {
		this.level = level;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.PropertyHolder#getProperties()
	 */
	public Property[] getProperties() {

		/*
		 * if (!propsOk) { propsOk = true; // if this member's level does not
		 * have properties at all if (isCalculated || level.getProps().size() ==
		 * 0) { properties.clear(); } else {
		 * 
		 * try { model.completeMember(this); } catch (OlapException e) {
		 * logger.error("?", e); } } }
		 */
		if (isCalculated || properties.size() == 0)
			return new Property[0]; // or null ???

		return (Property[]) properties.toArray(new Property[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.PropertyHolder#getProperty(String)
	 */
	public Property getProperty(String name) {

		/*
		 * if (!propsOk) { propsOk = true; // if this member's level does not
		 * have properties at all if (isCalculated || level.getProps().size() ==
		 * 0) {
		 * 
		 * properties.clear(); } else {
		 * 
		 * try { model.completeMember(this); } catch (OlapException e) {
		 * logger.error("?", e); } } }
		 */

		if (isCalculated || properties.size() == 0)
			return null;

		for (int i = 0; i < properties.size(); i++) {
			Property prop = (Property) properties.get(i);
			if (name.equals(prop.getName()))
				return prop;
		}

		return null; // not found
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Member#getRootDistance()
	 */
	public int getRootDistance() {
		if (level == null && isCalculated)
			return 0;
		return level.getDepth();
	}

	/**
	 * get level number = depth of member
	 * 
	 * @return int
	 */
	public int getDepth() {
		if (level == null && isCalculated)
			return 0;
		return this.level.getDepth(); // level number is depth
	}

	/**
	 * 
	 * @return XMLA_Member[]
	 */
	public XMLA_Member[] getChildren() throws OlapException {
		if (childrenOk) {
			if (childrenCardinality == 0)
				return new XMLA_Member[0];
			else
				return (XMLA_Member[]) aChildren.toArray(new XMLA_Member[0]);
		}

		// determine children
		model.retrieveMemberChildren(this); // will retrieve children

		return (XMLA_Member[]) aChildren.toArray(new XMLA_Member[0]);
	}

	/**
	 * set list of children.
	 * 
	 * @param aNewChildren
	 *            List of children to be added
	 */
	public void setChildren(ArrayList aChildren) {
		this.aChildren = aChildren;
		if (aChildren == null)
			childrenCardinality = 0;
		else
			childrenCardinality = aChildren.size();
	}

	/**
	 * Returns the complete.
	 * 
	 * @return boolean
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Returns the parent.
	 * 
	 * @return XMLA_Member
	 */
	public Member getParent() throws OlapException {

		if (parentOk)
			return parent;

		/*
		 * if (isCalculated) { //get the parent by unique name String[] names =
		 * uniqueName.split("\\."); if (names.length <= 2) { // no parent in
		 * unique name parentOk = true; parent = null; return null; }
		 * StringBuffer sb = new StringBuffer(); for (int i = 0; i <
		 * names.length - 1; i++) { if (i > 0) sb.append('.');
		 * sb.append(names[i]); } parentUniqueName = sb.toString(); parent =
		 * model.lookupMemberByUName(parentUniqueName); parentOk = true; return
		 * parent; }
		 */

		if (model.isMicrosoft() || isCalculated) {
			parentUniqueName = StringUtil.parentFromUName(uniqueName);
			if (parentUniqueName == null) {
				parent = null;
				parentOk = true;
				return parent;
			} else {
				parent = (XMLA_Member) model
						.lookupMemberByUName(parentUniqueName);
				if (parent != null) {
					parentOk = true;
					return parent;
				}
			}
		}

		// this call should not be needed
		model.retrieveMemberParent(this);
		return parent;
	}

	/**
	 * get Dimension of member
	 * 
	 * @return dimension
	 */
	public Dimension getDimension() {
		if (level != null)
			return level.getHierarchy().getDimension();
		Dimension dim = model.lookupDimByUName(dimUName);
		return dim;
	}

	/**
	 * Sets the parent.
	 * 
	 * @param parent
	 *            The parent to set
	 */
	public void setParent(XMLA_Member parent) {
		this.parent = parent;
		this.parentOk = true;
	}

	/**
	 * get hierarchy of member
	 * 
	 * @return Hierarchy
	 */
	public Hierarchy getHierarchy() {
		if (level != null)
			return level.getHierarchy();
		XMLA_Dimension dim = (XMLA_Dimension) getDimension();
		String defHier = dim.getDefaultHier();
		if (defHier != null && defHier.length() > 0)
			return model.lookupHierByUName(defHier);
		else
			return null;
	}

	/**
	 * Returns the isCalculated.
	 * 
	 * @return boolean
	 */
	public boolean isCalculated() {
		return isCalculated;
	}

	/**
	 * @return the unique name
	 */
	public String toMdx() {
		return this.uniqueName;
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
	 * add a property
	 */
	public void addProp(Property prop) {
		properties.add(prop);
	}

	/**
	 * clear properties
	 */
	public void clearProps() {
		properties.clear();
	}

	/**
	 * @return
	 */
	public boolean isPropsOk() {
		return propsOk;
	}

	/**
	 * @param b
	 */
	public void setPropsOk(boolean b) {
		propsOk = b;
	}

	/**
	 * @return
	 */
	public boolean isChildrenOk() {
		return childrenOk;
	}

	/**
	 * @return
	 */
	public boolean isParentOk() {
		return parentOk;
	}

	/**
	 * @param b
	 */
	public void setChildrenOk(boolean b) {
		childrenOk = b;
	}

	/**
	 * @param b
	 */
	public void setParentOk(boolean b) {
		parentOk = b;
	}

	/**
	 * @return true , if the model is SAP
	 */
	public boolean isSAP() {
		return model.isSAP();
	}

	public boolean isMicrosoft() {
		return model.isMicrosoft();
	}

	public boolean isMondrian() {
		return model.isMondrian();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitMember(this);
	}

	/**
	 * @return displayInfo
	 */
	public long getDisplayInfo() {
		return displayInfo;
	}

	/**
	 * @param displayInfo
	 *            string
	 */
	public void setDisplayInfo(String strDisplayInfo) {
		if (strDisplayInfo == null || displayInfo != -1)
			return;
		displayInfo = Long.parseLong(strDisplayInfo);
		// the two least significant bytes is the children cardinality
		if (childrenCardinality == -1)
			childrenCardinality = Math.abs(displayInfo) % 65536;
	}

	/**
	 * @return true, if it is an "All" member
	 */
	public boolean isAll() {
		return ((XMLA_Level) this.getLevel()).isAll();
	}

} // End XMLA_Member
