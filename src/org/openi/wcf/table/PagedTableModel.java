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
import com.tonbeller.wcf.table.TableModelChangeEvent;
import com.tonbeller.wcf.table.TableModelDecorator;
import com.tonbeller.wcf.table.TableRow;

/**
 * a TableModel decorator that divides the table model into pages
 */
class PagedTableModel extends TableModelDecorator {
	private int pageSize = 10;
	private int currentPage;
	private boolean showAll = false;

	public PagedTableModel() {
	}

	public PagedTableModel(TableModel model) {
		super(model);
	}

	public int getRowCount() {
		if (showAll)
			return super.getRowCount();
		validate();
		int offs = currentPage * pageSize;
		int rows = super.getRowCount();
		if (offs + pageSize > rows)
			return rows - offs;
		return pageSize;
	}

	public TableRow getRow(int rowIndex) {
		if (showAll)
			return super.getRow(rowIndex);
		validate();
		return super.getRow(rowIndex + currentPage * pageSize);
	}

	public void setPageSize(int newPageSize) {
		pageSize = newPageSize;
		if (pageSize < 1)
			pageSize = 1;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setCurrentPage(int newCurrentPage) {
		currentPage = newCurrentPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getPageCount() {
		int rc = super.getRowCount();
		int pc = rc / pageSize;
		if ((rc % pageSize) != 0)
			pc += 1;
		return pc;
	}

	void validate() {
		int pageCount = getPageCount();
		if (currentPage >= pageCount)
			currentPage = pageCount - 1;
		if (currentPage < 0)
			currentPage = 0;
	}

	public void setShowAll(boolean newShowAll) {
		showAll = newShowAll;
	}

	public boolean isShowAll() {
		return showAll;
	}

	public void tableModelChanged(TableModelChangeEvent event) {
		currentPage = 0;
		validate();
	}

}