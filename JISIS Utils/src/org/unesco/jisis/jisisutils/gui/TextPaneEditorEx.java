/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisisutils.gui;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import org.openide.util.Exceptions;
import org.unesco.jisis.corelib.util.SerializableImage;

/**
 *
 * @author jc_dauphin
 */
public class TextPaneEditorEx extends AbstractCellEditor implements TableCellEditor {
   JTable    table_;
   JTextPane textPane_;
   private JScrollPane scrollPane_ = null;

   public TextPaneEditorEx() {
      textPane_ = new JTextPane();
      textPane_.setEditable(false);
      scrollPane_ = new JScrollPane(textPane_);
      scrollPane_.setBorder(BorderFactory.createEmptyBorder());
   }

   public Object getCellEditorValue() {
      return textPane_.getDocument();
   }

   // Implement the one method specified by TableCellEditor.
   public Component getTableCellEditorComponent(JTable table, Object obj, boolean isSelected,
           int r, int column) {
      table_ = table;
      if (obj == null) {
         textPane_.setText("");
      } else if (obj instanceof String) {
         System.out.println("String row=" + r + " col=" + column);
         textPane_.setText((String) obj);
      } else if (obj instanceof byte[]) {
         System.out.println("Bytes[] row=" + r + " col=" + column);
         this.setValue((byte[]) obj);
      } else {
         throw new RuntimeException("Unhandled type " + obj.getClass().getName());
      }
      
      return scrollPane_;
   }
    private void setValue(byte[] value) {
       setValueEx(value);
//      InputStream       in = new ByteArrayInputStream(value);
//      ObjectInputStream ois;
//      try {
//         ois = new ObjectInputStream(in);
//         Object         o   = ois.readObject();
//         StyledDocument doc = (StyledDocument) o;
//
//         textPane_.setDocument(doc);
//
//      } catch (ClassNotFoundException ex) {
//         Exceptions.printStackTrace(ex);
//      } catch (IOException ex) {
//         Exceptions.printStackTrace(ex);
//      }
   }
    private void setValueEx(byte[] value) {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      textPane_.setDocument(doc);
      InputStream in = new ByteArrayInputStream(value);
      ObjectInputStream ois;
      try {
         ois = new ObjectInputStream(in);
         int strLen = ois.readInt();
         int nImages = ois.readInt();
         if (strLen > 0) {
            String text = ois.readUTF();
            doc.insertString(0, text, null);
         }
         if (nImages > 0) {
             List<ImageContent> v = new ArrayList<ImageContent>(nImages);
            for (int i = 0; i < nImages; i++) {
               int offset = ois.readInt();
               System.out.println("DeSerialize - Image offset =" + offset);
               SerializableImage image = (SerializableImage) ois.readObject();
               v.add(new ImageContent(offset, image));
            }
           
            // sort on offset in case
            Collections.sort(v, new Comparator() {
               @Override
               public int compare(Object o1, Object o2) {
                  ImageContent ic1 = (ImageContent) o1;
                  ImageContent ic2 = (ImageContent) o2;
                  if (ic1.offset_ < ic2.offset_) {
                     return -1;
                  }
                  if (ic1.offset_ > ic2.offset_) {
                     return 1;
                  }
                  /* Same offset */
                  return 0;
               }
            });
            // Start from highest to lowest
             for (int i = nImages - 1; i >= 0; i--) {
               // insert a blank char at image position
               int offset = v.get(i).offset_;
               //doc.insertString(offset, " ", null);
//               this.setSelectionStart(offset);
//               this.setSelectionEnd(offset);
               textPane_.setCaretPosition(offset);
               System.out.println("Insert image offset =" + offset);
               textPane_.insertIcon(new ImageIcon(v.get(i).image_.getImage()));
            }
            
         }
         //doc.addUndoableEditListener(undoManager);
      } catch (BadLocationException ex) {
         Exceptions.printStackTrace(ex);
      } catch (ClassNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   
   
   class ImageContent {
      int       offset_;
      SerializableImage image_;

      public ImageContent(int offset, ImageIcon imageIcon) {
         offset_ = offset;
         image_  = new SerializableImage(imageIcon.getImage());
      }

      private ImageContent(int offset, SerializableImage image) {
          offset_ = offset;
         image_ = image;
      }
   }
}
