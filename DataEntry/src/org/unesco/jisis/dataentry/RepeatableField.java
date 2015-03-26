/*
 * RepeatableField.java
 *
 * Created on July 13, 2006, 7:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



package org.unesco.jisis.dataentry;

import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.openide.util.Exceptions;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.util.SerializableImage;

import org.unesco.jisis.jisisutils.gui.TextDataEntryDocument;

/**
 *
 * @author jcd
 *
 * RepeatableField extends JTextPane which is a text component that can be
 * marked up with attributes that are represented graphically.
 *
 */
class MyKeyListener extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent ke) {
        JTextComponent c = (JTextComponent) ke.getSource();
        char ch = ke.getKeyChar();
//        String msg = "Typing !";
//        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
        
    }
}
public class RepeatableField extends JTextPane implements ActionListener, DocumentListener {
   private int           id_;    // Occurrence index
   private int           type_       = Global.FIELD_TYPE_STRING;
   protected boolean     isModified_ = false;
   protected UndoManager undoManager = new UndoManager();

   /**
    * Creates a new instance of RepeatableField
    */
   public RepeatableField(String text, int id) {
      super();
      id_ = id;
      type_ = Global.FIELD_TYPE_STRING;
      // TextDataEntryDocument extends DefaultStyledDocument so that the subfield
      // delimiters will be highlighted
      TextDataEntryDocument doc = new TextDataEntryDocument();
      doc.addDocumentListener(this);
      setDocument(doc);

      this.setText(text);
      this.setDragEnabled(true);
      setUndoRedo();
      this.addKeyListener(new MyKeyListener());

      isModified_ = false;
   }

   //==================================
   // Needed for horizontal scrolling!
   //==================================
//   @Override
//   public boolean getScrollableTracksViewportWidth() {
//      return (getSize().width < getParent().getSize().width);
//   }
//
//   @Override
//   public void setSize(Dimension d) {
//      if (d.width < getParent().getSize().width) {
//         d.width = getParent().getSize().width;
//      }
//      super.setSize(d);
//   }



   /**
    * Constructor for a BLOB field
    * @param id
    */
   public RepeatableField(int id) {

      super();
      type_ = Global.FIELD_TYPE_BLOB;
      id_ = id;
      getActionMap().put("paste-from-clipboard", new AbstractAction() {

         public void actionPerformed(ActionEvent e) {
            JTextPane pane = (JTextPane) e.getSource();
            // get the contents on the clipboard in a Transferable object

            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            // check if contents are empty, if so, return
            if (t == null) {
               return;
            }
            DataFlavor[] df = t.getTransferDataFlavors();
            for (DataFlavor flavor : df) {
               String flavorName = flavor.getHumanPresentableName();
               String mime = flavor.getMimeType();
               System.out.println("flavorName=" + flavorName + " mime=" + mime);
            }
            // If the user is pasting an image, we insert the image in the
            // JTextPane

            if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
               BufferedImage image;
               try {
                  image = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
                  pane.insertIcon(new ImageIcon(image));
               } catch (Exception e1) {
                  e1.printStackTrace();
               }
            } else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
               try {
                  java.util.List list = (java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor);
                  ListIterator it = list.listIterator();
                  while (it.hasNext()) {
                     File f = (File) it.next();
                     ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                     pane.insertIcon(icon);
                  }
               } catch (Exception e1) {
                  e1.printStackTrace();
               }
            }
            if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
               String text = "";
               try {
                  text = (String) t.getTransferData(DataFlavor.stringFlavor);
               } catch (UnsupportedFlavorException ex) {
                  Exceptions.printStackTrace(ex);
               } catch (IOException ex) {
                  Exceptions.printStackTrace(ex);
               }
               pane.paste();
            }
         }
      });
      //this.setValue(data);
      this.setDragEnabled(true);
      setUndoRedo();
      isModified_ = false;
   }

   void setJTextPaneFont(Font font) {

      System.out.println("Changing font to"+font.toString());
      this.setFont(font);
      updateUI();
      
   }

   private void setUndoRedo() {

      undoManager.setLimit(500);
      Action undoAction = new UndoAction(undoManager);
      Action redoAction = new RedoAction(undoManager);
      // Assign the actions to keys
      registerKeyboardAction(undoAction,
                             KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK),
                             JComponent.WHEN_FOCUSED);
      registerKeyboardAction(redoAction,
                             KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK),
                             JComponent.WHEN_FOCUSED);
      getDocument().addUndoableEditListener(undoManager);
      undoManager.discardAllEdits();
   }

   public void insertUpdate(DocumentEvent evt) {
      isModified_ = true;
   }

   public void removeUpdate(DocumentEvent evt) {
      isModified_ = true;
   }

   public void changedUpdate(DocumentEvent evt) {
      isModified_ = true;
   }

   public void actionPerformed(ActionEvent arg0) {
      //throw new UnsupportedOperationException("Not supported yet.");
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

   /**
    * Exctract the text and images from the JTextPane Document
    * 0:3 int - Text length in bytes
    * 4:7 int - Number of ImageContent objects (n)
    * Text
    * offset(int), Image(Object)    (n times)
    *
    * @return - byte array of the above data
    */
     private byte[] getDocumentValue() {
      StyledDocument doc = this.getStyledDocument();
      doc.removeUndoableEditListener(undoManager);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream    oos;
      try {
         Image                image        = null;
         String               strText      = doc.getText(0, doc.getLength());
         String               noHTMLString = strText.replaceAll("\\<.*?\\>", "");
         List<ImageContent> v            = new ArrayList<ImageContent>();
         for (int i = 0; i <= noHTMLString.length() + 1; i++) {
            if (doc.getCharacterElement(i).getName().equals("icon")) {
               Element      element = doc.getCharacterElement(i);
               AttributeSet set     = element.getAttributes();
               if (StyleConstants.getIcon(set) != null) {
                  ImageIcon imageIcon = (ImageIcon) StyleConstants.getIcon(set);
                  v.add(new ImageContent(element.getStartOffset(), imageIcon));
               }
            }
         }
         try {
            oos = new ObjectOutputStream(out);
            oos.writeInt(noHTMLString.length());
            System.out.println("Serialize - Text Length =" + noHTMLString.length());
            oos.writeInt(v.size());
            if (noHTMLString.length() > 0) {
               oos.writeUTF(noHTMLString);
            }
            // assertTrue(out.toByteArray().length > 0);
            if (v.size() > 0) {
               for (ImageContent ic : v) {
                  oos.writeInt(ic.offset_);
                  System.out.println("Serialize - Image offset =" + ic.offset_);
                  oos.writeObject(ic.image_);
               }
            }
            oos.close();
         } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
         }
      } catch (BadLocationException ex) {
         Exceptions.printStackTrace(ex);
      }
      return out.toByteArray();
   }

   private void setValue(byte[] value) {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      this.setDocument(doc);
      if (value.length == 0)
          return;
      InputStream       in = new ByteArrayInputStream(value);
      ObjectInputStream ois;
      try {
         ois = new ObjectInputStream(in);
         int strLen  = ois.readInt();
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
         doc.addUndoableEditListener(undoManager);
      } catch (BadLocationException ex) {
         Exceptions.printStackTrace(ex);
      } catch (ClassNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   public void setValue(Object value) {
      if (value == null) {
         this.setText("");
      } else if (value instanceof String) {
         this.setText((String) value);
      } else if (value instanceof byte[]) {
         this.setValue((byte[]) value);
      } else {
         throw new RuntimeException("Unhandled type " + value.getClass().getName());
      }
   }

   public Object getValue() {
      if (type_ == Global.FIELD_TYPE_STRING) {
         return this.getText();
      } else if (type_ == Global.FIELD_TYPE_BLOB) {
         return getDocumentValue();
      } else {
         throw new RuntimeException("Unhandled type " + type_);
      }
   }

   int getType() {
      return type_;
   }

   public int getID() {
      return id_;
   }
   
   public boolean isModified() {
      return isModified_;
   }

   // The Undo action
   public class UndoAction extends AbstractAction {
      public UndoAction(UndoManager manager) {
         this.manager = manager;
      }

      public void actionPerformed(ActionEvent evt) {
         try {
            manager.undo();
         } catch (CannotUndoException e) {
            Toolkit.getDefaultToolkit().beep();
         }
      }

      private UndoManager manager;
   }

   // The Redo action
   public class RedoAction extends AbstractAction {
      public RedoAction(UndoManager manager) {
         this.manager = manager;
      }

      public void actionPerformed(ActionEvent evt) {
         try {
            manager.redo();
         } catch (CannotRedoException e) {
            Toolkit.getDefaultToolkit().beep();
         }
      }

      private UndoManager manager;
   }
}
