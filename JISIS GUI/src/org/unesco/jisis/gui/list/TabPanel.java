/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui.list;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

/**
 *
 * @author jcd
 */
/**
 * A panel where you can add TabObjects to
 */
public class TabPanel extends JPanel
        implements ChangeListener {

   private JTabbedPane tab = new JTabbedPane();
   private PropertyChangeSupport pcs =
           new PropertyChangeSupport(this);

   public TabPanel() {
      tab.setPreferredSize(new Dimension(400, 350));
      tab.addChangeListener(this);
      add(tab);
   }

   /*
    * This method is for the medicator panel to add its listener to listen to
    * change event inside the tab
    */
   @Override
   public void addPropertyChangeListener(PropertyChangeListener p) {
      // For notifying which tab is currently selected
      pcs.addPropertyChangeListener(p);

      // Go through all tabs and
      // add this as a property change listener
      for (int i = 0; i < tab.getTabCount(); i++) {
         TabObject o = (TabObject) tab.getComponentAt(i);
         o.addPropertyChangeListener(p);
      }
   }

   public void addTab(TabObject o) {
      tab.add(o);
   }

   public TabObject getSelected() {
      return (TabObject) tab.getSelectedComponent();
   }

   public void stateChanged(javax.swing.event.ChangeEvent e) {
      // Let the listener know that this tab is selected
      pcs.firePropertyChange(ListTabPanel.TAB_SELECTED,
              e.getSource(), null);

   }
}
