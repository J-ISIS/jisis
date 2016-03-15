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
import org.marc4j.marc.Subfield;

import org.openide.util.Exceptions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.marc4j.ErrorHandler;
import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.converter.impl.Iso5426ToUnicode;
import org.marc4j.converter.impl.Iso6937ToUnicode;
import org.unesco.jisis.jisiscore.client.GuiGlobal;

/**
 *
 * @author jc_dauphin
 *
 * Basic Encoding Set (contained in rt.jar)
 *
 * Canonical Name |     Description
 * ASCII              | American Standard Code for Information Interchange
 * Cp1252             | Windows Latin-1
 * ISO8859_1      | ISO 8859-1, Latin alphabet No. 1
 * UnicodeBig     | Sixteen-bit Unicode Transformation Format, big-endian byte
 *                | order, with byte-order mark
 * UnicodeBigUnmarked | Sixteen-bit Unicode Transformation Format,
 *                    | big-endian byte order
 * UnicodeLittle  |     Sixteen-bit Unicode Transformation Format,
 *                |little-endian byte order, with byte-order mark
 * UnicodeLittleUnmarked |Sixteen-bit Unicode Transformation Format,
 *                       |little-endian byte order
 * UTF8               | Eight-bit Unicode Transformation Format
 * UTF-16             | Sixteen-bit Unicode Transformation Format, byte order
 *                | specified by a mandatory initial byte-order mark
 *
 */
public class NioIsoReader implements MarcReader {
    private static final int DEFAULT_LINE_LENGTH = 80;
    private byte[]    lineBuf_            = null;
    private int       lineLength_         = DEFAULT_LINE_LENGTH;

    /** True if file has lines with cr/lf */
    private boolean bReadLines = true;

    /** Default Field Terminator */
    private final static int FIELD_TERMINATOR = 30;    /* 35 ICOMOS! */

    /** Default Record Terminator */
    private final static int      RECORD_TERMINATOR = 29;
    private final static int      SUBFIELD_PREFIX   = '^';
    private FileChannel    channel_          = null;
    private CharBuffer     charBuffer_       = null;
    private CharsetDecoder decoder_          = null;

    /** Default encoding */
    private String                 encoding_         = "ISO8859_1";
    private int                    length_           = 0;
    private boolean                override_         = false;
    private boolean                hasNext_          = true;
    private boolean                firstRecord_      = true;
    private int                    recordTerminator_ = RECORD_TERMINATOR;
    private int                    fieldTerminator_  = FIELD_TERMINATOR;
    private MarcFactory            factory_;
    private NioBufferedReader      nbr_;
    private org.marc4j.marc.Record record_;
    
    private ErrorHandler errors;
    
     private CharConverter converterAnsel = null;

    private CharConverter converterUnimarc = null;
    
    // These are used to algorithmically determine what encoding scheme was 
    // used to encode the data in the Marc record
    private String conversionCheck1 = null;    
    private String conversionCheck2 = null;
    private String conversionCheck3 = null;

    private boolean permissive = false;
    /**
     * Constructs an instance with the specified input stream.
     */
    public NioIsoReader(File file) {
        this(file, null, DEFAULT_LINE_LENGTH);
        firstRecord_ = true;
    }

    /**
     * Constructs an instance with the specified input stream and character
     * encoding.
     */
    public NioIsoReader(File file, String encoding, int lineLength) {
        if (lineLength == 0) {
            bReadLines = false;
            lineLength = 2048 * 10;
        }
        lineLength_ = lineLength;
        lineBuf_    = new byte[lineLength_];
        try {
            channel_ = new FileInputStream(file).getChannel();
            length_  = (int) channel_.size();
            nbr_     = new NioBufferedReader(channel_);
        } catch (IOException ioe) {
            throw new ImportException(ioe);
        }
        firstRecord_ = true;
        factory_     = MarcFactory.newInstance();
        if (encoding != null) {
            encoding_ = encoding;
            override_ = true;
        }
        System.out.println(Charset.availableCharsets());

        if (encoding_.equals("MARC-8") || encoding_.equals("ISO5426")
               || encoding_.equals("ISO6937")) {
          // Do nothing
           decoder_ = null;
       } else {
          /**
           * Converting bytes to chars is called decoding A Charset is created
           * using the Charset.forName() method. String charsetName =
           * "ISO-8859-1"; Charset charset = Charset.forName( charsetName );
           * converting a Byte- Buffer to a CharBuffer using a Charset: Charset
           * charset = Charset.forName( charsetName ); CharsetDecoder decoder =
           * charset.newDecoder(); CharBuffer charBuffer = decoder.decode(
           * byteBuffer );
           */
          Charset charset = Charset.forName(encoding_);
          decoder_ = charset.newDecoder();
       }
    }

    /**
     * Returns true if the iteration has more records, false otherwise.
     */
    public boolean hasNext() {
        return nbr_.hasRemaining();
    }

    /**
     * Returns the next MARC4J record in the iteration.
     *
     * @return Record - the record object
     */

    byte[] bytesLeader = new byte[24];
   // A tag shall contain 3 alphabetic or numeric ASCII characters
   byte[] bytesTag = new byte[3];
   // The length of the field, we assume it is 4 bytes, better approach
   // would be to take it from the Entry Map in the leader
   byte[] bytesLength = new byte[4];
   // The offset from the base address of the start pf the field,
   // we assume it is 5 bytes, better approach would be to take it from
   // the Entry Map in the leader
   byte[] bytesStart = new byte[5];


    public org.marc4j.marc.Record next() {
       
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
            
            bytesRead = bais.read(bytesLeader);
            try {
                ldr = parseLeader(bytesLeader);
            } catch (IOException e) {
                throw new MarcException("Error parsing leader with data: " + new String(bytesLeader), e);
            }
           
            // if MARC 21 then check encoding
            switch (ldr.getCharCodingScheme()) {
            case ' ' :
                if (!override_) {
                    encoding_ = "ISO8859_1";
                }
                break;
            case 'a' :
                if (!override_) {
                    encoding_ = "UTF-8";
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
            
            // The directory shall begin in character (or byte) position 24 of
            // the flattened record
            // Read the directory entries,
            for (int i = 0; i < size; i++) {

                /** Read the field tag */
                bytesRead = bais.read(bytesTag);
                tags[i]   = new String(bytesTag);

                /**
                 * Read the data field length
                 *  Note: This length shall include the field terminator!
                 */
                bytesRead  = bais.read(bytesLength);
                lengths[i] = Integer.parseInt(new String(bytesLength));

                /** Read the data field offset  from base */
                bytesRead = bais.read(bytesStart);
            }
            // The directory shall end with a field terminator
            int c = bais.read();
            if (c != fieldTerminator_) {
                GuiGlobal.output("Directory should end with a field terminator! Found:" + Integer.toString(c)
                              + " was expecting field terminator at end of directory:"
                              + Integer.toString(fieldTerminator_));
            }
            // Read the variable-length fields
            for (int i = 0; i < size; i++) {
                byte[] fieldData = new byte[lengths[i] - 1];
                bytesRead = bais.read(fieldData);
                ControlField field = factory_.newControlField();
                field.setTag(tags[i]);
                field.setData(getDataAsString(fieldData));
                record_.addVariableField(field);

                /** Read the field terminator byte */
                int cc = bais.read();
                if (cc != fieldTerminator_) {
                    GuiGlobal.output("Found:" + Integer.toString(cc) + " was expecting field terminator at end of field:"
                                  + Integer.toString(fieldTerminator_));
                }
            }

            /** Read the record terminator byte */
            c = bais.read();
            if (c != recordTerminator_) {
                GuiGlobal.output("Found:" + Integer.toString(c) + " was expecting record terminator at end of record:"
                              + Integer.toString(recordTerminator_));
            }
            firstRecord_ = false;
        } catch (IOException e) {
            throw new MarcException("an error occured reading input", e);
        }
//        long lexit = Runtime.getRuntime().freeMemory();
//       System.out.println("next Exit - free memory="+lexit);
        return record_;
    }

    /**
     *
     * @param tag
     * @param field
     * @return
     * @throws java.io.IOException
     */
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
                if (code == FIELD_TERMINATOR) {
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
            case FIELD_TERMINATOR :
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
            case FIELD_TERMINATOR :
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
      if (encoding_.equals("MARC-8") || encoding_.equals("ISO5426")
              || encoding_.equals("ISO6937")) {
         if (encoding_.equals("MARC-8") || encoding_.equals("MARC8")) {
            dataElement = getMarc8Conversion(bytes);
         } else if (encoding_.equalsIgnoreCase("Unimarc") || encoding_.equals("ISO5426")) {
            dataElement = getUnimarcConversion(bytes);

         } else {
            Iso6937ToUnicode converter = new Iso6937ToUnicode();
            dataElement = converter.convert(bytes);
         }
      } else {
         try {
            // Make a ByteBuffer from the byte array
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            // converting the Byte-Buffer to a CharBuffer using the Charset
            // define by encoding
            CharBuffer charBuffer = decoder_.decode(byteBuffer);
            dataElement = charBuffer.toString();
         } catch (CharacterCodingException ex) {
            Exceptions.printStackTrace(ex);
         }
      }
      return dataElement;
   }
    
   private String getDataAsStringEx(byte[] bytes) {
      String dataElement = null;
      if (encoding_.equals("UTF-8") || encoding_.equals("UTF8")) {
         try {
            dataElement = new String(bytes, "UTF-8");
         } catch (UnsupportedEncodingException e) {
            throw new MarcException("unsupported encoding", e);
         }
      } else if (encoding_.equals("UTF8-Maybe")) {
         try {
            dataElement = new String(bytes, "UTF-8");
         } catch (UnsupportedEncodingException e) {
            throw new MarcException("unsupported encoding", e);
         }
      } else if (encoding_.equals("MARC-8") || encoding_.equals("MARC8")) {
         dataElement = getMarc8Conversion(bytes);
      } else if (encoding_.equalsIgnoreCase("Unimarc") || encoding_.equals("ISO5426")) {
         dataElement = getUnimarcConversion(bytes);
      } else if (encoding_.equals("MARC8-Maybe")) {
//         String dataElement1 = getMarc8Conversion(bytes);
//         String dataElement2 = getUnimarcConversion(bytes);
//         String dataElement3 = null;
//         try {
//            dataElement3 = new String(bytes, "ISO-8859-1");
//         } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//         }
//         if (dataElement1.equals(dataElement2) && dataElement1.equals(dataElement3)) {
//            dataElement = dataElement1;
//         } else {
//            conversionCheck1 = conversionCheck1 + "|>" + Normalizer.compose(dataElement1, false);
//            conversionCheck2 = conversionCheck2 + "|>" + dataElement2;
//            conversionCheck3 = conversionCheck3 + "|>" + dataElement3;
//            dataElement = dataElement1 + "%%@%%" + dataElement2 + "%%@%%" + dataElement3;
//         }
      } else if (encoding_.equals("MARC8-Broken")) {
         try {
            dataElement = new String(bytes, "ISO-8859-1");
         } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         String newdataElement = dataElement.replaceAll("&lt;", "<");
         newdataElement = newdataElement.replaceAll("&gt;", ">");
         newdataElement = newdataElement.replaceAll("&amp;", "&");
         newdataElement = newdataElement.replaceAll("&apos;", "'");
         newdataElement = newdataElement.replaceAll("&quot;", "\"");
         if (!newdataElement.equals(dataElement)) {
            dataElement = newdataElement;
            errors.addError(ErrorHandler.ERROR_TYPO, "Subfield contains escaped html character entities, un-escaping them. ");
         }
         String rep1 = "" + (char) 0x1b + "\\$1$1";
         String rep2 = "" + (char) 0x1b + "\\(B";
         newdataElement = dataElement.replaceAll("\\$1(.)", rep1);
         newdataElement = newdataElement.replaceAll("\\(B", rep2);
         if (!newdataElement.equals(dataElement)) {
            dataElement = newdataElement;
            errors.addError(ErrorHandler.MAJOR_ERROR, "Subfield seems to be missing MARC8 escape sequences, trying to restore them.");
         }
         try {
            dataElement = getMarc8Conversion(dataElement.getBytes("ISO-8859-1"));
         } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

      } else if (encoding_.equals("ISO-8859-1") || encoding_.equals("ISO8859_1")) {
         try {
            dataElement = new String(bytes, "ISO-8859-1");
         } catch (UnsupportedEncodingException e) {
            throw new MarcException("unsupported encoding", e);
         }
      } else {
         throw new MarcException("Unknown or unsupported Marc character encoding:" + encoding_);
      }
      if (errors != null && dataElement.matches("[^&]*&[a-z]*;.*")) {
         String newdataElement = dataElement.replaceAll("&lt;", "<");
         newdataElement = newdataElement.replaceAll("&gt;", ">");
         newdataElement = newdataElement.replaceAll("&amp;", "&");
         newdataElement = newdataElement.replaceAll("&apos;", "'");
         newdataElement = newdataElement.replaceAll("&quot;", "\"");
         if (!newdataElement.equals(dataElement)) {
            dataElement = newdataElement;
            errors.addError(ErrorHandler.ERROR_TYPO, "Subfield contains escaped html character entities, un-escaping them. ");
         }
      }
      return dataElement;
   }

   private boolean byteArrayContains(byte[] bytes, byte[] seq) {
      for (int i = 0; i < bytes.length - seq.length; i++) {
         if (bytes[i] == seq[0]) {
            for (int j = 0; j < seq.length; j++) {
               if (bytes[i + j] != seq[j]) {
                  break;
               }
               if (j == seq.length - 1) {
                  return (true);
               }
            }
         }
      }
      return (false);
   }
   static byte badEsc[] = {(byte) ('b'), (byte) ('-'), 0x1b, (byte) ('s')};
   static byte overbar[] = {(byte) (char) (0xaf)};

   private String getMarc8Conversion(byte[] bytes) {
      String dataElement = null;
      if (converterAnsel == null) {
         converterAnsel = new AnselToUnicode(errors);
      }
      if (permissive && (byteArrayContains(bytes, badEsc) || byteArrayContains(bytes, overbar))) {
         String newDataElement = null;
         try {
            dataElement = new String(bytes, "ISO-8859-1");
            newDataElement = dataElement.replaceAll("(\\e)b-\\es([psb])", "$1$2");
            if (!newDataElement.equals(dataElement)) {
               dataElement = newDataElement;
               errors.addError(ErrorHandler.MINOR_ERROR, "Subfield contains odd pattern of subscript or superscript escapes. ");
            }
            newDataElement = dataElement.replace((char) 0xaf, (char) 0xe5);
            if (!newDataElement.equals(dataElement)) {
               dataElement = newDataElement;
               errors.addError(ErrorHandler.ERROR_TYPO, "Subfield contains 0xaf overbar character, changing it to proper MARC8 representation ");
            }
            dataElement = converterAnsel.convert(dataElement);
         } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      } else {
         dataElement = converterAnsel.convert(bytes);
      }
      if (permissive && dataElement.matches("[^&]*&#x[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f];.*")) {
         Pattern pattern = Pattern.compile("&#x([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]);");
         Matcher matcher = pattern.matcher(dataElement);
         StringBuffer newElement = new StringBuffer();
         int prevEnd = 0;
         while (matcher.find()) {
            newElement.append(dataElement.substring(prevEnd, matcher.start()));
            newElement.append(getChar(matcher.group(1)));
            prevEnd = matcher.end();
         }
         newElement.append(dataElement.substring(prevEnd));
         dataElement = newElement.toString();
      }
      return (dataElement);
   }

   private String getUnimarcConversion(byte[] bytes) {
      if (converterUnimarc == null) {
         converterUnimarc = new Iso5426ToUnicode();
      }
      String dataElement = converterUnimarc.convert(bytes);
      dataElement = dataElement.replaceAll("\u0088", "");
      dataElement = dataElement.replaceAll("\u0089", "");
//        for ( int i = 0 ; i < bytes.length; i++)
//        {
//            if (bytes[i] == -120 || bytes[i] == -119)
//            {
//                char tmp = (char)bytes[i]; 
//                char temp2 = dataElement.charAt(0);
//                char temp3 = dataElement.charAt(4);
//                int tmpi = (int)tmp;
//                int tmp2 = (int)temp2;
//                int tmp3 = (int)temp3;
//                i = i;
//
//            }
//        }
      if (dataElement.matches("[^<]*<U[+][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]>.*")) {
         Pattern pattern = Pattern.compile("<U[+]([0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f])>");
         Matcher matcher = pattern.matcher(dataElement);
         StringBuffer newElement = new StringBuffer();
         int prevEnd = 0;
         while (matcher.find()) {
            newElement.append(dataElement.substring(prevEnd, matcher.start()));
            newElement.append(getChar(matcher.group(1)));
            prevEnd = matcher.end();
         }
         newElement.append(dataElement.substring(prevEnd));
         dataElement = newElement.toString();
      }
      return (dataElement);

   }
   private String getChar(String charCodePoint) {
      int charNum = Integer.parseInt(charCodePoint, 16);
      String result = "" + ((char) charNum);
      return (result);
   }

    /**
     *
     * @return
     */
    byte[] getRecord() {
        byte[] buf = null;
        Leader ldr;

        /**
         * The leader is always at the beginning of a line and
         * cannot contain cr/lf
         * Leader is 24 Ascii characters (or bytes)
         */
        try {
            if (!nbr_.getBytesLeader(bytesLeader)) {
               throw new MarcException("EOF");
            }
            ldr = parseLeader(bytesLeader);
        } catch (BufferUnderflowException bue) {
            System.out.println("Abnormal EOF lenght_=" + length_);
            throw new MarcException("EOF");
        } catch (IOException e) {
            throw new MarcException("error parsing leader with data: " + new String(bytesLeader), e);
        }
        // Full Record Length, include 24 bytes for leader and 1 byte for record
        // terminator
        int rlen = ldr.getRecordLength();
        // Allocate a byte buffer for the full MARC record
        buf = new byte[rlen];
        // Copy the leader
        System.arraycopy(bytesLeader, 0, buf, 0, bytesLeader.length);
        
        int    offset = bytesLeader.length;
        byte[] b      = null;
        while (offset < rlen) {
            // Bytes to read
            int nbytes = (offset == bytesLeader.length)
                         ? Math.min(lineLength_ - bytesLeader.length, rlen - offset)
                         : Math.min(lineLength_, rlen - offset);
            //assert nbytes > 0;
            b = nbr_.getLine(nbytes);
            if (b == null) {
                break;
            }
            // System.out.println("rlen="+rlen+" offset="+offset+" b.length="+b.length);
            System.arraycopy(b, 0, buf, offset, b.length);
            offset += b.length;
            // Don't remember why I introduced this code!
//            if ((buf[offset - 2] == fieldTerminator_) && (buf[offset - 1] == recordTerminator_)) {
//                break;
//            }
        }
        b = null;
        
        if (firstRecord_) {
            // Adjust the RT and FT
            if ((buf[offset - 1] != recordTerminator_)) {
                GuiGlobal.output("Was expecting as RT: " + recordTerminator_);
                GuiGlobal.output("Changed to : " + (int) buf[offset - 1]);
                recordTerminator_ = buf[offset - 1];
            }
            if ((buf[offset - 2] != fieldTerminator_)) {
                GuiGlobal.output("Was expecting as FT: " + fieldTerminator_);
                GuiGlobal.output("Changed to : " + (int) buf[offset - 2]);
                fieldTerminator_ = buf[offset - 2];
            }
        } else {

            /** Check that we are at the end of a record */
            if ((buf[offset - 2] == fieldTerminator_) && (buf[offset - 1] == recordTerminator_)) {
                // Do nothing, we are at end of record
            } else {
                GuiGlobal.output("Error** Was expecting as FT: " + fieldTerminator_);
                GuiGlobal.output("Found : " + (int) buf[offset - 2]);
                GuiGlobal.output("Error** Was expecting as RT: " + recordTerminator_);
                GuiGlobal.output("Found : " + (int) buf[offset - 1]);
                // try to recover so that we are at beginning of record
                while ((b = nbr_.getLine(lineLength_)) != null) {
                    int len = b.length;
                    if (len < 2) {
                        continue;
                    }
                    if ((b[len - 2] == fieldTerminator_) && (b[len - 1] == recordTerminator_)) {
                        break;
                    }
                }
            }
        }
        return buf;
    }

    class NioBufferedReader {
        private static final int BUFFER_SIZE = 2048 * 10;
        private final ByteBuffer       buff_       = ByteBuffer.allocateDirect(BUFFER_SIZE);

        /**
         * Mind two's complement arithmetic, especially when doing IO!
         * In Java, bytes are signed, but the "bytes" you read off an InputStream
         * are unsigned.
         */
        private FileChannel channel_;

        NioBufferedReader(FileChannel channel) {
            channel_ = channel;
            /* Be sure to fill buffer when starting */
            buff_.position(BUFFER_SIZE);
        }

        void getBytes(byte[] buf) {
            for (int i = 0; i < buf.length; i++) {
                buf[i] = getByteFromBuf();
            }
        }
         boolean getBytesLeader(byte[] buf) {
            // Skip leading blanklines
            int c;

            while ((c = getByteFromBuf()) == '\n' || c == '\r')  {
               // Skip blank lines
            }
            if (c == -1) {
               return false;
            }
            buf[0] = (byte) c;
            for (int i = 1; i < buf.length; i++) {
                buf[i] = getByteFromBuf();
            }
            return true;
        }

        byte getByteFromBuf() {
            byte b = 0;
            if (!buff_.hasRemaining()) {
                if (fillBuffer() == -1) {
                    return -1;
                }
            }
            b = buff_.get();
            return b;
        }

        private int fillBuffer() {
            int nRead = -1;
            try {
                /* Reset read position in buffer to 0 */
                buff_.rewind();
                /* Read bytes from channel */
                nRead = channel_.read(buff_);
                if ((nRead == -1) || (nRead == 0)) {
                    return -1;
                }
                buff_.limit(nRead);
                buff_.rewind();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return nRead;
        }
 private byte[] getLine(int nbytes) {
        int c;
        int p = 0;
        try {
             if (bReadLines) {
               while ((c = getByte()) != -1) {
                    if ((c == '\n') && (p == nbytes)) {
                        // end of line lf, line is read 
                        // we don't retain '\n' in buffer. The line is read, stop reading
                        break;
                    }
                    if ((c == '\r') && (p == nbytes)) {
                        // end of line cr, swallow the character and continue reading
                        continue;
                    }
                    if (p > lineBuf_.length) {
                        throw new RuntimeException("Buffer Line Length exceeded :" + lineBuf_.length);
                    }
                    lineBuf_[p] = (byte) c;
                    p++;
                    
                }
                
            } else {
                 // Read nbytes characters including '\n' and '\r' 
                 while ((c = getByte()) != -1) {

                     if (p > lineBuf_.length) {
                         throw new RuntimeException("Buffer Line Length exceeded :" + lineBuf_.length);
                     }
                     lineBuf_[p] = (byte) c;
                     p++;
                     if ((p == nbytes)) {
                         // We have read exactly nbytes character,
                         break;
                     }
                 }
            }
           
        } catch (Exception ioe) {
            c = -1;
        }
        if ((c == -1) && (p == 0)) {
            return null;
        } else {
            byte[] data = new byte[p];
            // System.out.println("p=" + p);
            System.arraycopy(lineBuf_, 0, data, 0, p);
            return data;
        }
    }
       

        private int getByte() {
            return getByteFromBuf() & 0xFF;
        }

        boolean hasRemaining() {
            if (buff_.hasRemaining()) {
                return true;
            }
            if (fillBuffer() == -1) {
                return false;
            }
            if (buff_.hasRemaining()) {
                return true;
            }
            return false;
        }
    }
}
