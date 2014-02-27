package org.openi.wcf.table;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;

import org.openi.util.plugin.PluginUtils;
import org.w3c.dom.Document;

import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.component.ComponentSupport;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestListener;
import com.tonbeller.wcf.form.FormDocument;
import com.tonbeller.wcf.selection.SelectionModel;
import com.tonbeller.wcf.table.TableModel;
import com.tonbeller.wcf.utils.ResourceLocator;
import com.tonbeller.wcf.utils.SoftException;
import com.tonbeller.wcf.utils.XmlUtils;

/**
 * a component that combines a table and its property form in a single
 * component. The user may switch between them via the edit button.
 * 
 * @author av
 */
public class EditableTableComponent extends ComponentSupport implements
		ITableComponent {
	TableComponent tableComp;
	TablePropertiesFormComponent formComp;
	String editButtonId;
	boolean editable = true;

	/**
	 * creates an editable table component.
	 * 
	 * @param id
	 * @param tableComp
	 * @param formComp
	 *            - form for editing the table properties
	 */
	public EditableTableComponent(String id, Component parent,
			TableComponent tableComp, TablePropertiesFormComponent formComp) {
		super(id, parent);
		this.tableComp = tableComp;
		this.formComp = formComp;
		tableComp.setParent(this);
		formComp.setParent(this);

		// this is a little sloppy, because both components
		// are validated although only one can be visible
		addFormListener(tableComp);
		addFormListener(formComp);

		editButtonId = id + ".edit";
		getDispatcher().addRequestListener(editButtonId, null,
				editButtonListener);
	}

	public static EditableTableComponent instance(RequestContext context,
			String id, TableComponent table) {
		Locale locale = context.getLocale();
		ResourceBundle resb = ResourceBundle.getBundle(
				"com.tonbeller.wcf.table.resources", locale);
		String path = PluginUtils.getPluginDir()
				+ "/ui/resources/wcf/tableproperties.xml";
		
		/*
		URL url = null;
		try {
		//	url = ResourceLocator.getResource(context.getServletContext(),
					//locale, path);
			url = context.getServletContext().getResource(path);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SoftException(e);
		}
		*/
		//Document doc = XmlUtils.parse(url);
		Document doc = null;
		try {
			doc = XmlUtils.parse(new File(path).toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		// In replaceI18n(...) wird geprüft, ob "bundle"-Attribut vorhanden
		FormDocument.replaceI18n(context, doc, null);

		TablePropertiesFormComponent formComp = new TablePropertiesFormComponent(
				id + ".form", null, doc, table);
		formComp.setVisible(false);
		formComp.setCloseable(true);
		return new EditableTableComponent(id, null, table, formComp);
	}

	RequestListener editButtonListener = new RequestListener() {
		public void request(RequestContext context) throws Exception {
			tableComp.validate(context);
			formComp.setVisible(true);
		}
	};

	public void initialize(RequestContext context) throws Exception {
		super.initialize(context);
		tableComp.initialize(context);
		formComp.initialize(context);
	}

	public void destroy(HttpSession session) throws Exception {
		formComp.destroy(session);
		tableComp.destroy(session);
		super.destroy(session);
	}

	public Document render(RequestContext context) throws Exception {
		if (isEditFormVisible())
			return formComp.render(context);

		Document doc = tableComp.render(context);
		if (editable)
			doc.getDocumentElement().setAttribute("editId", editButtonId);
		return doc;
	}

	public boolean isVisible() {
		return tableComp.isVisible();
	}

	public void setVisible(boolean b) {
		tableComp.setVisible(b);
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean b) {
		editable = b;
	}

	public boolean isEditFormVisible() {
		return formComp.isVisible();
	}

	/**
	 * @return
	 */
	public String getBorder() {
		return tableComp.getBorder();
	}

	/**
	 * @return
	 */
	public int getCurrentPage() {
		return tableComp.getCurrentPage();
	}

	/**
	 * @return
	 */
	public TableModel getModel() {
		return tableComp.getModel();
	}

	/**
	 * @return
	 */
	public int getPageCount() {
		return tableComp.getPageCount();
	}

	/**
	 * @return
	 */
	public int getPageSize() {
		return tableComp.getPageSize();
	}

	/**
	 * @return
	 */
	public String getRenderId() {
		return tableComp.getRenderId();
	}

	/**
	 * @return
	 */
	public RowComparator getRowComparator() {
		return tableComp.getRowComparator();
	}

	/**
	 * @return
	 */
	public SelectionModel getSelectionModel() {
		return tableComp.getSelectionModel();
	}

	/**
	 * @return
	 */
	public boolean isClosable() {
		return tableComp.isClosable();
	}

	/**
	 * @return
	 */
	public boolean isPageable() {
		return tableComp.isPageable();
	}

	/**
	 * @return
	 */
	public boolean isSortable() {
		return tableComp.isSortable();
	}

	/**
	 * @param border
	 */
	public void setBorder(String border) {
		tableComp.setBorder(border);
	}

	/**
	 * @param b
	 */
	public void setClosable(boolean b) {
		tableComp.setClosable(b);
	}

	/**
	 * @param newCurrentPage
	 */
	public void setCurrentPage(int newCurrentPage) {
		tableComp.setCurrentPage(newCurrentPage);
	}

	/**
	 * @param message
	 */
	public void setError(String message) {
		tableComp.setError(message);
	}

	/**
	 * @param newModel
	 */
	public void setModel(TableModel newModel) {
		tableComp.setModel(newModel);
		formComp.columnTreeModelChanged();
	}

	/**
	 * @param newPageable
	 */
	public void setPageable(boolean newPageable) {
		tableComp.setPageable(newPageable);
	}

	/**
	 * @param newPageSize
	 */
	public void setPageSize(int newPageSize) {
		tableComp.setPageSize(newPageSize);
	}

	/**
	 * @param renderId
	 */
	public void setRenderId(String renderId) {
		tableComp.setRenderId(renderId);
	}

	/**
	 * @param selectionModel
	 */
	public void setSelectionModel(SelectionModel selectionModel) {
		tableComp.setSelectionModel(selectionModel);
	}

	/**
	 * @param newSortable
	 */
	public void setSortable(boolean newSortable) {
		tableComp.setSortable(newSortable);
	}

	/**
	 * @param index
	 */
	public void setSortColumnIndex(int index) {
		tableComp.setSortColumnIndex(index);
	}

	/**
	 * @return Returns the tableComp.
	 */
	public TableComponent getTableComp() {
		return tableComp;
	}

	public boolean isReadOnly() {
		return tableComp.isReadOnly();
	}

	public void setReadOnly(boolean readOnly) {
		tableComp.setReadOnly(readOnly);
	}
}
