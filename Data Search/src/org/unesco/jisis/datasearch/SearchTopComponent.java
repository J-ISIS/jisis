package org.unesco.jisis.datasearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Timer;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.*;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;
import org.unesco.jisis.corelib.common.FieldSelectionTable.FstEntry;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.DefaultDBNotFoundException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.corelib.exceptions.ResourceNotFoundException;
import org.unesco.jisis.corelib.index.DictionaryTerm;
import org.unesco.jisis.corelib.index.DocBuilder;
import org.unesco.jisis.corelib.index.HighlightTermsInRecord;
import org.unesco.jisis.corelib.index.QueryTerm;
import org.unesco.jisis.corelib.index.SearchableField;
import org.unesco.jisis.corelib.index.TermParams;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.util.StringUtils;
import org.unesco.jisis.fxbrowser.SwingFXWebView;
import org.unesco.jisis.jisisutil.history.HistoryTextArea;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
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
 *
 * @author  sphilips
 */
public class SearchTopComponent extends TopComponent implements ListSelectionListener, Observer {

   private static SearchTopComponent instance;
   /** path to the icon used by the component and its open action */
   static final String ICON_PATH = "org/unesco/jisis/datasearch/browse.png";
   private static final String PREFERRED_ID = "srchTopComponent";
   private static final String[] queryType_ = new String[]{
      NbBundle.getMessage(SearchTopComponent.class, "matches"),
      NbBundle.getMessage(SearchTopComponent.class, "matches-not")
   //NbBundle.getMessage(srchTopComponent.class, "is"),
   //NbBundle.getMessage(srchTopComponent.class, "is-not"),
   //NbBundle.getMessage(srchTopComponent.class, "begins-with"),
   //NbBundle.getMessage(srchTopComponent.class, "ends-with")
   };
   private ClientDatabaseProxy db_ = null;
   private SearchableField[] searchableFields_ = null;
   private ArrayList<JComponent[]> queryPanels_ = new ArrayList<>();
   private Boolean isGuidedSearch_;
   private Timer suggestionTimer_ = null;
   private TimerTask suggestionTimerTask_ = null;
   private FieldDefinitionTable fdt_;
   private FieldSelectionTable fst_;
   private long[] results_ = null;
   private long[] sortedResults_ = null;

   private List<SearchComponent> searchComponents_;

   private FormattedRecord currentFormattedRecord_ = null;
   //private JWebBrowser webBrowser_ = null;
   private String selectedPftName_ = null;
   private List<String> searchTerms_ = new ArrayList<>();

   protected int termSorting_         = SuggestionsOptionDialog.SORT_TERM_FIELD_FREQ;
   protected int termCaseSensitivity_ = SuggestionsOptionDialog.TERM_CASE_INSENSITIVE;
   
  
   private SwingFXWebView swingFXWebView = null;
   
   private HighlightTermsInRecord highlightTermsInRecord_;
   
 
   List<QueryTerm> queryTerms_ = new ArrayList<>();
   
   private int mfnListPageNumber_ = 0;
   private int mfnListItemsPerPage_ = 100;
   
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SearchTopComponent.class);

   /** Creates new form srchTopComponent
     * @param db */
   public SearchTopComponent(IDatabase db) {
      try {
         if (db instanceof ClientDatabaseProxy) {
            db_ = (ClientDatabaseProxy) db;
         } else {
            throw new RuntimeException("SearchTopComponent: Cannot cast DB to ClientDatabaseProxy");
         }
         /* Register this TopComponent as attached to this DB */
         db_.addWindow(this);
         /* Add this TopComponent as Observer to DB changes */
         db_.addObserver((Observer) this);
         
         initComponents();

         fdt_ = db_.getFieldDefinitionTable();
         fst_ = db_.getFieldSelectionTable();

         suggestionTimerTask_ = null;
         suggestionTimer_ = new Timer();

         java.awt.GridBagConstraints gridBagConstraints;
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;

         suggestionBox.setLayout(new java.awt.GridBagLayout());
         suggestionBox.add(this.suggestionScroll, gridBagConstraints);
         suggestionList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
         suggestionList.setModel(new DefaultListModel());

         /**
          * Mac problem if the JPopUpMenu has not been set to unFocusable
          * Otherwise the 1st letter of the txtQuery is selected and the cursor
          * is not set after, thus typing a second character overrides the
          * previous one
          */
         suggestionBox.setFocusable(false);

         if (db_.getDisplayFont() != null) {
            suggestionList.setFont(db_.getDisplayFont());
         }

         //setName(NbBundle.getMessage(srchTopComponent.class, "CTL_srchTopComponent"));
         try {
            setName(NbBundle.getMessage(SearchTopComponent.class, "CTL_srchTopComponent")
                    + " (" + db.getDbHome() + "//" + db.getDatabaseName() + ")");
         } catch (MissingResourceException ex) {
            new ResourceNotFoundException(ex).displayWarning();
         } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
         }
         setToolTipText(NbBundle.getMessage(SearchTopComponent.class, "HINT_srchTopComponent"));
         setIcon(ImageUtilities.loadImage(ICON_PATH, true));


         recordPanel.setPreferredSize(new Dimension(700, 500));

         if (swingFXWebView == null) {
            swingFXWebView = new SwingFXWebView();
            recordPanel.add(swingFXWebView, BorderLayout.CENTER);
         }

         highlightTermsInRecord_ = new HighlightTermsInRecord(fst_);

         populateFields();
         setGuidedSearch(true);
      } catch (RuntimeException | DbException ex) {
         Exceptions.printStackTrace(ex);
      }
   }
   
    

   private void populateFields() {
      try {
         // The searchable fields
         HashSet<Integer> tags = new HashSet<>();
         ArrayList<SearchableField> searchableFields = new ArrayList<>();
         searchableFields.add(new SearchableField("<All Searchable Fields>", -1));

         for (int i = 0; i < this.fst_.getEntriesCount(); i++) {
            FstEntry entry = this.fst_.getEntryByIndex(i);
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

         // The output formats
         cmbPftSelect.setModel(new DefaultComboBoxModel(db_.getPrintFormatNames()));
         cmbPftSelect.getModel().setSelectedItem("RAW");
         selectedPftName_ = (String) cmbPftSelect.getSelectedItem();

         // The record Display Fields
         SearchableField[] displayfields = new SearchableField[fdt_.getFieldsCount() + 1];
         displayfields[0] = new SearchableField("MFN", -1);

         for (int i = 0; i < displayfields.length - 1; i++) {
            FieldDefinition field = fdt_.getFieldByIndex(i);
            displayfields[i + 1] = new SearchableField(field.getName(), field.getTag());
         }

         titleBox.setModel(new DefaultComboBoxModel(displayfields));

      } catch (DbException ex) {
         ex.printStackTrace();
      }
   }

   private void setGuidedSearch(boolean show) {
      if (show) {
         if (isGuidedSearch_ == null || isGuidedSearch_ == false) {
            termsPanel.removeAll();
            queryPanels_.clear();
            addTermPanel();
            enterQueryLabel.setVisible(false);
            matchAllRadio.setVisible(true);
            matchAnyRadio.setVisible(true);
         }
      } else {
         if (this.isGuidedSearch_ == null || this.isGuidedSearch_ == true) {
            queryPanels_.clear();
            termsPanel.removeAll();
         }

         HistoryTextArea  textarea = new HistoryTextArea(db_.getDbName()+"_expertSearch");
         javax.swing.JScrollPane scrollpane = new javax.swing.JScrollPane();
         scrollpane.setViewportView(textarea);
         termsPanel.add(scrollpane);
         queryPanels_.add(new JComponent[]{textarea});

         enterQueryLabel.setVisible(true);
         matchAllRadio.setVisible(false);
         matchAnyRadio.setVisible(false);
      }

      termsScrollPane.validate();
      termsScrollPane.paint(this.termsScrollPane.getGraphics());
      isGuidedSearch_ = show;
   }

    private String[] buildAllIndexFieldNames() {
        HashSet<Integer> tags = new HashSet<>();
        List<String> fieldNames = new ArrayList<>();
        for (int i = 0; i < fst_.getEntriesCount(); i++) {
            FstEntry entry = fst_.getEntryByIndex(i);
            int tag = entry.getTag();
            if (!tags.add(tag)) {
                continue;
            }
            String name = entry.getName();
            String fieldName = "_" + tag;
            if (name != null && name.length() > 0) {
                fieldName = name;
            }
            fieldNames.add(fieldName);
        }
        String[] fld = new String[fieldNames.size()];
        fieldNames.toArray(fld);
        return fld;
    }
    private String[] buildIndexFieldName(int fstTag) {
        String fieldName = null;
        for (int i = 0; i < fst_.getEntriesCount(); i++) {
            FstEntry entry = fst_.getEntryByIndex(i);
            int tag = entry.getTag();
            if (tag == fstTag) {
                String name = entry.getName();
                fieldName = "_" + tag;
                if (name != null && name.length() > 0) {
                    fieldName = name;
                }
                break;
            }
        }
        String[] fieldNames = new String[1];
        fieldNames[0] = fieldName;
        return fieldNames;
    }
   // <editor-fold defaultstate="collapsed" desc="Guided search">
   private void addTermPanel() {
      addTermPanel(this.queryPanels_.size());
   }

   private void addTermPanel(int index) {
      final javax.swing.JComboBox cmbFstEntry;
      javax.swing.JComboBox cmbQueryType;
      javax.swing.JButton btnPlus;
      javax.swing.JButton btnMinus;
      javax.swing.JTextField txtQuery;
      javax.swing.JPanel termPanel;

      // Simple Query Panel
      termPanel = new javax.swing.JPanel();
      // Combo for selecting FST entry
      cmbFstEntry = new javax.swing.JComboBox();
      // Combo for selecting type of Query
      cmbQueryType = new javax.swing.JComboBox();
      // Text field to enter query terms
      txtQuery = new javax.swing.JTextField();
      if (db_.getDisplayFont()!=null) {
             txtQuery.setFont(db_.getDisplayFont());
      }
      // Button for adding a new Query Panel
      btnPlus = new javax.swing.JButton();
      // Button to delete the query panel
      btnMinus = new javax.swing.JButton();

      termPanel.setOpaque(false);

      // FST entry ID or ALL
      if (this.searchableFields_ != null) {
         cmbFstEntry.setModel(new javax.swing.DefaultComboBoxModel(searchableFields_));
      }

      // Matching, Not Matching
      cmbQueryType.setModel(new javax.swing.DefaultComboBoxModel(queryType_));

      txtQuery.setText("");
      btnPlus.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "plus")); // NOI18N
      btnMinus.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "minus")); // NOI18N

      javax.swing.GroupLayout termPanelLayout = new javax.swing.GroupLayout(termPanel);
      termPanel.setLayout(termPanelLayout);
      termPanelLayout.setHorizontalGroup(
              termPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(termPanelLayout.createSequentialGroup().addContainerGap()
              .addComponent(cmbFstEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(cmbQueryType, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
              .addComponent(txtQuery, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
              .addComponent(btnPlus).addGap(2, 2, 2).addComponent(btnMinus).addContainerGap()));
      termPanelLayout.setVerticalGroup(
              termPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(termPanelLayout.createSequentialGroup().addGap(5)
              .addGroup(termPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(cmbFstEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(cmbQueryType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(btnPlus).addComponent(btnMinus)
              .addComponent(txtQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))) //.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              );

      // Event handlers
      btnPlus.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            plusClicked(evt);
         }
      });

      btnMinus.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            minusClicked(evt);
         }
      });

      cmbFstEntry.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            searchableFieldClicked(evt);
         }

         private void searchableFieldClicked(ActionEvent evt) {
            SearchableField searchableField = (SearchableField) cmbFstEntry.getSelectedItem();
            if (searchableField != null) {
               searchableFieldChanged(evt, searchableField);
            }
         }
      });

      txtQuery.addActionListener(new java.awt.event.ActionListener() {

         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             requestFocusInWindow();
            queryFieldEnter(evt);
         }
      });

      txtQuery.addKeyListener(new java.awt.event.KeyAdapter() {

         @Override
         public void keyPressed(java.awt.event.KeyEvent evt) {
            requestFocusInWindow();
            queryFieldPressed(evt);
         }
      });

      txtQuery.addKeyListener(new java.awt.event.KeyAdapter() {

         @Override
         public void keyTyped(java.awt.event.KeyEvent evt) {
             requestFocusInWindow();
            queryFieldTyped(evt);
         }
      });
      
      txtQuery.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            JTextField tf = (JTextField) e.getSource();
            tf.requestFocusInWindow();


            System.out.println("Mouse Clicked text field content="+tf.getText());
         }
      });

      // Register and add to the panel.
      JComponent[] queryPanel = {termPanel, cmbFstEntry, cmbQueryType,
                                 txtQuery, btnPlus, btnMinus};

      this.queryPanels_.add(index, queryPanel);
      if (this.queryPanels_.size() == 1) {
         btnMinus.setEnabled(false);
      } else {
         for (JComponent[] qp : this.queryPanels_) {
            qp[5].setEnabled(true);
         }
      }


      this.termsPanel.add(termPanel, index);
      //this.termsPanel.validate();
      this.termsScrollPane.validate();

      this.termsScrollPane.scrollRectToVisible(this.termsPanel.getVisibleRect());
   }

   private void removeTermPanel(int index) {
      if (this.queryPanels_.size() == 1) {
         return;
      }

      this.termsPanel.remove(this.queryPanels_.get(index)[0]);
      this.termsScrollPane.revalidate();
      this.termsScrollPane.update(this.termsScrollPane.getGraphics());

      this.queryPanels_.remove(index);
      if (this.queryPanels_.size() == 1) {
         this.queryPanels_.get(0)[5].setEnabled(false);
      }
   }

   private void plusClicked(java.awt.event.ActionEvent evt) {
      int index = 0;
      for (JComponent[] fields : this.queryPanels_) {
         if (evt.getSource() == fields[4]) {
            this.addTermPanel(index + 1);
            return;
         }
         index += 1;
      }
   }

   private void minusClicked(java.awt.event.ActionEvent evt) {
      int index = 0;
      for (JComponent[] fields : this.queryPanels_) {
         if (evt.getSource() == fields[5]) {
            this.removeTermPanel(index);
            return;
         }
         index += 1;
      }
   }

   private void queryFieldEnter(java.awt.event.ActionEvent evt) {
      System.out.println("Entered.");
      if (this.suggestionBox.isShowing()) {
         this.suggestionBox.setVisible(false);
      }
   //evt.consume();
   }

   private void queryFieldPressed(java.awt.event.KeyEvent evt) {
      int index;

      if (this.suggestionTimerTask_ != null) {
         this.suggestionTimerTask_.cancel();
      }

      if (this.suggestionBox.isShowing()) {
         if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE || evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            this.suggestionBox.setVisible(false);
            return;
         } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            index = this.suggestionList.getSelectedIndex() + 1;
            this.suggestionList.setSelectedIndex(index >= 0 ? index : 0);
            this.suggestionList.ensureIndexIsVisible(index >= 0 ? index : 0);
            return;
         } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            index = this.suggestionList.getSelectedIndex() - 1;
            this.suggestionList.setSelectedIndex(index >= 0 ? index : 0);
            this.suggestionList.ensureIndexIsVisible(index >= 0 ? index : 0);
            return;
         } else {
         }
      }

   }

   private void queryFieldTyped(java.awt.event.KeyEvent evt) {
      if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
         // update suggestions
      } else if (!Pattern.matches("(\\p{L}|\\p{N}|=|:|\\-)", String.valueOf(evt.getKeyChar()))) {
         return;
      }

      int index = 0;
      for (index = 0; index < this.queryPanels_.size(); index += 1) {
         if (this.queryPanels_.get(index)[3] == evt.getSource()) {
            break;
         }
      }

      /* Something was typed */
      if (suggestionTimerTask_ != null) {
         suggestionTimerTask_.cancel();
      }

      showRetrievingTerms();
      suggestionTimerTask_ = new SuggestionTimerTask(this, (JComboBox) this.queryPanels_.get(index)[1],
              (JTextComponent) this.queryPanels_.get(index)[3]);
      this.suggestionTimer_.schedule(this.suggestionTimerTask_, 500);
   }
   /**
    * The Searchable field has changed
    * @param evt
    * @param searchableField
    */
   private void searchableFieldChanged(ActionEvent evt, SearchableField searchableField) {
       int index = 0;
      for (index = 0; index < this.queryPanels_.size(); index += 1) {
         if (this.queryPanels_.get(index)[1] == evt.getSource()) {
            break;
         }
      }
        if (suggestionTimerTask_ != null) {
         suggestionTimerTask_.cancel();
      }

      showRetrievingTerms();
      suggestionTimerTask_ = new SuggestionTimerTask(this, (JComboBox) this.queryPanels_.get(index)[1],
              (JTextComponent) this.queryPanels_.get(index)[3]);
      suggestionTimer_.schedule(this.suggestionTimerTask_, 500);
   }
   // </editor-fold>

   public void showRetrievingTerms() {
      // Set status text
      final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
      StatusDisplayer.getDefault().setStatusText("Retrieving Dictionary term suggestions ...");
      RepaintManager.currentManager(frame).paintDirtyRegions();

   }
   /**
    *
    * @param field - Searchable Field Tag or -1 for all
    * @param value - Term Prefix
    * @return
    */
   public boolean populateDictionary(int field, String value) {
      try {
         value = value.trim(); //.toLowerCase();
         value = org.apache.commons.lang3.StringUtils.stripAccents(value);
         value = org.apache.commons.lang3.StringUtils.upperCase(value);
         if (this.suggestionList == null || value.length() < 1) {
            return false;
         }
         suggestionList.clearSelection();
         DefaultListModel model = (DefaultListModel) suggestionList.getModel();
         model.clear();

         // Populate the dictionary
         String[] fieldNames;
         if (field == -1) {
            fieldNames = buildAllIndexFieldNames();
         } else {
            fieldNames = buildIndexFieldName(field);
         }
         ModifiableJisisParams params = new ModifiableJisisParams();
         params.add(TermParams.TERMS, "true");
         params.add(TermParams.TERMS_FIELD, fieldNames);
         //params.add(TermParams.TERMS_LOWER,  prefix);
         params.add(TermParams.TERMS_LOWER_INCLUSIVE, "true");
         //params.add(TermParams.TERMS_PREFIX_STR, prefix);
         params.add(TermParams.TERMS_REGEXP_STR, value + ".*");
        
         
         if (termCaseSensitivity_ == SuggestionsOptionDialog.TERM_CASE_INSENSITIVE) {
            
            params.add(TermParams.TERMS_REGEXP_FLAG, "case_insensitive","unicode_case");
         } else {
            params.add(TermParams.TERMS_REGEXP_FLAG, "unicode_case");
         }

         

         if (termSorting_ == SuggestionsOptionDialog.SORT_TERM_FIELD_FREQ) {
            params.add(TermParams.TERMS_SORT, TermParams.TERMS_SORT_INDEX);
         } else {
             params.add(TermParams.TERMS_SORT, TermParams.TERMS_SORT_COUNT);
         }
         //no lower bound, upper bound or rows
         int maxTerms = 200;
         params.add(TermParams.TERMS_LIMIT, String.valueOf(maxTerms));
         //List<DictionaryTerm> v = db_.getTermSuggestions(value,fieldNames, 100);
         List<DictionaryTerm> list = db_.getTermSuggestions(params);
         if (list == null) {
            return false;
         }
         for (DictionaryTerm term : list) {
            if (value.length() < 3 && model.size() == 700) {
               break;
            }
            model.addElement(term.getField() + "[" + term.getFreq() + "]  \t" + term.getText());
         }
         suggestionList.setModel(model);
         return true;
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
      return false;
   }

    public void showDictionary(final JComponent parent) {
        if (!suggestionBox.isShowing()) {
            suggestionBox.setPopupSize(parent.getSize().width, 200);
            suggestionBox.show(parent, 0, parent.getSize().height);
            suggestionBox.setFocusable(false);
            parent.setFocusable(true);
            parent.requestFocusInWindow();
            
            
        }
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
   public void displayRecord(IRecord record, FormattedRecord formattedRecord) {

      if (formattedRecord == null) {
         return;
      }
      currentFormattedRecord_ = formattedRecord;
      

      String fmtName = (String) this.cmbPftSelect.getSelectedItem();
      String content = formattedRecord.getRecord();
      // Hilight search terms 
      try {
         String pft = db_.getPrintFormat(fmtName);
         if (fmtName.equals("RAW") && pft.equals("")) {
            content = highlightTermsInRecord_.highlightRaw(db_, record, content, queryTerms_);
         } else {
            content = highlightTermsInRecord_.highlight(db_, record, content, queryTerms_);
         }
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
     

      final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
      StatusDisplayer.getDefault().setStatusText("Starting browser display ...");
      RepaintManager.currentManager(frame).paintDirtyRegions();
      
      doDisplayRecord(content);
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
        Document document = Jsoup.parse(htmlContent);

        Elements occurrences = document.select("td:nth-child(2)"); // Field/Occurrence column

        for (Element occurrence : occurrences) {
            String occurrenceHtml = occurrence.html();
            for (String s : textToHilight) {

               // The term is fully upper case, thus do case insensitive
                // replacement for case insensitive highlighting
                String escaped = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(s);
                occurrenceHtml
                    = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(occurrenceHtml);
                occurrenceHtml
                    = StringUtils.hilightNormalized(occurrenceHtml, escaped);

            }
            occurrence.html(occurrenceHtml);
        }
        String s = document.html();
        return s;

    }
   
   private String highlightTermsHTML(String htmlContent, List<String> textToHilight) {
      Document document = Jsoup.parse(htmlContent);

      Elements elements = document.body().select("*");

      for (Element element : elements) {
         String text = element.ownText();
         if (text == null || text.length() == 0) {
            continue;
         }
         for (String s : textToHilight) {
             
               // The term is fully upper case, thus do case insensitive
               // replacement for case insensitive highlighting
               String escaped = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(s);
               text = 
                       org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(text);
               text =
                       StringUtils.hilightNormalized(text, escaped);                                  
        }
         element.html(text);
      }
      String s = document.html();
      return s;
   }

  
   private static File temp = null;
   private URL makeTempFile(String content) {
      if (temp == null) {
         try {
            // Create temp file.
            temp = File.createTempFile("pattern", ".suffix", new File(Global.getClientTempPath()));
            System.out.println("Temp creation Record url="+temp.toString());
         } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
         }
         // Delete temp file when program exits.
         temp.deleteOnExit();
      }
      try {
         
         // Write to temp file
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp),
                         "UTF8"));
        
         out.write(content);
         out.close();
      } catch (IOException e) {
      }
      URL url = null;
      try {
         url = temp.toURI().toURL();
      } catch (MalformedURLException ex) {
         Exceptions.printStackTrace(ex);
      }
      System.out.println("Record url="+url.toString());
      return url;
   }

   /**
    * Build the overall Lucene query
    * @param key - FST entry tag or FST entry name if present
    * @param option - "matching" or "not matching"
    * @param value  - query terms
    * @return 
    */
    private String buildQuery(String key, String option, String value) {
        value = buildExpr(Integer.parseInt(key), value, false);

        if (option.equals(SearchTopComponent.queryType_[1])) { // Not contains
            value = "-(" + value + ")";
        }

        return value;
    }

    /**
     * Build the Lucene query element for this FST entry
     * 
     * title:"The Right Way" 
     *  _24:"The Right Way" 
     * 
     * @param field - FST entry tag or -1 meaning all fields
     * @param text - The text to search for
     * @param wildcard - true if we should append the wildcard char
     * @return - The Lucene fielded search query between parentheses
     *  (_24:"The Right Way")
     *  (title:"The Right Way") 
     * 
     * Note: Lucene supports escaping special characters that are part of the 
     * query syntax. The current list special characters are
     * 
     *     + - && || ! ( ) { } [ ] ^ " ~ * ? : \
     */
   private String buildExpr(int field, String text, boolean wildcard) {
      text = text.trim();
      /**
       * Remove diacritics (~= accents) from a string. The case will not be altered.
       * For instance, 'à' will be replaced by 'a'.
       * Note that ligatures will be left as is.
       * StringUtils.stripAccents(null)                = null
       * StringUtils.stripAccents("")                  = ""
       * StringUtils.stripAccents("control")           = "control"
       * StringUtils.stripAccents("éclair")     = "eclair"
       **/
       text = org.apache.commons.lang3.StringUtils.stripAccents(text);
       /**
        * all elements generated by the Inverted file FST will be translated to 
        * upper case before they are stored in the dictionary, even when the 
        * FST produces them in lower cas
        */
       text = org.apache.commons.lang3.StringUtils.upperCase(text);
      if (text.length() > 0) {
         text = QueryParser.escape(text);
         if (wildcard) {
            text += '*';
         } else {
            text = "\"" + text + "\"";
         }
      }
      StringBuilder buffer = new StringBuilder();
      if (field < 0) {   // Search in all fields
         /**
          * The OR operator is the default conjunction operator. This means that
          * if there is no Boolean operator between two terms, the OR operator 
          * is used. The OR operator links two terms and finds a matching 
          * document if either of the terms exist in a document. 
          */
         
         for (SearchableField fieldName : this.searchableFields_) {
            if (fieldName.tag == -1) {
               continue;
            }
            /**
             * field name followed by a colon ":" and then the term we are looking for
             */
            String fn = (fieldName.name != null && fieldName.name.length()>0) 
                    ? fieldName.name : ("_"+fieldName.tag);
            buffer.append(fn).append(":").append(text).append("\n");
         }
        
      } else { // Search in a particular field

         String fn = "_" + field;
         for (SearchableField fieldName : this.searchableFields_) {
            if (fieldName.tag == field) {
               if (fieldName.name != null && fieldName.name.length() > 0) {
                  fn = fieldName.name;
               }
               break;
            }
         }
         buffer.append(fn).append(":").append(text).append("\n");
      }
       return "(" + buffer.toString() + ")";
   }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        matchGroup = new javax.swing.ButtonGroup();
        suggestionBox = new javax.swing.JPopupMenu();
        suggestionScroll = new javax.swing.JScrollPane();
        suggestionList = new javax.swing.JList();
        verticalSplitPane = new javax.swing.JSplitPane();
        resultsPanel = new javax.swing.JPanel();
        horizontalSplitPane = new javax.swing.JSplitPane();
        indexPanel = new javax.swing.JPanel();
        recordLabel = new javax.swing.JLabel();
        mfnScrollPane = new javax.swing.JScrollPane();
        mfnList = new javax.swing.JList();
        titleBox = new javax.swing.JComboBox();
        btnPgUp = new javax.swing.JButton();
        btnPgDn = new javax.swing.JButton();
        chkbSortByMfn = new javax.swing.JCheckBox();
        outputPanel = new javax.swing.JPanel();
        formatPanel = new javax.swing.JPanel();
        formatLabel = new javax.swing.JLabel();
        cmbPftSelect = new javax.swing.JComboBox();
        recordPanel = new javax.swing.JPanel();
        inputPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        enterQueryLabel = new javax.swing.JLabel();
        matchAllRadio = new javax.swing.JRadioButton();
        matchAnyRadio = new javax.swing.JRadioButton();
        btnDictionaryOptions = new javax.swing.JButton();
        guidedBox = new javax.swing.JCheckBox();
        termsScrollPane = new javax.swing.JScrollPane();
        termsPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        btnSearch = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();

        suggestionScroll.setComponentPopupMenu(suggestionBox);

        suggestionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        suggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                suggestionListMouseClicked(evt);
            }
        });
        suggestionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                suggestionListValueChanged(evt);
            }
        });
        suggestionScroll.setViewportView(suggestionList);

        verticalSplitPane.setDividerLocation(200);
        verticalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        verticalSplitPane.setResizeWeight(0.2);

        horizontalSplitPane.setDividerLocation(240);

        recordLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        recordLabel.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "results")); // NOI18N
        recordLabel.setMinimumSize(new java.awt.Dimension(27, 24));
        recordLabel.setPreferredSize(new java.awt.Dimension(27, 24));

        mfnScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        mfnList.setModel(new DefaultListModel());
        mfnList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                mfnListValueChanged(evt);
            }
        });
        mfnScrollPane.setViewportView(mfnList);

        titleBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                titleBoxActionPerformed(evt);
            }
        });

        btnPgUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/datasearch/2uparrow16.png"))); // NOI18N
        btnPgUp.setMnemonic('U');
        btnPgUp.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "SearchTopComponent.btnPgUp.text")); // NOI18N
        btnPgUp.setToolTipText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "SearchTopComponent.btnPgUp.toolTipText")); // NOI18N
        btnPgUp.setEnabled(false);
        btnPgUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPgUpActionPerformed(evt);
            }
        });

        btnPgDn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/datasearch/2downarrow16.png"))); // NOI18N
        btnPgDn.setMnemonic('D');
        btnPgDn.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "SearchTopComponent.btnPgDn.text")); // NOI18N
        btnPgDn.setEnabled(false);
        btnPgDn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPgDnActionPerformed(evt);
            }
        });

        chkbSortByMfn.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "SearchTopComponent.chkbSortByMfn.text")); // NOI18N
        chkbSortByMfn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkbSortByMfnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout indexPanelLayout = new javax.swing.GroupLayout(indexPanel);
        indexPanel.setLayout(indexPanelLayout);
        indexPanelLayout.setHorizontalGroup(
            indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(indexPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(indexPanelLayout.createSequentialGroup()
                        .addComponent(mfnScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(indexPanelLayout.createSequentialGroup()
                        .addGroup(indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(titleBox, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(indexPanelLayout.createSequentialGroup()
                                .addComponent(btnPgUp)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnPgDn)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(indexPanelLayout.createSequentialGroup()
                        .addComponent(recordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                        .addComponent(chkbSortByMfn, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        indexPanelLayout.setVerticalGroup(
            indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(indexPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(recordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkbSortByMfn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(titleBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mfnScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPgUp)
                    .addComponent(btnPgDn))
                .addContainerGap())
        );

        horizontalSplitPane.setLeftComponent(indexPanel);

        outputPanel.setLayout(new java.awt.BorderLayout());

        formatLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        formatLabel.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "format")); // NOI18N

        cmbPftSelect.setLightWeightPopupEnabled(false);
        cmbPftSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPftSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout formatPanelLayout = new javax.swing.GroupLayout(formatPanel);
        formatPanel.setLayout(formatPanelLayout);
        formatPanelLayout.setHorizontalGroup(
            formatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formatPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(formatLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbPftSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(513, Short.MAX_VALUE))
        );
        formatPanelLayout.setVerticalGroup(
            formatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formatPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbPftSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(formatLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        outputPanel.add(formatPanel, java.awt.BorderLayout.NORTH);

        recordPanel.setLayout(new java.awt.BorderLayout());
        outputPanel.add(recordPanel, java.awt.BorderLayout.CENTER);

        horizontalSplitPane.setRightComponent(outputPanel);

        javax.swing.GroupLayout resultsPanelLayout = new javax.swing.GroupLayout(resultsPanel);
        resultsPanel.setLayout(resultsPanelLayout);
        resultsPanelLayout.setHorizontalGroup(
            resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(horizontalSplitPane))
        );
        resultsPanelLayout.setVerticalGroup(
            resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsPanelLayout.createSequentialGroup()
                .addComponent(horizontalSplitPane)
                .addContainerGap())
        );

        verticalSplitPane.setBottomComponent(resultsPanel);

        inputPanel.setLayout(new java.awt.GridBagLayout());

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        enterQueryLabel.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "enter-query")); // NOI18N
        jPanel2.add(enterQueryLabel);

        matchGroup.add(matchAllRadio);
        matchAllRadio.setSelected(true);
        matchAllRadio.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "match-all")); // NOI18N
        jPanel2.add(matchAllRadio);

        matchGroup.add(matchAnyRadio);
        matchAnyRadio.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "match-any")); // NOI18N
        jPanel2.add(matchAnyRadio);

        btnDictionaryOptions.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "SearchTopComponent.btnDictionaryOptions.text")); // NOI18N
        btnDictionaryOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDictionaryOptionsActionPerformed(evt);
            }
        });
        jPanel2.add(btnDictionaryOptions);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(jPanel2, gridBagConstraints);

        guidedBox.setSelected(true);
        guidedBox.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "guided-search")); // NOI18N
        guidedBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guidedBoxActionPerformed(evt);
            }
        });
        optionsPanel.add(guidedBox, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        inputPanel.add(optionsPanel, gridBagConstraints);

        termsPanel.setBackground(java.awt.SystemColor.text);
        termsPanel.setLayout(new javax.swing.BoxLayout(termsPanel, javax.swing.BoxLayout.Y_AXIS));
        termsScrollPane.setViewportView(termsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        inputPanel.add(termsScrollPane, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        btnSearch.setMnemonic('S');
        btnSearch.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "BTN_SEARCH")); // NOI18N
        btnSearch.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        buttonsPanel.add(btnSearch, gridBagConstraints);

        btnClear.setMnemonic('c');
        btnClear.setText(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "clear")); // NOI18N
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        buttonsPanel.add(btnClear, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        inputPanel.add(buttonsPanel, gridBagConstraints);

        verticalSplitPane.setTopComponent(inputPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(verticalSplitPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(verticalSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private void guidedBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guidedBoxActionPerformed
       this.setGuidedSearch(this.guidedBox.isSelected());
}//GEN-LAST:event_guidedBoxActionPerformed

    /*------------------------------------
     * Search Action
     *------------------------------------
     */
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
       // JComponent[] fields = {termPanel, keywordBox, optionBox, queryField, plusButton, minusButton};

       String squery = "";
       String userQuery = "";
       StringBuilder buffer = new StringBuilder();
       StringBuilder sb = new StringBuilder(); // Query as entered by user
       
       
 
       if (this.isGuidedSearch_) {
          searchComponents_ = new ArrayList<>();
          for (JComponent[] fields : this.queryPanels_) {
             // The field tag
             String tag = "" + ((SearchableField) ((javax.swing.JComboBox) fields[1]).getSelectedItem()).tag;
             String option = (String) ((javax.swing.JComboBox) fields[2]).getSelectedItem();
             String value = ((javax.swing.text.JTextComponent) fields[3]).getText();

             if (value.equals("")) {
                continue;
             }
             value = org.apache.commons.lang3.StringUtils.stripAccents(value);
             /**
              * all elements generated by the Inverted file FST will be
              * translated to upper case before they are stored in the
              * dictionary, even when the FST produces them in lower cas
              */
             value = org.apache.commons.lang3.StringUtils.upperCase(value);
             searchComponents_.add(new SearchComponent(tag, option, value));
          }
          /**
           * Build the overall query
           */
          String join = this.matchAllRadio.isSelected() ? " AND " : " OR  ";
          for (JComponent[] fields : this.queryPanels_) {
             // The field tag
             String key = "" + ((SearchableField) ((javax.swing.JComboBox) fields[1]).getSelectedItem()).tag;
             String option = (String) ((javax.swing.JComboBox) fields[2]).getSelectedItem();
             String value = ((javax.swing.text.JTextComponent) fields[3]).getText();

             if (value.equals("")) {
                continue;
             }
             sb.append(key.equalsIgnoreCase("-1") ? "Search All Fields"
                     : "Search Field " + key);
             sb.append(" ").append(option).append(" ");
             sb.append(value);
             sb.append(join);

             squery = this.buildQuery(key, option, value);
             buffer.append(squery);
             buffer.append(join);
          }

          squery = buffer.toString();
          if (squery.endsWith(" AND ") || squery.endsWith(" OR  ")) {
             squery = squery.substring(0, squery.length() - 5);
          }
          userQuery = sb.toString();
          if (userQuery.endsWith(" AND ") || userQuery.endsWith(" OR  ")) {
             userQuery = userQuery.substring(0, userQuery.length() - 5);
          }
          // Build the list of search terms to highlight in the html
          
          String defaultField = "_" + searchableFields_[1].tag;
          if (searchableFields_[1].name != null && searchableFields_[1].name.length() > 0) {
             defaultField = searchableFields_[1].name;
          }
          MyQueryParser parser;
          parser = new MyQueryParser(Lucene.MATCH_VERSION,defaultField, new KeywordAnalyzer());
          Query query = null;
          try {
             query = parser.parse(squery);
          } catch (ParseException ex) {
             LOGGER.error("Query Parsing Exception", ex);
             JOptionPane.showMessageDialog(this, "A syntax error occurred in the query!", "Syntax Error", JOptionPane.WARNING_MESSAGE);
             return;
          }
          /**
           * Both prefix and wildcard queries are lowercased by default, but
           * this behavior can be controlled:
           */
          parser.setLowercaseExpandedTerms(false);
          queryTerms_ = parser.getQueryTerms();    

       } else {
           // Expert Lucene Search
        
          squery = ((javax.swing.text.JTextComponent) this.queryPanels_.get(0)[0]).getText();
          /**
           * Get rid of newlines in case user pressed enter
           */
          squery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(squery, "\n", " ");

          squery = squery.trim();
          userQuery = squery;
          HistoryTextArea textArea = ((HistoryTextArea) this.queryPanels_.get(0)[0]);
          textArea.addCurrentToHistory();
          /**
           * Boolean operators allow terms to be combined through logic
           * operators. Lucene supports AND, "+", OR, NOT and "-" as Boolean
           * operators (Note: Boolean operators must be ALL CAPS).
           *
           *
           */
//         squery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(squery, " and ", " AND ");
//         squery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(squery, " or ", " OR ");
//         squery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(squery, " not ", " NOT ");
          /**
           * searchableFields_[0] is the <All Fields> thus the default Lucene
           * field is provided by searchableFields_[1]
           */
          String defaultField = "_" + searchableFields_[1].tag;
          if (searchableFields_[1].name != null && searchableFields_[1].name.length() > 0) {
             defaultField = searchableFields_[1].name;
          }
          // We escape the wildcard characters to avoid an exception and to get
          // it as part of the term.

          String escapedQuery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(squery, "*", "\\*");
          escapedQuery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(escapedQuery, "?", "\\?");
          escapedQuery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(escapedQuery, "~", "\\~");
          escapedQuery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(escapedQuery, "^", "\\^");
          MyQueryParser parser;
          parser = new MyQueryParser(Lucene.MATCH_VERSION,defaultField, new KeywordAnalyzer());
          Query query = null;
          try {
             query = parser.parse(escapedQuery);
          } catch (ParseException ex) {
             LOGGER.error("Query Parsing Exception", ex);
             JOptionPane.showMessageDialog(this, "A syntax error occurred in the query!", "Syntax Error", JOptionPane.WARNING_MESSAGE);
             return;
          }
          /**
           * Both prefix and wildcard queries are lowercased by default, but
           * this behavior can be controlled:
           */
          parser.setLowercaseExpandedTerms(false);

          try {
             // We escape the wildcard character to avoid an exception and to get
             // it as part of the term.

//             String escapedQuery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(squery, "*", "\\*");
//
//             Term[] terms = LuceneUtils.realTermsForQueryString(defaultField, escapedQuery, new KeywordAnalyzer());
//             LOGGER.debug("Advanced Search user query=[{}] Nb of Terms recognized: [{}]",
//                     new Object[]{escapedQuery, terms.length});
//             //System.out.println("  <QueryTermsInfo num=\"" + terms.size() + "\"/>");


             queryTerms_ = parser.getQueryTerms();
             
             for (QueryTerm term : queryTerms_) {

                String queryText = term.value();

                if (queryText.equals("*")) { // All docs query
                   continue;
                }
                /**
                 * Normalized the text
                 */
                queryText = DocBuilder.normalizeIndexTerm(queryText);

                /**
                 * Replace term with normalized term in user query
                 */
                squery = org.unesco.jisis.corelib.util.StringUtils.fastReplaceAll(squery, term.value(), queryText);
               
                LOGGER.debug("Advanced Search user queryTerm=[{}]", queryText);
             }

          } catch (RuntimeException ex) {
              Exceptions.printStackTrace(ex);
                    
         }
        
      }
     
 
       // Set the Wait status text
       final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
       StatusDisplayer.getDefault().setStatusText("Searching database - Please wait...");
       RepaintManager.currentManager(frame).paintDirtyRegions();
       frame.getGlassPane().setCursor(Utilities.createProgressCursor(frame));
       frame.getGlassPane().setVisible(true);
       
       try {         
          results_ = db_.searchLucene(squery);
          if (results_ != null) {
              sortedResults_ = new long[results_.length];
              System.arraycopy(results_, 0,sortedResults_, 0, results_.length);
             // Sort on MFN
             Arrays.sort(sortedResults_);
          }
          // Display the number of records retrieved
          StringBuilder sb1 = new StringBuilder();
          if (results_==null) {
             sb1.append("0"); 
          } else {
             sb1.append(results_.length);
          }
          sb1.append(" ").append(org.openide.util.NbBundle.getMessage(SearchTopComponent.class, "results").toLowerCase());
          recordLabel.setText(sb1.toString());
          /* Build the Jlist model from the mfn retrieved  (if any) */
          DefaultListModel model = (DefaultListModel) this.mfnList.getModel();
          model.clear();
          // Trigger the titleBoxActionPerformed method (Fill the JList model)
          this.titleBoxActionPerformed(null);
                           
          if (results_ == null) {
             swingFXWebView.loadContent("");
             JOptionPane.showMessageDialog(this, "No results were found!", "No results found", JOptionPane.INFORMATION_MESSAGE);
          } else {
             // Display 1st record by triggering a select event
             mfnList.setSelectedIndex(0);

             // Save the search results
             SearchResult searchResult = new SearchResult(0, db_.getDatabaseName(), userQuery, squery, results_);
             db_.addSearchResult(searchResult);
          }

       } catch (DbException ex) {
          LOGGER.error("ERROR when searching", ex);
       }

       // clear status text
       StatusDisplayer.getDefault().setStatusText(""); // NOI18N
       // clear wait cursor
       frame.getGlassPane().setCursor(null);
       frame.getGlassPane().setVisible(false);

    }//GEN-LAST:event_btnSearchActionPerformed

    private void mfnListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_mfnListValueChanged
       if (evt.getValueIsAdjusting()) {
          return;
       }
       if (this.mfnList.getSelectedIndex() == -1) {
          return;
       }
       this.displayRecord(((MfnListItem) this.mfnList.getSelectedValue()).mfn);
    }//GEN-LAST:event_mfnListValueChanged

    private void suggestionListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_suggestionListValueChanged
       if (!evt.getValueIsAdjusting() && this.suggestionBox.isShowing() && this.suggestionList.getSelectedValue() != null) {
          // The value is set as tag[freq]\tterm_value,thus we extract the term_value


          ((JTextComponent) this.suggestionBox.getInvoker()).setText(((String) this.suggestionList.getSelectedValue()).split("\t", 2)[1]);
       }
    }//GEN-LAST:event_suggestionListValueChanged

    private void cmbPftSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPftSelectActionPerformed
       selectedPftName_ = (String) this.cmbPftSelect.getSelectedItem();
        if (mfnList.getSelectedIndex() == -1) {
          return;
       }
        long mfn = currentFormattedRecord_.getMfn();
       this.displayRecord(mfn);
}//GEN-LAST:event_cmbPftSelectActionPerformed
   private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {
      //this.populateFields();
      this.setGuidedSearch(this.isGuidedSearch_);
   }
   
   private long[] getResults() {
       if (chkbSortByMfn.isSelected()) {
           // Return sorted results
           return sortedResults_;
       }
       return results_;
   }

private void suggestionListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_suggestionListMouseClicked
   //if (evt.getClickCount() >= 2) // Double click to close
   this.suggestionBox.setVisible(false);
}//GEN-LAST:event_suggestionListMouseClicked
private void titleBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_titleBoxActionPerformed
   if (results_ == null) {
      return;
   }

   int tag = ((SearchableField) this.titleBox.getSelectedItem()).tag;
   DefaultListModel model = (DefaultListModel) this.mfnList.getModel();
   model.clear();


   int offset = mfnListPageNumber_ * mfnListItemsPerPage_;
   int count = Math.min(getResults().length - offset, mfnListItemsPerPage_);

   if (offset <= 0) {
      btnPgUp.setEnabled(false);
   } else {
      btnPgUp.setEnabled(true);
   }
   if (offset + count < getResults().length) {
      btnPgDn.setEnabled(true);
   } else {
      btnPgDn.setEnabled(false);
   }

   MfnListItem record = null;
   for (int i = 0; i < count; i++) {
      long mfn = getResults()[offset + i];
      if (tag == -1) {
         /**
          * All fields - then name is the mfn itself
          */
         String name = String.format("[%6d] %7d", i + offset + 1, mfn);
         record = new MfnListItem(name, mfn);
      } else {
         try {
            /**
             * Records are identified by a specific field content
             */
            String name = String.format("[%6d] %s", i + offset + 1, db_.getRecord(mfn).getField(tag).getStringFieldValue());
            record = new MfnListItem(name, mfn);
         } catch (DbException ex) {
            LOGGER.error("ERROR when getting field value for mfn [{}] tag [{}]",
                    new Object[]{mfn, tag, ex});
         }
      }
      model.addElement(record);
   }

   this.mfnList.setModel(model);
   if (count > 0) {
      this.mfnList.setSelectedIndex(0);
   }


}//GEN-LAST:event_titleBoxActionPerformed

private void btnDictionaryOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDictionaryOptionsActionPerformed
   java.awt.EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
         SuggestionsOptionDialog dialog = new SuggestionsOptionDialog(new javax.swing.JFrame(), true);
         dialog.setOptions(termSorting_, termCaseSensitivity_);
         dialog.setLocationRelativeTo(null);
         dialog.setVisible(true);
         if (dialog.succeeded()) {
            termSorting_ = dialog.getTermSorting();
            termCaseSensitivity_ = dialog.getTermCaseSensitivity();
         }
      }
   });
}//GEN-LAST:event_btnDictionaryOptionsActionPerformed

   private void btnPgUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPgUpActionPerformed

      if (mfnListPageNumber_ - 1 >= 0) {
         mfnListPageNumber_--;
      }
      this.titleBoxActionPerformed(null);
   }//GEN-LAST:event_btnPgUpActionPerformed

   private void btnPgDnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPgDnActionPerformed

      if ((mfnListPageNumber_ + 1) * mfnListItemsPerPage_ < getResults().length) {
         mfnListPageNumber_++;
      }
      this.titleBoxActionPerformed(null);
   }//GEN-LAST:event_btnPgDnActionPerformed

    private void chkbSortByMfnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkbSortByMfnActionPerformed
        mfnListPageNumber_ = 0;
        this.titleBoxActionPerformed(null);
    }//GEN-LAST:event_chkbSortByMfnActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDictionaryOptions;
    private javax.swing.JButton btnPgDn;
    private javax.swing.JButton btnPgUp;
    private javax.swing.JButton btnSearch;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JCheckBox chkbSortByMfn;
    private javax.swing.JComboBox cmbPftSelect;
    private javax.swing.JLabel enterQueryLabel;
    private javax.swing.JLabel formatLabel;
    private javax.swing.JPanel formatPanel;
    private javax.swing.JCheckBox guidedBox;
    private javax.swing.JSplitPane horizontalSplitPane;
    private javax.swing.JPanel indexPanel;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton matchAllRadio;
    private javax.swing.JRadioButton matchAnyRadio;
    private javax.swing.ButtonGroup matchGroup;
    private javax.swing.JList mfnList;
    private javax.swing.JScrollPane mfnScrollPane;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JLabel recordLabel;
    private javax.swing.JPanel recordPanel;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JPopupMenu suggestionBox;
    private javax.swing.JList suggestionList;
    private javax.swing.JScrollPane suggestionScroll;
    private javax.swing.JPanel termsPanel;
    private javax.swing.JScrollPane termsScrollPane;
    private javax.swing.JComboBox titleBox;
    private javax.swing.JSplitPane verticalSplitPane;
    // End of variables declaration//GEN-END:variables

   /**
    * Gets default instance. Do not use directly: reserved for *.settings files only,
    * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
    * To obtain the singleton instance, use {@link findInstance}.
    */
   public static synchronized SearchTopComponent getDefault() {
      if (instance != null) {
         instance.close();
         instance = null;
      }
      ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();

      if (connectionInfo.getDefaultDatabase() != null && instance == null) {
         IDatabase db = connectionInfo.getDefaultDatabase();
         instance = new SearchTopComponent(db);

      }

      return instance;
   }

   /**
    * Obtain the srchTopComponent instance. Never call {@link #getDefault} directly!
    */
   public static synchronized SearchTopComponent findInstance() throws DefaultDBNotFoundException {
      TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
      if (win == null) {
         ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find srch component. It will not be located properly in the window system.");
         return getDefault();
      }
      if (win instanceof SearchTopComponent) {
         return (SearchTopComponent) win;
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
      try {
         /* Reload format list */
         this.cmbPftSelect.setModel(new DefaultComboBoxModel(this.db_.getPrintFormatNames()));
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      this.repaint();
   }

   @Override
    public void componentActivated() {
        super.componentActivated();

        try {
            /* Reload format list */
            this.cmbPftSelect.setModel(new DefaultComboBoxModel(this.db_.getPrintFormatNames()));

            cmbPftSelect.getModel().setSelectedItem(selectedPftName_);
          
            this.toFront();
            JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
            frame.getRootPane().updateUI();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

   @Override
   public void componentClosed() {
      
      db_.deleteWindow(this);
      /**
       * This instance was set as observer 
       * We need to remove it
       */
      db_.deleteObserver(this);
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
      //exportButton.setEnabled(false);
   }

   void enableExportButton() {
      //this.exportButton.setEnabled(true);
   }

   public void valueChanged(ListSelectionEvent e) {
       
//      try {
//         DbViewTopComponent win = DbViewTopComponent.findInstance();
//         win.open();
//         win.requestActive();
//      /*win.setRecordByMFN(Long.parseLong(lstMFN.getSelectedValue().toString()));*/
//      } catch (DefaultDBNotFoundException ex) {
//         ex.displayWarning();
//      }
   }

   @Override
   public void update(Observable arg0, Object arg1) {
      
      if (db_.pftHasChanged()) {
         try {
            String[] pftNames = db_.getPrintFormatNames();
            // check that current pft is not deleted
            boolean pftDeleted = true;
            for (String pftName : pftNames) {
               if (selectedPftName_.equals(pftName)) {
                  pftDeleted = false;
                  break;
               }
            }
            if (pftDeleted) {
               selectedPftName_ = "RAW"; // Always exists !
            }
            cmbPftSelect.setModel(new DefaultComboBoxModel(pftNames));
            
            cmbPftSelect.getModel().setSelectedItem(selectedPftName_);
            
            if (mfnList.getSelectedIndex() == -1) {
               return;
            }
            long mfn = currentFormattedRecord_.getMfn();
            this.displayRecord(mfn);
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
      }

   }

   final static class ResolvableHelper implements Serializable {

      private static final long serialVersionUID = 1L;

      public Object readResolve() {
         return SearchTopComponent.getDefault();
      }
   }

   private class mL extends MouseAdapter {
      /*
      public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() >= 2) {
      srchQuery.setText(srchQuery.getText() + " " + termsList.getSelectedValue().toString());
      srchTopComponent2.getDefault().repaint();
      srchQuery.repaint();
      }
      }
       */
   }
}

class MfnListItem {

   public long mfn;      // Record mfn
   public String name;   // List item name

   public MfnListItem(String name, long mfn) {
      this.mfn = mfn;
      this.name = name;
   }

   @Override
   public String toString() {
      if (this.name.equals("") || this.name== null) {
         return "[" + this.mfn + "]";
      }
      return this.name;
   }
}

class SuggestionTimerTask extends TimerTask {

   private SearchTopComponent panel;
   private JComboBox cmbSearchableFields = null;
   private JTextComponent txtQuery = null;

   public SuggestionTimerTask(SearchTopComponent panel, JComboBox cmbSearchableFields, JTextComponent txtQuery) {
      this.panel = panel;
      this.cmbSearchableFields = cmbSearchableFields;
      this.txtQuery = txtQuery;
   }

   public void run() {
      System.out.println("Running SuggestionTimerTask TASK");
      if (this.cmbSearchableFields == null|| this.txtQuery == null
              || (this.txtQuery.getText().equals(""))) {
         return;
      }
      if (this.txtQuery.hasFocus()|| this.cmbSearchableFields.hasFocus()) {

         Runnable event = new Runnable() {

            @Override
            public void run() {
               String value = txtQuery.getText();
               boolean show = panel.populateDictionary(((SearchableField) cmbSearchableFields.getSelectedItem()).tag,
                       value);
               if (show) {
                  panel.showDictionary(txtQuery);
                  /**
                   * Workaround for Mac
                   * requestFocusInWindow() doesn't always set the cursor
                   * The workaround is 
                   * JTextField.requestFocusInWindow();
                   * JTextField.setCaretPosition(JTextField.getDocument().getLength();
                   */
                  txtQuery.requestFocusInWindow();
                  txtQuery.setCaretPosition(txtQuery.getDocument().getLength());
                  // clear status text
                  StatusDisplayer.getDefault().setStatusText(""); // NOI18N
               }
            }
         };
         SwingUtilities.invokeLater(event);
      }

   }
}
   
   class MfnListRenderer extends JLabel implements ListCellRenderer {

      public MfnListRenderer() {
         setOpaque(true);
      }

      @Override
       public Component getListCellRendererComponent(JList list, 
               Object value, int index, 
               boolean isSelected, 
               boolean cellHasFocus) {
    

         setText(value.toString());

         Color background;
         Color foreground;

         // check if this cell represents the current DnD drop location
         JList.DropLocation dropLocation = list.getDropLocation();
         if (dropLocation != null
                 && !dropLocation.isInsert()
                 && dropLocation.getIndex() == index) {

            background = Color.BLUE;
            foreground = Color.WHITE;

            // check if this cell is selected
         } else if (isSelected) {
            background = Color.RED;
            foreground = Color.WHITE;

            // unselected, and not the DnD drop location
         } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
         }

         setBackground(background);
         setForeground(foreground);

         return this;
      }

  
   }

