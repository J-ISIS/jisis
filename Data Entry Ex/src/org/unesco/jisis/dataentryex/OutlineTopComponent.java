/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.*;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.picklist.ValidationData;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
final class OutlineTopComponent extends TopComponent implements Observer {

   private ClientDatabaseProxy db_;
   private FieldDefinitionTable fdt_ = null;
   private WorksheetDef wks_;
  
   private long currentMfn_ = 0;
   private IRecord currRec_ = null;
   private IRecord dbRec_ = null; // New Instance of the record as on db
   private boolean showEmptyFields_ = false;
   private ComponentOrientation orientation_;
   private static OutlineTopComponent instance;
   /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
   private static final String PREFERRED_ID = "OutlineTopComponent";
 
   private  DataEntryPanel dataEntryPanel = null;
   private boolean recordChanged_ = false;
   private Mediator med = new Mediator();
   
   private List<PickListData> pickListDataList_;
   private List<ValidationData> validationDataList_;
   
   static List<IRecord> recordStack_ = new ArrayList<IRecord>();
   
   private boolean wksError = false;

   public OutlineTopComponent(IDatabase db) {

       if (db instanceof ClientDatabaseProxy) {
           db_ = (ClientDatabaseProxy) db;
           try {
               fdt_ = db_.getFieldDefinitionTable();

             

           } catch (DbException ex) {
               Exceptions.printStackTrace(ex);
           }
       } else {
           throw new RuntimeException("OutlineTopComponent: Cannot cast DB to ClientDatabaseProxy");
       }
       wksError = false;
       
       try {
           /**
            * Check 1st if worksheets exists
            */
           String[] worksheetNames = db_.getWorksheetNames();
           if (worksheetNames == null) {
               String label = NbBundle.getMessage(OutlineTopComponent.class,
                   "MSG_DatabaseWithoutAnyWorksheets");
               String title = NbBundle.getMessage(OutlineTopComponent.class,
                   "MSG_DataEntryErrorDialogTitle");
               NotifyDescriptor d
                   = new NotifyDescriptor.Confirmation(label, title,
                       NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
               DialogDisplayer.getDefault().notify(d);
               wksError = true;
               return;

           }
           int iWKS = -1;
           for (int i = 0; i < worksheetNames.length; i++) {
               WorksheetDef wd = db_.getWorksheetDef(worksheetNames[i]);
               int fieldCount = wd.getFieldsCount();
               if (fieldCount > 0) {
                   iWKS = i;
                   break;
               }
           }
           if (iWKS == -1) {
               String label = NbBundle.getMessage(OutlineTopComponent.class,
                   "MSG_DatabaseWithEmptyWorksheets");
               String title = NbBundle.getMessage(OutlineTopComponent.class,
                   "MSG_DataEntryErrorDialogTitle");
               NotifyDescriptor d
                   = new NotifyDescriptor.Confirmation(label, title,
                       NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
               DialogDisplayer.getDefault().notify(d);
                wksError = true;
               return;
           }
           /* Register this TopComponent as attached to this DB */
           db_.addWindow(this);
           initComponents();
           setName(NbBundle.getMessage(OutlineTopComponent.class, "CTL_OutlineTopComponent"));
           setToolTipText(NbBundle.getMessage(OutlineTopComponent.class, "HINT_OutlineTopComponent"));

           cmbWKS.setModel(new DefaultComboBoxModel(worksheetNames));
           cmbWKS.setSelectedItem(iWKS);
         
           setName(NbBundle.getMessage(OutlineTopComponent.class, "CTL_OutlineTopComponent")
               + " (" + db.getDbHome() + "//" + db_.getDatabaseName() + ")");

      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }

      

      String wksName = cmbWKS.getSelectedItem().toString();
      try {
         wks_ = db_.getWorksheetDef(wksName);
         pickListDataList_ = db_.getPickListData(wksName);
         validationDataList_ = db_.getValidationData(wksName);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }

      dataEntryPanel = new DataEntryPanel(this, db, wks_, pickListDataList_, validationDataList_);
      recordPanel.add(dataEntryPanel,BorderLayout.CENTER);

      btnNew.setMnemonic(KeyEvent.VK_N);
      btnSave.setMnemonic(KeyEvent.VK_S);
      btnDel.setMnemonic(KeyEvent.VK_D);
      btnFirst.setMnemonic(KeyEvent.VK_F);
      btnPrev.setMnemonic(KeyEvent.VK_P);
      btnNext.setMnemonic(KeyEvent.VK_E);
      btnLast.setMnemonic(KeyEvent.VK_L);

      btnClear.setMnemonic(KeyEvent.VK_R);
      btnCopy.setMnemonic(KeyEvent.VK_C);
      btnCreateCopy.setMnemonic(KeyEvent.VK_Y);

      btnPaste.setMnemonic(KeyEvent.VK_A);

      btnRTL.setMnemonic(KeyEvent.VK_T);
      btnReload.setMnemonic(KeyEvent.VK_O);

      btnShowHide.setMnemonic(KeyEvent.VK_H);
      btnVAL.setMnemonic(KeyEvent.VK_V);
      
      
      newRecord();
      
   }
   public boolean getWksError() {
       return wksError;
   }

   private   class Mediator {
      private boolean newRecord = true;
      /**
       * Initial state when a DB record is displayed
       */
      public  void dbRecord() {
         // Disable reload until record changed
         btnReload.setEnabled(false);
         btnSave.setEnabled(false);
         btnNew.setEnabled(true);
         btnDel.setEnabled(true);
         newRecord = false;
         btnCreateCopy.setEnabled(true);
         btnCopy.setEnabled(true);
      }
      public void recordChanged() {
         if (!newRecord) {
            btnReload.setEnabled(true);
         }
         btnSave.setEnabled(true);
         btnCreateCopy.setEnabled(true);
         btnCopy.setEnabled(true);
      }
      public void newRecord() {
         btnReload.setEnabled(false);
         btnSave.setEnabled(false);
         btnNew.setEnabled(false);
         btnDel.setEnabled(false);
         btnCreateCopy.setEnabled(false);
         btnCopy.setEnabled(false);
         newRecord = true;
      }

   }

   public void setRecordChangedFlag(boolean value) {
      recordChanged_ = value;
      if (recordChanged_) {
         med.recordChanged();
         lblRecordChanged.setText("Record Changed and not Saved!!!");
         lblRecordChanged.setForeground(Color.red);
      } else {
         lblRecordChanged.setText("");
      }
   }

   public boolean getRecordChangedFlag() {
      return recordChanged_;
   }

  
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        navigationPanel = new javax.swing.JPanel();
        lblRecordChanged = new javax.swing.JLabel();
        wksPanel = new javax.swing.JPanel();
        lblWKS = new javax.swing.JLabel();
        cmbWKS = new javax.swing.JComboBox();
        browsePanel = new javax.swing.JPanel();
        lblMFN = new javax.swing.JLabel();
        txtMFN = new javax.swing.JTextField();
        btnFirst = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        crudPanel = new javax.swing.JPanel();
        btnNew = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDel = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        optionsPanel = new javax.swing.JPanel();
        btnReload = new javax.swing.JButton();
        btnCreateCopy = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnCopy = new javax.swing.JButton();
        btnPaste = new javax.swing.JButton();
        btnShowHide = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnRTL = new javax.swing.JButton();
        btnVAL = new javax.swing.JButton();
        btnMarc21FixedFieldEditor = new javax.swing.JButton();
        recordPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblRecordChanged, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.lblRecordChanged.text")); // NOI18N

        wksPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(lblWKS, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.lblWKS.text")); // NOI18N

        cmbWKS.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.cmbWKS.toolTipText")); // NOI18N
        cmbWKS.setMaximumSize(new java.awt.Dimension(150, 32767));
        cmbWKS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbWKSActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout wksPanelLayout = new javax.swing.GroupLayout(wksPanel);
        wksPanel.setLayout(wksPanelLayout);
        wksPanelLayout.setHorizontalGroup(
            wksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(wksPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWKS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbWKS, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );
        wksPanelLayout.setVerticalGroup(
            wksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(wksPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(wksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblWKS)
                    .addComponent(cmbWKS, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        browsePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(lblMFN, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.lblMFN.text")); // NOI18N

        txtMFN.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        txtMFN.setText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.txtMFN.text")); // NOI18N
        txtMFN.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.txtMFN.toolTipText")); // NOI18N
        txtMFN.setMaximumSize(new java.awt.Dimension(75, 2147483647));
        txtMFN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMFNActionPerformed(evt);
            }
        });
        txtMFN.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMFNKeyPressed(evt);
            }
        });

        btnFirst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryex/2leftarrow.png"))); // NOI18N
        btnFirst.setMnemonic(KeyEvent.VK_F);
        org.openide.awt.Mnemonics.setLocalizedText(btnFirst, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnFirst.text")); // NOI18N
        btnFirst.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnFirst.toolTipText")); // NOI18N
        btnFirst.setPreferredSize(new java.awt.Dimension(30, 25));
        btnFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirstActionPerformed(evt);
            }
        });

        btnPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryex/1leftarrow.png"))); // NOI18N
        btnPrev.setMnemonic(KeyEvent.VK_P);
        org.openide.awt.Mnemonics.setLocalizedText(btnPrev, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnPrev.text")); // NOI18N
        btnPrev.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnPrev.toolTipText")); // NOI18N
        btnPrev.setPreferredSize(new java.awt.Dimension(30, 25));
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryex/1rightarrow.png"))); // NOI18N
        btnNext.setMnemonic(KeyEvent.VK_E);
        org.openide.awt.Mnemonics.setLocalizedText(btnNext, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnNext.text")); // NOI18N
        btnNext.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnNext.toolTipText")); // NOI18N
        btnNext.setPreferredSize(new java.awt.Dimension(30, 25));
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryex/2rightarrow.png"))); // NOI18N
        btnLast.setMnemonic(KeyEvent.VK_L);
        org.openide.awt.Mnemonics.setLocalizedText(btnLast, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnLast.text")); // NOI18N
        btnLast.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnLast.toolTipText")); // NOI18N
        btnLast.setPreferredSize(new java.awt.Dimension(30, 25));
        btnLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout browsePanelLayout = new javax.swing.GroupLayout(browsePanel);
        browsePanel.setLayout(browsePanelLayout);
        browsePanelLayout.setHorizontalGroup(
            browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(browsePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMFN)
                .addGap(18, 18, 18)
                .addComponent(txtMFN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(42, 42, 42)
                .addComponent(btnFirst, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(btnLast, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        browsePanelLayout.setVerticalGroup(
            browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(browsePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, browsePanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnFirst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(browsePanelLayout.createSequentialGroup()
                        .addComponent(btnLast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(browsePanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtMFN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblMFN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(btnPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addContainerGap())
        );

        crudPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryex/new.png"))); // NOI18N
        btnNew.setMnemonic(KeyEvent.VK_N);
        org.openide.awt.Mnemonics.setLocalizedText(btnNew, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnNew.text")); // NOI18N
        btnNew.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnNew.toolTipText")); // NOI18N
        btnNew.setPreferredSize(new java.awt.Dimension(30, 25));
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

        btnSave.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryex/save.png"))); // NOI18N
        btnSave.setMnemonic(KeyEvent.VK_S);
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setPreferredSize(new java.awt.Dimension(30, 25));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnDel.setForeground(new java.awt.Color(204, 0, 51));
        btnDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/dataentryex/delete.png"))); // NOI18N
        btnDel.setMnemonic(KeyEvent.VK_D);
        org.openide.awt.Mnemonics.setLocalizedText(btnDel, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnDel.text")); // NOI18N
        btnDel.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnDel.toolTipText")); // NOI18N
        btnDel.setPreferredSize(new java.awt.Dimension(30, 25));
        btnDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout crudPanelLayout = new javax.swing.GroupLayout(crudPanel);
        crudPanel.setLayout(crudPanelLayout);
        crudPanelLayout.setHorizontalGroup(
            crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(crudPanelLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(btnNew, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addComponent(btnDel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(crudPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        crudPanelLayout.setVerticalGroup(
            crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, crudPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnDel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNew, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(crudPanelLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        optionsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnReload.setMnemonic('R');
        org.openide.awt.Mnemonics.setLocalizedText(btnReload, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnReload.text")); // NOI18N
        btnReload.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnReload.toolTipText")); // NOI18N
        btnReload.setFocusable(false);
        btnReload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnCreateCopy, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnCreateCopy.text")); // NOI18N
        btnCreateCopy.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnCreateCopy.toolTipText")); // NOI18N
        btnCreateCopy.setFocusable(false);
        btnCreateCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCreateCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCreateCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateCopyActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnClear, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnClear.text")); // NOI18N
        btnClear.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnClear.toolTipText")); // NOI18N
        btnClear.setFocusable(false);
        btnClear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnCopy.setMnemonic('C');
        org.openide.awt.Mnemonics.setLocalizedText(btnCopy, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnCopy.text")); // NOI18N
        btnCopy.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnCopy.toolTipText")); // NOI18N
        btnCopy.setFocusable(false);
        btnCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyActionPerformed(evt);
            }
        });

        btnPaste.setMnemonic('P');
        org.openide.awt.Mnemonics.setLocalizedText(btnPaste, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnPaste.text")); // NOI18N
        btnPaste.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnPaste.toolTipText")); // NOI18N
        btnPaste.setFocusable(false);
        btnPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPasteActionPerformed(evt);
            }
        });

        btnShowHide.setMnemonic('H');
        org.openide.awt.Mnemonics.setLocalizedText(btnShowHide, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnShowHide.text")); // NOI18N
        btnShowHide.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnShowHide.toolTipText")); // NOI18N
        btnShowHide.setFocusable(false);
        btnShowHide.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowHide.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowHide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowHideActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnReload)
                .addGap(6, 6, 6)
                .addComponent(btnCreateCopy)
                .addGap(6, 6, 6)
                .addComponent(btnClear)
                .addGap(6, 6, 6)
                .addComponent(btnCopy)
                .addGap(6, 6, 6)
                .addComponent(btnPaste)
                .addGap(6, 6, 6)
                .addComponent(btnShowHide)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnReload, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCreateCopy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCopy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPaste, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnShowHide, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(btnRTL, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnRTL.text")); // NOI18N
        btnRTL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRTLActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnVAL, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnVAL.text")); // NOI18N
        btnVAL.setToolTipText(org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnVAL.toolTipText")); // NOI18N
        btnVAL.setFocusable(false);
        btnVAL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVAL.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnVAL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnValidationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnRTL)
                .addGap(6, 6, 6)
                .addComponent(btnVAL)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnVAL, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRTL, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(btnMarc21FixedFieldEditor, org.openide.util.NbBundle.getMessage(OutlineTopComponent.class, "OutlineTopComponent.btnMarc21FixedFieldEditor.text")); // NOI18N
        btnMarc21FixedFieldEditor.setEnabled(false);
        btnMarc21FixedFieldEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarc21FixedFieldEditorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout navigationPanelLayout = new javax.swing.GroupLayout(navigationPanel);
        navigationPanel.setLayout(navigationPanelLayout);
        navigationPanelLayout.setHorizontalGroup(
            navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navigationPanelLayout.createSequentialGroup()
                .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(navigationPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnMarc21FixedFieldEditor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRecordChanged, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(crudPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(browsePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        navigationPanelLayout.setVerticalGroup(
            navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navigationPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(browsePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(wksPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(crudPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(navigationPanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, navigationPanelLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(navigationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRecordChanged, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMarc21FixedFieldEditor, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addGap(76, 76, 76))
        );

        recordPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(recordPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(navigationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(navigationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(recordPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
   /**
    * A change in the worksheet selected occurred
    * @param evt
    */
   private void cmbWKSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbWKSActionPerformed
      String wksName = cmbWKS.getSelectedItem().toString();
      try {
         wks_ = db_.getWorksheetDef(wksName);
         pickListDataList_ = db_.getPickListData(wksName);
         validationDataList_ = db_.getValidationData(wksName);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
       
   
      currRec_ = dataEntryPanel.getRecord();
      // Show empty fields if we are creating a new record
      if (currRec_.getMfn() == 0) {
         showEmptyFields_ = true;
      }
      dataEntryPanel.changeWorksheet(wks_, currRec_, showEmptyFields_,
              pickListDataList_, validationDataList_);
}//GEN-LAST:event_cmbWKSActionPerformed

   private void txtMFNKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMFNKeyPressed
       // Check if current record modified and not saved
      if (!canCloseDataEntryEx()) {
         return;
      }
      if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
         long mfn = 0;
         try {
            mfn = Long.parseLong(txtMFN.getText());
         } catch (Exception e) {
            String msg = NbBundle.getMessage(OutlineTopComponent.class, "MSG_MfnParseError");
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            setRecord(currRec_);
            return;
         }
         try {
            if (mfn <= 0 || mfn > db_.getLastMfn()) {
               String msg = NbBundle.getMessage(OutlineTopComponent.class, "MSG_MfnInvalid");
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
               med.dbRecord();
            }
         } catch (DbException ex) {
            new RecordNotFoundException(ex).displayWarning();
         }
      }
   }//GEN-LAST:event_txtMFNKeyPressed

   private void btnFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirstActionPerformed
      // Check if current record modified and not saved
      System.out.println("btnFirstActionPerformed entry");
      if (!canCloseDataEntryEx()) {
         return;
      }
      System.out.println("btnFirstActionPerformed after canClose");
      try {
         IRecord rec = db_.getFirst();
         if (rec != null) {
            dbRec_ = Record.newInstance(rec);
            setRecord(rec);
            med.dbRecord();
         }
      } catch (DbException ex) {
         new RecordNotFoundException(ex).displayWarning();
      }
}//GEN-LAST:event_btnFirstActionPerformed

   private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
      // Check if current record modified and not saved
      if (!canCloseDataEntryEx()) {
         return;
      }
      try {
         IRecord rec = db_.getPrev();
         if (rec != null) {
            dbRec_ = Record.newInstance(rec);
            setRecord(rec);
            med.dbRecord();
         }

      } catch (DbException ex) {
         new RecordNotFoundException(ex).silent();
      }
}//GEN-LAST:event_btnPrevActionPerformed

   private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
       // Check if current record modified and not saved
      if (!canCloseDataEntryEx()) {
         return;
      }
      try {
         IRecord rec = db_.getNext();
         if (rec != null) {
            dbRec_ = Record.newInstance(rec);
            setRecord(rec);
            med.dbRecord();
         }
      } catch (DbException ex) {
         new RecordNotFoundException(ex).silent();
      }
}//GEN-LAST:event_btnNextActionPerformed

   private void btnLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastActionPerformed
       // Check if current record modified and not saved
      if (!canCloseDataEntryEx()) {
         return;
      }
      try {
         IRecord rec = db_.getLast();
         if (rec != null) {
            dbRec_ = Record.newInstance(rec);
            setRecord(rec);
            med.dbRecord();
         }
      } catch (DbException ex) {
         new RecordNotFoundException(ex).displayWarning();
      }
}//GEN-LAST:event_btnLastActionPerformed

   private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        // Check if current record modified and not saved
      if (!canCloseDataEntryEx()) {
         return;
      }
      newRecord();
}//GEN-LAST:event_btnNewActionPerformed

   private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
     
      saveRecord();
      
}//GEN-LAST:event_btnSaveActionPerformed

   private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed

      long mfn = currRec_.getMfn();
      String label = NbBundle.getMessage(OutlineTopComponent.class, "MSG_DeleteRecLabel");
      String title = NbBundle.getMessage(OutlineTopComponent.class, "MSG_DeleteRecDialogTitle");

      NotifyDescriptor d =
              new NotifyDescriptor.Confirmation(label, title,
              NotifyDescriptor.OK_CANCEL_OPTION);
      if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
         deleteRecord();
         String msg = NbBundle.getMessage(OutlineTopComponent.class,
                 "MSG_RecordSuccessfullyDeleted")+" MFN ="+mfn;
         GuiGlobal.output(msg);
      }
}//GEN-LAST:event_btnDelActionPerformed

   private void btnRTLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRTLActionPerformed
//      orientation_ = (orientation_== ComponentOrientation.LEFT_TO_RIGHT) ?
//         ComponentOrientation.RIGHT_TO_LEFT
//         : ComponentOrientation.LEFT_TO_RIGHT;
//      editPanel_.applyComponentOrientation(orientation_);
//      editPanel_.setOrientation(orientation_);
      //editPanel_.refresh();
   }//GEN-LAST:event_btnRTLActionPerformed

   private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
      setRecord(dbRec_);
      setRecordChangedFlag(false);
      med.dbRecord();
}//GEN-LAST:event_btnReloadActionPerformed

   private void btnCreateCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateCopyActionPerformed
      createCopyRecord();
      String msg = NbBundle.getMessage(OutlineTopComponent.class,
              "MSG_RecordSuccessfullyCopied")+ ""+currRec_.getMfn();
      GuiGlobal.output(msg);
}//GEN-LAST:event_btnCreateCopyActionPerformed

   private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
      if (currRec_ == null) {
         currRec_ = dataEntryPanel.getRecord();
      }
      currRec_.clear();
      setRecord(currRec_);
      med.recordChanged();
}//GEN-LAST:event_btnClearActionPerformed

   private void btnCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyActionPerformed
      dataEntryPanel.completeEdit();
      IRecord record = dataEntryPanel.getRecord();
      recordStack_.add(record);
}//GEN-LAST:event_btnCopyActionPerformed

   private void btnPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasteActionPerformed
       final int n = recordStack_.size();
       if (n > 0) {
           IRecord rec = recordStack_.get(n - 1);
           rec.setMfn(0l);
           setRecord(rec);
           med.dbRecord();
           setRecordChangedFlag(true);
       } 
}//GEN-LAST:event_btnPasteActionPerformed

   private void btnShowHideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowHideActionPerformed
      showEmptyFields_ = !showEmptyFields_;
       if (currRec_ == null) {
         currRec_ = dataEntryPanel.getRecord();
      }
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

      currRec_ = dataEntryPanel.getRecord();
      applyValidationRules();
   }//GEN-LAST:event_btnValidationActionPerformed

   private void txtMFNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMFNActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_txtMFNActionPerformed

    private void btnMarc21FixedFieldEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarc21FixedFieldEditorActionPerformed
        // TODO add your handling code here:
        Marc21FixedFieldsEditor dialog = new Marc21FixedFieldsEditor("Test Marc21FixedFieldEditor");
         dialog.setLocationRelativeTo(null);
        dialog.pack();
        dialog.setVisible(true);
    }//GEN-LAST:event_btnMarc21FixedFieldEditorActionPerformed

   private void newRecord() {

      med.newRecord();
      txtMFN.setText("0");
      dataEntryPanel.newRecord();

   }

   private boolean canCloseDataEntryEx() {
      if (!recordChanged_) {
         return true;
      }
      String label = NbBundle.getMessage(OutlineTopComponent.class, "MSG_SaveRecordChanges", db_.getDbName());
      String title = NbBundle.getMessage(OutlineTopComponent.class, "MSG_SaveRecordChangesTitle");
      NotifyDescriptor d =
              new NotifyDescriptor.Confirmation(label, title,
              NotifyDescriptor.YES_NO_CANCEL_OPTION);
      Object notify = DialogDisplayer.getDefault().notify(d);
      if (notify == NotifyDescriptor.CANCEL_OPTION) {
         return false;
      } else if (notify == NotifyDescriptor.NO_OPTION) {
         setRecordChangedFlag(false);
         return true;
      } else if (notify == NotifyDescriptor.YES_OPTION) {
         saveRecord();
         String msg = NbBundle.getMessage(OutlineTopComponent.class,
                 "MSG_RecordSuccessfullySaved") + " MFN =" + currRec_.getMfn();
         GuiGlobal.output(msg);

         System.out.println("Before setRecordChangedFlag");
         setRecordChangedFlag(false);
         System.out.println("After setRecordChangedFlag");
         return true;
      }
      return true;
   }

     private void saveRecord() {

          dataEntryPanel.completeEdit();
          currRec_ = dataEntryPanel.getRecord();
          if (!applyValidationRules()) {
               String msg = NbBundle.getMessage(OutlineTopComponent.class,
                       "MSG_RecordNotValidated");
               GuiGlobal.output(msg);
               return;
          }
          try {
               Record record = (Record) currRec_;
               record.removeEmptyFields();
               currRec_ = db_.updateRecord(record);

               // Be sure to be on this record
               db_.getRecordCursor(currRec_.getMfn());

          } catch (Exception ex) {
               Exceptions.printStackTrace(ex);
          }
          setRecordChangedFlag(false);
          setRecord(currRec_);
          med.dbRecord();
          String msg = NbBundle.getMessage(OutlineTopComponent.class,
                  "MSG_RecordSuccessfullySaved") + " MFN=" + currRec_.getMfn();
          GuiGlobal.output(msg);
     }

   private void createCopyRecord() {

      try {
         // The record to copy is the content of the record displayed
         // This content is not necesseraly saved
         IRecord displayedRecord = dataEntryPanel.getRecord();
         displayedRecord.setMfn(0l); // Set mfn to 0 Long
         currRec_ = db_.addRecord((Record) displayedRecord);
         // Be sure to be on this record
         db_.getRecordCursor(currRec_.getMfn());
         dbRec_ = Record.newInstance(currRec_);
         med.dbRecord();
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
      setRecord(currRec_);
   }
   private void deleteRecord() {
      if(currRec_ == null || currRec_.getMfn() == 0l) {
         return;
      }
      long mfn = currRec_.getMfn();
      try {
         db_.deleteRecord(mfn);
         newRecord();
      } catch (DbException ex) {
         new RecordNotDeleted(ex).displayWarning();
      }
   }

   private void setRecord(IRecord record) {
      if (record != null) {        
         currRec_ = record;
         txtMFN.setText(currRec_.getMfn() + "");
         // Show empty fields if we are creating a new record
         if (currRec_.getMfn() == 0) {
            showEmptyFields_ = true;
         }
         dataEntryPanel.setRecord(record, showEmptyFields_);
      }
   }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel browsePanel;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnCopy;
    private javax.swing.JButton btnCreateCopy;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnFirst;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnMarc21FixedFieldEditor;
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
    private javax.swing.JPanel crudPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JLabel lblMFN;
    private javax.swing.JLabel lblRecordChanged;
    private javax.swing.JLabel lblWKS;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel navigationPanel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel recordPanel;
    private javax.swing.JTextField txtMFN;
    private javax.swing.JPanel wksPanel;
    // End of variables declaration//GEN-END:variables
   /**
    * Gets default instance. Do not use directly: reserved for *.settings files only,
    * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
    * To obtain the singleton instance, use {@link findInstance}.
    */
   public static synchronized OutlineTopComponent getDefault() {

      if (instance != null) {
         instance.close();
         instance = null;
      }

      ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
      if (connectionInfo.getDefaultDatabase() != null) {
         instance = new OutlineTopComponent(connectionInfo.getDefaultDatabase());
      }
      return instance;
   }

   /**
    * Obtain the OutlineTopComponent instance. Never call {@link #getDefault} directly!
    */
   public static synchronized OutlineTopComponent findInstance() {
      TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
      if (win == null) {
         Logger.getLogger(OutlineTopComponent.class.getName()).warning(
                 "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
         return getDefault();
      }
      if (win instanceof OutlineTopComponent) {
         return (OutlineTopComponent) win;
      }
      Logger.getLogger(OutlineTopComponent.class.getName()).warning(
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

    /**
    * Returns:
    * true if top component is ready to close, false otherwise.
    */
   @Override
   public boolean canClose() {
      return canCloseDataEntryEx();
      
   }
   @Override
   public void componentClosed() {
      
      db_.deleteWindow(this);
   }

   @Override
    public void componentDeactivated() {


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

   public void update(Observable o, Object arg) {
       if (db_.databaseHasChanged()) {
         try {

            // Be sure to be on the record displayed, because another window
            // may have change the record cursor
            currentMfn_ = currRec_.getMfn();
            if (currentMfn_ > 0) {
               IRecord record = db_.getRecordCursor(currentMfn_);
            }
         //displayRecord(db_.getCurrent());
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
      }

   }

   final static class ResolvableHelper implements Serializable {

      private static final long serialVersionUID = 1L;

      public Object readResolve() {
         return OutlineTopComponent.getDefault();
      }
   }

   
}
