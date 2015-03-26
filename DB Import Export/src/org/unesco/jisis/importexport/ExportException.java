/*
 * ExportException.java
 *
 * Created on February 3, 2007, 5:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.importexport;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author rustam
 */
public class ExportException extends Exception {
    
    /** Creates a new instance of ExportException */
    private ExportException() {
    }
    
    public ExportException(Throwable th) {
        super(th);
    }

    public void displayError() {
     NotifyDescriptor d =
                new NotifyDescriptor.Message("Export operation failed. Reason: " + this.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
}
