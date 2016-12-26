/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jetty.webserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jcdauphi
 */
public class JettyRunner {

    private ExecutorService exec_;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JettyRunner.class);

    private JettyServer jettyServer; 
    public JettyRunner(String dbHomePath) {
        ContextHandlerCollection contexts = new AppContextBuilder().buildContext(dbHomePath);

        jettyServer = new JettyServer();
        jettyServer.setHandler(contexts);
        
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                try {
                    jettyServer.start();
                } catch (Exception exception) {
                    LOGGER.debug("Cannot Start Jetty Web Server [{}]", exception);
                    exception.printStackTrace();
                }

            }
        };
        LOGGER.debug("Starting Jetty Web Server ...");
        exec_ = Executors.newSingleThreadExecutor();
        exec_.execute(runner);
        LOGGER.debug("Jetty Web Server Started");
    }
    
    public void stop() {
        try {
            jettyServer.stop();
        } catch (Exception ex) {
            LOGGER.error("Cannot Stop Jetty Web Server [{}]", ex);
        }
    }
}