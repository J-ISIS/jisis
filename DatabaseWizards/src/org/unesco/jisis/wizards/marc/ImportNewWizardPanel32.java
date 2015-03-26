/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.wizards.marc;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;

import java.io.File;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.exceptions.DbException;

/**
 *
 * @author jc_dauphin
 */
public class ImportNewWizardPanel32 implements WizardDescriptor.Panel {
// public final void addChangeListener(ChangeListener l) {
// }
//
// public final void removeChangeListener(ChangeListener l) {
// }
   private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);    // or can use ChangeSupport in NB 6.0

   /**
    * The visual component that displays this panel. If you need to access the
    * component from this class, just use getComponent().
    */
   private ImportNewVisualPanel32 component;
   private WizardDescriptor       wizardDescriptor;

   // Get the visual component for the panel. In this template, the component
   // is kept separate. This can be more efficient: if the wizard is created
   // but never displayed, or not all panels are displayed, it is better to
   // create only those which really need to be visible.
   public Component getComponent() {
      if (component == null) {
         component = new ImportNewVisualPanel32(this);
      }
      return component;
   }

   public HelpCtx getHelp() {
      // Show no Help button for this panel:
      return HelpCtx.DEFAULT_HELP;
      // If you have context help:
      // return new HelpCtx(SampleWizardPanel1.class);
   }

   public boolean isValid() {
      // clean the error messages
      wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
      return component.valid(wizardDescriptor);
      // If it is always OK to press Next or Finish, then:
      // return true;
      // If it depends on some condition (form filled out...), then:
      // return someCondition();
      // and when this condition changes (last form field filled in...) then:
      // fireChangeEvent();
      // and uncomment the complicated stuff below.
   }

   public final void addChangeListener(ChangeListener l) {
      synchronized (listeners) {
         listeners.add(l);
      }
   }

   public final void removeChangeListener(ChangeListener l) {
      synchronized (listeners) {
         listeners.remove(l);
      }
   }

   protected final void fireChangeEvent() {
      Iterator<ChangeListener> it;
      synchronized (listeners) {
         it = new HashSet<ChangeListener>(listeners).iterator();
      }
      ChangeEvent ev = new ChangeEvent(this);
      while (it.hasNext()) {
         it.next().stateChanged(ev);
      }
   }

   // You can use a settings object to keep track of state. Normally the
   // settings object will be the WizardDescriptor, so you can use
   // WizardDescriptor.getProperty & putProperty to store information entered
   // by the user.
   public void readSettings(Object settings) {
      wizardDescriptor = (WizardDescriptor) settings;
      ImportNewVisualPanel32 panel = (ImportNewVisualPanel32) getComponent();
      try {
         IConnection conn = ConnectionPool.getDefaultConnection();
         // conn.echo();
         String[] dbHomes = conn.getDbHomes();
         component.fillDbHomes(dbHomes);
      } catch (DbException ex) {
         component.fillDbHomes(new String[] { "" });
         throw new org.openide.util.NotImplementedException("Not Implemented");
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public void storeSettings(Object settings) {
      WizardDescriptor       wd     = (WizardDescriptor) settings;
      ImportNewVisualPanel32 panel  = (ImportNewVisualPanel32) getComponent();
      String                 dbHome = panel.getDbHome();
      String                 dbName = panel.getDbName();
      wd.putProperty("dbHome", dbHome);
      wd.putProperty("dbName", dbName);
      int createDbOption = panel.getCreateDbOption();
      wd.putProperty("createDbOption", createDbOption);
      if (createDbOption == ImportNewVisualPanel32.CREATE_DB_FROM_FDT) {
         File fdtFile = panel.getFDTFile();
         File fstFile = panel.getFSTFile();
         wd.putProperty("fdtFile", fdtFile);
         wd.putProperty("fstFile", fstFile);
      }
   }

}
