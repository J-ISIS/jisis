package org.unesco.jisis.dictionary;


import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Timer;
import java.util.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.FieldSelectionTable.FstEntry;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.IndexInfo;
import org.unesco.jisis.corelib.common.ModifiableJisisParams;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.DefaultDBNotFoundException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.corelib.index.DictionaryTerm;
import org.unesco.jisis.corelib.index.SearchableField;
import org.unesco.jisis.corelib.index.TermParams;
import org.unesco.jisis.gui.GuiUtils;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisisutils.distributed.DistributedTableModel;
import org.unesco.jisis.jisisutils.threads.IdeCursor;

class Popupmenu_mouseAdapter extends java.awt.event.MouseAdapter {
   JPopupMenu popup;

   Popupmenu_mouseAdapter(JPopupMenu popupmenu) {
      popup = popupmenu;
   }

   @Override
   public void mousePressed(MouseEvent e) {
      mayShowPopup(e);
   }

   @Override
   public void mouseReleased(MouseEvent e) {
      mayShowPopup(e);
   }

   private void mayShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
         popup.show(e.getComponent(), e.getX(), e.getY());
      }
   }
}


/**
 * Top component which displays something.
 */
public class DictionaryTopComponent extends TopComponent implements Observer {

    private static DictionaryTopComponent instance;

    protected static Component fromComponent;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/unesco/jisis/dictionary/browse.png";

    private static final String PREFERRED_ID = "dictionaryTopComponent";

    private ClientDatabaseProxy db_;
    private DistributedTableModel model_;

    private IndexInfo indexInfo_;
    private TableRowSorter sorter_;

    private Timer filterTimer_ = null;
    private TimerTask filterTimerTask_ = null;

    private FieldSelectionTable fst_;

    private SearchableField[] searchableFields_ = null;

    private Timer suggestionTimer_ = null;
    private TimerTask suggestionTimerTask_ = null;

   private javax.swing.JPopupMenu suggestionBox;
   private javax.swing.JList suggestionList;
   private javax.swing.JScrollPane suggestionScroll;

    public DictionaryTopComponent(IDatabase db) {

        final JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();
        IdeCursor.changeCursorWaitStatus(true);
        StatusDisplayer.getDefault().setStatusText("Loading Indexed Tems, please wait...");
        RepaintManager.currentManager(mainWin).paintDirtyRegions();
        if (db instanceof ClientDatabaseProxy) {
            db_ = (ClientDatabaseProxy) db;
        } else {
            throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
        }
        /* Register this TopComponent as attached to this DB */
        db_.addWindow(this);
        /* Add this TopComponent as Observer to DB changes */
        db_.addObserver((Observer) this);
        try {
            this.setDisplayName("Dictionary" + " (" + db.getDbHome() + "//" + db.getDatabaseName() + ")");

            model_ = new DistributedTableModel(new TermsTableDataSource(db));

            initComponents();

            initIndexInfo();

            setName(NbBundle.getMessage(DictionaryTopComponent.class, "CTL_dictionaryTopComponent"));
            setToolTipText(NbBundle.getMessage(DictionaryTopComponent.class, "HINT_dictionaryTopComponent"));
            setIcon(ImageUtilities.loadImage(ICON_PATH, true));

            /* Construct the JTable for displaying the dictionary terms */
            termsTable_.setAutoCreateColumnsFromModel(false);
            termsTable_.setColumnSelectionAllowed(false);
            termsTable_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            //System.out.println("TermsDictionary TopComponent columnCount=" + model_.getColumnCount());
            for (int i = 0; i < model_.getColumnCount(); i++) {
                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

                if (i == 1 || i == 2) {
                    renderer.setHorizontalAlignment(JLabel.LEFT);
                } else {
                    renderer.setHorizontalAlignment(JLabel.RIGHT);
                }
                int w = 100;
                if (i == 2) {
                    w = 350;
                }
                TableColumn column = new TableColumn(i, w, renderer, null);
                termsTable_.addColumn(column);
            }
            if (db_.getDisplayFont() != null) {
                termsTable_.setFont(db_.getDisplayFont());
            }

            termsTable_.setRowSelectionAllowed(true);
            termsTable_.setCellSelectionEnabled(true);

//         sorter_ = new FilterOnlyTableRowSorter<DistributedTableModel>(termsTable_);
//         termsTable_.setRowSorter(sorter_);
            suggestionBox = new javax.swing.JPopupMenu();
            suggestionScroll = new javax.swing.JScrollPane();
            suggestionList = new javax.swing.JList();
            suggestionTimerTask_ = null;
            suggestionTimer_ = new Timer();

            suggestionScroll.setComponentPopupMenu(suggestionBox);

            suggestionList.setModel(new javax.swing.AbstractListModel() {
                String[] strings = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

                public int getSize() {
                    return strings.length;
                }

                public Object getElementAt(int i) {
                    return strings[i];
                }
            });
            suggestionList.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    suggestionListMouseClicked(evt);
                }
            });
            suggestionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                    suggestionListValueChanged(evt);
                }
            });

            if (db_.getDisplayFont() != null) {
                suggestionList.setFont(db_.getDisplayFont());
            }
            suggestionScroll.setViewportView(suggestionList);
            java.awt.GridBagConstraints gridBagConstraints;
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;

            suggestionBox.setLayout(new java.awt.GridBagLayout());
            suggestionBox.add(this.suggestionScroll, gridBagConstraints);
            suggestionList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
            suggestionList.setModel(new DefaultListModel());

            txtQuery.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    queryFieldEnter(evt);
                }
            });

            txtQuery.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    queryFieldPressed(evt);
                }
            });

            txtQuery.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    queryFieldTyped(evt);
                }
            });

            if (db_.getDisplayFont() != null) {
                txtQuery.setFont(db_.getDisplayFont());
            }

            cmbFstEntry.addActionListener(new java.awt.event.ActionListener() {

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

            fst_ = db_.getFieldSelectionTable();

            GuiUtils.TweakJTable(termsTable_);

            createPopupMenu();

            populateFields();

            int[] fstTags = fst_.getEntriesTag();
            String[] fields = new String[fstTags.length + 1];
            fields[0] = "<all the fields>";

            for (int k = 0; k < fstTags.length; k++) {
                fields[k + 1] = Integer.toString(fstTags[k]);
            }

            // FST entry ID or ALL
            if (this.searchableFields_ != null) {
                cmbFstEntry.setModel(new javax.swing.DefaultComboBoxModel(searchableFields_));
            }

        } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            IdeCursor.changeCursorWaitStatus(false);
            StatusDisplayer.getDefault().setStatusText("");
            RepaintManager.currentManager(mainWin).paintDirtyRegions();
        }
    }

   private void populateFields() {

     
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
     
   }

   /**
    * The Searchable field has changed
    * @param evt
    * @param searchableField
    */
   private void searchableFieldChanged(ActionEvent evt, SearchableField searchableField) {

        if (suggestionTimerTask_ != null) {
         suggestionTimerTask_.cancel();
      }

      showRetrievingTerms();
      suggestionTimerTask_ = new SuggestionTimerTask(this, cmbFstEntry,
              txtQuery);
      suggestionTimer_.schedule(this.suggestionTimerTask_, 500);
   }

    private void initIndexInfo() {
        try {
            indexInfo_ = db_.getIndexInfo();
            txtIndexName.setText(indexInfo_.getIndexName());
            txtNumFields.setText(Integer.toString(indexInfo_.getNumFields()));
            txtNumRecords.setText(Integer.toString(indexInfo_.getNumDocs()));
            txtNumTerms.setText(Integer.toString(indexInfo_.getNumTerms()));
            //Date date = new Date(indexInfo_.getLastModified());
            txtLastModified.setText(indexInfo_.getLastModified());
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
     
   
    /**
    * Creates a pop-up menu.
    */
   public void createPopupMenu() {
      JMenuItem menuItem;
      // creates the popup menu.
      JPopupMenu popup = new JPopupMenu();

     popup.add(new CopyAction(termsTable_));

      // adds listener to the text area so the popup menu can work.
      Popupmenu_mouseAdapter popupListener = new Popupmenu_mouseAdapter(popup);
      termsTable_.addMouseListener(popupListener);
   }

    private DictionaryTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DictionaryTopComponent.class, "CTL_dictionaryTopComponent"));
        setToolTipText(NbBundle.getMessage(DictionaryTopComponent.class, "HINT_dictionaryTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        quickSearchPanel = new javax.swing.JPanel();
        cmbFstEntry = new javax.swing.JComboBox();
        txtQuery = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblIndexContent = new javax.swing.JLabel();
        infoPanel = new javax.swing.JPanel();
        lblNumFields = new javax.swing.JLabel();
        txtNumFields = new javax.swing.JTextField();
        lblNumRecords = new javax.swing.JLabel();
        txtNumRecords = new javax.swing.JTextField();
        lblNumTerms = new javax.swing.JLabel();
        txtNumTerms = new javax.swing.JTextField();
        lblLastModified = new javax.swing.JLabel();
        txtLastModified = new javax.swing.JTextField();
        lblIndexName = new javax.swing.JLabel();
        txtIndexName = new javax.swing.JTextField();
        tablePanel = new javax.swing.JPanel();
        scrollPane_ = new javax.swing.JScrollPane();
        termsTable_ = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setAutoscrolls(true);

        mainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        quickSearchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quick Search", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        txtQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQueryActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Query:");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Search:");

        javax.swing.GroupLayout quickSearchPanelLayout = new javax.swing.GroupLayout(quickSearchPanel);
        quickSearchPanel.setLayout(quickSearchPanelLayout);
        quickSearchPanelLayout.setHorizontalGroup(
            quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, quickSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(quickSearchPanelLayout.createSequentialGroup()
                        .addComponent(cmbFstEntry, 0, 252, Short.MAX_VALUE)
                        .addGap(82, 82, 82))
                    .addGroup(quickSearchPanelLayout.createSequentialGroup()
                        .addComponent(txtQuery)
                        .addContainerGap())))
        );
        quickSearchPanelLayout.setVerticalGroup(
            quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickSearchPanelLayout.createSequentialGroup()
                .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbFstEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(quickSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtQuery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        lblIndexContent.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblIndexContent, "Index Content");

        infoPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblNumFields.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblNumFields, "Number of fields:");

        txtNumFields.setEditable(false);
        txtNumFields.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        lblNumRecords.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblNumRecords, "Number of Records:");

        txtNumRecords.setEditable(false);
        txtNumRecords.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        lblNumTerms.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblNumTerms, "Number of terms:");

        txtNumTerms.setEditable(false);
        txtNumTerms.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        lblLastModified.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblLastModified, "Last modified:");

        txtLastModified.setEditable(false);
        txtLastModified.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        javax.swing.GroupLayout infoPanelLayout = new javax.swing.GroupLayout(infoPanel);
        infoPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(infoPanelLayout.createSequentialGroup()
                        .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNumTerms)
                            .addComponent(lblLastModified)
                            .addComponent(lblNumFields))
                        .addGap(42, 42, 42)
                        .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtNumTerms, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtLastModified, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
                            .addComponent(txtNumFields, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(55, Short.MAX_VALUE))
                    .addGroup(infoPanelLayout.createSequentialGroup()
                        .addComponent(lblNumRecords)
                        .addGap(26, 26, 26)
                        .addComponent(txtNumRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        infoPanelLayout.setVerticalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtNumFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNumFields))
                .addGap(18, 18, 18)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNumRecords, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNumRecords, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtNumTerms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNumTerms))
                .addGap(18, 18, 18)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLastModified)
                    .addComponent(txtLastModified, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        lblIndexName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblIndexName, "Index name:");

        txtIndexName.setEditable(false);

        tablePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        termsTable_.setAutoCreateColumnsFromModel(false);
        termsTable_.setModel(model_);
        scrollPane_.setViewportView(termsTable_);

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addComponent(scrollPane_, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 142, Short.MAX_VALUE))
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane_)
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(lblIndexName, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIndexName, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(107, 107, 107)
                        .addComponent(lblIndexContent)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(quickSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(infoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIndexContent)
                    .addComponent(lblIndexName)
                    .addComponent(txtIndexName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(infoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(quickSearchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 265, Short.MAX_VALUE))
                    .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQueryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQueryActionPerformed

    private String getTagFromField(String field){
        String[] splitted = field.split("@");
        return splitted[0];
    }

    private String[] buildIndexFieldNames() {
      HashSet<Integer> tags = new HashSet<Integer>();
      List<String> fieldNames = new ArrayList<String>();
      for (int i = 0; i < fst_.getEntriesCount(); i++) {
         FstEntry entry = fst_.getEntryByIndex(i);
         int tag = entry.getTag();
         if (!tags.add(tag)) {
            continue;
         }
         fieldNames.add("" + tag);
      }

      String[] fld = new String[fieldNames.size()];
      fieldNames.toArray(fld);
      return fld;

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
    /**
    *
    * @param field - Searchable Field Tag or -1 for all
    * @param value - Prefix
    * @return
    */
   public boolean populateDictionary(int field, String value) {
        try {
         value = value.trim(); //.toLowerCase();
         if (this.suggestionList == null || value.length() < 1) {
            return false;
         }
         suggestionList.clearSelection();
         DefaultListModel model = (DefaultListModel) suggestionList.getModel();
         model.clear();

         // Populate the dictionary
         String[] fieldNames = null;
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
        
         
//         if (termCaseSensitivity_ == SuggestionsOptionDialog.TERM_CASE_INSENSITIVE) {
            
            params.add(TermParams.TERMS_REGEXP_FLAG, "case_insensitive","unicode_case");
//         } else {
//            params.add(TermParams.TERMS_REGEXP_FLAG, "unicode_case");
//         }

         

//         if (termSorting_ == SuggestionsOptionDialog.SORT_TERM_FIELD_FREQ) {
//            params.add(TermParams.TERMS_SORT, TermParams.TERMS_SORT_INDEX);
//         } else {
             params.add(TermParams.TERMS_SORT, TermParams.TERMS_SORT_COUNT);
//         }
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
   public void showDictionary(JComponent parent) {
      if (!suggestionBox.isShowing()) {
         suggestionBox.setPopupSize(parent.getSize().width, 200);
         suggestionBox.show(parent, 0, parent.getSize().height);
         parent.requestFocus();
      }
   }

   private void suggestionListMouseClicked(java.awt.event.MouseEvent evt) {
      //if (evt.getClickCount() >= 2) // Double click to close
      this.suggestionBox.setVisible(false);
   }

   private void suggestionListValueChanged(javax.swing.event.ListSelectionEvent evt) {
       if (!evt.getValueIsAdjusting() && this.suggestionBox.isShowing() && this.suggestionList.getSelectedValue() != null) {
          // The value is set as tag[freq]\tterm_value,thus we extract the term_value


          ((JTextComponent) this.suggestionBox.getInvoker()).setText(((String) this.suggestionList.getSelectedValue()).split("\t", 2)[1]);
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

      /* Something was typed */
      if (suggestionTimerTask_ != null) {
         suggestionTimerTask_.cancel();
      }

      showRetrievingTerms();
      suggestionTimerTask_ = new SuggestionTimerTask(this, cmbFstEntry,
              txtQuery);
      this.suggestionTimer_.schedule(this.suggestionTimerTask_, 500);
   }

    public void showRetrievingTerms() {
      // Set status text
      final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
      StatusDisplayer.getDefault().setStatusText("Retrieving Dictionary term suggestions ...");
      RepaintManager.currentManager(frame).paintDirtyRegions();

   }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbFstEntry;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel lblIndexContent;
    private javax.swing.JLabel lblIndexName;
    private javax.swing.JLabel lblLastModified;
    private javax.swing.JLabel lblNumFields;
    private javax.swing.JLabel lblNumRecords;
    private javax.swing.JLabel lblNumTerms;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel quickSearchPanel;
    private javax.swing.JScrollPane scrollPane_;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable termsTable_;
    private javax.swing.JTextField txtIndexName;
    private javax.swing.JTextField txtLastModified;
    private javax.swing.JTextField txtNumFields;
    private javax.swing.JTextField txtNumRecords;
    private javax.swing.JTextField txtNumTerms;
    private javax.swing.JTextField txtQuery;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized DictionaryTopComponent getDefault() throws DefaultDBNotFoundException {
        try {
        if (instance != null) {
            instance.close();
            instance = null;
        }
        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
        if (connectionInfo.getDefaultDatabase() != null && instance == null) {
            IDatabase db = connectionInfo.getDefaultDatabase();
            instance = new DictionaryTopComponent(db);
        }
         } catch (Exception ex) {
         //Exceptions.printStackTrace(ex);
         // Do nothing
      }
        return instance;
    }

    /**
     * Obtain the dictionaryTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DictionaryTopComponent findInstance() throws DefaultDBNotFoundException {
        // TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        TopComponent win = getDefault();
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find dictionary component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DictionaryTopComponent) {
            return (DictionaryTopComponent)win;
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

//    public JList getTermsList() {
//        return this.termsList;
//    }

    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() throws DefaultDBNotFoundException {
            return DictionaryTopComponent.getDefault();
        }
    }
     /** We are observer for the database changes */
   public void update(Observable o, Object arg) {
      if (db_.indexHasChanged()) {
       model_.clearCache();

       model_.fireTableDataChanged();
       termsTable_.setModel(model_);
       initIndexInfo();
       termsTable_.updateUI();

//      String msg= "Database was changed !";
//      DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
      }

   }
}
class CopyAction extends AbstractAction {
  JTable table_;

  public CopyAction(JTable table) {
     super("Copy (Ctrl+C)",new ImageIcon("copy.png"));
    table_ = table;
  }
  public void actionPerformed(ActionEvent e) {
    e.setSource(table_); //table is a JTable
    ActionListener copyAction2 = table_.getActionMap().get("copy");
    copyAction2.actionPerformed(e);
  }
}

class SuggestionTimerTask extends TimerTask {

   private DictionaryTopComponent panel;
   private JComboBox cmbSearchableFields = null;
   private JTextComponent txtQuery = null;

   public SuggestionTimerTask(DictionaryTopComponent panel, JComboBox cmbSearchableFields, JTextComponent txtQuery) {
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

            public void run() {
               String value = txtQuery.getText();
               boolean show = panel.populateDictionary(((SearchableField) cmbSearchableFields.getSelectedItem()).tag,
                       value);
               if (show) {
                  panel.showDictionary(txtQuery);
                  // clear status text
                  StatusDisplayer.getDefault().setStatusText(""); // NOI18N
               }
            }
         };
         SwingUtilities.invokeLater(event);
      }

   }
}