/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dictionary;

/**
 *
 * @author jc_dauphin
 */

import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.index.DictionaryTerm;
import org.unesco.jisis.jisisutils.distributed.DistributedTableDescription;
import org.unesco.jisis.jisisutils.distributed.IDistributedTableDataSource;

/**
 *
 * @author jc_dauphin
 */
public class TermsTableDataSource implements IDistributedTableDataSource {

   private IDatabase db_;
   private DistributedTableDescription tableDescription_ = null;

   public TermsTableDataSource(IDatabase db) {
      db_ = db;
   }
   static String[] columnNames = {"iTerm", "Field", "Term", "Freq"};
   static Class[] columnClasses = {String.class, String.class, String.class, String.class};

   public DistributedTableDescription getTableDescription() throws Exception {

      long nRows = db_.getDictionaryTermsCount();

      System.out.println("DistributedTableDescription nRows=" + nRows);

      if (tableDescription_ == null) {
          
         tableDescription_ = new DistributedTableDescription(columnNames,
                 columnClasses, nRows);
      }

      return tableDescription_;
   }

   public Object[][] retrieveRows(int from, int to) throws Exception {
      List<DictionaryTerm> terms = db_.getDictionaryTermsChunck(from, to);
      int nRows = terms.size();

      System.out.println("retrieveRows nRows=" + nRows);

      Object[][] data = new Object[nRows][columnNames.length];
      int n = columnNames.length;

      System.out.println("retrieveRows nFields=" + n);

      for (int i = 0; i < nRows; i++) {
         DictionaryTerm term = terms.get(i);
         String s = null;
         for (int j = 0; j < n; j++) {
            switch (j) {
               case 0:
                  s = Integer.toString(from + i);
                  break;
               case 1:
                  s = term.getField();
                  break;
               case 2:
                  s = term.getText();
                  break;
               case 3:
                  s = Integer.toString(term.getFreq());
                  break;
            }

            data[i][j] = s;
         }
      }

      return data;
   }

   public Object[][] retrieveRows(int from, int to, ProgressHandle progress) throws Exception {
      List<DictionaryTerm> terms = db_.getDictionaryTermsChunck(from, to);
      int nRows = terms.size();

      System.out.println("retrieveRows nRows=" + nRows);

      Object[][] data = new Object[nRows][columnNames.length];
      int n = columnNames.length;

      System.out.println("retrieveRows nFields=" + n);

      for (int i = 0; i < nRows; i++) {
         DictionaryTerm term = terms.get(i);
         String s = null;
         for (int j = 0; j < n; j++) {
            switch (j) {
               case 0:
                  s = Integer.toString(from + i);
                  break;
               case 1:
                  s = term.getField();
                  break;
               case 2:
                  s = term.getText();
                  break;
               case 3:
                  s = Integer.toString(term.getFreq());
                  break;
            }

            data[i][j] = s;
         }

         progress.setDisplayName("Retrieving record" + i + "/" + nRows);
      }

      return data;
   }

   public int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns)
           throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int[] getSelectedRows() throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int[] getSelectedColumns() throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getRowCount() {

      long nRow = 0;
      try {
         nRow = db_.getDictionaryTermsCount();
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      return nRow;
   }

    @Override
    public boolean isZeroBased() {
        return true; // To get the last element
    }
}
