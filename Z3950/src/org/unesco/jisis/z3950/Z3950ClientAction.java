/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.z3950;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class Z3950ClientAction implements ActionListener {

   public void actionPerformed(ActionEvent e) {


        Z3950TopComponent win = new Z3950TopComponent();

        win.open();
        win.requestActive();
      // TODO implement action body
   }
}
