/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.gui;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellEditor;
import org.unesco.jisis.corelib.common.Global;

/**
 *
 * @author jc_dauphin
 *
 *  JTextField textField = new JTextField();
 *  textField.setBorder(BorderFactory.createEmptyBorder());
 *  DefaultCellEditor editor = new DefaultCellEditor(textField);
 *  editor.setClickCountToStart(1);
 *  table.getColumn(table.getColumnName(0)).setCellEditor(editor);
 *  table.getColumn(table.getColumnName(1))
 *                   .setCellEditor(new StringActionTableCellEditor(editor));
 */
public class EditorDlgActionTableCellEditor extends AbstractActionTableCellEditor{
   private String dlgTitle_;
    public EditorDlgActionTableCellEditor(TableCellEditor editor){
        super(editor);
    }

    protected void editCell(JTable table, int row, int column) {
        JTextArea textArea = new JTextArea(10, 50);
        if (Global.getApplicationFont() != null) {
            textArea.setFont(Global.getApplicationFont());
        }
        Object value = table.getValueAt(row, column);
        if (value != null) {
            textArea.setText((String) value);
            textArea.setCaretPosition(0);
        }
        PftSourceDlg dlg = new PftSourceDlg(frame, (String) value);
        dlg.setTitle(dlgTitle_);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        if (!dlg.succeeded()) {
            return;
        }

        table.setValueAt(dlg.getSource(), row, column);
    }
    
    public void setDialogTitle(String title) {
      dlgTitle_ = title;
    }


}
