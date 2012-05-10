package org.openi.navigator.member;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.navi.MemberTree;
import com.tonbeller.wcf.tree.AbstractTreeModel;
import com.tonbeller.wcf.tree.TreeModel;

/**
 * maps the MemberTree table extension to a wcf tree model
 * 
 * @author av
 * @author SUJEN
 * 
 */
public class TreeModelAdapter extends AbstractTreeModel implements TreeModel {

	Hierarchy[] hiers;
	MemberTree tree;
	Level noChildrenLevel = null;
	Locale locale;
	boolean showSingleHierarchyNode = false;

	interface OverflowListener {
		void overflowOccured();
	}

	OverflowListener overflowListener;

	private static Logger logger = Logger.getLogger(TreeModelAdapter.class);

	/**
	 * Constructor for TreeModelAdapter. Root nodes are the root nodes of the
	 * hierarchy.
	 */
	public TreeModelAdapter(Hierarchy hier, MemberTree tree, Locale locale) {
		this.hiers = new Hierarchy[] { hier };
		this.tree = tree;
		this.locale = locale;
	}

	/**
	 * Constructor for TreeModelAdapter. Root nodes are the hierarchies.
	 */
	public TreeModelAdapter(Hierarchy[] hiers, MemberTree tree, Locale locale) {
		this.hiers = hiers;
		this.tree = tree;
		this.locale = locale;
	}

	/**
	 * if one hierarchy return its root members. if multiple, return the
	 * hierarchies
	 */
	public Object[] getRoots() {
		try {
			if (showSingleHierarchyNode || hiers.length > 1)
				return hiers;
			return tree.getRootMembers(hiers[0]);
		} catch (MemberTree.TooManyMembersException e) {
			overflowOccured(e);
			return new Member[0];
		}
	}

	public boolean hasChildren(Object node) {
		if (node instanceof Hierarchy)
			return true;
		if (noChildrenLevel != null) {
			Member m = (Member) node;
			if (noChildrenLevel.equals(m.getLevel()))
				return false;
		}
		return tree.hasChildren((Member) node);
	}

	public Object[] getChildren(Object node) {
		try {
			if (node instanceof Hierarchy)
				return tree.getRootMembers((Hierarchy) node);
			Member[] children = (Member[]) tree.getChildren((Member) node);
			if (children == null)
				return new Member[0];
			return children;
		} catch (MemberTree.TooManyMembersException e) {
			overflowOccured(e);
			return new Member[0];
		}
	}

	/**
	 * @see com.tonbeller.wcf.tree.TreeModel#getParent(Object)
	 */
	public Object getParent(Object node) {
		if (showSingleHierarchyNode || hiers.length > 1) {
			if (node instanceof Hierarchy)
				return null;
			Object parent = tree.getParent((Member) node);
			if (parent == null)
				return ((Member) node).getLevel().getHierarchy();
			return parent;
		}
		return tree.getParent((Member) node);
	}

	public void fireModelChanged() {
		super.fireModelChanged();
	}

	/**
	 * members from this level will pretend to have no children
	 */
	public Level getNoChildrenLevel() {
		return noChildrenLevel;
	}

	/**
	 * members from this level will pretend to have no children
	 */
	public void setNoChildrenLevel(Level level) {
		noChildrenLevel = level;
	}

	public void modelChanged() {
		super.fireModelChanged();
	}

	private void overflowOccured(Exception e) {
		logger.error(null, e);
		if (overflowListener != null)
			overflowListener.overflowOccured();
	}

	public OverflowListener getOverflowListener() {
		return overflowListener;
	}

	public void setOverflowListener(OverflowListener overflowListener) {
		this.overflowListener = overflowListener;
	}

	public boolean isShowSingleHierarchyNode() {
		return showSingleHierarchyNode;
	}

	public void setShowSingleHierarchyNode(boolean showSingleHierarchyNode) {
		this.showSingleHierarchyNode = showSingleHierarchyNode;
	}
}