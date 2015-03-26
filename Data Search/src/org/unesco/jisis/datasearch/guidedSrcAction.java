package org.unesco.jisis.datasearch;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.ParametersPanel;
import org.unesco.jisis.gui.Util;

/**
 * Action which shows guidedSrc component.
 */
public class guidedSrcAction extends AbstractAction {

    public guidedSrcAction() {
        super(NbBundle.getMessage(guidedSrcAction.class, "CTL_guidedSrcAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(guidedSrcTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
        String osName = System.getProperty("os.name");
       
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }

        guidedSrcTopComponent win = new guidedSrcTopComponent(db);
        win.open();
        win.repaint();
        win.requestActive();

    }
}
