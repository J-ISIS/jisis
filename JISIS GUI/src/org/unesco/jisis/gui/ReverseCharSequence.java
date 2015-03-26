/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;


/*
 * :noTabs=false:
 *
 * Copyright (C) 2008 Kazutoshi Satoda
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
/**
 * Reversed view of a given CharSequence.
 */
public class ReverseCharSequence implements CharSequence {

   private final CharSequence base;

   public ReverseCharSequence(CharSequence base) {
      this.base = base;
   }

   public CharSequence baseSequence() {
      return base;
   }

   public char charAt(int index) {
      return base.charAt(base.length() - index - 1);
   }

   public int length() {
      return base.length();
   }

   public CharSequence subSequence(int start, int end) {
      int baseLength = base.length();
      return new ReverseCharSequence(
              base.subSequence(baseLength - end, baseLength - start));
   }

   @Override
   public String toString() {
      int baseLength = base.length();
      StringBuilder builder = new StringBuilder(baseLength);
      for (int i = baseLength - 1; i >= 0; --i) {
         builder.append(base.charAt(i));
      }
      return builder.toString();
   }

}
