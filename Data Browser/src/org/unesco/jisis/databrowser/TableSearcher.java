/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.databrowser;

//~--- non-JDK imports --------------------------------------------------------

import org.unesco.jisis.gui.RecordTableDataSource;
import org.unesco.jisis.jisisutils.distributed.DistributedTableModel;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jc_dauphin
 */
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.unesco.jisis.corelib.common.IDatabase;

/**
 * This is a TableModel that encapsulates Lucene
 * search logic within a TableModel implementation.
 * It is implemented as a TableModel decorator,
 * similar to the TableSorter demo from Sun that decorates
 * a TableModel and provides sorting functionality. The benefit
 * of this architecture is that you can decorate any TableModel
 * implementation with this searching table model -- making it
 * easy to add searching functionaliy to existing JTables -- or
 * making new search capable table lucene.
 *
 * <p>This decorator works by holding a reference to a decorated ot inner
 * TableModel. All data is stored within that table model, not this
 * table model. Rather, this table model simply manages links to
 * data in the inner table model according to the search. All methods on
 * TableSearcher forward to the inner table model with subtle filtering
 * or alteration according to the search criteria.
 *
 * <p>Using the table model:
 *
 * Pass the TableModel you want to decorate in at the constructor. When
 * the TableModel initializes, it displays all search results. Call
 * the search method with any valid Lucene search String and the data
 * will be filtered by the search string. Users can always clear the search
 * at any time by searching with an empty string. Additionally, you can
 * add a button calling the clearSearch() method.
 *
 * @author Jonathan Simon - jonathan_s_simon@yahoo.com
 */
public class TableSearcher extends AbstractTableModel {
   /** The inner table model we are decorating */
   protected DistributedTableModel tableModel_;

   /**
    * This listener is used to register this class as a listener to
    * the decorated table model for update events
    */
   private TableModelListener    tableModelListener_;
   private RecordTableDataSource tableDataSource_;

   /**
    * these keeps reference to the decorated table model for data
    * only rows that match the search criteria are linked
    */
   private List<Integer> rowToModelIndex = new ArrayList<Integer>();

   /**
    * Links between this table model and the decorated table model
    * are maintained through links based on row number. This is a
    * key constant to denote "row number" for indexing
    */
   private static final String ROW_NUMBER = "ROW_NUMBER";

   /**
    * Cache the current search String. Also used internally to
    * key whether there is an active search running or not. i.e. if
    * searchString is null, there is no active search.
    */
   private String searchString = null;

   /**
    * @param tableModel The table model to decorate
    */
   public TableSearcher(RecordTableDataSource tableDataSource, DistributedTableModel tableModel) {
//    analyzer = new WhitespaceAnalyzer();
      tableDataSource_    = tableDataSource;
      tableModelListener_ = new TableModelHandler();

      setTableModel(tableModel);
      tableModel.addTableModelListener(tableModelListener_);
      clearSearchingState();
   }

   /**
    *
    * @return The inner table model this table model is decorating
    */
   public TableModel getTableModel() {
      return tableModel_;
   }

   /**
    * Set the table model used by this table model
    * @param tableModel The new table model to decorate
    */
   public void setTableModel(DistributedTableModel tableModel) {
      // remove listeners if there...
      if (this.tableModel_ != null) {
         this.tableModel_.removeTableModelListener(tableModelListener_);
      }

      this.tableModel_ = tableModel;

      if (this.tableModel_ != null) {
         this.tableModel_.addTableModelListener(tableModelListener_);
      }

      // recalculate the links between this table model and
      // the inner table model since the decorated model just changed
      reindex();
      // let all listeners know the table has changed
      fireTableStructureChanged();
   }

   /**
    * Reset the search results and links to the decorated (inner) table
    * model from this table model.
    */
   private void reindex() {
//    try {
//        // recreate the RAMDirectory
//        directory = new RAMDirectory();
//        IndexWriter writer = new IndexWriter(directory, analyzer, true);
//
//        // iterate through all rows
//        for (int row=0; row < tableModel.getRowCount(); row++){
//
//            //for each row make a new document
//            Document document = new Document();
//            //add the row number of this row in the decorated table model
//            //this will allow us to retrive the results later
//            //and map this table model's row to a row in the decorated
//            //table model
//            document.add(new Field(ROW_NUMBER, "" + row, Field.Store.YES, Field.Index.TOKENIZED));
//            //iterate through all columns
//            //index the value keyed by the column name
//            //NOTE: there could be a problem with using column names with spaces
//            for (int column=0; column < tableModel.getColumnCount(); column++){
//                String columnName = tableModel.getColumnName(column);
//                String columnValue = String.valueOf(tableModel.getValueAt(row, column)).toLowerCase();
//                document.add(new Field(columnName, columnValue, Field.Store.YES, Field.Index.TOKENIZED));
//            }
//            writer.addDocument(document);
//        }
//        writer.optimize();
//        writer.close();
//    } catch (Exception e){
//        e.printStackTrace();
//    }
   }

   /**
    * Run a new search.
    *
    * @param searchString Any valid lucene search string
    */
   public void search(String searchString) {
      // if search string is null or empty, clear the search == search all
      if ((searchString == null) || searchString.equals("")) {
         clearSearchingState();
         fireTableDataChanged();

         return;
      }

      // cache search String
      this.searchString = searchString;

//    //make a new index searcher with the in memory (RAM) index.
//    IndexSearcher is = new IndexSearcher(directory);
//
//    //make an array of fields - one for each column
//    String[] fields = new String[tableModel.getColumnCount()];
//    for (int t=0; t<tableModel.getColumnCount(); t++){
//        fields[t]=tableModel.getColumnName(t);
//    }
//
//    //build a query based on the fields, searchString and cached analyzer
//    //NOTE: This is an area for improvement since the MultiFieldQueryParser
//    // has some weirdness.
//    MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
//    Query query = parser.parse(searchString);
//    //run the search
//    Hits hits = is.search(query);
//    //reset this table model with the new results
//    resetSearchResults(hits);
//        } catch (Exception e){
//    e.printStackTrace();
//        }
      // JComponent[] fields = {termPanel, keywordBox, optionBox, queryField, plusButton, minusButton};
      try {
         long[]    records;
         IDatabase db = tableDataSource_.getRemoteDatabase();

         records = db.search(searchString);

         resetSearchResults(records);
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      // notify all listeners that the table has been changed
      fireTableStructureChanged();
   }

   /**
    *
    * @param hits The new result set to set this table to.
    */
   private void resetSearchResults(long[] recordsHits) {
//    try {
//        //clear our index mapping this table model rows to
//        //the decorated inner table model
//        rowToModelIndex.clear();
//        //iterate through the hits
//        //get the row number stored at the index
//        //that number is the row number of the decorated
//        //tabble model row that we are mapping to
//        for (int t=0; t<hits.length(); t++){
//            Document document = hits.doc(t);
//            Fieldable field = document.getField(ROW_NUMBER);
//            rowToModelIndex.add(new Integer(field.stringValue()));
//        }
//    } catch (Exception e){
//        e.printStackTrace();
//    }
   }

   private int getModelRow(int row) {
      return ((Integer) rowToModelIndex.get(row)).intValue();
   }

   /**
    * Clear the currently active search
    * Resets the complete dataset of the decorated
    * table model.
    */
   private void clearSearchingState() {
      searchString = null;

      rowToModelIndex.clear();

      for (int t = 0; t < tableModel_.getRowCount(); t++) {
         rowToModelIndex.add(new Integer(t));
      }
   }

   // TableModel interface methods
   public int getRowCount() {
      return (tableModel_ == null)
             ? 0
             : rowToModelIndex.size();
   }

   public int getColumnCount() {
      return (tableModel_ == null)
             ? 0
             : tableModel_.getColumnCount();
   }

   public String getColumnName(int column) {
      return tableModel_.getColumnName(column);
   }

   public Class getColumnClass(int column) {
      return tableModel_.getColumnClass(column);
   }

   public boolean isCellEditable(int row, int column) {
      return tableModel_.isCellEditable(getModelRow(row), column);
   }

   public Object getValueAt(int row, int column) {
      return tableModel_.getValueAt(getModelRow(row), column);
   }

   public void setValueAt(Object aValue, int row, int column) {
      tableModel_.setValueAt(aValue, getModelRow(row), column);
   }

   private boolean isSearching() {
      return searchString != null;
   }

   private class TableModelHandler implements TableModelListener {
      public void tableChanged(TableModelEvent e) {
         // If we're not searching, just pass the event along.
         if (!isSearching()) {
            clearSearchingState();
            reindex();
            fireTableChanged(e);

            return;
         }

         // Something has happened to the data that may have invalidated the search.
         reindex();
         search(searchString);
         fireTableDataChanged();

         return;
      }
   }
}
