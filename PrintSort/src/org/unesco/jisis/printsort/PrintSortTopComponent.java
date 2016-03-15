/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.printsort;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import java.awt.Dimension;

import java.awt.EventQueue;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.KeyInfo;
import org.unesco.jisis.corelib.common.MfnRange;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.FormattingException;
import org.unesco.jisis.corelib.index.ParsedFstEntry;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.pft.interpretor.HtmlContext;
import org.unesco.jisis.corelib.pft.interpretor.IContext;
import org.unesco.jisis.corelib.pft.interpretor.PlainTextContext;
import org.unesco.jisis.corelib.record.FieldFactory;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.corelib.util.Util;
import org.unesco.jisis.gui.DirectoryChooser;
import org.unesco.jisis.gui.EditorDlgActionTableCellEditor;
import org.unesco.jisis.gui.GuiUtils;
import org.unesco.jisis.jisiscore.client.*;
import org.unesco.jisis.jisiscore.common.ColumnData;
import org.unesco.jisis.jisisutils.FileExtFilter;
import org.unesco.jisis.jisisutils.gui.CheckCellRenderer;

import org.unesco.jisis.jisisutil.history.HistoryTextArea;
import org.unesco.jisis.jisisutil.history.HistoryTextField;
import org.unesco.jisis.jisisutils.gui.SpinnerCellEditor;
import org.unesco.jisis.searchhistory.SearchHistoryModel;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
final class PrintSortTopComponent extends TopComponent implements Observer {

    private static PrintSortTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "PrintSortTopComponent";
    private ClientDatabaseProxy db_ = null;
    private FileOutputStream out_; // declare a file output object
    private PrintStream printStream_; // declare a print stream object
    private RequestProcessor.Task task_ = null;
    private CancellableProgress cancellable_;

    static final int PRINT_NO_SORTING = 0;
    static final int PRINT_SORT_HITFILE = 1;
    static final int PRINT_SORT_TAB = 2;

    private File tmpHitSortFile_;    // The temp hit sort file

    private File dbHitSortFile_;   // The database hit sort file

    private File dbHitSortHxfFile_; // The database form data that created the database hit sort file

    private ParsedSortKey[] parsedSortKeys_;

    private KeyInfo[] keys_;

    private String outputDirectory;

    /**
     * Creates a new named RequestProcessor with defined throughput which can support interruption of the
     * thread the processor runs in. public RequestProcessor(String name, int throughput, boolean
     * interruptThread)
     *
     * Parameters: name - the name to use for the request processor thread throughput - the maximal count of
     * requests allowed to run in parallel interruptThread - true if RequestProcessor.Task.cancel() shall
     * interrupt the thread
     */
    private final static RequestProcessor requestProcessor_ = new RequestProcessor("interruptible tasks", 1, true);

    public PrintSortTopComponent(IDatabase db) {

        if (db instanceof ClientDatabaseProxy) {
            db_ = (ClientDatabaseProxy) db;
        } else {
            throw new RuntimeException("PrintSortTopComponent: Cannot cast DB to ClientDatabaseProxy");
        }

        /* Register this TopComponent as attached to this DB */
        db_.addWindow(this);

        /* Add this TopComponent as Observer to DB changes */
        db_.addObserver((Observer) this);

        initComponents();

        outputDirectory = Global.getClientWorkPath();

        String dbHitSortFilePath = Global.getClientWorkPath() + File.separator
            + db_.getDbName()
            + Global.HIT_SORT_FILE_EXT;
        dbHitSortFile_ = new File(dbHitSortFilePath);

        String dbHitSortHxfFilePath = Global.getClientWorkPath() + File.separator
            + db_.getDbName()
            + Global.HIT_SORT_HXF_FILE_EXT;
        dbHitSortHxfFile_ = new File(dbHitSortHxfFilePath);

        String[] pftNames = null;
        try {
            txtPrintFromDB.setText(db_.getDatabaseName());
            pftNames = db_.getPrintFormatNames();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        cmbSelectFormat.setModel(new DefaultComboBoxModel(pftNames));
        txtOutputDir.setText(outputDirectory);

        prepareSearchHistory();
        prepareMarkedRecordsHistory();

      //cmbSelectFst.setModel(new DefaultComboBoxModel(getFstNames()));
        setTableSortKeys();
        try {
            setName(NbBundle.getMessage(PrintSortTopComponent.class, "CTL_PrintSortTopComponent")
                + " (" + db_.getDbHome() + "//" + db_.getDatabaseName() + ")");
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        setToolTipText(NbBundle.getMessage(PrintSortTopComponent.class, "HINT_PrintSortTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

    }

    private void prepareSearchHistory() {
        List<SearchResult> searchResults = db_.getSearchResults();
        String[] searches = {"No Search"};
        if (searchResults != null && searchResults.size() > 0) {
            SearchHistoryModel searchHistoryModel = new SearchHistoryModel(searchResults);
            int n = searchHistoryModel.getSize();
            searches = new String[n];
            for (int i = 0; i < n; i++) {
                searches[i] = searchHistoryModel.getElementAt(i).toString();
            }
        }
        cmbSearch.setModel(new DefaultComboBoxModel(searches));
        cmbSearchSort.setModel(new DefaultComboBoxModel(searches));
    }

    private void prepareMarkedRecordsHistory() {
        List<MarkedRecords> markedRecords = db_.getMarkedRecordsList();
        String[] markedSets = {"No Marked Sets"};
        if (markedRecords != null && !markedRecords.isEmpty()) {

            int n = markedRecords.size();
            markedSets = new String[n];
            for (int i = 0; i < n; i++) {
                markedSets[i] = markedRecords.get(i).toString();
            }
        }
        cmbMarked.setModel(new DefaultComboBoxModel(markedSets));
        cmbMarkedSort.setModel(new DefaultComboBoxModel(markedSets));

    }

    private void setTableSortKeys() {
        SortKeyTableModel sortKeyTableModel = new SortKeyTableModel();
        tableSortKeys.setModel(sortKeyTableModel);
        tableSortKeys.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableSortKeys.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        for (int i = 0; i < sortKeyTableModel.getColumnCount(); i++) {
            TableCellEditor editor;
            DefaultCellEditor defaultCellEditor;
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            TableColumn tc = null;
            int w;
            switch (i) {
                case SortKeyData.COL_CHECK:
                    TableCellRenderer r = new CheckCellRenderer();
                    JCheckBox checkBox = new JCheckBox();
                    checkBox.setHorizontalAlignment(JCheckBox.CENTER);
                    editor = new DefaultCellEditor(checkBox);
                    w = SortKeyTableModel.columns_[i].width_;
                    tc = new TableColumn(i, w, r, editor);
                    break;
                case SortKeyData.COL_KEY:
                    renderer.setHorizontalAlignment(SortKeyTableModel.columns_[i].alignment_);
                    w = SortKeyTableModel.columns_[i].width_;
                    tc = new TableColumn(i, w, renderer, null);
                    break;
                case SortKeyData.COL_FST:

                    renderer.setHorizontalAlignment(SortKeyTableModel.columns_[i].alignment_);
                    w = SortKeyTableModel.columns_[i].width_;

                    EditorDlgActionTableCellEditor actionEditor = null;
                    JTextField textField = new JTextField();
                    textField.setBorder(BorderFactory.createEmptyBorder());
                    defaultCellEditor = new DefaultCellEditor(textField);
                    defaultCellEditor.setClickCountToStart(1);
                    actionEditor = new EditorDlgActionTableCellEditor(defaultCellEditor);

                    tc = new TableColumn(i, w, renderer, actionEditor);
                    break;
                case SortKeyData.COL_LENGTH:
                    JSpinner spinLength = new JSpinner(new SpinnerNumberModel(15, 1, 1000, 1));

                    editor = new SpinnerCellEditor(spinLength);
                    w = SortKeyTableModel.columns_[i].width_;
                    tc = new TableColumn(i, w, renderer, editor);
                    break;
                case SortKeyData.COL_INDICATOR:
                    JSpinner spinIndicator = new JSpinner(new SpinnerNumberModel(0, 0, 3, 1));

                    editor = new SpinnerCellEditor(spinIndicator);
                    w = SortKeyTableModel.columns_[i].width_;
                    tc = new TableColumn(i, w, renderer, editor);
                    break;

            }
            tableSortKeys.addColumn(tc);
        }
        GuiUtils.TweakJTable(tableSortKeys);

    }

    private String[] getFstNames() {

        String[] fstNames = db_.getFstNames();
        String[] allFstNames = new String[fstNames.length + 1];
        allFstNames[0] = Global.DEFAULT_FST_NAME;
        for (int i = 0; i < fstNames.length; i++) {
            allFstNames[i + 1] = fstNames[i];
        }
        return allFstNames;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        printSortTabbedPane = new javax.swing.JTabbedPane();
        scrollPanePrint = new javax.swing.JScrollPane();
        printPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        outputFileName = new org.unesco.jisis.jisisutil.history.HistoryTextField(db_.getDbName()+"_outputFileName");
        lblOutputDirectory = new javax.swing.JLabel();
        txtOutputDir = new org.unesco.jisis.jisisutil.history.HistoryTextField(db_.getDbName()+"_txtOutputDir");
        btnBrowse = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        rdbAllMfn = new javax.swing.JRadioButton();
        rdbMfnRange = new javax.swing.JRadioButton();
        rdbMfns = new javax.swing.JRadioButton();
        txtMfns = new org.unesco.jisis.jisisutil.history.HistoryTextField(db_.getDbName()+"_txtMfns");
        jLabel10 = new javax.swing.JLabel();
        rdbMarked = new javax.swing.JRadioButton();
        cmbMarked = new javax.swing.JComboBox();
        rdbSearchResults = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        cmbSearch = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        txtPrintFromDB = new javax.swing.JTextField();
        btnLastSetting = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cmbOutputFormat = new javax.swing.JComboBox();
        cmbSelectFormat = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        rdbNoSorting = new javax.swing.JRadioButton();
        rdbHitFileSort = new javax.swing.JRadioButton();
        rdbSortTab = new javax.swing.JRadioButton();
        scrollPaneSort = new javax.swing.JScrollPane();
        sortPanel = new javax.swing.JPanel();
        selectPanel = new javax.swing.JPanel();
        rdbMfnRangeSort = new javax.swing.JRadioButton();
        rdbAllMfnSort = new javax.swing.JRadioButton();
        rdbMfnsSort = new javax.swing.JRadioButton();
        txtMfnsSort = new org.unesco.jisis.jisisutil.history.HistoryTextField(db_.getDbName()+"_txtMfnsSort");
        jLabel11 = new javax.swing.JLabel();
        rdbMarkedSort = new javax.swing.JRadioButton();
        cmbMarkedSort = new javax.swing.JComboBox();
        rdbSearchResultsSort = new javax.swing.JRadioButton();
        cmbSearchSort = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        headingPanel = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        spinHeaderNumber = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        lblHeadingLevelIndentation = new javax.swing.JLabel();
        spinHeadingLevelIndentation = new javax.swing.JSpinner();
        chkPrintWithHeadings = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtHeadingFormat = new HistoryTextArea(db_.getDbName()+"_txtHeadingFormat");
        sorKeysPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableSortKeys = new javax.swing.JTable();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        btnSort = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        printSortTabbedPane.setName("Print..."); // NOI18N

        printPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jPanel2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel3.text")); // NOI18N

        outputFileName.setText(org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.outputFileName.text")); // NOI18N

        lblOutputDirectory.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblOutputDirectory, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.lblOutputDirectory.text")); // NOI18N

        txtOutputDir.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(btnBrowse, NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.btnBrowse.text")); // NOI18N
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        btnPrint.setMnemonic('P');
        org.openide.awt.Mnemonics.setLocalizedText(btnPrint, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.btnPrint.text")); // NOI18N
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        btnCancel.setMnemonic('C');
        org.openide.awt.Mnemonics.setLocalizedText(btnCancel, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.btnCancel.text")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(outputFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblOutputDirectory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtOutputDir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBrowse)
                        .addGap(65, 65, 65)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnPrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(103, 103, 103))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(outputFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrint))
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOutputDirectory)
                    .addComponent(txtOutputDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse)
                    .addComponent(btnCancel))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        buttonGroup2.add(rdbAllMfn);
        rdbAllMfn.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbAllMfn, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbAllMfn.text")); // NOI18N
        rdbAllMfn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbAllMfnActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdbMfnRange);
        rdbMfnRange.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rdbMfnRange.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbMfnRange, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbMfnRange.text")); // NOI18N
        rdbMfnRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMfnRangeActionPerformed(evt);
            }
        });

        buttonGroup2.add(rdbMfns);
        org.openide.awt.Mnemonics.setLocalizedText(rdbMfns, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbMfns.text")); // NOI18N
        rdbMfns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMfnsActionPerformed(evt);
            }
        });

        txtMfns.setText(org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.txtMfns.text")); // NOI18N
        txtMfns.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel10.text")); // NOI18N

        buttonGroup2.add(rdbMarked);
        org.openide.awt.Mnemonics.setLocalizedText(rdbMarked, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbMarked.text")); // NOI18N
        rdbMarked.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMarkedActionPerformed(evt);
            }
        });

        cmbMarked.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        buttonGroup1.add(rdbSearchResults);
        rdbSearchResults.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbSearchResults, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbSearchResults.text")); // NOI18N
        rdbSearchResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSearchResultsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel2.text")); // NOI18N

        cmbSearch.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSearch.setEnabled(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rdbMfnRange)
                            .addComponent(rdbSearchResults))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(rdbMfns)
                            .addComponent(rdbAllMfn)))
                    .addComponent(rdbMarked))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addContainerGap(179, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbMarked, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtMfns, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
                            .addComponent(cmbSearch, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(113, 113, 113))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rdbAllMfn)
                            .addComponent(rdbMfnRange))
                        .addGap(21, 21, 21)
                        .addComponent(rdbMfns))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtMfns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbMarked)
                    .addComponent(cmbMarked, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbSearchResults)
                    .addComponent(jLabel2)
                    .addComponent(cmbSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jPanel4.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        txtPrintFromDB.setEditable(false);
        txtPrintFromDB.setText(org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.txtPrintFromDB.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnLastSetting, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.btnLastSetting.text")); // NOI18N
        btnLastSetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastSettingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtPrintFromDB, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLastSetting)
                .addGap(233, 233, 233))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(txtPrintFromDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnLastSetting))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel4.text")); // NOI18N

        cmbOutputFormat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "HTML", "Plain Text" }));
        cmbOutputFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbOutputFormatActionPerformed(evt);
            }
        });

        cmbSelectFormat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSelectFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSelectFormatActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel16.text")); // NOI18N

        buttonGroup3.add(rdbNoSorting);
        rdbNoSorting.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbNoSorting, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbNoSorting.text")); // NOI18N

        buttonGroup3.add(rdbHitFileSort);
        org.openide.awt.Mnemonics.setLocalizedText(rdbHitFileSort, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbHitFileSort.text")); // NOI18N

        buttonGroup3.add(rdbSortTab);
        org.openide.awt.Mnemonics.setLocalizedText(rdbSortTab, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbSortTab.text")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(cmbSelectFormat, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(cmbOutputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jLabel16)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdbHitFileSort)
                    .addComponent(rdbSortTab)
                    .addComponent(rdbNoSorting))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbNoSorting)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdbHitFileSort)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdbSortTab))
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cmbSelectFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addComponent(jLabel4)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cmbOutputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout printPanelLayout = new javax.swing.GroupLayout(printPanel);
        printPanel.setLayout(printPanelLayout);
        printPanelLayout.setHorizontalGroup(
            printPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(printPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(printPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(101, Short.MAX_VALUE))
        );
        printPanelLayout.setVerticalGroup(
            printPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(printPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(225, 225, 225))
        );

        scrollPanePrint.setViewportView(printPanel);

        printSortTabbedPane.addTab(org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.scrollPanePrint.TabConstraints.tabTitle"), scrollPanePrint); // NOI18N

        scrollPaneSort.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        sortPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        selectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.selectPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        buttonGroup4.add(rdbMfnRangeSort);
        rdbMfnRangeSort.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rdbMfnRangeSort.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbMfnRangeSort, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbMfnRangeSort.text")); // NOI18N
        rdbMfnRangeSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMfnRangeSortActionPerformed(evt);
            }
        });

        buttonGroup5.add(rdbAllMfnSort);
        rdbAllMfnSort.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdbAllMfnSort, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbAllMfnSort.text")); // NOI18N
        rdbAllMfnSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbAllMfnSortActionPerformed(evt);
            }
        });

        buttonGroup5.add(rdbMfnsSort);
        org.openide.awt.Mnemonics.setLocalizedText(rdbMfnsSort, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbMfnsSort.text")); // NOI18N
        rdbMfnsSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMfnsSortActionPerformed(evt);
            }
        });

        txtMfnsSort.setText(org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.txtMfnsSort.text")); // NOI18N
        txtMfnsSort.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel11.text")); // NOI18N

        buttonGroup5.add(rdbMarkedSort);
        rdbMarkedSort.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbMarkedSort, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbMarkedSort.text")); // NOI18N
        rdbMarkedSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMarkedSortActionPerformed(evt);
            }
        });

        cmbMarkedSort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        buttonGroup4.add(rdbSearchResultsSort);
        rdbSearchResultsSort.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rdbSearchResultsSort, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.rdbSearchResultsSort.text")); // NOI18N
        rdbSearchResultsSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSearchResultsSortActionPerformed(evt);
            }
        });

        cmbSearchSort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSearchSort.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel12.text")); // NOI18N

        javax.swing.GroupLayout selectPanelLayout = new javax.swing.GroupLayout(selectPanel);
        selectPanel.setLayout(selectPanelLayout);
        selectPanelLayout.setHorizontalGroup(
            selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(selectPanelLayout.createSequentialGroup()
                        .addComponent(rdbMarkedSort)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(selectPanelLayout.createSequentialGroup()
                        .addComponent(rdbSearchResultsSort)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbMarkedSort, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbSearchSort, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(286, 286, 286))
                    .addGroup(selectPanelLayout.createSequentialGroup()
                        .addComponent(rdbMfnRangeSort)
                        .addGap(11, 11, 11)
                        .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rdbMfnsSort)
                            .addComponent(rdbAllMfnSort))
                        .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(selectPanelLayout.createSequentialGroup()
                                .addGap(39, 39, 39)
                                .addComponent(jLabel11))
                            .addGroup(selectPanelLayout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(txtMfnsSort)
                                .addGap(286, 286, 286))))))
        );
        selectPanelLayout.setVerticalGroup(
            selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectPanelLayout.createSequentialGroup()
                .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(selectPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rdbMfnRangeSort)
                            .addComponent(rdbAllMfnSort))
                        .addGap(4, 4, 4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, selectPanelLayout.createSequentialGroup()
                        .addContainerGap(25, Short.MAX_VALUE)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMfnsSort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdbMfnsSort))
                .addGap(18, 18, 18)
                .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdbMarkedSort)
                    .addComponent(cmbMarkedSort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(selectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbSearchResultsSort)
                    .addComponent(jLabel12)
                    .addComponent(cmbSearchSort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        headingPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.headingPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel13.text")); // NOI18N

        spinHeaderNumber.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4, 1));
        spinHeaderNumber.setValue(4);

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel15.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblHeadingLevelIndentation, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.lblHeadingLevelIndentation.text")); // NOI18N

        spinHeadingLevelIndentation.setModel(new javax.swing.SpinnerNumberModel(3, 1, 256, 1));
        spinHeadingLevelIndentation.setValue(4);

        chkPrintWithHeadings.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkPrintWithHeadings, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.chkPrintWithHeadings.text")); // NOI18N

        txtHeadingFormat.setColumns(20);
        txtHeadingFormat.setRows(5);
        jScrollPane3.setViewportView(txtHeadingFormat);

        javax.swing.GroupLayout headingPanelLayout = new javax.swing.GroupLayout(headingPanel);
        headingPanel.setLayout(headingPanelLayout);
        headingPanelLayout.setHorizontalGroup(
            headingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headingPanelLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spinHeaderNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(lblHeadingLevelIndentation)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinHeadingLevelIndentation, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(98, 98, 98)
                        .addComponent(chkPrintWithHeadings, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 797, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        headingPanelLayout.setVerticalGroup(
            headingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblHeadingLevelIndentation)
                        .addComponent(spinHeadingLevelIndentation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(headingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel13)
                        .addComponent(spinHeaderNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel15))
                    .addComponent(chkPrintWithHeadings))
                .addGap(5, 5, 5)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addContainerGap())
        );

        sorKeysPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.sorKeysPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        tableSortKeys.setAutoCreateColumnsFromModel(false);
        jScrollPane2.setViewportView(tableSortKeys);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel17.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel18.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.jLabel19.text")); // NOI18N

        btnSort.setMnemonic('S');
        org.openide.awt.Mnemonics.setLocalizedText(btnSort, org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.btnSort.text")); // NOI18N
        btnSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSortActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sorKeysPanelLayout = new javax.swing.GroupLayout(sorKeysPanel);
        sorKeysPanel.setLayout(sorKeysPanelLayout);
        sorKeysPanelLayout.setHorizontalGroup(
            sorKeysPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sorKeysPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sorKeysPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 891, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSort)
                    .addComponent(jLabel18)
                    .addComponent(jLabel17)
                    .addComponent(jLabel19))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sorKeysPanelLayout.setVerticalGroup(
            sorKeysPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sorKeysPanelLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(btnSort)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout sortPanelLayout = new javax.swing.GroupLayout(sortPanel);
        sortPanel.setLayout(sortPanelLayout);
        sortPanelLayout.setHorizontalGroup(
            sortPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sortPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sortPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(selectPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(headingPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sorKeysPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        sortPanelLayout.setVerticalGroup(
            sortPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sortPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(headingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sorKeysPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        scrollPaneSort.setViewportView(sortPanel);

        printSortTabbedPane.addTab(org.openide.util.NbBundle.getMessage(PrintSortTopComponent.class, "PrintSortTopComponent.scrollPaneSort.TabConstraints.tabTitle"), scrollPaneSort); // NOI18N

        add(printSortTabbedPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

   private void cmbSelectFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSelectFormatActionPerformed
       // TODO add your handling code here:
   }//GEN-LAST:event_cmbSelectFormatActionPerformed

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

//   private void printAll() {
//      cancellable_ = new CancellableProgress();
//      final ProgressHandle progress = ProgressHandleFactory.createHandle("Printing Data...",
//              cancellable_);
//      progress.start();
//      progress.switchToIndeterminate();
//      long from = 0;
//      long to = 0;
//      try {
//         from = db_.getFirst().getMfn();
//         to = db_.getLast().getMfn();
//         out_ = new FileOutputStream(getOutputFile());
//         // Connect print stream to the output stream
//         p_ = new PrintStream(out_, true, "UTF-8");
//
//         for (long i = from; i <= to; i++) {
//            if (cancellable_.cancelRequested()) {
//               progress.finish();
//               break;
//            }
//            IRecord iRec = db_.getRecordCursor(i);
//            printRecord(iRec);
//            progress.setDisplayName("Printing MFN:" + Long.toString(iRec.getMfn()));
//         }
//
//      } catch (Exception e) {
//         System.err.println("Error writing to file");
//         Exceptions.printStackTrace(e);
//      } finally {
//         if (p_ != null) {
//            p_.close();
//         }
//         progress.finish();
//      }
//   }
    /**
     * iii. Heading format -------------- You may provide here your own format for printing the headings. As
     * headings are created by means of an FST, they do not necessarily correspond to actual fields in the
     * record (e.g. a heading may be a single word in a field). On the other hand, the formatting language
     * (which is also used to format headings as well as records) has no specific command to format headings;
     * it may only format fields or subfields. For this reason CDS/ISIS assigns to each heading a special tag
     * which may then be used in the format to refer to the heading. If you provide your own heading format
     * you should note that at the time of printing, CDS/ISIS will have taken the following actions before
     * executing it:
     *
     * 1.	each heading is assigned a tag equal to the field identifier specified in the FST used to build the
     * corresponding sort key (note however that when CDS/ISIS uses the default heading format it reassigns to
     * each heading a sequential number starting from 1);
     *
     * 2.	the current set of headings is then compared with the previous one and those which did not change
     * are deleted, as this normally means that they should not be printed. You may therefore use conditional
     * formatting to provide the required spacing. Note, however, that headings are not deleted, even if there
     * was no change, if you specified 2 or 3 in the Heading processing indicator of the corresponding sort
     * key.
     *
     * Assume for example that the field identifiers assigned to the first and second sort keys are 1 and 2
     * respectively, the following heading format may be used to always provide a blank line before the first
     * level heading and a blank line before the second level heading only when there is no change in the
     * first heading: MHL,""#V1(0,4)/""#N1,V2(4,8) (note the use of the dummy field N1 to produce the blank
     * line only when the first heading is missing).
     *
     * Also note that when you provide your own heading format CDS/ISIS will only use the data indention
     * parameter (supplied on the print worksheet) to offset the records printed under the last level heading.
     * It is therefore your responsibility to provide any required indentation for the headings themselves. If
     * you do not provide a heading format (i.e. you leave this field empty), CDS/ISIS will provide a default
     * system format as follows: MHL,""#V1(0,i)/""#V2(i,2i)/ . . . # where i is the value you have assigned to
     * the data indention parameter on the print worksheet, and V1, V2, etc. are the first, second, etc.
     * heading. The above default format will leave one blank line before each heading and one blank line
     * before the first record printed under the last level heading.
     *
     * @param rec
     * @return
     */
    private static String[] previousKeys = new String[]{"", "", "", ""};

    private IRecord getDummyHeadingsRecord(IRecord rec, String sCombination) {
        // Create an empty record
        assert (sCombination.length() == 12);
        /**
         * Decode combination indexes
         */
        int[] j = new int[4];
        j[0] = Integer.parseInt(sCombination.substring(0, 3));
        j[1] = sCombination.substring(3, 6).equals("   ") ? -1
            : Integer.parseInt(sCombination.substring(3, 6));
        j[2] = sCombination.substring(6, 9).equals("   ") ? -1
            : Integer.parseInt(sCombination.substring(6, 9));
        j[3] = sCombination.substring(9, 12).equals("   ") ? -1
            : Integer.parseInt(sCombination.substring(9, 12));

        List<List<Field>> luceneFieldList = extractSortKeys(rec);

        IRecord dummyRecord = Record.createRecord();
        boolean hasUserHeadingFormat = hasUserHeadingFormat();

        // Loop on the number of sort keys
        for (int k = 0; k < parsedSortKeys_.length; k++) {
            // Get the sort key value
            List<Field> luceneFields = luceneFieldList.get(k);
            int index = j[k];
            assert (index >= 0 && index < luceneFields.size());
            String s = luceneFields.get(index).stringValue();
            if (s.equals(previousKeys[k])) {
                continue;
            }
            previousKeys[k] = s;
            List<ParsedFstEntry> parsedSortFstEntries
                = parsedSortKeys_[k].parsedSortFstEntries_;

            // buid the field tag
            int tag = k + 1; // Default sequential tag numbering
            if (hasUserHeadingFormat) {
                /**
                 * We get the FST field tag from the 1st FST entry for this sort key Note that all subsequent
                 * FST entries for this sort key should have the same tag.
                 */
                tag = parsedSortFstEntries.get(0).getTag();
            }
            /**
             * Create a field with this tag
             */
            IField field = FieldFactory.makeField(tag);
            field.setType(Global.FIELD_TYPE_ALPHANUMERIC);

            try {
                field.setFieldValue(s);
            } catch (DbException ex) {
                Exceptions.printStackTrace(ex);
            }
            /**
             * For each sort key, we have a single field
             */
            dummyRecord.addField(field);
        }
        return dummyRecord;
    }

    /**
     * Extract the sort keys for this record using the FST format
     *
     * @param record
     * @return - List of List of Lucene Fields extracted for this particular record. One list for each sort
     * key
     */
    private synchronized List<List<Field>> extractSortKeys(IRecord record) {

        List<List<Field>> luceneFieldList = new ArrayList<List<Field>>();

        int sortKeyCount = parsedSortKeys_.length;
        // Loop on the number of sort keys
        for (int k = 0; k < sortKeyCount; k++) {
            /**
             * The List of FST parsed entries for this sort key . We have several entries if the FST format
             * has several lines separated by "+"
             */
            List<ParsedFstEntry> parsedSortFstEntries = parsedSortKeys_[k].parsedSortFstEntries_;

            // Loop on the FST entries for this key
            List<org.apache.lucene.document.Field> fieldList
                = new ArrayList<org.apache.lucene.document.Field>();
            for (ParsedFstEntry parsedSortFstEntry : parsedSortFstEntries) {
                /**
                 * We re-use the index parsing - extract from the record the terms produced by the FST entry.
                 * We get back Lucene fields !!
                 */
                Field[] fields = parsedSortFstEntry.extract(db_, record);
                if (fields != null && fields.length > 0) {
                    fieldList.addAll(Arrays.asList(fields));
                }
            }
            /**
             * If first sort key is absent, record will be skpped We create a dummy field for other sort keys
             */
            if (k > 0 && fieldList.isEmpty()) {
                /**
                 * No data was extracted, fields may be absent
                 */
                // Add a dummy blank field
                Field field = new org.apache.lucene.document.Field("dummy", "", new FieldType(TextField.TYPE_STORED));
                fieldList.add(field);
            }
            luceneFieldList.add(fieldList);
        }
        return luceneFieldList;
    }

    private void printSorted(File hitSortFile) {
        cancellable_ = new CancellableProgress();
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Printing Data...",
            cancellable_);
        progress.start();
        progress.switchToIndeterminate();
        BufferedReader in;
        try {
            String fullFilepath = hitSortFile.getAbsolutePath();

            in = new BufferedReader(new InputStreamReader(new FileInputStream(fullFilepath), "UTF8"));

            String filePath = getOutputFile();
            out_ = new FileOutputStream(filePath);
            // Connect print stream to the output stream
            printStream_ = new PrintStream(out_, true, "UTF-8");

            if (getOutputFormat().equals("HTML")) {
                printStream_.print(HtmlContext.getXhtmlBegin(db_.getDatabaseName()));
            }
            String headingPft;
            ISISFormatter headingFormatter = null;
            if (getPrintWithHeadings()) {
                // Yes print the headins
                IContext context = null;
                String outputFormat = getOutputFormat();
                if (outputFormat.equals("HTML")) {
                    context = new HtmlContext();
                } else {
                    context = new PlainTextContext();
                }
                /**
                 * Process Heading Format *
                 */
                headingPft = getHeadingFormat();
                headingFormatter = ISISFormatter.getFormatter(context, headingPft);
                if (headingFormatter == null) {
                    GuiGlobal.output(ISISFormatter.getParsingError());
                    GuiGlobal.output("Heading Format:" + headingPft);
                    return;
                } else if (headingFormatter.hasParsingError()) {
                    GuiGlobal.output(ISISFormatter.getParsingError());
                    GuiGlobal.output("Heading Format:" + headingPft);
                    return;
                }
                GuiGlobal.output("Heading Format:" + headingPft);
            }

            // Read the hit sort file
            String s;

            while ((s = in.readLine()) != null) {
                if (cancellable_.cancelRequested()) {
                    progress.finish();
                    break;
                }
                // The mfn is in positions 0:9
                long mfn = Long.parseLong(s.substring(0, 9));
                // The term index for the combination in the array extracted by the FST 
                String sCombination = s.substring(9, 21);

                IRecord iRec = db_.getRecordCursor(mfn);

                if (iRec == null) {
                    GuiGlobal.outputErr("Record wirh MFN " + mfn + " on Hit Sort File doesn' exist !");
                    continue;
                }

                // Print the heading if requested
                boolean printRecord = true;
                String previousHeadings = "";
                if (getPrintWithHeadings()) {
                    /**
                     * Make a dummy record with the result of the sort key fsts
                     */
                    Record dummyRecord = (Record) getDummyHeadingsRecord(iRec, sCombination);
                    // GuiGlobal.output("Heading Dummy Record:" + dummyRecord.toString());
                    IContext context = null;
                    if (getOutputFormat().equals("HTML")) {
                        context = new HtmlContext();
                    } else {
                        context = new PlainTextContext();
                    }
                    headingFormatter.setContext(context);
                    headingFormatter.setRecord(db_, dummyRecord);
                    // Extract the Headings Text
                    headingFormatter.eval();
                    String headings = headingFormatter.getText();
                    if (headings.trim().length() == 0) {
                        printRecord = false; // Don't print if headings empty!
                    } else {

                        if (!headings.equalsIgnoreCase(previousHeadings)) {
                            printStream_.print(headings);
                        }
                        previousHeadings = headings;
                    }
                }
                if (printRecord) {
                    printRecord(iRec);
                    progress.setDisplayName("Printing MFN:" + Long.toString(iRec.getMfn()));
                }
            }
            in.close();
            if (getOutputFormat().equals("HTML")) {
                printStream_.print(HtmlContext.getXhtmlEnd());
            }
        } catch (Exception e) {
            System.err.println("Error writing to file");
            Exceptions.printStackTrace(e);
        } finally {
            if (printStream_ != null) {
                printStream_.close();
                try {
                    out_.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            //in.close();

            progress.finish();
        }

    }

    private void doPrint(MfnRange[] mfnRanges) {

        if (mfnRanges == null || mfnRanges.length == 0) {
            return;
        }
        cancellable_ = new CancellableProgress();
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Printing Data...",
            cancellable_);
        progress.start();
        progress.switchToIndeterminate();
        try {

            out_ = new FileOutputStream(getOutputFile());
            // Connect print stream to the output stream
            printStream_ = new PrintStream(out_, true, "UTF-8");
            /**
             * htmlHeader is true if the PFT itself contains the HTML header tagging:
             * <!DOCTYPE HTML>
             * <html>
             * <head>
             * <title>Title of the document</title>
             * </head>
             * <body>
             *
             */
            boolean hasHtmlHeader = false;
            String pftName = getPrintFormatName();
            String pft = db_.getPrintFormat(getPrintFormatName());
            if (getOutputFormat().equals("HTML")) {

                if (pftName.equalsIgnoreCase("raw")) {
                    /**
                     * The RAW css class is defined in the string returned by getXhtmlBegin
                     */
                } else {
                    // HTML is defined by user's pft

                    if (pft.indexOf("<!DOCTYPE") >= 0 || pft.indexOf("<!doctype") >= 0) {
                        hasHtmlHeader = true;
                    }
                }
            }

            if (!hasHtmlHeader) {
                printStream_.print(HtmlContext.getXhtmlBegin(db_.getDatabaseName()));
            }

            for (int i = 0; i < mfnRanges.length; i++) {
                if (cancellable_.cancelRequested() || Thread.interrupted()) {
                    progress.finish();
                    break;
                }
                long first = mfnRanges[i].getFirst();
                long last = mfnRanges[i].getLast();
                if (last >= first) {
                    for (long j = first; j <= last; j++) {
                        if (cancellable_.cancelRequested() || Thread.interrupted()) {
                            break;
                        }
                        IRecord iRec = db_.getRecordCursor(j);
                        if (iRec == null) {
                            GuiGlobal.outputErr("Record wirh MFN " + j + " doesn' exist !");
                            continue;
                        }
                        printRecord(iRec);
                        progress.setDisplayName("Printing MFN:" + Long.toString(iRec.getMfn()));
                    }
                } else {
                    for (long j = first; j >= last; j--) {
                        if (cancellable_.cancelRequested() || Thread.interrupted()) {
                            break;
                        }
                        IRecord iRec = db_.getRecordCursor(j);
                        if (iRec == null) {
                            GuiGlobal.outputErr("Record wirh MFN " + j + " doesn' exist !");
                            continue;
                        }
                        printRecord(iRec);
                        progress.setDisplayName("Printing MFN:" + Long.toString(iRec.getMfn()));
                    }
                }
            }
            if (getOutputFormat().equals("HTML") && !hasHtmlHeader) {
                printStream_.print(HtmlContext.getXhtmlEnd());
            }

        } catch (Exception e) {
            System.err.println("Error writing to file");
            Exceptions.printStackTrace(e);
        } finally {
            if (printStream_ != null) {
                printStream_.close();
            }

            progress.finish();
        }
    }

    private MfnRange[] getPrintMfnRanges() {
        final int option = getMfnsRangeOption();
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

                String s = txtMfns.getText();
                mfnRanges = Global.parseMfns(s);
                break;
            case Global.MFNS_OPTION_MARKED:

                int markedIndex = cmbMarked.getSelectedIndex();
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
                int searchIndex = cmbSearch.getSelectedIndex();
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

    private String saveFormData() {
        // Save form data in a map
        Map<String, String> data = new HashMap<String, String>();

        data.put("txtPrintFromDB", txtPrintFromDB.getText());
        data.put("rdbAllMfn", rdbAllMfn.isSelected() ? "true" : "false");
        data.put("rdbMfnRange", rdbMfnRange.isSelected() ? "true" : "false");
        data.put("rdbMfns", rdbMfns.isSelected() ? "true" : "false");
        data.put("txtMfns", txtMfns.getText());
        data.put("rdbMarked", rdbMarked.isSelected() ? "true" : "false");
        data.put("cmbMarkedItem", (String) cmbMarked.getSelectedItem());
        data.put("rdbSearchResults", rdbSearchResults.isSelected() ? "true" : "false");
        data.put("cmbSearchItem", (String) cmbSearch.getSelectedItem());
        data.put("outputFileName", outputFileName.getText());
        data.put("cmbOutputFormatItem", (String) cmbOutputFormat.getSelectedItem());
        data.put("cmbSelectFormatItem", (String) cmbSelectFormat.getSelectedItem());
        data.put("rdbNoSorting", rdbNoSorting.isSelected() ? "true" : "false");
        data.put("rdbHitFileSort", rdbHitFileSort.isSelected() ? "true" : "false");
        data.put("rdbSortTab", rdbSortTab.isSelected() ? "true" : "false");
        data.put("rdbMfnRangeSort", rdbMfnRangeSort.isSelected() ? "true" : "false");

        data.put("rdbAllMfnSort", rdbAllMfnSort.isSelected() ? "true" : "false");

        data.put("rdbMfnsSort", rdbMfnsSort.isSelected() ? "true" : "false");
        data.put("txtMfnsSort", txtMfnsSort.getText());

        data.put("rdbMarkedSort", rdbMarkedSort.isSelected() ? "true" : "false");
        data.put("cmbMarkedSortItem", (String) cmbMarkedSort.getSelectedItem());
        data.put("rdbSearchResultsSort", rdbSearchResultsSort.isSelected() ? "true" : "false");
        data.put("cmbSearchSortItem", (String) cmbSearchSort.getSelectedItem());

        data.put("spinHeaderNumber", spinHeaderNumber.getValue().toString());
        data.put("spinHeadingLevelIndentation", spinHeadingLevelIndentation.getValue().toString());

        data.put("chkPrintWithHeadings", chkPrintWithHeadings.isSelected() ? "true" : "false");

        data.put("txtHeadingFormat", txtHeadingFormat.getText());

        //data.put("txtStopwordFile", txtStopwordFile.getText());

        List<SortKeyData> sortKeyDataList = getSortKeyData();
        for (int i = 0; i < sortKeyDataList.size(); i++) {
            SortKeyData sortKeyData = sortKeyDataList.get(i);
            data.put("checked" + i, (sortKeyData.isKeyChecked_) ? "true" : "false");
            data.put("key" + i, sortKeyData.key_);
            data.put("length" + i, sortKeyData.length_ + "");
            data.put("headingProcessorIndicator" + i, sortKeyData.headingProcessorIndicator_ + "");
            data.put("fst" + i, sortKeyData.fst_);
        }

        Type typeOfMap = new TypeToken<Map<String, String>>() {
        }.getType();
        Gson gson = new Gson();
        String json = gson.toJson(data, typeOfMap);
        System.out.println(json);
        try {
            Global.prefs_.put(db_.getDatabaseName() + "PRINT_SORT_DATA", json);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        return json;
    }

    private void restoreFormData() {

        String json = "";
        try {
            json = Global.prefs_.get(db_.getDatabaseName() + "PRINT_SORT_DATA", "");
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        System.out.println("restore\n" + json);
        if (json.equals("")) {
            return;
        }
        restoreFormData(json);
    }

    private void restoreFormData(String json) {

        if (json.equals("")) {
            return;
        }

        Gson gson = new Gson();
        Type typeOfMap = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> data = gson.fromJson(json, typeOfMap);

        try {
            Global.prefs_.put(db_.getDatabaseName() + "PRINT_SORT_DATA", json);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }

        String text = data.get("txtPrintFromDB");
        if (text != null) {
            txtPrintFromDB.setText(text);
        }
        String status = data.get("rdbAllMfn");
        rdbAllMfn.setSelected(status.equalsIgnoreCase("true") ? true : false);
        status = data.get("rdbMfnRange");
        rdbMfnRange.setSelected(status.equalsIgnoreCase("true") ? true : false);
        status = data.get("rdbMfns");
        rdbMfns.setSelected(status.equalsIgnoreCase("true") ? true : false);
        text = data.get("txtMfns");
        if (text != null) {
            txtMfns.setText(text);
        }

        status = data.get("rdbMarked");
        rdbMarked.setSelected(status.equalsIgnoreCase("true") ? true : false);
        String item = data.get("cmbMarkedItem");
        if (item != null) {
            cmbMarked.setSelectedItem(item);
        }

        status = data.get("rdbSearchResults");
        rdbSearchResults.setSelected(status.equalsIgnoreCase("true") ? true : false);
        item = data.get("cmbSearchItem");
        if (item != null) {
            cmbSearch.setSelectedItem(item);
        }

        text = data.get("outputFileName");
        if (text != null) {
            outputFileName.setText(text);
        }
        item = data.get("cmbOutputFormatItem");
        if (item != null) {
            cmbOutputFormat.setSelectedItem(item);
        }

        item = data.get("cmbSelectFormatItem");
        if (item != null) {
            cmbSelectFormat.setSelectedItem(item);
        }

        status = data.get("rdbNoSorting");
        rdbNoSorting.setSelected(status.equalsIgnoreCase("true") ? true : false);

        status = data.get("rdbHitFileSort");
        rdbHitFileSort.setSelected(status.equalsIgnoreCase("true") ? true : false);

        status = data.get("rdbSortTab");
        rdbSortTab.setSelected(status.equalsIgnoreCase("true") ? true : false);

        status = data.get("rdbMfnRangeSort");
        rdbMfnRangeSort.setSelected(status.equalsIgnoreCase("true") ? true : false);

        status = data.get("rdbAllMfnSort");
        rdbAllMfnSort.setSelected(status.equalsIgnoreCase("true") ? true : false);

        status = data.get("rdbMfnsSort");
        rdbMfnsSort.setSelected(status.equalsIgnoreCase("true") ? true : false);

        text = data.get("txtMfnsSort");
        txtMfnsSort.setText(text);

        status = data.get("rdbMarkedSort");
        rdbMarkedSort.setSelected(status.equalsIgnoreCase("true") ? true : false);

        item = data.get("cmbMarkedSortItem");
        if (item != null) {
            cmbMarkedSort.setSelectedItem(item);
        }

        status = data.get("rdbSearchResultsSort");
        rdbSearchResultsSort.setSelected(status.equalsIgnoreCase("true") ? true : false);

        data.put("cmbSearchItemSort", (String) cmbSearchSort.getSelectedItem());
        item = data.get("cmbSearchItemSort");
        if (item != null) {
            cmbSearchSort.setSelectedItem(item);
        }

        text = data.get("spinHeaderNumber");
        if (text != null) {
            spinHeaderNumber.setValue(Integer.parseInt(text));
        }

        text = data.get("spinHeadingLevelIndentation");
        if (text != null) {
            spinHeadingLevelIndentation.setValue(Integer.parseInt(text));
        }

        status = data.get("chkPrintWithHeadings");
        chkPrintWithHeadings.setSelected(status.equalsIgnoreCase("true") ? true : false);

        text = data.get("txtHeadingFormat");
        if (text != null) {
            txtHeadingFormat.setText(text);
        }


        List<SortKeyData> sortKeyDataList = new ArrayList<SortKeyData>();

        for (int i = 0; i < 4; i++) {
            status = data.get("checked" + i);
            boolean isChecked = status.equalsIgnoreCase("true") ? true : false;
            String key = data.get("key" + i);
            String length = data.get("length" + i);
            int len = (length == null) ? 15 : Integer.parseInt(length);
            String headingProcessorIndicator = data.get("headingProcessorIndicator" + i);
            int heading = (headingProcessorIndicator == null) ? 0 : Integer.parseInt(headingProcessorIndicator);
            String fst = data.get("fst" + i);
            sortKeyDataList.add(new SortKeyData(isChecked, key, len, heading, fst));
        }

        SortKeyTableModel model = (SortKeyTableModel) tableSortKeys.getModel();

        model.setData(sortKeyDataList);
    }

    private void createPdf(String fileName) {

        try {
            String pdfFileName = Util.changeFileExtension(fileName, "pdf");

            //create a new document
            Document document = new Document();

            //get Instance of the PDFWriter
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));

            //document header attributes
            document.addAuthor("J-ISIS");
            document.addCreationDate();
            document.addProducer();
            document.addCreator("J-ISIS");
            document.addTitle(db_.getDbName());
            document.setPageSize(PageSize.A4);

            //open document
            document.open();

            //To convert a HTML file from the filesystem
            //String File_To_Convert = "docs/SamplePDF.html";
            //FileInputStream fis = new FileInputStream(fileName);
            BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(fileName), "UTF8"));

           //URL for HTML page
            //URL myWebPage = new URL("http://demo.mysamplecode.com/");
            //InputStreamReader fis = new InputStreamReader(myWebPage.openStream());
            //get the XMLWorkerHelper Instance
            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
            //convert to PDF
            worker.parseXHtml(pdfWriter, document, bufferedReader);

            //close the document
            document.close();
            //close the writer
            pdfWriter.close();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (DocumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

   private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed

       if (getOutputFileName() == null || getOutputFileName().trim().equals("")) {
           NotifyDescriptor d
               = new NotifyDescriptor.Message(NbBundle.getMessage(PrintSortTopComponent.class,
                       "MSG_PLS_ENTER_OUTPUT_FILE_NAME"));

           DialogDisplayer.getDefault().notify(d);
           return;
       }

       String json = saveFormData();   // For recall
        String dbHitSortHxfFilePath = Global.getClientWorkPath() + File.separator
                                       + db_.getDbName()
                                       + Global.HIT_SORT_HXF_FILE_EXT;
       Util.string2File(json, dbHitSortHxfFilePath, "UTF-8");
       
       /**
        * Save data in history
        */
       if (!txtMfns.getText().trim().isEmpty()) {
           HistoryTextField historyTextField = (HistoryTextField) txtMfns;
           historyTextField.addCurrentToHistory();
       }
       if (!outputFileName.getText().trim().isEmpty()) {
           HistoryTextField historyTextField = (HistoryTextField) outputFileName;
           historyTextField.addCurrentToHistory();
       }
       if (!txtOutputDir.getText().trim().isEmpty()) {
           HistoryTextField historyTextField = (HistoryTextField) txtOutputDir;
           historyTextField.addCurrentToHistory();
       }
       if (!txtMfnsSort.getText().trim().isEmpty()) {
           HistoryTextField historyTextField = (HistoryTextField) txtMfnsSort;
           historyTextField.addCurrentToHistory();
       }
       if (!txtHeadingFormat.getText().trim().isEmpty()) {
           HistoryTextArea historyTextArea = (HistoryTextArea) txtHeadingFormat;
           historyTextArea.addCurrentToHistory();
       }
       final int sortOption = getPrintSortOption();
       GuiGlobal.output("Starting Printing");
       final Date start = new Date();
       try {

           Runnable printRun = new Runnable() {
               public void run() {
                   if (!EventQueue.isDispatchThread()) {
                       try {
                           MfnRange[] mfnRanges = null;

                           switch (sortOption) {
                               case PRINT_NO_SORTING:
                                   // Print without sorting
                                   mfnRanges = getPrintMfnRanges();
                                   doPrint(mfnRanges);
                                   break;
                               case PRINT_SORT_HITFILE:
                                   // Print according to hitfile
                                   if (!dbHitSortFile_.exists()) {
                                       NotifyDescriptor d
                                           = new NotifyDescriptor.Message(NbBundle.getMessage(PrintSortTopComponent.class,
                                                   "MSG_NO_HIT_SORT_FILE"));
                                       DialogDisplayer.getDefault().notify(d);
                                       return;
                                   }
                                   String dbHitSortHxfFilePath = Global.getClientWorkPath() + File.separator
                                       + db_.getDbName()
                                       + Global.HIT_SORT_HXF_FILE_EXT;
                                   String json = Util.file2String(dbHitSortHxfFilePath, "UTF-8");
                                   String outFileName = outputFileName.getText();
                                   String outputFormat = (String) cmbOutputFormat.getSelectedItem();
                                   String selectFormat = (String) cmbSelectFormat.getSelectedItem();

                                   restoreFormData(json);
                                   outputFileName.setText(outFileName);
                                   cmbOutputFormat.setSelectedItem(outputFormat);
                                   cmbSelectFormat.setSelectedItem(selectFormat);

                                   KeyInfo[] keys = getSortKeyInfo();
                                   if (keys == null) {
                                       GuiGlobal.outputErr("Sorting Interrupted - Sort keys are not defined");                                      
                                       return;
                                   }

                                   final SortDatabase sortDB = new SortDatabase(db_, keys);

                                   if (sortDB.getErrorCount() > 0) {
                                       GuiGlobal.outputErr("Sorting Interrupted due to errors in the sort keys");
                                       return;
                                   }
                                   sortDB.setMfnRange(mfnRanges);

                                   // Save the Parsed sort keys
                                   parsedSortKeys_ = sortDB.getParsedSortKey();

                                   printSorted(dbHitSortFile_);
                                   break;
                               case PRINT_SORT_TAB:
                                   // Sort and print
                                   mfnRanges = getPrintMfnRanges();
                                   doSortAndPrint(mfnRanges);
                                   break;
                           }
                       } catch (Exception ex) {
                           Exceptions.printStackTrace(ex);
                       } finally {
                           EventQueue.invokeLater(this);
                       }
                   } else {
                       // Second Invocation, we are on the event queue now
                       Date end = new Date();
                       GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to print records");
                       NotifyDescriptor d
                           = new NotifyDescriptor.Message(NbBundle.getMessage(PrintSortTopComponent.class,
                                   "MSG_PRINTING_DONE"));
                       DialogDisplayer.getDefault().notify(d);
                       // Make the pdf only if HTML output
                       if (getOutputFormat().equals("HTML")) {
                           createPdf(getOutputFile());
                       }
                   }
               }
           };
           task_ = requestProcessor_.post(printRun);
       } catch (Exception ex) {
           Exceptions.printStackTrace(ex);
       }


   }//GEN-LAST:event_btnPrintActionPerformed

   private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
       close();
   }//GEN-LAST:event_btnCancelActionPerformed

    /**
     *
     * @param mfnRanges
     */
    private void doSort(MfnRange[] mfnRanges) {

        KeyInfo[] keys = getSortKeyInfo();
        if (keys == null) {
            return;
        }

        final SortDatabase sortDB = new SortDatabase(db_, keys);

        if (sortDB.getErrorCount() > 0) {
            return;
        }
        sortDB.setMfnRange(mfnRanges);

        // Save the Parsed sort keys
        parsedSortKeys_ = sortDB.getParsedSortKey();

        final Date start = new Date();

        Runnable sortRun = new Runnable() {

            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    try {

                        sortDB.run();

                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        EventQueue.invokeLater(this);
                    }
                } else {
                    // Second Invocation, we are on the event queue now

                    Date end = new Date();
                    GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to sort file");

                    tmpHitSortFile_ = sortDB.getTempFile();
                    FileUtils.deleteQuietly(dbHitSortFile_);
                    tmpHitSortFile_.renameTo(dbHitSortFile_);

                    String json = saveFormData();
                    String dbHitSortHxfFilePath = Global.getClientWorkPath() + File.separator
                        + db_.getDbName()
                        + Global.HIT_SORT_HXF_FILE_EXT;
                    Util.string2File(json, dbHitSortHxfFilePath, "UTF-8");

                }
            }
        };
        task_ = requestProcessor_.post(sortRun);

    }

    /**
     *
     * @return
     */
    private KeyInfo[] getSortKeyInfo() {
        /**
         * Get the Sort Key Table data
         */
        List<SortKeyData> v = getSortKeyData();
        if (v.isEmpty()) {
            return null;
        }
        /**
         * Count the number Table rows checked
         */
        int n = 0;
        for (SortKeyData skd : v) {
            if (skd.isKeyChecked_) {
                n++;
            }
        }
        if (n == 0) {
            return null;
        }
        keys_ = new KeyInfo[n];

        int j = 0;
        for (SortKeyData skd : v) {
            if (!skd.isKeyChecked_) {
                continue;
            }
            int length = skd.length_;
            int headingIndicator = skd.headingProcessorIndicator_;
            String fst = skd.fst_;
            keys_[j] = new KeyInfo(j, length, headingIndicator, fst);
            j++;
        }
        return keys_;
    }

    /**
     *
     * @param mfnRanges
     */
    private void doSortAndPrint(MfnRange[] mfnRanges) {

        KeyInfo[] keys = getSortKeyInfo();
        if (keys == null) {
            return;
        }

        final SortDatabase sortDB = new SortDatabase(db_, keys);

        if (sortDB.getErrorCount() > 0) {
            return;
        }
        sortDB.setMfnRange(mfnRanges);
        // Save the Parsed sort keys
        parsedSortKeys_ = sortDB.getParsedSortKey();

        final Date start = new Date();
        Runnable sortRun = new Runnable() {

            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    try {

                        sortDB.run();

                        tmpHitSortFile_ = sortDB.getTempFile();

                        printSorted(tmpHitSortFile_);

                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        EventQueue.invokeLater(this);
                    }
                } else {
                    // Second Invocation, we are on the event queue now

                    Date end = new Date();
                    GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to sort file");
                    GuiGlobal.setHitSortFileName(tmpHitSortFile_.getName());
                    tmpHitSortFile_ = sortDB.getTempFile();
                    FileUtils.deleteQuietly(dbHitSortFile_);
                    tmpHitSortFile_.renameTo(dbHitSortFile_);
                }
            }
        };
        task_ = requestProcessor_.post(sortRun);

    }

    private void doHitSortFile() {
        MfnRange[] mfnRanges = null;
        try {
            long from = db_.getFirst().getMfn();
            long to = db_.getLast().getMfn();
            final int option = getSortMfnsRangeOption();
            List<Long> mfns;
            switch (option) {
                case Global.MFNS_OPTION_ALL:

                    mfnRanges = new MfnRange[1];
                    mfnRanges[0] = new MfnRange(from, to);
                    break;
                case Global.MFNS_OPTION_RANGE:
                    String s = txtMfnsSort.getText();
                    mfnRanges = Global.parseMfns(s);
                    break;
                case Global.MFNS_OPTION_MARKED:
                    int markedIndex = cmbMarked.getSelectedIndex();
                    if (markedIndex < 0) {
                        break;
                    }
                    List<MarkedRecords> markedRecordsList = db_.getMarkedRecordsList();
                    MarkedRecords markedRecords = markedRecordsList.get(markedIndex);
                    mfns = markedRecords.getMfns();
                    mfnRanges = new MfnRange[mfns.size()];
                    for (int i = 0; i < mfns.size(); i++) {
                        long mfn = mfns.get(i);
                        mfnRanges[i] = new MfnRange(mfn, mfn);
                    }
                    break;
                case Global.MFNS_OPTION_SEARCH:
                    int searchIndex = cmbSearch.getSelectedIndex();
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
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        doSort(mfnRanges);
    }
   private void btnSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSortActionPerformed
       doHitSortFile();
       if (!txtMfnsSort.getText().trim().isEmpty()) {
           HistoryTextField historyTextField = (HistoryTextField) txtMfnsSort;
           historyTextField.addCurrentToHistory();
       }
       if (!txtHeadingFormat.getText().trim().isEmpty()) {
           HistoryTextArea historyTextArea = (HistoryTextArea) txtHeadingFormat;
           historyTextArea.addCurrentToHistory();
       }

}//GEN-LAST:event_btnSortActionPerformed

    /**
     * Mfn radio button on PRINT Panel
     */
   private void rdbMfnsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMfnsActionPerformed
       txtMfns.setEnabled(true);
       cmbSearch.setEnabled(false);
   }//GEN-LAST:event_rdbMfnsActionPerformed

    /**
     * All radio button on PRINT PANEL
     */
   private void rdbAllMfnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbAllMfnActionPerformed
       txtMfns.setEnabled(false);
       cmbSearch.setEnabled(false);

   }//GEN-LAST:event_rdbAllMfnActionPerformed

    /**
     * All Mfns on SORT Panel
     */
   private void rdbAllMfnSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbAllMfnSortActionPerformed
       txtMfnsSort.setEnabled(false);
       cmbSearchSort.setEnabled(false);
}//GEN-LAST:event_rdbAllMfnSortActionPerformed
    /**
     * Mfn Range on SORT panel
     */
   private void rdbMfnsSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMfnsSortActionPerformed
       txtMfnsSort.setEnabled(true);
       cmbSearchSort.setEnabled(false);
}//GEN-LAST:event_rdbMfnsSortActionPerformed

   private void rdbSearchResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbSearchResultsActionPerformed

       String firstItem = (String) cmbSearch.getItemAt(0);
       if (firstItem.equalsIgnoreCase("no search")) {
           return;
       }
       txtMfns.setEnabled(false);
       rdbMfns.setEnabled(false);
       rdbAllMfn.setEnabled(false);
       rdbMarked.setEnabled(false);
       cmbSearch.setEnabled(true);
   }//GEN-LAST:event_rdbSearchResultsActionPerformed

    /**
     * Search Results radio button on SORT Panel
     */
   private void rdbSearchResultsSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbSearchResultsSortActionPerformed
       String firstItem = (String) cmbSearchSort.getItemAt(0);
       if (firstItem.equalsIgnoreCase("no search")) {
           return;
       }
       txtMfnsSort.setEnabled(false);
       rdbMfnsSort.setEnabled(false);
       rdbAllMfnSort.setEnabled(false);
       rdbMarkedSort.setEnabled(false);
       cmbSearchSort.setEnabled(true);
   }//GEN-LAST:event_rdbSearchResultsSortActionPerformed

   private void rdbMfnRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMfnRangeActionPerformed
       rdbMfnRange.setEnabled(true);
       txtMfns.setEnabled(false);
       rdbMfns.setEnabled(true);
       rdbAllMfn.setEnabled(true);
       rdbMarked.setEnabled(true);
       rdbAllMfn.setSelected(true);

       cmbSearch.setEnabled(false);

       rdbSearchResults.setSelected(false);
   }//GEN-LAST:event_rdbMfnRangeActionPerformed

   private void rdbMfnRangeSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMfnRangeSortActionPerformed

       rdbMfnRangeSort.setEnabled(true);
       txtMfnsSort.setEnabled(false);
       rdbMfnsSort.setEnabled(true);
       rdbAllMfnSort.setEnabled(true);
       rdbMarkedSort.setEnabled(true);
       rdbAllMfnSort.setSelected(true);

       cmbSearchSort.setEnabled(false);

       rdbSearchResultsSort.setSelected(false);
   }//GEN-LAST:event_rdbMfnRangeSortActionPerformed

   private void rdbMarkedSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMarkedSortActionPerformed
       txtMfnsSort.setEnabled(false);
       cmbSearchSort.setEnabled(false);
   }//GEN-LAST:event_rdbMarkedSortActionPerformed

   private void rdbMarkedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMarkedActionPerformed
       txtMfns.setEnabled(false);
       cmbSearch.setEnabled(false);// TODO add your handling code here:
   }//GEN-LAST:event_rdbMarkedActionPerformed

   private void cmbOutputFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbOutputFormatActionPerformed
       // TODO add your handling code here:
   }//GEN-LAST:event_cmbOutputFormatActionPerformed

   private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed

       selectDirectory();
   }//GEN-LAST:event_btnBrowseActionPerformed

   private void btnLastSettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastSettingActionPerformed
       restoreFormData();
   }//GEN-LAST:event_btnLastSettingActionPerformed

    private String selectDirectory() {
        //prefs = Preferences.userNodeForPackage(this.getClass());
        String lastDir = Global.getClientWorkPath();
        DirectoryChooser dc = new DirectoryChooser(new File(lastDir));
        dc.showOpenDialog(this);
        File file;
        if ((file = dc.getSelectedFile()) != null) {
            txtOutputDir.setText(file.getAbsolutePath());
            return file.getAbsolutePath();
        }
        return lastDir;
    }

    /**
     *
     * @param rec
     */
    private void printRecord(IRecord rec) {
        org.unesco.jisis.corelib.record.Record record
            = (Record) rec;
        if (record != null) {
            try {
                if (getPrintFormatName().equals("RAW")) {
                    //dataViewer.setText(rec.toHtml());
                    printStream_.print(record.toHtmlBase64());

                } else {

                    IContext context = null;
                    String outputFormat = getOutputFormat();
                    if (outputFormat.equals("HTML")) {
                        context = new HtmlContext();
                    } else {
                        context = new PlainTextContext();
                    }
               //**************************************

               // Parsing should be done only once !!!
                    //
                    // Code need to be revised
                    //**************************************
                    String pft = db_.getPrintFormat(getPrintFormatName());

                    ISISFormatter formatter = ISISFormatter.getFormatter(context, pft);
                    if (formatter == null) {
                        GuiGlobal.output(ISISFormatter.getParsingError());
                        return;
                    } else if (formatter.hasParsingError()) {
                        GuiGlobal.output(ISISFormatter.getParsingError());
                        return;
                    }

                    formatter.setRecord(db_, record);
                    formatter.eval();

                    String s = formatter.getText();

                    printStream_.print(s);

                }

            } catch (DbException dbe) {
                Exceptions.printStackTrace(dbe);

            } catch (RuntimeException re) {
                new FormattingException(re.getMessage()).displayWarning();
            }

        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnLastSetting;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnSort;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.JCheckBox chkPrintWithHeadings;
    private javax.swing.JComboBox cmbMarked;
    private javax.swing.JComboBox cmbMarkedSort;
    private javax.swing.JComboBox cmbOutputFormat;
    private javax.swing.JComboBox cmbSearch;
    private javax.swing.JComboBox cmbSearchSort;
    private javax.swing.JComboBox cmbSelectFormat;
    private javax.swing.JPanel headingPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblHeadingLevelIndentation;
    private javax.swing.JLabel lblOutputDirectory;
    private javax.swing.JTextField outputFileName;
    private javax.swing.JPanel printPanel;
    private javax.swing.JTabbedPane printSortTabbedPane;
    private javax.swing.JRadioButton rdbAllMfn;
    private javax.swing.JRadioButton rdbAllMfnSort;
    private javax.swing.JRadioButton rdbHitFileSort;
    private javax.swing.JRadioButton rdbMarked;
    private javax.swing.JRadioButton rdbMarkedSort;
    private javax.swing.JRadioButton rdbMfnRange;
    private javax.swing.JRadioButton rdbMfnRangeSort;
    private javax.swing.JRadioButton rdbMfns;
    private javax.swing.JRadioButton rdbMfnsSort;
    private javax.swing.JRadioButton rdbNoSorting;
    private javax.swing.JRadioButton rdbSearchResults;
    private javax.swing.JRadioButton rdbSearchResultsSort;
    private javax.swing.JRadioButton rdbSortTab;
    private javax.swing.JScrollPane scrollPanePrint;
    private javax.swing.JScrollPane scrollPaneSort;
    private javax.swing.JPanel selectPanel;
    private javax.swing.JPanel sorKeysPanel;
    private javax.swing.JPanel sortPanel;
    private javax.swing.JSpinner spinHeaderNumber;
    private javax.swing.JSpinner spinHeadingLevelIndentation;
    private javax.swing.JTable tableSortKeys;
    private javax.swing.JTextArea txtHeadingFormat;
    private javax.swing.JTextField txtMfns;
    private javax.swing.JTextField txtMfnsSort;
    private javax.swing.JTextField txtOutputDir;
    private javax.swing.JTextField txtPrintFromDB;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only, i.e. deserialization
     * routines; otherwise you could get a non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized PrintSortTopComponent getDefault() {

        if (instance != null) {
            instance.close();
            instance = null;
        }
        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
        try {
            if (connectionInfo.getDefaultDatabase() != null && instance == null) {
                instance = new PrintSortTopComponent(connectionInfo.getDefaultDatabase());
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return instance;
    }

    /**
     * Obtain the PrintSortTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized PrintSortTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(PrintSortTopComponent.class.getName()).warning(
                "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof PrintSortTopComponent) {
            return (PrintSortTopComponent) win;
        }
        Logger.getLogger(PrintSortTopComponent.class.getName()).warning(
            "There seem to be multiple components with the '" + PREFERRED_ID
            + "' ID. That is a potential source of errors and unexpected behavior.");
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
    }

    /**
     * replaces this in object stream
     */
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
            return PrintSortTopComponent.getDefault();
        }
    }

    public String getOutputFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(outputDirectory);
        if (!outputDirectory.endsWith(File.separator)) {
            sb.append(File.separator);
        }
        sb.append(getOutputFileName());
        return sb.toString();
    }

    public String getOutputFileName() {
        String fn = outputFileName.getText();
        if (fn.trim().equals("")) {
            return fn;
        }
        if (getOutputFormat().equals("HTML") && !fn.toLowerCase().endsWith(".html")) {
            fn += ".html";
            outputFileName.setText(fn);
            updateUI();
        }
        if (getOutputFormat().equals("Plain Text") && !fn.toLowerCase().endsWith(".txt")) {
            fn += ".txt";
            outputFileName.setText(fn);
            updateUI();
        }
        return fn;
    }

    private String selectFile(String[] ext, String[] description) {

        //prefs = Preferences.userNodeForPackage(this.getClass());
        String lastDir = Global.prefs_.get("OUTPUT_REPORT_DIR", "");
        JFileChooser fc = new JFileChooser(lastDir);

      // Remove the filter that accepts all files
//      FileFilter ft = fc.getAcceptAllFileFilter();
//      fc.removeChoosableFileFilter(ft);
        for (int i = 0; i < ext.length; i++) {
            // Add the file extension filter
            FileExtFilter filter = new FileExtFilter(ext[i], description[i]);
            fc.addChoosableFileFilter(filter);
        }

        fc.showOpenDialog(this);
        File file;
        if ((file = fc.getSelectedFile()) != null) {
            Global.prefs_.put("OUTPUT_REPORT_DIR", file.getAbsolutePath());
            String fn = file.getAbsolutePath();
            if (getOutputFormat().equals("HTML") && !fn.toLowerCase().endsWith(".html")) {
                fn += ".html";
            }
            if (getOutputFormat().equals("Plain Text") && !fn.toLowerCase().endsWith(".txt")) {
                fn += ".txt";
            }
            return fn;
        }
        return "";
    }

    public String getPrintFormatName() {
        String pft = (String) cmbSelectFormat.getSelectedItem();
        return pft;
    }

    public String getOutputFormat() {
        String fmt = (String) cmbOutputFormat.getSelectedItem();
        return fmt;
    }

    public int getPrintSortOption() {
        if (rdbNoSorting.isSelected()) {
            return PRINT_NO_SORTING;
        } else if (rdbHitFileSort.isSelected()) {
            return PRINT_SORT_HITFILE;
        } else {
            return PRINT_SORT_TAB;
        }

    }

    public int getMfnsRangeOption() {
        if (rdbMfnRange.isSelected()) {
            if (rdbAllMfn.isSelected()) {
                return Global.MFNS_OPTION_ALL;
            } else if (rdbMfns.isSelected()) {
                return Global.MFNS_OPTION_RANGE;
            } else if (rdbMarked.isSelected()) {
                return Global.MFNS_OPTION_MARKED;
            }
        }
        // Should be Search range
        if (!rdbSearchResults.isSelected()) {
            // Should not happen !
            throw new RuntimeException("PrintSortTopComponent inconsistent state in radio buttons!");
        }
        return Global.MFNS_OPTION_SEARCH;
    }

    public int getSortMfnsRangeOption() {
        if (rdbMfnRangeSort.isSelected()) {
            if (rdbAllMfnSort.isSelected()) {
                return Global.MFNS_OPTION_ALL;
            } else if (rdbMfnsSort.isSelected()) {
                return Global.MFNS_OPTION_RANGE;
            } else if (rdbMarkedSort.isSelected()) {
                return Global.MFNS_OPTION_MARKED;
            }
        }
        // Should be Search range
        if (!rdbSearchResultsSort.isSelected()) {
            // Should not happen !
            throw new RuntimeException("PrintSortTopComponent inconsistent state in radio buttons!");
        }
        return Global.MFNS_OPTION_SEARCH;

    }

    /**
     * This number must be at most equal to the number of sort keys specified. If this is not the case it
     * should be set to the number of sort keys provided.
     *
     * @return
     */
    public int getNumberOfHeadings() {
        int n = (Integer) spinHeaderNumber.getValue();
        return n;
    }

    /**
     * Build the default heading format
     *
     * If you do not provide a heading format (i.e. you leave this field empty), CDS/ISIS will provide a
     * default system format as follows: MHL,""#V1(0,i)/""#V2(i,2i)/ . . . # where i is the value you have
     * assigned to the data indention parameter, and V1, V2, etc. are the first, second, etc. heading. The
     * above default format will leave one blank line before each heading and one blank line before the first
     * record printed under the last level heading.
     *
     * @return
     */
    private String defaultHeadingFormat() {
        StringBuilder sb = new StringBuilder();
        int indentation = getHeadingLevelIndentation();
        sb.append("MHL");
        int first = 0;
        for (int i = 0; i < getNumberOfHeadings(); i++) {
            int tag = i + 1;
            sb.append(",\"\"#V").append(Integer.toString(tag))
                .append("(").append(Integer.toString(i * indentation))
                .append(",").append(Integer.toString(tag * indentation))
                .append(")/");
        }
        sb.append("#");
        return sb.toString();
    }

    private boolean hasUserHeadingFormat() {
        String headingFormat = txtHeadingFormat.getText();
        if (headingFormat.equals("")) {
            return false;
        }
        return true;
    }

    public String getHeadingFormat() {
        String headingFormat = txtHeadingFormat.getText();
        if (headingFormat.equals("")) {
            headingFormat = defaultHeadingFormat();
        }
        return headingFormat;
    }

    public int getHeadingLevelIndentation() {
        int n = (Integer) spinHeadingLevelIndentation.getValue();
        return n;
    }

    public boolean getPrintWithHeadings() {
        return chkPrintWithHeadings.isSelected();
    }

    /**
     *
     * @return a vector of SortKeyData
     */
    public List getSortKeyData() {
        SortKeyTableModel model = (SortKeyTableModel) tableSortKeys.getModel();
        return model.getData();
    }

    /**
     * We are observer for the changes in the DB that can change the form Update the PFT combo if any change
     * on PFTs Update the Search History Combos if any new search
     *
     * @param arg0
     * @param arg1
     */
    public void update(Observable arg0, Object arg1) {

        if (db_.pftHasChanged()) {
            String pftName = (String) cmbSelectFormat.getSelectedItem();
            String[] pftNames = null;
            try {
                pftNames = db_.getPrintFormatNames();
            } catch (DbException ex) {
                Exceptions.printStackTrace(ex);
            }
            cmbSelectFormat.setModel(new DefaultComboBoxModel(pftNames));

            cmbSelectFormat.setSelectedItem(pftName);
        }
        if (db_.searchHistoryHasChanged()) {
            prepareSearchHistory();
        }
        if (db_.markedRecordsHasChanged()) {
            prepareMarkedRecordsHistory();
        }
    }
}

class SortKeyData {

    boolean isKeyChecked_;
    String key_;
    int length_;
    int headingProcessorIndicator_;
    String fst_;
    public static final int COL_CHECK = 0;
    public static final int COL_KEY = 1;
    public static final int COL_LENGTH = 2;
    public static final int COL_INDICATOR = 3;
    public static final int COL_FST = 4;

    public SortKeyData(String key, int length, int headingProcessorIndicator, String fst) {
        isKeyChecked_ = false;
        key_ = key;
        length_ = length;
        headingProcessorIndicator_ = headingProcessorIndicator;
        fst_ = fst;

    }

    public SortKeyData(boolean checkStatus, String key, int length,
        int headingProcessorIndicator, String fst) {
        isKeyChecked_ = checkStatus;
        key_ = key;
        length_ = length;
        headingProcessorIndicator_ = headingProcessorIndicator;
        fst_ = fst;

    }
}

class SortKeyTableModel extends AbstractTableModel {

    static final public ColumnData columns_[] = {
        new ColumnData("", 100, JLabel.LEFT),
        new ColumnData("Sorting Key", 100, JLabel.LEFT),
        new ColumnData("Length", 100, JLabel.RIGHT),
        new ColumnData("Heading Processor Indicator", 100, JLabel.RIGHT),
        new ColumnData("FST", 500, JLabel.LEFT)
    };
    protected List<SortKeyData> vdata_;

    public SortKeyTableModel() {
        vdata_ = new ArrayList<SortKeyData>();
        setDefaultData();
    }

    public SortKeyTableModel(List<SortKeyData> vdata) {
        vdata_ = vdata;

    }

    public void setDefaultData() {
        vdata_.clear();
        vdata_.add(new SortKeyData("First", 15, 0, ""));
        vdata_.add(new SortKeyData("Second", 15, 0, ""));
        vdata_.add(new SortKeyData("Third", 15, 0, ""));
        vdata_.add(new SortKeyData("Fourth", 15, 0, ""));

    }

    public List<SortKeyData> getData() {
        return vdata_;
    }

    public int getRowCount() {
        return (vdata_ == null) ? 0 : vdata_.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns_[column].title_;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == SortKeyData.COL_KEY) {
            return false;
        }
        return true;
    }

    public int getColumnCount() {
        return columns_.length;
    }

    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= getRowCount()) {
            return "";
        }
        SortKeyData kdata = vdata_.get(row);
        switch (col) {
            case SortKeyData.COL_CHECK:
                return kdata.isKeyChecked_;
            case SortKeyData.COL_KEY:
                return kdata.key_;
            case SortKeyData.COL_LENGTH:
                return kdata.length_;
            case SortKeyData.COL_INDICATOR:
                return kdata.headingProcessorIndicator_;
            case SortKeyData.COL_FST:
                return kdata.fst_;
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (row < 0 || row >= getRowCount() || value == null) {
            return;
        }
        SortKeyData kdata = vdata_.get(row);
        switch (col) {
            case SortKeyData.COL_CHECK:
                kdata.isKeyChecked_ = (Boolean) value;
                return;
            case SortKeyData.COL_KEY:
                return;
            case SortKeyData.COL_LENGTH:
                kdata.length_ = (Integer) value;
                break;
            case SortKeyData.COL_INDICATOR:
                kdata.headingProcessorIndicator_ = (Integer) value;
                break;
            case SortKeyData.COL_FST:
                kdata.fst_ = (String) value;
                break;
        }
        return;
    }

    void setData(List<SortKeyData> sortKeyDataList) {
        vdata_ = sortKeyDataList;
    }

}
