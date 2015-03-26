/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryexdl;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author jcd
 */
public class DefaultFormattingException extends Exception {

    /** Creates a new instance of RecordNotFoundException */
    private String errorMessage  = "";

    public DefaultFormattingException(String message) {
        errorMessage = message;
    }


    public void displayWarning() {
        String warnMsg = NbBundle.getMessage(DefaultFormattingException.class, "MSG_DefaultFormatError") + " " + errorMessage;
        NotifyDescriptor d =
                new NotifyDescriptor.Message(warnMsg, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
}