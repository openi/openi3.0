package org.openi.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 
 * @author SUJEN
 * 
 */
public class StringUtils {

	private static Logger logger = Logger.getLogger(StringUtils.class);
	
	public static String replaceMatchingParams(String str,
			Map<String, String> params) {
		Iterator<String> itr = params.keySet().iterator();
		while (itr.hasNext()) {
			String paramKey = (String) itr.next();
			String paramValue = (String) params.get(paramKey);
			str = str.replaceAll(paramKey, paramValue);
		}
		return str;
	}

	public static boolean isInStringArray(String str, String[] arrayList) {
		boolean inStringArray = false;
		if (arrayList != null) {
			for (int i = 0; i < arrayList.length; i++) {
				logger.info("str = " + str);
				logger.info("item = " + arrayList[i]);
				if (arrayList[i].equalsIgnoreCase(str)) {
					inStringArray = true;
					break;
				}
			}
		}
		return inStringArray;
	}

	public static void main(String args[]) {
		String testString = "<html><body>Analysis title: PARAM_analysistitle</body></html>";
		Map<String, String> params = new HashMap<String, String>();
		params.put("PARAM_analysistitle", "OpenI Analysis");
		System.out.println(replaceMatchingParams(testString, params));
	}

}
