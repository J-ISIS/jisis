
package org.unesco.jisis.dataentryex.xml;

import org.unesco.jisis.corelib.xml.IXMLReaderMediator;
import org.unesco.jisis.corelib.xml.XMLItemParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author jcd
 */
public class XmlFixedFieldsParser extends XMLItemParser {

   public XmlFixedFieldsParser(IXMLReaderMediator med) {
      super("FixedFields", med);
   }

   
   @Override
     public void startElement(
           String uri,
           String localName,
           String qName,
           Attributes attrib)
           throws SAXException {
      super.startElement( uri, localName, qName, attrib);
      if(qName.equals( tagName_) ) {
//         String recordDefinition = null;
//         for (int i = 0; i < attrib.getLength(); i++) {
//            String attrName = attrib.getQName(i);
//            if (attrName.equalsIgnoreCase("authority")) {
//                recordDefinition = attrib.getValue(i);
//            }
//         }
          XmlFixedFieldsReaderMediator med = (XmlFixedFieldsReaderMediator) med_;
        
         med_.beginTagParsing() ;
         System.out.println("XmlAuthorityParser qName="+qName);
      }
     }
   @Override
    protected void copyBuffer() {
        
    }

}
