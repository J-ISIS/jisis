/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.GroovyConsole;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.unesco.jisis.gui.Util;

public final class GroovyConsoleAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
        
        if (!Util.isAdmin()) {
            return;
        }
          GroovyConsoleTopComponent win = new GroovyConsoleTopComponent();
        win.open();
        win.repaint();
        win.requestActive();
      
    }
}
