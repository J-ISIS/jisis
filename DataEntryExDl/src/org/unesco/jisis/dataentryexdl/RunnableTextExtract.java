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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.openide.util.Exceptions;

/**
 *
 * @author jcd
 */
public class RunnableTextExtract implements Runnable {

   private File f;
   private String text = null;
   public static Logger logger = null;
    static {
      logger = Logger.getRootLogger();
      logger.setLevel(Level.OFF);

   }
    private static final int PROGRESS_BAR_WIDTH = 200;

   public RunnableTextExtract(File f) {
      this.f = f;
      
      
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

   @Override
   public void run() {
      
      try {
         text = extractTextFromDocument(new FileInputStream(f));
      } catch (FileNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      }

   }
   
   public String getText() {
      return text;
   }
}
