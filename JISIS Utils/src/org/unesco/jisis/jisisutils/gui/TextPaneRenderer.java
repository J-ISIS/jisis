/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisisutils.gui;

import java.awt.Color;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import org.openide.util.Exceptions;
import org.unesco.jisis.corelib.record.ImageContext;
import org.unesco.jisis.corelib.util.SerializableImage;

/**
 *
 * @author jc_dauphin
 */
public class TextPaneRenderer extends JTextPane implements TableCellRenderer {
   protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

   public TextPaneRenderer() {
      super();
      // setWrapStyleWord(true);
      // setLineWrap(true);
      setOpaque(true);
      setBorder(noFocusBorder);
   }

   private Color unselectedForeground;
   private Color unselectedBackground;

   /**
    * Overrides <code>JComponent.setForeground</code> to assign the unselected-foreground color to
    * the specified color.
    *
    * @param c set the foreground color to this value
    */
   public void setForeground(Color c) {
      super.setForeground(c);
      unselectedForeground = c;
   }

   /**
    * Overrides <code>JComponent.setBackground</code> to assign the unselected-background color to
    * the specified color.
    *
    * @param c set the background color to this value
    */
   public void setBackground(Color c) {
      super.setBackground(c);
      unselectedBackground = c;
   }

   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
           boolean hasFocus, int row, int column) {
      if (isSelected) {
         super.setForeground(table.getSelectionForeground());
         super.setBackground(table.getSelectionBackground());
      } else {
         super.setForeground((unselectedForeground != null)
                             ? unselectedForeground
                             : table.getForeground());
         super.setBackground((unselectedBackground != null)
                             ? unselectedBackground
                             : table.getBackground());
      }
      setFont(table.getFont());
      if (hasFocus) {
         setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
         if (table.isCellEditable(row, column)) {
            super.setForeground(UIManager.getColor("Table.focusCellForeground"));
            super.setBackground(UIManager.getColor("Table.focusCellBackground"));
         }
      } else {
         setBorder(noFocusBorder);
      }
      if (value == null) {
         this.setText("");
      } else if (value instanceof String) {
         System.out.println("String row=" + row + " col=" + column);
         this.setText((String) value);
      } else if (value instanceof byte[]) {
         System.out.println("Bytes[] row=" + row + " col=" + column);
         this.setValue((byte[]) value);
      } else {
         throw new RuntimeException("Unhandled type " + value.getClass().getName());
      }
      
      return this;
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
//         this.setDocument(doc);
//
//      } catch (ClassNotFoundException ex) {
//         Exceptions.printStackTrace(ex);
//      } catch (IOException ex) {
//         Exceptions.printStackTrace(ex);
//      }
   }
   private void setValueEx(byte[] value) {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      this.setDocument(doc);
      if (value.length == 0) {
         return;
      }
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
               this.setCaretPosition(offset);
               System.out.println("Insert image offset =" + offset);
               this.insertIcon(new ImageIcon(v.get(i).image_.getImage()));
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
