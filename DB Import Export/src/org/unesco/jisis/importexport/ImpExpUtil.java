/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.importexport;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.StringTokenizer;
import org.openide.util.Exceptions;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.exceptions.DbException;

/**
 *
 * @author jc_dauphin
 */
class FstLineParser {
   private String line_;
   private int    is_;
   private int    ie_;
   private int    maxl_;
   String         tag_;
   String         technique_;
   String         format_;

   public FstLineParser(String line) {
      line_ = line;
      maxl_ = line.length();
      is_   = 0;
      ie_   = 0;

      parse();
   }

   private void parse() {
      tag_       = nextToken();
      technique_ = nextToken();
      format_    = (line_.substring(ie_ + 1)).trim();
   }

   private String nextToken() {
      int i = ie_;

      while ((i < maxl_) && (line_.charAt(i) == ' ')) {
         i++;
      }

      is_ = i;

      while ((i < maxl_) && (line_.charAt(i) != ' ')) {
         i++;
      }

      ie_ = i;

      if (is_ > ie_) {
         return null;
      }

      return line_.substring(is_, ie_);
   }

   public String getTag() {
      return tag_;
   }

   public String getTechnique() {
      return technique_;
   }

   public String getFormat() {
      return format_;
   }
}
public class ImpExpUtil {
   /**
    * 
    * @param s        - String to convert 
    * @param encoding - Original encoding of s 
    * @return         - Converted String
    */
   public static String convertToUTF8(String s, String encoding) {
      /**
       * Converting bytes to chars is called decoding
       * A Charset is created using the Charset.forName() method.
       * String charsetName = "ISO-8859-1";
       * Charset charset = Charset.forName( charsetName );
       * converting a Byte- Buffer to a CharBuffer using a Charset:
       * Charset charset = Charset.forName( charsetName );
       * CharsetDecoder decoder = charset.newDecoder();
       * CharBuffer charBuffer = decoder.decode( byteBuffer );
       */
      Charset charset = Charset.forName(encoding);
      CharsetDecoder decoder = charset.newDecoder();
      String newStr = null;
      try {
         // Make a ByteBuffer from the byte array
         ByteBuffer byteBuffer = ByteBuffer.wrap(s.getBytes());
         // converting the Byte-Buffer to a CharBuffer using the Charset
         // define by encoding
         CharBuffer charBuffer = decoder.decode(byteBuffer);
         newStr = charBuffer.toString();
      // Save as UTF8

      } catch (CharacterCodingException ex) {
         Exceptions.printStackTrace(ex);
      }
      return newStr;
   }
   public static FieldDefinitionTable importOldFdt(String fdtFile, String encoding) {
      // create field definition table
      
      FieldDefinitionTable fdt   = new FieldDefinitionTable();
      
      try {
         File              f   = new File(fdtFile);
         FileInputStream   fis = new FileInputStream(f);
         String fdtEncoding = encoding;
         if (encoding.equals("MARC-8") || encoding.equals("ISO5426")
              || encoding.equals("ISO6937")) {
            fdtEncoding = "ISO-8859-1";
         } 
         InputStreamReader isr = new InputStreamReader(fis, fdtEncoding);
        
         BufferedReader br   = new BufferedReader(isr);
         String         line = br.readLine();
         /** Skip the PFT name lines */
         while (!line.startsWith("***")) {
            line = br.readLine();
         }
         line = br.readLine();
         while (line != null) {
            if (line.trim().length() == 0) {
               line = br.readLine();
               continue;
            }
            String          name    = line.substring(0, 30).trim();
            String          pattern = line.substring(30, 49).trim();
            String          endLine = line.substring(50);
            StringTokenizer rest    = new StringTokenizer(endLine);
            int             tag     = Integer.parseInt(rest.nextToken());
            int             boh     = Integer.parseInt(rest.nextToken());
            int             type    = Integer.parseInt(rest.nextToken());
            int             irep    = Integer.parseInt(rest.nextToken());
            boolean         rep     = (irep == 1)? true : false;

            boolean         indicators    =  false;
            boolean         firstField    =  false;

            fdt.setField(tag, name, type, indicators, rep, firstField, pattern);
            line = br.readLine();
         }
         br.close();
         isr.close();
         fis.close();
      } catch (NumberFormatException ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      } catch (FileNotFoundException ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      } catch (IOException ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      }
     
      return fdt;
   }

   public static FieldSelectionTable importOldFst(String fstFile, String encoding) {
     
      FieldSelectionTable fst   = new FieldSelectionTable();

      try {
         File              f    = new File(fstFile);
         FileInputStream   fis  = new FileInputStream(f);
          String fstEncoding = encoding;
         if (encoding.equals("MARC-8") || encoding.equals("ISO5426")
              || encoding.equals("ISO6937")) {
            fstEncoding = "ISO-8859-1";
         } 
         InputStreamReader isr  = new InputStreamReader(fis, fstEncoding);
         BufferedReader    br   = new BufferedReader(isr);
         String            line = br.readLine();

         while (line != null) {
            if (line.trim().length() == 0) {
               line = br.readLine();
               continue;
            }
            FstLineParser parser    = new FstLineParser(line);
            int           tag       = Integer.parseInt(parser.getTag());
            int           technique = Integer.parseInt(parser.getTechnique());
            String        format    = parser.getFormat();
                try {
                    //System.out.println(tag + " " + technique + " " + format);
                    fst.addEntryAlways(tag, "", technique, format);
                } catch (DbException ex) {
                    Exceptions.printStackTrace(ex);
                }
            line = br.readLine();
         }
         br.close();
         isr.close();
         fis.close();
      } catch (NumberFormatException ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      } catch (FileNotFoundException ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      } catch (IOException ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      } 
      return fst;
   }
}
