package org.unesco.jisis.dictionary;

//~--- non-JDK imports --------------------------------------------------------
import org.openide.util.NbBundle;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.IndexInfo;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;


/**
 * Action which shows dictionary component.
 */
public class dictionaryAction extends AbstractAction {

    public dictionaryAction() {
        super(NbBundle.getMessage(dictionaryAction.class, "CTL_dictionaryAction"));

        putValue(SMALL_ICON,
                new ImageIcon(ImageUtilities.loadImage(DictionaryTopComponent.ICON_PATH, true)));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
      try {
         IndexInfo indexInfo = db.getIndexInfo();
         if (indexInfo == null) {
            return;
         }
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }

        DictionaryTopComponent win = new DictionaryTopComponent(db);

        win.open();
        win.requestActive();

    }
}
