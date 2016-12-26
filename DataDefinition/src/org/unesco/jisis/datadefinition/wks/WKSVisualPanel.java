package org.unesco.jisis.datadefinition.wks;


import java.awt.Cursor;
import java.awt.Rectangle;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import org.unesco.jisis.jisiscore.common.WKSModelEx;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.util.ImageUtilities;
import org.unesco.jisis.gui.GuiUtils;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.jisiscore.common.FDTModelEx;
import org.unesco.jisis.gui.EditorDlgActionTableCellEditor;
import org.unesco.jisis.jisiscore.common.TableRowTransferHandler;

class ObservableEx extends Observable {
   // The setChanged() protected method must be overridden to make it public

   @Override
   public synchronized void setChanged() {
      super.setChanged();
   }
}

public final class WKSVisualPanel extends JPanel  {
    
    static final String DOWN_PATH = "org/unesco/jisis/datadefinition/1downarrow.png";
    static final String UP_PATH = "org/unesco/jisis/datadefinition/1uparrow.png";
     static final String TWO_DOWN_PATH = "org/unesco/jisis/datadefinition/2downarrow16.png";
    static final String TWO_UP_PATH = "org/unesco/jisis/datadefinition/2uparrow16.png";

    private FDTModelEx fdtModel_ = null;
    private WKSModelEx wksModel_ = null;
    private boolean wksChanged_ = false;
    private ObservableEx wksVisualPanelObservers_ =  new ObservableEx() { };
    
     private static final Cursor MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR); 
    private Cursor cursor;
        
    /**
     * Creates new form WKSVisualPanel
     */
    public WKSVisualPanel() {
       fdtModel_ = new FDTModelEx();
       wksModel_ = new WKSModelEx();
        initComponents();
        createFdtTableColumns();
        createWksTableColumns();
        setUpWksDisplayColumn();
        GuiUtils.TweakJTable(tblWKS);
        GuiUtils.TweakJTable(tblAvail);
        
       tblWKS.clearSelection();
       btnAdd.setEnabled(true);
       btnAddAll.setEnabled(true);
       btnRemove.setEnabled(false);
       btnRemoveAll.setEnabled(false);
       btnUpWks.setEnabled(false);
       btnDownWks.setEnabled(false);
        if (tblAvail.getRowCount() > 0) {
            tblAvail.setRowSelectionInterval(0, 0);
        }

        // Handle the listeners
      


         // List Selection Listeners
        tblAvail.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                tblWKS.clearSelection();
                btnAdd.setEnabled(true);
                btnAddAll.setEnabled(true);
                btnRemove.setEnabled(false);
                btnRemoveAll.setEnabled(false);
                btnUpWks.setEnabled(false);
                btnDownWks.setEnabled(false);
            }
        });


        tblWKS.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                tblAvail.clearSelection();
                 btnAdd.setEnabled(false);
                btnAddAll.setEnabled(false);
                btnRemove.setEnabled(true);
                btnRemoveAll.setEnabled(true);
                btnUpWks.setEnabled(true);
                btnDownWks.setEnabled(true);
             
            }
        });
        
        // Make JTable scroll automatically, when drag-cursor goes beyond the 
        //component during drag and drop.

        tblWKS.setAutoscrolls(true);

        
        btnAdd.setIcon(new ImageIcon(ImageUtilities.loadImage(DOWN_PATH, true)));
        btnRemove.setIcon(new ImageIcon(ImageUtilities.loadImage(UP_PATH, true)));
        btnAddAll.setIcon(new ImageIcon(ImageUtilities.loadImage(TWO_DOWN_PATH, true)));
        btnRemoveAll.setIcon(new ImageIcon(ImageUtilities.loadImage(TWO_UP_PATH, true)));
    }
    
    public WKSVisualPanel(IDatabase db, String wksName) {

       WorksheetDef wdf = null;
       try {
            wdf = new WorksheetDef(db.getWorksheetDef(wksName));
            fdtModel_ = new FDTModelEx(db.getFieldDefinitionTable());
            wksModel_ = new WKSModelEx(wdf);
        } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
        }
        
        initComponents();

         String recValFormat = wdf.getRecordValidationFormat();
         validationEditorPane.setText(recValFormat);
       
        createFdtTableColumns();
        createWksTableColumns();
        setUpWksDisplayColumn();
        GuiUtils.TweakJTable(tblWKS);
        
        tblWKS.setDragEnabled(true);
        tblWKS.setDropMode(DropMode.INSERT_ROWS);
        tblWKS.setTransferHandler(new TableRowTransferHandler(tblWKS)); 
        GuiUtils.TweakJTable(tblAvail);
        
        btnAdd.setIcon(new ImageIcon(ImageUtilities.loadImage(DOWN_PATH, true)));
        btnRemove.setIcon(new ImageIcon(ImageUtilities.loadImage(UP_PATH, true)));
        btnAddAll.setIcon(new ImageIcon(ImageUtilities.loadImage(TWO_DOWN_PATH, true)));
        btnRemoveAll.setIcon(new ImageIcon(ImageUtilities.loadImage(TWO_UP_PATH, true)));

       tblWKS.clearSelection();
       btnAdd.setEnabled(true);
       btnAddAll.setEnabled(true);
       btnRemove.setEnabled(false);
       btnRemoveAll.setEnabled(false);
       btnUpWks.setEnabled(false);
       btnDownWks.setEnabled(false);
       // List Selection Listeners
        tblAvail.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                tblWKS.clearSelection();
                btnAdd.setEnabled(true);
                btnAddAll.setEnabled(true);
                btnRemove.setEnabled(false);
                btnRemoveAll.setEnabled(false);
                btnUpWks.setEnabled(false);
                btnDownWks.setEnabled(false);
            }
        });


       tblWKS.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
             tblAvail.clearSelection();
             btnAdd.setEnabled(false);
             btnAddAll.setEnabled(false);
             btnRemove.setEnabled(true);
             btnRemoveAll.setEnabled(true);
             btnUpWks.setEnabled(true);
             btnDownWks.setEnabled(true);

          }
       });
       tblAvail.setRowSelectionInterval(0, 0);
    }

    private void createFdtTableColumns() {
      for (int i = 0; i < fdtModel_.getColumnCount(); i++) {
         int w = FDTModelEx.fdtColumns[i].width_;
         TableColumn tc = new TableColumn(i, w, null, null);
         tblAvail.addColumn(tc);
      }
   }

    private void createWksTableColumns() {
      for (int i = 0; i < wksModel_.getColumnCount(); i++) {
         int w = WKSModelEx.wksColumns[i].width_;
         DefaultCellEditor editor = null;
         EditorDlgActionTableCellEditor actionEditor = null;
         if (i == WKSModelEx.VALIDATION_COLUMN_INDEX ||
             i == WKSModelEx.PICKLIST_COLUMN_INDEX   ||
             i == WKSModelEx.DEFAULT_COLUMN_INDEX) {
            JTextField textField = new JTextField();
            textField.setBorder(BorderFactory.createEmptyBorder());
            editor = new DefaultCellEditor(textField);
            editor.setClickCountToStart(1);
            actionEditor = new EditorDlgActionTableCellEditor(editor);
            switch (i) {
               case WKSModelEx.VALIDATION_COLUMN_INDEX:
                  actionEditor.setDialogTitle(NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_FieldValidationEditor"));
                  break;
               case WKSModelEx.PICKLIST_COLUMN_INDEX:
                  actionEditor.setDialogTitle(NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_PickListEditor"));
                  break;
               case WKSModelEx.DEFAULT_COLUMN_INDEX:
                  actionEditor.setDialogTitle(NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_DefaultValueEditor"));
                  break;
            }
         }
         TableColumn tc = new TableColumn(i, w, null, actionEditor);
         tblWKS.addColumn(tc);
      }
   }
      public void setUpWksDisplayColumn() {
        //Set up the editor for the sport cells.
        JComboBox comboBox = new JComboBox();
        comboBox.addItem(""); 
        comboBox.addItem("Text/Textarea");          // 1
        comboBox.addItem("Text(fixed length)");     // 2
        comboBox.addItem("Table");                  // 3
        comboBox.addItem("PasswordS");              // 4 
        comboBox.addItem("Date");                   // 5
        comboBox.addItem("Select simple");          // 6
        comboBox.addItem("Select multiple");        // 7
        comboBox.addItem("Checkbox");               // 8
        comboBox.addItem("Radio Button");           // 9
        comboBox.addItem("HTML Area");              // 10 
         comboBox.addItem("External HTML");         // 11
        comboBox.addItem("Upload File");            // 12
        comboBox.addItem("URL");                    // 13
        comboBox.addItem("Read Only");              // 14
       
        TableColumn displayColumn = tblWKS.getColumnModel().getColumn(WKSModelEx.DISPLAY_COLUMN_INDEX);
        displayColumn.setCellEditor(new DefaultCellEditor(comboBox));

        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        displayColumn.setCellRenderer(renderer);
    }

    private void ensureRowIsVisible(int row) {
      Rectangle vis = getVisibleRect();
      Rectangle cellBounds = tblWKS.getCellRect(row, 0, true);
      vis.y = cellBounds.y;
      vis.height = cellBounds.height;
      tblWKS.scrollRectToVisible(vis);
   }
    public void load(IDatabase db, String wksName) {
        
        try {
            WorksheetDef wdf = new WorksheetDef(db.getWorksheetDef(wksName));
            fdtModel_ = new FDTModelEx(db.getFieldDefinitionTable());
            wksModel_ = new WKSModelEx(wdf);
            String recValFormat = wdf.getRecordValidationFormat();
            validationEditorPane.setText(recValFormat);
            tblWKS.setModel(wksModel_);
        } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
        }
        
    }
    
   @Override
    public String getName() {
        return NbBundle.getMessage(WKSVisualPanel.class, "MSG_DbWorkSheetStep");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        availScrollPane = new javax.swing.JScrollPane();
        tblAvail = new javax.swing.JTable();
        wksScrollPane = new javax.swing.JScrollPane();
        tblWKS = new javax.swing.JTable();
        lblAvail = new javax.swing.JLabel();
        lblWKS = new javax.swing.JLabel();
        lblRecVal = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        validationEditorPane = new javax.swing.JEditorPane();
        jToolBar1 = new javax.swing.JToolBar();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnAddAll = new javax.swing.JButton();
        btnRemoveAll = new javax.swing.JButton();
        btnUpWks = new javax.swing.JButton();
        btnDownWks = new javax.swing.JButton();

        tblAvail.setAutoCreateColumnsFromModel(false);
        tblAvail.setModel(fdtModel_);
        availScrollPane.setViewportView(tblAvail);

        tblWKS.setAutoCreateColumnsFromModel(false);
        tblWKS.setModel(wksModel_);
        wksScrollPane.setViewportView(tblWKS);

        lblAvail.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblAvail, org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "LBL_Avail")); // NOI18N

        lblWKS.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblWKS, org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "LBL_WKSDef")); // NOI18N

        lblRecVal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblRecVal, org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "LBL_RecLevelVal")); // NOI18N

        jScrollPane1.setViewportView(validationEditorPane);

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/datadefinition/wks/1downarrow.png"))); // NOI18N
        btnAdd.setToolTipText(org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "BTN_Add")); // NOI18N
        btnAdd.setPreferredSize(new java.awt.Dimension(30, 25));
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAdd);

        btnRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/datadefinition/wks/1uparrow.png"))); // NOI18N
        btnRemove.setToolTipText(org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "BTN_Remove")); // NOI18N
        btnRemove.setPreferredSize(new java.awt.Dimension(30, 25));
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });
        jToolBar1.add(btnRemove);

        btnAddAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/datadefinition/wks/2downarrow16.png"))); // NOI18N
        btnAddAll.setToolTipText(org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "BTN_ADD_ALL")); // NOI18N
        btnAddAll.setPreferredSize(new java.awt.Dimension(30, 25));
        btnAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAllActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAddAll);

        btnRemoveAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/datadefinition/wks/2uparrow16.png"))); // NOI18N
        btnRemoveAll.setToolTipText(org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "BTN_REMOVE_ALL")); // NOI18N
        btnRemoveAll.setPreferredSize(new java.awt.Dimension(30, 25));
        btnRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveAllActionPerformed(evt);
            }
        });
        jToolBar1.add(btnRemoveAll);

        btnUpWks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/datadefinition/wks/1uparrow.png"))); // NOI18N
        btnUpWks.setToolTipText("Move Up Selected WKS Entry");
        btnUpWks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpWksActionPerformed(evt);
            }
        });

        btnDownWks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/datadefinition/wks/1downarrow.png"))); // NOI18N
        btnDownWks.setToolTipText("Move Down Selected WKS Entry");
        btnDownWks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownWksActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(availScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblRecVal)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblAvail, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(lblWKS)
                                .addGap(29, 29, 29)
                                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(wksScrollPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUpWks, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDownWks, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAvail)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(availScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblWKS)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wksScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(btnUpWks)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDownWks)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblRecVal)
                        .addGap(54, 54, 54))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int selRow = tblWKS.getSelectedRow();
        if (selRow != -1) {
            WKSModelEx wksModel = (WKSModelEx) tblWKS.getModel();
            wksModel.removeRow(selRow);
            wksModel.fireTableDataChanged();
            setWksChanged(true);
            tblWKS.setRowSelectionInterval(selRow,selRow);
        }
    }//GEN-LAST:event_btnRemoveActionPerformed
    
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        int selRow = tblAvail.getSelectedRow();
        if (selRow != -1) {
            int tag = Integer.parseInt(tblAvail.getModel().getValueAt(selRow, 0).toString());
            String desc = tblAvail.getModel().getValueAt(selRow, 1).toString();
            
            WKSModelEx wksModel = (WKSModelEx) tblWKS.getModel();
            wksModel.addRow(tag, desc, "","", "", "", "", "");
            wksModel.fireTableDataChanged();
            setWksChanged(true);
            
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAllActionPerformed
      
        int n = tblAvail.getRowCount();
        WKSModelEx wksModel = (WKSModelEx) tblWKS.getModel();
        for (int i=0; i<n; i++) {
            int tag = Integer.parseInt(tblAvail.getModel().getValueAt(i, 0).toString());
            String desc = tblAvail.getModel().getValueAt(i, 1).toString();
               
            wksModel.addRow(tag, desc, "", "", "", "", "", "");
        }
        wksModel.fireTableDataChanged();
        setWksChanged(true);       
}//GEN-LAST:event_btnAddAllActionPerformed

    private void btnRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveAllActionPerformed
       WKSModelEx wksModel = (WKSModelEx) tblWKS.getModel();
       wksModel.removeAll();
       wksModel.fireTableDataChanged();
       setWksChanged(true);
       tblAvail.setRowSelectionInterval(0,0);
}//GEN-LAST:event_btnRemoveAllActionPerformed

    /**
     * Move down the selected entry in the table if The Up Arrow is clicked 
     * @param evt 
     */
    private void btnUpWksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpWksActionPerformed
           int selRow = tblWKS.getSelectedRow();
        if (selRow != -1) {
            WKSModelEx wksModel = (WKSModelEx) tblWKS.getModel();
            if (wksModel.moveRowDown(selRow)) {
                wksModel.fireTableDataChanged();
                ensureRowIsVisible(selRow-1);
                tblWKS.setRowSelectionInterval(selRow - 1, selRow - 1);
                setWksChanged(true);
            }
        }
    }//GEN-LAST:event_btnUpWksActionPerformed
    /**
     * Move up the selected entry in the table if The Down Arrow is clicked 
     * @param evt 
     */
    private void btnDownWksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownWksActionPerformed
          int selRow = tblWKS.getSelectedRow();
        if (selRow != -1) {
            WKSModelEx wksModel = (WKSModelEx) tblWKS.getModel();
            if (wksModel.moveRowUp(selRow)) {
                wksModel.fireTableDataChanged();
                ensureRowIsVisible(selRow+1);
                // Select the moved row
                tblWKS.setRowSelectionInterval(selRow + 1, selRow + 1);
                setWksChanged(true);
            }
        } 
    }//GEN-LAST:event_btnDownWksActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane availScrollPane;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnAddAll;
    private javax.swing.JButton btnDownWks;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnRemoveAll;
    private javax.swing.JButton btnUpWks;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblAvail;
    private javax.swing.JLabel lblRecVal;
    private javax.swing.JLabel lblWKS;
    private javax.swing.JTable tblAvail;
    private javax.swing.JTable tblWKS;
    private javax.swing.JEditorPane validationEditorPane;
    private javax.swing.JScrollPane wksScrollPane;
    // End of variables declaration//GEN-END:variables
    
    public void setAvailableFields(FDTModelEx fdtModel) {
        tblAvail.setModel(fdtModel);
        tblAvail.updateUI();
    }
    
    public WKSModelEx getWKSModel() {
        return (WKSModelEx) tblWKS.getModel();
    }
    
    public String getRecordValFormat() {
        return validationEditorPane.getText();
    }

   public void valueChanged(ListSelectionEvent e) {



      throw new UnsupportedOperationException("Not supported yet.");
   }
    public void setWksChanged(boolean b) {
      wksChanged_ = b;
      changeNotify();

   }

   public boolean isWksChanged() {
      return wksModel_.hasTableDataChanged();
   }
   
    public void addObserver(Observer newObserver) {
      wksVisualPanelObservers_.addObserver(newObserver);
   }

   protected void changeNotify() {
      System.out.println("NotifyObservers");
      wksVisualPanelObservers_.setChanged();
      wksVisualPanelObservers_.notifyObservers();
   }

   public class SelectionListener implements ListSelectionListener {
      JTable table;
      
   // It is necessary to keep the table since it is not possible
   // to determine the table from the event's source
      SelectionListener(JTable table) {
         this.table = table;
      }
      public void valueChanged(ListSelectionEvent e) {
         // If cell selection is enabled, both row and column change events are fired
         if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
            // Column selection changed
            int first = e.getFirstIndex();
            int last = e.getLastIndex();
         } else if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed() ){
            // Row selection changed
            int first = e.getFirstIndex();
            int last = e.getLastIndex();
         }
         if (e.getValueIsAdjusting()) {
            // The mouse button has not yet been released
         }
      }
   }
    
}

