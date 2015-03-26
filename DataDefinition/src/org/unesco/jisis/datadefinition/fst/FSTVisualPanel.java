package org.unesco.jisis.datadefinition.fst;

import org.unesco.jisis.jisiscore.common.FSTModelEx;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.util.ImageUtilities;
import org.unesco.jisis.gui.GuiUtils;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.FieldSelectionTable;

import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.jisiscore.common.FDTModelEx;
import org.unesco.jisis.gui.EditorDlgActionTableCellEditor;

 


public final class FSTVisualPanel extends JPanel  implements TableModelListener {
    
    static final String DOWN_PATH = "org/unesco/jisis/datadefinition/1downarrow.png";
    static final String UP_PATH = "org/unesco/jisis/datadefinition/1uparrow.png";
 
    private boolean changed_ = false;
    
    /** Creates new form DbCreateVisualPanel4 */
   @SuppressWarnings("static-access")
    public FSTVisualPanel() {
        initComponents();
        
        btnAdd.setIcon(new ImageIcon(ImageUtilities.loadImage(DOWN_PATH, true)));
        btnRemove.setIcon(new ImageIcon(ImageUtilities.loadImage(UP_PATH, true)));
        
        tblFST.setModel(new FSTModelEx());
        
        buildFstTable();
         
    }
   public FSTVisualPanel(IDatabase db) {
        initComponents();
          
        btnAdd.setIcon(new ImageIcon(ImageUtilities.loadImage(DOWN_PATH, true)));
        btnRemove.setIcon(new ImageIcon(ImageUtilities.loadImage(UP_PATH, true)));
        
        load(db);
        
        buildFstTable();
     
    }
   private void buildFstTable() {
      tblFST.getModel().addTableModelListener(this);
      FSTModelEx model = (FSTModelEx) tblFST.getModel();
     
      JComboBox techComboBox = new JComboBox(FSTModelEx.techniques);
      DefaultCellEditor techEditor = new DefaultCellEditor(techComboBox);
      //techEditor.setClickCountToStart(2);
 EditorDlgActionTableCellEditor actionEditor = null;
 DefaultCellEditor defaultCellEditor = null;
 TableColumn column = null;

      for (int k = 0; k < FSTModelEx.fstColumns.length; k++) {
         DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
         renderer.setHorizontalAlignment(FSTModelEx.fstColumns[k].alignment_);

         switch(k) {
            case FSTModelEx.ID_COLUMN_INDEX:
               column = new TableColumn(k,FSTModelEx.fstColumns[k].width_,
                       renderer, null);
               break;
            case FSTModelEx.NAME_COLUMN_INDEX:
               column = new TableColumn(k,FSTModelEx.fstColumns[k].width_,
                       renderer, null);
               break;
            case FSTModelEx.TECHNIQUE_COLUMN_INDEX:
               column = new TableColumn(k,FSTModelEx.fstColumns[k].width_,
                       renderer, techEditor);
               break;
               
            case FSTModelEx.PFT_COLUMN_INDEX: 
               JTextField textField = new JTextField();
               textField.setBorder(BorderFactory.createEmptyBorder());
               defaultCellEditor = new DefaultCellEditor(textField);
               defaultCellEditor.setClickCountToStart(1);
               actionEditor = new EditorDlgActionTableCellEditor(defaultCellEditor);
               column = new TableColumn(k,FSTModelEx.fstColumns[k].width_,
                       renderer, actionEditor);
         }
         tblFST.addColumn(column);
      }
      GuiUtils.TweakJTable(tblAvail);
      GuiUtils.TweakJTable(tblFST);
      GuiUtils.makeColumnHeaderBold(tblFST);
      //tblFST.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
   }
    
    public void load(IDatabase db) {
        
        try {
            this.setFormatTxt(db);
            FDTModelEx fdtModel = new FDTModelEx(db.getFieldDefinitionTable());
            tblAvail.setModel(fdtModel);
            fdtModel.fireTableDataChanged();
            FieldSelectionTable fst = db.getFieldSelectionTable();
            FSTModelEx fstModel = new FSTModelEx(fst);
            tblFST.setModel(fstModel);
            fstModel.fireTableDataChanged();
            tblFST.getModel().addTableModelListener(this);
            chkStoreRecordInIndex.setSelected((fst.getStoreRecordInIndex()==0) ? false : true);
            chkMakeCatchallField.setSelected((fst.getMakeCatchallField()==0) ? false : true);         
        } catch (DbException dbe) {
            new GeneralDatabaseException(dbe).displayWarning();
        }
    }
    
   @Override
    public String getName() {
        return NbBundle.getMessage(FSTVisualPanel.class, "MSG_DbFSTStep");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblAvailable = new javax.swing.JLabel();
        availScrollPane = new javax.swing.JScrollPane();
        tblAvail = new javax.swing.JTable();
        ctrlPanel = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        chkStoreRecordInIndex = new javax.swing.JCheckBox();
        chkMakeCatchallField = new javax.swing.JCheckBox();
        fstScrollPane = new javax.swing.JScrollPane();
        tblFST = new javax.swing.JTable();
        lblFST = new javax.swing.JLabel();
        lblDefFormat = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        org.openide.awt.Mnemonics.setLocalizedText(lblAvailable, org.openide.util.NbBundle.getMessage(FSTVisualPanel.class, "LBL_Avail")); // NOI18N

        tblAvail.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        availScrollPane.setViewportView(tblAvail);

        ctrlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnAdd.setToolTipText(org.openide.util.NbBundle.getMessage(FSTVisualPanel.class, "HINT_Add")); // NOI18N
        btnAdd.setPreferredSize(new java.awt.Dimension(30, 25));
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setToolTipText(org.openide.util.NbBundle.getMessage(FSTVisualPanel.class, "HINT_Remove")); // NOI18N
        btnRemove.setPreferredSize(new java.awt.Dimension(30, 25));
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chkStoreRecordInIndex, "Store the record in the index");
        chkStoreRecordInIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkStoreRecordInIndexActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chkMakeCatchallField, "Make a catch-all field");
        chkMakeCatchallField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMakeCatchallFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ctrlPanelLayout = new javax.swing.GroupLayout(ctrlPanel);
        ctrlPanel.setLayout(ctrlPanelLayout);
        ctrlPanelLayout.setHorizontalGroup(
            ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ctrlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkStoreRecordInIndex)
                .addGap(18, 18, 18)
                .addComponent(chkMakeCatchallField, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        ctrlPanelLayout.setVerticalGroup(
            ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ctrlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(chkStoreRecordInIndex)
                        .addComponent(chkMakeCatchallField)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblFST.setAutoCreateColumnsFromModel(false);
        fstScrollPane.setViewportView(tblFST);

        org.openide.awt.Mnemonics.setLocalizedText(lblFST, org.openide.util.NbBundle.getMessage(FSTVisualPanel.class, "LBL_FST")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblDefFormat, org.openide.util.NbBundle.getMessage(FSTVisualPanel.class, "LBL_DefFormat")); // NOI18N

        jScrollPane1.setViewportView(jEditorPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(ctrlPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fstScrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAvailable, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFST, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(availScrollPane)
                    .addComponent(lblDefFormat, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblAvailable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(availScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ctrlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFST)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fstScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDefFormat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int selRow = tblFST.getSelectedRow();
        if (selRow == -1 || tblFST.getRowCount() <= 0) {
           return;
        }
        FSTModelEx fstModel = (FSTModelEx) tblFST.getModel();
        fstModel.removeRow(selRow);
        if (tblFST.getRowCount()>0) {
           int row = (selRow==0) ? 0 : selRow-1;
           tblFST.setRowSelectionInterval(row, row);
        }
        tblFST.updateUI();
        
    }//GEN-LAST:event_btnRemoveActionPerformed
    private void ensureFdtRowIsVisible(int row) {
      Rectangle vis = getVisibleRect();
      Rectangle cellBounds = tblAvail.getCellRect(row, 0, true);
      vis.y = cellBounds.y;
      vis.height = cellBounds.height;
      tblAvail.scrollRectToVisible(vis);
   }
    private void ensureFstRowIsVisible(int row) {
      Rectangle vis = getVisibleRect();
      Rectangle cellBounds = tblFST.getCellRect(row, 0, true);
      vis.y = cellBounds.y;
      vis.height = cellBounds.height;
      tblFST.scrollRectToVisible(vis);
   }
    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        int selRow = tblAvail.getSelectedRow();
        if (selRow == -1) {
           return;
        }
        int tag = Integer.parseInt(tblAvail.getModel().getValueAt(selRow, 0).toString());
        FSTModelEx fstModel = (FSTModelEx) tblFST.getModel();
        fstModel.addRow(tag, "", 1, "v" + Integer.toString(tag));
        
        tblFST.setRowSelectionInterval(tblFST.getRowCount()-1, tblFST.getRowCount()-1);
        tblFST.updateUI();
        ensureFstRowIsVisible(tblFST.getRowCount()-1);
        /** Move selection on FDT next row if we can */
        int row = (selRow==tblAvail.getRowCount()-1) ? selRow : selRow+1;
        tblAvail.setRowSelectionInterval(row, row);
        tblAvail.updateUI();
        ensureFdtRowIsVisible(row);
    }//GEN-LAST:event_btnAddActionPerformed

   private void chkStoreRecordInIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkStoreRecordInIndexActionPerformed
      // TODO add your handling code here:
      changed_ = true;
      // Simulate a change in the FST table to enable the Save button
      FSTModelEx model = (FSTModelEx) tblFST.getModel();
      model.fireTableDataChanged();
   }//GEN-LAST:event_chkStoreRecordInIndexActionPerformed

   private void chkMakeCatchallFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMakeCatchallFieldActionPerformed
      // TODO add your handling code here:
        changed_ = true;
      // Simulate a change in the FST table to enable the Save button
      FSTModelEx model = (FSTModelEx) tblFST.getModel();
      model.fireTableDataChanged();
   }//GEN-LAST:event_chkMakeCatchallFieldActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane availScrollPane;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnRemove;
    private javax.swing.JCheckBox chkMakeCatchallField;
    private javax.swing.JCheckBox chkStoreRecordInIndex;
    private javax.swing.JPanel ctrlPanel;
    private javax.swing.JScrollPane fstScrollPane;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAvailable;
    private javax.swing.JLabel lblDefFormat;
    private javax.swing.JLabel lblFST;
    private javax.swing.JTable tblAvail;
    private javax.swing.JTable tblFST;
    // End of variables declaration//GEN-END:variables
    
    public void setAvailableFields(FDTModelEx fdtModel) {
        tblAvail.setModel(fdtModel);
        tblAvail.updateUI();
    }
    
   public FSTModelEx getFSTModel() {
      FSTModelEx model = (FSTModelEx) tblFST.getModel();
      model.setMakeCatchallField(chkMakeCatchallField.isSelected() ? 1 : 0);
      model.setStoreRecordInIndex(chkStoreRecordInIndex.isSelected() ? 1 : 0);
      return model;

   }
    
    public String getDefaultFormat() {
        return jEditorPane1.getText();
    }
    
    public void setFormatTxt(IDatabase db) {
        try {
            jEditorPane1.setText(db.getPrintFormat(db.getDefaultPrintFormatName()));
        } catch (DbException ex) {
            ex.printStackTrace();
        }
    }

   public void tableChanged(TableModelEvent e) {
      changed_ = true;
      //tblFST.updateUI();
     
   }
   
   public boolean hasChanged() {
      return changed_;
   }
   public void setChanged(boolean changed) {
      changed_ = changed;
   }
}

