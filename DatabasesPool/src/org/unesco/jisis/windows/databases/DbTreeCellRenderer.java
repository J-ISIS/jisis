/*
 * DbTreeCellRenderer.java
 *
 * Created on June 22, 2006, 7:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.windows.databases;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openide.util.ImageUtilities;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;

/**
 *
 * @author rustam
 */
public class DbTreeCellRenderer extends DefaultTreeCellRenderer {
    
    static final String FOLDER_ICON_PATH = "org/unesco/jisis/windows/databases/folder.png";
    static final String ICON_PATH = "org/unesco/jisis/windows/databases/data.png";
    static final ImageIcon FOLDER_ICON = new ImageIcon(ImageUtilities.loadImage(FOLDER_ICON_PATH, true));
    static final ImageIcon LEAF_ICON = new ImageIcon(ImageUtilities.loadImage(ICON_PATH, true));
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        JLabel cell = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        
        ClientDatabaseProxy defaultDb;
        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
        if (connectionInfo == null) {
            defaultDb = null;
        } else {
            defaultDb = (ClientDatabaseProxy) connectionInfo.getDefaultDatabase();
        }
        boolean isDefault = (defaultDb != null && value instanceof ClientDatabaseProxy && defaultDb.equals(value));
        String s = value.toString();
        if (defaultDb != null && value instanceof ClientDatabaseProxy) {
            ClientDatabaseProxy db = (ClientDatabaseProxy) value;
            s = db.getDbHome() + "//" + db.getDbName();
        }
        cell.setText(s + (isDefault ? " (default)" : ""));
        if (value instanceof DefaultMutableTreeNode) {
            cell.setIcon(FOLDER_ICON);
        } else {
            cell.setIcon(LEAF_ICON);
        }
        return cell;
    }
    
}
