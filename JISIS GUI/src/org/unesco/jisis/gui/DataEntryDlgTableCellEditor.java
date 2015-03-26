/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.gui;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author jcd
 */
public class DataEntryDlgTableCellEditor extends AbstractActionTableCellEditor {
    public  DataEntryDlgTableCellEditor(TableCellEditor editor){
        super(editor);
    }

    protected void editCell(JTable table, int row, int column){

       // Get the cell initial data value
        Object value = table.getValueAt(row, column);

        FieldEntryDlg dlg = new FieldEntryDlg(frame, value);
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        if (!dlg.succeeded())
           return;

        table.setValueAt(dlg.getSource(), row, column);
        
        editor.stopCellEditing();
        
    }


}
