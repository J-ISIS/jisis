package org.unesco.jisis.wizards.connopen;

import java.awt.Component;
import java.awt.Dialog;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import org.netbeans.api.progress.BaseProgressUtils;


import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ConnectionNIO;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.util.Notification;
import org.unesco.jisis.windows.connection.ConnTopComponent;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
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


      guiConnectToServer(hostname, port, username, password);
      ConnTopComponent.findInstance().refresh();
      
   }
   
   /**
    * 
    * @param hostname
    * @param port
    * @param username
    * @param password 
    */
    public void guiConnectToServer(final String hostname, final String port,
            final String username, final String password) {

        final AtomicBoolean cancel = new AtomicBoolean();

        final String msg = NbBundle.getMessage(ConnectionOpenWizardAction.class, "MSG_ConnectedToServer");
        Runnable task = () -> {

            Notification notification = connectToServer(hostname, port, username, password);
            if (notification.hasErrors() || notification.hasExceptions()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(notification.errorMessage()));

            } else {
                StatusDisplayer.getDefault().setStatusText(msg);
            }
        };

        final String msg1 = NbBundle.getMessage(ConnectionOpenWizardAction.class, "MSG_ConnectionToServerPleaseWait");
        BaseProgressUtils.runOffEventDispatchThread(task,
                msg1,
                cancel,
                false,
                0, 0);

    }

    /**
     * Connection to the server - A Notification object is used to collect 
     * information about errors during the connection and authentication.
     * 
     * The Notification object is sent back to the presentation so that it
     * can display further information about the errors.
     * @param hostname
     * @param port
     * @param username
     * @param password
     * @return 
     */
    public Notification connectToServer(final String hostname, final String port,
            final String username, final String password) {

        final Notification notification = new Notification();

        final int i = ConnectionPool.findConnection(hostname, Integer.parseInt(port));
        if (i != -1) {
            final String errorMsg = NbBundle.getMessage(ConnectionOpenWizardAction.class, "MSG_ConnAlreadyEstablished", hostname, port);
            notification.addError(errorMsg);
        } else {
            try {
                ConnectionNIO connection = (ConnectionNIO) ConnectionNIO.connect(hostname, Integer.parseInt(port));
                /**
                 * Connection established, Try to authenticate on server side
                 */
                boolean success = connection.authenticate(username, password);
                if (success) {
                    ConnectionPool.addConnection(connection);
                } else {
                    final String errorMsg = NbBundle.getMessage(ConnectionOpenWizardAction.class, "MSG_CannotAuthenticate");
                    
                    notification.addError(errorMsg);
                    connection.close();
                }
            } catch (DbException | IOException ex) {
                
                final String errorMsg = NbBundle.getMessage(ConnectionOpenWizardAction.class, "MSG_ExceptionRaised", ex.getMessage());
                LOGGER.error(errorMsg, ex);
                notification.addError(errorMsg, ex);
            } catch (Exception ex) {
                final String errorMsg = NbBundle.getMessage(ConnectionOpenWizardAction.class, "MSG_ExceptionRaised", ex.getMessage());
                LOGGER.error(errorMsg, ex);
                notification.addError(errorMsg, ex); 
            }
        }

        return notification;

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
