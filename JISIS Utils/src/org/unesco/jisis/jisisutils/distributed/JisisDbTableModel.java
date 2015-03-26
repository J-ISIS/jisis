/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.distributed;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jc_dauphin
 */
public class JisisDbTableModel extends TableModelMap {
   // Used to retrieve table data
   private IDistributedTableDataSource tableDataSource_;
   // The cache of data that has been retrieved.
   private DistributedTableClientCache tableClientCache_;
   // Contains the descriptive elements of the table
   private DistributedTableDescription tableDescription_;

   private int[] columnToField_;

   /**
    * Constructor for CachingTableModel.
    * @param tableDataSource The object from which data should be retrieved.
    */
   public JisisDbTableModel(IDistributedTableDataSource tableDataSource) throws Exception {
      this(tableDataSource, 1000, 10000);    // will set the two ints to their defaults in the constructor
   }

   /**
    * Constructor for CachingTableModel.
    * @param tableDataSource The object from which data should be retrieved.
    * @param chunkSize The number of rows that should be retrieved from the
    *         DistributedTableDataSource at one time
    * @param maximumCacheSize The number of rows that the DistributedTableModel
    *         should hold before overwriting data that's not required.
    */
   public JisisDbTableModel(IDistributedTableDataSource tableDataSource,
                                int chunkSize, int maximumCacheSize)
           throws Exception {
      tableDataSource_  = tableDataSource;
      tableDescription_ = tableDataSource.getTableDescription();
      tableClientCache_ = new DistributedTableClientCache(chunkSize,
                              maximumCacheSize, tableDataSource);
      int n = tableDescription_.getColumnCount();
      columnToField_    = new int[n];
      for (int i=0; i<n; i++) {
         columnToField_[i] = i;
      }
   }


   public int getRowCount() {
      return tableDescription_.getRowCount();
   }


   public int getColumnCount() {
      return tableDescription_.getColumnCount();
   }


    @Override
   public String getColumnName(int columnIndex) {
      if (columnIndex < tableDescription_.getColumnCount()) {
         int fieldIndex = columnToField_[columnIndex];
         return tableDescription_.getColumnNames()[fieldIndex];
      } else {
         return null;
      }
   }


    @Override
   public Class getColumnClass(int columnIndex) {

      if (columnIndex < tableDescription_.getColumnCount()) {
         int fieldIndex = columnToField_[columnIndex];
         return tableDescription_.getColumnClasses()[fieldIndex];
      } else {
         return null;
      }
   }


   public Object getValueAt(int rowIndex, int columnIndex) {
//       System.out.println("DistTableModel::getValueAt - tableDescription_.getRowCount()="+tableDescription_.getRowCount()
//               +" rowIndex="+rowIndex+" columnIndex="+columnIndex);
//       if (rowIndex <0 || rowIndex >=tableDescription_.getRowCount())
//           return null;
      int fieldIndex = columnToField_[columnIndex];
      return tableClientCache_.retrieveRowFromCache(rowIndex)[fieldIndex];
   }


    @Override
   public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
   }

   @Override
   public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}


   @Override
   public void addTableModelListener(TableModelListener l) {}


   @Override
   public void removeTableModelListener(TableModelListener l) {}

   /**
    * Initiates a sort by calling <code>sort</code> on the
    * DistributedTableDataSource.
    * @param sortColumn The column to sort on.
    * @param ascending Whether the table should be sorted in an ascending
    *        or descending order.
    * @param selectedRows The row indexes that are currently seleted
    *        in the table.
    * @return An array of the indexes of the selected rows in the table
    *         after the sort.
    */
   public int[] sort(int sortColumn, boolean ascending, int[] selectedRows)
           throws Exception {
      tableClientCache_.sortOccurred();

      return tableDataSource_.sort(sortColumn, ascending, selectedRows);
   }

   public void clearCache() {
       long nRows = tableDataSource_.getRowCount();
       tableDescription_.setRowCount(nRows);
       tableClientCache_.clearCache();
   }

   /**
    * Sets the rows and columns that are selected by calling
    * <code>setSelectedRowsAndColumns</code> on the DistributedTableDataSource.
    * @param selectedRows An array of the selected row indexes.
    * @param selectedColumns An array of the selected column indexes.
    */
   public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns)
           throws Exception {
      tableDataSource_.setSelectedRowsAndColumns(selectedRows, selectedColumns);
   }

   /**
    * Returns an array corresponding to the row indexes that are currently
    * selected.
    */
   public int[] getSelectedRows() throws Exception {
      return tableDataSource_.getSelectedRows();
   }

   /**
    * Returns an array corresponding to the column indexes that are currently
    * selected.
    */
   public int[] getSelectedColumns() throws Exception {
      return tableDataSource_.getSelectedColumns();
   }
}

