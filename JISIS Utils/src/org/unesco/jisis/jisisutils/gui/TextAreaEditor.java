/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui;

import java.awt.Component;
import java.awt.ComponentOrientation;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellEditor;



public class TextAreaEditor extends AbstractCellEditor implements TableCellEditor{

   private JTextArea textArea = new JTextArea();
   private JScrollPane scrollPane = new JScrollPane(textArea);
   
   public TextAreaEditor(){
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
   }
   
   @Override
   public Object getCellEditorValue() {
      return textArea.getText();
   }

    public void applyComponentOrientation(ComponentOrientation orientation) {
      textArea.applyComponentOrientation(orientation);
   }

   @Override
   public Component getTableCellEditorComponent(JTable table, Object value,
         boolean isSelected, int row, int column) {
      if (value != null) {
         textArea.setText(value.toString());
      } else {
         textArea.setText(null);
      }
      return scrollPane;
   }

   public boolean getLineWrap() {
      return textArea.getLineWrap();
   }

   public boolean getWrapStyleWord() {
      return textArea.getWrapStyleWord();
   }

   public void setLineWrap(boolean wrap) {
      textArea.setLineWrap(wrap);
   }

   public void setWrapStyleWord(boolean word) {
      textArea.setWrapStyleWord(word);
   }
   public void setEditable(boolean edit) {
      textArea.setEditable(edit);
   }
  
   
}