/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Z3950ClientPanel.java
 *
 * Created on 26 oct. 2010, 18:33:16
 */
package org.unesco.jisis.z3950;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.jisiscore.client.GuiGlobal;

/**
 *
 * @author jcd
 */
public class Z3950ClientPanel extends javax.swing.JPanel implements ItemListener, ListSelectionListener, ActionListener {

   XMLUtility xmlUtility = null;
   Utilities utilities = null;
   DefaultTableModel serverTableModel = null, resultTableModel = null;
   javax.swing.JDialog dialogInstance = null;
   java.awt.Frame frame = new java.awt.Frame();
   ArrayList categoryVect = new ArrayList();
   ArrayList locationVect = new ArrayList();
   javax.swing.Timer timer = null;
   private String currServer = "";
   private int currrow = 0;
    private ArrayList<ZSwingWorker> tasks;
//    public static int presentCount=1;
//    public static int completeCount=1;
   private int renderingReportStatus = 0;
   int min = 1, max = 100;
   int col = 1;
   int sno = 1;
   int marcCount = 0, marcCountImpl = 0;
   String chek = "";
   private RequestProcessor.Task task_ = null;
   private CancellableProgress cancellable_;
   public static final int EXPORT_ISO2709 = 0;
   public static final int EXPORT_XML     = 1;
   public static final int EXPORT_MARCXML = 2;
   public static final int EXPORT_TEXT    = 3;
   public static final int EXPORT_DATABASE = 4;
   
   /**
     * Creates a new named RequestProcessor with defined throughput which can support interruption of the 
     * thread the processor runs in.
     * public RequestProcessor(String name,
     *           int throughput,
     *           boolean interruptThread)
     * 
     * Parameters:
     * name - the name to use for the request processor thread
     * throughput - the maximal count of requests allowed to run in parallel
     * interruptThread - true if RequestProcessor.Task.cancel() shall interrupt the thread
     */ 
    private final static RequestProcessor requestProcessor_ = new RequestProcessor("interruptible tasks", 1, true);


   /** Creates new form Z3950ClientPanel */
   public Z3950ClientPanel() {
      initComponents();

      // I have not been able to add a JMenuBar in a panel with the GUI builder
      // Thus this done manually
      JMenuBar menuBar = new JMenuBar();
      JMenu menuExport = new JMenu("Export");
      JMenuItem iso2709MenuItem = new JMenuItem("ISO2709");
      ActionListener exportIso2709Listener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            List<Integer> selRecords = getSelectedRecords();
            if (selRecords.size() <= 0) {
               return;
            }
            export(selRecords, EXPORT_ISO2709);
         }
      };
      iso2709MenuItem.addActionListener(exportIso2709Listener);
      menuExport.add(iso2709MenuItem);
      JMenuItem xmlMenuItem = new JMenuItem("XML");
       ActionListener exportXmlListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            List<Integer> selRecords = getSelectedRecords();
            if (selRecords.size() <= 0) {
               return;
            }
            export(selRecords, EXPORT_XML);
         }
      };
      xmlMenuItem.addActionListener(exportXmlListener);
      menuExport.add(xmlMenuItem);

      JMenuItem marcxmlMenuItem = new JMenuItem("MarcXML");
       ActionListener exportMarcxmlListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            List<Integer> selRecords = getSelectedRecords();
            if (selRecords.size() <= 0) {
               return;
            }
            export(selRecords, EXPORT_MARCXML);
         }
      };
      marcxmlMenuItem.addActionListener(exportMarcxmlListener);
      menuExport.add(marcxmlMenuItem);

      JMenuItem textMenuItem = new JMenuItem("TEXT");
       ActionListener exportTextListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            List<Integer> selRecords = getSelectedRecords();
            if (selRecords.size() <= 0) {
               return;
            }
            export(selRecords, EXPORT_TEXT);
         }
      };
      textMenuItem.addActionListener(exportTextListener);
      menuExport.add(textMenuItem);
      JMenuItem dbMenuItem = new JMenuItem("Database");
       ActionListener exportDatabaseListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            List<Integer> selRecords = getSelectedRecords();
            if (selRecords.size() <= 0) {
               return;
            }
            export(selRecords, EXPORT_DATABASE);
         }
      };
      dbMenuItem.addActionListener(exportDatabaseListener);
      menuExport.add(dbMenuItem);

      menuBar.add(menuExport);
      controlPanel.add(Box.createRigidArea(new Dimension(150, 0)));

      menuBar.setAlignmentX(Component.CENTER_ALIGNMENT);
      // To be at the same level than the toolbar
      menuBar.setAlignmentY(0.47826087f);
      controlPanel.add(menuBar, Box.CENTER_ALIGNMENT);

      xmlUtility = XMLUtility.getInstance();
      utilities = Utilities.getInstance();
      setTables();
      applyKeyStrokesToTable();
      getServersDetails();
      setAttributes();

//        bnStart.setEnabled(false);
//        bnStop.setEnabled(false);
//        bnMore.setEnabled(false);



      restoreServersSelected();
//         this.getRootPane().setDefaultButton(bnSearch);

      progress.setStringPainted(true);
      progress.setString("");
      progress.setMinimum(min);
      progress.setMaximum(max);
      timer = new javax.swing.Timer(100, new java.awt.event.ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {

            progress.setIndeterminate(true);
            progress.setString("Searching....");
//                if(col%2==0) {
//                    progress.setForeground(Color.GRAY);
//                    progress.setValue(100);
//                } else {
//                    progress.setForeground(Color.LIGHT_GRAY);
//                    progress.setValue(100);
//                }
//                col++;
         }
      });

   }
   private List<Integer> getSelectedRecords() {
      List<Integer> selRecords = new ArrayList<Integer>();
      for (int i=0; i<resultTable.getRowCount(); i++) {
         if (((Boolean)resultTable.getValueAt(i, 0))) {
            selRecords.add(i);
         }
      }

      return selRecords;
   }
   private void export(final List<Integer> selRecords, final int export) {
      SaveOnFileDlg dialog = new SaveOnFileDlg(new javax.swing.JFrame(), true);
      switch (export) {
         case EXPORT_ISO2709:
            dialog.setTitle(NbBundle.getMessage(Z3950ClientPanel.class, "MSG_EXPORT_ISO2709"));
            break;
         case EXPORT_XML:
            dialog.setTitle(NbBundle.getMessage(Z3950ClientPanel.class, "MSG_EXPORT_XML"));
            break;
         case EXPORT_MARCXML:
            dialog.setTitle(NbBundle.getMessage(Z3950ClientPanel.class, "MSG_EXPORT_MARCXML"));
            break;
         case EXPORT_TEXT:
            dialog.setTitle(NbBundle.getMessage(Z3950ClientPanel.class, "MSG_EXPORT_TEXT"));
            break;
         case EXPORT_DATABASE:
            String errorMsg = NbBundle.getMessage(Z3950ClientPanel.class, "MSG_NotYetImplemented");
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
            return;
      }
     
      dialog.setLocationRelativeTo(null);
      dialog.setVisible(true);
      if (dialog.getReturnStatus() == SaveOnFileDlg.RET_OK) {

         try {
            final String fileName = dialog.getSelectedFile(export);
            Runnable exportRun = new Runnable() {
               @Override
               public void run() {
                  if (!EventQueue.isDispatchThread()) {
                     try {
                        Date start = new Date();
                        switch (export) {
                           case EXPORT_ISO2709:
                              doExportIso2709(selRecords, fileName);
                              break;
                           case EXPORT_XML:
                              doExportXml(selRecords, fileName);
                              break;
                           case EXPORT_MARCXML:
                              doExportMarcxml(selRecords, fileName);
                              break;
                          case EXPORT_TEXT:
                              doExportXml(selRecords, fileName);
                              break;
                          case EXPORT_DATABASE:
                              throw new UnsupportedOperationException("Not yet implemented");
                              
                        }
                        
                        Date end = new Date();
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to export records");

                     } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                     } finally {
                        EventQueue.invokeLater(this);
                        NotifyDescriptor d =
                                new NotifyDescriptor.Message(NbBundle.getMessage(Z3950ClientPanel.class,
                                "MSG_EXPORT_DONE"));
                        DialogDisplayer.getDefault().notify(d);
                     }
                     // Second Invocation, we are on the event queue now
                  }
               }
            };
            task_ = requestProcessor_.post(exportRun);
         } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
         }
      }
   }

    static class CancellableProgress implements Cancellable {

      private boolean cancelled = false;

      public boolean cancel() {
         cancelled = true;
         return true;
      }

      public boolean cancelRequested() {
         return cancelled;
      }
   }
    private void doExportIso2709(List<Integer> selRecords, String fileName) {

      if (selRecords == null || selRecords.isEmpty()) {
         return;
      }
      cancellable_ = new CancellableProgress();
      final ProgressHandle progress1 = ProgressHandleFactory.createHandle("Exporting Data...",
              cancellable_);
      progress1.start();
      progress1.switchToIndeterminate();
      OutputStream output = null;
      MarcWriter writer = null;
      try {
         Date start = new Date();
         output = new FileOutputStream(fileName);
         writer = new MarcStreamWriter(output, "UTF8");
         for (Integer i : selRecords) {
            if (cancellable_.cancelRequested()) {
               progress1.finish();
               break;
            }
            org.marc4j.marc.Record marc4jRecord = (Record) resultTable.getValueAt(i, 6);
            writer.write(marc4jRecord);
            progress1.setDisplayName("Exporting Sno:" + i);
         }
         writer.close();
      } catch (Exception e) {
         System.err.println("Error writing to file");
         Exceptions.printStackTrace(e);
      } finally {
         
         progress1.finish();
      }
   }
    
     private void doExportXml(List<Integer> selRecords, String fileName) {

      if (selRecords == null || selRecords.isEmpty()) {
         return;
      }
      cancellable_ = new CancellableProgress();
      final ProgressHandle progress1 = ProgressHandleFactory.createHandle("Exporting Data...",
              cancellable_);
      progress1.start();
      progress1.switchToIndeterminate();
      OutputStream output = null;
      DataOutputStream dataOut = null;
     
      try {
         Date start = new Date();
         output = new FileOutputStream(fileName);
         BufferedOutputStream buffer = new BufferedOutputStream(output);
         dataOut = new DataOutputStream(buffer);
        
         for (Integer i : selRecords) {
            if (cancellable_.cancelRequested()) {
               progress1.finish();
               break;
            }
            org.marc4j.marc.Record marc4jRecord = (Record) resultTable.getValueAt(i, 6);
            dataOut.writeUTF(marc4jRecord.toString());
           
            progress1.setDisplayName("Exporting Sno:" + i);
         }
         dataOut.close();
      } catch (Exception e) {
         System.err.println("Error writing to file");
         Exceptions.printStackTrace(e);
      } finally {
         if (dataOut != null) {
            try {
               dataOut.close();
            } catch (IOException ex) {
               Exceptions.printStackTrace(ex);
            }
         }
         progress1.finish();
      }
   }

     private void doExportMarcxml(List<Integer> selRecords, String fileName) {

      if (selRecords == null || selRecords.isEmpty()) {
         return;
      }
      cancellable_ = new CancellableProgress();
      final ProgressHandle progress1 = ProgressHandleFactory.createHandle("Exporting Data...",
              cancellable_);
      progress1.start();
      progress1.switchToIndeterminate();
      OutputStream output = null;
      MarcXmlWriter writer = null;
      try {
         Date start = new Date();
         output = new FileOutputStream(fileName);
         writer = new MarcXmlWriter(output, "UTF8", true);
         for (Integer i : selRecords) {
            if (cancellable_.cancelRequested()) {
               progress1.finish();
               break;
            }
            org.marc4j.marc.Record marc4jRecord = (Record) resultTable.getValueAt(i, 6);
            writer.write(marc4jRecord);
            progress1.setDisplayName("Exporting Sno:" + i);
         }
         writer.close();
      } catch (Exception e) {
         System.err.println("Error writing to file");
         Exceptions.printStackTrace(e);
      } finally {
         
         progress1.finish();
      }
   }


 

   private void exportDatabase(List<Integer> selRecords) {
      SaveOnFileDlg dialog = new SaveOnFileDlg(new javax.swing.JFrame(), true);
      dialog.setTitle(NbBundle.getMessage(Z3950ClientPanel.class, "MSG_EXPORT_DATABASE"));
      dialog.setLocationRelativeTo(null);
      dialog.setVisible(true);

   }
   
   static final public String   serverColumnNames[] = {
        " ",
        NbBundle.getMessage(Z3950ClientPanel.class, "MSG_Name"),
        NbBundle.getMessage(Z3950ClientPanel.class, "MSG_Status"),
        NbBundle.getMessage(Z3950ClientPanel.class, "MSG_XML"),
        NbBundle.getMessage(Z3950ClientPanel.class, "MSG_Records"),

    };
    static final public String resultColumnNames[] = {
          " ",
         NbBundle.getMessage(Z3950ClientPanel.class, "MSG_SNo"),
         NbBundle.getMessage(Z3950ClientPanel.class, "MSG_Main_Entry"),
         NbBundle.getMessage(Z3950ClientPanel.class, "MSG_Title"),
         NbBundle.getMessage(Z3950ClientPanel.class, "MSG_Hash"),
         NbBundle.getMessage(Z3950ClientPanel.class, "MSG_Group"),
         NbBundle.getMessage(Z3950ClientPanel.class, "MSG_RecordDetails")
      };

   private void setTables() {

      /**----------------------------------------------------------------
       * Define The server Table
       * ----------------------------------------------------------------
       */
      serverTableModel = new DefaultTableModel(serverColumnNames, 0) {
         @Override
         public boolean isCellEditable(int r, int c) {
            if (c == 0) {
               return true;
            } else {
               return false;
            }
         }
         @Override
         public Class getColumnClass(int column) {
            return getValueAt(0, column).getClass();
         }
      };
      serverTable.setModel(serverTableModel);
      ListSelectionModel listMod = serverTable.getSelectionModel();
      listMod.addListSelectionListener(this);
      serverTable.getColumnModel().getColumn(0).setMinWidth(50);
      serverTable.getColumnModel().getColumn(0).setMaxWidth(50);

      TableColumn tc = serverTable.getColumnModel().getColumn(0);
      tc.setHeaderRenderer(new CheckBoxHeader(new ServerTableItemListener()));

      serverTable.getColumnModel().getColumn(1).setMinWidth(50);
      serverTable.getColumnModel().getColumn(1).setPreferredWidth(100);

      serverTable.getColumnModel().getColumn(2).setMinWidth(50);
      serverTable.getColumnModel().getColumn(2).setPreferredWidth(100);

      serverTable.getColumnModel().getColumn(3).setMinWidth(0);
      serverTable.getColumnModel().getColumn(3).setMaxWidth(0);

      serverTable.getColumnModel().getColumn(4).setMinWidth(0);
      serverTable.getColumnModel().getColumn(4).setMaxWidth(0);

//        serverTable.setAutoCreateRowSorter(true);
      serverTable.getTableHeader().setReorderingAllowed(false);
      serverTable.addMouseListener(new MouseAdapter() {

         @Override
         public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
               JTable source = (JTable) e.getSource();
               int row = source.rowAtPoint(e.getPoint());
               int column = source.columnAtPoint(e.getPoint());

               if (!source.isRowSelected(row)) {
                  source.changeSelection(row, column, false, false);
               }
               serversPopup.show(e.getComponent(), e.getX(), e.getY());
            }
         }
      });

      String[] serverFunctions = {
         "New",
         "Edit",
         "Delete",
         "MoveUp",
         "MoveDown"
      };
      serversPopup.removeAll();
      JMenuItem item = null;
      for (int i = 0; i < serverFunctions.length; i++) {
         if (serverFunctions[i].equalsIgnoreCase("New")) {
            item = serversPopup.add(serverFunctions[i]);
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_MASK));
            item.addActionListener(this);
         }
         else if(serverFunctions[i].equalsIgnoreCase("Edit")) {
            item = serversPopup.add(serverFunctions[i]);
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_MASK));
            item.addActionListener(this);
         }
         else if(serverFunctions[i].equalsIgnoreCase("Delete")) {
            item = serversPopup.add(serverFunctions[i]);
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            item.addActionListener(this);
         }
         else if(serverFunctions[i].equalsIgnoreCase("MoveUp")) {
            item = serversPopup.add(serverFunctions[i]);
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK));
            item.addActionListener(this);
         }
         else if(serverFunctions[i].equalsIgnoreCase("MoveDown")) {
            item = serversPopup.add(serverFunctions[i]);
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK));
            item.addActionListener(this);
         }
      }
      serversPopup.setSize(200, 500);
//        serverTable.setAutoCreateRowSorter(true);
      serverTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      

      resultTableModel = new DefaultTableModel(resultColumnNames, 0) {

         @Override
          public boolean isCellEditable(int r, int c) {
            if (c == 0) { // Checkbox
               return true;
            } else {
               return false;
            }
         }

         @Override
         public Class getColumnClass(int column) {
            return getValueAt(0, column).getClass();
         }
      };
      resultTable.setModel(resultTableModel);
      // Checkbox for selection
      resultTable.getColumnModel().getColumn(0).setMinWidth(50);
      resultTable.getColumnModel().getColumn(0).setMaxWidth(50);
      TableColumn rtc = resultTable.getColumnModel().getColumn(0);
      rtc.setHeaderRenderer(new CheckBoxHeader(new ResultTableItemListener()));
      // Retrieved number
      resultTable.getColumnModel().getColumn(1).setMinWidth(50);
      resultTable.getColumnModel().getColumn(1).setPreferredWidth(50);
      resultTable.getColumnModel().getColumn(1).setMaxWidth(50);
      // Main Entry
      resultTable.getColumnModel().getColumn(2).setMinWidth(50);
      resultTable.getColumnModel().getColumn(2).setPreferredWidth(200);
      
      // Title
      resultTable.getColumnModel().getColumn(3).setMinWidth(50);
      resultTable.getColumnModel().getColumn(3).setPreferredWidth(200);
      // HAsh
      resultTable.getColumnModel().getColumn(4).setMinWidth(0);
      resultTable.getColumnModel().getColumn(4).setMaxWidth(0);
      // Group
      resultTable.getColumnModel().getColumn(5).setMinWidth(100);
      resultTable.getColumnModel().getColumn(5).setPreferredWidth(100);
      resultTable.getColumnModel().getColumn(5).setMaxWidth(100);

//        resultTable.getColumnModel().getColumn(4).setMinWidth(20);
//        resultTable.getColumnModel().getColumn(4).setPreferredWidth(50);
      // REcord details
      resultTable.getColumnModel().getColumn(6).setMinWidth(50);
      resultTable.getColumnModel().getColumn(6).setPreferredWidth(150);
//        resultTable.setAutoCreateRowSorter(true);
      resultTable.getTableHeader().setReorderingAllowed(false);

      resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      //resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {
      java.awt.GridBagConstraints gridBagConstraints;

      serversPopup = new javax.swing.JPopupMenu();
      jSplitPane1 = new javax.swing.JSplitPane();
      serverPanel = new javax.swing.JPanel();
      jPanel7 = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();
      cmbCategory = new javax.swing.JComboBox();
      jLabel2 = new javax.swing.JLabel();
      cmbCountry = new javax.swing.JComboBox();
      jLabel3 = new javax.swing.JLabel();
      cmbServerType = new javax.swing.JComboBox();
      jPanel1 = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      jScrollPane2 = new javax.swing.JScrollPane();
      serverTable = new javax.swing.JTable();
      searchPanel = new javax.swing.JPanel();
      queryPanel = new javax.swing.JPanel();
      cmbAttribute1 = new javax.swing.JComboBox();
      txtAttribute1 = new javax.swing.JTextField();
      cmbRelation = new javax.swing.JComboBox();
      cmbAttribute2 = new javax.swing.JComboBox();
      txtAttribute2 = new javax.swing.JTextField();
      btnSearch = new javax.swing.JButton();
      jLabel4 = new javax.swing.JLabel();
      progress = new javax.swing.JProgressBar();
      btnCancel = new javax.swing.JButton();
      jSeparator1 = new javax.swing.JSeparator();
      resultPanel = new javax.swing.JPanel();
      jScrollPane3 = new javax.swing.JScrollPane(resultTable);
      resultTable = new javax.swing.JTable();
      displayPanel = new javax.swing.JPanel();
      controlPanel = new javax.swing.JPanel();
      jToolBar1 = new javax.swing.JToolBar();
      btnNew = new javax.swing.JButton();
      btnEdit = new javax.swing.JButton();
      btnDelete = new javax.swing.JButton();

      setLayout(new java.awt.BorderLayout());

      jSplitPane1.setDividerLocation(250);

      jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      jPanel7.setLayout(new java.awt.GridBagLayout());

      jLabel1.setText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.jLabel1.text")); // NOI18N
      jLabel1.setAutoscrolls(true);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
      jPanel7.add(jLabel1, gridBagConstraints);

      cmbCategory.setPreferredSize(new java.awt.Dimension(150, 22));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
      jPanel7.add(cmbCategory, gridBagConstraints);

      jLabel2.setText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.jLabel2.text")); // NOI18N
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
      jPanel7.add(jLabel2, gridBagConstraints);

      cmbCountry.setPreferredSize(new java.awt.Dimension(150, 22));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
      jPanel7.add(cmbCountry, gridBagConstraints);

      jLabel3.setText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.jLabel3.text")); // NOI18N
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
      gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
      jPanel7.add(jLabel3, gridBagConstraints);

      cmbServerType.setPreferredSize(new java.awt.Dimension(150, 22));
      cmbServerType.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cmbServerTypeActionPerformed(evt);
         }
      });
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
      jPanel7.add(cmbServerType, gridBagConstraints);

      serverTable.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
         },
         new String [] {
            "Title 1", "Title 2", "Title 3", "Title 4"
         }
      ));
      jScrollPane2.setViewportView(serverTable);

      jScrollPane1.setViewportView(jScrollPane2);

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE))
      );

      javax.swing.GroupLayout serverPanelLayout = new javax.swing.GroupLayout(serverPanel);
      serverPanel.setLayout(serverPanelLayout);
      serverPanelLayout.setHorizontalGroup(
         serverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(serverPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(serverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
               .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
      );
      serverPanelLayout.setVerticalGroup(
         serverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(serverPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
      );

      jSplitPane1.setLeftComponent(serverPanel);

      searchPanel.setLayout(new java.awt.BorderLayout());

      queryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

      cmbAttribute1.setPreferredSize(new java.awt.Dimension(100, 22));
      cmbAttribute1.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            cmbAttribute1ItemStateChanged(evt);
         }
      });

      txtAttribute1.setColumns(15);
      txtAttribute1.setMinimumSize(new java.awt.Dimension(11, 22));
      txtAttribute1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            txtAttribute1ActionPerformed(evt);
         }
      });

      cmbRelation.setPreferredSize(new java.awt.Dimension(60, 22));
      cmbRelation.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            cmbRelationItemStateChanged(evt);
         }
      });

      cmbAttribute2.setPreferredSize(new java.awt.Dimension(100, 22));
      cmbAttribute2.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            cmbAttribute2ItemStateChanged(evt);
         }
      });

      txtAttribute2.setColumns(15);
      txtAttribute2.setMinimumSize(new java.awt.Dimension(11, 22));
      txtAttribute2.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            txtAttribute2ActionPerformed(evt);
         }
      });

      btnSearch.setMnemonic('s');
      btnSearch.setText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.btnSearch.text")); // NOI18N
      btnSearch.setToolTipText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.btnSearch.toolTipText")); // NOI18N
      btnSearch.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnSearchActionPerformed(evt);
         }
      });

      jLabel4.setText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.jLabel4.text")); // NOI18N

      progress.setPreferredSize(new java.awt.Dimension(200, 18));

      btnCancel.setText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.btnCancel.text")); // NOI18N
      btnCancel.setToolTipText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.btnCancel.toolTipText")); // NOI18N
      btnCancel.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnCancelActionPerformed(evt);
         }
      });

      jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

      javax.swing.GroupLayout queryPanelLayout = new javax.swing.GroupLayout(queryPanel);
      queryPanel.setLayout(queryPanelLayout);
      queryPanelLayout.setHorizontalGroup(
         queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, queryPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addGroup(queryPanelLayout.createSequentialGroup()
                  .addComponent(cmbAttribute1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(5, 5, 5)
                  .addComponent(txtAttribute1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(queryPanelLayout.createSequentialGroup()
                  .addComponent(cmbAttribute2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(5, 5, 5)
                  .addComponent(txtAttribute2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGap(18, 18, 18)
            .addComponent(cmbRelation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(145, 145, 145)
            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(40, 40, 40)
            .addComponent(btnSearch)
            .addGap(18, 18, 18)
            .addComponent(jLabel4)
            .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnCancel)
            .addGap(143, 143, 143))
      );
      queryPanelLayout.setVerticalGroup(
         queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(queryPanelLayout.createSequentialGroup()
            .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(queryPanelLayout.createSequentialGroup()
                  .addGap(38, 38, 38)
                  .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(queryPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                           .addComponent(jLabel4)
                           .addComponent(btnSearch)))
                     .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnCancel)
                        .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
               .addGroup(queryPanelLayout.createSequentialGroup()
                  .addContainerGap()
                  .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(cmbAttribute1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(queryPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                           .addGroup(queryPanelLayout.createSequentialGroup()
                              .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(txtAttribute1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(cmbRelation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                              .addGap(18, 18, 18)
                              .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(queryPanelLayout.createSequentialGroup()
                                    .addGap(1, 1, 1)
                                    .addComponent(txtAttribute2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addComponent(cmbAttribute2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                  .addGap(13, 13, 13)))
            .addContainerGap())
      );

      searchPanel.add(queryPanel, java.awt.BorderLayout.NORTH);

      resultPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      resultPanel.setPreferredSize(new java.awt.Dimension(100, 300));

      resultTable.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
         },
         new String [] {
            "Title 1", "Title 2", "Title 3", "Title 4"
         }
      ));
      jScrollPane3.setViewportView(resultTable);

      javax.swing.GroupLayout resultPanelLayout = new javax.swing.GroupLayout(resultPanel);
      resultPanel.setLayout(resultPanelLayout);
      resultPanelLayout.setHorizontalGroup(
         resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(resultPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1052, Short.MAX_VALUE)
            .addContainerGap())
      );
      resultPanelLayout.setVerticalGroup(
         resultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(resultPanelLayout.createSequentialGroup()
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
            .addContainerGap())
      );

      searchPanel.add(resultPanel, java.awt.BorderLayout.CENTER);

      javax.swing.GroupLayout displayPanelLayout = new javax.swing.GroupLayout(displayPanel);
      displayPanel.setLayout(displayPanelLayout);
      displayPanelLayout.setHorizontalGroup(
         displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 1074, Short.MAX_VALUE)
      );
      displayPanelLayout.setVerticalGroup(
         displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 100, Short.MAX_VALUE)
      );

      searchPanel.add(displayPanel, java.awt.BorderLayout.PAGE_END);

      jSplitPane1.setRightComponent(searchPanel);

      add(jSplitPane1, java.awt.BorderLayout.CENTER);

      controlPanel.setLayout(new javax.swing.BoxLayout(controlPanel, javax.swing.BoxLayout.X_AXIS));

      jToolBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      jToolBar1.setAlignmentX(0.0F);
      jToolBar1.setPreferredSize(new java.awt.Dimension(13, 25));

      btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/z3950/images/New16.gif"))); // NOI18N
      btnNew.setMnemonic('n');
      btnNew.setToolTipText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.btnNew.toolTipText")); // NOI18N
      btnNew.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnNewActionPerformed(evt);
         }
      });
      jToolBar1.add(btnNew);

      btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/z3950/images/Edit16.gif"))); // NOI18N
      btnEdit.setMnemonic('t');
      btnEdit.setToolTipText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.btnEdit.toolTipText")); // NOI18N
      btnEdit.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnEditActionPerformed(evt);
         }
      });
      jToolBar1.add(btnEdit);

      btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/z3950/images/Delete16.gif"))); // NOI18N
      btnDelete.setMnemonic('d');
      btnDelete.setToolTipText(org.openide.util.NbBundle.getMessage(Z3950ClientPanel.class, "Z3950ClientPanel.btnDelete.toolTipText")); // NOI18N
      btnDelete.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnDeleteActionPerformed(evt);
         }
      });
      jToolBar1.add(btnDelete);

      controlPanel.add(jToolBar1);

      add(controlPanel, java.awt.BorderLayout.NORTH);
   }// </editor-fold>//GEN-END:initComponents

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
   
       addServer();
       //       clearChecks();
}//GEN-LAST:event_btnNewActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
       // TODO add your handling code here:
       editServer();
       //         clearChecks();
}//GEN-LAST:event_btnEditActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
       // TODO add your handling code here:
       deleteServer();
       //       clearChecks();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void cmbAttribute1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbAttribute1ItemStateChanged
       // TODO add your handling code here:
       cmbAttribute1.setToolTipText(cmbAttribute1.getSelectedItem().toString());
}//GEN-LAST:event_cmbAttribute1ItemStateChanged

    private void txtAttribute1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAttribute1ActionPerformed
       // TODO add your handling code here:
       btnSearch.doClick();
}//GEN-LAST:event_txtAttribute1ActionPerformed

    private void cmbRelationItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbRelationItemStateChanged
       // TODO add your handling code here:
       cmbRelation.setToolTipText(cmbRelation.getSelectedItem().toString());
}//GEN-LAST:event_cmbRelationItemStateChanged

    private void cmbAttribute2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbAttribute2ItemStateChanged
       // TODO add your handling code here:
       cmbAttribute2.setToolTipText(cmbAttribute2.getSelectedItem().toString());
}//GEN-LAST:event_cmbAttribute2ItemStateChanged

    private void txtAttribute2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAttribute2ActionPerformed
       // TODO add your handling code here:
       btnSearch.doClick();
}//GEN-LAST:event_txtAttribute2ActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
      
       
       clearStatuses();
       resultTableModel.setRowCount(0);

       searchZ3950Servers();

}//GEN-LAST:event_btnSearchActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
       cancelSearch();
}//GEN-LAST:event_btnCancelActionPerformed

    private void cmbServerTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbServerTypeActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_cmbServerTypeActionPerformed
   private void restoreServersSelected() {
      
      java.util.prefs.Preferences pref = java.util.prefs.Preferences.userNodeForPackage(Z3950ClientPanel.class);
      String serverList = "";
      String key = "Z3950Servers";
      serverList = pref.get(key, "");
      int rowCount = serverTable.getRowCount();

      if (serverList != null) {
         String serverNames[] = serverList.split(":");
         for (int i = 0; i < serverNames.length; i++) {
            String serverName = serverNames[i];
            for (int j = 0; j < rowCount; j++) {
               String s = serverTable.getValueAt(j, 1).toString();
               if (s.equalsIgnoreCase(serverName)) {
                  serverTable.setValueAt(true, j, 0);
               }
            }
         }
      }
   }

   private void applyKeyStrokesToTable() {
      try {


         KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
         serverTable.getInputMap().put(delete, "Delete");
         Action deleteAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
               deleteServer();
            }
         };
         serverTable.getActionMap().put("Delete", deleteAction);

         KeyStroke moveup = KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK);
         serverTable.getInputMap().put(moveup, "moveup");
         Action moveupAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
               moveUpServer();
            }
         };
         serverTable.getActionMap().put("moveup", moveupAction);

         KeyStroke movedown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK);
         serverTable.getInputMap().put(movedown, "MoveDown");
         Action movedownAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
               moveDownServer();
            }
         };
         serverTable.getActionMap().put("MoveDown", movedownAction);

      } catch (Exception e) {
      }
   }

   private void setAttributes() {
      ArrayList catVect = new ArrayList();
      ArrayList locVect = new ArrayList();
      Bib1UseAttributes bib = new Bib1UseAttributes();
      java.util.Set keyset = bib.getBib1AttributeNoHashKeys();
      String[] keys = new String[bib.getBib1AttributeNoHashSize()];
      keyset.toArray(keys);
      Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);
      for (int i = 0; i < keys.length; i++) {
         cmbAttribute1.addItem(keys[i]);
         cmbAttribute2.addItem(keys[i]);
      }
      cmbAttribute1.setSelectedItem("Title");
      cmbServerType.addItem("All");
      cmbServerType.addItem("Z3950");
      cmbServerType.addItem("SRU/W");

      cmbRelation.addItem("AND");
      cmbRelation.addItem("OR");
      int i, j;
//        System.out.println("catvect before:"+categoryVect);
//        catVect.addElement(categoryVect.elementAt(0));
      int flag = 0;
      for (i = 0; i < categoryVect.size(); i++) {
         flag = 0;
         for (j = 0; j < catVect.size(); j++) {
            if (categoryVect.get(i).equals(catVect.get(j))) {
               flag = 1;
            }
         }
         if (flag == 0) {
            catVect.add(categoryVect.get(i));
         }
      }
//        System.out.println("catvect :"+catVect);

      for (i = 0; i < locationVect.size(); i++) {
         flag = 0;
         for (j = 0; j < locVect.size(); j++) {
            if (locationVect.get(i).equals(locVect.get(j))) {
               flag = 1;
            }
         }
         if (flag == 0) {
            locVect.add(locationVect.get(i));
         }
      }
//        System.out.println("locvect :"+locVect);

      cmbCategory.addItem("All");
      cmbCountry.addItem("All");

      for (i = 0; i < catVect.size(); i++) {
         cmbCategory.addItem(catVect.get(i));
      }

      for (i = 0; i < locVect.size(); i++) {
         cmbCountry.addItem(locVect.get(i));
      }

      cmbCategory.setSelectedIndex(0);
      cmbCountry.setSelectedIndex(0);
      cmbServerType.setSelectedIndex(0);

      cmbCategory.addItemListener(this);
      cmbCountry.addItemListener(this);
      cmbServerType.addItemListener(this);


      cmbAttribute1.setRenderer(new ComboBoxTooltipRenderer());
      cmbAttribute1.setToolTipText(cmbAttribute1.getSelectedItem().toString());
      cmbAttribute2.setRenderer(new ComboBoxTooltipRenderer());
      cmbAttribute2.setToolTipText(cmbAttribute2.getSelectedItem().toString());
      cmbRelation.setRenderer(new ComboBoxTooltipRenderer());
      cmbRelation.setToolTipText(cmbRelation.getSelectedItem().toString());
      cmbServerType.setRenderer(new ComboBoxTooltipRenderer());
      cmbServerType.setToolTipText(cmbServerType.getSelectedItem().toString());
      cmbCategory.setRenderer(new ComboBoxTooltipRenderer());
      cmbCategory.setToolTipText(cmbCategory.getSelectedItem().toString());
      cmbCountry.setRenderer(new ComboBoxTooltipRenderer());
      cmbCountry.setToolTipText(cmbCountry.getSelectedItem().toString());
   }

   public void setAttributesNew() {
      ArrayList catVect = new ArrayList();
      ArrayList locVect = new ArrayList();

      int i, j;

      int flag = 0;
      for (i = 0; i < categoryVect.size(); i++) {
         flag = 0;
         for (j = 0; j < catVect.size(); j++) {
            if (categoryVect.get(i).equals(catVect.get(j))) {
               flag = 1;
            }
         }
         if (flag == 0) {
            catVect.add(categoryVect.get(i));
         }
      }
//        System.out.println("catvect :"+catVect);

      for (i = 0; i < locationVect.size(); i++) {
         flag = 0;
         for (j = 0; j < locVect.size(); j++) {
            if (locationVect.get(i).equals(locVect.get(j))) {
               flag = 1;
            }
         }
         if (flag == 0) {
            locVect.add(locationVect.get(i));
         }
      }
//        System.out.println("locvect :"+locVect);

      cmbCategory.addItem("All");
      cmbCountry.addItem("All");


      for (i = 0; i < catVect.size(); i++) {
         cmbCategory.addItem(catVect.get(i));
//          System.out.println(catVect.elementAt(i));
      }

      for (i = 0; i < locVect.size(); i++) {
         cmbCountry.addItem(locVect.get(i));
//          System.out.println(locVect.elementAt(i));
      }


      cmbCategory.setSelectedIndex(0);
      cmbCountry.setSelectedIndex(0);
      cmbServerType.setSelectedIndex(0);

      cmbCategory.addItemListener(this);
      cmbCountry.addItemListener(this);
      cmbServerType.addItemListener(this);
   }

   private void getServersDetails() {
      try {
         locationVect = new ArrayList();
         categoryVect = new ArrayList();
         String fileName = Global.getClientWorkPath();
         fileName = fileName.concat("/Z3950Servers.xml");
         File inFile = new File(fileName);
         FileInputStream fis = new FileInputStream(inFile);
         FileChannel inChannel = fis.getChannel();
         ByteBuffer buf = ByteBuffer.allocate((int) inChannel.size());
         inChannel.read(buf);
         inChannel.close();
         String myString = new String(buf.array());
         org.jdom.Element root = xmlUtility.getRootElementFromXML(myString);
         java.util.List clist = root.getChildren();
//            System.out.println("children:"+clist.size());
//            System.out.println("root:"+root);



         for (int i = 0; i < clist.size(); i++) {
            java.util.ArrayList vect = new java.util.ArrayList();
            org.jdom.Element child = (org.jdom.Element) clist.get(i);
            String xml = xmlUtility.generateXML((org.jdom.Element) child.clone());
            vect.add(false);
            vect.add(child.getChildText("Name"));
            categoryVect.add(child.getChildText("Category"));
            locationVect.add(child.getChildText("Location"));
            vect.add("");
            vect.add(xml);
//                System.out.println("xml:"+xml);
//                System.out.println("vect:+"+i+vect);
            serverTableModel.addRow(vect.toArray());
         }
//            System.out.println(myString);

      } catch (java.io.FileNotFoundException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void getServersDetailsNew() {
      serverTableModel.setRowCount(0);
      String cat = cmbCategory.getSelectedItem().toString();
      String loc = cmbCountry.getSelectedItem().toString();
      String type = cmbServerType.getSelectedItem().toString();
      try {
         String fileName = Global.getClientWorkPath();
         fileName = fileName.concat("/Z3950Servers.xml");
         File inFile = new File(fileName);
         FileInputStream fis = new FileInputStream(inFile);
         FileChannel inChannel = fis.getChannel();
         ByteBuffer buf = ByteBuffer.allocate((int) inChannel.size());
         inChannel.read(buf);
         inChannel.close();
         String myString = new String(buf.array());
         org.jdom.Element root = xmlUtility.getRootElementFromXML(myString);
         java.util.List clist = root.getChildren();
//            System.out.println("children:"+clist.size());
//            System.out.println("root:"+root);



         for (int i = 0; i < clist.size(); i++) {
            java.util.ArrayList vect = new java.util.ArrayList();
            org.jdom.Element child = (org.jdom.Element) clist.get(i);
            String xml = xmlUtility.generateXML((org.jdom.Element) child.clone());
            vect.add(false);
            vect.add(child.getChildText("Name"));
            String category = child.getChildText("Category");
            categoryVect.add(category);
            String location = child.getChildText("Location");
            locationVect.add(location);
            String type1 = child.getChildText("Type");
            vect.add("");
            vect.add(xml);
//                System.out.println("xml:"+xml);
            if (cat.equalsIgnoreCase("All") && loc.equalsIgnoreCase("All") && type.equalsIgnoreCase("All")) {
               serverTableModel.addRow(vect.toArray());
            } else if (cat.equalsIgnoreCase("All") && loc.equalsIgnoreCase("All") && !type.equalsIgnoreCase("All")) {
               if (type.equalsIgnoreCase(type1)) {
                  serverTableModel.addRow(vect.toArray());
               }
            } else if (cat.equalsIgnoreCase("All") && !loc.equalsIgnoreCase("All") && type.equalsIgnoreCase("All")) {
               if (location.equalsIgnoreCase(loc)) {
                  serverTableModel.addRow(vect.toArray());
               }
            } else if (!cat.equalsIgnoreCase("All") && loc.equalsIgnoreCase("All") && type.equalsIgnoreCase("All")) {
               if (category.equalsIgnoreCase(cat)) {
                  serverTableModel.addRow(vect.toArray());
               }
            } else if (!cat.equalsIgnoreCase("All") && loc.equalsIgnoreCase("All") && !type.equalsIgnoreCase("All")) {
               if (category.equalsIgnoreCase(cat) && type.equalsIgnoreCase(type1)) {
                  serverTableModel.addRow(vect.toArray());
               }
            } else if (!cat.equalsIgnoreCase("All") && !loc.equalsIgnoreCase("All") && type.equalsIgnoreCase("All")) {
               if (category.equalsIgnoreCase(cat) && location.equalsIgnoreCase(loc)) {
                  serverTableModel.addRow(vect.toArray());
               }
            } else if (!cat.equalsIgnoreCase("All") && !loc.equalsIgnoreCase("All") && !type.equalsIgnoreCase("All")) {
               if (category.equalsIgnoreCase(cat) && location.equalsIgnoreCase(loc) && type.equalsIgnoreCase(type1)) {
                  serverTableModel.addRow(vect.toArray());
               }
            }
         }
//            System.out.println(myString);
         cmbServerType.setToolTipText(cmbServerType.getSelectedItem().toString());
         cmbCategory.setToolTipText(cmbCategory.getSelectedItem().toString());
         cmbCountry.setToolTipText(cmbCountry.getSelectedItem().toString());
         cmbAttribute1.setToolTipText(cmbAttribute1.getSelectedItem().toString());
         cmbAttribute2.setToolTipText(cmbAttribute2.getSelectedItem().toString());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void refreshServers() {
      serverTableModel.setRowCount(0);
      cmbCategory.removeItemListener(this);
      cmbCountry.removeItemListener(this);
      cmbServerType.removeItemListener(this);

      cmbCategory.removeAllItems();
      cmbCountry.removeAllItems();
//        cmbServerType.removeAllItems();

      getServersDetails();
      setAttributesNew();
      getServersDetailsNew();
   }

   public void addServer() {
      if (this.dialogInstance == null) {
         Z3950ServerDialog pane = new Z3950ServerDialog(frame, "Add", "Add Z3950Server", categoryVect, locationVect);
         pane.setVisible(true);
      } else {
         Z3950ServerDialog pane = new Z3950ServerDialog(this.dialogInstance, "Add", "Add Z3950Server", categoryVect, locationVect);
         pane.setVisible(true);
      }
//        getServersDetailsNew();
      refreshServers();
   }

   public void editServer() {
//        int selrows=serverTable.getSelectedRowCount();

//        int selrow=0;
      int rc = serverTable.getRowCount();
      int k = 0;
      //System.out.println("row count:"+rc);
      if (rc != 0) {
         for (int i = 0; i < rc; i++) {
            String s = serverTable.getValueAt(i, 0).toString();
            if (s.equals("true")) {
               k++;
            }
         }
         //System.out.println("n:"+n);
         int[] selrows = new int[k];
         if (k <= 1) {
            int selrow = serverTable.getSelectedRow();
            for (int i = 0; i < rc; i++) {
               String s = serverTable.getValueAt(i, 0).toString();
               if (s.equals("true")) {
                  selrow = i;
               }
            }
            if (selrow != -1) {
               String server = serverTable.getValueAt(selrow, 3).toString();
               if (this.dialogInstance == null) {
                  Z3950ServerDialog pane = new Z3950ServerDialog(frame, "Modify", "Modify Z3950Server", categoryVect, locationVect);
                  pane.setServerDetails(server);
                  pane.setVisible(true);
               } else {
                  Z3950ServerDialog pane = new Z3950ServerDialog(this.dialogInstance, "Modify", "Modify Z3950Server", categoryVect, locationVect);
                  pane.setServerDetails(server);
                  pane.setVisible(true);
               }
            } else {
               javax.swing.JOptionPane pane = new javax.swing.JOptionPane();
               pane.setLocation(utilities.getScreenLocation(pane.getSize()));
               pane.showMessageDialog(jSplitPane1, "Select Server..");
            }
         } else {
            javax.swing.JOptionPane pane = new javax.swing.JOptionPane();
            pane.setLocation(utilities.getScreenLocation(pane.getSize()));
            pane.showMessageDialog(jSplitPane1, "Select one sever only for editing..");
         }

      } else {
         javax.swing.JOptionPane pane = new javax.swing.JOptionPane();
         pane.setLocation(utilities.getScreenLocation(pane.getSize()));
         pane.showMessageDialog(jSplitPane1, "Invalid operation..");
      }
      refresh();
      refreshServers();
   }

   public void deleteServer() {

//        int selrow=0;
      int rc = serverTable.getRowCount();
      int k = 0;
      //System.out.println("row count:"+rc);
      if (rc != 0) {
         for (int i = 0; i < rc; i++) {
            String s = serverTable.getValueAt(i, 0).toString();
            if (s.equals("true")) {
               k++;
            }
         }
         int[] selrows = new int[k];
         if (k > 0) {
            int l = 0;
            for (int i = 0; i < rc; i++) {
               String s = serverTable.getValueAt(i, 0).toString();
               if (s.equals("true")) {
                  selrows[l] = i;
                  l++;
               }
            }
            javax.swing.JOptionPane jpane = new javax.swing.JOptionPane();
            int dtype = jpane.showConfirmDialog(jSplitPane1, "Do you want to delete[" + k + "] server(s)?", "Question", 0);
            if (dtype == 0) {
//                for(int j=0;j<selrows.length;j++)
//                {
//                    String server=serverTable.getValueAt(selrows[j],3).toString();

//                    String oldName=serverTable.getValueAt(selrows[j],1).toString();
               //          System.out.println("oldname:"+oldName);
               try {
                  String fileName = System.getProperty("user.home");
                  fileName = fileName.concat("/Z3950Servers.xml");
                  File inFile = new File(fileName);
//                        javax.swing.JOptionPane jpane=new javax.swing.JOptionPane();
//                        int dtype=jpane.showConfirmDialog(jSplitPane1,"Do you want to delete?","Question",0);
//        //                System.out.println("dtype:"+dtype);
//                        if(dtype==0) {
                  if (inFile.exists()) {
                     FileInputStream fis = new FileInputStream(inFile);
                     SAXBuilder sb = new SAXBuilder();
                     Document doc = null;
                     try {
                        doc = sb.build(fis);
                     } catch (Exception e) {
                     }
                     org.jdom.Element root = doc.getRootElement();
                     org.jdom.Element newRoot = new org.jdom.Element("ZServers");
                     java.util.List childList = root.getChildren();
                     for (int i = 0; i < childList.size(); i++) {
                        org.jdom.Element child = (org.jdom.Element) childList.get(i);
                        String name = child.getChildText("Name");
                        //                        System.out.println("name:"+name);
                        int flag = 0;
                        for (int v = 0; v < selrows.length; v++) {
                           String oldName = serverTable.getValueAt(selrows[v], 1).toString();
                           if (name.equalsIgnoreCase(oldName)) {
                              flag = 1;
                           }
//                                        {
//                                     //                            newRoot.addContent((org.jdom.Element)newEle.clone());
//                                        } else
//                                        {
//                                        newRoot.addContent((org.jdom.Element)child.clone());
//                                        }
                        }
                        if (flag == 0) {
                           newRoot.addContent((org.jdom.Element) child.clone());
                        }
                     }

                     XMLOutputter xout = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                     xout.output(new Document((Element) newRoot.clone()), new FileOutputStream(inFile));
                  }
//                    }
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
            refresh();

         } else {
            int selrow = serverTable.getSelectedRow();
            if (selrow != -1) {
               javax.swing.JOptionPane jpane = new javax.swing.JOptionPane();
               int dtype = jpane.showConfirmDialog(jSplitPane1, "Do you want to delete[1] server(s)?", "Question", 0);
               if (dtype == 0) {

                  try {
                     String fileName = System.getProperty("user.home");
                     fileName = fileName.concat("/Z3950Servers.xml");
                     File inFile = new File(fileName);
                     if (inFile.exists()) {
                        FileInputStream fis = new FileInputStream(inFile);
                        SAXBuilder sb = new SAXBuilder();
                        Document doc = null;
                        try {
                           doc = sb.build(fis);
                        } catch (Exception e) {
                        }
                        org.jdom.Element root = doc.getRootElement();
                        org.jdom.Element newRoot = new org.jdom.Element("ZServers");
                        java.util.List childList = root.getChildren();
                        for (int i = 0; i < childList.size(); i++) {
                           org.jdom.Element child = (org.jdom.Element) childList.get(i);
                           String name = child.getChildText("Name");
                           //                        System.out.println("name:"+name);
                           String oldName = serverTable.getValueAt(selrow, 1).toString();
                           if (name.equalsIgnoreCase(oldName)) {
                              //                            newRoot.addContent((org.jdom.Element)newEle.clone());
                           } else {
                              newRoot.addContent((org.jdom.Element) child.clone());
                           }
                        }
                        XMLOutputter xout = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());



                        xout.output(new Document((Element) newRoot.clone()), new FileOutputStream(inFile));
                     }
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               }
               refresh();
            } else {
               javax.swing.JOptionPane pane = new javax.swing.JOptionPane();
               pane.showMessageDialog(jSplitPane1, "Select Server(s)..");
            }
         }
      } else {
         javax.swing.JOptionPane pane = new javax.swing.JOptionPane();
         pane.showMessageDialog(jSplitPane1, "Invalid operation..");
      }
      refreshServers();
   }

   public void moveUpServer() {
      int selrow = serverTable.getSelectedRow();
      if (selrow != -1) {
         if (selrow != 0) {
            swapRows(selrow, selrow - 1);
            serverTable.changeSelection(selrow - 1, 0, false, false);
         }
//            clearChecks();
      }
   }

   public void moveDownServer() {
      int selrow = serverTable.getSelectedRow();
      int rc = serverTable.getRowCount();
      if (selrow != -1) {
         if (selrow != (rc - 1)) {
            swapRows(selrow, selrow + 1);
            serverTable.changeSelection(selrow + 1, 0, false, false);
         }
//            clearChecks();
      }
   }

   public void swapRows(int crow, int nrow) {
      Object cck = serverTable.getValueAt(crow, 0);
      Object nck = serverTable.getValueAt(nrow, 0);

      Object cname = serverTable.getValueAt(crow, 1);
      Object nname = serverTable.getValueAt(nrow, 1);

      Object cxml = serverTable.getValueAt(crow, 3);
      Object nxml = serverTable.getValueAt(nrow, 3);

      Object crecords = serverTable.getValueAt(crow, 4);
      Object nrecords = serverTable.getValueAt(nrow, 4);

      serverTable.setValueAt(cck, nrow, 0);
      serverTable.setValueAt(nck, crow, 0);

      serverTable.setValueAt(cname, nrow, 1);
      serverTable.setValueAt(nname, crow, 1);

      serverTable.setValueAt(cxml, nrow, 3);
      serverTable.setValueAt(nxml, crow, 3);

      serverTable.setValueAt(crecords, nrow, 4);
      serverTable.setValueAt(nrecords, crow, 4);

      clearStatuses();
   }

   public void refresh() {
      serverTableModel.setRowCount(0);
      getServersDetails();

   }

   /**
    * Clear the server first column checkboxes
    */
   public void clearChecks() {
      for (int i = 0; i < serverTable.getRowCount(); i++) {
         serverTable.setValueAt(false, i, 0);
      }
   }

   public void searchZ3950Servers() {
      
      resultTableModel.setRowCount(0);
      Bib1UseAttributes bib = new Bib1UseAttributes();
      // Server Search properties
      Hashtable searchProp = new Hashtable();
      // Array to store all server search properties
      ArrayList searchProps = new ArrayList();
      // Compute the number of servers selected
      int n = 0;
      for (int i = 0; i < serverTable.getRowCount(); i++) {
         String s = serverTable.getValueAt(i, 0).toString();
         // serverName is true if 1st column checkbox is checked
         if (s.equals("true")) {
            n++;
         }
      }
      if (n > 0) {
         java.util.prefs.Preferences pref = java.util.prefs.Preferences.userNodeForPackage(Z3950ClientPanel.class);
         String serverNameList = "";
         String key = "Z3950Servers";


         // Store the index of the selected server rows in selRows
         // and save the names of the selected servers for next run
         int[] selRows = new int[n];
         int l = 0;
         for (int i = 0; i < serverTable.getRowCount(); i++) {
            String s = serverTable.getValueAt(i, 0).toString();
            if (s.equals("true")) {
               String ss = serverTable.getValueAt(i, 1).toString();
               serverNameList = serverNameList.concat(ss).concat(":");
               selRows[l] = i;
               l++;
            }
         }

         pref.put(key,serverNameList);

         if (selRows.length > 0) {
            if (attrValidation()) {
               for (int i = 0; i < selRows.length; i++) {
                  searchProp = new Hashtable();
                  String xml = serverTable.getValueAt(selRows[i], 3).toString();
                  org.jdom.Element root = xmlUtility.getRootElementFromXML(xml);
                  searchProp.put("currthread", root.getChildText("Name"));
                  org.jdom.Element zurl = root.getChild("Zurl");
                  searchProp.put("ServiceHost", zurl.getChildText("BaseURL"));
                  searchProp.put("ServicePort", zurl.getChildText("Port"));
                  searchProp.put("database", zurl.getChildText("DataBase"));
                  searchProp.put("encoding", zurl.getChildText("Encoding"));
                  searchProp.put("UserName", 
                          (zurl.getChildText("UserName")==null)? "" : zurl.getChildText("UserName"));
                  searchProp.put("Password", 
                          (zurl.getChildText("Password")==null)? "" : zurl.getChildText("Password"));
                  searchProp.put("RecordType", 
                          (zurl.getChildText("RecordType")==null)? "marc21": zurl.getChildText("RecordType"));
                   searchProp.put("ElementSetName",
                          (zurl.getChildText("ElementSetName")==null)? "f": zurl.getChildText("ElementSetName"));
                  
                  searchProp.put("attribute1", bib.getBib1AttributeNo(cmbAttribute1.getSelectedItem()));
                  searchProp.put("value1", txtAttribute1.getText().trim());
                  searchProp.put("value2", txtAttribute2.getText().trim());
                  searchProp.put("attribute2", bib.getBib1AttributeNo(cmbAttribute2.getSelectedItem()));
                  searchProp.put("relation", cmbRelation.getSelectedItem());
                  searchProp.put("selrow", String.valueOf(selRows[i]));
                  //                    System.out.println("selrow:"+selrows[i]);
                  serverTable.setValueAt("Searching...", selRows[i], 2);
                  searchProps.add(searchProp);
                
               }
               //                return rhash;

               doSearch(searchProps);

            } else {
               timer.stop();
               progress.setIndeterminate(false);
               progress.setString("");
               javax.swing.JOptionPane pane = new javax.swing.JOptionPane();
               pane.showMessageDialog(jSplitPane1, "Please give attribute(s)...");
            }
         }
      } else {
         timer.stop();
         progress.setIndeterminate(false);
         progress.setString("");
         javax.swing.JOptionPane pane = new javax.swing.JOptionPane();
         pane.showMessageDialog(jSplitPane1, "Select Server(s)...");
      }
   }

   public boolean attrValidation() {
      String att1 = txtAttribute1.getText().trim();
      String att2 = txtAttribute2.getText().trim();
      if (!att1.equals("") || !att2.equals("")) {
         return true;
      } else {
         return false;
      }
   }

   public void setServerGroups() {
      for (int i = 0; i < resultTable.getRowCount(); i++) {
         String st = "";
         ArrayList vect = (ArrayList) resultTable.getValueAt(i, 5);
         for (int j = 0; j < vect.size(); j++) {
            String st1 = vect.get(j).toString();
            st = st.concat(st1).concat(",");
         }
         resultTable.setValueAt(st, i, 5);
//            System.out.println(resultTable.getValueAt(i,5));
      }
   }

   public String getTitleSlashResponsibility(org.marc4j.marc.Record iso) {
        String s1 = "";
        String s2 = "";
        // TAg 245, Title and Statement of REsponsability Area
        org.marc4j.marc.DataField f = (org.marc4j.marc.DataField) iso.getVariableField("245");
        if(f != null){
            s1 = f.getSubfield('a').getData();
            if(s1.charAt(s1.length()-1) == '/'){
                s1 = s1.substring(0,s1.length()-1);
                if(s1 == null){
                    s1 = "_";
                }
            }

            s2 = (f.getSubfield('c')==null) ? "-" : f.getSubfield('c').getData();

        }

        return (s1+"/"+s2);
    }

   private org.marc4j.converter.CharConverter getConverter(String convertEncoding) throws Exception {

      org.marc4j.converter.CharConverter charconv = null;
      if (null != convertEncoding) {

         try {
            if (convertEncoding.startsWith("MARC-8")) {
               charconv = new org.marc4j.converter.impl.AnselToUnicode();
            } else if (convertEncoding.startsWith("ISO-5426")) {
               charconv = new org.marc4j.converter.impl.Iso5426ToUnicode();
            } else if (convertEncoding.startsWith("ISO-6937")) {
               charconv = new org.marc4j.converter.impl.Iso6937ToUnicode();
            } else {
               throw new Exception("Unknown character set");
            }

         } catch (javax.xml.parsers.FactoryConfigurationError e) {
            e.printStackTrace();
            throw new Exception(e);
         } catch (MarcException e) {
            e.printStackTrace();
            throw new Exception("There is a problem with character conversion: "
                    + convertEncoding + " " + e);
         } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
         }
      }
      return charconv;
   }


   /**
    * Convert from ISO2709 to marc21 or unimarc and
    * @param records The records retrieved from the Z39.50 server
    * @param searchProp The Z39.50 Search properties
    */
   public void convertAndFillResultTable(List records, Hashtable searchProp) {

      try {
         String recordType = (String) searchProp.get("RecordType");
         String encoding = (String) searchProp.get("encoding");
         org.marc4j.converter.CharConverter charConverter = null;
         if (encoding.startsWith("MARC-8") || encoding.startsWith("ISO-5426")
                 || encoding.startsWith("ISO-6937")) {
            charConverter = getConverter(encoding);
         }
         setCurrServer(searchProp.get("currthread").toString());
           
         for (int i = 0; i < records.size(); i++) {
            org.marc4j.marc.Record iso2709 = null;
            ArrayList rowData = new ArrayList();
            rowData.add(false);
            Object obj = records.get(i);
//            System.out.println("********* New Record i="+i+"************");
//            System.out.println("Record "+i+" obj is instance of: "+obj.getClass().getCanonicalName());
//            System.out.println("@@@@@@@\n record tostring content ["+ i + "]=\n" + records.get(i));
      
            byte[] data = null;
            if (obj instanceof com.k_int.IR.Syntaxes.iso2709) {
               com.k_int.IR.Syntaxes.iso2709 iso = (com.k_int.IR.Syntaxes.iso2709) obj;
               data = (byte[]) iso.getOriginalObject();
            }
            //System.out.println("Record "+i+ " raw data:\n");
            //prettyPrintHex(data);
            System.out.println("Encoding="+encoding);
            if (encoding.startsWith("UTF-8")) {
               // Already UNICODE
               InputStream input = new ByteArrayInputStream(data);
               org.marc4j.MarcReader reader = new org.marc4j.MarcStreamReader(input, "UTF8");
               iso2709 = reader.next();
            } else {
               try {

                  iso2709 = Converter.getInstance().convertToUnicode(data, charConverter);
               } catch (Exception e2) {
                  System.out.println("Record " + i + "Exception in Converter!!!!!!!!!");

                  iso2709 = null;
               }
            }
            // Here we have a memory representation of the record ISO2709 structure
            // This standard does not specify the content of a record and does not,
            //in general, assign meaning to tags, indicators, or data element identifiers

            if (iso2709 != null) {

               Hashtable recHash = new Hashtable();
//                Converter conv=new Converter();
               rowData.add(String.valueOf(sno));
               String mainEntry = "";
               if (recordType.equals("marc21")) {
                  // A MARC record can only have one MAIN ENTRY, so it cannot have
                  // more than one 1XX field; that is, it can have 100 or 110 or 111 
                  // or 130, or no 1XX at all, but it cannot have a 100 and a 110, etc.
                  org.marc4j.marc.DataField field = (org.marc4j.marc.DataField) iso2709.getVariableField("100");
                  if (field == null) {
                     field = (org.marc4j.marc.DataField) iso2709.getVariableField("110");
                     if (field == null) {
                        field = (org.marc4j.marc.DataField) iso2709.getVariableField("111");
                        if (field == null) {
                           field = (org.marc4j.marc.DataField) iso2709.getVariableField("130");
                        }
                     }
                  }

                  if (field == null) {
                     mainEntry = "";
                  } else {
                     mainEntry = (field.getSubfield('a')) == null ? "" : field.getSubfield('a').getData();


                  }
                  rowData.add(mainEntry);
                  recHash.put("mainentry", mainEntry);
                  String title = getTitleSlashResponsibility(iso2709);
                  rowData.add(title);
                  recHash.put("title", title);
                  org.marc4j.marc.DataField pfield = (org.marc4j.marc.DataField) iso2709.getVariableField("260");
                  if (pfield != null) {
                     String pp = (pfield.getSubfield('a')==null) ? "" : pfield.getSubfield('a').getData();
                     String np = (pfield.getSubfield('b')==null) ? "" :pfield.getSubfield('b').getData();
                     String dp = (pfield.getSubfield('c')==null) ? "" :pfield.getSubfield('c').getData();
                     recHash.put("pop", pp);
                     recHash.put("nop", np);
                     recHash.put("dop", dp);
                  }
               } else {
                  org.marc4j.marc.DataField field = (org.marc4j.marc.DataField) iso2709.getVariableField("700");
                  if (field == null) {
                     field = (org.marc4j.marc.DataField) iso2709.getVariableField("710");
                  }
                  if (field == null) {
                     field = (org.marc4j.marc.DataField) iso2709.getVariableField("720");
                  }
                  if (field == null) {
                     mainEntry = "";
                  } else {
                     mainEntry = (field.getSubfield('a') == null) ? "" : field.getSubfield('a').getData();
                     mainEntry += " - ";
                     mainEntry += (field.getSubfield('c') == null) ? "" : " - " + field.getSubfield('a').getData();
                  }


                  rowData.add(mainEntry);
                  recHash.put("mainentry", mainEntry);

                  field = (org.marc4j.marc.DataField) iso2709.getVariableField("200");


                  String s1 = "";
                  if (field != null) {
                     s1 = (field.getSubfield('a') == null) ? "" : field.getSubfield('a').getData();
                     String s2 = (field.getSubfield('e') == null) ? "" : field.getSubfield('e').getData();
                     if (s2 != null) {
                        s1 += "_";
                        s1 += s2;
                     }
                     String s3 = (field.getSubfield('f') == null) ? "" : field.getSubfield('f').getData();
                     if (s3 != null) {
                        s1 += "_";
                        s1 += s3;
                     }
                  }
                  String title = s1;
                  rowData.add(title);
                  recHash.put("title", title);
                  org.marc4j.marc.DataField pfield = (org.marc4j.marc.DataField) iso2709.getVariableField("200");
                  if (pfield != null) {
                     String pp = (pfield.getSubfield('a') == null) ? "" : pfield.getSubfield('a').getData();

                     String np = (pfield.getSubfield('b') == null) ? "" : pfield.getSubfield('b').getData();

                     String dp = (pfield.getSubfield('c') == null) ? "" : pfield.getSubfield('c').getData();
                     if (dp == null) {
                        dp = "";
                     }
                     recHash.put("pop", pp);
                     recHash.put("nop", np);
                     recHash.put("dop", dp);
                  }
               }
               rowData.add(recHash);
               ArrayList serverg = new ArrayList();
               serverg.add(currServer);
               rowData.add(serverg);
//               Hashtable cmdHash = new Hashtable();
//               cmdHash.put(currServer, iso2709);
//               rowData.add(cmdHash);
               rowData.add(iso2709);
//               if (!dupCheck(recHash)) {
////                System.out.println("getCurrrow():.."+getCurrrow());
//                  ArrayList group = (ArrayList) resultTable.getValueAt(getCurrrow(), 4);
//                  Hashtable cHash = (Hashtable) resultTable.getValueAt(getCurrrow(), 5);
//
////                System.out.println("group:..."+group);
//                  if (group == null) {
//                     group = new ArrayList();
//                     cHash = new Hashtable();
//                     group.add(getCurrServer());
//                     resultTable.setValueAt(group, getCurrrow(), 4);
//                     cHash.put(currServer, iso2709);
////                    System.out.println("group in if:"+group);
//                  } else {
//
//                     int found = 0;
//                     String currserver = getCurrServer();
////                    System.out.println("currserver....:"+currserver);
//                     for (int l = 0; l < group.size(); l++) {
//                        String currs = group.get(l).toString();
//                        if (currs.equalsIgnoreCase(currserver)) {
//                           found = 1;
//                        }
//                     }
//                     if (found == 0) {
//                        group.add(currserver);
//                        cHash.put(currserver, iso2709);
//                        resultTable.setValueAt(group, getCurrrow(), 4);
//                        resultTable.setValueAt(cHash, getCurrrow(), 5);
//
//                     }
//
//                  }
//               } else
               {
                  resultTableModel.addRow(rowData.toArray());
                  sno++;
               }
//            }
            } else {
               System.out.println("#################################################################");
            }
         }
//            writer.flush();
//            writer.close();
      } catch (Exception e) {

         e.printStackTrace();
         System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
      }

   }

   public boolean dupCheck(Hashtable recHash) {

      boolean rettype = true;
      int rc = resultTable.getRowCount();
      try {
         if (rc == 0) {
            rettype = true;
         } else {
            for (int i = 0; i < rc; i++) {
               Hashtable rHash = (Hashtable) resultTable.getValueAt(i, 3);
               String title1 = rHash.get("title").toString();
               String title2 = recHash.get("title").toString();
               String mainentry1 = rHash.get("mainentry").toString();
               String mainentry2 = recHash.get("mainentry").toString();
               String pop1 = rHash.get("pop").toString();
               String pop2 = recHash.get("pop").toString();
               String nop1 = rHash.get("nop").toString();
               String nop2 = recHash.get("nop").toString();
               String dop1 = rHash.get("dop").toString();
               String dop2 = recHash.get("dop").toString();
//                if(rHash.get("title").equals(recHash.get("title")) && rHash.get("mainentry").equals(recHash.get("mainentry")) && rHash.get("pop").equals(recHash.get("pop")) && rHash.get("nop").equals(recHash.get("nop")) && rHash.get("dop").equals(recHash.get("dop")))
//                    rettype=false;

               if (title1.equalsIgnoreCase(title2) && mainentry1.equalsIgnoreCase(mainentry2) && pop1.equalsIgnoreCase(pop2) && nop1.equalsIgnoreCase(nop2) && dop1.equalsIgnoreCase(dop2)) {
                  rettype = false;
                  setCurrrow(i);
                  break;
               }

            }
         }
      } catch (Exception e) {
      }
      return rettype;

   }
   class MultiThreads {
      private int nThreads;
      public MultiThreads(int nThreads) {
         this.nThreads = nThreads;
      }
      public void threadDone() {
         nThreads--;
      }
      public boolean moreThreadsRunning() {
         return (nThreads > 0);
      }
   }
  
   public void doSearch(ArrayList searchProps) {


      if (searchProps.size() > 0) {
         timer.start();
         final ArrayList results = new ArrayList();
         
         int nThreads = searchProps.size();
         tasks = new ArrayList<ZSwingWorker>(nThreads);
         final MultiThreads multiThreads = new MultiThreads(nThreads);
         for (int i = 0; i < searchProps.size(); i++) {
            final Hashtable searchProp = (Hashtable) searchProps.get(i);

            int selrow = serverTable.getSelectedRow();
            final String selr = searchProp.get("selrow").toString();
            System.out.println("selected row:" + selr);
            serverTable.setValueAt("Searching...", Integer.parseInt(selr), 2);
            System.out.println("searchProp:" + searchProp);
            setCurrServer(searchProp.get("currthread").toString());

            /**
             * Inner class that is called by the thread worker for giving back
             * intermediate results
             */
            Informable inform = new Informable() {
               @Override
               public void recordsRetrieved(List records) {
                  convertAndFillResultTable(records, searchProp);
                  results.addAll(records);
               }
            };

            tasks.add(new ZSwingWorker(searchProp, inform) {
               // This method is invoked when the worker is finished
               // its task

               @Override
               protected void done() {
                  try {
                     System.out.println("TAsk is done");
                     multiThreads.threadDone();
                      System.out.println("TAsk is donemultiThreads.moreThreadsRunning()"+multiThreads.moreThreadsRunning());
                     if (!multiThreads.moreThreadsRunning()) {
                        progress.setIndeterminate(false);
                        progress.setString("Completed...");
                        setServerGroups();
                        timer.stop();
                     }
                     // Get the number of records. Note that the
                     // method get will throw any exception thrown
                     // during the execution of the worker.

                     System.out.println("doInBackground is complete nRecords=" + get());
                     if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
                        System.out.println("javax.swing.SwingUtilities.isEventDispatchThread()returned false.");
                     }
                     serverTable.setValueAt(results, Integer.parseInt(selr), 4);

                     String coms = "Completed...".concat("[").concat(String.valueOf(results.size())).concat("]");
                     serverTable.setValueAt(coms, Integer.parseInt(selr), 2);
                     //repaint();
                     //          progressBar.setVisible(false);
                  } catch (Exception e) {
                     System.out.println("Caught an exception: " + e);
                  }
               }
            });
            // Start the worker. Note that control is
            // returned immediately
            tasks.get(i).execute();
         }
      }
   }

   private void cancelSearch() {
      for (ZSwingWorker task : tasks) {
         task.cancel(true);
      }
   }

   public void clearStatuses() {
      for (int i = 0; i < serverTable.getRowCount(); i++) {
         serverTable.setValueAt("", i, 2);
      }
   }

   public void clearAction() {
      txtAttribute2.setText("");
      txtAttribute1.setText("");
      resultTableModel.setRowCount(0);
   }

   private void serverTableMouseClicked(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
      int row = serverTable.getSelectedRow();
      int col = serverTable.getSelectedColumn();
      if (row != -1 && col != -1) {
         if (col != 0) {
            String boo = String.valueOf(serverTable.getValueAt(row, 0));
            if (boo.equalsIgnoreCase("false")) {
               clearChecks();//serverTable.setValueAt(new Boolean(true),selrow,0);
            }//            System.out.println("booo"+boo);
         }


      }
   }

   private void serverTableAncestorAdded(javax.swing.event.AncestorEvent evt) {
// TODO add your handling code here:
   }

   private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {

      refreshServers();
   }

   @Override
   public void itemStateChanged(ItemEvent e) {
      getServersDetailsNew();
   }

   public int getRenderingReportStatus() {
      return renderingReportStatus;
   }

   public void setRenderingReportStatus(int renderingReportStatus) {
      this.renderingReportStatus = renderingReportStatus;
   }

   @Override
   public void valueChanged(ListSelectionEvent e) {
      int selrow = serverTable.getSelectedRow();
      if (selrow != -1) {
         sno = 1;
         resultTableModel.setRowCount(0);
         ArrayList rVect = (ArrayList) serverTable.getValueAt(selrow, 4);
         if (rVect != null) {
            //convertAndFillResultTable(rVect, null);
            setServerGroups();
         }

      }
   }

   public String getCurrServer() {
      return currServer;
   }

   public void setCurrServer(String currServer) {
      this.currServer = currServer;
   }

   public int getCurrrow() {
      return currrow;
   }

   public void setCurrrow(int currrow) {
      this.currrow = currrow;
   }

   public int getSelectedRow() {
      return resultTable.getSelectedRow();
   }

   public Object getCmdHash() {
      int selrow = resultTable.getSelectedRow();
      if (selrow != -1) {
         return resultTable.getValueAt(selrow, 5);
      } else {
         return null;
      }
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equalsIgnoreCase("New")) {
         addServer();
      } else if (e.getActionCommand().equalsIgnoreCase("Edit")) {
         editServer();
      } else if (e.getActionCommand().equalsIgnoreCase("Delete")) {
         deleteServer();
      } else if (e.getActionCommand().equalsIgnoreCase("MoveUp")) {
         moveUpServer();
      } else if (e.getActionCommand().equalsIgnoreCase("MoveDown")) {
         moveDownServer();
      }

//        System.out.println("action:"+e.getActionCommand());
   }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton btnCancel;
   private javax.swing.JButton btnDelete;
   private javax.swing.JButton btnEdit;
   private javax.swing.JButton btnNew;
   private javax.swing.JButton btnSearch;
   private javax.swing.JComboBox cmbAttribute1;
   private javax.swing.JComboBox cmbAttribute2;
   private javax.swing.JComboBox cmbCategory;
   private javax.swing.JComboBox cmbCountry;
   private javax.swing.JComboBox cmbRelation;
   private javax.swing.JComboBox cmbServerType;
   private javax.swing.JPanel controlPanel;
   private javax.swing.JPanel displayPanel;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel7;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JScrollPane jScrollPane3;
   private javax.swing.JSeparator jSeparator1;
   private javax.swing.JSplitPane jSplitPane1;
   private javax.swing.JToolBar jToolBar1;
   private javax.swing.JProgressBar progress;
   private javax.swing.JPanel queryPanel;
   private javax.swing.JPanel resultPanel;
   private javax.swing.JTable resultTable;
   private javax.swing.JPanel searchPanel;
   private javax.swing.JPanel serverPanel;
   private javax.swing.JTable serverTable;
   private javax.swing.JPopupMenu serversPopup;
   private javax.swing.JTextField txtAttribute1;
   private javax.swing.JTextField txtAttribute2;
   // End of variables declaration//GEN-END:variables

   class ServerTableItemListener implements ItemListener {
      @Override
      public void itemStateChanged(ItemEvent e) {
         Object source = e.getSource();
         if (source instanceof AbstractButton == false) {
            return;
         }
         boolean checked = e.getStateChange() == ItemEvent.SELECTED;
         for (int x = 0, y = serverTable.getRowCount(); x < y; x++) {
            serverTable.setValueAt( checked, x, 0);
         }
      }
   }
    class ResultTableItemListener implements ItemListener {
      @Override
      public void itemStateChanged(ItemEvent e) {
         Object source = e.getSource();
         if (source instanceof AbstractButton == false) {
            return;
         }
         boolean checked = e.getStateChange() == ItemEvent.SELECTED;
         for (int x = 0, y = resultTable.getRowCount(); x < y; x++) {
            resultTable.setValueAt( checked, x, 0);
         }
      }
   }

   class CheckBoxHeader extends JCheckBox
           implements TableCellRenderer, MouseListener {
      protected CheckBoxHeader rendererComponent;
      protected int column;
      protected boolean mousePressed = false;

      public CheckBoxHeader(ItemListener itemListener) {
         rendererComponent = this;
         rendererComponent.addItemListener(itemListener);
      }

      @Override
      public Component getTableCellRendererComponent(
              JTable table, Object value,
              boolean isSelected, boolean hasFocus, int row, int column) {
//        System.out.println("cell ("+row+","+column+")");
         if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
               rendererComponent.setForeground(header.getForeground());
               rendererComponent.setBackground(header.getBackground());
               rendererComponent.setFont(header.getFont());
               header.addMouseListener(rendererComponent);
            }
            final Font boldFont = header.getFont().deriveFont(Font.BOLD);
            rendererComponent.setFont(boldFont);
            rendererComponent.setBorder(header.getBorder());
            rendererComponent.setBorderPainted(true);
            rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
         }
         setColumn(column);
//    rendererComponent.setText(" ");
         setBorder(UIManager.getBorder("TableHeader.cellBorder"));
         return rendererComponent;
      }

      protected void setColumn(int column) {
         this.column = column;
      }

      public int getColumn() {
         return column;
      }

      protected void handleClickEvent(MouseEvent e) {
         if (mousePressed) {
            mousePressed = false;
            JTableHeader header = (JTableHeader) (e.getSource());
            JTable tableView = header.getTable();
            TableColumnModel columnModel = tableView.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = tableView.convertColumnIndexToModel(viewColumn);
            if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {
               doClick();
            }
         }
      }

      @Override
      public void mouseClicked(MouseEvent e) {
         handleClickEvent(e);
         ((JTableHeader) e.getSource()).repaint();
      }

      @Override
      public void mousePressed(MouseEvent e) {
         mousePressed = true;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
      }

      @Override
      public void mouseEntered(MouseEvent e) {
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }
   }
}
