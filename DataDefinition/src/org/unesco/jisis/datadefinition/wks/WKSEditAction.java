package org.unesco.jisis.datadefinition.wks;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.Util;

/**
 * Action which shows WKSEdit component.
 */
public class WKSEditAction extends AbstractAction {

    public WKSEditAction() {
        super(NbBundle.getMessage(WKSEditAction.class, "CTL_WKSEditAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(WKSEditTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {

        if (!Util.isAdminOrOper()) {
            return;
        }
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }

        TopComponent win = new WKSEditTopComponent(db);
        win.open();
        win.requestActive();

    }
}
