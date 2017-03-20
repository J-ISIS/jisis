/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.proxy;

import java.awt.Component;
import java.util.Observer;
import org.openide.windows.IOPosition;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public class GuiGlobal {

    private static String hitSortFileName_;

    /**
     * public abstract InputOutput getIO(String name, boolean newIO) Parameters: name - A localised display
     * name for the tab newIO - if true, a new InputOutput is returned, else an existing InputOutput of the
     * same name may be returned
     */
    private static final InputOutput io = IOProvider.getDefault().getIO("Output Console", false);

    private static Component hitSortFileComp_;

    private static final ObservableEx selectHitSortFileObserver_ = new ObservableEx() {
    };
    private static final ObservableEx newHitSortFileObserver_ = new ObservableEx();

    public static void addNewHitSortFileObserver(Observer newObserver) {
        newHitSortFileObserver_.addObserver(newObserver);
    }

    public static void deleteNewHitSortFileObserver(Observer observer) {
        newHitSortFileObserver_.deleteObserver(observer);
    }

    protected static void changeNotifyNewHitSortFile(Object obj) {
        //System.out.println("NotifyObservers");
        newHitSortFileObserver_.setChanged();
        newHitSortFileObserver_.notifyObservers(obj);
    }

    /**
     * Observers to detect when a new HitSort File has bee selected
     */
    public static void addHitSortFileObserver(Observer newObserver) {
        selectHitSortFileObserver_.addObserver(newObserver);
    }

    public static void deleteHitSortFileObserver(Observer observer) {
        selectHitSortFileObserver_.deleteObserver(observer);
    }

    public static void setNewHitSortFileName(String hitSortFileName) {

        changeNotifyNewHitSortFile("HitSortFile");
    }

    /**
     * The Global class works as a mediator between the Hit Sort File Combo and other TopComponents that can
     * enable/disable the Combo
     *
     * @param hitSortFileComp - The component that contains the Combo which is in fact HitFileSortPanel where
     * the setEnabled method is overrided to enable/disable the Combo
     */
    public static void setHitSortFileComponent(Component hitSortFileComp) {
        hitSortFileComp_ = hitSortFileComp;
    }

    public static void setEnabledHitSortFileComponent(boolean flag) {
        hitSortFileComp_.setEnabled(flag);
    }

    protected static void changeNotifyHitSortFile(Object obj) {
        //System.out.println("NotifyObservers");
        selectHitSortFileObserver_.setChanged();
        selectHitSortFileObserver_.notifyObservers(obj);
    }

    public static void setHitSortFileName(String hitSortFileName) {
        hitSortFileName_ = hitSortFileName;
        changeNotifyHitSortFile("HitSortFile");
    }

    public static String getHitSortFileName() {
        return hitSortFileName_;

    }

    public static void output(String s) {

        io.select();
        io.setFocusTaken(true);
        io.getOut().println(s);
        io.getOut().flush();
        
        IOPosition.Position pos = IOPosition.currentPosition(io); 
        pos.scrollTo(); 

    }
    
     public static void outputErr(String s) {

        io.select();
        io.setFocusTaken(true);
        io.getErr().println(s);
        io.getErr().flush();
        
        IOPosition.Position pos = IOPosition.currentPosition(io); 
        pos.scrollTo(); 

    }
}
