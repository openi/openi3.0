package org.openi.olap.mondrian;

import mondrian.olap.SchemaReader;

import com.tonbeller.jpivot.olap.model.Alignable;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.Property;
import com.tonbeller.jpivot.olap.model.Visitor;
import com.tonbeller.jpivot.olap.model.impl.PropertyImpl;
import com.tonbeller.jpivot.olap.query.MDXMember;

/**
 * MondrianMember is an adapter class for the Mondrian Member.
 */
public class MondrianMember implements Member, MDXMember {

	private mondrian.olap.Member monMember;
	private MondrianLevel level;
	private MondrianModel model;
	private Property[] properties = null;

	/**
	 * Constructor
	 * 
	 * @param monMember
	 *            corresponding Mondrian Member
	 * @param level
	 *            Olap hierarchy parent object
	 */
	protected MondrianMember(mondrian.olap.Member monMember,
			MondrianLevel level, MondrianModel model) {
		this.monMember = monMember;
		this.level = level;
		this.model = model;
		level.addMember(this);

		mondrian.olap.Property[] props = monMember.getLevel().getProperties();
		if (props != null) {
			properties = new Property[props.length];
			for (int i = 0; i < props.length; i++) {
				MondrianProp prop = new MondrianProp();
				if (props[i].getType() == mondrian.olap.Property.Datatype.TYPE_NUMERIC)
					prop.setAlignment(Alignable.Alignment.RIGHT);
				String propName = props[i].getName();
				prop.setName(propName);
				String caption = props[i].getCaption();
				if (caption != null && !caption.equals(propName)) {
					// name and caption are different
					// we want to show caption instead of name
					prop.setLabel(caption);
					prop.setMondrianName(propName);
					// if the property has a separate Label, then it does not
					// require normalization
					// since it is to be displayed as-is
					prop.setNormalizable(false);
				} else {
					prop.setLabel(propName);
				}
				String propValue = monMember
						.getPropertyFormattedValue(propName);
				prop.setValue(propValue);
				properties[i] = prop;
			}
		}

	}

	public String getLabel() {
		return monMember.getCaption();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Member#getRootDistance()
	 */
	public int getRootDistance() {
		SchemaReader scr = model.getSchemaReader();
		mondrian.olap.Member m = monMember;
		int rootDistance = 0;
		while (true) {
			m = scr.getMemberParent(m);
			if (m == null)
				return rootDistance;
			rootDistance += 1;
		}
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Member#getLevel()
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.PropertyHolder#getProperties()
	 */
	public Property[] getProperties() {

		if (properties == null || properties.length == 0)
			return new Property[0]; // or null ???

		return properties;
	}

	/**
	 * @return parent
	 * @see com.tonbeller.jpivot.olap.query.MDXMember#getParent()
	 */
	public Member getParent() {
		mondrian.olap.Member monParent = monMember.getParentMember();
		MondrianMember parent = model.addMember(monParent);
		return parent;
	}

	/**
	 * @return parent unique name
	 * @see com.tonbeller.jpivot.olap.model.MDXMember#getParentUniqueName()
	 */
	public String getParentUniqueName() {
		return monMember.getParentUniqueName();
	}

	/**
	 * @return true, if it is an "All" member
	 */
	public boolean isAll() {
		return monMember.isAll();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.PropertyHolder#getProperty(String)
	 */
	public Property getProperty(String name) {

		if (properties == null || properties.length == 0)
			return null;

		for (int i = 0; i < properties.length; i++) {
			if (name.equals(properties[i].getName()))
				return properties[i];
		}

		return null; // not found
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Visitable#accept(Visitor)
	 */
	public void accept(Visitor visitor) {
		visitor.visitMember(this);
	}

	/**
	 * Returns the corresponding Mondrian Member.
	 * 
	 * @return mondrian.olap.Member
	 */
	public mondrian.olap.Member getMonMember() {
		return monMember;
	}

	/**
	 * @return the unique name
	 */
	public String getUniqueName() {
		return monMember.getUniqueName();
	}

	/**
	 * @return true,if the member is calculated
	 */
	public boolean isCalculated() {
		return monMember.isCalculated();
	}

	public Object getRootDecoree() {
		return this;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof MondrianMember))
			return false;
		mondrian.olap.Member mm = ((MondrianMember) obj).getMonMember();
		return monMember.equals(mm);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return monMember.hashCode();
	}

	/**
	 * a mondrian property can have a caption different from name we only show
	 * the caption
	 */
	public static class MondrianProp extends PropertyImpl {
		String mondrianName = null; // only set if different from name

		/**
		 * @return Returns the mondrianName.
		 */
		public String getMondrianName() {
			return mondrianName;
		}

		/**
		 * @param mondrianName
		 *            The mondrianName to set.
		 */
		public void setMondrianName(String mondrianName) {
			this.mondrianName = mondrianName;
		}
	} // MondrianProp

} // MondrianMember
