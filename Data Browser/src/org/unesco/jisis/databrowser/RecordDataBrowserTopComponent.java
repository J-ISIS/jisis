/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.databrowser;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RepaintManager;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DefaultDBNotFoundException;
import org.unesco.jisis.gui.GuiUtils;
import org.unesco.jisis.gui.MultiSortTableCellHeaderRenderer;
import org.unesco.jisis.gui.RecordTableDataSource;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;
import org.unesco.jisis.jisisutils.distributed.DistributedTableModel;
import org.unesco.jisis.jisisutils.gui.TextAreaEditor;
import org.unesco.jisis.jisisutils.gui.TextAreaRenderer;
import org.unesco.jisis.jisisutils.gui.TextPaneEditorEx;
import org.unesco.jisis.jisisutils.gui.TextPaneRenderer;

/**
 * Top component which displays something.
 */
public class RecordDataBrowserTopComponent extends TopComponent implements Observer {

   private static RecordDataBrowserTopComponent instance;
   /** path to the icon used by the component and its open action */
   static final String ICON_PATH = "org/unesco/jisis/databrowser/Table.png";
   private static final String PREFERRED_ID = "RecordDataBrowserTopComponent";
   private ClientDatabaseProxy db_;
   private RecordTableDataSource dataSource_;
   private DistributedTableModel model_;
   private JTable nonScrollingColumns_;
   private ComponentOrientation orientation_ = ComponentOrientation.LEFT_TO_RIGHT;




   public RecordDataBrowserTopComponent(IDatabase db) {

      if (db instanceof ClientDatabaseProxy) {
         db_ = (ClientDatabaseProxy) db;
      } else {
         throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
      }
      /* Register this TopComponent as attached to this DB */
      db_.addWindow(this);

      /* Add this TopComponent as Observer to DB changes */
      db_.addObserver((Observer) this);
      /* Add this TopComponent as Observer to HitSortFile change */
      GuiGlobal.addHitSortFileObserver((Observer) this);

      GuiGlobal.setEnabledHitSortFileComponent(true);
      try {
         long recCount = db_.getRecordsCount();
         long maxMFN = db_.getLastMfn();
         System.out.println("RecordDataBrowserTopComponent recCount=" + recCount +
                 " maxMFN=" + maxMFN);

         /* Display the db name on the tab index */
         this.setDisplayName("DB Browser" + " (" +db.getDbHome()+"//"+ db.getDatabaseName() + ")");
         /* Create the Table model for this DB, overriding the isCellEditable
          * method so that we can edit the cell for showing the scroll bar.
          * Editing is later-on disabled by calling setEditable(false) on the
          * JTextArea of the cell editor. Rather tricky, but it works.
          */
         dataSource_ = new RecordTableDataSource(db);
//          long indexes[]= { 25L,20L,15L,10L,5L };
//          dataSource_.setIndexMap(indexes);
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

      initComponents();
      setName(NbBundle.getMessage(RecordDataBrowserTopComponent.class, "CTL_RecordDataBrowserTopComponent"));

      setToolTipText(NbBundle.getMessage(RecordDataBrowserTopComponent.class, "HINT_RecordDataBrowserTopComponent"));

      initTable();
      putClientProperty("print.printable", Boolean.TRUE); // NOI18N


   //        setIcon(Utilities.loadImage(ICON_PATH, true));
   }


   private void initTable() {
      /** Initialize the JTable main body component */
      /* Don't let the system create the column for us */
      table_.setAutoCreateColumnsFromModel(false);
      /* allow column selection */
      table_.setColumnSelectionAllowed(true);

      /* Change the column header so that the text is in bold */

      JTableHeader header = table_.getTableHeader();

      final Font boldFont = header.getFont().deriveFont(Font.BOLD);
      final TableCellRenderer headerRenderer = header.getDefaultRenderer();
      header.setDefaultRenderer(new MultiSortTableCellHeaderRenderer() {

         @Override
         public Component getTableCellRendererComponent(JTable table, Object value,
                 boolean isSelected, boolean hasFocus, int row, int column) {
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
      if (db_.getDisplayFont()!=null) {
             cellRenderer.setFont(db_.getDisplayFont());
      }

      TextPaneEditorEx paneCellEditor = new TextPaneEditorEx();


      /* Set the cell renderer we will use */
      TextPaneRenderer paneCellRenderer = new TextPaneRenderer();
      if (db_.getDisplayFont()!=null) {
             paneCellRenderer.setFont(db_.getDisplayFont());
      }


      /* Create the columns with our cell renderer and editor */
      TableColumn column = null;
      for (int i = 0; i < model_.getColumnCount(); i++) {
         int w = (i == 0) ? 100 : 150;
         if (model_.getColumnClass(i).equals(Object.class)) {
            column = new TableColumn(i, w, paneCellRenderer, paneCellEditor);
         } else {
            column = new TableColumn(i, w, cellRenderer, cellEditor);
         }
         table_.addColumn(column);
      }

      GuiUtils.TweakJTable(table_);
      table_.setRowHeight(table_.getRowHeight() * 4);

      initRowHeader();

//      RowSorter sorter = new TableRowSorter(model_);
//      table_.setRowSorter(sorter);
   }

   private void initETable() {
//        /** Initialize the JTable main body component */
//       /* Don't let the system create the column for us */
//       etable_.setAutoCreateColumnsFromModel(false);
//       /* allow column selection */
//       etable_.setColumnSelectionAllowed(true);
//
//       /* Change the column header so that the text is in bold */
//
//       JTableHeader header = etable_.getTableHeader();
//       final Font boldFont = header.getFont().deriveFont(Font.BOLD);
//       final TableCellRenderer headerRenderer = header.getDefaultRenderer();
//       header.setDefaultRenderer( new TableCellRenderer() {
//	 public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
//	   Component comp =
//                   headerRenderer.getTableCellRendererComponent( table, value,
//                   isSelected, hasFocus, row, column );
//
//	   comp.setFont( boldFont );
//	   return comp;
//          }
//	});
//
//        /* Set the cell editor we will use */
//        TextAreaEditor cellEditor = new TextAreaEditor();
//        cellEditor.setLineWrap(true);
//        cellEditor.setWrapStyleWord(true);
//        cellEditor.setEditable(false);
//
//        /* Set the cell renderer we will use */
//        TextAreaRenderer cellRenderer = new TextAreaRenderer();
//        cellRenderer.setLineWrap(true);
//        cellRenderer.setWrapStyleWord(true);
//
//        /* Create the columns with our cell renderer and editor */
//        for (int i=0; i<model_.getColumnCount(); i++) {
//           int w = (i==0) ? 100 : 150;
//           ETableColumn column = new ETableColumn(i, w, cellRenderer, cellEditor,etable_);
//           etable_.addColumn(column);
//        }
//        GuiUtils.TweakJTable(etable_);
//        etable_.setRowHeight(etable_.getRowHeight() * 4);
//
//        initRowHeaderE();
   }

   private void initRowHeaderE() {
//       // if not on 1.6 comment this out
//      etable_.setFillsViewportHeight(true);
//
//      /* Create a JTable for the row header */
//      nonScrollingColumns_ = new JTable();
//
//      /* Tweak the presentation as for the body table */
//      GuiUtils.TweakJTable(nonScrollingColumns_);
//      nonScrollingColumns_.setRowHeight(nonScrollingColumns_.getRowHeight() * 4);
//
//      nonScrollingColumns_.setAutoCreateColumnsFromModel(false);
//      nonScrollingColumns_.setModel(etable_.getModel());
//
//      nonScrollingColumns_.setSelectionModel(etable_.getSelectionModel());
//      nonScrollingColumns_.setFillsViewportHeight(true);
//
//      JTableHeader nonScrollingHeader = nonScrollingColumns_.getTableHeader();
//      nonScrollingHeader.setResizingAllowed(false);
//      nonScrollingHeader.setReorderingAllowed(false);
//
//      TableColumnModel tcm = etable_.getColumnModel();
//
//      TableColumn firstColumn = tcm.getColumn(0);
//      etable_.removeColumn(firstColumn);
//
//      nonScrollingColumns_.addColumn(firstColumn);
//      nonScrollingColumns_.setPreferredScrollableViewportSize(nonScrollingColumns_.getPreferredSize());
//
//       nonScrollingColumns_.setBackground(etable_.getTableHeader().getBackground());
//       nonScrollingColumns_.setForeground(etable_.getTableHeader().getForeground());
//       //keyboard navigation in rowHeader
//        etable_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//             public void valueChanged(ListSelectionEvent event) {
//                if (!event.getValueIsAdjusting()) {
//                   ListSelectionModel selection = (ListSelectionModel)event.getSource();
//                   int rowSelected = selection.getMinSelectionIndex();
//                   if (rowSelected >= 0) {
//                      int colSelected = etable_.getSelectedColumn();
//                      if (colSelected < 0) colSelected = 0;
//                      Rectangle rect = etable_.getCellRect(rowSelected, colSelected, false);
//                      etable_.scrollRectToVisible(rect);
//                   }
//                }
//             }
//        });
//      scrollPane_.setRowHeaderView(nonScrollingColumns_);
//      scrollPane_.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, nonScrollingHeader);
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


    /**
     * Overriden to implement CTRL-+ for resizing of all columns,
     * CTRL-- for clearing the quick filter and CTRL-* for invoking the
     * column selection dialog.
     * @see javax.swing.JTable#processKeyBinding(KeyStroke, KeyEvent, int, boolean)
     */
   @Override
   protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
           int condition, boolean pressed) {
      // This is here because the standard way using input map and action map
      // did not work since the event was "eaten" by the code in JTable that
      // forwards it to the CellEditor (the code resides in the
      // super.processKeyBinding method).
      
      if (pressed) {

         int asterisk = '*';
         int c = e.getKeyChar();
         //System.out.println("asterisk="+asterisk+"c="+c);
         if (e.getKeyChar() == asterisk && ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)) {
            //showColumnSelectionDialog();
            setOrientation();
            e.consume();
            return true;
         }
      }
      boolean retValue = super.processKeyBinding(ks, e, condition, pressed);
      return retValue;
   }

   private void showColumnSelectionDialog() {
      String[] columnNames = null;
      try {
         columnNames = dataSource_.getTableDescription().getColumnNames();
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
           RtlSelectDlg dlg = new RtlSelectDlg(columnNames, new javax.swing.JFrame(), true);
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
            if (!dlg.succeeded()) {
               return;
            }
    }

   private void setOrientation() {

      orientation_ = (orientation_ == ComponentOrientation.LEFT_TO_RIGHT) ? ComponentOrientation.RIGHT_TO_LEFT
              : ComponentOrientation.LEFT_TO_RIGHT;
      for (int i = 0; i < table_.getColumnCount(); i++) {
         TableColumn tc = table_.getColumnModel().getColumn(i);
         if (model_.getColumnClass(i).equals(Object.class)) {
            TextPaneRenderer paneCellRenderer = (TextPaneRenderer) tc.getCellRenderer();
            paneCellRenderer.setComponentOrientation(orientation_);
            paneCellRenderer.updateUI();

         } else {
            TextAreaRenderer cellRenderer = (TextAreaRenderer) tc.getCellRenderer();
            cellRenderer.applyComponentOrientation(orientation_);
            cellRenderer.updateUI();
          }
      }
      this.applyComponentOrientation(orientation_);
      //table_.applyComponentOrientation(orientation_);

      //model_.fireTableStructureChanged();
//       table_.updateUI();
//       nonScrollingColumns_.updateUI();
//       table_.getTableHeader().updateUI();
//       nonScrollingColumns_.getTableHeader().updateUI();


   }
   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jScrollPane1 = new javax.swing.JScrollPane();
      jTextPane1 = new javax.swing.JTextPane();
      recordBrowserTabbedPane_ = new javax.swing.JTabbedPane();
      browserPanel_ = new javax.swing.JPanel();
      scrollPane_ = new javax.swing.JScrollPane();
      table_ = new javax.swing.JTable();
      searchPanel_ = new javax.swing.JPanel();
      searchLabel_ = new javax.swing.JLabel();
      searchScrollPane_ = new javax.swing.JScrollPane();
      searchTextPane_ = new javax.swing.JTextPane();

      jScrollPane1.setViewportView(jTextPane1);

      browserPanel_.setLayout(new java.awt.BorderLayout());

      table_.setAutoCreateColumnsFromModel(false);
      table_.setModel(model_);
      table_.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
      scrollPane_.setViewportView(table_);

      browserPanel_.add(scrollPane_, java.awt.BorderLayout.CENTER);

      recordBrowserTabbedPane_.addTab("Browser", browserPanel_);

      searchPanel_.setLayout(new java.awt.BorderLayout());

      org.openide.awt.Mnemonics.setLocalizedText(searchLabel_, "Search Expression:"); // NOI18N
      searchPanel_.add(searchLabel_, java.awt.BorderLayout.PAGE_START);

      searchScrollPane_.setViewportView(searchTextPane_);

      searchPanel_.add(searchScrollPane_, java.awt.BorderLayout.CENTER);

      recordBrowserTabbedPane_.addTab("Search", searchPanel_);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(recordBrowserTabbedPane_, javax.swing.GroupLayout.DEFAULT_SIZE, 693, Short.MAX_VALUE)
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addComponent(recordBrowserTabbedPane_, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JPanel browserPanel_;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JTextPane jTextPane1;
   private javax.swing.JTabbedPane recordBrowserTabbedPane_;
   private javax.swing.JScrollPane scrollPane_;
   private javax.swing.JLabel searchLabel_;
   private javax.swing.JPanel searchPanel_;
   private javax.swing.JScrollPane searchScrollPane_;
   private javax.swing.JTextPane searchTextPane_;
   private javax.swing.JTable table_;
   // End of variables declaration//GEN-END:variables

   /**
    * Gets default instance. Do not use directly: reserved for *.settings files only,
    * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
    * To obtain the singleton instance, use {@link findInstance}.
    */
   public static synchronized RecordDataBrowserTopComponent getDefault() {
      try {
         if (instance != null) {
            instance.close();
            instance = null;
         }
         
         ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();

         if (connectionInfo.getDefaultDatabase() != null && instance == null) {
            instance = new RecordDataBrowserTopComponent(connectionInfo.getDefaultDatabase());
         }

      } catch (Exception ex) {
         //Exceptions.printStackTrace(ex);
         // Do nothing
      }
      return instance;
   }

   /**
    * Obtain the RecordDataBrowserTopComponent instance. Never call {@link #getDefault} directly!
    */
   public static synchronized RecordDataBrowserTopComponent findInstance() throws DefaultDBNotFoundException {
      TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
      if (win == null) {
         Logger.getLogger(RecordDataBrowserTopComponent.class.getName()).warning(
                 "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
         return getDefault();
      }
      if (win instanceof RecordDataBrowserTopComponent) {
         return (RecordDataBrowserTopComponent) win;
      }
      Logger.getLogger(RecordDataBrowserTopComponent.class.getName()).warning(
              "There seem to be multiple components with the '" + PREFERRED_ID +
              "' ID. That is a potential source of errors and unexpected behavior.");
      return getDefault();
   }

   @Override
   public int getPersistenceType() {
      return TopComponent.PERSISTENCE_NEVER;
   }

   @Override
   public void componentOpened() {
      // TODO add custom code on component opening
   }

   @Override
   public void componentActivated() {
      super.componentActivated();
      table_.updateUI();
      GuiGlobal.setEnabledHitSortFileComponent(true);

   }

   @Override
   protected void componentDeactivated() {
      super.componentDeactivated();
      GuiGlobal.setEnabledHitSortFileComponent(false);
   }

   @Override
   public void componentClosed() {
      db_.deleteWindow(this);
      db_.deleteObserver((Observer) this);
      /* Delete this TopComponent as Observer to HitSortFile change */
      GuiGlobal.deleteHitSortFileObserver((Observer) this);
      GuiGlobal.setEnabledHitSortFileComponent(false);
   }

   /** replaces this in object stream */
   @Override
   public Object writeReplace() {
      return new ResolvableHelper();
   }

   @Override
   protected String preferredID() {
      return PREFERRED_ID;
   }

   final static class ResolvableHelper implements Serializable {

      private static final long serialVersionUID = 1L;

      public Object readResolve() throws DefaultDBNotFoundException {
         return RecordDataBrowserTopComponent.getDefault();
      }
   }

   /** We are observer for the database changes */
   public void update(Observable o, Object arg) {
      if (arg != null) {
         String s = (String) arg;
         if (s.equals("HitSortFile")) {
            showBusyCursor(true);
            setIndexMap(loadHitSortFile());
            showBusyCursor(false);
         }
         return;
      }
      if (db_.databaseHasChanged()) {
         model_.clearCache();
         model_.fireTableDataChanged();
         /* Have to reset the model otherwise it was not working
          * it may come from ETable and maybe not necessary
          * for JTable
          */
         table_.setModel(model_);
         table_.updateUI();
         nonScrollingColumns_.updateUI();
//      String msg= "Database was changed !";
//      DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));

      }
   }

   synchronized private long[] loadHitSortFile() {
      long[] index = null;
      String fileName = GuiGlobal.getHitSortFileName();
      if (fileName.equals("<none>")) {
         return index;
      }

      try {
         String fullFilepath = Global.getClientWorkPath() + File.separator + fileName + Global.HIT_SORT_FILE_EXT;
         BufferedReader in = null;
         in = new BufferedReader(new InputStreamReader(new FileInputStream(fullFilepath), "UTF8"));
         String s = null;
         int n = (int) db_.getRecordsCount();
         index = new long[n];
         int i = 0;
         while ((s = in.readLine()) != null) {
            long mfn = Long.parseLong(s.substring(0, 9));
            //System.out.println("mfn="+mfn);
            index[i] = mfn;
            i++;
         }
         in.close();
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
      return index;

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

   /**
    * Showing/hiding busy cursor, before this funcionality was in Rave winsys,
    * the code is copied from that module.
    * It needs to be called from event-dispatching thread to work synch,
    * otherwise it is scheduled into that thread. */
   static void showBusyCursor(final boolean busy) {
      if (SwingUtilities.isEventDispatchThread()) {
         doShowBusyCursor(busy);
      } else {
         SwingUtilities.invokeLater(new Runnable() {

            public void run() {
               doShowBusyCursor(busy);
            }
         });
      }
   }

   private static void doShowBusyCursor(boolean busy) {
      JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
      if (busy) {
         RepaintManager.currentManager(mainWindow).paintDirtyRegions();
         mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         mainWindow.getGlassPane().setVisible(true);
         mainWindow.repaint();
      } else {
         mainWindow.getGlassPane().setVisible(false);
         mainWindow.getGlassPane().setCursor(null);
         mainWindow.repaint();
      }
   }
}
