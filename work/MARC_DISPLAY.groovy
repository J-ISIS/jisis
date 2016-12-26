package jisisgroovy

import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.corelib.record.IField;


def MARC_DISPLAY() {

   String encoding = "UTF-8";
   Record rec = (Record) binding.getVariable("record");
   return rec.toMarcDisplay(encoding);
  
   }
MARC_DISPLAY()