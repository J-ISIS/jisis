/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.distributed;

/**
 * A filter function used for filtering table rows.
 * @author jc_dauphin
 */
public interface RowFilter {

   /**
    * Filters table rows. Returns true if the row should be excluded, false otherwise.
    * @param row- row to check
    * @param tableModel- table's model
    * @return
    */
   public boolean exclude(int row, javax.swing.table.TableModel tableModel);
}
