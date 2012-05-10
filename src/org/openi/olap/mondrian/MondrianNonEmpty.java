package org.openi.olap.mondrian;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.navi.NonEmpty;

/**
 * @author hh
 * 
 *         Implementation of the NonEmpty Extension for Mondrian Data Source.
 */
public class MondrianNonEmpty extends ExtensionSupport implements NonEmpty {

	static Logger logger = Logger.getLogger(MondrianNonEmpty.class);

	/**
	 * Constructor sets ID
	 */
	public MondrianNonEmpty() {
		super.setId(NonEmpty.ID);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.NonEmpty#isNonEmpty()
	 */
	public boolean isNonEmpty() {

		MondrianModel m = (MondrianModel) getModel();
		MondrianQueryAdapter adapter = (MondrianQueryAdapter) m
				.getQueryAdapter();
		if (adapter == null)
			return false; // not initialized

		mondrian.olap.Query monQuery = adapter.getMonQuery();

		// loop over query axes
		// say yes if any axis has the nonEmpty flag
		// say no. if none of the axes have the NON EMPTY

		for (int i = 0; i < monQuery.getAxes().length; i++) {
			mondrian.olap.QueryAxis qAxis = monQuery.getAxes()[i];
			/*
			 * if ( containsOnlyMeasures(qAxis.set) ) continue;
			 */
			if (qAxis.isNonEmpty())
				return true;
		}
		return false; // none of the axes NON EMPTY so far

	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.NonEmpty#setNonEmpty(boolean)
	 */
	public void setNonEmpty(boolean nonEmpty) {

		MondrianModel m = (MondrianModel) getModel();
		MondrianQueryAdapter adapter = (MondrianQueryAdapter) m
				.getQueryAdapter();
		if (adapter == null) {
			logger.error("setNonEmpty  query adapter=null");
			return; // not initialized
		}

		mondrian.olap.Query monQuery = adapter.getMonQuery();

		// loop over query axes
		// set the nonEmpty flag, for all axes,

		boolean bChange = false;
		for (int i = 0; i < monQuery.getAxes().length; i++) {
			mondrian.olap.QueryAxis qAxis = monQuery.getAxes()[i];
			/*
			 * if ( containsOnlyMeasures(qAxis.set) ) continue;
			 */
			if (qAxis.isNonEmpty() != nonEmpty) {
				qAxis.setNonEmpty(nonEmpty);
				bChange = true;
			}
		}

		if (bChange && logger.isInfoEnabled())
			logger.info("Non Empty =" + nonEmpty);

		if (bChange)
			((MondrianModel) getModel()).fireModelChanged();

	}

	/**
	 * check, whether set contains members, which are not measures
	 */
	/*
	 * private boolean containsOnlyMeasures(Exp exp) { if ( exp instanceof
	 * FunCall ) { FunCall f = (FunCall)exp; if ( f.isSet() // set, examine
	 * every single arg || f.isCallToTuple() // check the members in the tuple
	 * || f.isCallToCrossJoin() ) { // analyze every set of the crossjoin for (
	 * int i = 0; i < f.args.length; i ++ ) { if (
	 * !containsOnlyMeasures(f.args[i]) ) return false; } } else { // other
	 * function, examine first argument if ( !containsOnlyMeasures(f.args[0]) )
	 * return false; } } else { // supposed to be a member if ( exp instanceof
	 * mondrian.olap.Member && !((mondrian.olap.Member)exp).isMeasure() ) return
	 * false; } return true; }
	 */

} // End MondrianNonEmpty
