/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author jcd
 *
 * Ex: field.setDocument(new FixedSizeDocument(5));
 */
public class FixedSizeDocument extends PlainDocument {

   private int max = 10;

   public FixedSizeDocument(int max) {
      this.max = max;
   }

   @Override
   public void insertString(int offs, String str, AttributeSet a)
           throws BadLocationException {
      // check string being inserted does not exceed max length

      if (getLength() + str.length() > max) {
         // If it does, then truncate it

         str = str.substring(0, max - getLength());
      }
      super.insertString(offs, str, a);
   }
}
