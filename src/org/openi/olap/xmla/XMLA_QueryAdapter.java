package org.openi.olap.xmla;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.mdxparse.CompoundId;
import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.FunCall;
import com.tonbeller.jpivot.olap.mdxparse.Parameter;
import com.tonbeller.jpivot.olap.mdxparse.ParsedQuery;
import com.tonbeller.jpivot.olap.mdxparse.QueryAxis;
import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.MemberPropertyMeta;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.navi.MemberProperties;
import com.tonbeller.jpivot.olap.query.Quax;
import com.tonbeller.jpivot.olap.query.QuaxChangeListener;
import com.tonbeller.jpivot.olap.query.QueryAdapter;
import com.tonbeller.jpivot.util.StringUtil;

/**
 * XMLA Adapter to MDX Query
 * @author SUJEN
 */
public class XMLA_QueryAdapter extends QueryAdapter implements
		QuaxChangeListener {

	static Logger logger = Logger.getLogger(XMLA_QueryAdapter.class);

	private ParsedQuery parsedQuery;
	private ParsedQuery cloneQuery;
	private XMLA_Result result;
	private String originalMDX;
	private int nAxes; // number of axes

	/**
	 * Constructor
	 */
	XMLA_QueryAdapter(XMLA_Model model) {
		super(model);

		genMDXHierarchize = true; // Result hierarchize cannot be used
		// genMDXHierarchize = model.isSAP();

		// HHTASK clone ???
		parsedQuery = model.getPQuery(); // .clone();
		QueryAxis[] queryAxes = parsedQuery.getAxes();

		// initialize the query axis state objects
		nAxes = queryAxes.length;

		quaxes = new XMLA_Quax[nAxes];
		for (int i = 0; i < nAxes; i++) {
			quaxes[i] = new XMLA_Quax(i, queryAxes[i], model);
			quaxes[i].addChangeListener(this);
		}

	}

	/**
	 * implement QuaxChangeListener
	 */
	public void quaxChanged(Quax quax, Object source, boolean changedByNavi) {
		useQuax = true;
		// remove the parameters for this axis from the parsed query
		Map paraMap = parsedQuery.getParaMap();
		int iOrdinal = quax.getOrdinal();
		Collection params = paraMap.values();
		List removeList = new ArrayList();
		for (Iterator iter = params.iterator(); iter.hasNext();) {
			Parameter param = (Parameter) iter.next();
			int iAxis = param.getIAxis();
			if (iAxis == iOrdinal) {
				// the parameter was on the axis for the quax
				// so it is lost - remove it
				removeList.add(param.getName().toUpperCase());
			}
		}
		for (Iterator iter = removeList.iterator(); iter.hasNext();) {
			String objToRemove = (String) iter.next();
			paraMap.remove(objToRemove);
		}
	}

	/**
	 * @return Quaxes array
	 */
	public Quax[] getQuaxes() {
		return quaxes;
	}

	/**
	 * Update the Query Object before Execute. The current query is build from -
	 * the original query - adding the drilldown groups - apply pending swap
	 * axes - apply pending sorts.
	 * 
	 * Called from Model.getResult before the query is executed.
	 */
	protected void onExecute() {

		// if quax is to be used, generate axes from quax
		if (useQuax) {
			int iQuaxToSort = -1;
			if (sortMan != null)
				iQuaxToSort = sortMan.activeQuaxToSort();

			QueryAxis[] qAxes = parsedQuery.getAxes();
			for (int i = 0; i < quaxes.length; i++) {
				boolean doHierarchize = false;
				if (genMDXHierarchize && quaxes[i].isHierarchizeNeeded()
						&& i != iQuaxToSort) {
					doHierarchize = true;
					if (logger.isDebugEnabled())
						logger.debug("MDX Generation added Hierarchize()");
				}
				Exp eSet = (Exp) quaxes[i].genExp(doHierarchize);
				qAxes[i].setExp(eSet);
			} // for quaxes
		} // useQuax

		// DIMENSION PROPERTIES
		QueryAxis[] qAxes = parsedQuery.getAxes();
		for (int i = 0; i < quaxes.length; i++) {
			XMLA_MemberProperties mPropExt = (XMLA_MemberProperties) model
					.getExtension(MemberProperties.ID);
			MemberPropertyMeta[] mprops = null;
			if (mPropExt != null)
				mprops = mPropExt.getVisibleProperties();
			if (mprops != null && mprops.length > 0) {
				List dProps = new ArrayList();
				PropsLoop: for (int j = 0; j < mprops.length; j++) {
					String hierUname = mprops[j].getScope();
					XMLA_Hierarchy hier = ((XMLA_Model) model)
							.lookupHierByUName(hierUname);
					String dimUname;
					if (hier != null) {
						dimUname = hier.getDimUniqueName();
						// if the dimension is not on the axis - ignore
						Dimension dim = hier.getDimension();
						Quax q = findQuax(dim);
						if ((q == null) || !quaxes[i].equals(q))
							continue PropsLoop;
					} else
						continue PropsLoop;
					CompoundId cid = new CompoundId(dimUname);
					String propName = mprops[j].getName();
					cid.append(StringUtil.bracketsAround(propName));
					dProps.add(cid);
				}
				qAxes[i].setDimProps(dProps);
			}
		} // for quaxes

		// generate order function if neccessary
		if (sortMan != null) {
			if (!useQuax) {
				// if Quax is used, the axis exp's are re-generated every time.
				// if not -
				// adding a sort to the query must not be permanent.
				// Therefore, we clone the orig state of the query object and
				// use
				// the clone furthermore in order to avoid duplicate "Order"
				// functions.
				if (cloneQuery == null) {
					if (sortMan.isSortOnQuery())
						cloneQuery = (ParsedQuery) parsedQuery.clone();
				} else {
					// reset to original state
					if (sortMan.isSortOnQuery())
						parsedQuery = (ParsedQuery) cloneQuery.clone();
					else
						parsedQuery = cloneQuery;
				}
			}
			sortMan.addSortToQuery();
		}

		// swap axes function if neccessary
		if (axesSwapped) {
			swapAxes();
		}

		// add FONT_SIZE to cell properties, if neccessary
		// CELL PROPERTIES VALUE, FORMATTED_VALUE, FONT_SIZE
		Map cmpmap = ((XMLA_Model) model).getCalcMeasurePropMap();
		if (cmpmap != null && cmpmap.size() > 0) {
			List cellProps = parsedQuery.getCellProps();
			CompoundId cid = new CompoundId("FONT_SIZE", false);
			boolean found = false;
			for (Iterator iter = cellProps.iterator(); iter.hasNext();) {
				CompoundId ci = (CompoundId) iter.next();
				if (ci.toMdx().equalsIgnoreCase("FONT_SIZE")) {
					found = true;
					break;
				}
			}
			if (!found)
				cellProps.add(cid);
		}

		long t1 = System.currentTimeMillis();

		String mdx = parsedQuery.toMdx();

		if (logger.isDebugEnabled())
			logger.debug(mdx);

		long t2 = System.currentTimeMillis();
		logger.info("monQuery.toString took " + (t2 - t1) + " millisec");

		((XMLA_Model) model).setCurrentMdx(mdx);

	}

	protected void onExecuteDrill() {
		long t1 = System.currentTimeMillis();

		// dsf call toDrillMdx
		String mdx = parsedQuery.toDrillMdx();

		if (logger.isDebugEnabled())
			logger.debug(mdx);

		long t2 = System.currentTimeMillis();
		logger.info("monQuery.toString took " + (t2 - t1) + " millisec");

		((XMLA_Model) model).setCurrentMdx(mdx);

	}

	/**
	 * return the corresponding mdx
	 */
	String getCurrentMdx() {
		String mdx = parsedQuery.toMdx();
		return mdx;
	}

	/**
	 * @return the XMLA Query object
	 */
	public ParsedQuery getParsedQuery() {
		return parsedQuery;
	}

	/**
	 * create set expression for list of members
	 * 
	 * @param memList
	 * @return set expression
	 */
	protected Object createMemberSet(List memList) {
		Exp[] exps = new Exp[memList.size()];
		int i = 0;
		for (Iterator iter = memList.iterator(); iter.hasNext();) {
			XMLA_Member m = (XMLA_Member) iter.next();
			exps[i++] = m;
		}
		FunCall f = new FunCall("{}", exps, FunCall.TypeBraces);
		return f;
	}

	// ***************
	// Expand Collapse
	// ***************

	/**
	 * find out, whether a member can be expanded. this is true, if - the member
	 * is on an axis and - the member is not yet expanded and - the member has
	 * children
	 * 
	 * @see com.tonbeller.jpivot.olap.navi.DrillExpand#canExpand(Member)
	 * @param Member
	 *            to be expanded
	 * @return true if the member can be expanded
	 */
	public boolean canExpand(Member member) {

		// a calculated member cannot be expanded
		if (((XMLA_Member) member).isCalculated())
			return false;

		if (!isDrillable(member, false))
			return false;

		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);
		return (quax == null) ? false : quax.canExpand(member);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.DrillExpand#canExpand(Member)
	 * @param position
	 *            position to be expanded
	 * @param Member
	 *            to be expanded
	 * @return true if the member can be expanded
	 */
	public boolean canExpand(Member[] pathMembers) {

		Member m = pathMembers[pathMembers.length - 1];
		// a calculated member cannot be expanded
		if (((XMLA_Member) m).isCalculated())
			return false;

		if (!isDrillable(m, false))
			return false;

		Dimension dim = m.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		return (quax == null) ? false : quax.canExpand(pathMembers);
	}

	/**
	 * first check whether the member is *really* drillable
	 */
	public void expand(Member member) {
		XMLA_Member m = (XMLA_Member) member;
		if (isDrillable(m, true))
			super.expand(member);
		else
			model.fireModelChanged();
	}

	/**
	 * expand a member in a specific position first check whether the member is
	 * *really* drillable
	 */
	public void expand(Member[] pathMembers) {
		XMLA_Member m = (XMLA_Member) pathMembers[pathMembers.length - 1];
		if (isDrillable(m, true))
			super.expand(pathMembers);
		else
			model.fireModelChanged();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.DrillExpand#canExpand(Member)
	 * @param Member
	 *            to be collapsed
	 * @return true if the member can be collapsed
	 */
	public boolean canCollapse(Member member) {
		// a calculated member cannot be collapsed
		if (((XMLA_Member) member).isCalculated())
			return false;
		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		return (quax == null) ? false : quax.canCollapse(member);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.DrillExpand#canCollapse(Member)
	 * @param position
	 *            position to be expanded
	 * @return true if the position can be collapsed
	 */
	public boolean canCollapse(Member[] pathMembers) {

		Member member = pathMembers[pathMembers.length - 1];
		// a calculated member cannot be collapsed
		if (((XMLA_Member) member).isCalculated())
			return false;
		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		return (quax == null) ? false : quax.canCollapse(pathMembers);
	}

	// ************
	// DrillReplace
	// ************

	/**
	 * drill down is possible if <code>member</code> has children
	 */
	public boolean canDrillDown(Member member) {

		if (!isDrillable(member, false))
			return false;
		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);
		return (quax == null) ? false : quax.canDrillDown(member);
	}

	// *********
	// Swap Axes
	// *********

	/**
	 * swap axes update all references to axis number in other objects
	 */
	void setSwapAxes(boolean swap) {
		if (parsedQuery.getAxes().length >= 2) {
			axesSwapped = swap;
			if (logger.isInfoEnabled()) {
				logger.info("swapAxes " + axesSwapped);
			}
			model.fireModelChanged();
		}
	}

	// ********
	// Internal
	// ********

	/**
	 * swap axes in parsed query
	 */
	private void swapAxes() {
		QueryAxis[] queryAxes = parsedQuery.getAxes();
		if (queryAxes.length >= 2) {
			Exp exp = queryAxes[0].getExp();
			queryAxes[0].setExp(queryAxes[1].getExp());
			queryAxes[1].setExp(exp);
		}
	}

	/**
	 * determine, whether a memebr is drillable
	 * 
	 * @param member
	 * @return true, if a member is drillable
	 */
	private boolean isDrillable(Member member, boolean allowComplete) {
		XMLA_Member m = (XMLA_Member) member;
		long ccard = m.getChildrenCardinality(); // -1 if not initialized
		if (ccard >= 0)
			return (ccard > 0);
		XMLA_Level level = (XMLA_Level) member.getLevel();
		XMLA_Model xmod = (XMLA_Model) model;
		XMLA_Hierarchy hier = (XMLA_Hierarchy) level.getHierarchy();
		// for performance issues, it is better if we can decide whether a
		// member
		// is drillable *without* completing it first.
		if (xmod.isMicrosoft()
				&& hier.getStructure() == XMLA_Hierarchy.STRUCTURE_FULLYBALANCED
				&& level.getChildLevel() != null) {
			// fully balanced, drillable, if and only if member is on deepest
			// level
			// does not work with SAP, Hierarchy.structure not supported
			return true;
		}
		if (!allowComplete)
			return true;
		try {
			xmod.completeMember(m);
		} catch (OlapException e) {
			logger.error("?", e);
			return false;
		}
		return (m.getChildrenCardinality() > 0);
	}

} // End XMLA_QueryAdapter
