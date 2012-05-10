package org.openi.olap.xmla;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.FunCall;
import com.tonbeller.jpivot.olap.mdxparse.Literal;
import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.util.TreeNode;
import com.tonbeller.jpivot.util.TreeNodeCallback;

/**
 * static XMLA Utils
 * @author SUJEN
 */
public class XMLA_Util {

	static Logger logger = Logger.getLogger(XMLA_Util.class);

	/**
	 * generate Exp[] from Member[]
	 * 
	 * @param members
	 * @return Exp[]
	 * @deprecated
	 */
	public static Exp[] member2Exp(Member[] members) {
		Exp[] memExp = new Exp[members.length];
		for (int i = 0; i < memExp.length; i++) {
			memExp[i] = Literal.createSymbol(((XMLA_Member) members[i])
					.getUniqueName());
		}
		return memExp;
	}

	/**
	 * generate Exp from Member
	 * 
	 * @param member
	 * @return Exp
	 * @deprecated
	 */
	public static Exp member2Exp(Member member) {
		return Literal.createSymbol(member.getLabel());
	}

	/**
	 * determine hierarchy index on axis
	 * 
	 * @param axis
	 * @param member
	 * @return int
	 */
	public static int hierIndex(XMLA_Axis axis, XMLA_Member member) {
		XMLA_Level lev = (XMLA_Level) member.getLevel();
		XMLA_Hierarchy hier = (XMLA_Hierarchy) lev.getHierarchy();

		return hierIndex(axis, hier);
	}

	/**
	 * determine hierarchy index on axis
	 * 
	 * @param axis
	 * @param hier
	 * @return int
	 */
	public static int hierIndex(XMLA_Axis axis, XMLA_Hierarchy hier) {

		Hierarchy[] hiersOfAxis = axis.getHierarchies();
		for (int j = 0; j < hiersOfAxis.length; j++) {
			if (((XMLA_Hierarchy) hiersOfAxis[j]).isEqual(hier)) {
				return j;
			}
		}
		return -1; // should never get here

	}

	/**
	 * find the result axis for a member
	 */
	public static int axisOrdinalForMember(Axis[] axes, XMLA_Member member) {
		XMLA_Level lev = (XMLA_Level) member.getLevel();
		XMLA_Hierarchy hier = (XMLA_Hierarchy) lev.getHierarchy();

		return axisOrdinalForHier(axes, hier);
	}

	/**
	 * find the result axis for a hierarchy
	 */
	public static int axisOrdinalForHier(Axis[] axes, XMLA_Hierarchy hier) {

		AxesLoop: for (int i = 0; i < axes.length; i++) {
			Hierarchy[] hiersOfAxis = axes[i].getHierarchies();
			for (int j = 0; j < hiersOfAxis.length; j++) {
				if (((XMLA_Hierarchy) hiersOfAxis[j]).isEqual(hier)) {
					return i;
				}
			}
		}
		return -1; // should never get here

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
	public static XMLA_Member[] getMemberDescendants(XMLA_Member member,
			XMLA_Level level) throws OlapException {
		int depth = level.getDepth();
		XMLA_Level lev = (XMLA_Level) member.getLevel();
		if (depth <= lev.getDepth())
			return new XMLA_Member[0];
		// XMLA_Member[] currentMembers = new XMLA_Member[] { member };
		List currentMembers = new ArrayList();
		currentMembers.add(member);
		while (depth > lev.getDepth()) {
			lev = lev.getChildLevel();
			List aMembers = new ArrayList();
			for (Iterator iter = currentMembers.iterator(); iter.hasNext();) {
				XMLA_Member m = (XMLA_Member) iter.next();
				XMLA_Member[] mems = m.getChildren();
				for (int i = 0; i < mems.length; i++) {
					aMembers.add(mems[i]);
				}
			}
			currentMembers = aMembers;
		}
		return (XMLA_Member[]) currentMembers.toArray(new XMLA_Member[0]);
	}

	/**
	 * @param ancestor
	 * @param descendant
	 */
	public static boolean isDescendant(XMLA_Member ancestor,
			XMLA_Member descendant) {
		// a calculated member, even if defined under "ancestor" is *not*
		// descendant,
		// WITM MEMBER a.b as '..'
		// a.children does *not* include b
		if (descendant.isCalculated())
			return false;
		if (ancestor.equals(descendant))
			return false;
		int ancestorLevelNumber = ((XMLA_Level) ancestor.getLevel()).getDepth();
		XMLA_Member mm = descendant;
		while (mm != null
				&& ancestorLevelNumber < ((XMLA_Level) mm.getLevel())
						.getDepth()) {
			try {
				mm = (XMLA_Member) mm.getParent();
			} catch (OlapException e) {
				logger.error("?", e); // should not occur
				break;
			}
		}

		if (mm.equals(ancestor))
			return true;
		else
			return false;
	}

	/**
	 * @return an Expression Object for the top level members of an hierarchy
	 */
	public static Exp topLevelMembers(Hierarchy hier, boolean expandAllMember) {
		XMLA_Level topLevel = (XMLA_Level) ((XMLA_Hierarchy) hier).getLevels()[0];
		XMLA_Member mAll = (XMLA_Member) ((XMLA_Hierarchy) hier).getAllMember();
		// if there is an All Member, we will have to expand it
		// according to expandAllMember flag
		if (mAll != null) {
			XMLA_Member[] memar = new XMLA_Member[] { mAll };
			Exp mAllSet = new FunCall("{}", memar, FunCall.TypeBraces);
			if (!expandAllMember) {
				return mAll;
			}
			// must expand
			// create Union({AllMember}, AllMember.children)
			Exp mAllChildren = new FunCall("children", memar,
					FunCall.TypeProperty);
			Exp union = new FunCall("union",
					new Exp[] { mAllSet, mAllChildren }, FunCall.TypeFunction);
			return union;
		}

		XMLA_Member[] topMembers;
		try {
			// HHTASK ok, for a parent-child hierarchy ?
			topMembers = topLevel.getMembers();
		} catch (OlapException e) {
			// should not occur
			logger.error("?", e);
			return null;
		}
		if (topMembers.length == 1)
			return topMembers[0]; // single member
		return new FunCall("{}", topMembers, FunCall.TypeBraces);
	}

	/**
	 * 
	 * @param root
	 * @param iDim
	 * @return
	 */
	static List collectMembers(TreeNode root, final int iDim) {
		if (root == null)
			return null;
		final List memberList = new ArrayList();
		root.walkChildren(new TreeNodeCallback() {

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
				Exp exp = (Exp) node.getReference();
				if (exp instanceof XMLA_Member) {
					XMLA_Member m = (XMLA_Member) exp;
					if (!memberList.contains(m))
						memberList.add(m);
				} else {
					// must be FunCall
					FunCall f = (FunCall) exp;
					try {
						resolveFunCallMembers(f, memberList);
					} catch (OlapException e) {
						logger.error("?", e);
					}
				}
				return TreeNodeCallback.CONTINUE_SIBLING; // continue next
															// sibling
			}
		});
		return memberList;
	}

	/**
	 * 
	 * @param f
	 * @param memberList
	 */
	static void resolveFunCallMembers(FunCall f, List memberList)
			throws OlapException {
		if (f.isCallTo("Children")) {
			XMLA_Member m = (XMLA_Member) f.getArgs()[0];
			XMLA_Member[] members = m.getChildren();
			for (int i = 0; i < members.length; i++) {
				if (!memberList.contains(members[i]))
					memberList.add(members[i]);
			}
		} else if (f.isCallTo("Descendants")) {
			XMLA_Member m = (XMLA_Member) f.getArgs()[0];
			XMLA_Level level = (XMLA_Level) f.getArgs()[1];
			XMLA_Member[] members = XMLA_Util.getMemberDescendants(m, level);
			for (int i = 0; i < members.length; i++) {
				if (!memberList.contains(members[i]))
					memberList.add(members[i]);
			}
		} else if (f.isCallTo("Members")) {
			XMLA_Level level = (XMLA_Level) f.getArgs()[0];
			XMLA_Member[] members = level.getMembers();
			for (int i = 0; i < members.length; i++) {
				if (!memberList.contains(members[i]))
					memberList.add(members[i]);
			}
		} else if (f.isCallTo("Union")) {
			resolveFunCallMembers((FunCall) f.getArgs()[0], memberList);
			resolveFunCallMembers((FunCall) f.getArgs()[1], memberList);
		} else if (f.isCallTo("{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				if (!memberList.contains(f.getArgs()[i]))
					memberList.add(f.getArgs()[i]);
			}
		} else if (
		// we cannot handle this properly, just return members of base set
		f.isCallTo("TopCount") || f.isCallTo("BottomCount")
				|| f.isCallTo("TopPercent") || f.isCallTo("BottomPercent")) {
			resolveFunCallMembers((FunCall) f.getArgs()[0], memberList);
		} else {
			logger.error("invalid FunCall encountered " + f.getFunction());
		}
	}

	/**
	 * Map function names to XMLA Function type
	 * 
	 * @param fuName
	 * @return Syntax type
	 */
	public static int funCallSyntax(String fuName) {
		if (fuName.equals("()"))
			return FunCall.TypeParentheses;
		else if (fuName.equals("{}"))
			return FunCall.TypeBraces;
		else if (fuName.equalsIgnoreCase("members"))
			return FunCall.TypeProperty;
		else if (fuName.equalsIgnoreCase("children"))
			return FunCall.TypeProperty;
		else
			return FunCall.TypeFunction;
		// HHTASK complete
	}
} // End XMLA_Util
