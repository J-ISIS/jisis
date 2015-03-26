/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.index;

//~--- non-JDK imports --------------------------------------------------------

import org.unesco.jisis.jisiscore.client.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.jisiscore.client.GuiGlobal;

/**
 *
 * @author jc_dauphin
 */
public class ReIndex implements Runnable {
   private ClientDatabaseProxy db_;

   public ReIndex(IDatabase db) {
      if (db instanceof ClientDatabaseProxy) {
         db_ = (ClientDatabaseProxy) db;
      } else {
         throw new RuntimeException(
             "ReIndex: Cannot cast DB to ClientDatabaseProxy");
      }
   }

   // -----------------------------------------------------------------------
   // Runnable -- this should only run on a background thread, never AWT thread.
   // -----------------------------------------------------------------------
    public void run() {

        String dbName = null;
        try {
            dbName = db_.getDatabaseName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GuiGlobal.output("DOING REINDEXING - Indexing Database:" + dbName);
         GuiGlobal.output("PLEASE WAIT");

        Date start = new Date();
        final ProgressHandle progress = ProgressHandleFactory.createHandle("Re Indexing: " + dbName + "...", new Cancellable() {

            public boolean cancel() {
                cancelled = true;
                return true;
            }
            private boolean cancelled;
        });

        progress.start();
        progress.switchToIndeterminate();

        try {
            db_.reIndex();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            progress.finish();
            Date end = new Date();
            GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to Index DB");
            GuiGlobal.output("REINDEXING IS DONE");
        }
    }
}
