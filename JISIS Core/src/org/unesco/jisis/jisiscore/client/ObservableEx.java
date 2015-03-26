/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisiscore.client;

import java.util.Observable;

/**
 *
 * @author jc Dauphin
 */

public class ObservableEx extends Observable {

    // The setChanged() protected method must be overridden to make it public
   @Override
    public synchronized void setChanged() {
        super.setChanged();
    }


}