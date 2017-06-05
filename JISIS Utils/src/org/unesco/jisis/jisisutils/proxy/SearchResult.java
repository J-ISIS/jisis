/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.proxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.unesco.jisis.corelib.util.Util;



/**
 *
 * @author jcd
 */
public class SearchResult implements Serializable {
   private int searchNumber_;
   private String dbName_;


   private ArrayList<Long> mfns_;
   private String searchQuery_;    // user query
   private String luceneQuery_;    // null for free text search

   @Override
   public String toString() {
      return "SearchResult{" + "searchNumber=" + searchNumber_ + "dbName="+dbName_
              +" mfns=" + mfns_ + "searchQuery=" + searchQuery_ + '}';
   }

   public SearchResult(int searchNumber, String dbName, String searchQuery, String luceneQuery, long[] mfns) {
      searchNumber_ = searchNumber;
      dbName_ = dbName;
      searchQuery_ = searchQuery;
      mfns_ = Util.convertArray(mfns);
      luceneQuery_ = luceneQuery;
     
   }

    public String getDbName() {
      return dbName_;
   }

   public void setDbName(String dbName) {
      dbName_ = dbName;
   }
   public List<Long> getMfns() {
      return mfns_;
   }

   public void setMfns(long[] mfns) {
      mfns_ = Util.convertArray(mfns);
   }

   public int getSearchNumber() {
      return searchNumber_;
   }

   public void setSearchNumber(int searchNumber) {
      searchNumber_ = searchNumber;
   }

   public String getSearchQuery() {
      return searchQuery_;
   }

   public void setSearchQuery(String searchQuery) {
      searchQuery_ = searchQuery;
   }

    public String getLuceneQuery() {
      return luceneQuery_;
   }

   public void setLuceneQuery(String luceneQuery) {
      luceneQuery_ = luceneQuery;
   }
}
