package org.unesco.jisis.jisisutils.gui;

//~--- non-JDK imports --------------------------------------------------------



//~--- JDK imports ------------------------------------------------------------


import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * The render is used to display the left-corner cell in the spreadsheet.
 *
 */
public class SpreadSheetCellRenderer extends JButton implements TableCellRenderer {
  
   /**
    * The main body of the spreadsheet.
    */
   public JTable body_;
   public JScrollPane scrollPane_;

   /**
    * The header.
    */
   public JTableHeader header;
   private int         pushedColumnstart, pushedColumnend;

   /**
    * Constructor of SpreadSheetCellRenderer.
    * @param header the header.
    * @param body The main body of the spreadsheet.
    */
   public SpreadSheetCellRenderer(JTableHeader header, JTable body, JScrollPane scrollPane) {
      pushedColumnstart = pushedColumnend = -1;
      this.header       = header;
      this.body_         = body;
      scrollPane_        = scrollPane;
      setMargin(new Insets(0, 0, 0, 0));
      setHorizontalAlignment(SwingConstants.CENTER);
   }

   /*
    *  (non-Javadoc)
    * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
    */
   public Component getTableCellRendererComponent(JTable table, 
           Object value, boolean isSelected,
           boolean hasFocus, int row, int column) {
      if (column == pushedColumnstart) {
          System.out.println("SpreadSheetCellRenderersetClickedColumn getTableCellRendererComponent"+Integer.toString(column));
          int c = this.body_.columnAtPoint(
                     SwingUtilities.convertPoint(
                        scrollPane_, 130, 50, this.body_));
         this.body_.setColumnSelectionInterval(this.body_.getColumnCount() - 1, c);
         this.body_.addColumnSelectionInterval(0, c);
         int r = this.body_.rowAtPoint(
                     SwingUtilities.convertPoint(
                        scrollPane_, 50, 35, this.body_));
         this.body_.setRowSelectionInterval(this.body_.getRowCount() - 1, 0);
         this.body_.addRowSelectionInterval(0, r);
         this.body_.editCellAt(r, c);
         this.body_.editingCanceled(new ChangeEvent(table.getTableHeader()));
         this.body_.requestFocus();
         this.body_.getTableHeader().repaint();
         setClickedColumn(-1, -1);
         table.editingStopped(new ChangeEvent(table.getTableHeader()));
      }
      if (isAllSelected(this.body_)) {
         this.setBackground(Color.darkGray);
         this.setForeground(Color.white);
      } else {
         setForeground(this.body_.getForeground());
         setBackground(UIManager.getColor("Button.background"));
      }
      return this;
   }

   /**
    * Determines if all cells are selected.
    * @param body the main body of the spreadsheet.
    * @return true if all cells are selected and false otherwise.
    */
   private boolean isAllSelected(JTable body) {
      int[] srows = body.getSelectedRows();
      if (body.getRowCount() == srows.length) {
         int[] scols = body.getSelectedColumns();
         if (body.getColumnCount() == scols.length) {
            return true;
         }
      }
      return false;
   }

   /**
    * Specifies the selected columns.
    * @param colstart the first selected column.
    * @param colend the last selected column.
    */
   public void setClickedColumn(int colstart, int colend) {
      pushedColumnstart = colstart;
      pushedColumnend   = colend;
      System.out.println("setClickedColumn "+Integer.toString(colstart)+" colend="+Integer.toString(colend));
   }

  
}
