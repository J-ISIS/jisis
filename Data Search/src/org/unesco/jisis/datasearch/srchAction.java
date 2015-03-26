package org.unesco.jisis.datasearch;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.unesco.jisis.gui.Util;



import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.IDatabase;

/*
 * Action which shows srch component.
 */
public class srchAction extends AbstractAction {

    public srchAction() {
        super(NbBundle.getMessage(srchAction.class, "CTL_srchAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(SearchTopComponent.ICON_PATH, true)));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String osName = System.getProperty("os.name");
   
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }

        SearchTopComponent win = new SearchTopComponent(db);
       
        win.open();
        win.repaint();
        win.requestActive();

    }
}
