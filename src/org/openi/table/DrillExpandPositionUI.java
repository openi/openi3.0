package org.openi.table;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.model.Position;
import com.tonbeller.jpivot.olap.navi.DrillExpandPosition;
import com.tonbeller.jpivot.table.span.Span;

/**
 * 
 * @author av
 * @author SUJEN
 */
public class DrillExpandPositionUI extends DrillExpandUI {
	public static final String ID = "drillPosition";

	public String getId() {
		return ID;
	}

	DrillExpandPosition expandPosition;

	protected boolean initializeExtension() {
		OlapModel om = table.getOlapModel();
		expandPosition = (DrillExpandPosition) om
				.getExtension(DrillExpandPosition.ID);
		return expandPosition != null;
	}

	protected boolean canExpand(Span span) {
		if (!positionContainsMember(span))
			return false;
		return expandPosition.canExpand((Position) span.getPosition()
				.getRootDecoree(), (Member) span.getMember().getRootDecoree());
	}

	protected boolean canCollapse(Span span) {
		if (!positionContainsMember(span))
			return false;
		return expandPosition.canCollapse((Position) span.getPosition()
				.getRootDecoree(), (Member) span.getMember().getRootDecoree());
	}

	protected void expand(Span span) {
		expandPosition.expand((Position) span.getPosition().getRootDecoree(),
				(Member) span.getMember().getRootDecoree());
	}

	protected void collapse(Span span) {
		expandPosition.collapse((Position) span.getPosition().getRootDecoree(),
				(Member) span.getMember().getRootDecoree());
	}

	protected String getCollapseImage() {
		return "collapse-icon.png";
	}

	protected String getExpandImage() {
		return "expand-icon.png";
	}

	protected String getOtherImage() {
		return "drill-position-other.gif";
	}

	@Override
	public Object getBookmarkState(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
