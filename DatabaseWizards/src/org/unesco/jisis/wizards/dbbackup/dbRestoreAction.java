package org.unesco.jisis.wizards.dbbackup;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows dbRestore component.
 */
public class dbRestoreAction extends AbstractAction {
    
    public dbRestoreAction() {
        super(NbBundle.getMessage(dbRestoreAction.class, "CTL_dbRestoreAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(dbRestoreTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = dbRestoreTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
    
}
