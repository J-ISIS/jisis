/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DatabaseChooserlDialog.java
 *
 * Created on Nov 3, 2008, 6:28:45 PM
 */
package org.unesco.jisis.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.util.NbBundle;

import org.unesco.jisis.corelib.client.*;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.NoDatabaseSelectedException;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;





/**
 *
 * @author jc_dauphin
 */
public class DatabaseChooserDialog extends javax.swing.JDialog {

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    ClientDatabaseProxy db_ = null;
   
    DefaultMutableTreeNode rootNode_ = new DefaultMutableTreeNode(
            NbBundle.getMessage(DatabaseChooserDialog.class, "DatabaseChooserDialog.MSG_Database_Servers"));

    /** Creates new form DatabaseChooserlDialog */
    public DatabaseChooserDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
         setTitle(NbBundle.getMessage(DatabaseChooserDialog.class, "DatabaseChooserDialog.title"));
        initComponents();
        populateTree();
        DefaultTreeModel model = new DefaultTreeModel(rootNode_);
        dbPoolTree.setModel(model);
        dbPoolTree.setCellRenderer(new DbTreeCellRenderer());
        dbPoolTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        dbPoolTree.addTreeSelectionListener(new SelectionListener());
        expandAll(dbPoolTree, new TreePath(rootNode_), true);
        /**
       * Allow selection by double click on a single element or last element
       * of MULTIPLE_INTERVAL_SELECTION
       */
       MouseListener treeListener = new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent event) {

             if (event.getClickCount() == 2) {
                JTree tree = (JTree) event.getSource();
                // Get the selected node
                DefaultMutableTreeNode selectedNode =
                        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (selectedNode == null) {
                   return;
                }
                if (!selectedNode.isLeaf()) {
                   return;
                }
                Object userObj = selectedNode.getUserObject();
                if (userObj instanceof ClientDatabaseProxy) {
                   db_ = (ClientDatabaseProxy) userObj;
                   try {
                      txtDatabase.setText(db_.getDbHome() + "//" + db_.getDbName());
                   } catch (ClassCastException cce) {
                      new NoDatabaseSelectedException().displayWarning();
                   }
                }
                doClose(RET_OK);
              
             }
          }
       };
       dbPoolTree.addMouseListener(treeListener);


    }

    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    private void populateTree() {

        ArrayList<ConnectionInfo> connections = ConnectionPool.getConnections();
        for (int i = 0; i < connections.size(); i++) {
            ConnectionInfo conInfo = connections.get(i);
            DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(conInfo);
            rootNode_.add(serverNode);
            ArrayList<IDatabase> dbs = conInfo.getDatabases();
            DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(
            NbBundle.getMessage(DatabaseChooserDialog.class, "DatabaseChooserDialog.MSG_Databases_Opened"));
            serverNode.add(dbNode);
            for (int j = 0; j < dbs.size(); j++) {
                
                    dbNode.add(new DefaultMutableTreeNode((ClientDatabaseProxy)dbs.get(j)));
 
            }

        }
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      okButton = new javax.swing.JButton();
      cancelButton = new javax.swing.JButton();
      jScrollPane1 = new javax.swing.JScrollPane();
      dbPoolTree = new javax.swing.JTree();
      txtDatabase = new javax.swing.JTextField();
      lblDatabase = new javax.swing.JLabel();
      lblComment = new javax.swing.JLabel();

      setTitle("null");
      addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
            closeDialog(evt);
         }
      });

      okButton.setText(org.openide.util.NbBundle.getMessage(DatabaseChooserDialog.class, "DatabaseChooserDialog.okButton.text")); // NOI18N
      okButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            okButtonActionPerformed(evt);
         }
      });

      cancelButton.setText(org.openide.util.NbBundle.getMessage(DatabaseChooserDialog.class, "DatabaseChooserDialog.cancelButton.text")); // NOI18N
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cancelButtonActionPerformed(evt);
         }
      });

      jScrollPane1.setViewportView(dbPoolTree);

      txtDatabase.setEditable(false);
      txtDatabase.setText(org.openide.util.NbBundle.getMessage(DatabaseChooserDialog.class, "DatabaseChooserDialog.txtDatabase.text")); // NOI18N

      lblDatabase.setText(org.openide.util.NbBundle.getMessage(DatabaseChooserDialog.class, "DatabaseChooserDialog.lblDatabase.text")); // NOI18N

      lblComment.setText(org.openide.util.NbBundle.getMessage(DatabaseChooserDialog.class, "DatabaseChooserDialog.lblComment.text")); // NOI18N

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(cancelButton)
                  .addContainerGap())
               .addGroup(layout.createSequentialGroup()
                  .addComponent(lblComment)
                  .addContainerGap(387, Short.MAX_VALUE))
               .addGroup(layout.createSequentialGroup()
                  .addComponent(lblDatabase)
                  .addGap(18, 18, 18)
                  .addComponent(txtDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                  .addGap(156, 156, 156))))
         .addGroup(layout.createSequentialGroup()
            .addGap(41, 41, 41)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(215, Short.MAX_VALUE))
      );

      layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(lblComment)
            .addGap(28, 28, 28)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(26, 26, 26)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(lblDatabase)
               .addComponent(txtDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(cancelButton)
               .addComponent(okButton))
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    public IDatabase getSelectedDatabase() {
        return (IDatabase) db_;

    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    class SelectionListener implements TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent evt) {
            // Get the source
            JTree tree = (JTree) evt.getSource();
            // Get the selected node
            DefaultMutableTreeNode selectedNode =
                    (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null) {
                return;
            }
            if (!selectedNode.isLeaf()) {
                return;
            }
            Object userObj = selectedNode.getUserObject();
            if (userObj instanceof ClientDatabaseProxy) {
                db_ = (ClientDatabaseProxy) userObj;
                try {
                    txtDatabase.setText(db_.getDbHome() + "//" + db_.getDbName());
                } catch (ClassCastException cce) {
                    new NoDatabaseSelectedException().displayWarning();
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                DatabaseChooserDialog dialog = new DatabaseChooserDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton cancelButton;
   private javax.swing.JTree dbPoolTree;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JLabel lblComment;
   private javax.swing.JLabel lblDatabase;
   private javax.swing.JButton okButton;
   private javax.swing.JTextField txtDatabase;
   // End of variables declaration//GEN-END:variables
    private int returnStatus = RET_CANCEL;
}


