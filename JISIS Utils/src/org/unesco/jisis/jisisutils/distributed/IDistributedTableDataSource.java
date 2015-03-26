
/**
 *
 */
package org.unesco.jisis.jisisutils.distributed;

//~--- non-JDK imports --------------------------------------------------------

import org.netbeans.api.progress.ProgressHandle;

/**
 * @author jc_dauphin
 *
 */

/**
 * Interface that isolates all of the local or remote method calls necessary to
 * retrieve data from a distributed table model in an efficient way.
 * @author jc Dauphin, 2005.
 */
public interface IDistributedTableDataSource {
  

    long getRowCount();
   /**
    * Returns an object contain descriptive data for the table, (column
    * names, class types, number of rows and columns). This method will
    * only ever be called once when a table is displayed.
    * @throws Exception If something goes wrong.
    */
   DistributedTableDescription getTableDescription() throws Exception;

   /**
    * Method used for data retrieval.
    * @param The row from which you want to retrieve data.
    * @param The row to which you want to retrieve data.
    * @return A two dimensional array [row][column] of data from the remote
    * table data store.
    * @throws Exception If something goes wrong.
    */
   //Object[][] retrieveRows(int from, int to) throws Exception;
   
    public Object[][] retrieveRows(int from, int to, ProgressHandle progress)throws Exception;

   /**
    * Initiates a sort by calling <code>sort</code> on the
    * DistributedTableDataSource.
    * @param sortColumn The column to sort on.
    * @param ascending Whether the table should be sorted in an ascending
    *        or descending order.
    * @param selectedRows The row indexes that are currently selected
    *        in the table.
    * @return An array of the indexes of the selected rows in the table
    *         after the sort.
    * @throws Exception If something goes wrong.
    */
   int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws Exception;

   /**
    * Sets the rows and columns that are selected by calling
    * <code>setSelectedRowsAndColumns</code> on the DistributedTableDataSource.
    * @param selectedRows An array of the selected row indexes.
    * @param selectedColumns An array of the selected column indexes.
    * @throws Exception If something goes wrong.
    */
   void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns) throws Exception;

   /**
    * Returns an array corresponding to the row indexes that are currently
    * selected.
    * @throws Exception If something goes wrong.
    */
   int[] getSelectedRows() throws Exception;

   /**
    * Returns an array corresponding to the column indexes that are currently
    * selected.
    * @throws Exception If something goes wrong.
    */
   int[] getSelectedColumns() throws Exception;
   
   boolean isZeroBased();
}
