/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jetty.webserver;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author jcdauphi
 */

public class AppContextBuilder {
   private WebAppContext webAppContext;
   private ServletContextHandler servletContextHandler;
   
 

   public ContextHandlerCollection buildWebAppContext(String dbHomePath) {
        ContextHandlerCollection contexts = new ContextHandlerCollection();

      webAppContext = new WebAppContext();
      webAppContext.setDescriptor("WEB-INF/web.xml");
      // ResourceHandler serving static content: 
     
  
      webAppContext.setResourceBase(dbHomePath);
      webAppContext.setContextPath(".");
      
       contexts.setHandlers(new Handler[] { webAppContext, new DefaultHandler() });
     
      return contexts;
   }
    public ContextHandlerCollection buildContext(String dbHomePath) {

       ContextHandlerCollection contexts = new ContextHandlerCollection();

        servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        /**
         * The context path is the prefix of a URL path that is used to select
         * the web application to which an incoming request is routed. 
         * Typically a URL in a Java servlet server is of the format
         * http://hostname.com/contextPath/servletPath/pathInfo, where each of 
         * the path elements may be zero or more / separated elements. 
         * If there is no context path, the context is referred to as the root context.
         * 
         * How you set the context path depends on how you deploy the web 
         * application (or ContextHandler): 
         */
        servletContextHandler.setContextPath("/");
        /**
         *  ResourceHandler serving static content: 
         * localhost:8585 will display the database folder
         */
        servletContextHandler.setResourceBase(dbHomePath);

        System.out.println("Context Class Loader "+Thread.currentThread().getContextClassLoader());
       
        servletContextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

        servletContextHandler.addServlet(DefaultServlet.class, "/");

        final ServletHolder jsp =
                servletContextHandler.addServlet(JspServlet.class, "*.jsp");
        
        System.out.println("classpath="+ servletContextHandler.getClassPath());
        jsp.setInitParameter("classpath", servletContextHandler.getClassPath());
        
        

        // add your own additional servlets like this:
        // context.addServlet(JSONServlet.class, "/json");
        contexts.setHandlers(new Handler[] { servletContextHandler, new DefaultHandler() });

        return contexts;
    }
}
