///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.unesco.jisis.windows.databases;
//
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import org.openide.util.Exceptions;
//import org.unesco.jisis.corelib.common.IDatabase;
//import org.unesco.jisis.corelib.exceptions.DbException;
//import org.unesco.jisis.corelib.propertyeditor.PropEditor;
//import org.unesco.jisis.gui.Util;
//
//
//public final class DatabaseConfigurationAction implements ActionListener {
//   String conf =
//    "#GROUP NAME = Database\n"
//    + "#PARAMETER TYPE = DIRECTORY\n"
//    + "#EDITABLE = true\n"
//    + "#DOCUMENTATION = Help for directory\n"
//    + "directory param = C:\n"
//    + "#GROUP NAME = Index\n"
//    + "#PARAMETER TYPE = FILE\n"
//    + "#EDITABLE = true\n"
//    + "#DOCUMENTATION = Help for directory\n"
//    + "Any File = C:\n"
//    + "#PARAMETER TYPE = FILE\n"
//    + "#EDITABLE = true\n"
//    + "#DOCUMENTATION = Help for directory\n"
//    + "Stopword File = C:\n";
//
//   public void actionPerformed(ActionEvent evt) {
//    
//         IDatabase db = Util.getDatabaseToUse(evt);
//         if (db == null) {
//            return;
//         }
//         try {
//         String dbConfig = db.getDbConfig();
//         PropEditor.openFileString(null, dbConfig);
//      } catch (DbException ex) {
//         Exceptions.printStackTrace(ex);
//      }
//
//        
//   }
//}
