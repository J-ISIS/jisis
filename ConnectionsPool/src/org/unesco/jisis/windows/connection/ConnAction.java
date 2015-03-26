package org.unesco.jisis.windows.connection;


import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.IConnection;


/**
 * Action which shows Conn component.
 */
public class ConnAction extends AbstractAction {
    
    public ConnAction() {
        super(NbBundle.getMessage(ConnAction.class, "CTL_ConnAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(ConnTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        IConnection defaultConn = null;
        try {
            defaultConn = ConnectionPool.getDefaultConnection();
            TopComponent win = ConnTopComponent.findInstance();
            win.open();
            win.requestActive();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
