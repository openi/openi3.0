package org.openi.olap.drillthrough;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import com.tonbeller.wcf.table.TableModel;
import com.tonbeller.wcf.table.TableRow;

/**
 * Helper class to do drillthrough, currently supports functionality to generate
 * CSV report of the drillthrough table model, so the general idea is to first
 * load the table model into DT table into session by doing olap drillthrough on
 * clicked cell, and then generate CSV report off this table model
 * 
 * @author SUJEN
 * 
 */
public class DrillthroughHelper {

	private static Logger logger = Logger.getLogger(DrillthroughHelper.class);

	/**
	 * DT Table Model to CSV output stream
	 * 
	 * @param model
	 * @param out
	 * @throws IOException
	 */
	public static void drillthroughTableModelToCSV(
			TableModel dtm, OutputStream out)
			throws IOException {
		
		if(dtm == null)
			return;
		
		final OutputStream writer = out;

		if (logger.isInfoEnabled())
			logger.info("Writing DT table headers to CSV");

		writeTableHeaders(dtm, writer);

		if (logger.isInfoEnabled())
			logger.info("Writing DT row data to CSV");

		writeRowData(dtm, writer);

		writer.close();
	}

	private static void writeTableHeaders(TableModel dtm,
			OutputStream writer) throws IOException {
		int colsCount = dtm.getColumnCount();
		for (int colIndex = 0; colIndex < colsCount; colIndex++) {
			String colTitle = dtm.getColumnTitle(colIndex);
			//if(colTitle.contains("'") || colTitle.contains(" ") || colTitle.contains(","))
			colTitle = colTitle.replaceAll("\"", "\"\"");
			colTitle = "\"" + colTitle + "\"";
			writer.write(colTitle.getBytes());
			if (colIndex == (colsCount - 1)) {
				writer.write("\n".getBytes());
			} else {
				writer.write(",".getBytes());
			}
		}
		writer.flush();
	}
	
	private static void writeRowData(TableModel dtm,
			OutputStream writer) throws IOException {
		int rowsCount = dtm.getRowCount();

		if (logger.isInfoEnabled())
			logger.info("Number of rows to write = " + rowsCount);

		int colsCount = dtm.getColumnCount();
		for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
			TableRow row = dtm.getRow(rowIndex);
			if(row == null)
				break;
			for (int colIndex = 0; colIndex < colsCount; colIndex++) {
				Object valueObj = row.getValue(colIndex);
				String value = "";
				if(valueObj != null) {
					value = row.getValue(colIndex).toString();
					//if(value.contains("'") || value.contains(" ") || value.contains(","))
					value = value.replaceAll("\"", "\"\"");
					value = "\"" + value + "\"";
				}
					
				writer.write(value.getBytes());
				if (colIndex == (colsCount - 1)) {
					writer.write("\n".getBytes());
				} else {
					writer.write(",".getBytes());
				}
			}
		}
		writer.flush();
	}
}
