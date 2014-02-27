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

import java.util.Arrays;
import java.util.Comparator;

import com.tonbeller.wcf.table.TableModel;
import com.tonbeller.wcf.table.TableModelChangeEvent;
import com.tonbeller.wcf.table.TableModelDecorator;
import com.tonbeller.wcf.table.TableRow;

/**
 * a table model decorator that sorts the underlying table model
 */

class SortedTableModel extends TableModelDecorator {
	TableRow[] data = null;

	public SortedTableModel() {
	}

	public SortedTableModel(TableModel model) {
		super(model);
	}

	public int getRowCount() {
		if (data == null)
			return super.getRowCount();
		return data.length;
	}

	public TableRow getRow(int index) {
		if (data == null)
			return super.getRow(index);
		return data[index];
	}

	public void unSort() {
		data = null;
	}

	public void sort(Comparator compare) {
		int N = super.getRowCount();
		data = new TableRow[N];
		for (int i = 0; i < N; i++)
			data[i] = super.getRow(i);
		Arrays.sort(data, compare);
	}

	public void tableModelChanged(TableModelChangeEvent event) {
		data = null;
	}

}
