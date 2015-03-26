/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.weboutput;



import org.unesco.jisis.corelib.util.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jcd
 */
public class TextMenu {

   private final JTextComponent component_;
   private final JPopupMenu menu_;
   private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TextMenu.class);
   /**
    * Give component a right-click menu with Cut, Copy, Paste, Delete and Select
    * All.
    */
   public TextMenu(JTextComponent component) {

      component_ = component;
      component.addMouseListener(new MyMouseListener()); // Find out when the user right-clicks component

      cutAction = new CutAction();
      copyAction = new CopyAction();
      pasteAction = new PasteAction();
      deleteAction = new DeleteAction();
      selectAction = new SelectAction();

      menu_ = new JPopupMenu();
      menu_.add(cutAction);
      menu_.add(copyAction);
      menu_.add(pasteAction);
      menu_.add(deleteAction);
      menu_.addSeparator();
      menu_.add(selectAction);
   }
 
   private class MyMouseListener extends MouseAdapter {

      public void mousePressed(MouseEvent m) {
         show(m);
      }

      public void mouseReleased(MouseEvent m) {
         show(m);
      }
   }

   private void show(MouseEvent m) {
      try {
         if (m.isPopupTrigger()) { // Only do something if this is the correct event

            // Disable everything, we'll enable those that can work next
            cutAction.setEnabled(false);
            copyAction.setEnabled(false);
            pasteAction.setEnabled(false);
            deleteAction.setEnabled(false);
            selectAction.setEnabled(false);

            // Find out if component is editable or read-only, and if it has some selected text
            boolean editable = component_.isEditable();
            boolean selection = (component_.getSelectedText() == null ? false : true);

            if (editable && selection) { // Editable with selection, enable Cut and Delete
               cutAction.setEnabled(true);
               deleteAction.setEnabled(true);
            }
            if (selection) // Selection, enable Copy
            {
               copyAction.setEnabled(true);
            }
            if (editable && Clipboard.hasText()) // Editable and clipboard has text, enable Paste
            {
               pasteAction.setEnabled(true);
            }
            if (component_.getText().length() > 0) // Has text, enable Select All
            {
               selectAction.setEnabled(true);
            }

            // Show the menu to the user
            menu_.show(m.getComponent(), m.getX(), m.getY());
         }
      } catch (Throwable t) {
         LOGGER.error("TextMenu Error", t);
      }
   }

  
   private final CutAction cutAction;

   private class CutAction extends AbstractAction {

      public CutAction() {
         super("Cut");
      }

      /** Perform a <i>cut</i> operation on the Text component. Removes the selected text
       * from the Text Component, and stores it in the system clipboard.
       */
      public void actionPerformed(ActionEvent a) {
         component_.cut();
      }
   }
   private final CopyAction copyAction;

   private class CopyAction extends AbstractAction {

      public CopyAction() {
         super("Copy");
      }

      /**
       * Perform a <i>copy</i> operation on the Text Component. Copies the
       * selected text from the Text Component to the system clipboard.
       */
      public void actionPerformed(ActionEvent a) {
         component_.copy();
      }
   }
   private final PasteAction pasteAction;

   private class PasteAction extends AbstractAction {

      public PasteAction() {
         super("Paste");
      }
      /**
       * Perform a <i>paste</i> operation on the Text Component. Inserts text from the
       * system clipboard into the Text Component.
       */
      public void actionPerformed(ActionEvent a) {
        component_.paste();
      }
   }
   private final DeleteAction deleteAction;

   private class DeleteAction extends AbstractAction {

      public DeleteAction() {
         super("Delete");
      }

      /**
       * Perform a <i>cut</i> operation on the Text Component. Removes the
       * selected text from the Text Component, and stores it in the system
       * clipboard.
       */
      public void actionPerformed(ActionEvent a) {
         component_.cut();
      }
   }
   private final SelectAction selectAction;

   private class SelectAction extends AbstractAction {

      public SelectAction() {
         super("Select All");
      }

      public void actionPerformed(ActionEvent a) {
         component_.selectAll();
      }
   }
}