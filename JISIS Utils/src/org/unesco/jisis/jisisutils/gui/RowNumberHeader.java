/*
RowNumberHeader.java
 *
Created on 17 ao�t 2007, 12:02
 *
To change this template, choose Tools | Template Manager
and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

import javax.swing.JScrollPane;
import javax.swing.JTable;

public class RowNumberHeader extends JTable {

   RowHeaderRenderer render_ = null;
   JTable mainTable_;

   public  RowNumberHeader(JTable table, JScrollPane scrollPane) {
      super(new RowHeaderModel(table));
      mainTable_ = table;
      configure(table, scrollPane);
   }
   
   

   @Override
   public int getRowHeight() {
      return mainTable_.getRowHeight();
   }

   @Override
   public int getRowHeight(int row) {
      return mainTable_.getRowHeight(row);
   }

   protected void configure(JTable table, JScrollPane scrollPane) {
      render_ = new RowHeaderRenderer(table, scrollPane);
      setRowHeight(table.getRowHeight());
      setIntercellSpacing(table.getIntercellSpacing());
      setShowHorizontalLines(false);
      setShowVerticalLines(false);
   }

   /* Returns the preferred size of the viewport for this table.*/
//   public Dimension getPreferredScrollableViewportSize() {
//      return new Dimension(32, super.getPreferredSize().height);
//   }             
//
//   public TableCellRenderer getDefaultRenderer(Class c) {
//      return render_;
//   }

  
public RowHeaderRenderer getRowHeaderRenderer() {
      return render_;
   }
   
}