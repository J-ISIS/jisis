package org.unesco.jisis.wizards.dbcreate;

import java.awt.Component;
import javax.naming.NoPermissionException;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.jisiscore.common.FDTModelEx;
import org.unesco.jisis.datadefinition.fst.FSTVisualPanel;


public class FSTWizardPanel implements WizardDescriptor.Panel {
    
    
    public FSTWizardPanel() {
    }
    
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        IConnection conn;
        try {
            conn = ConnectionPool.getDefaultConnection();
            if(conn!= null && !conn.getUserInfo().getIsAdmin()){
                throw new NoPermissionException();
            } else {
                if (component == null) {
                    component = new FSTVisualPanel();
                }
            }
        } catch (NoPermissionException ex) {
            Exceptions.printStackTrace(ex);
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
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
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
     */
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        WizardDescriptor wd =(WizardDescriptor) settings;
        
        FSTVisualPanel panel4 = (FSTVisualPanel) getComponent();
        FDTModelEx fdtModel =(FDTModelEx)wd.getProperty("fdt");
        panel4.setAvailableFields(fdtModel);
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor wd = (WizardDescriptor) settings;
        FSTVisualPanel panel5 = (FSTVisualPanel) getComponent();
        TableModel fstModel = panel5.getFSTModel();
        String defFormat = panel5.getDefaultFormat();
        wd.putProperty("defformat", defFormat);
        wd.putProperty("fst", fstModel);
    }
    
}

