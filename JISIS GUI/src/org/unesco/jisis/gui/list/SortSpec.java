/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui.list;

import java.awt.Component;
import java.util.Comparator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author jcd
 */
/**
 * Specification for a Sort Condition. To create an instance of this class, you
 * need to provide implementation of - getSortString() - getName()
 */
abstract public class SortSpec extends DefaultListCellRenderer
        implements Comparator, Predicate {

   protected String search;

   public int compare(Object o1, Object o2) {
      return getSortString(o1).
              compareTo(getSortString(o2));
   }

   public boolean evaluate(Object o) {
      return (getSortString(o).startsWith(search));
   }

   @Override
   public Component getListCellRendererComponent(JList list,
           Object value, int index, boolean isSelected,
           boolean cellHasFocus) {

      if (isSelected) {
         setBackground(list.getSelectionBackground());
         setForeground(list.getSelectionForeground());
      } else {
         setBackground(list.getBackground());
         setForeground(list.getForeground());
      }
      setFont(list.getFont());

      setText(getSortString(value));
      return this;
   }

   /**
    * Return the name of this Sort
    */
   @Override
   abstract public String getName();

   /**
    * Return String to display, sort & search by
    */
   abstract public String getSortString(Object o);

   public void setSearch(String s) {
      search = s;
   }

   @Override
   public String toString() {
      return getName();
   }
}
