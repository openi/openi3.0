package org.openi.olap.xmla;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.mdxparse.ParsedQuery;
import com.tonbeller.jpivot.olap.mdxparse.QueryAxis;
import com.tonbeller.jpivot.olap.navi.NonEmpty;

/**
 * Non Empty extension
 * @author SUJEN
 */
public class XMLA_NonEmpty extends ExtensionSupport implements NonEmpty {

	/**
	 * Constructor sets ID
	 */
	public XMLA_NonEmpty() {
		super.setId(NonEmpty.ID);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.NonEmpty#isNonEmpty()
	 */
	public boolean isNonEmpty() {

		XMLA_Model m = (XMLA_Model) getModel();
		XMLA_QueryAdapter adapter = (XMLA_QueryAdapter) m.getQueryAdapter();
		ParsedQuery pQuery = adapter.getParsedQuery();

		// loop over query axes
		// say yes if all axes have the nonEmpty flag
		// say no. if there is an axis w/o NON EMPTY

		QueryAxis[] qAxes = pQuery.getAxes();
		for (int i = 0; i < qAxes.length; i++) {
			QueryAxis qAxis = qAxes[i];
			if (!qAxis.isNonEmpty())
				return false;
		}
		return true; // all axes NON EMPTY so far

	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.NonEmpty#setNonEmpty(boolean)
	 */
	public void setNonEmpty(boolean nonEmpty) {

		XMLA_Model m = (XMLA_Model) getModel();
		XMLA_QueryAdapter adapter = (XMLA_QueryAdapter) m.getQueryAdapter();
		ParsedQuery pQuery = adapter.getParsedQuery();

		// loop over query axes
		// set the nonEmpty flag, for all axes,

		boolean bChange = false;
		QueryAxis[] qAxes = pQuery.getAxes();
		for (int i = 0; i < qAxes.length; i++) {
			QueryAxis qAxis = qAxes[i];
			if (qAxis.isNonEmpty() != nonEmpty) {
				qAxis.setNonEmpty(nonEmpty);
				bChange = true;
			}
		}

		if (bChange)
			m.fireModelChanged();

	}

} // End XMLA_NonEmpty
