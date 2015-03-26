/*
 * SubField.java
 *
 */
package org.unesco.jisis.z3950;

public class SubField implements java.io.Serializable {

   private char delimiter;
   private String data;

   /**
    * default constructor
    */
   public SubField() {
   }


   /**
    * Constructor with a MARC subfield delimiter and data as arguments
    * @param delimiter The MARC subfield delimiter
    * @param data The subfield data as an array of characters
    */
   public SubField(char delimiter, char[] data) {
      this(delimiter, new String(data));
   }


   /**
    * Constructor with a MARC subfield delimiter and data as arguments
    * @param delimiter The MARC subfield delimiter
    * @param data The subfield data as a string
    */
   public SubField(char delimiter, String data) {

      this.delimiter = delimiter;
      this.data = data;
      if (this.data == null) {
         this.data = "";
      }
   }

   /**
   <p>
   This method returns the delimiter of the subfield.
   </p>
    */
   public char getDelimiter() {
      return this.delimiter;
   }

   public String getData() {
      return data;
   }

   public int length() {
      return 1 + data.length();
   }

   @Override
   public String toString() {
      return "$" + delimiter + " " + data;
   }
}
