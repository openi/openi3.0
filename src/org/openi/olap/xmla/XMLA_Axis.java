package org.openi.olap.xmla;

import java.util.ArrayList;
import java.util.List;

import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Visitor;

/**
 * Result Axis XMLA
 * @author SUJEN
 */
public class XMLA_Axis implements Axis {

	private String name;

	private int ordinal;
	private int nHier = 0;

	private List aHiers = new ArrayList();
	private List aPositions = new ArrayList();

	/**
	 * c'tor
	 * 
	 * @param name
	 */
	XMLA_Axis(int ordinal, String name) {
		this.ordinal = ordinal;
		this.name = name;
	}

	void addHier(XMLA_Hierarchy hier) {
		aHiers.add(hier);
		++nHier;
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
	 * Returns the nHier.
	 * 
	 * @return int
	 */
	public int getNHier() {
		return nHier;
	}

	/**
	 * add position
	 * 
	 * @param pos
	 */
	void addPosition(XMLA_Position pos) {
		aPositions.add(pos);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Axis#getPositions()
	 */
	public List getPositions() {
		return aPositions;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Axis#getHierarchies()
	 */
	public Hierarchy[] getHierarchies() {
		return (Hierarchy[]) aHiers.toArray(new Hierarchy[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Visitable#accept(Visitor)
	 */
	public void accept(Visitor visitor) {
		visitor.visitAxis(this);
	}

	public Object getRootDecoree() {
		return this;
	}

	/**
	 * @return the ordinal of the axis , slicer = -1
	 */
	public int getOrdinal() {
		return ordinal;
	}

} // End XMLA_Axis
