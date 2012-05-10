package org.openi.olap.mondrian;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import mondrian.rolap.RolapConnectionProperties;
import mondrian.olap.MemoryLimitExceededException;
import mondrian.util.MemoryMonitor;
import mondrian.util.MemoryMonitorFactory;

import org.apache.log4j.Logger;

import com.tonbeller.wcf.table.AbstractTableModel;
import com.tonbeller.wcf.table.DefaultTableRow;
import com.tonbeller.wcf.table.TableRow;

/**
 * A wcf table model for drill through data, requires an sql query and
 * connection information to be set.
 */

public class MondrianDrillThroughTableModel extends AbstractTableModel {
	private static Logger logger = Logger
			.getLogger(MondrianDrillThroughTableModel.class);
	private String title = "Drill Through Table";
	private String caption = "";
	private String sql = "";
	private String jdbcUser;
	private String jdbcUrl;
	private String jdbcPassword;
	private String jdbcDriver;
	private String dataSourceName;

	private DataSource dataSource;
	private static Context jndiContext;

	private boolean ready = false;

	private TableRow[] rows = new TableRow[0];
	private String[] columnTitles = new String[0];

	public MondrianDrillThroughTableModel() {
	}

	public int getRowCount() {
		if (!ready) {
			executeQuery();
		}
		return rows.length;
	}

	public TableRow getRow(int rowIndex) {
		if (!ready) {
			executeQuery();
		}
		return rows[rowIndex];
	}

	public String getTitle() {
		return title;
	}

	/**
	 * @return
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
		this.ready = false;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * wcf table component calls this method from it's constructor to get the
	 * number of columns
	 * 
	 */
	public int getColumnCount() {
		if (!ready) {
			executeQuery();
		}
		return columnTitles.length;
	}

	public String getColumnTitle(int columnIndex) {
		if (!ready) {
			executeQuery();
		}
		return columnTitles[columnIndex];
	}

	/**
	 * execute sql query
	 * 
	 * @throws Exception
	 */
	private void executeQuery() {
		Connection con = null;
		class Listener implements MemoryMonitor.Listener {
			String oomMsg;

			Listener() {
			}

			public void memoryUsageNotification(long used, long max) {
				StringBuffer buf = new StringBuffer(200);
				buf.append("OutOfMemory used=");
				buf.append(used);
				buf.append(", max=");
				buf.append(max);
				if (dataSourceName != null) {
					buf.append(" for data source: ");
					buf.append(dataSourceName);
				} else if (jdbcUrl != null) {
					buf.append(" for jcbc URL: ");
					buf.append(jdbcUrl);
				}
				this.oomMsg = buf.toString();
			}

			void check() throws MemoryLimitExceededException {
				if (oomMsg != null) {
					throw new MemoryLimitExceededException(oomMsg);
				}
			}
		}
		Listener listener = new Listener();
		MemoryMonitor mm = MemoryMonitorFactory.getMemoryMonitor();
		try {
			mm.addListener(listener);

			con = getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(sql);
			ResultSetMetaData md = rs.getMetaData();
			int numCols = md.getColumnCount();
			columnTitles = new String[numCols];

			// check for OutOfMemory
			listener.check();

			// set column headings
			for (int i = 0; i < numCols; i++) {
				// columns are 1 based
				columnTitles[i] = md.getColumnName(i + 1);
			}
			title = title.concat(" for "
					+ columnTitles[columnTitles.length - 1]);
			// loop through rows
			List tempRows = new ArrayList();
			while (rs.next()) {
				Object[] row = new Object[numCols];
				// loop on columns, 1 based
				for (int i = 0; i < numCols; i++) {
					row[i] = rs.getObject(i + 1);
				}
				tempRows.add(new DefaultTableRow(row));

				// check for OutOfMemory
				listener.check();
			}
			rs.close();
			rows = (TableRow[]) tempRows.toArray(new TableRow[0]);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("?", e);
			// problem occured, set table model to zero size
			rows = new TableRow[1];
			columnTitles = new String[1];
			columnTitles[0] = "An error occured";
			Object[] row = new Object[1];
			row[0] = e.toString();
			rows[0] = new DefaultTableRow(row);
			ready = false;
			return;
		} finally {
			try {
				con.close();
			} catch (Exception e1) {
				// ignore
			}
			mm.removeListener(listener);
		}
		ready = true;
	}

	/**
	 * get sql connection
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		if (dataSource == null && dataSourceName == null) {

			if (jdbcUrl == null) {
				throw new RuntimeException("Mondrian Connect string '"
						+ "' must contain either '"
						+ RolapConnectionProperties.Jdbc + "' or '"
						+ RolapConnectionProperties.DataSource + "'");
			}
			return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
		} else {
			return getDataSource().getConnection();
		}
	}

	private DataSource getDataSource() {
		if (dataSource == null) {
			// Get connection from datasource.
			try {
				dataSource = (DataSource) getJndiContext().lookup(
						dataSourceName);
			} catch (NamingException e) {
				throw new RuntimeException(
						"Error while looking up data source (" + dataSourceName
								+ ")", e);
			}
		}
		return dataSource;
	}

	private Context getJndiContext() throws NamingException {
		if (jndiContext == null) {
			jndiContext = new InitialContext();
		}
		return jndiContext;
	}

	/**
	 * @return
	 */
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * @param jdbcDriver
	 */
	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	/**
	 * @return
	 */
	public String getJdbcPassword() {
		return jdbcPassword;
	}

	/**
	 * @param jdbcPassword
	 */
	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}

	/**
	 * @return
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * @param jdbcUrl
	 */
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	/**
	 * @return
	 */
	public String getJdbcUser() {
		return jdbcUser;
	}

	/**
	 * @param jdbcUser
	 */
	public void setJdbcUser(String jdbcUser) {
		this.jdbcUser = jdbcUser;
	}

	/**
	 * @return
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @param caption
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @return
	 */
	public String getDataSourceName() {
		return dataSourceName;
	}

	/**
	 * @param string
	 */
	public void setDataSourceName(String string) {
		dataSourceName = string;
	}

	/**
	 * Allow support for external data sources
	 * 
	 * @param externalDataSource
	 *            the external datasource to use
	 */
	public void setExternalDataSource(DataSource externalDataSource) {
		this.dataSource = externalDataSource;
	}

}
