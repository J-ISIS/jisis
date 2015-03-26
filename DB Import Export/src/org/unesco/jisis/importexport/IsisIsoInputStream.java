/*
 * IsisIsoInputStream.java
 *
 * Created on 10 janvier 2008, 15:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.unesco.jisis.importexport;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author jc_dauphin
 */
public class IsisIsoInputStream extends FilterInputStream {

   protected byte[] buf = new byte[80];
   int ipos = 0;

   /**
    * Constructs a new IsisIsoInputStream initialized with the
    * specified input stream
    * @param in the input stream
    */
   public IsisIsoInputStream(InputStream in) {
      super(in);
   }

   public int readFilteringCRLF() throws IOException {

      // This is the routine that really implements the special
      // functionality of this class; the others just call this
      // one to get the data that they need.
      int c;
      do {
         c = in.read();
      } while ((c == '\n' || c == '\r') && c != -1);
      return c;
   }

   /**
    * Reads the input stream until if fills up buffer b with b.length bytes of 
    * data.The CR/LF characters are skipped
    */
   public int readFilteringCRLF(byte[] b) throws IOException {
      for (int i = 0; i < b.length; i++) {
         int c = readFilteringCRLF();
         if (c == -1) {
            return i;
         }
         b[i] = (byte) c;
      }
      return b.length;

   }

   public int readFilteringCRLF(byte[] b, int off, int len) throws IOException {

      for (int i = 0; i < len; i++) {
         int c = readFilteringCRLF();
         if (c == -1) {
            return i;
         }
         b[off + i] = (byte) c;
      }
      return len;

   }
}
