/*
 * Bib1UseAttributes.java
 *

 */

package org.unesco.jisis.z3950;
import java.util.*;
/**
 *
 * Name         	Value 	Semantics
 * -------------------------------
 * Personal name 	1 	See bib1 semantics
 * Corporate name 	2
 * Conference name 	3
 * Title        	4
 * Title series 	5
 * Title uniform 	6
 * ISBN         	7
 * ISSN         	8
 * LC card number 	9
 * BNB card no. 	10
 * BGF number      	11
 * Local number 	12
 * Dewey classification	13
 * UDC classification 	14
 * Bliss classification	15
 * LC call number 	16
 * NLM call number 	17
   .......http://www.loc.gov/z3950/agency/defns/bib1.html
 * Abstract 	62
 * Note 	63

 * Author-title 	1000
 * Record type          1001
 * Name                 1002
 * Author         	1003
 * Author-name personal	1004
 * Author-name corporate   1005
 * Author-name conference  1006

 * Author-Title-Subject 	1036
 * 
 */
public class Bib1UseAttributes {
    Hashtable bib1AttributeName=null,bib1AttributeNo=null;
    
    /** Creates a new instance of Bib1UseAttributes */
    public Bib1UseAttributes() {
        setBib1AttributeNameHash();
        setBib1AttributeNoHash();
    }
    
    public void setBib1AttributeNameHash()
    {
        bib1AttributeName=new Hashtable();
        bib1AttributeName.put("1","Personal name");
        bib1AttributeName.put("2","Corporate name");
        bib1AttributeName.put("3","Conference name");
        bib1AttributeName.put("4","Title");
        bib1AttributeName.put("5","Title series");
        bib1AttributeName.put("1003","Author");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
//        bib1AttributeName.put("","");
        
        
    }
    public void setBib1AttributeNoHash()
    {
        bib1AttributeNo=new Hashtable();
        bib1AttributeNo.put("Personal name","1");
        bib1AttributeNo.put("Corporate name","2");
        bib1AttributeNo.put("Conference name","3");
        bib1AttributeNo.put("Title","4");
        bib1AttributeNo.put("Title series","5");
        bib1AttributeNo.put("Author","1003");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
//        bib1AttributeNo.put("","");
        
        
    }
    public java.util.Set getBib1AttributeNameHashKeys()
    {
        return bib1AttributeName.keySet();
    }
    public int getBib1AttributeNameHashSize()
    {
        return bib1AttributeName.size();
    }
    public java.util.Set getBib1AttributeNoHashKeys()
    {
        return bib1AttributeNo.keySet();
    }
    public int getBib1AttributeNoHashSize()
    {
        return bib1AttributeNo.size();
    }
    public Object getBib1AttributeNo(Object key)
    {
        return bib1AttributeNo.get(key);
    }
}
