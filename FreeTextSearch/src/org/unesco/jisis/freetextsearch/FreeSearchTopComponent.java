/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.freetextsearch;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.*;
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
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.MfnRange;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.corelib.util.StringUtils;
import org.unesco.jisis.jisisutil.history.HistoryTextArea;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;
import org.unesco.jisis.jisiscore.client.SearchResult;
import org.unesco.jisis.jisiscore.common.AsyncCallback;
import org.unesco.jisis.searchhistory.SearchHistoryAction;
import org.unesco.jisis.searchhistory.SearchHistoryModel;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.unesco.jisis.freetextsearch//FreeSearch//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "FreeSearchTopComponent"
    //iconBase="SET/PATH/TO/ICON/HERE", 
   
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
//@ActionID(category = "Window", id = "org.unesco.jisis.freetextsearch.FreeSearchTopComponent")
@ActionID(category = "Window", id = "org.unesco.jisis.freetextsearch.FreeSearchTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_FreeSearchAction"
)
@Messages({
    "CTL_FreeSearchAction=FreeSearch",
    "CTL_FreeSearchTopComponent=Free Text Search",
    "HINT_FreeSearchTopComponent=This is a FreeSearch window"
})
public final class FreeSearchTopComponent extends TopComponent implements Observer {
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FreeSearchTopComponent.class);
    
    private static ClientDatabaseProxy db_ = null;

    private static RequestProcessor.Task searchTask_ = null;

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
  
    private CancellableProgress cancellable_;

    private static String booleanExpr_;
    private static ArrayList<Long> mfns_ = new ArrayList<>();
    
    private final String NO_SEARCH_SETS = "No Search Sets";

    public FreeSearchTopComponent() {
        initComponents();
        setName(Bundle.CTL_FreeSearchTopComponent());
        setToolTipText(Bundle.HINT_FreeSearchTopComponent());

    }
    
     public FreeSearchTopComponent(IDatabase db) {

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
         
        cmbSearchSets.addActionListener(
                new ActionListener() {
                   @Override
                   public void actionPerformed(ActionEvent e) {
                      JComboBox combo = (JComboBox) e.getSource();
                      int i = cmbSearchSets.getSelectedIndex();
                      if (i != -1) {
                         String s = (String) combo.getSelectedItem();
                         if (s.equals(NO_SEARCH_SETS)) {

                         } else {
                            List<SearchResult> searchResults = db_.getSearchResults();
                            SearchHistoryAction searchHistoryAction
                            = new SearchHistoryAction();
                            searchHistoryAction.actionPerformed(db_, searchResults.get(i));
                         }

                      }
                   }

                }
        );
      
        try {
            setName(NbBundle.getMessage(FreeSearchTopComponent.class, "CTL_FreeSearchTopComponent")
                + " (" +db.getDbHome()+"//"+ db_.getDatabaseName() + ")");
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        setToolTipText(Bundle.HINT_FreeSearchTopComponent());
        
         prepareSearchHistory();
         
      
        //WindowManager.getDefault().findTopComponent("output").putClientProperty("netbeans.winsys.tc.closing_disabled", Boolean.TRUE);
        //nobody can close Output window now!
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
        cmbSearchSets.setModel(new DefaultComboBoxModel<>(searches));

    }
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always regenerated by the Form Editor.
     */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      mainPanel = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      scrollPaneQuery = new javax.swing.JScrollPane();
      queryTextArea = new HistoryTextArea(db_.getDbName()+"_FreeTextSearch");
      btnSearch = new javax.swing.JButton();
      syntaxPanel = new javax.swing.JPanel();
      jLabel2 = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      jLabel5 = new javax.swing.JLabel();
      jLabel6 = new javax.swing.JLabel();
      cmbSearchSets = new javax.swing.JComboBox();
      jLabel8 = new javax.swing.JLabel();

      setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

      mainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      mainPanel.setPreferredSize(new java.awt.Dimension(700, 500));

      org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.jLabel1.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.jLabel3.text")); // NOI18N

      queryTextArea.setColumns(20);
      queryTextArea.setRows(5);
      scrollPaneQuery.setViewportView(queryTextArea);

      btnSearch.setMnemonic('S');
      org.openide.awt.Mnemonics.setLocalizedText(btnSearch, org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.btnSearch.text")); // NOI18N
      btnSearch.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnSearchActionPerformed(evt);
         }
      });

      syntaxPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.syntaxPanel.border.title"))); // NOI18N

      jLabel2.setFont(new java.awt.Font("Tahoma", 2, 13)); // NOI18N
      org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.jLabel2.text")); // NOI18N

      jLabel4.setFont(new java.awt.Font("Tahoma", 2, 13)); // NOI18N
      org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.jLabel4.text")); // NOI18N

      jLabel5.setFont(new java.awt.Font("Tahoma", 2, 13)); // NOI18N
      org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.jLabel5.text")); // NOI18N

      javax.swing.GroupLayout syntaxPanelLayout = new javax.swing.GroupLayout(syntaxPanel);
      syntaxPanel.setLayout(syntaxPanelLayout);
      syntaxPanelLayout.setHorizontalGroup(
         syntaxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(syntaxPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(syntaxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(24, Short.MAX_VALUE))
      );

      syntaxPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel2, jLabel4, jLabel5});

      syntaxPanelLayout.setVerticalGroup(
         syntaxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(syntaxPanelLayout.createSequentialGroup()
            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel5)
            .addContainerGap(28, Short.MAX_VALUE))
      );

      syntaxPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel2, jLabel4, jLabel5});

      jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
      org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.jLabel6.text")); // NOI18N

      cmbSearchSets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

      org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(FreeSearchTopComponent.class, "FreeSearchTopComponent.jLabel8.text")); // NOI18N

      javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
      mainPanel.setLayout(mainPanelLayout);
      mainPanelLayout.setHorizontalGroup(
         mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(mainPanelLayout.createSequentialGroup()
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(mainPanelLayout.createSequentialGroup()
                  .addGap(253, 253, 253)
                  .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addGap(517, 517, 517))
               .addGroup(mainPanelLayout.createSequentialGroup()
                  .addContainerGap()
                  .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(scrollPaneQuery)
                     .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addGroup(mainPanelLayout.createSequentialGroup()
                              .addGap(161, 161, 161)
                              .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE))
                           .addGroup(mainPanelLayout.createSequentialGroup()
                              .addGap(13, 13, 13)
                              .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(mainPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(442, 442, 442))
                                 .addGroup(mainPanelLayout.createSequentialGroup()
                                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                       .addComponent(syntaxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                       .addGroup(mainPanelLayout.createSequentialGroup()
                                          .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                          .addGap(202, 202, 202)))
                                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                       .addComponent(cmbSearchSets, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                                       .addGroup(mainPanelLayout.createSequentialGroup()
                                          .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                          .addGap(122, 122, 122)))))))
                        .addGap(159, 159, 159)))))
            .addContainerGap())
      );
      mainPanelLayout.setVerticalGroup(
         mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(mainPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel3)
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(mainPanelLayout.createSequentialGroup()
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(syntaxPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(mainPanelLayout.createSequentialGroup()
                  .addGap(39, 39, 39)
                  .addComponent(jLabel8)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(cmbSearchSets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(18, 18, 18)
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(scrollPaneQuery, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(btnSearch)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 849, Short.MAX_VALUE)
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        
        JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
        String query = queryTextArea.getText().trim();
        query = StringUtils.fastReplaceAll(query, "\n", " ");

        /**
         * Parse the query
         * 
         * query = (?| empty) (#sarchSetNumber | *firstMfn,lastMfn | empty) boolean_expression
         */
        MfnRange[] mfnRanges = null;
        int searchSetNumber;
        SearchResult previousSearchResult = null;       
        long startMfn;
        long endMfn;
        
        if (query.startsWith("?")) {
            query = query.substring(1).trim(); // Get rid of ?
        }
        try {
            if (query.startsWith("#")) {
                //#searchSetNumber  Boolean_Expression 
                query = query.substring(1); // Get rid of #
                // split the query in '#searchSetNumber' and 'Boolean_Expression'
                // get index of blank that separates '#searchSetNumber' from 'Boolean_Expression'
                int i = query.indexOf(" ");
                if (i == -1) {
                    JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_BLANK_BETWEEN_SEARCH_SET_NUMBER_AND_QUERY_NOT_FOUND")); //error
                    return;
                }
                String s = query.substring(0, i).trim();
                searchSetNumber = Integer.parseInt(s);
                booleanExpr_ = query.substring(i + 1).trim();
                
                List<SearchResult> searchResults = db_.getSearchResults();
                if (searchResults != null && searchResults.size() > 0) {
                    for (SearchResult searchResult : searchResults) {
                        if (searchResult.getSearchNumber() == searchSetNumber) {
                            previousSearchResult = searchResult;
                            break;
                        }
                    }
                }
                if (previousSearchResult == null) {
                    JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_SEARCH_SET_NOT_FOUND"));
                    return;
                }
            } else if (query.startsWith("*")) {
                //    *startMFN,endMFN Boolean_Expression
                query = query.substring(1); // Get rid of *
                // split the query in 'startMFN,endMFN' and 'Boolean_Expression'
                // get index of blank that separates startMFN,endMFN' from 'Boolean_Expression'
                int i = query.indexOf(" ");
                if (i == -1) {
                    JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_BLANK_BETWEEN_RANGE_AND_QUERY_NOT_FOUND"));
                    return;

                }
                booleanExpr_ = query.substring(i + 1).trim();
                /**
                 * s should be equal to starttMfn,endMfn
                 */
                String s = query.substring(0, i).trim();
                String[] range = s.split(",");
                if (range.length != 2) {
                    //error
                    JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_RANGE_ERROR"));
                    return;
                }
                startMfn = Integer.parseInt(range[0]);
                if (startMfn < db_.getFirst().getMfn()) {
                    JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_INVALID_FIRST_MFN_LESS_THAN_FIRST"));
                    return;
                }
                endMfn = Integer.parseInt(range[1]);
                if (endMfn >= db_.getLastMfn()) {
                    JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_INVALID_LAST_MFN_GE_LAST"));
                    return;
                }
                mfnRanges = new MfnRange[1];
                mfnRanges[0] = new MfnRange(startMfn, endMfn);

            } else {
                // Search all records
                booleanExpr_ = query;

                mfnRanges = new MfnRange[1];
                mfnRanges[0] = new MfnRange(db_.getFirst().getMfn(),
                    db_.getLast().getMfn());

            }
        } catch (MissingResourceException | HeadlessException | NumberFormatException | DbException ex) {
            LOGGER.error("Error when parsing the query, exception raised", ex);
             JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_ERROR_WHEN_PARSING_QUERY_EXCEPTION"));
            return;
        }

        /**
         * Make the boolean expression output true if matched
         */
        StringBuilder sb = new StringBuilder(booleanExpr_);
        sb.insert(0, "if (");
        sb.append(") then 'true' fi");
        booleanExpr_ = sb.toString();

        /**
         * Parse the boolean query
         */
        ISISFormatter formatter = ISISFormatter.getFormatter(booleanExpr_);
        if (formatter == null) {
            LOGGER.error("Error in pft:" + booleanExpr_ + "\n" + ISISFormatter.getParsingError());
            GuiGlobal.output("Error in pft:" + booleanExpr_ + "\n" + ISISFormatter.getParsingError());
            JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_ERROR_WHEN_PARSING_BOOLEAN_QUERY"));
            return;
        } else if (formatter.hasParsingError()) {
              GuiGlobal.output("Error in pft:" + booleanExpr_ + "\n" + ISISFormatter.getParsingError());
            JOptionPane.showMessageDialog(mainWindow,
                        NbBundle.getMessage(FreeSearchTopComponent.class,
                            "MSG_ERROR_WHEN_PARSING_BOOLEAN_QUERY"));
            return;
        }

        mfns_.clear();
        /**
         * Create a call back object - is called on search thread completion or search thread failure
         * 
         */
        SearchAsyncCallback searchAsyncCallback = new SearchAsyncCallback(this);
        search(mfnRanges, previousSearchResult, formatter, searchAsyncCallback);     
        HistoryTextArea historyTextArea = (HistoryTextArea) queryTextArea;
        historyTextArea.addCurrentToHistory();

    }//GEN-LAST:event_btnSearchActionPerformed
      
   /**
    * 
    * @param mfnRanges
    * @param formatter 
    */
    private void search(final MfnRange[] mfnRanges, final SearchResult previousSearchResult,
                        final ISISFormatter formatter, final SearchAsyncCallback asyncCallback) {
       
        try {
            final NotifyDescriptor d
                = new NotifyDescriptor.Message(NbBundle.getMessage(FreeSearchTopComponent.class,
                        "MSG_FREE_TEXT_SEARCH_DONE"));
            final Date start = new Date();
            Runnable searchRun = new Runnable() {
                @Override
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            if (previousSearchResult == null) {
                                mfns_ = doSearch(mfnRanges, formatter);
                            } else {
                                mfns_ = doSearchPreviousSearchSet(previousSearchResult, formatter);
                            }
                            
                        } catch (Exception ex) {
                            LOGGER.error("Free Search Thread failed", ex);
                            asyncCallback.onFailure(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, Job is done, we are on the event queue now

                        JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                        if (cancellable_.cancelRequested()) {
                            JOptionPane.showMessageDialog(mainWindow,
                                NbBundle.getMessage(FreeSearchTopComponent.class,
                                    "MSG_FREE_TEXT_SEARCH_CANCELED"));
                            asyncCallback.onCancel();
                        } else {
                            Date end = new Date();
                            GuiGlobal.output("" + mfns_.size() + " records match the query ["
                                + Long.toString(end.getTime() - start.getTime()) + "} milliseconds to perform Free Text Search");
                            asyncCallback.onSuccess(null);
                            JOptionPane.showMessageDialog(mainWindow,
                                NbBundle.getMessage(FreeSearchTopComponent.class,
                                    "MSG_FREE_TEXT_SEARCH_DONE"));
                        }
                    }
                }
            };

            searchTask_ = requestProcessor_.post(searchRun);

        } catch (Exception ex) {
            GuiGlobal.output("Error when executing Free Text Search:\n " + ex.getMessage());
             asyncCallback.onFailure(ex);
        }
    }
    /**
     * Free Text Searching on ranges of MFNs
     * 
     * @param mfnRanges - The ranges of MFNs to process
     * @param formatter - The boolean query Intermediate language (IL) to evaluate for the records
     *                    defined by the ranges of MFN
     * @return          - The list of MFNs that match the boolean expression
     */
    private ArrayList<Long> doSearch(MfnRange[] mfnRanges, ISISFormatter formatter) {

        ArrayList<Long> mfnHits = new ArrayList<>();
        if (mfnRanges == null || mfnRanges.length == 0) {
            return mfnHits;
        }
        cancellable_ = new CancellableProgress();
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Searching Records...",
            cancellable_);
        progress.start();
        progress.switchToIndeterminate();
        long cursor = 0;
        long mfn = 0;

        for (MfnRange mfnRange : mfnRanges) {
            if (Thread.interrupted() || cancellable_.cancelRequested()) {
                progress.finish();
                break;
            }
            try {
                long startMfn = mfnRange.getFirst();
                long endMfn = mfnRange.getLast();
                if (endMfn >= startMfn) {
                    for (long j = startMfn; j <= endMfn; j++) {
                        if (Thread.interrupted() || cancellable_.cancelRequested()) {
                            break;
                        }
                        cursor = j;
                        mfn = 0;
                        final Record record = (Record) db_.getRecordCursor(j);
                        if (record == null) {
                            continue;
                        }
                        mfn = record.getMfn();

                        if (searchIfRecordIsMatchingQuery(record, formatter)) {
                            mfnHits.add(record.getMfn());
                            GuiGlobal.output("Record with MFN " + record.getMfn() + " matches query");
                        }
                        progress.setDisplayName("Free Text Search Record MFN:" + Long.toString(record.getMfn()));
                    }
                } else {
                    for (long j = startMfn; j >= endMfn; j--) {
                        if (Thread.interrupted() || cancellable_.cancelRequested()) {
                            break;
                        }
                        cursor = j;
                        mfn = 0;
                        Record record = (Record) db_.getRecordCursor(j);
                        if (record == null) {
                            continue;
                        }
                        if (searchIfRecordIsMatchingQuery(record, formatter)) {
                            mfnHits.add(record.getMfn());
                            GuiGlobal.output("Record with MFN " + record.getMfn() + " matches query");
                        }
                        progress.setDisplayName("Free Text Search Record MFN:" + Long.toString(record.getMfn()));
                    }
                }
            } catch (Exception ex) {
                GuiGlobal.output("Error when executing Global Add for MFN:" + mfn + " cursor=" + cursor + "\n "
                    + ex.getMessage());
                LOGGER.error("Error when executing Global Add for MFN:" + mfn + " cursor=" + cursor + "\n ", ex);
            }
        }
        progress.finish();
        return mfnHits;
    }
     /**
      * Free Text Searching on a previous Searh Set (Free Text Search or Lucene Search)
      * 
      * @param previousSearchResult - Previous SearchResut object to use
      * @param formatter            - The boolean query Intermediate language (IL) to evaluate for the records
      *                                retrieved in the the previous SearchResult set
      * @return                     - The list of MFNs that match the boolean expression 
      */
    private ArrayList<Long> doSearchPreviousSearchSet(SearchResult previousSearchResult, ISISFormatter formatter) {

        ArrayList<Long> mfnHits = new ArrayList<>();
        List<Long> mfnList = previousSearchResult.getMfns();
        if (mfnList == null || mfnList.size() == 0) {
            return mfnHits;
        }
        cancellable_ = new CancellableProgress();
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Searching Records...",
            cancellable_);
        progress.start();
        progress.switchToIndeterminate();

        for (Long mfn : mfnList) {
            if (Thread.interrupted() || cancellable_.cancelRequested()) {
                progress.finish();
                break;
            }
            try {
                final Record record = (Record) db_.getRecordCursor(mfn);
                if (record == null) {
                    continue;
                }
                if (searchIfRecordIsMatchingQuery(record, formatter)) {
                    mfnHits.add(record.getMfn());
                    GuiGlobal.output("Record with MFN " + record.getMfn() + " matches query");
                }
                progress.setDisplayName("Free Text Search Record MFN:" + Long.toString(record.getMfn()));
            } catch (Exception ex) {
                GuiGlobal.output("Error when executing Global Add for MFN:" + mfn + "\n "
                    + ex.getMessage());
                 LOGGER.error("Error when executing Global Add for MFN:" + mfn  + "\n ", ex);
            }
        }
        progress.finish();
        return mfnHits;
    }

    
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton btnSearch;
   private javax.swing.JComboBox cmbSearchSets;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JLabel jLabel6;
   private javax.swing.JLabel jLabel8;
   private javax.swing.JPanel mainPanel;
   private javax.swing.JTextArea queryTextArea;
   private javax.swing.JScrollPane scrollPaneQuery;
   private javax.swing.JPanel syntaxPanel;
   // End of variables declaration//GEN-END:variables
    


    @Override
   public void componentClosed() {
      super.componentClosed();
      /**
       * unsets maximzed mode so that next time we set
       */
//      Action action = org.openide.awt.Actions.forID("Window", "org.netbeans.core.windows.actions.MaximizeWindowAction");
//      action.actionPerformed(null);
      if (searchTask_ != null) {
         searchTask_.cancel();
      }
      // TODO add custom code on component closing
   }
    
  
   
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

   @Override
    public void componentActivated() {
        super.componentActivated();
//        Action action = org.openide.awt.Actions.forID("Window", "org.netbeans.core.windows.actions.MaximizeWindowAction");
//        action.actionPerformed(null);
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

    @Override
    public void update(Observable o, Object arg) {
       prepareSearchHistory(); 
        
    }

    /**
     * Evaluate the Boolean Expression Intermediate Language (IL) against the record
     * 
     * @param record     - The record to process
     * @param formatter  - the Boolean Expression Intermediate Language (IL)
     * @return           - true if the boolean expression generates "true" as output, i.e. it means that
     *                     the record satisfies the boolean expression
     */
    private  boolean searchIfRecordIsMatchingQuery(Record record, ISISFormatter formatter) {
        
        formatter.setRecord(db_, record);
        formatter.eval();
        if (formatter.getText().equalsIgnoreCase("true")) {
            return true;         
        }   
        return false;
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
    
    static class SearchAsyncCallback implements AsyncCallback<ArrayList<Long> > {
        FreeSearchTopComponent topComponent_;
        public SearchAsyncCallback(FreeSearchTopComponent topComponent) {
            topComponent_ = topComponent;
        }
        @Override
        public void onFailure(Throwable caught) {
            LOGGER.error("Free Search Failure:", caught);
        }

        /**
         * Called when free text search is finished successfully 
         * @param result 
         */
        @Override
        public void onSuccess(ArrayList<Long> result) {
            if (!mfns_.isEmpty()) {
                // Save the search results
                long[] results = new long[mfns_.size()];
                for (int i = 0; i < mfns_.size(); i++) {
                    results[i] = mfns_.get(i);
                }
                SearchResult searchResult = null;
                try {
                    /**
                     * Note that luceneQuery is null in that case
                     */
                    searchResult = new SearchResult(0, db_.getDatabaseName(), booleanExpr_, null, results);
                } catch (DbException ex) {
                    Exceptions.printStackTrace(ex);
                }
                db_.addSearchResult(searchResult);
                topComponent_.prepareSearchHistory();
            }

        }

        @Override
        public void onCancel() {
            if (searchTask_ != null) {
                searchTask_.cancel();
            }
        }
        
    }
}
