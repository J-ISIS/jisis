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
public class XmlFixedfieldParser extends XMLItemParser {

    public XmlFixedfieldParser(IXMLReaderMediator med) {
        super("Fixedfield", med);
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

        String tag = "";
        String repeatitive = "";
        String fullUrl = "";

        for (int i = 0; i < length; i++) {
            String attrName = attrib.getQName(i);
            if (attrName.equalsIgnoreCase("tag")) {
                tag = attrib.getValue(i);
            } else if (attrName.equalsIgnoreCase("name")) {
                repeatitive = attrib.getValue(i);
            } else if (attrName.equalsIgnoreCase("url")) {
                fullUrl = attrib.getValue(i);
            }
        }
        XmlFixedFieldsReaderMediator med = (XmlFixedFieldsReaderMediator) med_;

        med.setTag(tag);
        System.out.println("XmlFixedFieldParser qName=" + qName);

    }

    @Override
    protected void copyBuffer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
