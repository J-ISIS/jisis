/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.fstmanager;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.Util;

public final class FstManagerAction extends AbstractAction {

    public FstManagerAction() {
        super(NbBundle.getMessage(FstManagerAction.class, "CTL_FstManagerAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(OutlineTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {

        if (!Util.isAdmin()) {
            return;
        }
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
        String osName = System.getProperty("os.name");

        FstManagerTopComponent win = new FstManagerTopComponent(db);

        win.open();
        //win.repaint();
        win.requestActive();
    }
}
