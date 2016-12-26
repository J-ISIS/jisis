/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryexdl;


import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;


 
public  class DataEntryExDlAction extends AbstractAction {
   public DataEntryExDlAction() {
      super(NbBundle.getMessage(DataEntryExDlAction.class, "CTL_DataEntryExDlAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(OutlineTopComponent.ICON_PATH, true)));
   }
   @Override
    public void actionPerformed(ActionEvent evt) {
        // TODO implement action body
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
       if (!Util.isAdminOrOper()) {
           return;
       }
        FieldSelectionTable fst = null;
        try {
            fst = db.getFieldSelectionTable();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (!Util.isFstCorrect(fst)) {
            String msg = NbBundle.getMessage(DataEntryExDlAction.class, "MSG_DataEntryErrorsInFst");
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        try {
            String[] worksheetNames = db.getWorksheetNames();
            if (worksheetNames == null) {
                String label = NbBundle.getMessage(DataEntryExDlAction.class, "MSG_DatabaseWithoutAnyWorksheets");
                String title = NbBundle.getMessage(DataEntryExDlAction.class, "MSG_DataEntryErrorDialogTitle");
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(label, title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            int iWKS = -1;
            for (int i = 0; i < worksheetNames.length; i++) {
                WorksheetDef wd = db.getWorksheetDef(worksheetNames[i]);
                int fieldCount = wd.getFieldsCount();
                if (fieldCount > 0) {
                    iWKS = i;
                    break;
                }
            }
            if (iWKS == -1) {
                String label = NbBundle.getMessage(DataEntryExDlAction.class,
                    "MSG_DatabaseWithEmptyWorksheets");
                String title = NbBundle.getMessage(DataEntryExDlAction.class,
                    "MSG_DataEntryErrorDialogTitle");
                NotifyDescriptor d
                    = new NotifyDescriptor.Confirmation(label, title,
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
