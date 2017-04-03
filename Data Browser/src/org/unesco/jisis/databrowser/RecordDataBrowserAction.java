/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.databrowser;

//~--- non-JDK imports --------------------------------------------------------

import org.openide.util.NbBundle;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;

/**
 * Action which shows RecordDataBrowser component.
 */
public class RecordDataBrowserAction extends AbstractAction {

    public RecordDataBrowserAction() {
        super(NbBundle.getMessage(RecordDataBrowserAction.class, "CTL_RecordDataBrowserAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(RecordDataBrowserTopComponent.ICON_PATH, true)));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
          try {
            if (db.getRecordsCount() <= 0) {
                String label = NbBundle.getMessage(RecordDataBrowserAction.class,
                    "MSG_DatabaseIsEmpty");
                String title = NbBundle.getMessage(RecordDataBrowserAction.class,
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
        GuiGlobal.setEnabledHitSortFileComponent(true);
        RecordDataBrowserTopComponent win = new RecordDataBrowserTopComponent(db);

        win.open();
        win.repaint();
        win.requestActive();

    }
}
