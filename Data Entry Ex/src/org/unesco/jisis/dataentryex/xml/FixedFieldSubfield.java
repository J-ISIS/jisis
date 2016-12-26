/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex.xml;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jcd
 */
public class FixedFieldSubfield {
    public String positionsLabel_;
    public String positionsDescription_;
    public List<SubfieldValue> subfieldValues_;
    
   public FixedFieldSubfield(String positionsLabel, String positionsDescription, List<SubfieldValue> subfieldValues) {
      positionsLabel_ = positionsLabel;
      /**
       * get rid of white spaces (spaces, new lines etc.) from the beginning and end of the string. 
       */
      positionsDescription_ = positionsDescription.trim();
     
      
      if (subfieldValues.isEmpty()) {
         subfieldValues_ = new ArrayList<>();
      } else {
         // Create new List with same capacity as original (for efficiency).
         subfieldValues_ = new ArrayList<>(subfieldValues.size());

         for (SubfieldValue subfieldValue : subfieldValues) {
            subfieldValues_.add((SubfieldValue) subfieldValue.clone());
         }
      }
   }
    
     @Override
    public String toString() {
       StringBuilder sb = new StringBuilder();
       sb.append("Positions Label: ").append(positionsLabel_).append("\n");
       sb.append("Positions Description: ").append(positionsDescription_).append("\n");
       
       for (SubfieldValue subfieldValue : subfieldValues_) {
          sb.append(subfieldValue.toString());
       }
       
       return sb.toString();
    }
     @Override
     public FixedFieldSubfield clone() {
        FixedFieldSubfield fixedFieldSubfield = new FixedFieldSubfield(positionsLabel_, positionsDescription_, subfieldValues_);
        return fixedFieldSubfield;
     }
}
