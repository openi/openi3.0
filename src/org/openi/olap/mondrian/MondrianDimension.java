package org.openi.olap.mondrian;

import java.util.ArrayList;

import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Visitor;
import com.tonbeller.jpivot.olap.query.MDXElement;
import com.tonbeller.tbutils.res.Resources;

/**
 * @author SUJEN
 * MondrianDimension is an adapter class for the Mondrian Dimension.
 */
public class MondrianDimension implements Dimension, MDXElement {

	private mondrian.olap.Dimension monDimension = null;
	private ArrayList aHierarchies;
	MondrianModel model;
	Resources resources;

	protected MondrianDimension(mondrian.olap.Dimension monDimension,
			MondrianModel model) {
		this.monDimension = monDimension;
		this.model = model;
		aHierarchies = new ArrayList();
		resources = Resources.instance(model.getLocale(),
				MondrianDimension.class);
	}

	/**
	 * add Hierarchy
	 * 
	 * @param hierarchy
	 *            MondrianHierarchy to be stored
	 */
	protected void addHierarchy(MondrianHierarchy hierarchy) {
		aHierarchies.add(hierarchy);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Dimension#getHierarchies()
	 */
	public Hierarchy[] getHierarchies() {
		return (Hierarchy[]) aHierarchies.toArray(new MondrianHierarchy[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Dimension#isTime()
	 */
	public boolean isTime() {
		return monDimension.getDimensionType() == mondrian.olap.DimensionType.TimeDimension;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Dimension#isMeasure()
	 */
	public boolean isMeasure() {
		return monDimension.isMeasures();
	}

	public String getLabel() {
		String label = monDimension.getCaption();
		return resources.getOptionalString(label, label);
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
	 * @return the unique name
	 * @see com.tonbeller.jpivot.olap.model.Dimension#getUniqueName()
	 */
	public String getUniqueName() {
		return monDimension.getUniqueName();
	}

	/**
	 * @return the corresponding Mondrian dimension
	 */
	public mondrian.olap.Dimension getMonDimension() {
		return monDimension;
	}

} // MondrianDimension
