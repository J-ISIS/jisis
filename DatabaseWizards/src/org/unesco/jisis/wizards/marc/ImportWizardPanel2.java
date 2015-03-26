/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.wizards.marc;

//~--- non-JDK imports --------------------------------------------------------

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Component;

import java.io.File;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ImportWizardPanel2 implements WizardDescriptor.Panel {
//  public final void addChangeListener(ChangeListener l) {
//  }
//
//  public final void removeChangeListener(ChangeListener l) {
//  }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);    // or can use ChangeSupport in NB 6.0

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ImportVisualPanel2 component;
    private WizardDescriptor   wizardDescriptor;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new ImportVisualPanel2(this);
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

        int                importType = (Integer) wizardDescriptor.getProperty("importType");
        ImportVisualPanel2 panel      = (ImportVisualPanel2) getComponent();

        panel.setIsNewDB(importType);
    }

    public void storeSettings(Object settings) {
        WizardDescriptor   wd       = (WizardDescriptor) settings;
        ImportVisualPanel2 panel    = (ImportVisualPanel2) getComponent();
        String             dbHome   = panel.getDBHome();
        String             dbName   = panel.getDBName();
        File               impFile  = panel.getImpFile();
        File               fdtFile  = panel.getFDTFile();
        File               fstFile  = panel.getFSTFile();
        String             encoding = panel.getEncoding();

        wd.putProperty("dbHome", dbHome);
        wd.putProperty("dbName", dbName);
        wd.putProperty("impFile", impFile);
        wd.putProperty("fdtFile", fdtFile);
        wd.putProperty("fstFile", fstFile);
        wd.putProperty("encoding", encoding);
        wd.putProperty("inputLineLength", panel.getInputLineLength());
        wd.putProperty("subfieldDelimiter", panel.getSubfieldDelimiter());
        wd.putProperty("reformattingFST", panel.getReformattingFST());
        wd.putProperty("renumberFromMFN", panel.getRenumberFromMFN());
        wd.putProperty("inputTagMFN", panel.getInputTagMFN());
        wd.putProperty("importOption", panel.getImportOption());
    }
}
