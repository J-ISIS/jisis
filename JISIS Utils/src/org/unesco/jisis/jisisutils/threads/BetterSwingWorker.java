/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.threads;

import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author jcd
 */

public abstract class BetterSwingWorker {
    private final SwingWorker<Void, Void> worker = new SimpleSwingWorker();
 
    public void execute() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                before();
            }
        });
 
        worker.execute();
    }
 
    protected void before() {
        //Nothing by default
    }
 
    protected abstract void doInBackground() throws Exception;
 
    protected abstract void done();
 
    private class SimpleSwingWorker extends SwingWorker<Void, Void> {
        @Override
        protected Void doInBackground() throws Exception {
            BetterSwingWorker.this.doInBackground();
 
            return null;
        }
 
        @Override
        protected void done() {
            try {
                get();
            } catch (final InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (final ExecutionException ex) {
                throw new RuntimeException(ex.getCause());
            }
 
            BetterSwingWorker.this.done();
        }
    }
}