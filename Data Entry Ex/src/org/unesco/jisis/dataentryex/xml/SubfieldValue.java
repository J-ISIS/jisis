/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex.xml;

/**
 *
 * @author jcd
 */
public class SubfieldValue {
    public String valueLabel_;
    public String valueDescription_;
    
    public SubfieldValue(String valueLabel, String valueDescription) {
        valueLabel_ = valueLabel;
        valueDescription_ = valueDescription;
    }
      
    public void setValueLabel(String valueLabel) {
        valueLabel_ = valueLabel;
    }
    
     public String getValueLabel() {
        return valueLabel_;
    }
      public void setValueDescription(String valueDescription) {
        valueDescription_ = valueDescription;
    }
    
     public String getValueDescription() {
        return valueDescription_;
    }
     
    @Override
     public SubfieldValue clone() {
        SubfieldValue subfieldValue = new SubfieldValue(valueLabel_, valueDescription_);
        return subfieldValue;
     }
    
    @Override
     public String toString() {
        StringBuilder sb = new StringBuilder();
       sb.append("   Value Label: ").append(valueLabel_).append("\n");
       sb.append("   Value Description: ").append(valueDescription_).append("\n");
       
       return sb.toString();
     }
    
}
