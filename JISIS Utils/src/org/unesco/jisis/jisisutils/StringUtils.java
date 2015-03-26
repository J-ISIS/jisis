/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils;

/**
 *
 * @author jc_dauphin
 */
public class StringUtils {

   public static String removeChar(String s, char c) {
      StringBuffer r = new StringBuffer(s.length());

      r.setLength(s.length());

      int current = 0;

      for (int i = 0; i < s.length(); i++) {
         char cur = s.charAt(i);

         if (cur != c) {
            r.setCharAt(current++, cur);
         }
      }

      r.setLength(current);

      return r.toString();
   }

   public static String replaceChar(String source, int toReplace, int replacement) {
      StringBuffer ret = new StringBuffer(source);
      for (int i = 0; i < ret.length(); i++) {
         int cur = ret.charAt(i);

         if (cur == toReplace) {
            ret.setCharAt(i, (char) replacement);
         }
      }
      return ret.toString();
   }

   /**
    *  utility method that can replace all instances of a substring (toReplace)
    * inside a given string (source) with a replacement string (replacement).
    */
   public static String replaceAll(String source, String toReplace, String replacement) {
      int idx = source.lastIndexOf(toReplace);

      if (idx != -1) {
         StringBuffer ret = new StringBuffer(source);

         ret.replace(idx, idx + toReplace.length(), replacement);

         while ((idx = source.lastIndexOf(toReplace, idx - 1)) != -1) {
            ret.replace(idx, idx + toReplace.length(), replacement);
         }

         source = ret.toString();
      }

      return source;
   }

   public static String stringToHTMLString(String string) {
      StringBuffer sb = new StringBuffer(string.length());
      // true if last char was blank
      boolean lastWasBlankChar = false;
      int len = string.length();
      char c;

      for (int i = 0; i < len; i++) {
         c = string.charAt(i);

         if (c == ' ') {
            // blank gets extra work,
            // this solves the problem you get if you replace all
            // blanks with &nbsp;, if you do that you loss
            // word breaking
            if (lastWasBlankChar) {
               lastWasBlankChar = false;

               sb.append("&nbsp;");

            } else {
               lastWasBlankChar = true;

               sb.append(' ');
            }

         } else {
            lastWasBlankChar = false;

            //
            // HTML Special Chars
            if (c == '"') {
               sb.append("&quot;");
            } else if (c == '&') {
               sb.append("&amp;");
            } else if (c == '<') {
               sb.append("&lt;");
            } else if (c == '>') {
               sb.append("&gt;");
            } else if (c == '\n') {
               // Handle Newline
               sb.append("&lt;br/&gt;");

            } else {
               int ci = 0xffff & c;

               if (ci < 160) {
                  // nothing special only 7 Bit
                  sb.append(c);

               } else {
                  // Not 7 Bit use the unicode system
                  sb.append("&#");
                  sb.append(new Integer(ci).toString());
                  sb.append(';');
               }
            }
         }
      }

      return sb.toString();
   }

   /**
    * Count the number of times the string "subString" occurs in "source"
    * @param source
    * @param subString
    * @return
    */
   public static int getSubstringCount(String source, String subString) {
      int count = 0;
      int pos = 0;

      while ((pos = source.indexOf(subString, pos)) >= 0) {
         pos += subString.length();
         count++;
      }

      return count;
   }

   public static String padRight(String s, int n) {
      return String.format("%1$-" + n + "s", s);
   }

   public static String padLeft(String s, int n) {
      return String.format("%1$#" + n + "s", s);
   }

   /**
    ** pad a string S with a size of N with char C
    ** on the left (True) or on the right(false)
    **/
   public static synchronized String paddingString(String s, int n, char c,
                                            boolean paddingLeft) {
      StringBuffer str = new StringBuffer(s);
      int strLength = str.length();
      if (n > 0 && n > strLength) {
         for (int i = 0; i <= n; i++) {
            if (paddingLeft) {
               if (i < n - strLength) {
                  str.insert(0, c);
               }
            } else {
               if (i > strLength) {
                  str.append(c);
               }
            }
         }
      }
      return str.toString();
   }
   
   public static String removeLastChar(String s) {
      if (s == null || s.length() == 0) {
         return s;
      }
      return s.substring(0, s.length() - 1);
   }


}
