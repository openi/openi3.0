package org.openi.olap.xmla;

import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.FunCall;
import com.tonbeller.jpivot.olap.mdxparse.Literal;
import com.tonbeller.jpivot.olap.mdxparse.ParsedQuery;
import com.tonbeller.jpivot.olap.mdxparse.QueryAxis;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.Position;
import com.tonbeller.jpivot.olap.navi.SortRank;
import com.tonbeller.jpivot.olap.query.SortRankBase;

/**
 * SortRank Implementation XMLA
 * @author SUJEN
 */
public class XMLA_SortRank extends SortRankBase implements SortRank {

	/**
	 * returns true, if one of the members is a measure
	 * 
	 * @param position
	 *            the position to check for sortability
	 * @return true, if the position is sortable
	 * @see com.tonbeller.jpivot.olap.navi.SortRank#isSortable(Position)
	 */
	public boolean isSortable(Position position) {
		Member[] members = position.getMembers();
		for (int i = 0; i < members.length; i++)
			if (members[i].getLevel().getHierarchy().getDimension().isMeasure())
				return true;
		return false;
	}

	/**
	 * apply sort to query
	 */
	public void addSortToQuery() {
		if (sorting && sortPosMembers != null) {
			XMLA_Model model = (XMLA_Model) getModel();
			ParsedQuery pq = ((XMLA_QueryAdapter) model.getQueryAdapter())
					.getParsedQuery();

			switch (sortMode) {
			case com.tonbeller.jpivot.olap.navi.SortRank.ASC:
			case com.tonbeller.jpivot.olap.navi.SortRank.DESC:
			case com.tonbeller.jpivot.olap.navi.SortRank.BASC:
			case com.tonbeller.jpivot.olap.navi.SortRank.BDESC:
				// call sort
				orderAxis(pq);
				break;
			case com.tonbeller.jpivot.olap.navi.SortRank.TOPCOUNT:
				topBottomAxis(pq, "TopCount");
				break;
			case com.tonbeller.jpivot.olap.navi.SortRank.BOTTOMCOUNT:
				topBottomAxis(pq, "BottomCount");
				break;
			default:
				return; // do nothing
			}
		}
	}

	/**
	 * add Order Funcall to QueryAxis
	 * 
	 * @param monAx
	 * @param monSortMode
	 */
	private void orderAxis(ParsedQuery pq) {
		// Order(TopCount) is allowed, Order(Order) is not permitted
		QueryAxis[] queryAxes = pq.getAxes();
		QueryAxis qa = queryAxes[quaxToSort.getOrdinal()];
		Exp setForAx = qa.getExp();

		// setForAx is the top level Exp of the axis
		// put an Order FunCall around
		Exp[] args = new Exp[3];
		args[0] = setForAx; // the set to be sorted is the set representing the
							// query axis
		// if we got more than 1 position member, generate a tuple for the 2.arg
		Exp sortExp;
		if (sortPosMembers.length > 1) {
			sortExp = new FunCall("()", (XMLA_Member[]) sortPosMembers,
					FunCall.TypeParentheses);
		} else {
			sortExp = (XMLA_Member) sortPosMembers[0];
		}
		args[1] = sortExp;
		args[2] = Literal.createString(sortMode2String(sortMode));
		FunCall order = new FunCall("Order", args, FunCall.TypeFunction);
		qa.setExp(order);
	}

	/**
	 * add Top/BottomCount Funcall to QueryAxis
	 * 
	 * @param monAx
	 * @param nShow
	 */
	private void topBottomAxis(ParsedQuery pq, String function) {
		// TopCount(TopCount) and TopCount(Order) is not permitted

		QueryAxis[] queryAxes = pq.getAxes();
		QueryAxis qa = queryAxes[quaxToSort.getOrdinal()];
		Exp setForAx = qa.getExp();
		Exp sortExp;
		// if we got more than 1 position member, generate a tuple
		if (sortPosMembers.length > 1) {
			sortExp = new FunCall("()", (XMLA_Member[]) sortPosMembers,
					FunCall.TypeParentheses);
		} else {
			sortExp = (XMLA_Member) sortPosMembers[0];
		}

		Exp[] args = new Exp[3];
		args[0] = setForAx; // the set representing the query axis
		args[1] = Literal.create(new Integer(topBottomCount));
		args[2] = sortExp;
		FunCall topbottom = new FunCall(function, args, FunCall.TypeFunction);
		qa.setExp(topbottom);
	}

	/**
	 * @param sort
	 *            mode according to JPivot
	 * @return sort mode String according to MDX
	 */
	static private String sortMode2String(int sortMode) {
		switch (sortMode) {
		case com.tonbeller.jpivot.olap.navi.SortRank.ASC:
			return "ASC";
		case com.tonbeller.jpivot.olap.navi.SortRank.DESC:
			return "DESC";
		case com.tonbeller.jpivot.olap.navi.SortRank.BASC:
			return "BASC";
		case com.tonbeller.jpivot.olap.navi.SortRank.BDESC:
			return "BDESC";
		default:
			return ""; // should not happen
		}
	}

} // End XMLA_SortRank
