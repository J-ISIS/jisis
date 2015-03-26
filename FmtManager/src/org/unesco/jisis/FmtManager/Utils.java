/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.FmtManager;

import java.awt.Color;

/**
 *
 * @author jcd
 */
public class Utils {

   public static String colorToHex(Color color) {
      String colorstr = new String("#");

      // Red
      String str = Integer.toHexString(color.getRed());
      if (str.length() > 2) {
         colorstr = str.substring(0, 2);
      } else if (str.length() < 2) {
         colorstr += "0" + str;
      } else {
         colorstr += str;
      }

      // Green
      str = Integer.toHexString(color.getGreen());
      if (str.length() > 2) {
         str = str.substring(0, 2);
      } else if (str.length() < 2) {
         colorstr += "0" + str;
      } else {
         colorstr += str;
      }

      // Blue
      str = Integer.toHexString(color.getBlue());
      if (str.length() > 2) {
         colorstr = str.substring(0, 2);
      } else if (str.length() < 2) {
         colorstr += "0" + str;
      } else {
         colorstr += str;
      }

      return colorstr;
   }
   // NEW
   public static final char[] WORD_SEPARATORS = {' ', '\t', '\n',
      '\r', '\f', '.', ',', ':', '-', '(', ')', '[', ']', '{',
      '}', '<', '>', '/', '|', '\\', '\'', '\"'};

   public static boolean isSeparator(char ch) {
      for (int k = 0; k < WORD_SEPARATORS.length; k++) {
         if (ch == WORD_SEPARATORS[k]) {
            return true;
         }
      }
      return false;
   }
}