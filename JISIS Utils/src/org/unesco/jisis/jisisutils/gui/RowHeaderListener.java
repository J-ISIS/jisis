package org.unesco.jisis.jisisutils.gui;

//~--- non-JDK imports --------------------------------------------------------

import java.awt.Point;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

/**
 * The listener for receiving row header action events from the spreadsheet.
 *
 */
public class RowHeaderListener extends MouseAdapter {

   /** The header. */
   public JTable header_;

   /** The main body of the spreadsheet. */
   public JTable body_;

   /**
    * Constructor of RowHeaderListener.
    * @param header - the JTable header.
    * @param body   - the JTable main body of the spreadsheet.
    */
   public RowHeaderListener(JTable header, JTable body) {
      header_ = header;
      body_   = body;
   }

   /**
    * Invoked when a mouse button is pressed on a row header cell.
    * @param e the action event.
    */
   public void mousePressed(MouseEvent e) {
      Point origin = e.getPoint();
      int row = header_.rowAtPoint(origin);
      if (row == -1)
         return; // no cell found
      RowHeaderRenderer rowHeaderRenderer = 
              (RowHeaderRenderer) header_.getDefaultRenderer(header_.getColumnClass(0));
      // Notify the renderer of the row clicked
      rowHeaderRenderer.setClickedRow(body_, row);
   }

   /**
    * The setter for the header.
    * @param header the header.
    */
   public void setHeader(JTable header) {
      header_ = header;
   }
}
