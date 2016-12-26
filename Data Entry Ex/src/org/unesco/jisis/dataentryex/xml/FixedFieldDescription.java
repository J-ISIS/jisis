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
public class FixedFieldDescription {
    
    protected  String tag_;
    protected List<FixedFieldSubfield> fixedFieldSubfields_;
    
    public FixedFieldDescription(String tag, List<FixedFieldSubfield> fixedFieldSubfields) {
        tag_ = tag;
        if (fixedFieldSubfields.isEmpty()) {
            fixedFieldSubfields_ = new ArrayList<>();
        } else {
            // Create new List with same capacity as original (for efficiency).
            fixedFieldSubfields_ = new ArrayList<>(fixedFieldSubfields.size());

            for (FixedFieldSubfield fixedFieldSubfield : fixedFieldSubfields) {
                fixedFieldSubfields_.add((FixedFieldSubfield) fixedFieldSubfield.clone());
            }
        }

    }
    
    public String getTag() {
        return tag_;
    }
    
    public List<FixedFieldSubfield> getFixedFieldSubfields() {
        return fixedFieldSubfields_;
    }
    
    @Override
    public String toString() {
       StringBuilder sb = new StringBuilder();
       sb.append("Fixed Field: ").append(tag_).append("\n");
       
       for (FixedFieldSubfield fixedFieldSubfield : fixedFieldSubfields_) {
          sb.append(fixedFieldSubfield.toString());
       }
       
       return sb.toString();
    }
}
