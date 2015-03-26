/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils;

/**
 * An Adapter to transform any object into a Row interface
 * @author jc_dauphin
 */
public interface IRowAdapter {

    int getColumnCount();

    /** Returns the type of the column mapped as column position column */
    Class getColumnClass(int column);

    Object getValueAt(int column);

    void setValueAt(Object value,int column);

}
