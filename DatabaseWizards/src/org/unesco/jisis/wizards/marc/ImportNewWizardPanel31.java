/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.wizards.marc;

import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.awt.Cursor;
import javax.swing.JFrame;
import javax.swing.RepaintManager;
import javax.swing.event.ChangeListener;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;

/**
 *
 * @author jc_dauphin
 */
public class ImportNewWizardPanel31 implements WizardDescriptor.Panel<WizardDescriptor> {
   /**
    * The visual component_ that displays this panel. If you need to access the
 component_ from this class, just use getComponent().
    */
   private ImportNewVisualPanel31 component_;
   private String lastMfn_;
   

   // Get the visual component_ for the panel. In this template, the component_
   // is kept separate. This can be more efficient: if the wizard is created
   // but never displayed, or not all panels are displayed, it is better to
   // create only those which really need to be visible.
   public ImportNewVisualPanel31 getComponent() {
      if (component_ == null) {
         component_ = new ImportNewVisualPanel31();
      }
      return component_;
   }

   public HelpCtx getHelp() {
      // Show no Help button for this panel:
      return HelpCtx.DEFAULT_HELP;
      // If you have context help:
      // return new HelpCtx(SampleWizardPanel1.class);
   }

   public boolean isValid() {
      // If it is always OK to press Next or Finish, then:
      return true;
      // If it depends on some condition (form filled out...), then:
      // return someCondition();
      // and when this condition changes (last form field filled in...) then:
      // fireChangeEvent();
      // and uncomment the complicated stuff below.
   }

   public final void addChangeListener(ChangeListener l) {}

   public final void removeChangeListener(ChangeListener l) {}
   /*
    * private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    * public final void addChangeListener(ChangeListener l) {
    *   synchronized (listeners) {
    *       listeners.add(l);
    *   }
    * }
    * public final void removeChangeListener(ChangeListener l) {
    *   synchronized (listeners) {
    *       listeners.remove(l);
    *   }
    * }
    * protected final void fireChangeEvent() {
    *   Iterator<ChangeListener> it;
    *   synchronized (listeners) {
    *       it = new HashSet<ChangeListener>(listeners).iterator();
    *   }
    *   ChangeEvent ev = new ChangeEvent(this);
    *   while (it.hasNext()) {
    *       it.next().stateChanged(ev);
    *   }
    * }
    */

   // You can use a settings object to keep track of state. Normally the
   // settings object will be the WizardDescriptor, so you can use
   // WizardDescriptor.getProperty & putProperty to store information entered
   // by the user.
   public void readSettings(WizardDescriptor wd) {
      try {
         IConnection conn = ConnectionPool.getDefaultConnection();
         // conn.echo();
         String[] dbHomes = conn.getDbHomes();
         component_.fillDbHomes(dbHomes);
      } catch (DbException ex) {
         component_.fillDbHomes(new String[] { "" });
         throw new org.openide.util.NotImplementedException("Not Implemented");
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }
    private static void doShowBusyCursor(boolean busy) {
        JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
        if (busy) {
            RepaintManager.currentManager(mainWindow).paintDirtyRegions();
            mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            mainWindow.getGlassPane().setVisible(true);
            mainWindow.repaint();
        } else {
            mainWindow.getGlassPane().setVisible(false);
            mainWindow.getGlassPane().setCursor(null);
            mainWindow.repaint();
        }
    }

    public void storeSettings(WizardDescriptor wd) {

        ImportNewVisualPanel31 panel = (ImportNewVisualPanel31) getComponent();
        String dbHome = panel.getDbHome();
        String dbName = panel.getDbName();
        wd.putProperty("dbHome", dbHome);
        wd.putProperty("dbName", dbName);
        IConnection conn = null;
        conn = ConnectionPool.getDefaultConnection();
        // Open the Database
        Component waitingComponent = this.getComponent();
        try {
            waitingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ClientDatabaseProxy db = new ClientDatabaseProxy(conn);
            db.getDatabase(dbHome, dbName, Global.DATABASE_DURABILITY_WRITE);
            long lastMfn = db.getLastMfn();
            wd.putProperty("lastMfn", "" + lastMfn);
            lastMfn_ = Long.toString(lastMfn);
         //db.close();

        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            waitingComponent.setCursor(null);
        }

    }

   public String getLastMfn() {
      return lastMfn_;
   }
}
