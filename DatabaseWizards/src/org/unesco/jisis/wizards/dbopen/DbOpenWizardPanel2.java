package org.unesco.jisis.wizards.dbopen;

import java.awt.Component;
import java.util.*;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.IConnection;


public class DbOpenWizardPanel2 implements WizardDescriptor.Panel {
    
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
        if (component == null) {
            component = new DbOpenVisualPanel2();
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
        IConnection conn = null;
        try {
            DbOpenVisualPanel2 panel2 = (DbOpenVisualPanel2) getComponent();
            WizardDescriptor wd =(WizardDescriptor) settings;
            String dbHome = (String) wd.getProperty("dbhome");
            conn = ConnectionPool.getDefaultConnection();
            Set dbNames = null;
            List<String> dbPermitted = new ArrayList<String>();
            
            List<String> allDbPermitted = new ArrayList<String>();
            try {
                if (conn.getUserInfo().getIsAdmin()) {
                    allDbPermitted = conn.getDbNames(dbHome);
                } else {
                    allDbPermitted = conn.getDbNames(dbHome);
                    Map permissions = conn.getUserInfo().getPermissions();
                    dbNames = permissions.keySet();
                    Iterator i = dbNames.iterator();
                    while (i.hasNext()){
                        String dbName = (String)i.next();
                        if (((Integer)permissions.get(dbName)).intValue()>0) {
                            dbPermitted.add(dbName);
                        }
                    }
                    allDbPermitted.retainAll(dbPermitted);
                }
            } catch (Exception ex) {
                throw new org.openide.util.NotImplementedException(ex.getMessage());
            }
            
            panel2.fillDbNames(allDbPermitted);
        } catch (Exception  ex) {
            ex.printStackTrace();
        }
    }
    
    public void storeSettings(Object settings) {
        DbOpenVisualPanel2 panel2 = (DbOpenVisualPanel2) getComponent();
        WizardDescriptor wd =(WizardDescriptor) settings;
        String dbName = panel2.getDbName();
        wd.putProperty("dbname", dbName);
    }
    
}

