/*
 * DeleteButton.java
 *
 * Created on July 13, 2006, 10:29 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentry;

import javax.swing.JButton;

/**
 *
 * @author rustam
 */
public class DeleteButton extends JButton {
    
    private int id_;
    
    /**
     * Creates a new instance of DeleteButton
     */
    public DeleteButton(int id) {
        super();
        id_ = id;
    }
    
    public int getID() {
        return id_;
    }
}
