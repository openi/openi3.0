package org.openi.olap.xmla;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java_cup.runtime.Symbol;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.core.Extension;
import com.tonbeller.jpivot.core.ModelChangeEvent;
import com.tonbeller.jpivot.core.ModelChangeListener;
import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.Formula;
import com.tonbeller.jpivot.olap.mdxparse.FunCall;
import com.tonbeller.jpivot.olap.mdxparse.Lexer;
import com.tonbeller.jpivot.olap.mdxparse.Literal;
import com.tonbeller.jpivot.olap.mdxparse.ParsedQuery;
import com.tonbeller.jpivot.olap.mdxparse.parser;
import com.tonbeller.jpivot.olap.model.Dimension;
import com.tonbeller.jpivot.olap.model.Hierarchy;
import com.tonbeller.jpivot.olap.model.Level;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapDiscoverer;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapItem;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.model.Result;
import com.tonbeller.jpivot.olap.navi.SortRank;
import com.tonbeller.jpivot.olap.query.ExpBean;
import com.tonbeller.jpivot.olap.query.MdxOlapModel;
import com.tonbeller.jpivot.olap.query.PositionNodeBean;
import com.tonbeller.jpivot.olap.query.Quax;
import com.tonbeller.jpivot.olap.query.QuaxBean;
import com.tonbeller.jpivot.olap.query.QueryAdapter;
import com.tonbeller.jpivot.util.TreeNode;

/**
 * Model for XMLA
 * @author SUJEN
 * 
 */
public class XMLA_Model extends MdxOlapModel implements OlapModel,
		QueryAdapter.QueryAdapterHolder {

	static Logger logger = Logger.getLogger(XMLA_Model.class);

	private String ID = null;

	private String uri = null; // "http://TBNTSRV3/XML4A/msxisapi.dll";
	private String user = null;
	private String password = null;
	private String catalog = null; // "Foodmart 2000";
	private String dataSource = null; // "Provider=MSOLAP.2;Data Source=local";

	private String mdxQuery;
	private String currentMdx;

	private ParsedQuery pQuery = null;

	private XMLA_Result result = null;

	private List aDimensions = new ArrayList();
	private List aHierarchies = new ArrayList();
	private List aLevels = new ArrayList();
	private List aMeasures = new ArrayList();
	private List aMembers = new ArrayList();

	private XMLA_QueryAdapter queryAdapter = null;

	private String cube = null; // save cube name
	private boolean isNewCube = false;
	private XMLA_SOAP soap = null;

	private boolean isInitialized = false;
	private Locale loc = null;

	// Cell properties are encoded as FONT_SIZE values
	private Map calcMeasurePropMap;

	/**
	 * default constructor
	 */
	public XMLA_Model() {

		// System.setProperty("http.proxyHost", "localhost"); //
		// "proxy.tonbeller.com"
		// System.setProperty("http.proxyPort", "80");
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
	 * 
	 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(HttpSessionBindingEvent)
	 */
	public void initialize() throws OlapException {
		logger.info(this);

		boolean logInfo = logger.isInfoEnabled();

		if (catalog == null)
			throw new OlapException("XMLA Model requires catalog specification");
		// do we have a special locale setting?
		// if yes, promote it to the connection
		loc = getLocale(); // Locale.GERMANY
		if (loc != null) {
			if (logInfo) {
				String msg = "Locale language=" + loc.getLanguage()
						+ " Country=" + loc.getCountry();
				logger.info(msg);
			}
		}

		if (dataSource != null && dataSource.length() > 0)
			soap = new XMLA_SOAP(uri, user, password, dataSource);
		else
			// discover yourself
			soap = new XMLA_SOAP(uri, user, password);

		if (logInfo) {
			List dsprops = soap.discoverDSProps();
			for (Iterator iter = dsprops.iterator(); iter.hasNext();) {
				OlapItem oi = (OlapItem) iter.next();
				Map pmap = oi.getProperties();
				logger.info("Property: " + oi.getName());
				for (Iterator iterator = pmap.keySet().iterator(); iterator
						.hasNext();) {
					Object keyo = iterator.next();
					logger.info(keyo + "=" + pmap.get(keyo));
				}
			}
		}
		parse(mdxQuery);
		queryAdapter = new XMLA_QueryAdapter(this);
		XMLA_SortRank sortExt = (XMLA_SortRank) getExtension(SortRank.ID);
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
		initCubeMetaData();
		isNewCube = false;
		calcMeasurePropMap = new HashMap();
	}

	/**
	 * Sets the mdxQuery.
	 * 
	 * @param mdxQuery
	 *            The mdxQuery to set
	 */
	public void setMdxQuery(String mdxQuery) {
		this.mdxQuery = mdxQuery;
		this.currentMdx = mdxQuery.replace('\r', ' ');
		if (logger.isInfoEnabled())
			logger.info("setMdxQuery:" + mdxQuery);
	}

	/**
	 * Let Mondrian parse and execute the query
	 * 
	 * @see com.tonbeller.jpivot.olap.model.OlapModel#getResult()
	 * @return Result of Query Execution
	 */
	public synchronized Result getResult() throws OlapException {

		if (result != null)
			return result;

		if (!isInitialized) {
			logger.fatal("Model not initialized");
			throw new OlapException("Model not initialized");
		}

		// initialize cube meta data
		if (isNewCube)
			initCubeMetaData();
		isNewCube = false;

		queryAdapter.onExecute();

		long lBefore = System.currentTimeMillis();
		logger.debug(currentMdx);
		XMLA_Result res = new XMLA_Result(this, soap, catalog, currentMdx);
		long lTime = System.currentTimeMillis() - lBefore;
		logger.debug("Time for executeQuery(ms)=" + lTime);
		// no exception gotten
		result = res;

		queryAdapter.afterExecute(result);

		return result;
	}

	/**
	 * get the result variable without any action
	 * 
	 * @return current XMLA result, or null
	 */
	XMLA_Result currentResult() {
		return result;
	}

	// dsf
	public synchronized Result getDrillResult() throws OlapException {

		if (result != null)
			return result;

		if (!isInitialized) {
			logger.fatal("Model not initialized");
			throw new OlapException("Model not initialized");
		}

		// initialize cube meta data
		if (isNewCube)
			initCubeMetaData();
		isNewCube = false;

		// dsf : call onExecuteDrill
		queryAdapter.onExecuteDrill();

		long lBefore = System.currentTimeMillis();
		logger.debug(currentMdx);
		XMLA_Result res = new XMLA_Result(this, soap, catalog, currentMdx, true);
		long lTime = System.currentTimeMillis() - lBefore;
		logger.debug("Time for executeQuery(ms)=" + lTime);
		// no exception gotten
		result = res;
		queryAdapter.afterExecute(result);
		return result;
	}

	/**
	 * get the MDX for the user to edit
	 * 
	 * @return current MDX statement
	 * @see com.tonbeller.jpivot.olap.query.MdxOlapModel#getCurrentMdx()
	 */
	public String getCurrentMdx() {
		if (queryAdapter != null)
			return queryAdapter.getCurrentMdx();
		else
			return this.mdxQuery;
	}

	/**
	 * set the mdx entered by the user.
	 * 
	 * @task error handling: restore mdx in case of error
	 * @throws OlapException
	 *             if the syntax is invalid
	 * @param mdxQuery
	 */
	public void setUserMdx(String mdxQuery) throws OlapException {
		if (this.currentMdx.equals(mdxQuery))
			return;

		parse(mdxQuery);

		this.mdxQuery = mdxQuery;
		result = null;
		queryAdapter = new XMLA_QueryAdapter(this);
		XMLA_SortRank sortExt = (XMLA_SortRank) getExtension(SortRank.ID);
		if (sortExt != null)
			sortExt.reset();

		if (logger.isInfoEnabled())
			logger.info("setUserMdx =" + mdxQuery);
		this.currentMdx = mdxQuery.replace('\r', ' ');
	}

	/**
	 * Returns the mdxQuery.
	 * 
	 * @return String
	 */
	String getMdxQuery() {
		return mdxQuery;
	}

	/**
	 * Sets the currentMdx.
	 * 
	 * @param currentMdx
	 *            The currentMdx to set
	 */
	void setCurrentMdx(String currentMdx) {
		this.currentMdx = currentMdx.replaceAll("\r", "");
	}

	/**
	 * 
	 * @see com.tonbeller.jpivot.olap.model.OlapModel#getDimensions()
	 */
	public Dimension[] getDimensions() {
		return (Dimension[]) aDimensions.toArray(new Dimension[0]);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.OlapModel#getMeasures()
	 */
	public Member[] getMeasures() {
		return (Member[]) aMeasures.toArray(new Member[0]);
	}

	/**
	 * session terminated, closing connections etc
	 * 
	 * @task close connection here
	 */
	public void destroy() {
		super.destroy();
	}

	public Object getRootDecoree() {
		return this;
	}

	/**
	 * lookup Dimension by unique name
	 * 
	 * @param name
	 * @return Dimension
	 */
	XMLA_Dimension lookupDimByUName(String uName) {

		for (Iterator iter = aDimensions.iterator(); iter.hasNext();) {
			XMLA_Dimension dim = (XMLA_Dimension) iter.next();
			if (dim.getUniqueName().equals(uName))
				return dim;
		}

		return null;
	}

	/**
	 * lookup hierarchy by unique name
	 * 
	 * @param name
	 * @return Hierarchy
	 */
	XMLA_Hierarchy lookupHierByUName(String uName) {

		for (Iterator iter = aHierarchies.iterator(); iter.hasNext();) {
			XMLA_Hierarchy hier = (XMLA_Hierarchy) iter.next();
			if (hier.getUniqueName().equals(uName))
				return hier;
		}

		return null;
	}

	/**
	 * lookup level by unique name
	 * 
	 * @param name
	 * @return level
	 */
	XMLA_Level lookupLevelByUName(String uName) {

		for (Iterator iter = aLevels.iterator(); iter.hasNext();) {
			XMLA_Level lev = (XMLA_Level) iter.next();
			if (lev.getUniqueName().equals(uName))
				return lev;
		}

		return null;
	}

	/**
	 * lookup member by unique name
	 * 
	 * @param name
	 * @return Member
	 */
	public Member lookupMemberByUName(String uName) {

		for (Iterator iter = aMembers.iterator(); iter.hasNext();) {
			XMLA_Member mem = (XMLA_Member) iter.next();
			if (mem.getUniqueName().equals(uName))
				return mem;
		}

		return null;
	}

	/**
	 * add all members for an hierarchy
	 * 
	 * @param hier
	 */
	/*
	 * protected void addMembers(XMLA_Hierarchy hier) throws OlapException { if
	 * (hier.isMembersGotten()) return; try {
	 * discoverMembers(hier.getUniqueName()); } catch (SOAPException ex) { throw
	 * new OlapException("SOAP Error getting members"); }
	 * 
	 * }
	 */

	/**
	 * add single member
	 */
	void addMember(XMLA_Member mem) {
		aMembers.add(mem);
	}

	/**
	 * remove single member
	 */
	public void removeMember(XMLA_Member mem) {
		if (aMembers.contains(mem))
			aMembers.remove(mem);
	}

	/**
	 * create a Memento bean object holding current state.
	 * 
	 * @return Memento current state
	 */
	public Object getBookmarkState(int levelOfDetail) {
		try {
			XMLA_Memento memento = new XMLA_Memento();
			memento.setVersion(XMLA_Memento.CURRENT_VERSION);
			memento.setUri(uri);
			memento.setDataSource(dataSource);
			memento.setCatalog(catalog);
			memento.setUser(user);
			memento.setPassword(password);
			// set the MDX query string
			// When the state is reset, this mdx will be parsed as the
			// startup query.
			memento.setMdxQuery(currentMdx);
			boolean useQuax = queryAdapter.isUseQuax();
			memento.setUseQuax(useQuax);
			if (useQuax) {
				XMLA_Quax[] quaxes = (XMLA_Quax[]) queryAdapter.getQuaxes();

				QuaxBean[] quaxBeans = new QuaxBean[quaxes.length];
				for (int i = 0; i < quaxes.length; i++) {
					quaxBeans[i] = new QuaxBean();
					beanFromQuax(quaxBeans[i], quaxes[i]);
				} // for i quaxes

				// set quaxes to memento
				memento.setQuaxes(quaxBeans);
			}
			// axes swapped
			memento.setAxesSwapped(queryAdapter.isSwapAxes());
			// sorting
			XMLA_SortRank sortExt = (XMLA_SortRank) getExtension(SortRank.ID);
			if (sortExt != null)
				storeSort(sortExt, memento);
			// calc measure property assignment
			if (calcMeasurePropMap != null)
				memento.setCalcMeasurePropMap(calcMeasurePropMap);
			return memento;
		} catch (OlapException e) {
			logger.error(null, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * restore state from Memento.
	 * 
	 * @param Object
	 *            state bean to be restored
	 */
	public void setBookmarkState(Object state) {

		XMLA_Memento memento = (XMLA_Memento) state;

		mdxQuery = memento.getMdxQuery();
		try {
			if (isInitialized) {
				// already initialized, only new query adapter needed

				parse(mdxQuery);
				queryAdapter = new XMLA_QueryAdapter(this);
				XMLA_SortRank sortExt = (XMLA_SortRank) getExtension(SortRank.ID);
				if (sortExt != null)
					sortExt.reset();
				isNewCube = false;
			} else {

				uri = memento.getUri();
				dataSource = memento.getDataSource();
				catalog = memento.getCatalog();
				mdxQuery = memento.getMdxQuery();

				// Regardless of any state, we will have to process the start
				// MDX.
				// It might contain WITH MEMBER declarations, which must not be
				// lost.
				// The start MDX is processed in the QueryAdapter c'tor.

				initialize();
			}

			// need to get the result first, so that members are gotten
			result = (XMLA_Result) getResult();
		} catch (OlapException e) {
			// should really not occur
			String err = e.getMessage();
			logger.fatal(err);
			throw new RuntimeException(err);
		}
		boolean useQuax = memento.isUseQuax();
		queryAdapter.setUseQuax(useQuax);

		if (useQuax) {
			// reset the Quaxes to current state
			QuaxBean[] quaxBeans = memento.getQuaxes();
			Quax quaxes[] = queryAdapter.getQuaxes();
			// update the quaxes
			try {
				quaxesFromBeans(quaxes, quaxBeans);
			} catch (OlapException e) {
				throw new IllegalArgumentException(e.toString());
			}
		}
		// sorting
		XMLA_SortRank sortExt = (XMLA_SortRank) getExtension(SortRank.ID);
		restoreSort(sortExt, memento);

		// swap axes if neccessary
		queryAdapter.setSwapAxes(memento.isAxesSwapped());

		// calc measure property assignment
		calcMeasurePropMap = memento.getCalcMeasurePropMap();
		if (calcMeasurePropMap == null)
			calcMeasurePropMap = new HashMap();
		result = null;
	}

	/**
	 * 
	 * @param rootBean
	 * @return
	 */
	private TreeNode createPosTreeFromBean(PositionNodeBean rootBean)
			throws OlapException {
		ExpBean expBean = rootBean.getReference(); // null for root
		Exp exp;
		if (expBean == null)
			exp = null;
		else
			exp = (Exp) createExpFromBean(expBean);
		TreeNode node = new TreeNode(exp);
		PositionNodeBean[] beanChildren = rootBean.getChildren();
		for (int i = 0; i < beanChildren.length; i++) {
			TreeNode childNode = createPosTreeFromBean(beanChildren[i]);
			node.addChildNode(childNode);
		}
		return node;
	}

	/**
	 * 
	 * @param expBean
	 * @return
	 * @throws OlapException
	 */
	protected Object createExpFromBean(ExpBean expBean) throws OlapException {
		if (expBean.getType() == ExpBean.TYPE_MEMBER) {
			XMLA_Member member = (XMLA_Member) lookupMemberByUName(expBean
					.getName());
			if (member == null) {
				retrieveMember(expBean.getName());
			}
			member = (XMLA_Member) lookupMemberByUName(expBean.getName());
			if (member == null) {
				// probably schema changed, cannot restore state
				throw new OlapException("could not find member "
						+ expBean.getName());
			}
			return member;
		} else if (expBean.getType() == ExpBean.TYPE_FUNCALL) {
			// FunCall
			String name = expBean.getName();
			ExpBean[] argBeans = expBean.getArgs();
			Exp[] args = new Exp[argBeans.length];
			for (int i = 0; i < argBeans.length; i++) {
				args[i] = (Exp) createExpFromBean(argBeans[i]);
			}

			int synType = XMLA_Util.funCallSyntax(name);
			FunCall f = new FunCall(name, args, synType);
			return f;
		} else if (expBean.getType() == ExpBean.TYPE_LEVEL) {
			// Level
			XMLA_Level lev = lookupLevelByUName(expBean.getName());
			if (lev == null) {
				// probably schema changed, cannot restore state
				throw new OlapException("could not find Level "
						+ expBean.getName());
			}
			return lev;
		} else if (expBean.getType() == ExpBean.TYPE_HIER) {
			// Hierarchy
			XMLA_Hierarchy hier = lookupHierByUName(expBean.getName());
			if (hier == null) {
				// probably schema changed, cannot restore state
				throw new OlapException("could not find Hierarchy "
						+ expBean.getName());
			}
			return hier;
		} else if (expBean.getType() == ExpBean.TYPE_DIM) {
			// Dimension
			XMLA_Dimension dim = lookupDimByUName(expBean.getName());
			if (dim == null) {
				// probably schema changed, cannot restore state
				throw new OlapException("could not find Dimension "
						+ expBean.getName());
			}
			return dim;
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
			Double ddd = (Double) expBean.getLiteralValue();
			return Literal.create(ddd);
		} else
			throw new OlapException("Invalid ExpBean Type " + expBean.getType());
	}

	protected ExpBean createBeanFromExp(Object exp) throws OlapException {
		ExpBean bean = new ExpBean();
		if (exp instanceof Member) {
			XMLA_Member m = (XMLA_Member) exp;
			bean.setType(ExpBean.TYPE_MEMBER);
			bean.setName(m.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof FunCall) {
			FunCall f = (FunCall) exp;
			bean.setType(ExpBean.TYPE_FUNCALL);
			bean.setName(f.getFunction());
			ExpBean[] args = new ExpBean[f.getArgs().length];
			for (int i = 0; i < args.length; i++) {
				args[i] = createBeanFromExp(f.getArgs()[i]);
			}
			bean.setArgs(args);
		} else if (exp instanceof Level) {
			XMLA_Level lev = (XMLA_Level) exp;
			bean.setType(ExpBean.TYPE_LEVEL);
			bean.setName(lev.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof Hierarchy) {
			XMLA_Hierarchy hier = (XMLA_Hierarchy) exp;
			bean.setType(ExpBean.TYPE_HIER);
			bean.setName(hier.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof Dimension) {
			XMLA_Dimension dim = (XMLA_Dimension) exp;
			bean.setType(ExpBean.TYPE_DIM);
			bean.setName(dim.getUniqueName());
			bean.setArgs(new ExpBean[0]);
		} else if (exp instanceof Literal) {
			Literal lit = (Literal) exp;
			Object val = lit.getValueObject();
			if (lit.type == Literal.TYPE_NUMERIC) {
				if (val instanceof Integer)
					bean.setType(ExpBean.TYPE_INTEGER_LITERAL);
				else
					bean.setType(ExpBean.TYPE_DOUBLE_LITERAL);
			} else {
				bean.setType(ExpBean.TYPE_STRING_LITERAL);
			}
			bean.setLiteralValue(val);
			bean.setArgs(new ExpBean[0]);
		} else {
			logger.fatal("cannot create ExpBean type ="
					+ exp.getClass().toString());
		}
		return bean;
	}

	/**
	 * 
	 * @param mdxQuery
	 */
	private void parse(String mdxQuery) throws OlapException {

		// parse the query string
		parser parser_obj;
		Reader reader = new StringReader(mdxQuery);
		parser_obj = new parser(new Lexer(reader));

		Symbol parse_tree = null;
		pQuery = null;
		try {
			parse_tree = parser_obj.parse();
		} catch (Exception e) {
			throw new OlapException(e);
		}
		pQuery = (ParsedQuery) parse_tree.value;
		pQuery.afterParse();

		String newCube = pQuery.getCube();
		if (cube == null || !cube.equals(newCube)) {
			isNewCube = true;
			cube = newCube;
		}
	}

	/**
	 * get meta data from mdx data source
	 */
	private void initCubeMetaData() throws OlapException {

		/*
		 * // find out, whether this cube contains mandatory SAP variables //
		 * SAP bug // SAP variables are not returned fully, // only data type
		 * and description. if (this.isSAP()){ List sapvars =
		 * discoverer.discoverSapVar(catalog, pQuery.getCube()); }
		 */

		// read dimensions
		List dims = soap.discoverDim(catalog, pQuery.getCube());
		evaluateDimensions(dims);

		// read hierarchies
		List hiers = soap.discoverHier(catalog, pQuery.getCube(), null);
		evaluateHiers(hiers);

		// now, as we got all hierarchies, assign them to the dimensions
		// first, create HashMap of dimensions
		HashMap hDim = new HashMap();
		for (Iterator iter = aDimensions.iterator(); iter.hasNext();) {
			XMLA_Dimension dim = (XMLA_Dimension) iter.next();
			hDim.put(dim.getUniqueName(), dim);
		}
		for (Iterator iter = aHierarchies.iterator(); iter.hasNext();) {
			XMLA_Hierarchy hier = (XMLA_Hierarchy) iter.next();
			XMLA_Dimension dim = (XMLA_Dimension) hDim.get(hier
					.getDimUniqueName());
			if (dim != null) {
				dim.addHier(hier);
				hier.setDimension(dim);
			}
		}

		// read levels
		List levels = soap.discoverLev(catalog, pQuery.getCube(), null, null);
		evaluateLevels(levels);

		// now, as we got all Levels, assign them to the hierarchies
		// first, create HashMap of dimensions
		HashMap hHier = new HashMap();
		for (Iterator iter = aHierarchies.iterator(); iter.hasNext();) {
			XMLA_Hierarchy hier = (XMLA_Hierarchy) iter.next();
			hHier.put(hier.getUniqueName(), hier);
		}
		for (Iterator iter = aLevels.iterator(); iter.hasNext();) {
			XMLA_Level lev = (XMLA_Level) iter.next();
			XMLA_Hierarchy hier = (XMLA_Hierarchy) hHier.get(lev
					.getHierUniqueName());
			if (hier != null) {
				hier.addLevel(lev);
				lev.setHierarchy(hier);
			}
		}

		// now go through levels of hierarchy and set the child/parent pointers
		for (Iterator iter = aHierarchies.iterator(); iter.hasNext();) {
			XMLA_Hierarchy hier = (XMLA_Hierarchy) iter.next();
			adjustLevels(hier);
		}

		// get all properties for the cube
		// assign properties to levels (not SAP)
		// assign properties to dimensions (SAP)
		List propList = null;
		try {
			propList = soap.discoverProp(getCatalog(), getCube(), null, null,
					null);
		} catch (OlapException e) {
			// not handled
			logger.error("?", e);
		}

		if (propList != null && propList.size() > 0) {
			PropLoop: for (Iterator iterator = propList.iterator(); iterator
					.hasNext();) {
				OlapItem poi = (OlapItem) iterator.next();
				String name = poi.getName();
				// what is the level for this property
				// SAP: level information is garbage
				String propType = poi.getProperty("PROPERTY_TYPE");
				// 1 == MDPROP_MEMBER
				// 2 = MDPROP_CELL
				if (!"1".equals(propType))
					continue PropLoop; // only member properties
				String levUName = poi.getProperty("LEVEL_UNIQUE_NAME");
				String dimUName = poi.getProperty("DIMENSION_UNIQUE_NAME");
				String caption = poi.getCaption();
				if (caption == null)
					caption = name;
				XMLA_Dimension dim = this.lookupDimByUName(dimUName);
				if (this.isSAP() || this.isMondrian()) {
					// dimension contain the properties
					XMLA_MemberProp prop = new XMLA_MemberProp(name, caption,
							dim);
					dim.addProp(prop);

				} else {
					// Microsoft, use Level
					if (levUName != null && levUName.length() > 0) {
						XMLA_Level level = lookupLevelByUName(levUName);
						XMLA_MemberProp prop = new XMLA_MemberProp(name,
								caption, level);
						level.addProp(prop);
					}
				}

			}
		}

	}

	/**
	 * set paren and child level
	 * 
	 * @param hier
	 */
	private void adjustLevels(XMLA_Hierarchy hier) {
		XMLA_Level[] levels = (XMLA_Level[]) hier.getLevels();
		XMLA_Level[] orderedLevels = new XMLA_Level[levels.length];
		for (int i = 0; i < levels.length; i++) {
			int num = levels[i].getDepth();
			orderedLevels[num] = levels[i];
		}
		for (int i = 0; i < levels.length; i++) {
			int num = levels[i].getDepth();
			if (num > 0) {
				levels[i].setParentLevel(orderedLevels[num - 1]);
			} else {
				levels[i].setParentLevel(null);
			}

			if (num < levels.length - 1) {
				levels[i].setChildLevel(orderedLevels[num + 1]);
			} else {
				levels[i].setChildLevel(null);
			}
		}

	}

	/**
	 * Evaluate the result of Discovery MDSCHEMA_DIMENSIONS request
	 * 
	 * @param dims
	 *            list of dimension items
	 */
	private void evaluateDimensions(List dims) {

		for (Iterator iter = dims.iterator(); iter.hasNext();) {
			OlapItem dimit = (OlapItem) iter.next();

			XMLA_Dimension dim = new XMLA_Dimension();
			dim.setName(dimit.getName());
			dim.setCaption(dimit.getCaption());
			dim.setUniqueName(dimit.getUniqueName());

			String str = dimit.getProperty("DIMENSION_ORDINAL");
			if (str != null)
				dim.setOrdinal(Integer.parseInt(str));

			str = dimit.getProperty("DIMENSION_TYPE");
			if (str != null)
				dim.setType(Integer.parseInt(str));

			str = dimit.getProperty("DIMENSION_CARDINALITY");
			if (str != null)
				dim.setCardinality(Integer.parseInt(str));

			str = dimit.getProperty("DEFAULT_HIERARCHY");
			if (str != null)
				dim.setDefaultHier(str);

			str = dimit.getProperty("IS_VIRTUAL");
			if (str != null)
				dim.setVirtual(str.equals("true"));

			str = dimit.getProperty("IS_READWRITE");
			if (str != null)
				dim.setReadWrite(str.equals("true"));

			str = dimit.getProperty("DIMENSION_UNIQUE_SETTINGS");
			if (str != null)
				dim.setUniqueSettings(Integer.parseInt(str));

			str = dimit.getProperty("DIMENSION_IS_VISIBLE");
			if (str != null)
				dim.setVisible(str.equals("true"));

			aDimensions.add(dim);
		}

	}

	/**
	 * Evaluate the result of Discovery MDSCHEMA_HIERARCHIES request
	 * 
	 * @param hiers
	 *            list of olap items
	 */
	private void evaluateHiers(List hiers) {

		for (Iterator iter = hiers.iterator(); iter.hasNext();) {
			OlapItem hierit = (OlapItem) iter.next();

			XMLA_Hierarchy hier = new XMLA_Hierarchy(this);
			// hier.setName(hierit.getName());
			hier.setCaption(hierit.getCaption());
			hier.setUniqueName(hierit.getUniqueName());

			String str = hierit.getProperty("HIERARCHY_CAPTION");
			if (str != null)
				hier.setCaption(str);

			str = hierit.getProperty("DIMENSION_UNIQUE_NAME");
			if (str != null)
				hier.setDimUniqueName(str);

			str = hierit.getProperty("DIMENSION_TYPE");
			if (str != null)
				hier.setDimType(Integer.parseInt(str));

			str = hierit.getProperty("HIERARCHY_CARDINALITY");
			if (str != null)
				hier.setCardinality(Integer.parseInt(str));

			str = hierit.getProperty("DEFAULT_MEMBER");
			if (str != null)
				hier.setDefaultMember(str);

			str = hierit.getProperty("ALL_MEMBER");
			if (str != null)
				hier.setAllMember(str);

			str = hierit.getProperty("STRUCTURE");
			if (str != null)
				hier.setStructure(Integer.parseInt(str));

			str = hierit.getProperty("IS_VIRTUAL");
			if (str != null)
				hier.setVirtual(str.equals("true"));

			str = hierit.getProperty("IS_READWRITE");
			if (str != null)
				hier.setReadWrite(str.equals("true"));

			str = hierit.getProperty("DIMENSION_UNIQUE_SETTINGS");
			if (str != null)
				hier.setDimUniqueSettings(Integer.parseInt(str));

			str = hierit.getProperty("DIMENSION_IS_VISIBLE");
			if (str != null)
				hier.setDimVisible(str.equals("true"));

			str = hierit.getProperty("HIERARCHY_ORDINAL");
			if (str != null)
				hier.setOrdinal(Integer.parseInt(str));

			str = hierit.getProperty("DIMENSION_IS_SHARED");
			if (str != null)
				hier.setDimShared(str.equals("true"));

			aHierarchies.add(hier);
		}
	}

	/**
	 * Evaluate the result of Discovery MDSCHEMA_LEVELS request
	 * 
	 * @param levels
	 *            List of OlapItems
	 */
	private void evaluateLevels(List levels) {

		for (Iterator iter = levels.iterator(); iter.hasNext();) {
			OlapItem levit = (OlapItem) iter.next();
			XMLA_Level lev = new XMLA_Level(this);
			lev.setName(levit.getName());
			lev.setCaption(levit.getCaption());
			lev.setUniqueName(levit.getUniqueName());

			String str = levit.getProperty("DIMENSION_UNIQUE_NAME");
			if (str != null)
				lev.setDimUniqueName(str);

			str = levit.getProperty("HIERARCHY_UNIQUE_NAME");
			if (str != null)
				lev.setHierUniqueName(str);

			str = levit.getProperty("LEVEL_NUMBER");
			if (str != null)
				lev.setNumber(Integer.parseInt(str));

			str = levit.getProperty("LEVEL_CARDINALITY");
			if (str != null)
				lev.setCardinality(Integer.parseInt(str));

			str = levit.getProperty("LEVEL_TYPE");
			if (str != null)
				lev.setType(Integer.parseInt(str));

			str = levit.getProperty("CUSTOM_ROLLUP_SETTINGS");
			if (str != null)
				lev.setCustomRollupSettings(Integer.parseInt(str));

			str = levit.getProperty("LEVEL_UNIQUE_SETTINGS");
			if (str != null)
				lev.setUniqueSettings(Integer.parseInt(str));

			str = levit.getProperty("LEVEL_IS_VISIBLE");
			if (str != null)
				lev.setVisible(str.equals("true"));

			str = levit.getProperty("LEVEL_ORDERING_PROPERTY");
			if (str != null)
				lev.setOrderingProperty(str);

			str = levit.getProperty("LEVEL_DBTYPE");
			if (str != null)
				lev.setDbType(Integer.parseInt(str));

			str = levit.getProperty("LEVEL_NAME_SQL_COLUMN_NAME");
			if (str != null)
				lev.setNameSqlColumnName(str);

			str = levit.getProperty("LEVEL_KEY_SQL_COLUMN_NAME");
			if (str != null)
				lev.setKeySqlColumnName(str);

			str = levit.getProperty("LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME");
			if (str != null)
				lev.setUniqueNameSqlColumnName(str);

			aLevels.add(lev);
		}

	}

	/**
	 * retrieve the cube's members for a given level
	 * 
	 * @throws OlapException
	 */
	void completeLevel(XMLA_Level level) throws OlapException {

		List mems = soap.discoverMem(catalog, cube, null,
				level.getHierUniqueName(), level.getUniqueName());
		// evaluate
		ArrayList aNewMembers = new ArrayList();
		ArrayList aAllMembers = new ArrayList();
		evaluateMembers(mems, aNewMembers, aAllMembers);
		aMembers.addAll(aNewMembers);
		level.setMembers(aAllMembers);
	}

	/**
	 * retrieve a members children
	 * 
	 * @throws OlapException
	 */
	void retrieveMemberChildren(XMLA_Member member) throws OlapException {
		// potential killer

		// 1=children
		List mems = soap.discoverMemTree(catalog, cube, member.getUniqueName(),
				1);

		// evaluate
		ArrayList aNewMembers = new ArrayList();
		ArrayList aAllMembers = new ArrayList();
		// one level, properties are delivered
		evaluateMembers(mems, aNewMembers, aAllMembers);

		aMembers.addAll(aNewMembers);
		// assign children
		setMemberChildren(member, aAllMembers);

	}

	/**
	 * retrieve a members parent
	 * 
	 * @throws OlapException
	 */
	void retrieveMemberParent(XMLA_Member member) throws OlapException {

		member.setParentOk(true);
		if (((XMLA_Level) member.getLevel()).getDepth() == 0
				|| ((XMLA_Level) member.getLevel()).getParentLevel() == null) {
			member.setParent(null);
			return;
		}

		if (member.isComplete()) {
			// we know, if there is no parent at all
			String pUname = member.getParentUniqueName();
			if (pUname == null || pUname.length() == 0) {
				member.setParent(null);
				return;
			}
		}

		// 4=parent
		// ###SAP### for a measure, the member itself is returned
		List mems = soap.discoverMemTree(catalog, cube, member.getUniqueName(),
				4);

		// for SAP
		for (Iterator iter = mems.iterator(); iter.hasNext();) {
			OlapItem oli = (OlapItem) iter.next();
			if (oli.getUniqueName().equals(member.getUniqueName())) {
				// member itself
				member.setParent(null);
				return;
			}
		}

		if (mems.size() == 0) {
			member.setParent(null);
			return;
		}

		// evaluate
		ArrayList aNewMembers = new ArrayList();
		ArrayList aAllMembers = new ArrayList();
		// one level, properties are delivered
		evaluateMembers(mems, aNewMembers, aAllMembers);
		aMembers.addAll(aNewMembers);

		// assign parent
		XMLA_Member pmem = (XMLA_Member) aAllMembers.get(0);
		member.setParent(pmem);

	}

	/**
	 * retrieve the cube's members for a given hierarchy
	 * 
	 * @throws OlapException
	 */
	void completeMember(XMLA_Member member) throws OlapException {
		// 8 = self
		List mems = soap.discoverMemTree(catalog, cube, member.getUniqueName(),
				8);
		// evaluate
		ArrayList aNewMembers = new ArrayList();
		ArrayList aAllMembers = new ArrayList();
		evaluateMembers(mems, aNewMembers, aAllMembers);

		/*
		 * // if this member is resulting from a "children" function call, //
		 * then it is a good idea, to complete all its siblings in one call List
		 * mems; Member parent = member.getParent(); boolean parentChildren =
		 * false; if (parent != null && queryAdapter.isChildrenOnAxis(parent)) {
		 * // 1 = children mems = soap.discoverMemTree(catalog, cube,
		 * parent.getUniqueName(), 1); parentChildren = true; } else { // 8 =
		 * self mems = soap.discoverMemTree(catalog, cube,
		 * member.getUniqueName(), 8); } // evaluate ArrayList aNewMembers = new
		 * ArrayList(); ArrayList aAllMembers = new ArrayList();
		 * evaluateMembers(mems, aNewMembers, aAllMembers, true);
		 * 
		 * if (parentChildren) { for (Iterator iter = aAllMembers.iterator();
		 * iter.hasNext();) { XMLA_Member m = (XMLA_Member) iter.next(); if
		 * (!m.isParentOk()) m.setParent((XMLA_Member) parent); } }
		 * aMembers.addAll(aNewMembers);
		 */
	}

	/**
	 * retrieve a member by unique name
	 * 
	 * @throws OlapException
	 */

	public void retrieveMember(String uniqueName) throws OlapException {

		// 8 = self
		List mems = soap.discoverMemTree(catalog, cube, uniqueName, 8);

		// evaluate
		ArrayList aNewMembers = new ArrayList();
		ArrayList aAllMembers = new ArrayList();
		evaluateMembers(mems, aNewMembers, aAllMembers);
		aMembers.addAll(aNewMembers);
		XMLA_Member member = (XMLA_Member) this.lookupMemberByUName(uniqueName);
		if (member == null) {
			throw new OlapException("could not find member " + uniqueName);
		}
	}

	/**
	 * assign children member
	 * 
	 * @param member
	 * @param aAllMembers
	 */
	private void setMemberChildren(XMLA_Member member, ArrayList aAllMembers) {
		ArrayList aChildren = new ArrayList();

		for (Iterator iter = aAllMembers.iterator(); iter.hasNext();) {
			XMLA_Member mem = (XMLA_Member) iter.next();
			if (member.getUniqueName().equals(mem.getParentUniqueName()))
				aChildren.add(mem); // mem is a child
		}

		member.setChildren(aChildren);
		member.setChildrenOk(true);

		for (Iterator iter = aChildren.iterator(); iter.hasNext();) {
			XMLA_Member child = (XMLA_Member) iter.next();
			child.setParent(member);
			child.setParentOk(true);
		}
	}

	/**
	 * assign children and parent to member
	 * 
	 * @param member
	 * @param aAllMembers
	 */
	/*
	 * private void setParentAndChildren(XMLA_Member member, ArrayList
	 * aAllMembers) { ArrayList aChildren = new ArrayList();
	 * 
	 * for (Iterator iter = aAllMembers.iterator(); iter.hasNext();) {
	 * XMLA_Member mem = (XMLA_Member) iter.next(); if
	 * (member.getUniqueName().equals(mem.getParentUniqueName()))
	 * aChildren.add(mem); // mem is a child
	 * 
	 * if (mem.getUniqueName().equals(member.getParentUniqueName())) {
	 * 
	 * member.setParent(mem); // mem is the parent } }
	 * 
	 * member.setChildren(aChildren);
	 * 
	 * for (Iterator iter = aChildren.iterator(); iter.hasNext();) { XMLA_Member
	 * child = (XMLA_Member) iter.next(); child.setParent(member); } }
	 */
	/**
	 * Evaluate the result of Discovery MDSCHEMA_MEMBERS request
	 * 
	 * @param evalProps
	 *            true, if properties should be evaluated
	 * @param mems
	 *            List of OlapItems
	 */
	private void evaluateMembers(List mems, List aNewMembers, List aAllMembers) {

		MLoop: for (Iterator iter = mems.iterator(); iter.hasNext();) {
			OlapItem oi = (OlapItem) iter.next();

			String uName = oi.getUniqueName();
			XMLA_Member mem = (XMLA_Member) this.lookupMemberByUName(uName);
			if (mem != null) {
				aAllMembers.add(mem); // already there
			} else {
				// not there, create new
				String caption = oi.getCaption();
				String levUName = oi.getProperty("LEVEL_UNIQUE_NAME");
				// long levelNumber = Long.parseLong((String)
				// hMemItems.get("LEVEL_NUMBER")); Microsoft
				// String hierUName = oi.getProperty("HIERARCHY_UNIQUE_NAME");
				XMLA_Level lev = this.lookupLevelByUName(levUName);
				boolean isCalc = isMemberInFormulas(uName);
				mem = new XMLA_Member(this, uName, caption, lev, isCalc);
				aNewMembers.add(mem);
				aAllMembers.add(mem);
			}
			if (!mem.isComplete()) {
				// set the secondary values (not gotten from result)
				long ordinal = 0;
				String sOrdinal = oi.getProperty("MEMBER_ORDINAL");
				if (sOrdinal != null)
					ordinal = Long.parseLong(sOrdinal);
				// not always there for SAP
				String name = oi.getName();
				int type = 0;
				String str = oi.getProperty("MEMBER_TYPE");
				if (str != null)
					type = Integer.parseInt(str);
				long childrenCard = 0;
				String sCard = oi.getProperty("CHILDREN_CARDINALITY");
				if (sCard != null)
					childrenCard = Long.parseLong(sCard);
				// not always there for SAP, default 0
				long parentLevel = 0;
				String sLev = oi.getProperty("PARENT_LEVEL");
				if (sLev != null)
					parentLevel = Long.parseLong(sLev);
				// not always there for SAP, default 0
				String parentUniqueName = oi.getProperty("PARENT_UNIQUE_NAME");
				String key = oi.getProperty("MEMBER_KEY");
				boolean isPlaceHolderMember = ("true".equals(oi
						.getProperty("IS_PLACEHOLDERMEMBER")));
				boolean isDataMember = ("true".equals(oi
						.getProperty("IS_DATAMEMBER")));
				mem.complete(name, type, ordinal, parentUniqueName,
						childrenCard, parentLevel, isDataMember,
						isPlaceHolderMember, key);
			}
			// properties gotten by result, DIMENSION PROPERTIES
			/*
			 * if (!mem.isPropsOk() && evalProps) { mem.setPropsOk(true);
			 * XMLA_Level lev = (XMLA_Level) mem.getLevel(); Map propMap =
			 * lev.getProps(); Map oiProps = oi.getProperties(); for (Iterator
			 * iterator = propMap.values().iterator(); iterator.hasNext();) {
			 * XMLA_MemberProp mProp = (XMLA_MemberProp) iterator.next(); String
			 * xmlTag = mProp.getXmlTag(); if (oiProps.containsKey(xmlTag)) {
			 * PropertyImpl prop = new PropertyImpl();
			 * prop.setName(mProp.getName()); prop.setValue((String)
			 * oiProps.get(xmlTag)); mem.addProp(prop); } } }
			 */
		} // MLoop
	}

	/**
	 * find out, whether a member is in the formulas
	 * 
	 * @param uName
	 *            - unique name
	 * @return
	 */
	public boolean isMemberInFormulas(String uName) {
		if (pQuery == null)
			return false;
		Formula[] formulas = pQuery.getFormulas();
		for (int i = 0; i < formulas.length; i++) {
			if (formulas[i].isMember()
					&& uName.equals(formulas[i].getUniqeName()))
				return true;
		}
		return false;
	}

	/**
	 * Returns the catalog.
	 * 
	 * @return String
	 */
	public String getCatalog() {
		return catalog;
	}

	/**
	 * Returns the uri.
	 * 
	 * @return String
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the catalog.
	 * 
	 * @param catalog
	 *            The catalog to set
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	/**
	 * Returns the dataSource.
	 * 
	 * @return String
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the dataSource.
	 * 
	 * @param dataSource
	 *            The dataSource to set
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Sets the uri.
	 * 
	 * @param uri
	 *            The uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the pQuery.
	 * 
	 * @return ParsedQuery
	 */
	public ParsedQuery getPQuery() {
		return pQuery;
	}

	/**
	 * Returns the queryAdapter.
	 * 
	 * @return XMLA_QueryAdapter
	 */
	public QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}

	/**
	 * @return ID
	 */
	public String getID() {
		return ID;
	}

	/**
	 * @param ID
	 */
	public void setID(String string) {
		ID = string;
	}

	/**
	 * @return user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 */
	public void setUser(String string) {
		user = string;
	}

	/**
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param string
	 */
	public void setPassword(String string) {
		password = string;
	}

	public boolean isSAP() {
		return (soap.getProvider() == OlapDiscoverer.PROVIDER_SAP);
	}

	public boolean isMicrosoft() {
		return (soap.getProvider() == OlapDiscoverer.PROVIDER_MICROSOFT);
	}

	public boolean isMondrian() {
		return (soap.getProvider() == OlapDiscoverer.PROVIDER_MONDRIAN);
	}

	/**
	 * @return cube name
	 */
	public String getCube() {
		if (pQuery == null)
			return null;
		else
			return pQuery.getCube();
	}

	/**
	 * @return calcMeasurePropMap
	 */
	public Map getCalcMeasurePropMap() {
		return calcMeasurePropMap;
	}

	public void setServletContext(ServletContext servletContext) {
		// we don't need it
	}

} // End XMLA_Model

