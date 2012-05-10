package org.openi.chart;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.servlet.ServletUtilities;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.TableOrder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.tonbeller.jpivot.core.ModelChangeEvent;
import com.tonbeller.jpivot.core.ModelChangeListener;
import com.tonbeller.jpivot.olap.model.Cell;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.model.Position;
import com.tonbeller.jpivot.olap.model.Result;
import com.tonbeller.jpivot.olap.navi.DrillThrough;
import com.tonbeller.jpivot.olap.navi.MemberTree;
import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.component.ComponentSupport;
import com.tonbeller.wcf.controller.Dispatcher;
import com.tonbeller.wcf.controller.DispatcherSupport;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.controller.RequestListener;
import com.tonbeller.wcf.table.ITableComponent;
import com.tonbeller.wcf.table.TableModel;
import com.tonbeller.wcf.utils.DomUtils;
import com.tonbeller.wcf.utils.XmlUtils;

/**
 * Contains a reference to an OlapModel. Produces a chart image.
 * 
 * @author SUJEN
 */
public class EnhancedChartComponent extends ComponentSupport implements
		ModelChangeListener {
	private static Logger logger = Logger.getLogger(EnhancedChartComponent.class);

	String ref;
	Document document;
	OlapModel olapModel;
	boolean dirty = true;
	Locale locale;

	Result result;
	Iterator cellIterator;
	int dimCount;
	Element rootElement;
	int colCount;

	// servlet mapping - optionally can be set as a servlet context parameter
	// with name "chartServlet"
	// for example:
	/*
	 * <context-param> <param-name>chartServlet</param-name>
	 * <param-value>/path/to/chartServlet</param-value> </context-param>
	 */
	String CHART_SERVLET = "/DisplayChart";
	final String CHART_SERVLET_KEY = "chartServlet";
	boolean baseDisplayURLSet = false;
	String webControllerURL = "";

	String filename = null;
	// chart properties
	public static final int DEFAULT_CHART_WIDTH = 500;
	public static final int DEFAULT_CHART_HEIGHT = 300;
	final static double DEFAULT_LEGEND_WIDTH = 100.0;
	protected String chartTitle = "";
	protected java.awt.Font titleFont = JFreeChart.DEFAULT_TITLE_FONT;
	protected String fontName = "Verdana";
	protected int fontStyle = java.awt.Font.BOLD;
	protected int fontSize = 18;
	protected String slicerFontName = "Verdana";
	protected int slicerFontStyle = java.awt.Font.PLAIN;
	protected int slicerFontSize = 11;
	protected String axisFontName = "Verdana";
	protected int axisFontStyle = java.awt.Font.PLAIN;
	protected int axisFontSize = 11;
	protected String axisTickFontName = "Verdana";
	protected int axisTickFontStyle = java.awt.Font.PLAIN;
	protected int axisTickFontSize = 11;
	protected String legendFontName = "Verdana";
	protected int legendFontStyle = java.awt.Font.PLAIN;
	protected int legendFontSize = 11;
	protected int legendPosition = 3;// RectangleEdge.RIGHT;//Legend.EAST;
	protected int slicerPosition = 1;// Title.BOTTOM;
	protected int slicerAlignment = 0;// Title.CENTER;

	protected int bgColorR = 255;
	protected int bgColorG = 255;
	protected int bgColorB = 255;
	protected int chartType = 1;
	protected int chartHeight = DEFAULT_CHART_HEIGHT;
	protected int chartWidth = DEFAULT_CHART_WIDTH;
	protected String horizAxisLabel = "";
	protected String vertAxisLabel = "";
	protected boolean showLegend = true;
	protected boolean showSlicer = true;
	protected boolean showTooltips = true;
	protected boolean drillThroughEnabled = true;
	protected int tickLabelRotate = 30; // default 30 degree rotation
	protected ChartRenderingInfo info = null;
	protected Dispatcher dispatcher = new DispatcherSupport();

	/*
	 * As there is no float type formatter in
	 * com.tonbeller.wcf.format.BasicTypes class, making foregroundAlpha as
	 * double type. But in library, this field of float type. So need to cast
	 * into float type while setting foreground alpha for chart.
	 */
	protected double foregroundAlpha = 1.0;
	protected boolean showPareto = false;
	protected boolean useChartSize = false; // chart size based on saved width
											// and height.
	private boolean writeImageMap = true;
	private boolean showCustomLabels = false;

	/*
	 * for the option to either hide or show the crowded category tick labels
	 * default is set to true, i.e. hide the crowded category tick labels
	 */
	private boolean suppressCrowdedLabels = true;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the id of this component
	 * @param parent
	 *            parent component
	 * @param ref
	 *            a reference to an olap model
	 * @param baseDisplayURL
	 *            URL for chart servlet. Overrides default
	 * @param controllerURL
	 *            URL for web application where JPivot/WCF is running. Overrides
	 *            default
	 * @param context
	 *            RequestContext
	 */
	public EnhancedChartComponent(String id, Component parent, String ref,
			String baseDisplayURL, String controllerURL, RequestContext context) {
		super(id, parent);
		this.ref = ref;

		this.olapModel = (OlapModel) context.getModelReference(ref);
		this.olapModel.addModelChangeListener(this);
		this.locale = context.getLocale();
		// extend the controller
		getDispatcher().addRequestListener(null, null, dispatcher);
		// optional servlet context parameter for chart servlet location
		String chartServlet = baseDisplayURL;
		if (chartServlet == null) {
			chartServlet = context.getServletContext().getInitParameter(
					CHART_SERVLET_KEY);
		} else {
			baseDisplayURLSet = true;
		}
		if (chartServlet != null) {
			this.CHART_SERVLET = chartServlet;
		}
		if (controllerURL != null) {
			this.webControllerURL = controllerURL;
		}
	}

	public EnhancedChartComponent(String id, Component parent, String ref,
			RequestContext context) {
		this(id, parent, ref, null, null, context);
	}

	/**
	 * called once by the creating tag
	 */
	public void initialize(RequestContext context) throws Exception {
		super.initialize(context);
	}

	/**
	 * Entry point for producing charts, called by wcf render tag. Produces a
	 * jfreechart dataset from olap model, then creates a chart and writes it to
	 * the servlet container temp directory. Returns a DOM document for Renderer
	 * to transform into html. Requires that jfreechart servlet is installed in
	 * this application context.
	 */
	public Document render(RequestContext context) throws Exception {
		// check if we need to produce a new chart
		if (dirty) {
			// clear old listeners
			dispatcher.clear();
			this.result = olapModel.getResult();
			this.cellIterator = result.getCells().iterator();
			this.dimCount = result.getAxes().length;
			DefaultCategoryDataset dataset = null;
			switch (dimCount) {
			case 1:
				logger.info("1-dim data");
				dataset = build1dimDataset();
				break;
			case 2:
				logger.info("2-dim data");
				dataset = build2dimDataset();
				break;
			default:
				logger.error("less than 1 or more than 2 dimensions");
				throw new IllegalArgumentException(
						"ChartRenderer requires a 1 or 2 dimensional result");
			}
			// re-set dirty flag
			dirty = false;

			// create font
			titleFont = new java.awt.Font(fontName, fontStyle, fontSize);
			Color bgColor = new Color(this.getBgColorR(), this.getBgColorG(),
					this.getBgColorB());
			Font slicerFont = new Font(this.getSlicerFontName(),
					this.getSlicerFontStyle(), this.getSlicerFontSize());
			Font axisFont = new Font(this.getAxisFontName(),
					this.getAxisFontStyle(), this.getAxisFontSize());
			Font axisTickFont = new Font(this.getAxisTickFontName(),
					this.getAxisTickFontStyle(), this.getAxisTickFontSize());
			Font legendFont = new Font(this.getLegendFontName(),
					this.getLegendFontStyle(), this.getLegendFontSize());

			CategoryURLGenerator urlGenerator = new jpivotCategoryURLGenerator(
					webControllerURL);

			JFreeChart chart = EnhancedChartFactory.createChart(olapModel,
					chartType, chartTitle, horizAxisLabel, vertAxisLabel,
					showLegend, showTooltips, drillThroughEnabled, titleFont,
					bgColor, slicerFont, axisFont, axisTickFont, legendFont,
					legendPosition, tickLabelRotate, 1.0f, showSlicer,
					slicerPosition, slicerAlignment, showPareto, this.locale,
					null, urlGenerator, webControllerURL, chartWidth,
					chartHeight, suppressCrowdedLabels);

			try {

				info = new ChartRenderingInfo(new StandardEntityCollection());

				// Write the chart image to the temporary directory
				HttpSession session = context.getSession();
				filename = ServletUtilities.saveChartAsPNG(chart, chartWidth,
						chartHeight, info, session);

			} catch (Exception e) {
				logger.error(e);
				filename = "public_error_500x300.png";
				dirty = true;
			}
		}
		
		DocumentBuilder parser = XmlUtils.getParser();
		String xchart = "<xchart>" + writeImageMap(filename.substring(0, filename.lastIndexOf(".")), info, false)
				+ "</xchart>";
		InputStream stream = new ByteArrayInputStream(xchart.getBytes("UTF-8"));

		document = parser.parse(stream);
		Element root = document.getDocumentElement();
		// create url for img tag
		String graphURL = getGraphURL(context);

		Element img = document.createElement("img");
		img.setAttribute("src", graphURL);
		img.setAttribute("width", new Integer(chartWidth).toString());
		img.setAttribute("height", new Integer(chartHeight).toString());
		img.setAttribute("style", "border:0;");
		img.setAttribute("usemap", "#" + filename.substring(0, filename.lastIndexOf(".")));
		root.appendChild(img);

		return document;
	}

	public String getGraphURL(RequestContext context) {
		String graphURL = "";
		if (baseDisplayURLSet) {
			graphURL = CHART_SERVLET;
		} else {
			graphURL = context.getRequest().getContextPath() + CHART_SERVLET;
		}
		graphURL = graphURL + (graphURL.indexOf('?') >= 0 ? "&" : "?")
				+ "filename=" + getFilename();
		return graphURL;
	}

	/**
	 * Writes an image map as a String This function has been requested to be
	 * added to jfreechart Also requires slight change to
	 * ChartEntity.getImageMapAreaTag() to produce valid xml tag and use &amp;
	 * entity in urls. Diffs sent to jfreechart project - so hopefully this will
	 * be in there soon
	 * 
	 * @param name
	 *            the map name.
	 * @param info
	 *            the chart rendering info.
	 * @param useOverLibForToolTips
	 *            whether to use OverLIB for tooltips
	 *            (http://www.bosrup.com/web/overlib/).
	 */
	public String writeImageMap(String name, ChartRenderingInfo info,
			boolean useOverLibForToolTips) {
		StringBuffer sb = new StringBuffer();
		sb.append("<map name=\"" + name + "\">");
		EntityCollection entities = info.getEntityCollection();
		Iterator iterator = entities.iterator();

		while (iterator.hasNext()) {
			ChartEntity entity = (ChartEntity) iterator.next();
			// the tag returned by getImageMapAreaTag is not valid xml
			// String area = entity.getImageMapAreaTag(useOverLibForToolTips);

			String area = "";

			if (useOverLibForToolTips)
				area = entity
						.getImageMapAreaTag(
								new org.jfree.chart.imagemap.OverLIBToolTipTagFragmentGenerator(),
								new org.jfree.chart.imagemap.StandardURLTagFragmentGenerator());
			else
				area = entity
						.getImageMapAreaTag(
								new org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator(),
								new org.jfree.chart.imagemap.StandardURLTagFragmentGenerator());
			// modify to valid xml tag
			area = area.replaceAll("&", "&amp;");
			// area = area.toLowerCase();
			// area = area.replaceAll(">$", "/>");
			if (area.length() > 0) {
				sb.append(area);
			}
		}
		sb.append("</map>");
		return sb.toString();
	}

	/**
	 * build slicer text string
	 */
	private String buildSlicer() {
		StringBuffer slicer = new StringBuffer();
		slicer.append("Slicer: ");
		Iterator pi = result.getSlicer().getPositions().iterator();
		while (pi.hasNext()) {
			Position p = (Position) pi.next();
			Member[] members = p.getMembers();
			for (int i = 0; i < members.length; i++) {
				slicer.append(members[i].getLevel().getLabel());
				slicer.append("=");
				slicer.append(members[i].getLabel());
				if (i < members.length - 1) {
					slicer.append(", ");
				}
			}
		}
		return slicer.toString();
	}

	/**
	 * Build a jfreechart CategoryDataset with a single series
	 * 
	 */
	private DefaultCategoryDataset build1dimDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		// column axis
		List columnPositions = result.getAxes()[0].getPositions();
		colCount = columnPositions.size();
		// cells
		List cells = result.getCells();

		String series = "Series";

		// loop on column positions
		for (int i = 0; i < colCount; i++) {
			Member[] colMembers = ((Position) columnPositions.get(i))
					.getMembers();

			StringBuffer key = new StringBuffer();
			// loop on col position members
			for (int j = 0; j < colMembers.length; j++) {
				// build up composite name for this row
				key.append(colMembers[j].getLabel() + ".");
			}
			dataset.addValue(getNumberValue((Cell) cells.get(i)), series,
					key.toString());
		}
		return dataset;
	}

	/**
	 * Get cell value as a Number. Parses the cell value string using the locale
	 * set in this.locale.
	 * 
	 * @param cell
	 * @return value as Number (can be null)
	 */
	private Number getNumberValue(Cell cell) {
		// **** HACK AR 2004.01.10
		// String value = cell.getFormattedValue();
		Object value = cell.getValue();

		DecimalFormatSymbols dfs = new DecimalFormatSymbols(locale);
		DecimalFormat formatter = new DecimalFormat();
		formatter.setDecimalFormatSymbols(dfs);
		Number number = null;
		try {
			number = (Number) value;
			// number = formatter.parse(value, new ParsePosition(0));
		} catch (Exception e) {
			number = null;
		}
		return number;
	}

	/**
	 * Get a unique name string for a dataitem derived from the member chain
	 * 
	 * @param myTree
	 *            (full member tree)
	 * @param members
	 *            - the list to be processed (either X/Y axis)
	 * @return retValue as String
	 */
	private String buildName(MemberTree myTree, Member[] members) {
		String retValue = new String();
		HashMap levelMap = new HashMap();
		HashMap hierarchyMap = new HashMap();
		for (int j = members.length - 1; j >= 0; j--) {
			Member member = members[j];
			while (member != null) {
				// only process if no other items from this level processed -
				// should not be duplicates!
				if (!levelMap.containsValue(member.getLevel())) {
					levelMap.put(member.getLevel().toString(),
							member.getLevel());
					if (member.getRootDistance() == 0) {
						// if root member, only add to name if no other members
						// of the hierarchy are already added
						if (!hierarchyMap.containsValue(member.getLevel()
								.getHierarchy())
								|| myTree.getRootMembers(member.getLevel()
										.getHierarchy()).length > 1) {
							hierarchyMap.put(member.getLevel().getHierarchy()
									.toString(), member.getLevel()
									.getHierarchy());
							retValue = member.getLabel() + "." + retValue;
						}
					} else {
						hierarchyMap.put(member.getLevel().getHierarchy()
								.toString(), member.getLevel().getHierarchy());
						retValue = member.getLabel() + "." + retValue;
					}
				}
				member = myTree.getParent(member);
			}
		}
		return retValue;
	}

	/**
	 * Build a jfreechart CategoryDataset with multiple series
	 * 
	 */
	private DefaultCategoryDataset build2dimDataset() {

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		// column axis

		List columnPositions = result.getAxes()[0].getPositions();// ladX.getPositions();
		int colCount = columnPositions.size();

		// row axis

		List rowPositions = result.getAxes()[1].getPositions();// ladY.getPositions();
		int rowCount = rowPositions.size();
		List cells = result.getCells();

		// get the full member tree
		MemberTree myTree = ((MemberTree) olapModel.getExtension(MemberTree.ID));

		// for each column, starting with the bottom member, progress up the
		// mmeber chain until the root is reached
		// keep track of the levels and hierarchies to avoid duplicates on level
		// or hierarchys.
		// *note: keeping track of the levels might be just extra work, I don't
		// know if they CAN be repeated.
		// if not, that logic can be easily removed (see buildName - above)
		// For each hierarchy, If a root member is reached
		// (getRootDistance()=0), then only include it if there have been no
		// other
		// lower level members already added:
		// ie. All_dim1.dim1_lvl1.dim1_lvl2.All_dim2.dim2_lvl1 renders as
		// dim1_lvl1.dim1_lvl2.dim2_lvl1
		// whereas All_dim1.All_dim2 renders as the same.
		// The important part is that we include each parent on the way up, to
		// ensure a unique name to
		// place in the map for the dataset (no longer overwriting each other)

		for (int i = 0; i < colCount; i++) {
			Position p = (Position) columnPositions.get(i);
			Member colMembers[] = p.getMembers();

			// build the label name for this column
			String label = buildName(myTree, colMembers);

			// For each row, use the same logic to build a unique key for each
			// data item
			for (int k = 0; k < rowCount; k++) {
				Position rp = (Position) rowPositions.get(k);
				Member rowMembers[] = rp.getMembers();

				// build key name
				String key = buildName(myTree, rowMembers);

				Cell cell = (Cell) cells.get((k * colCount) + i);
				dataset.addValue(getNumberValue(cell), label.toString(),
						key.toString());
			}

		}
		return dataset;
	}

	/**
	 * @return
	 */
	public int getColCount() {
		return colCount;
	}

	/**
	 * true means that render() will create a new chart
	 */
	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public void modelChanged(ModelChangeEvent e) {
		this.dirty = true;
	}

	public void structureChanged(ModelChangeEvent e) {
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public int getChartHeight() {
		return chartHeight;
	}

	/**
	 * @param chartHeight
	 */
	public void setChartHeight(int chartHeight) {
		this.chartHeight = chartHeight;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public String getChartTitle() {
		return chartTitle;
	}

	/**
	 * @param chartTitle
	 */
	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public int getChartType() {
		return chartType;
	}

	/**
	 * @param chartType
	 */
	public void setChartType(int chartType) {
		this.chartType = chartType;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public int getChartWidth() {
		return chartWidth;
	}

	/**
	 * @param chartWidth
	 */
	public void setChartWidth(int chartWidth) {
		this.chartWidth = chartWidth;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public String getHorizAxisLabel() {
		return horizAxisLabel;
	}

	/**
	 * @param axisLabel
	 */
	public void setHorizAxisLabel(String axisLabel) {
		horizAxisLabel = axisLabel;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public boolean getShowLegend() {
		return showLegend;
	}

	/**
	 * @param showLegend
	 */
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public String getFontName() {
		return fontName;
	}

	/**
	 * @param titleFont
	 */
	public void setFontName(String fontname) {
		this.fontName = fontname;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public String getVertAxisLabel() {
		return vertAxisLabel;
	}

	/**
	 * @param axisLabel
	 */
	public void setVertAxisLabel(String axisLabel) {
		vertAxisLabel = axisLabel;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public int getFontStyle() {
		return fontStyle;
	}

	/**
	 * @param fontStyle
	 */
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public int getBgColorB() {
		return bgColorB;
	}

	/**
	 * @param bgColorB
	 */
	public void setBgColorB(int bgColorB) {
		this.bgColorB = checkRGB(bgColorB);
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public int getBgColorG() {
		return bgColorG;
	}

	/**
	 * @param bgColorG
	 */
	public void setBgColorG(int bgColorG) {
		this.bgColorG = checkRGB(bgColorG);
		this.dirty = true;
	}

	/**
	 * @return
	 */
	public int getBgColorR() {
		return bgColorR;
	}

	/**
	 * @param bgColorR
	 */
	public void setBgColorR(int bgColorR) {
		this.bgColorR = checkRGB(bgColorR);
		this.dirty = true;
	}

	/**
	 * Enforce limits of 0 - 255 for RGB values.
	 */
	private int checkRGB(int v) {
		if (v > 255) {
			v = 255;
		} else if (v < 0) {
			v = 0;
		}
		return v;
	}

	/**
	 * A URLGenerator class to generate pie urls that work with jpivot
	 * 
	 * @author ati
	 * 
	 */
	public class jpivotPieURLGenerator extends StandardPieURLGenerator {
		/** Prefix to the URL */
		private String prefix = "";

		private List cells = result.getCells();

		private int rowCount;

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
				String controllerURL) {
			this(order, dataset);
			this.prefix = controllerURL;
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

			if (canDrillThrough((Cell) cells.get(cellpos))
					&& (!((Cell) cells.get(cellpos)).isNull())) {
				String id = DomUtils.randomId();
				dispatcher.addRequestListener(id, null,
						new DrillThroughHandler((Cell) cells.get(cellpos)));

				boolean firstParameter = url.indexOf("?") == -1;
				url += firstParameter ? "?" : "&";
				url += id;
				return url;
			} else {
				return null;
			}
		}
	}

	/**
	 * A URLGenerator class to generate chart urls that work with jpivot
	 * 
	 * @author robin
	 * 
	 */
	public class jpivotCategoryURLGenerator extends
			StandardCategoryURLGenerator {

		/** Prefix to the URL */
		private String prefix = "";

		/** Series parameter name to go in each URL */
		private String seriesParameterName = "col";

		/** Category parameter name to go in each URL */
		private String categoryParameterName = "row";

		private List cells = result.getCells();

		jpivotCategoryURLGenerator() {
		}

		jpivotCategoryURLGenerator(String prefix) {
			this.prefix = prefix;
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
		 * @param series
		 *            the series index (zero-based).
		 * @param category
		 *            the category index (zero-based).
		 * 
		 * @return the generated URL
		 */
		public String generateURL(CategoryDataset data, int series, int category) {
			String url = prefix;
			// convert col, row into ordinal
			// series is col, category is row
			// (reverese terminology to jfreechart, that way series on measures
			// which is more logical)
			int cellpos = (category * colCount) + series;
			if (canDrillThrough((Cell) cells.get(cellpos))
					&& (!((Cell) cells.get(cellpos)).isNull())) {
				String id = DomUtils.randomId();
				dispatcher.addRequestListener(id, null,
						new DrillThroughHandler((Cell) cells.get(cellpos)));

				boolean firstParameter = url.indexOf("?") == -1;
				url += firstParameter ? "?" : "&";
				url += id;
				return url;
			} else {
				return null;
			}
		}
	}

	/**
	 * request handler for chart drill through does nothing if cell can't be
	 * drilled through (e.g. calculated measure)
	 * 
	 * @author robin
	 * 
	 */
	// class DrillThroughHandler implements RequestListener {
	// Cell cell;
	// DrillThroughHandler(Cell cell) {
	// this.cell = cell;
	// }
	// public void request(RequestContext context) throws Exception {
	//
	// if ( canDrillThrough(cell) ) {
	//
	// HttpSession session = context.getSession();
	// final String drillTableRef = olapModel.getID() + ".drillthroughtable";
	// com.tonbeller.wcf.table.TableComponent tc =
	// (com.tonbeller.wcf.table.TableComponent)
	// session.getAttribute(drillTableRef);
	// // get a new drill through table model
	// TableModel tm = drillThrough(cell);
	//
	// // need to create a new table model for each drill through request
	// // because table model creates an array of columns
	// tc = new com.tonbeller.wcf.table.TableComponent(drillTableRef, tm);
	// tc.initialize(context);
	// ((DefaultSelectionModel)
	// tc.getSelectionModel()).setMode(SelectionModel.NO_SELECTION);
	// session.setAttribute(drillTableRef, tc);
	// tc.setVisible(true);
	// }
	// }
	// }
	class DrillThroughHandler implements RequestListener {
		Cell cell;

		DrillThroughHandler(Cell cell) {
			this.cell = cell;
		}

		public void request(RequestContext context) throws Exception {

			if (canDrillThrough(cell)) {

				HttpSession session = context.getSession();
				final String drillTableRef = olapModel.getID()
						+ ".drillthroughtable";
				ITableComponent tc = (ITableComponent) session
						.getAttribute(drillTableRef);
				// get a new drill through table model
				TableModel tm = drillThrough(cell);
				tc.setModel(tm);
				tc.setVisible(true);
			}
		}
	}

	protected boolean canDrillThrough(Cell cell) {
		return ((DrillThrough) olapModel.getExtension(DrillThrough.ID))
				.canDrillThrough((Cell) cell.getRootDecoree());
	}

	/**
	 * returns a DrillThroughTableModel for the drill through
	 * 
	 * @param cell
	 * @return
	 */
	protected TableModel drillThrough(Cell cell) {
		return ((DrillThrough) olapModel.getExtension(DrillThrough.ID))
				.drillThrough((Cell) cell.getRootDecoree());
	}

	public boolean isDrillThroughEnabled() {
		return drillThroughEnabled;
	}

	public void setDrillThroughEnabled(boolean drillThroughEnabled) {
		this.drillThroughEnabled = drillThroughEnabled;
	}

	/**
	 * @return
	 */
	public String getAxisFontName() {
		return axisFontName;
	}

	/**
	 * @param axisFontName
	 */
	public void setAxisFontName(String axisFontName) {
		this.axisFontName = axisFontName;
	}

	/**
	 * @return
	 */
	public int getAxisFontSize() {
		return axisFontSize;
	}

	/**
	 * @param axisFontSize
	 */
	public void setAxisFontSize(int axisFontSize) {
		this.axisFontSize = axisFontSize;
	}

	/**
	 * @return
	 */
	public int getAxisFontStyle() {
		return axisFontStyle;
	}

	/**
	 * @param axisFontStyle
	 */
	public void setAxisFontStyle(int axisFontStyle) {
		this.axisFontStyle = axisFontStyle;
	}

	/**
	 * @return
	 */
	public String getLegendFontName() {
		return legendFontName;
	}

	/**
	 * @param legendFontName
	 */
	public void setLegendFontName(String legendFontName) {
		this.legendFontName = legendFontName;
	}

	/**
	 * @return
	 */
	public int getLegendFontSize() {
		return legendFontSize;
	}

	/**
	 * @param legendFontSize
	 */
	public void setLegendFontSize(int legendFontSize) {
		this.legendFontSize = legendFontSize;
	}

	/**
	 * @return
	 */
	public int getLegendFontStyle() {
		return legendFontStyle;
	}

	/**
	 * @param legendFontStyle
	 */
	public void setLegendFontStyle(int legendFontStyle) {
		this.legendFontStyle = legendFontStyle;
	}

	/**
	 * @return
	 */
	public int getSlicerAlignment() {
		return slicerAlignment;
	}

	/**
	 * @param slicerAlignment
	 */
	public void setSlicerAlignment(int slicerAlignment) {
		this.slicerAlignment = slicerAlignment;
	}

	/**
	 * @return
	 */
	public String getSlicerFontName() {
		return slicerFontName;
	}

	/**
	 * @param slicerFontName
	 */
	public void setSlicerFontName(String slicerFontName) {
		this.slicerFontName = slicerFontName;
	}

	/**
	 * @return
	 */
	public int getSlicerFontSize() {
		return slicerFontSize;
	}

	/**
	 * @param slicerFontSize
	 */
	public void setSlicerFontSize(int slicerFontSize) {
		this.slicerFontSize = slicerFontSize;
	}

	/**
	 * @return
	 */
	public int getSlicerFontStyle() {
		return slicerFontStyle;
	}

	/**
	 * @param slicerFontStyle
	 */
	public void setSlicerFontStyle(int slicerFontStyle) {
		this.slicerFontStyle = slicerFontStyle;
	}

	/**
	 * @return
	 */
	public int getSlicerPosition() {
		return slicerPosition;
	}

	/**
	 * @param slicerPosition
	 */
	public void setSlicerPosition(int slicerPosition) {
		this.slicerPosition = slicerPosition;
	}

	/**
	 * @return
	 */
	public int getLegendPosition() {
		return legendPosition;
	}

	/**
	 * @param legendPosition
	 */
	public void setLegendPosition(int legendPosition) {
		this.legendPosition = legendPosition;
	}

	/**
	 * @return
	 */
	public String getAxisTickFontName() {
		return axisTickFontName;
	}

	/**
	 * @param axisTickFontName
	 */
	public void setAxisTickFontName(String axisTickFontName) {
		this.axisTickFontName = axisTickFontName;
	}

	/**
	 * @return
	 */
	public int getAxisTickFontSize() {
		return axisTickFontSize;
	}

	/**
	 * @param axisTickFontSize
	 */
	public void setAxisTickFontSize(int axisTickFontSize) {
		this.axisTickFontSize = axisTickFontSize;
	}

	/**
	 * @return
	 */
	public int getAxisTickFontStyle() {
		return axisTickFontStyle;
	}

	/**
	 * @param axisTickFontStyle
	 */
	public void setAxisTickFontStyle(int axisTickFontStyle) {
		this.axisTickFontStyle = axisTickFontStyle;
	}

	/**
	 * @return Returns the tickLabelRotate.
	 */
	public int getTickLabelRotate() {
		return tickLabelRotate;
	}

	/**
	 * @param tickLabelRotate
	 *            The tickLabelRotate to set.
	 */
	public void setTickLabelRotate(int tickLabelRotate) {
		this.tickLabelRotate = tickLabelRotate;
	}

	/**
	 * @return
	 */
	public boolean isShowSlicer() {
		return showSlicer;
	}

	/**
	 * @param showSlicer
	 */
	public void setShowSlicer(boolean showSlicer) {
		this.showSlicer = showSlicer;
	}

	/**
	 * @return
	 */
	public String getFilename() {
		return filename;
	}
}
