/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.searchhistory;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.FormattedRecord;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.Lucene;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.corelib.index.QueryTerm;
import org.unesco.jisis.corelib.index.SearchableField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.fxbrowser.SwingFXWebView;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.MarkedRecords;
import org.unesco.jisis.jisiscore.client.SearchResult;


class SearchComponent {
   public String tag_;   // field name (tag or FST entry name)
   public String option_;
   public String searchTerm_;
   
   public SearchComponent(String tag, String option, String searchTerm) {
      tag_ = tag;
      option_ = option;
      searchTerm_ = searchTerm;
   }
}

 class MyQueryParser extends QueryParser {

   private final List<QueryTerm> terms = new ArrayList<>();

   public MyQueryParser(Version version, String defaultField, Analyzer analyzer) {
      super(version, defaultField, analyzer);
   }

   @Override
   protected final Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
      Query query = super.getFieldQuery(field, queryText, quoted);
      QueryTerm term = new QueryTerm(field, queryText, quoted);
      terms.add(term);
      return query;
   }

   public List<QueryTerm> getQueryTerms() {
      return terms;
   }
}
/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.unesco.jisis.searchhistory//DataViewerList//EN",
autostore = false)
public final class DataViewerListTopComponent extends TopComponent implements Observer {

    private static DataViewerListTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "DataViewerListTopComponent";
    private ClientDatabaseProxy db_ = null;
    
     private FieldDefinitionTable fdt_;
   private FieldSelectionTable fst_;
   
   private SearchResult searchResult_;

    private String currentPftName_ = null;

    private FormattedRecord currentFormattedRecord_ = null;
    
    List<QueryTerm> queryTerms_ = new ArrayList<>();
    
    List<String> searchTerms_;
    
    private SearchableField[] searchableFields_ = null;
    
  
    private long currentMfn_ = 0;
    private ArrayList<Long> markedRecords_ = new ArrayList<>();

    private List<Long> mfns_ = null;
    private int cursor_ = 0;
    private int lastIndex_;

    private SwingFXWebView swingFXWebView = null;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DataViewerListTopComponent.class);

   public DataViewerListTopComponent() {

   }
    public DataViewerListTopComponent(IDatabase db, SearchResult searchResult) {
        searchResult_ = searchResult;
        mfns_ = searchResult.getMfns();
        if (db instanceof ClientDatabaseProxy) {
            db_ = (ClientDatabaseProxy) db;
        } else {
            throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
        }
        /* Register this TopComponent as attached to this DB */
        db_.addWindow(this);

        try {
            long recCount = db_.getRecordsCount();
            long maxMFN = db_.getLastMfn();
            System.out.println("DbViewTopComponent recCount=" + recCount
                + " maxMFN=" + maxMFN);
            fdt_ = db_.getFieldDefinitionTable();
            fst_ = db_.getFieldSelectionTable();

        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        initComponents();

//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        webPanel.setPreferredSize(new Dimension(700, 500));

        if (swingFXWebView == null) {
            swingFXWebView = new SwingFXWebView();
            webPanel.add(swingFXWebView, BorderLayout.CENTER);
        }

        int searchNumber = searchResult.getSearchNumber();
        String dbName = searchResult.getDbName();
        int numberOfHits = searchResult.getMfns().size();

        String msg = NbBundle.getMessage(DataViewerListTopComponent.class, "CTL_DisplayName")
            + " " + "#" + searchNumber + " (" + dbName + ") " + "hits=" + numberOfHits;

        setName(msg);
        setDisplayName(msg);
        setToolTipText(NbBundle.getMessage(DataViewerListTopComponent.class, "HINT_DataViewerListTopComponent"));

        //setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        getPfts();

        populateFields();

        String luceneQuery = searchResult_.getLuceneQuery();

        // Build the list of search terms to highlight in the html
        if (luceneQuery == null) {
            // Free Text Search
            String squery = searchResult_.getSearchQuery();

            // Extract literals between quotes
            String[] literals = StringUtils.substringsBetween(squery, "'", "'");

            searchTerms_ = new ArrayList<>();
            for (String s : literals) {
                if (s.equalsIgnoreCase("true")) {
                    continue;
                }
                searchTerms_.add(s);
            }
        } else {
            // Lucene Search

            /**
             * searchableFields_[0] is the <All Fields> thus the default Lucene field is provided by
             * searchableFields_[1]
             */
            String defaultField = "_" + searchableFields_[1].tag;
            if (searchableFields_[1].name != null && searchableFields_[1].name.length() > 0) {
                defaultField = searchableFields_[1].name;
            }
            MyQueryParser parser;
            parser = new MyQueryParser(Lucene.MATCH_VERSION, defaultField, new KeywordAnalyzer());
            Query query = null;
            try {
                query = parser.parse(luceneQuery);
            } catch (ParseException ex) {
                LOGGER.error("Query Parsing Exception", ex);
                JOptionPane.showMessageDialog(this, "A syntax error occurred in the query!", "Syntax Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            /**
             * Both prefix and wildcard queries are lowercased by default, but this behavior can be
             * controlled:
             */
            parser.setLowercaseExpandedTerms(false);
            queryTerms_ = parser.getQueryTerms();
            searchTerms_ = new ArrayList<String>();
            for (QueryTerm queryTerm : queryTerms_) {
                String termText = queryTerm.value();
                if (searchTerms_.contains(termText)) {
                    continue;
                }
                searchTerms_.add(termText);
            }

        }

        lastIndex_ = mfns_.size() - 1;
        try {
            long maxMFN = mfns_.get(lastIndex_);
            txtMaxMfn.setText(maxMFN + "");
            setName(NbBundle.getMessage(DataViewerListTopComponent.class, "CTL_DataViewerListTopComponent")
                + " (" + db.getDbHome() + "//" + db_.getDatabaseName() + ")");
            displayFirstRecord();
        } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
        }
    }

    private void populateFields() {

        // The searchable fields
        HashSet<Integer> tags = new HashSet<>();
        ArrayList<SearchableField> searchableFields = new ArrayList<>();
        searchableFields.add(new SearchableField("<All Searchable Fields>", -1));

        for (int i = 0; i < this.fst_.getEntriesCount(); i++) {
            FieldSelectionTable.FstEntry entry = this.fst_.getEntryByIndex(i);
            int tag = entry.getTag();
            String name = entry.getName();
            if (!tags.add(tag)) {
                continue;
            }
            String fieldName = "_" + tag;
            if (name != null && name.length() > 0) {
                fieldName = name;
            }

            searchableFields.add(new SearchableField(
                fieldName,
                tag));
        }

        searchableFields_ = new SearchableField[searchableFields.size()];
        searchableFields.toArray(searchableFields_);

    }


   private void getPfts() {
      try {
        
         currentPftName_ = "RAW";
         String[] pftNames = db_.getPrintFormatNames();
         cmbPftSelect.setModel(new DefaultComboBoxModel(pftNames));
      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }
   }
   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ctrlPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        lblMFN = new javax.swing.JLabel();
        txtMFN = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnFirst = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnPrev = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btnNext = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        btnLast = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        lblFormat = new javax.swing.JLabel();
        cmbPftSelect = new javax.swing.JComboBox();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        lblMaxMfn = new javax.swing.JLabel();
        txtMaxMfn = new javax.swing.JTextField();
        jToolBar2 = new javax.swing.JToolBar();
        btnMarkMenu = new javax.swing.JButton();
        chkMarkRecord = new javax.swing.JCheckBox();
        webPanel = new javax.swing.JPanel();

        ctrlPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ctrlPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(lblMFN, org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.lblMFN.text")); // NOI18N
        jToolBar1.add(lblMFN);

        txtMFN.setEditable(false);
        txtMFN.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtMFN.setText(org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.txtMFN.text")); // NOI18N
        txtMFN.setToolTipText(org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.txtMFN.toolTipText")); // NOI18N
        txtMFN.setMaximumSize(new java.awt.Dimension(100, 100));
        txtMFN.setPreferredSize(new java.awt.Dimension(40, 20));
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
        jToolBar1.add(txtMFN);
        jToolBar1.add(jSeparator1);

        btnFirst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/searchhistory/2leftarrow.png"))); // NOI18N
        btnFirst.setToolTipText(org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.btnFirst.toolTipText")); // NOI18N
        btnFirst.setPreferredSize(new java.awt.Dimension(30, 25));
        btnFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirstActionPerformed(evt);
            }
        });
        jToolBar1.add(btnFirst);
        jToolBar1.add(jSeparator3);

        btnPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/searchhistory/1leftarrow.png"))); // NOI18N
        btnPrev.setToolTipText(org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.btnPrev.toolTipText")); // NOI18N
        btnPrev.setPreferredSize(new java.awt.Dimension(30, 25));
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });
        jToolBar1.add(btnPrev);
        jToolBar1.add(jSeparator4);

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/searchhistory/1rightarrow.png"))); // NOI18N
        btnNext.setToolTipText(org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.btnNext.toolTipText")); // NOI18N
        btnNext.setPreferredSize(new java.awt.Dimension(30, 25));
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNext);
        jToolBar1.add(jSeparator5);

        btnLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/searchhistory/2rightarrow.png"))); // NOI18N
        btnLast.setToolTipText(org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.btnLast.toolTipText")); // NOI18N
        btnLast.setPreferredSize(new java.awt.Dimension(30, 25));
        btnLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastActionPerformed(evt);
            }
        });
        jToolBar1.add(btnLast);
        jToolBar1.add(jSeparator6);

        org.openide.awt.Mnemonics.setLocalizedText(lblFormat, org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.lblFormat.text")); // NOI18N
        jToolBar1.add(lblFormat);

        cmbPftSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbPftSelect.setMaximumSize(new java.awt.Dimension(150, 32767));
        cmbPftSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPftSelectActionPerformed(evt);
            }
        });
        jToolBar1.add(cmbPftSelect);
        jToolBar1.add(jSeparator2);

        org.openide.awt.Mnemonics.setLocalizedText(lblMaxMfn, org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.lblMaxMfn.text")); // NOI18N
        jToolBar1.add(lblMaxMfn);

        txtMaxMfn.setEditable(false);
        txtMaxMfn.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtMaxMfn.setMaximumSize(new java.awt.Dimension(100, 2147483647));
        txtMaxMfn.setPreferredSize(new java.awt.Dimension(40, 20));
        jToolBar1.add(txtMaxMfn);

        jToolBar2.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(btnMarkMenu, org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.btnMarkMenu.text")); // NOI18N
        btnMarkMenu.setFocusable(false);
        btnMarkMenu.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnMarkMenu.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnMarkMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkMenuActionPerformed(evt);
            }
        });
        jToolBar2.add(btnMarkMenu);

        org.openide.awt.Mnemonics.setLocalizedText(chkMarkRecord, org.openide.util.NbBundle.getMessage(DataViewerListTopComponent.class, "DataViewerListTopComponent.chkMarkRecord.text")); // NOI18N
        chkMarkRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMarkRecordActionPerformed(evt);
            }
        });
        jToolBar2.add(chkMarkRecord);

        javax.swing.GroupLayout ctrlPanelLayout = new javax.swing.GroupLayout(ctrlPanel);
        ctrlPanel.setLayout(ctrlPanelLayout);
        ctrlPanelLayout.setHorizontalGroup(
            ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ctrlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 616, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ctrlPanelLayout.setVerticalGroup(
            ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ctrlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        webPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        webPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(webPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
                    .addComponent(ctrlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 868, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(ctrlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

   private void txtMFNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMFNActionPerformed
      // TODO add your handling code here:
}//GEN-LAST:event_txtMFNActionPerformed

   private void txtMFNKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMFNKeyPressed



}//GEN-LAST:event_txtMFNKeyPressed

   private void btnFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirstActionPerformed
      if (cursor_ == 0) {
         return;
      }
      displayFirstRecord();
}//GEN-LAST:event_btnFirstActionPerformed

   private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed


         if (cursor_ == 0) {
           return;
         }
         cursor_--;
         long mfn = mfns_.get(cursor_);
         displayRecord(mfn);
    
}//GEN-LAST:event_btnPrevActionPerformed

   private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed

      
         if (cursor_ == lastIndex_) {
           return;
         }
         cursor_++;
         long mfn = mfns_.get(cursor_);
         displayRecord(mfn);
      
}//GEN-LAST:event_btnNextActionPerformed

   private void btnLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastActionPerformed

       cursor_ = lastIndex_;
       long lastMfn = mfns_.get(cursor_);
       displayRecord(lastMfn);

}//GEN-LAST:event_btnLastActionPerformed

   private void cmbPftSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPftSelectActionPerformed

       currentPftName_ = cmbPftSelect.getSelectedItem().toString();
       displayRecord(currentMfn_);

   }//GEN-LAST:event_cmbPftSelectActionPerformed

   private void btnMarkMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkMenuActionPerformed
       createAndShowMenu((JComponent) evt.getSource(), btnMarkMenu);
   }//GEN-LAST:event_btnMarkMenuActionPerformed

   private void chkMarkRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMarkRecordActionPerformed
      if (chkMarkRecord.isSelected()) {
         markedRecords_.add(currentMfn_);
      } else {
         int index = markedRecords_.indexOf(currentMfn_);
         if (index >= 0) {
            markedRecords_.remove(index);
         }
      }
}//GEN-LAST:event_chkMarkRecordActionPerformed

    private void displayFirstRecord() {

        cursor_ = 0;
        long firstMfn = mfns_.get(cursor_);
        displayRecord(firstMfn);
    }
   
    private void doDisplayRecord(final String content) {

      Runnable displayRun = new Runnable() {
         @Override
         public void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
               try {
                  swingFXWebView.loadContent(content);
               } catch (Exception ex) {
                  Exceptions.printStackTrace(ex);
               } finally {
                  // clear status text
                  StatusDisplayer.getDefault().setStatusText(""); // NOI18N
                  // clear wait cursor
                  SwingUtilities.invokeLater(this);
               }

            }
         }
      };
      RequestProcessor.Task loadTask = RequestProcessor.getDefault().post(displayRun);
      loadTask.waitFinished();
   }

    public void displayRecord(long mfn) {
        String fmtName = (String) this.cmbPftSelect.getSelectedItem();
        FormattedRecord formattedRecord = null;
        IRecord record = null;
        try {
            formattedRecord = db_.getRecordFmt(mfn, fmtName);
            record = db_.getRecord(mfn);
        } catch (DbException ex) {
            LOGGER.error("Error formatting record with mfn {}", mfn, ex);
        }
        displayRecord(record, formattedRecord);
    }
     
     public void displayRecord(IRecord record, FormattedRecord formattedRecord) {

      if (formattedRecord == null) {
         return;
      }
      currentFormattedRecord_ = formattedRecord;
      

      String fmtName = (String) this.cmbPftSelect.getSelectedItem();
      String content = formattedRecord.getRecord();
      
      
       // Hilight search terms only for RAW format
      try {
         String pft = db_.getPrintFormat(fmtName);
                if (fmtName.equals("RAW") && pft.equals("")) {
                    content = highlightHTML(content, searchTerms_);
                } else {
                    content = highlightTermsHTML(content, searchTerms_);
                }
     } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }

      final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
      StatusDisplayer.getDefault().setStatusText("Starting browser display ...");
      RepaintManager.currentManager(frame).paintDirtyRegions();
      
      doDisplayRecord(content);
      
      long mfn = formattedRecord.getMfn();
      txtMFN.setText(mfn + "");
      currentMfn_ = mfn;

      int index = markedRecords_.indexOf(currentMfn_);
      chkMarkRecord.setSelected((index >= 0) ? true : false);
   }
     
     /**
    * Change the RAW HTML content with search terms highlighted
    * 
    * We use Jsoup to parse the RAW HTML content and to select the table cells
    * with id="occurrence". The table cell HTML content is changed if it 
    * contains searched terms.
    * 
    * @param htmlContent - The RAW HTML content for a record
    * @param textToHilight - The list of searched terms that matched this 
    *                        record 
    * @return - The raw HTML content changed for highlighting the search terms
    */
    private String highlightHTML(String htmlContent, List<String> textToHilight) {

        /**
         * Change the "<>" html entities codes so that they will not be unescape
         */
        htmlContent = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(htmlContent, "&lt;", "@lt;");
        htmlContent = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(htmlContent, "&gt;", "@gt;");

        Document document = Jsoup.parse(htmlContent);

        Elements occurrences = document.select("td:nth-child(2)"); // Field/Occurrence column
        for (Element occurrence : occurrences) {
            String occurrenceHtml = occurrence.html();
            /**
             * Jsoup converts special characters to HTML entities - unescape all
             */
            occurrenceHtml
                = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(occurrenceHtml);

            for (String s : textToHilight) {
               // The term is fully upper case, thus do case insensitive

                occurrenceHtml
                    = org.unesco.jisis.corelib.util.StringUtils.hilightNormalized(occurrenceHtml, s);

            }
            /**
             * Restore the initial "<>" html entities so that they will not be interpreted as html tags
             */
            occurrenceHtml = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(occurrenceHtml, "@lt;", "&lt;");
            occurrenceHtml = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(occurrenceHtml, "@gt;", "&gt;");

            occurrence.html(occurrenceHtml);
        }
        String s = document.html();
        return s;

    }
    private String highlightTermsHTML(String htmlContent, List<String> textToHilight) {

        /**
         * Change the "<>" html entities codes so that they will not be unescape
         */
        htmlContent = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(htmlContent, "&lt;", "@lt;");
        htmlContent = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(htmlContent, "&gt;", "@gt;");
        Document document = Jsoup.parse(htmlContent);
        Elements elements = document.body().select("*");

        for (Element element : elements) {
            String text = element.ownText();
            if (text == null || text.length() == 0) {
                continue;
            }
            /**
             * Jsoup converts special characters to HTML entities - unescape all
             */
            text
                = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(text);
            for (String s : textToHilight) {

                text
                    = org.unesco.jisis.corelib.util.StringUtils.hilightNormalized(text, s);
            }
            /**
             * Restore the initial "<>" html entities so that they will not be interpreted as html tags
             */
            text = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(text, "@lt;", "&lt;");
            text = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(text, "@gt;", "&gt;");
            element.html(text);
        }
        String s = document.html();
        return s;
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirst;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnMarkMenu;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JCheckBox chkMarkRecord;
    private javax.swing.JComboBox cmbPftSelect;
    private javax.swing.JPanel ctrlPanel;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JLabel lblFormat;
    private javax.swing.JLabel lblMFN;
    private javax.swing.JLabel lblMaxMfn;
    private javax.swing.JTextField txtMFN;
    private javax.swing.JTextField txtMaxMfn;
    private javax.swing.JPanel webPanel;
    // End of variables declaration//GEN-END:variables
   /**
    * Gets default instance. Do not use directly: reserved for *.settings files only,
    * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
    * To obtain the singleton instance, use {@link #findInstance}.
     * @return 
    */
   public static synchronized DataViewerListTopComponent getDefault() {
      if (instance == null) {
         instance = new DataViewerListTopComponent();
      }
      return instance;
   }

   /**
    * Obtain the DataViewerListTopComponent instance. Never call {@link #getDefault} directly!
     * @return 
    */
   public static synchronized DataViewerListTopComponent findInstance() {
      TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
      if (win == null) {
         Logger.getLogger(DataViewerListTopComponent.class.getName()).warning(
                 "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
         return getDefault();
      }
      if (win instanceof DataViewerListTopComponent) {
         return (DataViewerListTopComponent) win;
      }
      Logger.getLogger(DataViewerListTopComponent.class.getName()).warning(
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

   void writeProperties(java.util.Properties p) {
      // better to version settings since initial version as advocated at
      // http://wiki.apidesign.org/wiki/PropertyFiles
      p.setProperty("version", "1.0");
      // TODO store your settings
   }

   Object readProperties(java.util.Properties p) {
      if (instance == null) {
         instance = this;
      }
      instance.readPropertiesImpl(p);
      return instance;
   }

   private void readPropertiesImpl(java.util.Properties p) {
      String version = p.getProperty("version");
      // TODO read your settings according to their version
   }

   @Override
   protected String preferredID() {
      return PREFERRED_ID;
   }

   @Override
   public void update(Observable o, Object arg) {
      if (db_.databaseHasChanged()) {
         try {
            long maxMFN = db_.getLastMfn();
            txtMaxMfn.setText(maxMFN + "");
            // Be sure to be on the record displayed, because another window
            // may have changed the record cursor
            IRecord record = db_.getRecordCursor(currentMfn_);
         //displayRecord(db_.getCurrent());
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
      }
      if (db_.pftHasChanged()) {
         // Update the list of Pfts
         String[] pftNames = {""};
         try {
            pftNames = db_.getPrintFormatNames();
             cmbPftSelect.setModel(new DefaultComboBoxModel(pftNames));
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
        if (!Arrays.asList(pftNames).contains(currentPftName_)) {
           // Has been removed ?
           currentPftName_ = "RAW";
           
        }

        
          cmbPftSelect.setSelectedItem(currentPftName_);

          displayRecord(currentMfn_);

         
      }
   }
   
   private  void createAndShowMenu(final JComponent component, final AbstractButton moreButton) {
      JPopupMenu menuMark = new JPopupMenu();
      JMenuItem clearMarks = new JMenuItem(NbBundle.getMessage(DataViewerListTopComponent.class, "MSG_ClearMarkedRecords"));
      ActionListener clearMarkedRecordsListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (markedRecords_.isEmpty()) {
               return;
            }
            int n = markedRecords_.size();
            markedRecords_.clear();
            chkMarkRecord.setSelected(false);
            String msg = n+" "+NbBundle.getMessage(DataViewerListTopComponent.class,
                    "MSG_MarkedRecordsCleared");
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg);
            DialogDisplayer.getDefault().notify(d);
         }
      };
      clearMarks.addActionListener(clearMarkedRecordsListener);
      menuMark.add(clearMarks);
      JMenuItem saveMarksMenuItem = new JMenuItem(NbBundle.getMessage(DataViewerListTopComponent.class, "MSG_SaveMarkedRecords"));
      ActionListener saveMarksListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (markedRecords_.isEmpty()) {
               return;
            }
            String markedSetName = "marked";
            MarkedRecords markedRecords = new MarkedRecords(0, db_.getDbName(), markedSetName, markedRecords_);
            db_.addMarkedRecords(markedRecords);
             int n = markedRecords_.size();
             String msg = n+" "+NbBundle.getMessage(DataViewerListTopComponent.class,
                    "MSG_MarkedRecordsSaved");
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg);
            DialogDisplayer.getDefault().notify(d);
         }
      };
      saveMarksMenuItem.addActionListener(saveMarksListener);
      menuMark.add(saveMarksMenuItem);
      menuMark.show(component, 0, component.getHeight());
   }
   
   /**
   * Extract out terms from query.
   * <p><b>IMPLEMENTATION NOTE:</b> Lucene does not provide a robust,
   * single method from extracting the low terms of a query.
   * Experimentation has shown that some Query types
   * {@link Query#extractTerms(Set)} methods do not work, or do
   * not work as desired.  Therefore, this method checks for specific
   * Query types to extract terms.
   * </p>
   * @param   q       Query to extract terms of.
   * @param   r       {@link IndexReader} the executed the query.
   * @param   terms   {@link Term} {@link Set set} to fill; if
   *                  <tt>null</tt>, a newly allocated set will be
   *                  returned.
   * @return  Set of terms.
   */
   static public final String  FIELD_CONTENT = "field";
    public static Set<Term> extractTermsFromQuery(
        Query q,
        IndexReader r,
        Set<Term> terms
    ) {
        if (terms == null) {
            terms = new HashSet<Term>();
        }
        if (q instanceof TermQuery) {
            terms.add(((TermQuery) q).getTerm());

        } else if (q instanceof WildcardQuery) {
            terms.add(((WildcardQuery) q).getTerm());

        } else if (q instanceof PhraseQuery) {
            PhraseQuery pq = (PhraseQuery) q;
            String s = pq.toString(null);
            int i = s.indexOf('"');
            if (i == 0) {
                terms.add(new Term(FIELD_CONTENT, s));
            } else {
                terms.add(new Term(s.substring(0, i - 1), s.substring(i)));
            }

        } else if (q instanceof MultiPhraseQuery) {
            ((MultiPhraseQuery) q).extractTerms(terms);

        } else if (q instanceof PrefixQuery) {
            Term t = ((PrefixQuery) q).getPrefix();
            terms.add(new Term(t.field(), t.text() + "*"));

        } else if (q instanceof FuzzyQuery) {
            FuzzyQuery fq = (FuzzyQuery) q;
            try {
                q = fq.rewrite(r);
            } catch (Exception e) {
                LOGGER.warn("Error rewriting fuzzy query [" + fq + "]: " + e);
            }
            extractTermsFromQuery(q, r, terms);

        } else if (q instanceof BooleanQuery) {
            for (BooleanClause clause : ((BooleanQuery) q).getClauses()) {
                if (clause.getOccur() != BooleanClause.Occur.MUST_NOT) {
                    extractTermsFromQuery(clause.getQuery(), r, terms);
                }
            }

        } else {
            try {
                q.extractTerms(terms);
            } catch (Exception e) {
                LOGGER.warn("Caught exception trying to extract terms from query ["
                    + q + "]: ", e);
            }
        }
        return terms;
    }
}
