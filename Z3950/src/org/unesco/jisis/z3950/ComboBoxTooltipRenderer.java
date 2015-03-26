/*
 * ComboBoxTooltipRenderer.java
 *
 * Created on December 18, 2007, 1:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.unesco.jisis.z3950;

import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author root
 */
public class ComboBoxTooltipRenderer extends BasicComboBoxRenderer {

   /** Creates a new instance of ComboBoxTooltipRenderer */
   public ComboBoxTooltipRenderer() {
   }

   @Override
   public Component getListCellRendererComponent(
           JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      setToolTipText(value.toString());

      return this;
   }
}
