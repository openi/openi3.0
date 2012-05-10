package org.openi.olap.mondrian;

import mondrian.olap.Util.PropertyList;
import mondrian.rolap.RolapConnectionProperties;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.Cell;
import com.tonbeller.jpivot.olap.navi.DrillThrough;
import com.tonbeller.wcf.table.TableModel;

/**
 * @author Robin Bagot
 * 
 *         Implementation of the DrillExpand Extension for Mondrian Data Source.
 */
public class MondrianDrillThrough extends ExtensionSupport implements
		DrillThrough {

	private boolean extendedContext = true;

	/**
	 * Constructor sets ID
	 */
	public MondrianDrillThrough() {
		super.setId(DrillThrough.ID);
	}

	/**
	 * drill through is possible if <code>member</code> is not calculated
	 */
	public boolean canDrillThrough(Cell cell) {
		return ((MondrianCell) cell).getMonCell().canDrillThrough();
		// String sql = ((MondrianCell)
		// cell).getMonCell().getDrillThroughSQL(extendedContext);
		// return sql != null;
	}

	/**
	 * does a drill through, retrieves data that makes up the selected Cell
	 */
	public TableModel drillThrough(Cell cell) {
		String sql = ((MondrianCell) cell).getMonCell().getDrillThroughSQL(
				extendedContext);
		if (sql == null) {
			throw new NullPointerException("DrillThroughSQL returned null");
		}
		MondrianDrillThroughTableModel dtm = new MondrianDrillThroughTableModel();
		dtm.setSql(sql);
		PropertyList connectInfo = ((MondrianModel) getModel())
				.getConnectProperties();
		String jdbcUrl = connectInfo.get(RolapConnectionProperties.Jdbc.name());
		String jdbcUser = connectInfo.get(RolapConnectionProperties.JdbcUser
				.name());
		String jdbcPassword = connectInfo
				.get(RolapConnectionProperties.JdbcPassword.name());
		String dataSourceName = connectInfo
				.get(RolapConnectionProperties.DataSource.name());
		dtm.setJdbcUrl(jdbcUrl);
		dtm.setJdbcUser(jdbcUser);
		dtm.setJdbcPassword(jdbcPassword);
		dtm.setDataSourceName(dataSourceName);
		dtm.setExternalDataSource(((MondrianModel) getModel())
				.getExternalDataSource());
		return dtm;
	}

	public boolean isExtendedContext() {
		return extendedContext;
	}

	public void setExtendedContext(boolean extendedContext) {
		this.extendedContext = extendedContext;
	}

}
