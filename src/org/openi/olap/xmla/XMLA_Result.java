package org.openi.olap.xmla;

import java.util.*;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.model.Axis;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.QueryResultHandler;
import com.tonbeller.jpivot.olap.model.impl.FormatStringParser;
import com.tonbeller.jpivot.olap.model.impl.PropertyImpl;
import com.tonbeller.jpivot.olap.query.ResultBase;
import org.openi.olap.xmla.XMLA_PropValAssign.ValAssign;

/**
 * Query Result XMLA
 * @author SUJEN
 */
public class XMLA_Result extends ResultBase implements QueryResultHandler {

	static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

	static Logger logger = Logger.getLogger(XMLA_Result.class);

	XMLA_Axis axis; // axis beeing currently processed

	private XMLA_Member[] tuple; // current tuple
	private FormatStringParser formatStringParser = new FormatStringParser();
	private int axisOrdinalMeasures = -1;
	private int nXPositions = 0;
	private Map calcMeasurePos = new HashMap();
	private Map drillHeader;
	private List drillRows;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the associated MondrianModel
	 */
	public XMLA_Result(XMLA_Model model, XMLA_SOAP soap, String catalog,
			String mdx) throws OlapException {
		super(model);
		soap.executeQuery(mdx, catalog, this);
	}

	public XMLA_Result(XMLA_Model model, XMLA_SOAP soap, String catalog,
			String mdx, boolean drillthrough) throws OlapException {
		super(model);
		if (drillthrough) {
			soap.executeDrillQuery(mdx, catalog, this);
		} else {
			soap.executeQuery(mdx, catalog, this);

		}
	}

	/**
	 * handle AxisInfo tag
	 * 
	 * @see com.tonbeller.bii.olap.model.QueryResultHandler#handleAxisInfo
	 */
	public void handleAxisInfo(String axisName, int axisOrdinal) {
		axis = new XMLA_Axis(axisOrdinal, axisName);
		if (axisOrdinal == -1)
			slicer = axis; // -1 = slicer
		else
			axesList.add(axis);
	}

	/**
	 * handle HierarchyInfo tag add hierarchy to Axis beeing currently processed
	 * 
	 * @see com.tonbeller.bii.olap.model.QueryResultHandler#handleHierInfo
	 */
	public void handleHierInfo(String hierName, int axisOrdinal, int number) {
		XMLA_Hierarchy hier;
		if (hierName.indexOf("[") > -1 && hierName.indexOf("]") > -1) {
			hier = ((XMLA_Model) model).lookupHierByUName(hierName);
		} else {
			hier = ((XMLA_Model) model).lookupHierByUName("[" + hierName + "]");
		}

		axis.addHier(hier);
	}

	/**
	 * handle axis
	 * 
	 * @see com.tonbeller.bii.olap.model.QueryResultHandler#handleAxis(java.lang.String,
	 *      int)
	 */
	public void handleAxis(String axisName, int axisOrdinal) {
		axis = lookupAxisByName(axisName);
	}

	/**
	 * handle Tuple
	 * 
	 * @see com.tonbeller.bii.olap.model.QueryResultHandler#handleTuple
	 */
	public void handleTuple(int axisOrdinal, int positionOrdinal) {
		int n = axis.getHierarchies().length;
		tuple = new XMLA_Member[n];
	}

	/**
	 * handle member
	 * 
	 * @see com.tonbeller.bii.olap.model.QueryResultHandler#handleMember
	 */
	public void handleMember(String uniqueName, String caption,
			String levUName, String displayInfo, Map otherProps,
			int axisOrdinal, int positionOrdinal, int memberOrdinal) {
		XMLA_Member member = (XMLA_Member) ((XMLA_Model) model)
				.lookupMemberByUName(uniqueName);
		XMLA_Level lev = ((XMLA_Model) model).lookupLevelByUName(levUName);
		logger.debug("handleMember: uniqueName - " + uniqueName + ", level - "
				+ levUName);
		if (member == null) {
			// not there yet, create +add it
			// the result does not contain all member properties,
			// but it should be sufficient.

			// long levelNumber = -1; //Microsoft
			// attribute Hierarchy is not reliable (SAP)

			boolean isCalc = ((XMLA_Model) model)
					.isMemberInFormulas(uniqueName);
			member = new XMLA_Member(((XMLA_Model) model), uniqueName, caption,
					lev, isCalc);
		} else {
			// for a calculated member, the level might be wrong (guessed)
			XMLA_Level mLev = (XMLA_Level) member.getLevel();
			if (!mLev.equals(lev)) {
				lev = mLev;
				member.setLevel(mLev);
			}
		}
		member.setDisplayInfo(displayInfo);
		// desired member properties here
		// if (model.isSAP() &&
		if (!member.isCalculated()) {
			member.clearProps();
			Map props;
			XMLA_Model xmod = (XMLA_Model) model;
			// HHTASK - performance better use map with *tag* as key
			if (xmod.isSAP() || xmod.isMondrian())
				props = ((XMLA_Dimension) member.getDimension()).getProps();
			else
				props = ((XMLA_Level) member.getLevel()).getProps();
			Iterator itOtherProps = otherProps.keySet().iterator();
			while (itOtherProps.hasNext()) {
				String tag = (String) itOtherProps.next();
				if (xmod.isSAP() || xmod.isMondrian()) {
					if (!tag.startsWith("_"))
						continue; // SAP Property tags always(?) start with "_"
				}
				Iterator itProps = props.values().iterator();
				while (itProps.hasNext()) {
					XMLA_MemberProp prop = (XMLA_MemberProp) itProps.next();
					if (prop.getXmlTag().equals(tag)) {
						String nam = prop.getName();
						String val = (String) otherProps.get(tag);
						if (val != null && val.length() > 0) {
							// TODO set alignment of property - see
							// Property.setAlignmet()
							member.addProp(new PropertyImpl(nam, val));
						}
					}
				}
			}
			// XMLA_MemberProp
		} else {
			// calculated
			if (member.getLevel().getHierarchy().getDimension().isMeasure()) {
				// calculated measure
				axisOrdinalMeasures = axisOrdinal;
				calcMeasurePos.put(new Integer(positionOrdinal), member);
			}
		}

		tuple[memberOrdinal] = member;

		if (memberOrdinal == tuple.length - 1) {
			// this is the last member in the tuple
			// add the position to the axis

			// for a slicerAxis, we do not want the "All" members
			if (axisOrdinal == -1) {
				// slicer
				int n = 0;
				for (int i = 0; i < tuple.length; i++) {
					XMLA_Member m = tuple[i];
					lev = (XMLA_Level) m.getLevel();

					if (lev.getType() == 1) { // MDLEVEL_TYPE_ALL, see oledb.h
						// this is an All Member on the slicer axis, do not add
						// it
						tuple[i] = null;
					} else {
						++n;
					}
				} // for
				if (n < tuple.length) {
					XMLA_Member[] newTuple = new XMLA_Member[n];
					int j = 0;
					for (int i = 0; i < tuple.length; i++) {
						if (tuple[i] != null)
							newTuple[j++] = tuple[i];
					}
					tuple = newTuple;
				}
			}

			XMLA_Position pos = new XMLA_Position(axisOrdinal);
			pos.setMembers(tuple);
			axis.addPosition(pos);
		}
	}

	/**
	 * handle Celldata , start of cell loop
	 * 
	 * @see com.tonbeller.bii.olap.model.QueryResultHandler#handleCellData()
	 */
	public void handleCellData() {
		int nCells = 1;
		for (Iterator iter = axesList.iterator(); iter.hasNext();) {
			XMLA_Axis ax = (XMLA_Axis) iter.next();
			int nPositions = ax.getPositions().size();
			if (nXPositions == 0)
				nXPositions = nPositions; // # positions on axis 0
			nCells = nCells * nPositions;
		}
		// create all cells, as the XMLA result contains only nonempty cells
		for (int i = 0; i < nCells; i++) {
			// dsf pass the model to the cell so that drillthrough knows about
			// the model
			aCells.add(new XMLA_Cell(i, ((XMLA_Model) model)));
		}
	}

	/**
	 * handle Cell
	 * 
	 * @see com.tonbeller.bii.olap.model.QueryResultHandler#handleCell
	 */
	public void handleCell(int iOrdinal, Object value, String fmtValue,
			String fontSize) {

		XMLA_Cell cell = (XMLA_Cell) aCells.get(iOrdinal);
		cell.setValue(value);
		cell.setFormattedValue(fmtValue, formatStringParser);
		if (fontSize != null) {
			int iFontSize = Integer.parseInt(fontSize);

			// is this cell for a calculated measure ?
			// cell ordinal = posY*(AxisXSize-1) + posX
			int posY = iOrdinal / nXPositions;
			int posX = iOrdinal - posY * nXPositions;
			XMLA_Member m = null;
			if (axisOrdinalMeasures == 0)
				m = (XMLA_Member) calcMeasurePos.get(new Integer(posX));
			else if (axisOrdinalMeasures == 1)
				m = (XMLA_Member) calcMeasurePos.get(new Integer(posY));
			if (m != null) {
				Map calcMeasurePropMap = ((XMLA_Model) model)
						.getCalcMeasurePropMap();
				XMLA_PropValAssign cmprops = (XMLA_PropValAssign) calcMeasurePropMap
						.get(m.getUniqueName());
				if (cmprops != null) {
					Set propSet = cmprops.getPropMap().keySet();
					PropLoop: for (Iterator iter = propSet.iterator(); iter
							.hasNext();) {
						String prop = (String) iter.next();
						List valAssignList = cmprops.getValAssignList(prop);
						for (Iterator iterator = valAssignList.iterator(); iterator
								.hasNext();) {
							XMLA_PropValAssign.ValAssign vAssign = (ValAssign) iterator
									.next();
							int mask = vAssign.getBitMask();
							int mVal = mask & iFontSize;
							if (mVal == vAssign.getBitVal()) {
								cell.addProperty(prop, vAssign.getVal());
								continue PropLoop;
							}
						}
					} // PropLoop
				}
			}
		}
	}

	private XMLA_Axis lookupAxisByName(String name) {

		if (name.equals("SlicerAxis"))
			return (XMLA_Axis) slicer;

		for (Iterator iter = axesList.iterator(); iter.hasNext();) {
			XMLA_Axis ax = (XMLA_Axis) iter.next();
			if (ax.getName().equals(name))
				return ax;
		}
		return null;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.model.Result#getAxes()
	 */
	public Axis[] getAxes() {
		return (XMLA_Axis[]) axesList.toArray(new XMLA_Axis[0]);
	}

	// dsf add getters and setters for drillheader and drillrows

	/**
	 * @return Returns the drillHeaders.
	 */
	public Map getDrillHeader() {
		return drillHeader;
	}

	/**
	 * @param drillHeaders
	 *            The drillHeaders to set.
	 */
	public void setDrillHeader(Map drillHeader) {
		this.drillHeader = drillHeader;
	}

	/**
	 * @return Returns the drillRows.
	 */
	public List getDrillRows() {
		return drillRows;
	}

	/**
	 * @param drillRows
	 *            The drillRows to set.
	 */
	public void setDrillRows(List drillRows) {
		this.drillRows = drillRows;
	}

	@Override
	public Axis getSlicer() {
		Axis slicerAxis = super.getSlicer();
		if (slicerAxis == null) {
			slicerAxis = new XMLA_Axis(-1, "SlicerAxis");
		}
		return slicerAxis;
	}
} // End XMLA_Result
