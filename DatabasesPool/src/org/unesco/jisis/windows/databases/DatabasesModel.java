/*
 * DatabasesModel.java
 *
 * Created on June 21, 2006, 1:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.windows.databases;

import java.util.ArrayList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;


/**
 *
 * @author rustam
 */
public class DatabasesModel implements TreeModel {
    
    ArrayList dbs = new ArrayList();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("org/unesco/jisis/windows/databases/Bundle").getString("OPEN_DATABASES"));
    
    /**
     * Creates a new instance of DatabasesModel
     */
    public DatabasesModel() {
        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
        if (connectionInfo != null) {
            dbs = connectionInfo.getDatabases();
        }
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        Object child = null;
        if (parent instanceof DefaultMutableTreeNode) {
            child = dbs.get(index);
        }
        return child;
    }

    public int getChildCount(Object parent) {
        int childCount = 0;
        if (parent instanceof DefaultMutableTreeNode) {
            childCount = dbs.size();
        }
        return childCount;
    }

    public boolean isLeaf(Object node) {
        return !(getChildCount(node) > 0);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
        int childIndex = 0;
        
        if (parent instanceof DefaultMutableTreeNode) {
            childIndex = dbs.indexOf(child);
        }
        
        return childIndex;
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }
    
}
