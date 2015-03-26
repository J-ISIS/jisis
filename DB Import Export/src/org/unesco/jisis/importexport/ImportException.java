/*
 * ImportException.java
 *
 * Created on February 3, 2007, 5:14 PM
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
public class ImportException extends RuntimeException {
    
    /** Creates a new instance of ImportException */
    private ImportException() {
    }
    
    public ImportException(Throwable th) {
        super(th);
    }
    
    public ImportException(String st) {
        super(st);
    }
    
    public void displayError() {
     NotifyDescriptor d =
                new NotifyDescriptor.Message("Import operation failed. Reason: " + this.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
}
