package org.unesco.jisis.wizards.dbcreate;

import java.awt.Component;
import javax.naming.NoPermissionException;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.IConnection;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.NoConnectionException;


public class DbNameWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {
    
    
    public DbNameWizardPanel() {
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
    @Override
    public Component getComponent() {
        IConnection conn = null;
        try {
            conn = ConnectionPool.getDefaultConnection();
            if(conn!= null && !conn.getUserInfo().getIsAdmin()){
                throw new NoPermissionException();
            } else {
                if (component == null) {
                    component = new DbNameVisualPanel();
                }
            }
        } catch (NoPermissionException ex) {
            ex.printStackTrace();
        } 
        
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    @Override
    public boolean isValid() {
       DbNameVisualPanel panel2 = (DbNameVisualPanel) getComponent();
       String dbName = panel2.getDbName();
//       if (dbName.equals("NewDatabase1")) {
//          String errorMsg = NbBundle.getMessage(DbNameWizardPanel.class, "ERR_DbName");
//          DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
//          return false; 
//       }
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }
    
    @Override
    public final void addChangeListener(ChangeListener l) {}
    @Override
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
    @Override
    public void readSettings(WizardDescriptor wiz) {
        
        DbNameVisualPanel panel2 = (DbNameVisualPanel) getComponent();
        
        try {
            IConnection conn = ConnectionPool.getDefaultConnection();
            conn.echo();
            String[] dbHomes = conn.getDbHomes();
            panel2.fillDbHomesList(dbHomes);
        } catch (DbException ex) {
            panel2.fillDbHomesList(new String[]{""});
            throw new org.openide.util.NotImplementedException(ex.getMessage());
        } 
        
    }
    
    @Override
    public void storeSettings(WizardDescriptor wiz) {
        
        
        DbNameVisualPanel panel2 = (DbNameVisualPanel) getComponent();
        String dbHome = panel2.getDbHome();
        String dbName = panel2.getDbName();
        wiz.putProperty("dbhome", dbHome);
        wiz.putProperty("dbname", dbName);
        
    }
    
}

