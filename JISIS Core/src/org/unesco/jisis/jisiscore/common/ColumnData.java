/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisiscore.common;

/**
 *
 * @author jc_dauphin
 */
public class ColumnData { // Encapsulates Column information

   public String title_;
   public int width_;
   public int alignment_;

   public ColumnData(String title, int width, int alignment) {
      title_ = title;
      width_ = width;
      alignment_ = alignment;
   }
}