package org.openi.analysis.export;

/**
 * Factory class to return an instance of AnalysisExporter based on the export format
 * 
 * @author SUJEN
 *
 */
public class ExporterFactory {

	public static final int PDF = 0;
	public static final int XLS = 1;

	/**
	 * 
	 * @param exportFormat
	 * @return AnalysisExporter instance
	 * 
	 */
	public static AnalysisExporter getExporter(int exportFormat) {
		switch (exportFormat) {
			case 0:
				return new PDFExporter();
			case 1:
				return new ExcelExporter();
		}
		return null;
	}
}
