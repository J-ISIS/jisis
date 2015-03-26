/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui;

import java.awt.Component;
import java.text.Format;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Cell renderer used to format data using a given Format object
 * @author jc_dauphin
 */

public class FormattedCellRenderer extends DefaultTableCellRenderer {

   protected Format m_format;

   public FormattedCellRenderer(Format format) {
      m_format = format;
   }

   @Override
   public Component getTableCellRendererComponent(JTable table,
           Object value, boolean isSelected, boolean hasFocus,
           int nRow, int nCol) {
      return super.getTableCellRendererComponent(table,
              value == null ? null : m_format.format(value),
              isSelected, hasFocus, nRow, nCol);
   }
}