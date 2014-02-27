package org.openi.wcf.format;

import java.text.DecimalFormat;
import java.text.ParsePosition;

import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.FormatHandlerSupport;


/**
 * parses/prints numbers via DecimalFormat
 */
public abstract class NumberHandler extends FormatHandlerSupport {
  double minValue = Double.NaN;
  
  public String format(Object o, String userPattern) {
    if (o == null) {
      return "";
    }

    DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance(getLocale());
    df.applyPattern(findPattern(userPattern));

    return df.format(o);
  }


  public Object parse(String s, String userPattern) throws FormatException {
    if (s == null) {
      throw new FormatException(getErrorMessage(""));
    }

    s = s.trim();

    if (s.length() == 0) {
      throw new FormatException(getErrorMessage(""));
    }

    DecimalFormat df = (DecimalFormat) DecimalFormat.getNumberInstance(getLocale());
    df.applyPattern(findPattern(userPattern));

    ParsePosition pos = new ParsePosition(0);
    Number n = (Number) df.parse(s, pos);

    if ((n == null) || (pos.getIndex() != s.length()))
      throw new FormatException(getErrorMessage(s));
    
    if (!Double.isNaN(minValue) && n.doubleValue() < minValue)
      throw new FormatException(getErrorMessage(s));

    return n;
  }

  /**
   * Returns the minValue.
   * @return double
   */
  public double getMinValue() {
    return minValue;
  }

  /**
   * Sets the minValue.
   * @param minValue The minValue to set
   */
  public void setMinValue(double minValue) {
    this.minValue = minValue;
  }

}