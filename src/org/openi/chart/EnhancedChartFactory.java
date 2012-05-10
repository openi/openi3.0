package org.openi.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.TableOrder;
import org.jfree.util.UnitType;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleInsets;

import org.openi.analysis.Analysis;

import com.tonbeller.jpivot.chart.ChartFactory;
import com.tonbeller.jpivot.olap.model.Cell;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.model.Result;
import com.tonbeller.jpivot.olap.navi.DrillThrough;
import com.tonbeller.wcf.controller.Dispatcher;
import com.tonbeller.wcf.controller.DispatcherSupport;
import com.tonbeller.wcf.utils.DomUtils;

/**
 * 
 * @author SUJEN
 *
 */
public class EnhancedChartFactory extends ChartFactory{
	private static Logger logger = Logger.getLogger(ChartFactory.class);

	protected static Result result;
	protected static OlapModel olapModel;
	protected static Dispatcher dispatcher = new DispatcherSupport();

	public EnhancedChartFactory() {
		super();
	}

	/**
	 * wrapper method that uses openi domain objects as input, returns chart.
	 * 
	 * @param analysis
	 * @param dataset
	 * @return
	 * @throws OlapException
	 */
	public static JFreeChart createChart(Analysis analysis,
			OlapModel olapModel, Locale locale, List colorPalette,
			CategoryURLGenerator urlGenerator, String webControllerURL,
			int width, int height, boolean suppressCrowdedLabels)
			throws OlapException {
		logger.debug("creating chart for analysis: "
				+ analysis.getAnalysisTitle());

		JFreeChart chart = EnhancedChartFactory
				.createChart(
						olapModel,
						analysis.getChartType(),
						analysis.getChartTitle(),
						analysis.getHorizAxisLabel(),
						analysis.getVertAxisLabel(),
						analysis.isShowLegend(),
						true,
						analysis.isDrillThroughEnabled(),
						new Font(analysis.getFontName(), analysis
								.getFontStyle(), analysis.getFontSize()),
						new Color(analysis.getBgColorR(), analysis
								.getBgColorG(), analysis.getBgColorB()),
						new Font(analysis.getSlicerFontName(), analysis
								.getSlicerFontStyle(), analysis
								.getSlicerFontSize()),
						new Font(analysis.getAxisFontName(), analysis
								.getAxisFontStyle(), analysis.getAxisFontSize()),
						new Font(analysis.getAxisTickFontName(), analysis
								.getAxisTickFontStyle(), analysis
								.getAxisTickFontSize()),
						new Font(analysis.getLegendFontName(), analysis
								.getLegendFontStyle(), analysis
								.getLegendFontSize()), analysis
								.getLegendPosition(), analysis
								.getTickLabelRotate(), 1.0f, analysis
								.isShowSlicer(), analysis.getSlicerPosition(),
						analysis.getSlicerAlignment(),
						analysis.getShowPareto(), locale, colorPalette,
						urlGenerator, webControllerURL, width, height,
						suppressCrowdedLabels);

		return chart;
	}

	/**
	 * method used to stream chart directly to the output stream. Intended for
	 * use in a servlet (e.g. - dashboard servlet).
	 * 
	 * @param out
	 * @param analysis
	 * @param dataset
	 * @param width
	 * @param height
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws OlapException
	 */

	public static void createChart(OutputStream out, Analysis analysis,
			OlapModel olapModel, int width, int height, Locale locale,
			List colorPalette, CategoryURLGenerator urlGenerator,
			String webControllerURL, boolean suppressCrowdedLabels)
			throws FileNotFoundException, IOException, OlapException {

		logger.debug("writing chart for analysis: "
				+ analysis.getAnalysisTitle() + " " + width + "x" + height);
		logger.debug("to stream type: " + out.getClass().getName());

		JFreeChart chart = EnhancedChartFactory.createChart(analysis,
				olapModel, locale, colorPalette, urlGenerator,
				webControllerURL, width, height, suppressCrowdedLabels);
		ChartUtilities.writeChartAsPNG(out, chart, width, height);
	}

	/**
	 * factory method used in the wcf EnhancedChartComponent, also used
	 * internally by other wrapper createChart methods
	 * 
	 * @return
	 * @throws OlapException
	 */
	public static JFreeChart createChart(OlapModel olapModel, int chartType,
			String chartTitle, String horizAxisLabel, String vertAxisLabel,
			boolean showLegend, boolean showTooltips,
			boolean drillThroughEnabled, Font titleFont, Paint bgPaint,
			Font slicerFont, Font axisFont, Font axisTickFont, Font legendFont,
			int legendPosition, double tickLabelRotate, float foregroundAlpha,
			boolean showSlicer, int slicerPosition, int slicerAlignment,
			boolean showPareto, Locale locale, List colorPalette,
			CategoryURLGenerator urlGenerator, String webControllerURL,
			int chartWidth, int chartHeight, boolean suppressCrowdedLabels)
			throws OlapException {
		logger.debug("trying to create chartType: " + chartType
				+ " with locale=" + locale.getLanguage());

		JFreeChart chart = null;

		// jpivotPieURLGenerator pieUrlgenerator = new
		// jpivotPieURLGenerator(PiePlot.PER_ROW);
		// CategoryURLGenerator urlGenerator = null;

		DatasetAdapter adapter = new DatasetAdapter(locale);
		DefaultCategoryDataset dataset = null;

		switch (chartType) {
		case 1:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createBarChart(chartTitle, titleFont,
					horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.VERTICAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 2:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createBarChart3D(chartTitle,
					titleFont, horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.VERTICAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 3:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createBarChart(chartTitle, titleFont,
					horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.HORIZONTAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 4:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createBarChart3D(chartTitle,
					titleFont, horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.HORIZONTAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 5:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createStackedBarChart(chartTitle,
					titleFont, horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.VERTICAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 6:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createStackedBarChart3D(chartTitle,
					titleFont, horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.VERTICAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 7:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createStackedBarChart(chartTitle,
					titleFont, horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.HORIZONTAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 8:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createStackedBarChart3D(chartTitle,
					titleFont, horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.HORIZONTAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 9:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createLineChart(chartTitle, titleFont,
					horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.VERTICAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 10:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createLineChart(chartTitle, titleFont,
					horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.HORIZONTAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 11:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createAreaChart(chartTitle, titleFont,
					horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.VERTICAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 12:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createAreaChart(chartTitle, titleFont,
					horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.HORIZONTAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 13:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createStackedAreaChart(chartTitle,
					titleFont, horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.VERTICAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		case 14:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createStackedAreaChart(chartTitle,
					titleFont, horizAxisLabel, vertAxisLabel, dataset,
					PlotOrientation.HORIZONTAL, showLegend, showTooltips,
					drillThroughEnabled, urlGenerator);

			break;

		// pie by column
		case 15:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createPieChart(chartTitle, titleFont,
					dataset, TableOrder.BY_COLUMN, showLegend, showTooltips,
					drillThroughEnabled, new jpivotPieURLGenerator(
							TableOrder.BY_COLUMN, dataset, webControllerURL,
							olapModel));

			break;

		// pie by row
		case 16:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.createPieChart(chartTitle, titleFont,
					dataset, TableOrder.BY_ROW, showLegend, showTooltips,
					drillThroughEnabled, new jpivotPieURLGenerator(
							TableOrder.BY_ROW, dataset, webControllerURL,
							olapModel));

			break;

		case 17:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.create3DPieChart(chartTitle,
					titleFont, dataset, TableOrder.BY_COLUMN, showLegend,
					showTooltips, drillThroughEnabled,
					new jpivotPieURLGenerator(TableOrder.BY_COLUMN, dataset,
							webControllerURL, olapModel));

			break;

		case 18:
			dataset = adapter.buildCategoryDataset(olapModel);
			chart = EnhancedChartFactory.create3DPieChart(chartTitle,
					titleFont, dataset, TableOrder.BY_ROW, showLegend,
					showTooltips, drillThroughEnabled,
					new jpivotPieURLGenerator(TableOrder.BY_ROW, dataset,
							webControllerURL, olapModel));

			break;

		case 19:
			chart = EnhancedChartFactory.createTimeChart(chartTitle,
					horizAxisLabel, vertAxisLabel,
					new DatasetAdapter(locale).buildXYDataset(olapModel));
			break;

		default:
			throw new OlapException("An unknown Chart Type was requested: "
					+ chartType);
		}

		// chart.setTitle("customized title");
		ChartCustomizer customizer = new ChartCustomizer();
		customizer.customizeChart(chart, bgPaint, axisFont, axisTickFont,
				legendFont, legendPosition, tickLabelRotate, foregroundAlpha,
				colorPalette, chartWidth, chartHeight, suppressCrowdedLabels);

		if (showPareto) {
			if (chart.getPlot() instanceof CategoryPlot) {
				customizer.generatePareto((CategoryPlot) chart.getPlot());
			}
		}

		/*
		 * if(chart.getPlot() instanceof CategoryPlot){
		 * customizer.formatNumberAxis(NumberFormat.getInstance(loc),
		 * (CategoryPlot)chart.getPlot()); }
		 */

		logger.debug("showSlicer=" + showSlicer);
		if (showSlicer) {
			customizer.customizeSlicer(chart, olapModel, slicerFont,
					slicerPosition, slicerAlignment, locale);
		}
		return chart;
	}

	public static JFreeChart createTimeChart(String title,
			String horizAxisLabel, String vertAxisLabel, XYDataset dataset) {
		logger.debug("creating time series chart");

		JFreeChart chart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
				title, horizAxisLabel, vertAxisLabel, dataset, true, true,
				false);

		chart.setBackgroundPaint(Color.white);
		LegendTitle sl = (LegendTitle) chart.getLegend();

		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setAxisOffset(new RectangleInsets(UnitType.ABSOLUTE, 5.0, 5.0,
				5.0, 5.0));
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairVisible(false);

		XYItemRenderer renderer = plot.getRenderer();

		if (renderer instanceof StandardXYItemRenderer) {
			StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
			// rr.setPlotShapes(true);
			rr.setPlotImages(true);
			rr.setShapesFilled(true);
			rr.setItemLabelsVisible(true);
		}

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

		return chart;
	}

	public static JFreeChart createLineChart(String title,
			java.awt.Font titleFont, String categoryAxisLabel,
			String valueAxisLabel, CategoryDataset data,
			PlotOrientation orientation, boolean legend, boolean tooltips,
			boolean urls, CategoryURLGenerator urlGenerator) {
		CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
		ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

		LineAndShapeRenderer renderer = new LineAndShapeRenderer();

		if (tooltips) {
			renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		}

		if (urls) {
			renderer.setItemURLGenerator(urlGenerator);
		}

		CategoryPlot plot = new CategoryPlot(data, categoryAxis, valueAxis,
				renderer);
		plot.setOrientation(orientation);

		JFreeChart chart = new JFreeChart(title, titleFont, plot, legend);

		return chart;
	}

	public static JFreeChart createBarChart(String title,
			java.awt.Font titleFont, String categoryAxisLabel,
			String valueAxisLabel, CategoryDataset data,
			PlotOrientation orientation, boolean legend, boolean tooltips,
			boolean urls, CategoryURLGenerator urlGenerator) {
		CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
		ValueAxis valueAxis = new NumberAxis(valueAxisLabel);
		BarRenderer renderer = new BarRenderer();

		if (tooltips) {
			renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		}
		if (urls) {
			renderer.setItemURLGenerator(urlGenerator);
		}
		CategoryPlot plot = new CategoryPlot(data, categoryAxis, valueAxis,
				renderer);
		plot.setOrientation(orientation);
		JFreeChart chart = new JFreeChart(title, titleFont, plot, legend);

		return chart;

	}

	public static JFreeChart createStackedBarChart(String title,
			java.awt.Font titleFont, String domainAxisLabel,
			String rangeAxisLabel, CategoryDataset data,
			PlotOrientation orientation, boolean legend, boolean tooltips,
			boolean urls, CategoryURLGenerator urlGenerator) {

		CategoryAxis categoryAxis = new CategoryAxis(domainAxisLabel);
		ValueAxis valueAxis = new NumberAxis(rangeAxisLabel);

		// create the renderer...
		StackedBarRenderer renderer = new StackedBarRenderer();
		if (tooltips) {
			renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		}
		if (urls) {
			renderer.setItemURLGenerator(urlGenerator);
		}

		CategoryPlot plot = new CategoryPlot(data, categoryAxis, valueAxis,
				renderer);
		plot.setOrientation(orientation);
		JFreeChart chart = new JFreeChart(title, titleFont, plot, legend);

		return chart;

	}

	public static JFreeChart createAreaChart(String title,
			java.awt.Font titleFont, String categoryAxisLabel,
			String valueAxisLabel, CategoryDataset data,
			PlotOrientation orientation, boolean legend, boolean tooltips,
			boolean urls, CategoryURLGenerator urlGenerator) {

		CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
		categoryAxis.setCategoryMargin(0.0);
		ValueAxis valueAxis = new NumberAxis(valueAxisLabel);
		AreaRenderer renderer = new AreaRenderer();
		if (tooltips) {
			renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		}
		if (urls) {
			renderer.setItemURLGenerator(urlGenerator);
		}
		CategoryPlot plot = new CategoryPlot(data, categoryAxis, valueAxis,
				renderer);
		plot.setOrientation(orientation);
		JFreeChart chart = new JFreeChart(title, titleFont, plot, legend);

		return chart;

	}

	/**
	 * Creates a chart containing multiple pie charts, from a TableDataset.
	 * 
	 * @param title
	 *            the chart title.
	 * @param data
	 *            the dataset for the chart.
	 * @param extractType
	 *            <code>PER_ROW</code> or <code>PER_COLUMN</code> (defined in
	 *            {@link PiePlot}).
	 * @param legend
	 *            a flag specifying whether or not a legend is required.
	 * @param tooltips
	 *            configure chart to generate tool tips?
	 * @param urls
	 *            configure chart to generate URLs?
	 * 
	 * @return a pie chart.
	 */
	public static JFreeChart createPieChart(String title,
			java.awt.Font titleFont, CategoryDataset data, TableOrder order,
			boolean legend, boolean tooltips, boolean urls,
			PieURLGenerator urlGenerator) {

		MultiplePiePlot plot = new MultiplePiePlot(data);
		plot.setDataExtractOrder(order);

		PiePlot pp = (PiePlot) plot.getPieChart().getPlot();
		// pp.setInsets(new Insets(0, 5, 5, 5));
		pp.setBackgroundPaint(null);
		// no outline around each piechart
		pp.setOutlineStroke(null);
		// plot.setOutlineStroke(null);
		PieToolTipGenerator tooltipGenerator = null;
		tooltipGenerator = new StandardPieToolTipGenerator();

		// PieURLGenerator urlGenerator = null;
		if (!urls) {
			urlGenerator = null;
		}

		pp.setToolTipGenerator(tooltipGenerator);
		pp.setLabelGenerator(null);
		pp.setURLGenerator(urlGenerator);

		JFreeChart chart = new JFreeChart(title, titleFont, plot, legend);

		return chart;

	}

	public static JFreeChart create3DPieChart(String title,
			java.awt.Font titleFont, CategoryDataset data, TableOrder order,
			boolean legend, boolean tooltips, boolean urls,
			PieURLGenerator urlGenerator) {
		MultiplePiePlot plot = new MultiplePiePlot(data);
		plot.setDataExtractOrder(order);

		// plot.setOutlineStroke(null);
		JFreeChart pieChart = new JFreeChart(new PiePlot3D(null));
		pieChart.setBackgroundPaint(null);
		plot.setPieChart(pieChart);

		PiePlot3D pp = (PiePlot3D) plot.getPieChart().getPlot();
		pp.setBackgroundPaint(null);
		// pp.setInsets(new Insets(0, 5, 5, 5));

		// no outline around each piechart
		pp.setOutlineStroke(null);

		PieToolTipGenerator tooltipGenerator = null;

		if (tooltips) {
			// tooltipGenerator = new StandardPieToolTipGenerator();
		}

		if (!urls) {
			urlGenerator = null;
		}

		pp.setToolTipGenerator(tooltipGenerator);
		pp.setLabelGenerator(null);
		pp.setURLGenerator(urlGenerator);

		JFreeChart chart = new JFreeChart(title, titleFont, plot, legend);

		return chart;
	}

	/**
	 * Creates a stacked area chart with default settings.
	 * 
	 * @param title
	 *            the chart title.
	 * @param categoryAxisLabel
	 *            the label for the category axis.
	 * @param valueAxisLabel
	 *            the label for the value axis.
	 * @param data
	 *            the dataset for the chart.
	 * @param legend
	 *            a flag specifying whether or not a legend is required.
	 * @param tooltips
	 *            configure chart to generate tool tips?
	 * @param urls
	 *            configure chart to generate URLs?
	 * 
	 * @return an area chart.
	 */

	public static JFreeChart createStackedAreaChart(String title,
			java.awt.Font titleFont, String categoryAxisLabel,
			String valueAxisLabel, CategoryDataset data,
			PlotOrientation orientation, boolean legend, boolean tooltips,
			boolean urls, CategoryURLGenerator urlGenerator) {

		CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
		ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

		StackedAreaRenderer renderer = new StackedAreaRenderer();
		if (tooltips) {
			renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		}
		if (urls) {
			renderer.setItemURLGenerator(urlGenerator);
		}

		CategoryPlot plot = new CategoryPlot(data, categoryAxis, valueAxis,
				renderer);
		plot.setOrientation(orientation);
		JFreeChart chart = new JFreeChart(title, titleFont, plot, legend);

		return chart;

	}

	/**
	 * A URLGenerator class to generate pie urls that work with jpivot
	 * 
	 * @author ati
	 * 
	 */
	public static class jpivotPieURLGenerator extends StandardPieURLGenerator {

		/** Prefix to the URL */
		private String prefix = "";
		private List cells = null;
		private int rowCount;
		OlapModel olapModel = null;
		private TableOrder order; // COLUMN or ROW - used to calculate cell
									// Position

		jpivotPieURLGenerator() {
		}

		jpivotPieURLGenerator(String prefix) {
			this.prefix = prefix;
		}

		/*
		 * Use this constructor to set dataExtraction type (PER_COLUMN/PER_ROW),
		 * and allow for rowcount of current dataset (could be changed to just
		 * take rowCount)
		 */
		jpivotPieURLGenerator(TableOrder order, DefaultCategoryDataset dataset) {
			this.order = order;
			this.rowCount = dataset.getRowCount();
		}

		/*
		 * As above with web controller URL
		 */
		jpivotPieURLGenerator(TableOrder order, DefaultCategoryDataset dataset,
				String controllerURL, OlapModel olapModel) {
			this(order, dataset);
			this.prefix = controllerURL;

			try {
				this.olapModel = olapModel;
				this.cells = olapModel.getResult().getCells();
			} catch (OlapException e) {
				e.printStackTrace();
			}

		}

		/**
		 * Implementation of generateURL that integrates with jpivot/wcf
		 * framework. A request handler is added for each cell/item. No test is
		 * done to see if a cell is drillable, since the url has to added (I
		 * think, like an all or nothing ?) Generates a URL for a particular
		 * item within a series.
		 * 
		 * @param data
		 *            the dataset.
		 * @param key
		 *            the data item key.
		 * @param pieIndex
		 *            the index of the pie containing key (zero-based).
		 * 
		 * @return the generated URL
		 */

		public String generateURL(PieDataset data, Comparable key, int pieIndex) {
			String url = prefix;
			int index = data.getIndex(key);
			int cellpos;
			if (order == TableOrder.BY_COLUMN) {
				cellpos = (pieIndex * rowCount) + index;
			} else {
				cellpos = pieIndex + (rowCount * index);
			}

			if (EnhancedChartFactory.canDrillThrough((Cell) cells.get(cellpos),
					olapModel) && (!((Cell) cells.get(cellpos)).isNull())) {
				String id = DomUtils.randomId();

				// To Dos
				// add request listener for drill through handler here

				url += id;
				return url;
			} else {
				return null;
			}
		}
	}

	protected static boolean canDrillThrough(Cell cell, OlapModel olapModel) {
		return ((DrillThrough) olapModel.getExtension(DrillThrough.ID))
				.canDrillThrough((Cell) cell.getRootDecoree());
	}

}
