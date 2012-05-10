package org.openi.olap.mondrian;

import mondrian.olap.Exp;
import mondrian.olap.Syntax;
import mondrian.mdx.UnresolvedFunCall;
import mondrian.mdx.MemberExpr;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.util.TreeNode;

/**
 * support old stuff as of MDX version 2 (Memento) for a while HHTASK: remove,
 * when old Bookmarks are replaced
 */

public class MondrianOldStuff {

	static Logger logger = Logger.getLogger(MondrianModel.class);

	/**
	 * create Position Tree from old Member Sets
	 * 
	 * @param quax
	 * @param quaxBean
	 */
	static void handleQubonMode(MondrianQuax quax, MondrianQuaxBean quaxBean,
			MondrianModel model) {

		MondrianMemberSetBean[] msbs = quaxBean.getMemberSets();

		// Qubon mode: create Position Tree from member sets
		TreeNode rootNode = new TreeNode(null);
		TreeNode currentNode = rootNode;
		for (int j = 0; j < msbs.length; j++) {
			int msbType = msbs[j].getType();
			TreeNode node = null;
			switch (msbType) {
			case 0: // member list
				String[] memberList = msbs[j].getMemberList();
				MemberExpr[] monMembers = new MemberExpr[memberList.length];
				for (int k = 0; k < monMembers.length; k++) {
					MondrianMember m = (MondrianMember) model
							.lookupMemberByUName(memberList[k]);
					if (m == null) {
						logger.error("old Memento Object is invalid, Axis "
								+ quax.getOrdinal()
								+ " #Member could not be found "
								+ memberList[k]);
						return;
					}
					monMembers[k] = new MemberExpr(m.getMonMember());
				}
				UnresolvedFunCall fSet = new UnresolvedFunCall("{}",
						Syntax.Braces, monMembers);
				node = new TreeNode(fSet);
				currentNode.addChildNode(node);
				break;
			case 1: // member children
				memberList = msbs[j].getMemberList();

				MondrianMember m = (MondrianMember) model
						.lookupMemberByUName(memberList[0]);
				if (m == null) {
					logger.error("old Memento Object is invalid, Axis "
							+ quax.getOrdinal()
							+ " #Member could not be found " + memberList[0]);
					return;
				}
				final mondrian.olap.Member monMember = m.getMonMember();

				UnresolvedFunCall fChildren = new UnresolvedFunCall("Children",
						Syntax.Property,
						new Exp[] { new MemberExpr(monMember) });
				node = new TreeNode(fChildren);
				currentNode.addChildNode(node);
				break;
			default:
				logger.error("old Memento Object is invalid, Axis "
						+ quax.getOrdinal() + " unexpected member set type "
						+ msbType);
				return;
			}

			currentNode = node;
		} // for member sets

		quax.setPosTreeRoot(rootNode, true);
	}

	/**
	 * apply old DrillExes to quax
	 * 
	 * @param quax
	 * @param quaxBean
	 */
	static void handleDrillExMode(MondrianQuax quax, MondrianQuaxBean quaxBean,
			MondrianModel model) {

		try {
			// quax position tree must be initialized here
			model.getResult();
		} catch (OlapException e) {
			logger.error("old Memento Object , Axis " + quax.getOrdinal()
					+ " Exception getResult");
			e.printStackTrace();
			return;
		}
		MondrianDrillExBean[] drillExes = quaxBean.getDrillExes();
		for (int j = 0; j < drillExes.length; j++) {
			String[] pathMembers = drillExes[j].getPathMembers();

			Member[] members = new Member[pathMembers.length];
			for (int k = 0; k < members.length; k++) {
				Member m = model.lookupMemberByUName(pathMembers[k]);
				if (m == null) {
					logger.error("old Memento Object is invalid, Axis "
							+ quax.getOrdinal()
							+ " drillex Member could not be found "
							+ pathMembers[k]);
					return;
				}
				members[k] = m;

			} // for path members

			if (quax.canExpand(members))
				quax.expand(members);

		} // for drillExes
	}

} // MondrianOldStuff
