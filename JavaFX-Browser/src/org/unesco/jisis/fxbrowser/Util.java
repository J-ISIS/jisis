/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.fxbrowser;



import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

public class Util {
  // create a button with an icon
  static public Button createIconButton(String buttonText, String imageLoc, String tooltipText, EventHandler<ActionEvent> actionEventHandler) {
    final Button button = new Button(buttonText);
    button.setTooltip(new Tooltip(tooltipText));
    button.getStyleClass().add("icon-button");
    button.setMaxWidth(Double.MAX_VALUE);
    button.setAlignment(Pos.CENTER_LEFT);
    final ImageView imageView = new ImageView(new Image(getResource(imageLoc)));
    imageView.setFitHeight(16);
    imageView.setPreserveRatio(true);
    button.setGraphic(imageView);
    VBox.setMargin(button, new Insets(0, 5, 0, 5));
    button.setOnAction(actionEventHandler);

    return button;
  }

  // turn an awt image into a JavaFX image.
  static javafx.scene.image.Image bufferedImageToFXImage(java.awt.Image image) throws IOException {
    if (!(image instanceof RenderedImage)) {
      BufferedImage bufferedImage =
        new BufferedImage(
          image.getWidth(null),
          image.getHeight(null),
          BufferedImage.TYPE_INT_ARGB
        );
      Graphics g = bufferedImage.createGraphics();
      g.drawImage(image, 0, 0, null);
      g.dispose();
      image = bufferedImage;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write((RenderedImage) image, "png", out);
    out.flush();
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    return new javafx.scene.image.Image(in);
  }

  // get a resource relative to the application class.
  static String getResource(String path) {
    return SwingFXWebView.class.getResource(path).toExternalForm();
  }

  // debugging routine to dump the scene graph.
  public  static void dump(Node n) { dump(n, 0); }
  private static void dump(Node n, int depth) {
    for (int i = 0; i < depth; i++) {
          System.out.print("  ");
      }
    System.out.println(n);
    if (n instanceof Parent) {
          for (Node c : ((Parent) n).getChildrenUnmodifiable()) {
            dump(c, depth + 1);
        }
      }
  }

  // debugging routine to highlight the borders of nodes.
  public  static void highlight(Node n) { highlight(n, 0); }
  private static void highlight(Node n, int depth) {
    n.setStyle("-fx-stroke: red; -fx-stroke-width: 1; -fx-stroke-type: inside;");
    if (n instanceof Parent) {
          for (Node c : ((Parent) n).getChildrenUnmodifiable()) {
            highlight(c, depth + 1);
        }
      }
  }
  
  public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    transformer.transform(new DOMSource(doc), 
         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
}

}

