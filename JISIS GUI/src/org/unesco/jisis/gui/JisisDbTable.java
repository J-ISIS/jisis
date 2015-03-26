/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;


import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisisutils.distributed.DistributedTableModel;
import org.unesco.jisis.jisisutils.gui.TextAreaEditor;
import org.unesco.jisis.jisisutils.gui.TextAreaRenderer;

/**
 *
 * @author jc_dauphin
 */
public class JisisDbTable extends JTable {

   private ClientDatabaseProxy db_;
   private RecordTableDataSource dataSource_;
   private DistributedTableModel model_;
   private JTable nonScrollingColumns_;
   private JTable table_ = this;
   private javax.swing.JScrollPane scrollPane_;

   public JisisDbTable(IDatabase db) {

      if (db instanceof ClientDatabaseProxy) {
         db_ = (ClientDatabaseProxy) db;
      } else {
         throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
      }
     initTable();
   }

   private void initTable() {

      table_      = this;
      /** Initialize the JTable main body component */
      /* Don't let the system create the column for us */
      table_.setAutoCreateColumnsFromModel(false);
      /* allow column selection */
      table_.setColumnSelectionAllowed(true);
      table_.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

       try {
          dataSource_ = new RecordTableDataSource(db_);
         /* Create the Table model for this DB, overriding the isCellEditable
          * method so that we can edit the cell for showing the scroll bar.
          * Editing is later-on disabled by calling setEditable(false) on the
          * JTextArea of the cell editor. Rather tricky, but it works.
          */
         model_ = new DistributedTableModel(dataSource_) {

            /** Override isCellEditable */
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
               return true;
            }
         };
      } catch (Exception e) {
         e.printStackTrace();
      }
      table_.setModel(model_);

      scrollPane_ = new javax.swing.JScrollPane();
      scrollPane_.setViewportView(table_);


      /* Change the column header so that the text is in bold */

      JTableHeader header = table_.getTableHeader();
      final Font boldFont = header.getFont().deriveFont(Font.BOLD);
      final TableCellRenderer headerRenderer = header.getDefaultRenderer();
      header.setDefaultRenderer(new TableCellRenderer() {

         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp =
                    headerRenderer.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            comp.setFont(boldFont);
            return comp;
         }
      });

      /* Set the cell editor we will use */
      TextAreaEditor cellEditor = new TextAreaEditor();
      cellEditor.setLineWrap(true);
      cellEditor.setWrapStyleWord(true);
      cellEditor.setEditable(false);

      /* Set the cell renderer we will use */
      TextAreaRenderer cellRenderer = new TextAreaRenderer();
      cellRenderer.setLineWrap(true);
      cellRenderer.setWrapStyleWord(true);

      /* Create the columns with our cell renderer and editor */
      for (int i = 0; i < model_.getColumnCount(); i++) {
         int w = (i == 0) ? 100 : 150;
         TableColumn column = new TableColumn(i, w, cellRenderer, cellEditor);
         table_.addColumn(column);
      }

      GuiUtils.TweakJTable(table_);
      table_.setRowHeight(table_.getRowHeight() * 4);

      initRowHeader();
   }
   private void initRowHeader() {

      // if not on 1.6 comment this out
      table_.setFillsViewportHeight(true);

      /* Create a JTable for the row header */
      nonScrollingColumns_ = new JTable();

      /* Tweak the presentation as for the body table */
      GuiUtils.TweakJTable(nonScrollingColumns_);
      nonScrollingColumns_.setRowHeight(nonScrollingColumns_.getRowHeight() * 4);

      nonScrollingColumns_.setAutoCreateColumnsFromModel(false);
      nonScrollingColumns_.setModel(table_.getModel());

      nonScrollingColumns_.setSelectionModel(table_.getSelectionModel());
      nonScrollingColumns_.setFillsViewportHeight(true);

      JTableHeader nonScrollingHeader = nonScrollingColumns_.getTableHeader();
      nonScrollingHeader.setResizingAllowed(false);
      nonScrollingHeader.setReorderingAllowed(false);

      TableColumnModel tcm = table_.getColumnModel();

      TableColumn firstColumn = tcm.getColumn(0);
      table_.removeColumn(firstColumn);

      nonScrollingColumns_.addColumn(firstColumn);
      nonScrollingColumns_.setPreferredScrollableViewportSize(nonScrollingColumns_.getPreferredSize());

      nonScrollingColumns_.setBackground(table_.getTableHeader().getBackground());
      nonScrollingColumns_.setForeground(table_.getTableHeader().getForeground());
      //keyboard navigation in rowHeader
      table_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

         public void valueChanged(ListSelectionEvent event) {
            if (!event.getValueIsAdjusting()) {
               ListSelectionModel selection = (ListSelectionModel) event.getSource();
               int rowSelected = selection.getMinSelectionIndex();
               if (rowSelected >= 0) {
                  int colSelected = table_.getSelectedColumn();
                  if (colSelected < 0) {
                     colSelected = 0;
                  }
                  Rectangle rect = table_.getCellRect(rowSelected, colSelected, false);
                  table_.scrollRectToVisible(rect);
               }
            }
         }
      });
      scrollPane_.setRowHeaderView(nonScrollingColumns_);
      scrollPane_.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, nonScrollingHeader);

   }
   public void setIndexMap(long indexes[]) {

      dataSource_.setIndexMap(indexes);
      model_.clearCache();
      model_.fireTableDataChanged();

      table_.setModel(model_);
      table_.changeSelection(0, 0, false, false);
      //table_.updateUI();
      //nonScrollingColumns_.updateUI();

   }


}
