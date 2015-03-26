/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author jc_dauphin
 */

/**
 * The RowHeaderRenderer is the the cell renderer for the cells of the JTable 
 * used as a row header. it returns a component that render each cell.
 *
 * Note: The selection logic is provided by the RowHeaderRenderer that displays
 * the selected row header cell differently as well as the row cells of the
 * body table.
 */
public class RowHeaderRenderer extends JButton implements TableCellRenderer {
   private int pushedRow_;

   /** The JTable main body of the table spreadsheet.  */
   public JTable body_;
   
   public JScrollPane scrollPane_;

   /** Constructor to instantiate a RowHeaderRenderer. */
   public RowHeaderRenderer(JTable body, JScrollPane scrollPane) {
      pushedRow_ = -1;
      body_     = body;
      scrollPane_ = scrollPane;
   }
  /** Returns the component used for drawing the cell.
   *
   *Parameters:
   *    table - the JTable that is asking the renderer to draw; can be null
   *    value - the value of the cell to be rendered. 
   *            It is up to the specific renderer to interpret and draw the value.
   *            For example, if value is the string "true", it could be rendered
   *            as a string or it could be rendered as a check box that is 
   *            checked. null is a valid value
   *   isSelected - true if the cell is to be rendered with the selection
   *                highlighted; otherwise false
   *   hasFocus - if true, render cell appropriately. 
   *              For example, put a special border on the cell, if the cell can
   *              be edited, render in the color used to indicate editing
   *   row - the row index of the cell being drawn. When drawing the header, 
   *         the value of row is -1
   *   column - the column index of the cell being drawn
   **/
   
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
      

      if (row == pushedRow_) {
         int c = body_.columnAtPoint(
                     SwingUtilities.convertPoint(
                        scrollPane_, 130, 50, body_));
         //System.out.println("row == pushed row  c="+ Integer.toString(c));

         body_.setColumnSelectionInterval(body_.getColumnCount() - 1, c);
         body_.addColumnSelectionInterval(0, c);
         System.out.println("row == pushed row "+ Integer.toString(row));
         // Select the row of the body table 
         body_.setRowSelectionInterval(pushedRow_, pushedRow_);
         body_.requestFocus();
         body_.getTableHeader().repaint();
         pushedRow_ = -1;
      }

      if (isSelected) {
         setForeground(Color.white);
         setBackground(Color.darkGray);
         System.out.println("Selected row "+ Integer.toString(row));
      } else {
         setForeground(body_.getForeground());
         setBackground(UIManager.getColor("Button.background"));
         System.out.println("Non Selected row "+ Integer.toString(row));
      }
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
      } else {
         setText(value.toString());
         setIcon(null);
      }
       body_.invalidate();
   }

   /**
    * Specifies the selected row.
    * @param body the main body of the spreadsheet.
    * @param row the selected row.
    */
   public void setClickedRow(JTable body, int row) {
      System.out.println("setClickedRow "+ Integer.toString(row));
      pushedRow_ = row;
      body.repaint();
   }

   /**
    * Specifies the main body of the spreadsheet.
    * @param body the main body of the spreadsheet.
    */
   public void setTable(JTable body) {
      body_ = body;
   }
}
