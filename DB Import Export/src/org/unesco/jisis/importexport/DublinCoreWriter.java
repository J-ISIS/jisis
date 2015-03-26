/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.importexport;

import java.util.Iterator;
import java.util.List;
import org.marc4j.MarcException;
import org.marc4j.MarcWriter;
import org.marc4j.converter.CharConverter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author jc_dauphin
 */
public class DublinCoreWriter implements MarcWriter {
   public static final String RDF_NS =
           "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

   public static final String DC_NS =
           "http://purl.org/dc/elements/1.1/";

   public static final Attributes atts = new AttributesImpl();

   private ContentHandler ch;

   private CharConverter converter = null;

   public DublinCoreWriter(ContentHandler ch) {
      this.ch = ch;
      try {
         ch.startDocument();
         ch.startPrefixMapping("rdf", RDF_NS);
         ch.startPrefixMapping("dc", DC_NS);
         ch.startElement(RDF_NS, "Description", "rdf:Description", atts);
      } catch (SAXException e) {
         throw new MarcException(e.getMessage(), e);
      }
   }

   public void close() {
      try {
         ch.endElement(RDF_NS, "Description", "rdf:Description");

         ch.endPrefixMapping("dc");
         ch.endPrefixMapping("rdf");
         ch.endDocument();

      } catch (SAXException e) {
         throw new MarcException(e.getMessage(), e);
      }
   }



   public void write(Record record) {
      DataField field;
      char[] data;

      try {
         field = (DataField) record.getVariableField("100");
         if (field != null) {
            data = getDataElements(field);
            ch.startElement(DC_NS, "creator", "dc:creator", atts);
            ch.characters(data, 0, data.length);
            ch.endElement(DC_NS, "creator", "dc:creator");
         }

         field = (DataField) record.getVariableField("245");
         if (field != null) {
            data = getDataElements(field, "abfghk");
            ch.startElement(DC_NS, "title", "dc:title", atts);
            ch.characters(data, 0, data.length);
            ch.endElement(DC_NS, "title", "dc:title");
         }
         String[] subjects = { "600", "610", "611", "630", "650"};
         List list = record.getVariableFields(subjects);
         Iterator i = list.iterator();
         while (i.hasNext()) {
            field = (DataField) i.next();
            data = getDataElements(field);
            ch.startElement(DC_NS, "subject", "dc:subject", atts);
            ch.characters(data, 0, data.length);
            ch.endElement(DC_NS, "subject", "dc:subject");
         }
      } catch (SAXException e) {
         throw new MarcException(e.getMessage(), e);
      }

   }

   private char[] getDataElements(DataField field) {
      return getDataElements(field, null);
   }

   private char[] getDataElements(DataField field, String codeString) {
      StringBuffer sb = new StringBuffer();

      char[] codes = "abcdefghijklmnopqrstuvwxyz".toCharArray();

      if (codeString != null)
         codes = codeString.toCharArray();

      for (int i=0; i<codes.length; i++) {
         Subfield sf = field.getSubfield(codes[i]);
         if (sf != null) {
            if (i>1)
               sb.append(" ");
            sb.append(sf.getData());
         }
      }
      if (converter == null) {
         return sb.toString().toCharArray();
      } else {
         String data = converter.convert(sb.toString());
         return data.toCharArray();
      }

   }

   public void setConverter(CharConverter converter) {
      this.converter = converter;

   }

   public CharConverter getConverter() {
      return converter;
   }

}
