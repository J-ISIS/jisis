/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.sorting;

/**
 *
 * @author jc_dauphin
 */
public class JisisSortRecord implements Record {
   private String data_;

    public JisisSortRecord(String data) {
       data_ = data;
    }
   public String skey() {
      return data_.substring(9);
   }
   public int key() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public String toString() {
      return data_;
   }

   public boolean equals(Object o) {
      if (!(o instanceof JisisSortRecord))
         return false;
      JisisSortRecord rec = (JisisSortRecord) o;
      return rec.skey().equals(skey());
   }

   public int compareTo(JisisSortRecord rec) {
      int rel = rec.skey().compareTo(skey());
      //System.out.println("rel="+rel +" rec.skey="+rec.skey()+" skey="+skey());
      return rel;

   }

}
