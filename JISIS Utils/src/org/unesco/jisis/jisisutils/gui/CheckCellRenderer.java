/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author jc_dauphin
 */

public class CheckCellRenderer extends JCheckBox
        implements TableCellRenderer {

   protected static Border m_noFocusBorder =
           new EmptyBorder(1, 1, 1, 1);
   protected static Border m_focusBorder = UIManager.getBorder(
           "Table.focusCellHighlightBorder");

   public CheckCellRenderer() {
      super();
      setOpaque(true);
      setBorderPainted(true);
      setBorder(m_noFocusBorder);
      setHorizontalAlignment(JCheckBox.CENTER);
   }

   public Component getTableCellRendererComponent(JTable table,
           Object value, boolean isSelected, boolean hasFocus,
           int nRow, int nCol) {
      if (value instanceof Boolean) {

         Boolean b = (Boolean) value;
         setSelected(b.booleanValue());
      }
      setBackground(isSelected && !hasFocus ? table.getSelectionBackground() : table.getBackground());
      setForeground(isSelected && !hasFocus ? table.getSelectionForeground() : table.getForeground());
      setFont(table.getFont());
      setBorder(hasFocus ? m_focusBorder : m_noFocusBorder);
      return this;
   }
}