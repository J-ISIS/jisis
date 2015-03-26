/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.windows.databases;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.gui.Util;


public final class CloseDatabaseAction extends AbstractAction {

    public CloseDatabaseAction() {
        super(NbBundle.getMessage(CloseDatabaseAction.class, "CTL_CloseDatabaseAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(DbTopComponent.CLOSEDB_ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
        DbTopComponent dbTopComponent = DbTopComponent.findInstance();
        dbTopComponent.closeDatabase((ClientDatabaseProxy) db);
    }

}
