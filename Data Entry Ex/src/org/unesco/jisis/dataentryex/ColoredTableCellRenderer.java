/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryex;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author jcd
 */

public class ColoredTableCellRenderer extends JLabel implements TableCellRenderer
  {
  private Color gray = new Color(100, 100, 100);

  /** Construct a new <code>ColeredCellRenderer</code>. */

  public ColoredTableCellRenderer()
    {
    setOpaque(true);
    }


   /**
    * Return the component (in this case a <code>JLabel</code> that is used
    * as a "rubber stamp" for drawing items in the <code>JList</code>. The
    * background of the cell is black, and the foreground will be the color of
    * the colored string.
    *
    * @param table - The associated <code>JTable</code> instance
    * @param value - The <code>ColoredString</code> to draw.
    * @param isSelected - <b>true</b> if this item is currently selected in the
    * table.
    * @param hasFocus - <b>true</b> if this item currently has focus in the
    * table.
    * @param row - cell row
    * @param column - column row
    * @return
    */
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
     
      ColoredString s = (ColoredString)value;

    setText(s.getString());
    setBackground(isSelected ? gray : Color.black);
    setForeground(s.getColor());
    return(this);

   }
  }
