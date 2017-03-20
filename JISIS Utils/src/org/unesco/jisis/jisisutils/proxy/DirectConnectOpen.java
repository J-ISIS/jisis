/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.proxy;

import org.unesco.jisis.jisisutils.proxy.MRUDatabasesOptions;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import org.netbeans.api.progress.BaseProgressUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ConnectionNIO;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.DbInfo;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.util.Notification;
import org.unesco.jisis.jisisutils.threads.IdeCursor;


/**
 *
 * @author jcdau
 */
public class DirectConnectOpen {
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DirectConnectOpen.class);
     /**
    * 
    * @param hostname
    * @param port
    * @param username
    * @param password S
    */
    public static void guiConnectToServer(final String hostname, final String port,
            final String username, final String password) {

        final AtomicBoolean cancel = new AtomicBoolean();

        final String msg = NbBundle.getMessage(DirectConnectOpen.class, "MSG_ConnectedToServer");
        
        Runnable task = () -> {

            Notification notification = connectToServer(hostname, port, username, password);
            if (notification.hasErrors() || notification.hasExceptions()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(notification.errorMessage()));

            } else {
                StatusDisplayer.getDefault().setStatusText(msg);
            }
        };

        final String msg1 = NbBundle.getMessage(DirectConnectOpen.class, "MSG_ConnectionToServerPleaseWait");
        BaseProgressUtils.runOffEventDispatchThread(task,
                msg1,
                cancel,
                false,
                0, 0);

    }

    /**
     * Connection to the server - A Notification object is used to collect 
     * information about errors during the connection and authentication.
     * 
     * The Notification object is sent back to the presentation so that it
     * can display further information about the errors.
     * @param hostname
     * @param port
     * @param username
     * @param password
     * @return 
     */
    public static Notification connectToServer(final String hostname, final String port,
            final String username, final String password) {

        final Notification notification = new Notification();

        final int i = ConnectionPool.findConnection(hostname, Integer.parseInt(port));
        if (i != -1) {
            final String errorMsg = NbBundle.getMessage(DirectConnectOpen.class, "MSG_ConnAlreadyEstablished", hostname, port);
            notification.addError(errorMsg);
        } else {
            try {
                ConnectionNIO connection = (ConnectionNIO) ConnectionNIO.connect(hostname, Integer.parseInt(port));
                /**
                 * Connection established, Try to authenticate on server side
                 */
                boolean success = connection.authenticate(username, password);
                if (success) {
                    ConnectionPool.addConnection(connection);
                } else {
                    final String errorMsg = NbBundle.getMessage(DirectConnectOpen.class, "MSG_CannotAuthenticate");
                    
                    notification.addError(errorMsg);
                    connection.close();
                }
            } catch (DbException | IOException ex) {
                
                final String errorMsg = NbBundle.getMessage(DirectConnectOpen.class, "MSG_ExceptionRaised", ex.getMessage());
                LOGGER.error(errorMsg, ex);
                notification.addError(errorMsg, ex);
            } catch (Exception ex) {
                final String errorMsg = NbBundle.getMessage(DirectConnectOpen.class, "MSG_ExceptionRaised", ex.getMessage());
                LOGGER.error(errorMsg, ex);
                notification.addError(errorMsg, ex); 
            }
        }

        return notification;

    }
    
    public static void openViewDatabase(final IDatabase db, final String dbHome, final String dbName) {
      final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();

      // Set status text
       IdeCursor.changeCursorWaitStatus(true);
       
      StatusDisplayer.getDefault().setStatusText("Reading Database Info - Please wait...");
      RepaintManager.currentManager(frame).paintDirtyRegions();

    

      Runnable openRun = new Runnable() {
         @Override
         public void run() {
             if (!EventQueue.isDispatchThread()) {
                 try {
                     //Global.output("Starting Open");
                     Date start = new Date();
                     db.getDatabase(dbHome, dbName, Global.DATABASE_DURABILITY_WRITE);
                     Date end = new Date();
                  //Global.output(Long.toString(end.getTime() - start.getTime())
                     //              + " milliseconds to open database");

                     IdeCursor.changeCursorWaitStatus(true);

                 } catch (DbException ex) {
                     Exceptions.printStackTrace(ex);
                 } finally {
                  // clear status text
                 
                   IdeCursor.changeCursorWaitStatus(false);
                   StatusDisplayer.getDefault().setStatusText("");
                   RepaintManager.currentManager(frame).paintDirtyRegions();

                  EventQueue.invokeLater(this);
               }
               // Second Invocation, we are on the event queue now
            }
         }
      };

      RequestProcessor.Task openTask = RequestProcessor.getDefault().post(openRun);
      openTask.waitFinished();
      
       
        
        IConnection connection = ConnectionPool.getDefaultConnection();
        final DbInfo dbInfo = new DbInfo(connection.getUserInfo(), connection.getServer(),
                connection.getPort(),
                dbHome, dbName);
        MRUDatabasesOptions opts = MRUDatabasesOptions.getInstance();
        opts.addDatabase(dbInfo);
      
       Action action
                = FileUtil.getConfigObject("Actions/Browse/org-unesco-jisis-database-explorer-DbViewAction.instance", Action.class);
        action.actionPerformed(null);
//      DbViewAction dbViewAction = new DbViewAction();
//      dbViewAction.actionPerformed(null);    

   }
    
}
