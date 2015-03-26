/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;

/**
 *
 * @author jcd
 */
/** This code is inspired by an example of a book :
 *  Java/IO  - Elliotte Rusty Harold - ed. Oreilly
 *
 */
public class TextFilePreview extends JTextArea implements PropertyChangeListener {

   private String preview = "";
   private File selectedFile = null;

   public TextFilePreview(JFileChooser fc) {
      super(10, 20);
      this.setEditable(false);
      this.setPreferredSize(new Dimension(150, 150));
      this.setLineWrap(true);
      this.setBorder(BorderFactory.createEtchedBorder());
      fc.addPropertyChangeListener(this);

   }

   public void loadText() {
      if (selectedFile != null) {
         try {
            if (selectedFile.isDirectory()) {
               preview = "";
               return;
            }

            FileInputStream in = new FileInputStream(selectedFile);
            byte[] data = new byte[250];
            int byteRead = 0;
            for (int i = 0; i < 250; i++) {
               int b = in.read();
               if (b == -1) {
                  break;
               }
               byteRead++;
               data[i] = (byte) b;
            }
            preview = new String(data, 0, byteRead);
            in.close();
         } catch (Exception e) {
            preview = "Preview error";
         }
      }
   }

   public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
         selectedFile = (File) e.getNewValue();
         if (isShowing()) {
            loadText();
            this.setText(preview);
         }
      }
   }
}
