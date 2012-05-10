package org.openi.olap.xmla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Bean assign a property values to binary coded integers
 * @author SUJEN
 */
public class XMLA_PropValAssign {

	private Map propMap = new HashMap();
	private int firstBit = 0;
	static private int LASTBIT = 15; // use FONTSIZE, < 2**16

	static Logger logger = Logger.getLogger(XMLA_PropValAssign.class);

	/**
	 * @param values
	 *            - possible value assignments
	 */
	public void addProp(String prop, List values) {
		// how many bits do we need ? log(2, #values)
		int nValues = values.size();
		if (nValues == 0)
			return;
		int nBits = 1;
		int n = (nValues - 1) / 2;
		while (n > 0) {
			++nBits;
			n = n / 2;
		}

		int mask = 1 << nBits;
		mask = mask - 1; // 2**nBits -1
		mask = mask << firstBit;

		if (firstBit + nBits > LASTBIT) {
			// not enough bits to encode the property values (FONT_SIZE)
			logger.error("could not encode property values " + prop + " #"
					+ nValues);
			return;
		}

		List vAssignList = new ArrayList();
		int iBitVal = 0;
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			String val = (String) iter.next();
			ValAssign vAssign = new ValAssign();
			vAssign.setVal(val);
			vAssign.setBitMask(mask);
			int bitVal = iBitVal << firstBit;
			vAssign.setBitVal(bitVal);
			vAssignList.add(vAssign);
			++iBitVal;
		}
		propMap.put(prop, vAssignList);
		firstBit += nBits;
	}

	/**
	 * @param prop
	 * @return the value assignment list
	 */
	public List getValAssignList(String prop) {
		return (List) propMap.get(prop);
	}

	/**
	 * @return
	 */
	public Map getPropMap() {
		return propMap;
	}

	/**
	 * @param map
	 */
	public void setPropMap(Map map) {
		propMap = map;
	}

	/**
	 * @return
	 */
	public int getFirstBit() {
		return firstBit;
	}

	/**
	 * @param i
	 */
	public void setFirstBit(int i) {
		firstBit = i;
	}

	/**
	 * Bean - assigned property value
	 */
	public static class ValAssign {
		private String val;
		private int bitMask;
		private int bitVal;

		/**
		 * @return
		 */
		public String getVal() {
			return val;
		}

		/**
		 * @param string
		 */
		public void setVal(String string) {
			val = string;
		}

		/**
		 * @return
		 */
		public int getBitMask() {
			return bitMask;
		}

		/**
		 * @param bitMask
		 */
		public void setBitMask(int bitMask) {
			this.bitMask = bitMask;
		}

		/**
		 * @return
		 */
		public int getBitVal() {
			return bitVal;
		}

		/**
		 * @param i
		 */
		public void setBitVal(int i) {
			bitVal = i;
		}

	} // ValAssign

} // XMLA_PropValAssign
