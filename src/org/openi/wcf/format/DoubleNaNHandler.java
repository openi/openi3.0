package org.openi.wcf.format;

import com.tonbeller.wcf.format.FormatException;

/**
 * Parses and formats double numbers, treating Double.NaN as empty string
 * and vice versa
 */
public class DoubleNaNHandler extends DoubleHandler {
  /*
   * if empty - return Double.NaN
   */
  public Object parse(String s, String userPattern) throws FormatException {
    if (s != null && s.length() == 0) {
      return new Double(Double.NaN);
    } else {
      return super.parse(s, userPattern);
    }
  }

  /*
   * if Double.NaN, return empty instead of \uFFFD
   */
  public String format(Object o, String userPattern) {
    if (o instanceof Double && Double.isNaN(((Double)o).doubleValue())) {
      return "";
    } else {
      return super.format(o, userPattern);
    }
  }
}
