package org.openi.olap.xmla;

import com.tonbeller.jpivot.olap.model.Member;
import com.tonbeller.jpivot.olap.query.PositionBase;

/**
 * XMLA Position
 * @author SUJEN
 */
public class XMLA_Position extends PositionBase {

  int axisOrdinal;

  /**
   * 
   * @param axisOrdinal
   */
  protected XMLA_Position(int axisOrdinal) {
    super();
    this.axisOrdinal = axisOrdinal;
  }

  /**
    * Sets the members.
    * @param members The members to set
    */
  public void setMembers(Member[] members) {
    this.members = members;
  }

  /**
   * Sets the axisOrdinal.
   * @param axisOrdinal The axisOrdinal to set
   */
  public void setAxisOrdinal(int axisOrdinal) {
    this.axisOrdinal = axisOrdinal;
  }

  /**
   * Returns the axisOrdinal.
   * @return int
   */
  public int getAxisOrdinal() {
    return axisOrdinal;
  }

  /**
   * 
   * @param other
   * @return boolean
   */
  public boolean isEquivalent(XMLA_Position other) {
    // same positions, if members are equal
    Member[] othermembers = other.getMembers();
    int nMembers = members.length;
    if (othermembers.length != nMembers)
      return false;
    for (int i = 0; i < nMembers; i++) {
      if (!othermembers[i].getLabel().equals(members[i].getLabel()))
        return false;
    }
    return true;
  }

} // XMLA_Position
