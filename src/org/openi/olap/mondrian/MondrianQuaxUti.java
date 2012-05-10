package org.openi.olap.mondrian;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mondrian.olap.Exp;
import mondrian.olap.FunCall;
import mondrian.olap.OlapElement;
import mondrian.olap.SchemaReader;
import mondrian.olap.Syntax;
import mondrian.mdx.DimensionExpr;
import mondrian.mdx.HierarchyExpr;
import mondrian.mdx.LevelExpr;
import mondrian.mdx.MemberExpr;
import mondrian.mdx.UnresolvedFunCall;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.query.Quax;
import com.tonbeller.jpivot.olap.query.QuaxUti;
import com.tonbeller.jpivot.olap.query.SetExp;
import com.tonbeller.jpivot.olap.query.Quax.CannotHandleException;

/**
 * Utility Functions for Quax
 */
public class MondrianQuaxUti implements QuaxUti {

	static Logger logger = Logger.getLogger(MondrianQuaxUti.class);

	private MondrianModel model = null;

	private SchemaReader scr;

	/**
	 * c'tor
	 * 
	 * @param model
	 */
	MondrianQuaxUti(MondrianModel model) {
		this.model = model;
		this.scr = model.getSchemaReader();
	}

	private static boolean isCallTo(FunCall f, String name) {
		return f.getFunName().compareToIgnoreCase(name) == 0;
	}

	/**
	 * check whether a Funcall does NOT resolve to top level of hierarchy
	 * 
	 * @param oFun
	 * @return
	 */
	public boolean isFunCallNotTopLevel(Object oFun)
			throws CannotHandleException {
		FunCall f = (FunCall) oFun;
		if (isCallTo(f, "Children")) {
			return true; // children *not* top level
		} else if (isCallTo(f, "Descendants")) {
			return true; // descendants*not* top level
		} else if (isCallTo(f, "Members")) {
			mondrian.olap.Level lev = getLevelArg(f, 0);
			return (lev.getDepth() > 0);
		} else if (isCallTo(f, "Union")) {
			if (isFunCallNotTopLevel(f.getArg(0)))
				return true;
			return isFunCallNotTopLevel(f.getArg(1));
		} else if (isCallTo(f, "{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				if (!isMemberOnToplevel(getMemberArg(f, i)))
					return true;
			}
			return false;
		}
		throw new Quax.CannotHandleException(f.getFunName());
	}

	/**
	 * @return true if member is on top level (has no parent)
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#isMemberOnToplevel
	 */
	public boolean isMemberOnToplevel(Object oMem) {
		mondrian.olap.Member m = ((MondrianMember) oMem).getMonMember();
		return isMemberOnToplevel(m);
	}

	/**
   */
	private boolean isMemberOnToplevel(mondrian.olap.Member m) {
		if (m.getLevel().getDepth() > 0)
			return false;
		else
			return true;
	}

	/**
	 * 
	 * @param oFun
	 * @param member
	 * @return true if FunCall matches member
	 */
	public boolean isMemberInFunCall(Object oFun, Member member)
			throws CannotHandleException {
		FunCall f = (FunCall) oFun;
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		return isMemberInFunCall(f, m);
	}

	/**
	 * 
	 * @param f
	 * @param m
	 * @return true if FunCall matches member
	 */
	private boolean isMemberInFunCall(FunCall f, mondrian.olap.Member m)
			throws CannotHandleException {
		if (isCallTo(f, "Children")) {
			return isMemberInChildren(f, m);
		} else if (isCallTo(f, "Descendants")) {
			return isMemberInDescendants(f, m);
		} else if (isCallTo(f, "Members")) {
			return isMemberInLevel(f, m);
		} else if (isCallTo(f, "Union")) {
			return isMemberInUnion(f, m);
		} else if (isCallTo(f, "{}")) {
			return isMemberInSet(f, m);
		}
		throw new Quax.CannotHandleException(f.getFunName());
	}

	/**
	 * @param f
	 *            Children FunCall
	 * @param mSearch
	 *            member to search for
	 * @return true if member mSearch is in set of children function
	 */
	private boolean isMemberInChildren(FunCall f, mondrian.olap.Member mSearch) {
		if (mSearch.isCalculatedInQuery())
			return false;
		mondrian.olap.Member parent = getMemberArg(f, 0);
		if (parent.equals(mSearch.getParentMember()))
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
	private boolean isMemberInDescendants(FunCall f,
			mondrian.olap.Member mSearch) {
		if (mSearch.isCalculatedInQuery())
			return false;
		mondrian.olap.Member ancestor = getMemberArg(f, 0);
		mondrian.olap.Level level = getLevelArg(f, 1);
		if (mSearch.equals(ancestor))
			return false;
		if (!mSearch.isChildOrEqualTo(ancestor))
			return false;
		if (level.equals(mSearch.getLevel()))
			return true;
		return false;
	}

	/**
	 * @param f
	 *            Members FunCall
	 * @param mSearch
	 *            member to search for
	 * @return true if member mSearch is in set of Members function
	 */
	private boolean isMemberInLevel(FunCall f, mondrian.olap.Member mSearch) {
		if (mSearch.isCalculatedInQuery())
			return false;
		mondrian.olap.Level level = getLevelArg(f, 0);
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
	private boolean isMemberInSet(FunCall f, mondrian.olap.Member mSearch) {
		// set of members expected
		for (int i = 0; i < f.getArgs().length; i++) {
			mondrian.olap.Member m = getMemberArg(f, i);
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
	private boolean isMemberInUnion(FunCall f, mondrian.olap.Member mSearch)
			throws CannotHandleException {
		// Unions may be nested
		for (int i = 0; i < 2; i++) {
			FunCall fChild = (FunCall) f.getArg(i);
			if (isMemberInFunCall(fChild, mSearch))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param oFun
	 * @param member
	 * @return true if FunCall contains child of member
	 */
	public boolean isChildOfMemberInFunCall(Object oFun, Member member)
			throws CannotHandleException {
		FunCall f = (FunCall) oFun;
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		if (isCallTo(f, "Children")) {
			return (getMemberArg(f, 0).equals(m));
		} else if (isCallTo(f, "Descendants")) {
			mondrian.olap.Member ancestor = getMemberArg(f, 0);
			mondrian.olap.Level lev = getLevelArg(f, 1);
			mondrian.olap.Level parentLevel = lev.getParentLevel();
			if (parentLevel != null && m.getLevel().equals(parentLevel)) {
				if (m.isChildOrEqualTo(ancestor))
					return true;
				else
					return false;
			} else
				return false;
		} else if (isCallTo(f, "Members")) {
			mondrian.olap.Level lev = getLevelArg(f, 0);
			mondrian.olap.Level parentLevel = lev.getParentLevel();
			if (parentLevel != null && m.getLevel().equals(parentLevel))
				return true;
			else
				return false;
		} else if (isCallTo(f, "Union")) {
			if (isChildOfMemberInFunCall(f.getArg(0), member))
				return true;
			else
				return isChildOfMemberInFunCall(f.getArg(1), member);
		} else if (isCallTo(f, "{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				mondrian.olap.Member mm = getMemberArg(f, i);
				if (mm.isCalculatedInQuery())
					continue;
				mondrian.olap.Member mmp = mm.getParentMember();
				if (mmp != null && mmp.equals(m))
					return true;
			}
			return false;
		}
		throw new Quax.CannotHandleException(f.getFunName());
	}

	private static mondrian.olap.Level getLevelArg(FunCall f, final int index) {
		return ((LevelExpr) f.getArg(index)).getLevel();
	}

	private static mondrian.olap.Member getMemberArg(FunCall f, int index) {
		return ((MemberExpr) f.getArg(index)).getMember();
	}

	/**
	 * @param oFun
	 * @param member
	 * @return true if FunCall contains descendants of member
	 */
	public boolean isDescendantOfMemberInFunCall(Object oFun, Member member)
			throws CannotHandleException {
		FunCall f = (FunCall) oFun;
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		if (isCallTo(f, "Children")) {
			mondrian.olap.Member mExp = getMemberArg(f, 0);
			return (mExp.isChildOrEqualTo(m));
		} else if (isCallTo(f, "Descendants")) {
			mondrian.olap.Member mExp = getMemberArg(f, 0);
			return (mExp.isChildOrEqualTo(m));
		} else if (isCallTo(f, "Members")) {
			mondrian.olap.Level levExp = getLevelArg(f, 0);
			return (levExp.getDepth() > m.getLevel().getDepth());
		} else if (isCallTo(f, "Union")) {
			if (isDescendantOfMemberInFunCall(f.getArg(0), member))
				return true;
			else
				return isDescendantOfMemberInFunCall(f.getArg(1), member);
		} else if (isCallTo(f, "{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				mondrian.olap.Member mExp = getMemberArg(f, i);
				if (mExp.isCalculatedInQuery())
					continue;
				if (!m.equals(mExp) && mExp.isChildOrEqualTo(m))
					return true;
			}
			return false;
		}
		throw new Quax.CannotHandleException(f.getFunName());
	}

	/**
	 * remove descendants of member from set Funcall this function is only
	 * called if there *are* descendants of member in funcall
	 * 
	 * @param oFun
	 * @param member
	 * @return the remainder after descendants were removed
	 */
	public Object removeDescendantsFromFunCall(Object oFun, Member member)
			throws CannotHandleException {
		FunCall f = (FunCall) oFun;
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		if (isCallTo(f, "Children")) {
			// as we know, that there is a descendent of m in x.children,
			// we know that *all* x.children are descendants of m
			return null;
		} else if (isCallTo(f, "Descendants")) {
			// as we know, that there is a descendent of m in x.descendants
			// we know that *all* x.descendants are descendants of m
			return null;
		} else if (isCallTo(f, "Members")) {
			mondrian.olap.Level levExp = getLevelArg(f, 0);
			List members = scr.getLevelMembers(levExp, false);
			List remainder = new ArrayList();
			for (int i = 0; i < members.size(); i++) {
				mondrian.olap.Member currMember = (mondrian.olap.Member) members
						.get(i);
				if (!currMember.isChildOrEqualTo(m))
					remainder.add(currMember);
			}
			return createMemberSet(remainder);
		} else if (isCallTo(f, "{}")) {
			List remainder = new ArrayList();
			for (int i = 0; i < f.getArgs().length; i++) {
				mondrian.olap.Member mExp = getMemberArg(f, i);
				if (mExp.isCalculatedInQuery())
					continue;
				if (mExp.equals(m) || !mExp.isChildOrEqualTo(m))
					remainder.add(mExp);
			}
			return createMemberSet(remainder);
		} else if (isCallTo(f, "Union")) {
			// TODO XMLA
			Exp[] uargs = new Exp[2];
			uargs[0] = (Exp) removeDescendantsFromFunCall(f.getArg(0), member);
			uargs[1] = (Exp) removeDescendantsFromFunCall(f.getArg(1), member);
			if (uargs[0] == null && uargs[1] == null)
				return null;
			if (uargs[1] == null)
				return uargs[0];
			if (uargs[0] == null)
				return uargs[1];
			if (uargs[0] instanceof mondrian.olap.Member) {
				Exp e = uargs[0];
				uargs[0] = new UnresolvedFunCall("{}", Syntax.Braces,
						new Exp[] { e });
			}
			if (uargs[1] instanceof mondrian.olap.Member) {
				Exp e = uargs[1];
				uargs[1] = new UnresolvedFunCall("{}", Syntax.Braces,
						new Exp[] { e });
			}
			return new UnresolvedFunCall("Union", uargs);
		}
		throw new Quax.CannotHandleException(f.getFunName());
	}

	/**
	 * generate an object for a list of members
	 * 
	 * @param mList
	 *            list of members
	 * @return null for empty list, single member or set function otherwise
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#createMemberSet(java.util.List)
	 */
	public Object createMemberSet(List mList) {
		if (mList.size() == 0)
			return null;
		else if (mList.size() == 1)
			return mList.get(0);
		else {
			Exp[] remExps = (Exp[]) mList.toArray(new Exp[0]);
			return new UnresolvedFunCall("{}", Syntax.Braces, remExps);
		}

	}

	/**
	 * remove children FunCall from Union should never be called
	 * 
	 * @param f
	 * @param monMember
	 */
	static FunCall removeChildrenFromUnion(FunCall f,
			mondrian.olap.Member monMember) {

		FunCall f1 = (FunCall) f.getArg(0);
		FunCall f2 = (FunCall) f.getArg(1);
		if (isCallTo(f1, "Children") && getMemberArg(f1, 0).equals(monMember)) {
			return f2;
		}
		if (isCallTo(f2, "Children") && getMemberArg(f1, 0).equals(monMember)) {
			return f1;
		}
		FunCall f1New = f1;
		if (isCallTo(f1, "Union"))
			f1New = removeChildrenFromUnion(f1, monMember);
		FunCall f2New = f2;
		if (isCallTo(f2, "Union"))
			f2New = removeChildrenFromUnion(f2, monMember);

		if (f1 == f1New && f2 == f2New)
			return f;

		return new UnresolvedFunCall("Union", new Exp[] { f1New, f2New });
	}

	/**
	 * return member for exp object
	 * 
	 * @see QuaxUti#memberForObj(java.lang.Object)
	 */
	public Member memberForObj(Object oExp) {
		mondrian.olap.Member monMember = toMember(oExp);
		Member member = model.lookupMemberByUName(monMember.getUniqueName());
		return member;
	}

	/**
	 * @return exp object for member
	 * @see QuaxUti#objForMember(com.tonbeller.jpivot.olap.model.Member)
	 */
	public Object objForMember(Member member) {
		return ((MondrianMember) member).getMonMember();
	}

	/**
	 * @return exp object for dimension
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#objForDim
	 */
	public Object objForDim(Dimension dim) {
		return ((MondrianDimension) dim).getMonDimension();
	}

	/**
	 * return a members dimension
	 * 
	 * @see QuaxUti#dimForMember(com.tonbeller.jpivot.olap.model.Member)
	 */
	public Dimension dimForMember(Member member) {
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		mondrian.olap.Dimension monDim = m.getDimension();
		return model.lookupDimension(monDim.getUniqueName());
	}

	/**
	 * @return a members hierarchy
	 * @see QuaxUti#hierForMember(com.tonbeller.jpivot.olap.model.Member)
	 */
	public Hierarchy hierForMember(Member member) {
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		mondrian.olap.Hierarchy monHier = m.getHierarchy();
		return model.lookupHierarchy(monHier.getUniqueName());
	}

	/**
	 * @param oLevel
	 *            expression object representing level
	 * @return Level for given Expression Object
	 * @see QuaxUti#LevelForObj(java.lang.Object)
	 */
	public Level LevelForObj(Object oLevel) {
		if (oLevel instanceof LevelExpr)
			oLevel = ((LevelExpr) oLevel).getLevel();
		mondrian.olap.Level monLevel = (mondrian.olap.Level) oLevel;
		return model.lookupLevel(monLevel.getUniqueName());
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
	 * return a FunCalls argument of given index
	 * 
	 * @see QuaxUti#funCallArg(java.lang.Object, int)
	 */
	public Object funCallArg(Object oFun, int index) {
		FunCall f = (FunCall) oFun;
		final Exp arg = f.getArg(index);
		final OlapElement element = fromExp(arg);
		return element == null ? (Object) arg : element;
	}

	/**
	 * @return function name
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#funCallName
	 */
	public String funCallName(Object oFun) {
		return ((FunCall) oFun).getFunName();
	}

	/**
	 * check level and add a members descendatns to list
	 */
	public void addMemberDescendants(List list, Member member, Level level,
			int[] maxLevel) {
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		mondrian.olap.Level lev = ((MondrianLevel) level).getMonLevel();
		int parentLevel = lev.getDepth() + 1;
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
					if (isCallTo(f, "Descendants")
							&& getMemberArg(f, 0).equals(m)) {
						break AddDescendants;
					}
				}
			}
			UnresolvedFunCall fChildren = new UnresolvedFunCall("Descendants",
					Syntax.Function, new Exp[] { new MemberExpr(m),
							new LevelExpr(lev) });
			/*
			 * // remove all existing Descendants of m from worklist for
			 * (Iterator iter = workList.iterator(); iter.hasNext();) { Exp exp
			 * = (Exp) iter.next(); if (exp instanceof mondrian.olap.Member &&
			 * ((MemberExpr) exp).isChildOrEqualTo(m)) iter.remove(); }
			 */
			list.add(fChildren);
		} // AddDescendants
	}

	/**
	 * @see QuaxUti#getParentMember(java.lang.Object)
	 */
	public Member getParentMember(Object oExp) {
		mondrian.olap.Member m = toMember(oExp);
		mondrian.olap.Member monParent = m.getParentMember();
		if (monParent == null)
			return null;
		Member parent = model.lookupMemberByUName(monParent.getUniqueName());
		return parent;
	}

	/**
	 * create FunCall
	 * 
	 * @see QuaxUti#createFunCall(java.lang.String, java.lang.Object[], int)
	 */
	public Object createFunCall(String function, Object[] args, int funType) {
		Exp[] expArgs = new Exp[args.length];
		for (int i = 0; i < expArgs.length; i++) {
			expArgs[i] = toExp(args[i]);
		}
		Syntax syntax;
		switch (funType) {
		case QuaxUti.FUNTYPE_BRACES:
			syntax = Syntax.Braces;
			break;
		case QuaxUti.FUNTYPE_PROPERTY:
			syntax = Syntax.Property;
			break;
		case QuaxUti.FUNTYPE_TUPLE:
			syntax = Syntax.Parentheses;
			break;
		case QuaxUti.FUNTYPE_INFIX:
			syntax = Syntax.Infix;
			break;
		default:
			syntax = Syntax.Function;
		}
		return new UnresolvedFunCall(function, syntax, expArgs);
	}

	/**
	 * @return true, if an expression is a FunCall to e specific function
	 * @see QuaxUti#isFunCallTo(Object, String)
	 */
	public boolean isFunCallTo(Object oExp, String function) {
		return isCallTo((FunCall) oExp, function);
	}

	/**
	 * @return true if member (2.arg) is child of Member (1.arg)
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#checkParent
	 */
	public boolean checkParent(Member pMember, Object cMemObj) {
		mondrian.olap.Member mc = toMember(cMemObj);
		if (mc.isCalculatedInQuery())
			return false;
		mondrian.olap.Member mp = mc.getParentMember();
		if (mp == null)
			return false;
		else
			return mp.equals(((MondrianMember) pMember).getMonMember());
	}

	/**
	 * @return true if member (1.arg) is child of Member (2.arg)
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#checkParent
	 */
	public boolean checkChild(Member cMember, Object pMemObj) {
		mondrian.olap.Member mc = ((MondrianMember) cMember).getMonMember();
		if (mc.isCalculatedInQuery())
			return false;
		mondrian.olap.Member mp = mc.getParentMember();
		if (mp == null)
			return false;
		else
			return mp.equals(pMemObj);
	}

	/**
	 * return true if Member (2.arg) is descendant of Member (1.arg)
	 * 
	 * @see QuaxUti#isDescendantOfMemberInFunCall(Object,
	 *      com.tonbeller.jpivot.olap.model.Member)
	 */
	public boolean checkDescendantM(Member aMember, Member dMember) {
		mondrian.olap.Member monMember = ((MondrianMember) aMember)
				.getMonMember();
		mondrian.olap.Member monDesc = ((MondrianMember) dMember)
				.getMonMember();
		if (monDesc.isCalculatedInQuery() || monDesc.equals(monMember))
			return false;
		return monDesc.isChildOrEqualTo(monMember);
	}

	/**
	 * return true if Expression (2.arg) is descendant of Member (1.arg)
	 * 
	 * @see QuaxUti#isDescendantOfMemberInFunCall(Object,
	 *      com.tonbeller.jpivot.olap.model.Member)
	 */
	public boolean checkDescendantO(Member aMember, Object oMember) {
		mondrian.olap.Member monMember = ((MondrianMember) aMember)
				.getMonMember();
		mondrian.olap.Member monDesc = toMember(oMember);
		if (monDesc.isCalculatedInQuery() || monDesc.equals(monMember))
			return false;
		return monDesc.isChildOrEqualTo(monMember);
	}

	/**
	 * check level and add a levels members to list
	 */
	public void addLevelMembers(List list, Level level, int[] maxLevel) {
		mondrian.olap.Level lev = ((MondrianLevel) level).getMonLevel();
		int iLevel = lev.getDepth();
		if (iLevel < maxLevel[0])
			return;
		if (iLevel > maxLevel[0]) {
			maxLevel[0] = iLevel;
			list.clear();
		}
		AddMembers: if (iLevel > 0) {
			// do nothing if already on List
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Exp exp = (Exp) iter.next();
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (isCallTo(f, "Members")) {
						break AddMembers;
					}
				}
			}
			UnresolvedFunCall fMembers = new UnresolvedFunCall("Members",
					Syntax.Property, new Exp[] { new LevelExpr(lev) });
			/*
			 * // remove all existing level members from worklist for (Iterator
			 * iter = workList.iterator(); iter.hasNext();) { Exp exp = (Exp)
			 * iter.next(); if (exp instanceof mondrian.olap.Member &&
			 * ((MemberExpr) exp).getLevel().equals(lev)) iter.remove(); }
			 */
			list.add(fMembers);
		} // AddDescendants
	}

	/**
	 * determine hierarchy for Exp
	 * 
	 * @param oExp
	 * @return hierarchy
	 */
	public Hierarchy hierForExp(Object exp) throws CannotHandleException {
		if (exp instanceof MemberExpr)
			exp = ((MemberExpr) exp).getMember();
		else if (exp instanceof LevelExpr)
			exp = ((LevelExpr) exp).getLevel();
		else if (exp instanceof HierarchyExpr)
			exp = ((HierarchyExpr) exp).getHierarchy();
		else if (exp instanceof DimensionExpr)
			exp = ((DimensionExpr) exp).getDimension();
		if (exp instanceof mondrian.olap.OlapElement)
			return model.lookupHierarchy(((mondrian.olap.OlapElement) exp)
					.getHierarchy().getUniqueName());

		if (exp instanceof SetExp) {
			// set expression generated by CalcSet extension
			SetExp set = (SetExp) exp;
			return set.getHier();
		}

		// must be FunCall
		FunCall f = (FunCall) exp;
		if (isCallTo(f, "Children")) {
			mondrian.olap.Member m = getMemberArg(f, 0);
			return model.lookupHierarchy(m.getHierarchy().getUniqueName());
		} else if (isCallTo(f, "Descendants")) {
			mondrian.olap.Member m = getMemberArg(f, 0);
			return model.lookupHierarchy(m.getHierarchy().getUniqueName());
		} else if (isCallTo(f, "Members")) {
			mondrian.olap.Level lev = getLevelArg(f, 0);
			return model.lookupHierarchy(lev.getHierarchy().getUniqueName());
		} else if (isCallTo(f, "Union")) {
			// continue with first set
			return hierForExp(f.getArg(0));
		} else if (isCallTo(f, "{}")) {
			mondrian.olap.Member m = getMemberArg(f, 0);
			return model.lookupHierarchy(m.getHierarchy().getUniqueName());
		} else if (isCallTo(f, "TopCount") || isCallTo(f, "BottomCount")
				|| isCallTo(f, "TopPercent") || isCallTo(f, "BottomPercent")
				|| isCallTo(f, "Filter")) {
			// continue with base set of top bottom function
			return hierForExp(f.getArg(0));
		}
		throw new Quax.CannotHandleException(f.getFunName());
	}

	/**
	 * @return the depth of a member's level
	 * @see QuaxUti#levelDepthForMember(java.lang.Object)
	 */
	public int levelDepthForMember(Object oExp) {
		mondrian.olap.Member m = toMember(oExp);
		mondrian.olap.Level level = m.getLevel();
		return level.getDepth();
	}

	/**
	 * @return an Expression Object for the top level members of an hierarchy
	 * @see QuaxUti#topLevelMembers(com.tonbeller.jpivot.olap.model.Hierarchy,
	 *      boolean)
	 */
	public Object topLevelMembers(Hierarchy hier, boolean expandAllMember) {
		return MondrianUtil.topLevelMembers(
				((MondrianHierarchy) hier).getMonHierarchy(), expandAllMember,
				scr);
	}

	/**
	 * @return the parent level of a given level
	 * @see QuaxUti#getParentLevel(com.tonbeller.jpivot.olap.model.Level)
	 */
	public Level getParentLevel(Level level) {
		mondrian.olap.Level monLevel = ((MondrianLevel) level).getMonLevel();
		mondrian.olap.Level monParentLevel = monLevel.getParentLevel();
		return model.lookupLevel(monParentLevel.getUniqueName());
	}

	public Exp toExp(Object o) {
		if (o instanceof OlapElement) {
			if (o instanceof mondrian.olap.Member) {
				return new MemberExpr((mondrian.olap.Member) o);
			} else if (o instanceof mondrian.olap.Level) {
				return new LevelExpr((mondrian.olap.Level) o);
			} else if (o instanceof mondrian.olap.Hierarchy) {
				return new HierarchyExpr((mondrian.olap.Hierarchy) o);
			} else {
				return new DimensionExpr((mondrian.olap.Dimension) o);
			}
		} else {
			return (Exp) o;
		}
	}

	private OlapElement fromExp(Exp e) {
		if (e instanceof MemberExpr) {
			MemberExpr memberExpr = (MemberExpr) e;
			return memberExpr.getMember();
		} else if (e instanceof LevelExpr) {
			LevelExpr levelExpr = (LevelExpr) e;
			return levelExpr.getLevel();
		} else if (e instanceof HierarchyExpr) {
			HierarchyExpr hierarchyExpr = (HierarchyExpr) e;
			return hierarchyExpr.getHierarchy();
		} else if (e instanceof DimensionExpr) {
			DimensionExpr dimensionExpr = (DimensionExpr) e;
			return dimensionExpr.getDimension();
		} else {
			return null;
		}
	}

	/**
	 * @param oExp
	 *            expression
	 * @return true, if exp is member
	 * @see QuaxUti#isMember(java.lang.Object)
	 */
	public boolean isMember(Object oExp) {
		return (oExp instanceof mondrian.olap.Member || oExp instanceof MemberExpr);
	}

	private mondrian.olap.Member toMember(Object cMemObj) {
		if (cMemObj instanceof mondrian.olap.Member) {
			return (mondrian.olap.Member) cMemObj;
		} else {
			MemberExpr memberExpr = (MemberExpr) cMemObj;
			return memberExpr.getMember();
		}
	}

	/**
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#isFunCall
	 */
	public boolean isFunCall(Object oExp) {
		return oExp instanceof mondrian.olap.FunCall;
	}

	/**
	 * check level and add a member's parents children to list
	 */
	public void addMemberSiblings(List list, Member member, int[] maxLevel) {
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		int level = m.getLevel().getDepth();
		if (level < maxLevel[0])
			return;
		if (level > maxLevel[0]) {
			maxLevel[0] = level;
			list.clear();
		}
		AddSiblings: if (level > 0) {
			mondrian.olap.Member parent = m.getParentMember();
			// do nothing if already on List
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Exp exp = (Exp) iter.next();
				if (isFunCall(exp)) {
					FunCall f = (FunCall) exp;
					if (isCallTo(f, "Children")
							&& getMemberArg(f, 0).equals(parent)) {
						break AddSiblings;
					}
				}
			}
			UnresolvedFunCall fSiblings = new UnresolvedFunCall("Children",
					Syntax.Property, new Exp[] { new MemberExpr(parent) });
			/*
			 * // remove all existing children of parent from worklist; for
			 * (Iterator iter = workList.iterator(); iter.hasNext();) { Exp exp
			 * = (Exp) iter.next(); if (exp instanceof mondrian.olap.Member &&
			 * ((MemberExpr) exp).getParentMember().equals(parent))
			 * iter.remove(); }
			 */
			list.add(fSiblings);
		} // AddSiblings
	}

	/**
	 * check level and add a member to list
	 */
	public void addMemberChildren(List list, Member member, int[] maxLevel) {
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		int childLevel = m.getLevel().getDepth() + 1;
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
				if (isFunCall(exp)) {
					FunCall f = (FunCall) exp;
					if (isCallTo(f, "Children") && getMemberArg(f, 0).equals(m)) {
						break AddChildren;
					}
				}
			}
			UnresolvedFunCall fChildren = new UnresolvedFunCall("Children",
					Syntax.Property, new Exp[] { new MemberExpr(m) });
			/*
			 * // remove all existing children of m from worklist; for (Iterator
			 * iter = workList.iterator(); iter.hasNext();) { Exp exp = (Exp)
			 * iter.next(); if (exp instanceof mondrian.olap.Member &&
			 * ((MemberExpr) exp).getParentMember().equals(m)) iter.remove(); }
			 */
			list.add(fChildren);
		} // AddChildren
	}

	/**
	 * check level and add a member's uncles to list
	 */
	public void addMemberUncles(List list, Member member, int[] maxLevel) {
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		int parentLevel = m.getLevel().getDepth() - 1;
		if (parentLevel < maxLevel[0])
			return;
		if (parentLevel > maxLevel[0]) {
			maxLevel[0] = parentLevel;
			list.clear();
		}
		AddUncels: if (parentLevel > 0) {
			mondrian.olap.Member parent = m.getParentMember();
			mondrian.olap.Member grandPa = parent.getParentMember();

			// do nothing if already on List
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Exp exp = (Exp) iter.next();
				if (isFunCall(exp)) {
					FunCall f = (FunCall) exp;
					if (isCallTo(f, "Children")
							&& getMemberArg(f, 0).equals(grandPa)) {
						break AddUncels; // already there
					}
				}
			}
			UnresolvedFunCall fUncles = new UnresolvedFunCall("Children",
					Syntax.Property, new Exp[] { new MemberExpr(grandPa) });
			/*
			 * // remove all existing children of grandPa from worklist; for
			 * (Iterator iter = workList.iterator(); iter.hasNext();) { Exp exp
			 * = (Exp) iter.next(); if (exp instanceof mondrian.olap.Member &&
			 * ((MemberExpr) exp).getParentMember().equals(grandPa))
			 * iter.remove(); }
			 */
			list.add(fUncles);
		} // AddUncels
	}

	/**
	 * @return a members unique name
	 * @see QuaxUti#getMemberUniqueName(java.lang.Object)
	 */
	public String getMemberUniqueName(Object oExp) {
		mondrian.olap.Member m = toMember(oExp);
		return m.getUniqueName();
	}

	/**
	 * create String representation for FunCall
	 * 
	 * @param oFun
	 * @return
	 */
	public StringBuffer funString(Object oFun) {
		FunCall f = (FunCall) oFun;
		StringBuffer buf = new StringBuffer();
		if (isCallTo(f, "Children")) {
			mondrian.olap.Member m = getMemberArg(f, 0);
			buf.append(m.getUniqueName());
			buf.append(".children");
		} else if (isCallTo(f, "Descendants")) {
			mondrian.olap.Member m = getMemberArg(f, 0);
			mondrian.olap.Level lev = getLevelArg(f, 1);
			buf.append("Descendants(");
			buf.append(m.getUniqueName());
			buf.append(",");
			buf.append(lev.getUniqueName());
			buf.append(")");
		} else if (isCallTo(f, "members")) {
			mondrian.olap.Level lev = getLevelArg(f, 0);
			buf.append(lev.getUniqueName());
			buf.append(".Members");
		} else if (isCallTo(f, "Union")) {
			buf.append("Union(");
			FunCall f1 = (FunCall) f.getArg(0);
			buf.append(funString(f1));
			buf.append(",");
			FunCall f2 = (FunCall) f.getArg(1);
			buf.append(funString(f2));
			buf.append(")");
		} else if (isCallTo(f, "{}")) {
			buf.append("{");
			for (int i = 0; i < f.getArgs().length; i++) {
				if (i > 0)
					buf.append(",");
				mondrian.olap.Member m = getMemberArg(f, i);
				buf.append(m.getUniqueName());
			}
			buf.append("}");
		} else if (isCallTo(f, "TopCount") || isCallTo(f, "BottomCount")
				|| isCallTo(f, "TopPercent") || isCallTo(f, "BottomPercent")) {
			// just generate Topcount(set)
			buf.append(f.getFunName());
			buf.append("(");
			FunCall f1 = (FunCall) f.getArg(0);
			buf.append(funString(f1));
			buf.append(")");
		}
		return buf;
	}

	/**
	 * display member array for debugging purposes
	 * 
	 * @param mPath
	 * @return
	 */
	public String memberString(Member[] mPath) {
		if (mPath == null || mPath.length == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mPath.length; i++) {
			if (i > 0)
				sb.append(" ");
			mondrian.olap.Member m = ((MondrianMember) mPath[i]).getMonMember();
			sb.append(m.getUniqueName());
		}
		return sb.toString();
	}

	/**
	 * @param oExp
	 *            expression
	 * @return true if expression equals member
	 * @see QuaxUti#equalMember(java.lang.Object,
	 *      com.tonbeller.jpivot.olap.model.Member)
	 */
	public boolean equalMember(Object oExp, Member member) {
		mondrian.olap.Member m = ((MondrianMember) member).getMonMember();
		mondrian.olap.Member oMon = toMember(oExp);
		return (m.equals(oMon));
	}

	/**
	 * check an expression whether we can handle it (expand, collapse) currently
	 * we can basically handle following FunCalls member.children,
	 * member.descendants, level.members
	 * 
	 * @see com.tonbeller.jpivot.olap.query.QuaxUti#canHandle(java.lang.Object)
	 */
	public boolean canHandle(Object oExp) {

		if (isMember(oExp))
			return true;
		FunCall f = (FunCall) oExp;
		if (isCallTo(f, "children"))
			return true;
		if (isCallTo(f, "descendants"))
			return true;
		if (isCallTo(f, "members"))
			return true;
		if (isCallTo(f, "{}"))
			return true;
		if (isCallTo(f, "union")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				if (!canHandle(f.getArg(i)))
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
		List members = scr.getMemberChildren(toMember(oMember));
		return members.toArray(new mondrian.olap.Member[0]);
	}

	/**
	 * @return members of level
	 */
	public Object[] getLevelMembers(Level level) {
		mondrian.olap.Level monLevel = ((MondrianLevel) level).getMonLevel();
		List members = scr.getLevelMembers(monLevel, false);
		return members.toArray(new mondrian.olap.Member[0]);
	}

} // MondrianQuaxUti
