package org.openi.olap.xmla;

import org.apache.log4j.Logger;

import com.tonbeller.jpivot.olap.mdxparse.Exp;
import com.tonbeller.jpivot.olap.mdxparse.QueryAxis;

/**
 * Quax implementation for XMLA
 * @author SUJEN
 */
public class XMLA_Quax extends com.tonbeller.jpivot.olap.query.Quax {

  private XMLA_Model model;
  private Exp originalSet;

  static Logger logger = Logger.getLogger(XMLA_Quax.class);

  /**
   * c'tor
   * @param monQuax
   */
  XMLA_Quax(int ordinal, QueryAxis queryAxis, XMLA_Model model) {
    super(ordinal);

    this.model = model;
    originalSet = queryAxis.getExp();
 
    super.setUti(new XMLA_QuaxUti());
  }


  /**
   * @return the original set
   */
  public Exp getOriginalSet() {
    return originalSet;
  }

 
} // End XMLA_Quax
