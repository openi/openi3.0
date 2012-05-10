package org.openi.navigator.member;

import java.util.List;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.wcf.selection.DefaultSelectionModel;

/**
 * A SelectionModel that allows only <code>Members</code> to be selected. The
 * Model may contain objects of other types but only Members are selectable.
 * 
 * @author av
 * @author SUJEN
 * 
 */
public class MemberSelectionModel extends DefaultSelectionModel {

	List orderedSelection;

	/**
	 * Constructor for MemberSelectionModel.
	 */
	public MemberSelectionModel() {
		super();
	}

	/**
	 * Constructor for MemberSelectionModel.
	 * 
	 * @param mode
	 */
	public MemberSelectionModel(int mode) {
		super(mode);
	}

	/**
	 * true if item is a member
	 */
	public boolean isSelectable(Object item) {
		return super.isSelectable(item) && item instanceof Member;
	}

	public void setOrderedSelection(List list) {
		super.setSelection(list);
		this.orderedSelection = list;
	}

	public List getOrderedSelection() {
		return orderedSelection;
	}

}
