/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisisutils.gui;

import java.awt.Color;
import javax.print.attribute.AttributeSet;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author jc_dauphin
 */
public class TextDataEntryDocument extends DefaultStyledDocument {
   private SimpleAttributeSet    bold       = new SimpleAttributeSet();
   private SimpleAttributeSet    normal     = new SimpleAttributeSet();
   private int                   currentPos = 0;
   private DefaultStyledDocument doc;
   private Element               rootElement;
   private MutableAttributeSet   keyword;

   public TextDataEntryDocument() {
      doc         = this;
      rootElement = doc.getDefaultRootElement();
      // set the bold attribute
      StyleConstants.setBold(bold, true);
      keyword = new SimpleAttributeSet(normal);
      StyleConstants.setForeground(keyword, Color.decode("#660099"));
      StyleConstants.setBold(keyword, true);
      keyword.addAttribute(StyleConstants.NameAttribute, "keyword");
   }

   /**
    * Override to apply syntax highlighting after the document has been updated
    * @param offset - the offset into the document to insert the content >= 0.
    * @param str - the string to insert
    * @param a - the attributes to associate with the inserted content.
    * @throws javax.swing.text.BadLocationException
    */


   @Override
   public void insertString(int offset, String str, javax.swing.text.AttributeSet a)
           throws BadLocationException {
      super.insertString(offset, str, a);
      processChangedLines(offset, str.length());
   }
   


   /**
    * Override to apply syntax highlighting after the document has been updated
    * @param offset
    * @param length
    * @throws javax.swing.text.BadLocationException
    */

   @Override
   public void remove(int offset, int length) throws BadLocationException {
      super.remove(offset, length);
      processChangedLines(offset, length);
   }

   /**
    *    Determine how many lines have been changed,
    *    then apply highlighting to each line
    *    @param offset- the offset into the document where the change occurs
    *    @param length- Length of string changed
    *    @throws javax.swing.text.BadLocationException
    */
   public void processChangedLines(int offset, int length) throws BadLocationException {
      // Get the document content
      String content = doc.getText(0, doc.getLength());
      // The lines affected by the latest document update
      // Gets the child element index closest to the given offset.
      int startLine = rootElement.getElementIndex(offset);
      int endLine   = rootElement.getElementIndex(offset + length);
      for (int i = startLine; i <= endLine; i++) {
         applyHighlighting(content, i);
      }
   }

   /**
    * Parse the line to determine the appropriate highlighting
    * @param content - The full document content
    * @param line    - The element index of the line to parse
    * @throws javax.swing.text.BadLocationException
    */
   private void applyHighlighting(String content, int line) throws BadLocationException {
      // offsets delimiting the line in "content"
      int startOffset   = rootElement.getElement(line).getStartOffset();
      int endOffset     = rootElement.getElement(line).getEndOffset() - 1;
      int lineLength    = endOffset - startOffset;
      int contentLength = content.length();
      if (endOffset >= contentLength) {
         endOffset = contentLength - 1;
      }
      //System.out.println("Tokenize content=" + content + " line=" + line + " Tokenize startOffset=" + startOffset + " endOffset=" + endOffset);
      // Tokenize and color the tokens
      // colorTokens(content, startOffset, endOffset);
      if (endOffset > startOffset) {
         int i = startOffset;
         while (i < endOffset) {
            
            
            char charAt = content.charAt(i);
            if (charAt == '^') {
               doc.setCharacterAttributes(i, 2, keyword, false);
               i += 2;
            } else {
               i++;
            }
         }
      }
   }
}
