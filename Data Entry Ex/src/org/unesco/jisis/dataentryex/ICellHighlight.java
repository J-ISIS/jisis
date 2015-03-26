/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryex;

import javax.swing.JTable;

/**
 *
 * @author jcd
 */
public interface ICellHighlight {
   public abstract boolean shouldHighlight(JTable tbl, Object value, int row,
           int column);

}
