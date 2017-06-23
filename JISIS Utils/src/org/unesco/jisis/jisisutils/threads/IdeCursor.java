/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.threads;

import java.awt.Component;
import java.awt.Cursor;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;

/**
 *
 * @author jcd
 */
public class IdeCursor {

   private void IdeCursor() {
   }

   public static void changeCursorWaitStatus(final boolean isWaiting) {
      Mutex.EVENT.writeAccess(() -> {
          try {
              JFrame mainFrame =
                      (JFrame) WindowManager.getDefault().getMainWindow();
              Component glassPane = mainFrame.getGlassPane();
              if (isWaiting) {
                  glassPane.setVisible(true);
                  glassPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
              } else {
                  glassPane.setVisible(false);
                  glassPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
              }
          } catch (Exception e) {
          }
      });
   }
 
    public static void startWaitCursor() {
         JFrame frame =
                      (JFrame) WindowManager.getDefault().getMainWindow();
        RootPaneContainer root = (RootPaneContainer) frame.getRootPane()
                .getTopLevelAncestor();
        root.getGlassPane().setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        root.getGlassPane().setVisible(true);
    }

    public static void stopWaitCursor() {
        JFrame frame =
                      (JFrame) WindowManager.getDefault().getMainWindow();
        RootPaneContainer root = (RootPaneContainer) frame.getRootPane()
                .getTopLevelAncestor();
        root.getGlassPane().setCursor(
                Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        root.getGlassPane().setVisible(false);
    }
    
    
   /**
    * Showing/hiding busy cursor, before this functionality was in Rave winsys,
    * the code is copied from that module.
    * It needs to be called from event-dispatching thread to work synch,
    * otherwise it is scheduled into that thread.
     * @param busy */
    
    public static void showBusyCursor(final boolean busy) {
      if (SwingUtilities.isEventDispatchThread()) {
         doShowBusyCursor(busy);
      } else {
         SwingUtilities.invokeLater(() -> {
             doShowBusyCursor(busy);
         });
      }
   }

   private static void doShowBusyCursor(boolean busy) {
      JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
      if (busy) {
         RepaintManager.currentManager(mainWindow).paintDirtyRegions();
         mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         mainWindow.getGlassPane().setVisible(true);
         mainWindow.repaint();
      } else {
         mainWindow.getGlassPane().setVisible(false);
         mainWindow.getGlassPane().setCursor(null);
         mainWindow.repaint();
      }
   }
   
}
