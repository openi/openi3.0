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

import com.tonbeller.wcf.table.TableModel;
import com.tonbeller.wcf.table.TableModelChangeListener;
import com.tonbeller.wcf.table.TableRow;

/**
 * @author av
 */
public class EmptyTableModel implements TableModel {
	static TableModel model = new EmptyTableModel();

	public static TableModel instance() {
		return model;
	}

	private EmptyTableModel() {
	}

	public String getTitle() {
		return "";
	}

	public int getRowCount() {
		return 0;
	}

	public TableRow getRow(int rowIndex) {
		return null;
	}

	public int getColumnCount() {
		return 0;
	}

	public String getColumnTitle(int columnIndex) {
		return null;
	}

	public void addTableModelChangeListener(TableModelChangeListener listener) {
	}

	public void removeTableModelChangeListener(TableModelChangeListener listener) {
	}

	public void fireModelChanged(boolean identityChanged) {
	}
}
