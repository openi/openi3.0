package org.openi.wcf.format;

import com.tonbeller.wcf.format.FormatException;

/**
 * non empty string
 * 
 * @author av
 */
public class RequiredStringHandler extends StringHandler {
  public Object parse(String object, String userPattern) {
    String s = (String) super.parse(object, userPattern);
    if (s == null || s.trim().length() == 0)
      throw new FormatException(getErrorMessage(""));
    return s;
  }
}
