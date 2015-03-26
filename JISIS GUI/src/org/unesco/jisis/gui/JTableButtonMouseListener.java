/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

/**
 * JTable will not forward mouse events to components contained in its cells. 
 * If you want to be able to press the buttons you add to JTable,
 * you have to create your own MouseListener that forwards events to the 
 * JButton cells. JTableButtonMouseListener demonstrates how you could do this.
 * 
 * @author jcd
 */
class JTableButtonMouseListener implements MouseListener {

   private JTable table_;

   private void forwardEventToButton(MouseEvent e) {
      TableColumnModel columnModel = table_.getColumnModel();
      int column = columnModel.getColumnIndexAtX(e.getX());
      int row = e.getY() / table_.getRowHeight();
      Object value;
      JButton button;
      MouseEvent buttonEvent;
      if (row >= table_.getRowCount() || row < 0
              || column >= table_.getColumnCount() || column < 0) {
         return;
      }
      value = table_.getValueAt(row, column);
      if (!(value instanceof JButton)) {
         return;
      }
      button = (JButton) value;
      buttonEvent =
              (MouseEvent) SwingUtilities.convertMouseEvent(table_, e, button);
      button.dispatchEvent(buttonEvent);
// This is necessary so that when a button is pressed and released
// it gets rendered properly. Otherwise, the button may still appear
// pressed down when it has been released.
      table_.repaint();
   }

   public JTableButtonMouseListener(JTable table) {
      table_ = table;
   }

   public void mouseClicked(MouseEvent e) {
      forwardEventToButton(e);
   }

   public void mouseEntered(MouseEvent e) {
      forwardEventToButton(e);
   }

   public void mouseExited(MouseEvent e) {
      forwardEventToButton(e);
   }

   public void mousePressed(MouseEvent e) {
      forwardEventToButton(e);
   }

   public void mouseReleased(MouseEvent e) {
      forwardEventToButton(e);
   }
}