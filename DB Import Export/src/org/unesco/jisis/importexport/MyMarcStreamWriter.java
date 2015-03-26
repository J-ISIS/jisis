// $Id: MarcStreamWriter.java,v 1.4 2006/08/04 12:24:05 bpeters Exp $
/**
 * Copyright (C) 2004 Bas Peters
 *
 * This file is part of MARC4J
 *
 * MARC4J is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * MARC4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with MARC4J; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.unesco.jisis.importexport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import org.marc4j.Constants;
import org.marc4j.MarcException;
import org.marc4j.MarcWriter;
import org.marc4j.converter.CharConverter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.openide.util.Exceptions;


/**
 * Class for writing MARC record objects in ISO 2709 format.
 * 
 * <p>
 * The following example reads a file with MARCXML records and outputs the
 * record set in ISO 2709 format:
 * </p>
 * 
 * <pre>
 * InputStream input = new FileInputStream(&quot;marcxml.xml&quot;);
 * MarcXmlReader reader = new MarcXmlReader(input);
 * MarcWriter writer = new MarcStreamWriter(System.out);
 * while (reader.hasNext()) {
 *     Record record = reader.next();
 *     writer.write(record);
 * }
 * writer.close();
 * </pre>
 * 
 * <p>
 * To convert characters like for example from UCS/Unicode to MARC-8 register
 * a {@link org.marc4j.converter.CharConverter}&nbsp;implementation:
 * </p>
 * 
 * <pre>
 * InputStream input = new FileInputStream(&quot;marcxml.xml&quot;);
 * MarcXmlReader reader = new MarcXmlReader(input);
 * MarcWriter writer = new MarcStreamWriter(System.out);
 * writer.setConverter(new UnicodeToAnsel());
 * while (reader.hasNext()) {
 *     Record record = reader.next();
 *     writer.write(record);
 * }
 * writer.close();
 * </pre>
 * 
 * @author Bas Peters
 * @version $Revision: 1.4 $
 */
public class MyMarcStreamWriter implements MarcWriter {

   private OutputStream out = null;
   private String encoding = "ISO8859_1";
   private CharConverter converter = null;
   private static DecimalFormat format4 = new DecimalFormat("0000");
   private static DecimalFormat format5 = new DecimalFormat("00000");

   /** RECORD TERMINATOR */
    private int RT = Constants.RT;

    /** FIELD TERMINATOR */
    private int FT = Constants.FT;

    /** SUBFIELD DELIMITER */
    private int US = Constants.US;

     /** The line separator. */
     private static String lineSeparator;

   /**
    * Constructs an instance and creates a <code>Writer</code> object with
    * the specified output stream.
    */
   public MyMarcStreamWriter(OutputStream out) {
      this.out = out;
   }

   /**
    * Constructs an instance and creates a <code>Writer</code> object with
    * the specified output stream and character encoding.
    */
   public MyMarcStreamWriter(OutputStream out, String encoding) {
      this.encoding = encoding;
      this.out = out;
   }

   public void setFieldTerminator(int fieldTerminator) {
      FT = fieldTerminator;
   }
   public void setSubfieldDelimiter(int subfieldDelimiter) {
      US = subfieldDelimiter;
   }
   public void setRecordTerminator(int recordTerminator) {
     RT = recordTerminator;
   }

   /**
    * Returns the character converter.
    *
    * @return CharConverter the character converter
    */
   public CharConverter getConverter() {
      return converter;
   }

   /**
    * Sets the character converter.
    *
    * @param converter
    *            the character converter
    */
   public void setConverter(CharConverter converter) {
      this.converter = converter;
      if (encoding.equals("MARC-8") || encoding.equals("ISO5426")
                 || encoding.equals("ISO6937")) {
            encoding = "ISO8859_1";
         }
   }

   /**
    * Writes a <code>Record</code> object to the writer.
    *
    * @param record -
    *            the <code>Record</code> object
    */
   public void write(Record record) {
      try {
         byte[] bytesRecord = getRecordStreamBytes(record);
         out.write(bytesRecord);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      }
   }

    private byte[] getRecordStreamBytes(Record record) {
      int previous = 0;

      ByteArrayOutputStream result = null;
      try {

         result = new ByteArrayOutputStream();
         ByteArrayOutputStream data = new ByteArrayOutputStream();
         ByteArrayOutputStream dir = new ByteArrayOutputStream();

         // control fields
         List fields = record.getControlFields();
         Iterator i = fields.iterator();
         while (i.hasNext()) {
            ControlField cf = (ControlField) i.next();

            data.write(getDataElement(cf.getData()));
            data.write(FT);
            dir.write(getEntry(cf.getTag(), data.size() - previous,
                    previous));
            previous = data.size();
         }

         /*-------------------------------------------------------------------
          * The following loop was modified because when creating ControlFied
          * objects, the control field objects with a tag >10 are considered
          * as DataField objects and are stored in the fields array
          * ------------------------------------------------------------------
          */
         // data fields
         fields = record.getDataFields();
         i = fields.iterator();
         while (i.hasNext()) {
            Object obj = i.next();
            if (obj instanceof DataField) {

               DataField df = (DataField) obj;
               data.write(df.getIndicator1());
               data.write(df.getIndicator2());
               List subfields = df.getSubfields();
               Iterator si = subfields.iterator();
               while (si.hasNext()) {
                  Subfield sf = (Subfield) si.next();
                  data.write(US);
                  data.write(sf.getCode());
                  data.write(getDataElement(sf.getData()));
               }
               data.write(FT);
               dir.write(getEntry(df.getTag(), data.size() - previous,
                       previous));
               previous = data.size();
            } else {
               ControlField cf = (ControlField) obj;

               data.write(getDataElement(cf.getData()));
               data.write(FT);
               dir.write(getEntry(cf.getTag(), data.size() - previous,
                       previous));
               previous = data.size();
            }
         }
         dir.write(FT);

         // base address of data and logical record length
         Leader ldr = record.getLeader();

         ldr.setBaseAddressOfData(24 + dir.size());
         ldr.setRecordLength(ldr.getBaseAddressOfData() + data.size() + 1);

         // write record to output stream
         dir.close();
         data.close();
         // Write the leader
         writeLeaderAsStreamBytes(result,ldr);
         // Write the directory
         result.write(dir.toByteArray());
         // Write the data
         result.write(data.toByteArray());
         // write Record Terminator
         result.write(RT);
         result.close();

      } catch (IOException e) {
         throw new MarcException("IO Error occured while writing record", e);
      }
      //dumpRecord(result.toByteArray());
      return result.toByteArray();
   }
    
    
    private void writeLeaderAsStreamBytes(ByteArrayOutputStream result, Leader ldr) throws IOException {
      result.write(format5.format(ldr.getRecordLength()).getBytes(encoding));
      result.write(ldr.getRecordStatus());
      result.write(ldr.getTypeOfRecord());
      result.write(new String(ldr.getImplDefined1()).getBytes(encoding));
      result.write(ldr.getCharCodingScheme());
      result.write(Integer.toString(ldr.getIndicatorCount()).getBytes(encoding));
      result.write(Integer.toString(ldr.getSubfieldCodeLength()).getBytes(
              encoding));
      result.write(format5.format(ldr.getBaseAddressOfData()).getBytes(
              encoding));
      result.write(new String(ldr.getImplDefined2()).getBytes(encoding));
      result.write(new String(ldr.getEntryMap()).getBytes(encoding));
   }
   public void writeAsLines(Record record, int outputLineLength) {

      try {

         byte[] buf = getRecordStreamBytes(record);
         int len = buf.length;

         int offset = 0;
         while (offset < len) {
            int n = outputLineLength;
            if (offset+n > len)
               n = len - offset;

            out.write(buf, offset, n);
            out.write(getLineSeparator().getBytes());
            offset += n;

         }
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      }
   }
  public static String getLineSeparator() {
            if (lineSeparator == null) {
                try {
                    lineSeparator = System.getProperty("line.separator", "\n");
                }
                catch (SecurityException se) {
                    lineSeparator = "\n";
                }
            }
            return lineSeparator;
        }

   /**
    * Closes the writer.
    */
   public void close() {
      try {
         out.close();
      } catch (IOException e) {
         throw new MarcException("IO Error occured on close", e);
      }
   }

   private byte[] getDataElement(String data) throws IOException {
      if (converter != null) {
         return converter.convert(data).getBytes(encoding);
      }
      return data.getBytes(encoding);
   }

   private byte[] getEntry(String tag, int length, int start)
           throws IOException {
      return (tag + format4.format(length) + format5.format(start)).getBytes(encoding);
   }
}