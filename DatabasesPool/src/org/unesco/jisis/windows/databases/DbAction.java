package org.unesco.jisis.windows.databases;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Action which shows Db component.
 */
public class DbAction extends AbstractAction {
    
    public DbAction() {
        super(NbBundle.getMessage(DbAction.class, "CTL_DbAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(DbTopComponent.ICON_PATH, true)));
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent win = DbTopComponent.findInstance();
        win.open();
        win.requestActive();
    }
    
}
