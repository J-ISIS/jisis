package org.unesco.jisis.datadefinition.fst;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.Util;

/**
 * Action which shows FstEdit component.
 */
public class FstEditAction extends AbstractAction {

    public FstEditAction() {
        super(NbBundle.getMessage(FstEditAction.class, "CTL_FstEditAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(FstEditTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
       if (!Util.isAdmin()) {
           return;
       }
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
        TopComponent win = new FstEditTopComponent(db);
        win.open();
        win.requestActive();

    }
}
