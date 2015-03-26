/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils;

/**
 *
 * @author jcdauphi
 */

public class ReadException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5944725504745547021L;

    public ReadException(String msg) {
        super(msg);
    }

    public ReadException(Throwable e) {
        super(e);
    }

    public ReadException(String msg, Throwable e) {
        super(msg, e);
    }
}