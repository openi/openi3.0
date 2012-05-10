package org.openi.olap.xmla;

/**
 * @author stflourd
 * @author SUJEN
 * 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import com.tonbeller.wcf.table.AbstractTableModel;
import com.tonbeller.wcf.table.DefaultTableRow;
import com.tonbeller.wcf.table.TableRow;
import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.*;

/**
 * A wcf table model for drill through data, requires an sql query and
 * connection information to be set.
 */

public class XMLA_DrillThroughTableModel extends AbstractTableModel {
	private static Logger logger = Logger
			.getLogger(XMLA_DrillThroughTableModel.class);
	private String title = "Drill Through Table";
	private String caption = "";
	private String dataSourceName;
	private int cellOrdinal;

	/**
	 * @return Returns the cellOrdinal.
	 */
	public int getCellOrdinal() {
		return cellOrdinal;
	}

	/**
	 * @param cellOrdinal
	 *            The cellOrdinal to set.
	 */
	public void setCellOrdinal(int cellOrdinal) {
		this.cellOrdinal = cellOrdinal;
	}

	XMLA_Model model;
	XMLA_Model drillModel;
	XMLA_Result drillResult;

	/**
	 * @return Returns the model.
	 */
	public XMLA_Model getModel() {
		return model;
	}

	/**
	 * @param model
	 *            The model to set.
	 */
	public void setModel(XMLA_Model model) {
		this.model = model;
	}

	// private static Context jndiContext;

	private boolean ready = false;

	private TableRow[] rows = new TableRow[0];
	private String[] columnTitles = new String[0];

	public XMLA_DrillThroughTableModel() {
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

	// create a drillthrough MDX to query the cube with
	private String buildDrillThroughMdx(XMLA_Result res) {
		Axis[] axes = res.getAxes();
		StringBuffer mdxBuf = new StringBuffer(" Drillthrough Select ");
		int nXPositions = axes[0].getPositions().size();
		int posIdx = 0;
		for (int i = 0; i < axes.length; i++) {

			// axis 0 ==> x-Coordinate
			// axis 1 ==> y-Coordinate
			if (i == 0) {
				posIdx = cellOrdinal
						- ((cellOrdinal / nXPositions) * nXPositions);
			} else {
				posIdx = cellOrdinal / nXPositions;
			}

			XMLA_Position position = (XMLA_Position) axes[i].getPositions()
					.get(posIdx);
			Member[] positionMembers = position.getMembers();
			if (i == 1) {
				mdxBuf.append(",");
			}
			mdxBuf.append("{(");
			for (int j = 0; j < positionMembers.length; j++) {
				XMLA_Member member = (XMLA_Member) positionMembers[j];
				if (j > 0) {
					mdxBuf.append(",");
				}
				mdxBuf.append(member.getUniqueName());
			}
			mdxBuf.append(")}");
			if (i == 0) {
				mdxBuf.append(" on Columns");
			} else {
				mdxBuf.append(" on Rows");

			}
		}

		mdxBuf.append(" from [" + this.model.getCube() + "]");

		Axis slicer = res.getSlicer();
		// note at this stage we only deal with position 0
		List slicerList = slicer.getPositions();

		if (slicerList != null && slicerList.size() > 0) {
			XMLA_Position slicerPosition = (XMLA_Position) slicerList.get(0);
			Member[] slicerMembers = slicerPosition.getMembers();
			for (int j = 0; j < slicerMembers.length; j++) {
				XMLA_Member member = (XMLA_Member) slicerMembers[j];
				if (j == 0) {
					mdxBuf.append(" Where ( ");
				} else if (j > 0) {
					mdxBuf.append(" , ");
				}
				mdxBuf.append(member.getUniqueName());

			}

			if (slicerMembers.length > 0) {
				mdxBuf.append(")");

			}
		}
		return mdxBuf.toString();

	}

	/**
	 * execute
	 * 
	 * @throws Exception
	 */
	private void executeQuery() {

		XMLA_Result res = null;
		try {
			res = (XMLA_Result) model.getResult();

			String mdx = buildDrillThroughMdx(res);
			if (drillModel == null) {
				drillModel = new XMLA_Model();
				drillModel.setCatalog(model.getCatalog());
				drillModel.setDataSource(model.getDataSource());
				drillModel.setMdxQuery(mdx);
				drillModel.setID("Drill" + model.getID());
				drillModel.setUri(model.getUri());
				drillModel.setUser(model.getUser());
				drillModel.setPassword(model.getPassword());
				// drillModel.initializeDrillThrough();
				drillModel.initialize();
			}

			drillResult = (XMLA_Result) drillModel.getDrillResult();

			// populate column header
			// note the use of a Map for the column headers, this is because
			// the xml returned for the rows contains variable number of columns
			Map headerMap = drillResult.getDrillHeader();
			int numCols = headerMap.size();
			columnTitles = new String[numCols];
			Set headerSet = headerMap.entrySet();
			Iterator headerSetIt = headerSet.iterator();
			while (headerSetIt.hasNext()) {
				Map.Entry e = (Map.Entry) headerSetIt.next();
				;
				columnTitles[((Integer) e.getValue()).intValue()] = e.getKey()
						.toString();
			}

			// populate rows
			List dataRows = drillResult.getDrillRows();
			List tempRows = new ArrayList();
			Iterator dataRowIt = dataRows.iterator();
			while (dataRowIt.hasNext()) {
				Object[] row = new Object[numCols];
				Map columnMap = (HashMap) dataRowIt.next();
				Set columnSet = columnMap.entrySet();
				Iterator colSetIt = columnSet.iterator();
				while (colSetIt.hasNext()) {
					Map.Entry e = (Map.Entry) colSetIt.next();
					String value = e.getValue().toString();
					String colName = e.getKey().toString();
					int colNo = ((Integer) headerMap.get(colName)).intValue();
					row[colNo] = value;
				}
				tempRows.add(new DefaultTableRow(row));
			}
			rows = (TableRow[]) tempRows.toArray(new TableRow[0]);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("?", e);
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
			} catch (Exception e1) {
				// ignore
			}
		}
		ready = true;
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

}
