/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author jcd
 */
public class JTableButtonEditor extends BasicCellEditor
                  implements ActionListener,
                  TableCellEditor {
   
   protected Object value;
   protected Color foreground;
   protected Color background;
   protected Font font;
   
   public JTableButtonEditor(JButton button) {
      super(button);
      button.addActionListener(this);
   }

   public void setForeground(Color foreground) {
      this.foreground = foreground;
      editor.setForeground(foreground);
   }

   public void setBackground(Color background) {
      this.background = background;
      editor.setBackground(background);
   }

   public void setFont(Font font) {
      this.font = font;
      editor.setFont(font);
   }

   public Object getCellEditorValue() {
      return value;
   }

   public void editingStarted(EventObject event) {
      // Edit starting - click the button if necessary
      if (!(event instanceof MouseEvent)) {
         // Keyboard event - click the button
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               ((JButton)editor).doClick();
            }
         });
      }
   }

   public Component getTableCellEditorComponent(
            JTable tbl,
            Object value, boolean isSelected,
            int row, int column) {
      editor.setForeground(foreground != null ? foreground :
                                      tbl.getForeground());
      editor.setBackground(background != null ? background :
                                      tbl.getBackground());
      editor.setFont(font != null ? font : tbl.getFont());

      this.value = value;
      setValue(value);
      return editor;
   }

   protected void setValue(Object value) {
      JButton button = (JButton)editor;
      if (value == null) {
         button.setText("");
         button.setIcon(null);
      } else if (value instanceof Icon) {
         button.setText("");
         button.setIcon((Icon)value);
      } else if (value instanceof DataWithIcon) {
         DataWithIcon d = (DataWithIcon)value;
         button.setText(d.toString());
         button.setIcon(d.getIcon());
      } else {
         button.setText(value.toString() ) ;
         button.setIcon(null);
      }
   }

   public void actionPerformed(ActionEvent evt) {
      // Button pressed - stop the edit
      stopCellEditing();
   }

 
}
