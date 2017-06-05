/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.FmtManager;

import java.awt.Color;
import java.awt.Font;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.pft.parser.cc.FmtParserConstants;
import org.unesco.jisis.corelib.pft.parser.cc.FmtParserTokenManager;
import org.unesco.jisis.corelib.pft.parser.cc.SimpleCharStream;
import org.unesco.jisis.corelib.pft.parser.cc.Token;
import org.unesco.jisis.corelib.pft.parser.cc.TokenMgrError;

/**
 *
 * @author jc_dauphin
 */
/**
 * Setting the tabsize to 1 is needed for synchronizing the Token column with
 * the document content. Otherwise JavaCC expands the tabs and returns the
 * column index accordingly.
 * @author jcd
 */
class NoTabExpansionCharStream extends SimpleCharStream {
   public NoTabExpansionCharStream(java.io.Reader dstreamReader) {
      super(dstreamReader);
   }
   @Override
    public void setTabSize(int i) { tabSize = i; }
    public int getTabSize(int i) { return tabSize; }
}
public class FmtDocumentEx extends DefaultStyledDocument implements FmtParserConstants {

    public static final String DEFAULT_FONT_FAMILY = "Monospaced";
    public static final int    DEFAULT_FONT_SIZE   = 12;
           
    public static final SimpleAttributeSet DEFAULT_NORMAL;
     public static final SimpleAttributeSet DEFAULT_NORMAL_BOLD;
    public static final SimpleAttributeSet DEFAULT_COMMENT;
    public static final SimpleAttributeSet DEFAULT_STRING;
    public static final SimpleAttributeSet DEFAULT_KEYWORD;
    public static final SimpleAttributeSet DEFAULT_COND_LITER;
    public static final SimpleAttributeSet DEFAULT_UNCOND_LITER;
    public static final SimpleAttributeSet DEFAULT_REP_LITER;
    public static final SimpleAttributeSet DEFAULT_FIELD;
    public static final SimpleAttributeSet DEFAULT_EXECUTION;
    
    
   static {
     
      DEFAULT_NORMAL = new SimpleAttributeSet();
      StyleConstants.setForeground(DEFAULT_NORMAL, Color.black);
      StyleConstants.setFontFamily(DEFAULT_NORMAL, DEFAULT_FONT_FAMILY);
      StyleConstants.setFontSize(DEFAULT_NORMAL, DEFAULT_FONT_SIZE);
      DEFAULT_NORMAL.addAttribute(StyleConstants.NameAttribute, "normal");

      DEFAULT_NORMAL_BOLD = new SimpleAttributeSet(DEFAULT_NORMAL);
      StyleConstants.setBold(DEFAULT_NORMAL_BOLD, true);
      DEFAULT_NORMAL_BOLD.addAttribute(StyleConstants.NameAttribute, "normal_bold");
      
      //default style for new keyword types
      DEFAULT_KEYWORD = new SimpleAttributeSet(DEFAULT_NORMAL);
      StyleConstants.setForeground(DEFAULT_KEYWORD, Color.decode("#660099"));
      StyleConstants.setBold(DEFAULT_KEYWORD, true);
      DEFAULT_KEYWORD.addAttribute(StyleConstants.NameAttribute, "keyword");
         
      DEFAULT_COMMENT = new SimpleAttributeSet(DEFAULT_NORMAL);
      StyleConstants.setForeground(DEFAULT_COMMENT, Color.decode("#990000"));
      StyleConstants.setBold(DEFAULT_COMMENT, true);
      StyleConstants.setItalic(DEFAULT_COMMENT, true);
      DEFAULT_COMMENT.addAttribute(StyleConstants.NameAttribute, "comment");

      DEFAULT_COND_LITER = new SimpleAttributeSet(DEFAULT_NORMAL);
      StyleConstants.setForeground(DEFAULT_COND_LITER, Color.decode("#48D1CC"));
      DEFAULT_COND_LITER.addAttribute(StyleConstants.NameAttribute, "doubleQuote");

      DEFAULT_REP_LITER = new SimpleAttributeSet(DEFAULT_NORMAL);
      StyleConstants.setForeground(DEFAULT_REP_LITER, Color.decode("#FF009B"));
      DEFAULT_REP_LITER.addAttribute(StyleConstants.NameAttribute, "repeatLiteral");

      DEFAULT_UNCOND_LITER = new SimpleAttributeSet(DEFAULT_NORMAL);
      StyleConstants.setForeground(DEFAULT_UNCOND_LITER, Color.decode("#99006b"));
      DEFAULT_UNCOND_LITER.addAttribute(StyleConstants.NameAttribute, "singleQuote");
      
      DEFAULT_FIELD = new SimpleAttributeSet(DEFAULT_NORMAL);
      StyleConstants.setForeground(DEFAULT_FIELD, Color.decode("#000099"));
      StyleConstants.setBold(DEFAULT_FIELD, false);
      DEFAULT_FIELD.addAttribute(StyleConstants.NameAttribute, "fields");
     
      DEFAULT_EXECUTION = new SimpleAttributeSet(DEFAULT_NORMAL);
      StyleConstants.setForeground(DEFAULT_EXECUTION, Color.red);
      DEFAULT_EXECUTION.addAttribute(StyleConstants.NameAttribute, "execution");
      
      DEFAULT_STRING = new SimpleAttributeSet();
      StyleConstants.setForeground(DEFAULT_STRING, new java.awt.Color(153, 0, 107)); //dark pink
      StyleConstants.setFontFamily(DEFAULT_STRING, DEFAULT_FONT_FAMILY);
      StyleConstants.setFontSize(DEFAULT_STRING, DEFAULT_FONT_SIZE);
   }
    
   private final DefaultStyledDocument doc;
   
   private Element rootElement;
   private boolean multiLineComment;
   private final MutableAttributeSet normal = new SimpleAttributeSet(DEFAULT_NORMAL);;
   private final MutableAttributeSet keyword = new SimpleAttributeSet(DEFAULT_KEYWORD);
   private final MutableAttributeSet comment = new SimpleAttributeSet(DEFAULT_COMMENT);
   private final MutableAttributeSet doubleQuote = new SimpleAttributeSet(DEFAULT_COND_LITER);
   private final MutableAttributeSet singleQuote = new SimpleAttributeSet(DEFAULT_UNCOND_LITER);
   private final MutableAttributeSet repeatLiteral = new SimpleAttributeSet(DEFAULT_REP_LITER);
   private final MutableAttributeSet fields = new SimpleAttributeSet(DEFAULT_FIELD);
   private final MutableAttributeSet execution = new SimpleAttributeSet(DEFAULT_EXECUTION);
   private final MutableAttributeSet select = new SimpleAttributeSet(DEFAULT_EXECUTION);
   private final MutableAttributeSet normalBold = new SimpleAttributeSet(DEFAULT_NORMAL_BOLD);
   
   private int fontSize_;
  
    public FmtDocumentEx() {
        doc = this;

        fontSize_ = DEFAULT_FONT_SIZE ;
        rootElement = doc.getDefaultRootElement();
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        if (Global.getApplicationFont() != null) {
            Font font = Global.getApplicationFont();

            // Set the font family, size, and style, based on properties of
            // the Font object. Note that JTextPane supports a number of
            // character attributes beyond those supported by the Font class.
            // For example, underline, strike-through, super- and sub-script.
            StyleConstants.setFontFamily(normal, font.getFamily());
            StyleConstants.setFontSize(normal, font.getSize());
            StyleConstants.setItalic(normal, (font.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(normal, (font.getStyle() & Font.BOLD) != 0);

            StyleConstants.setFontFamily(normalBold, font.getFamily());
            StyleConstants.setFontSize(normalBold, font.getSize());
            StyleConstants.setItalic(normalBold, (font.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(normalBold, (font.getStyle() & Font.BOLD) != 0);

            StyleConstants.setFontFamily(singleQuote, font.getFamily());
            StyleConstants.setFontSize(singleQuote, font.getSize());
            StyleConstants.setItalic(singleQuote, (font.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(singleQuote, (font.getStyle() & Font.BOLD) != 0);

            StyleConstants.setFontFamily(doubleQuote, font.getFamily());
            StyleConstants.setFontSize(doubleQuote, font.getSize());
            StyleConstants.setItalic(doubleQuote, (font.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(doubleQuote, (font.getStyle() & Font.BOLD) != 0);

            StyleConstants.setFontFamily(repeatLiteral, font.getFamily());
            StyleConstants.setFontSize(repeatLiteral, font.getSize());
            StyleConstants.setItalic(repeatLiteral, (font.getStyle() & Font.ITALIC) != 0);
            StyleConstants.setBold(repeatLiteral, (font.getStyle() & Font.BOLD) != 0);

        }
      
      
   }

 

   /**
    * Override to apply syntax highlighting after the document has been updated
    * @param offset - the offset into the document to insert the content >= 0.
    * @param str - the string to insert
    * @param a - the attributes to associate with the inserted content.
    * @throws javax.swing.text.BadLocationException
    */
   @Override
   public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
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
    * Determine how many lines have been changed,
    * then apply highlighting to each line
    * @param offset- the offset into the document where the change occurs
    * @param length- Length of string changed
    * @throws javax.swing.text.BadLocationException
    */
   public void processChangedLines(int offset, int length)
           throws BadLocationException {
      // Get the document content
       String content = doc.getText(0, doc.getLength());

      //  The lines affected by the latest document update
      // Gets the child element index closest to the given offset.
      int startLine = rootElement.getElementIndex(offset);
      int endLine   = rootElement.getElementIndex(offset + length);

      //  Make sure all comment lines prior to the start line are commented
      //  and determine if the start line is still in a multi line comment

      setMultiLineComment( commentLinesBefore( content, startLine ) );

      //  Do the actual highlighting
      for (int i = startLine; i <= endLine; i++) {
         applyHighlighting(content, i);
      }
      //  Resolve highlighting to the next end multi line delimiter

      if (isMultiLineComment()) {
         commentLinesAfter(content, endLine);
      } else {
         highlightLinesAfter(content, endLine);
      }
      
   }
   
   /**
    * Determine how many lines we need to quote,
    * then apply quoting to each line
    * @param offset- the offset into the document where the change occurs
    * @param length- Length of string changed
    * @throws javax.swing.text.BadLocationException
    */
   public void quoteLines(int offset, int length)
           throws BadLocationException {
      // Get the document content
       String content = doc.getText(0, doc.getLength());

      //  The lines affected by the latest document update
      // Gets the child element index closest to the given offset.
      int startLine = rootElement.getElementIndex(offset);
      int endLine   = rootElement.getElementIndex(offset + length);

      /**
       * We should go from the top to the bottom so that the bottom positions
       * remains unchanged.
       */
      for (int i = endLine; i >= startLine; i--) {
         quoteLine(content, i);
      }      
   }
   private void quoteLine(String content, int line)
           throws BadLocationException {
      // offsets delimiting the line in "content"
      final int startOffset = rootElement.getElement(line).getStartOffset();
      final int endOffset = rootElement.getElement(line).getEndOffset() - 1;

      int lineLength = endOffset - startOffset;
      int contentLength = content.length();

//      if (endOffset >= contentLength) {
//         endOffset = contentLength - 1;
//      }


      doc.insertString(endOffset, "\\n'", normal);
      doc.insertString(startOffset, "'", normal);

   }
   /**
    * Get the offset to the end of the last line
    * @param offset
    * @param length
    * @return
    * @throws BadLocationException 
    */
    public int getLastLineEndOffset(int offset, int length)
           throws BadLocationException {
      int startLine = rootElement.getElementIndex(offset);
      int endLine   = rootElement.getElementIndex(offset + length);
      int endOffset = rootElement.getElement(endLine).getEndOffset() - 1;
      return endOffset;
       
   }
   
   /*=================================================*/
   /*
    *  Highlight lines when a multi line comment is still 'open'
    *  (ie. matching end delimiter has not yet been encountered)
    */
   private boolean commentLinesBefore(String content, int line) {
      int startOffset = rootElement.getElement(line).getStartOffset();
      int endOffset = rootElement.getElement(line).getEndOffset() - 1;

      //  Start of comment not found, nothing to do

      int startDelimiter = lastIndexOf(content, getStartDelimiter(), startOffset);

      if (startDelimiter < 0 || startDelimiter < startOffset) {
         return false;
      }

      //  Matching start/end of comment found, nothing to do

      int endDelimiter = indexOf(content, getEndDelimiter(), startDelimiter);

      if (endDelimiter > startOffset && endDelimiter != -1) {
         return false;
      }

      //  End of comment not found, highlight the lines

      doc.setCharacterAttributes(startDelimiter, startOffset - startDelimiter + 1, comment, false);
      return true;
   }

   /*
    *  Highlight comment lines to matching end delimiter
    */
   private void commentLinesAfter(String content, int line) {
      int offset = rootElement.getElement(line).getEndOffset();

      //  End of comment not found, nothing to do

      int endDelimiter = indexOf(content, getEndDelimiter(), offset);

      if (endDelimiter < 0) {
         return;
      }

      //  Matching start/end of comment found, comment the lines

      int startDelimiter = lastIndexOf(content, getStartDelimiter(), endDelimiter);

      if (startDelimiter < 0 || startDelimiter <= offset) {
         doc.setCharacterAttributes(offset, endDelimiter - offset + 1, comment, false);
      }
   }

   /*
    *  Highlight lines to start or end delimiter
    */
   private void highlightLinesAfter(String content, int line)
           throws BadLocationException {
      int offset = rootElement.getElement(line).getEndOffset();

      //  Start/End delimiter not found, nothing to do

      int startDelimiter = indexOf(content, getStartDelimiter(), offset);
      int endDelimiter = indexOf(content, getEndDelimiter(), offset);

      if (startDelimiter < 0) {
         startDelimiter = content.length();
      }

      if (endDelimiter < 0) {
         endDelimiter = content.length();
      }

      int delimiter = Math.min(startDelimiter, endDelimiter);

      if (delimiter < offset) {
         return;
      }

      //	Start/End delimiter found, reapply highlighting

      int endLine = rootElement.getElementIndex(delimiter);

      for (int i = line + 1; i < endLine; i++) {
         Element branch = rootElement.getElement(i);
         Element leaf = doc.getCharacterElement(branch.getStartOffset());
         AttributeSet as = leaf.getAttributes();

         if (as.isEqual(comment)) {
            applyHighlighting(content, i);
         }
      }
   }
   /*
    *  Override for other languages
    */

   protected String getStartDelimiter() {
      return "/*";
   }

   /*
    *  Override for other languages
    */
   protected String getEndDelimiter() {
      return "*/";
   }

   /* 
    *  Override for other languages 
    */
   protected String getSingleLineDelimiter() {
      return "//";
   }

   /* 
    *  Override for other languages 
    */
   protected String getEscapeString(String quoteDelimiter) {
      return "\\" + quoteDelimiter;
   }
   /*
    *  Assume the needle will the found at the start/end of the line
    */

   private int indexOf(String content, String needle, int offset) {
      int index;

      while ((index = content.indexOf(needle, offset)) != -1) {
         String text = getLine(content, index).trim();

         if (text.startsWith(needle) || text.endsWith(needle)) {
            break;
         } else {
            offset = index + 1;
         }
      }

      return index;
   }

   /*
    *  Assume the needle will the found at the start/end of the line
    */
   private int lastIndexOf(String content, String needle, int offset) {
      int index;

      while ((index = content.lastIndexOf(needle, offset)) != -1) {
         String text = getLine(content, index).trim();

         if (text.startsWith(needle) || text.endsWith(needle)) {
            break;
         } else {
            offset = index - 1;
         }
      }

      return index;
   }
   /*
    *  We have found a start delimiter
    *  and are still searching for the end delimiter
    */

   private boolean isMultiLineComment() {
      return multiLineComment;
   }

   private void setMultiLineComment(boolean value) {
      multiLineComment = value;
   }
   /*
    *  Does this line contain the start delimiter
    */

   private boolean startingMultiLineComment(String content, int startOffset, int endOffset)
           throws BadLocationException {
      int index = indexOf(content, getStartDelimiter(), startOffset);

      if ((index < 0) || (index > endOffset)) {
         return false;
      } else {
         setMultiLineComment(true);
         return true;
      }
   }

   /*
    *  Does this line contain the end delimiter
    */
   private boolean endingMultiLineComment(String content, int startOffset, int endOffset)
           throws BadLocationException {
      int index = indexOf(content, getEndDelimiter(), startOffset);

      if ((index < 0) || (index > endOffset)) {
         return false;
      } else {
         setMultiLineComment(false);
         return true;
      }
   }
   /*=================================================*/

   /**
    * Parse the line to determine the appropriate highlighting
    *
    * @param content - The full document content
    * @param line - The element index of the line to parse
    * @throws javax.swing.text.BadLocationException
    */
   private void applyHighlighting(String content, int line)
           throws BadLocationException {
      // offsets delimiting the line in "content"
      int startOffset = rootElement.getElement(line).getStartOffset();
      int endOffset = rootElement.getElement(line).getEndOffset() - 1;

      int lineLength = endOffset - startOffset;
      int contentLength = content.length();

      if (endOffset >= contentLength) {
         endOffset = contentLength - 1;
      }

      //  check for multi line comments
      //  (always set the comment attribute for the entire line)

      if (endingMultiLineComment(content, startOffset, endOffset)
              || isMultiLineComment()
              || startingMultiLineComment(content, startOffset, endOffset)) {
         doc.setCharacterAttributes(startOffset, endOffset - startOffset + 1, comment, false);
         return;
      }
      //  set normal attributes for the line 
      doc.setCharacterAttributes(startOffset, lineLength, normal, true);

      //  check for single line comment 
//      int index = content.indexOf(getSingleLineDelimiter(), startOffset);
//
//      if ((index > -1) && (index < endOffset)) {
//         doc.setCharacterAttributes(
//                 index,
//                 endOffset - index + 1,
//                 comment,
//                 false);
//         endOffset = index - 1;
//      }
      //System.out.println("Tokenize content=" + content + " line=" + line + " Tokenize startOffset=" + startOffset + " endOffset=" + endOffset);
      //  Tokenize and color the tokens
      colorTokens(content, startOffset, endOffset);
   }

   private boolean isAlreadyColored(AttributeSet startAttribute, AttributeSet endAttribute,
           String nameAttribute) {
      if (!(startAttribute.getAttribute(StyleConstants.NameAttribute).equals(nameAttribute))) {
         return false;
      }
      if (!(endAttribute.getAttribute(StyleConstants.NameAttribute).equals(nameAttribute))) {
         return false;
      }
      //System.out.println("Same attributes");
      return true;
   }

   private void colorToken(int type, int startOffset, int len) {

      Element charElement = getCharacterElement(startOffset);
      AttributeSet startAttrs = charElement.getAttributes();
      charElement = getCharacterElement(startOffset + len);
      AttributeSet endAttrs = charElement.getAttributes();
//      System.out.println("startAttrName=" + startAttrs.getAttribute(StyleConstants.NameAttribute));
//      System.out.println("endAttrName=" + endAttrs.getAttribute(StyleConstants.NameAttribute));

//      try {
//         String s = doc.getText(startOffset, len);
//         System.out.println("string ["+s+"]"+" startOffset="+startOffset+" len="+len);
//      } catch (BadLocationException ex) {
//         Exceptions.printStackTrace(ex);
//      }

      MutableAttributeSet multiAttributeSet = normal;
      switch (type) {
         /* Literals */
         case COND_LITER:
            if (!isAlreadyColored(startAttrs, endAttrs, "doubleQuote")) {
                 multiAttributeSet = doubleQuote;
            }
            break;
         case UNCOND_LITER:
            if (!isAlreadyColored(startAttrs, endAttrs, "singleQuote")) {
                 multiAttributeSet = singleQuote;             
            }
            break;
         case REP_LITER:
            if (!isAlreadyColored(startAttrs, endAttrs, "repeatLiteral")) {
                 multiAttributeSet = repeatLiteral;
            }
            break;

         case EQ:    // "="
         case LT:    // "<"
         case GT:    // ">"
         case LE:  // "<="
         case GE:  // ">="
         case NE: // "<>"
         case NOT:   // "not"
         case AND:   // "and"
         case OR:    // "or"
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                 multiAttributeSet = keyword;
            }
            break;
         case ASSIGN:  // ":="
         case SEL:     // "->"
         case DOT_DOT: // ".."
            if (!isAlreadyColored(startAttrs, endAttrs, "normalBold")) {
                multiAttributeSet = normalBold;
            }
            break;
         case FIELD:
         case DNFIELD:
         case DFIELD:
         case SUBFIELD:
         case FIELD_FRAG_1:
         case FIELD_FRAG_2:
         case FIELD_FRAG_3:
            if (!isAlreadyColored(startAttrs, endAttrs, "fields")) {
                multiAttributeSet = fields;
            }
            break;
         case IF:
         case THEN:
         case ELSE:
         case FI:
         case WHILE:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;
         case INT:
         case NUMBER:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;

         case MFN:
         case RECORD:
         case CMD_C:
         case CMD_X:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;
         case MHU:
         case MHL:
         case MDU:
         case MDL:
         case MPU:
         case MPL:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;
         case VAR_E:
         case VAR_S:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;
         case EXT_FUNC:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;
         case EXT_FMT:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;
         case FNC_VAL:
         case FNC_RSUM:
         case FNC_RMIN:
         case FNC_RMAX:
         case FNC_RAVR:
         case FNC_L:
         case FNC_LR:
         case FNC_NPST:
         case FNC_NOCC:
         case FNC_OCC:
         case FNC_SIZE:
         case FNC_TYPE:
         case FNC_TAG:
         case FNC_REF:
         case FNC_S:
         case FNC_SS:
         case FNC_DATE:
         case FNC_DB:
         case FNC_F:
         case FNC_A:
         case FNC_P:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;
         case FONTS:
         case COLORS:
         case CMD_M:
         case CMD_TAB:
         case CMD_QC:
         case CMD_QJ:
         case CMD_QR:
         case CMD_BOLD:
         case CMD_ITAL:
         case CMD_UL:
         case BOX:
         case PICT:
         case BPICT:
         case TITLE:
         case KEEPL:
         case CMD_NEW_PAGE:
         case CMD_FONT:
         case CMD_FONT_SIZE:
         case CMD_COLOR:
         case LINK:
            if (!isAlreadyColored(startAttrs, endAttrs, "keyword")) {
                multiAttributeSet = keyword;
            }
            break;
         default:
            if (!isAlreadyColored(startAttrs, endAttrs, "normal")) {
                multiAttributeSet = normal;
            }
      }
        if (fontSize_ != DEFAULT_FONT_SIZE) {
            StyleConstants.setFontSize(multiAttributeSet, fontSize_);
        }
        doc.setCharacterAttributes(startOffset, len, multiAttributeSet, false);
   }

   /**
    * Parse the line for tokens to color
    *
    * @param content
    * @param startOffset
    * @param endOffset
    */
   private void colorTokens(String content, int startOffset, int endOffset) {
      if (content.length() == 0 || startOffset == -1 || endOffset == -1) {
         return;
      }
    
         // Scan the line
         String s = content.substring(startOffset, endOffset + 1);

         // Scan the line
         Reader r = new StringReader(s);
         NoTabExpansionCharStream scs = new NoTabExpansionCharStream(r);
         // Set the tab size to 1 to avoid column indexes with tab expansion
         scs.setTabSize(1);
         FmtParserTokenManager mgr = new FmtParserTokenManager(scs);
         
         
         while (true) {
            try {
               if (colorAllTokens(mgr, startOffset).kind == EOF) {
                  break;
               }
            } catch (TokenMgrError tme) {
               
//                  GuiGlobal.output("TokenMgrError Lexical Error: " + tme.getMessage());
//               try {
//                  mgr.recoverTo(' ');
//               } catch (IOException ex) {
//                  Exceptions.printStackTrace(ex);
//               }
            }
         }

        
      
   }
   
    private  Token colorAllTokens(FmtParserTokenManager mgr, int startOffset) {
      Token t;
      for (t = mgr.getNextToken(); t.kind != EOF; t = mgr.getNextToken()) {
          int col = t.beginColumn;
            int len = t.image.length();
            if (t.kind == COND_LITER || t.kind == UNCOND_LITER || t.kind == REP_LITER) {
               // Increment column because the token is stripped from the surrounding
               // delimiters
               col++;
            }
            //System.out.println("\n================\n"+"Token="+t.image+" col="+col+" len="+len);
            colorToken(t.kind, col + startOffset - 1, len);
      }
      return t;
   }
   
  

   private String getLine(String content, int offset) {
      int line = rootElement.getElementIndex(offset);
      Element lineElement = rootElement.getElement(line);
      int start = lineElement.getStartOffset();
      int end = lineElement.getEndOffset();
      return content.substring(start, end - 1);
   }

    void setFontSize(int fontSize) {
        fontSize_ = fontSize;
       
    }
}