package org.openi.analysis.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.openi.analysis.Analysis;
import org.openi.chart.EnhancedChartComponent;
import org.openi.service.exception.ServiceException;
import org.openi.util.file.FileUtils;
import org.openi.util.plugin.PluginUtils;
import org.openi.util.xml.XmlUtils;
import org.w3c.dom.Document;

import com.tonbeller.jpivot.print.PrintComponent;
import com.tonbeller.jpivot.table.TableComponent;
import com.tonbeller.wcf.component.RendererParameters;
import com.tonbeller.wcf.controller.RequestContext;

/**
 * 
 * @author SUJEN
 * 
 */
public class ExcelExporter implements AnalysisExporter {

	private static Logger logger = Logger.getLogger(ExcelExporter.class);

	@SuppressWarnings("unchecked")
	@Override
	public void export(Analysis analysis, String pivotID, File file,
			RequestContext context) throws Exception {
		RendererParameters.setParameter(context.getRequest(), "mode", "excel",
				"request");

		Map parameters = new HashMap();
		PrintComponent printConfig = (PrintComponent) context
				.getModelReference("print" + pivotID);
		if (printConfig != null) {
			if (printConfig.isSetTableWidth()) {
				parameters.put(printConfig.PRINT_TABLE_WIDTH, new Double(
						printConfig.getTableWidth()));
			}
			if (printConfig.getReportTitle().trim().length() != 0) {
				parameters.put(printConfig.PRINT_TITLE, printConfig
						.getReportTitle().trim());
			}
			parameters.put(printConfig.PRINT_PAGE_ORIENTATION,
					printConfig.getPageOrientation());
			parameters.put(printConfig.PRINT_PAPER_TYPE,
					printConfig.getPaperType());
			if (printConfig.getPaperType().equals("custom")) {
				parameters.put(printConfig.PRINT_PAGE_WIDTH, new Double(
						printConfig.getPageWidth()));
				parameters.put(printConfig.PRINT_PAGE_HEIGHT, new Double(
						printConfig.getPageHeight()));
			}
			parameters.put(printConfig.PRINT_CHART_PAGEBREAK, new Boolean(
					printConfig.isChartPageBreak()));

		}
		
		
		if (analysis.isShowChart()) {
			EnhancedChartComponent chartComponent = (EnhancedChartComponent) context
				.getSession().getAttribute("chart" + pivotID);
			
			Document doc = chartComponent.render(context);
			if(doc == null)
				throw new Exception("chart component not loaded properly");
			
			//ignore the document, not returning the chart html, so need to transform chart component doc into html, using chart.xsl 
			
			String chartFilename = chartComponent.getFilename();
			
			String host = context.getRequest().getServerName();
			int port = context.getRequest().getServerPort();
			String location = context.getRequest().getContextPath();
			String scheme = context.getRequest().getScheme();

			/*String chartResourceURL = scheme + "://" + host + ":" + port
					+ location + "/plugin/openi/api/wcfCompResource/";
			chartResourceURL += "wcfChartComp";
			chartResourceURL += "?inline=false&amp;pivotID=" + pivotID + "&amp;chartWidth="
					+ analysis.getChartWidth() + "&amp;chartHeight="
					+ analysis.getChartHeight() + "&amp;chartType="
					+ analysis.getChartType();*/
					
			String chartResourceURL = new File(System.getProperty("java.io.tmpdir"), chartFilename).getAbsolutePath();
			parameters.put("chartimage", chartResourceURL);
			parameters.put("chartheight", analysis.getChartHeight());
			parameters.put("chartwidth", analysis.getChartWidth());
		}
		
		
		String tableCompID = "table" + pivotID;
		TableComponent tableComp = (TableComponent) (context.getSession()
				.getAttribute(tableCompID));
		Document doc = tableComp.render(context);
		if (doc == null)
			throw new ServiceException(
					"Error while exporting the analysis report with pivod ID : "
							+ pivotID);

		DOMSource source = new DOMSource(doc);
		// set up xml transformation
		Transformer transformer = XmlUtils.getTransformer(getFOPXSLUri(), true);
		for (Iterator it = parameters.keySet().iterator(); it.hasNext();) {
			String name = (String) it.next();
			Object value = parameters.get(name);
			transformer.setParameter(name, value);
		}
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		// do transform
		transformer.transform(source, result);
		sw.flush();

		OutputStream out = new FileOutputStream(file);
		out.write(sw.toString().getBytes());

		RendererParameters.removeParameter(context.getRequest(), "mode",
				"excel", "request");

	}

	private static File getFOPXSLUri() {
		return new File(FileUtils.applyUnixFileSeperator(PluginUtils
				.getPluginDir()), "ui/resources/jpivot/table/xls_mdxtable.xsl");
	}

}
