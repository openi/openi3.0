package org.openi.chart;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.tonbeller.jpivot.olap.model.Cell;
import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.model.OlapException;
import com.tonbeller.jpivot.olap.model.OlapModel;
import com.tonbeller.jpivot.olap.model.Position;
import com.tonbeller.jpivot.olap.navi.MemberTree;


/**
 * Takes an olap model, adapts it to a jfreechart dataset
 * @author SUJEN
 * 
 */
public class DatasetAdapter {
    private static Logger logger = Logger.getLogger(DatasetAdapter.class);
    private Locale locale;
    private NumberFormat numberFormatter;

    /**
	 * @param locale
	 */
	public DatasetAdapter(Locale locale) {
		this.locale = locale;
	}

	/**
     * @param dataset
     * @return
     */
    public DefaultCategoryDataset buildCategoryDataset(OlapModel olapModel)
        throws OlapException {
        long start = System.currentTimeMillis();

        DefaultCategoryDataset dataset = null;
        int dimCount = olapModel.getResult().getAxes().length;

        switch (dimCount) {
        case 1:
            logger.info("1-dim data");
            dataset = build1dimDataset(olapModel);
            break;

        case 2:
            logger.info("2-dim data");
            dataset = build2dimDataset(olapModel);
            break;

        default:
            logger.error("less than 1 or more than 2 dimensions");
            throw new IllegalArgumentException("ChartRenderer requires a 1 or 2 dimensional result");
        }

        logger.debug("built datset in: " + (System.currentTimeMillis() - start) + "ms");

        return dataset;
    }

    /**
     * Build a jfreechart CategoryDataset with a single series
     * @param result TODO
     *
     */
    private DefaultCategoryDataset build1dimDataset(OlapModel olapModel) throws OlapException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // column axis
        List columnPositions = olapModel.getResult().getAxes()[0].getPositions();
        int colCount = columnPositions.size();

        // cells
        List cells = olapModel.getResult().getCells();
        String series = "Series";

        // loop on column positions
        for (int i = 0; i < colCount; i++) {
            Member[] colMembers = ((Position) columnPositions.get(i)).getMembers();
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
     * Build a jfreechart CategoryDataset with multiple series
     * @param olapModel
     * @param result TODO
     *
     */
    private DefaultCategoryDataset build2dimDataset(OlapModel olapModel)
        throws OlapException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // column axis
        List columnPositions = olapModel.getResult().getAxes()[0].getPositions(); //ladX.getPositions();
        int colCount = columnPositions.size();

        // row axis
        List rowPositions = olapModel.getResult().getAxes()[1].getPositions(); //ladY.getPositions();
        int rowCount = rowPositions.size();
        List cells = olapModel.getResult().getCells();

        // get the full member tree
        MemberTree myTree = ((MemberTree) olapModel.getExtension(MemberTree.ID));

        // for each column, starting with the bottom member, progress up the mmeber chain until the root is reached
        // keep track of the levels and hierarchies to avoid duplicates on level or hierarchys.
        //      *note: keeping track of the levels might be just extra work, I don't know if they CAN be repeated.
        //          if not, that logic can be easily removed (see buildName - above)
        // For each hierarchy, If a root member is reached (getRootDistance()=0), then only include it if there have been no other
        // lower level members already added:
        //       ie. All_dim1.dim1_lvl1.dim1_lvl2.All_dim2.dim2_lvl1 renders as dim1_lvl1.dim1_lvl2.dim2_lvl1
        //          whereas All_dim1.All_dim2 renders as the same.
        // The important part is that we include each parent on the way up, to ensure a unique name to
        // place in the map for the dataset (no longer overwriting each other)
        
        for (int i = 0; i < colCount; i++) {
            Position p = (Position) columnPositions.get(i);
            Member[] colMembers = p.getMembers();

            // build the label name for this column
            String label = buildName(myTree, colMembers);

            // For each row, use the same logic to build a unique key for each data item
            for (int k = 0; k < rowCount; k++) {
                Position rp = (Position) rowPositions.get(k);
                Member[] rowMembers = rp.getMembers();

                // build key name
                String key = buildName(myTree, rowMembers);
                Cell cell = (Cell) cells.get((k * colCount) + i);

                dataset.addValue(getNumberValue(cell), label.toString(), key.toString());
            }
        }
        return dataset;
    }

    /**
     * Get cell value as a Number. Parses the cell value string
     * using the locale set in this.locale.
     * @param cell
     * @return value as Number (can be null)
     */
    private Number getNumberValue(Cell cell) {
    	//05/10/2007: added to fix class cast exception 
    	Number value = null;
    	try {
    		value = (Number) cell.getValue();
    	} catch (ClassCastException e) {
    		try {
    			value = Double.parseDouble((String)cell.getValue());
    		} catch(NumberFormatException ex) {
    		}
    	}
    	
    	/*
        //added to fix data format bug in range axis
        if(numberFormatter == null){
            if ((cell.getFormattedValue() != null) && (cell.getFormattedValue() != "")) {
                String fmtValue = cell.getFormattedValue();
                fmtValue = fmtValue.trim();

                DecimalFormatSymbols dfs = new DecimalFormatSymbols(this.locale);
                if(fmtValue.endsWith(String.valueOf(dfs.getPercent()))){
                    //numberFormatter = NumberFormat.getPercentInstance(this.locale);
                	//change to fix bug related with the display of percentage value in chart axis
                	numberFormatter = new DecimalFormat("0.00%");
                } else if(fmtValue.indexOf(dfs.getCurrencySymbol()) >= 0){
                    numberFormatter = NumberFormat.getCurrencyInstance(this.locale);
                    numberFormatter.setMaximumFractionDigits(0);
                    numberFormatter.setMinimumFractionDigits(0);
                } else if (value instanceof Double) {
                    numberFormatter = NumberFormat.getNumberInstance(this.locale);
                } else {
                    numberFormatter = NumberFormat.getIntegerInstance(this.locale);
                }
            } else {
                if (value instanceof Double) {
                    numberFormatter = NumberFormat.getNumberInstance(this.locale);
                } else {
                    numberFormatter = NumberFormat.getIntegerInstance(this.locale);
                }
            }
        }
        */
        return value;
    }

    /**
     * Get a unique name string for a dataitem derived from the member chain
     *
     * @param myTree  (full member tree)
     * @param members - the list to be processed (either X/Y axis)
     * @return retValue as String
     */
    private String buildName(MemberTree myTree, Member[] members) {
        String retValue = new String();
        HashMap levelMap = new HashMap();
        HashMap hierarchyMap = new HashMap();

        for (int j = members.length - 1; j >= 0; j--) {
            Member member = members[j];

            while (member != null) {
                // only process if no other items from this level processed - should not be duplicates!
                if (!levelMap.containsValue(member.getLevel())) {levelMap.put(member.getLevel().toString(), member.getLevel());

                    if (member.getRootDistance() == 0) {
                        // if root member, only add to name if no other members of the hierarchy are already added
                        if (!hierarchyMap.containsValue(
                                    member.getLevel().getHierarchy()) ||
                                (myTree.getRootMembers(
                                    member.getLevel().getHierarchy()).length > 1)) {
                            hierarchyMap.put(member.getLevel().getHierarchy().toString(),member.getLevel().getHierarchy());
                            retValue = member.getLabel() + "." + retValue;
                        }
                    } else {
                        hierarchyMap.put(member.getLevel().getHierarchy().toString(),member.getLevel().getHierarchy());
                        retValue = member.getLabel() + "." + retValue;
                    }
                }
                member = myTree.getParent(member);
            }
        }
        return retValue;
    }

    /**
     * Very experimental, notice that the time dimension needs to be on the rows,
     * and there can be no other dimensions on rows.
     *
     * Each dimension on the column will have it's own series, added to the TimeSeriesCollection
     */
    public XYDataset buildXYDataset(OlapModel olapModel)
        throws OlapException {
        long start = System.currentTimeMillis();
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        
        // column axis
        List columnPositions = olapModel.getResult().getAxes()[0].getPositions(); //ladX.getPositions();
        int colCount = columnPositions.size();

        // row axis
        List rowPositions = olapModel.getResult().getAxes()[1].getPositions(); //ladY.getPositions();
        int rowCount = rowPositions.size();
        List cells = olapModel.getResult().getCells();

        // get the full member tree
        MemberTree myTree = ((MemberTree) olapModel.getExtension(MemberTree.ID));

        // each column gets its own time series
        for (int i = 0; i < colCount; i++) {
            Position p = (Position) columnPositions.get(i);
            Member[] colMembers = p.getMembers();

            // build the label name for this column
            String label = buildName(myTree, colMembers);
            TimeSeries series = createTimeSeries(label,
                    parseRootLevel(rowPositions));

            for (int k = 0; k < rowCount; k++) {
                Position rp = (Position) rowPositions.get(k);
                Member[] rowMembers = rp.getMembers();
                Cell cell = (Cell) cells.get((k * colCount) + i);

                try {
                    // should determine if this is a month, or day, year
                    // similar to buildName, need a buildMonth and/or buildDate, 
                    // allowing date hierarchies. 
                    RegularTimePeriod current = null;

                    if (series.getTimePeriodClass() == Quarter.class) {
                        current = createQuarter(myTree, rowMembers);
                    } else if (series.getTimePeriodClass() == Month.class) {
                        current = createMonth(myTree, rowMembers);
                    } else if (series.getTimePeriodClass() == Day.class) {
                        current = createDay(myTree, rowMembers);
                    }
                    series.add(current, getNumberValue(cell));
                } catch (ParseException e) {
                    // logger.error(e);
                }
            }
            dataset.addSeries(series);
        }

        dataset.setDomainIsPointsInTime(true);
        logger.debug("created XY Dataset in: " + (System.currentTimeMillis() - start) + " ms");
        return dataset;
    }

    private TimeSeries createTimeSeries(String label, String rootLevel) {
        logger.debug("creating timeseries for: " + rootLevel);
        TimeSeries series = null;

        if ("quarter".equalsIgnoreCase(rootLevel)) {
            series = new TimeSeries(label, Quarter.class);
        } else if ("month".equalsIgnoreCase(rootLevel)) {
            series = new TimeSeries(label, Month.class);
        } else if ("day".equalsIgnoreCase(rootLevel)) {
            series = new TimeSeries(label, Day.class);
        }
        return series;
    }

    private RegularTimePeriod createQuarter(MemberTree memberTree,
        Member[] members) {
        String year = memberTree.getParent(members[0]).getLabel();
        int quarter = 1;
        String memberLabel = members[0].getLabel();

        // month = parseMonth(memberLabel);
        if ("Q1".equalsIgnoreCase(memberLabel)) {
            quarter = 1;
        } else if ("Q2".equalsIgnoreCase(memberLabel)) {
            quarter = 2;
        } else if ("Q3".equalsIgnoreCase(memberLabel)) {
            quarter = 3;
        } else if ("Q4".equalsIgnoreCase(memberLabel)) {
            quarter = 4;
        }

        return new Quarter(quarter, Integer.parseInt(year));
    }

    /**
    * @return month object - a org.jfree.data.time.Month (RegularTimePeriod)
    */
    private Month createMonth(MemberTree memberTree, Member[] members)
        throws ParseException {
        
        int month = 1;
        String memberLabel = members[0].getLabel();
        month = parseMonth(memberLabel);
        
        int year = 1900;
        Member parent =  memberTree.getParent(members[0]);
        String yearLabel = parent.getLabel();
        try {
	        if (Integer.parseInt(yearLabel) <= 4) {
	        	 parent =  memberTree.getParent(parent);
	        	 if (parent != null)
	        		 yearLabel = parent.getLabel();
	        }
        } catch (NumberFormatException e) {}
        year = parseYear(yearLabel);        
        return new Month(month, year);
    }
    

    private RegularTimePeriod createDay(MemberTree memberTree, Member[] members) {
        int day = 1;
        String dayLabel = members[0].getLabel();
        day = Integer.parseInt(dayLabel);
        
        int month = 1;
        Member parent =  memberTree.getParent(members[0]);
        String monthLabel = parent.getLabel();
        month = parseMonth(monthLabel);
        
        int year = 1900;
        parent =  memberTree.getParent(parent);
        String yearLabel = parent.getLabel();
        try {
	        if (Integer.parseInt(yearLabel) <= 4) {
	        	 parent =  memberTree.getParent(parent);
	        	 if (parent != null)
	        		 yearLabel = parent.getLabel();
	        }
        } catch (NumberFormatException e) {}
        year = parseYear(yearLabel);
        return new Day(day, month, year);
    }

    private int parseYear(String memberLabel) {
    	 Integer year = null;

         //try to parse assuming int:
         try {
        	 year = new Integer(memberLabel);
         } catch (NumberFormatException e) {
             // logger.debug("could not parse as int");
         }

         // try string?
         if (year == null) {
             try {
                 SimpleDateFormat formatter = new SimpleDateFormat("YYYY");
                 Date date = formatter.parse(memberLabel);
                 year = new Integer(date.getYear());
             } catch (ParseException e) {
                 // logger.debug(e);
             }
         }

         return year.intValue();
	}

	private int parseMonth(String memberLabel) {
        Integer month = null;

        //try to parse assuming int:
        try {
            month = new Integer(memberLabel);
        } catch (NumberFormatException e) {
            // logger.debug("could not parse as int");
        }
        // try string?
        if (month == null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("MMM");
                Date date = formatter.parse(memberLabel);
                month = new Integer(date.getMonth() + 1);
            } catch (ParseException e) {
                // logger.debug(e);
            }
        }
        return month.intValue();
    }

    private String parseRootLevel(List rowPositions) {
        String rootLevel = null;

        try {
            rootLevel = ((Position) rowPositions.get(0)).getMembers()[0].getLevel().getLabel();
        } catch (Exception e) {
            logger.debug(e);
        }
        return rootLevel;
    }
    
	public NumberFormat getNumberFormatter() {
		return numberFormatter;
	}
}
