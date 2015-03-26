/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.datadefinition.wksex;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.Util;

public final class WKSEditExAction extends AbstractAction {

    public WKSEditExAction() {
        super(NbBundle.getMessage(WKSEditExAction.class, "CTL_WKSEditExAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(WKSEditExTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {

        if (!Util.isAdminOrOper()) {
            return;
        }
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }

        TopComponent win = new WKSEditExTopComponent(db);
        win.open();
        win.requestActive();

    }
}
