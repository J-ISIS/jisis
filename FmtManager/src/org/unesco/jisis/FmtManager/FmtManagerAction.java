/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.FmtManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.ParametersPanel;
import org.unesco.jisis.gui.Util;

public class FmtManagerAction extends AbstractAction {

    public FmtManagerAction() {
        super(NbBundle.getMessage(FmtManagerAction.class, "CTL_FmtManagerAction"));
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

        FmtManagerTopComponent win = new FmtManagerTopComponent(db);

        win.open();
        //win.repaint();
        win.requestActive();

    }
}
