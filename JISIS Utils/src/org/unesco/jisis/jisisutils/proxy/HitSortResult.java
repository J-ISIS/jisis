/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.proxy;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author jcd
 */
public class HitSortResult implements Serializable {

   private int hitSortNumber_;
   private String dbName_;
   private List<Long> mfns_;
   private String hitSortName_;

   @Override
   public String toString() {
      return "HitSort{" + "hitSortNumber=" + hitSortNumber_ + "dbName="+dbName_
              +" mfns=" + mfns_ + "hitsortName=" + hitSortName_ + '}';
   }

   public HitSortResult(int hitSortNumber, String dbName, String hitSortName,List<Long> mfns) {
      this.hitSortNumber_ = hitSortNumber;
      this.dbName_ = dbName;
      this.hitSortName_ = hitSortName;

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
      this.mfns_ = mfns;
   }

   public int getHitSortNumber() {
      return hitSortNumber_;
   }

   public void setHitSortNumber(int hitSortNumber) {
      this.hitSortNumber_ = hitSortNumber;
   }

   public String getHitSortName() {
      return hitSortName_;
   }

   public void setHitSortName(String hitSortName) {
      this.hitSortName_ = hitSortName;
   }

}
