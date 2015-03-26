/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryex;

import java.util.Hashtable;
import javax.swing.table.TableCellEditor;
import org.netbeans.swing.outline.Outline;

/**
 *
 * @author jcd
 */
public class RowSubfieldEditorModel {

   private Outline outline_;
   private Hashtable data;

   public RowSubfieldEditorModel(Outline outline) {
      outline_ = outline;
      data = new Hashtable();
   }

   public void addEditorForRow(int row, TableCellEditor e) {
      data.put(new Integer(row), e);
   }

   public void removeEditorForRow(int row) {
      data.remove(new Integer(row));
   }

   public TableCellEditor getEditor(int row) {
      return (TableCellEditor) data.get(new Integer(row));
   }
}
