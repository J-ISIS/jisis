/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JPopupMenu;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.UserInfo;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.util.StringUtils;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;


/**
 *
 * @author jc_dauphin
 */
public class Util {

    public static boolean hasConnectionsEstablished() {
        /* Check that we have a connection */
        if (ConnectionPool.getConnections() == null) {
            return false;
        }

        if (ConnectionPool.getConnections().size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean hasDatabasesOpened(IConnection con) {
        if (con == null) {
            return false;
        }
        ConnectionInfo conInfo = ConnectionPool.getConnectionInfo(con);
        if (conInfo == null) {
            return false;
        }
        ArrayList<IDatabase> db = conInfo.getDatabases();
        if (db == null) {
            return false;
        }
        if (db.size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean hasSeveralDatabasesOpened(IConnection con) {
        if (Util.hasDatabasesOpened(con)) {
            ConnectionInfo conInfo = ConnectionPool.getConnectionInfo(con);
            ArrayList<IDatabase> db = conInfo.getDatabases();
            if (db.size() > 1) {
                return true;
            }
        }
        return false;
    }

    public static Frame frameForActionEvent(ActionEvent e) {
         if (e == null){
              return null;
         }
        if (e.getSource() instanceof Component) {
            Component c = (Component) e.getSource();
            while (c != null) {
                if (c instanceof Frame) {
                    return (Frame) c;
                }
                c = (c instanceof JPopupMenu) ? ((JPopupMenu) c).getInvoker() : c.getParent();
            }
        }
        return null;
    }

    /**
     * a fairly common idiom in a Swing application is to popup a dialog in response
     * to selecting a menu item
     */
    public static IDatabase getDatabaseToUse(ActionEvent evt) {
        IDatabase db;
         ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
         if (evt == null) {
            db = connectionInfo.getDefaultDatabase();
            return db;
        }

        if (!Util.hasConnectionsEstablished()) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(NbBundle.getMessage(Util.class,
                    "Util.MSG_noConnectionsEstablished"));
            DialogDisplayer.getDefault().notify(d);
            return null;
        }
        IConnection con = ConnectionPool.getDefaultConnection();
       
        if (!Util.hasDatabasesOpened(con)) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(NbBundle.getMessage(Util.class,
                    "Util.MSG_noDatabasesOpened"));
            DialogDisplayer.getDefault().notify(d);
            return null;
        }
        if (!Util.hasSeveralDatabasesOpened(con)) {
            
            return connectionInfo.getDefaultDatabase();
        }

        /* Ask to the user which db he/she wants to use */
        Frame dialogOwner = frameForActionEvent(evt);
        DatabaseChooserDialog dlg = new DatabaseChooserDialog(dialogOwner, true);
        dlg.pack();
        dlg.setLocationRelativeTo(dialogOwner);
        dlg.setVisible(true);
        if (dlg.getReturnStatus() == DatabaseChooserDialog.RET_CANCEL) {
            return null;
        }
        db = dlg.getSelectedDatabase();
        
        connectionInfo.setDefaultDatabase(db);
        return db;
    }

     public static boolean isFstCorrect(FieldSelectionTable fst) {

      int entryCount = fst.getEntriesCount();

      int errorCount = 0;
      for (int i = 0; i < entryCount; i++) {
         FieldSelectionTable.FstEntry entry  = fst.getEntryByIndex(i);
         int                          tag    = entry.getTag();
         String                       pft    = entry.getFormat();
         int                          teq    = entry.getTechnique();


         String s = String.format("| %5d | %1d | %s", tag, teq, pft);
         GuiGlobal.output(s);
          String name  = entry.getName();
         if (name == null || name.equals("")) {
             continue;
         }
         if (StringUtils.isValidFstIdentifier(name)) {
             // Do nothing
         } else {
            GuiGlobal.outputErr("Invalid FST Entry Name: "+name);
            errorCount++;
         }
         try {

            ISISFormatter pftIL = ISISFormatter.getFormatter(pft);
            if (pftIL == null) {
                 GuiGlobal.outputErr(ISISFormatter.getParsingError());
                  errorCount++;
             } else if (pftIL.hasParsingError()) {
                 GuiGlobal.outputErr(ISISFormatter.getParsingError());
                  errorCount++;
             }
         } catch (Exception e) {
            errorCount++;
            GuiGlobal.outputErr("Errors in this FST entry!");
         }
      }
      GuiGlobal.output("Number of parsing errors in the FST: "+errorCount);
      return (errorCount>0) ? false : true;

   }
     
    /**
     *
     * @return
     */
    public static boolean isAdminOrOper() {
        IConnection conn = ConnectionPool.getDefaultConnection();
        UserInfo userInfo = conn.getUserInfo();
        Map<String, String> permissions = userInfo.getPermissions();
        if (userInfo.getIsAdmin() || permissions.get("crud") != null || permissions.get("oper") != null) {
            // Do nothing, user is authorized
            return true;
        } else {
            String label = NbBundle.getMessage(Util.class,
                "MSG_NotAuthorized");
            String title = NbBundle.getMessage(Util.class,
                "MSG_NotAuthorizedDialogTitle");
            NotifyDescriptor d
                = new NotifyDescriptor.Confirmation(label, title,
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;

        }
    }

     public static boolean isAdmin() {
        IConnection conn = ConnectionPool.getDefaultConnection();
        UserInfo userInfo = conn.getUserInfo();

        if (userInfo.getIsAdmin()) {
            // Do nothing, user is authorized
            return true;
        } else {
            String label = NbBundle.getMessage(Util.class,
                "MSG_MustBeAdmin");
            String title = NbBundle.getMessage(Util.class,
                "MSG_MustBeAdminDialogTitle");
            NotifyDescriptor d
                = new NotifyDescriptor.Confirmation(label, title,
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;

        }
    }
     
      

}
