package org.unesco.jisis.database.explorer;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;

/**
 * Action which shows DbView component.
 */
public class DbViewAction extends AbstractAction {

    public DbViewAction() {
        super(NbBundle.getMessage(DbViewAction.class, "CTL_DbViewAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(DbViewTopComponent.ICON_PATH, true)));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        String osName = System.getProperty("os.name");
       
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
        try {
            if (db.getRecordsCount() <= 0) {
                String label = NbBundle.getMessage(DbViewAction.class,
                    "MSG_DatabaseIsEmpty");
                String title = NbBundle.getMessage(DbViewAction.class,
                    "MSG_DataEntryErrorDialogTitle");
                NotifyDescriptor d
                    = new NotifyDescriptor.Confirmation(label, title,
                        NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }

        DbViewTopComponent win = new DbViewTopComponent(db);
     
        win.open();
        win.repaint();
        win.requestActive();

    }
}
