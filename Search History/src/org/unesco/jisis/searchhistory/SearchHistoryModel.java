/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.searchhistory;

import java.util.List;
import javax.swing.AbstractListModel;
import org.unesco.jisis.jisiscore.client.SearchResult;

/**
 *
 * @author jcd
 */
public class SearchHistoryModel  extends AbstractListModel {
   private List<SearchResult> searchResults_;
   public SearchHistoryModel( List<SearchResult> searchResults) {
      searchResults_ = searchResults;
   }
   @Override
   public int getSize() {
      return searchResults_.size();
   }

   @Override
   public Object getElementAt(int index) {
      SearchResult searchResult = searchResults_.get(index);
      int searchNumber = searchResult.getSearchNumber();
      String dbName = searchResult.getDbName();
      int numberOfHits = searchResult.getMfns().size();
      String query = searchResult.getSearchQuery();
      SearchHistoryRow row =
         new SearchHistoryRow(searchNumber, dbName, numberOfHits, query);

      return row;
   }
   private class SearchHistoryRow {
        private final int searchNumber;
        private final String dbName;
        private final int numberOfHits;
        private final String query;

        public SearchHistoryRow (int searchNumber, String dbName, int numberOfHits, String query) {
            this.searchNumber = searchNumber;
            this.dbName = dbName;
            this.numberOfHits = numberOfHits;
            this.query = query;
        }



      @Override
        public String toString() {
         // The return string will be displayed on JList row
            return "#"+searchNumber+" ("+dbName+") "+"hits="+numberOfHits+
                    " query="+query;
        }
    }

}
