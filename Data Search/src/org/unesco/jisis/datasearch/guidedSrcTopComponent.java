package org.unesco.jisis.datasearch;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import org.openide.ErrorManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.DefaultDBNotFoundException;
import org.unesco.jisis.corelib.exceptions.FormattingException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.FieldComparator;
import org.unesco.jisis.jisiscore.client.GuiGlobal;

/**
 * Top component which displays something.
 */
final class guidedSrcTopComponent extends TopComponent {

    private static guidedSrcTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/unesco/jisis/datasearch/browse.png";
    private static final String PREFERRED_ID = "guidedSrcTopComponent";
    private ClientDatabaseProxy db_;
    private long[] mfns_;

    public guidedSrcTopComponent(IDatabase db) {
        initComponents();
        try {

            if (db instanceof ClientDatabaseProxy) {
                db_ = (ClientDatabaseProxy) db;
            } else {
                throw new RuntimeException("guidedSrcTopComponent: Cannot cast DB to ClientDatabaseProxy");
            }
            /* Register this TopComponent as attached to this DB */
            db_.addWindow(this);

            Record r;
            r = (Record) db_.getLast();
            FieldDefinitionTable fdt = db_.getFieldDefinitionTable();
            FieldSelectionTable fst = db_.getFieldSelectionTable();
           
            List<IField> ff = r.getFields();
            //attributes.addAll(fdt.getFields());
            String[] fields = new String[ff.size() + 2];
            fields[0] = "";
          
            for (int i = 0; i < ff.size(); i++) {
                IField f = ff.get(i);
                fields[i + 1] = f.getTag() + "@" + fdt.getFieldByTag(f.getTag()).getName();
            }
            fields[ff.size() + 1] = "MFN";
            int[] fstTags = fst.getEntriesTag();
            String[] srcFields = new String[fstTags.length + 1];
            int j = 0;
            srcFields[0] = "Any";
            for (int i = 0; i < fstTags.length; i++) {
                System.out.println("i=" + Integer.toString(i) + " tag=" + Integer.toString(fstTags[i]));
                System.out.println((fdt.getFieldByTag(fstTags[i]) == null) ? "NULL"
                        : fdt.getFieldByTag(fstTags[i]).getName());
                //IField f = r.getField(fstTags[i]);
                if (fdt.getFieldByTag(fstTags[i]) != null) {
                    /** fsTags may contain old legacy tags ! */
                    srcFields[j + 1] = Integer.toString(fstTags[i]) + "@" +
                            fdt.getFieldByTag(fstTags[i]).getName();
                    j++;
                }
            }
            field1.setModel(new DefaultComboBoxModel(srcFields));
            field2.setModel(new DefaultComboBoxModel(srcFields));
            field3.setModel(new DefaultComboBoxModel(srcFields));
            orderField.setModel(new DefaultComboBoxModel(fields));
       
        } catch (DbException ex) {
            ex.printStackTrace();
        }
        setName(NbBundle.getMessage(guidedSrcTopComponent.class, "CTL_guidedSrcTopComponent"));
        setToolTipText(NbBundle.getMessage(guidedSrcTopComponent.class, "HINT_guidedSrcTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      resultPanel = new javax.swing.JScrollPane();
      lstMFN = new javax.swing.JList();
      searchButton = new javax.swing.JButton();
      field1 = new javax.swing.JComboBox();
      jTextField1 = new javax.swing.JTextField();
      logOp1 = new javax.swing.JComboBox();
      jTextField2 = new javax.swing.JTextField();
      logOp2 = new javax.swing.JComboBox();
      field2 = new javax.swing.JComboBox();
      field3 = new javax.swing.JComboBox();
      jTextField3 = new javax.swing.JTextField();
      lblMFN = new javax.swing.JLabel();
      jButton3 = new javax.swing.JButton();
      jLabel1 = new javax.swing.JLabel();
      formatTxt = new javax.swing.JTextField();
      jLabel2 = new javax.swing.JLabel();
      orderField = new javax.swing.JComboBox();
      jScrollPane2 = new javax.swing.JScrollPane();
      lstResult = new javax.swing.JEditorPane();
      exportButton = new javax.swing.JButton();
      jSeparator1 = new javax.swing.JSeparator();
      jScrollPane3 = new javax.swing.JScrollPane();
      listMfn = new javax.swing.JList();

      resultPanel.setViewportView(lstMFN);

      org.openide.awt.Mnemonics.setLocalizedText(searchButton, "Search");
      searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            searchButtonMouseClicked(evt);
         }
      });

      field1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

      logOp1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "stop", "and", "or" }));

      logOp2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "stop", "and", "or", "not" }));

      field2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

      field3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

      org.openide.awt.Mnemonics.setLocalizedText(lblMFN, "MFN's");

      org.openide.awt.Mnemonics.setLocalizedText(jButton3, "View the results in format");
      jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            jButton3MouseClicked(evt);
         }
      });

      org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Format:");

      formatTxt.setText("v1/v2");

      org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Order by");

      orderField.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            orderFieldItemStateChanged(evt);
         }
      });

      jScrollPane2.setViewportView(lstResult);

      org.openide.awt.Mnemonics.setLocalizedText(exportButton, "Export To Html");
      exportButton.setVerifyInputWhenFocusTarget(false);
      exportButton.addMouseListener(new java.awt.event.MouseAdapter() {
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            exportButtonMouseClicked(evt);
         }
      });

      listMfn.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      jScrollPane3.setViewportView(listMfn);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(25, 25, 25)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(field2, 0, 109, Short.MAX_VALUE)
               .addComponent(field3, 0, 109, Short.MAX_VALUE)
               .addComponent(field1, 0, 109, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                           .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)))
                     .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(logOp2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(logOp1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(exportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                  .addGap(36, 36, 36)
                  .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(67, 67, 67)
                  .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(21, 21, 21)
                  .addComponent(formatTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                  .addComponent(jLabel2)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(orderField, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(25, 25, 25)))
            .addContainerGap())
         .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 982, Short.MAX_VALUE)
         .addGroup(layout.createSequentialGroup()
            .addGap(24, 24, 24)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(23, 23, 23)
                  .addComponent(jScrollPane2))
               .addComponent(lblMFN))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(logOp1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(field1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addGap(23, 23, 23)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(logOp2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(field2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addGap(25, 25, 25)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(exportButton)
                     .addComponent(field3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addGroup(layout.createSequentialGroup()
                  .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(48, 48, 48)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(formatTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1)
                     .addComponent(jLabel2)
                     .addComponent(orderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addComponent(lblMFN))
               .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
               .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents

    private void searchButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMouseClicked
        try {
            String srchQuery = this.buildQuery();
            ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
            IDatabase db = connectionInfo.getDefaultDatabase();
            mfns_ = db.search(srchQuery);
            //System.out.println("search reported" +  mfns.length + "results");
            Long[] mfnsData = new Long[mfns_.length];


            if (mfns_.length > 0) {

                for (int i = 0; i < mfns_.length; i++) {
                    mfnsData[i] = new Long(mfns_[i]);
                }
                lstResult.setText("");
                listMfn.repaint();
            }

            listMfn.setModel(new JList(mfnsData).getModel());
            listMfn.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_searchButtonMouseClicked

    private void exportButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportButtonMouseClicked
        int role = 0;
        IConnection conn = null;
        
//        try {
//            conn = ConnectionPool.getDefaultConnection();
//            try {
//                role = ((Integer) (conn.getUserInfo().getPermissions().get(DatabasePool.getDefaultDatabase().getDatabaseName()))).intValue();
//            } catch (Exception e) {
//                role = 0;
//            }
//            if (!(conn.getUserInfo().getIsAdmin()) && role != 2) {
//            } else {
//                String filename = "";
//                RecordExporter rExp = new RecordExporter();
//                IRecord rec = null;
//                String toPrint = "";
//                JFileChooser chooser = new JFileChooser();
//                File currentDirectory = new File("");
//                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//
//                if (currentDirectory != null) {
//                    chooser.setCurrentDirectory(currentDirectory);
//                }
//
//                chooser.setDialogTitle("Choose");
//
//                int returnVal = chooser.showSaveDialog(null);
//
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    FileObject newfile = null;
//                    File toDir = chooser.getSelectedFile().getParentFile();
//                    filename = toDir.getAbsolutePath() + "/" + chooser.getSelectedFile().getName();
//                //System.out.println(filename);
//                }
//
//                PrintFormatter pf = new PrintFormatter();
//                List recs = new ArrayList();
//                IDatabase db = null;
//                try {
//                    for (int i = 0; i < new Long(mfns_.length).intValue(); i++) {
//                        db = DatabasePool.getDefaultDatabase();
//                        recs.add(db.getRecord(mfns_[i]));
//                    }
//                    toPrint = pf.getHtmlFormat(db, recs, formatTxt.getText());
//                } catch (DbException dbe) {
//                    new FormattingException(dbe.getMessage()).displayWarning();
//                } catch (DefaultDBNotFoundException ex) {
//                    ex.printStackTrace();
//                }
//                rExp.exportToHTML(filename, toPrint);
//            }
//        } catch (NoConnectionException ex) {
//            ex.displayWarning();
//        }
    }//GEN-LAST:event_exportButtonMouseClicked

    private void orderFieldItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_orderFieldItemStateChanged
        String toPrint = "";
        IDatabase db = null;
        try {
            ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
            db = connectionInfo.getDefaultDatabase();
            ISISFormatter formatter = ISISFormatter.getFormatter(formatTxt.getText());
             if (formatter == null) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return;
             } else if (formatter.hasParsingError()) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return;
             }
            List<Record> records = new LinkedList<Record>();
            String item = (String) orderField.getSelectedItem();
            int tag = 0;
            if (item.compareToIgnoreCase("") != 0) {
                if (item.compareToIgnoreCase("MFN") == 0) {
                    Arrays.sort(mfns_);
                } else {
                    tag = Integer.parseInt(getTagFromField(item));
                }
                for (int i = 0; i < mfns_.length; i++) {
                    db = connectionInfo.getDefaultDatabase();
                    System.out.println("looking for record" + i);
                    records.add((Record) db.getRecord(mfns_[i]));
                }
                if (tag > 0) {
                    Collections.sort(records, new FieldComparator(tag));
                }
                for (int i = 0; i < records.size(); i++) {
                    formatter.setRecord(db, records.get(i));
                    formatter.eval();
                    toPrint +=  records.get(i).getMfn() + "\n" + formatter.getText() + "\n \n \n";
                }
                lstResult.setText(toPrint);
                lstMFN.repaint();
            }
        } catch (DbException ex) {
            ex.printStackTrace();
        } 
    }//GEN-LAST:event_orderFieldItemStateChanged

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        String toPrint = new String();
        
        IDatabase db = null;
        try {
            ISISFormatter formatter = ISISFormatter.getFormatter(formatTxt.getText());
             if (formatter == null) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return;
             } else if (formatter.hasParsingError()) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return;
             }
             ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
             db = connectionInfo.getDefaultDatabase();
            for (int i = 0; i < new Long(mfns_.length).intValue(); i++) {
                formatter.setRecord(db, db.getRecord(mfns_[i]));
                formatter.eval();
                toPrint += mfns_[i] + "\n" + formatter.getText() + "\n \n \n";
            }
            lstResult.setText(toPrint);
            lstMFN.repaint();
        } catch (DbException dbe) {
            new FormattingException(dbe.getMessage()).displayWarning();
        } 
    }//GEN-LAST:event_jButton3MouseClicked
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton exportButton;
   private javax.swing.JComboBox field1;
   private javax.swing.JComboBox field2;
   private javax.swing.JComboBox field3;
   private javax.swing.JTextField formatTxt;
   private javax.swing.JButton jButton3;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JScrollPane jScrollPane3;
   private javax.swing.JSeparator jSeparator1;
   private javax.swing.JTextField jTextField1;
   private javax.swing.JTextField jTextField2;
   private javax.swing.JTextField jTextField3;
   private javax.swing.JLabel lblMFN;
   private javax.swing.JList listMfn;
   private javax.swing.JComboBox logOp1;
   private javax.swing.JComboBox logOp2;
   private javax.swing.JList lstMFN;
   private javax.swing.JEditorPane lstResult;
   private javax.swing.JComboBox orderField;
   private javax.swing.JScrollPane resultPanel;
   private javax.swing.JButton searchButton;
   // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized guidedSrcTopComponent getDefault() {
        if (instance != null) {
            instance.close();
            instance = null;
        }

        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();

        if (connectionInfo.getDefaultDatabase() != null && instance == null) {
            instance = new guidedSrcTopComponent(connectionInfo.getDefaultDatabase());
        }

        return instance;
    }

    /**
     * Obtain the guidedSrcTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized guidedSrcTopComponent findInstance() throws DefaultDBNotFoundException {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find guidedSrc component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof guidedSrcTopComponent) {
            return (guidedSrcTopComponent) win;
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
        this.repaint();
        lstResult.setText("");
        lstMFN.setModel(new JList().getModel());
        try {
            ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
            IDatabase db = connectionInfo.getDefaultDatabase();
            if (db instanceof ClientDatabaseProxy) {
                db_ = (ClientDatabaseProxy) db;
            } else {
                throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
            }

            String name = db_.getDefaultPrintFormatName();
            formatTxt.setText(db_.getPrintFormat(name));
        } catch (DbException ex) {
            ex.printStackTrace();
        } 

    }

   @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        db_.deleteWindow(this);
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

    public void disableExportButton() {
        exportButton.setEnabled(false);
    }

    public void enableExportButton() {
        exportButton.setEnabled(true);
    }

    private String getTagFromField(String field) {
        String[] splitted = field.split("@");
        return splitted[0];
    }

    private String buildExpr(String text, int field, String fieldText) {
        String query = "";
        if (text != null && (!text.equals(""))) {
            if (field != 0) {
                query += text;
                query += "/(" + this.getTagFromField(fieldText) + ")";
            } else {
                query += text;
            }
        }
        return query;
    }

    private String buildLogic(String logOp) {
        if (logOp.equalsIgnoreCase("and")) {
            return "*";
        }
        if (logOp.equalsIgnoreCase("or")) {
            return "+";
        }
        return "";
    }

    private String buildQuery() {
        String query = "";
        query += this.buildExpr(jTextField1.getText(), field1.getSelectedIndex(), (String) field1.getSelectedItem());
        String logOp = (String) logOp1.getSelectedItem();
        if (!(logOp.equalsIgnoreCase("stop"))) {
            query += this.buildLogic(logOp);
            query += this.buildExpr(jTextField2.getText(), field2.getSelectedIndex(), (String) field2.getSelectedItem());
            logOp = (String) logOp2.getSelectedItem();
            if (!(logOp.equalsIgnoreCase("stop"))) {
                query += this.buildLogic(logOp);
                query += this.buildExpr(jTextField3.getText(), field3.getSelectedIndex(), (String) field3.getSelectedItem());
            }
        }
        //System.out.println("query: " + query);
        return query;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return guidedSrcTopComponent.getDefault();
        }
    }
}
