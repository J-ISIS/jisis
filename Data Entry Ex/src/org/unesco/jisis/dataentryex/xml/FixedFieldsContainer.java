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
public class FixedFieldsContainer {
     private final List<FixedFieldDescription> fixedFieldsList_;
   
    /**
     * Constructor
     */
    public  FixedFieldsContainer() {
         fixedFieldsList_ = new ArrayList<>();
    }

    /**
   

   

    /**
     * Removes all of the Field Descriptions
     */
    public void clear() {
        fixedFieldsList_.clear();

    }

    /**
     * Get the number of fields in the fixedFieldsList_
     * @return the number of fields in the authority container
     */
    public int getFieldsCount() {
        return fixedFieldsList_.size();
    }

    /**
     * Get a Field Description object  by index
     * @param index index in the table of the FieldDecriptionEx object
     * @return the FieldDescriptionEx
     */
    public FixedFieldDescription getFieldByIndex(int index) {
        if ((index < 0) || (index >= fixedFieldsList_.size())) {
            throw new IndexOutOfBoundsException("Marc21Authority:getFieldByIndex invalid index: "+index);

        }
        return fixedFieldsList_.get(index);
    }

    /**
     * Get a FieldDescritionEx object by tag
     *
     * @param tag tag of the FieldDescription to get
     * @return a FieldDescription object with the tag
     */
    public FixedFieldDescription getFieldByTag(int tag) {
       int i = findField(tag);
       if (i >=0)
          return fixedFieldsList_.get(i);

      
        return null;
    }

    /**
     * Find the index of a FieldDefinition object with the tag in the FDT table
     * @param tag the tag of the FieldDefinition object to retrieve
     * @return the index of FieldDefinition object with the tag or -1 if not
     * found
     */
    public int findField(int tag) {
        for (int i = 0; i < fixedFieldsList_.size(); i++) {
            FixedFieldDescription field = fixedFieldsList_.get(i);
            if (field.getTag().equals(tag+"")) {
                return i;
            }
        }
        return -1;
    }

    

 

   @Override
    public String toString() {
       StringBuilder sb = new StringBuilder();
       for (FixedFieldDescription fd : fixedFieldsList_) {
          sb.append(fd.toString()).append("\n");
       }
       return sb.toString();
    }

   void setField(FixedFieldDescription fldDescription) {
      fixedFieldsList_.add(fldDescription);
      
   }
}
