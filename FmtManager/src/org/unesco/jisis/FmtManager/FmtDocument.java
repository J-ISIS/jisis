/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.FmtManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.*;

/**
 *
 * @author jc_dauphin
 */
public class FmtDocument extends DefaultStyledDocument {

   private String word = "";
   private SimpleAttributeSet bold = new SimpleAttributeSet();
   private SimpleAttributeSet string = new SimpleAttributeSet();
   private SimpleAttributeSet normal = new SimpleAttributeSet();
   private SimpleAttributeSet number = new SimpleAttributeSet();
   private SimpleAttributeSet comments = new SimpleAttributeSet();
   private int currentPos = 0;
   private List<String> keywords = new ArrayList<String>();

   public static int STRING_MODE  = 10;
   public static int TEXT_MODE    = 11;
   public static int NUMBER_MODE  = 12;
   public static int COMMENT_MODE = 13;
   private int mode = TEXT_MODE;

   public FmtDocument() {
      //set the bold attribute
      StyleConstants.setBold(bold, true);
      StyleConstants.setForeground(string, Color.red);
      StyleConstants.setForeground(number, Color.magenta);
      StyleConstants.setForeground(comments, Color.blue);
      StyleConstants.setItalic(comments, true);
   }

   private void insertKeyword(String str, int pos) {
      try {
         //remove the old word and formatting
         this.remove(pos - str.length(), str.length());
         /*replace it with the same word, but new formatting
          *we MUST call the super class insertString method here, otherwise we
          *would end up in an infinite loop !!!!!*/
         super.insertString(pos - str.length(), str, bold);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private void insertTextString(String str, int pos) {
      try {
         //remove the old word and formatting
         this.remove(pos, str.length());
         super.insertString(pos, str, string);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private void insertNumberString(String str, int pos) {
      try {
         //remove the old word and formatting
         this.remove(pos, str.length());
         super.insertString(pos, str, number);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private void insertCommentString(String str, int pos) {
      try {
         //remove the old word and formatting
         this.remove(pos, str.length());
         super.insertString(pos, str, comments);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private void checkForString() {
      int offs = this.currentPos;
      Element element = this.getParagraphElement(offs);
      String elementText = "";
      try {
         //this gets our chuck of current text for the element we're on
         elementText = this.getText(element.getStartOffset(),
                 element.getEndOffset() -
                 element.getStartOffset());
      } catch (Exception ex) {
         //whoops!
         System.out.println("no text");
      }
      int strLen = elementText.length();
      if (strLen == 0) {
         return;
      }
      int i = 0;

      if (element.getStartOffset() > 0) {
         //translates backward if neccessary
         offs = offs - element.getStartOffset();
      }
      int quoteCount = 0;
      if ((offs >= 0) && (offs <= strLen - 1)) {
         i = offs;
         while (i > 0) {
            //the while loop walks back until we hit a delimiter

            char charAt = elementText.charAt(i);
            if ((charAt == '"')) {
               quoteCount++;
            }
            i--;
         }
         int rem = quoteCount % 2;
         //System.out.println(rem);
         mode = (rem == 0) ? TEXT_MODE : STRING_MODE;
      }
   }

   private void checkForKeyword() {
      if (mode != TEXT_MODE) {
         return;
      }
      int offs = this.currentPos;
      Element element = this.getParagraphElement(offs);
      String elementText = "";
      try {
         //this gets our chuck of current text for the element we're on
         elementText = this.getText(element.getStartOffset(),
                 element.getEndOffset() - element.getStartOffset());
      } catch (Exception ex) {
         //whoops!
         System.out.println("no text");
      }
      int strLen = elementText.length();
      if (strLen == 0) {
         return;
      }
      int i = 0;

      if (element.getStartOffset() > 0) {
         //translates backward if neccessary
         offs = offs - element.getStartOffset();
      }
      if ((offs >= 0) && (offs <= strLen - 1)) {
         i = offs;
         while (i > 0) {
            //the while loop walks back until we hit a delimiter
            i--;
            char charAt = elementText.charAt(i);
            if ((charAt == ' ') | (i == 0) | (charAt == '(') | (charAt == ')')
                    | (charAt == '{')
                    | (charAt == ',')
                    | (charAt == '}')) { //if i == 0 then we're at the begininng
               if (i != 0) {
                  i++;
               }
               word = elementText.substring(i, offs);//skip the period

               String s = word.trim().toLowerCase();
               //this is what actually checks for a matching keyword
               if (keywords.contains(s)) {
                  insertKeyword(word, currentPos);
               }
               break;
            }
         }
      }
   }

   private void checkForNumber() {
      int offs = this.currentPos;
      Element element = this.getParagraphElement(offs);
      String elementText = "";
      try {
         //this gets our chuck of current text for the element we're on
         elementText = this.getText(element.getStartOffset(),
                 element.getEndOffset() - element.getStartOffset());
      } catch (Exception ex) {
         //whoops!
         System.out.println("no text");
      }
      int strLen = elementText.length();
      if (strLen == 0) {
         return;
      }
      int i = 0;

      if (element.getStartOffset() > 0) {
         //translates backward if neccessary
         offs = offs - element.getStartOffset();
      }
      mode = TEXT_MODE;
      if ((offs >= 0) && (offs <= strLen - 1)) {
         i = offs;
         while (i > 0) {
            //the while loop walks back until we hit a delimiter
            char charAt = elementText.charAt(i);
            if ((charAt == ' ') | (i == 0) | (charAt == '(') | (charAt == ')') |
                    (charAt == '{') | (charAt == '}') /*|*/) { //if i == 0 then we're at the begininng
               if (i != 0) {
                  i++;
               }
               mode = NUMBER_MODE;
               break;
            } else if (!(charAt >= '0' & charAt <= '9' | charAt == '.' | charAt == '+' | charAt == '-' | charAt == '/' | charAt == '*' | charAt == '%' | charAt == '=')) {
               mode = TEXT_MODE;
               break;
            }
            i--;
         }
      }
   }

   private void checkForComment() {
      int offs = this.currentPos;
      Element element = this.getParagraphElement(offs);
      String elementText = "";
      try {
         //this gets our chuck of current text for the element we're on
         elementText = this.getText(element.getStartOffset(),
                 element.getEndOffset() - element.getStartOffset());
      } catch (Exception ex) {
         //whoops!
         System.out.println("no text");
      }
      int strLen = elementText.length();
      if (strLen == 0) {
         return;
      }
      int i = 0;

      if (element.getStartOffset() > 0) {
         //translates backward if neccessary
         offs = offs - element.getStartOffset();
      }
      if ((offs >= 1) && (offs <= strLen - 1)) {
         i = offs;
         char commentStartChar1 = elementText.charAt(i - 1);
         char commentStartChar2 = elementText.charAt(i);
         if (commentStartChar1 == '/' && commentStartChar2 == '*') {
            mode = COMMENT_MODE;
            this.insertCommentString("/*", currentPos - 1);
         } else if (commentStartChar1 == '*' && commentStartChar2 == '/') {
            mode = TEXT_MODE;
            this.insertCommentString("*/", currentPos - 1);
         }
      }
   }

   private void processChar(String str) {
      char strChar = str.charAt(0);
      if (mode != this.COMMENT_MODE) {
         mode = TEXT_MODE;
      }
      switch (strChar) {
         case ('{'):
         case ('}'):
         case (' '):
         case ('\n'):
         case ('('):
         case (')'):
         case (';'):
         case ('.'):
             {
               checkForKeyword();
               if (mode == STRING_MODE && strChar == '\n') {
                  mode = TEXT_MODE;
               }
            }
            break;
         case ('"'):
             {
               insertTextString(str, currentPos);
               this.checkForString();
            }
            break;
         case ('0'):
         case ('1'):
         case ('2'):
         case ('3'):
         case ('4'):
         case ('5'):
         case ('6'):
         case ('7'):
         case ('8'):
         case ('9'):
             {
               checkForNumber();
            }
            break;
         case ('*'):
         case ('/'):
             {
               checkForComment();
            }
            break;
      }
      if (mode == this.TEXT_MODE) {
         this.checkForString();
      }
      if (mode == this.STRING_MODE) {
         insertTextString(str, this.currentPos);
      } else if (mode == this.NUMBER_MODE) {
         insertNumberString(str, this.currentPos);
      } else if (mode == this.COMMENT_MODE) {
         insertCommentString(str, this.currentPos);
      }

   }

   private void processChar(char strChar) {
      char[] chrstr = new char[1];
      chrstr[0] = strChar;
      String str = new String(chrstr);
      processChar(str);
   }

   @Override
   public void insertString(int offs,
           String str,
           AttributeSet a) throws BadLocationException {
      super.insertString(offs, str, normal);

      int strLen = str.length();
      int endpos = offs + strLen;
      int strpos;
      for (int i = offs; i < endpos; i++) {
         currentPos = i;
         strpos = i - offs;
         processChar(str.charAt(strpos));
      }
      currentPos = offs;
   }

   public List<String> getKeywords() {
      return this.keywords;
   }

   public void setKeywords(List<String> aKeywordList) {
      if (aKeywordList != null) {
         this.keywords = aKeywordList;
      }
   }
}

