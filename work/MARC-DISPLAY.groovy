package jisisgroovy

import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.IField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

def MARC_DISPLAY() {

   IRecord rec = binding.getVariable("record");
   org.marc4j.marc.MarcFactory factory = org.marc4j.marc.MarcFactory.newInstance();
   def leader    = '000000000000000000004500';
   // Prepare the leader
   def nfields = rec.getFieldCount()
   for (int i = 0; i < nFields; i++) {
       int itag = iRec.getFieldByIndex(i).getTag();
       if (itag >= 3000 && itag <= 3023) {
          String fieldVal = iRec.getFieldByIndex(i).getStringFieldValue();
          if (fieldVal != null && fieldVal.length() > 0) {
             leader = StringUtils.replaceCharAt(leader,
                                            itag - 3000, fieldVal.charAt(0));
          }
       }
    }
    if (encoding.equalsIgnoreCase("UTF-8")) {
       leader = StringUtils.replaceCharAt(leader, 9, 'a');
    }
    org.marc4j.marc.Record record = factory.newRecord(leader);
    def subfieldDelimiter = 31;
    
    for (int i = 0; i < nFields; i++) {
        IField field = iRec.getFieldByIndex(i);
        if (field.getTag() >= 1000) {
           // Skip fields with more than 3 digits
          continue;
        }
        convertIsis2IsoField(record, factory, field, subfieldDelimiter);
    }
       
    
  return record.toString();    
}
def  convertIsis2IsoField(org.marc4j.marc.Record record, MarcFactory factory, IField field,
                                             int subfieldDelimiter) {
      
      String tag = Integer.toString(field.getTag());
      if (tag.length() == 1) {
         tag = "00" + tag;
      } else if (tag.length() == 2) {
         tag = "0" + tag;
      }
      //Add a field for each occurrence
      int nOccurrences = field.getOccurrenceCount();
      for (int j = 0; j < nOccurrences; j++) {
         String fieldVal = (String) field.getOccurrenceValue(j);
         if (subfieldDelimiter != -1) {
            fieldVal = StringUtils.replaceChar(fieldVal, 94,subfieldDelimiter);
         }
         record.addVariableField(factory.newControlField(tag, fieldVal));
      }
   }
MARC_DISPLAY()