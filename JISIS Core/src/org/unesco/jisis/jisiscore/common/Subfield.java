/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.jisiscore.common;

//~--- JDK imports ------------------------------------------------------------


import java.io.IOException;
import java.io.Serializable;
import org.unesco.jisis.corelib.exceptions.DbException;

/**
 *
 * @author jc_dauphin
 */
public class Subfield implements Serializable {
   private transient String  subfieldTag_;
   private transient String  value_;
   private static final long serialVersionUID = 1L;

   public Subfield(String subfieldTag, String value) {
      subfieldTag_ = subfieldTag;
      value_       = value;
   }

   public String getSubfieldTag() {
      return subfieldTag_;
   }

   public boolean hasSubfields() {
      return true;
   }

   public String getSubfieldValue() {
      return value_;
   }

   public void setFieldValue(String value) throws DbException {
      subfieldTag_ = "*";
      value_       = value;
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof Subfield)) {
         System.out.println("Object");

         return false;
      }

      Subfield subfield = (Subfield) o;
      String   tag1     = subfieldTag_;
      String   tag2     = subfield.subfieldTag_;

      if (!((tag1 == null)
            ? tag2 == null
            : tag1.equals(tag2))) {
         System.out.println("Subfield Tags are differents Values: " + tag1 + tag2);

         return false;
      }

      String val1 = value_;
      String val2 = subfield.value_;

      if (!((val1 == null)
            ? val2 == null
            : val1.equals(val2))) {
         System.out.println("Subfield values are differents Values: " + val1 + val2);

         return false;
      }

      return true;
   }

   private void writeObject(java.io.ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      out.writeUTF(subfieldTag_);
      out.writeUTF(value_);
   }

   private void readObject(java.io.ObjectInputStream in)
           throws IOException, ClassNotFoundException {
      in.defaultReadObject();

      subfieldTag_ = in.readUTF();
      value_       = in.readUTF();
   }
}
