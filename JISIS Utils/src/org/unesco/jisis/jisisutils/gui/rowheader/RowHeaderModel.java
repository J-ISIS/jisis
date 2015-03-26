/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui.rowheader;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jc_dauphin
 */
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


/**
 * Model for creating a 1-column Table supposed to contain the row numbers
 */
public class RowHeaderModel extends AbstractTableModel {
   JTable table_;

   protected RowHeaderModel(JTable tableToMirror) {
      table_ = tableToMirror;
   }

   public int getRowCount() {
      System.out.println("RowHeaderModel getRowCount()="+table_.getModel().getRowCount());
      return table_.getModel().getRowCount();
   }

   public int getColumnCount() {
      return 1;
   }

   public Object getValueAt(int row, int column) {
      System.out.println("RowHeaderModel row="+row);
      return String.valueOf(row + 1);
   }
}

