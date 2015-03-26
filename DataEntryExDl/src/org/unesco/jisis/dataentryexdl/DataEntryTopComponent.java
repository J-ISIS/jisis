package org.unesco.jisis.dataentryexdl;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import org.apache.commons.io.FileUtils;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.UserInfo;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.*;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.picklist.ValidationData;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;
import org.unesco.jisis.jisisutils.threads.GuiExecutor;


/**
 * Top component which displays something.
 */
public class DataEntryTopComponent extends TopComponent implements Observer {

   private IRecord currRec_ = null;
   private IRecord dbRec_ = null; // New Instance of the record as on db
   private boolean showEmptyFields_ = true;
   private javax.swing.JList termsList_;
   private ClientDatabaseProxy db_ = null;
   private EditPanel editPanel_;
   private ComponentOrientation orientation_ = ComponentOrientation.LEFT_TO_RIGHT;
   private static DataEntryTopComponent instance;
   /** path to the icon used by the component and its open action */
   static final String ICON_PATH = "org/unesco/jisis/dataentryexdl/edit.png";
   /** path to the icon used by the component and its open action */
//   static final String DOUBLE_LEFT_PATH = "org/unesco/jisis/database/explorer/2leftarrow.png";
//   static final String ONE_LEFT_PATH = "org/unesco/jisis/database/explorer/1leftarrow.png";
//   static final String ONE_RIGHT_PATH = "org/unesco/jisis/database/explorer/1rightarrow.png";
//   static final String DOUBLE_RIGHT_PATH = "org/unesco/jisis/database/explorer/2rightarrow.png";
//   static final String NEW_PATH = "org/unesco/jisis/dataentryexdl/new.png";
//   static final String SAVE_PATH = "org/unesco/jisis/dataentryexdl/save.png";
//   static final String DEL_PATH = "org/unesco/jisis/dataentryexdl/delete.png";
   private static final String PREFERRED_ID = "DataEntryTopComponent";
   private List<PickListData> pickListDataList_;
   private List<ValidationData> validationDataList_;

   /*--------------------------------------------------------
    * NOTE: It is quite important to read the wks and fdt here
    * to avoid re-reading in the called objects
    * --------------------------------------------------------
    */
   private FieldDefinitionTable fdt_ = null;
   private WorksheetDef wks_ = null;
   
   private UserInfo userInfo_;
   
   private UndoRedo.Manager undoRedomanager_ = new UndoRedo.Manager();

   public DataEntryTopComponent(IDatabase db) {
       if (db instanceof ClientDatabaseProxy) {
           db_ = (ClientDatabaseProxy) db;
           try {
              String wksName = db_.getDatabaseName();
               fdt_ = db_.getFieldDefinitionTable();
               pickListDataList_ = db_.getPickListData(wksName);
               validationDataList_ = db_.getValidationData(wksName);
           } catch (DbException ex) {
               Exceptions.printStackTrace(ex);
           }
       } else {
         throw new RuntimeException(" DataEntryTopComponent: Cannot cast DB to ClientDatabaseProxy");
       }
      /* Register this TopComponent as attached to this DB */
      db_.addWindow(this);
      initComponents();
      
        IConnection conn = ConnectionPool.getDefaultConnection();
        userInfo_ = conn.getUserInfo();
            
      //setName(NbBundle.getMessage(DataEntryTopComponent.class, "CTL_DataEntryTopComponent"));
      setToolTipText(NbBundle.getMessage(DataEntryTopComponent.class, "HINT_DataEntryExDlTopComponent"));
      setIcon(ImageUtilities.loadImage(ICON_PATH, true));

//      btnFirst.setIcon(new ImageIcon(ImageUtilities.loadImage(DOUBLE_LEFT_PATH, true)));
//      btnPrev.setIcon(new ImageIcon(ImageUtilities.loadImage(ONE_LEFT_PATH, true)));
//      btnNext.setIcon(new ImageIcon(ImageUtilities.loadImage(ONE_RIGHT_PATH, true)));
//      btnLast.setIcon(new ImageIcon(ImageUtilities.loadImage(DOUBLE_RIGHT_PATH, true)));
//      btnNew.setIcon(new ImageIcon(ImageUtilities.loadImage(NEW_PATH, true)));
//      btnSave.setIcon(new ImageIcon(ImageUtilities.loadImage(SAVE_PATH, true)));
//      btnDel.setIcon(new ImageIcon(ImageUtilities.loadImage(DEL_PATH, true)));
      //dataPanel.addMouseListener(new mL());
      try {
         String[] worksheetNames = db_.getWorksheetNames();
         if (worksheetNames == null) {
            String label = NbBundle.getMessage(DataEntryTopComponent.class,
                    "MSG_DatabaseWithoutAnyWorksheets");
            String title = NbBundle.getMessage(DataEntryTopComponent.class,
                    "MSG_DataEntryErrorDialogTitle");
            NotifyDescriptor d =
               new NotifyDescriptor.Confirmation(label, title,
               NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;

         }
         cmbWKS.setModel(new DefaultComboBoxModel(worksheetNames));

         int iWKS = -1;
         for (int i=0; i< worksheetNames.length; i++) {
            WorksheetDef wd = db_.getWorksheetDef( worksheetNames[i]);
            int fieldCount = wd.getFieldsCount();
            if (fieldCount >0) {
               iWKS = i;
               break;
            }
         }
         if (iWKS == -1) {
            String label = NbBundle.getMessage(DataEntryTopComponent.class,
                    "MSG_DatabaseWithEmptyWorksheets");
            String title = NbBundle.getMessage(DataEntryTopComponent.class,
                    "MSG_DataEntryErrorDialogTitle");
            NotifyDescriptor d =
               new NotifyDescriptor.Confirmation(label, title,
               NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
         }
         cmbWKS.setSelectedItem(iWKS);

         setName(NbBundle.getMessage(DataEntryTopComponent.class, "CTL_DataEntryExDlTopComponent")
                 + " (" +db.getDbHome()+"//"+ db_.getDatabaseName() + ")");
         newRecord();
      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }
        Action deleteFieldAction = new AbstractAction() {
           public void actionPerformed(ActionEvent e) {
               RepeatableField source = (RepeatableField) e.getSource();
               
               //do nothing
           }
       };
      getInputMap().put(KeyStroke.getKeyStroke("F2"),
                            "deleteField");
      getActionMap().put("deleteField",
                             deleteFieldAction);
   }

   @Override
   public UndoRedo getUndoRedo() {
      return undoRedomanager_;
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        dataEntryPanel = new javax.swing.JPanel();
        dataPanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        toolbarNavigation = new javax.swing.JToolBar();
        lblMFN = new javax.swing.JLabel();
        txtMFN = new javax.swing.JTextField();
        btnFirst = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        toolbarCRUD = new javax.swing.JToolBar();
        btnNew = new javax.swing.JButton();
        btnDel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        toolbarWKS = new javax.swing.JToolBar();
        lblWKS = new javax.swing.JLabel();
        cmbWKS = new javax.swing.JComboBox();
        toolbarRTL = new javax.swing.JToolBar();
        btnRTL = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        btnVAL = new javax.swing.JButton();
        toolbarOptions = new javax.swing.JToolBar();
        btnReload = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btnCreateCopy = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnClear = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnCopy = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        btnPaste = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnShowHide = new javax.swing.JButton();

        mainPanel.setLayout(new java.awt.BorderLayout());

        dataPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout dataEntryPanelLayout = new javax.swing.GroupLayout(dataEntryPanel);
        dataEntryPanel.setLayout(dataEntryPanelLayout);
        dataEntryPanelLayout.setHorizontalGroup(
            dataEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataEntryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 946, Short.MAX_VALUE)
                .addContainerGap())
        );
        dataEntryPanelLayout.setVerticalGroup(
            dataEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dataEntryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainPanel.add(dataEntryPanel, java.awt.BorderLayout.CENTER);

        controlPanel.setPreferredSize(new java.awt.Dimension(100, 90));

        toolbarNavigation.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        toolbarNavigation.setFloatable(false);
        toolbarNavigation.setRollover(true);

        lblMFN.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblMFN, org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "LBL_MFN_1")); // NOI18N
        toolbarNavigation.add(lblMFN);

        txtMFN.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtMFN.setText("1");
        txtMFN.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_MFN")); // NOI18N
        txtMFN.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMFNKeyPressed(evt);
            }
        });
        toolbarNavigation.add(txtMFN);

        btnFirst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryexdl/2leftarrow.png"))); // NOI18N
        btnFirst.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_First")); // NOI18N
        btnFirst.setPreferredSize(new java.awt.Dimension(30, 25));
        btnFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirstActionPerformed(evt);
            }
        });
        toolbarNavigation.add(btnFirst);

        btnPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryexdl/1leftarrow.png"))); // NOI18N
        btnPrev.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_Prev")); // NOI18N
        btnPrev.setPreferredSize(new java.awt.Dimension(30, 25));
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });
        toolbarNavigation.add(btnPrev);

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryexdl/1rightarrow.png"))); // NOI18N
        btnNext.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_Next")); // NOI18N
        btnNext.setPreferredSize(new java.awt.Dimension(30, 25));
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        toolbarNavigation.add(btnNext);

        btnLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryexdl/2rightarrow.png"))); // NOI18N
        btnLast.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_Last")); // NOI18N
        btnLast.setPreferredSize(new java.awt.Dimension(30, 25));
        btnLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastActionPerformed(evt);
            }
        });
        toolbarNavigation.add(btnLast);

        toolbarCRUD.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        toolbarCRUD.setFloatable(false);
        toolbarCRUD.setRollover(true);

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryexdl/new.png"))); // NOI18N
        btnNew.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_New")); // NOI18N
        btnNew.setPreferredSize(new java.awt.Dimension(30, 25));
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });
        toolbarCRUD.add(btnNew);

        btnDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryexdl/delete.png"))); // NOI18N
        btnDel.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_Del")); // NOI18N
        btnDel.setPreferredSize(new java.awt.Dimension(30, 25));
        btnDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelActionPerformed(evt);
            }
        });
        toolbarCRUD.add(btnDel);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryexdl/save.png"))); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_Save")); // NOI18N
        btnSave.setPreferredSize(new java.awt.Dimension(30, 25));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        toolbarCRUD.add(btnSave);

        toolbarWKS.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        toolbarWKS.setFloatable(false);
        toolbarWKS.setRollover(true);

        lblWKS.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblWKS, org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "LBL_WKS_1")); // NOI18N
        toolbarWKS.add(lblWKS);

        cmbWKS.setToolTipText(org.openide.util.NbBundle.getMessage(DataEntryTopComponent.class, "HINT_WKSSel")); // NOI18N
        cmbWKS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbWKSActionPerformed(evt);
            }
        });
        toolbarWKS.add(cmbWKS);

        toolbarRTL.setBorder(new javax.swing.border.MatteBorder(null));
        toolbarRTL.setFloatable(false);
        toolbarRTL.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(btnRTL, "RTL");
        btnRTL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRTLActionPerformed(evt);
            }
        });
        toolbarRTL.add(btnRTL);
        toolbarRTL.add(jSeparator6);

        org.openide.awt.Mnemonics.setLocalizedText(btnVAL, "VAL");
        btnVAL.setToolTipText("Apply the validation rules if any");
        btnVAL.setFocusable(false);
        btnVAL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVAL.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnVAL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValidationActionPerformed(evt);
            }
        });
        toolbarRTL.add(btnVAL);

        toolbarOptions.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        toolbarOptions.setFloatable(false);
        toolbarOptions.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(btnReload, "Reload");
        btnReload.setToolTipText("Cancels all the changes made and restores the record to its initial status.");
        btnReload.setFocusable(false);
        btnReload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });
        toolbarOptions.add(btnReload);
        toolbarOptions.add(jSeparator4);

        org.openide.awt.Mnemonics.setLocalizedText(btnCreateCopy, "Create a copy");
        btnCreateCopy.setToolTipText("Creates a new record with the same contents of the current one. The created record is assigned the next available MFN.");
        btnCreateCopy.setFocusable(false);
        btnCreateCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCreateCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCreateCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateCopyActionPerformed(evt);
            }
        });
        toolbarOptions.add(btnCreateCopy);
        toolbarOptions.add(jSeparator3);

        org.openide.awt.Mnemonics.setLocalizedText(btnClear, "Clear");
        btnClear.setToolTipText("Clears the contents of all the fields in the worksheet. ");
        btnClear.setFocusable(false);
        btnClear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        toolbarOptions.add(btnClear);
        toolbarOptions.add(jSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(btnCopy, "Copy");
        btnCopy.setToolTipText("Copy the current record in the stack.");
        btnCopy.setFocusable(false);
        btnCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyActionPerformed(evt);
            }
        });
        toolbarOptions.add(btnCopy);
        toolbarOptions.add(jSeparator5);

        org.openide.awt.Mnemonics.setLocalizedText(btnPaste, "Paste");
        btnPaste.setToolTipText("Paste a record from the stack");
        btnPaste.setFocusable(false);
        btnPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPasteActionPerformed(evt);
            }
        });
        toolbarOptions.add(btnPaste);
        toolbarOptions.add(jSeparator2);

        org.openide.awt.Mnemonics.setLocalizedText(btnShowHide, "Show/Hide Empty Fields");
        btnShowHide.setToolTipText("show (or remove) empty fields from the display");
        btnShowHide.setFocusable(false);
        btnShowHide.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowHide.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowHide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowHideActionPerformed(evt);
            }
        });
        toolbarOptions.add(btnShowHide);

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(toolbarWKS, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(118, 118, 118)
                        .addComponent(toolbarNavigation, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(toolbarCRUD, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(toolbarRTL, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(toolbarOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(toolbarRTL, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toolbarWKS, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toolbarCRUD, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toolbarNavigation, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toolbarOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainPanel.add(controlPanel, java.awt.BorderLayout.NORTH);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbWKSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbWKSActionPerformed
       setRecord(currRec_);
    }//GEN-LAST:event_cmbWKSActionPerformed

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed

        long mfn = currRec_.getMfn();
        if (mfn == 0) {
           return;
        }
        String label = NbBundle.getMessage(DataEntryTopComponent.class, "MSG_DeleteRecLabel");
        String title = NbBundle.getMessage(DataEntryTopComponent.class, "MSG_DeleteRecDialogTitle");

        NotifyDescriptor d =
                new NotifyDescriptor.Confirmation(label, title,
                NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            deleteRecord();
            String msg = NbBundle.getMessage(DataEntryTopComponent.class,
                    "MSG_RecordSuccessfullyDeleted")+" MFN ="+mfn;
            GuiGlobal.output(msg);
        }
    }//GEN-LAST:event_btnDelActionPerformed

    private void txtMFNKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMFNKeyPressed
       if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
           if (!canChangeRecord()) {
              txtMFN.setText(currRec_.getMfn() + "");
             return;
          }
          long mfn = 0;
          try {
             mfn = Long.parseLong(txtMFN.getText());
          } catch (Exception e) {
             String msg = NbBundle.getMessage(DataEntryTopComponent.class, "MSG_MfnParseError");
             DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
             setRecord(currRec_);
             return;
          }
         try {
            if (mfn <= 0 || mfn > db_.getLastMfn()) {
               String msg = NbBundle.getMessage(DataEntryTopComponent.class, "MSG_MfnInvalid");
               DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
               setRecord(currRec_);
               return;
            }
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
          try {
             IRecord rec = db_.getRecordCursor(mfn);
             if (rec != null) {
                
                setRecord(rec);
             }
          } catch (DbException ex) {
             new RecordNotFoundException(ex).displayWarning();
          }
       }

    }//GEN-LAST:event_txtMFNKeyPressed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        saveRecord();
        
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
       if (!canChangeRecord()) {
          return;
       }
       newRecord();
    }//GEN-LAST:event_btnNewActionPerformed

    private void btnLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastActionPerformed
        if (!canChangeRecord()) {
          return;
        }
       try {
          IRecord rec = db_.getLast();
          if (rec != null) {
            
             setRecord(rec);
          }
       } catch (DbException ex) {
          new RecordNotFoundException(ex).displayWarning();
       }
    }//GEN-LAST:event_btnLastActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
       if (!canChangeRecord()) {
          return;
       }
       if (currRec_.getMfn() == 0) {
           return;
        }
       try {
          IRecord rec = db_.getNext();
          if (rec != null) {
             
             setRecord(rec);
          }
       } catch (DbException ex) {
          new RecordNotFoundException(ex).silent();
       }
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        if (!canChangeRecord()) {
          return;
        }
        if (currRec_.getMfn() == 0) {
           return;
        }
       try {
          IRecord rec = db_.getPrev();
          if (rec != null) {
            
             setRecord(rec);
          }

       } catch (DbException ex) {
          new RecordNotFoundException(ex).silent();
       }
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirstActionPerformed
       if (!canChangeRecord()) {
          return;
       }
       try {
          IRecord rec = db_.getFirst();
          if (rec != null) {
            
             setRecord(rec);
          }
       } catch (DbException ex) {
          new RecordNotFoundException(ex).displayWarning();
       }
    }//GEN-LAST:event_btnFirstActionPerformed

//    private static void applyComponentOrientation(Component c, ComponentOrientation o) {
//
//    c.setComponentOrientation(o);
//    if (c instanceof EditEntry ) {
//       EditEntry ee = (EditEntry) c;
//       ee.updateUI();
//    }
//    if (c instanceof RepeatableField ) {
//       RepeatableField rf = (RepeatableField) c;
//       rf.setCaretPosition(0);
//       rf.updateUI();
//    }
//
//     //if (c instanceof Container) {
//      Container container = (Container)c;
//      int ncomponents = container.getComponentCount();
//      for (int i = 0 ; i < ncomponents ; ++i) {
//        applyComponentOrientation( container.getComponent(i), o );
//      }
//    //}
//  }
    private void btnRTLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRTLActionPerformed
       orientation_ = (orientation_== ComponentOrientation.LEFT_TO_RIGHT) ?
               ComponentOrientation.RIGHT_TO_LEFT
               : ComponentOrientation.LEFT_TO_RIGHT;
       editPanel_.applyComponentOrientation(orientation_);
       editPanel_.setOrientation(orientation_);
       //editPanel_.refresh();

}//GEN-LAST:event_btnRTLActionPerformed

    private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
       currRec_ = dbRec_;
       setRecord(currRec_);
    }//GEN-LAST:event_btnReloadActionPerformed

    private void btnCreateCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateCopyActionPerformed
       createCopyRecord();
        String msg = NbBundle.getMessage(DataEntryTopComponent.class,
                "MSG_RecordSuccessfullyCopied")+ ""+currRec_.getMfn();
        GuiGlobal.output(msg);
    }//GEN-LAST:event_btnCreateCopyActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
       currRec_.clear();
       setRecord(currRec_);
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_btnCopyActionPerformed

    private void btnPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasteActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_btnPasteActionPerformed

    private void btnShowHideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowHideActionPerformed
       showEmptyFields_ = !showEmptyFields_;
       setRecord(currRec_);
    }//GEN-LAST:event_btnShowHideActionPerformed

    private boolean applyValidationRules() {
      boolean validationOK = true;
      if (validationDataList_ != null) {
         for (ValidationData validationData : validationDataList_) {
            if (validationData.getType() == ValidationData.RECORD_EDIT_BEGIN
                    || validationData.getType() == ValidationData.RECORD_EDIT_END) {
               continue;
            }

            try {
               String valFormat = validationData.getFormat();
               if ((valFormat != null) && (valFormat.length() > 0)) {
                  // Interpret the format
                  ISISFormatter formatter = ISISFormatter.getFormatter(valFormat);
                  if (formatter == null) {
                     GuiGlobal.output("Error on following Validation Rule:\n" + valFormat);
                     GuiGlobal.output(ISISFormatter.getParsingError());
                     continue;
                  } else if (formatter.hasParsingError()) {
                     GuiGlobal.output("Error on following Validation Rule:\n" + valFormat);
                     GuiGlobal.output(ISISFormatter.getParsingError());
                     continue;
                  }
                  // Execute format with current record
                  formatter.setRecord(db_, currRec_);
                  formatter.eval();
                  String result = formatter.getText();
                  // In case validation rule not respected, we expect an error msg
                  if (result.length() > 0) {
                     new ValidationFailedException(result).displayWarning();
                     validationOK = false;
                     continue;

                  }
               }
            } catch (RuntimeException re) {
               new FormattingException(re.getMessage()).displayWarning();
               //return false;
               continue;
            }
         }
      }
      return validationOK;
   }
   private void btnValidationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnValidationActionPerformed
     
      applyValidationRules();
       
   }//GEN-LAST:event_btnValidationActionPerformed

   private void newRecord() {

         IRecord record = Record.createRecord();
         setRecord(record /*db_.addNewRecord()*/);
   }
   /**
    * Save a single document on the server side
    * @param path 
    */
    private void saveDocument(final String path) {
        final File f = new File(path);

        // Start you operation here
       final Runnable saveDocumentRun = new Runnable() {
          public void run() {

             final byte[] content;
             try {
                // Your operation here 
                content = FileUtils.readFileToByteArray(f);
                db_.saveDocument(f.getName(), content);
                GuiExecutor.instance().execute(new Runnable() {
                   @Override
                   public void run() {
                      String msg = NbBundle.getMessage(DataEntryTopComponent.class,
                              "MSG_SavingDocumentOnServer") + " " + path;
                      GuiGlobal.output(msg);


                      msg = NbBundle.getMessage(DataEntryTopComponent.class,
                              "MSG_DocumentSavedOnServer");
                      GuiGlobal.output(msg);
                   }
                });

             } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                // Report error
             } finally {
                // clear status text
             }
          }
       };

       ProgressUtils.showProgressDialogAndRun(saveDocumentRun, "Saving documents on server ..." + path + " Please wait");
     
        
    }
    
    
   
   private void saveDocuments() {

      /**
       * As the documents will be saved on the server side, we will use relative
       * URLs. We will replace relative URLs by absolute URLs when the record is
       * sent from the server to the client.
       *
       */
      Record record = (Record) currRec_;
      try {
         int nFields = record.getFieldCount();
         for (int i = 0; i < nFields; i++) {
            IField field = record.getFieldByIndex(i);
            int fieldType = field.getType();
            if (fieldType == Global.FIELD_TYPE_DOC && field.hasOccurrences()) {
               // Copy the document to the server
               // The path is in the 2nd occurrence
               if (field.getOccurrenceCount() != 2) {
                  continue;
               }
               String path = (String) field.getOccurrence(1).getValue();
               if (path.startsWith("<a href=")) {
                  // Document already on server and server url already built!
               } else {
                  saveDocument(path);
                  String fileName = new File(path).getName();
                  String backslash = System.getProperty("file.separator");
                  path = path.replace(backslash, "/");
                  String serverUri = "./" + db_.getDatabaseName() + "/idocs/" + fileName;
                  String serverUrl = "<a href=\"" + serverUri + "\">" + fileName + "</a>";
                  //String url = "<a href=\"file://" + path + "\">" + path + "</a>";
                  field.setOccurrence(1, serverUrl);
               }
            }
         }
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   private void saveRecord() {
      if (!applyValidationRules()) {
         String msg = NbBundle.getMessage(DataEntryTopComponent.class,
                 "MSG_RecordNotValidated");
         GuiGlobal.output(msg);
         return;
      }
      try {
         saveDocuments();
         Record record = (Record) currRec_;
         record.removeEmptyFields();
         currRec_ = db_.updateRecord(record);
         // Be sure to be on this record
         db_.getRecordCursor(currRec_.getMfn());
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
      // Calling setRecord is too time consuming
       setRecord(currRec_);
       txtMFN.setText(currRec_.getMfn() + "");
       editPanel_.setUnModified();

      String msg = NbBundle.getMessage(DataEntryTopComponent.class,
              "MSG_RecordSuccessfullySaved") + " MFN=" + currRec_.getMfn();
      GuiGlobal.output(msg);

   }
    private void createCopyRecord() {

         try {
            currRec_.setMfn(0l);
            currRec_ = db_.addRecord((Record) currRec_);
            // Be sure to be on this record
            db_.getRecordCursor(currRec_.getMfn());
         } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
         }
         setRecord(currRec_);
   }

   private void deleteRecord() {
      long mfn = currRec_.getMfn();
      try {
         db_.deleteRecord(mfn);
         newRecord();
      } catch (DbException ex) {
         new RecordNotDeleted(ex).displayWarning();
      }
   }
   /**
    * Unlock the record if we are the owner
    * @param record 
    */
   private void unlockRecord(IRecord record) {
      
         if (record != null && record.getMfn() != 0) {
            try {
               int lockStatus = db_.getRecordLockStatus(record.getMfn(), userInfo_);
               if (lockStatus == Global.RECORD_LOCK_OWNED) {
                  UserInfo lockOwner = db_.recordLockOwner(record.getMfn());
                  if (lockOwner.equals(userInfo_)) {
                     // Unlock currRec_
                     db_.unlockRecord(record.getMfn(), userInfo_);
                  }
               }
            } catch (DbException ex) {
               Exceptions.printStackTrace(ex);
            }
         }
   }
   /**
    * --------------------------------------------------------------------------
    * setRecord - core method that displays the data entry form
    * --------------------------------------------------------------------------
    * This method displays the original database record for editing. 
    * @param record - The record to edit. Contains the db record data if we are
    *                 updating, or empty data if we are vreating a new record.
    */

   private void setRecord(IRecord record) {
      if (record != null) {
         // Unlock Previous record displayed if needed 
         unlockRecord(currRec_);
         boolean editEnable = true;
         currRec_ = record;
         if (currRec_.getMfn() != 0) {
            try {
               int lockStatus = db_.getRecordLockStatus(currRec_.getMfn(), userInfo_);
               if (lockStatus == Global.RECORD_LOCKED || lockStatus ==Global.RECORD_LOCK_OWNED) {
                  UserInfo lockOwner = db_.recordLockOwner(currRec_.getMfn());
                  String label = NbBundle.getMessage(DataEntryTopComponent.class,
                          "MSG_RecordLockedByUser", currRec_.getMfn(), lockOwner);
                  String title = NbBundle.getMessage(DataEntryTopComponent.class,
                          "MSG_DataEntryRecordLockedTitle");
                  NotifyDescriptor d =
                          new NotifyDescriptor.Confirmation(label, title,
                          NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                  DialogDisplayer.getDefault().notify(d);
                  editEnable = false;
               } else {
                  db_.lockRecord(currRec_.getMfn(), userInfo_);
               }
            } catch (DbException ex) {
               Exceptions.printStackTrace(ex);
            }
         }
        
         dbRec_ = Record.newInstance(record);
         txtMFN.setText(currRec_.getMfn() + "");
         dataPanel.removeAll();
         String wksName = cmbWKS.getSelectedItem().toString();
         try {
            wks_ = db_.getWorksheetDef(wksName);
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
         // Show empty fields if we are creating a new record
         if (currRec_.getMfn() == 0) {
            showEmptyFields_ = true;
         }
         // We send editPanel_ a reference to currRec_
         editPanel_ = new EditPanel(db_, fdt_, wks_, pickListDataList_,
                 validationDataList_, currRec_, showEmptyFields_, undoRedomanager_);
         editPanel_.setUnModified();
         if (!editEnable) {
            editPanel_.desableEdit();
         }
         
         JScrollPane scrollPane = new JScrollPane(editPanel_);
         scrollPane.getVerticalScrollBar().setUnitIncrement(16);
         dataPanel.add(scrollPane, BorderLayout.CENTER);
         dataPanel.updateUI();
          
      }
   }
   @Override
   protected void componentShowing() {
      System.out.println("componentShowing");
      super.componentShowing();
      if (currRec_ != null && currRec_.getMfn() != 0) {
         // Be sure to be synchronized with content of currRec_ 
         try {
            db_.getRecordCursor(currRec_.getMfn());
         } catch (DbException ex) {
            new RecordNotFoundException(ex).displayWarning();
         }
      }

   }
   private boolean isRecordChanged() {
      if (currRec_.equals(dbRec_)) {
         return false;
      }
      return true;
      
   }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnCopy;
    private javax.swing.JButton btnCreateCopy;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnFirst;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPaste;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnRTL;
    private javax.swing.JButton btnReload;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnShowHide;
    private javax.swing.JButton btnVAL;
    private javax.swing.JComboBox cmbWKS;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JPanel dataEntryPanel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JLabel lblMFN;
    private javax.swing.JLabel lblWKS;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar toolbarCRUD;
    private javax.swing.JToolBar toolbarNavigation;
    private javax.swing.JToolBar toolbarOptions;
    private javax.swing.JToolBar toolbarRTL;
    private javax.swing.JToolBar toolbarWKS;
    private javax.swing.JTextField txtMFN;
    // End of variables declaration//GEN-END:variables
   /**
    * Gets default instance. Do not use directly: reserved for *.settings files only,
    * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
    * To obtain the singleton instance, use {@link findInstance}.
    */
   public static synchronized DataEntryTopComponent getDefault() throws DefaultDBNotFoundException {

      if (instance != null) {
         instance.close();
         instance = null;
      }
      ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
      if (connectionInfo.getDefaultDatabase() != null) {
         instance = new DataEntryTopComponent(connectionInfo.getDefaultDatabase());
      }
      return instance;
   }

   /**
    * Obtain the DataEntryTopComponent instance. Never call {@link #getDefault} directly!
    */
   public static synchronized DataEntryTopComponent findInstance() throws DefaultDBNotFoundException {
      TopComponent win = getDefault();
      if (win == null) {
         ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find DataEntry component. It will not be located properly in the window system.");
         return getDefault();
      }
      if (win instanceof DataEntryTopComponent) {
         return (DataEntryTopComponent) win;
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
   public void componentClosed() {
   // TODO add custom code on component closing
      db_.deleteWindow(this);
      unlockRecord(currRec_);
   }
   @Override
   public boolean canClose() {
      if (!checkModifiedRecordSaved()) {
         return false;
      }
      return true;
   }
   
   public boolean canChangeRecord() {
      if (!checkModifiedRecordSaved()) {
         return false;
      }
      return true;
   }
   
   /**
    * 
    * @return 
    */
   private boolean checkModifiedRecordSaved() {

      if (editPanel_.isRecordModified()) {
         NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                 "Current Record was modified!\nDo you want to save it?",
                 NotifyDescriptor.YES_NO_CANCEL_OPTION,
                 NotifyDescriptor.QUESTION_MESSAGE);

         Object option = DialogDisplayer.getDefault().notify(nd);
         if (option == NotifyDescriptor.CANCEL_OPTION) {
            // Do nothing
            return false;

         } else if (option == NotifyDescriptor.YES_OPTION) {
           saveRecord();
           return false;
         }
      }
      return true;
   }

   /** replaces this in object stream */
   /*
   public Object writeReplace() {
   return new ResolvableHelper();
   }
    */
   @Override
   protected String preferredID() {
      return PREFERRED_ID;
   }

    public void update(Observable o, Object arg) {
        if (db_.databaseHasChanged()) {
            try {

                long mfn = db_.getCurrentRecordMfn();
               
                // Be sure to be on this record
                currRec_ = db_.getRecordCursor(mfn);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            setRecord(currRec_);

        }
    }

   /*
   final static class ResolvableHelper implements Serializable {
   private static final long serialVersionUID = 1L;
   public Object readResolve() {
   try {
   return DataEntryTopComponent.getDefault();
   } catch (DBNotFoundException ex) {
   NotifyDescriptor d =
   new NotifyDescriptor.Message("No Default Database selected. Please, select on from Databases Pool window.", NotifyDescriptor.WARNING_MESSAGE);
   DialogDisplayer.getDefault().notify(d);
   return null;
   }
   }
   }
    */

//   private class mL extends MouseAdapter {
//
//      @Override
//      public void mouseClicked(MouseEvent e) {
//
//      // setText(e.getClass().getName());
//      }
//   }
}
