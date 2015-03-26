/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.freetextsearch;

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
    id = "org.unesco.jisis.freetextsearch.FreeTextSearchAction"
)
@ActionRegistration(
    displayName = "#CTL_FreeTextSearchAction"
)
@ActionReference(path = "Menu/Tools", position = 800, separatorBefore = 787, separatorAfter = 812)
@Messages("CTL_FreeTextSearchAction=Free Text Search")
public final class FreeTextSearchAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        IDatabase db = Util.getDatabaseToUse(e);
        if (db == null) {
            return;
        }
       
        final String OUTPUT_ID = "output";
        TopComponent outputWindow = WindowManager.getDefault().findTopComponent(OUTPUT_ID);
        
        Mode sliding  = WindowManager.getDefault().findMode("bottomSlidingSide");

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
       
       
    
        TopComponent topComponent = new FreeSearchTopComponent(db);
       
        topComponent.open();
        topComponent.requestActive();
        
        
        
//        Action action=org.openide.awt.Actions.forID("Window", "org.netbeans.core.windows.actions.MaximizeWindowAction"); 
//        if (action.isEnabled()) {
//          action.actionPerformed(null);
//        }
 
         
    }
}
