package org.openi.table;

import java.io.IOException;
import java.net.URL;

import org.openi.util.wcf.ObjectFactory;
import org.xml.sax.SAXException;

import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.table.TableComponent;

/**
 * creates a tablecomponent from xml configuration file
 * 
 * @author av
 * @author SUJEN
 */
public class TableComponentFactory {

	private TableComponentFactory() {
	}

	public static TableComponent instance(String id, URL configXml,
			OlapModel olapModel) throws IOException, SAXException {
		URL rulesXml = TableComponent.class.getResource("rules.xml");
		TableComponent table = (TableComponent) ObjectFactory.instance(
				rulesXml, configXml);
		table.setOlapModel(olapModel);
		table.setId(id);
		return table;
	}

}
