/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 *
 * 
 */
package org.openi.wcf.table;

import java.util.Comparator;

import com.tonbeller.wcf.table.TableColumn;
import com.tonbeller.wcf.table.TableRow;

/**
 * compares two TableRow objects for sorting
 */

class RowComparator implements Comparator {
	TableColumn column;

	public RowComparator() {
		column = new TableColumn(0);
	}

	public RowComparator(TableColumn column) {
		this.column = column;
	}

	public int compare(Object o1, Object o2) {
		TableRow tr1 = (TableRow) o1;
		TableRow tr2 = (TableRow) o2;
		int columnIndex = column.getColumnIndex();
		Object v1 = tr1.getValue(columnIndex);
		Object v2 = tr2.getValue(columnIndex);
		Comparator comp = column.getComparator();
		int res;
		if (v1 == null && v2 == null)
			res = 0;
		else if (v1 == null)
			res = 1;
		else if (v2 == null)
			res = -1;
		else
			res = comp.compare(v1, v2);
		if (column.isDescending())
			return -res;
		return res;
	}

	public TableColumn getColumn() {
		return column;
	}

	public void setColumn(TableColumn column) {
		this.column = column;
	}

	public int getColumnIndex() {
		return column.getColumnIndex();
	}

}