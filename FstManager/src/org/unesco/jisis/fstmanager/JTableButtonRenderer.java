/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.fstmanager;


import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
/**
 *
 * @author Daniel F. Savarese
 * Used to reduce coding time for a colored button in a jtable.
 * This is basically only used as Swing uses renders to draw everything,
 * and clicks are not automatically passed to objects when their physical
 * area is clicked but there are other layers of containers on top of it.
 * http://www.devx.com/getHelpOn/10MinuteSolution/20425
 *
 */
public class JTableButtonRenderer implements TableCellRenderer {
  private TableCellRenderer __defaultRenderer;

  public JTableButtonRenderer(TableCellRenderer renderer) {
    __defaultRenderer = renderer;
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
						 boolean isSelected,
						 boolean hasFocus,
						 int row, int column)
  {
    if(value instanceof Component)
      return (Component)value;
    return __defaultRenderer.getTableCellRendererComponent(
	   table, value, isSelected, hasFocus, row, column);
  }
}
