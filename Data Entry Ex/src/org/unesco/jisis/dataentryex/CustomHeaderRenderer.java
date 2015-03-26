package org.unesco.jisis.dataentryex;


import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellRenderer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jcd
 */
public class CustomHeaderRenderer extends JLabel implements TableCellRenderer {

   public Component getTableCellRendererComponent(JTable table,
           Object value, boolean isSelected, boolean hasFocus,
           int row, int column) {
// Retrieve the text to display
      String sText = (String) value;
// Set all sorts of interesting alignment options
      setVerticalAlignment(SwingConstants.CENTER);
      setHorizontalAlignment(SwingConstants.LEFT);
// Assign a border
      setBorder(new EtchedBorder());
// Set the text to the correct value
      switch (column) {
         case 0:
            setText("Server");
            break;
         case 1:
            setText("Location");
            break;
         case 2:
            setText("Status");
            break;
      }
      return this;
   }
}
