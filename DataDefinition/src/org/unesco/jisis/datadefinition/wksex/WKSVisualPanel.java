package org.unesco.jisis.datadefinition.wksex;

import org.unesco.jisis.jisisutils.gui.JTableScrolling;
import org.unesco.jisis.jisisutils.gui.Scrolling;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.unesco.jisis.gui.GuiUtils;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.gui.EditorDlgActionTableCellEditor;
import org.unesco.jisis.jisiscore.common.FDTModelEx;


public final class WKSVisualPanel extends JPanel {

   static final String DOWN_PATH = "org/unesco/jisis/datadefinition/1downarrow.png";
   static final String UP_PATH = "org/unesco/jisis/datadefinition/1uparrow.png";
   static final String TWO_DOWN_PATH = "org/unesco/jisis/datadefinition/2downarrow16.png";
   static final String TWO_UP_PATH = "org/unesco/jisis/datadefinition/2uparrow16.png";
   private FDTModelEx fdtModel_ = null;
   

   String wksName_ = null;
   WorksheetDef wdf = null;
   FieldDefinitionTable fdt = null;
   private Outline outline;
   private WksFieldTreeModel treeModel;
   private OutlineModel outlineModel;
   private WksRowModel rowModel;

   /**
    * Creates new form WKSVisualPanel
    */
   public WKSVisualPanel() {
      fdtModel_ = new FDTModelEx();
     
      initComponents();
      createFdtTableColumns();
      
      GuiUtils.TweakJTable(tblAvail);
    
      // List Selection Listeners
      tblAvail.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

         public void valueChanged(ListSelectionEvent e) {
         }
      });


      outline.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

         public void valueChanged(ListSelectionEvent e) {
         }
      });




   }

   public WKSVisualPanel(IDatabase db, String wksName) {

      wksName_ = wksName;
      try {
         wdf = db.getWorksheetDef(wksName);
         fdt = db.getFieldDefinitionTable();
         fdtModel_ = new FDTModelEx(fdt);
         
      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }

      initComponents();

      String recValFormat = wdf.getRecordValidationFormat();
      validationEditorPane.setText(recValFormat);

      createFdtTableColumns();

      GuiUtils.TweakJTable(tblAvail);

      tblAvail.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

         public void valueChanged(ListSelectionEvent e) {
            boolean en = outline.getSelectedRow() != -1 && tblAvail.getSelectedRow() != -1;
            btnAddField.setEnabled(en);

         }
      });

      // Set the Outline TreeTable

      treeModel = new WksFieldTreeModel(wksName, wdf, fdt);
      rowModel = new WksRowModel();

      outlineModel = DefaultOutlineModel.createOutlineModel(treeModel, rowModel);

      outline = new Outline();

      outline.setRootVisible(true);

      outline.setModel(outlineModel);

      setWksOutlineColumns();
      wksScrollPane.setViewportView(outline);

      wksScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

      WksTreeNode root = (WksTreeNode) treeModel.getRoot();

      outline.setRowSelectionInterval(0, 0);


      //GuiUtils.TweakJTable(outline);

      // List Selection Listeners
  
      outline.getSelectionModel().addListSelectionListener(
              new ListSelectionListener() {

                 public void valueChanged(ListSelectionEvent e) {
                    boolean en = outline.getSelectedRow() != -1 && tblAvail.getSelectedRow() != -1;
                    btnAddField.setEnabled(en);
                    btnRemoveField.setEnabled(outline.getSelectedRow() != -1 && outline.getRowCount() > 1);
                    btnClearWks.setEnabled(outline.getRowCount() > 1);

  
                 }
              });



      btnAddField.setEnabled(false);
      btnRemoveField.setEnabled(false);
   }

   private void createFdtTableColumns() {
      tblAvail.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      for (int i = 0; i < fdtModel_.getColumnCount(); i++) {
         int w = FDTModelEx.fdtColumns[i].width_;
         TableColumn tc = new TableColumn(i, w, null, null);
         tblAvail.addColumn(tc);
      }
   }

   private void setWksOutlineColumns() {

      outline.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      TableColumnModel tcm = outline.getColumnModel();

      for (int i = 0; i < outline.getColumnCount(); i++) {
         TableColumn tc = tcm.getColumn(i);
         if (i==0) {
            tc.setPreferredWidth(100);
         }
         else if (i == rowModel.getColumnIndex("nodeType")) {
            // Tree node
            tc.setPreferredWidth(100);
         } else if (i == rowModel.getColumnIndex("indicators")) {
            tc.setPreferredWidth(50);
         } else if (i == rowModel.getColumnIndex("repeatable")) {
            tc.setPreferredWidth(50);
         } else if (i == rowModel.getColumnIndex("firstSubfield")) {
            tc.setPreferredWidth(75);
         } else if (i == rowModel.getColumnIndex("fieldType")) {
            TableCellRenderer renderer = new DefaultTableCellRenderer();

            JComboBox cmbType = new JComboBox(Global.fieldTypes);
            cmbType.setRequestFocusEnabled(false);
            TableCellEditor editor = new DefaultCellEditor(cmbType);
            ((DefaultCellEditor)editor).setClickCountToStart(2);
            tc.setCellEditor(editor);
            tc.setCellRenderer(renderer);
            tc.setPreferredWidth(100);
         } else if (i == rowModel.getColumnIndex("description")) {
            tc.setPreferredWidth(300);
         } else if (i == rowModel.getColumnIndex("displayControl")) {
            tc.setPreferredWidth(100);
            setUpWksDisplayColumn();
         } else if (i == rowModel.getColumnIndex("helpMsg")) {
            tc.setPreferredWidth(150);
         }
         else if (i == rowModel.getColumnIndex("valFormat") 
               || i == rowModel.getColumnIndex("pickList")
               || i == rowModel.getColumnIndex("defaultValue")) {
           
          
            JTextField textField = new JTextField();
            textField.setBorder(BorderFactory.createEmptyBorder());
            DefaultCellEditor editor = new DefaultCellEditor(textField);
            editor.setClickCountToStart(1);
            EditorDlgActionTableCellEditor actionEditor = new EditorDlgActionTableCellEditor(editor);
            tc.setCellEditor(actionEditor);
            tc.setPreferredWidth(250);
         }
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
       
        TableColumn displayColumn = outline.getColumnModel().getColumn(rowModel.getColumnIndex("displayControl"));
        displayColumn.setCellEditor(new DefaultCellEditor(comboBox));

        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        displayColumn.setCellRenderer(renderer);
    }
    
    public void load(IDatabase db, String wksName) {
        wksName_ = wksName;
        try {
            wdf = db.getWorksheetDef(wksName);
            fdt = db.getFieldDefinitionTable();
            fdtModel_ = new FDTModelEx(fdt);
            
            String recValFormat = wdf.getRecordValidationFormat();
            validationEditorPane.setText(recValFormat);

            treeModel = new WksFieldTreeModel(wksName, wdf, fdt);

            rowModel = new WksRowModel();
	    outlineModel = DefaultOutlineModel.createOutlineModel(treeModel,
				rowModel, true);

            outline.setModel(outlineModel);
            setWksOutlineColumns();

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
        lblAvail = new javax.swing.JLabel();
        lblRecVal = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        validationEditorPane = new javax.swing.JEditorPane();
        jToolBar1 = new javax.swing.JToolBar();
        lblWKS = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnAddField = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnAddAllFields = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnRemoveField = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btnClearWks = new javax.swing.JButton();

        tblAvail.setAutoCreateColumnsFromModel(false);
        tblAvail.setModel(fdtModel_);
        availScrollPane.setViewportView(tblAvail);

        lblAvail.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblAvail, org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "LBL_Avail")); // NOI18N

        lblRecVal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblRecVal, org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "LBL_RecLevelVal")); // NOI18N

        jScrollPane1.setViewportView(validationEditorPane);

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        lblWKS.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblWKS, org.openide.util.NbBundle.getMessage(WKSVisualPanel.class, "LBL_WKSDef")); // NOI18N
        jToolBar1.add(lblWKS);
        jToolBar1.add(jSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(btnAddField, "Add");
        btnAddField.setToolTipText("Add Fields Selected in the FDT");
        btnAddField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFieldActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAddField);
        jToolBar1.add(jSeparator2);

        org.openide.awt.Mnemonics.setLocalizedText(btnAddAllFields, "Add All");
        btnAddAllFields.setToolTipText("Add All FDT Fields");
        btnAddAllFields.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAllFieldsActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAddAllFields);
        jToolBar1.add(jSeparator3);

        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveField, "Remove");
        btnRemoveField.setToolTipText("Remove Selected Worksheet Field and associated subfields");
        btnRemoveField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveFieldActionPerformed(evt);
            }
        });
        jToolBar1.add(btnRemoveField);
        jToolBar1.add(jSeparator4);

        org.openide.awt.Mnemonics.setLocalizedText(btnClearWks, "Remove All");
        btnClearWks.setToolTipText("Remove ALL Worksheet Fields and associated subfields");
        btnClearWks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearWksActionPerformed(evt);
            }
        });
        jToolBar1.add(btnClearWks);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(availScrollPane)
                    .addComponent(wksScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lblRecVal)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblAvail)
                                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblAvail)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(availScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wksScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
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

   public WksTreeNode  getSelectedNode() {
       return ((WksTreeNode) outline.getValueAt(outline.getSelectedRow(), 0));
     }
  




    private void btnAddFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFieldActionPerformed
      int[] selRows = tblAvail.getSelectedRows();
        if (selRows.length == 0) {
           String label = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_NoFdtFieldSelected");
            String title = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_WorksheetErrorDialogTitle");
            NotifyDescriptor d =
                    new NotifyDescriptor.Confirmation(label, title,
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);

            return;
        }
      
       WksTreeNode root = (WksTreeNode) treeModel.getRoot();
       boolean expand = (root.getChildCount() == 0);
       if (root.getChildCount() > 0) {
          // Check if selected fields are already in the worksheet
          for (int i = 0; i < selRows.length; i++) {
             int selRow = selRows[i];
             int tag = Integer.parseInt(tblAvail.getModel().getValueAt(selRow, 0).toString());
             for (WksTreeNode node = (WksTreeNode) root.getFirstChild(); node != null; node = (WksTreeNode) node.getNextSibling()) {
                Properties data = (Properties) node.getUserObject();
                int wksTag = Integer.valueOf(data.getProperty("tag"));
                if (wksTag == tag) {
                   String label = NbBundle.getMessage(WKSVisualPanel.class,
                           "MSG_FieldAlreadyInWorksheet", tag);
                   String title = NbBundle.getMessage(WKSVisualPanel.class,
                           "MSG_WorksheetErrorDialogTitle");
                   NotifyDescriptor d =
                           new NotifyDescriptor.Confirmation(label, title,
                           NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                   DialogDisplayer.getDefault().notify(d);

                   return;
                }
             }
          }
       }
        int selOutlineRow = outline.getSelectedRow();
        if (selOutlineRow == -1) {
           String label = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_NoWksFieldSelected");
            String title = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_WorksheetErrorDialogTitle");
            NotifyDescriptor d =
                    new NotifyDescriptor.Confirmation(label, title,
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);

            return;
        }
        WksTreeNode selNode = getSelectedNode();
        Properties data = (Properties) selNode.getUserObject();
        String nodeType = data.getProperty("nodeType");
        if (!("fieldNode".equals(nodeType)) && !("rootNode".equals(nodeType))) {
           String label = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_YouShouldSelectAFieldNode");
            String title = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_WorksheetErrorDialogTitle");
            NotifyDescriptor d =
                    new NotifyDescriptor.Confirmation(label, title,
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);

            return;
        }
        WksTreeNode node = null;
        WksTreeNode childNode = selNode;
       for (int i = 0; i < selRows.length; i++) {
          int selRow = selRows[i];

          int tag = Integer.parseInt(tblAvail.getModel().getValueAt(selRow, 0).toString());

          WorksheetDef.WorksheetField wksField = wdf.getFieldByTag(tag);
          if (wksField == null) {
             wksField = new WorksheetDef.WorksheetField(tag,false, false,
                     (Integer)(tblAvail.getModel().getValueAt(selRow, 2)),
                     (String) (tblAvail.getModel().getValueAt(selRow, 1)), // Description
                     "","",
                     "", "", "", "");
          } else {
             // TODO
          }
          FieldDefinition fdtEntry = fdt.getFieldByTag(tag);
          node = treeModel.fillModelFromField(wksField, fdtEntry);
          
          if (root.getChildCount() == 0) {
             root.add(node);
          } else {
             int index = treeModel.getIndexOfChild(root, childNode);
             root.insert(node, index + 1);
          }
          int index = treeModel.getIndexOfChild(root, node);
          treeModel.fireChildAdded(new TreePath(root), index, root);
          childNode = node;
       }
        // Select the next field in the FDT table
        int row = selRows[selRows.length-1];
        if(row+1 < tblAvail.getRowCount()) {
           tblAvail.setRowSelectionInterval(row+1, row+1);
           Scrolling.scrollVertically(tblAvail, JTableScrolling.getRowBounds(tblAvail, row+1));
        }
        if (expand) {
           outline.expandPath(new TreePath(root));
           treeModel.firePathChanged(new TreePath(root));
        }
        // Select the last added node
       
        row = root.getIndex(node);
       if (row >= 0 && row < outline.getRowCount() - 1) {
          outline.setRowSelectionInterval(row + 1, row + 1);
          Scrolling.scrollVertically(outline, JTableScrolling.getRowBounds(outline, row + 1));
       }
    }//GEN-LAST:event_btnAddFieldActionPerformed

    private void btnRemoveFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveFieldActionPerformed
       int selOutlineRow = outline.getSelectedRow();
        if (selOutlineRow == -1) {
           String label = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_NoWksFieldSelected");
            String title = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_WorksheetErrorDialogTitle");
            NotifyDescriptor d =
                    new NotifyDescriptor.Confirmation(label, title,
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);


            return;
        }
        WksTreeNode selNode = getSelectedNode();
        Properties data = (Properties) selNode.getUserObject();
        String nodeType = data.getProperty("nodeType");
        if (!("fieldNode".equals(nodeType)) && !("rootNode".equals(nodeType))) {
           String label = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_YouShouldSelectAFieldNode");
            String title = NbBundle.getMessage(WKSVisualPanel.class,
                    "MSG_WorksheetErrorDialogTitle");
            NotifyDescriptor d =
                    new NotifyDescriptor.Confirmation(label, title,
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);

            return;
        }
        WksTreeNode root = (WksTreeNode) treeModel.getRoot();
        int index = treeModel.getIndexOfChild(root, selNode);
         root.remove(index);
         treeModel.fireChildRemoved(new TreePath(root), index, root);
         outline.setRowSelectionInterval(index, index);
    }//GEN-LAST:event_btnRemoveFieldActionPerformed

    private void btnClearWksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearWksActionPerformed
       WksTreeNode root = (WksTreeNode) treeModel.getRoot();
       int n = root.getChildCount();
       int[] sels = new int[n];

       int i=0;
       for (WksTreeNode node = (WksTreeNode) root.getFirstChild(); node!=null; node = (WksTreeNode)node.getNextSibling()) {
          sels[i] = root.getIndex(node);
          i++;
       }
       for (i=n-1; i>=0; i--) {
           root.remove(sels[i]);
           treeModel.fireChildRemoved(new TreePath(root), sels[i], root);
       }
       outline.setRowSelectionInterval(0, 0);
    }//GEN-LAST:event_btnClearWksActionPerformed

    private void btnAddAllFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAllFieldsActionPerformed

       
       WksTreeNode root = (WksTreeNode) treeModel.getRoot();
       if (root.getChildCount()>0) {
          btnClearWksActionPerformed(null);
       }
       for (int i = 0; i < fdt.getFieldsCount(); i++) {
          FieldDefinition fdtEntry = fdt.getFieldByIndex(i);
          int tag = fdtEntry.getTag();

          WorksheetDef.WorksheetField wksField = new WorksheetDef.WorksheetField(tag,
                  fdtEntry.isRepeatable(),
                  false,
                  fdtEntry.getType(),
                  fdtEntry.getName(), // Description
                  "","",
                  "", "", "", "");

          WksTreeNode node = treeModel.fillModelFromField(wksField, fdtEntry);

          if (root.getChildCount() == 0) {
             root.add(node);
          } else {
             int index = treeModel.getIndexOfChild(root, root.getLastChild());
             root.insert(node, index + 1);
          }
          int index = treeModel.getIndexOfChild(root, node);
          treeModel.fireChildAdded(new TreePath(root), index, root);
       }
       outline.expandPath(new TreePath(root));
       treeModel.firePathChanged(new TreePath(root));


    }//GEN-LAST:event_btnAddAllFieldsActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane availScrollPane;
    private javax.swing.JButton btnAddAllFields;
    private javax.swing.JButton btnAddField;
    private javax.swing.JButton btnClearWks;
    private javax.swing.JButton btnRemoveField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblAvail;
    private javax.swing.JLabel lblRecVal;
    private javax.swing.JLabel lblWKS;
    private javax.swing.JTable tblAvail;
    private javax.swing.JEditorPane validationEditorPane;
    private javax.swing.JScrollPane wksScrollPane;
    // End of variables declaration//GEN-END:variables
    
    public void setAvailableFields(FDTModelEx fdtModel) {
        tblAvail.setModel(fdtModel);
        tblAvail.updateUI();
    }

   public WorksheetDef getWorksheetDef() {
      WorksheetDef worksheetDef = new WorksheetDef(wksName_);
      WksTreeNode root = (WksTreeNode) treeModel.getRoot();
      int n = root.getChildCount();
      WorksheetDef.WorksheetField wksField = null;
      WorksheetDef.WorksheetSubField wksSubfield = null;
      int tag = 0;
      boolean repeatable = false;
      boolean descriptors = false;
      String fieldType = "";
      String description = "";
      String displayControl = "";
      String size = "";
      String defaultValue = "";
      String helpMsg = "";
      String valFormat = "";
      String pickList = "";
      String subfieldCode = "";
      // Go through the field nodes which are root childs
      for (int i = 0; i < n; i++) {
         WksTreeNode node = (WksTreeNode) root.getChildAt(i);
         Properties data = (Properties) node.getUserObject();
         String nodeType = data.getProperty("nodeType");
         tag = Integer.valueOf(data.getProperty("tag"));
         fieldType = data.getProperty("fieldType");
         repeatable = (data.getProperty("repeatable").equals("true")) ? true : false;
         description = data.getProperty("description");
         displayControl = data.getProperty("displayControl");
         size = data.getProperty("size");
         defaultValue = data.getProperty("defaultValue");
         helpMsg = data.getProperty("helpMsg");
         valFormat = data.getProperty("valFormat");
         pickList = data.getProperty("pickList");
         subfieldCode = data.getProperty("subfieldCode");

         wksField = new WorksheetDef.WorksheetField(tag, repeatable, descriptors,
                 Global.fiedType(fieldType), description,
                 displayControl, size,defaultValue, helpMsg,
                 valFormat, pickList);

         // Subfiels?
         int nSubfields = node.getChildCount();
         
         for (int j = 0; j < nSubfields; j++) {
            WksTreeNode subfieldNode = (WksTreeNode) node.getChildAt(j);
            data = (Properties) subfieldNode.getUserObject();
            nodeType = data.getProperty("nodeType");
            tag = Integer.valueOf(data.getProperty("tag"));
            repeatable = (data.getProperty("repeatable").equals("true")) ? true : false;
            description = data.getProperty("description");
            fieldType = data.getProperty("fieldType");
            displayControl = data.getProperty("displayControl");
            size = data.getProperty("size");
            defaultValue = data.getProperty("defaultValue");
            helpMsg = data.getProperty("helpMsg");
            valFormat = data.getProperty("valFormat");
            pickList = data.getProperty("pickList");
            subfieldCode = data.getProperty("subfieldCode");

            wksSubfield = new WorksheetDef.WorksheetSubField(tag, subfieldCode,
                    repeatable, Global.fiedType(fieldType),
                    description, displayControl, size, defaultValue,
                    helpMsg, valFormat, pickList);
            wksField.addSubField(wksSubfield);
         }
         worksheetDef.addField(wksField);
      }
      
      return worksheetDef;
   }
    
    public OutlineModel getWKSModel() {
        return  outline.getOutlineModel();
    }
    
    public String getRecordValFormat() {
        return validationEditorPane.getText();
    }

   public void valueChanged(ListSelectionEvent e) {



      throw new UnsupportedOperationException("Not supported yet.");
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

