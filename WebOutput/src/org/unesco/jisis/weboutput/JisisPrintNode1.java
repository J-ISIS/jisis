/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.weboutput;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;

/**
 *
 * @author jc_dauphin
 */
public class JisisPrintNode1  extends AbstractNode {
    public JisisPrintNode1() {
        super(Children.LEAF);
        CookieSet cookies = getCookieSet();
        cookies.add(new JisisPrintCookie1());
    }


}
