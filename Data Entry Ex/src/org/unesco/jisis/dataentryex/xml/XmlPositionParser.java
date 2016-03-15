/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex.xml;


import org.unesco.jisis.corelib.xml.IXMLReaderMediator;
import org.unesco.jisis.corelib.xml.XMLItemParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author jcd
 */
public class XmlPositionParser  extends XMLItemParser {

   public XmlPositionParser(IXMLReaderMediator med) {
      super("position", med);
   }
   
   @Override
    public void startElement(
           String uri,
           String localName,
           String qName,
           Attributes attrib)
           throws SAXException {

      super.startElement(uri, localName, qName, attrib);

      int length = attrib.getLength();

      String positionLabel = "";
     

      for (int i = 0; i < length; i++) {
         String attrName = attrib.getQName(i);
         if (attrName.equalsIgnoreCase("label")) {
            positionLabel = attrib.getValue(i);
         }
      }
      

       XmlFixedFieldsReaderMediator med = (XmlFixedFieldsReaderMediator) med_;
      
      med.setPositionsLabel(positionLabel);
      System.out.println("XmlFieldParser qName="+qName);

   }

    @Override
    protected void copyBuffer() {
        
    }
}
