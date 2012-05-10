package org.openi.olap.xmla;

import java.io.Serializable;
import java.util.Map;

import com.tonbeller.jpivot.olap.query.Memento;

/**
 * Java Bean object to hold the state of an XMLA MDX session. Contains parts of
 * XMLA_Model and subordinate objects.
 * @author SUJEN
 * 
 */
public class XMLA_Memento extends Memento implements Serializable {

	static final int CURRENT_VERSION = 1;
	int version;
	private String uri = null;
	private String user = null;
	private String password = null;
	private String catalog = null;
	private String dataSource = null;

	private Map calcMeasurePropMap = null;

	/**
	 * @return
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param string
	 */
	public void setDataSource(String string) {
		dataSource = string;
	}

	/**
	 * @param string
	 */
	public void setPassword(String string) {
		password = string;
	}

	/**
	 * @param string
	 */
	public void setUri(String string) {
		uri = string;
	}

	/**
	 * @param string
	 */
	public void setUser(String string) {
		user = string;
	}

	/**
	 * @return
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

	/**
	 * @return
	 */
	public String getCatalog() {
		return catalog;
	}

	/**
	 * @param string
	 */
	public void setCatalog(String string) {
		catalog = string;
	}

	/**
	 * @return
	 */
	public Map getCalcMeasurePropMap() {
		return calcMeasurePropMap;
	}

	/**
	 * @param map
	 */
	public void setCalcMeasurePropMap(Map map) {
		calcMeasurePropMap = map;
	}

} // XMLA_Memento
