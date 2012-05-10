package org.openi.chart;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
//import org.jfree.chart.labels.CategoryLabelGenerator;
import org.jfree.data.category.CategoryDataset;
import java.text.NumberFormat;


/**
 * @author plucas
 * @author SUJEN
 */
public class LabelGenerator implements CategoryItemLabelGenerator {//CategoryLabelGenerator {
    /**
     * The index of the category on which to base the percentage
     * (null = use series total).
     */
    private Integer category;

    /** A percent formatter. */
    private NumberFormat formatter = NumberFormat.getPercentInstance();

    /**
     * Creates a new label generator that displays the item value and a
     * percentage relative to the value in the same series for the
     * specified category.
     *
     * @param category  the category index (zero-based).
     */
    public LabelGenerator(final int category) {
        this(new Integer(category));
    }

    /**
     * Creates a new label generator that displays the item value and
     * a percentage relative to the value in the same series for the
     * specified category.  If the category index is <code>null</code>,
     * the total of all items in the series is used.
     *
     * @param category  the category index (<code>null</code> permitted).
     */
    public LabelGenerator(Integer category) {
        this.category = category;
    }

    /**
     * Generates a label for the specified item. The label is typically
     * a formatted version of the data value, but any text can be used.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     * @param series  the series index (zero-based).
     * @param category  the category index (zero-based).
     *
     * @return the label (possibly <code>null</code>).
     */
    public String generateLabel(CategoryDataset dataset, int series,
        int category) {
        String result = null;
        double base = 0.0;

        if (this.category != null) {
            final Number b = dataset.getValue(series, this.category.intValue());
            base = b.doubleValue();
        } else {
            base = calculateSeriesTotal(dataset, series);
        }

        Number value = dataset.getValue(series, category);

        if (value != null) {
            final double v = value.doubleValue();
            // you could apply some formatting here
            result = value.toString() + " (" + this.formatter.format(v / base)
                + ")";
        }

        return result;
    }

    /**
     * Calculates a series total.
     *
     * @param dataset  the dataset.
     * @param series  the series index.
     *
     * @return The total.
     */
    private double calculateSeriesTotal(CategoryDataset dataset, int series) {
        double result = 0.0;

        for (int i = 0; i < dataset.getColumnCount(); i++) {
            Number value = dataset.getValue(series, i);

            if (value != null) {
                result = result + value.doubleValue();
            }
        }

        return result;
    }

	public String generateColumnLabel(CategoryDataset arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String generateRowLabel(CategoryDataset arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}
