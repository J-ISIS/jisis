/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.SwingWorker;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.openide.util.Exceptions;

/**
 *
 * @author jcd
 */
public class TextExtractSwingWorker extends SwingWorker<String, Object> {

   private File f;
   private String text = null;
   public static Logger logger = null;

   static {
      logger = Logger.getRootLogger();
      logger.setLevel(Level.OFF);

   }

   public TextExtractSwingWorker(File f) {
      this.f = f;
   }

   @Override
   protected String doInBackground() throws Exception {
      try {
         text = extractTextFromDocument(new FileInputStream(f));
      } catch (FileNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      }
      return text;  //can be retrieved by calling get()
   }

   public static String extractTextFromDocument(InputStream in) {

      Tika tika = new Tika();
      tika.setMaxStringLength(-1);

      String text = null;
      try {
         text = tika.parseToString(in);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      } catch (TikaException ex) {
         Exceptions.printStackTrace(ex);
      }
      return text;
   }

   public String getText() {
      return text;
   }
}
