package org.openi.olap.xmla;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.navi.MdxQuery;
import com.tonbeller.wcf.format.FormatException;

/**
 * set user defined MDX Query String
 * @author SUJEN
 */
public class XMLA_MdxQuery extends ExtensionSupport implements MdxQuery {

	public XMLA_MdxQuery() {
		super.setId(MdxQuery.ID);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.MdxQuery#getMdxQuery()
	 */
	public String getMdxQuery() {
		XMLA_Model m = (XMLA_Model) getModel();
		return m.getCurrentMdx();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.MdxQuery#setMdxQuery(String)
	 */
	public void setMdxQuery(String mdxQuery) {
		try {
			XMLA_Model m = (XMLA_Model) getModel();
			if (mdxQuery.equals(m.getCurrentMdx()))
				return;
			m.setUserMdx(mdxQuery);
			m.fireModelChanged();
		} catch (OlapException e) {
			throw new FormatException(e.getMessage());
		}
	}

} // End XMLA_MdxQuery
