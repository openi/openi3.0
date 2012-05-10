package org.openi.olap.xmla;

/**
 * @author stflourd
 * @author SUJEN
 * 
 */

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.Cell;
import com.tonbeller.jpivot.olap.navi.DrillThrough;
import com.tonbeller.wcf.table.TableModel;

public class XMLA_DrillThrough extends ExtensionSupport implements DrillThrough {

	private boolean extendedContext = true;

	/**
	 * Constructor sets ID
	 */
	public XMLA_DrillThrough() {
		super.setId(DrillThrough.ID);
	}

	/**
	 * drill through is possible if <code>member</code> is not calculated
	 */

	public boolean canDrillThrough(Cell cell) {
		// todo need to check if the cell is a calculated field before. If
		// calculated then
		// do't allow drillthrough
		return true;
	}

	/**
	 * does a drill through, retrieves data that makes up the selected Cell
	 */
	public TableModel drillThrough(Cell cell) {
		XMLA_DrillThroughTableModel dtm = new XMLA_DrillThroughTableModel();
		XMLA_Model xmodel = ((XMLA_Cell) cell).getModel();
		dtm.setCellOrdinal(((XMLA_Cell) cell).getOrdinal());
		dtm.setModel(xmodel);
		return dtm;
	}

	/**
	 * gets the mondrian connection
	 * 
	 * @return
	 */

	public boolean isExtendedContext() {
		return extendedContext;
	}

	public void setExtendedContext(boolean extendedContext) {
		this.extendedContext = extendedContext;
	}

}
