package org.openi.analysis.export;

import java.io.File;

import org.openi.analysis.Analysis;

import com.tonbeller.wcf.controller.RequestContext;

/**
 * 
 * @author SUJEN
 *
 */
public interface AnalysisExporter {

	/**
	 * 
	 * @param doc
	 * @return
	 */
	public void export(Analysis analysis, String pivotID, File file, RequestContext context) throws Exception;
	
}
