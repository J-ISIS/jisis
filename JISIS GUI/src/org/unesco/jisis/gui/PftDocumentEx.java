/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import java.awt.Color;
import java.awt.Font;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

import javax.swing.event.UndoableEditListener;
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
public class PftDocumentEx extends DefaultStyledDocument implements FmtParserConstants {

    private final DefaultStyledDocument doc;
    private UndoableEditListener[] undoListeners;
    private final Element rootElement;
    private boolean multiLineComment;
    private final MutableAttributeSet normal;
    private final MutableAttributeSet keyword;
    private final MutableAttributeSet comment;
    private final MutableAttributeSet doubleQuote;
    private final MutableAttributeSet singleQuote;
    private final MutableAttributeSet repeatLiteral;
    private final MutableAttributeSet fields;
    private final MutableAttributeSet execution;
    private final MutableAttributeSet select;
    private final MutableAttributeSet normalBold;
    private HashSet keywords;
    private final String fontFamily = "Monospaced";
    private final int fontSize = 12;
    
    private int fontSize_;
    private boolean sourceIsNotPft = true;

    public PftDocumentEx() {
        doc = this;

        rootElement = doc.getDefaultRootElement();
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

        normal = new SimpleAttributeSet();
        StyleConstants.setForeground(normal, Color.black);
        
       fontSize_ = fontSize;

        if (Global.getApplicationFont() != null) {
            Font font = Global.getApplicationFont();
            
            StyleConstants.setFontFamily(normal, font.getFamily());
            StyleConstants.setFontSize(normal, font.getSize());          
        } else {
            StyleConstants.setFontFamily(normal, fontFamily);
            StyleConstants.setFontSize(normal, fontSize);
        }
         normal.addAttribute(StyleConstants.NameAttribute, "normal");

        normalBold = new SimpleAttributeSet(normal);
        StyleConstants.setBold(normalBold, true);

        fields = new SimpleAttributeSet(normal);
        StyleConstants.setForeground(fields, Color.decode("#000099"));
        StyleConstants.setBold(fields, false);
        fields.addAttribute(StyleConstants.NameAttribute, "fields");

        comment = new SimpleAttributeSet(normal);
        StyleConstants.setForeground(comment, Color.decode("#990000"));
        StyleConstants.setBold(comment, false);
      //StyleConstants.setItalic(comment, false);

        keyword = new SimpleAttributeSet(normal);
        StyleConstants.setForeground(keyword, Color.decode("#660099"));
        StyleConstants.setBold(keyword, true);
        keyword.addAttribute(StyleConstants.NameAttribute, "keyword");

        doubleQuote = new SimpleAttributeSet(normal);
        StyleConstants.setForeground(doubleQuote, Color.decode("#48D1CC"));
        doubleQuote.addAttribute(StyleConstants.NameAttribute, "doubleQuote");

        repeatLiteral = new SimpleAttributeSet(normal);
        StyleConstants.setForeground(repeatLiteral, Color.decode("#FF009B"));
        repeatLiteral.addAttribute(StyleConstants.NameAttribute, "repeatLiteral");

        execution = new SimpleAttributeSet(normal);
        StyleConstants.setForeground(execution, Color.red);

        singleQuote = new SimpleAttributeSet(normal);
        StyleConstants.setForeground(singleQuote, Color.decode("#99006b"));
        singleQuote.addAttribute(StyleConstants.NameAttribute, "singleQuote");

        select = execution;
    }
    
    public void setFontSize(int fontSize) {
        fontSize_ = fontSize;
    }

    public void setKeywords(HashSet aKeywordList) {
        if (aKeywordList != null) {
            this.keywords = aKeywordList;
        }
    }

    /**
     * Override to apply syntax highlighting after the document has been updated
     *
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
     *
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
     * Determine how many lines have been changed, then apply highlighting to each line
     *
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
        int endLine = rootElement.getElementIndex(offset + length);

        for (int i = startLine; i <= endLine; i++) {
            applyHighlighting(content, i);
        }

    }

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

      //System.out.println("Tokenize content=" + content + " line=" + line + " Tokenize startOffset=" + startOffset + " endOffset=" + endOffset);
        //  Tokenize and color the tokens
        if (sourceIsNotPft) {
            MutableAttributeSet multiAttributeSet = normal;
            if (fontSize_ != fontSize) {
                StyleConstants.setFontSize(multiAttributeSet, fontSize_);
            }
            doc.setCharacterAttributes(startOffset, lineLength, multiAttributeSet, false);
        } else {
            colorTokens(content, startOffset, endOffset);
        }
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
      //System.out.println("startAttrName=" + startAttrs.getAttribute(StyleConstants.NameAttribute));
        //System.out.println("endAttrName=" + endAttrs.getAttribute(StyleConstants.NameAttribute));

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
                if (!isAlreadyColored(startAttrs, endAttrs, "doubleQuote")) {
                    multiAttributeSet = repeatLiteral;
                }
                break;
//         case COND_LITER_ERROR:
//         case UNCOND_LITER_ERROR:
//         case REP_LITER_ERROR:
//            break;
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

//     public static final int PLUS = 14;
//     public static final int MINUS = 15;
//     public static final int DIV = 16;
//     public static final int TIMES = 17;
//
//     public static final int UMINUS = 18;
//
//     public static final int DOT = 10;      // "."
//
//     public static final int HASH = 12;     // "#"
//     public static final int PERCENT = 13;  // "%"
//     public static final int LPAREN = 19;
//     public static final int RPAREN = 20;
//     public static final int LBRACK = 21;
//     public static final int RBRACK = 22;
//     public static final int LBRACE = 23;
//     public static final int RBRACE = 24;
//     public static final int COMMA = 25;
//     public static final int COLON = 74;   // ":"
//
//     public static final int SCANNER_ERROR = 107;
//
//     public static final int EOF = 0;
//
//     public static final int IDENT = 67;
//     public static final int error = 1;
                if (!isAlreadyColored(startAttrs, endAttrs, "normal")) {
                    multiAttributeSet = normal;
                }
        }
         if (fontSize_ != fontSize) {
            StyleConstants.setFontSize(multiAttributeSet, fontSize_);
        }
        doc.setCharacterAttributes(startOffset, len, multiAttributeSet, false);
       
    }

    /**
     * Parse the line for tokens to color    
     * @param content
     * @param startOffset
     * @param endOffset
     */
    private void colorTokens(String content, int startOffset, int endOffset) {
        if (content.length() == 0 || startOffset == -1 || endOffset == -1) {
            return;
        }
        try {
            // Scan the line
            String s = content.substring(startOffset, endOffset + 1);

            // Scan the line
            Reader r = new StringReader(s);
            SimpleCharStream scs = new SimpleCharStream(r);
            FmtParserTokenManager mgr = new FmtParserTokenManager(scs);

            Token t;
            for (t = mgr.getNextToken(); t.kind != EOF; t = mgr.getNextToken()) {
                int col = t.beginColumn;
                int len = t.image.length();
                colorToken(t.kind, col + startOffset -1, len);

            }
        } catch (TokenMgrError tme) {
            System.out.println("TokenMgrError: " + tme.getMessage());

        }
    }

    private String getLine(String content, int offset) {
        int line = rootElement.getElementIndex(offset);
        Element lineElement = rootElement.getElement(line);
        int start = lineElement.getStartOffset();
        int end = lineElement.getEndOffset();
        return content.substring(start, end - 1);
    }

}
