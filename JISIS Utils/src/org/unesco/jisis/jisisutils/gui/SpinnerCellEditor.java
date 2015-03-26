/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

/**
 *
 * @author jc_dauphin
 */
import java.awt.Component;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.table.TableCellEditor;

public class SpinnerCellEditor extends AbstractCellEditor
        implements TableCellEditor {

   private final JSpinner spinner;

   public SpinnerCellEditor(JSpinner spinner) {
      this.spinner = spinner;
   }

   public Component getTableCellEditorComponent(JTable table, Object value,
                                       boolean isSelected, int row, int col) {
      cancelCellEditing(); // cancel any edits in progress.
      spinner.setValue(value);
      return spinner;
   }

   public Object getCellEditorValue() {
      try {
         spinner.commitEdit();
      } catch (ParseException e) { /* ignore, revert */ }
      return spinner.getValue();
   }

   public boolean stopCellEditing() {
      try {
         spinner.commitEdit();
      } catch (ParseException e) { /* ignore, revert */ }
      return super.stopCellEditing();
   }
   // XXX should ignore first click on component, so that a click on
   // the left-hand-side doesn't change the spinner's value as well
   // as commence editing.
   // XXX spinners in tables need more space: both height and width.
}

