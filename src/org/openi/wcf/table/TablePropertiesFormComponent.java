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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.form.FormComponent;
import com.tonbeller.wcf.selection.AbstractSelectionModel;
import com.tonbeller.wcf.table.TableColumn;
import com.tonbeller.wcf.tree.AbstractTreeModel;
import com.tonbeller.wcf.tree.MutableTreeModelDecorator;
import com.tonbeller.wcf.tree.NodeRenderer;
import com.tonbeller.wcf.tree.TreeComponent;
import com.tonbeller.wcf.tree.TreeHandler;
import com.tonbeller.wcf.tree.TreeModel;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * A form that allows the user to change the properties of a table component.
 * 
 * @author av
 */
public class TablePropertiesFormComponent extends FormComponent {

	private Element closeElement;
	private TableComponent tableComp;
	private Element titleElement;
	private Resources resources;
	private TreeModel mutableTreeModel;
	private TreeModel columnTreeModel;
	private TreeComponent treeComp;

	private static Logger logger = Logger
			.getLogger(TablePropertiesFormComponent.class);

	public TablePropertiesFormComponent(String id, Component parent,
			Document document, TableComponent table) {
		super(id, parent, document);
		this.tableComp = table;
		columnTreeModel = new TableColumnTreeModel();
		mutableTreeModel = new MutableTreeModelDecorator(columnTreeModel);
		try {
			DOMXPath dx = new DOMXPath("//title");
			titleElement = (Element) dx.selectSingleNode(getDocument());
			dx = new DOMXPath("./imgButton");
			closeElement = (Element) dx.selectSingleNode(titleElement);
		} catch (JaxenException e) {
			logger.error("?", e);
		}
	}

	public void initialize(RequestContext context) throws Exception {
		super.initialize(context);
		this.resources = context
				.getResources(TablePropertiesFormComponent.class);

		try {
			DOMXPath dx = new DOMXPath("//skip[@id='" + getId() + ".tree']");
			Element te = (Element) dx.selectSingleNode(getDocument());
			TreeHandler th = (TreeHandler) getHandler(te);
			treeComp = th.getTree();
			treeComp.setNodeRenderer(new TableColumnNodeRenderer());
			treeComp.setSelectionModel(new TableColumnSelectionModel());
		} catch (JaxenException e) {
			logger.error(null, e);
		}

		String title = tableComp.getModel().getTitle();
		if (title != null)
			title = resources.getString("wcf.table.props.title.args", title);
		else
			title = resources.getString("wcf.table.props.title.noargs");
		setTitle(title);
	}

	/**
	 * called when the user presses OK
	 */
	public void onApply(RequestContext context) throws Exception {
		if (isCloseable())
			setVisible(false);
	}

	/**
	 * called when the user presses Cancel
	 */
	public void onCancel(RequestContext context) throws Exception {
		if (isCloseable())
			setVisible(false);
	}

	public TreeModel getColumnTreeModel() {
		return mutableTreeModel;
	}

	private class TableColumnTreeModel extends AbstractTreeModel {
		public Object[] getRoots() {
			return tableComp.getTableColumns();
		}

		public boolean hasChildren(Object node) {
			return false;
		}

		public Object[] getChildren(Object node) {
			return null;
		}

		public Object getParent(Object node) {
			return null;
		}
	}

	/**
	 * renders a tree node (i.e. a TableColumn)
	 */
	private class TableColumnNodeRenderer implements NodeRenderer {
		public Element renderNode(RequestContext context, Document factory,
				Object node) {
			int colIndex = ((TableColumn) node).getColumnIndex();
			String label = tableComp.getModel().getColumnTitle(colIndex);
			Element nodeElem = factory.createElement(DEFAULT_NODE_ELEMENT_NAME);
			nodeElem.setAttribute("label", label);
			return nodeElem;
		}
	}

	private class TableColumnSelectionModel extends AbstractSelectionModel {
		public TableColumnSelectionModel() {
			super(MULTIPLE_SELECTION);
		}

		public Set getSelection() {
			Set set = new HashSet();
			TableColumn[] cols = tableComp.getTableColumns();
			for (int i = 0; i < cols.length; i++) {
				if (!cols[i].isHidden())
					set.add(cols[i]);
			}
			return set;
		}

		public void add(Object obj) {
			((TableColumn) obj).setHidden(false);
		}

		public void remove(Object obj) {
			((TableColumn) obj).setHidden(true);
		}

		public void clear() {
			TableColumn[] cols = tableComp.getTableColumns();
			for (int i = 0; i < cols.length; i++)
				cols[i].setHidden(true);
		}
	}

	/**
	 * used by xml form
	 */
	public TableComponent getTable() {
		return tableComp;
	}

	public String getTitle() {
		return titleElement.getAttribute("value");
	}

	public void setTitle(String title) {
		titleElement.setAttribute("value", title);
	}

	/**
	 * @return
	 */
	public boolean isCloseable() {
		return !closeElement.hasAttribute("hidden");
	}

	public void setCloseable(boolean b) {
		if (b)
			DomUtils.removeAttribute(closeElement, "hidden");
		else
			closeElement.setAttribute("hidden", "true");
	}

	void columnTreeModelChanged() {
		columnTreeModel.fireModelChanged(true);
	}
}
