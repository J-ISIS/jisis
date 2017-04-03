package org.unesco.jisis.wizards.dbcreate;


import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;

import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.CreateDbParams;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.common.FDTModelEx;
import org.unesco.jisis.jisiscore.common.FSTModelEx;
import org.unesco.jisis.jisiscore.common.WKSModelEx;

//An example action demonstrating how the wizard could be called from within
public final class DbCreateAction extends CallableSystemAction {
   private WizardDescriptor.Panel<WizardDescriptor>[] panels;

   @Override
    public void performAction() {
        if (!ConnectionPool.ensureDefaultConnection()) {
            String errorMsg = NbBundle.getMessage(DbCreateAction.class, "MSG_NoConnection");
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
            return;
        }
        if (!Util.isAdmin()) {
            return;
        }

        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());

        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(NbBundle.getMessage(DbCreateAction.class,
            "MSG_WizardDialogTitle"));

        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);

        dialog.setVisible(true);
        dialog.toFront();

        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;

        if (!cancelled) {
            finished(wizardDescriptor);
        }

    }

   private void finished(WizardDescriptor wd) {
      String         dbhome    = (String) wd.getProperty("dbhome");
      String         dbname    = (String) wd.getProperty("dbname");
      FDTModelEx     fdtModel  = (FDTModelEx) wd.getProperty("fdt");
      WKSModelEx     wksModel  = (WKSModelEx) wd.getProperty("wks");
      FSTModelEx     fstModel  = (FSTModelEx) wd.getProperty("fst");
      String         defFormat = (String) wd.getProperty("defformat");
      
     
      
      CreateDbParams dbp       = new CreateDbParams(dbhome, dbname);
      // create field definition table
      FieldDefinitionTable fdt = fdtModel.getFieldDefinitionTable();

      dbp.setFieldDefinitionTable(fdt);

      // create data entry worksheet
      WorksheetDef wks = wksModel.getWorksheetDef();

      wks.setName("Default worksheet");
      dbp.setDefaultWorkSheet(wks);

      // create field selection table
      FieldSelectionTable fst = fstModel.getFieldSelectionTable();

      dbp.setFieldSelectionTable(fst);
      dbp.setDefaultPft("defDispFormat", defFormat);

       try {
           IConnection conn = ConnectionPool.getDefaultConnection();
           conn.echo();
           ClientDatabaseProxy db = new ClientDatabaseProxy(conn);
           db.createDatabase(dbp, Global.DATABASE_DURABILITY_WRITE);
           String infoMsg = NbBundle.getMessage(DbCreateAction.class, "MSG_DatabaseCreated");
           NotifyDescriptor d = new NotifyDescriptor.Message(infoMsg,
                   NotifyDescriptor.INFORMATION_MESSAGE);
           DialogDisplayer.getDefault().notify(d);
       } catch (DbException ex) {
           throw new org.openide.util.NotImplementedException(ex.getMessage());
      }
      /* Set panels reference to null so that new panels will be created */
      panels = null;
   }

   /**
    * Initialize panels representing individual wizard's steps and sets
    * various properties for them influencing wizard appearance.
    */
   private WizardDescriptor.Panel[] getPanels() {
      if (panels == null) {
         panels = new WizardDescriptor.Panel[] { new DbNameWizardPanel(), new FDTWizardPanel(),
                 new WKSWizardPanel(), new FSTWizardPanel() };

         String[] steps = new String[panels.length];

         for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();

            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();

            if (c instanceof JComponent) {    // assume Swing components
               JComponent jc = (JComponent) c;

               // Sets step number of a component
               jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
               // Sets steps names for a panel
               jc.putClientProperty("WizardPanel_contentData", steps);
               // Turn on subtitle creation on each step
               jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
               // Show steps on the left side with the image on the background
               jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
               // Turn on numbering of all steps
               jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
            }
         }
      }

      return panels;
   }

   @Override
   public String getName() {
      return NbBundle.getMessage(DbCreateAction.class, "CTL_DbCreateWizardAction");
   }

   @Override
   public String iconResource() {
      return "org/unesco/jisis/wizards/dbcreate/new.png";
   }

   @Override
   public HelpCtx getHelpCtx() {
      return HelpCtx.DEFAULT_HELP;
   }

   @Override
   protected boolean asynchronous() {
      return false;
   }
}
