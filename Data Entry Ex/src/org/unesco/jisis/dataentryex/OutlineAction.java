/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.UserInfo;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;

/**
 * Action which shows Outline component.
 */
public class OutlineAction extends AbstractAction {

   public OutlineAction() {
      super(NbBundle.getMessage(OutlineAction.class, "CTL_OutlineAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(OutlineTopComponent.ICON_PATH, true)));
   }

   public void actionPerformed(ActionEvent evt) {


       if (!Util.isAdminOrOper()) {
           return;
       }
      IDatabase db = Util.getDatabaseToUse(evt);
      if (db == null) {
         return;
      }
      
     
       TopComponent win = new OutlineTopComponent(db);
       if (!((OutlineTopComponent) win).getWksError()) {
           win.open();
           win.requestActive();
       }
     
   }
}
