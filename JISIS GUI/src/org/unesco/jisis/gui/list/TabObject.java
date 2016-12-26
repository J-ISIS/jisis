/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui.list;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;

/**
 *
 * @author jcd
 */
/*
 * A abstract class developer should implement for each tab:
 */
public abstract class TabObject extends JPanel {

   protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

   @Override
   public void addPropertyChangeListener(PropertyChangeListener p) {
      pcs.addPropertyChangeListener(p);
   }

   public void notifyAdd(Object o) {
      pcs.firePropertyChange(ListTabPanel.TAB_OBJ_ADD,
              o, null);
   }

   public void notifyDelete(Object o) {
      pcs.firePropertyChange(ListTabPanel.TAB_OBJ_DELETE,
              o, null);
   }

   public void notifyUpdate(Object o) {
      pcs.firePropertyChange(ListTabPanel.TAB_OBJ_UPDATE,
              o, null);
   }

   /*
    * This method should populate the object into this tab
    */
   public abstract void setData(Object o);
}
