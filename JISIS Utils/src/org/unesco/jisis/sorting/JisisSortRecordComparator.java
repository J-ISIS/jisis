/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.sorting;

/**
 *
 * @author jc_dauphin
 */
public class JisisSortRecordComparator implements Comparator {

   public boolean relation(Object a, Object b) {
      if ( !(a instanceof JisisSortRecord) ) {
         throw new RuntimeException("RecordComparator parameter 1");
      }
       if ( !(b instanceof JisisSortRecord) ) {
         throw new RuntimeException("RecordComparator parameter 2");
      }
      return execute((JisisSortRecord) a, (JisisSortRecord) b);

   }

   public static boolean execute(JisisSortRecord a, JisisSortRecord b) {
      int rel = a.compareTo(b);
      
      return rel > 0;
   }

}
