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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.component.NestableComponentSupport;
import com.tonbeller.wcf.component.RenderListener;
import com.tonbeller.wcf.controller.Dispatcher;
import com.tonbeller.wcf.controller.DispatcherSupport;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestListener;
import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.scroller.Scroller;
import com.tonbeller.wcf.selection.SelectionMgr;
import com.tonbeller.wcf.selection.SelectionModel;
import com.tonbeller.wcf.table.CellRenderer;
import com.tonbeller.wcf.table.TableColumn;
import com.tonbeller.wcf.table.TableModel;
import com.tonbeller.wcf.table.TableModelChangeEvent;
import com.tonbeller.wcf.table.TableModelChangeListener;
import com.tonbeller.wcf.table.TableRow;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * The main class for the table.
 */
public class TableComponent extends NestableComponentSupport implements
		ITableComponent {

	private TableColumn[] tableColumns;
	private TableModel tableModel;
	private boolean sortable = true;
	private boolean pageable = true;
	private boolean closable = true;
	private boolean colHeaders = true;
	private Resources resources;

	private String border = null;
	private String width = null;
	private String renderId = null;
	private String error = null;

	SelectionMgr selectionMgr;
	RowComparator comparator = new RowComparator();
	Dispatcher dispatcher = new DispatcherSupport();

	// initialized by initialize()

	SortedTableModel sorter = new SortedTableModel();
	PagedTableModel pager = new PagedTableModel();
	Document factory;
	Element root;

	String closeHandlerId;

	class CloseHandler implements RequestListener {
		public void request(RequestContext context) throws Exception {
			setVisible(false);
		}
	}

	String selectButtonId;

	class SelectButtonHandler implements RequestListener {
		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);

			// validate other forms
			validate(context);

			// in read-only mode dont change selection
			if (isReadOnly())
				return;

			SelectionModel sm = getSelectionModel();
			if (isFirstSelectableRowSelected()) {
				sm.clear();
				sm.fireSelectionChanged(context);
			} else {
				sm.clear();
				TableModel tm = getModel();
				final int N = tm.getRowCount();
				for (int i = 0; i < N; i++) {
					TableRow row = tm.getRow(i);
					if (sm.isSelectable(row))
						sm.add(row);
				}
				sm.fireSelectionChanged(context);
			}
		}

		/**
		 * returns the state of the first row that is selectable
		 */
		boolean isFirstSelectableRowSelected() {
			SelectionModel sm = getSelectionModel();
			TableModel tm = getModel();
			final int N = tm.getRowCount();
			for (int i = 0; i < N; i++) {
				TableRow row = tm.getRow(i);
				if (sm.isSelectable(row)) {
					return sm.contains(row);
				}
			}
			return false;
		}
	}

	String gotoButtonId;
	String gotoInputId;

	class GotoButtonHandler implements RequestListener {
		public GotoButtonHandler() {
		}

		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);
			// validate other forms
			validate(context);
			try {
				int page = Integer.parseInt(context.getParameter(gotoInputId));
				pager.setCurrentPage(page - 1);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
	}

	String pageSizeButtonId;
	String pageSizeInputId;

	class PageSizeButtonHandler implements RequestListener {
		public PageSizeButtonHandler() {
		}

		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);
			// validate other forms
			validate(context);
			try {
				int ps = Integer
						.parseInt(context.getParameter(pageSizeInputId));
				if (ps < 1)
					ps = 1;
				pager.setPageSize(ps);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
	}

	TableModelChangeListener changeListener = new TableModelChangeListener() {
		public void tableModelChanged(TableModelChangeEvent event) {
			if (event.isIdentityChanged()) {
				setModel(getModel());
			}
		}
	};

	public TableComponent(String id, Component parent, TableModel model) {
		super(id, parent);
		selectionMgr = new SelectionMgr(getDispatcher(), this);
		closeHandlerId = getId() + ".close";
		selectButtonId = getId() + ".select";
		gotoButtonId = getId() + ".goto.button";
		gotoInputId = getId() + ".goto.input";
		pageSizeButtonId = getId() + ".pageSize.button";
		pageSizeInputId = getId() + ".pageSize.input";
		getDispatcher().addRequestListener(closeHandlerId, null,
				new CloseHandler());
		getDispatcher().addRequestListener(selectButtonId, null,
				new SelectButtonHandler());
		getDispatcher().addRequestListener(gotoButtonId, null,
				new GotoButtonHandler());
		getDispatcher().addRequestListener(pageSizeButtonId, null,
				new PageSizeButtonHandler());
		getDispatcher().addRequestListener(null, null, dispatcher);

		// default chain for JSP scripting
		sorter.setDecoree(model);
		pager.setDecoree(sorter);

		setModel(model);
	}

	/**
	 * creates a Table component that is initially hidden. It must be assigned a
	 * TableModel before it may set visible or used in another way
	 * 
	 * @param id
	 *            - id of session attribute
	 * @see #setModel
	 */
	public TableComponent(String id, Component parent) {
		super(id, parent);
		this.tableModel = null;
		super.setVisible(false);
		selectionMgr = new SelectionMgr(getDispatcher(), this);
	}

	public void initialize(RequestContext context) throws Exception {
		super.initialize(context);
		this.resources = context.getResources(TableComponent.class);
	}

	/** column metadata */
	public TableColumn getTableColumn(int columnIndex) {
		return tableColumns[columnIndex];
	}

	/** column metadata */
	public void setTableColumn(int columnIndex, TableColumn tableColumn) {
		tableColumns[columnIndex] = tableColumn;
	}

	/** set the data model and initializes everything */
	public void setModel(TableModel newModel) {
		if (tableModel != null)
			tableModel.removeTableModelChangeListener(changeListener);

		tableModel = newModel;
		tableColumns = new TableColumn[tableModel.getColumnCount()];
		for (int i = 0; i < tableColumns.length; i++)
			tableColumns[i] = new TableColumn(i);
		if (tableColumns.length > 0)
			comparator.setColumn(tableColumns[0]);
		else
			comparator.setColumn(new TableColumn(0));
		tableModel.addTableModelChangeListener(changeListener);
		getSelectionModel().clear();
	}

	/** the underlying data model w/o sort/paging decorators */
	public TableModel getModel() {
		return tableModel;
	}

	/** the current selection */
	public void setSelectionModel(SelectionModel selectionModel) {
		selectionMgr.setSelectionModel(selectionModel);
	}

	/** the current selection */
	public SelectionModel getSelectionModel() {
		return selectionMgr.getSelectionModel();
	}

	/** allows to enable/disable sorting of columns */
	public void setSortable(boolean newSortable) {
		sortable = newSortable;
	}

	/** allows to enable/disable sorting of columns */
	public boolean isSortable() {
		return sortable;
	}

	/** allows to enable/disable paging of rows */
	public void setPageable(boolean newPageable) {
		pageable = newPageable;
	}

	/** allows to enable/disable paging of rows */
	public boolean isPageable() {
		return pageable;
	}

	/** set the current sort column */
	public void setSortColumnIndex(int index) {
		comparator.setColumn(tableColumns[index]);
	}

	public RowComparator getRowComparator() {
		return comparator;
	}

	public int getPageSize() {
		return pager.getPageSize();
	}

	public void setPageSize(int newPageSize) {
		pager.setPageSize(newPageSize);
	}

	public int getCurrentPage() {
		return pager.getCurrentPage();
	}

	public void setCurrentPage(int newCurrentPage) {
		pager.setCurrentPage(newCurrentPage);
	}

	public int getPageCount() {
		return pager.getPageCount();
	}

	public Element render(RequestContext context, Document factory)
			throws Exception {
		startRendering(context);

		dispatcher.clear();

		this.factory = factory;
		this.root = factory.createElement("xtable-component");

		// build decorator chain
		TableModel model = this.getModel();
		if (isSortable()) {
			sorter.setDecoree(model);
			model = sorter;
		}
		if (isPageable()) {
			pager.setDecoree(model);
			model = pager;
		}

		if (isSortable())
			sorter.sort(getRowComparator());
		renderHeading(model, context);
		renderBody(model, context);
		if (isPageable())
			renderPager(model, context);

		// add title
		if (model.getTitle() != null && model.getTitle().length() > 0)
			root.setAttribute("title", model.getTitle());
		if (isClosable())
			root.setAttribute("closeId", closeHandlerId);
		if (error != null)
			root.setAttribute("error", error);

		if (border != null)
			root.setAttribute("border", border);
		if (width != null)
			root.setAttribute("width", width);
		if (renderId != null)
			root.setAttribute("renderId", renderId);

		stopRendering();

		return root;
	}

	private void startRendering(RequestContext context) {
		selectionMgr.startRendering(context);
		for (int i = 0; i < tableColumns.length; i++) {
			CellRenderer r = tableColumns[i].getCellRenderer();
			if (r instanceof RenderListener)
				((RenderListener) r).startRendering(context);
		}
	}

	private void stopRendering() {
		selectionMgr.stopRendering();
		for (int i = 0; i < tableColumns.length; i++) {
			CellRenderer r = tableColumns[i].getCellRenderer();
			if (r instanceof RenderListener)
				((RenderListener) r).stopRendering();
		}
	}

	void renderHeading(TableModel model, RequestContext context)
			throws FormatException {
		int visibleColumns = 0;

		// heading
		Element tr = tr();

		// extra column for selection
		int selMode = getSelectionModel().getMode();
		if (selMode != SelectionModel.NO_SELECTION) {
			Element th = th(tr);
			visibleColumns += 1;
			if (selMode == SelectionModel.MULTIPLE_SELECTION)
				th.setAttribute("selectId", selectButtonId);
			else
				text(th, "\u00a0"); // &nbsp;
		}

		// data columns
		int N = model.getColumnCount();
		for (int i = 0; i < N; i++) {
			TableColumn tc = getTableColumn(i);
			int columnIndex = tc.getColumnIndex();
			if (!tc.isHidden()) {
				Element th = th(tr);
				th.setAttribute("id", DomUtils.randomId());
				if (isSortable() && tc.isSortable()) {
					StringBuffer img = new StringBuffer();

					// here the letters "d" (descending) and "a" (ascending) are
					// swapped
					// this corresponds to the images
					img.append(tc.isDescending() ? "a" : "d");

					img.append(getRowComparator().getColumnIndex() == columnIndex ? "c"
							: "n");
					th.setAttribute("sort", img.toString());
					RequestListener h = new SortButtonHandler(tc);
					dispatcher.addRequestListener(th.getAttribute("id"), null,
							h);
				}
				text(th, model.getColumnTitle(columnIndex));
				visibleColumns += 1;
			}
		}
		root.setAttribute("visibleColumns", "" + visibleColumns);

		if (!colHeaders)
			root.removeChild(tr);
	}

	void renderBody(TableModel model, RequestContext context)
			throws FormatException {
		int rowCount = model.getRowCount();
		int colCount = model.getColumnCount();
		int selectionMode = getSelectionModel().getMode();

		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			Element tr = tr();
			TableRow row = model.getRow(rowIndex);

			// extra column if selectable
			if (selectionMode != SelectionModel.NO_SELECTION) {
				Element td = td(tr);
				selectionMgr.renderButton(td, row);
			}

			for (int i = 0; i < colCount; i++) {
				TableColumn tc = getTableColumn(i);
				if (!tc.isHidden()) {
					int columnIndex = tc.getColumnIndex();
					Element td = td(tr);
					CellRenderer renderer = tc.getCellRenderer();
					renderer.render(context, td, row.getValue(columnIndex));
				}
			}
		}
	}

	void renderPager(TableModel model, RequestContext context) {
		int pageCount = pager.getPageCount();

		if (model.getRowCount() < 2)
			return;

		Element tr = tr();
		Element td = td(tr);

		// comupte colspan
		int visibleColumns = countVisibleColumns(model);
		td.setAttribute("colspan", Integer.toString(visibleColumns));

		int currentPage = pager.getCurrentPage();

		// add back buttons
		if (currentPage > 0) {
			DomUtils.appendText(td, "\u00a0");
			addPageLink(td, 0, "first");
			addPageLink(td, currentPage - 1, "prev");
		}

		// add "Page 1/5"
		DomUtils.appendText(td, "\u00a0");
		Object[] args = new Object[2];
		args[0] = new Integer(currentPage + 1); // zero based
		args[1] = new Integer(pageCount);
		text(td, resources.getString("wcf.table.pages", args));

		// add forward buttons
		if (currentPage < pageCount - 1) {
			DomUtils.appendText(td, "\u00a0");
			addPageLink(td, currentPage + 1, "next");
			addPageLink(td, pageCount - 1, "last");
		}

		// add "Goto Page"
		if (pageCount > 1) {
			DomUtils.appendText(td, "\u00a0");
			Element e = DomUtils.appendElement(td, "xgotopage");
			String label = resources.getString("wcf.table.goto.label");
			e.setAttribute("label", label);
			e.setAttribute("buttonId", gotoButtonId);
			e.setAttribute("inputId", gotoInputId);
			e.setAttribute("value", "" + (currentPage + 1));
		}

		// add "Rows / Page"
		DomUtils.appendText(td, "\u00a0");
		Element e = DomUtils.appendElement(td, "xgotopage");
		String label = resources.getString("wcf.table.rowcnt.label");
		e.setAttribute("label", label);
		e.setAttribute("buttonId", pageSizeButtonId);
		e.setAttribute("inputId", pageSizeInputId);
		e.setAttribute("value", "" + pager.getPageSize());
	}

	private int countVisibleColumns(TableModel model) {
		int visibleColumns = 0;
		if (getSelectionModel().getMode() != SelectionModel.NO_SELECTION)
			visibleColumns = 1;
		int N = model.getColumnCount();
		for (int i = 0; i < N; i++)
			if (!getTableColumn(i).isHidden())
				visibleColumns += 1;
		return visibleColumns;
	}

	void addPageLink(Element td, int page, String direction) {
		Element pg = factory.createElement("xpagenav");
		td.appendChild(pg);
		String id = DomUtils.randomId();
		pg.setAttribute("id", id);
		pg.setAttribute("page", Integer.toString(page + 1));
		pg.setAttribute("direction", direction);
		RequestListener h = new PageHandler(page);
		dispatcher.addRequestListener(id, null, h);
	}

	class PageHandler implements RequestListener {
		int page;

		public PageHandler(int page) {
			this.page = page;
		}

		public void request(RequestContext context) throws Exception {
			Scroller.enableScroller(context);

			// validate other forms
			validate(context);
			// update pageno
			pager.setCurrentPage(page);
		}
	}

	private class SortButtonHandler implements RequestListener {
		TableColumn tc;

		SortButtonHandler(TableColumn tc) {
			this.tc = tc;
		}

		public void request(RequestContext context) throws Exception {
			// Keep scroll position
			Scroller.enableScroller(context);
			// validate other forms
			validate(context);
			// change sort direction
			if (tc.getColumnIndex() == comparator.getColumnIndex())
				tc.setDescending(!tc.isDescending());
			else
				comparator.setColumn(tc);
			sorter.sort(getRowComparator());
		}
	}

	/**
	 * shall this table have a close button?
	 */
	public boolean isClosable() {
		return closable;
	}

	/**
	 * shall this table have a close button?
	 */
	public void setClosable(boolean b) {
		closable = b;
	}

	/**
	 * gets the border attribute of the generated table. Overrides the global
	 * stylesheet parameter "border".
	 * 
	 * @return the border attribute or null
	 */
	public String getBorder() {
		return border;
	}

	/**
	 * sets the border attribute of the generated table. Overrides the global
	 * stylesheet parameter "border".
	 * 
	 * @param border
	 *            the border attribute or null to use the stylesheet parameter
	 */
	public void setBorder(String border) {
		this.border = border;
	}

	/**
	 * sets the width attribute of the generated table.
	 */
	public void setWidth(String width) {
		this.width = width;
	}

	/**
	 * sets the renderId attribute of the generated table. Overrides the global
	 * stylesheet parameter "renderId".
	 * 
	 * @param renderId
	 *            the renderId attribute or null to use the stylesheet parameter
	 */
	public void setRenderId(String renderId) {
		this.renderId = renderId;
	}

	/**
	 * gets the renderId attribute of the generated table. Overrides the global
	 * stylesheet parameter "renderId".
	 * 
	 * @return the renderId attribute or null
	 */
	public String getRenderId() {
		return renderId;
	}

	Element tr() {
		Element xtr = factory.createElement(tr);
		root.appendChild(xtr);
		return xtr;
	}

	Element th(Element tr) {
		Element xth = factory.createElement(th);
		tr.appendChild(xth);
		return xth;
	}

	Element td(Element tr) {
		Element xtd = factory.createElement(td);
		tr.appendChild(xtd);
		return xtd;
	}

	void text(Element parent, String text) {
		Text t = factory.createTextNode(text);
		parent.appendChild(t);
	}

	private String tr = "xtr";
	private String td = "xtd";
	private String th = "xth";

	public void setTr(String newTr) {
		tr = newTr;
	}

	public String getTr() {
		return tr;
	}

	public void setTd(String newTd) {
		td = newTd;
	}

	public String getTd() {
		return td;
	}

	public void setTh(String newTh) {
		th = newTh;
	}

	public String getTh() {
		return th;
	}

	PagedTableModel getPager() {
		return pager;
	}

	public TableColumn[] getTableColumns() {
		return tableColumns;
	}

	/**
	 * sets the error message to display
	 * 
	 * @param message
	 *            the message to display or null to remove a previous error
	 *            message
	 */
	public void setError(String message) {
		this.error = message;
	}

	public boolean isColHeaders() {
		return colHeaders;
	}

	public void setColHeaders(boolean title) {
		this.colHeaders = title;
	}

	public boolean isReadOnly() {
		return selectionMgr.isReadOnly();
	}

	public void setReadOnly(boolean readOnly) {
		selectionMgr.setReadOnly(readOnly);
	}
}