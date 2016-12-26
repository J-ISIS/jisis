/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentry;

import javax.swing.JButton;

/**
 *
 * @author jcd
 */
public class PickListButton extends JButton {
     private int id_;
      private int tag_;
    
    /**
     * Creates a new instance of DeleteButton
     */
    public PickListButton(int id, int tag) {
        super();
        id_ = id;
        tag_ = tag;
    }
    
    public int getID() {
        return id_;
    }
    public int getTag() {
       return tag_;
    }
}
