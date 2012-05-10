package org.openi.olap.mondrian;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.navi.MdxQuery;
import com.tonbeller.wcf.format.FormatException;

/**
 * 
 * @author av
 */
public class MondrianMdxQuery extends ExtensionSupport implements MdxQuery {

	public MondrianMdxQuery() {
		super.setId(MdxQuery.ID);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.MdxQuery#getMdxQuery()
	 */
	public String getMdxQuery() {
		MondrianModel m = (MondrianModel) getModel();
		return m.getCurrentMdx();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.MdxQuery#setMdxQuery(String)
	 */
	public void setMdxQuery(String mdxQuery) {
		try {
			MondrianModel m = (MondrianModel) getModel();
			if (m.setUserMdx(mdxQuery))
				m.fireModelChanged(); // only if changed
		} catch (OlapException e) {
			throw new FormatException(e.getMessage());
		}
	}

} // End MondrianMdxQuery
