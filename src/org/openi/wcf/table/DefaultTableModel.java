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

import java.util.ArrayList;
import java.util.Collection;

import com.tonbeller.wcf.table.TableModel;
import com.tonbeller.wcf.table.TableModelChangeListener;
import com.tonbeller.wcf.table.TableModelChangeSupport;
import com.tonbeller.wcf.table.TableRow;

/**
 * a simple table model. Its implemented as an ArrayList containing TableRow objects
 */

public class DefaultTableModel extends ArrayList implements TableModel {
  private String title;
  private String[] columnTitles;
  TableModelChangeSupport changeSupport;

  public DefaultTableModel() {
    this.changeSupport = new TableModelChangeSupport(this);
  }

  public DefaultTableModel(Collection rows, String[] columnTitles) {
    this.addAll(rows);
    this.columnTitles = columnTitles;
    this.changeSupport = new TableModelChangeSupport(this);
  }

  public String getTitle() {
    return title;
  }
  public void setTitle(String newTitle) {
    title = newTitle;
  }

  public int getRowCount() {
    return size();
  }

  public TableRow getRow(int rowIndex) {
    return (TableRow) get(rowIndex);
  }
  
  public void setColumnTitles(String[] newColumnTitles) {
    columnTitles = newColumnTitles;
  }
  
  public String[] getColumnTitles() {
    return columnTitles;
  }
  
  public String getColumnTitle(int i) {
    return columnTitles[i];
  }
  
  public void setColumnTitle(int i, String newColumnTitle) {
    columnTitles[i] = newColumnTitle;
  }
  
  public int getColumnCount() {
    if (columnTitles == null)
      return 0;
    return columnTitles.length;
  }

  public void addTableModelChangeListener(TableModelChangeListener listener) {
    changeSupport.addTableModelChangeListener(listener);
  }

  public void removeTableModelChangeListener(TableModelChangeListener listener) {
    changeSupport.removeTableModelChangeListener(listener);
  }

  public void fireModelChanged(boolean identityChanged) {
    changeSupport.fireModelChanged(identityChanged);
  }

}