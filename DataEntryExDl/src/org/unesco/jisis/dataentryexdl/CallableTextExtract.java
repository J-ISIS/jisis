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
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.openide.util.Exceptions;

/**
 *
 * @author jcd
 */
public class CallableTextExtract implements Callable<String> {

   private File f;

   public CallableTextExtract(File f) {
      this.f = f;
      
   }

   public static String extractTextFromDocument(InputStream in) {

      Logger.getRootLogger().setLevel(Level.INFO);
      
      Logger logger = Logger.getRootLogger();
      logger.setLevel(Level.OFF);

   
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
   public String call() throws Exception {
      String text = null;
      try {
         text = extractTextFromDocument(new FileInputStream(f));
      } catch (FileNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } 
      return text;
   }
}
