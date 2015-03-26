/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.threads;

import java.awt.Component;
import javax.swing.JFrame;
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
      Mutex.EVENT.writeAccess(new Runnable() {
         public void run() {
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
         }
      });
   }
}
