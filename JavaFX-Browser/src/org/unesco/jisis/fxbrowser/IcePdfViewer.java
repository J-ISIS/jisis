/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.fxbrowser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

/**
 *
 * @author jcd
 */
public class IcePdfViewer {

   private SwingController controller;
   public final static String TITLE = "ICEpdf Viewer";

   public IcePdfViewer(String filePath) {


      // build a controller
      SwingController controller = new SwingController();

      // Build a SwingViewFactory configured with the controller
      SwingViewBuilder factory = new SwingViewBuilder(controller);

      // Use the factory to build a JPanel that is pre-configured
      //with a complete, active Viewer UI.
      JPanel viewerComponentPanel = factory.buildViewerPanel();

      // add copy keyboard command
      ComponentKeyBinding.install(controller, viewerComponentPanel);

      // add interactive mouse link annotation support via callback
      controller.getDocumentViewController().setAnnotationCallback(
              new org.icepdf.ri.common.MyAnnotationCallback(
              controller.getDocumentViewController()));


      // Create a JFrame to display the panel in
      JFrame window = new JFrame("J-ISIS ICEpdf PDF Viewer Component");
      window.getContentPane().add(viewerComponentPanel);
      window.pack();
      window.setVisible(true);

      // Open a PDF document to view
      URL url = null;
      try {
         url = new URL(filePath);
      } catch (MalformedURLException ex) {
         Logger.getLogger(IcePdfViewer.class.getName()).log(Level.SEVERE, null, ex);
      }
      controller.openDocument(url);



   }

   public void openFile(String filePath) {
      // Now that the GUI is all in place, we can try openning a PDF
      controller.openDocument(filePath);

   }
}