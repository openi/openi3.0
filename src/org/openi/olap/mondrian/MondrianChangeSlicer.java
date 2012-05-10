package org.openi.olap.mondrian;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mondrian.olap.AxisOrdinal;
import mondrian.olap.Exp;
import mondrian.olap.QueryAxis;
import mondrian.olap.Syntax;
import mondrian.olap.AxisOrdinal;
import mondrian.mdx.UnresolvedFunCall;
import mondrian.mdx.MemberExpr;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.core.ExtensionSupport;
import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.Position;
import com.tonbeller.jpivot.olap.model.Result;
import com.tonbeller.jpivot.olap.navi.ChangeSlicer;

/**
 * @author hh
 * @author SUJEN
 */
public class MondrianChangeSlicer extends ExtensionSupport implements
		ChangeSlicer {

	static Logger logger = Logger.getLogger(MondrianChangeSlicer.class);

	/**
	 * Constructor sets ID
	 */
	public MondrianChangeSlicer() {
		super.setId(ChangeSlicer.ID);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.ChangeSlicer#getSlicer()
	 */
	public Member[] getSlicer() {

		MondrianModel model = (MondrianModel) getModel();
		// use result rather than query
		Result res = null;
		try {
			res = model.getResult();
		} catch (OlapException ex) {
			// do not handle
			return new Member[0];
		}

		Axis slicer = res.getSlicer();
		List positions = slicer.getPositions();
		List members = new ArrayList();
		for (Iterator iter = positions.iterator(); iter.hasNext();) {
			Position pos = (Position) iter.next();
			Member[] posMembers = pos.getMembers();
			for (int i = 0; i < posMembers.length; i++) {
				if (!members.contains(posMembers[i]))
					members.add(posMembers[i]);
			}
		}

		return (Member[]) members.toArray(new Member[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.ChangeSlicer#setSlicer(Member[])
	 */
	public void setSlicer(Member[] members) {
		MondrianModel model = (MondrianModel) getModel();
		MondrianQueryAdapter adapter = (MondrianQueryAdapter) model
				.getQueryAdapter();
		mondrian.olap.Query monQuery = adapter.getMonQuery();

		boolean logInfo = logger.isInfoEnabled();

		if (members.length == 0) {
			// empty slicer
			monQuery.setSlicerAxis(null);
			if (logInfo)
				logger.info("slicer set to null");
		} else {
			ArrayList collectedMemberExpressions = new ArrayList();
			ArrayList conditions = new ArrayList();
			String prevHierarchyName = "";
			String hierarchyName = "";
			String mbrUniqueName = "";
			UnresolvedFunCall f = null;
			boolean firstCondition = true;
			for (int i = 0; i < members.length; i++) {
				mbrUniqueName = ((MondrianMember) members[i]).getUniqueName();
				hierarchyName = mbrUniqueName.substring(1,
						mbrUniqueName.indexOf("]"));
				if (!hierarchyName.equals(prevHierarchyName)) {
					if (collectedMemberExpressions.size() > 0) {
						if (firstCondition) {
							f = new UnresolvedFunCall(
									"{}",
									Syntax.Braces,
									(Exp[]) collectedMemberExpressions
											.toArray(new Exp[collectedMemberExpressions
													.size()]));
						} else {
							conditions
									.add(new UnresolvedFunCall(
											"{}",
											Syntax.Braces,
											(Exp[]) collectedMemberExpressions
													.toArray(new Exp[collectedMemberExpressions
															.size()])));
							f = new UnresolvedFunCall(
									"CrossJoin",
									Syntax.Function,
									(Exp[]) conditions
											.toArray(new Exp[conditions.size()]));
							conditions.clear();
						}
						conditions.add(f);
						firstCondition = false;
						if (logInfo)
							logger.info("Added a new filter condition for Hierarchy: "
									+ prevHierarchyName
									+ ", Conditions number: "
									+ collectedMemberExpressions.size());
						collectedMemberExpressions.clear();
						if (logInfo)
							logger.info("Clear conditions list. Size = "
									+ collectedMemberExpressions.size());
					}
					prevHierarchyName = hierarchyName;
					if (logInfo)
						logger.info("Collecting filters on member: "
								+ hierarchyName);
				}
				collectedMemberExpressions.add(createExpressionFor(monQuery,
						(MondrianMember) members[i]));
			}

			// Add lastly collected member to filters conditions list
			if (collectedMemberExpressions.size() > 0) {
				conditions.add(new UnresolvedFunCall("{}", Syntax.Braces,
						(Exp[]) collectedMemberExpressions
								.toArray(new Exp[collectedMemberExpressions
										.size()])));
				if (logInfo)
					logger.info("Added a new filter condition for Hierarchy: "
							+ hierarchyName);
			}

			if (conditions.size() == 1)
				monQuery.setSlicerAxis(new QueryAxis(false, (Exp) conditions
						.get(0), AxisOrdinal.StandardAxisOrdinal.SLICER,
						QueryAxis.SubtotalVisibility.Undefined));
			else {
				// SeraSoft - More dimensions selected. Build a CrossJoin
				// function
				UnresolvedFunCall intersectConditions = new UnresolvedFunCall(
						"Crossjoin", Syntax.Function,
						(Exp[]) conditions.toArray(new Exp[conditions.size()]));
				monQuery.setSlicerAxis(new QueryAxis(false,
						intersectConditions,
						AxisOrdinal.StandardAxisOrdinal.SLICER,
						QueryAxis.SubtotalVisibility.Undefined));

			}
		}
		model.fireModelChanged();
	}

	protected Exp createExpressionFor(mondrian.olap.Query monQuery,
			MondrianMember member) {
		return new MemberExpr(member.getMonMember());
	}

} // End MondrianChangeSlicer
