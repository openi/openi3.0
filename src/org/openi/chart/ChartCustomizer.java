package org.openi.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LevelRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.DataUtilities;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.ui.VerticalAlignment;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.navi.ChangeSlicer;

/**
 * customizes chart properties based on model (enhanced chart component)
 * 
 * @author SUJEN
 */
public class ChartCustomizer {
	private static Logger logger = Logger.getLogger(ChartCustomizer.class);
	public static final int MIN_MARGIN_BETN_CATEGORY_LABELS = 23;

	private double getMarginBetnCategoryLabels(CategoryPlot catPlot,
			int categoryCount, int chartWidth, int chartHeight) {
		// the default value of both lowerMargin and UpperMargin of a category
		// axis equals to 5% of axis length
		// the default value of both itemMargin and categoryMargin of a category
		// axis equals to 20% of axis length
		double categoryMargin = catPlot.getDomainAxis().getCategoryMargin()
				* chartWidth;

		// number of tick items in each category
		int itemCount = catPlot.getDataset().getRowCount();

		double itemMargin = 0.20 * chartWidth;
		if (catPlot.getRenderer() instanceof BarRenderer)
			itemMargin = ((BarRenderer) catPlot.getRenderer()).getItemMargin()
					* chartWidth;
		else if (catPlot.getRenderer() instanceof LevelRenderer)
			itemMargin = ((LevelRenderer) catPlot.getRenderer())
					.getItemMargin() * chartWidth;

		// total tick items in chart
		int totalItems = categoryCount * itemCount;

		// calculating the width of each individual category Margin
		double indCategoryMargin = 0;
		if (categoryCount == 1)
			indCategoryMargin = 0;
		else
			indCategoryMargin = categoryMargin / (categoryCount - 1);

		// calculating the width of each individual item Margin

		double indItemMargin = 0;
		if (itemCount != 1)
			indItemMargin = itemMargin / (categoryCount * itemCount);

		// calculating the width of each item
		double indItemWidth = (0.5 * chartWidth) / (categoryCount * itemCount);

		double marginBetnCategoryLabels = indCategoryMargin + itemCount
				* indItemWidth + (itemCount - 1) * indItemMargin;
		return marginBetnCategoryLabels;
	}

	private int getIntervalToSkip(CategoryPlot catPlot, int chartWidth,
			int chartHeight) {
		int intervalToSkip = 1;
		int newCategoryCount = 0;
		int categoryCount = catPlot.getCategories().size();
		boolean crowded = true;
		while (crowded) {
			newCategoryCount = categoryCount / (intervalToSkip + 1);
			if ((categoryCount % (intervalToSkip + 1) != 0))
				newCategoryCount++;
			double marginBetnCategories = getMarginBetnCategoryLabels(catPlot,
					newCategoryCount, chartWidth, chartHeight);
			if (marginBetnCategories >= MIN_MARGIN_BETN_CATEGORY_LABELS)
				crowded = false;
			else
				intervalToSkip++;
		}
		return intervalToSkip;
	}

	private void hideCategoryLabelsInInterval(CategoryPlot catPlot,
			int intervalToSkip) {
		int l = 0;
		for (int k = 0; k < catPlot.getCategories().size(); k++) {
			if (l != 0 && l <= intervalToSkip) {
				catPlot.getDomainAxis().setTickLabelFont(
						catPlot.getCategories().get(k).toString(),
						new Font("Serif", Font.BOLD, 0));
			} else if (l > intervalToSkip) {
				l = 0;
			}
			l++;
		}
	}

	private boolean isCategoryLabelsCrowded(CategoryPlot catPlot,
			int chartWidth, int chartHeight) {
		boolean crowded = false;
		double marginBetnCategoryLabels = getMarginBetnCategoryLabels(catPlot,
				catPlot.getCategories().size(), chartWidth, chartHeight);
		if (marginBetnCategoryLabels < MIN_MARGIN_BETN_CATEGORY_LABELS)
			crowded = true;
		return crowded;
	}

	public void customizeChart(JFreeChart chart, Paint bgPaint, Font axisFont,
			Font axisTickFont, Font legendFont, int legendPosition,
			double tickLabelRotate, float foregoundAlpha, List colorPalette,
			int chartWidth, int chartHeight, boolean suppressCrowdedLabels) {
		chart.setBackgroundPaint(bgPaint);
		chart.setBackgroundPaint(new Color(255, 255, 255));
		logger.debug("setting background paint: " + bgPaint);

		Plot plot = chart.getPlot();
		logger.debug("plot type: " + plot.getClass().getName());
		plot.setForegroundAlpha(1.0f);

		if (plot instanceof CategoryPlot) {
			CategoryPlot catPlot = (CategoryPlot) plot;
			catPlot.getDomainAxis().setLabelFont(axisFont);
			catPlot.getRangeAxis().setLabelFont(axisFont);
			catPlot.getDomainAxis().setTickLabelFont(axisTickFont);
			catPlot.getRangeAxis().setTickLabelFont(axisTickFont);

			// added to fix the issue ---> (over-crowding of x-axis category
			// labels)

			if (isCategoryLabelsCrowded(catPlot, chartWidth, chartHeight)
					&& suppressCrowdedLabels) {
				int intervalToSkip = getIntervalToSkip(catPlot, chartWidth,
						chartHeight);
				hideCategoryLabelsInInterval(catPlot, intervalToSkip);
			}

			catPlot.getDomainAxis().setMaximumCategoryLabelWidthRatio(100.00f);
			double angle = (-2.0 * Math.PI) / 360.0 * tickLabelRotate;
			CategoryLabelPositions oldp = catPlot.getDomainAxis()
					.getCategoryLabelPositions();
			/*
			 * CategoryLabelPositions newp = new CategoryLabelPositions(oldp
			 * .getLabelPosition(RectangleEdge.TOP), new
			 * CategoryLabelPosition(RectangleAnchor.TOP,
			 * TextBlockAnchor.TOP_RIGHT, TextAnchor.TOP_RIGHT, angle),
			 * oldp.getLabelPosition(RectangleEdge.LEFT),
			 * oldp.getLabelPosition(RectangleEdge.RIGHT));
			 */
			CategoryLabelPositions newp = new CategoryLabelPositions(
					oldp.getLabelPosition(RectangleEdge.TOP),
					new CategoryLabelPosition(RectangleAnchor.TOP,
							TextBlockAnchor.TOP_RIGHT, TextAnchor.TOP_RIGHT,
							angle, CategoryLabelWidthType.RANGE, 0.0f),
					oldp.getLabelPosition(RectangleEdge.LEFT),
					oldp.getLabelPosition(RectangleEdge.RIGHT));
			catPlot.getDomainAxis().setCategoryLabelPositions(newp);
			// ((BarRenderer)chart.getCategoryPlot().getRenderer()).setGradientPaintTransformer(new
			// StandardGradientPaintTransformer());
			// XXX: setting series colors
			logger.info("setting up series colors");
			if (colorPalette == null || colorPalette.size() == 0) {
				setupSeriesPaint(this.getDefaultColorPalette(), chart
						.getCategoryPlot().getRenderer());
			} else {
				setupSeriesPaint(colorPalette, chart.getCategoryPlot()
						.getRenderer());
			}

		} else if (plot instanceof PiePlot3D) {
			PiePlot3D piePlot = (PiePlot3D) plot;

			// piePlot.setSectionLabelFont(axisFont);
			piePlot.setLabelFont(axisFont);

			// piePlot.setSeriesLabelFont(axisTickFont);
			// piePlot.setSectionLabelType(piePlot.NO_LABELS);
			piePlot.setDirection(org.jfree.util.Rotation.CLOCKWISE);
			// casting int type to float type
			piePlot.setNoDataMessage("No data to display");
		} else if (plot instanceof MultiplePiePlot) {
			logger.info("settings for PiePlot");
			MultiplePiePlot piePlot = (MultiplePiePlot) plot;
			JFreeChart subchart = piePlot.getPieChart();
			PiePlot p = (PiePlot) subchart.getPlot();
			// piePlot.setSectionLabelFont(axisFont);
			// piePlot.setSeriesLabelFont(axisTickFont);
			// piePlot.setLabelFont(axisFont);
			// piePlot.setExplodePercent(1, 0.60);
			if (colorPalette == null || colorPalette.size() == 0) {
				setupPiePaint(this.getDefaultColorPalette(), p);
			} else {
				setupPiePaint(colorPalette, p);
			}

			// piePlot.setSectionLabelType(piePlot.NO_LABELS);
		} else if (plot instanceof XYPlot) {
			XYPlot xyPlot = (XYPlot) plot;
			xyPlot.getDomainAxis().setLabelFont(axisFont);
			xyPlot.getRangeAxis().setLabelFont(axisFont);
			xyPlot.getDomainAxis().setTickLabelFont(axisTickFont);
			xyPlot.getRangeAxis().setTickLabelFont(axisTickFont);
			// xyPlot.getDomainAxis().setMaxCategoryLabelWidthRatio(100);
			xyPlot.setBackgroundPaint(bgPaint);
			xyPlot.setRangeGridlinesVisible(true);
			xyPlot.setRangeGridlinePaint(new Color(166, 166, 166));
			// xyPlot.setRangeTickBandPaint(purple);
			// Color purple = new Color(171, 25, 97);
			// xyPlot.setRangeCrosshairPaint(purple);
			xyPlot.getDomainAxis().setAxisLineVisible(false);
			xyPlot.getRangeAxis().setAxisLineVisible(false);
			xyPlot.setOutlinePaint(new Color(0, 0, 0));
			// xyPlot.getRangeAxis().setMinimumAxisValue(0);
		}

		customizeLegend(chart, legendFont, legendPosition);

	}

	/**
	 * @param component
	 * @param chart
	 * @param legendFont
	 */
	private void customizeLegend(JFreeChart chart, Font legendFont,
			int legendPosition) {
		LegendTitle legend = (LegendTitle) chart.getLegend();

		if (legend != null) {
			legend.setItemFont(legendFont);
			// legend.setPreferredWidth(EnhancedChartComponent.DEFAULT_LEGEND_WIDTH);
			/*
			 * legend.setAnchor(legendPosition);
			 * legend.setOutlinePaint(legend.getBackgroundPaint());
			 */
			legend.setBorder(BlockBorder.NONE);
			RectangleEdge legendRectEdge = RectangleEdge.BOTTOM;
			switch (legendPosition) {
			case 0:
				legendRectEdge = RectangleEdge.LEFT;
				break;
			case 1:
				legendRectEdge = RectangleEdge.TOP;
				break;
			case 2:
				legendRectEdge = RectangleEdge.RIGHT;
				break;
			case 3:
				legendRectEdge = RectangleEdge.BOTTOM;
				break;
			}
			legend.setPosition(legendRectEdge);
		}
	}

	private RectangleEdge createRectangleEdge(int slicerPosition) {
		RectangleEdge slicerRectPos = RectangleEdge.BOTTOM;

		switch (slicerPosition) {
		case 0:
			slicerRectPos = RectangleEdge.LEFT;
			break;

		case 1:
			slicerRectPos = RectangleEdge.TOP;
			break;

		case 2:
			slicerRectPos = RectangleEdge.RIGHT;
			break;

		case 3:
			slicerRectPos = RectangleEdge.BOTTOM;
			break;
		}
		return slicerRectPos;

	}

	private HorizontalAlignment createHorizontalAlignment(int slicerAlignment) {
		HorizontalAlignment slicerHorizAlignment = HorizontalAlignment.LEFT;

		switch (slicerAlignment) {
		case 4:
			slicerHorizAlignment = HorizontalAlignment.CENTER;
			break;

		case 3:
			slicerHorizAlignment = HorizontalAlignment.LEFT;
			break;

		case 2:
			slicerHorizAlignment = HorizontalAlignment.RIGHT;
			break;
		}

		return slicerHorizAlignment;
	}

	/**
	 * Series paint will be set in the list order of the colorPallette
	 * parameter. Can contain a list of any java.awt.Paint objects (Color,
	 * GradientPaint, etc). Will only attempt if the paintList is not null.
	 * 
	 * @param colorPalette
	 *            list of java.awt.Paint objects
	 * @param renderer
	 */
	public void setupSeriesPaint(List paintList, CategoryItemRenderer renderer) {
		logger.debug("setupSeriesPaint: " + paintList);

		if (paintList != null) {
			Iterator palette = paintList.iterator();

			for (int i = 0; palette.hasNext(); i++) {
				Paint current = (Paint) palette.next();
				renderer.setSeriesPaint(i, current);
			}
		}
	}

	public void setupPiePaint(List paintList, PiePlot piePlot) {
		logger.debug("setupPiePaint: " + paintList);

		if (paintList != null) {
			Iterator palette = paintList.iterator();

			for (int i = 0; palette.hasNext(); i++) {
				Paint current = (Paint) palette.next();
				piePlot.setSectionPaint(i, current);

			}
		}
	}

	public void generatePareto(CategoryPlot plot) {
		CategoryDataset dataset = plot.getDataset();
		PieDataset pieDataset = DatasetUtilities.createPieDatasetForRow(
				dataset, 0);
		KeyedValues cumulative = DataUtilities
				.getCumulativePercentages(pieDataset);
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();

		CategoryDataset dataset2 = DatasetUtilities.createCategoryDataset(
				"Cumulative Pct", // will show up in legend
				cumulative);
		NumberAxis axis2 = new NumberAxis("Culmulative Percentage"); // axis
																		// label

		axis2.setNumberFormatOverride(NumberFormat.getPercentInstance());
		// axis2.setMaximumAxisValue(1.0);//set max to 100%, otherwise defaults
		// to 105%
		axis2.setUpperBound(1.0);// 1.0.1 update
		//
		plot.setRangeAxis(1, axis2);
		plot.setDataset(1, dataset2);
		plot.setRenderer(1, renderer);
		plot.mapDatasetToRangeAxis(1, 1);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

	}

	/**
	 * build slicer text string
	 */
	public String buildSlicer(OlapModel olapModel, Locale locale) {
		StringBuffer slicer = new StringBuffer();

		ChangeSlicer changeSlicer = (ChangeSlicer) olapModel
				.getExtension("changeSlicer");

		if (changeSlicer == null || changeSlicer.getSlicer() == null)
			return "";
		Member[] members = changeSlicer.getSlicer();

		for (int i = 0; i < members.length; i++) {
			slicer.append(members[i].getLevel().getHierarchy().getDimension()
					.getLabel()
					+ "->" + members[i].getLevel().getLabel());

			slicer.append("=");
			slicer.append(members[i].getLabel());

			if (i < (members.length - 1)) {
				slicer.append(", ");
			}
		}

		if (slicer.length() != 0)
			slicer.insert(0, "Slicer: ");

		return slicer.toString();
	}

	private boolean duplicateMemberLabelExists(Member[] members) {
		List<String> labels = new ArrayList<String>();
		boolean exist = false;

		for (int i = 0; i < members.length; i++) {
			String label = members[i].getLabel();
			if (labels.contains(label)) {
				exist = true;
				break;
			}
			labels.add(label);
		}

		return exist;
	}

	/**
	 * Get cell value as a Number. Parses the cell value string using the locale
	 * set in this.locale.
	 * 
	 * @param cell
	 * @return value as Number (can be null)
	 */

	/*
	 * private Number getNumberValue(Cell cell) { //**** HACK AR 2004.01.10
	 * //String value = cell.getFormattedValue(); Object value =
	 * cell.getValue();
	 * 
	 * //added to fix data format bug in range axis
	 * if(cell.getFormattedValue()!=null && cell.getFormattedValue()!="" ) {
	 * 
	 * String fmtValue=cell.getFormattedValue(); fmtValue=fmtValue.trim();
	 * if(fmtValue.endsWith("%")) { rangeIsPercentage =true; } else
	 * if(fmtValue.startsWith("$")) { rangeIsCurrency=true;
	 * 
	 * } else { rangeIsDefault=true; } //rangeIsDefault=false;
	 * //if(!cell.getFormat().isPercent()) // rangeIsPercentage=false; }
	 * //////////////// DecimalFormatSymbols dfs = new
	 * DecimalFormatSymbols(locale); DecimalFormat formatter = new
	 * DecimalFormat(); formatter.setDecimalFormatSymbols(dfs);
	 * 
	 * Number number = null;
	 * 
	 * try { // need this formatter so that we can properly render jfreecharts
	 * number = formatter.parse(String.valueOf(value)); } catch (Exception e) {
	 * logger.error(e); number = null; }
	 * 
	 * return number; }
	 */
	private float toFloat(double input) {
		float output = 1.0f;

		try {
			output = (float) input;
		} catch (Exception e) {
			logger.warn(e);
		}

		return output;
	}

	private List<Color> getDefaultColorPalette() {
		List<Color> palette = new ArrayList<Color>();
		palette.add(new Color(51, 102, 54));
		palette.add(new Color(153, 204, 255));
		palette.add(new Color(153, 153, 51));
		palette.add(new Color(204, 153, 51));
		palette.add(new Color(0, 102, 102));
		palette.add(new Color(51, 153, 255));
		palette.add(new Color(153, 51, 0));
		palette.add(new Color(204, 204, 53));
		palette.add(new Color(102, 153, 204));
		palette.add(new Color(153, 153, 204));
		palette.add(new Color(204, 204, 204));
		palette.add(new Color(102, 153, 153));
		palette.add(new Color(204, 204, 102));
		palette.add(new Color(204, 102, 0));
		palette.add(new Color(153, 153, 255));
		palette.add(new Color(0, 102, 204));
		palette.add(new Color(153, 204, 204));
		palette.add(new Color(153, 153, 153));
		palette.add(new Color(255, 204, 0));
		palette.add(new Color(0, 153, 153));
		palette.add(new Color(153, 204, 51));
		palette.add(new Color(255, 153, 0));
		palette.add(new Color(153, 153, 102));
		palette.add(new Color(51, 153, 102));
		palette.add(new Color(204, 204, 51));
		return palette;
	}

	public void formatNumberAxis(NumberFormat numberFormatter, CategoryPlot plot) {
		// format range if it is percent/currency
		// TODO - need to bring this in from enhanced chart component
		if (numberFormatter != null) {
			NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
			numberAxis.setNumberFormatOverride(numberFormatter);
		}

		/*
		 * if(!rangeIsDefault && (catPlot.getRangeAxis() instanceof NumberAxis))
		 * { NumberAxis axis=(NumberAxis)catPlot.getRangeAxis();
		 * if(rangeIsPercentage && !rangeIsCurrency) {
		 * 
		 * axis.setNumberFormatOverride(NumberFormat.getPercentInstance()); }
		 * else if(rangeIsCurrency && !rangeIsPercentage) { NumberFormat
		 * fmt=NumberFormat.getCurrencyInstance();
		 * fmt.setMaximumFractionDigits(0); fmt.setMinimumFractionDigits(0);
		 * axis.setNumberFormatOverride(fmt); } }
		 */
	}

	public void customizeSlicer(JFreeChart chart, OlapModel olapModel,
			Font slicerFont, int slicerPosition, int slicerAlignment,
			Locale locale) {
		logger.debug("customizing slicer position: " + slicerPosition);
		/*
		 * TextTitle slicerText = new TextTitle( buildSlicer(result, locale),
		 * slicerFont, this.createHorizontalAlignment(slicerAlignment) );
		 */
		TextTitle slicerText = new TextTitle(buildSlicer(olapModel, locale),
				slicerFont, Color.BLACK,
				this.createRectangleEdge(slicerPosition),
				this.createHorizontalAlignment(slicerAlignment),
				VerticalAlignment.CENTER, new RectangleInsets(0, 0, 0, 0));

		slicerText.setPosition(this.createRectangleEdge(slicerPosition));
		chart.addSubtitle(slicerText);

	}

}
