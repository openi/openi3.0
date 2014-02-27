package org.openi.wcf.format;

import java.util.List;

import com.tonbeller.wcf.format.FormatHandlerSupport;

/**
 * @author av
 */
public class StringHandler extends FormatHandlerSupport {

  public String format(Object o, String userPattern) {
    // dont show "null"
    if (o == null) 
      return "";
    return String.valueOf(o);
  }


  public Object parse(String s, String userPattern) {
    // microsoft sends \r\n sometimes?!
    if (s.indexOf(13) >= 0) {
      StringBuffer sb = new StringBuffer();
      char[] ca = s.toCharArray();
      for (int i = 0; i < ca.length; i++) {
        if (ca[i] != 13)
          sb.append(ca[i]);
      }
      s = sb.toString();
    }
    return s;
  }

  public boolean canHandle(Object value) {
    return value instanceof String;
  }

  public Object toNativeArray(List list) {
    String[] array = new String[list.size()];
    for (int i = 0; i < array.length; i++)
      array[i] = (String) list.get(i);
    return array;
  }
  
  public Object[] toObjectArray(Object value) {
    if (value instanceof String)
      return new String[] {(String) value };
    return (String[]) value;
  }

}