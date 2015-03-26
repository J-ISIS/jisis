package org.unesco.jisis.datadefinition.fdt;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.Util;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;


/**
 * Action which shows FDTEdit component.
 */
public class FDTEditAction extends AbstractAction {

    public FDTEditAction() {
        super(NbBundle.getMessage(FDTEditAction.class, "CTL_FDTEditAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(FDTEditTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
         if (!Util.isAdmin()) {
           return;
       }
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }

        TopComponent win = new FDTEditTopComponent(db);
         /* Register this TopComponent as attached to this DB */

       ((ClientDatabaseProxy) db).addWindow(win);
        win.open();
        win.requestActive();
    }
}
