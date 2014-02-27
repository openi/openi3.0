package org.openi.wcf.format;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.FormatHandlerSupport;


/**
 * @author av
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DateHandler extends FormatHandlerSupport {

  public String format(Object o, String userPattern) {
    if (o == null) {
      return "";
    }

    Locale locale = getLocale();
    SimpleDateFormat sdf = new SimpleDateFormat(findPattern(userPattern), locale);

    return sdf.format(o);
  }

  public Object parse(String s, String userPattern) throws FormatException {
    if (s == null) {
      return null;
    }

    s = s.trim();
    if (s.length() == 0) {
      return null;
    }

    SimpleDateFormat sdf = new SimpleDateFormat(findPattern(userPattern), getLocale());
    ParsePosition pos = new ParsePosition(0);
    Object o = sdf.parse(s, pos);

    if ((o == null) || (pos.getIndex() != s.length())) {
      throw new FormatException(getErrorMessage(s));
    }

    return o;
  }
  
  public boolean canHandle(Object value) {
    return value instanceof Date;
  }
 
 
  public Object toNativeArray(List list) {
    Date[] array = new Date[list.size()];
    for (int i = 0; i < array.length; i++)
      array[i] = (Date)list.get(i);
    return array;
  }
  
  public Object[] toObjectArray(Object value) {
  	if (value instanceof Date)
  	  return new Date[]{(Date)value};
  	return (Date[])value;
  }
  
}