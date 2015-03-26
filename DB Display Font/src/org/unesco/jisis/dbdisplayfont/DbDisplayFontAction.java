/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dbdisplayfont;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.jisisutils.gui.SwingUtils;


public final class DbDisplayFontAction implements ActionListener {

   public void actionPerformed(ActionEvent evt) {
      // TODO implement action body
//      IDatabase db = Util.getDatabaseToUse(evt);
//
//      ClientDatabaseProxy database = null;
//      if (db == null) {
//         String errorMsg = NbBundle.getMessage(DbDisplayFontAction.class, "MSG_NoDatabaseOpened");
//         DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
//         return;
//      }
//      if (db instanceof ClientDatabaseProxy) {
//         database = (ClientDatabaseProxy) db;
//
//      } else {
//         throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
//      }
      JFontChooserTest jft = new JFontChooserTest();
      jft.showDialog();
      if (jft.succeeded()) {
         Font font = jft.getSelectedFont();

//         database.setDisplayFont(font);
        
         SwingUtils.setApplicationFont(font);
         Global.prefs_.put("APPLICATION_FONT_FAMILY", font.getFamily());
         Global.prefs_.put("APPLICATION_FONT_STYLE", ""+font.getStyle());
         Global.prefs_.put("APPLICATION_FONT_SIZE", ""+font.getSize());
         
         Global.setApplicationFont(font);


      }

   }
}
