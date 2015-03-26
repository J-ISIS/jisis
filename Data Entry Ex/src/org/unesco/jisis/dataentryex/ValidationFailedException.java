/*
 * RecordNotFoundException.java
 *
 * Created on June 30, 2006, 7:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryex;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author rustam
 */
public class ValidationFailedException extends Exception {
    
    /** Creates a new instance of RecordNotFoundException */
    private String errorMessage  = "";
    
    public ValidationFailedException(String message) {
        errorMessage = message;
    }
 
    
    public void displayWarning() {
        String warnMsg = NbBundle.getMessage(ValidationFailedException.class, "MSG_ValidationFormatError") + "\n " + errorMessage;
        NotifyDescriptor d =
                new NotifyDescriptor.Message(warnMsg, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
}
