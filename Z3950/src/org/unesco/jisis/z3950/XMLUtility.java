/*
 * XMLUtility.java
 *
 */

package org.unesco.jisis.z3950;
import java.util.Properties;
import org.jdom.*;
import org.jdom.input.*;
/**
 *
 * @author root
 */
public class XMLUtility {
    private static final XMLUtility instance = new XMLUtility();
    
    
    
    /** Make the class not instantiable */
    private   XMLUtility() {
    }    
    
    public static XMLUtility getInstance(){
        return instance;
    }

    /**
     * Parse the XML string
     * @param xml the xml String to parse
     * @return
     */
   public Document getDocumentFromXML(String xml) {
      Document doc = null;
      try {
         SAXBuilder sax = new SAXBuilder();
         doc = sax.build(new java.io.StringReader(xml));
      } catch (Exception e) {
          e.printStackTrace();
      }
      return doc;
   }
   /**
    * Get the root element of the xml string
    * @param xml The xml String to parse
    * @return The root element
    */
   public Element getRootElementFromXML(String xml) {
      Element root = null;
      try {
         root = this.getDocumentFromXML(xml).getRootElement();
      } catch (Exception e) {
          e.printStackTrace();
      }
      return root;
   }
   /**
    * Generates an xml String for the whole Document
    * @param doc - The Document Object Model
    * @return The xml String
    */
   public String generateXML(org.jdom.Document doc) {
      String xml = "";
      try {
         org.jdom.output.XMLOutputter xmlout = new org.jdom.output.XMLOutputter();
         xml = xmlout.outputString(doc);

      } catch (Exception e) {
          e.printStackTrace();
      }
      return xml;
   }
    /**
    * Generates an xml String from the root element for the Document
    * @param root The element from which we generate
    * @return The xml String
    */
   public String generateXML(org.jdom.Element root) {
      String xml = "";
      try {
         org.jdom.Document doc = new org.jdom.Document(root);
         org.jdom.output.XMLOutputter xmlout = new org.jdom.output.XMLOutputter();
         xml = xmlout.outputString(doc);

      } catch (Exception e) {
          e.printStackTrace();
      }
      return xml;
   }

   /**
    *
    * @param handlerName
    * @param process
    * @return
    */
   public org.jdom.Element getRequestDataRoot(String handlerName, String process) {
      org.jdom.Element ele = new org.jdom.Element("OperationId");
      ele.setAttribute("handler", handlerName.trim());
      ele.setAttribute("process", process.trim());

      return ele;
   }

   /**
    *
    * @param xmlStr
    * @return
    */
   public Properties getHandlerDetails(String xmlStr) {
      Properties retProp = new Properties();
      try {
         System.out.println("xmlStr  " + xmlStr);
         org.jdom.input.SAXBuilder sax = new org.jdom.input.SAXBuilder();
         org.jdom.Document doc = sax.build(new java.io.StringReader(xmlStr));
         Element ele = doc.getRootElement();
         retProp.setProperty("HANDLER", ele.getAttributeValue("handler"));
         retProp.setProperty("PROCESS", ele.getAttributeValue("process"));

      } catch (Exception e) {

         e.printStackTrace();
      }

      return retProp;
   }
    
}
