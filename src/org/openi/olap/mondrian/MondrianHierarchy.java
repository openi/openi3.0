package org.openi.olap.mondrian;

import java.util.ArrayList;

import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Visitor;
import com.tonbeller.jpivot.olap.query.MDXElement;
import com.tonbeller.tbutils.res.Resources;

/**
 * MondrianHierarchy is an adapter class for the Mondrian Hierarchy.
 */
public class MondrianHierarchy implements Hierarchy, MDXElement {

	private mondrian.olap.Hierarchy monHierarchy;
	private MondrianDimension dimension;
	private ArrayList aLevels;
	private MondrianModel model;
	private Resources resources;

	/**
	 * Constructor
	 * 
	 * @param monHierarchy
	 *            Mondrian Hierarchy
	 * @param dimension
	 *            parent
	 */
	protected MondrianHierarchy(mondrian.olap.Hierarchy monHierarchy,
			MondrianDimension dimension, MondrianModel model) {
		this.monHierarchy = monHierarchy;
		this.dimension = dimension;
		this.model = model;
		this.resources = Resources.instance(model.getLocale(),
				MondrianHierarchy.class);
		aLevels = new ArrayList();
		dimension.addHierarchy(this);
	}

	/**
	 * add level
	 * 
	 * @param level
	 *            MondrianLevel
	 */
	protected void addLevel(MondrianLevel level) {
		aLevels.add(level);
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
		return (Level[]) aLevels.toArray(new MondrianLevel[0]);
	}

	public String getLabel() {
		String label = monHierarchy.getCaption();
		return resources.getOptionalString(label, label);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Visitable#accept(Visitor)
	 */
	public void accept(Visitor visitor) {
		visitor.visitHierarchy(this);
	}

	/**
	 * Returns the monHierarchy.
	 * 
	 * @return mondrian.olap.Hierarchy
	 */
	public mondrian.olap.Hierarchy getMonHierarchy() {
		return monHierarchy;
	}

	public Object getRootDecoree() {
		return this;
	}

	/**
	 * @return the unique name
	 * @see com.tonbeller.jpivot.olap.model.Hierarchy#getUniqueName()
	 */
	public String getUniqueName() {
		return monHierarchy.getUniqueName();
	}

	public boolean hasAll() {
		return monHierarchy.hasAll();
	}
}
