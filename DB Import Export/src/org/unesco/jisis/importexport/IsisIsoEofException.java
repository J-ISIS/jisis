/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.importexport;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author jc_dauphin
 */
class IsisIsoEofException extends Exception {

   IsisIsoEofException(String string) {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   private IsisIsoEofException() {
   }
 
    
    public IsisIsoEofException(Throwable th) {
        super(th);
    }

    public void displayError() {
     NotifyDescriptor d =
                new NotifyDescriptor.Message("Import Terminated: " + this.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }
}
