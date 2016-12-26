package org.unesco.jisis.jisisutils;

import java.util.ArrayList;
import java.util.List;
/**---------------------------------------------------------
 * Simple demo of CSV parser class.
 *
 * public class CSVSimple {
 *
 *    public static void main(String[] args) {
 *       CSV parser = new CSV( );
 *       List  list = parser.parse(
 *             "\"LU\",86.25,\"11/4/1998\",\"2:19PM\",+4.0625");
 *       Iterator it = list.iterator( );
 *
 *       while (it.hasNext( )) {
 *          System.out.println(it.next( ));
 *       }
 *    }
 * }
 * -------------------------------------------------------
 *
 *     CSV parser = new CSV( );
 *     List list = null;
 *     // Create a new instance of a LineNumberReader object
 *     // that is reading from a FileReader object.
 *     FileReader fileReader = new FileReader("data.csv");
 *     LineNumberReader reader = new LineNumberReader(fileReader);
 *
 *     // Read from the FileReader.
 *     String line=null;
 *     while ((line = reader.readLine()) != null) {
 *         list =parser.parse(line);
 *         System.out.print((reader.getLineNumber());
 *         terator it = list.iterator( );
 *         while (it.hasNext( )) {
 *            System.out.print(it.next( )+" ");
 *         }
 *         
 *       }
 *     }
 *
 *     // Close the LineNumberReader and FileReader.
 *     fileReader.close();
 *     reader.close();
 */

public class CSV {

   public static final char DEFAULT_SEP = ',';
   /** The fields in the current String */
   protected List list = new ArrayList();
   /** the separator char for this parser */
   protected char fieldSep;


   /** Construct a CSV parser, with the default separator (','). */
   public CSV() {

      this(DEFAULT_SEP);

   }

   /** Construct a CSV parser with a given separator.
    * @param sep The single char for the separator (not a list of
    * separator characters)
    */
   public CSV(char sep) {
      fieldSep = sep;
   }

   /** parse: break the input String into fields
    * @return java.util.Iterator containing each field
    * from the original as a String, in order.
    */
   public List parse(String line) {

      StringBuffer sb = new StringBuffer();

      list.clear();            // Empty the list

      int i = 0;

      if (line.length() == 0) {
         list.add(line);
         return list;
      }

      do {
         sb.setLength(0);
         if (i < line.length() && line.charAt(i) == '"') {
            i = advQuoted(line, sb, ++i);    // skip quote
         } else {
            i = advPlain(line, sb, i);
         }
         list.add(sb.toString());
         System.out.println("csv"+ sb.toString());
         i++;
      } while (i < line.length());

      return list;
   }

   /** advQuoted: quoted field; return index of next separator */
   protected int advQuoted(String s, StringBuffer sb, int i) {
      int j;
      int len = s.length();
      for (j = i; j < len; j++) {
         if (s.charAt(j) == '"' && j + 1 < len) {
            if (s.charAt(j + 1) == '"') {
               j++; // skip escape char
            } else if (s.charAt(j + 1) == fieldSep) { //next delimiter
               j++; // skip end quotes
               break;
            }
         } else if (s.charAt(j) == '"' && j + 1 == len) { // end quotes at end of line
            break; //done
         }
         sb.append(s.charAt(j));    // regular character.
      }

      return j;
   }

   /** advPlain: unquoted field; return index of next separator */
   protected int advPlain(String s, StringBuffer sb, int i) {

      int j;
      j = s.indexOf(fieldSep, i); // look for separator

      System.out.println("csv"+ "i = " + i + ", j = " + j);

      if (j == -1) {                   // none found
         sb.append(s.substring(i));
         return s.length();
      } else {
         sb.append(s.substring(i, j));
         return j;
      }
   }
}