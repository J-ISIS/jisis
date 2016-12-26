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
class XmlCodeDescriptionParser extends XMLItemParser {

   public XmlCodeDescriptionParser(IXMLReaderMediator med) {
      super("description", med);
   }

    @Override
    protected void copyBuffer() {
         XmlFixedFieldsReaderMediator med = (XmlFixedFieldsReaderMediator) med_;
         med.setCodeDescription(buffer_.toString().trim());
    }
    
    @Override
     public void startElement(
           String uri,
           String localName,
           String qName,
           Attributes attrib)
           throws SAXException {

      super.startElement(uri, localName, qName, attrib);

    

   }
}
