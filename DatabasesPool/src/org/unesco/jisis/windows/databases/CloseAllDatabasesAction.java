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

public class CloseAllDatabasesAction extends AbstractAction {

    public CloseAllDatabasesAction() {
        super(NbBundle.getMessage(CloseDatabaseAction.class, "CTL_CloseAllDatabaseAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(DbTopComponent.CLOSEDB_ICON_PATH, true)));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
       
        DbTopComponent dbTopComponent = DbTopComponent.findInstance();
        dbTopComponent.closeAllDatabases();
    }


}
