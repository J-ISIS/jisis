/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.GroovyConsole;

import groovy.lang.Binding;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.RepaintManager;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.exceptions.DbException;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
public class GroovyConsoleTopComponent extends TopComponent  {

    private static GroovyConsoleTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    private static final String PREFERRED_ID = "GroovyConsoleTopComponent";
    private ExplorerManager manager;
    private Lookup lookup;
    private groovy.ui.Console console = null;

    public GroovyConsoleTopComponent() {
        initComponents();
        
         final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
         // Set status text
      StatusDisplayer.getDefault().setStatusText("Opening Groovy Console - Please wait...");
      RepaintManager.currentManager(frame).paintDirtyRegions();

      frame.getGlassPane().setCursor(Utilities.createProgressCursor(frame));
      frame.getGlassPane().setVisible(true);
       manager = new ExplorerManager();
       setName(NbBundle.getMessage(GroovyConsoleTopComponent.class,
               "CTL_GroovyConsoleTopComponent"));
       setToolTipText(NbBundle.getMessage(GroovyConsoleTopComponent.class,
               "HINT_GroovyConsoleTopComponent"));


      Runnable openRun = new Runnable() {
         public void run() {
            if (!EventQueue.isDispatchThread()) {
               try {
                  bindGroovyConsole();
                  frame.getGlassPane().setCursor(Utilities.createProgressCursor(frame));
                  frame.getGlassPane().setVisible(true);          
               } finally {
                  // clear status text
                  StatusDisplayer.getDefault().setStatusText(""); // NOI18N
                  // clear wait cursor
                  frame.getGlassPane().setCursor(null);
                  frame.getGlassPane().setVisible(false);

                  EventQueue.invokeLater(this);
               }
               // Second Invocation, we are on the event queue now
            }
         }
      };

      RequestProcessor.Task openTask = RequestProcessor.getDefault().post(openRun);
      openTask.waitFinished();
        
    }
    
    private void bindGroovyConsole() {
       

        Binding bind = new Binding();
        
        console = new groovy.ui.Console(this.getClass().getClassLoader(),bind);
        bind.setProperty("console",console);
//        bind.setProperty("captureStdOut",true);
       
        try {
            console.run();

            add(console.getFrame().getRootPane().getJMenuBar(),BorderLayout.NORTH);
            add(console.getFrame().getContentPane(),BorderLayout.CENTER);
            ActionMap am = this.getActionMap();
            InputMap im = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            setShortcutMaps(console.getFrame().getRootPane().getJMenuBar(),am,im);
            console.getFrame().getRootPane().setVisible(false);
            console.setMaxOutputChars(1024*1024);
//            console.setCursorPos(0);
            /**
             * console.exit() will remove the Console Window but print value
             * will not go to the console output!
             */
//            console.exit();


            lookup = ExplorerUtils.createLookup(manager, am);
            this.revalidate();           
        } catch (Throwable e) {
            e.printStackTrace();
            ErrorManager.getDefault().notify(e);
        }
    }
   

    @Override
    public void componentActivated() {
        super.componentActivated();
        this.toFront();
        JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
        frame.getRootPane().updateUI();
    }

    private  void setShortcutMaps(JMenuBar jmb, ActionMap am, InputMap im) {

        for (JMenuItem me3 : getMenuItems(jmb)) {
            Action a = ((JMenuItem)me3).getAction();
            KeyStroke k = ((JMenuItem)me3).getAccelerator();
            im.put(k,a.getValue(Action.NAME));
            am.put(a.getValue(Action.NAME),a);
        }

    }

    private List<JMenuItem> getMenuItems(MenuElement me) {
        List thisLevelMenuItems = new ArrayList();
        if (me!=null && me.getSubElements()!=null && me.getSubElements().length>0) {
            for (MenuElement me1 : me.getSubElements()) {
                if (me1 instanceof JMenuItem && !(me1 instanceof JMenu)) {
                    thisLevelMenuItems.add((JMenuItem)me1);
                } else {
                    thisLevelMenuItems.addAll(getMenuItems(me1));
                }
            }
        }
        return thisLevelMenuItems;
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    @Override
    public Lookup getLookup() {
        return lookup;
    }
// ...methods as before, but replace componentActivated and
// componentDeactivated with e.g.:
   @Override
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(manager, true);
    }
   @Override
    public void removeNotify() {
        ExplorerUtils.activateActions(manager, false);
        super.removeNotify();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        pnlButtons = new javax.swing.JPanel();
        pnlMain = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

    }// </editor-fold> 


    // Variables declaration - do not modify
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlMain;
    // End of variables declaration

    /**
     * Gets default instance. Do not use directly: reserved for .settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized GroovyConsoleTopComponent getDefault() {
        if (instance == null) {
            instance = new GroovyConsoleTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the GroovyConsoleTopComponent instance. Never call
{@link #getDefault} directly!
     */
    public static synchronized GroovyConsoleTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
"Cannot find GroovyConsole component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof GroovyConsoleTopComponent) {
            return (GroovyConsoleTopComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

   @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

   @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

   @Override
    public void componentClosed() {
        // TODO add custom code on component closing
      if (console != null) {
         console.exit();
      }
    }

    /** replaces this in object stream */
   @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

   @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return GroovyConsoleTopComponent.getDefault();
        }
    }

}
