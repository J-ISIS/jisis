/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.searchhistory;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Observer;
import javax.swing.JPopupMenu;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.gui.Util;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;
import org.unesco.jisis.jisisutils.proxy.SearchResult;


public final class SearchHistoryAction implements ActionListener {

   @Override
   public void actionPerformed(ActionEvent evt) {
      // TODO implement action body
      IDatabase db = Util.getDatabaseToUse(evt);
      if (db == null) {
         return;
      }
      String osName = System.getProperty("os.name");

      ClientDatabaseProxy dbClient = null;
      if (db instanceof ClientDatabaseProxy) {
         dbClient = (ClientDatabaseProxy) db;
      } else {
         throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
      }

      List<SearchResult> searchResults = dbClient.getSearchResults();
      if (searchResults == null || searchResults.size() <= 0) {
         return;
      }

      SearchHistoryModel searchHistoryModel = new SearchHistoryModel(searchResults);
      Frame dialogOwner = frameForActionEvent(evt);
      SearchHistoryDialog dialog = new SearchHistoryDialog(new javax.swing.JFrame(), searchHistoryModel);

      dialog.pack();
      dialog.setLocationRelativeTo(dialogOwner);
      dialog.setVisible(true);
      int ret = dialog.getReturnStatus();
      if (ret == SearchHistoryDialog.RET_OK) {
         if (dialog.accept()) {
            SearchResult searchResult = searchResults.get(dialog.getSelectedIndex());
            NotifyDescriptor nd = new NotifyDescriptor.Message("You selected:\n" + searchResult.toString());
            DialogDisplayer.getDefault().notify(nd);
            DataViewerListTopComponent win = new DataViewerListTopComponent(dbClient, searchResult);
            /* Add this TopComponent as Observer to DB changes */
            dbClient.addObserver((Observer) win);
            win.open();
            win.repaint();
            win.requestActive();
         } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message("No item were selected !");
            DialogDisplayer.getDefault().notify(nd);
         }
      }

   }
   public void actionPerformed(IDatabase db, SearchResult searchResult) {

      ClientDatabaseProxy dbClient = null;
      if (db instanceof ClientDatabaseProxy) {
         dbClient = (ClientDatabaseProxy) db;
      } else {
         throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
      }

      DataViewerListTopComponent win = new DataViewerListTopComponent(dbClient, searchResult);
      /* Add this TopComponent as Observer to DB changes */
      dbClient.addObserver((Observer) win);
      win.open();
      win.repaint();
      win.requestActive();

   }
   
   

   public static Frame frameForActionEvent(ActionEvent e) {
      if (e.getSource() instanceof Component) {
         Component c = (Component) e.getSource();
         while (c != null) {
            if (c instanceof Frame) {
               return (Frame) c;
            }
            c = (c instanceof JPopupMenu) ? ((JPopupMenu) c).getInvoker() : c.getParent();
         }
      }
      return null;
   }
}
