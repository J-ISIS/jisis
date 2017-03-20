/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.proxy;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jcd
 */
public class MarkedRecords {
   private int markedSetNumber;
   private String dbName;


   private ArrayList<Long> mfns;
   private String markedSetName;

   @Override
   public String toString() {
      return "MarkedRecords{" + "markedSetNumber=" + markedSetNumber + "dbName="+dbName
              +" mfns=" + mfns + "markedSetName=" + markedSetName + '}';
   }

   public MarkedRecords(int markedSetNumber, String dbName, String markedSetName,List<Long> mfns) {
      this.markedSetNumber = markedSetNumber;
      this.dbName = dbName;
      this.markedSetName = markedSetName;
      this.mfns = new ArrayList<Long>(mfns);

   }

    public String getDbName() {
      return dbName;
   }

   public void setDbName(String dbName) {
      this.dbName = dbName;
   }
   public List<Long> getMfns() {
      return mfns;
   }

   public void setMfns(List<Long> mfns) {
      this.mfns = new ArrayList<Long>(mfns);
   }

   public int getMarkedSetNumber() {
      return markedSetNumber;
   }

   public void setMarkedSetNumber(int markedSetNumber) {
      this.markedSetNumber = markedSetNumber;
   }

   public String getMarkedSetName() {
      return markedSetName;
   }

   public void setMarkedSetName(String markedSetName) {
      this.markedSetName = markedSetName;
   }
}
