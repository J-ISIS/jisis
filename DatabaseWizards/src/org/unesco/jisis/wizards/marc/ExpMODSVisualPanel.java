/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * ExpMODSVisualPanel.java
 *
 * Created on Jan 23, 2009, 12:10:04 PM
 */
package org.unesco.jisis.wizards.marc;

import java.awt.Dimension;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;

import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.DirectoryChooser;
import org.unesco.jisis.gui.LargeComboBoxRenderer;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;
import org.unesco.jisis.jisisutils.proxy.MarkedRecords;
import org.unesco.jisis.jisisutils.proxy.SearchResult;

/**
 *
 * @author jc_dauphin
 */
public class ExpMODSVisualPanel extends javax.swing.JPanel {

    private ClientDatabaseProxy db_;

    /**
     * Creates new form ExpMODSVisualPanel
     */
    public ExpMODSVisualPanel() {
        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
        IDatabase db = connectionInfo.getDefaultDatabase();

        if (db instanceof ClientDatabaseProxy) {
            db_ = (ClientDatabaseProxy) db;
        } else {
            throw new RuntimeException("ExpMODSVisualPanel: Cannot cast DB to ClientDatabaseProxy");
        }
        initComponents();

        String[] fstNames = db_.getFstNames();
        String[] cmbModel;
        if (fstNames == null) {
            cmbModel = new String[]{"<none>"};
        } else {
            cmbModel = new String[fstNames.length + 1];
            cmbModel[0] = "<none>";
            System.arraycopy(fstNames, 0, cmbModel, 1, fstNames.length);
        }
        cmbReformattingFST.setModel(new DefaultComboBoxModel(cmbModel));

        String lastDir = Global.getClientWorkPath();
        Global.prefs_.put("IMPEXP_OUTPUT_DIR", lastDir);
        txtOutputDir.setText(lastDir);

        cmbSearch.setEnabled(false);
        cmbMarked.setEnabled(false);
        rdbAllMfn.setEnabled(true);
        rdbMfns.setEnabled(true);
        rdbMarked.setEnabled(true);
        rdbHitSort.setEnabled(true);
        prepareSearchHistory();
        prepareMarkedRecordsHistory();
        prepareHitSortHistory();
        cmbMarked.setEnabled(false);
        cmbSearch.setEnabled(false);
    }

    private void prepareSearchHistory() {
        List<SearchResult> searchResults = db_.getSearchResults();
        String[] searches = {"No Search"};
        if (searchResults != null && searchResults.size() > 0) {

            int n = searchResults.size();
            searches = new String[n];
            for (int i = 0; i < n; i++) {
                searches[i] = searchResults.get(i).toString();
            }
        } else {
            // Disable Search radio button and combo box
            cmbSearch.setEnabled(false);
            rdbSearchResult.setEnabled(false);
        }
        cmbSearch.setModel(new DefaultComboBoxModel(searches));
        /**
         * Make Combo text display short, and tool tip for full text
         */
        cmbSearch.setPrototypeDisplayValue("Short");
        cmbSearch.setRenderer(new LargeComboBoxRenderer(500));

        cmbSearch.setPreferredSize(new Dimension(500, 30));
        cmbSearch.setMaximumSize(new Dimension(500, 30));

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
        } else {
            // Disable Search radio button and combo box
            cmbMarked.setEnabled(false);
            rdbMarked.setEnabled(false);
        }
        cmbMarked.setModel(new DefaultComboBoxModel(markedSets));
        cmbMarked.setPrototypeDisplayValue("Short");

        cmbMarked.setRenderer(new LargeComboBoxRenderer(500));

        cmbMarked.setPreferredSize(new Dimension(500, 30));
        cmbMarked.setMaximumSize(new Dimension(500, 30));

    }

    private void prepareHitSortHistory() {

        String dbHitSortFilePath = Global.getClientWorkPath() + File.separator
                + db_.getDbName()
                + Global.HIT_SORT_FILE_EXT;
        File dbHitSortFile_ = new File(dbHitSortFilePath);

        String dbHitSortHxfFilePath = Global.getClientWorkPath() + File.separator
                + db_.getDbName()
                + Global.HIT_SORT_HXF_FILE_EXT;
        File dbHitSortHxfFile_ = new File(dbHitSortHxfFilePath);

        String[] hitSortNames = new String[1];
        if (!dbHitSortFile_.exists()) {
            hitSortNames[0] = "No HitSorts";
            // Disable Hit File radio button and combo box
            cmbHitSortFile.setEnabled(false);
            rdbHitSort.setEnabled(false);

        } else {
            hitSortNames[0] = dbHitSortFilePath;
        }

        cmbHitSortFile.setModel(new DefaultComboBoxModel(hitSortNames));

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ExpMODSVisualPanel.class, "MSG_ExpMODSVisualPanel");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        expFileName = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        cmbReformattingFST = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        NumberFormat nf = NumberFormat.getIntegerInstance();
        txtRenumberFromMFN = new javax.swing.JFormattedTextField(nf);
        jLabel7 = new javax.swing.JLabel();
        txtOutputTagMFN = new javax.swing.JFormattedTextField(nf);
        cmbEncoding = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        rdbMfnRange = new javax.swing.JRadioButton();
        rdbAllMfn = new javax.swing.JRadioButton();
        rdbMarked = new javax.swing.JRadioButton();
        rdbMfns = new javax.swing.JRadioButton();
        txtMfns = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        rdbSearchResult = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        cmbSearch = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        cmbHitSortFile = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        txtOutputDir = new javax.swing.JTextField();
        cmbMarked = new javax.swing.JComboBox();
        rdbHitSort = new javax.swing.JRadioButton();

        jPanel1.setPreferredSize(new java.awt.Dimension(725, 570));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel2.text")); // NOI18N

        btnBrowse.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.btnBrowse.text")); // NOI18N
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel5.text")); // NOI18N

        cmbReformattingFST.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbReformattingFST.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbReformattingFSTActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel6.text")); // NOI18N

        txtRenumberFromMFN.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel7.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel7.text")); // NOI18N

        txtOutputTagMFN.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        cmbEncoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "US-ASCII", "CP850 (or IBM850)", "ISO-8859-1", "UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "CP1256 (Arabic Windows-1256)", "MARC-8", "ISO-5426 (Used by UNIMARC)", "ISO-6937 (Used by UNIMARC)" }));
        cmbEncoding.setSelectedIndex(3);
        cmbEncoding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbEncodingActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel8.text")); // NOI18N

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel13.text")); // NOI18N

        buttonGroup1.add(rdbMfnRange);
        rdbMfnRange.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rdbMfnRange.setSelected(true);
        rdbMfnRange.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.rdbMfnRange.text")); // NOI18N
        rdbMfnRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMfnRangeActionPerformed(evt);
            }
        });

        buttonGroup2.add(rdbAllMfn);
        rdbAllMfn.setSelected(true);
        rdbAllMfn.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.rdbAllMfn.text")); // NOI18N
        rdbAllMfn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbAllMfnActionPerformed(evt);
            }
        });

        buttonGroup2.add(rdbMarked);
        rdbMarked.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.rdbMarked.text")); // NOI18N
        rdbMarked.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMarkedActionPerformed(evt);
            }
        });

        buttonGroup2.add(rdbMfns);
        rdbMfns.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.rdbMfns.text")); // NOI18N
        rdbMfns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbMfnsActionPerformed(evt);
            }
        });

        txtMfns.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.txtMfns.text")); // NOI18N
        txtMfns.setEnabled(false);

        jLabel14.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel14.text")); // NOI18N

        buttonGroup1.add(rdbSearchResult);
        rdbSearchResult.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        rdbSearchResult.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.rdbSearchResult.text")); // NOI18N
        rdbSearchResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSearchResultActionPerformed(evt);
            }
        });

        jLabel15.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel15.text")); // NOI18N

        cmbSearch.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmbHitSortFile.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.jLabel1.text")); // NOI18N

        txtOutputDir.setEditable(false);
        txtOutputDir.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.txtOutputDir.text")); // NOI18N

        cmbMarked.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        buttonGroup1.add(rdbHitSort);
        rdbHitSort.setText(org.openide.util.NbBundle.getMessage(ExpMODSVisualPanel.class, "ExpMODSVisualPanel.rdbHitSort.text")); // NOI18N
        rdbHitSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbHitSortActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(rdbSearchResult)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel15))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtRenumberFromMFN)
                            .addComponent(txtOutputTagMFN)
                            .addComponent(cmbReformattingFST, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(96, 96, 96)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(rdbMfnRange, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGap(26, 26, 26)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(txtOutputDir, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnBrowse))
                                .addComponent(expFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(119, 119, 119)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(rdbAllMfn)
                                            .addGap(38, 38, 38)
                                            .addComponent(rdbMfns))
                                        .addComponent(rdbMarked)))
                                .addComponent(rdbHitSort))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtMfns)
                                .addComponent(cmbMarked, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbSearch, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbHitSortFile, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(expFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtOutputDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowse))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel14)
                        .addGap(1, 1, 1)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbMfnRange)
                    .addComponent(rdbAllMfn)
                    .addComponent(rdbMfns)
                    .addComponent(txtMfns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbMarked)
                    .addComponent(cmbMarked, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbSearchResult)
                    .addComponent(jLabel15)
                    .addComponent(cmbSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbHitSortFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdbHitSort))
                .addGap(29, 29, 29)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cmbReformattingFST, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(cmbEncoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtRenumberFromMFN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(txtOutputTagMFN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(174, 174, 174))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 749, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 494, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(15, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        selectDirectory();
}//GEN-LAST:event_btnBrowseActionPerformed

    private void cmbReformattingFSTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbReformattingFSTActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_cmbReformattingFSTActionPerformed

    private void cmbEncodingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbEncodingActionPerformed
        int index = cmbEncoding.getSelectedIndex();

}//GEN-LAST:event_cmbEncodingActionPerformed

    private void rdbAllMfnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbAllMfnActionPerformed
        cmbMarked.setEnabled(false);
        txtMfns.setEnabled(false);
}//GEN-LAST:event_rdbAllMfnActionPerformed

    private void rdbMfnsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMfnsActionPerformed
        cmbMarked.setEnabled(false);
        txtMfns.setEnabled(true);
}//GEN-LAST:event_rdbMfnsActionPerformed

    private void rdbMarkedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMarkedActionPerformed
        cmbMarked.setEnabled(true);
        cmbSearch.setEnabled(false);
        cmbHitSortFile.setEnabled(false);

        txtMfns.setEnabled(false);
    }//GEN-LAST:event_rdbMarkedActionPerformed

    private void rdbMfnRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbMfnRangeActionPerformed
        // Select default All and deselect ranges and marked
        rdbAllMfn.setEnabled(true);
        rdbMfns.setEnabled(true);
        if (!cmbMarked.getItemAt(0).equals("No Marked Sets")) {
            rdbMarked.setEnabled(true);
        }
        rdbAllMfn.setSelected(true);
        rdbMfns.setSelected(false);
        rdbMarked.setSelected(false);
        txtMfns.setEnabled(false);

        cmbSearch.setEnabled(false);
        cmbHitSortFile.setEnabled(false);
    }//GEN-LAST:event_rdbMfnRangeActionPerformed

    private void rdbSearchResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbSearchResultActionPerformed
        cmbSearch.setEnabled(true);
        cmbMarked.setEnabled(false);
        cmbHitSortFile.setEnabled(false);

        // Deselect All/Mfns/Marked
        rdbAllMfn.setSelected(false);
        rdbMfns.setSelected(false);
        rdbMarked.setSelected(false);
        txtMfns.setEnabled(false);
        rdbAllMfn.setEnabled(false);
        rdbMfns.setEnabled(false);
        rdbMarked.setEnabled(false);
    }//GEN-LAST:event_rdbSearchResultActionPerformed

    private void rdbHitSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbHitSortActionPerformed
        cmbHitSortFile.setEnabled(true);
        cmbSearch.setEnabled(false);
        cmbMarked.setEnabled(false);

        // Deselect All/Mfns/Marked
        rdbAllMfn.setSelected(false);
        rdbMfns.setSelected(false);
        rdbMarked.setSelected(false);
        txtMfns.setEnabled(false);
    }//GEN-LAST:event_rdbHitSortActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JComboBox cmbEncoding;
    private javax.swing.JComboBox cmbHitSortFile;
    private javax.swing.JComboBox cmbMarked;
    private javax.swing.JComboBox cmbReformattingFST;
    private javax.swing.JComboBox cmbSearch;
    private javax.swing.JTextField expFileName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JRadioButton rdbAllMfn;
    private javax.swing.JRadioButton rdbHitSort;
    private javax.swing.JRadioButton rdbMarked;
    private javax.swing.JRadioButton rdbMfnRange;
    private javax.swing.JRadioButton rdbMfns;
    private javax.swing.JRadioButton rdbSearchResult;
    private javax.swing.JTextField txtMfns;
    private javax.swing.JTextField txtOutputDir;
    private javax.swing.JFormattedTextField txtOutputTagMFN;
    private javax.swing.JFormattedTextField txtRenumberFromMFN;
    // End of variables declaration//GEN-END:variables

    public String getSelectedFile() {
        String fileName = expFileName.getText();
        if (!fileName.toLowerCase().endsWith(".mods")) {
            fileName += ".mods";
        }
        String fullFileName = Global.prefs_.get("IMPEXP_OUTPUT_DIR", "") + File.separator
                + fileName;

        return fullFileName;
    }

    private String selectDirectory() {
        //prefs = Preferences.userNodeForPackage(this.getClass());
        String lastDir = Global.getClientWorkPath();
        DirectoryChooser dc = new DirectoryChooser(new File(lastDir));
        dc.showOpenDialog(this);
        File file;
        if ((file = dc.getSelectedFile()) != null) {
            Global.prefs_.put("IMPEXP_OUTPUT_DIR", file.getAbsolutePath());
            txtOutputDir.setText(file.getAbsolutePath());
            return file.getAbsolutePath();
        }
        return "";
    }

    public String getReformattingFST() {
        int index = cmbReformattingFST.getSelectedIndex();
        return (String) ((index == -1) ? "<none>" : cmbReformattingFST.getSelectedItem());
    }

    public String getEncoding() {
        int index = cmbEncoding.getSelectedIndex();
        return (String) ((index == -1) ? "" : cmbEncoding.getSelectedItem());
    }

    public String geHitSortFile() {
        int index = cmbHitSortFile.getSelectedIndex();
        return (String) ((index == -1) ? "" : cmbHitSortFile.getSelectedItem());
    }

    public int getRenumberFromMFN() {
        Number num = (Number) txtRenumberFromMFN.getValue();
        return (num == null) ? -1 : num.intValue();
    }

    public int getOutputTagMFN() {
        Number num = (Number) txtOutputTagMFN.getValue();
        return (num == null) ? -1 : num.intValue();
    }

    public int getMfnsRangeOption() {
        if (rdbMfnRange.isSelected() && rdbAllMfn.isSelected()) {
            return Global.MFNS_OPTION_ALL;
        } else if (rdbMfnRange.isSelected() && rdbMfns.isSelected()) {
            return Global.MFNS_OPTION_RANGE;
        } else if (rdbMfnRange.isSelected() && rdbMarked.isSelected()) {
            return Global.MFNS_OPTION_MARKED;
        } else if (rdbSearchResult.isSelected()) {
            return Global.MFNS_OPTION_SEARCH;
        } else if (rdbHitSort.isSelected()) {
            return Global.MFNS_OPTION_HITSORT;
        } else {
            return Global.MFNS_OPTION_ALL;
        }
    }

    public String getMfnRanges() {
        String s = txtMfns.getText();
        return s;
    }

    public boolean isSearchResult() {
        return rdbSearchResult.isSelected();
    }

    public int getSearchHistoryIndex() {
        int index = cmbSearch.getSelectedIndex();
        return index;
    }

    public int getMarkedRecordsIndex() {
        int index = cmbMarked.getSelectedIndex();
        return index;
    }

}
