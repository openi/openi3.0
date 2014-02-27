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
/*
 * Created on Jun 11, 2004
 */
package org.openi.wcf.table;

import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.selection.SelectionModel;
import com.tonbeller.wcf.table.TableModel;

/**
 * Common behaviour of EditableTableComponent and TableComponent
 * 
 * @author av
 */
public interface ITableComponent extends Component {
	/** set the data model and initializes everything */
	void setModel(TableModel newModel);

	/** the underlying data model w/o sort/paging decorators */
	TableModel getModel();

	/** the current selection */
	void setSelectionModel(SelectionModel selectionModel);

	/** the current selection */
	SelectionModel getSelectionModel();

	/** allows to enable/disable sorting of columns */
	void setSortable(boolean newSortable);

	/** allows to enable/disable sorting of columns */
	boolean isSortable();

	/** allows to enable/disable paging of rows */
	void setPageable(boolean newPageable);

	/** allows to enable/disable paging of rows */
	boolean isPageable();

	/** set the current sort column */
	void setSortColumnIndex(int index);

	RowComparator getRowComparator();

	int getPageSize();

	void setPageSize(int newPageSize);

	int getCurrentPage();

	void setCurrentPage(int newCurrentPage);

	int getPageCount();

	/**
	 * shall this table have a close button?
	 */
	boolean isClosable();

	/**
	 * shall this table have a close button?
	 */
	void setClosable(boolean b);

	/**
	 * gets the border attribute of the generated table. Overrides the global
	 * stylesheet parameter "border".
	 * 
	 * @return the border attribute or null
	 */
	String getBorder();

	/**
	 * sets the border attribute of the generated table. Overrides the global
	 * stylesheet parameter "border".
	 * 
	 * @param border
	 *            the border attribute or null to use the stylesheet parameter
	 */
	void setBorder(String border);

	/**
	 * sets the renderId attribute of the generated table. Overrides the global
	 * stylesheet parameter "renderId".
	 * 
	 * @param renderId
	 *            the renderId attribute or null to use the stylesheet parameter
	 */
	void setRenderId(String renderId);

	/**
	 * gets the renderId attribute of the generated table. Overrides the global
	 * stylesheet parameter "renderId".
	 * 
	 * @return the renderId attribute or null
	 */
	String getRenderId();

	/**
	 * sets the error message to display
	 * 
	 * @param message
	 *            the message to display or null to remove a previous error
	 *            message
	 */
	void setError(String message);

	/**
	 * user may view data but not change the selection
	 */
	public boolean isReadOnly();

	/**
	 * user may view data but not change the selection
	 */
	public void setReadOnly(boolean readOnly);

}