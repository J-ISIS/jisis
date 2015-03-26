/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.distributed;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jc_dauphin
 */
public class JisisDbTableClientCache {
 // THE MAXIMUM SIZE OF THE CACHE
   private int maximumCacheSize_ = -1;
   // THE CACHE OF ROWS
   private Object[] data_ = null;
   // THE NUMBER OF ROWS THAT ARE RETRIEVED AT A TIME
   private int chunkSize_ = -1;
   // AN INDEX- AN INTS ARE STORED CORREPONDING TO A ROWS REAL INDEX IN THE TABLE. THE LOCATION OF THE INDEX IN THIS
   // ARRAY SHOWS WHICH LOCATION TO ACCESS IN THE data ARRAY
   private int[] rowIndexLookup_ = null;
   // STORES THE INDEX THAT THE NEXT WRITES TO THE TWO ARRAYS SHOULD TAKE PLACE IN.
   // WHEN IT REACHES
   // THE MAX CACHE SIZE IT GOES BACK TO ZERO
   private int writePositionIndex_ = 0;
   // THE INDEX IN THE TABLE TO FETCH DATA FROM, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
   private int toIndex_ = -1;
   // CONVENIENCE VARIABLE, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
   private int tableIndex_ = -1;
   // THE SOURCE OF DATA
   private IDistributedTableDataSource tableDataSource_ = null;
   // THE LAST ARRAY INDEX OF THE CACHE TO BE INDEXED
   private int lastRowAccess_ = 0;
   // THE LAST INDEX THAT WAS REQUIRED WHEN A FETCH OCCURRED. DETERMINES WHETHER THE USER IS ASCENDING
   // OR DESCENDING THE TABLE
   private int lastRequiredFetchRowIndex_ = 0;
   // CONVENIENCE
   // private int i = 0;
   // THE INDEX IN THE TABLE TO FETCH DATA TO, (STORED AS A CLASS VARIABLE FOR EFFICIENCY)
   private int                         fromIndex_ = -1;
   private DistributedTableDescription tableDescription_;
   private String                      Retrieving_data =
      "Retrieving Data From Server...";

   /**
    * Creates new DistributedTableClientCache
    * @param chunkSize The number of rows of data that are to be retrieved from
    * the remote store at a time.
    * @param maximumCacheSize The maximum number of rows that will be cached.
    * When this number is exceeded by new data that has been fetched,
    * the oldest data is overwritten.
    * @tableDataSource A source of table data,
    *                  (via the method <code>retrieveRows</code>).
    */
   public JisisDbTableClientCache(int chunkSize, int maximumCacheSize,
                                      IDistributedTableDataSource tableDataSource)
           throws Exception {
      tableDataSource_  = tableDataSource;
      tableDescription_ = tableDataSource.getTableDescription();

      // ENSURE CHUNK SIZE NOT TOO SMALL
      if (chunkSize < 50) {
         chunkSize = 50;
      }
      chunkSize_ = chunkSize;
      // ENSURE MAX CACHE SIZE NOT TOO SMALL
      if (maximumCacheSize < 300) {
         maximumCacheSize = 300;
      }
      maximumCacheSize_ = maximumCacheSize;
      // MAKE SURE THE CHUNK SIZE NOT BIGGER THAN THE MAX CACHE SIZE
      if (chunkSize > maximumCacheSize) {
         chunkSize = maximumCacheSize;
      }
      // INITIALIZE THE ARRAYS
      data_           = new Object[maximumCacheSize];
      rowIndexLookup_ = new int[maximumCacheSize];

      // SET ALL THE ROWS TO -1, (THEY INITIALIZE TO 0).
      for (int i = 0; i < rowIndexLookup_.length; i++) {
         rowIndexLookup_[i] = -1;
      }
   }
 /**
    * Retrieves a row from the data cache. If the row is not currently in
    * the cache it will be retrieved from the DistributedTableDataSource
    * object.
    * @param rowIndex The row index in the table that is to be retrieved.
    */
   public Object[] retrieveRowFromCache(int rowIndex) {
      ensureRowCached(rowIndex);
      int index = getIndexOfRowInCache(rowIndex);

      return (Object[]) data_[index];
   }

   /**
    * Called after a sort has been carried out to nullify the data
    * in the cache so that the newly sorted data must be fetched from
    * the server.
    */
   public void sortOccurred() {
     clearCache();
   }

   public void clearCache() {
      // SET ALL THE ROWS TO -1, (THEY INITIALIZE TO 0).
      for (int i = 0; i < data_.length; i++) {
         data_[i]           = null;
         rowIndexLookup_[i] = -1;
      }
      lastRowAccess_ = 0;
      lastRequiredFetchRowIndex_ = 0;
      writePositionIndex_ = 0;
   }

   private Object[][] rows = null;

   private void doRetrieveData() {
      Runnable retrieveRun = new Runnable() {
         public void run() {
            final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Retrieving_data);
            progressHandle.start();
            progressHandle.switchToIndeterminate();

            // RETRIEVE THE DATA
            try {
               rows = tableDataSource_.retrieveRows(fromIndex_, toIndex_, progressHandle);
            } catch (Exception ex) {
               ex.printStackTrace();

               throw new RuntimeException("Problem occurred retrieving table data \n");
            }
            progressHandle.finish();
         }
      };

      //ProgHandle.start(99);
      RequestProcessor.Task task = RequestProcessor.getDefault().post(retrieveRun);
      task.waitFinished();
      //task.cancel();

   }

   /**
    * Ensures that a row index in the table is cached and if not a chunk of
    * data is retrieved.
    */
   private void ensureRowCached(int rowIndex) {
       if (rowIndex < 0) {
             System.out.println("DistributedTableClientCache:Error!!! rowOndex <0 ="+rowIndex);
             throw new RuntimeException("DistributedTableClientCache:Error!!! rowOndex <0 ="+rowIndex);
         }
      if (!isRowCached(rowIndex)) {
         // HAVE TO FETCH DATA FROM THE REMOTE STORE
         // SET THE toIndex AND fromIndex VARIABLES
         // TEST IF THE USER IS DESCENDING THE TABLE
         if (rowIndex >= lastRequiredFetchRowIndex_) {
            fromIndex_ = rowIndex;
            toIndex_   = rowIndex + chunkSize_;

            try {
               int n = tableDescription_.getRowCount();
               if (toIndex_ >= tableDescription_.getRowCount()) {
                  toIndex_ = tableDescription_.getRowCount()-1;
               }
            } catch (Exception ex) {
               ex.printStackTrace();
            }
         } else {
            // USER IS ASCENDING THE TABLE
            fromIndex_ = rowIndex - chunkSize_;
            if (fromIndex_ < 0) {
               fromIndex_ = 0;
            }
            toIndex_ = rowIndex + 1;
         }
         if (toIndex_ < fromIndex_ ) {
             System.out.println("Error!!! fromIndex_="+fromIndex_+" toIndex_="+toIndex_);
             throw new RuntimeException("DistributedTableClientCache: Error!!! fromIndex_="+fromIndex_+" toIndex_="+toIndex_);
         }

         System.out.println("Retrieve data from Server from="+fromIndex_+" to="+toIndex_);
         doRetrieveData();

         // ADD THE DATA TO THE CACHE
         for (int i = 0; i < rows.length; i++) {
            // SET THE VALUE IN THE DATA ARRAY
            data_[writePositionIndex_] = rows[i];

            // CREATE AN INDEX TO THE NEW CACHED DATA
            tableIndex_                          = fromIndex_ + i;
            rowIndexLookup_[writePositionIndex_] = tableIndex_;

            // CLOCK UP writePositionIndex_ AND REZERO IF NECESSARY
            if (writePositionIndex_ == (maximumCacheSize_ - 1)) {
               writePositionIndex_ = 0;
            } else {
               writePositionIndex_++;
            }
         }
      }
      lastRequiredFetchRowIndex_ = rowIndex;
   }



   /**
    * Returns the array index of a particular row index in the table
    */
   private int getIndexOfRowInCache(int rowIndex) {

      for (int i = lastRowAccess_; i < rowIndexLookup_.length; i++) {
         if (rowIndexLookup_[i] == rowIndex) {
            lastRowAccess_ = i;

            return i;
         }
      }

      for (int i = 0; i < lastRowAccess_; i++) {
         if (rowIndexLookup_[i] == rowIndex) {

            lastRowAccess_ = i;
            return i;
         }
      }

      return -1;
   }

   /**
    * Returns whether a particular row index in the table is cached.
    */
   private boolean isRowCached(int rowIndexInTable) {
      return getIndexOfRowInCache(rowIndexInTable) >= 0;
   }
}
