/*
 * ConnectionsModel.java
 *
 * Created on June 21, 2006, 1:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.windows.connection;

import java.util.ArrayList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.unesco.jisis.corelib.client.ConnectionPool;

/**
 *
 * @author rustam
 */
public class ConnectionsModel implements TreeModel {
    
    ArrayList conns = new ArrayList();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(
            java.util.ResourceBundle.getBundle("org/unesco/jisis/windows/connection/Bundle").getString("CONNECTION_POOL"));
    
    /** Creates a new instance of ConnectionsModel */
    public ConnectionsModel() {
        /*
        try {
            addConnection(new Connection("localhost", 1111, "rasim", "123"));
        } catch (DbException ex) {
            throw new org.openide.util.NotImplementedException(ex.getMessage());
        }
         */
        conns = ConnectionPool.getConnections();
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        Object child = null;
        if (parent instanceof DefaultMutableTreeNode) {
            child = conns.get(index);
        }
        return child;
    }

    public int getChildCount(Object parent) {
        int childCount = 0;
        if (parent instanceof DefaultMutableTreeNode) {
            childCount = conns.size();
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
            childIndex = conns.indexOf(child);
        }
        return childIndex;
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }
    
}
