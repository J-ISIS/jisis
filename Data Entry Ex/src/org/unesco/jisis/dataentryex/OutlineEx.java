/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.table.TableCellEditor;
import org.netbeans.swing.outline.Outline;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.gui.DataEntryDlgTableCellEditor;

/**
 *
 * @author jcd
 */
public class OutlineEx extends Outline {

    private Hashtable data;

    public OutlineEx() {
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

   @Override
   public boolean getScrollableTracksViewportHeight() {
      if (getParent() instanceof JViewport) {
         return getParent().getHeight() > getPreferredSize().height;
      } else {
         return false;
      }
   }

   @Override
   /**
    * Override to choose the blob celleditor
    */
   public TableCellEditor getCellEditor(int row, int col) {
      if (col != 4) {
         return super.getCellEditor(row, col);
      }
      DataEntryNode dataEntryNode = (DataEntryNode) getValueAt(row, 0);
      Hashtable data = (Hashtable) dataEntryNode.getUserObject();
      String nodeType = (String) data.get("type");
      // The selected node should be a data entry field node
      if (!("fieldNode".equals(nodeType))) {
         return super.getCellEditor(row, col);
      }
      String fieldType = (String) data.get("fieldType");
      if (fieldType.equals(Global.fiedType(Global.FIELD_TYPE_BLOB))) {
         return getDataEntryActionEditor(row);

      }
//             TableCellEditor tmpEditor = null;
//             if (rm != null) {
//                tmpEditor = rm.getEditor(row);
//             }
//             if (tmpEditor != null) {
//                return tmpEditor;
//             }
      return super.getCellEditor(row, col);
   }
   private static DataEntryDlgTableCellEditor dataEntryActionEditor = null;

   public TableCellEditor getDataEntryActionEditor(int row) {
      if (dataEntryActionEditor == null) {
         DefaultCellEditor editor = null;

         JTextField textField = new JTextField();
         textField.setBorder(BorderFactory.createEmptyBorder());
         editor = new DefaultCellEditor(textField);
         editor.setClickCountToStart(1);
         dataEntryActionEditor = new DataEntryDlgTableCellEditor(editor);
      }
      return dataEntryActionEditor;
   }

   public TableCellEditor getComboEditor(String[] values)  {
        JComboBox cb = new JComboBox(values);
        DefaultCellEditor ed = new DefaultCellEditor(cb);
        return ed;

   }


}
