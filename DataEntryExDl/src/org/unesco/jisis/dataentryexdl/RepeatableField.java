/*
 * RepeatableField.java
 *
 * Created on July 13, 2006, 7:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



package org.unesco.jisis.dataentryexdl;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.List;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.util.SerializableImage;
import org.unesco.jisis.jisisutils.gui.TextDataEntryDocument;
import org.unesco.jisis.jisisutils.threads.GuiExecutor;
import org.unesco.jisis.jisisutils.threads.IdeCursor;

/**
 * A RepeatableField object encapsulate the field occurrence data entry text field where the user can edit
 *
 * RepeatableField extends JTextPane which is a text component that can be
 * marked up with attributes that are represented graphically.
 * 
 * @author jcd
 *
 *
 */



public final class RepeatableField extends JTextPane implements ActionListener, DocumentListener {
   private int           id_;    // Occurrence index
   private int           type_       = Global.FIELD_TYPE_STRING;
   protected boolean     isModified_ = false;
   protected UndoManager undoManager_ = new UndoManager();
   // TextDataEntryDocument extends DefaultStyledDocument so that the subfield
   // delimiters will be highlighted
   private DefaultStyledDocument  document_;
   private JProgressBar progressBar;
   OccurrenceEditEvent occurrenceEditEvent_;
  
   // Line height of this RepeatableField component
     private int fontLineHeight_;
        
     // Metrics of this RepeatableField component
     private FontMetrics fontMetrics_;
   

      
   private void loadHugeDocument(final String text) {
      
      final JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();
      progressBar = new JProgressBar();
      progressBar.setStringPainted(true);
      mainWin.add(progressBar, BorderLayout.SOUTH);
      JTextPaneDisplayWorker worker = new JTextPaneDisplayWorker(this, text) {
         // This method is invoked when the worker is finished
         // its task
         @Override
         protected void done() {
            try {
               //  Note that the method get will throw any exception thrown 
               // during the execution of the worker.
              
               get();
               IdeCursor.changeCursorWaitStatus(false);
               StatusDisplayer.getDefault().setStatusText("Loading GUI Please Wait ...");
               RepaintManager.currentManager(mainWin).paintDirtyRegions();
               progressBar.setVisible(false);
               RepeatableField.this.setCaretPosition(0);
               RepeatableField.this.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
               isModified_ = false;
            } catch (Exception e) {
               String why = null;
               Throwable cause = e.getCause();
               if (cause != null) {
                  why = cause.getMessage();
               } else {
                  why = e.getMessage();
               }
               System.err.println("Error loading RepeatableField: " + why);

               JOptionPane.showMessageDialog(mainWin,
                       "Error in loadHugeDocument:", why,
                       JOptionPane.ERROR_MESSAGE);
            }
         }
      };
      
      // A property listener used to update the progress bar
      PropertyChangeListener listener =
              new PropertyChangeListener() {
                 @Override
                 public void propertyChange(PropertyChangeEvent event) {
                    if ("progress".equals(event.getPropertyName())) {
                       progressBar.setValue((Integer) event.getNewValue());
                    }
                 }
              };
      worker.addPropertyChangeListener(listener);
      
      // Start the worker. Note that control is 
      // returned immediately
    
      StatusDisplayer.getDefault().setStatusText("Loading GUI Please Wait ...");
      RepaintManager.currentManager(mainWin).paintDirtyRegions();
      IdeCursor.changeCursorWaitStatus(true);
      worker.execute();
      

//      document_ =  (DefaultStyledDocument) RepeatableField.this.getDocument();
//      Document blank = new DefaultStyledDocument();
//      RepeatableField.this.setDocument(blank);
//     
//      Element e = document_.getDefaultRootElement();
//      // Copy attribute Set
//      AttributeSet attr = e.getAttributes().copyAttributes();
//
//      try {
//         document_.insertString(0, text, attr);  
//      } catch (BadLocationException ex) {
//         ex.printStackTrace();
//      }
//      Date start = new Date();
//      
//     RepeatableField.this.setDocument(document_);
//     Date end = new Date();
//     System.out.println(Long.toString(end.getTime() - start.getTime())
//                 + " milliseconds to setDocument");

   }

  
   private void forceUpdatingDocumentDisplay() {
      RepeatableField.this.setDocument(new DefaultStyledDocument());
      RepeatableField.this.setDocument(document_);
      RepeatableField.this.revalidate();
      RepeatableField.this.repaint();
   }

   
  
   /**
    * Creates a new instance of RepeatableField
    *
    * @param text - Field Occurrence content
    * @param id - Occurrence index
    */
    @SuppressWarnings("LeakingThisInConstructor")
    public RepeatableField(OccurrenceEditEvent occurrenceEditEvent, final String text, final int id, final int fieldType) {
        super();

        occurrenceEditEvent_ = occurrenceEditEvent; // Callback object
        id_ = id;
        type_ = Global.FIELD_TYPE_STRING;

        Font font = new Font("Monospaced", Font.PLAIN, DataEntryTopComponent.DEFAULT_FONT_SIZE);
        setJTextPaneFont(font);
        RepeatableField.this.setContentType("text/plain;charset=UTF-8");

        //System.out.println("FIELD TEXT LENGTH " + text.length());
        final JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();

        if (fieldType == Global.FIELD_TYPE_DOC) {
//         final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
//          StatusDisplayer.getDefault().setStatusText("Updating GUI Text ...");
//          RepaintManager.currentManager(frame).paintDirtyRegions();
            document_ = new DefaultStyledDocument();
            RepeatableField.this.setDocument(document_);
            loadHugeDocument(text);
       } else {
          // TextDataEntryDocument extends DefaultStyledDocument so that the subfield
          // delimiters will be highlighted
          document_ = new TextDataEntryDocument();
          RepeatableField.this.setDocument(document_);
          RepeatableField.this.setText(text);
          Action deleteFieldAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                RepeatableField source = (RepeatableField) e.getSource();
                occurrenceEditEvent_.notifyCaller(null, id_, "delete-field");

             }
          };
          getInputMap().put(KeyStroke.getKeyStroke("F2"),
                  "deleteField");
          getActionMap().put("deleteField",
                  deleteFieldAction);
       }
        isModified_ = false;
        GuiExecutor.instance().execute(new Runnable() {
            @Override
            public void run() {
                RepeatableField.this.setCaretPosition(0);
                RepeatableField.this.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
                document_.addDocumentListener(RepeatableField.this);

                RepeatableField.this.setDragEnabled(true);
                setUndoRedo();
                isModified_ = false;
            }
        });

    }

   private void setMainText(String text, JTextComponent component) {
      document_ = new DefaultStyledDocument();
      
      Element e = document_.getDefaultRootElement();
      // Copy attribute Set
      AttributeSet attr = e.getAttributes().copyAttributes();
      try {
         document_.insertString(0, text, attr);
      } catch (BadLocationException err) {
         err.printStackTrace();
      }
      component.setDocument(document_);
     
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
    public RepeatableField(OccurrenceEditEvent occurrenceEditEvent, int id) {

        super();
        type_ = Global.FIELD_TYPE_BLOB;
        id_ = id;
        occurrenceEditEvent_ = occurrenceEditEvent; // Callback object
        getActionMap().put("paste-from-clipboard", new AbstractAction() {

            @Override
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

    public void setJTextPaneFont(Font font) {

        System.out.println("Changing font to" + font.toString());

        super.setFont(font);
        fontMetrics_ = getFontMetrics(getFont());
        fontLineHeight_ = fontMetrics_.getHeight();

        updateUI();
    }
    
    public int getFontLineHeight() {
        return fontLineHeight_;
    }
   
  

   private void setUndoRedo() {

     
      undoManager_.setLimit(500);
      Action undoAction = new UndoAction(undoManager_);
      Action redoAction = new RedoAction(undoManager_);
      ActionMap actionMap = this.getActionMap();
      org.openide.actions.UndoAction u = SystemAction.get(org.openide.actions.UndoAction.class);
      actionMap.put(org.openide.actions.UndoAction.ACTION_COMMAND_KEY, undoAction);
     
      org.openide.actions.RedoAction r = SystemAction.get(org.openide.actions.RedoAction.class);
      actionMap.put(org.openide.actions.RedoAction.ACTION_COMMAND_KEY, redoAction);
     
      // Assign the actions to keys
      registerKeyboardAction(undoAction,
                             KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK),
                             JComponent.WHEN_FOCUSED);
      registerKeyboardAction(redoAction,
                             KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK),
                             JComponent.WHEN_FOCUSED);
      
      getDocument().addUndoableEditListener(undoManager_);
      undoManager_.discardAllEdits();
   }

   @Override
   public void insertUpdate(DocumentEvent evt) {
      isModified_ = true;
      System.out.println("insertUpdate");
   }

   public void removeUpdate(DocumentEvent evt) {
      isModified_ = true;
   }

   /**
    * The changedUpdate() provides notification of attribute changes
    * @param evt 
    */
   @Override
   public void changedUpdate(DocumentEvent evt) {    
      
      //isModified_ = true;
       System.out.println("changedUpdate");
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
      doc.removeUndoableEditListener(undoManager_);
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
         doc.addUndoableEditListener(undoManager_);
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
   
    public void setModified(boolean modified) {
     isModified_ = modified;
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
