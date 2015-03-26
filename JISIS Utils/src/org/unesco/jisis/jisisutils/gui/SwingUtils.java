/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import java.util.EventListener;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

/**
 *
 * @author jc_dauphin
 */
public class SwingUtils {
   /**
    *                    Checks if the key listener is already registerd on the component.
    *                   
    *                    @param component the component
    *                    @param l         the listener
    *                    @return true if already registered. Otherwise false.
    */
   public static boolean isKeyListenerRegistered(Component component, KeyListener l) {
      KeyListener[] listeners = component.getKeyListeners();

      for (KeyListener listener : listeners) {
         if (listener == l) {
            return true;
         }
      }

      return false;
   }

   /**
    * Inserts the key listener at the particular index in the listeners' chain.
    *
    * @param component
    * @param l
    * @param index
    */
   public static void insertKeyListener(Component component, KeyListener l, int index) {
      KeyListener[] listeners = component.getKeyListeners();

      for (KeyListener listener : listeners) {
         component.removeKeyListener(listener);
      }

      for (int i = 0; i < listeners.length; i++) {
         KeyListener listener = listeners[i];

         if (index == i) {
            component.addKeyListener(l);
         }

         component.addKeyListener(listener);
      }

      // inex is too large, add to the end.
      if (index > listeners.length - 1) {
         component.addKeyListener(l);
      }
   }

   /**
    * Checks if the mouse listener is already registerd on the component.
    *
    * @param component the component
    * @param l         the listener
    * @return true if already registered. Otherwise false.
    */
   public static boolean isMouseListenerRegistered(Component component, MouseListener l) {
      MouseListener[] listeners = component.getMouseListeners();

      for (MouseListener listener : listeners) {
         if (listener == l) {
            return true;
         }
      }

      return false;
   }

   /**
    * Inserts the mouse listener at the particular index in the listeners' chain.
    *
    * @param component
    * @param l
    * @param index
    */
   public static void insertMouseListener(Component component, MouseListener l, int index) {
      MouseListener[] listeners = component.getMouseListeners();

      for (MouseListener listener : listeners) {
         component.removeMouseListener(listener);
      }

      for (int i = 0; i < listeners.length; i++) {
         MouseListener listener = listeners[i];

         if (index == i) {
            component.addMouseListener(l);
         }

         component.addMouseListener(listener);
      }

      // inex is too large, add to the end.
      if (index > listeners.length - 1) {
         component.addMouseListener(l);
      }
   }

   /**
    * Checks if the mouse motion listener is already registerd on the component.
    *
    * @param component the component
    * @param l         the listener
    * @return true if already registered. Otherwise false.
    */
   public static boolean isMouseMotionListenerRegistered(Component component,
           MouseMotionListener l) {
      MouseMotionListener[] listeners = component.getMouseMotionListeners();

      for (MouseMotionListener listener : listeners) {
         if (listener == l) {
            return true;
         }
      }

      return false;
   }

   /**
    * Inserts the mouse motion listener at the particular index in the listeners' chain.
    *
    * @param component
    * @param l
    * @param index
    */
   public static void insertMouseMotionListener(Component component, MouseMotionListener l,
           int index) {
      MouseMotionListener[] listeners = component.getMouseMotionListeners();

      for (MouseMotionListener listener : listeners) {
         component.removeMouseMotionListener(listener);
      }

      for (int i = 0; i < listeners.length; i++) {
         MouseMotionListener listener = listeners[i];

         if (index == i) {
            component.addMouseMotionListener(l);
         }

         component.addMouseMotionListener(listener);
      }

      // inex is too large, add to the end.
      if (index > listeners.length - 1) {
         component.addMouseMotionListener(l);
      }
   }

   /**
    * Gets the scroll pane around the component.
    *
    * @param innerComponent
    * @return the scroll pane. Null if the component is not in any JScrollPane.
    */
   public static Component getScrollPane(Component innerComponent) {
      Component component = innerComponent;

      if ((component.getParent() != null) && (component.getParent().getParent() != null)
              && (component.getParent().getParent() instanceof JScrollPane)) {
         component = (JComponent) component.getParent().getParent();

         return component;

      } else {
         return null;
      }
   }

   /**
    * Checks if the listener is always registered to the EventListenerList to avoid duplicated registration of the same listener
    *
    * @param list the EventListenerList to register the listener.
    * @param t    the type of the EventListener.
    * @param l    the listener.
    * @return true if already registered. Otherwise false.
    */
   public static boolean isListenerRegistered(EventListenerList list, Class t, EventListener l) {
      Object[] objects = list.getListenerList();

      return isListenerRegistered(objects, t, l);
   }

   /**
    * Checks if the listener is always registered to the Component to avoid duplicated registration of the same listener
    *
    * @param component the component that you want to register the listener.
    * @param t         the type of the EventListener.
    * @param l         the listener.
    * @return true if already registered. Otherwise false.
    */
   public static boolean isListenerRegistered(Component component, Class t, EventListener l) {
      Object[] objects = component.getListeners(t);

      return isListenerRegistered(objects, t, l);
   }

   private static boolean isListenerRegistered(Object[] objects, Class t, EventListener l) {
      for (int i = 0; i < objects.length; i++) {
         Object listener = objects[i];

         if (t.isAssignableFrom(listener.getClass()) && (listener == l)) {
            return true;
         }
      }

      return false;
   }

   public static void setApplicationFont(Font font) {
      //
      // sets the default font for all Swing components.

//      UIDefaults defaults = UIManager.getDefaults();
//      Enumeration keys = defaults.keys();
//      while (keys.hasMoreElements()) {
//         Object key = keys.nextElement();
//
//         if ((key instanceof String) && (((String) key).endsWith(".font"))) {
//            defaults.put(key, font);
//         }
//      }
      
//       UIManager.put("Button.font", font);
//       UIManager.put("ToggleButton.font", font);
//       UIManager.put("RadioButton.font", font);
//       UIManager.put("CheckBox.font", font);
//       UIManager.put("ColorChooser.font", font);
       UIManager.put("ComboBox.font", font);
       UIManager.put("Label.font", font);
       UIManager.put("List.font", font);
//       UIManager.put("MenuBar.font", font);
//       UIManager.put("MenuItem.font", font);
//       UIManager.put("RadioButtonMenuItem.font", font);
//       UIManager.put("CheckBoxMenuItem.font", font);
//       UIManager.put("Menu.font", font);
//       UIManager.put("PopupMenu.font", font);
//       UIManager.put("OptionPane.font", font);
//       UIManager.put("Panel.font", font);
//       UIManager.put("ProgressBar.font", font);
//       UIManager.put("ScrollPane.font", font);
//       UIManager.put("Viewport.font", font);
       UIManager.put("TabbedPane.font", font);
       UIManager.put("Table.font", font);
       UIManager.put("TableHeader.font", font);
       UIManager.put("TextField.font", font);
       UIManager.put("PasswordField.font", font);
       UIManager.put("TextArea.font", font);
       UIManager.put("TextPane.font", font);
       UIManager.put("EditorPane.font", font);
//       UIManager.put("TitledBorder.font", font);
//       UIManager.put("ToolBar.font", font);
//       UIManager.put("ToolTip.font", font);
       UIManager.put("Tree.font", font);
   }

}
