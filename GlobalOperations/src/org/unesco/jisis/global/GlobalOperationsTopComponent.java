/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.global;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.FocusAdapter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.MfnRange;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.jisisutil.history.HistoryTextArea;
import org.unesco.jisis.jisisutil.history.HistoryTextField;

import org.unesco.jisis.jisiscore.common.AsyncCallback;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;
import org.unesco.jisis.jisisutils.proxy.MarkedRecords;
import org.unesco.jisis.jisisutils.proxy.SearchResult;
import org.unesco.jisis.searchhistory.SearchHistoryModel;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.unesco.jisis.global//GlobalOperations//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "GlobalOperationsTopComponent",
    //iconBase="SET/PATH/TO/ICON/HERE", 
    persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.unesco.jisis.global.GlobalOperationsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_GlobalOperationsAction"
   
)
@Messages({
    "CTL_GlobalOperationsAction=GlobalOperations",
    "CTL_GlobalOperationsTopComponent=GlobalOperations Window",
    "HINT_GlobalOperationsTopComponent=This is a GlobalOperations window"
})
public final class GlobalOperationsTopComponent extends TopComponent implements Observer {

   private static final String PREFERRED_ID = "GlobalOperationsTopComponent";
  
   private ClientDatabaseProxy db_ = null;

   private static RequestProcessor.Task task_ = null;
   private static CancellableProgress cancellable_;

   private final String NO_SEARCH_SETS = "No Search Sets";
   private final String NO_MARKED_SETS = "No Marked Sets";
    
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
  
   
    

    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GlobalOperationsTopComponent.class);

    public GlobalOperationsTopComponent() {
        setName(Bundle.CTL_GlobalOperationsTopComponent());
        setToolTipText(Bundle.HINT_GlobalOperationsTopComponent());

    }

    public GlobalOperationsTopComponent(IDatabase db) {

        
        
        if (db instanceof ClientDatabaseProxy) {
            db_ = (ClientDatabaseProxy) db;
        } else {
            throw new RuntimeException("GlobalOperationsTopComponent: Cannot cast DB to ClientDatabaseProxy");
        }
        
       try {
           setName(NbBundle.getMessage(GlobalOperationsTopComponent.class, "CTL_GlobalOperationsTopComponent")
               + " (" +db.getDbHome()+"//"+ db_.getDatabaseName() + ")");
       } catch (DbException ex) {
           Exceptions.printStackTrace(ex);
       }
         setToolTipText(Bundle.HINT_GlobalOperationsTopComponent());

        /* Register this TopComponent as attached to this DB */
        db_.addWindow(this);

        /* Add this TopComponent as Observer to DB changes */
        db_.addObserver((Observer) this);
        initComponents();

        ItemListener listenerAdd = new RangeAddRadioListener();
        rdbMfnRangeAdd.addItemListener(listenerAdd);
        rdbRangeAllAdd.addItemListener(listenerAdd);
        rdbRangeListAdd.addItemListener(listenerAdd);
        rdbSearchResultsAdd.addItemListener(listenerAdd);
        rdbMarkedRecordsAdd.addItemListener(listenerAdd);

        ItemListener listenerDelete = new RangeDeleteRadioListener();
        rdbMfnRangeDelete.addItemListener(listenerDelete);
        rdbRangeAllDelete.addItemListener(listenerDelete);
        rdbRangeListDelete.addItemListener(listenerDelete);
        rdbSearchResultsDelete.addItemListener(listenerDelete);
        rdbMarkedRecordsDelete.addItemListener(listenerDelete);

        ItemListener listenerReplace = new RangeReplaceRadioListener();
        rdbMfnRangeReplace.addItemListener(listenerReplace);
        rdbRangeAllReplace.addItemListener(listenerReplace);
        rdbRangeListReplace.addItemListener(listenerReplace);
        rdbSearchResultsReplace.addItemListener(listenerReplace);
        rdbMarkedRecordsReplace.addItemListener(listenerReplace);

        prepareSearchHistory();

        prepareMarkedRecordsHistory();
        
        txtSubfield.addFocusListener(new CursorAtStartFocusListener());
//        txtMfnListAdd.setInputVerifier(new MfnListInputVerifier());
//        txtMfnListDelete.setInputVerifier(new MfnListInputVerifier());
//        txtMfnListReplace.setInputVerifier(new MfnListInputVerifier());
        
        spinnerFieldTag.setInputVerifier(new TagInputVerifier());
        
        rdbMfnRangeAdd.setSelected(true);
        rdbRangeAllAdd.setSelected(true);
        
        addPanel.setVerifyInputWhenFocusTarget(false);
        deletePanel.setVerifyInputWhenFocusTarget(false);
        replacePanel.setVerifyInputWhenFocusTarget(false);

        
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGroupRangeAdd = new javax.swing.ButtonGroup();
        btnGroupRangeDelete = new javax.swing.ButtonGroup();
        btnGroupRangeReplace = new javax.swing.ButtonGroup();
        btnGroupRangeTypeAdd = new javax.swing.ButtonGroup();
        btnGroupRangeTypeDelete = new javax.swing.ButtonGroup();
        btnGroupRangeTypeReplace = new javax.swing.ButtonGroup();
        globalTabbedPane = new javax.swing.JTabbedPane();
        scrollPaneAdd = new javax.swing.JScrollPane();
        addPanel = new javax.swing.JPanel();
        rdbMfnRangeAdd = new javax.swing.JRadioButton();
        rdbSearchResultsAdd = new javax.swing.JRadioButton();
        txtMfnListAdd = new org.unesco.jisis.jisisutil.history.HistoryTextField(db_.getDbName()+"_txtMfnListAdd");
        cmbSearchSetAdd = new javax.swing.JComboBox();
        rdbRangeAllAdd = new javax.swing.JRadioButton();
        rdbRangeListAdd = new javax.swing.JRadioButton();
        lblFromSearchSetAdd = new javax.swing.JLabel();
        rdbMarkedRecordsAdd = new javax.swing.JRadioButton();
        lblFromMarkedSetAdd = new javax.swing.JLabel();
        cmbMarkedSetAdd = new javax.swing.JComboBox();
        lblMfnListAdd = new javax.swing.JLabel();
        lblFieldTag = new javax.swing.JLabel();
        spinnerFieldTag = new javax.swing.JSpinner();
        chkAddIfNotPresent = new javax.swing.JCheckBox();
        chkInsertBeforePosition = new javax.swing.JCheckBox();
        spinnerPositionAdd = new javax.swing.JSpinner();
        spinnerOccurrence = new javax.swing.JSpinner();
        lblOccurrence = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtContent = new org.unesco.jisis.jisisutil.history.HistoryTextArea(db_.getDbName()+"_txtContentAdd");
        btnGoAdd = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        scrollPaneDelete = new javax.swing.JScrollPane();
        deletePanel = new javax.swing.JPanel();
        rdbMfnRangeDelete = new javax.swing.JRadioButton();
        rdbSearchResultsDelete = new javax.swing.JRadioButton();
        txtMfnListDelete = new HistoryTextField(db_.getDbName()+"_txtMfnListDelete");
        rdbRangeAllDelete = new javax.swing.JRadioButton();
        rdbRangeListDelete = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        lblFromSearchSetDelete = new javax.swing.JLabel();
        cmbSearchSetDelete = new javax.swing.JComboBox();
        rdbMarkedRecordsDelete = new javax.swing.JRadioButton();
        lblFromMarkedSetDelete = new javax.swing.JLabel();
        cmbMarkedSetDelete = new javax.swing.JComboBox();
        lblFieldTagDelete = new javax.swing.JLabel();
        lblOccurrenceDelete = new javax.swing.JLabel();
        spinnerFieldTagDelete = new javax.swing.JSpinner();
        spinnerOccurrenceDelete = new javax.swing.JSpinner();
        lblSubfield = new javax.swing.JLabel();
        lblSubfieldDelimiter = new javax.swing.JLabel();
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter("*");
        } catch (java.text.ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
        }
        txtSubfield = new javax.swing.JFormattedTextField(formatter);
        btnGoDelete = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        scrollPaneReplace = new javax.swing.JScrollPane();
        replacePanel = new javax.swing.JPanel();
        rdbMfnRangeReplace = new javax.swing.JRadioButton();
        rdbSearchResultsReplace = new javax.swing.JRadioButton();
        txtMfnListReplace = new HistoryTextField(db_.getDbName()+"_txtMfnListReplace");
        rdbRangeAllReplace = new javax.swing.JRadioButton();
        rdbRangeListReplace = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        rdbMarkedRecordsReplace = new javax.swing.JRadioButton();
        lblFromSearchSetReplace = new javax.swing.JLabel();
        lblFromMarkedSet = new javax.swing.JLabel();
        cmbSearchSetReplace = new javax.swing.JComboBox();
        cmbMarkedSetReplace = new javax.swing.JComboBox();
        lblToFind = new javax.swing.JLabel();
        txtTextToFind = new org.unesco.jisis.jisisutil.history.HistoryTextField(db_.getDbName()+"_txtToFind",true,false);
        lblNewText = new javax.swing.JLabel();
        txtNewText = new HistoryTextField(db_.getDbName()+"_txtNewText");
        optionsPanel = new javax.swing.JPanel();
        chkCaseSensitiveReplace = new javax.swing.JCheckBox();
        chkWholeWordsOnlyReplace = new javax.swing.JCheckBox();
        chkPromptOnReplace = new javax.swing.JCheckBox();
        scopePanel = new javax.swing.JPanel();
        txtTagsReplace = new HistoryTextField(db_.getDbName()+"_txtTagsReplace");
        txtSubfieldsReplace = new HistoryTextField(db_.getDbName()+"_txtSubfieldsReplace");
        txtOccurrencesReplace = new HistoryTextField(db_.getDbName()+"_txtOccurrencesReplace");
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        btnGoReplace = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setLayout(new java.awt.BorderLayout());

        addPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.addPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 12), new java.awt.Color(0, 51, 255))); // NOI18N

        btnGroupRangeAdd.add(rdbMfnRangeAdd);
        rdbMfnRangeAdd.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbMfnRangeAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbMfnRangeAdd.text")); // NOI18N
        rdbMfnRangeAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMfnRangeAddActionPerformed(evt);
            }
        });

        btnGroupRangeAdd.add(rdbSearchResultsAdd);
        rdbSearchResultsAdd.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbSearchResultsAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbSearchResultsAdd.text")); // NOI18N
        rdbSearchResultsAdd.setVerifyInputWhenFocusTarget(false);

        txtMfnListAdd.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtMfnListAdd.text")); // NOI18N
        txtMfnListAdd.setEnabled(false);

        cmbSearchSetAdd.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSearchSetAdd.setEnabled(false);

        btnGroupRangeTypeAdd.add(rdbRangeAllAdd);
        rdbRangeAllAdd.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbRangeAllAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbRangeAllAdd.text")); // NOI18N
        rdbRangeAllAdd.setVerifyInputWhenFocusTarget(false);

        btnGroupRangeTypeAdd.add(rdbRangeListAdd);
        org.openide.awt.Mnemonics.setLocalizedText(rdbRangeListAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbRangeListAdd.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFromSearchSetAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblFromSearchSetAdd.text")); // NOI18N

        btnGroupRangeAdd.add(rdbMarkedRecordsAdd);
        rdbMarkedRecordsAdd.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbMarkedRecordsAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbMarkedRecordsAdd.text")); // NOI18N
        rdbMarkedRecordsAdd.setVerifyInputWhenFocusTarget(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblFromMarkedSetAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblFromMarkedSetAdd.text")); // NOI18N

        cmbMarkedSetAdd.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbMarkedSetAdd.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblMfnListAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblMfnListAdd.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFieldTag, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblFieldTag.text")); // NOI18N

        spinnerFieldTag.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinnerFieldTag.setName("fieldTag"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkAddIfNotPresent, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.chkAddIfNotPresent.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkInsertBeforePosition, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.chkInsertBeforePosition.text")); // NOI18N

        spinnerPositionAdd.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        spinnerOccurrence.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        spinnerOccurrence.setRequestFocusEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblOccurrence, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblOccurrence.text")); // NOI18N

        jScrollPane1.setVerifyInputWhenFocusTarget(false);

        txtContent.setColumns(20);
        txtContent.setRows(5);
        txtContent.setName("DataToAdd"); // NOI18N
        jScrollPane1.setViewportView(txtContent);

        btnGoAdd.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnGoAdd, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.btnGoAdd.text")); // NOI18N
        btnGoAdd.setToolTipText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.btnGoAdd.toolTipText")); // NOI18N
        btnGoAdd.setActionCommand(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.btnGoAdd.actionCommand")); // NOI18N
        btnGoAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoAddActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel3.text")); // NOI18N
        jLabel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel4.text")); // NOI18N
        jLabel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel5.text")); // NOI18N
        jLabel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout addPanelLayout = new javax.swing.GroupLayout(addPanel);
        addPanel.setLayout(addPanelLayout);
        addPanelLayout.setHorizontalGroup(
            addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(addPanelLayout.createSequentialGroup()
                        .addComponent(lblFieldTag)
                        .addGap(40, 40, 40)
                        .addComponent(spinnerFieldTag, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkAddIfNotPresent, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkInsertBeforePosition, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(addPanelLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(spinnerPositionAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(113, 113, 113)
                                .addComponent(spinnerOccurrence, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblOccurrence, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(139, 139, 139)
                        .addComponent(btnGoAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(addPanelLayout.createSequentialGroup()
                            .addComponent(rdbMfnRangeAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(39, 39, 39)
                            .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(rdbRangeListAdd)
                                .addComponent(rdbRangeAllAdd))
                            .addGap(76, 76, 76)
                            .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblMfnListAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 540, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtMfnListAdd)))
                        .addGroup(addPanelLayout.createSequentialGroup()
                            .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(rdbSearchResultsAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(rdbMarkedRecordsAdd))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblFromSearchSetAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblFromMarkedSetAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(30, 30, 30)
                            .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(cmbSearchSetAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 624, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbMarkedSetAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 624, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1149, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(91, Short.MAX_VALUE))
        );
        addPanelLayout.setVerticalGroup(
            addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addPanelLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbMfnRangeAdd)
                    .addComponent(rdbRangeAllAdd))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMfnListAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMfnListAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdbRangeListAdd))
                .addGap(37, 37, 37)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbSearchResultsAdd)
                    .addComponent(lblFromSearchSetAdd)
                    .addComponent(cmbSearchSetAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbMarkedRecordsAdd)
                    .addComponent(lblFromMarkedSetAdd)
                    .addComponent(cmbMarkedSetAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addComponent(jLabel4)
                .addGap(16, 16, 16)
                .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblOccurrence)
                    .addGroup(addPanelLayout.createSequentialGroup()
                        .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFieldTag)
                            .addComponent(spinnerFieldTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkAddIfNotPresent))
                        .addGap(18, 18, 18)
                        .addComponent(chkInsertBeforePosition)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(addPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinnerPositionAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spinnerOccurrence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnGoAdd))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(188, 188, 188))
        );

        scrollPaneAdd.setViewportView(addPanel);

        globalTabbedPane.addTab(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.scrollPaneAdd.TabConstraints.tabTitle"), scrollPaneAdd); // NOI18N

        deletePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.deletePanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 12), new java.awt.Color(51, 0, 255))); // NOI18N

        btnGroupRangeDelete.add(rdbMfnRangeDelete);
        rdbMfnRangeDelete.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        rdbMfnRangeDelete.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbMfnRangeDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbMfnRangeDelete.text")); // NOI18N

        btnGroupRangeDelete.add(rdbSearchResultsDelete);
        rdbSearchResultsDelete.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbSearchResultsDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbSearchResultsDelete.text")); // NOI18N
        rdbSearchResultsDelete.setVerifyInputWhenFocusTarget(false);

        txtMfnListDelete.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtMfnListDelete.text")); // NOI18N
        txtMfnListDelete.setEnabled(false);

        btnGroupRangeTypeDelete.add(rdbRangeAllDelete);
        rdbRangeAllDelete.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbRangeAllDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbRangeAllDelete.text")); // NOI18N
        rdbRangeAllDelete.setVerifyInputWhenFocusTarget(false);
        rdbRangeAllDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbRangeAllDeleteActionPerformed(evt);
            }
        });

        btnGroupRangeTypeDelete.add(rdbRangeListDelete);
        org.openide.awt.Mnemonics.setLocalizedText(rdbRangeListDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbRangeListDelete.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFromSearchSetDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblFromSearchSetDelete.text")); // NOI18N

        cmbSearchSetDelete.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSearchSetDelete.setEnabled(false);

        btnGroupRangeDelete.add(rdbMarkedRecordsDelete);
        rdbMarkedRecordsDelete.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbMarkedRecordsDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbMarkedRecordsDelete.text")); // NOI18N
        rdbMarkedRecordsDelete.setVerifyInputWhenFocusTarget(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblFromMarkedSetDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblFromMarkedSetDelete.text")); // NOI18N

        cmbMarkedSetDelete.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbMarkedSetDelete.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblFieldTagDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblFieldTagDelete.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblOccurrenceDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblOccurrenceDelete.text")); // NOI18N

        spinnerFieldTagDelete.setModel(new javax.swing.SpinnerNumberModel());

        spinnerOccurrenceDelete.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));

        org.openide.awt.Mnemonics.setLocalizedText(lblSubfield, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblSubfield.text")); // NOI18N

        lblSubfieldDelimiter.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblSubfieldDelimiter, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblSubfieldDelimiter.text")); // NOI18N

        txtSubfield.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtSubfield.text")); // NOI18N

        btnGoDelete.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnGoDelete.setMnemonic('D');
        org.openide.awt.Mnemonics.setLocalizedText(btnGoDelete, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.btnGoDelete.text")); // NOI18N
        btnGoDelete.setToolTipText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.btnGoDelete.toolTipText")); // NOI18N
        btnGoDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoDeleteActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel6.text")); // NOI18N
        jLabel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel7.text")); // NOI18N
        jLabel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout deletePanelLayout = new javax.swing.GroupLayout(deletePanel);
        deletePanel.setLayout(deletePanelLayout);
        deletePanelLayout.setHorizontalGroup(
            deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deletePanelLayout.createSequentialGroup()
                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 837, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(deletePanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rdbMarkedRecordsDelete)
                            .addComponent(rdbSearchResultsDelete)
                            .addComponent(rdbMfnRangeDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(deletePanelLayout.createSequentialGroup()
                                .addComponent(lblFromMarkedSetDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbMarkedSetDelete, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(deletePanelLayout.createSequentialGroup()
                                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblFromSearchSetDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(rdbRangeListDelete)
                                    .addComponent(rdbRangeAllDelete))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel1)
                                    .addComponent(txtMfnListDelete)
                                    .addComponent(cmbSearchSetDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(0, 625, Short.MAX_VALUE))
            .addGroup(deletePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(deletePanelLayout.createSequentialGroup()
                        .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFieldTagDelete)
                            .addComponent(lblOccurrenceDelete))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(spinnerFieldTagDelete)
                            .addComponent(spinnerOccurrenceDelete, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(58, 58, 58)
                        .addComponent(lblSubfield, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblSubfieldDelimiter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSubfield, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(103, 103, 103)
                        .addComponent(btnGoDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        deletePanelLayout.setVerticalGroup(
            deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(deletePanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdbMfnRangeDelete)
                    .addGroup(deletePanelLayout.createSequentialGroup()
                        .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(deletePanelLayout.createSequentialGroup()
                                .addComponent(rdbRangeAllDelete)
                                .addGap(18, 18, 18))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, deletePanelLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(7, 7, 7)))
                        .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rdbRangeListDelete)
                            .addComponent(txtMfnListDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(50, 50, 50)
                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbSearchResultsDelete)
                    .addComponent(lblFromSearchSetDelete)
                    .addComponent(cmbSearchSetDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdbMarkedRecordsDelete)
                    .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFromMarkedSetDelete)
                        .addComponent(cmbMarkedSetDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(36, 36, 36)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFieldTagDelete)
                    .addComponent(spinnerFieldTagDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubfield)
                    .addComponent(lblSubfieldDelimiter)
                    .addComponent(txtSubfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGoDelete))
                .addGap(24, 24, 24)
                .addGroup(deletePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinnerOccurrenceDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOccurrenceDelete))
                .addGap(45, 45, 45)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(396, Short.MAX_VALUE))
        );

        scrollPaneDelete.setViewportView(deletePanel);

        globalTabbedPane.addTab(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.scrollPaneDelete.TabConstraints.tabTitle"), scrollPaneDelete); // NOI18N

        replacePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.replacePanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 12), new java.awt.Color(0, 51, 204))); // NOI18N

        btnGroupRangeReplace.add(rdbMfnRangeReplace);
        rdbMfnRangeReplace.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        rdbMfnRangeReplace.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbMfnRangeReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbMfnRangeReplace.text")); // NOI18N

        btnGroupRangeReplace.add(rdbSearchResultsReplace);
        rdbSearchResultsReplace.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbSearchResultsReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbSearchResultsReplace.text")); // NOI18N
        rdbSearchResultsReplace.setVerifyInputWhenFocusTarget(false);

        txtMfnListReplace.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtMfnListReplace.text")); // NOI18N
        txtMfnListReplace.setEnabled(false);

        btnGroupRangeTypeReplace.add(rdbRangeAllReplace);
        rdbRangeAllReplace.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbRangeAllReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbRangeAllReplace.text")); // NOI18N
        rdbRangeAllReplace.setVerifyInputWhenFocusTarget(false);
        rdbRangeAllReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbRangeAllReplaceActionPerformed(evt);
            }
        });

        btnGroupRangeTypeReplace.add(rdbRangeListReplace);
        org.openide.awt.Mnemonics.setLocalizedText(rdbRangeListReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbRangeListReplace.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel2.text")); // NOI18N

        btnGroupRangeReplace.add(rdbMarkedRecordsReplace);
        rdbMarkedRecordsReplace.setFont(new java.awt.Font("Tahoma", 3, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbMarkedRecordsReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.rdbMarkedRecordsReplace.text")); // NOI18N
        rdbMarkedRecordsReplace.setVerifyInputWhenFocusTarget(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblFromSearchSetReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblFromSearchSetReplace.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblFromMarkedSet, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblFromMarkedSet.text")); // NOI18N

        cmbSearchSetReplace.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSearchSetReplace.setEnabled(false);

        cmbMarkedSetReplace.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbMarkedSetReplace.setEnabled(false);

        lblToFind.setFont(new java.awt.Font("Tahoma", 2, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblToFind, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblToFind.text")); // NOI18N

        txtTextToFind.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtTextToFind.text")); // NOI18N

        lblNewText.setFont(new java.awt.Font("Tahoma", 2, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblNewText, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.lblNewText.text")); // NOI18N

        txtNewText.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtNewText.text")); // NOI18N

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.optionsPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(51, 0, 204))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkCaseSensitiveReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.chkCaseSensitiveReplace.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkWholeWordsOnlyReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.chkWholeWordsOnlyReplace.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkPromptOnReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.chkPromptOnReplace.text")); // NOI18N

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkCaseSensitiveReplace)
                    .addComponent(chkWholeWordsOnlyReplace)
                    .addComponent(chkPromptOnReplace))
                .addContainerGap(157, Short.MAX_VALUE))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkCaseSensitiveReplace)
                .addGap(18, 18, 18)
                .addComponent(chkWholeWordsOnlyReplace)
                .addGap(18, 18, 18)
                .addComponent(chkPromptOnReplace)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        scopePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.scopePanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(51, 0, 204))); // NOI18N

        txtTagsReplace.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtTagsReplace.text")); // NOI18N

        txtSubfieldsReplace.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtSubfieldsReplace.text")); // NOI18N

        txtOccurrencesReplace.setText(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.txtOccurrencesReplace.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel11.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel12.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel13.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel14.text")); // NOI18N

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(222, 0, 102));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel15.text")); // NOI18N

        javax.swing.GroupLayout scopePanelLayout = new javax.swing.GroupLayout(scopePanel);
        scopePanel.setLayout(scopePanelLayout);
        scopePanelLayout.setHorizontalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scopePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtOccurrencesReplace, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(scopePanelLayout.createSequentialGroup()
                        .addGroup(scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(txtSubfieldsReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(scopePanelLayout.createSequentialGroup()
                                .addComponent(txtTagsReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel15)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        scopePanelLayout.setVerticalGroup(
            scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scopePanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTagsReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSubfieldsReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtOccurrencesReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnGoReplace.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnGoReplace.setMnemonic('R');
        org.openide.awt.Mnemonics.setLocalizedText(btnGoReplace, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.btnGoReplace.text")); // NOI18N
        btnGoReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoReplaceActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel8.text")); // NOI18N
        jLabel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel9.text")); // NOI18N
        jLabel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.jLabel10.text")); // NOI18N
        jLabel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout replacePanelLayout = new javax.swing.GroupLayout(replacePanel);
        replacePanel.setLayout(replacePanelLayout);
        replacePanelLayout.setHorizontalGroup(
            replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(replacePanelLayout.createSequentialGroup()
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                    .addGroup(replacePanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(replacePanelLayout.createSequentialGroup()
                                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rdbSearchResultsReplace)
                                    .addComponent(rdbMarkedRecordsReplace))
                                .addGap(18, 18, 18)
                                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblFromMarkedSet)
                                    .addComponent(lblFromSearchSetReplace))
                                .addGap(18, 18, 18)
                                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(cmbMarkedSetReplace, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbSearchSetReplace, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 820, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(replacePanelLayout.createSequentialGroup()
                                .addComponent(rdbMfnRangeReplace)
                                .addGap(52, 52, 52)
                                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rdbRangeListReplace)
                                    .addComponent(rdbRangeAllReplace))
                                .addGap(47, 47, 47)
                                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 496, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtMfnListReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 820, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(replacePanelLayout.createSequentialGroup()
                        .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, replacePanelLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(scopePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, replacePanelLayout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblToFind)
                                    .addComponent(lblNewText))
                                .addGap(27, 27, 27)
                                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtTextToFind)
                                    .addComponent(txtNewText, javax.swing.GroupLayout.PREFERRED_SIZE, 682, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(35, 35, 35)
                        .addComponent(btnGoReplace)))
                .addContainerGap(153, Short.MAX_VALUE))
        );
        replacePanelLayout.setVerticalGroup(
            replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(replacePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdbMfnRangeReplace)
                    .addGroup(replacePanelLayout.createSequentialGroup()
                        .addComponent(rdbRangeAllReplace)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addGap(9, 9, 9)
                        .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMfnListReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdbRangeListReplace))))
                .addGap(44, 44, 44)
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbSearchResultsReplace)
                    .addComponent(lblFromSearchSetReplace)
                    .addComponent(cmbSearchSetReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbMarkedRecordsReplace)
                    .addComponent(lblFromMarkedSet)
                    .addComponent(cmbMarkedSetReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addComponent(jLabel9)
                .addGap(10, 10, 10)
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblToFind)
                    .addComponent(txtTextToFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNewText)
                    .addComponent(txtNewText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel10)
                .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(replacePanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(scopePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(replacePanelLayout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(btnGoReplace)))
                .addContainerGap())
        );

        scrollPaneReplace.setViewportView(replacePanel);

        globalTabbedPane.addTab(org.openide.util.NbBundle.getMessage(GlobalOperationsTopComponent.class, "GlobalOperationsTopComponent.scrollPaneReplace.TabConstraints.tabTitle"), scrollPaneReplace); // NOI18N

        add(globalTabbedPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnGoAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoAddActionPerformed
       
        boolean valid = true;
        // Check field tag > 0
        int itag = (Integer) spinnerFieldTag.getValue();
        if (itag == 0) {
            String message = NbBundle.getMessage(GlobalOperationsTopComponent.class,
                "MSG_FIELD_TAG_MAY_NOT_BE_ZERO");
            NotifyDescriptor d
                = new NotifyDescriptor.Message(message,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            valid = false;
        }
        
        if ( txtContent.getText().trim().length() == 0 ) {
            // No data to add
              String message = NbBundle.getMessage(GlobalOperationsTopComponent.class,
                "MSG_DATA_CONTENT_MAY_NOT_BE_EMPTY");
            NotifyDescriptor d
                = new NotifyDescriptor.Message(message,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            valid = false;
            
        }
        if (!valid) {
            return;
        }
        /* -- used for calling the InputVerifier */
        JComponent c = (JComponent) evt.getSource();
        if (c.getVerifyInputWhenFocusTarget()) {
            c.requestFocusInWindow();
            if (!c.hasFocus()) {
                return;
            }
        }
        final MfnRange[] mfnRanges = getGlobalAddRanges();

        final Map parameters = getGlobalAddParameters();
    
        /* -- action */
 
        if (! confirmGlobalOperation()) {
            return;
        }
        
        HistoryTextArea historyTextArea = (HistoryTextArea) txtContent;
        historyTextArea.addCurrentToHistory();
        try {
           

            final NotifyDescriptor d
                = new NotifyDescriptor.Message(NbBundle.getMessage(GlobalOperationsTopComponent.class,
                        "MSG_GLOBAL_ADD_DONE"));
            final GlobalAsyncCallback globalAsyncCallback = new GlobalAsyncCallback(this);
            final Date start = new Date();

            Runnable addRun = new Runnable() {
                @Override
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            doAdd(mfnRanges, parameters, globalAsyncCallback);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        db_.setDatabaseChanged();
                        Date end = new Date();
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to perform Global Add to Records");
                        JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                        JOptionPane.showMessageDialog(mainWindow,
                            NbBundle.getMessage(GlobalOperationsTopComponent.class,
                                "MSG_GLOBAL_ADD_DONE"));

                    }
                }
            };
            task_ = requestProcessor_.post(addRun);
        } catch (Exception ex) {
            GuiGlobal.output("Error when executing Global Add:\n " + ex.getMessage());
        }
    }//GEN-LAST:event_btnGoAddActionPerformed

    private void btnGoDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoDeleteActionPerformed
        /* -- used for calling the InputVerifier */
        JComponent c = (JComponent) evt.getSource();
        if (c.getVerifyInputWhenFocusTarget()) {
            c.requestFocusInWindow();
            if (!c.hasFocus()) {
                return;
            }
        }

        // Check field tag > 0
        int itag = (Integer) spinnerFieldTag.getValue();
        if (itag == 0) {
            String message = NbBundle.getMessage(GlobalOperationsTopComponent.class,
                "MSG_FIELD_TAG_MAY_NOT_BE_ZERO");
            NotifyDescriptor d
                = new NotifyDescriptor.Message(message,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }

         // Check subfield delimiter
        /* -- action */
        if (!confirmGlobalOperation()) {
            return;
        }
        try {
            final MfnRange[] mfnRanges = getGlobalDeleteRanges();

            final Map parameters = getGlobalDeleteParameters();

            final NotifyDescriptor d
                = new NotifyDescriptor.Message(NbBundle.getMessage(GlobalOperationsTopComponent.class,
                        "MSG_GLOBAL_DELETE_DONE"));
            final GlobalAsyncCallback globalAsyncCallback = new GlobalAsyncCallback(this);
            final Date start = new Date();
            Runnable deleteRun = new Runnable() {
                @Override
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            doDelete(mfnRanges, parameters, globalAsyncCallback);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        Date end = new Date();
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to delete field from records");
                        db_.setDatabaseChanged();
                        JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                        JOptionPane.showMessageDialog(mainWindow,
                            NbBundle.getMessage(GlobalOperationsTopComponent.class,
                                "MSG_GLOBAL_DELETE_DONE"));
                    }
                }
            };
            task_ = requestProcessor_.post(deleteRun);
        } catch (Exception ex) {
            GuiGlobal.output("Error when executing Global Delete:\n " + ex.getMessage());
        }
    }//GEN-LAST:event_btnGoDeleteActionPerformed

    private void btnGoReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoReplaceActionPerformed
         /* -- used for calling the InputVerifier */
     
        boolean valid = true;
        if (txtTextToFind.getText().trim().length() == 0) {
            // Text to find is empty
              String message = NbBundle.getMessage(GlobalOperationsTopComponent.class,
                "MSG_TEXT_TO_FIND_MAY_NOT_BE_EMPTY");
            NotifyDescriptor d
                = new NotifyDescriptor.Message(message,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            valid = false;

        }
        if (txtNewText.getText().trim().length() == 0) {
            // New Replacement Text is empty
      
              String message = NbBundle.getMessage(GlobalOperationsTopComponent.class,
                "MSG_REPLACE_TEXT_MAY_NOT_BE_EMPTY");
            NotifyDescriptor d
                = new NotifyDescriptor.Message(message,
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            valid = false;
        }
        
        if (!valid) {
            return;
        }
        // tags, subfields, occurrences
        /* -- action */
 
        if (! confirmGlobalOperation()) {
            return;
        }
        org.unesco.jisis.jisisutil.history.HistoryTextField historyTextField = (HistoryTextField) txtTextToFind;
        historyTextField.addCurrentToHistory();
        historyTextField = (HistoryTextField) txtNewText;
        historyTextField.addCurrentToHistory();
        
        
        
        
        try {
            final MfnRange[] mfnRanges = getGlobalReplaceRanges();

            final Map parameters = getGlobalReplaceParameters();

            final String tagList = (String) parameters.get(REPLACE_FIELD_TAG_KEY);
            if (tagList.trim().isEmpty()) {
                String message = NbBundle.getMessage(GlobalOperationsTopComponent.class,
                    "MSG_TAGS_TEXT_MAY_NOT_BE_EMPTY");
                NotifyDescriptor d
                    = new NotifyDescriptor.Message(message,
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;

            } else {
                historyTextField = (HistoryTextField) txtTagsReplace;
                historyTextField.addCurrentToHistory();
            }

            final String subfieldList = (String) parameters.get(REPLACE_FIELD_SUBFIELD_KEY);
              if (!subfieldList.trim().isEmpty()) {
                 historyTextField = (HistoryTextField) txtSubfieldsReplace;
                 historyTextField.addCurrentToHistory();
            }

            final String occurrenceList = (String) parameters.get(REPLACE_FIELD_OCCURRENCE_KEY);
              if (!occurrenceList.trim().isEmpty()) {
                 historyTextField = (HistoryTextField) txtOccurrencesReplace;
                 historyTextField.addCurrentToHistory();
            }


            final NotifyDescriptor d
                = new NotifyDescriptor.Message(NbBundle.getMessage(GlobalOperationsTopComponent.class,
                        "MSG_GLOBAL_REPLACE_DONE"));
            final GlobalAsyncCallback globalAsyncCallback = new GlobalAsyncCallback(this);
            final Date start = new Date();
            Runnable replaceRun = new Runnable() {
                @Override
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {                           
                            doReplace(mfnRanges, parameters, globalAsyncCallback);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        Date end = new Date();
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to replace in records");
                        DialogDisplayer.getDefault().notify(d);
                        // Second Invocation, we are on the event queue now
                        db_.setDatabaseChanged();
                    }
                }
            };
            task_ = requestProcessor_.post(replaceRun);
        } catch (Exception ex) {
            GuiGlobal.output("Error when executing Global Replace:\n " + ex.getMessage());
        }
    }//GEN-LAST:event_btnGoReplaceActionPerformed

    private void rdbMfnRangeAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMfnRangeAddActionPerformed

        rdbRangeAllAdd.setSelected(true);
        rdbRangeListAdd.setSelected(false);
        txtMfnListAdd.setText("");
    }//GEN-LAST:event_rdbMfnRangeAddActionPerformed

    private void rdbRangeAllDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbRangeAllDeleteActionPerformed
        // TODO add your handling code here:
        rdbRangeAllDelete.setSelected(true);
        rdbRangeListDelete.setSelected(false);
        txtMfnListDelete.setText("");
    }//GEN-LAST:event_rdbRangeAllDeleteActionPerformed

    private void rdbRangeAllReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbRangeAllReplaceActionPerformed
        // TODO add your handling code here:
        rdbRangeAllReplace.setSelected(true);
        rdbRangeListReplace.setSelected(false);
        txtMfnListReplace.setText("");
    }//GEN-LAST:event_rdbRangeAllReplaceActionPerformed

     private boolean confirmGlobalOperation() {
      
      String label = NbBundle.getMessage(GlobalOperationsTopComponent.class, "MSG_ConfirmGlobalOperation", db_.getDbName());
      String title = NbBundle.getMessage(GlobalOperationsTopComponent.class, "MSG_ConfirmGlobalOperationTitle");
      NotifyDescriptor d =
              new NotifyDescriptor.Confirmation(label, title,
              NotifyDescriptor.YES_NO_CANCEL_OPTION);
      Object notify = DialogDisplayer.getDefault().notify(d);
      if (notify == NotifyDescriptor.CANCEL_OPTION) {
         return false;
      } else if (notify == NotifyDescriptor.NO_OPTION) {
         return false;
      } else if (notify == NotifyDescriptor.YES_OPTION) {       
         return true;
      }
      return true;
   }
      
     
     
      
    protected void doAdd(MfnRange[] mfnRanges, Map<String, Object> parameters, GlobalAsyncCallback globalAsyncCallback) {

        if (mfnRanges == null || mfnRanges.length == 0) {
            return;
        }
        cancellable_ = new CancellableProgress();
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Add Data...",
            cancellable_);
        progress.start();
        progress.switchToIndeterminate();
        long cursor;
        long mfn;

        for (MfnRange mfnRange : mfnRanges) {
            if (Thread.interrupted() || cancellable_.cancelRequested()) {
                progress.finish();
                globalAsyncCallback.onCancel();
                return;
            }

            long startMfn = mfnRange.getFirst();
            long endMfn = mfnRange.getLast();
            if (endMfn >= startMfn) {
                for (long j = startMfn; j <= endMfn; j++) {
                    if (Thread.interrupted() || cancellable_.cancelRequested()) {
                        progress.finish();
                        globalAsyncCallback.onCancel();
                        return;
                    }
                    cursor = j;
                    mfn = 0;
                    try {
                        Record record = (Record) db_.getRecordCursor(j);
                        if (record == null) {
                            continue;
                        }
                        mfn = record.getMfn();

                        record = RecordOperations.addToRecord(record, parameters);

                        db_.updateRecordEx(record);

                        progress.setDisplayName("Global Add MFN:" + Long.toString(record.getMfn()));
                    } catch (Exception ex) {
                        LOGGER.error("Error when executing Global Add for MFN: [{}] cursor=[{}]", new Object[]{mfn, cursor}, ex);
                        GuiGlobal.output("Error when executing Global Add for MFN:" + mfn + " cursor=" + cursor + "\n "
                            + ex.getMessage());
                        globalAsyncCallback.onFailure(ex);
                    }
                }
            } else {
                for (long j = startMfn; j >= endMfn; j--) {
                    if (Thread.interrupted() || cancellable_.cancelRequested()) {
                        progress.finish();
                        globalAsyncCallback.onCancel();
                        return;
                    }
                    cursor = j;
                    mfn = 0;
                    try {
                        Record record = (Record) db_.getRecordCursor(j);
                        if (record == null) {
                            continue;
                        }
                        record = RecordOperations.addToRecord(record, parameters);
                        mfn = record.getMfn();
                        db_.updateRecordEx(record);
                        progress.setDisplayName("Global Add MFN:" + Long.toString(record.getMfn()));
                    } catch (Exception ex) {
                        LOGGER.error("Error when executing Global Add for MFN: [{}] cursor=[{}]", new Object[]{mfn, cursor}, ex);
                        GuiGlobal.output("Error when executing Global Add for MFN:" + mfn + " cursor=" + cursor + "\n "
                            + ex.getMessage());
                        globalAsyncCallback.onFailure(ex);
                    }
                }
            }

        }
        progress.finish();
        globalAsyncCallback.onSuccess(null);
        
    }
    /**
     * 
     * @param mfnRanges
     * @param parameters 
     */
    
    protected void doDelete(MfnRange[] mfnRanges, Map<String, Object> parameters, GlobalAsyncCallback globalAsyncCallback) {

        if (mfnRanges == null || mfnRanges.length == 0) {
            return;
        }
        cancellable_ = new CancellableProgress();
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Delete Data...",
            cancellable_);
        progress.start();
        progress.switchToIndeterminate();
        long cursor;
        long mfn;

        for (MfnRange mfnRange : mfnRanges) {
            if (Thread.interrupted() || cancellable_.cancelRequested()) {
                progress.finish();
                globalAsyncCallback.onCancel();
                return;
            }

            long startMfn = mfnRange.getFirst();
            long endMfn = mfnRange.getLast();
            if (endMfn >= startMfn) {
                for (long j = startMfn; j <= endMfn; j++) {
                    if (Thread.interrupted() || cancellable_.cancelRequested()) {
                        progress.finish();
                        globalAsyncCallback.onCancel();
                        return;
                    }
                    cursor = j;
                    mfn = 0;
                    try {
                        Record record = (Record) db_.getRecordCursor(j);
                        if (record == null) {
                            continue;
                        }
                        mfn = record.getMfn();

                        record = RecordOperations.deleteFromRecord(record, parameters);

                        db_.updateRecordEx(record);

                        progress.setDisplayName("Global Add MFN:" + Long.toString(record.getMfn()));
                    } catch (Exception ex) {
                        LOGGER.error("Error when executing Global Delete for MFN: [{}] cursor=[{}]", new Object[]{mfn, cursor}, ex);
                        GuiGlobal.output("Error when executing Global Delete for MFN:" + mfn + " cursor=" + cursor + "\n "
                            + ex.getMessage());

                        globalAsyncCallback.onFailure(ex);
                    }
                }
            } else {
                for (long j = startMfn; j >= endMfn; j--) {
                    if (Thread.interrupted() || cancellable_.cancelRequested()) {
                        progress.finish();
                        globalAsyncCallback.onCancel();
                        return;
                    }
                    cursor = j;
                    mfn = 0;
                    try {
                        Record record = (Record) db_.getRecordCursor(j);
                        if (record == null) {
                            continue;
                        }
                        record = RecordOperations.deleteFromRecord(record, parameters);
                        mfn = record.getMfn();
                        db_.updateRecordEx(record);
                        progress.setDisplayName("Global Add MFN:" + Long.toString(record.getMfn()));
                    } catch (Exception ex) {
                        LOGGER.error("Error when executing Global Delete for MFN: [{}] cursor=[{}]", new Object[]{mfn, cursor}, ex);
                        GuiGlobal.output("Error when executing Global Delete for MFN:" + mfn + " cursor=" + cursor + "\n "
                            + ex.getMessage());

                        globalAsyncCallback.onFailure(ex);
                    }
                }
            }

        }
        progress.finish();
        globalAsyncCallback.onSuccess(null);
    }
     
    protected void doReplace(MfnRange[] mfnRanges, Map<String, Object> parameters, GlobalAsyncCallback globalAsyncCallback) {

        if (mfnRanges == null || mfnRanges.length == 0) {
            return;
        }

        final String textToFind = (String) parameters.get(TEXT_TO_FIND);
        final String replaceWith = (String) parameters.get(REPLACE_WITH);

        final String tagList = (String) parameters.get(REPLACE_FIELD_TAG_KEY);

        final String subfieldList = (String) parameters.get(REPLACE_FIELD_SUBFIELD_KEY);

        final String occurrenceList = (String) parameters.get(REPLACE_FIELD_OCCURRENCE_KEY);

        final int caseSensitive = (Integer) parameters.get(CASE_SENSITIVE); // 1 or 0
        final int wholeWordOnly = (Integer) parameters.get(WHOLE_WORD_ONLY); // 1 or 0
        final int promptOnReplace = (Integer) parameters.get(PROMPT_ON_REPLACE); // 1 or 0

        if (tagList.trim().isEmpty()) {
            // No fields specified !
            return;
        }
        // Get the list tags
        String[] fieldTags = tagList.split(",");

        // Get the occurrence numbers
        String[] occurrences = null;
        if (!occurrenceList.trim().isEmpty()) {
            occurrences = occurrenceList.split(",");
        }

        // Get char subfield tags
        char[] subfieldTags = null;
        if (!subfieldList.trim().isEmpty()) {
            subfieldTags = subfieldList.toCharArray();
        }
        cancellable_ = new CancellableProgress();
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Replacing Data...",
            cancellable_);
        progress.start();
        progress.switchToIndeterminate();
        long cursor;
        long mfn;

        for (MfnRange mfnRange : mfnRanges) {
            if (Thread.interrupted() || cancellable_.cancelRequested()) {
                progress.finish();
                globalAsyncCallback.onCancel();
                return;
            }

            long startMfn = mfnRange.getFirst();
            long endMfn = mfnRange.getLast();
            if (endMfn >= startMfn) {
                for (long j = startMfn; j <= endMfn; j++) {
                    if (Thread.interrupted() || cancellable_.cancelRequested()) {
                        progress.finish();
                        globalAsyncCallback.onCancel();
                        return;
                    }
                    cursor = j;
                    mfn = 0;
                    try {
                        Record record = (Record) db_.getRecordCursor(j);
                        if (record == null) {
                            continue;
                        }
                        mfn = record.getMfn();

                        record = RecordOperations.replaceInRecord(record, fieldTags, occurrences, subfieldTags, textToFind,
                            replaceWith, caseSensitive, wholeWordOnly, promptOnReplace);

                        db_.updateRecordEx(record);
                        progress.setDisplayName("Global Add MFN:" + Long.toString(record.getMfn()));
                    } catch (Exception ex) {
                        LOGGER.error("Error when executing Global Replace for MFN: [{}] cursor=[{}]", new Object[]{mfn, cursor}, ex);
                        GuiGlobal.output("Error when executing Global Replace for MFN:" + mfn + " cursor=" + cursor + "\n "
                            + ex.getMessage());
                    }
                }
            } else {
                for (long j = startMfn; j >= endMfn; j--) {
                    if (Thread.interrupted() || cancellable_.cancelRequested()) {
                        progress.finish();
                        globalAsyncCallback.onCancel();
                        return;
                    }
                    cursor = j;
                    mfn = 0;
                    try {
                        Record record = (Record) db_.getRecordCursor(j);
                         if (record == null) {
                            continue;
                        }
                        record = RecordOperations.replaceInRecord(record, fieldTags, occurrences, subfieldTags, textToFind,
                            replaceWith, caseSensitive, wholeWordOnly, promptOnReplace);
                       
                        mfn = record.getMfn();
                        db_.updateRecordEx(record);
                        progress.setDisplayName("Global Add MFN:" + Long.toString(record.getMfn()));
                    } catch (Exception ex) {
                        LOGGER.error("Error when executing Global Replace for MFN: [{}] cursor=[{}]", new Object[]{mfn, cursor}, ex);
                        GuiGlobal.output("Error when executing Global Replace for MFN:" + mfn + " cursor=" + cursor + "\n "
                            + ex.getMessage());
                         globalAsyncCallback.onFailure(ex);
                    }
                }
            }

        }
        progress.finish();
        globalAsyncCallback.onSuccess(null);
    }
    
    
 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addPanel;
    private javax.swing.JButton btnGoAdd;
    private javax.swing.JButton btnGoDelete;
    private javax.swing.JButton btnGoReplace;
    private javax.swing.ButtonGroup btnGroupRangeAdd;
    private javax.swing.ButtonGroup btnGroupRangeDelete;
    private javax.swing.ButtonGroup btnGroupRangeReplace;
    private javax.swing.ButtonGroup btnGroupRangeTypeAdd;
    private javax.swing.ButtonGroup btnGroupRangeTypeDelete;
    private javax.swing.ButtonGroup btnGroupRangeTypeReplace;
    private javax.swing.JCheckBox chkAddIfNotPresent;
    private javax.swing.JCheckBox chkCaseSensitiveReplace;
    private javax.swing.JCheckBox chkInsertBeforePosition;
    private javax.swing.JCheckBox chkPromptOnReplace;
    private javax.swing.JCheckBox chkWholeWordsOnlyReplace;
    private javax.swing.JComboBox cmbMarkedSetAdd;
    private javax.swing.JComboBox cmbMarkedSetDelete;
    private javax.swing.JComboBox cmbMarkedSetReplace;
    private javax.swing.JComboBox cmbSearchSetAdd;
    private javax.swing.JComboBox cmbSearchSetDelete;
    private javax.swing.JComboBox cmbSearchSetReplace;
    private javax.swing.JPanel deletePanel;
    private javax.swing.JTabbedPane globalTabbedPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblFieldTag;
    private javax.swing.JLabel lblFieldTagDelete;
    private javax.swing.JLabel lblFromMarkedSet;
    private javax.swing.JLabel lblFromMarkedSetAdd;
    private javax.swing.JLabel lblFromMarkedSetDelete;
    private javax.swing.JLabel lblFromSearchSetAdd;
    private javax.swing.JLabel lblFromSearchSetDelete;
    private javax.swing.JLabel lblFromSearchSetReplace;
    private javax.swing.JLabel lblMfnListAdd;
    private javax.swing.JLabel lblNewText;
    private javax.swing.JLabel lblOccurrence;
    private javax.swing.JLabel lblOccurrenceDelete;
    private javax.swing.JLabel lblSubfield;
    private javax.swing.JLabel lblSubfieldDelimiter;
    private javax.swing.JLabel lblToFind;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JRadioButton rdbMarkedRecordsAdd;
    private javax.swing.JRadioButton rdbMarkedRecordsDelete;
    private javax.swing.JRadioButton rdbMarkedRecordsReplace;
    private javax.swing.JRadioButton rdbMfnRangeAdd;
    private javax.swing.JRadioButton rdbMfnRangeDelete;
    private javax.swing.JRadioButton rdbMfnRangeReplace;
    private javax.swing.JRadioButton rdbRangeAllAdd;
    private javax.swing.JRadioButton rdbRangeAllDelete;
    private javax.swing.JRadioButton rdbRangeAllReplace;
    private javax.swing.JRadioButton rdbRangeListAdd;
    private javax.swing.JRadioButton rdbRangeListDelete;
    private javax.swing.JRadioButton rdbRangeListReplace;
    private javax.swing.JRadioButton rdbSearchResultsAdd;
    private javax.swing.JRadioButton rdbSearchResultsDelete;
    private javax.swing.JRadioButton rdbSearchResultsReplace;
    private javax.swing.JPanel replacePanel;
    private javax.swing.JPanel scopePanel;
    private javax.swing.JScrollPane scrollPaneAdd;
    private javax.swing.JScrollPane scrollPaneDelete;
    private javax.swing.JScrollPane scrollPaneReplace;
    private javax.swing.JSpinner spinnerFieldTag;
    private javax.swing.JSpinner spinnerFieldTagDelete;
    private javax.swing.JSpinner spinnerOccurrence;
    private javax.swing.JSpinner spinnerOccurrenceDelete;
    private javax.swing.JSpinner spinnerPositionAdd;
    private javax.swing.JTextArea txtContent;
    private javax.swing.JTextField txtMfnListAdd;
    private javax.swing.JTextField txtMfnListDelete;
    private javax.swing.JTextField txtMfnListReplace;
    private javax.swing.JTextField txtNewText;
    private javax.swing.JTextField txtOccurrencesReplace;
    private javax.swing.JFormattedTextField txtSubfield;
    private javax.swing.JTextField txtSubfieldsReplace;
    private javax.swing.JTextField txtTagsReplace;
    private javax.swing.JTextField txtTextToFind;
    // End of variables declaration//GEN-END:variables
    
  
    @Override
    public void componentOpened() {
        super.componentOpened();
        // TODO add custom code on component opening
    }

    @Override
   public void componentActivated() {
      super.componentActivated();
//      final String OUTPUT_ID = "output";
//      TopComponent outputWindow = WindowManager.getDefault().findTopComponent(OUTPUT_ID);

      /**
       * Be sure TopComponent is maximized
       * http://wiki.netbeans.org/DevFaqWindowsMaximizeViaCode
       */
      //requestActive();
      
//      Action action = org.openide.awt.Actions.forID("Window", "org.netbeans.core.windows.actions.MaximizeWindowAction");
//      action.actionPerformed(null);
      globalTabbedPane.repaint();
   }
    @Override
    public void componentClosed() {
        super.componentClosed();
        if (task_ != null) {
            task_.cancel();
        }
        /**
         * unsets maximzed mode so that next time we set
         */
        Action action = org.openide.awt.Actions.forID("Window", "org.netbeans.core.windows.actions.MaximizeWindowAction");
        action.actionPerformed(null);
    }
     

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    

    private void prepareSearchHistory() {
      List<SearchResult> searchResults = db_.getSearchResults();
      String[] searches = {NO_SEARCH_SETS};
      if (searchResults != null && searchResults.size() > 0) {
         SearchHistoryModel searchHistoryModel = new SearchHistoryModel(searchResults);
         int n = searchHistoryModel.getSize();
         searches = new String[n];
         for (int i = 0; i < n; i++) {
            searches[i] = searchHistoryModel.getElementAt(i).toString();
         }
      }
     cmbSearchSetAdd.setModel(new DefaultComboBoxModel(searches));
     cmbSearchSetDelete.setModel(new DefaultComboBoxModel(searches));
     cmbSearchSetReplace.setModel(new DefaultComboBoxModel(searches));
   }
    private void prepareMarkedRecordsHistory() {
      List<MarkedRecords> markedRecords = db_.getMarkedRecordsList();
      String[] markedSets = {NO_MARKED_SETS};
      if ( markedRecords != null &&  !markedRecords.isEmpty()) {

         int n = markedRecords.size();
         markedSets = new String[n];
         for (int i = 0; i < n; i++) {
            markedSets[i] = markedRecords.get(i).toString();
         }
      }
      cmbMarkedSetAdd.setModel(new DefaultComboBoxModel(markedSets));
      cmbMarkedSetDelete.setModel(new DefaultComboBoxModel(markedSets));
      cmbMarkedSetReplace.setModel(new DefaultComboBoxModel(markedSets));

   }
    /**
     * Private Helper Methods to get TAB Panels Data
     */
    public static final String FIELD_TAG_KEY = "FieldTag"; //NOI18N
    public static final String FIELD_OCCURRENCE_KEY = "FieldOccurrence"; //NOI18N
    public static final String ADD_ONLY_IF_NOT_PRESENT = "AddOnlyIfNotPresent"; //NOI18N
    public static final String INSERT_BEFORE_POSITION_FLAG = "InsertBeforePosition"; //NOI18N
    public static final String INSERT_BEFORE_POSITION_VALUE = "InsertBeforePositionValue"; //NOI18N
    public static final String TEXT_TO_ADD = "TextToAdd"; //NOI18N
 
    private java.util.Map<java.lang.String, java.lang.Object> getGlobalAddParameters() {
        java.util.Map<java.lang.String, java.lang.Object> map = new HashMap<String, Object>();

        map.put(FIELD_TAG_KEY, spinnerFieldTag.getValue());
        map.put(FIELD_OCCURRENCE_KEY, spinnerOccurrence.getValue());
        map.put(ADD_ONLY_IF_NOT_PRESENT, chkAddIfNotPresent.isSelected() ? 1 : 0);
        map.put(INSERT_BEFORE_POSITION_FLAG, chkInsertBeforePosition.isSelected() ? 1 : 0);
        map.put(INSERT_BEFORE_POSITION_VALUE, spinnerPositionAdd.getValue());
        map.put(TEXT_TO_ADD, txtContent.getText());
        return map;
    }
    
    /**
      * Private Helper Methods to get TAB Panels Data
     */
    public static final String DELETE_FIELD_TAG_KEY = "FieldTag"; //NOI18N
    public static final String DELETE_FIELD_OCCURRENCE_KEY = "FieldOccurrence"; //NOI18N
     public static final String DELETE_FIELD_SUBFIELD_KEY = "FieldOccurrenceSubfield"; //NOI18N
  
 
    private java.util.Map<java.lang.String, java.lang.Object> getGlobalDeleteParameters() {
        java.util.Map<java.lang.String, java.lang.Object> map = new HashMap<String, Object>();

        map.put(DELETE_FIELD_TAG_KEY, spinnerFieldTagDelete.getValue());
        map.put(DELETE_FIELD_OCCURRENCE_KEY, spinnerOccurrenceDelete.getValue());
        
        map.put(DELETE_FIELD_SUBFIELD_KEY, txtSubfield.getText());
        return map;
    }
    
    public static final String TEXT_TO_FIND = "TextToFind"; //NOI18N
    public static final String REPLACE_WITH = "ReplaceWithText"; //NOI18N
    public static final String REPLACE_FIELD_TAG_KEY = "FieldTag"; //NOI18N
    public static final String REPLACE_FIELD_OCCURRENCE_KEY = "FieldOccurrence"; //NOI18N
    public static final String REPLACE_FIELD_SUBFIELD_KEY = "FieldOccurrenceSubfield"; //NOI18N
  
   
  
    public static final String CASE_SENSITIVE = "ReplaceCaseSensitive"; //NOI18N
    public static final String WHOLE_WORD_ONLY = "ReplaceWholeWordOnly"; //NOI18N
    public static final String PROMPT_ON_REPLACE = "PromptOnReplace"; //NOI18N
  
 
    private java.util.Map<java.lang.String, java.lang.Object> getGlobalReplaceParameters() {
        java.util.Map<java.lang.String, java.lang.Object> map = new HashMap<String, Object>();

        map.put(TEXT_TO_FIND, txtTextToFind.getText());
        map.put(REPLACE_WITH, txtNewText.getText());

        map.put(REPLACE_FIELD_TAG_KEY, txtTagsReplace.getText());
        map.put(REPLACE_FIELD_OCCURRENCE_KEY, txtOccurrencesReplace.getText());
        map.put(REPLACE_FIELD_SUBFIELD_KEY, txtSubfieldsReplace.getText());

        map.put(CASE_SENSITIVE, chkCaseSensitiveReplace.isSelected() ? 1 : 0);
        map.put(WHOLE_WORD_ONLY, chkWholeWordsOnlyReplace.isSelected() ? 1 : 0);
        map.put(PROMPT_ON_REPLACE, chkPromptOnReplace.isSelected() ? 1 : 0);

        return map;
    }
    
   
    
    private MfnRange[] getGlobalAddRanges() {
        final int option = getAddMfnsRangeOption();
        MfnRange[] mfnRanges = null;
        switch (option) {
            case Global.MFNS_OPTION_ALL:

                try {
                    mfnRanges = new MfnRange[1];
                    mfnRanges[0] = new MfnRange(db_.getFirst().getMfn(),
                        db_.getLast().getMfn());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                break;
            case Global.MFNS_OPTION_RANGE:

                String s = txtMfnListAdd.getText();
                mfnRanges = Global.parseMfns(s);
                HistoryTextField historyTextField = (HistoryTextField) txtMfnListAdd;
                historyTextField.addCurrentToHistory();
                break;
            case Global.MFNS_OPTION_MARKED:

                int markedIndex = cmbMarkedSetAdd.getSelectedIndex();
                if (markedIndex < 0) {
                    break;
                }
                List<MarkedRecords> markedRecordsList = db_.getMarkedRecordsList();
                MarkedRecords markedRecords = markedRecordsList.get(markedIndex);
                List<Long> mfns = markedRecords.getMfns();
                mfnRanges = new MfnRange[mfns.size()];
                for (int i = 0; i < mfns.size(); i++) {
                    long mfn = mfns.get(i);
                    mfnRanges[i] = new MfnRange(mfn, mfn);
                }

                break;
            case Global.MFNS_OPTION_SEARCH:
                int searchIndex = cmbSearchSetAdd.getSelectedIndex();
                List<SearchResult> searchResults = db_.getSearchResults();
                SearchResult searchResult = searchResults.get(searchIndex);
                mfns = searchResult.getMfns();
                mfnRanges = new MfnRange[mfns.size()];
                for (int i = 0; i < mfns.size(); i++) {
                    long mfn = mfns.get(i);
                    mfnRanges[i] = new MfnRange(mfn, mfn);
                }

                break;

        }
        return mfnRanges;

    }
    
     private MfnRange[] getGlobalDeleteRanges() {
        final int option = getDeleteMfnsRangeOption();
        MfnRange[] mfnRanges = null;
        switch (option) {
            case Global.MFNS_OPTION_ALL:

                try {
                    mfnRanges = new MfnRange[1];
                    mfnRanges[0] = new MfnRange(db_.getFirst().getMfn(),
                        db_.getLast().getMfn());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                break;
            case Global.MFNS_OPTION_RANGE:

                String s = txtMfnListDelete.getText();
                mfnRanges = Global.parseMfns(s);
                HistoryTextField historyTextField = (HistoryTextField) txtMfnListDelete;
                historyTextField.addCurrentToHistory();
                break;
            case Global.MFNS_OPTION_MARKED:

                int markedIndex = cmbMarkedSetDelete.getSelectedIndex();
                if (markedIndex < 0) {
                    break;
                }
                List<MarkedRecords> markedRecordsList = db_.getMarkedRecordsList();
                MarkedRecords markedRecords = markedRecordsList.get(markedIndex);
                List<Long> mfns = markedRecords.getMfns();
                mfnRanges = new MfnRange[mfns.size()];
                for (int i = 0; i < mfns.size(); i++) {
                    long mfn = mfns.get(i);
                    mfnRanges[i] = new MfnRange(mfn, mfn);
                }

                break;
            case Global.MFNS_OPTION_SEARCH:
                int searchIndex = cmbSearchSetDelete.getSelectedIndex();
                List<SearchResult> searchResults = db_.getSearchResults();
                SearchResult searchResult = searchResults.get(searchIndex);
                mfns = searchResult.getMfns();
                mfnRanges = new MfnRange[mfns.size()];
                for (int i = 0; i < mfns.size(); i++) {
                    long mfn = mfns.get(i);
                    mfnRanges[i] = new MfnRange(mfn, mfn);
                }

                break;

        }
        return mfnRanges;

    }
     
      private MfnRange[] getGlobalReplaceRanges() {
        final int option = getReplaceMfnsRangeOption();
        MfnRange[] mfnRanges = null;
        switch (option) {
            case Global.MFNS_OPTION_ALL:

                try {
                    mfnRanges = new MfnRange[1];
                    mfnRanges[0] = new MfnRange(db_.getFirst().getMfn(),
                                                db_.getLast().getMfn());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                break;
            case Global.MFNS_OPTION_RANGE:

                String s = txtMfnListReplace.getText();
                mfnRanges = Global.parseMfns(s);
                HistoryTextField historyTextField = (HistoryTextField) txtMfnListReplace;
                historyTextField.addCurrentToHistory();
                break;
            case Global.MFNS_OPTION_MARKED:

                int markedIndex = cmbMarkedSetReplace.getSelectedIndex();
                if (markedIndex < 0) {
                    break;
                }
                List<MarkedRecords> markedRecordsList = db_.getMarkedRecordsList();
                MarkedRecords markedRecords = markedRecordsList.get(markedIndex);
                List<Long> mfns = markedRecords.getMfns();
                mfnRanges = new MfnRange[mfns.size()];
                for (int i = 0; i < mfns.size(); i++) {
                    long mfn = mfns.get(i);
                    mfnRanges[i] = new MfnRange(mfn, mfn);
                }

                break;
            case Global.MFNS_OPTION_SEARCH:
                int searchIndex = cmbSearchSetReplace.getSelectedIndex();
                List<SearchResult> searchResults = db_.getSearchResults();
                SearchResult searchResult = searchResults.get(searchIndex);
                mfns = searchResult.getMfns();
                mfnRanges = new MfnRange[mfns.size()];
                for (int i = 0; i < mfns.size(); i++) {
                    long mfn = mfns.get(i);
                    mfnRanges[i] = new MfnRange(mfn, mfn);
                }

                break;

        }
        return mfnRanges;

    }
     
    
    public int getAddMfnsRangeOption() {
        if (rdbMfnRangeAdd.isSelected()) {
            if (rdbRangeAllAdd.isSelected()) {
                return Global.MFNS_OPTION_ALL;
            } else if (rdbRangeListAdd.isSelected()) {
                return Global.MFNS_OPTION_RANGE;
            }
        } else if (rdbSearchResultsAdd.isSelected()) {
            return Global.MFNS_OPTION_SEARCH;

        } else if (rdbMarkedRecordsAdd.isSelected()) {

            return Global.MFNS_OPTION_MARKED;
        }

        // Should not happen !
        throw new RuntimeException("GlobalOperationsTopComponent inconsistent state in radio buttons!");

    }
    
    public int getDeleteMfnsRangeOption() {
        if (rdbMfnRangeDelete.isSelected()) {
            if (rdbRangeAllDelete.isSelected()) {
                return Global.MFNS_OPTION_ALL;
            } else if (rdbRangeListDelete.isSelected()) {
                return Global.MFNS_OPTION_RANGE;
            }
        } else if (rdbSearchResultsDelete.isSelected()) {
            return Global.MFNS_OPTION_SEARCH;

        } else if (rdbMarkedRecordsDelete.isSelected()) {

            return Global.MFNS_OPTION_MARKED;
        }

        // Should not happen !
        throw new RuntimeException("GlobalOperationsTopComponent inconsistent state in radio buttons!");

    }
 
    public int getReplaceMfnsRangeOption() {
        if (rdbMfnRangeReplace.isSelected()) {
            if (rdbRangeAllReplace.isSelected()) {
                return Global.MFNS_OPTION_ALL;
            } else if (rdbRangeListReplace.isSelected()) {
                return Global.MFNS_OPTION_RANGE;
            }
        } else if (rdbSearchResultsReplace.isSelected()) {
            return Global.MFNS_OPTION_SEARCH;

        } else if (rdbMarkedRecordsReplace.isSelected()) {

            return Global.MFNS_OPTION_MARKED;
        }

        // Should not happen !
        throw new RuntimeException("GlobalOperationsTopComponent inconsistent state in radio buttons!");

    }

    @Override
    public void update(Observable o, Object arg) {

        if (db_.searchHistoryHasChanged()) {
            prepareSearchHistory();
        }
        if (db_.markedRecordsHasChanged()) {
            prepareMarkedRecordsHistory();
        }
        if (((String) cmbSearchSetAdd.getItemAt(0)).equals(NO_SEARCH_SETS)) {
            rdbSearchResultsAdd.setEnabled(false);

        } else {
            rdbSearchResultsAdd.setEnabled(true);

        }
        if (((String) cmbMarkedSetAdd.getItemAt(0)).equals(NO_MARKED_SETS)) {
            rdbMarkedRecordsAdd.setEnabled(false);

        } else {
            rdbMarkedRecordsAdd.setEnabled(true);

        }
        
    }


  static class CancellableProgress implements Cancellable {

      private boolean cancelled_ = false;

      @Override
      public boolean cancel() {
         cancelled_ = true;
         return true;
      }

      public boolean cancelRequested() {
         return cancelled_;
      }
   }
  
    private class RangeAddRadioListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
             if (e.getSource() == rdbMfnRangeAdd) { 
                rdbRangeAllAdd.setEnabled(true);
                rdbRangeListAdd.setEnabled(true);
                 rdbRangeAllAdd.doClick();
             } else if (e.getSource() == rdbRangeAllAdd) {
                // Disable List field, Search Results Set Combo and Marked Record Combo
                txtMfnListAdd.setText("");
                if (txtMfnListAdd.getBackground( ) == Color.RED) {
                    txtMfnListAdd.setBackground( UIManager.getColor( "TextField.background" ) ); 
                }
                txtMfnListAdd.setEnabled(false);
               
                cmbSearchSetAdd.setEnabled(false);
                cmbMarkedSetAdd.setEnabled(false);
                if (((String)cmbSearchSetAdd.getItemAt(0)).equals(NO_SEARCH_SETS)) {
                    rdbSearchResultsAdd.setEnabled(false);
                   
                } else {
                     rdbSearchResultsAdd.setEnabled(true);
                    
                }
                 if (((String)cmbMarkedSetAdd.getItemAt(0)).equals(NO_MARKED_SETS)) {
                    rdbMarkedRecordsAdd.setEnabled(false);
                   
                } else {
                    rdbMarkedRecordsAdd.setEnabled(true);
                   
                 }
            } else if (e.getSource() == rdbRangeListAdd) {
                // Enable List field, and disable Search Results Set Combo and Marked Record Combo
                txtMfnListAdd.setEnabled(true);
                cmbSearchSetAdd.setEnabled(false);
                cmbMarkedSetAdd.setEnabled(false);
                if (((String) cmbSearchSetAdd.getItemAt(0)).equals(NO_SEARCH_SETS)) {
                    rdbSearchResultsAdd.setEnabled(false);

                }else {
                     rdbSearchResultsAdd.setEnabled(true);

                }
                if (((String) cmbMarkedSetAdd.getItemAt(0)).equals(NO_MARKED_SETS)) {
                    rdbMarkedRecordsAdd.setEnabled(false);
 
                }else {
                    rdbMarkedRecordsAdd.setEnabled(true);
 
                 }
            } else if (e.getSource() == rdbSearchResultsAdd) {
                rdbRangeAllAdd.setEnabled(false);
                rdbRangeListAdd.setEnabled(false);
                // Disable List field,  Marked Record Combo, and enable Search Set Combo
                txtMfnListAdd.setEnabled(false);
                cmbSearchSetAdd.setEnabled(true);
                cmbMarkedSetAdd.setEnabled(false);
            } else if (e.getSource() == rdbMarkedRecordsAdd) {
               rdbRangeAllAdd.setEnabled(false);
                rdbRangeListAdd.setEnabled(false);
                // Disable List field,  Search Set Combo, and enable  Marked Record Combo
                txtMfnListAdd.setEnabled(false);
                cmbSearchSetAdd.setEnabled(false);
                cmbMarkedSetAdd.setEnabled(true);
            }

        }
    }

    private class RangeDeleteRadioListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() == rdbRangeAllDelete) {
                txtMfnListDelete.setText("");
                if (txtMfnListDelete.getBackground( ) == Color.RED) {
                    txtMfnListDelete.setBackground( UIManager.getColor( "TextField.background" ) ); 
                }
                
                // Disable List field, Search Results Set Combo and Marked Record Combo
                txtMfnListDelete.setEnabled(false);
                cmbSearchSetDelete.setEnabled(false);
                cmbMarkedSetDelete.setEnabled(false);
            } else if (e.getSource() == rdbRangeListDelete) {
                // Enable List field, and disable Search Results Set Combo and Marked Record Combo
                txtMfnListDelete.setEnabled(true);
                cmbSearchSetDelete.setEnabled(false);
                cmbMarkedSetDelete.setEnabled(false);
            } else if (e.getSource() == rdbSearchResultsDelete) {
                // Disable List field,  Marked Record Combo, and enable Search Set Combo
                txtMfnListDelete.setEnabled(false);
                cmbSearchSetDelete.setEnabled(true);
                cmbMarkedSetDelete.setEnabled(false);
            } else if (e.getSource() == rdbMarkedRecordsDelete) {
                // Disable List field,  Search Set Combo, and enable  Marked Record Combo
                txtMfnListDelete.setEnabled(false);
                cmbSearchSetDelete.setEnabled(false);
                cmbMarkedSetDelete.setEnabled(true);
            }

        }
    }

    private class RangeReplaceRadioListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() == rdbRangeAllReplace) {
                txtMfnListReplace.setText("");
                if (txtMfnListReplace.getBackground( ) == Color.RED) {
                    txtMfnListReplace.setBackground( UIManager.getColor( "TextField.background" ) ); 
                }
                // Disable List field, Search Results Set Combo and Marked Record Combo
                txtMfnListReplace.setEnabled(false);
                cmbSearchSetReplace.setEnabled(false);
                cmbMarkedSetReplace.setEnabled(false);
            } else if (e.getSource() == rdbRangeListReplace) {
                // Enable List field, and disable Search Results Set Combo and Marked Record Combo
                txtMfnListReplace.setEnabled(true);
                cmbSearchSetReplace.setEnabled(false);
                cmbMarkedSetReplace.setEnabled(false);
            } else if (e.getSource() == rdbSearchResultsReplace) {
                // Disable List field,  Marked Record Combo, and enable Search Set Combo
                txtMfnListReplace.setEnabled(false);
                cmbSearchSetReplace.setEnabled(true);
                cmbMarkedSetReplace.setEnabled(false);
            } else if (e.getSource() == rdbMarkedRecordsReplace) {
                // Disable List field,  Search Set Combo, and enable  Marked Record Combo
                txtMfnListReplace.setEnabled(false);
                cmbSearchSetReplace.setEnabled(false);
                cmbMarkedSetReplace.setEnabled(true);
            }

        }
    }
    /**
    * On gaining focus place the cursor at the start of the text. 
    * This is used for setting the subfield fieldTag  character delimiter
    */
   public class CursorAtStartFocusListener extends FocusAdapter {

      @Override
      public void focusGained(java.awt.event.FocusEvent evt) {
         Object source = evt.getSource();
         if (source instanceof JTextComponent) {
            JTextComponent comp = (JTextComponent) source;
            comp.setCaretPosition(0);
         } else {
            LOGGER.error(
                    "A text component expected instead of {0}",
                    source.getClass().getName());
         }
      }
   }
    
   /**
    * A component's input verifier is consulted whenever the component is about to lose the focus.
    * 
    * public void setVerifyInputWhenFocusTarget(boolean verifyInputWhenFocusTarget)
    * 
    * Sets the value to indicate whether input verifier for the current focus owner will be called before this
    * component requests focus. The default is true. Set to false on components such as a Cancel button or a
    * scrollbar, which should activate even if the input in the current focus owner is not "passed" by the 
    * input verifier for that component.
    * 
    * Note: This is done by unchecking  verifyInputWhenFocusTarget property in GUI builder
    */
   public class MfnListInputVerifier extends InputVerifier {

      @Override
      public boolean verify(JComponent input) {
         String text = ((JTextField) input).getText();
         

         List<String> errors = checkIfValidMfnList(text);
         if (errors.isEmpty()) {
            input.setBackground( UIManager.getColor( "TextField.background" ) );  
             return true;
         }
          input.setBackground( Color.red );
         return false;
      }
      
      /**
       * This method is called when the user attempts to advance focus out of the argument component into 
       * another Swing component in this window. 
       * @param input
       * @return 
       */
       @Override
        public boolean shouldYieldFocus(JComponent input) {
            boolean valid = verify(input);
            if (!valid) {
                NotifyDescriptor d
                    = new NotifyDescriptor.Message(NbBundle.getMessage(GlobalOperationsTopComponent.class,
                            "MSG_GLOBAL_INVALID_DATA"));

                DialogDisplayer.getDefault().notify(d);

            }
            return valid;
        }
    }

   /**
    * Validates if input String is a number
    */
   private String checkIfValidMfn(String s) {
      long mfn;
      String error = "";
      try {
         mfn = Long.parseLong(s);

         try {
            if (mfn <= 0 || mfn > db_.getLastMfn()) {
               error = "Invalid MFN < 0 or Greater Than Last MFN:" + s;
            }
         } catch (DbException ex) {
            error = "Error when getting last MFN";
         }
      } catch (NumberFormatException ex) {
         error = "Error parsing MFN:" + s;
      }
      return error;
   }

   /**
    * Check if MFN list is valid
    *
    * Syntax: Enter MFNs and/or MFN ranges separated by commas. For example
    * 1,10,100-150,50
    *
    * @param s - The list to check
    * @return
    */
   public List<String> checkIfValidMfnList(String s) {

      List<String> errors = new ArrayList<String>();
      // Get the list items
      String[] ranges = s.split(",");

      int n = ranges.length;
      if (n == 0) {
         errors.add("List of MFNs is empty !");
         return errors;
      }

      String sMfn;
      for (int i = 0; i < n; i++) {
         String[] range = ranges[i].trim().split("-");
         if (range.length == 1) {
            // Single MFN number
            sMfn = range[0].trim();
            String error = checkIfValidMfn(sMfn);
            if (!error.equals("")) {
               errors.add(error);
            }

         } else if (range.length == 2) {
            // Range of MFNs
            sMfn = range[0].trim();
            String error = checkIfValidMfn(sMfn);
            if (!error.equals("")) {
               errors.add(error);
            }

            sMfn = range[1].trim();
            error = checkIfValidMfn(sMfn);
            if (!error.equals("")) {
               errors.add(error);
            }

         } else {
            // Error
            String error = "Invalid List Item: " + s;
            errors.add(error);
         }
      }
      return errors;
   }
   
    public class TagInputVerifier extends InputVerifier {

        @Override
        public boolean verify(JComponent input) {
            int i=0;
            if (!(input instanceof JSpinner)) {
                return true;
            }
            JSpinner jspinner = (JSpinner) input;
            int itag = (Integer) jspinner.getValue();
            
            return (itag > 0);
        }
        
         /**
       * This method is called when the user attempts to advance focus out of the argument component into 
       * another Swing component in this window. 
       * @param input
       * @return 
       */
       @Override
        public boolean shouldYieldFocus(JComponent input) {
            boolean valid = verify(input);
            if (!valid) {
                NotifyDescriptor d
                    = new NotifyDescriptor.Message(NbBundle.getMessage(GlobalOperationsTopComponent.class,
                            "MSG_GLOBAL_INVALID_DATA"));

                DialogDisplayer.getDefault().notify(d);

            }
            return valid;
        }

    }
    
    static class GlobalAsyncCallback implements AsyncCallback {
        GlobalOperationsTopComponent topComponent_;
        public GlobalAsyncCallback(GlobalOperationsTopComponent topComponent) {
            topComponent_ = topComponent;
        }
        @Override
        public void onFailure(Throwable caught) {
            LOGGER.error("Global Operation Failure:", caught);
        }

        /**
         * Called when Global Operation is finished successfully 
         * @param result 
         */
        @Override
        public void onSuccess(Object result) {
           
            

        }

        @Override
        public void onCancel() {
            if (task_ != null) {
                task_.cancel();
            }
        }

       
        
    }
    
//    private static final class FieldTagZeroIllegalValidator extends AbstractValidator<String> {
//
//        protected FieldTagZeroIllegalValidator() {
//            super(String.class);
//        }
//
//        @Override
//        public void validate(Problems problems, String compName, String model) {
//            int value =  Integer.parseInt(model);
//            if (value == 0) {
//                String message = NbBundle.getMessage(FieldTagZeroIllegalValidator.class,
//                    "MSG_FIELD_TAG_MAY_NOT_BE_ZERO", compName); //NOI18N
//                problems.add(message);
//            }
//        }
//
//    }
}
     
   

  






