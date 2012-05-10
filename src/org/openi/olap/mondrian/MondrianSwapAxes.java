package org.openi.olap.mondrian;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.navi.SwapAxes;

/**
 * @author hh
 * @author SUJEN
 * 
 *         Implementation of the Swap Axes Extension for Mondrian Data Source.
 */
public class MondrianSwapAxes extends ExtensionSupport implements SwapAxes {

	/**
	 * Constructor sets ID
	 */
	public MondrianSwapAxes() {
		super.setId(SwapAxes.ID);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.SwapAxes#canSwapAxes()
	 * @return true, if the Mondrian Query exists and has two axes
	 */
	public boolean canSwapAxes() {
		MondrianModel model = (MondrianModel) getModel();
		mondrian.olap.Query monQuery = ((MondrianQueryAdapter) model
				.getQueryAdapter()).getMonQuery();
		if (monQuery != null)
			return (monQuery.getAxes().length == 2);
		else
			return false;
	}

	public void setSwapAxes(boolean swap) {
		MondrianModel model = (MondrianModel) getModel();
		((MondrianQueryAdapter) model.getQueryAdapter()).setSwapAxes(swap);
	}

	public boolean isSwapAxes() {
		MondrianModel model = (MondrianModel) getModel();
		return ((MondrianQueryAdapter) model.getQueryAdapter()).isSwapAxes();
	}

}
