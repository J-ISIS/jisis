/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex.xml;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.unesco.jisis.corelib.xml.IXMLReaderMediator;
import org.unesco.jisis.corelib.xml.XMLItemParser;
import org.unesco.jisis.corelib.xml.XMLNullParser;
import org.unesco.jisis.corelib.xml.XMLTagParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author jcd
 */



/**
 *
 * @author jcd
 */

/**
 * Reads the J-ISIS FDT
 * <fdt recorddefinition="UserDefinedRecords"
 *
 *
 * >
 * <field tag="10" name="Authors" type="alphanumeric" repetition="true" subfields="abc" />
 * ..................................
 * </fdt>
 *
 * The technique to parsing nested XML is to create parse handlers for each tag,
 * switch them into place when the appropriate tag begins, and restore the
 * previous parse handler when that same tag ends.
 *
 * The XMLFdtReaderMediator class creates instances of the parse handlers for
 * each of the dictionary tags and store them in a hashtable (Factory Method).
 * The XMLFdtReaderMediator handles  the switching between the parse handlers and thus
 * encapsulates how parse handlers interact (Mediator Pattern).
 *
 * The XMLFdtReaderMediator becomes the SAX parser and passes on the calls to
 * the current parse handler, keeping previous parse handlers on a stack.
 * @author jc_dauphin
 */
public class XmlFixedFieldsReaderMediator extends DefaultHandler implements IXMLReaderMediator {
   
    private FixedFieldsContainer fixedFieldsContainer_;
    private FixedFieldDescription fixedFieldDescription_;
    
   
    
    public String positionsLabel_;
    public String positionsDescription_;
    public List<SubfieldValue> subfieldValues_;
    
    public String codeLabel_;
    public String codeDescription_;
    
    protected  String tag_;
    protected List<FixedFieldSubfield> fixedFieldSubfields_;
    
    private XMLTagParser                         parseHandler_;
    private Hashtable                            parsers_;
    private Stack                                stack_;
    private boolean parseError_ = false;
   
   
    private boolean codeParsing = false;

//  -----------
    public   XmlFixedFieldsReaderMediator(FixedFieldsContainer fixedFieldsContainer, String inputFile, String path) {
       fixedFieldsContainer_     = fixedFieldsContainer;
       
       parsers_ = new Hashtable();
       stack_ = new Stack();
       parsers_.put("FixedFields", new XmlFixedFieldsParser(this));
       parsers_.put("fixedfield", new XmlFixedfieldParser(this));
       parsers_.put("position", new XmlPositionParser(this));
       parsers_.put("description", new XmlPositionDescriptionParser(this));
       parsers_.put("code", new XmlCodeParser(this));
       parsers_.put("description1", new XmlCodeDescriptionParser(this));
     
       positionsLabel_ = "";
       positionsDescription_ = "";
       subfieldValues_ = new ArrayList<>();

       parseHandler_ = new XmlFixedFieldsParser(this);
        try {
            SAXParserFactory sFact   = SAXParserFactory.newInstance();
            SAXParser        sParser = sFact.newSAXParser();
            File             fl      = new File(inputFile);
            sParser.parse(fl, this);
        } catch (SAXException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            parseError_ = true;

        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            parseError_ = true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            parseError_ = true;
        }
    }

    // -----------
    public XMLItemParser getParser(String attrib) {
        XMLItemParser iParser = null;
        iParser = (XMLItemParser) parsers_.get(attrib);
        if (iParser == null) {
            iParser = new XMLNullParser(this);
        }
        return iParser;
    }

    // --------------
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        parseHandler_.characters(ch, start, length);
    }

    // -----------
    public FixedFieldsContainer getFixedFieldsContainer() {
        return fixedFieldsContainer_;
    }
    public boolean getParseError() {
       return parseError_;
    }
    // -----------
    
  
    
    // ------
    // a tag has been started
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrib)
            throws SAXException {
      // push the current parser and get the new tag parser
      switch (qName) {
         case "FixedFields":
            // save old parser
            stack_.push(parseHandler_);
            parseHandler_ = new XmlFixedFieldsParser(this);
            break;
         case "fixedfield":
            // save old parser
            stack_.push(parseHandler_);
            fixedFieldSubfields_ = new ArrayList<FixedFieldSubfield>();
            parseHandler_ = new XmlFixedfieldParser(this);
            break;
         case "position":
            // save old parser
            stack_.push(parseHandler_);
            parseHandler_ = new XmlPositionParser(this);
            codeParsing = false;
            break;
         case "description":
            // save old parser
            stack_.push(parseHandler_);
             if (codeParsing) {
                 parseHandler_ = new XmlCodeDescriptionParser(this);
             } else {
                 parseHandler_ = new XmlPositionDescriptionParser(this);
             }
            break;
         case "code":
            // save old parser
            stack_.push(parseHandler_);
            parseHandler_ = new XmlCodeParser(this);
            codeParsing = true;
            break;
        
      }
        
       
        // pass call to new parse handler
        parseHandler_.startElement(uri, localName, qName, attrib);
    }

    // ------
   
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        parseHandler_.endElement(uri, localName, qName);
        if (qName.equalsIgnoreCase("fixedfield")) {
             // a fixed field tag has ended - store the field description in the container
           fixedFieldDescription_= new FixedFieldDescription(tag_, fixedFieldSubfields_);
           fixedFieldsContainer_.setField(fixedFieldDescription_);
        } else if (qName.equalsIgnoreCase("position")) {
            // a fixed field subfield defined by <position> tag has ended - store the position data
            FixedFieldSubfield fixedFieldSubfield = new FixedFieldSubfield(positionsLabel_, positionsDescription_,
                subfieldValues_);
            fixedFieldSubfields_.add(fixedFieldSubfield);
            positionsLabel_ = "";
            positionsDescription_ = "";
            subfieldValues_ = new ArrayList<>();
        }  else if (qName.equalsIgnoreCase("code")) {
            SubfieldValue subfieldValue = new SubfieldValue(codeLabel_, codeDescription_);
            subfieldValues_.add(subfieldValue);
             codeLabel_ = "";
            codeDescription_ = "";
        }
    }

    // -----------
    @Override
    public void endElement() {
        parseHandler_ = (XMLTagParser) stack_.pop();
    }

    

    @Override
    public void beginTagParsing() {}

    @Override
    public void endTagParsing() {}

   
    public void setTag(String tag) {
        tag_ = tag;
    }
    
    
    public void setPositionsLabel(String positionsLabel) {
        positionsLabel_ = positionsLabel;
    }
    public void setPositionsDescription(String positionsDescription) {
        positionsDescription_ = positionsDescription;
    }
    
    public void addSubfieldValue(SubfieldValue subfieldValue) {
        subfieldValues_.add(subfieldValue);
    }


    void setCodeLabel(String codeLabel) {
        codeLabel_ = codeLabel;
    }

    void setCodeDescription(String codeDescription) {
        codeDescription_ = codeDescription;
        
    }

   

   
}
