/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import org.openide.util.Exceptions;

/**
 *
 * @author jcd
 */
public class LoadDocumentSwingWorker extends SwingWorker<Void, Object> {

   private final JTextPane jtextPane_;
   private final String text_;

   public LoadDocumentSwingWorker(JTextPane jtextPane, String text) {
      jtextPane_ = jtextPane;
      text_ = text;
   }

   //runs on a background thread.
   @Override
   protected Void doInBackground() throws Exception {
      Reader r = new StringReader(text_);
      jtextPane_.setContentType("text/plain;charset=UTF-8");

      jtextPane_.read(r, null);
      return null;
   }

   //runs on EDT, allowed to update gui
   @Override
   protected void done() {
      try {
         this.get();//get() retrieves the return value from doInBackground()
         jtextPane_.updateUI();
      } catch (InterruptedException ex) {
         Exceptions.printStackTrace(ex);
      } catch (ExecutionException ex) {
         Exceptions.printStackTrace(ex);
      }


   }
}
