package org.unesco.jisis.jisisutils.distributed;

// PagingModel.java
// A larger table model that performs "paging" of its data.  This model
// reports a small number of rows (like 100 or so) as a "page" of data.  You
// can switch pages to view all of the rows as needed using the pageDown()
// and pageUp() methods.  Presumably, access to the other pages of data is
// dictated by other GUI elements such as up/down buttons, or maybe a text
// field that allows you to enter the page number you want to display.
//
import javax.swing.table.*;
import javax.swing.*;
import java.awt.event.*;

public class PagingModel extends AbstractTableModel {

     // Used to retrieve table data
   private IDistributedTableDataSource tableDataSource_;
   // The cache of data that has been retrieved.
   private DistributedTableClientCache tableClientCache_;
   // Contains the descriptive elements of the table
   private DistributedTableDescription tableDescription_;
   

    private int[] columnToField_;
    protected int pageSize_;
    protected int pageOffset_;
  
    int tableRowCount_;
    int realRowCount_;
  
    public PagingModel(IDistributedTableDataSource tableDataSource) throws Exception {
        this(tableDataSource, 10000, 20000);    // will set the two ints to their defaults in the constructor
    }


    /**
     *
     * @param tableDataSource
     * @param chunkSize
     * @param maximumCacheSize
     * @throws java.lang.Exception
     */
    public PagingModel(IDistributedTableDataSource tableDataSource,
            int chunkSize, int maximumCacheSize) throws Exception {

        tableDataSource_ = tableDataSource;
        tableDescription_ = tableDataSource.getTableDescription();
        tableClientCache_ = new DistributedTableClientCache(chunkSize,
                maximumCacheSize, tableDataSource);
         pageSize_ = chunkSize;
        /**
         * Total number of logical rows
         */
        realRowCount_ = tableDescription_.getRowCount();
        // JTable row count on screen 
        tableRowCount_ = Math.min(pageSize_, realRowCount_);

       

        int n = tableDescription_.getColumnCount();
        columnToField_ = new int[n];
        for (int i = 0; i < n; i++) {
            columnToField_[i] = i;
        }

    }

    // Return values appropriate for the visible table part.
    @Override
    public int getRowCount() {
        return tableRowCount_;
    }

    @Override
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
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    // Work only on the visible part of the table.

    /**
     *
     * @param rowIndex
     * @param columnIndex
     * @return
     */
   @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int realRowIndex = rowIndex + (pageOffset_ * pageSize_);

        if (realRowIndex < 0 || realRowIndex >= realRowCount_) {
            return null;
        }
        int fieldIndex = columnToField_[columnIndex];
        return tableClientCache_.retrieveRowFromCache(realRowIndex)[fieldIndex];
    }

    // Use this method to figure out which page you are on.
    public int getPageOffset() {
        return pageOffset_;
    }
    
    public void setPageOffset(int pageOffset) {
        pageOffset_ = pageOffset;
         fireTableDataChanged();
    }

    public int getPageCount() {
        return (int) Math.ceil((double) realRowCount_ / pageSize_);
    }

    // Use this method if you want to know how big the real table is . . . we
    // could also write "getRealValueAt()" if needed.
    public int getRealRowCount() {
        return realRowCount_;
    }

    public int getPageSize() {
        return pageSize_;
    }

    public void setPageSize(int pageSize) {
        if (pageSize == pageSize_) {
            return;
        }
        int oldPageSize = pageSize_;
        pageSize_ = pageSize;
        pageOffset_ = (oldPageSize * pageOffset_) / pageSize_;
        fireTableDataChanged();
        /*
    if (pageSize < oldPageSize) {
      fireTableRowsDeleted(pageSize, oldPageSize - 1);
    }
    else {
      fireTableRowsInserted(oldPageSize, pageSize - 1);
    }
         */
    }

  // Update the page offset and fire a data changed (all rows).
  public void pageDown() {
    if (pageOffset_ < getPageCount() - 1) {
      pageOffset_++;
      fireTableDataChanged();
    }
  }

  // Update the page offset and fire a data changed (all rows).
  public void pageUp() {
    if (pageOffset_ > 0) {
      pageOffset_--;
      fireTableDataChanged();
    }
  }
  
   public void clearCache() {
       //long nRows = tableDataSource_.getRowCount();
       tableDescription_.setRowCount(realRowCount_);
       tableClientCache_.clearCache();
   }
   
    /**
    * Sets the rows and columns that are selected by calling 
    * <code>setSelectedRowsAndColumns</code> on the DistributedTableDataSource.
    * @param selectedRows An array of the selected row indexes.
    * @param selectedColumns An array of the selected column indexes.
     * @throws java.lang.Exception
    */
   public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns)
           throws Exception {
      tableDataSource_.setSelectedRowsAndColumns(selectedRows, selectedColumns);
   }

   /**
    * Returns an array corresponding to the row indexes that are currently
    * selected.
     * @return 
     * @throws java.lang.Exception
    */
   public int[] getSelectedRows() throws Exception {
      return tableDataSource_.getSelectedRows();
   }

   /**
    * Returns an array corresponding to the column indexes that are currently
    * selected.
     * @return 
     * @throws java.lang.Exception
    */
   public int[] getSelectedColumns() throws Exception {
      return tableDataSource_.getSelectedColumns();
   }

  

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
     * @throws java.lang.Exception
    */
   public int[] sort(int sortColumn, boolean ascending, int[] selectedRows) 
           throws Exception {
      tableClientCache_.sortOccurred();

      return tableDataSource_.sort(sortColumn, ascending, selectedRows);
   }

  // We provide our own version of a scrollpane that includes
  // the page up and page down buttons by default.

    /**
     *
     * @param jt
     * @param sp
     * @return
     */
  public static JScrollPane createPagingScrollPaneForTable(JTable jt, JScrollPane sp) {
   
    TableModel tmodel = jt.getModel();

    // Don't choke if this is called on a regular table . . .
    if (! (tmodel instanceof PagingModel)) {
      return sp;
    }

    // Okay, go ahead and build the real scrollpane
    final PagingModel model = (PagingModel)tmodel;
    final JButton upButton = new JButton(new ArrowIcon(ArrowIcon.UP));
    upButton.setEnabled(false);  // starts off at 0, so can't go up
    final JButton downButton = new JButton(new ArrowIcon(ArrowIcon.DOWN));
    if (model.getPageCount() <= 1) {
      downButton.setEnabled(false);  // One page...can't scroll down
    }

    upButton.addActionListener((ActionEvent ae) -> {
        model.pageUp();

        // If we hit the top of the data, disable the up button.
        if (model.getPageOffset() == 0) {
            upButton.setEnabled(false);
        }
        downButton.setEnabled(true);
    });

    downButton.addActionListener((ActionEvent ae) -> {
        model.pageDown();

        // If we hit the bottom of the data, disable the down button.
        if (model.getPageOffset() == (model.getPageCount() - 1)) {
            downButton.setEnabled(false);
        }
        upButton.setEnabled(true);
    });

    // Turn on the scrollbars; otherwise we won't get our corners.
    sp.setVerticalScrollBarPolicy
        (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    sp.setHorizontalScrollBarPolicy
        (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

    // Add in the corners (page up/down).
    sp.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, upButton);
    sp.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, downButton);

    return sp;
  }
}
