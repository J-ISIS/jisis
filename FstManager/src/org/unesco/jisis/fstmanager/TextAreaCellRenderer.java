package org.unesco.jisis.fstmanager;

import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

class TextAreaCellRenderer extends JTextArea
        implements TableCellRenderer {

   protected static Border m_noFocusBorder =
           new EmptyBorder(1, 1, 1, 1);
   protected static Border m_focusBorder = javax.swing.UIManager
.getBorder("Table.focusCellHighlightBorder");

   public TextAreaCellRenderer() {
      setEditable(false);
      setLineWrap(true);
      setWrapStyleWord(true);
      setBorder(m_noFocusBorder);
   }

   public Component getTableCellRendererComponent(JTable table,
           Object value, boolean isSelected, boolean hasFocus,
           int nRow, int nCol) {
      if (value instanceof String) {
         setText((String) value);
      }
      setBackground(isSelected && !hasFocus ? table.getSelectionBackground() : table.getBackground());
      setForeground(isSelected && !hasFocus ? table.getSelectionForeground() : table.getForeground());
      setFont(table.getFont());
      setBorder(hasFocus ? m_focusBorder : m_noFocusBorder);
// Adjust row's height
//      int width = table.getColumnModel().getColumn(nCol).getWidth();
//      setSize(width, 1000);
//      int rowHeight = getPreferredSize().height;
//      if (table.getRowHeight(nRow) != rowHeight) {
//         table.setRowHeight(nRow, rowHeight);
//      }
      return this;
   }

   public String getToolTipText(MouseEvent event) {
      return null;
   }
}