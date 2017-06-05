/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.proxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jcd
 */
public class MarkedRecords implements Serializable {
   private int markedSetNumber_;
   private String dbName_;


   private ArrayList<Long> mfns_;
   private String markedSetName_;

   @Override
   public String toString() {
      return "MarkedRecords{" + "markedSetNumber=" + markedSetNumber_ + "dbName="+dbName_
              +" mfns=" + mfns_ + "markedSetName=" + markedSetName_ + '}';
   }

   public MarkedRecords(int markedSetNumber, String dbName, String markedSetName,List<Long> mfns) {
      this.markedSetNumber_ = markedSetNumber;
      this.dbName_ = dbName;
      this.markedSetName_ = markedSetName;
      this.mfns_ = new ArrayList<Long>(mfns);

   }

    public String getDbName() {
      return dbName_;
   }

   public void setDbName(String dbName) {
      this.dbName_ = dbName;
   }
   public List<Long> getMfns() {
      return mfns_;
   }

   public void setMfns(List<Long> mfns) {
      this.mfns_ = new ArrayList<Long>(mfns);
   }

   public int getMarkedSetNumber() {
      return markedSetNumber_;
   }

   public void setMarkedSetNumber(int markedSetNumber) {
      this.markedSetNumber_ = markedSetNumber;
   }

   public String getMarkedSetName() {
      return markedSetName_;
   }

   public void setMarkedSetName(String markedSetName) {
      this.markedSetName_ = markedSetName;
   }
}
