package org.openi.wcf.format;

import java.util.List;

import com.tonbeller.wcf.format.FormatException;


/**
 * creates Integer objects
 * @author av
 */
public class IntegerHandler extends NumberHandler {

  public Object parse(String s, String userPattern) throws FormatException {
    Number n = (Number) super.parse(s, userPattern);
    if (n.intValue() < 0) {
    	setName("posint");
        throw new FormatException(getErrorMessage(s));
    }
    return new Integer(n.intValue());
  }

  public boolean canHandle(Object value) {
    return value instanceof Integer;
  }
  
  public Object toNativeArray(List list) {
    int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++)
      array[i] = ((Number)list.get(i)).intValue();
    return array;
  }
  
  public Object[] toObjectArray(Object value) {
  	if (value instanceof Integer)
  	  return new Integer[]{(Integer)value};
  	int[] src = (int[])value;
    Integer[] dst = new Integer[src.length];
    for (int i = 0; i < src.length; i++)
      dst[i] = new Integer(src[i]);
    return dst;
  }
  
}