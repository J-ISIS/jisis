/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.importexport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import org.marc4j.MarcException;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.openide.util.Exceptions;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;

/**
 *
 * @author jc_dauphin
 */
public class ISO2709 {

    static private MarcFactory factory_ = org.marc4j.marc.MarcFactory.newInstance();
    static private CharsetDecoder decoder_ = null;

    public static void dumpRecord(byte[] record, String encoding) {

      Charset charset = Charset.forName(encoding);

      decoder_ = charset.newDecoder();
      StringBuffer sb = new StringBuffer();
      sb.append("Leader: |");
      for (int i = 0; i < 24; i++) {
         String s = String.format(" %x", record[i]);
         sb.append(s);
      }
      sb.append("|\n");

      try {
         ByteArrayInputStream bais = new ByteArrayInputStream(record);
         // Leader is 24 characters (or bytes)
         byte[] leader = new byte[24];

         int bytesRead = bais.read(leader);
         Leader ldr = null;
         try {
            ldr = parseLeader(leader);
         } catch (IOException e) {
            throw new MarcException("error parsing leader with data: " + new String(leader), e);
         }

         // otc.println(ldr.toString());
         // if MARC 21 then check encoding
//            switch (ldr.getCharCodingScheme()) {
//                case ' ':
//                    if (!override_) {
//                        encoding_ = "ISO8859_1";
//                    }
//
//                    break;
//
//                case 'a':
//                    if (!override_) {
//                        encoding_ = "UTF8";
//                    }
//            }

         //record_.setLeader(ldr);

         int directoryLength = ldr.getBaseAddressOfData() - (24 + 1);

         if ((directoryLength % 12) != 0) {
            throw new MarcException("invalid directory");
         }

         int size = directoryLength / 12;
         String[] tags = new String[size];
         int[] lengths = new int[size];
         // A tag shall contain 3 alphabetic or numeric ASCII characters
         byte[] tag = new byte[3];
         // The length of the field, we assume it is 4 bytes, better approach
         // would be to take it from the Entry Map in the leader
         byte[] length = new byte[4];
         // The offset from the base address of the start pf the field,
         // we assume it is 5 bytes, better approach would be to take it from
         // the Entry Map in the leader
         byte[] start = new byte[5];
         String tmp;

         // The directory shall begin in character (or byte) position 24 of
         // the flattened record
         // Read the directory entries,
         sb.append("Directory\n tag len offs\n ");
         for (int i = 0; i < size; i++) {

            /** Read the field tag */
            bytesRead = bais.read(tag);
            tags[i] = new String(tag);

            /**
             * Read the data field length
             *  Note: This length shall include the field terminator!
             */
            bytesRead = bais.read(length);
            lengths[i] = Integer.parseInt(new String(length));

            /** Read the data field offset  from base */
            bytesRead = bais.read(start);
            sb.append(String.format("| %5d | %5d | %5d\n",
                    Integer.parseInt(tags[i]),
                    lengths[i],
                    Integer.parseInt(new String(start))));
         }

         // The directory shall end with a field terminator
         int c = bais.read();
         sb.append("\nFT=" + String.format("%x", c) + "dec=" + c);

         // Read the variable-length fields
         for (int i = 0; i < size; i++) {
            byte[] fieldData = new byte[lengths[i] - 1];

            /** We don't filter the CR/LF for the data */
            bytesRead = bais.read(fieldData);

            ControlField field = factory_.newControlField();

            sb.append("\n<<" + tags[i] + ">>" + getDataAsString(fieldData));


            /** Read the field delimiter */
            int cc = bais.read();
            sb.append("\nFT=" + String.format("%x", cc) + "dec=" + cc);

         }

         /** Read the delimiter record */
         c = bais.read();
         sb.append("\nRT=" + String.format("%x", c) + "dec=" + c);

      } catch (IOException e) {
         throw new MarcException("an error occured reading input", e);
      }
      GuiGlobal.output(sb.toString());
   }

   private static Leader parseLeader(byte[] leaderData) throws IOException {
      InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(leaderData));
      Leader ldr = factory_.newLeader();
      char[] tmp = new char[5];

      isr.read(tmp);

      try {
         ldr.setRecordLength(Integer.parseInt(new String(tmp)));
      } catch (NumberFormatException e) {
         throw new MarcException("unable to parse record length", e);
      }

      ldr.setRecordStatus((char) isr.read());
      ldr.setTypeOfRecord((char) isr.read());
      tmp = new char[2];
      isr.read(tmp);
      ldr.setImplDefined1(tmp);
      ldr.setCharCodingScheme((char) isr.read());

      try {
         ldr.setIndicatorCount(Integer.parseInt(String.valueOf((char) isr.read())));
      } catch (NumberFormatException e) {
         throw new MarcException("unable to parse indicator count", e);
      }

      try {
         ldr.setSubfieldCodeLength(Integer.parseInt(String.valueOf((char) isr.read())));
      } catch (NumberFormatException e) {
         throw new MarcException("unable to parse subfield code length", e);
      }

      tmp = new char[5];
      isr.read(tmp);

      try {
         ldr.setBaseAddressOfData(Integer.parseInt(new String(tmp)));
      } catch (NumberFormatException e) {
         throw new MarcException("unable to parse base address of data", e);
      }

      tmp = new char[3];
      isr.read(tmp);
      ldr.setImplDefined2(tmp);
      tmp = new char[4];
      isr.read(tmp);
      ldr.setEntryMap(tmp);
      isr.close();

      return ldr;
   }

   private static String getDataAsString(byte[] bytes) {
      String dataElement = null;

      try {
         ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
         CharBuffer charBuffer = decoder_.decode(byteBuffer);

         dataElement = charBuffer.toString();
//          if (encoding_ != null) {
//             try {
//                dataElement = new String(bytes, encoding_);
//             } catch (UnsupportedEncodingException e) {
//                throw new MarcException("unsupported encoding", e);
//             }
//
//          } else {
//             dataElement = new String(bytes);
//          }
      } catch (CharacterCodingException ex) {
         Exceptions.printStackTrace(ex);
      }

      return dataElement;
   }
}
