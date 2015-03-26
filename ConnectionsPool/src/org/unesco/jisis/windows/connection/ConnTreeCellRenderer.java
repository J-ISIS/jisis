/*
 * ConnTreeCellRenderer.java
 *
 * Created on June 22, 2006, 7:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.windows.connection;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openide.util.ImageUtilities;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.IConnection;


/**
 *
 * @author rustam
 */
public class ConnTreeCellRenderer extends DefaultTreeCellRenderer {
    
    static final String FOLDER_ICON_PATH = "org/unesco/jisis/windows/connection/folder.png";
    static final String ICON_PATH = "org/unesco/jisis/windows/connection/network.png";
    static final ImageIcon folderIcon = new ImageIcon(ImageUtilities.loadImage(FOLDER_ICON_PATH, true));
    static final ImageIcon leafIcon = new ImageIcon(ImageUtilities.loadImage(ICON_PATH, true));
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel cell = null;
        IConnection defaultConn = null;

        cell = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
        if (connectionInfo != null) {
            defaultConn = connectionInfo.getConnection();
        }


        boolean isDefault = (defaultConn != null && value instanceof IConnection && defaultConn.equals(value));
        cell.setText(value.toString() + (isDefault ? 
           java.util.ResourceBundle.getBundle("org/unesco/jisis/windows/connection/Bundle").getString(" (DEFAULT)") : ""));

        if (value instanceof DefaultMutableTreeNode) {
            cell.setIcon(folderIcon);
        } else {
            cell.setIcon(leafIcon);
        }

        return cell;
    }
    
}
