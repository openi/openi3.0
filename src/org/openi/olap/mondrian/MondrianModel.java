package org.openi.olap.mondrian;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import mondrian.mdx.DimensionExpr;
import mondrian.mdx.HierarchyExpr;
import mondrian.mdx.LevelExpr;
import mondrian.mdx.MdxVisitorImpl;
import mondrian.mdx.MemberExpr;
import mondrian.mdx.ParameterExpr;
import mondrian.mdx.UnresolvedFunCall;
import mondrian.olap.Category;
import mondrian.olap.Cube;
import mondrian.olap.Exp;
import mondrian.olap.Formula;
import mondrian.olap.FunCall;
import mondrian.olap.Literal;
import mondrian.olap.MondrianException;
import mondrian.olap.Parameter;
import mondrian.olap.ParameterImpl;
import mondrian.olap.Query;
import mondrian.olap.QueryAxis;
import mondrian.olap.Role;
import mondrian.olap.SchemaReader;
import mondrian.olap.Syntax;
import mondrian.olap.Util;
import mondrian.olap.type.NumericType;
import mondrian.olap.type.Type;
import mondrian.olap.ResultLimitExceededException;
import mondrian.olap.MemoryLimitExceededException;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.spi.CatalogLocator;
import mondrian.spi.impl.ServletContextCatalogLocator;
import mondrian.util.MemoryMonitor;
import mondrian.util.MemoryMonitorFactory;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.core.Extension;
import com.tonbeller.jpivot.core.ModelChangeEvent;
import com.tonbeller.jpivot.core.ModelChangeListener;
import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.model.Result;
import com.tonbeller.jpivot.olap.navi.SortRank;
import com.tonbeller.jpivot.olap.query.ExpBean;
import com.tonbeller.jpivot.olap.query.MdxOlapModel;
import com.tonbeller.jpivot.olap.query.Memento;
import com.tonbeller.jpivot.olap.query.PositionNodeBean;
import com.tonbeller.jpivot.olap.query.QueryAdapter;
import com.tonbeller.wcf.bookmarks.Bookmarkable;

/**
 * The Model represents all (meta-)data for an MDX query.
 */
public class MondrianModel extends MdxOlapModel implements OlapModel,
		QueryAdapter.QueryAdapterHolder {

	@Override
	public Object getBookmarkState(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	static Logger logger = Logger.getLogger(MondrianModel.class);

	/**
	 * JPivot by default converts MDX from a logical to a physical
	 * representation of the metadata after an expansion, from "all children" to
	 * a list of the specific children. If one wants to save the query, having
	 * the query represent the current state of the data, the physical state,
	 * may mean that the saved query will not execute at some future date
	 * because children and come and go.
	 */
	static final String LOGICAL_MDX_PROP = "com.tonbeller.jpivot.mondrian.logical.mdx";

	/**
	 * The maximum number of cells a given request can return. If more are
	 * returned, a limit exceeded exception is thrown. Mondrian has a property
	 * that limits how many rows of data can be read in, but thats not
	 * sufficient. Consider, fifty thousand rows are read in but when the axis
	 * is "NON EMPTY" only 25 result versus "NON EMPTY" is not set on the axis
	 * and all fifty thousand are returned. One can not rely on the Mondrian
	 * "mondrian.result.limit" property to know what to do. So, this new
	 * property allows JPivot users to set how big tables can be (limit is rows
	 * time columns, total number of cells); this limits the size of the html
	 * page that is generated. By default, there is no limit.
	 */
	public static final String CELL_LIMIT_PROP = "com.tonbeller.jpivot.mondrian.cell.limit";

	/**
	 * The default value of the cell limit, 0 means that there is no limit.
	 */
	public static final Integer CELL_LIMIT_DEFAULT = new Integer(0);

	/*
	 * sample value
	 * provider=Mondrian;Jdbc=jdbc:odbc:MondrianFoodMart;Catalog=file
	 * :///c:/dev/mondrian/demo/FoodMart.xml
	 */
	private String connectString = null;
	private Util.PropertyList connectProperties = null;

	/*
	 * sample values sun.jdbc.odbc.JdbcOdbcDriver com.mysql.jdbc.Driver
	 */
	private String jdbcDriver = null;
	private mondrian.olap.Connection monConnection = null;

	/**
	 * the initial MDX query. This is never changed except when the user enters
	 * a new MDX query.
	 */
	private String mdxQuery;

	private Role role = null;

	private String currentMdx;
	private MondrianResult result = null;
	private HashMap hDimensions = new HashMap();
	private HashMap hHierarchies = new HashMap();
	private HashMap hLevels = new HashMap();
	private HashMap hMembers = new HashMap();
	private ArrayList aMeasures = new ArrayList();

	private List aLogicalModel = new LinkedList();

	private MondrianQueryAdapter queryAdapter = null;
	private Listener listener = null;

	private boolean isInitialized = false;
	private String ID = null;
	private Locale loc = null;

	private String sessionId = null;
	private String dynresolver = null;

	// selected locale to be used by dynResolver (if given)
	private String dynLocale = null;;
	boolean useChecksum = false;

	private boolean connectionPooling = true; // Mondrian connection Pooling

	private DataSource externalDataSource = null;

	private ServletContext servletContext = null;

	private Object bookMark = null;

	private String dataSourceChangeListener = null;

	public String getID() {
		return ID;
	}

	/**
	 * check for OutOfMemory
	 */
	final void checkListener() throws MemoryLimitExceededException {
		if (this.listener != null) {
			this.listener.check();
		}
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	/**
	 * constructor must be "default"
	 */
	public MondrianModel() {
		this.mdxQuery = null;
		this.currentMdx = null;

		addModelChangeListener(new ModelChangeListener() {
			public void modelChanged(ModelChangeEvent e) {
				result = null; // will force re-execution of query
			}

			public void structureChanged(ModelChangeEvent e) {
				result = null; // will force re-execution of query
			}
		});

	}

	/**
	 * Attempts to use the Query object's SchemaReader because this is based
	 * upon the Cube (calculated Memember) and not just the Connection. Cube
	 * SchemaReader contains calculated members. Query SchemaReader contains
	 * Cube plus defined members
	 * 
	 * @return
	 */
	public SchemaReader getSchemaReader() {
		return (queryAdapter != null) ? queryAdapter.getSchemaReader()
				: getConnection().getSchemaReader();
	}

	/**
	 * Returns the queryAdapter.
	 * 
	 * @return MondrianQueryAdapter
	 */
	public QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}

	/**
	 * Let Mondrian parse and execute the query
	 * 
	 * @see com.tonbeller.jpivot.olap.model.OlapModel#getResult()
	 * @return Result of Query Execution
	 */
	public synchronized Result getResult() throws OlapException {

		if (result != null) {
			return result;
		}

		if (!isInitialized) {
			throw new OlapException("Model not initialized");
		}

		this.listener = new Listener();
		MemoryMonitor mm = MemoryMonitorFactory.getMemoryMonitor();
		try {
			mm.addListener(this.listener);

			queryAdapter.onExecute();

			mondrian.olap.Result monResult = null;
			boolean tryagain = false;

			try {
				String mdx = null;
				if (Boolean.getBoolean(LOGICAL_MDX_PROP)) {
					mondrian.olap.Query modiQuery = rewriteMDXQuery(queryAdapter
							.getMonQuery());
					queryAdapter.setMonQuery(modiQuery);
					mdx = queryAdapter.getMonQuery().toString();
					setCurrentMdx(mdx);
				}
				if (logger.isDebugEnabled()) {
					if (mdx == null) {
						mdx = queryAdapter.getMonQuery().toString();
					}
					logger.debug(mdx);
				}
				// check for OutOfMemory
				this.listener.check();

				long t1 = System.currentTimeMillis();
				monResult = monConnection.execute(queryAdapter.getMonQuery());

				// check for OutOfMemory
				this.listener.check();

				if (logger.isInfoEnabled()) {
					long t2 = System.currentTimeMillis();
					logger.info("query execution time " + (t2 - t1) + " ms");
				}

			} catch (MondrianException ex) {
				Throwable rootCause = getRootCause(ex);
				if (rootCause instanceof ResultLimitExceededException) {
					// the result limit was exceeded - roll back
					logger.warn("Mondrian result limit exceeded: "
							+ rootCause.getMessage());
					if (bookMark != null) {
						setBookmarkState(bookMark);
						tryagain = true;
					}
				} else if (rootCause instanceof mondrian.olap.InvalidHierarchyException) {
					// there was no member for an hierarchy
					logger.warn("Mondrian Hierarchy with no members: "
							+ rootCause.getMessage());
					throw new EmptyCubeException(rootCause);
				} else {
					// the cause of the exception is different from
					// "Result Limit Exceeded"
					throw new OlapException(ex);
				}

				// NOTE: This code is never reached.
				if (!tryagain) {
					throw new ResultTooLargeException(ex);
				}
			}
			if (tryagain) {
				// roll back to bookmark occurred
				// a Result Limit Overflow will not occur here
				try {
					long t1 = System.currentTimeMillis();
					monResult = monConnection.execute(queryAdapter
							.getMonQuery());
					// check for OutOfMemory
					this.listener.check();

					if (logger.isInfoEnabled()) {
						long t2 = System.currentTimeMillis();
						logger.info("rollback query execution time "
								+ (t2 - t1) + " ms");
					}

				} catch (MondrianException ex) {
					// should not occur
					// throw ResultTooLargeException because this was the
					// original problem
					throw new ResultTooLargeException(
							"Error running previous query (prior to Result Overflow)",
							ex);
				}
			}
			result = new MondrianResult(monResult, this);
			if (tryagain) {
				result.setOverflowOccured(true);
			}

			queryAdapter.afterExecute(result);

			// set a bookmark, so that we can roll back to that state
			if (!tryagain) {
				bookMark = retrieveBookmarkState(EXTENSIONAL);
			}
		} finally {
			mm.removeListener(this.listener);
			this.listener = null;
		}

		return result;
	}

	/**
	 * get the result variable without any action
	 * 
	 * @return current Mondrian result, or null
	 */
	MondrianResult currentResult() {
		return result;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.OlapModel#getDimensions()
	 */
	public Dimension[] getDimensions() {
		return (Dimension[]) hDimensions.values().toArray(new Dimension[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.OlapModel#getMeasures()
	 */
	public Member[] getMeasures() {
		return (Member[]) aMeasures.toArray(new Member[0]);
	}

	/**
	 * set the Mondrian Connect String
	 * 
	 * @param connectString
	 *            Connect String - default:
	 *            provider=Mondrian;Jdbc=jdbc:odbc:MondrianFoodMart;
	 *            Catalog=file:///c:/j/mondrian/demo/FoodMart.xml
	 */
	public void setConnectString(String connectString) {
		this.connectString = connectString;
		result = null;
		queryAdapter = null;
		monConnection = null;
		if (logger.isInfoEnabled())
			logger.info("connectString=" + connectString);
	}

	/**
	 * set the Mondrian Connection Properties as an alternative to
	 * setConnectString
	 * 
	 * @param properties
	 */
	public void setConnectProperties(Util.PropertyList properties) {
		connectProperties = properties;
		result = null;
		queryAdapter = null;
		monConnection = null;
		if (logger.isInfoEnabled())
			logger.info("connectProperties=" + connectProperties);
	}

	/**
	 * set the JDBC Driver
	 * 
	 * @param jdbcDriver
	 *            JDBC Driver - default: sun.jdbc.odbc.JdbcOdbcDriver
	 */
	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
		result = null;
		queryAdapter = null;
		monConnection = null;
		if (logger.isInfoEnabled())
			logger.info("jdbcDriver=" + jdbcDriver);
	}

	/**
	 * Sets the mdxQuery.
	 * 
	 * @param mdxQuery
	 *            The mdxQuery to set
	 */
	public void setMdxQuery(String mdxQuery) {

		if (logger.isInfoEnabled())
			logger.info("setMdxQuery:" + mdxQuery);

		this.mdxQuery = mdxQuery;
		this.currentMdx = mdxQuery.replaceAll("\r", "");
		result = null;
		queryAdapter = null;
	}

	/**
	 * complete the initilization.
	 */
	public void initialize() throws OlapException {
		logger.info(this);
		boolean logInfo = logger.isInfoEnabled();

		// load the jdbc Driver
		if (jdbcDriver != null) {
			try {
				Class.forName(jdbcDriver);
			} catch (Exception ex) {
				String err = "Could not load Jdbc Driver " + jdbcDriver;
				logger.error(err);
				throw new OlapException(err);
			}
		}

		Util.PropertyList properties = getConnectProperties();

		boolean updatedProperties = false;

		if (properties == null) {
			properties = Util.parseConnectString(connectString);
			updatedProperties = true;
		}

		// get the Catalog from connect string
		String catString = properties.get("Catalog");
		URI uri = null;
		try {
			if (catString != null) {
				uri = new URI(catString);
			}
		} catch (URISyntaxException e) {
			// throw new IllegalArgumentException("Illegal Schema Url " +
			// catString );
			// ignore;
		}

		if (uri != null && uri.getScheme().equalsIgnoreCase("http")
				&& sessionId != null) {
			// an http schema url will be dynamically resolved
			// in that case, a session id has to be appended
			if (uri.getQuery() != null) {
				catString = catString + "&sessionId=" + sessionId;
			} else {
				catString = catString + "?sessionId=" + sessionId;
			}
			properties.put(RolapConnectionProperties.Catalog.name(), catString);
			updatedProperties = true;
		}

		if (dynresolver != null && dynresolver.length() > 0) {
			properties.put(
					RolapConnectionProperties.DynamicSchemaProcessor.name(),
					dynresolver);
			updatedProperties = true;
		}
		if (dynLocale != null) {
			properties.put(RolapConnectionProperties.Locale.name(), dynLocale);
			updatedProperties = true;
		}
		if (dataSourceChangeListener != null
				&& dataSourceChangeListener.length() > 0) {
			properties.put(
					RolapConnectionProperties.DataSourceChangeListener.name(),
					dataSourceChangeListener);
			updatedProperties = true;
		}

		// if we do *not* want connection pooling, we must explicitly tell
		// Mondrian
		if (!connectionPooling) {
			properties
					.put(RolapConnectionProperties.PoolNeeded.name(), "false");
			updatedProperties = true;
		}

		if (useChecksum) {
			properties.put(RolapConnectionProperties.UseContentChecksum.name(),
					"true");
			updatedProperties = true;
		}

		if (updatedProperties) {
			setConnectProperties(properties);
		}

		CatalogLocator catalogLocator = new ServletContextCatalogLocator(
				servletContext);

		// use external DataSource if present
		monConnection = mondrian.olap.DriverManager.getConnection(properties,
				catalogLocator, externalDataSource);

		if (monConnection == null) {
			String err = "Could not create Mondrian connection:" + properties;
			logger.error(err);
			throw new OlapException(err);
		}

		if (this.role != null) {
			monConnection.setRole(this.role);
		}

		if (logInfo)
			logger.info("MondrianModel: opening connection " + properties);

		// do we have a special locale setting?
		// if yes, promote it to the connection
		loc = getLocale(); // Locale.GERMANY
		if (loc != null) {
			if (logInfo) {
				String msg = "Locale language=" + loc.getLanguage()
						+ " Country=" + loc.getCountry();
				logger.info(msg);
			}
			((RolapConnection) monConnection).setLocale(loc);
		}

		mondrian.olap.Query monQuery;
		try {
			monQuery = parseMDX();
		} catch (OlapException e) {
			String err = e.getMessage();
			logger.error(err);
			throw new OlapException(err);
		}
		resetMetaData(monQuery); // reset the model data

		queryAdapter = new MondrianQueryAdapter(this, monQuery);

		MondrianSortRank sortExt = (MondrianSortRank) getExtension(SortRank.ID);
		if (sortExt != null)
			sortExt.reset();

		isInitialized = true;

		// as initialization is complete, notify extensions
		Map extMap = getExtensions();
		Collection extensions = extMap.values();
		for (Iterator iter = extensions.iterator(); iter.hasNext();) {
			Extension extension = (Extension) iter.next();
			extension.modelInitialized();
		}
	}

	/**
	 * parse
	 * 
	 * @return @throws OlapException
	 */
	private mondrian.olap.Query parseMDX() throws OlapException {
		mondrian.olap.Query monQuery;
		try {
			monQuery = getConnection().parseQuery(mdxQuery);
		} catch (MondrianException ex) {
			logger.error("Parse Failure", ex);
			// try to get a meaningfullerror message on parse failure
			Throwable rootCause = getRootCause(ex);
			throw new OlapException(rootCause.getMessage());
		} catch (Exception ex) {
			// not expected
			logger.fatal("unexpected parse failure " + ex.getMessage());
			throw new OlapException(ex);
		}
		if (monQuery == null) {
			logger.fatal("unexpected parse failure");
			throw new OlapException("unexpected parse failure");
		}
		return monQuery;
	}

	/**
	 * find root cause for exception
	 */
	private Throwable getRootCause(MondrianException ex) {
		Throwable rootCause = ex;
		Throwable cause = ex.getCause();
		while (cause != null && cause != rootCause) {
			rootCause = cause;
			cause = cause.getCause();
		}
		return rootCause;
	}

	/**
	 * add Dimension to Hashtable, if not already there
	 * 
	 * @param monDimension
	 *            - the "key" is the Mondrian Dimension
	 */
	private void addDimension(mondrian.olap.Dimension monDimension) {
		String uniqueName = monDimension.getUniqueName();
		if (!hDimensions.containsKey(uniqueName)) {
			MondrianDimension dimension = new MondrianDimension(monDimension,
					this);
			hDimensions.put(uniqueName, dimension);
			// make sure, that all hierarchies are initialized
			mondrian.olap.Hierarchy[] monHiers = monDimension.getHierarchies();
			for (int i = 0; i < monHiers.length; i++) {
				this.addHierarchy(monHiers[i], dimension);
			}
		}
	}

	/**
	 * add Hierarchy to Hashtable, if not already there
	 * 
	 * @param monHierarchy
	 *            - the "key" is the Mondrian Hierarchy
	 */
	private void addHierarchy(mondrian.olap.Hierarchy monHierarchy,
			MondrianDimension dimension) {
		String uniqueName = monHierarchy.getUniqueName();
		if (!hHierarchies.containsKey(uniqueName)) {
			MondrianHierarchy hierarchy = new MondrianHierarchy(monHierarchy,
					dimension, this);
			hHierarchies.put(uniqueName, hierarchy);
			// make sure, that all levels are initialized
			SchemaReader scr = getSchemaReader();
			List monLevels = scr.getHierarchyLevels(monHierarchy);
			for (int i = 0; i < monLevels.size(); i++) {
				this.addLevel((mondrian.olap.Level) monLevels.get(i), hierarchy);
			}
		}
	}

	/**
	 * add Level to Hashtable, if not already there
	 * 
	 * @param monLevel
	 *            - the "key" is the Mondrian Level
	 */
	protected void addLevel(mondrian.olap.Level monLevel,
			MondrianHierarchy hierarchy) {
		String uniqueName = monLevel.getUniqueName();
		if (!hLevels.containsKey(uniqueName)) {
			MondrianLevel level = new MondrianLevel(monLevel, hierarchy, this);
			hLevels.put(uniqueName, level);
		}
	}

	/**
	 * add Member to Hashtable, if not already there
	 * 
	 * @param monMember
	 *            - the "key" is the Mondrian Member
	 * @return the corresponding member
	 */
	public MondrianMember addMember(mondrian.olap.Member monMember) {
		String uniqueName = monMember.getUniqueName();
		if (hMembers.containsKey(uniqueName)) {
			return (MondrianMember) hMembers.get(uniqueName);
		} else {
			mondrian.olap.Level monLevel = monMember.getLevel();
			MondrianLevel level = this.lookupLevel(monLevel.getUniqueName());
			MondrianMember member = new MondrianMember(monMember, level, this);
			hMembers.put(uniqueName, member);
			if (monMember.isMeasure())
				aMeasures.add(member);
			return member;
		}
	}

	/**
	 * remove Member from Hashtable (for a calculated member)
	 * 
	 * @param uniqueName
	 */
	public void removeMember(String uniqueName) {
		if (hMembers.containsKey(uniqueName)) {
			MondrianMember m = (MondrianMember) hMembers.get(uniqueName);
			if (aMeasures.contains(m))
				aMeasures.remove(m);
			hMembers.remove(uniqueName);
		}
	}

	/**
	 * find the Dimension.
	 * 
	 * @param uniqueName
	 *            is the search key (
	 * @return the corresponding MondrianDimension
	 */
	public MondrianDimension lookupDimension(String uniqueName) {
		return (MondrianDimension) hDimensions.get(uniqueName);
	}

	/**
	 * find the Hierarchy in the dimensions.
	 * 
	 * @param uniqueName
	 *            is the search key
	 * @return the corresponding hierarchy
	 */
	public MondrianHierarchy lookupHierarchy(String uniqueName) {
		return (MondrianHierarchy) hHierarchies.get(uniqueName);
	}

	/**
	 * find member in the Olap Hierarchy.
	 * 
	 * @param uniqueName
	 *            is the search key (Mondrian member unique name)
	 * @return the corresponding member
	 */
	public Member lookupMemberByUName(String uniqueName) {
		// if the unique name was stored in a memento,
		// it is possible, that
		// - the member was not loaded yet
		// - the member was removed from the schema meanwhile
		MondrianMember m = (MondrianMember) hMembers.get(uniqueName);
		if (m != null)
			return m;
		final SchemaReader scr = getSchemaReader();

		List<mondrian.olap.Id.Segment> uniqueNameParts = Util
				.parseIdentifier(uniqueName);
		/*
		 * Pattern pat = Pattern.compile("\\[([^\\]]+)\\]"); Matcher mat =
		 * pat.matcher(uniqueName); int i = 0; ArrayList aName = new
		 * ArrayList(); while (mat.find()) { String group = mat.group(1);
		 * aName.add(group); } String[] uniqueNameParts =
		 * (String[])aName.toArray(new String[0]);
		 */

		Cube cube = queryAdapter.getMonQuery().getCube();
		mondrian.olap.Member monMember = (mondrian.olap.Member) Util
				.lookupCompound(scr, cube, uniqueNameParts, false,
						Category.Member);
		if (monMember != null)
			return addMember(monMember);

		if (monMember == null) {
			// there's still a chance to find the member
			// as a calculated member in a formula
			Formula[] formulas = queryAdapter.getMonQuery().getFormulas();
			for (int i = 0; i < formulas.length; i++) {
				monMember = formulas[i].getMdxMember();
				if (uniqueName.equals(monMember.getUniqueName()))
					return addMember(monMember);
			}
		}
		return null;
	}

	/**
	 * find level in the Olap Hierarchy.
	 * 
	 * @param uniqueName
	 *            is the search key (Mondrian level)
	 * @return the corresponding level
	 */
	public MondrianLevel lookupLevel(String uniqueName) {
		return (MondrianLevel) hLevels.get(uniqueName);
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * @return true if dimension and all of its hierachies can be accessed
	 *         according to role
	 */
	private boolean canAccess(mondrian.olap.Dimension dim) {
		Role role = this.monConnection.getRole();
		if (!role.canAccess(dim))
			return false;
		mondrian.olap.Hierarchy[] hiers = dim.getHierarchies();
		for (int i = 0; i < hiers.length; i++) {
			if (role.canAccess(hiers[i]))
				return true;
		}
		return false;
	}

	/**
	 * reset the model Hashtables.
	 */
	private void resetMetaData(mondrian.olap.Query monQuery) {
		this.hDimensions = new HashMap();
		this.hHierarchies = new HashMap();
		this.hLevels = new HashMap();
		this.hMembers = new HashMap();
		this.aMeasures = new ArrayList();

		// initialize meta data
		mondrian.olap.Cube cube = monQuery.getCube();
		mondrian.olap.Dimension[] monDims = cube.getDimensions();
		for (int i = 0; i < monDims.length; i++) {
			// Is the dimension accessable?
			if (canAccess(monDims[i]))
				this.addDimension(monDims[i]);
		}

		SchemaReader sr = cube.getSchemaReader(null);
		for (int i = 0; i < monDims.length; i++) {
			mondrian.olap.Hierarchy[] monHiers = monDims[i].getHierarchies();
			for (int j = 0; j < monHiers.length; j++) {
				List calcMembers = sr.getCalculatedMembers(monHiers[j]);
				for (Iterator it = calcMembers.iterator(); it.hasNext();) {
					this.addMember((mondrian.olap.Member) it.next());
				}
			}
		}
	}

	/**
	 * get the Mondrian Connection
	 * 
	 * @return The Mondrian Connection
	 */
	public mondrian.olap.Connection getConnection() {
		return monConnection;
	}

	/**
	 * get the MDX for the user to edit
	 * 
	 * @return current MDX statement
	 * @see MdxOlapModel#getCurrentMdx()
	 */
	public String getCurrentMdx() {
		// if the model was changed, due to ModelChangeListener,
		// then the current MDX is not really "current"
		if (result != null)
			return currentMdx;
		else if (queryAdapter == null) {
			return mdxQuery;
		} else {
			// get new result, this will update the mdx
			try {
				getResult();
			} catch (Exception e) {
				logger.error("unexpected Exeption getResult " + e.toString());
				throw new RuntimeException(e);
			}
			return currentMdx;
		}
	}

	/**
	 * set the mdx entered by the user.
	 * 
	 * @task error handling: restore mdx in case of error
	 * @throws OlapException
	 *             if the syntax is invalid
	 * @param mdxQuery
	 */
	boolean setUserMdx(String mdxQuery) throws OlapException {
		if (this.currentMdx.equals(mdxQuery))
			return false;

		String saveMdx = this.mdxQuery;
		this.mdxQuery = mdxQuery;
		if (logger.isInfoEnabled())
			logger.info("setUserMdx =" + mdxQuery);

		mondrian.olap.Query monQuery = null;
		try {
			monQuery = parseMDX();
		} catch (OlapException e) {
			logger.error("setUserMdx failed " + e.getMessage());
			// parse failed, restore old mdx
			this.mdxQuery = saveMdx;
			throw e; // re-throw
		}
		resetMetaData(monQuery); // reset the model data

		queryAdapter = new MondrianQueryAdapter(this, monQuery);

		// no exception gotten
		MondrianSortRank sortExt = (MondrianSortRank) getExtension(SortRank.ID);
		if (sortExt != null)
			sortExt.reset();

		result = null;
		this.currentMdx = mdxQuery.replace('\r', ' ');

		return true;
	}

	/**
	 * Returns the mdxQuery.
	 * 
	 * @return String
	 */
	protected String getMdxQuery() {
		return mdxQuery;
	}

	public Object getRootDecoree() {
		return this;
	}

	/**
	 * session terminated, closing connections etc
	 */
	public void destroy() {
		logger.info(null);
		super.destroy();
		if (monConnection != null) {
			if (logger.isDebugEnabled())
				logger.debug("MondrianModel: closing connection "
						+ monConnection);
			monConnection.close();
			monConnection = null;
		}
		this.sessionId = null;
	}

	/**
	 * Sets the currentMdx.
	 * 
	 * @param currentMdx
	 *            The currentMdx to set
	 */
	protected void setCurrentMdx(String currentMdx) {
		// this.currentMdx = currentMdx.replace('\r', ' ');
		this.currentMdx = currentMdx.replaceAll("\r", "");
	}

	/**
	 * Returns the monConnection.
	 * 
	 * @return mondrian.olap.Connection
	 */
	protected mondrian.olap.Connection getMonConnection() {
		return monConnection;
	}

	/**
	 * Get jdbcDriver.
	 * 
	 * @return jdbcDriver
	 */
	protected String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * Get connectString.
	 * 
	 * @return connectString.
	 */
	protected String getConnectString() {
		return connectString;
	}

	/**
	 * Get connectProperties
	 * 
	 * @return connectProperties.
	 */
	protected Util.PropertyList getConnectProperties() {
		return connectProperties;
	}

	/**
	 * create a Memento bean object holding current state.
	 * 
	 * @return MondrianMemento current state
	 */
	public Object retrieveBookmarkState(int levelOfDetail) {
		if (this.result == null)
			return null;
		try {
			if (levelOfDetail == Bookmarkable.EXTENSIONAL)
				return getExtensionalBookmarkState();
			return getIntensionalBookmarkState();
		} catch (OlapException e) {
			logger.error(null, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * creates a bookmark that will contail as much detail as possible. But this
	 * bookmark may not work when the data in the cube have changed.
	 */
	private Object getExtensionalBookmarkState() throws OlapException {
		MondrianMemento memento = createMemento();
		// set the MDX query string
		// When the state is reset, this mdx will be parsed as the
		// startup query.
		memento.setMdxQuery(currentMdx);
		boolean useQuax = queryAdapter.isUseQuax();
		memento.setUseQuax(useQuax);
		if (useQuax) {
			MondrianQuax[] quaxes = (MondrianQuax[]) queryAdapter.getQuaxes();
			MondrianQuaxBean[] quaxBeans = new MondrianQuaxBean[quaxes.length];
			for (int i = 0; i < quaxes.length; i++) {
				quaxBeans[i] = new MondrianQuaxBean();
				beanFromQuax(quaxBeans[i], quaxes[i]);
			} // for i quaxes
				// set quaxes to memento
			memento.setQuaxes(quaxBeans);
		}
		return memento;
	}

	/**
	 * creates a bookmark that will contail only those data, that are
	 * independent of the data in the cube. This bookmark can be restored even
	 * after cube data has changed.
	 */
	private Object getIntensionalBookmarkState() throws OlapException {
		MondrianMemento memento = createMemento();
		memento.setUseQuax(true);
		MondrianAxis[] axes = (MondrianAxis[]) result.getAxes();
		MondrianQuaxBean[] quaxBeans = new MondrianQuaxBean[axes.length];
		for (int i = 0; i < axes.length; i++)
			quaxBeans[i] = intensionalQuaxBeanFromAxis(axes[i]);
		memento.setQuaxes(quaxBeans);
		// create the appropriate mdx query
		// calculated measures will be adopted if they
		// only deal with measures.
		String newMdx = intensionalMdx(quaxBeans);
		memento.setMdxQuery(newMdx);
		return memento;
	}

	/**
	 * create intensional MDX query
	 */
	private String intensionalMdx(MondrianQuaxBean[] quaxBeans) {

		String saveMdx = this.currentMdx;
		Query cloneQuery = queryAdapter.getMonQuery().safeClone();
		MondrianQuax quaxes[] = new MondrianQuax[quaxBeans.length];
		for (int i = 0; i < quaxes.length; i++) {
			MondrianQuax q = (MondrianQuax) queryAdapter.getQuaxes()[i];
			QueryAxis a = queryAdapter.getMonQuery().axes[i];
			quaxes[i] = new MondrianQuax(q.getOrdinal(), a, this);
		}
		try {
			quaxesFromBeans(quaxes, quaxBeans);
		} catch (OlapException e) {
			logger.error(null, e);
			throw new IllegalArgumentException(e.toString());
		}
		MondrianQuax saveQuaxes[] = (MondrianQuax[]) queryAdapter.getQuaxes();
		queryAdapter.setQuaxes(quaxes);
		Query mQuery = queryAdapter.getMonQuery();
		// clear slicer, members in slicer are supposed to be not intensional
		mQuery.setSlicerAxis(null);
		// regenerate query from quaxes
		queryAdapter.onExecute();
		// remove formulas, which are not for measures only
		Formula[] formulas = mQuery.getFormulas();
		for (int i = 0; i < formulas.length; i++) {
			mondrian.olap.Member m = formulas[i].getMdxMember();
			boolean remove = false;
			if (!m.getDimension().isMeasures()) {
				// not measures - remove
				remove = true;
			} else {
				mondrian.olap.Exp exp = formulas[i].getExpression();
				if (notMeasures(exp))
					remove = true;
			}
			if (remove) {
				String name = formulas[i].getMdxMember().getUniqueName();
				if (mQuery.canRemoveFormula(name)) {
					mQuery.removeFormula(name, true);
				} else {
					logger.fatal("cannot remove formula "
							+ formulas[i].getName());
				}
			}
		}

		queryAdapter.onExecute(); // rewrites currentMdx
		String newMdx = this.currentMdx;
		// restore state
		queryAdapter.setQuaxes(saveQuaxes);
		queryAdapter.setMonQuery(cloneQuery);
		this.currentMdx = saveMdx;
		return newMdx;
	}

	/**
	 * @return true, if exp refers to members of a dimension other than Measures
	 */
	private boolean notMeasures(mondrian.olap.Exp exp) {
		if (exp instanceof mondrian.olap.Member) {
			mondrian.olap.Member m = (mondrian.olap.Member) exp;
			if (m.getDimension().isMeasures())
				return false;
			else
				return true;
		} else if (exp instanceof mondrian.olap.FunCall) {
			mondrian.olap.FunCall f = (FunCall) exp;
			Exp[] args = f.getArgs();
			for (int i = 0; i < args.length; i++) {
				if (notMeasures(args[i]))
					return true;
			}
		}
		return false;
	}

	/**
	 * creates a memento which is initialized except the quaxes and mdxQuery
	 * 
	 * @see Memento#setQuaxes
	 */
	private MondrianMemento createMemento() {
		MondrianMemento memento = new MondrianMemento();
		memento.setJdbcDriver(jdbcDriver);
		memento.setConnectString(connectString);
		memento.setVersion(MondrianMemento.CURRENT_VERSION);
		// axes swapped
		memento.setAxesSwapped(queryAdapter.isSwapAxes());
		// sorting
		MondrianSortRank sortExt = (MondrianSortRank) getExtension(SortRank.ID);
		if (sortExt != null)
			storeSort(sortExt, memento);
		return memento;
	}

	private MondrianQuaxBean intensionalQuaxBeanFromAxis(MondrianAxis axis)
			throws OlapException {
		MondrianQuaxBean quaxBean = new MondrianQuaxBean();
		MondrianHierarchy[] hiers = (MondrianHierarchy[]) axis.getHierarchies();

		quaxBean.setNDimension(hiers.length);
		quaxBean.setHierarchizeNeeded(false);
		quaxBean.setOrdinal(axis.getOrdinal());

		PositionNodeBean rootBean = new PositionNodeBean();
		rootBean.setReference(null);
		quaxBean.setPosTreeRoot(rootBean);

		PositionNodeBean parentBean = rootBean;
		for (int i = 0; i < hiers.length; i++) {
			ExpBean expBean;
			if (hiers[i].getDimension().isMeasure()) {
				Exp exp = createMeasuresExp(axis, i);
				expBean = createBeanFromExp(exp);
			} else {
				// the toplevel members may have changed when the bookmark is
				// restored,
				// so we can not compute them here
				ExpBean hierBean = createBeanFromExp(hiers[i].getMonHierarchy());
				expBean = new ExpBean();
				expBean.setType(ExpBean.TYPE_TOPLEVEL_MEMBERS);
				expBean.setArgs(new ExpBean[] { hierBean });
			}
			PositionNodeBean nodeBean = new PositionNodeBean();
			nodeBean.setReference(expBean);
			parentBean.setChildren(new PositionNodeBean[] { nodeBean });
			parentBean = nodeBean;
		}
		return quaxBean;
	}

	/**
	 * creates an Exp for the visible Measures
	 * 
	 * @param axis
	 *            the axis containing the measures dimension
	 * @param i
	 *            the i-th hiararchy on that axis is the measures hierarchy
	 */
	private Exp createMeasuresExp(MondrianAxis axis, int i) {
		List measuresList = new ArrayList();
		Set measuresSet = new HashSet();
		for (Iterator it = axis.getPositions().iterator(); it.hasNext();) {
			MondrianPosition mp = (MondrianPosition) it.next();
			MondrianMember member = (MondrianMember) mp.getMembers()[i];
			if (measuresSet.add(member))
				measuresList.add(new MemberExpr(member.getMonMember()));
		}
		if (measuresList.size() == 1)
			return (Exp) measuresList.get(0);
		Exp[] args = (Exp[]) measuresList.toArray(new Exp[measuresList.size()]);
		return new UnresolvedFunCall("{}", Syntax.Braces, args);
	}

	/**
	 * restore state from Memento.
	 * 
	 * @param state
	 *            bean to be restored
	 */
	public void setBookmarkState(Object state) {

		MondrianMemento memento = (MondrianMemento) state;
		int version = memento.getVersion();
		if (version <= 1) {
			logger.warn("Bookmark is of old state (not supported any more in the future)!\nPlease save again!");
		}
		mdxQuery = memento.getMdxQuery();

		if (isInitialized) {
			// already initialized, only new query adapter needed

			mondrian.olap.Query monQuery = null;
			try {
				monQuery = parseMDX();
			} catch (OlapException e) {
				// should really not occur
				String err = e.getMessage();
				logger.fatal(err);
				throw new RuntimeException(err);
			}

			// bookmark may change the cube of the same schema (aml does so)
			resetMetaData(monQuery);

			queryAdapter = new MondrianQueryAdapter(this, monQuery);

			MondrianSortRank sortExt = (MondrianSortRank) getExtension(SortRank.ID);
			if (sortExt != null)
				sortExt.reset();

		} else {

			connectString = memento.getConnectString();
			jdbcDriver = memento.getJdbcDriver();

			// Regardless of any state, we will have to process the start MDX.
			// It might contain WITH MEMBER declarations, which must not be
			// lost.
			// The start MDX is processed in the QueryAdapter c'tor.
			try {
				initialize();
			} catch (OlapException e) {
				// should really not occur
				String err = e.getMessage();
				logger.fatal(err);
				throw new RuntimeException(err);
			}
			// do we have to get a result ???
		}

		boolean useQuax = true;
		if (version >= 3) {
			useQuax = memento.isUseQuax();
			queryAdapter.setUseQuax(useQuax);
		}

		if (useQuax) {
			// reset the Quaxes to current state
			MondrianQuaxBean[] quaxBeans = (MondrianQuaxBean[]) memento
					.getQuaxes();
			MondrianQuax quaxes[] = (MondrianQuax[]) queryAdapter.getQuaxes();
			// update the quaxes

			if (version <= 1) {
				for (int i = 0; i < quaxes.length; i++) {
					boolean qubonMode = quaxBeans[i].isQubonMode();

					quaxes[i].setQubonMode(qubonMode);

					// handle old stuff as of MDX version 2
					if (qubonMode)
						MondrianOldStuff.handleQubonMode(quaxes[i],
								quaxBeans[i], this);
					else
						MondrianOldStuff.handleDrillExMode(quaxes[i],
								quaxBeans[i], this);
				}
			} else {
				try {
					quaxesFromBeans(quaxes, quaxBeans);
				} catch (OlapException e) {
					logger.error(null, e);
					throw new IllegalArgumentException(e.toString());
				}
			}
		}

		// sorting
		MondrianSortRank sortExt = (MondrianSortRank) getExtension(SortRank.ID);
		restoreSort(sortExt, memento);

		// swap axes if neccessary
		queryAdapter.setSwapAxes(memento.isAxesSwapped());

		// we can not fire a structureChanged event here because the bookmark
		// state
		// of other (GUI) components may already be restored. If they receive a
		// structureChanged they would throw away their bookmark state.
		fireModelChanged();
	}

	private Exp[] createExpsFromBeans(ExpBean[] beans) throws OlapException {
		Exp[] exps = new Exp[beans.length];
		for (int i = 0; i < beans.length; i++) {
			exps[i] = (Exp) createExpFromBean(beans[i]);
		}
		return exps;
	}

	/**
	 * create Mondrian exp from expBean
	 * 
	 * @param expBean
	 * @return @throws OlapException
	 */
	protected Object createExpFromBean(ExpBean expBean) throws OlapException {
		if (expBean.getType() == ExpBean.TYPE_TOPLEVEL_MEMBERS) {
			SchemaReader scr = getSchemaReader();
			Exp[] args = createExpsFromBeans(expBean.getArgs());
			HierarchyExpr he = (HierarchyExpr) args[0];
			mondrian.olap.Hierarchy h = he.getHierarchy();
			return MondrianUtil.topLevelMembers(h, false, scr);
		}

		if (expBean.getType() == ExpBean.TYPE_MEMBER) {
			MondrianMember member = (MondrianMember) lookupMemberByUName(expBean
					.getName());
			if (member == null) {
				// probably schema changed, cannot restore state
				throw new OlapException("could not find member "
						+ expBean.getName());
			}
			return new MemberExpr(member.getMonMember());
		}

		if (expBean.getType() == ExpBean.TYPE_FUNCALL) {
			// FunCall
			String name = expBean.getName();
			ExpBean[] argBeans = expBean.getArgs();
			Exp[] args = createExpsFromBeans(argBeans);
			if ("Parameter".equalsIgnoreCase(name)) {
				String paramId = String.valueOf(((Literal) args[0]).getValue());
				Exp value = args[2];
				Type type = value.getType();
				String descr = "";
				if (args.length == 4)
					descr = (String) ((Literal) args[3]).getValue();
				return new ParameterExpr(new ParameterImpl(paramId, value,
						descr, type));
			} else if ("ParamRef".equalsIgnoreCase(name)) {
				// FIXME support ParamRef
				throw new IllegalArgumentException(name);
			}
			Syntax syntax = MondrianUtil.funCallSyntax(name, argBeans.length);
			return new UnresolvedFunCall(name, syntax, args);
		}

		if (expBean.getType() == ExpBean.TYPE_LEVEL) {
			// Level
			MondrianLevel lev = this.lookupLevel(expBean.getName());
			if (lev == null) {
				// probably schema changed, cannot restore state
				throw new OlapException("could not find Level "
						+ expBean.getName());
			}
			return new LevelExpr(lev.getMonLevel());
		} else if (expBean.getType() == ExpBean.TYPE_HIER) {
			// Hierarchy
			MondrianHierarchy hier = this.lookupHierarchy(expBean.getName());
			if (hier == null) {
				// probably schema changed, cannot restore state
				throw new OlapException("could not find Hierarchy "
						+ expBean.getName());
			}
			return new HierarchyExpr(hier.getMonHierarchy());
		} else if (expBean.getType() == ExpBean.TYPE_DIM) {
			// Dimension
			MondrianDimension dim = this.lookupDimension(expBean.getName());
			if (dim == null) {
				// probably schema changed, cannot restore state
				throw new OlapException("could not find Dimension "
						+ expBean.getName());
			}
			return new DimensionExpr(dim.getMonDimension());
		} else if (expBean.getType() == ExpBean.TYPE_STRING_LITERAL) {
			// String literal
			String str = (String) expBean.getLiteralValue();
			return Literal.createString(str);
		} else if (expBean.getType() == ExpBean.TYPE_INTEGER_LITERAL) {
			// Integer literal
			Integer iii = (Integer) expBean.getLiteralValue();
			return Literal.create(iii);
		} else if (expBean.getType() == ExpBean.TYPE_DOUBLE_LITERAL) {
			// Double literal
			Double ddd = new Double(
					((Number) expBean.getLiteralValue()).doubleValue());
			return Literal.create(ddd);
		} else
			throw new OlapException("Invalid ExpBean Type " + expBean.getType());

	}

	protected ExpBean createBeanFromExp(Object exp) throws OlapException {
		ExpBean bean = new ExpBean();

		if (exp instanceof mondrian.olap.Member) {
			mondrian.olap.Member m = (mondrian.olap.Member) exp;
			bean.setType(ExpBean.TYPE_MEMBER);
			bean.setName(m.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof mondrian.olap.Level) {
			mondrian.olap.Level lev = (mondrian.olap.Level) exp;
			bean.setType(ExpBean.TYPE_LEVEL);
			bean.setName(lev.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof mondrian.olap.Hierarchy) {
			mondrian.olap.Hierarchy hier = (mondrian.olap.Hierarchy) exp;
			bean.setType(ExpBean.TYPE_HIER);
			bean.setName(hier.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof mondrian.olap.Dimension) {
			mondrian.olap.Dimension dim = (mondrian.olap.Dimension) exp;
			bean.setType(ExpBean.TYPE_DIM);
			bean.setName(dim.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof mondrian.olap.FunCall) {
			FunCall f = (FunCall) exp;
			bean.setType(ExpBean.TYPE_FUNCALL);
			bean.setName(f.getFunName());
			bean.setArgs(createBeansFromExps(f.getArgs()));
		} else if (exp instanceof ParameterExpr) {
			Parameter p = ((ParameterExpr) exp).getParameter();
			bean.setType(ExpBean.TYPE_FUNCALL);
			bean.setName("Parameter");
			bean.setArgs(createBeansFromExps(getParamterArgs(p)));
			// FIXME support ParamRef
		} else if (exp instanceof MemberExpr) {
			mondrian.olap.Member m = ((MemberExpr) exp).getMember();
			bean.setType(ExpBean.TYPE_MEMBER);
			bean.setName(m.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof LevelExpr) {
			mondrian.olap.Level lev = ((LevelExpr) exp).getLevel();
			bean.setType(ExpBean.TYPE_LEVEL);
			bean.setName(lev.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof HierarchyExpr) {
			mondrian.olap.Hierarchy hier = ((HierarchyExpr) exp).getHierarchy();
			bean.setType(ExpBean.TYPE_HIER);
			bean.setName(hier.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof DimensionExpr) {
			mondrian.olap.Dimension dim = ((DimensionExpr) exp).getDimension();
			bean.setType(ExpBean.TYPE_DIM);
			bean.setName(dim.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof mondrian.olap.Literal) {
			mondrian.olap.Literal lit = (mondrian.olap.Literal) exp;
			Object val = lit.getValue();
			if (lit.getCategory() == Category.Numeric) {
				if (val instanceof Integer)
					bean.setType(ExpBean.TYPE_INTEGER_LITERAL);
				else
					bean.setType(ExpBean.TYPE_DOUBLE_LITERAL);
			} else {
				// String || Symbol
				bean.setType(ExpBean.TYPE_STRING_LITERAL);
			}
			bean.setLiteralValue(val);
			bean.setArgs(new ExpBean[0]);
		} else {
			logger.fatal("cannot create ExpBean type ="
					+ exp.getClass().toString());
			throw new IllegalArgumentException(exp.getClass().toString());
		}

		return bean;
	}

	/**
	 * The "compiled expressions" crap mess up everything.
	 * <p />
	 * Returns the arguments are needed to create an UnresolvedFunCall from a
	 * Parameter.
	 */
	private Exp[] getParamterArgs(Parameter p) {
		Exp expName = Literal.createString(p.getName());
		Exp expValue;
		Exp expType;

		// unwrap the various wrappers of the parameter value
		Object objValue = p.getValue();
		if (objValue == null)
			objValue = p.getDefaultExp();
		if (objValue instanceof Literal)
			objValue = ((Literal) objValue).getValue();
		else if (objValue instanceof MemberExpr)
			objValue = ((MemberExpr) objValue).getMember();

		if (objValue instanceof String) {
			expValue = Literal.createString((String) objValue);
			expType = Literal.createSymbol("STRING");
		} else if (objValue instanceof Double) {
			expValue = Literal.create((Double) objValue);
			expType = Literal.createSymbol("NUMBER");
		} else if (objValue instanceof Integer) {
			expValue = Literal.create((Integer) objValue);
			expType = Literal.createSymbol("NUMBER");
		} else if (objValue instanceof mondrian.olap.Member) {
			mondrian.olap.Member m = (mondrian.olap.Member) objValue;
			expValue = new MemberExpr(m);
			expType = new DimensionExpr(m.getDimension());
		} else
			throw new IllegalArgumentException("unknown Param value: "
					+ objValue + ": " + objValue.getClass());

		Exp expDescr = p.getDescription() != null ? Literal.createString(p
				.getDescription()) : Literal.createString("");
		return new Exp[] { expName, expType, expValue, expDescr };
	}

	private ExpBean[] createBeansFromExps(Object[] exps) throws OlapException {
		ExpBean[] beans = new ExpBean[exps.length];
		for (int i = 0; i < exps.length; i++) {
			beans[i] = createBeanFromExp(exps[i]);
		}
		return beans;
	}

	public DataSource getSqlDataSource() {
		return ((RolapConnection) monConnection).getDataSource();
	}

	public String getDynresolver() {
		return dynresolver;
	}

	public void setDynresolver(String dynresolver) {
		this.dynresolver = dynresolver;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * get Mondrian Connection Pooling property
	 */
	public boolean isConnectionPooling() {
		return connectionPooling;
	}

	/**
	 * set Mondrian Connection Pooling property
	 */
	public void setConnectionPooling(boolean connectionPooling) {
		this.connectionPooling = connectionPooling;
	}

	/**
	 * get the external DataSource to be used by Mondrian
	 */
	public DataSource getExternalDataSource() {
		return externalDataSource;
	}

	/**
	 * set the external DataSource to be used by Mondrian
	 */
	public void setExternalDataSource(DataSource externalDataSource) {
		this.externalDataSource = externalDataSource;
	}

	/**
	 * Getter for property dynLocale.
	 * 
	 * @return Value of property dynLocale.
	 */
	public String getDynLocale() {
		return this.dynLocale;
	}

	/**
	 * Setter for property dynLocale.
	 * 
	 * @param dynLocale
	 *            New value of property dynLocale.
	 */
	public void setDynLocale(String dynLocale) {
		this.dynLocale = dynLocale;
	}

	/**
	 * Getter for property dataSourceChangeListener.
	 * 
	 * @return Value of property dataSourceChangeListener.
	 */
	public String getDataSourceChangeListener() {
		return this.dataSourceChangeListener;
	}

	/**
	 * Setter for property dataSourceChangeListener.
	 * 
	 * @param dataSourceChangeListener
	 *            New value of property dataSourceChangeListener.
	 */
	public void setDataSourceChangeListener(String dataSourceChangeListener) {
		this.dataSourceChangeListener = dataSourceChangeListener;
	}

	/**
	 * Rewrites the given MDX query with a generic version
	 * 
	 * @param queryString
	 *            MDX query text
	 * @return a {@link Result} object
	 */
	protected mondrian.olap.Query rewriteMDXQuery(mondrian.olap.Query query) {
		try {
			QueryVisitor visitor = new QueryVisitor();
			QueryAxis[] axes = query.getAxes();
			for (int i = 0; i < axes.length; i++) {
				axes[i].accept(visitor);
			}

			//
			// Cleans up the physical model for the known corner cases.
			// Case 1 : If the memberlist size = 1, then we should
			// not replace with logical version
			// eg: (Union((Union(Union(Crossjoin({
			// [Wholesaler].[All Wholesalers]},{[Product].[All
			// Products].[Alleya]})
			// (there are no children).
			// In this case we need to retain the product name, even though
			// a logical version might have been used in the previous query
			//
			// Case 2: Within the same member list, if members are not
			// at the same level, remove the lowest members
			// eg: [Time].[2006].[q12006] and [Time].[Avg Sales]
			// are not same
			//
			String originalQuery = query.toString();
			if (logger.isDebugEnabled()) {
				logger.debug("rewriteMDXQuery: originalQuery=" + originalQuery);
			}
			visitor.cleanUpPhysicalModel();
			removeLogicalNameFromList("[Time]");

			boolean changeHappened = false;
			Iterator ite = visitor.getPhysicalModel().iterator();
			while (ite.hasNext()) {
				List list = (List) ite.next();
				Iterator memIterator = list.iterator();

				loop: while (memIterator.hasNext()) {
					String memberName = ((mondrian.olap.Member) memIterator
							.next()).toString();
					List<mondrian.olap.Id.Segment> uniqueNameParts = Util
							.parseIdentifier(memberName);
					String dimension = uniqueNameParts.get(0).toString(); // name

					Iterator logicalIt = aLogicalModel.iterator();
					while (logicalIt.hasNext()) {
						String logicalName = (String) logicalIt.next();

						// First see if the same dimension exists
						// between prev query and this query
						// If exists, then verify that they are
						// at the same level
						// Eg. [product].[All products] is not at
						// the same level of [product].[All products].
						// [abc] whereas [product].[All products].children iss.

						if (logicalName.startsWith(dimension)
								&& isAtSameLevel(memberName, logicalName)) {
							originalQuery = replaceEnumeratedQuery(
									originalQuery, memberName, logicalName);
							changeHappened = true;
							// System.out.println("After replaceEnumeratedQuery originalQuery="+originalQuery);
							break loop;
						}
					}
					// Match Not found.
					// That means there is no logical version exists for this
					// dimension
					break loop;
				}
			}

			mondrian.olap.Query result = query;

			if (changeHappened) {
				// Should we need to parse again? This is required because we
				// have rewritten the query we have to parse it again to store
				// the current logical model so that it can be used next time.
				result = getConnection().parseQuery(originalQuery);
				visitor = new QueryVisitor();
				axes = result.getAxes();
				for (int i = 0; i < axes.length; i++) {
					axes[i].accept(visitor);
				}
			}

			aLogicalModel = visitor.getLogicalModel();

			return result;

		} catch (Exception e) { // SHOULD NOT HAPPEN
			logger.warn(" Error in rewriting MDX Query ");
			e.printStackTrace();
			aLogicalModel.clear();

			// In case of error, return the JPivot generated query
			return query;
		} finally {
			// empty
		}
	}

	/**
	 * Converts a Enumerated MDX with logical one The enumerated list will
	 * always be present with in "{}" Find the first occurrence of the given
	 * member in the query and replace everything with the logicalName until we
	 * find "}"
	 * 
	 * @param result
	 * @return String version of mondrian Result object.
	 */
	public String replaceEnumeratedQuery(final String query,
			final String memberName, final String logicalName) {
		if (logger.isDebugEnabled()) {
			logger.debug("replaceEnumeratedQuery: Given Query is " + query);
		}

		StringBuffer result = new StringBuffer(200);

		int index = query.indexOf(memberName);
		// first occurance of enum
		result.append(query.substring(0, index));
		// append with logical expr
		result.append(logicalName);

		index = query.indexOf("}", index);
		// remaining
		result.append(query.substring(index));

		if (logger.isDebugEnabled()) {
			logger.debug("replaceEnumeratedQuery: Resultant Query is "
					+ result.toString());
		}

		return result.toString();
	}

	/**
	 * Checks if 2 members are at the same level by comparing the no. of "."
	 * [dots] present
	 * 
	 * @param result
	 * @return String version of mondrian Result object.
	 */
	public boolean isAtSameLevel(final String memberName,
			final String logicalName) {
		final List<mondrian.olap.Id.Segment> memberUniqueNameParts = Util
				.parseIdentifier(memberName);
		final List<mondrian.olap.Id.Segment> logicalUniqueNameParts = Util
				.parseIdentifier(logicalName);
		return (memberUniqueNameParts.size() == logicalUniqueNameParts.size());

	}

	/**
	 * Visitor which builds a separate lists for logical members and
	 * physical(enumerated) members
	 */
	static class QueryVisitor extends MdxVisitorImpl {

		// we need quick remove in method removeLogicalNameFromList()
		// which is why this is a LinkedList.
		private LinkedList logicalDimensionList;
		private List physicalDimensionList;
		private List enumMemberList;

		private QueryVisitor() {
			this.logicalDimensionList = new LinkedList();
			this.physicalDimensionList = new ArrayList();
			this.enumMemberList = new ArrayList();

		}

		public Object visit(mondrian.mdx.UnresolvedFunCall call) {

			if (call.getFunName().equalsIgnoreCase("children")
					|| call.getFunName().equalsIgnoreCase("members")) {
				logicalDimensionList.add(call.toString());
				enumMemberList = new ArrayList();
				physicalDimensionList.add(enumMemberList);

			} else if (call.getFunName().equals("{}")) {
				enumMemberList = new ArrayList();
				physicalDimensionList.add(enumMemberList);
			}

			return null;
		}

		public Object visit(mondrian.mdx.ResolvedFunCall call) {

			if (call.getFunName().equalsIgnoreCase("children")
					|| call.getFunName().equalsIgnoreCase("members")) {

				logicalDimensionList.add(call.toString());
				enumMemberList = new ArrayList();
				physicalDimensionList.add(enumMemberList);

			} else if (call.getFunName().equals("{}")) {
				enumMemberList = new ArrayList();
				physicalDimensionList.add(enumMemberList);
			}

			return null;
		}

		public Object visit(mondrian.mdx.MemberExpr memberExpr) {
			mondrian.olap.Member member = memberExpr.getMember();
			enumMemberList.add(member);

			return null;
		}

		public LinkedList getLogicalModel() {
			return logicalDimensionList;
		}

		public List getPhysicalModel() {
			return physicalDimensionList;
		}

		/**
		 * Cleans up the physical model for the known corner cases. Case 1 : If
		 * the memberlist size = 1, then we should not replace with logical
		 * version eg: (Union((Union(Union(Crossjoin({ [Wholesaler].[All
		 * Wholesalers]},{[Product].[All Products].[Alleya]}) In this case we
		 * need to retain the product name, even though a logical version used
		 * in the prev query
		 * 
		 * Case 2: Within the same member list, if members are not at the same
		 * level, remove the lowest members eg: [Time].[2006].[q12006] and
		 * [Time].[Avg Sales] are not same
		 * 
		 * @param result
		 * @return String version of mondrian Result object.
		 */
		public void cleanUpPhysicalModel() {
			List newList = new ArrayList();
			// TODO: use iterator
			for (int i = 0; i < physicalDimensionList.size(); i++) {
				List aList = (List) physicalDimensionList.get(i);
				if (aList.size() != 1 && allSameDimension(aList)) {
					newList.add(aList);
				}
			}
			physicalDimensionList = newList;
		}

		// debug out
		public void print(Object msg) {
			System.out.println(msg);
		}

		// debug out
		public void printLists() {
			Iterator it = logicalDimensionList.iterator();
			while (it.hasNext()) {
				print((String) it.next());
			}
			print(" going to print members ");
			it = physicalDimensionList.iterator();
			while (it.hasNext()) {
				List list = (List) it.next();
				Iterator ite = list.iterator();
				while (ite.hasNext()) {
					print((mondrian.olap.Member) ite.next());
				}
				print(" Going to print next member lists");
			}
		}

		/**
		 * As an example, in a member List get the first Member's hierarchy name
		 * and then make sure that all of the rest are also part of the same
		 * hierarchy.
		 * 
		 * @param list
		 * @return
		 */
		private boolean allSameDimension(List list) {
			if (list.size() == 0) {
				return false;
			}

			// Get the hierarchy name
			String firstMemberName = ((mondrian.olap.Member) list.get(0))
					.toString();
			List<mondrian.olap.Id.Segment> uniqueNameParts = Util
					.parseIdentifier(firstMemberName);
			String dimName = uniqueNameParts.get(0).toString();// name;
			boolean result = true;
			Iterator it = list.iterator();
			while (it.hasNext()) {
				// see if member starts with the same name
				String name = ((mondrian.olap.Member) it.next()).toString();
				if (!name.startsWith(dimName)) {
					result = false;
					break;
				}
			}

			return result;
		}
	}

	/**
	 * sets the dirty hierarchy whose logical representation needs to be removed
	 */
	protected void removeLogicalNameFromList(final String hierName) {
		for (int i = 0; i < aLogicalModel.size(); i++) {
			String listName = (String) aLogicalModel.get(i);
			if (listName.startsWith(hierName)) {
				aLogicalModel.remove(i);
				break;
			}
		}
	}

	class Listener implements MemoryMonitor.Listener {
		String oomMsg;

		Listener() {
		}

		public void memoryUsageNotification(long used, long max) {
			StringBuffer buf = new StringBuffer(200);
			buf.append("OutOfMemory used=");
			buf.append(used);
			buf.append(", max=");
			buf.append(max);
			buf.append(" for mdx: ");
			buf.append(queryAdapter.getMonQuery().toString());
			this.oomMsg = buf.toString();
		}

		final void check() throws MemoryLimitExceededException {
			if (oomMsg != null) {
				throw new MemoryLimitExceededException(oomMsg);
			}
		}
	}

	public boolean isUseChecksum() {
		return useChecksum;
	}

	public void setUseChecksum(boolean useChecksum) {
		this.useChecksum = useChecksum;
	}

} // End MondrianModel

