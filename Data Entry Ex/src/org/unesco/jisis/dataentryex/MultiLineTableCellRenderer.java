/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;



import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class MultiLineTableCellRenderer extends JTextArea implements
        TableCellRenderer {

   protected static Border noFocusBorder;
   private Color unselectedForeground;
   private Color unselectedBackground;

   public MultiLineTableCellRenderer() {
      super();
      noFocusBorder = new EmptyBorder(1, 2, 1, 2);
      setLineWrap(false);
      setWrapStyleWord(false);
      setOpaque(true);
      setBorder(noFocusBorder);
   }

   public void setForeground(Color c) {
      super.setForeground(c);
      unselectedForeground = c;
   }

   public void setBackground(Color c) {
      super.setBackground(c);
      unselectedBackground = c;
   }

   public void updateUI() {
      super.updateUI();
      setForeground(null);
      setBackground(null);
   }

   public Component getTableCellRendererComponent(JTable table, Object value,
           boolean isSelected, boolean hasFocus,
           int row, int column) {

      if (isSelected) {
         super.setForeground(table.getSelectionForeground());
         super.setBackground(table.getSelectionBackground());
      } else {
         super.setForeground((unselectedForeground != null)
                 ? unselectedForeground
                 : table.getForeground());
         super.setBackground((unselectedBackground != null)
                 ? unselectedBackground
                 : table.getBackground());
      }

      setFont(table.getFont());

      if (hasFocus) {
         setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
         if (table.isCellEditable(row, column)) {
            super.setForeground(UIManager.getColor("Table.focusCellForeground"));
            super.setBackground(UIManager.getColor("Table.focusCellBackground"));
         }
      } else {
         setBorder(noFocusBorder);
      }

      setValue(value);
      int rowHeight = (int) getPreferredSize().getHeight();
      if (table.getRowHeight() != rowHeight) {
         table.setRowHeight(rowHeight);
      }

      return this;
   }

   protected void setValue(Object value) {
      setText((value == null) ? "" : value.toString());
   }
}



