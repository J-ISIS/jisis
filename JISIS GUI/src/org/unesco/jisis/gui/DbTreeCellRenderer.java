/*
 * DbTreeCellRenderer.java
 *
 * Created on June 22, 2006, 7:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.gui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;

/**
 *
 * @author rustam
 */
public class DbTreeCellRenderer extends DefaultTreeCellRenderer {
    
    static final String FOLDER_ICON_PATH = "org/unesco/jisis/gui/folder.png";
    static final String ICON_PATH        = "org/unesco/jisis/gui/data.png";
    static final ImageIcon jisisFolderIcon = new ImageIcon(ImageUtilities.loadImage(FOLDER_ICON_PATH, true));
    static final ImageIcon jisisLeafIcon = new ImageIcon(ImageUtilities.loadImage(ICON_PATH, true));
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        JLabel cell = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        
        String s = null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();
        if (userObject instanceof ConnectionInfo) {
            ConnectionInfo conInfo = (ConnectionInfo) userObject;
            s = conInfo.toString();
            cell.setIcon(jisisFolderIcon);

        } else if (userObject instanceof ClientDatabaseProxy) {
            ClientDatabaseProxy db = (ClientDatabaseProxy) userObject;
            try {
                s = db.getDbHome()+"//"+db.getDatabaseName();
            } catch (DbException ex) {
                Exceptions.printStackTrace(ex);
            }
            cell.setIcon(jisisLeafIcon);

        } else {
            //Should be of type String
            s = value.toString();
        }
        cell.setText(s);
       
        
        
        return cell;
        
    }
    
}
