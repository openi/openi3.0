package org.openi.pentaho.plugin;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.SolutionFileMetaAdapter;
import org.pentaho.platform.engine.core.solution.FileInfo;

public class OpenIContentTypeMetaProvider extends SolutionFileMetaAdapter {

	private static final Log logger = LogFactory
			.getLog(OpenIContentTypeMetaProvider.class);

	public OpenIContentTypeMetaProvider() {
	};

	public IFileInfo getFileInfo(ISolutionFile solutionFile, InputStream in) {
		try {
			String filename = solutionFile.getFileName();
			filename = (filename != null && filename.endsWith(".openi")) ? filename
					.substring(0, filename.indexOf(".openi")) : filename;

			IFileInfo info = new FileInfo();
			info.setTitle(filename);
			return info;

		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
}
