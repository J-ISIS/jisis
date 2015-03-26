/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.awt.Color;
import java.util.List;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.gui.AbstractActionTableCellEditor;
import org.unesco.jisis.gui.FieldEntryDlg;
import org.unesco.jisis.jisisutil.history.ListSelectorDialog;

/**
 *
 * @author jc Dauphin
 */
public class PickListTableCellEditor  extends AbstractActionTableCellEditor{
    PickListData pickListData_;
    public  PickListTableCellEditor(TableCellEditor editor, PickListData pickListData){
        super(editor);
        pickListData_ = pickListData;
    }

    protected void editCell(JTable table, int row, int column){
        JTextArea textArea = new JTextArea(10, 50);
        Object value = table.getValueAt(row, column);
        if(value!=null){
           if (value instanceof String){
            textArea.setText((String)value);
            textArea.setCaretPosition(0);
           } else {
             textArea.setText("<<BLOB>>");
           }
        }
        List<String> labels = pickListData_.getLabels();
            List<String> codes = pickListData_.getCodes();
            JXList jxList = new JXList((String[]) labels.toArray(new String[labels.size()]));
            ColorHighlighter colorHighlighter = new ColorHighlighter( HighlightPredicate.ROLLOVER_ROW, Color.CYAN, Color.WHITE); 
            jxList.addHighlighter(colorHighlighter);
            jxList.setRolloverEnabled(true);
            if (pickListData_.isMultiChoice()) {
               jxList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            } else {
               jxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            final ListSelectorDialog jd = new ListSelectorDialog(WindowManager.getDefault().getMainWindow(),
                    pickListData_.getDialogTitle(), jxList);
             jd.setLocationRelativeTo(null);
            int result = jd.showDialog();
            
        
        jd.setLocationRelativeTo(null);
        jd.setVisible(true);
        if (result != ListSelectorDialog.APPROVE_OPTION) {
            return;
        }
//        try {
//            int[] selected = jxList.getSelectedIndices();
//            PickListButton pickListButton = (PickListButton) e.getSource();
//            StringBuilder sb = new StringBuilder();
//
//            if (pickListData_.isRepeat()) {
//                // Build a new field occurrence from each selected item 
//                for (int i = 0; i < selected.length; i++) {
//                    sb = new StringBuilder();
//                    if (pickListData_.isFirstDescribe()) {
//                        // The first is what the user sees on the list. 
//                        // The second is what it will be really inserted in the
//                        // field. This is useful to mask codes with 
//                        // human-readable descriptions.
//                        sb.append(codes.get(selected[i]));
//                    } else {
//                        sb.append(labels.get(selected[i]));
//                    }
//                    if (fld_.getOccurrenceValue(pickListButton.getID()) == null) {
//                        // Cuurent occurrence is empty thus start from there
//                        fld_.setOccurrence(pickListButton.getID(), sb.toString());
//                    } else {
//                        fld_.setOccurrence(fld_.getOccurrenceCount(), sb.toString());
//                    }
//                }
//            } else {
//                // We work on the current occurrence
//                if (pickListData_.isAdd()) {
//                    //New selected items' text will be added to the text already in the field.
//                    Object obj = fld_.getOccurrenceValue(pickListButton.getID());
//                    if (obj == null || ((String) obj).length() == 0) {
//                        // Do nothing
//                    } else {
//                        // Add a blank
//                        sb.append((String) obj).append(" ");
//                    }
//
//                }
//                for (int i = 0; i < selected.length; i++) {
//                    if (pickListData_.isFirstDescribe()) {
//                        // The first is what the user sees on the list. 
//                        // The second is what it will be really inserted in the
//                        // field. This is useful to mask codes with 
//                        // human-readable descriptions.
//                        sb.append(codes.get(selected[i]));
//                    } else {
//                        sb.append(labels.get(selected[i]));
//                    }
//                    if (i < selected.length - 1) {
//                        sb.append(" ");
//                    }
//                }
//                fld_.setOccurrence(pickListButton.getID(), sb.toString());
//            }
//
//            redraw();
//        } catch (DbException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//            
//    
//
//        table.setValueAt(jd.getSource(), row, column);
    }


}
