package org.unesco.jisis.jisisutils.gui;



import java.awt.Point;
import java.awt.event.*;

import javax.swing.table.*;

/**
 * The listener for receiving cell action events from the spreadsheet.
 *
 * Note that the class extends MouseAdapter which is an abstract adapter class
 * for receiving mouse events. The methods in this class are empty.
 * This class exists as convenience for creating listener objects.
 */
public class SpreadSheetCellListener extends MouseAdapter {
   /** The table header. */
   JTableHeader header_;

   /**
    * Constructor of SpreadSheetCellListener.
    * @param header the header.
    */
   public SpreadSheetCellListener(JTableHeader header) {
      header_ = header;
   }

   /**
    * Invoked when a mouse button is pressed on a component.
    * @param e the action event.
    */
   public void mousePressed(MouseEvent e) {
      Point origin = e.getPoint();
      int   col    = header_.columnAtPoint(origin);
      if (col == -1) {
         return;    // no cell found
      }
      SpreadSheetCellRenderer spreadSheetCellRenderer =
         (SpreadSheetCellRenderer) (header_.getColumnModel().getColumn(col).getHeaderRenderer());
      spreadSheetCellRenderer.setClickedColumn(col, col);
      header_.repaint();
   }
}
