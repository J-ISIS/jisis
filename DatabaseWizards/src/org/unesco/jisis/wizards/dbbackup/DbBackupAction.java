package org.unesco.jisis.wizards.dbbackup;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.IConnection;



/**
 * Action which shows DbBackup component.
 */
public class DbBackupAction extends AbstractAction {
    IConnection conn;
    
    public DbBackupAction() {
        super(NbBundle.getMessage(DbBackupAction.class, "CTL_DbBackupAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(DbBackupTopComponent.ICON_PATH, true)));
    }
  
    public void actionPerformed(ActionEvent evt) {

        String errorMsg = NbBundle.getMessage(DbBackupAction.class, "MSG_NotImplemented");
          DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
          return;
//        if (!ConnectionPool.ensureDefaultConnection()) {
//          return;
//        }
//        if (!DatabasePool.ensureDefaultDatabase()) {
//          return;
//       }
//        NotImplemented ni;
//        conn = null;
//        try {
//            conn = ConnectionPool.getDefaultConnection();
//            if(conn!= null && !conn.getUserInfo().getIsAdmin()){
//                throw new NoPermissionException();
//            } else {
//                DbBackupTopComponent win = DbBackupTopComponent.findInstance();
//                win.open();
//                win.requestActive();
//        }
//        } catch (NoPermissionException ex) {
//            //ex.displayWarning();
//        }
    }
}


