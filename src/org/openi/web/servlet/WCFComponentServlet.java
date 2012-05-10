package org.openi.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.pentaho.platform.web.servlet.ServletBase;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

import org.openi.wcf.component.WCFComponentType;
import org.openi.wcf.component.WCFRender;
import org.openi.util.wcf.WCFUtils;

/**
 * 
 * @author SUJEN
 * 
 */
public class WCFComponentServlet extends HttpServlet {

	private static final Log logger = LogFactory
			.getLog(WCFComponentServlet.class);

	public WCFComponentServlet() {
		logger.debug("wcf component servlet instantiated");
	}
	
	public Log getLogger() {
		return WCFComponentServlet.logger;
	}

	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		process(request, response);
	}

	@Override
	protected void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		process(request, response);
	}

	protected void process(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		
		response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
	    PentahoSystem.systemEntryPoint();
	    
		String componentType = request.getParameter("componentType");
		String pivotID = request.getParameter("pivotID");
		if (componentType == null || componentType.equals("")
				|| pivotID == null || pivotID.equals(""))
			return;
		try {
			if (componentType.equals(WCFComponentType.TABLE)) {
				renderTableComponent(pivotID, request, response);
			} else if (componentType.equals(WCFComponentType.CHART)) {
				renderChartComponent(pivotID, request, response);
			} else if (componentType.equals(WCFComponentType.NAVIGATOR)) {
				renderNavComponent(pivotID, request, response);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			PentahoSystem.systemExitPoint();
		}
	}

	private void renderNavComponent(String pivotID,
			final HttpServletRequest request, final HttpServletResponse response)
			throws Exception {
		/*String compID = "xmlaNav" + pivotID;
		String compString = WCFUtils.componentToHTMLString(new WCFRender("",
				true, compID), null, compID);*/
		
		response.setContentType("text/plain");
		logger.info("rendering navigator component");
		response.getWriter().println("Nav");
	}

	private void renderChartComponent(String pivotID,
			final HttpServletRequest request, final HttpServletResponse response)
			throws Exception {
		/*String compID = "chart" + pivotID;
		String compString = WCFUtils.componentToHTMLString(new WCFRender("",
				true, compID), null, compID);*/
		
		response.setContentType("text/plain");
		logger.info("rendering chart component");
		response.getWriter().println("Chart");
	}

	private void renderTableComponent(String pivotID,
			final HttpServletRequest request, final HttpServletResponse response)
			throws Exception {
		/*String compID = "table" + pivotID;
		String compString = WCFUtils.componentToHTMLString(new WCFRender("",
				true, compID), null, compID);*/
		
		response.setContentType("text/plain");
		logger.info("rendering table component");
		response.getWriter().println("Table");
	}

}
