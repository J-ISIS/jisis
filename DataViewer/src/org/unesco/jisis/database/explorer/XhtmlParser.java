/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.database.explorer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author jcd
 */
public class XhtmlParser {

   private DocumentBuilder builder;

   public XhtmlParser() {
      init();
   }

   private static class DefaultErrorHandler implements ErrorHandler {

      public void warning(SAXParseException exception)
              throws SAXException {
         System.out.println("SAXParseException warning");
         // Do nothing
      }

      public void error(SAXParseException exception) throws SAXException {
         // to be filled in later
         System.out.println("SAXParseException error");
      }

      public void fatalError(SAXParseException exception)
              throws SAXException {
         System.out.println("SAXParseException fatal error");
         // to be filled in later
      }
   }

   private static class CachedDTD implements EntityResolver {

      public static final String XHTML_DTD_TRANSITIONAL = "xhtml1-transitional.dtd";
      public static final String XHTML_DTD = "xhtml1-strict.dtd";
      public static final String XHTML_LAT1 = "xhtml-lat1.ent";
      public static final String XHTML_SYMBOL = "xhtml-symbol.ent";
      public static final String XHTML_SPECIAL = "xhtml-special.ent";

      public InputSource resolveEntity(String publicID, String systemID)
              throws SAXException, IOException {
         String resource = systemID.substring(systemID.lastIndexOf("/") + 1);
         InputStream uri = null;
         if (resource.equals(XHTML_DTD)) {
            uri = this.getClass().getResourceAsStream(XHTML_DTD);
         } else if (resource.equals(XHTML_DTD_TRANSITIONAL)) {
            uri = this.getClass().getResourceAsStream(XHTML_DTD_TRANSITIONAL);
         } else if (resource.equals(XHTML_LAT1)) {
            uri = this.getClass().getResourceAsStream(XHTML_LAT1);
         } else if (resource.equals(XHTML_SYMBOL)) {
            uri = this.getClass().getResourceAsStream(XHTML_SYMBOL);
         } else if (resource.equals(XHTML_SPECIAL)) {
            uri = this.getClass().getResourceAsStream(XHTML_SPECIAL);
         } else {
            return null;
         }
         return new InputSource(uri);

      }
   }
   String test =
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
           + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
           + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
           + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
           + "<head>"
           + "<title>My First Document</title>\n"
           + "<style type=\"text/css\"> b { color: green; } </style>\n"
           + "</head>\n"
           + "<body>\n"
           + "<p>\n"
           + "<b>Greetings Earthlings!</b>\n"
           + " We've come for your Java.\n"
           + "</p>\n"
           + "</body>\n"
           + "</html>\n";

   private void init() {
      DocumentBuilder builder = null;
      Document doc = null;
      try {
         builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         builder.setEntityResolver(new CachedDTD());
         builder.setErrorHandler(new DefaultErrorHandler());
      } catch (ParserConfigurationException ex) {
         ex.printStackTrace();
      }


   }

   public void parse(String xhtmlContent) throws SAXException {
      InputSource in = new InputSource(new StringReader(xhtmlContent));
      parse(in);
   }

   public void parse(InputSource in) throws SAXException {

      try {
         Document doc = builder.parse(in);

      } catch (Exception e) {
         throw new RuntimeException("parse error.", e);
      }
   }

   public static void printTree(Document doc) {
      Element root = doc.getDocumentElement();
      System.out.println("Root Element:" + root.getTagName());

      System.out.println("***** ********* *****");
      // page
      NodeList list = root.getElementsByTagName("page");
      // page
      for (int i = 0; i < list.getLength(); i++) {
         // page
         Element element = (Element) list.item(i);
         // id
         String id = element.getAttribute("id");
         // title
         NodeList titleList = element.getElementsByTagName("title");
         // title
         Element titleElement = (Element) titleList.item(0);
         // title
         String title = titleElement.getFirstChild().getNodeValue();
         // file
         NodeList fileList = element.getElementsByTagName("file");
         // file
         Element fileElement = (Element) fileList.item(0);
         // file
         String file = fileElement.getFirstChild().getNodeValue();

         System.out.println("ID：" + id + "  "
                 + "title：" + title + "  "
                 + "file：" + file);
      }
   }
}

