/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.weboutput;

/**
 *
 * @author jc_dauphin
 */


import javax.swing.JOptionPane;
import org.openide.cookies.PrintCookie;

public class JisisPrintCookie1 implements PrintCookie {

    @Override
    public void print() {
        JOptionPane.showMessageDialog(null, "I am printing...");
    }

}