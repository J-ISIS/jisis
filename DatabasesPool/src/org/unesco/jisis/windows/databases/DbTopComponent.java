package org.unesco.jisis.windows.databases;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.NoDatabaseSelectedException;
import org.unesco.jisis.database.explorer.DbViewAction;
import org.unesco.jisis.databrowser.RecordDataBrowserAction;
import org.unesco.jisis.dataentryexdl.DataEntryTopComponent;
import org.unesco.jisis.gui.Util;
import org.unesco.jisis.index.IndexDatabaseAction;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;


/**
 * Top component which displays something.
 */
public class DbTopComponent extends TopComponent {

    private static DbTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/unesco/jisis/windows/databases/data.png";
    static final String REFRESH_ICON_PATH = "org/unesco/jisis/windows/databases/refresh.png";
    static final String BROWSE_ICON_PATH = "org/unesco/jisis/windows/databases/browse.png";
    static final String DATA_ENTRY_ICON_PATH = "org/unesco/jisis/windows/databases/dataentry.png";
    static final String DEFAULT_ICON_PATH = "org/unesco/jisis/windows/databases/default.png";
    static final String CLOSEDB_ICON_PATH = "org/unesco/jisis/windows/databases/close.png";
    static final String INDEXDB_ICON_PATH = "org/unesco/jisis/windows/databases/index.png";
    private static final String PREFERRED_ID = "DbTopComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(DbTopComponent.class);
  
    private RequestProcessor.Task reIndexTask = null;
    private ClientDatabaseProxy db_ = null;
    JMenuItem miIndex = null;
    
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

    private class ReIndexAction extends AbstractAction {

        public ReIndexAction() {
            putValue(Action.NAME, "Re-Index Database");
            putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(DbTopComponent.INDEXDB_ICON_PATH, true)));
        }

        public void actionPerformed(ActionEvent evnt) {
            TreePath node = dbPoolTree.getSelectionPath();


            if (node != null) {
                db_ = (ClientDatabaseProxy) node.getLastPathComponent();
                requestProcessor_.post(new Runnable() {

                    public void run() {
                        try {
                            final ProgressHandle progress = ProgressHandle.createHandle("Performing Indexing " + "...", new Cancellable() {

                                public boolean cancel() {
                                    cancelled = true;

                                    return true;
                                }
                                private boolean cancelled;
                            });
                            GuiGlobal.output("Starting indexing");
                            GuiGlobal.output("Please wait");
                            Date start = new Date();
                            progress.start();
                            progress.switchToIndeterminate();

                            db_.reIndex();

                            Date end = new Date();
                            GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to Index DB");
                            progress.finish();
                        } catch (DbException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                    }
                });
            }
        }
    }

    private DbTopComponent() {

        initComponents();
        setName(NbBundle.getMessage(DbTopComponent.class, "CTL_DbTopComponent"));
        setToolTipText(NbBundle.getMessage(DbTopComponent.class, "HINT_DbTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        dbPoolTree.setModel(new DatabasesModel());
        dbPoolTree.setCellRenderer(new DbTreeCellRenderer());

        JMenuItem miRefresh = new JMenuItem(NbBundle.getMessage(DbTopComponent.class, "CTL_Refresh"),
                new ImageIcon(ImageUtilities.loadImage(REFRESH_ICON_PATH, true)));
        miRefresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                refresh();
            }
        });


        miIndex = new JMenuItem(new IndexDatabaseAction());

//         miIndex = new JMenuItem(NbBundle.getMessage(DbTopComponent.class, "CTL_ReIndex"),
//                 new ImageIcon(Utilities.loadImage(BROWSE_ICON_PATH, true)));
//        miIndex.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent ae) {
//               StatusDisplayer.getDefault().setStatusText("ReIndexing");
//               reIndex(false);
//            }
//       });

        JMenuItem miBrowse = new JMenuItem(NbBundle.getMessage(DbTopComponent.class, "CTL_Browse"),
                new ImageIcon(ImageUtilities.loadImage(BROWSE_ICON_PATH, true)));
        miBrowse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                browse(false);
            }
        });

        JMenuItem miTableBrowse = new JMenuItem(NbBundle.getMessage(DbTopComponent.class,
                "CTL_Table_Browse"), new ImageIcon(ImageUtilities.loadImage(BROWSE_ICON_PATH, true)));
        miTableBrowse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                tableBrowse(false);
            }

            private void tableBrowse(boolean b) {
                tableRecordBrowse(false);
            }
        });

        JMenuItem miDataEntry = new JMenuItem(NbBundle.getMessage(DbTopComponent.class, "CTL_DataEntry"),
                new ImageIcon(ImageUtilities.loadImage(DATA_ENTRY_ICON_PATH, true)));
        miDataEntry.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (!Util.isAdminOrOper()) {
                    return;
                }
                dataEntry(false);
            }
        });


        JMenuItem setDefault = new JMenuItem(NbBundle.getMessage(DbTopComponent.class, "CTL_SetDefault"),
                new ImageIcon(ImageUtilities.loadImage(DEFAULT_ICON_PATH, true)));
        setDefault.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setDefault();
            }
        });

        JMenuItem closeDb = new JMenuItem(NbBundle.getMessage(DbTopComponent.class, "CTL_CloseDb"),
                new ImageIcon(ImageUtilities.loadImage(CLOSEDB_ICON_PATH, true)));
        closeDb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                closeDb();
            }
        });

        dbPopup.add(miRefresh);
        dbPopup.add(miBrowse);
        dbPopup.add(miTableBrowse);
        dbPopup.add(miDataEntry);
        dbPopup.add(miIndex);
        dbPopup.add(setDefault);
        dbPopup.add(closeDb);

    }

    public void refresh() {
        dbPoolTree.setModel(new DatabasesModel());
        dbPoolTree.updateUI();
    }

    private void browse(boolean def) {
        TreePath node = dbPoolTree.getSelectionPath();
        if (node != null && !def) {
            try {
                ClientDatabaseProxy db = (ClientDatabaseProxy) node.getLastPathComponent();
                DbViewAction dbViewAction = new DbViewAction();
                dbViewAction.actionPerformed(null);
               
            } catch (ClassCastException cce) {
                new NoDatabaseSelectedException().displayWarning();
            }
        } else {
            DbViewAction dbViewAction = new DbViewAction();
            dbViewAction.actionPerformed(null);
        }

    }

    private void tableRecordBrowse(boolean def) {
        TreePath node = dbPoolTree.getSelectionPath();
        if (node != null && !def) {
            try {
                ClientDatabaseProxy db = (ClientDatabaseProxy) node.getLastPathComponent();
                RecordDataBrowserAction recordDataBrowserAction = new RecordDataBrowserAction();
                recordDataBrowserAction.actionPerformed(null);
            } catch (ClassCastException cce) {
                new NoDatabaseSelectedException().displayWarning();
            }
        } else {
            RecordDataBrowserAction recordDataBrowserAction = new RecordDataBrowserAction();
            recordDataBrowserAction.actionPerformed(null);

        }

    }

    private void dataEntry(boolean def) {

        TreePath node = dbPoolTree.getSelectionPath();
        if (node != null && !def) {
            try {
                ClientDatabaseProxy db = (ClientDatabaseProxy) node.getLastPathComponent();
                TopComponent win = new DataEntryTopComponent(db);
                win.open();
                win.requestActive();
            } catch (ClassCastException cce) {
                new NoDatabaseSelectedException().displayWarning();
            }
        } else {
               ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
           
                TopComponent win = new DataEntryTopComponent(connectionInfo.getDefaultDatabase());
                win.open();
                win.requestActive();
           
        }

    }

    private void setDefault() {
        TreePath node = dbPoolTree.getSelectionPath();
        ClientDatabaseProxy db;
        if (node != null) {
            try {
                db = (ClientDatabaseProxy) node.getLastPathComponent();
                 ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
                connectionInfo.setDefaultDatabase(db);
                StatusDisplayer.getDefault().setStatusText("Default Db: " + db.getDbHome() + "//" + db.getDbName());
            } catch (ClassCastException cce) {
                new NoDatabaseSelectedException().displayWarning();
            }
        }
        dbPoolTree.setModel(new DatabasesModel());
        dbPoolTree.updateUI();


    }

    private void closeDb() {
        TreePath node = dbPoolTree.getSelectionPath();
        if (node == null) {
            return;
        }
        boolean isDbNode = node.getLastPathComponent() instanceof ClientDatabaseProxy;
        if (!isDbNode) {
            return;
        }
        ClientDatabaseProxy db = (ClientDatabaseProxy) node.getLastPathComponent();
        closeDatabase(db);
    }

    public void closeDatabase(ClientDatabaseProxy db) {
        try {
            db.close();
            ConnectionPool.removeDatabase(db);

        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        dbPoolTree.setModel(new DatabasesModel());
        dbPoolTree.updateUI();
    }

    public void closeAllDatabases() {
        IConnection connection = ConnectionPool.getDefaultConnection();
         ConnectionPool.closeAllDatabases(connection);
       
        dbPoolTree.setModel(new DatabasesModel());
        dbPoolTree.updateUI();
    }
    
    public void closeConnection() {
        try {
            closeAllDatabases();
            IConnection con = ConnectionPool.getDefaultConnection();
            ConnectionPool.closeConnection(con);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      dbPopup = new javax.swing.JPopupMenu();
      dbScrollPane = new javax.swing.JScrollPane();
      dbPoolTree = new javax.swing.JTree();
      jPanel1 = new javax.swing.JPanel();
      btnRefresh = new javax.swing.JButton();
      btnBrowse = new javax.swing.JButton();
      btnDataEntry = new javax.swing.JButton();
      btnSetDefault = new javax.swing.JButton();
      btnClose = new javax.swing.JButton();

      setLayout(new java.awt.BorderLayout());

      dbPoolTree.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            dbPoolTreeMouseClicked(evt);
         }
      });
      dbScrollPane.setViewportView(dbPoolTree);

      add(dbScrollPane, java.awt.BorderLayout.CENTER);

      jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      jPanel1.setMinimumSize(new java.awt.Dimension(100, 30));
      jPanel1.setPreferredSize(new java.awt.Dimension(100, 30));

      btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/windows/databases/refresh.png"))); // NOI18N
      btnRefresh.setContentAreaFilled(false);
      btnRefresh.setPreferredSize(new java.awt.Dimension(25, 25));
      btnRefresh.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnRefreshActionPerformed(evt);
         }
      });

      btnBrowse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/windows/databases/browse.png"))); // NOI18N
      btnBrowse.setContentAreaFilled(false);
      btnBrowse.setPreferredSize(new java.awt.Dimension(25, 25));
      btnBrowse.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnBrowseActionPerformed(evt);
         }
      });

      btnDataEntry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/windows/databases/dataentry.png"))); // NOI18N
      btnDataEntry.setContentAreaFilled(false);
      btnDataEntry.setPreferredSize(new java.awt.Dimension(25, 25));
      btnDataEntry.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnDataEntryActionPerformed(evt);
         }
      });

      btnSetDefault.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/windows/databases/default.png"))); // NOI18N
      btnSetDefault.setContentAreaFilled(false);
      btnSetDefault.setPreferredSize(new java.awt.Dimension(25, 25));
      btnSetDefault.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnSetDefaultActionPerformed(evt);
         }
      });

      btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/windows/databases/close.png"))); // NOI18N
      btnClose.setContentAreaFilled(false);
      btnClose.setPreferredSize(new java.awt.Dimension(25, 25));
      btnClose.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnCloseActionPerformed(evt);
         }
      });

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnDataEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnSetDefault, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(247, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(btnDataEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(btnSetDefault, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      add(jPanel1, java.awt.BorderLayout.NORTH);
   }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        closeDb();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void btnSetDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetDefaultActionPerformed
        setDefault();
    }//GEN-LAST:event_btnSetDefaultActionPerformed

    private void btnDataEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataEntryActionPerformed
        dataEntry(true);
    }//GEN-LAST:event_btnDataEntryActionPerformed

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        browse(true);
    }//GEN-LAST:event_btnBrowseActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refresh();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void dbPoolTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dbPoolTreeMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            int x = evt.getX();
            int y = evt.getY();
            dbPopup.show(dbPoolTree, x, y);
        }
    }//GEN-LAST:event_dbPoolTreeMouseClicked
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton btnBrowse;
   private javax.swing.JButton btnClose;
   private javax.swing.JButton btnDataEntry;
   private javax.swing.JButton btnRefresh;
   private javax.swing.JButton btnSetDefault;
   private javax.swing.JTree dbPoolTree;
   private javax.swing.JPopupMenu dbPopup;
   private javax.swing.JScrollPane dbScrollPane;
   private javax.swing.JPanel jPanel1;
   // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     * @return 
     */
    public static synchronized DbTopComponent getDefault() {
        if (instance == null) {
            instance = new DbTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DbTopComponent instance. Never call {@link #getDefault} directly!
     * @return 
     */
    public static synchronized DbTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find Db component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DbTopComponent) {
            return (DbTopComponent) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
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
    protected  void 	componentActivated() {
       refresh();
    }
    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream
     * @return  */
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

        public Object readResolve() {
            return DbTopComponent.getDefault();
        }
    }
}
