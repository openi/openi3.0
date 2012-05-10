package org.openi.olap.mondrian;

import com.tonbeller.jpivot.util.JPivotRuntimeException;

/**
 * This exception is currently never thrown.
 * 
 * This is thrown by the MondrianResult when the query returns no data, not even
 * any meta-data. This can occur if the user has a Mondrian Role that disallows
 * all data from a given Dimension. If this condition is not addressed, then a
 * table is created without one of its axis defined and manipulating that table
 * can cause JPivot to generate a NullPointerException.
 * <p>
 * Mondrian has an analogous issue with Dimension information. Mondrian can not
 * handle the situation where a Dimension has no data in the database, and will
 * result in a NullPointerException occurring in the Mondrian code. If this
 * exception were not thrown, then for JPivot, if an MDX query returns no data
 * on a given axis, a table is drawn missing that axis and further manipulation
 * of the table can result in a NullPointerException.
 * 
 * @author Richard M. Emberson
 * @author SUJEN
 */
abstract class NoValidMemberException extends JPivotRuntimeException {

	/**
	 * The axis without data.
	 */
	private final int axis;

	/**
	 * The MDX query.
	 */
	private final String mdx;

	/**
	 * Number of mondrian.olap.Position for the given axis.
	 */
	private final int numberOfPositions;
	/**
	 * Number of mondrian.olap.Member per Position.
	 */
	private final int numberOfMembers;

	NoValidMemberException(final int axis, final String mdx,
			final int numberOfPositions, final int numberOfMembers) {
		super("Axis " + axis + " has no data for mdx query '" + mdx + "'");
		this.axis = axis;
		this.mdx = mdx;
		this.numberOfPositions = numberOfPositions;
		this.numberOfMembers = numberOfMembers;
	}

	/**
	 * Get the axis with no data.
	 * 
	 * @return the axis without data.
	 */
	public int getAxis() {
		return axis;
	}

	/**
	 * Return the MDX query that generated this exception.
	 * 
	 * @return the MDX query.
	 */
	public String getMdx() {
		return mdx;
	}

	/**
	 * The number of Positions for the axis.
	 * 
	 * @return the number of positions.
	 */
	public int getNumberOfPositions() {
		return numberOfPositions;
	}

	/**
	 * The number of Members per Position (note that each Position for a given
	 * axis has the same number of members).
	 * 
	 * @return The number of Members.
	 */
	public int getNumberOfMembers() {
		return numberOfMembers;
	}
}
