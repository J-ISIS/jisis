/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author jcd
 */
public class HighlightRenderer implements TableCellRenderer {

   protected ICellHighlight cmp;
   protected TableCellRenderer targetRenderer;
   protected Color backColor;
   protected Color foreColor;
   protected Color highlightBack;
   protected Color highlightFore;

   public HighlightRenderer(ICellHighlight cmp,
           TableCellRenderer targetRenderer,
           Color backColor, Color foreColor,
           Color highlightBack, Color highlightFore) {
      this.cmp = cmp;
      this.targetRenderer = targetRenderer;
      this.backColor = backColor;
      this.foreColor = foreColor;
      this.highlightBack = highlightBack;
      this.highlightFore = highlightFore;
   }

   public Component getTableCellRendererComponent(JTable tbl,
           Object value, boolean isSelected,
           boolean hasFocus, int row, int column) {
      TableCellRenderer renderer = targetRenderer;
      if (renderer == null) {
         renderer = tbl.getDefaultRenderer(
                 tbl.getColumnClass(column));
      }
      Component comp =
              renderer.getTableCellRendererComponent(tbl,
              value, isSelected, hasFocus, row, column);
      if (isSelected == false && hasFocus == false && value
              != null) {
         if (cmp.shouldHighlight(tbl, value, row, column)) {
            comp.setForeground(highlightFore);
            comp.setBackground(highlightBack);
         } else {
            comp.setForeground(foreColor);
            comp.setBackground(backColor);
         }
      }
      return comp;
   }
}
