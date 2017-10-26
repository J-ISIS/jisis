package org.unesco.jisis.wizards.connopen;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import javax.swing.JComponent;


import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.DbInfo;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.server.DbServerService;
import org.unesco.jisis.jisisutils.proxy.ClientDatabaseProxy;
import org.unesco.jisis.jisisutils.proxy.DirectConnectOpen;
import org.unesco.jisis.jisisutils.proxy.MRUDatabasesOptions;
import org.unesco.jisis.windows.connection.ConnTopComponent;
import org.unesco.jisis.windows.databases.DbTopComponent;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.

@ActionID(id = "org.unesco.jisis.wizards.connopen.ConnectionOpenWizardAction", category = "Database")
@ActionRegistration(displayName = "#CTL_ConnectionOpenWizardAction", lazy = false)
@ActionReference(path = "Menu/Database", position = 100)
@Messages("CTL_ConnectionOpenWizardAction=Connection to the Server")
public final class ConnectionOpenWizardAction extends CallableSystemAction {

   private WizardDescriptor.Panel<org.openide.WizardDescriptor>[] panels;
   
   protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConnectionOpenWizardAction.class);

   @Override
   public void performAction() {
      WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
      // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
      wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
      wizardDescriptor.setTitle(NbBundle.getMessage(ConnectionOpenWizardAction.class, "CTL_ConnOpenWizardPanel"));
//      Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
//      dialog.setVisible(true);
      //dialog.toFront();
      displayDialog(wizardDescriptor);
      boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
      //dialog.toBack();
//      dialog.dispatchEvent(new WindowEvent(
//                    dialog, WindowEvent.WINDOW_CLOSING));
            
      
      if (!cancelled) {
         finished(wizardDescriptor);
      }
   }
   
    private void displayDialog(WizardDescriptor wizardDescriptor) {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
    }

   /**
    * Initialize panels representing individual wizard's steps and sets various
    * properties for them influencing wizard appearance.
    */
   private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
      if (panels == null) {
         panels = new WizardDescriptor.Panel[]{
            new ConnectionOpenWizardPanel1()
         };
         String[] steps = new String[panels.length];
         for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
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

    private void finished(WizardDescriptor wd) {
        final String hostname = (String) wd.getProperty("hostname");
        final String port = (String) wd.getProperty("port");
        final String username = (String) wd.getProperty("username");
        final String password = (String) wd.getProperty("password");

        DirectConnectOpen.guiConnectToServer(hostname, port, username, password);
        /**
         * Check that server connection succeeded
         */
        if (ConnectionPool.findConnection(hostname, Integer.parseInt(port)) == -1) {
            return;
        }
   
        ConnTopComponent.findInstance().refresh();

        /**
         * Do we have a database to open ?
         */
        String dbHome;
        String dbName;
        if (DbServerService.dbToOpenIsSet()) {
            dbHome = DbServerService.getJisisDbHomeToOpen();
            dbName = DbServerService.getJisisDbNameToOpen();
            ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
            IConnection connection = connectionInfo.getConnection();
            final ClientDatabaseProxy db = new ClientDatabaseProxy(connectionInfo.getConnection());
            DirectConnectOpen.openViewDatabase(db, dbHome, dbName);
            connectionInfo.addDatabase(db);

            final DbInfo dbInfo = new DbInfo(connection.getUserInfo(), connection.getServer(),
                    connection.getPort(),
                    dbHome, dbName);
            MRUDatabasesOptions opts = MRUDatabasesOptions.getInstance();
            opts.addDatabase(dbInfo);
            DbTopComponent.findInstance().refresh();

            //DbTopComponent.findInstance().requestActive();
        }

    }
 
   @Override
   public String getName() {
      return NbBundle.getMessage(ConnectionOpenWizardAction.class, "CTL_ConnOpenWizardAction");
   }

   @Override
   public String iconResource() {
      return "org/unesco/jisis/wizards/connopen/network.png";
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
