/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dbservercontentmanager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.ParametersPanel;
import org.unesco.jisis.gui.Util;


public final class DbServerContentManagerAction extends AbstractAction  {

   public DbServerContentManagerAction() {
      super(NbBundle.getMessage(DbServerContentManagerAction.class, "CTL_DbServerContentManagerAction"));
//        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(OutlineTopComponent.ICON_PATH, true)));
   }

   public void actionPerformed(ActionEvent evt) {

      IDatabase db = Util.getDatabaseToUse(evt);
      if (db == null) {
         return;
      }
      String osName = System.getProperty("os.name");

      DbServerContentManagerTopComponent win = new DbServerContentManagerTopComponent(db);

      win.open();
      //win.repaint();
      win.requestActive();
   }
}
