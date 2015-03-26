/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui.list;

import org.apache.commons.collections.Predicate;

/**
 *
 * @author jcd
 */

/**
  *  A Specification for a Filter.
  *  To create an instance of this class,
  *  you need to provide implementation for
  *  - getName()
  *  - evaluate(Object o)
  */
abstract public class FilterSpec implements Predicate
{
    /** Return the name of this Filter */
   abstract public String getName();

   @Override
   public String toString()
   {
       return getName();
   }
}

