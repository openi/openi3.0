package org.openi.navigator.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.tonbeller.jpivot.core.ModelChangeEvent;
import com.tonbeller.jpivot.core.ModelChangeListener;
import com.tonbeller.jpivot.olap.model.MemberPropertyMeta;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.table.TableComponent;
import com.tonbeller.jpivot.table.span.PropertySpanBuilder;
import com.tonbeller.tbutils.res.Resources;
import com.tonbeller.wcf.catedit.CategoryEditor;
import com.tonbeller.wcf.catedit.CategoryModelSupport;
import com.tonbeller.wcf.catedit.CategorySupport;
import com.tonbeller.wcf.catedit.Item;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.controller.Dispatcher;
import com.tonbeller.wcf.controller.DispatcherSupport;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestListener;

/**
 *
 * Select Properties dialog
 *
 * @author wawan
 *
 */
public class SelectProperties extends CategoryEditor implements ModelChangeListener {

	private void sortCategory() {
		if(categorySupportVisible == null) return;
		List items = categorySupportVisible.getItems();

		int scopeSize = scopeMap.size();
		List[] tempItems = new List[scopeSize];
		for (int i = 0; i < scopeSize; i++) {
			tempItems[i] = new ArrayList();
		}
		for (Iterator iterator = items.iterator(); iterator.hasNext();) {
			MpmItem mpmItem = (MpmItem) iterator.next();
			Integer index = (Integer)scopeMap.get(mpmItem.getMpm().getScope());
			if(index != null)
				tempItems[index.intValue()].add(mpmItem);
		}
		//convert tempItems to flat list
		items = new ArrayList();
		for (int i = 0; i < scopeSize; i++) {
			for (Iterator iterator = tempItems[i].iterator(); iterator.hasNext();) {
				items.add(iterator.next());
			}
		}

		categorySupportVisible.setItems(items);
	}

	public class CancelHandler implements RequestListener {
		private boolean hide;

		public CancelHandler(boolean hide) {
			this.hide = hide;
		}

		public void request(RequestContext context) throws Exception {
			editing = false;
			// we will recreate everything on the next render()
			revert(context);
			if (hide)
				setVisible(false);
		}
	}

	public class OkHandler implements RequestListener {
		private boolean hide;

		public OkHandler(boolean hide) {
			this.hide = hide;
		}

		public void request(RequestContext context) throws Exception {
			editing = false;
			boolean valid = validate(context);

			CategorySupport cs = (CategorySupport) categories.get(0);
			if(cs.getItems().size()==0)
				visiblePropertyMetas = null;
			else {
				List items = cs.getItems();
				visiblePropertyMetas = new ArrayList();
				for (Iterator iterator = items.iterator(); iterator.hasNext();) {
					MpmItem mpmItem = (MpmItem) iterator.next();
					visiblePropertyMetas.add(mpmItem.getMpm());
				}
			}

			propertyConfig.setVisiblePropertyMetas(visiblePropertyMetas);

			if (valid && hide)
				setVisible(false);
		}
	}

	class MpmItem implements Item {

		String label;
		MemberPropertyMeta mpm;

		public MpmItem(MemberPropertyMeta mpm) {
			super();
			this.label = mpm.getScope() + " [ " + mpm.getLabel() + " ]";
			this.mpm = mpm;
		}

		public MemberPropertyMeta getMpm() {
			return mpm;
		}

		public String getLabel() {
			return label;
		}

		public boolean isMovable() {
			return true;
		}

	}

	private String acceptButtonId;

	private String cancelButtonId;

	private RequestListener acceptHandler;

	private RequestListener revertHandler;

	private String okButtonId;

	private String revertButtonId;

	private List categories = new ArrayList();

	private Resources resources;

	/**
	 * after the user has started editing, the CategoryModel is no longer
	 * synchronized with the OlapModel. This means, the user may do one or more
	 * changes, and then apply these changes at once to the OlapModel.
	 */
	private boolean editing = false;

	private TableComponent tableComponent;

	private CategoryModelSupport categoryModel;

	private Dispatcher tempDispatcher = new DispatcherSupport();

	private static Logger logger = Logger.getLogger(SelectProperties.class);

	private PropertySpanBuilder propertyConfig;

	private List visiblePropertyMetas = null;

	private List[] availablePropertiesColumns;

	private CategorySupport categorySupportVisible;

	private CategorySupport categorySupportAvailable;

	private Map scopeMap;

	/**
	 * Constructor for SelectProperties.
	 */
	public SelectProperties(String id, Component parent, TableComponent tableComponent) {
		super(id, parent);

		logger.info("creating instance: " + this);

		acceptButtonId = id + ".accept";
		cancelButtonId = id + ".cancel";
		okButtonId = id + ".ok";
		revertButtonId = id + ".revert";

		this.tableComponent = tableComponent;
		OlapModel olapModel = tableComponent.getOlapModel();
		olapModel.addModelChangeListener(this);

		acceptHandler = new OkHandler(false);
		revertHandler = new CancelHandler(false);
		super.getDispatcher().addRequestListener(acceptButtonId, null, acceptHandler);
		super.getDispatcher().addRequestListener(revertButtonId, null, revertHandler);
		super.getDispatcher().addRequestListener(okButtonId, null, new OkHandler(true));
		super.getDispatcher().addRequestListener(cancelButtonId, null, new CancelHandler(true));
		super.getDispatcher().addRequestListener(null, null, tempDispatcher);
		categoryModel = new CategoryModelSupport() {
			public List getCategories() {
				return categories;
			}
		};
		super.setModel(categoryModel);
	}

	public void initialize(RequestContext context) throws Exception {
		super.initialize(context);
		resources = context.getResources(SelectProperties.class);
		// setCutPasteMode(true);
	}

	/**
	 * Returns the tempDispatcher.
	 *
	 * @return Dispatcher
	 */
	Dispatcher getTempDispatcher() {
		return tempDispatcher;
	}

	/**
	 * initializes the CategoryModel to reflect the propertyColumns
	 */
	void initializeCategories() throws OlapException {
		logger.info("SelectProperties.initializeCategories()");
		categories.clear();

		scopeMap = new HashMap();
		categorySupportVisible = new CategorySupport(resources.getString("properties.visible"), "cat-visible.png");
		categorySupportAvailable = new CategorySupport(resources.getString("properties.available"), "cat-available.png");
		categories.add(categorySupportVisible);
		categories.add(categorySupportAvailable);

		propertyConfig = (PropertySpanBuilder) tableComponent.getPropertyConfig();
		visiblePropertyMetas = propertyConfig.getVisiblePropertyMetas();

		availablePropertiesColumns = propertyConfig.getAvailablePropertiesColumns();
		if(availablePropertiesColumns == null) {
			return;
		}

		List itemListVisible = new ArrayList();
		List itemListAvailable = new ArrayList();

		if (visiblePropertyMetas != null) {
			for (Iterator iterator = visiblePropertyMetas.iterator(); iterator.hasNext();) {
				MemberPropertyMeta mpm = (MemberPropertyMeta) iterator.next();
				itemListVisible.add(new MpmItem(mpm));
			}
		}

		int index = 0;
		for (int i = 0; i < availablePropertiesColumns.length; i++) {
			for (Iterator it = availablePropertiesColumns[i].iterator(); it.hasNext();) {
				MemberPropertyMeta mpm = (MemberPropertyMeta) it.next();
				if(!scopeMap.containsKey(mpm.getScope()))
					scopeMap.put(mpm.getScope(), new Integer(index++));

				MpmItem mpmItem = new MpmItem(mpm);
				if(visiblePropertyMetas == null) {
					itemListVisible.add(mpmItem);
				} else if(!existsInList(visiblePropertyMetas, mpmItem)) {
					itemListAvailable.add(mpmItem);
				}
			}
		}

		categorySupportVisible.setEmptyAllowed(true);
		categorySupportVisible.setOrderSignificant(true);
		categorySupportVisible.setItems(itemListVisible);

		categorySupportAvailable.setEmptyAllowed(true);
		categorySupportAvailable.setOrderSignificant(true);
		categorySupportAvailable.setItems(itemListAvailable);
	}

	private boolean existsInList(List visiblePropertyMetas, MpmItem mpmItem) {
		if(visiblePropertyMetas == null) return false;

		for (Iterator iterator = visiblePropertyMetas.iterator(); iterator.hasNext();) {
			MemberPropertyMeta mpm = (MemberPropertyMeta) iterator.next();
			String tempString = mpm.getScope() + " [ " + mpm.getLabel() + " ]";
			if(tempString.equals(mpmItem.getLabel()))
				return true;
		}
		return false;
	}

	/**
	 * @return boolean
	 */
	public boolean isEditing() {
		return editing;
	}

	public void setEditing(boolean editing) {
		this.editing = editing;
	}

	public Element render(RequestContext context, Document factory) throws Exception {
		logger.info("SelectProperties.render()");
		if (!editing) {
			tempDispatcher.clear();
			initializeCategories();
			editing = true;
		}

		sortCategory();

		Element elem = super.render(context, factory);

		elem.setAttribute("accept-id", acceptButtonId);
		elem.setAttribute("accept-title", resources.getString("accept.title"));
		elem.setAttribute("revert-id", revertButtonId);
		elem.setAttribute("revert-title", resources.getString("revert.title"));
		elem.setAttribute("ok-id", okButtonId);
		elem.setAttribute("ok-title", resources.getString("ok.title"));
		elem.setAttribute("cancel-id", cancelButtonId);
		elem.setAttribute("cancel-title", resources.getString("cancel.title"));

		return elem;
	}

	public void modelChanged(ModelChangeEvent e) {
		editing = false;
	    categories.clear();
	    // invalidate hyperlinks
	    categoryModel.fireModelChanged();
	    setVisible(false);
	}

	public void structureChanged(ModelChangeEvent e) { 
	    modelChanged(e);
	}

}