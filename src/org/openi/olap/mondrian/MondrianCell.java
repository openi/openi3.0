package org.openi.olap.mondrian;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.model.NumberFormat;
import com.tonbeller.jpivot.olap.model.impl.CellBase;
import com.tonbeller.jpivot.util.NumSeparators;

/**
 * Cell Inplementation for Mondrian
 * @author SUJEN
 */
public class MondrianCell extends CellBase {

	static Logger logger = Logger.getLogger(MondrianModel.class);

	private mondrian.olap.Cell monCell;
	private MondrianModel model;

	private boolean isGrouping = false;
	private boolean isPercent = false;
	private int fractionDigits = 0;

	/**
	 * Constructor
	 */
	protected MondrianCell(mondrian.olap.Cell monCell, MondrianModel model) {
		this.monCell = monCell;
		this.model = model;

		/*
		 * mondrian.olap.Member measure = monCell.getMeasure(); Exp formatExp =
		 * (Exp)
		 * measure.getPropertyValue(mondrian.olap.Property.PROPERTY_FORMAT_EXP);
		 * String formatString = "Standard"; if (formatExp != null && formatExp
		 * instanceof mondrian.olap.Literal) { Literal lit =
		 * (mondrian.olap.Literal) formatExp; if (lit.getType() ==
		 * Category.String) { formatString = (String) lit.getValue(); } }
		 * 
		 * // Format is not very useful Format format = Format.get(formatString,
		 * model.getLocale());
		 * 
		 * numberFormat = new NumberFormat() { public boolean isGrouping() {
		 * return isGrouping; } public int getFractionDigits() { return
		 * fractionDigits; } };
		 */
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Cell#getValue()
	 */
	public Object getValue() {
		return monCell.getValue();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Cell#isNull()
	 */
	public boolean isNull() {
		return monCell.isNull();
	}

	/*
	 * determine formatting properties
	 * 
	 * @see com.tonbeller.jpivot.olap.model.Cell#getFormat()
	 */
	public NumberFormat getFormat() {
		if (monCell.isNull())
			return null;

		Object o = monCell.getValue();
		if (o instanceof Number) {
			// continue
		} else
			return null;

		isPercent = formattedValue.indexOf('%') >= 0;
		NumSeparators sep = NumSeparators.instance(model.getLocale());

		fractionDigits = 0;
		if (formattedValue.indexOf(sep.thouSep) >= 0)
			isGrouping = true;
		int i = formattedValue.indexOf(sep.decimalSep);
		if (i > 0) {
			while (++i < formattedValue.length()
					&& Character.isDigit(formattedValue.charAt(i)))
				++fractionDigits;
		}

		return new NumberFormat() {
			public boolean isGrouping() {
				return isGrouping;
			}

			public int getFractionDigits() {
				return fractionDigits;
			}

			public boolean isPercent() {
				return isPercent;
			}
		};

	}

	/**
	 * @return
	 */
	public mondrian.olap.Cell getMonCell() {
		return this.monCell;
	}
} // End MondrianCell
