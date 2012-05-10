package org.openi.table;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.navi.DrillExpandMember;
import com.tonbeller.jpivot.table.span.Span;

/**
 * @author av
 * @author SUJEN
 */
public class DrillExpandMemberUI extends DrillExpandUI {

	public static final String ID = "drillMember";

	public String getId() {
		return ID;
	}

	DrillExpandMember extension;

	protected boolean initializeExtension() {
		extension = (DrillExpandMember) table.getOlapModel().getExtension(
				DrillExpandMember.ID);
		return extension != null;
	}

	/**
	 * @see com.tonbeller.jpivot.ui.table.navi.DrillExpandUI#canExpand(Span)
	 */
	protected boolean canExpand(Span span) {
		if (positionContainsMember(span))
			return extension.canExpand((Member) span.getMember()
					.getRootDecoree());
		return false;
	}

	/**
	 * @see com.tonbeller.jpivot.ui.table.navi.DrillExpandUI#expand(Span)
	 */
	protected void expand(Span span) {
		extension.expand((Member) span.getMember().getRootDecoree());
	}

	/**
	 * @see com.tonbeller.jpivot.ui.table.navi.DrillExpandUI#canCollapse(Span)
	 */
	protected boolean canCollapse(Span span) {
		if (positionContainsMember(span))
			return extension.canCollapse((Member) span.getMember()
					.getRootDecoree());
		return false;
	}

	/**
	 * @see com.tonbeller.jpivot.ui.table.navi.DrillExpandUI#collapse(Span)
	 */
	protected void collapse(Span span) {
		extension.collapse((Member) span.getMember().getRootDecoree());
	}

	protected String getCollapseImage() {
		return "collapse-icon.png";
	}

	protected String getExpandImage() {
		return "expand-icon.png";
	}

	protected String getOtherImage() {
		return "drill-member-other.gif";
	}

	@Override
	public Object getBookmarkState(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
