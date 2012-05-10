package org.openi.olap.mondrian;

import java.io.Serializable;

import com.tonbeller.jpivot.olap.query.Memento;

/**
 * Java Bean object to hold the state of a Mondrian MDX session. Contains parts
 * of MondrianModel and subordinate objects.
 */
public class MondrianMemento extends Memento implements Serializable {

	static final int CURRENT_VERSION = 3;
	int version;

	// properties of the session
	String jdbcDriver;
	String connectString;

	/**
	 * Get connectString.
	 * 
	 * @return connectString
	 */
	public String getConnectString() {
		return connectString;
	}

	/**
	 * Get jdbcDriver.
	 * 
	 * @return jdbcDriver
	 */
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * Set connectString.
	 * 
	 * @param connectString
	 */
	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	/**
	 * Set jdbcDriver.
	 * 
	 * @param jdbcDriver
	 */
	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	/**
	 * @return version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param i
	 */
	public void setVersion(int i) {
		version = i;
	}

} // End MondrianMemento
