/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.wizards.marc;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.*;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;
import org.unesco.jisis.importexport.ImpExpTool;
import org.unesco.jisis.importexport.ImportException;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.mrudatabases.MRUDatabasesOptions;


//An example action demonstrating how the wizard could be called from within
//your code. You can copy-paste the code below wherever you need.
public final class ImportWizardAction extends CallableSystemAction {
   private IConnection conn_;
   private IDatabase db_;
   
   protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ImportWizardAction.class);

   public void performAction() {
      
      if (!ConnectionPool.ensureDefaultConnection()) {
         NotifyDescriptor d =
               new NotifyDescriptor.Message(NbBundle.getMessage(ImportWizardAction.class,
                  "MSG_noConnection"));
            DialogDisplayer.getDefault().notify(d);
         return;
      }
         if (!Util.isAdmin()) {
            return;
        }
     
        conn_ = ConnectionPool.getDefaultConnection();
      
         WizardDescriptor.Iterator<WizardDescriptor> iterator         = new ImportWizardIterator();
         WizardDescriptor          wizardDescriptor = new WizardDescriptor(iterator);
         // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
         // {1} will be replaced by WizardDescriptor.Iterator.name()
         wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
         wizardDescriptor.setTitle(NbBundle.getMessage(ImportWizardAction.class,
                 "MSG_ImportWizardTitle"));
         Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
         dialog.setVisible(true);
         dialog.toFront();
         boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
         if (!cancelled) {
            finished(wizardDescriptor);
         }
     
   }

   private boolean isDuplicateDatabaseName(String dbHome, String dbName) {
      List<String> dbNames = null;
      try {
       
         dbNames = conn_.getDbNames(dbHome);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      if (dbNames == null) {
          return false;
      }
      return (dbNames.indexOf(dbName) != -1);
             
   }

   private boolean promptOkToDestroyDB(String dbHome, String dbName) {
      // We want to create a new DB, check that it doesn't exist
      if (isDuplicateDatabaseName(dbHome, dbName)) {
         String s = NbBundle.getMessage(ImportWizardAction.class,
                 "MSG_DatabaseNameDuplicate");
         NotifyDescriptor d = new NotifyDescriptor.Confirmation(s, "IMPORT NEW",
                 NotifyDescriptor.YES_NO_CANCEL_OPTION,
                 NotifyDescriptor.QUESTION_MESSAGE);
         Object status = DialogDisplayer.getDefault().notify(d);
         if ((status == NotifyDescriptor.NO_OPTION) ||
                 (status == NotifyDescriptor.CANCEL_OPTION) ||
                 (status == NotifyDescriptor.CLOSED_OPTION)) {
            return false;
         }
      }
      return true;
   }

   private void closeDatabase(String dbHome, String dbName) {
        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
       
      ClientDatabaseProxy idb = (ClientDatabaseProxy) connectionInfo.getDatabase(dbHome, dbName);
      if (idb != null) {
         try {
            idb.close();
            ConnectionPool.removeDatabase(idb);
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
      }
   }

   private void finished(WizardDescriptor wd) {
      try {
         Map<String, Object> parameters = wd.getProperties();
         String dbHome = wd.getProperty("dbHome").toString();
         String dbName = wd.getProperty("dbName").toString();
         int databaseOption = (Integer) wd.getProperty("databaseOption");

         if (databaseOption == ImportNewVisualPanel2.IMPORT_CREATE_DB) {
            // We want to create a new DB, check that if the DB exists,
            // We want really to erase the previous content
            if (!promptOkToDestroyDB(dbHome, dbName)) {
               return;
            }
            // Be sure a DB with same name is not opened
            closeDatabase(dbHome, dbName);
            

            int createDbOption = (Integer) wd.getProperty("createDbOption");
            if (createDbOption == ImportNewVisualPanel32.CREATE_DB_FROM_FDT) {
               // Create an Empty DB with structure from old FDT & FST
               String fdtFile = wd.getProperty("fdtFile").toString();
               String fstFile = wd.getProperty("fstFile").toString();
               String encoding = wd.getProperty("encoding").toString();
               CreateDbParams dbp = ImpExpTool.createNewDbParm(dbHome, dbName,
                       fdtFile, fstFile, encoding);

               db_ = new ClientDatabaseProxy(conn_);

               ImpExpTool.createNewDb(db_, dbp);
            } else {
               // Create an Empty DB without structure
               db_ = new ClientDatabaseProxy(conn_);
               ImpExpTool.createEmptyDB(db_, dbHome, dbName);
            }
            
            /**
             * 
             */

            // Open the Database

            try {
               
               db_.getDatabase(dbHome, dbName, Global.DATABASE_BULK_WRITE);
               
            } catch (DbException ex) {
               LOGGER.error("dbHome="+dbHome+" dbName="+dbName,ex);
               throw new org.openide.util.NotImplementedException(ex.getMessage());
            }
           
           
         } else if (databaseOption == ImportNewVisualPanel2.IMPORT_IN_EXISTING_DB) {
            // Close database if already opened
            closeDatabase(dbHome, dbName);

            db_ = new ClientDatabaseProxy(conn_);
            try {
               db_.getDatabase(dbHome, dbName, Global.DATABASE_DURABILITY_WRITE);
            } catch (DbException ex) {
               Exceptions.printStackTrace(ex);
            }

            /*
             *  The instruction below is not correct as the DB is loaded in a
             * thread, this instruction is executed before the end of the
             * thread!
             */
            // targetDB.resetDatabaseInfo();
         } else {
            // Do nothing
         }
         ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
         connectionInfo.addDatabase(db_);

         DbInfo dbInfo = new DbInfo(conn_.getUserInfo(), conn_.getServer(),
                 conn_.getPort(),
                 dbHome, dbName);
         MRUDatabasesOptions opts = MRUDatabasesOptions.getInstance();
         opts.addDatabase(dbInfo);
         ConnectionPool.addDatabase(conn_, db_);
         // Call the appropriate method according to the format
         int format = (Integer) parameters.get("format");
         try {
            switch (format) {
               case Global.FORMAT_ISO2709:
                  //ImpExpTool.exportISO2709(db_, parameters);
                  ImpExpTool.importISO2709(parameters, db_);
                  break;
               case Global.FORMAT_MARC:
                  break;
               case Global.FORMAT_MARCXML:
                  ImpExpTool.importMarcXML(db_, parameters);
                  break;
               case Global.FORMAT_MARC21:
                  ImpExpTool.importMarc21(db_, parameters);
                  break;
               case Global.FORMAT_UNIMARC:
                  ImpExpTool.importUnimarc(db_, parameters);
                  break;
               case Global.FORMAT_MODS:
                  ImpExpTool.importMODS(db_, parameters);
                  break;
               case Global.FORMAT_DUBLIN_CORE:
                  ImpExpTool.importDublinCore(db_, parameters);
                  break;
               case Global.FORMAT_CSV:
                  break;
            }
            
         } catch (ImportException ex) {
            Exceptions.printStackTrace(ex);
         }
      } catch (ImportException ex) {
         ex.displayError();
      }

   }

   public String getName() {
      return NbBundle.getMessage(ImportWizardAction.class, "MSG_ImportWizardTitle");
   }

   @Override
   public String iconResource() {
      return null;
   }

   public HelpCtx getHelpCtx() {
      return HelpCtx.DEFAULT_HELP;
   }

   @Override
   protected boolean asynchronous() {
      return false;
   }
}
