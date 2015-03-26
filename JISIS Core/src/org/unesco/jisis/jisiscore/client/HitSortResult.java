/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisiscore.client;

import java.util.List;

/**
 *
 * @author jcd
 */
public class HitSortResult {

   private int hitSortNumber;
   private String dbName;
   private List<Long> mfns;
   private String hitSortName;

   @Override
   public String toString() {
      return "HitSort{" + "hitSortNumber=" + hitSortNumber + "dbName="+dbName
              +" mfns=" + mfns + "searchQuery=" + hitSortName + '}';
   }

   public HitSortResult(int hitSortNumber, String dbName, String hitSortName,List<Long> mfns) {
      this.hitSortNumber = hitSortNumber;
      this.dbName = dbName;
      this.hitSortName = hitSortName;

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
      this.mfns = mfns;
   }

   public int getHitSortNumber() {
      return hitSortNumber;
   }

   public void setHitSortNumber(int hitSortNumber) {
      this.hitSortNumber = hitSortNumber;
   }

   public String getHitSortName() {
      return hitSortName;
   }

   public void setHitSortName(String hitSortName) {
      this.hitSortName = hitSortName;
   }

}
