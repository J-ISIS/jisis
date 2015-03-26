/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutil.history;

/**
 *
 * @author jcd
 */

public interface MutableListModel extends javax.swing.ListModel {

    public boolean removeElement(Object elem);

    public void insertElementAt(Object elem, int index);
}
