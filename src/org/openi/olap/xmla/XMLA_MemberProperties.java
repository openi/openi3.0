package org.openi.olap.xmla;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.MemberPropertyMeta;
import com.tonbeller.jpivot.olap.navi.MemberProperties;

/**
 * retrieve member properties
 * @author SUJEN
 */
public class XMLA_MemberProperties extends ExtensionSupport implements
		MemberProperties {

	static Logger logger = Logger.getLogger(XMLA_MemberProperties.class);

	private MemberPropertyMeta[] visibleProps;

	public XMLA_MemberProperties() {
		super.setId(MemberProperties.ID);
	}

	/*
	 * get the property definitions for a certain level
	 * 
	 * @see
	 * com.tonbeller.jpivot.olap.navi.MemberProperties#getMemberPropertyMetas
	 */
	public MemberPropertyMeta[] getMemberPropertyMetas(Level level) {
		XMLA_Level xlev = (XMLA_Level) level;
		Map propMap;
		// for SAP the properties are stored with the dimension
		XMLA_Model xmlaModel = (XMLA_Model) this.getModel();

		if (xmlaModel.isSAP() || xmlaModel.isMondrian()) {
			propMap = ((XMLA_Dimension) xlev.getHierarchy().getDimension())
					.getProps();
		} else {
			propMap = xlev.getProps();
		}
		if (propMap.size() == 0)
			return new MemberPropertyMeta[0];
		String scope = getPropertyScope(level);
		MemberPropertyMeta[] props = new MemberPropertyMeta[propMap.size()];
		int i = 0;
		for (Iterator iter = propMap.values().iterator(); iter.hasNext();) {
			XMLA_MemberProp prop = (XMLA_MemberProp) iter.next();
			String name = prop.getName();

			String caption = prop.getCaption();

			if (xmlaModel.isSAP() || xmlaModel.isMondrian())
				props[i++] = new MemberPropertyMeta(caption + " / " + name,
						name, scope);
			else
				props[i++] = new MemberPropertyMeta(caption, name, scope);
		}

		return props;
	}

	/**
	 * @return true if level scope
	 * @see com.tonbeller.jpivot.olap.navi.MemberProperties#isLevelScope()
	 */
	public boolean isLevelScope() {
		return false;
	}

	/**
	 * @return property scope for member
	 * @see com.tonbeller.jpivot.olap.navi.MemberProperties#getPropertyScope
	 */
	public String getPropertyScope(Member m) {
		Level level = m.getLevel();
		return getPropertyScope(level);
	}

	/**
	 * returns the unique name of hierarchy
	 * 
	 * @param level
	 * @return unique name of hierarchy
	 */
	private String getPropertyScope(Level level) {
		return ((XMLA_Dimension) level.getHierarchy().getDimension())
				.getUniqueName();
	}

	/**
	 * sets the visible properties. Optimizing implementations of PropertyHolder
	 * may only return these properties.
	 * 
	 * @see com.tonbeller.jpivot.olap.model.PropertyHolder
	 */
	public void setVisibleProperties(MemberPropertyMeta[] props) {
		this.visibleProps = props;
		((XMLA_Model) getModel()).fireModelChanged();
	}

	/**
	 * sets the visible properties. Optimizing implementations of PropertyHolder
	 * may only return these properties.
	 * 
	 * @see com.tonbeller.jpivot.olap.model.PropertyHolder
	 */
	public MemberPropertyMeta[] getVisibleProperties() {
		return this.visibleProps;
	}

} // XMLA_MemberProperties
