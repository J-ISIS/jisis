/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.mrudatabases;



import org.unesco.jisis.jisisutils.proxy.MRUDatabasesOptions;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.RepaintManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionNIO;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.DbInfo;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.common.UserInfo;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.database.explorer.DbViewAction;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;
import org.unesco.jisis.jisisutils.threads.IdeCursor;
import org.unesco.jisis.windows.connection.ConnTopComponent;
import org.unesco.jisis.windows.databases.DbTopComponent;

public final class MRUDatabasesAction extends CallableSystemAction {

  
   @Override
   public void performAction() {
      // TODO implement action body
   }

   @Override
   public String getName() {
      return NbBundle.getMessage(MRUDatabasesAction.class, "CTL_MRUDatabasesAction");
   }

   @Override
   protected void initialize() {
      super.initialize();
      // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
      putValue("noIconInMenu", Boolean.TRUE);
   }

   @Override
   public HelpCtx getHelpCtx() {
      return HelpCtx.DEFAULT_HELP;
   }

   @Override
   protected boolean asynchronous() {
      return false;
   }
   
 
    /** {@inheritDoc}
     * Override to provide SubMenu for MRUDatabases (Most Recently Used Databases)
     * @return 
     */
   @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = new MRUFilesMenu(getName());
        return menu;
    }
 
 
 
    class MRUFilesMenu extends JMenu implements DynamicMenuContent {
 
        public MRUFilesMenu(String s) {
            super(s);
 
            MRUDatabasesOptions opts = MRUDatabasesOptions.getInstance();
            opts.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (!evt.getPropertyName().equals(MRUDatabasesOptions.MRU_FILE_LIST_PROPERTY)) {
                       return;
                    }
                    updateMenu();
                }
            });
 
            updateMenu();
        }
 
        @Override
        public JComponent[] getMenuPresenters() {
            return new JComponent[] {this};
        }
 
        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }
 
        private void updateMenu() {
            removeAll();
            MRUDatabasesOptions opts = MRUDatabasesOptions.getInstance();
            List<DbInfo> list = opts.getMRUDatabaseList();
            for (int i=0; i<list.size(); i++ ) {
                DbInfo dbInfo = list.get(i);
                String name = dbInfo.getServer()+
                        "//"+dbInfo.getDbHome()+
                        ":"+dbInfo.getDbName();
                Action action = createAction(dbInfo, name);
                action.putValue(Action.NAME,name);
                JMenuItem menuItem = new JMenuItem(action);
                add(menuItem);
            }
        }
 
 
       private class MyAction extends AbstractAction {

          DbInfo dbInfo_;

          MyAction(DbInfo dbInfo) {
             super();
             this.dbInfo_ = dbInfo;
          }

          @Override
          public void actionPerformed(ActionEvent e) {
             menuItemActionPerformed(dbInfo_);
          }
       }
       
       private Action createAction(DbInfo dbInfo, String actionCommand) {
          Action action = new MyAction(dbInfo);
          action.putValue(Action.ACTION_COMMAND_KEY, actionCommand);
          return action;
       }

      private void menuItemActionPerformed(DbInfo dbInfo) {
         final String server = dbInfo.getServer();
         final int port = dbInfo.getPort();
         final UserInfo userInfo = dbInfo.getUser();
         final String userName = userInfo.getUserName();
         final String passWord = userInfo.getPassword();
         final String dbHome = dbInfo.getDbHome();
         final String dbName = dbInfo.getDbName();


         IConnection connection = null;


         int idx = ConnectionPool.findConnection(server, port);
         StatusDisplayer.getDefault().setStatusText("Please wait...");
         if (idx == -1) {
            /**
             * Not yet connected to the server, thus make the connection
             */
            try {

               connection = ConnectionNIO.connect(server, port, userName, passWord);
            } catch (DbException | IOException ex) {
               NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
               DialogDisplayer.getDefault().notify(nd);
               Exceptions.printStackTrace(ex);
            }
            ConnectionPool.addConnection(connection);

         } else {
            /**
             * Already connected to the server
             */
            connection = ConnectionPool.getConnections().get(idx).getConnection();
            ConnectionPool.setDefaultConnection(connection);
         }
         // Refresh the Connection tree
         ConnTopComponent.findInstance().refresh();
         ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
         idx = connectionInfo.findDatabase(dbHome, dbName);
         if (idx == -1) {
            final ClientDatabaseProxy db = new ClientDatabaseProxy(connection);
            openViewDatabase(db, dbHome, dbName);
            connectionInfo.addDatabase(db);
         } else {
            ClientDatabaseProxy db = (ClientDatabaseProxy) connectionInfo.getDatabase(dbHome, dbName);
            ArrayList<TopComponent> windows = db.getTopComponents();
            if (windows.size()>0) {
               windows.get(0).requestActive();
            }
         }
         DbTopComponent.findInstance().refresh();
         //DbTopComponent.findInstance().requestActive();
         MRUDatabasesOptions opts = MRUDatabasesOptions.getInstance();
         opts.addDatabase(dbInfo);

      }
   }

   public static void openViewDatabase(final ClientDatabaseProxy db, final String dbHome, final String dbName) {
//      final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
//
//      // Set status text
//       IdeCursor.changeCursorWaitStatus(true);
//       
//      StatusDisplayer.getDefault().setStatusText("Reading Database Info - Please wait...");
//      RepaintManager.currentManager(frame).paintDirtyRegions();

    

//      Runnable openRun = new Runnable() {
//         @Override
//         public void run() {
//             if (!EventQueue.isDispatchThread()) {
                 try {
//                     //Global.output("Starting Open");
                     IdeCursor.changeCursorWaitStatus(true);
                     Date start = new Date();
                     db.getDatabase(dbHome, dbName, Global.DATABASE_DURABILITY_WRITE);
                     Date end = new Date();
                  //Global.output(Long.toString(end.getTime() - start.getTime())
                     //              + " milliseconds to open database");

                    

                 } catch (DbException ex) {
                     Exceptions.printStackTrace(ex);
                 } 
//                 finally {
//                  // clear status text
//                 
                  IdeCursor.changeCursorWaitStatus(false);
//                   StatusDisplayer.getDefault().setStatusText("");
//                   RepaintManager.currentManager(frame).paintDirtyRegions();
//
//                  EventQueue.invokeLater(this);
//               }
//               // Second Invocation, we are on the event queue now
//            }
//         }
//      };
//
//      RequestProcessor.Task openTask = RequestProcessor.getDefault().post(openRun);
//      openTask.waitFinished();
      
      DbViewAction dbViewAction = new DbViewAction();
      dbViewAction.actionPerformed(null);    

   }
}
