package org.unesco.jisis.wizards.dbbackup;

import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;

import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.unesco.jisis.corelib.common.IConnection;



/**
 * Action which shows DbBackup component.
 */
@ActionID(id = "org.unesco.jisis.wizards.dbbackup.DbBackupAction", category = "Database")
@ActionRegistration(displayName = "#CTL_DbBackupAction", lazy = false)
@ActionReference(path = "Menu/Database", position = 600)
public class DbBackupAction extends CallableSystemAction {
    IConnection conn;
    
    public DbBackupAction() {
        //super(NbBundle.getMessage(DbBackupAction.class, "CTL_DbBackupAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(DbBackupTopComponent.ICON_PATH, true)));
    }
  
    @Override
    public void performAction() {

        String errorMsg = NbBundle.getMessage(DbBackupAction.class, "MSG_NotImplemented");
          DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
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

    @Override
    public String getName() {
        return NbBundle.getMessage(DbBackupAction.class, "CTL_DbBackupAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}

