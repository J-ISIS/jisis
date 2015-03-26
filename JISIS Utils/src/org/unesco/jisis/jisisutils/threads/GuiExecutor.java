/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.threads;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 *
 * @author jc_dauphin
 */
public class GuiExecutor extends AbstractExecutorService {
   private static final GuiExecutor instance_ = new GuiExecutor();

   /**
    * Executes the given command at some time in the future.
    * @param command the runnable task.
    */
   public void execute(Runnable runnable) {
      if (SwingUtilities.isEventDispatchThread()) {
         runnable.run();
      } else {
         SwingUtilities.invokeLater(runnable);
      }
   }

   public static GuiExecutor instance() {
      return instance_;
   }

   private GuiExecutor() {}

   public void shutdown() {
      throw new UnsupportedOperationException();
   }

   public boolean isShutdown() {
      return false;
   }

   public boolean isTerminated() {
      return false;
   }

   public List<Runnable> shutdownNow() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
