/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.datadefinition.fdt;

import java.util.Comparator;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;


/**
 *
 * @author jc_dauphin
 */
public class FdtEntryComparator implements Comparator  { 

   public int compare ( Object o1, Object o2 )   {  
     FieldDefinition fd1 =  (FieldDefinition) o1; 
     FieldDefinition fd2 =  (FieldDefinition) o2; 
     if (fd1.getTag() > fd2.getTag()) {
        return 1;
     } else if (fd1.getTag() < fd2.getTag()) {
        return -1;
     } 
     return 0; 
    
    }                                      
   public boolean equals ( Object o )   { 
     return compare ( this, o ) == 0; 
    }

   public int hashCode() {
      int hash = 7;
      return hash;
   }
  }  