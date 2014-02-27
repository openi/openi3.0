package org.openi.wcf.format;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import com.tonbeller.wcf.format.FormatException;
import com.tonbeller.wcf.format.FormatHandlerSupport;
import com.tonbeller.wcf.utils.SoftException;


/**
 * validates string input via regular expression
 */
public class RegexHandler extends FormatHandlerSupport {
	private static Logger logger = Logger.getLogger(RegexHandler.class);
  /**
   * returns the unchanged string value of o.
   */
  public String format(Object o, String userPattern) {
    return String.valueOf(o);
  }

  /**
   * throws a FormatException if s does not match the regex
   * @throws FormatException
   */
  public Object parse(String s, String userPattern) throws FormatException {
    try {
      String regex = super.findPattern(userPattern);
      RE re = new RE(regex);
      if (!re.match(s)) {
        throw new FormatException(getErrorMessage(s));
      }
      return s;
    } catch (RESyntaxException e) {
			logger.error("exception caught", e);
      throw new SoftException(e);
    }
  }

  public boolean canHandle(Object value) {
    return false;
  }

  public Object toNativeArray(List list) {
    String[] array = new String[list.size()];
    for (int i = 0; i < array.length; i++)
      array[i] = (String)list.get(i);
    return array;
  }


  public Object[] toObjectArray(Object value) {
    if (value instanceof String)
      return new String[] {(String) value };
    return (String[]) value;
  }
  
}