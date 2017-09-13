/*
 * Z3950Query.java Represents a Z3950 query to be submitted to a Z39.50 Server.
 *
 * Created on February 12, 2008, 4:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.unesco.jisis.z3950;

import java.util.*;
import com.k_int.IR.*;

/**
 *
 * The jzkit representation of the query to evaluate
 *
 * private IRQuery q; A Generic Information Retrieval Query
 *
 * @uml.property name="q"
 * @uml.associationEnd multiplicity="(1 1)"
 */
public class Z3950Query extends IRQuery {

    /**
     * Constructor to create a new instance of Z3950Query
     *
     * @param conn
     * @param searchProp
     */
    public Z3950Query(Z3950Connection conn, Hashtable searchProp) {
        // We want to give the backend some hints
        this.hints = new Hashtable();
        String element_set_name = String.valueOf(searchProp.get("ElementSetName"));
//      this.hints.put("default_element_set_name", "f");
//      this.hints.put("small_set_setname", "f");
        this.hints.put("default_element_set_name", element_set_name);
        this.hints.put("small_set_setname", element_set_name);

        // We would like to specifically ask for marc21 records
        // (This overrides the one set with default_record_syntax above)
        String record_type = String.valueOf(searchProp.get("RecordType"));
        //this.hints.put("record_syntax", "marc21");
        this.hints.put("record_syntax", record_type);

        String encoding = String.valueOf(searchProp.get("Encoding"));
        this.hints.put("charset", "charset_utf8");
        this.hints.put("charset", encoding);

        // this vector contains the names of the remote Z39.50 databases we want to search
        this.collections = new Vector();
        this.collections.add(conn.getDatabase());                 // set Database
        //this.query_syntax = "PREFIX";

        String att1 = String.valueOf(searchProp.get("attribute1"));
        String val1 = String.valueOf(searchProp.get("value1"));
        String att2 = String.valueOf(searchProp.get("attribute2"));
        String val2 = String.valueOf(searchProp.get("value2"));
        String relation = String.valueOf(searchProp.get("relation"));
        String bquery = "";
        if (!val1.equals("")) {
            att1 = " @attr 1=".concat(att1);
            att1 = att1.concat(" ");
            att1 = att1.concat(val1);
            bquery = att1;
        }
//        System.out.println("value1:"+val1);

        if (!val2.equals("")) {
            relation = "@".concat(relation.toLowerCase());

            att2 = " @attr 1=".concat(att2);
            att2 = att2.concat(" ");
            att2 = att2.concat(val2);
            if (!val1.equals("")) {
                bquery = relation.concat(att1);

            }
            bquery = bquery.concat(att2);

        }
        bquery = "@attrset bib-1 ".concat(bquery);
        System.out.println("bquery: " + bquery);
//        this.query = "@attrset bib-1 @attr 1="+rhash.get("attribute1")+ rhash.get("value1");
        QueryModel queryModel = new com.k_int.IR.QueryModels.PrefixString(bquery);
        this.query = queryModel;
    }

    public Z3950Query getQuery() {
        return this;
    }

}
