package org.openi.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author SUJEN
 *
 */
public class FileUtils {
	private static Logger logger = LogManager.getLogger(FileUtils.class);

	public static void extractZip(File zipFile, File extractInto)
			throws IOException {
		logger.debug("extracting zip '" + zipFile.getPath() + "' into '"
				+ extractInto.getPath() + "'..");

		FileInputStream fis = new FileInputStream(zipFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		extractZip(bis, extractInto);
	}

	public static void extractZip(InputStream is, File extractInto)
			throws IOException {
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry ze = null;

		while ((ze = zis.getNextEntry()) != null) {
			String entryName = ze.getName();
			logger.debug("extracting entry :" + entryName);

			if (ze.isDirectory())
				continue;

			File newFile = new File(entryName);
			String directory = extractInto.getPath() + "/"
					+ newFile.getParent();
			createDir(directory);

			if (directory == null) {
				if (newFile.isDirectory())
					break;
			}
			FileOutputStream os = new FileOutputStream(new File(directory,
					newFile.getName()));
			try {
				streamCopy(zis, os);
				zis.closeEntry();
			} catch (Exception e) {
				logger.error("could not read zip entry '" + newFile.getPath()
						+ "'", e);
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (Throwable t) {
					}
				}
			}
		}
		logger.debug("extracting completed");
	}

	public static void deleteDirContents(File dir) {
		if (dir != null && dir.exists()) {
			if (dir.isDirectory()) {
				File[] files = dir.listFiles();
				for (File file : files) {
					if (file.isDirectory())
						deleteDirContents(file);
					file.delete();
				}
			}
		}
	}

	public static void streamCopy(InputStream is, OutputStream os)
			throws IOException {

		BufferedInputStream in = new BufferedInputStream(is);
		BufferedOutputStream out = new BufferedOutputStream(os);

		synchronized (in) {
			synchronized (out) {
				byte[] buffer = new byte[2048];
				while (true) {
					int bytesRead = in.read(buffer);
					if (bytesRead == -1)
						break;
					out.write(buffer, 0, bytesRead);
				}
			}
		}
		out.flush();
	}

	private static boolean createDir(String directory) {
		return new File(directory).mkdirs();
	}

	public static String extractFileExt(String fileName) {
		if (fileName != null && !"".equals(fileName)) {

			int indx = fileName.lastIndexOf(".");
			if (indx != -1) {
				return fileName.substring(indx);
			}
		}
		return "";
	}

	public static String applyUnixFileSeperator(String path) {
		if (!isEmpty(path)) {
			return path.replaceAll("\\\\", "/");
		} else { 
			return path;
		}
	}

	private static boolean isEmpty(String path) {
		if (path == null  || "".equals(path)) {
			return true;
		} else {
			return false;
		}		
	}
	
	public static File createTempDir() {
		String tempDirName = System.getProperty("java.io.tmpdir");
		if (tempDirName == null) {
			throw new RuntimeException("Temporary directory system property "
					+ "(java.io.tmpdir) is null.");
		}

		// create the temporary directory if it doesn't exist
		File tempDir = new File(tempDirName);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		return tempDir;
	}
}
