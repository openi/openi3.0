package org.openi.olap.xmla;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.FunCall;
import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.query.Quax;
import com.tonbeller.jpivot.olap.query.QuaxUti;
import com.tonbeller.jpivot.olap.query.SetExp;
import com.tonbeller.jpivot.olap.query.Quax.CannotHandleException;

/**
 * Utility Functions for Quax
 * @author SUJEN
 */
public class XMLA_QuaxUti implements QuaxUti {

	static Logger logger = Logger.getLogger(XMLA_QuaxUti.class);

	/**
	 * 
	 * @param f
	 * @param m
	 * @return true if FunCall matches member
	 */
	public boolean isMemberInFunCall(Object oExp, Member m)
			throws Quax.CannotHandleException {
		FunCall f = (FunCall) oExp;
		XMLA_Member xm = (XMLA_Member) m;
		try {
			if (f.isCallTo("Children")) {
				return isMemberInChildren(f, xm);
			} else if (f.isCallTo("Descendants")) {
				return isMemberInDescendants(f, xm);
			} else if (f.isCallTo("Members")) {
				return isMemberInLevel(f, xm);
			} else if (f.isCallTo("Union")) {
				return isMemberInUnion(f, xm);
			} else if (f.isCallTo("{}")) {
				return isMemberInSet(f, xm);
			}
		} catch (OlapException e) {
			logger.error("?", e);
			return false;
		}
		throw new Quax.CannotHandleException(f.getFunction());
	}

	/**
	 * @param f
	 *            Children FunCall
	 * @param mSearch
	 *            member to search for
	 * @return true if member mSearch is in set of children function
	 */
	private boolean isMemberInChildren(FunCall f, XMLA_Member mSearch) {
		// calculated members are not really child
		if (mSearch.isCalculated())
			return false;

		XMLA_Member parent = (XMLA_Member) f.getArgs()[0];
		if (checkParent(parent, mSearch))
			return true;
		return false;
	}

	/**
	 * @param f
	 *            Descendants FunCall
	 * @param mSearch
	 *            member to search for
	 * @return true if member mSearch is in set of Descendants function
	 */
	private boolean isMemberInDescendants(FunCall f, XMLA_Member mSearch)
			throws OlapException {
		// calculated members are not really child
		if (mSearch.isCalculated())
			return false;

		XMLA_Member ancestor = (XMLA_Member) f.getArgs()[0];
		XMLA_Level level = (XMLA_Level) f.getArgs()[1];
		XMLA_Level mLevel = (XMLA_Level) mSearch.getLevel();
		if (!mLevel.equals(level))
			return false;
		if (mSearch.equals(ancestor))
			return false;

		int ancestorLevelNumber = ((XMLA_Level) ancestor.getLevel()).getDepth();
		XMLA_Member mm = mSearch;
		while (ancestorLevelNumber < ((XMLA_Level) mm.getLevel()).getDepth()) {
			mm = (XMLA_Member) mm.getParent();
		}

		if (mm.equals(ancestor))
			return true;
		else
			return false;

	}

	/**
	 * @param f
	 *            Members FunCall
	 * @param mSearch
	 *            member to search for
	 * @return true if member mSearch is in set of Members function
	 */
	private boolean isMemberInLevel(FunCall f, XMLA_Member mSearch) {
		XMLA_Level level = (XMLA_Level) f.getArgs()[0];
		if (level.equals(mSearch.getLevel()))
			return true;
		return false;
	}

	/**
	 * @param f
	 *            Set FunCall
	 * @param mSearch
	 *            member to search for
	 * @return true if member mSearch is in set function
	 */
	private boolean isMemberInSet(FunCall f, XMLA_Member mSearch) {
		// set of members expected
		for (int i = 0; i < f.getArgs().length; i++) {
			XMLA_Member m = (XMLA_Member) f.getArgs()[i];
			if (m.equals(mSearch))
				return true;
		}
		return false;
	}

	/**
	 * @param f
	 *            Union FunCall
	 * @param mSearch
	 *            member to search for
	 * @return true if member mSearch is in set function
	 */
	private boolean isMemberInUnion(FunCall f, XMLA_Member mSearch)
			throws CannotHandleException {
		// Unions may be nested
		for (int i = 0; i < 2; i++) {
			FunCall fChild = (FunCall) f.getArgs()[i];
			if (isMemberInFunCall(fChild, mSearch))
				return true;
		}
		return false;
	}

	/**
	 * @param f
	 *            TopBottom FunCall
	 * @param mSearch
	 *            member to search for
	 * @return true if member mSearch is in set of Top/Bottom function
	 */
	/*
	 * private boolean isMemberInTopBottom(FunCall f, XMLA_Member mSearch)
	 * throws OlapException { if (!(f.getArgs()[0] instanceof FunCall)) {
	 * logger.error("unexpected Exp in TopBottom function: " +
	 * f.getArgs()[0].getClass()); return false; // should not occur } FunCall
	 * set = (FunCall) f.getArgs()[0]; if (set.isCallTo("Children")) { return
	 * isMemberInChildren(set, mSearch);
	 * 
	 * } else if (set.isCallTo("Descendants")) { return
	 * isMemberInDescendants(set, mSearch); } else if (set.isCallTo("Members"))
	 * { return isMemberInLevel(set, mSearch); } return false; }
	 */

	/**
	 * 
	 * @param f
	 * @param m
	 * @return true if FunCall contains child of member
	 */
	public boolean isChildOfMemberInFunCall(Object oFun, Member m)
			throws CannotHandleException {
		// calculated members do not have children
		if (((XMLA_Member) m).isCalculated())
			return false;
		FunCall f = (FunCall) oFun;
		try {

			if (f.isCallTo("Children")) {
				return (((XMLA_Member) f.getArgs()[0]).equals(m));
			} else if (f.isCallTo("Descendants")) {
				// true, if f = descendants(m2, level) contains any child of m
				// so level must be parent-level of m
				XMLA_Member ancestor = (XMLA_Member) f.getArgs()[0];
				XMLA_Level lev = (XMLA_Level) f.getArgs()[1];
				XMLA_Level parentLevel = lev.getParentLevel();
				if (parentLevel != null && m.getLevel().equals(parentLevel)) {
					XMLA_Member mm = (XMLA_Member) m;
					int ancestorLevelNumber = ((XMLA_Level) ancestor.getLevel())
							.getDepth();
					while (ancestorLevelNumber < ((XMLA_Level) mm.getLevel())
							.getDepth()) {
						mm = (XMLA_Member) mm.getParent();
					}

					if (mm.equals(ancestor))
						return true;
					else
						return false;
				} else
					return false;
			} else if (f.isCallTo("Members")) {
				XMLA_Level lev = (XMLA_Level) f.getArgs()[0];
				XMLA_Level parentLevel = lev.getParentLevel();
				if (parentLevel != null && m.getLevel().equals(parentLevel))
					return true;
				else
					return false;
			} else if (f.isCallTo("Union")) {
				if (isChildOfMemberInFunCall(f.getArgs()[0], m))
					return true;
				else
					return isChildOfMemberInFunCall(f.getArgs()[1], m);
			} else if (f.isCallTo("{}")) {
				for (int i = 0; i < f.getArgs().length; i++) {
					XMLA_Member mm = (XMLA_Member) f.getArgs()[i];
					XMLA_Member mmp = (XMLA_Member) mm.getParent();
					if (mmp != null && mmp.equals(m))
						return true;
				}
				return false;
			}
		} catch (OlapException e) {
			// should not occur
			logger.error("?", e);
			return false;
		}
		throw new Quax.CannotHandleException(f.getFunction());
	}

	/**
	 * 
	 * @param f
	 * @param m
	 * @return true if FunCall contains descendants of member
	 */

	public boolean isDescendantOfMemberInFunCall(Object oExp, Member member)
			throws CannotHandleException {
		XMLA_Member m = (XMLA_Member) member;
		// calculated members do not have children
		if (m.isCalculated())
			return false;

		FunCall f = (FunCall) oExp;
		if (f.isCallTo("Children")) {
			// true, if m2.children contains descendants of m
			// <==> m is equal or ancestor of m2
			XMLA_Member mExp = (XMLA_Member) f.getArgs()[0];
			return (m.equals(mExp) || XMLA_Util.isDescendant(m, mExp));
		} else if (f.isCallTo("Descendants")) {
			// true, if descendants(m2) contain descendants of m
			// <==> m is equal or ancestor of m2
			XMLA_Member mExp = (XMLA_Member) f.getArgs()[0];
			return (m.equals(mExp) || XMLA_Util.isDescendant(m, mExp));
		} else if (f.isCallTo("Members")) {
			XMLA_Level levExp = (XMLA_Level) f.getArgs()[0];
			return (levExp.getDepth() > ((XMLA_Level) m.getLevel()).getDepth());
		} else if (f.isCallTo("Union")) {
			if (isDescendantOfMemberInFunCall(f.getArgs()[0], m))
				return true;
			else
				return isDescendantOfMemberInFunCall(f.getArgs()[1], m);
		} else if (f.isCallTo("{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				XMLA_Member mExp = (XMLA_Member) f.getArgs()[i];
				return (!m.equals(mExp) && XMLA_Util.isDescendant(m, mExp));
			}
			return false;
		}
		throw new Quax.CannotHandleException(f.getFunction());
	}

	/**
	 * remove children FunCall from Union should never be called
	 * 
	 * @param f
	 * @param mPath
	 */
	static FunCall removeChildrenFromUnion(FunCall f, XMLA_Member monMember) {

		FunCall f1 = (FunCall) f.getArgs()[0];
		FunCall f2 = (FunCall) f.getArgs()[1];
		if (f1.isCallTo("Children")
				&& ((XMLA_Member) f1.getArgs()[0]).equals(monMember)) {
			return f2;
		}
		if (f2.isCallTo("Children")
				&& ((XMLA_Member) f1.getArgs()[0]).equals(monMember)) {
			return f1;
		}
		FunCall f1New = f1;
		if (f1.isCallTo("Union"))
			f1New = removeChildrenFromUnion(f1, monMember);
		FunCall f2New = f2;
		if (f2.isCallTo("Union"))
			f2New = removeChildrenFromUnion(f2, monMember);

		if (f1 == f1New && f2 == f2New)
			return f;

		return new FunCall("Union", new Exp[] { f1New, f2New });
	}

	/**
	 * check level and add a member's uncles to list
	 * 
	 * @param m
	 */
	public void addMemberUncles(List list, Member m, int[] maxLevel) {
		XMLA_Member xm = (XMLA_Member) m;

		int parentLevel = ((XMLA_Level) xm.getLevel()).getDepth() - 1;
		if (parentLevel < maxLevel[0])
			return;
		if (parentLevel > maxLevel[0]) {
			maxLevel[0] = parentLevel;
			list.clear();
		}
		AddUncels: if (parentLevel > 0) {
			XMLA_Member parent;
			XMLA_Member grandPa;
			try {
				parent = (XMLA_Member) xm.getParent();
				grandPa = (XMLA_Member) parent.getParent();
			} catch (OlapException e) {
				// should not occur
				logger.error("?", e);
				return;
			}

			// do nothing if already on List
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Exp exp = (Exp) iter.next();
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Children")
							&& ((XMLA_Member) f.getArgs()[0]).equals(grandPa)) {
						break AddUncels; // already there
					}
				}
			}
			FunCall fUncles = new FunCall("Children", new Exp[] { grandPa },
					FunCall.TypeProperty);
			/*
			 * // remove all existing children of grandPa from worklist; for
			 * (Iterator iter = workList.iterator(); iter.hasNext();) { Exp exp
			 * = (Exp) iter.next(); if (exp instanceof XMLA_Member &&
			 * ((XMLA_Member) exp).getParentMember().equals(grandPa))
			 * iter.remove(); }
			 */
			list.add(fUncles);
		} // AddUncels
	}

	/**
	 * check level and add a member's parents children to list
	 * 
	 * @param m
	 */
	public void addMemberSiblings(List list, Member m, int[] maxLevel) {
		XMLA_Member xm = (XMLA_Member) m;
		int level = ((XMLA_Level) xm.getLevel()).getDepth();
		if (level < maxLevel[0])
			return;
		if (level > maxLevel[0]) {
			maxLevel[0] = level;
			list.clear();
		}
		AddSiblings: if (level > 0) {
			XMLA_Member parent;
			try {
				parent = (XMLA_Member) xm.getParent();
			} catch (OlapException e) {
				// should not occur
				logger.error("?", e);
				return;
			}
			// do nothing if already on List
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Exp exp = (Exp) iter.next();
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Children")
							&& ((XMLA_Member) f.getArgs()[0]).equals(parent)) {
						break AddSiblings;
					}
				}
			}
			FunCall fSiblings = new FunCall("Children", new Exp[] { parent },
					FunCall.TypeProperty);
			/*
			 * // remove all existing children of parent from worklist; for
			 * (Iterator iter = workList.iterator(); iter.hasNext();) { Exp exp
			 * = (Exp) iter.next(); if (exp instanceof XMLA_Member &&
			 * ((XMLA_Member) exp).getParentMember().equals(parent))
			 * iter.remove(); }
			 */
			list.add(fSiblings);
		} // AddSiblings
	}

	/**
	 * check level and add a member to list
	 * 
	 * @param m
	 */
	public void addMemberChildren(List list, Member m, int[] maxLevel) {
		XMLA_Member xm = (XMLA_Member) m;
		int childLevel = ((XMLA_Level) xm.getLevel()).getDepth() + 1;
		if (childLevel < maxLevel[0])
			return;
		if (childLevel > maxLevel[0]) {
			maxLevel[0] = childLevel;
			list.clear();
		}
		AddChildren: if (childLevel > 0) {
			// do nothing if already on List
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Exp exp = (Exp) iter.next();
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Children")
							&& ((XMLA_Member) f.getArgs()[0]).equals(xm)) {
						break AddChildren;
					}
				}
			}
			FunCall fChildren = new FunCall("Children", new Exp[] { xm },
					FunCall.TypeProperty);
			/*
			 * // remove all existing children of m from worklist; for (Iterator
			 * iter = workList.iterator(); iter.hasNext();) { Exp exp = (Exp)
			 * iter.next(); if (exp instanceof XMLA_Member && ((XMLA_Member)
			 * exp).getParentMember().equals(m)) iter.remove(); }
			 */
			list.add(fChildren);
		} // AddChildren
	}

	/**
	 * check level and add a members descendatns to list
	 * 
	 * @param m
	 */
	public void addMemberDescendants(List list, Member m, Level lev,
			int[] maxLevel) {
		XMLA_Member xm = (XMLA_Member) m;
		int parentLevel = ((XMLA_Level) xm.getLevel()).getDepth() - 1;

		if (parentLevel < maxLevel[0])
			return;
		if (parentLevel > maxLevel[0]) {
			maxLevel[0] = parentLevel;
			list.clear();
		}
		AddDescendants: if (parentLevel > 0) {
			// do nothing if already on List
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Exp exp = (Exp) iter.next();
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Descendants")
							&& ((XMLA_Member) f.getArgs()[0]).equals(m)) {
						break AddDescendants;
					}
				}
			}
			FunCall fChildren = new FunCall("Descendants", new Exp[] { xm,
					(XMLA_Level) lev }, FunCall.TypeFunction);
			/*
			 * // remove all existing Descendants of m from worklist for
			 * (Iterator iter = workList.iterator(); iter.hasNext();) { Exp exp
			 * = (Exp) iter.next(); if (exp instanceof XMLA_Member &&
			 * ((XMLA_Member) exp).isChildOrEqualTo(m)) iter.remove(); }
			 */
			list.add(fChildren);
		} // AddDescendants
	}

	/**
	 * check level and add a levels members to list
	 * 
	 * @param m
	 */
	public void addLevelMembers(List list, Level lev, int[] maxLevel) {

		int level = ((XMLA_Level) lev).getDepth();
		if (level < maxLevel[0])
			return;
		if (level > maxLevel[0]) {
			maxLevel[0] = level;
			list.clear();
		}
		AddMembers: if (level > 0) {
			// do nothing if already on List
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Exp exp = (Exp) iter.next();
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Members")) {
						break AddMembers;
					}
				}
			}
			FunCall fMembers = new FunCall("Members",
					new Exp[] { (XMLA_Level) lev }, FunCall.TypeProperty);
			/*
			 * // remove all existing level members from worklist for (Iterator
			 * iter = workList.iterator(); iter.hasNext();) { Exp exp = (Exp)
			 * iter.next(); if (exp instanceof XMLA_Member && ((XMLA_Member)
			 * exp).getLevel().equals(lev)) iter.remove(); }
			 */
			list.add(fMembers);
		} // AddDescendants
	}

	/**
	 * create String representation for FunCall
	 * 
	 * @param f
	 * @return
	 */
	public StringBuffer funString(Object oFun) {
		FunCall f = (FunCall) oFun;
		StringBuffer buf = new StringBuffer();
		if (f.isCallTo("Children")) {
			XMLA_Member m = (XMLA_Member) f.getArgs()[0];
			buf.append(m.getUniqueName());
			buf.append(".children");
		} else if (f.isCallTo("Descendants")) {
			XMLA_Member m = (XMLA_Member) f.getArgs()[0];
			XMLA_Level lev = (XMLA_Level) f.getArgs()[1];
			buf.append("Descendants(");
			buf.append(m.getUniqueName());
			buf.append(",");
			buf.append(lev.getUniqueName());
			buf.append(")");
		} else if (f.isCallTo("members")) {
			XMLA_Level lev = (XMLA_Level) f.getArgs()[0];
			buf.append(lev.getUniqueName());
			buf.append(".Members");
		} else if (f.isCallTo("Union")) {
			buf.append("Union(");
			FunCall f1 = (FunCall) f.getArgs()[0];
			buf.append(funString(f1));
			buf.append(",");
			FunCall f2 = (FunCall) f.getArgs()[1];
			buf.append(funString(f2));
			buf.append(")");
		} else if (f.isCallTo("{}")) {
			buf.append("{");
			for (int i = 0; i < f.getArgs().length; i++) {
				if (i > 0)
					buf.append(",");
				XMLA_Member m = (XMLA_Member) f.getArgs()[i];
				buf.append(m.getUniqueName());
			}
			buf.append("}");
		} else if (f.isCallTo("TopCount") || f.isCallTo("BottomCount")
				|| f.isCallTo("TopPercent") || f.isCallTo("BottomPercent")) {
			// just generate Topcount(set)
			buf.append(f.getFunction());
			buf.append("(");
			FunCall f1 = (FunCall) f.getArgs()[0];
			buf.append(funString(f1));
			buf.append(")");
		}
		return buf;
	}

	/**
	 * determine hierarchy for Exp
	 * 
	 * @param exp
	 * @return hierarchy
	 */
	public Hierarchy hierForExp(Object oExp) throws CannotHandleException {
		if (oExp instanceof XMLA_Member)
			return ((XMLA_Member) oExp).getHierarchy();
		else if (oExp instanceof SetExp) {
			// set expression generated by CalcSet extension
			SetExp set = (SetExp) oExp;
			return set.getHier();
		}

		// must be FunCall
		FunCall f = (FunCall) oExp;
		if (f.isCallTo("Children")) {
			XMLA_Member m = (XMLA_Member) f.getArgs()[0];
			return m.getHierarchy();
		} else if (f.isCallTo("Descendants")) {
			XMLA_Member m = (XMLA_Member) f.getArgs()[0];
			return m.getHierarchy();
		} else if (f.isCallTo("Members")) {
			XMLA_Level lev = (XMLA_Level) f.getArgs()[0];
			return lev.getHierarchy();
		} else if (f.isCallTo("Union")) {
			// continue with first set
			return hierForExp(f.getArgs()[0]);
		} else if (f.isCallTo("{}")) {
			XMLA_Member m = (XMLA_Member) f.getArgs()[0];
			return m.getHierarchy();
		} else if (f.isCallTo("TopCount") || f.isCallTo("BottomCount")
				|| f.isCallTo("TopPercent") || f.isCallTo("BottomPercent")
				|| f.isCallTo("Filter")) {
			// continue with base set of top bottom function
			return hierForExp(f.getArgs()[0]);
		}
		throw new Quax.CannotHandleException(f.getFunction());
	}

	/**
	 * @param oExp
	 *            expression
	 * @return true, if exp is member
	 * @see QuaxUti#isMember(java.lang.Object)
	 */
	public boolean isMember(Object oExp) {
		return (oExp instanceof XMLA_Member);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#isFunCall
	 */
	public boolean isFunCall(Object oExp) {
		return (oExp instanceof FunCall);
	}

	/**
	 * @param oExp
	 *            expression
	 * @return true if expression equals member
	 * @see QuaxUti#equalMember(java.lang.Object,
	 *      com.tonbeller.jpivot.olap.model.Member)
	 */
	public boolean equalMember(Object oExp, Member member) {
		return ((XMLA_Member) member).equals(oExp);
	}

	/**
	 * @see QuaxUti#getParentMember(java.lang.Object)
	 */
	public Member getParentMember(Object oExp) {
		try {
			return ((XMLA_Member) oExp).getParent();
		} catch (OlapException e) {
			// should not occur
			logger.error("?", e);
			return null;
		}
	}

	/**
	 * create Children FunCall
	 * 
	 * @see QuaxUti#funChildren(com.tonbeller.jpivot.olap.model.Member)
	 */
	public Object funCallChildren(Member member) {
		return new FunCall("Children", new Exp[] { (XMLA_Member) member },
				FunCall.TypeProperty);
	}

	/**
	 * @return a members hierarchy
	 * @see QuaxUti#hierForMember(com.tonbeller.jpivot.olap.model.Member)
	 */
	public Hierarchy hierForMember(Member member) {
		XMLA_Member xm = (XMLA_Member) member;
		return xm.getHierarchy();
	}

	/**
	 * return a members dimension
	 * 
	 * @see QuaxUti#dimForMember(com.tonbeller.jpivot.olap.model.Member)
	 */
	public Dimension dimForMember(Member member) {
		XMLA_Member xm = (XMLA_Member) member;
		return xm.getHierarchy().getDimension();
	}

	/**
	 * @return a members unique name
	 * @see QuaxUti#getMemberUniqueName(java.lang.Object)
	 */
	public String getMemberUniqueName(Object oExp) {
		XMLA_Member m = (XMLA_Member) oExp;
		return m.getUniqueName();
	}

	/**
	 * @return true, if an expression is a FunCall to e specific function
	 * @see QuaxUti#funCallTo(java.lang.Object, java.lang.String)
	 */
	public boolean isFunCallTo(Object oExp, String function) {
		return ((FunCall) oExp).isCallTo(function);
	}

	/**
	 * return a FunCalls argument of given index
	 * 
	 * @see QuaxUti#funCallArg(java.lang.Object, int)
	 */
	public Object funCallArg(Object oExp, int index) {
		return ((FunCall) oExp).getArgs()[index];
	}

	/**
	 * @return funcall name
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#funCallName(java.lang.Object)
	 */
	public String funCallName(Object oFun) {
		return ((FunCall) oFun).getFunction();
	}

	/**
	 * @return true if member (2.arg) is child of Member (1.arg)
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#checkParent
	 */
	public boolean checkParent(Member pMember, Object cMemObj) {
		// check by unique name, if possible
		XMLA_Member pm = (XMLA_Member) pMember;
		XMLA_Member cm = (XMLA_Member) cMemObj;
		if (pm.isMicrosoft()) {
			String pUName = cm.getParentUniqueName();
			return ((XMLA_Member) pMember).getUniqueName().equals(pUName);
		} else {
			// SAP - unique name does not contain parent
			try {
				return (pm.equals(cm.getParent()));
			} catch (OlapException e) {
				return false; // should not occur
			}
		}
	}

	/**
	 * @return true if member (1.arg) is child of Member (2.arg)
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#checkParent
	 */
	public boolean checkChild(Member cMember, Object pMemObj) {
		return checkParent((XMLA_Member) pMemObj, cMember);
	}

	/**
	 * @return true if member (2.arg) is descendant of Member (1.arg)
	 * @see QuaxUti.checkDescendant
	 */
	public boolean checkDescendantM(Member aMember, Member dMember) {
		return XMLA_Util.isDescendant((XMLA_Member) aMember,
				(XMLA_Member) dMember);
	}

	/**
	 * @return true if member object (2.arg) is descendant of Member (1.arg)
	 * @see QuaxUti.checkDescendantO
	 */
	public boolean checkDescendantO(Member aMember, Object oMember) {
		return XMLA_Util.isDescendant((XMLA_Member) aMember,
				(XMLA_Member) oMember);
	}

	/**
	 * @return exp object for member
	 * @see QuaxUti#objForMember(com.tonbeller.jpivot.olap.model.Member)
	 */
	public Object objForMember(Member member) {
		return member;
	}

	/**
	 * @return exp object for dimension
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#objForDim(com.tonbeller.jpivot.olap.model.Dimension)
	 */
	public Object objForDim(Dimension dim) {
		return dim; // (Exp)((XMLA_Dimension)dim);
	}

	/**
	 * return member for exp object
	 * 
	 * @see QuaxUti#memberForObj(java.lang.Object)
	 */
	public Member memberForObj(Object oExp) {
		return (Member) oExp;
	}

	/**
	 * display member array for debugging purposes
	 * 
	 * @param member
	 * @return
	 */
	public String memberString(Member[] mPath) {
		if (mPath == null || mPath.length == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mPath.length; i++) {
			if (i > 0)
				sb.append(" ");
			sb.append(((XMLA_Member) mPath[i]).getUniqueName());
		}
		return sb.toString();
	}

	/**
	 * generate an object for a list of members
	 * 
	 * @param mList
	 *            list of members
	 * @return null for empty lis, single member or set function otherwise
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#createMemberSet
	 */
	public Object createMemberSet(List mList) {
		if (mList.size() == 0)
			return null;
		else if (mList.size() == 1)
			return (Exp) mList.get(0);
		else {
			Exp[] remExps = (Exp[]) mList.toArray(new Exp[0]);
			return new FunCall("{}", remExps, FunCall.TypeBraces);
		}
	}

	/**
	 * check whether a Funcall does NOT resolve to top level of hierarchy
	 * 
	 * @param f
	 * @return
	 */
	public boolean isFunCallNotTopLevel(Object oExp)
			throws CannotHandleException {
		FunCall f = (FunCall) oExp;
		if (f.isCallTo("Children")) {
			return true; // children *not* top level
		} else if (f.isCallTo("Descendants")) {
			return true; // descendants*not* top level
		} else if (f.isCallTo("Members")) {
			XMLA_Level lev = (XMLA_Level) f.getArgs()[0];
			return (lev.getDepth() > 0);
		} else if (f.isCallTo("Union")) {
			if (isFunCallNotTopLevel(f.getArgs()[0]))
				return true;
			return isFunCallNotTopLevel(f.getArgs()[1]);
		} else if (f.isCallTo("{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				if (!isMemberOnToplevel(f.getArgs()[i]))
					return true;
			}
			return false;
		}
		throw new Quax.CannotHandleException(f.getFunction());
	}

	/**
	 * @return true if member is on top level (has no parent)
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#isMemberOnToplevel
	 */
	public boolean isMemberOnToplevel(Object oMem) {
		XMLA_Member m = (XMLA_Member) oMem;
		if (((XMLA_Level) m.getLevel()).getDepth() > 0)
			return false;
		else
			return true;
	}

	/**
	 * @return the depth of a member's level
	 * @see QuaxUti#levelDepthForMember(java.lang.Object)
	 */
	public int levelDepthForMember(Object oExp) {
		XMLA_Member m = (XMLA_Member) oExp;
		XMLA_Level level = (XMLA_Level) m.getLevel();
		return level.getDepth();
	}

	/**
	 * @return an Expression Object for the top level members of an hierarchy
	 * @see QuaxUti#topLevelMembers(com.tonbeller.jpivot.olap.model.Hierarchy)
	 */
	public Object topLevelMembers(Hierarchy hier, boolean expandAllMember) {
		return XMLA_Util.topLevelMembers(hier, expandAllMember);
	}

	/**
	 * @return count of a FunCall's arguments
	 * @see QuaxUti#funCallArgCount(java.lang.Object)
	 */
	public int funCallArgCount(Object oFun) {
		FunCall f = (FunCall) oFun;
		return f.getArgs().length;
	}

	/**
	 * @param oLevel
	 *            expression object representing level
	 * @return Level for given Expression Object
	 * @see QuaxUti#LevelForObj(java.lang.Object)
	 */
	public Level LevelForObj(Object oLevel) {
		return (Level) oLevel;
	}

	/**
	 * @return the parent level of a given level
	 * @see QuaxUti#getParentLevel(com.tonbeller.jpivot.olap.model.Level)
	 */
	public Level getParentLevel(Level level) {
		return ((XMLA_Level) level).getParentLevel();
	}

	public mondrian.olap.Exp toExp(Object o) {
		return (mondrian.olap.Exp) o;
	}

	/**
	 * create FunCall
	 * 
	 * @see QuaxUti#createFunCall(java.lang.String, java.lang.Object[], int)
	 */
	public Object createFunCall(String function, Object[] args, int funType) {
		Exp[] expArgs = new Exp[args.length];
		for (int i = 0; i < expArgs.length; i++) {
			expArgs[i] = (Exp) args[i];
		}
		int type;
		switch (funType) {
		case QuaxUti.FUNTYPE_BRACES:
			type = FunCall.TypeBraces;
			break;
		case QuaxUti.FUNTYPE_PROPERTY:
			type = FunCall.TypeProperty;
			break;
		case QuaxUti.FUNTYPE_TUPLE:
			type = FunCall.TypeParentheses;
			break;
		case QuaxUti.FUNTYPE_INFIX:
			type = FunCall.TypeInfix;
			break;
		default:
			type = FunCall.TypeFunction;
		}
		return new FunCall(function, expArgs, type);
	}

	/**
	 * check an expression whether we can handle it (expand, collapse) currently
	 * we can basically handle following FunCalls member.children,
	 * member.descendants, level.members
	 * 
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#canHandle(java.lang.Object)
	 */
	public boolean canHandle(Object oExp) {

		if (oExp instanceof Member)
			return true;
		FunCall f = (FunCall) oExp;
		if (f.isCallTo("children"))
			return true;
		if (f.isCallTo("descendants"))
			return true;
		if (f.isCallTo("members"))
			return true;
		if (f.isCallTo("{}"))
			return true;
		if (f.isCallTo("union")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				if (!canHandle(f.getArgs()[i]))
					return false;
			}
			return true;
		}

		return false;
	}

	/**
	 * @return member children
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object oMember) {
		XMLA_Member[] mChildren;
		try {
			mChildren = ((XMLA_Member) oMember).getChildren();
		} catch (OlapException e) {
			// should not occur
			logger.fatal("unexpected exception", e);
			return null;
		}
		return mChildren;
	}

	/**
	 * get the members of a level
	 */
	public Object[] getLevelMembers(Level level) {
		XMLA_Member[] members;
		try {
			members = ((XMLA_Level) level).getMembers();
		} catch (OlapException e) {
			logger.fatal("unexpected failure level members", e);
			return null;
		}
		return members;
	}

} // XMLA_QuaxUti
