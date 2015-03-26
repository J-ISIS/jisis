/*
RowNumberHeader.java
 *
Created on 17 ao�t 2007, 12:02
 *
To change this template, choose Tools | Template Manager
and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui.rowheader;

//~--- JDK imports ------------------------------------------------------------

/**
 * Class to be used for displaying numbered row headers.
 *
 * In addition to the scroll bars and viewport, a JScrollPane can have a
 * column header and a row header. Each of these is a JViewport object that
 * you specify with setRowHeaderView, and setColumnHeaderView.
 *
 *  table = new JTable(stm);
 *  table.setRowSelectionAllowed(false);
 *  table.setColumnSelectionAllowed(true);
 *  RowNumberHeader rowHeader = new RowNumberHeader(table);
 *
 *  JScrollPane scroll = new JScrollPane(table);
 *  JViewport viewport = new JViewport();
 *  viewport.setView(rowHeader);
 *  viewport.setPreferredSize(rowHeader.getPreferredSize());
 *  scroll.setRowHeaderView(viewport);
 *  scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER,rowHeader.getTableHeader());
 *
 *  getContentPane().add(scroll, BorderLayout.CENTER);
 * @author jc_dauphin
 */
import javax.swing.*;
import javax.swing.table.*;


public class RowHeader extends JTable {
   protected JTable mainTable_;
   

   public RowHeader(JTable table) {
    super();
    mainTable_ = table;
    setModel(new RowHeaderModel(table));
    setAutoscrolls( false );
    setPreferredScrollableViewportSize(getMinimumSize());
    setRowSelectionAllowed(false);
    JComponent renderer = (JComponent)getDefaultRenderer(Object.class);
    LookAndFeel.installColorsAndFont(renderer, 
                                     "TableHeader.background", 
                                     "TableHeader.foreground", 
                                     "TableHeader.font");
    LookAndFeel.installBorder(this, "TableHeader.cellBorder");
  }

   @Override
   public TableCellRenderer getCellRenderer(int row, int column) {
      TableCellRenderer renderer = super.getCellRenderer(row, column);
      System.out.println("RowHeader getCellRenderer row="+row+" col="+column); 
      System.out.println("renderer="+renderer.toString());
        return renderer;
    }

   

   @Override
   public Object getValueAt(int row, int column) {
      System.out.println("RowHeader getValueAt row="+row+" col="+column);
      return super.getValueAt(row, column);
   }
 
   
//   public int getRowHeight(int row) {
//      return mainTable_.getRowHeight();
//   }

   
}
