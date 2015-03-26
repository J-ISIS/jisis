/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.distributed;

/**
 *
 * @author jc_dauphin
 */
public class HitDrivenTableModel extends TableModelMap {

   int       indexes[];
protected RowFilter rowFilter_;

   /**
    * Creates FilteredTableModel object with specified TableModel as the data source.
    * @param model
    */
   public HitDrivenTableModel(javax.swing.table.TableModel model) {
      super.setModel(model);
   }

   public void checkModel() {
    if (indexes.length != model.getRowCount()) {
      System.err.println("Sorter not informed of a change in model.");
    }
  }

   // The mapping only affects the contents of the data rows.
  // Pass all requests to these rows through the mapping array: "indexes".

  public Object getValueAt(int aRow, int aColumn) {
    checkModel();
    return model.getValueAt(indexes[aRow], aColumn);
  }

  public void setValueAt(Object aValue, int aRow, int aColumn) {
    checkModel();
    model.setValueAt(aValue, indexes[aRow], aColumn);
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
}
