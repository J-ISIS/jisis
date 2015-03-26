/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.global;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.Util;

@ActionID(
        category = "Tools",
        id = "org.unesco.jisis.global.GlobalAction"
)
@ActionRegistration(
        displayName = "#CTL_GlobalAction"
)
@ActionReference(path = "Menu/Tools", position = 712, separatorBefore = 693, separatorAfter = 731)
@Messages("CTL_GlobalAction=Global Action")
public final class GlobalAction implements ActionListener {

   @Override
   public void actionPerformed(ActionEvent e) {

      if (!Util.isAdminOrOper()) {
         return;
      }
      IDatabase db = Util.getDatabaseToUse(e);
      if (db == null) {
         return;
      }
      final String OUTPUT_ID = "output";
      TopComponent outputWindow = WindowManager.getDefault().findTopComponent(OUTPUT_ID);

      Mode sliding = WindowManager.getDefault().findMode("bottomSlidingSide");

      if (sliding != null) {
         sliding.dockInto(outputWindow);
      }
      if (outputWindow != null) {
         if (outputWindow.isOpened()) {
         } else {
            outputWindow.open();
         }
         outputWindow.requestActive();
         Action action = org.openide.awt.Actions.forID("Window", "org.netbeans.core.windows.actions.MinimizeWindowAction");
         if (action.isEnabled()) {
            action.actionPerformed(null);
         }

      }

      TopComponent win = new GlobalOperationsTopComponent(db);

      win.open();
      win.requestActive();

//        Action action=org.openide.awt.Actions.forID("Window", "org.netbeans.core.windows.actions.MaximizeWindowAction"); 
//        if (action.isEnabled()) {
//          action.actionPerformed(null);
//        }
   }
}
