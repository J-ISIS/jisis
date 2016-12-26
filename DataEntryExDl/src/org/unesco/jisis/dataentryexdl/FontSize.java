/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

/**
 *
 * @author jc Dauphin
 * 
 * A Simple class to create an object to embed the font size
 */
public class FontSize {
   private int fontSize_;
   
   public FontSize(int fontSize) {
      fontSize_ = fontSize;
   }
   
   public void setFontSize(int fontSize) {
      fontSize_ = fontSize;
   }
   
   public int getFontSize() {
      return fontSize_;
   }
}
