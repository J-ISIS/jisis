package org.unesco.jisis.wizards.dbopen;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.client.ConnectionInfo;

import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.DbInfo;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.exceptions.NoDatabaseSelectedException;
import org.unesco.jisis.database.explorer.DbViewAction;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;

import org.unesco.jisis.mrudatabases.MRUDatabasesOptions;
import org.unesco.jisis.windows.databases.*;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class DbOpenWizardAction extends CallableSystemAction {
    
    private WizardDescriptor.Panel<org.openide.WizardDescriptor>[] panels;

    private IConnection conn;
    
    
    
    public void performAction() {
       
       if (!ConnectionPool.ensureDefaultConnection()) {
          String errorMsg = NbBundle.getMessage(DbOpenWizardAction.class, "MSG_NoConnection");
          DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
          return;
       }
       
        try {
            conn = ConnectionPool.getDefaultConnection();
            if (conn == null) {
               String errorMsg = NbBundle.getMessage(DbOpenWizardAction.class, "MSG_NoConnection");
               DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
               return;
            }
            WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
            //wizardDescriptor.setClosingOptions(null);
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
            wizardDescriptor.setTitle(NbBundle.getMessage(DbOpenWizardAction.class, "MSG_WizardDialogTitle"));
            Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
            dialog.setVisible(true);
            dialog.toFront();
            boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
            if (!cancelled) {
                finished(wizardDescriptor);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void finished(WizardDescriptor wd) {
        final String dbHome = (String) wd.getProperty("dbhome");
        final String dbName = (String) wd.getProperty("dbname");

        System.out.println("dbHome: " + dbHome + "\ndbName: " + dbName);
        ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
        int i = connectionInfo.findDatabase(dbHome, dbName);
        if (i != -1) {
            String errorMsg = "DB:" + dbName + " already opened! ";
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
            return;
        }
       
        
        final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
        
        // Set status text
        StatusDisplayer.getDefault().setStatusText("Reading Database Info - Please wait...");
        RepaintManager.currentManager(frame).paintDirtyRegions();

        frame.getGlassPane().setCursor(Utilities.createProgressCursor(frame));
        frame.getGlassPane().setVisible(true);



//        Runnable openRun = new Runnable() {
//
//            public void run() {
//                if (!EventQueue.isDispatchThread()) {
                    try {
                        //Global.output("Starting Open");
                        Date start = new Date();

                        conn = ConnectionPool.getDefaultConnection();
                        ClientDatabaseProxy db = new ClientDatabaseProxy(conn);
                        db.getDatabase(dbHome, dbName, Global.DATABASE_DURABILITY_WRITE);
                        //DatabasePool.addDatabase(db);
                         connectionInfo.addDatabase(db);

                        Date end = new Date();
                    //Global.output(Long.toString(end.getTime() - start.getTime())
                    //              + " milliseconds to import ISO file");
                        try {

                            DbViewAction dbViewAction = new DbViewAction();
                            dbViewAction.actionPerformed(null);

                        } catch (ClassCastException cce) {
                            new NoDatabaseSelectedException().displayWarning();
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        // clear status text
                        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
                        // clear wait cursor
                        frame.getGlassPane().setCursor(null);
                        frame.getGlassPane().setVisible(false);

//                        EventQueue.invokeLater(this);

                    }
                // Second Invocation, we are on the event queue now
//               }
//            }
//        };
//
//        RequestProcessor.Task openTask = null;
//        openTask = RequestProcessor.getDefault().post(openRun);
//                
//        openTask.waitFinished();
        
        DbTopComponent.findInstance().refresh();
        //StatusDisplayer.getDefault().setStatusText("Default Db: " + dbHome + "//" + dbName);



        final DbInfo dbInfo = new DbInfo(conn.getUserInfo(), conn.getServer(),
                conn.getPort(),
                dbHome, dbName);
        MRUDatabasesOptions opts = MRUDatabasesOptions.getInstance();
        opts.addDatabase(dbInfo);

    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new DbOpenWizardPanel1()
                //new DbOpenWizardPanel2()
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
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
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
    
    public String getName() {
        return NbBundle.getMessage(DbOpenWizardAction.class, "CTL_DbOpenWizardAction");
    }
    
    public String iconResource() {
        return "org/unesco/jisis/wizards/dbopen/open.png";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}

