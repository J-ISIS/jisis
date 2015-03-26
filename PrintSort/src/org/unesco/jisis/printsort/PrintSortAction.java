/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.printsort;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.Util;
import org.unesco.jisis.jisiscore.client.GuiGlobal;

public final class PrintSortAction extends AbstractAction  {

    public PrintSortAction() {
      super(NbBundle.getMessage(PrintSortAction.class, "CTL_PrintSortAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(OutlineTopComponent.ICON_PATH, true)));
   }

   public void actionPerformed(ActionEvent evt) {
     
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
         GuiGlobal.setEnabledHitSortFileComponent(true);

        PrintSortTopComponent win = new PrintSortTopComponent(db);

        win.open();
        //win.repaint();
        win.requestActive();
   }
}
