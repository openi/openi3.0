package org.openi.olap.mondrian;

import java.util.ArrayList;
import java.util.List;

import mondrian.mdx.LevelExpr;
import mondrian.mdx.MemberExpr;
import mondrian.mdx.UnresolvedFunCall;
import mondrian.olap.Category;
import mondrian.olap.Dimension;
import mondrian.olap.Exp;
import mondrian.olap.FunCall;
import mondrian.olap.HierarchyBase;
import mondrian.olap.Member;
import mondrian.olap.Parameter;
import mondrian.olap.SchemaReader;
import mondrian.olap.Syntax;

import com.tonbeller.jpivot.util.TreeNode;
import com.tonbeller.jpivot.util.TreeNodeCallback;

/**
 * @author hh
 * @author SUJEN
 * 
 *         Utils for Mondrian Interface
 */
public class MondrianUtil {

	/**
	 * format exp to string for debugging purposes
	 * 
	 * @param exp
	 * @return
	 */
	public static String expString(Exp exp) {
		String s = "";
		if (exp instanceof FunCall) {
			FunCall f = (FunCall) exp;
			final String name = f.getFunName();
			s += "<function:" + name + ">";
			Exp[] args = f.getArgs();
			for (int i = 0; i < args.length; i++) {
				s += expString(args[i]);
			}
			s += "</function:" + name + ">";
		} else if (exp instanceof mondrian.olap.Member) {
			mondrian.olap.Member m = (mondrian.olap.Member) exp;
			s += "<member>" + m.getUniqueName() + "</member>";
		} else if (exp instanceof Parameter) {
			mondrian.olap.Parameter p = (mondrian.olap.Parameter) exp;
			s += "<parameter>" + p.getName() + "</parameter>";
		} else if (exp instanceof mondrian.olap.Hierarchy) {
			mondrian.olap.Hierarchy h = (mondrian.olap.Hierarchy) exp;
			s += "<hier>" + h.getUniqueName() + "</hier>";
		} else if (exp instanceof mondrian.olap.Level) {
			mondrian.olap.Level l = (mondrian.olap.Level) exp;
			s += "<level>" + l.getUniqueName() + "</level>";
		} else {
			s += " <exp>" + exp.toString() + "</exp>";
		}
		return s;
	}

	/**
	 * compare arrays of members
	 * 
	 * @param aMem1
	 * @param aMem2
	 * @return true, if arrays are equal
	 */
	public static boolean compareMembers(mondrian.olap.Member[] aMem1,
			mondrian.olap.Member[] aMem2) {
		if (aMem1.length != aMem2.length)
			return false;
		for (int i = 0; i < aMem1.length; i++) {
			// any null does not compare
			if (aMem1[i] == null)
				return false;
			if (!aMem1[i].equals(aMem2[i]))
				return false;
		}
		return true;
	}

	/**
	 * display member array for debugging purposes
	 * 
	 * @param mPath
	 * @return
	 */
	public static String memberString(mondrian.olap.Member[] mPath) {
		if (mPath == null || mPath.length == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mPath.length; i++) {
			if (i > 0)
				sb.append(" ");
			sb.append(mPath[i].getUniqueName());
		}
		return sb.toString();
	}

	/**
	 * generate Exp for top level members of hierarchy
	 * 
	 * @param monHier
	 * @param expandAllMember
	 *            - true if an "All" member is to be expanded
	 * @return Exp for top level members
	 */
	static Exp topLevelMembers(mondrian.olap.Hierarchy monHier,
			boolean expandAllMember, SchemaReader scr) {

		if (monHier.hasAll()) {
			// an "All" member is present -get it
			mondrian.olap.Member mona = ((mondrian.rolap.RolapHierarchy) monHier)
					.getAllMember();
			if (mona != null) {
				if (!expandAllMember)
					return new MemberExpr(mona);
				MemberExpr[] monar = { new MemberExpr(mona) };
				Exp monaSet = new UnresolvedFunCall("{}", Syntax.Braces, monar);

				// must expand
				// create Union({AllMember}, AllMember.children)
				Exp mAllChildren = new UnresolvedFunCall("children",
						Syntax.Property, monar);
				Exp union = new UnresolvedFunCall("union", Syntax.Function,
						new Exp[] { monaSet, mAllChildren });
				return union;
			}
		}
		// does this call work with parent-child
		List topMembers = scr.getHierarchyRootMembers(monHier);
		if (topMembers.size() == 1)
			return new MemberExpr((mondrian.olap.Member) topMembers.get(0)); // single
																				// member
		else if (topMembers.size() == 0)
			return null; // possible if access control active

		List list = new ArrayList(topMembers.size());
		for (int i = 0; i < topMembers.size(); i++) {
			mondrian.olap.Member m = (mondrian.olap.Member) topMembers.get(i);
			if (isVisible(scr, m)) {
				list.add(m);
			}
		}

		mondrian.olap.Member[] topMemberArr = (mondrian.olap.Member[]) list
				.toArray(new mondrian.olap.Member[list.size()]);

		return new UnresolvedFunCall("{}", Syntax.Braces,
				toExprArray(topMemberArr));
	}

	static MemberExpr[] toExprArray(Member[] members) {
		MemberExpr[] memberExprs = new MemberExpr[members.length];
		for (int i = 0; i < memberExprs.length; i++) {
			memberExprs[i] = new MemberExpr(members[i]);
		}
		return memberExprs;
	}

	/**
	 * determine descendants of member at specific level
	 * 
	 * @param scr
	 *            SchemaReader
	 * @param member
	 * @param level
	 * @return descendants
	 */
	public static mondrian.olap.Member[] getMemberDescendants(SchemaReader scr,
			mondrian.olap.Member member, mondrian.olap.Level level) {
		int depth = level.getDepth();
		mondrian.olap.Level lev = member.getLevel();
		if (depth <= lev.getDepth())
			return new mondrian.olap.Member[0];
		List currentMembers = new ArrayList();
		currentMembers.add(member);
		while (depth > lev.getDepth()) {
			lev = lev.getChildLevel();
			currentMembers = scr.getMemberChildren(currentMembers);
		}
		return (mondrian.olap.Member[]) currentMembers
				.toArray(new mondrian.olap.Member[0]);
	}

	/**
	 * Map function names to Mondrian Syntax type must be synchronized with
	 * mondrian/olap/fun/BuiltinFunTable.java
	 * 
	 * @param fuName
	 *            - function name
	 * @param nArgs
	 *            - number of function args
	 * @return Syntax type
	 */
	public static Syntax funCallSyntax(String fuName, int nArgs) {

		if (fuName.equals("()"))
			return Syntax.Parentheses;

		else if (fuName.equals("{}"))
			return Syntax.Braces;

		if (nArgs == 1) {

			if (fuName.equalsIgnoreCase("members"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("children"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("dimension"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("hierarchy"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("level"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("currentmember"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("defaultmember"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("firstchild"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("firstsibling"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("lastchild"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("lastsibling"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("nextmember"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("parent"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("prevmember"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("ordinal"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("value"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("lastperiods"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("siblings"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("name"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("uniquename"))
				return Syntax.Property;
			else if (fuName.equalsIgnoreCase("current"))
				return Syntax.Property;

			else if (fuName.equalsIgnoreCase("not"))
				return Syntax.Prefix;
			else if (fuName.equals("-"))
				return Syntax.Prefix;

		} else if (nArgs == 2) {

			if (fuName.indexOf('<') >= 0)
				return Syntax.Infix; // comparison operator
			else if (fuName.indexOf('=') >= 0)
				return Syntax.Infix; // comparison operator
			else if (fuName.indexOf('>') >= 0)
				return Syntax.Infix; // comparison operator
			else if (fuName.equals("*"))
				return Syntax.Infix;
			else if (fuName.equals(":"))
				return Syntax.Infix;
			else if (fuName.equals("+"))
				return Syntax.Infix;
			else if (fuName.equals("-"))
				return Syntax.Infix;
			else if (fuName.equals("/"))
				return Syntax.Infix;
			else if (fuName.equalsIgnoreCase("||"))
				return Syntax.Infix;
			else if (fuName.equalsIgnoreCase("and"))
				return Syntax.Infix;
			else if (fuName.equalsIgnoreCase("or"))
				return Syntax.Infix;
			else if (fuName.equalsIgnoreCase("xor"))
				return Syntax.Infix;
		}

		if (fuName.equalsIgnoreCase("lead"))
			return Syntax.Method;
		else if (fuName.equalsIgnoreCase("properties"))
			return Syntax.Method;
		else if (fuName.equalsIgnoreCase("item"))
			return Syntax.Method;

		return Syntax.Function;
	}

	/**
	 * 
	 * @param root
	 * @param iDim
	 * @return
	 */
	static List collectMembers(TreeNode root, final int iDim,
			final SchemaReader scr) {
		if (root == null)
			return null;
		final List memberList = new ArrayList();
		int ret = root.walkChildren(new TreeNodeCallback() {

			/**
			 * callback find node matching member Path exactly
			 */
			public int handleTreeNode(TreeNode node) {
				int iDimNode = node.getLevel() - 1;
				if (iDimNode < iDim)
					return TreeNodeCallback.CONTINUE; // we are below iDim,
														// don't care

				// iDimNode == iDim
				// node Exp must contain children of member[iDim]
				final Object ref = node.getReference();
				if (ref instanceof mondrian.olap.Member) {
					mondrian.olap.Member m = (mondrian.olap.Member) ref;
					if (!memberList.contains(m))
						memberList.add(m);
				} else if (ref instanceof MemberExpr) {
					mondrian.olap.Member m = ((MemberExpr) ref).getMember();
					if (!memberList.contains(m))
						memberList.add(m);
				} else {
					// must be FunCall
					FunCall f = (FunCall) ref;
					boolean b = MondrianUtil.resolveFunCallMembers(f,
							memberList, scr);
					if (!b)
						return TreeNodeCallback.BREAK; // indicate: cannot
														// handle
				}
				return TreeNodeCallback.CONTINUE_SIBLING; // continue next
															// sibling
			}
		});
		if (ret == TreeNodeCallback.BREAK)
			return null;
		else
			return memberList;
	}

	static boolean isCallTo(FunCall f, String name) {
		return f.getFunName().compareToIgnoreCase(name) == 0;
	}

	/**
	 * 
	 * @param f
	 * @param memberList
	 * @return true, if Funcalls could be handled, otherwise false
	 */
	static boolean resolveFunCallMembers(FunCall f, List memberList,
			SchemaReader scr) {
		boolean canHandle = true;
		if (isCallTo(f, "Children")) {
			mondrian.olap.Member m = ((MemberExpr) f.getArg(0)).getMember();
			List members = scr.getMemberChildren(m);
			for (int i = 0; i < members.size(); i++) {
				if (!memberList.contains(members.get(i)))
					memberList.add(members.get(i));
			}
		} else if (isCallTo(f, "Descendants")) {
			mondrian.olap.Member m = ((MemberExpr) f.getArg(0)).getMember();
			mondrian.olap.Level level = ((LevelExpr) f.getArg(1)).getLevel();
			mondrian.olap.Member[] members = MondrianUtil.getMemberDescendants(
					scr, m, level);
			for (int i = 0; i < members.length; i++) {
				if (!memberList.contains(members[i]))
					memberList.add(members[i]);
			}
		} else if (isCallTo(f, "Members")) {
			mondrian.olap.Level level = ((LevelExpr) f.getArg(0)).getLevel();
			List members = scr.getLevelMembers(level, false);
			for (int i = 0; i < members.size(); i++) {
				if (!memberList.contains(members.get(i)))
					memberList.add(members.get(i));
			}
		} else if (isCallTo(f, "Union")) {
			resolveFunCallMembers((FunCall) f.getArg(0), memberList, scr);
			resolveFunCallMembers((FunCall) f.getArg(1), memberList, scr);
		} else if (isCallTo(f, "{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				final Exp arg = f.getArg(i);
				if (arg instanceof MemberExpr) {
					Member member = ((MemberExpr) arg).getMember();
					if (!memberList.contains(member))
						memberList.add(member);
				}
			}
		} else {
			canHandle = false;
		}
		return canHandle;
	}

	/**
	 * creates a parameter name for a member
	 */
	public static String defaultParamName(Member m) {
		Dimension d = m.getDimension();
		String s = d.getName();
		s.replace(' ', '_');
		// s.replaceAll("[ \\.\"'!�$%&/()=?�������#-:;,]", "");
		return s + "_param";
	}

	/**
	 * Determine if the Member is GUI visible.
	 * 
	 * @param scr
	 *            the SchemaReader
	 * @param member
	 *            the Mondrian Member
	 * @return "true" if Member is GUI visible
	 */
	public static boolean isVisible(SchemaReader scr,
			mondrian.olap.Member member) {
		if (!scr.isVisible(member)) {
			// Is Schema and Role visible
			return false;
		} else {
			// Is GUI visible
			Object visible = member
					.getPropertyValue(mondrian.olap.Property.VISIBLE.name);
			return !Boolean.FALSE.equals(visible);
		}
	}

	public static mondrian.olap.Hierarchy[] removeNull(
			mondrian.olap.Hierarchy[] hierarchies) {
		List list = new ArrayList();
		for (int i = 0; i < hierarchies.length; i++) {
			if (hierarchies[i] != null)
				list.add(hierarchies[i]);
		}
		return (mondrian.olap.Hierarchy[]) list
				.toArray(new mondrian.olap.Hierarchy[list.size()]);
	}
} // End MondrianUtil.java
