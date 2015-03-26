package org.unesco.jisis.dataentry;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;


/**
 * Action which shows DataEntry component.
 */
public class DataEntryAction extends AbstractAction {

    public DataEntryAction() {
        super(NbBundle.getMessage(DataEntryAction.class, "CTL_DataEntryAction"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(DataEntryTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(ActionEvent evt) {
      
         IDatabase db = Util.getDatabaseToUse(evt);
         if (db == null) {
            return;
         }
         FieldSelectionTable fst = null;
         try {
            fst = db.getFieldSelectionTable();
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
         if (!Util.isFstCorrect(fst)) {
            String msg = NbBundle.getMessage(DataEntryAction.class, "MSG_DataEntryErrorsInFst");
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
         }
         try {
         String[] worksheetNames = db.getWorksheetNames();
         if (worksheetNames == null) {
            String label = NbBundle.getMessage(DataEntryAction.class, "MSG_DatabaseWithoutAnyWorksheets");
            String title = NbBundle.getMessage(DataEntryAction.class, "MSG_DataEntryErrorDialogTitle");
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(label, title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
         }
          int iWKS = -1;
         for (int i=0; i< worksheetNames.length; i++) {
            WorksheetDef wd = db.getWorksheetDef( worksheetNames[i]);
            int fieldCount = wd.getFieldsCount();
            if (fieldCount >0) {
               iWKS = i;
               break;
            }
         }
         if (iWKS == -1) {
            String label = NbBundle.getMessage(DataEntryAction.class,
                    "MSG_DatabaseWithEmptyWorksheets");
            String title = NbBundle.getMessage(DataEntryAction.class,
                    "MSG_DataEntryErrorDialogTitle");
            NotifyDescriptor d =
               new NotifyDescriptor.Confirmation(label, title,
               NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
         }
         TopComponent win = new DataEntryTopComponent(db);
         win.open();
         win.requestActive();
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }

    }
}
