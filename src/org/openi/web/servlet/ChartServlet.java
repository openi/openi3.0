package org.openi.web.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.urls.CategoryURLGenerator;
import org.openi.analysis.Analysis;
import org.openi.chart.EnhancedChartFactory;

import com.tonbeller.jpivot.olap.model.OlapModel;

/**
 * 
 * @author SUJEN
 *
 */
public class ChartServlet extends HttpServlet {
	
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
		int width = Integer.parseInt(request.getParameter("width"));
		int height = Integer.parseInt(request.getParameter("height"));
		String pivotID = request.getParameter("pivotID");
		
		Map<String, Analysis> loadedAnalyses = (Map) request.getSession().getAttribute("loadedAnalyses");
		Analysis analysis = loadedAnalyses.get(pivotID);
		OlapModel olapModel = (OlapModel) request.getSession().getAttribute("xmlaQuery" + pivotID);
		
		try {
			createChart(response.getOutputStream(), analysis, olapModel, width, height, request.getLocale(), null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createChart(OutputStream out, Analysis analysis,
			OlapModel olapModel, int width, int height, Locale locale,CategoryURLGenerator urlGenerator) throws Exception{
		try {		
			EnhancedChartFactory.createChart(out, analysis, olapModel, width, height, locale, null, null, null, false);
		} catch (Exception e) {
			throw e;
		} finally {
			 try {
				 out.close();
			 } catch (Throwable t) {}
		}
	}

}
