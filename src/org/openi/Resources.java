package org.openi;

import java.util.ResourceBundle;

/**
 * messages bundle utility class
 * 
 * @author SUJEN
 *
 */
public class Resources {

	public static final String BUNDLE_STRING = "org.openi.resources";

    public static final ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_STRING);

    /**
     * @param key
     * @return value from the messages properties file
     */
    public static String getString(String key) {
        return resourceBundle.getString(key);
    }
    
}
