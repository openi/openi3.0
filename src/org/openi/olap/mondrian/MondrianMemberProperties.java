package org.openi.olap.mondrian;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.MemberPropertyMeta;
import com.tonbeller.jpivot.olap.navi.MemberProperties;

/**
 * get member properties from Mondrian
 */
public class MondrianMemberProperties extends ExtensionSupport implements
		MemberProperties {

	public MondrianMemberProperties() {
		super.setId(MemberProperties.ID);
	}

	/*
	 * get the property definitions for a certain level
	 * 
	 * @see
	 * com.tonbeller.jpivot.olap.navi.MemberProperties#getMemberPropertyMetas
	 * (com.tonbeller.jpivot.olap.model.Level)
	 */
	public MemberPropertyMeta[] getMemberPropertyMetas(Level level) {
		mondrian.olap.Level monLevel = ((MondrianLevel) level).getMonLevel();
		mondrian.olap.Property[] monProps = monLevel.getProperties();
		if (monProps == null || monProps.length == 0)
			return new MemberPropertyMeta[0];

		String scope = getPropertyScope(monLevel);
		MemberPropertyMeta[] props = new MemberPropertyMeta[monProps.length];
		for (int i = 0; i < props.length; i++) {
			String name = monProps[i].getName();
			String label = monProps[i].getCaption();
			if (label == null)
				label = name;
			props[i] = new MemberPropertyMeta(label, name, scope);
		}
		return props;
	}

	/**
	 * returns the unique name of the level (if levelScope) or hierarchy (if
	 * !levelScope)
	 * 
	 * @param monLevel
	 * @return
	 */
	private String getPropertyScope(mondrian.olap.Level monLevel) {
		return monLevel.getHierarchy().getUniqueName();
	}

	/**
	 * @return false
	 */
	public boolean isLevelScope() {
		return false;
	}

	public String getPropertyScope(Member m) {
		MondrianLevel level = (MondrianLevel) m.getLevel();
		return getPropertyScope(level.getMonLevel());
	}

	/**
	 * sets the visible properties. Optimizing implementations of PropertyHolder
	 * may only return these properties.
	 * 
	 * @see com.tonbeller.jpivot.olap.model.PropertyHolder
	 */
	public void setVisibleProperties(MemberPropertyMeta[] props) {
		// ignored
	}

} // End MondrianMemberProperties
