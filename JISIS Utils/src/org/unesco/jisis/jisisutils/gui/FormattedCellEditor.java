/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

import java.text.*;
import javax.swing.*;

/**
 * Cell Editor to provide custom editing capability using a JFormattedTextField
 * @author jc_dauphin
 */

public class FormattedCellEditor extends DefaultCellEditor {

   public FormattedCellEditor(
           final JFormattedTextField formattedTextField) {
      super(formattedTextField);
      formattedTextField.removeActionListener(delegate);
      delegate = new EditorDelegate() {

         @Override
         public void setValue(Object value) {
            formattedTextField.setValue(value);
         }

         @Override
         public Object getCellEditorValue() {
            return formattedTextField.getValue();
         }
      };
      formattedTextField.addActionListener(delegate);
      formattedTextField.setBorder(null);
   }
}