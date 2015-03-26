/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.datadefinition.wksex;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 *
 * @author jcd
 */
// Imports
public class JisisTreeTable
        extends JTable
        implements MouseListener {

   private CustomDataModel model  = null;


   public JisisTreeTable(CustomDataModel model) {
      super(model);
      this.model = model;
// Configure the table
      setFont(new Font("Helvetica", Font.PLAIN, 12));
      setColumnSelectionAllowed(false);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setShowGrid(false);
      setIntercellSpacing(new Dimension(0, 1));
      setAutoCreateColumnsFromModel(false);
      sizeColumnsToFit(true);
// Prevent table column reordering
      JTableHeader header = getTableHeader();
      header.setUpdateTableInRealTime(false);
      header.setReorderingAllowed(false);
// Added our columns into the column model
      TableColumn newColumn = new TableColumn();
      newColumn.setCellRenderer(new CustomCellRenderer(model));
      newColumn.setHeaderValue("Server");
      newColumn.setHeaderRenderer(new CustomHeaderRenderer());
      addColumn(newColumn);
      newColumn.setCellRenderer(new CustomCellRenderer(model));
      newColumn.setHeaderValue("Location");
      newColumn.setHeaderRenderer(new CustomHeaderRenderer());
      addColumn(newColumn);
      newColumn.setCellRenderer(new CustomCellRenderer(model));
      newColumn.setHeaderValue("Status");
      newColumn.setHeaderRenderer(new CustomHeaderRenderer());
      addColumn(newColumn);
// Attach a mouse listener
      addMouseListener(this);
   }

   public void mouseClicked(MouseEvent e) {
      int iMouseX = e.getX();
      int iMouseY = e.getY();
      int iSelectedColumn = columnAtPoint(
              new Point(iMouseX, iMouseY));
      int iSelectedRow = rowAtPoint(new Point(iMouseX, iMouseY));
      if (iSelectedRow >= 0 && iSelectedRow < model.getRowCount()) {
// Get the type of service we are rendering
         MyTableNode node = (MyTableNode) model.listDisplayField.get(iSelectedRow);
// Test to see if the user clicked on the
// expand/collapse button
         if (iSelectedColumn == 0 && iMouseX >= 4 && iMouseX <= 12
                 && node.type.equals(model.FIELD)) {
// Expand the tree
            if (node.iChildren == 0 && node.iActualChildren > 0) {

               model.ExpandParent(node);
               repaint();
            } else if (node.iChildren > 0) {
               model.CollapseParent(node);
               repaint();
            }
         }
      }
   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
   }

   public void mousePressed(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {
   }
}
