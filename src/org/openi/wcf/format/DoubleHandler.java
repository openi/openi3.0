package org.openi.wcf.format;

import java.util.List;

import com.tonbeller.wcf.format.FormatException;

/**
 * number parser, that creates Double objects
 */
public class DoubleHandler extends NumberHandler {

  public Object parse(String s, String userPattern) throws FormatException {
    Number n = (Number) super.parse(s, userPattern);
    return new Double(n.doubleValue());
  }

  public boolean canHandle(Object value) {
    return value instanceof Number;
  }

  public Object toNativeArray(List list) {
    double[] array = new double[list.size()];
    for (int i = 0; i < array.length; i++)
      array[i] = ((Number)list.get(i)).doubleValue();
    return array;
  }

  public Object[] toObjectArray(Object value) {
  	if (value instanceof Double)
  	  return new Double[]{(Double)value};
  	double[] src = (double[])value;
    Double[] dst = new Double[src.length];
    for (int i = 0; i < src.length; i++)
      dst[i] = new Double(src[i]);
    return dst;
  }

}