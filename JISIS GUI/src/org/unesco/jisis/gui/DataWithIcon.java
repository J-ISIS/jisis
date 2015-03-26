/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import javax.swing.Icon;

/**
 *
 * @author jcd
 */
// A holder for data and an associated icon
public class DataWithIcon {

   protected Icon icon;
   protected Object data;

   public DataWithIcon(Object data, Icon icon) {
      this.data = data;
      this.icon = icon;
   }

   public Icon getIcon() {
      return icon;
   }

   public Object getData() {
      return data;
   }

   @Override
   public String toString() {
      return data.toString();
   }
}
