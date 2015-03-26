/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.distributed;

/**
 *
 * @author jc_dauphin
 */
public class FilteredTableModel extends TableModelMap {

   /** Original TableModel used as data source. */
   protected RowFilter rowFilter_;

   /**
    * Creates FilteredTableModel object with specified TableModel as the data source.
    * @param model
    */
   public FilteredTableModel(javax.swing.table.TableModel model) {
      super.setModel(model);
   }

   /**
    * Returns RowFilter used to filter table rows.
    * @return
    */
   public RowFilter getRowFilter() {
      return rowFilter_;
   }

   /**
    * Sets RowFilter used to filter table rows. Invokes filter() before return
    * @param rowFilter
    */
   public void setRowFilter(RowFilter rowFilter) {
      rowFilter_ = rowFilter;
   }

   /**
    * Forces filtering of data contained in TableModelMapping.model.
    * The original TableModelMapping.model is not modified.
    */
   public void filter() {
   }

   /**
    * Returns row index in original TableModelMapping.model by index in
    * current model (view index).
    * @param rowIndex
    * @return
    */
   public int getRealRowIndex(int rowIndex) {
      return 0;
   }

   /**
    * Returns original TableModel used as data source.
    * @return
    */
   public javax.swing.table.TableModel getModel() {
      return super.getModel();
   }

   /**
    * Sets original TableModel used as data source.
    * @param model
    */
   public void setModel(javax.swing.table.TableModel model) {
      super.model = model;
   }
}
