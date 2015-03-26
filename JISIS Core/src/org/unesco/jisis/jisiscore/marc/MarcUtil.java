/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisiscore.marc;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import org.openide.util.Exceptions;



import java.util.Iterator;
import java.util.List;
import org.unesco.jisis.corelib.common.Global;

import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;

/**
 *
 * @author jc_dauphin
 */
public class MarcUtil {
   static final public CodedField ldr05_record_status[] = {
      new CodedField('a', "increase in encoding level (not from CIP)"),
      new CodedField('c', "corrected or revised record"),
      new CodedField('d', "deleted record"),
      new CodedField('n', "new record"),
      new CodedField('p', "increase in encoding level from CIP")
   };
   static final public CodedField ldr06_record_type[] = {
      new CodedField('a', "language material (e.g., a book or print serial)"),
      new CodedField('c', "music printed (e.g., scrores, songbooks)"),
      new CodedField('d', "music manuscript"),
      new CodedField('e', "map, printed (e.g., travel maps, atlases)"),
      new CodedField('f', "map, manuscript"),
      new CodedField('g', "projected medium (e.g., videos, filmstrips, slides)"),
      new CodedField('i', "sound recording, nonmusical (e.g., a book on tape)"),
      new CodedField('j', "sound recording, musical (e.g., opera onCD)"),
      new CodedField('k', "two-dimensional nonprojected graphics (e.g. photos)"),
      new CodedField('m', "electronic resource (e.g, software)"),
      new CodedField('o', "kit"),
      new CodedField('p', "mixed material (archival)"),
      new CodedField('r', "3-D artifact or natural object (e.g., sculture, equipment)"),
      new CodedField('t', "manuscript language material (e.g., theses, letters)")

   };
    static final public CodedField ldr07_BiblioLevelrecord_status[] = {
      new CodedField('a', "increase in encoding level (not from CIP)"),
      new CodedField('c', "corrected or revised record"),
      new CodedField('d', "deleted record"),
      new CodedField('n', "new record"),
      new CodedField('p', "increase in encoding level from CIP")
   };
     static final public CodedField ldr08_Type_Of_Control[] = {
      new CodedField('a', "increase in encoding level (not from CIP)"),
      new CodedField('c', "corrected or revised record"),
      new CodedField('d', "deleted record"),
      new CodedField('n', "new record"),
      new CodedField('p', "increase in encoding level from CIP")
   };
       static final public CodedField ldr09_Coding_Scheme[] = {
      new CodedField('a', "increase in encoding level (not from CIP)"),
      new CodedField('c', "corrected or revised record"),
      new CodedField('d', "deleted record"),
      new CodedField('n', "new record"),
      new CodedField('p', "increase in encoding level from CIP")
   };
        static final public CodedField ldr17_Encoding_Level[] = {
      new CodedField('a', "increase in encoding level (not from CIP)"),
      new CodedField('c', "corrected or revised record"),
      new CodedField('d', "deleted record"),
      new CodedField('n', "new record"),
      new CodedField('p', "increase in encoding level from CIP")
   };
   static final public CodedField ldr18_Cataloging_Form[] = {
      new CodedField(' ', "non-ISBD (old or no cataloging rules followed"),
      new CodedField('a', "AACR2 (current cataloging rules followed"),
      new CodedField('i', "ISBD (ISBD punctuation, but not AACR2"),
      new CodedField('u', "unknown (e.g., records converted from non-marc")
   };
   private static MarcFactory factory = MarcFactory.newInstance();

   public static org.marc4j.marc.Record convertRecordToMarc(IRecord record) {
      String                 leader     = "000000000000000000004500";
      org.marc4j.marc.Record marcRecord = factory.newRecord(leader);
      try {
         // Add the record fields occurrence by occurrence
         int nFields = record.getFieldCount();
         for (int i = 0; i < nFields; i++) {
            IField f            = record.getFieldByIndex(i);
            int    nOccurrences = f.getOccurrenceCount();
            String tag          = Integer.toString(f.getTag());
            if (tag.length() == 1) {
               tag = "00" + tag;
            } else if (tag.length() == 2) {
               tag = "0" + tag;
            }
            for (int j = 0; j < nOccurrences; j++) {
               String fieldVal = (String) f.getOccurrenceValue(j);
               marcRecord.addVariableField(factory.newControlField(tag, fieldVal));
            }
         }
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
      return marcRecord;
   }

   public static IRecord convertMarcToRecord(org.marc4j.marc.Record marcRecord,
           final int subfieldDelimiter) {
      Leader   leader        = marcRecord.getLeader();
      IRecord  record        = org.unesco.jisis.corelib.record.Record.createRecord();
      String   charToReplace = new Character((char) subfieldDelimiter).toString();
      String   replacement   = Global.SUBFIELD_SEPARATOR;
      Iterator it            = marcRecord.getVariableFields().iterator();
      char     indic1;
      char     indic2;
      String   data = null;
      try {
         while (it.hasNext()) {
            VariableField vf   = (VariableField) it.next();
            int           iTag = Integer.parseInt(vf.getTag());

            /** getField creates the field if not found */
            IField field = record.getField(iTag);

            /**
             * All fields are supposed to have occurrences, a non repetetive
             * field will have 1 occurrence
             */
            if (vf instanceof DataField) {
               DataField df = (DataField) vf;
               indic1 = df.getIndicator1();
               indic2 = df.getIndicator2();
               List         subfields = df.getSubfields();
               Iterator     si        = subfields.iterator();
               StringBuffer sb        = new StringBuffer();
               while (si.hasNext()) {
                  Subfield sf = (Subfield) si.next();
                  sb.append(sf.getData());
                  sb.append(Global.SUBFIELD_SEPARATOR);
                  sb.append(sf.getCode());
                  sb.append(sf.getData());
               }
               data = sb.toString();
            } else {
               ControlField cf = (ControlField) vf;
               data = cf.getData();
            }
            int nOccurrences = field.getOccurrenceCount();
            field.setOccurrence(nOccurrences, data);
         }
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
      return record;
   }
}
