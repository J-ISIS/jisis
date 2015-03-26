/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

//~--- JDK imports ------------------------------------------------------------

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jc_dauphin
 */

/**
 * Model for creating a 1-column Table supposed to contain the row numbers
 */
class RowHeaderModel extends AbstractTableModel {
   JTable table_;

   protected RowHeaderModel(JTable tableToMirror) {
      table_ = tableToMirror;
   }

   public int getRowCount() {
      return table_.getModel().getRowCount();
   }

   public int getColumnCount() {
      return 1;
   }

   public Object getValueAt(int row, int column) {
      return String.valueOf(row + 1);
   }
}
