/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.FmtManager;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.UndoableEdit;
import org.openide.awt.UndoRedo;

/**
 *
 * @author jcd
 */
/**
 * The trick of this UndoManager is to jump over UndoableEdits of type
 * DocumentEvent.EventType.CHANGE. These are events like coloring that we don't
 * want to see in undo/redo.
 */
public class UndoManagerEx extends UndoRedo.Manager {

   private static final long serialVersionUID = 1L;

   /**
    * Default ctor.
    */
   public UndoManagerEx() {
      super();
      // Prepare to have a lot of DocumentEvent.EventType.CHANGE
      setLimit(200000);
   }

   /**
    * The same as super.editToBeUndone() just that we treat
    * DocumentEvent.EventType.CHANGE the same way as true ==
    * edit.isSignificant().
    */
   @Override
   protected UndoableEdit editToBeUndone() {
      UndoableEdit ue = super.editToBeUndone();

      if (ue == null) {
         return null;
      }

      int i = edits.indexOf(ue);
      while (i >= 0) {
         UndoableEdit edit = edits.elementAt(i--);
         if (edit.isSignificant()) {
            if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
               if (DocumentEvent.EventType.CHANGE != ((AbstractDocument.DefaultDocumentEvent) edit).getType()) {
                  return edit;
               }
            } else {
               return edit;
            }
         }
      }
      return null;
   }

   /**
    * The same as super.editToBeUndone() just that we treat
    * DocumentEvent.EventType.CHANGE the same way as true ==
    * edit.isSignificant().
    *
    * The method of the super class already seems to be a bit buggy. The
    * DocumentEvent.EventType.CHANGE fix doesn't remove the bugs but makes it
    * behave
    */
   @Override
   protected UndoableEdit editToBeRedone() {
      int count = edits.size();
      UndoableEdit ue = super.editToBeRedone();

      if (null == ue) {
         return null;
      }

      int i = edits.indexOf(ue);

      while (i < count) {
         UndoableEdit edit = edits.elementAt(i++);
         if (edit.isSignificant()) {
            if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
               if (DocumentEvent.EventType.CHANGE != ((AbstractDocument.DefaultDocumentEvent) edit).getType()) {
                  return edit;
               }
            } else {
               return edit;
            }
         }
      }
      return null;
   }
}
