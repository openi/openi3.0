package org.openi.olap;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.navi.ClickableExtension;
import com.tonbeller.jpivot.olap.navi.ClickableExtensionImpl;
import com.tonbeller.jpivot.table.ClickableMember;
import com.tonbeller.jpivot.tags.OlapModelProxy;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * places a table model into the session
 */
public abstract class OlapModelTag extends BodyTagSupport {

	private static Logger logger = Logger.getLogger(OlapModelTag.class);

	private List clickables;
	private String queryName;
	private boolean stackMode = true;

	public OlapModelTag() {
	}

	public void release() {
		super.release();
		stackMode = true;
	}

	public void addClickable(ClickableMember clickable) {
		clickables.add(clickable);
	}

	public int doStartTag() throws JspException {
		clickables = new ArrayList();
		return EVAL_BODY_BUFFERED;
	}

	public int doEndTag() throws JspException {
		try {
			logger.info("enter");
			RequestContext context = RequestContext.instance();
			OlapModel om = getOlapModel(context);
			om = (OlapModel) om.getTopDecorator();
			om.setLocale(context.getLocale());
			om.setID(id);

			HttpSession session = pageContext.getSession();
			om.setServletContext(session.getServletContext());

			ClickableExtension ext = (ClickableExtension) om
					.getExtension(ClickableExtension.ID);
			if (ext == null) {
				ext = new ClickableExtensionImpl();
				om.addExtension(ext);
			}
			ext.setClickables(clickables);

			OlapModelProxy omp = OlapModelProxy
					.instance(id, session, stackMode);
			if (queryName != null)
				omp.initializeAndShow(queryName, om);
			else
				omp.initializeAndShow(om);

			return EVAL_PAGE;
		} catch (Exception e) {
			logger.error(null, e);
			throw new JspException(e);
		}
	}

	protected abstract OlapModel getOlapModel(RequestContext context)
			throws Exception;

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public void setStackMode(boolean stackMode) {
		this.stackMode = stackMode;
	}
}
