package org.unesco.jisis.fstmanager;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellEditor;

class TextAreaCellEditor extends AbstractCellEditor
        implements TableCellEditor {

   public static int CLICK_COUNT_TO_EDIT = 2;
   protected JTextArea m_textArea;
   protected JScrollPane m_scroll;

   public TextAreaCellEditor() {
      m_textArea = new JTextArea();
      m_textArea.setLineWrap(true);
      m_textArea.setWrapStyleWord(true);
      m_scroll = new JScrollPane(m_textArea,
              JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
              JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
   }

   public Component getTableCellEditorComponent(JTable table,
           Object value,
           boolean isSelected,
           int nRow, int nCol) {
      m_textArea.setBackground(table.getBackground());
      m_textArea.setForeground(table.getForeground());
      m_textArea.setFont(table.getFont());
      m_textArea.setText(value == null ? "" : value.toString());
      return m_scroll;
   }

   public Object getCellEditorValue() {
      return m_textArea.getText();
   }

   public boolean isCellEditable(EventObject anEvent) {
      if (anEvent instanceof MouseEvent) {
         int click = ((MouseEvent) anEvent).getClickCount();
         return click >= CLICK_COUNT_TO_EDIT;
      }
      return true;
   }
}