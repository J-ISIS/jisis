/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutil.history;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jcd
 */
public class ListModelEditor {
    
     private static final String BUNDLE_PATH =  "org/unesco/jisis/jisisutil/history/Bundle";

    public void open(DefaultListModel listModel) {
        final DefaultTableModel tableModel = createTableModel(listModel);
        
        final JTable table = new JTable(tableModel){
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return (getSize().width < getParent().getSize().width);
            }

          
          
        };
       
        tweak(table);
        /**
         * This is needed to get horizontal scroll bar
         */
        table.getColumnModel().getColumn(0).setPreferredWidth(700);

        table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        final JScrollPane scrollPane = new JScrollPane( table );
       
        scrollPane.setPreferredSize(new Dimension(500, 500));
       
       
        table
            .setToolTipText(java.util.ResourceBundle.getBundle(BUNDLE_PATH).getString("MOVE: PGUP/PGDOWN; EDIT: DOUBLE-CLICK OR INSERT/DELETE"));

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int[] selRows = table.getSelectedRows();
                if (selRows.length == 0) {
                    return;
                }
                int firstSelectedRow = selRows[0];
                int key = e.getKeyCode();
                ListSelectionModel selectionModel = table.getSelectionModel();
                switch (key) {
                    case KeyEvent.VK_DELETE:
                        for (int i = selRows.length - 1; i >= 0; i--) {
                            tableModel.removeRow(selRows[i]);
                        }
                        if (firstSelectedRow >= 0
                            && firstSelectedRow < tableModel.getRowCount()) {
                            selectionModel.addSelectionInterval(firstSelectedRow,
                                firstSelectedRow);
                        }
                        e.consume();// avoid beep
                        break;
                    case KeyEvent.VK_INSERT:
                        tableModel.insertRow(firstSelectedRow + 1, new String[]{""});
                        e.consume(); // Dont edit cell
                        break;
                    case KeyEvent.VK_PAGE_UP:
                    case KeyEvent.VK_PAGE_DOWN:
                        boolean isUp = key == KeyEvent.VK_PAGE_UP;
                        int direction = isUp ? -1 : 1;
                        int min = selectionModel.getMinSelectionIndex() + direction;
                        int max = selectionModel.getMaxSelectionIndex() + direction;
                        if (min < 0 || max >= tableModel.getRowCount()) {
                            return; // avoid ArrayIndexOutOfBoundsException
                        }
                        for (int i = 0; i < selRows.length; i++) {
                            int row = selRows[isUp ? i : (selRows.length - 1 - i)];
                            int to = row + direction;
                            selectionModel.removeSelectionInterval(row, row);
                            selectionModel.addSelectionInterval(to, to);
                            tableModel.moveRow(row, row, to);
                        }
                        break;
                }
            }
        });
        // make the dialog resizable
        table.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                Window window = SwingUtilities.getWindowAncestor(table);
                if (window instanceof Dialog) {
                    Dialog dialog = (Dialog) window;
                    if (!dialog.isResizable()) {
                        dialog.setResizable(true);
                    }
                }
            }
        });
        int result = JOptionPane.showConfirmDialog(null, scrollPane, 
            java.util.ResourceBundle.getBundle(BUNDLE_PATH).getString("CHANGE PREVIOUSLY ENTERED STRINGS:"), 
                      JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            updatelistModel(listModel, tableModel);
        }
    }
    
    private void tweak(JTable table) {
         // ------------------------------------------------
      // Default row height is 16 pixels, increase to 20
      // ------------------------------------------------
      table.setRowHeight(20);

      // ------------------------------------------
      // Increase the gap between rows and columns
      // ------------------------------------------
      // Get defaults
      Dimension d = table.getIntercellSpacing();
      // d.width == 1, d.height == 1
      // Add 5 spaces to the left and right sides of a cell.
      // Add 2 spaces to the top and bottom sides of a cell.
      int gapWidth  = 10;
      int gapHeight = 4;

      table.setIntercellSpacing(new Dimension(gapWidth, gapHeight));
      // Increase the row height
      table.setRowHeight(table.getRowHeight() + gapHeight);
    }

    private DefaultTableModel createTableModel(DefaultListModel listModel) {
        Object[][] tableRowData = new String[listModel.size()][1];
        for (int i = 0; i < listModel.size(); i++) {
            tableRowData[i][0] = listModel.get(i);
        }
        return new DefaultTableModel(tableRowData, new String[]{""});
    }

    private void updatelistModel(DefaultListModel listModel,
        DefaultTableModel tableModel) {
        listModel.removeAllElements();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object cellText = tableModel.getValueAt(i, 0);
            if (cellText != null && !cellText.equals("")) {
                listModel.addElement(cellText);
            }
        }
    }
}
