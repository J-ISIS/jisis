/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.importexport;

//~--- non-JDK imports --------------------------------------------------------

import org.marc4j.Constants;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import org.openide.util.Exceptions;


//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;

/**
 *
 * @author jc_dauphin
 */
public class IsisIsoStreamReaderEx implements MarcReader {
   // private IsisIsoInputStream input_ = null;
   private Record           record_;
   private MarcFactory      factory_;
   private boolean          override_        = false;
   private boolean          hasNext_         = true;
   private String           encoding_        = "ISO8859_1";
   private final int        FIELD_DELIMITER  = 30;    /* 35 ICOMOS! */
   private final int        RECORD_DELIMITER = 29;
   private final int        SUBFIELD_PREFIX  = '^';
   private boolean          firstRecord_     = true;
   private int              recordDelimiter_ = RECORD_DELIMITER;
   private int              fieldDelimiter_  = FIELD_DELIMITER;
   private int              length_          = 0;
   
   private MappedByteBuffer byteBuffer_      = null;
   CharBuffer               charBuffer_      = null;
   CharsetDecoder           decoder_         = null;

   /**
    * Constructs an instance with the specified input stream.
    */
   public IsisIsoStreamReaderEx(File file) {
      this(file, null);

      firstRecord_ = true;
   }

   /**
    * Constructs an instance with the specified input stream and character
    * encoding.
    */
   public IsisIsoStreamReaderEx(File file, String encoding) {
      FileChannel channel = null;

      try {
         channel     = new FileInputStream(file).getChannel();
         length_     = (int) channel.size();
         byteBuffer_ = channel.map(FileChannel.MapMode.READ_ONLY, 0, length_);

      } catch (IOException ioe) {
         throw new ImportException(ioe);
      }


      firstRecord_ = true;
      factory_     = MarcFactory.newInstance();

      if (encoding != null) {
         this.encoding_ = encoding;
         override_      = true;
      }

      System.out.println(Charset.availableCharsets());

      Charset charset = Charset.forName(encoding_);

      decoder_ = charset.newDecoder();

      byte       accents[] = { (byte) 130, (byte) 138, (byte) 130, (byte) 138 };
      ByteBuffer testBytes = ByteBuffer.wrap(accents);
      CharBuffer testChars = null;

      try {
         testChars = decoder_.decode(testBytes);
      } catch (CharacterCodingException e) {
         System.out.println("Error Decoding");
      }

      System.out.println(testChars);
   }

   /**
    * Returns true if the iteration has more records, false otherwise.
    */
   public boolean hasNext() {
      
      return byteBuffer_.hasRemaining();
   }



   /**
    * Returns the next record in the iteration.
    *
    * @return Record - the record object
    */
   public Record next() {
      Leader ldr;
      int    bytesRead = 0;

      record_ = factory_.newRecord();

      try {
         byte[] buf = getRecord();

         if (buf == null) {
            throw new MarcException("EOF");
         }

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         // Leader is 24 characters (or bytes)
         byte[] leader = new byte[24];

         bytesRead = bais.read(leader);

         try {
            ldr = parseLeader(leader);
         } catch (IOException e) {
            throw new MarcException("error parsing leader with data: " + new String(leader), e);
         }

         // otc.println(ldr.toString());
         // if MARC 21 then check encoding
         switch (ldr.getCharCodingScheme()) {
         case ' ' :
            if (!override_) {
               encoding_ = "ISO8859_1";
            }

            break;

         case 'a' :
            if (!override_) {
               encoding_ = "UTF8";
            }
         }

         record_.setLeader(ldr);

         int directoryLength = ldr.getBaseAddressOfData() - (24 + 1);

         if ((directoryLength % 12) != 0) {
            throw new MarcException("invalid directory");
         }

         int      size    = directoryLength / 12;
         String[] tags    = new String[size];
         int[]    lengths = new int[size];
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
         for (int i = 0; i < size; i++) {
            /** Read the field tag */
            bytesRead = bais.read(tag);
            tags[i]   = new String(tag);

            /**
             * Read the data field length
             *  Note: This length shall include the field terminator!
             */
            bytesRead  = bais.read(length);
            lengths[i] = Integer.parseInt(new String(length));

            /** Read the data field offset  from base */
            bytesRead = bais.read(start);
         }

         // The directory shall end with a field terminator
         int c = bais.read();

         if ((c != FIELD_DELIMITER) && firstRecord_) {
            fieldDelimiter_ = c;

            GuiGlobal.output("Directory should end with a field delimiter! Found:"
                          + Integer.toString(c)
                          + " was expecting field terminator at end of directory:"
                          + Integer.toString(FIELD_DELIMITER));
         }

         // Read the variable-length fields
         for (int i = 0; i < size; i++) {
            byte[] fieldData = new byte[lengths[i] - 1];

            /** We don't filter the CR/LF for the data */
            bytesRead = bais.read(fieldData);

            ControlField field = factory_.newControlField();

            field.setTag(tags[i]);
            field.setData(getDataAsString(fieldData));
            record_.addVariableField(field);

            /** Read the field delimiter */
            int cc = bais.read();

            if ((cc != FIELD_DELIMITER) && firstRecord_) {
              GuiGlobal.output("Found:" + Integer.toString(cc)
                             + " was expecting field terminator at end of field:"
                             + Integer.toString(FIELD_DELIMITER));
            }
         }

         /** Read the delimiter record */
         c = bais.read();

         if ((c != RECORD_DELIMITER) && firstRecord_) {
            recordDelimiter_ = c;

            GuiGlobal.output("Found:" + Integer.toString(c)
                          + " was expecting record terminator at end of record:"
                          + Integer.toString(RECORD_DELIMITER));
         }

         firstRecord_ = false;
      } catch (IOException e) {
         throw new MarcException("an error occured reading input", e);
      }

      return record_;
   }

   private DataField parseDataField(String tag, byte[] field) throws IOException {
      ByteArrayInputStream bais      = new ByteArrayInputStream(field);
      char                 ind1      = (char) bais.read();
      char                 ind2      = (char) bais.read();
      DataField            dataField = factory_.newDataField();

      dataField.setTag(tag);
      dataField.setIndicator1(ind1);
      dataField.setIndicator2(ind2);

      int      code;
      int      size;
      int      readByte;
      byte[]   data;
      Subfield subfield;

      while (true) {
         readByte = bais.read();

         if (readByte < 0) {
            break;
         }

         switch (readByte) {
         case SUBFIELD_PREFIX :
            code = bais.read();

            if (code < 0) {
               throw new IOException("unexpected end of data field");
            }

            if (code == FIELD_DELIMITER) {
               break;
            }

            size = getSubfieldLength(bais);
            data = new byte[size];

            bais.read(data);

            subfield = factory_.newSubfield();

            subfield.setCode((char) code);
            subfield.setData(getDataAsString(data));
            dataField.addSubfield(subfield);

            break;

         case FIELD_DELIMITER :
            break;
         }
      }

      return dataField;
   }

   private int getSubfieldLength(ByteArrayInputStream bais) throws IOException {
      bais.mark(9999);

      int bytesRead = 0;

      while (true) {
         switch (bais.read()) {
         case Constants.US :
         case FIELD_DELIMITER :
            bais.reset();

            return bytesRead;

         case -1 :
            bais.reset();

            throw new IOException("subfield not terminated");

         default :
            bytesRead++;
         }
      }
   }

   private Leader parseLeader(byte[] leaderData) throws IOException {
      InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(leaderData));
      Leader            ldr = factory_.newLeader();
      char[]            tmp = new char[5];

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

   private String getDataAsString(byte[] bytes) {
      String dataElement = null;

      try {
         ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
         CharBuffer charBuffer = decoder_.decode(byteBuffer);

         dataElement = charBuffer.toString();
//       if (encoding_ != null) {
//          try {
//             dataElement = new String(bytes, encoding_);
//          } catch (UnsupportedEncodingException e) {
//             throw new MarcException("unsupported encoding", e);
//          }
//
//       } else {
//          dataElement = new String(bytes);
//       }
      } catch (CharacterCodingException ex) {
         Exceptions.printStackTrace(ex);
      }

      return dataElement;
   }


   byte[] getRecord() {
      byte[] buf = null;
      Leader ldr;
     

      /**
       * The leader is always at the beginning of a line and
       *  cannot contain cr/lf
       */
      // Leader is 24 characters (or bytes)
      byte[] leader = new byte[24];

      try {
         byteBuffer_.get(leader);
      } catch (BufferUnderflowException bue) {
         System.out.println("Abnormal EOF lenght_=" + length_ );

         throw new MarcException("EOF");
      }

      
      try {
         ldr = parseLeader(leader);
      } catch (IOException e) {
         throw new MarcException("error parsing leader with data: " + new String(leader), e);
      }

      int rlen = ldr.getRecordLength();

      buf = new byte[rlen];

      for (int j = 0; j < leader.length; j++) {
         buf[j] = leader[j];
      }

      int    offset = leader.length;
      byte[] b      = null;

      while (offset < rlen) {
         b = getLine();

         if (b == null) {
            break;
         }

         // System.out.println("rlen="+rlen+" offset="+offset+" b.length="+b.length);
         System.arraycopy(b, 0, buf, offset, b.length);

         offset += b.length;

         if ((buf[offset - 2] == fieldDelimiter_) && (buf[offset - 1] == recordDelimiter_)) {
            break;
         }
      }

      if (!firstRecord_) {
         /** Check that we are at the end of a record */
         if ((buf[offset - 2] == fieldDelimiter_) && (buf[offset - 1] == recordDelimiter_)) {
            // Do nothing, we are at end of record
         } else {
            // try to recover so that we are at beginning of record
            while ((b = getLine()) != null) {
               int len = b.length;

               if (len < 2) {
                  continue;
               }

               if ((b[len - 2] == fieldDelimiter_) && (b[len - 1] == recordDelimiter_)) {
                  break;
               }
            }
         }
      }

      return buf;
   }

   /**
    * Mind two's complement arithmetic, especially when doing IO!
    * In Java, bytes are signed, but the "bytes" you read off an InputStream
    * are unsigned.
    */
   private static final int LINE_LENGTH = 80;
   private static byte[] lbuf = new byte[LINE_LENGTH];
   
   private byte[] getLine() {
      
      int          c;
      int p=0;
      try {
         while ((c = getByte()) != -1 && (c != '\n')) {
           
            if (c != '\r') {
               if (p > LINE_LENGTH) {
                  throw new RuntimeException("Line exceeds 80!");
               }
               if (c == 31) c= 94;
               lbuf[p] = (byte) c;
               p++;
               
            } 
         }
        
      } catch (Exception ioe) {
         c = -1;
      }

      if ((c == -1) && (p == 0)) {
         return null;
      } else {
         byte[] data = new byte[p];
         System.arraycopy(lbuf, 0, data, 0, p);
         return data;
      }
   }

   private int getByte() {
      return byteBuffer_.get()& 0xFF;

   }
}
