/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author jcd
 *
 * A holder for data and an associated icon
 */
public class JTableButtonRenderer extends JButton implements TableCellRenderer {

   protected Color foreground;
   protected Color background;
   protected Font font;
   protected Border border;

   public JTableButtonRenderer() {
      this.border = getBorder();
      this.setOpaque(true);
   }

   @Override
   public void setForeground(Color foreground) {
      this.foreground = foreground;
      super.setForeground(foreground);
   }

   @Override
   public void setBackground(Color background) {
      this.background = background;
      super.setBackground(background);
   }

   @Override
   public void setFont(Font font) {
      this.font = font;
      super.setFont(font);
   }

   public Component getTableCellRendererComponent(
           JTable table, Object value,
           boolean isSelected,
           boolean hasFocus,
           int row, int column) {
      Color cellForeground = foreground
              != null ? foreground : table.getForeground();
      Color cellBackground = background
              != null ? background : table.getBackground();
      setFont(font != null ? font : table.getFont());
      if (hasFocus) {
         setBorder(UIManager.getBorder(
                 "Table.focusCellHighlightBorder"));
         if (table.isCellEditable(row, column)) {
            cellForeground = UIManager.getColor(
                    "Table.focusCellForeground");
            cellBackground = UIManager.getColor(
                    "Table.focusCellBackground");
         }
      } else {
         setBorder(border);
      }

      super.setForeground(cellForeground);
      super.setBackground(cellBackground);

      // Customize the component's appearance
      setValue(value);

      return this;
   }

   protected void setValue(Object value) {
      if (value == null) {
         setText("");
         setIcon(null);
      } else if (value instanceof Icon) {
         setText("");
         setIcon((Icon) value);
      } else if (value instanceof DataWithIcon) {
         DataWithIcon d = (DataWithIcon) value;
         setText(d.toString());
         setIcon(d.getIcon());
      } else {
         setText(value.toString());
         setIcon(null);
      }
   }
}
