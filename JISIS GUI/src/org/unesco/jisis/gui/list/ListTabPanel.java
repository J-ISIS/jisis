/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui.list;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/**
 *
 * @author jcd
 */
/**
 * The mediator class the holds ListPanel & TabPanel and controls the
 * interaction bewteen them
 */
public class ListTabPanel extends JPanel implements PropertyChangeListener {

   private ListPanel listPane;
   private TabPanel tabPane;
   // EVENT PROPERTIES that this mediator knows of
   // Fire by the ListPanel
   public static String LIST_SELECTED = "list select";
   // Fire by the TabPanel
   public static String TAB_OBJ_UPDATE = "obj update";
   public static String TAB_OBJ_ADD = "obj add";
   public static String TAB_OBJ_DELETE = "obj delete";
   public static String TAB_SELECTED = "tab select";

   public ListTabPanel(ListPanel l, TabPanel t) {
      listPane = l;
      tabPane = t;
      init();
   }

   private void init() {
      setLayout(new BorderLayout());
      add(listPane, BorderLayout.WEST);
      add(tabPane, BorderLayout.EAST);

      tabPane.addPropertyChangeListener(this);
      listPane.addPropertyChangeListener(this);
      listPane.setSelectedIndex(0);
   }

   public void propertyChange(PropertyChangeEvent evt) {
      // Object seleted on list, set data on tab
      if (evt.getPropertyName().equals(LIST_SELECTED)) {
         tabPane.getSelected().setData(evt.getNewValue());
      }

      // Update list base on the action of the tab
      if (evt.getPropertyName().equals(TAB_OBJ_UPDATE)) {
         listPane.update(evt.getOldValue(), evt.getOldValue());
      }
      if (evt.getPropertyName().equals(TAB_OBJ_ADD)) {
         listPane.add(evt.getOldValue());
      }
      if (evt.getPropertyName().equals(TAB_OBJ_DELETE)) {
         listPane.delete(evt.getOldValue());
      }
      if (evt.getPropertyName().equals(TAB_SELECTED)) {
         tabPane.getSelected().setData(listPane.getSelected());
      }
   }
}