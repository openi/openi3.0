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
package org.openi.wcf.convert;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.tonbeller.wcf.convert.ConvertException;
import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.FormatHandler;
import com.tonbeller.wcf.format.Formatter;
import com.tonbeller.wcf.ui.Item;
import com.tonbeller.wcf.ui.Select;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * @author av
 */
public abstract class SelectConverterBase extends NodeConverterBase {
	private static Logger logger = Logger.getLogger(SelectConverterBase.class);

	/**
	 * sets the selected attributes of the DOM item children of elem. calls
	 * updateModelReference if the list is valid (ie not disabled and form was
	 * submitted).
	 */
	public void convert(Formatter fmt, Map params, Map fileSource,
			Element elem, Object bean) throws ConvertException,
			FormatException, IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		// disabled = true? return
		if (Select.isDisabled(elem))
			return;

		String id = Select.getId(elem);

		// was this form submitted
		Object inputAvail = params.get(id + ".valid");
		if (inputAvail == null)
			return;

		// get the http values
		String[] values = (String[]) params.get(id);
		if (values == null)
			values = new String[0];

		DomUtils.removeAttribute(elem, "error");

		// set all items to unselected
		List items = Select.getItems(elem);
		for (Iterator it = items.iterator(); it.hasNext();)
			Item.setSelected((Element) it.next(), false);

		// select the list items
		List selected = selectListItems(elem, values, items);

		updateModelReference(fmt, elem, bean);
	}

	/**
	 * select the list items
	 */
	List selectListItems(Element elem, String[] values, List items)
			throws ConvertException {
		List selected = new ArrayList();
		for (int i = 0; i < values.length; i++) {
			String itemId = values[i];
			boolean found = false;
			search: for (Iterator it = items.iterator(); it.hasNext();) {
				Element item = (Element) it.next();
				if (Item.getId(item).equals(itemId)) {
					Item.setSelected(item, true);
					selected.add(item);
					found = true;
					break search;
				}
			}
			if (!found) {
				String mesg = "Item with id=\"" + itemId
						+ "\" not found in ListBox";
				showMissing(elem, mesg);
			}
		}
		return selected;
	}

	protected abstract void updateModelReference(Formatter fmt, Element elem,
			Object bean) throws FormatException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException;

	/**
	 * @see com.tonbeller.wcf.convert.NodeConverter#convert(Formatter, Object,
	 *      Element)
	 */
	public void convert(Formatter fmt, Object bean, Element elem)
			throws ConvertException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		// no model reference? nothing to do
		String modelReference = Select.getModelReference(elem);
		if (modelReference.length() == 0)
			return;

		// get a formatter
		String type = Select.getType(elem);
		String formatString = Select.getFormatString(elem);
		FormatHandler handler = fmt.getHandler(type);
		if (handler == null)
			throw new FormatException("no handler found for type: " + type);

		// retrieve values from the bean property
		Object o = PropertyUtils.getProperty(bean, modelReference);
		Object[] values = handler.toObjectArray(o);
		if (values == null)
			values = new Object[0];

		// deselect all items
		List items = Select.getItems(elem);
		for (Iterator it = items.iterator(); it.hasNext();)
			Item.setSelected((Element) it.next(), false);

		// for each value, find the corresponding item and select it
		for (int i = 0; i < values.length; i++) {
			Object beanValue = values[i];
			boolean found = false;
			search: for (Iterator it = items.iterator(); it.hasNext();) {
				Element item = (Element) it.next();
				Object itemValue = handler.parse(Item.getValue(item),
						formatString);
				if (itemValue.equals(beanValue)) {
					Item.setSelected(item, true);
					found = true;
					break search;
				}
			}
			if (!found) {
				String mesg = "No item has a value of \"" + beanValue + "\".";
				showMissing(elem, mesg);
			}
		}
	}

	private void showMissing(Element elem, String mesg) {
		boolean soft = "true".equals(elem.getAttribute("ignore-missing"));
		if (soft)
			logger.warn(mesg);
		else
			throw new ConvertException(mesg);
	}

}
