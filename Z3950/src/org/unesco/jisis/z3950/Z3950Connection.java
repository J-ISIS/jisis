
/*
 * Z3950Connection.java
 */
package org.unesco.jisis.z3950;

import java.util.*;

import com.k_int.z3950.IRClient.*;
// for OID Register
import com.k_int.codec.util.*;

// Information Retrieval Interfaces
import com.k_int.IR.*;


public class Z3950Connection {

   private Searchable search = null;
   private String dataBase = "";

   /* Impl notes: Used
    * mostly by Present request, because z3950 is stateful,
    * so it needs to know what searches it has run, so
    * it can return more specific records.*/
   /**
    * Handles the translation of Object IDs into their names.
    *
    * @uml.property name="reg"
    * @uml.associationEnd multiplicity="(1 1)"
    */
    private OIDRegister reg = OIDRegister.getRegister();
/*  Impl: z3950 specifies that most everything, such as attributes,
    * relations, return formats and more, are sent with an ID.  The
    * list of ids is at: http://www.loc.gov/z3950/agency/defns/oids.html.
    * jzkit uses this OIDRegister to do the translations.  The a2jruntime
    * should hold all the information to propertly fill the OIDRegister
    * with appropriate information.
    */
  /**
   * Constructor to create a Z3950 connection
   *
   * @param serviceHost "ServiceHost" : Host to connect to
   * @param servicePort "ServicePort" : Port on which to connect
   * @param dataBase
   */
   public Z3950Connection(Hashtable searchProp) {

      String serviceHost = String.valueOf(searchProp.get("ServiceHost"));
      String servicePort = String.valueOf(searchProp.get("ServicePort"));
      this.dataBase = String.valueOf(searchProp.get("database"));
      String userName = String.valueOf(searchProp.get("UserName"));
      String password = String.valueOf(searchProp.get("Password"));
      String recordType = String.valueOf(searchProp.get("RecordType"));
      String elementSetName = String.valueOf(searchProp.get("ElementSetName"));
      Package ir_package = Package.getPackage("com.k_int.IR");
       System.out.println("Using IR Interfaces : " +ir_package.getSpecificationTitle()+ " " +
		                                 ir_package.getSpecificationVersion()+ " " +
						 ir_package.getSpecificationVendor());
       Package a2j_runtime_package = Package.getPackage("com.k_int.codec.runtime");

       if ( a2j_runtime_package != null )
         System.out.println("Using A2J Runtime : "+a2j_runtime_package.getImplementationTitle()+ " " +
		                                a2j_runtime_package.getImplementationVersion()+ " " +
		                                a2j_runtime_package.getImplementationVendor());

//      OIDRegisterEntry requested_syntax = null;
//       OIDRegisterEntry html = new OIDRegisterEntry("html",
//                "{1,2,840,10003,5,1000,34,1}", "record format", null);
//        reg.register_oid(html);
//# Record Syntax OID's
//
//oid.unimarc={1,2,840,10003,5,1}
//name.unimarc=UNIMarc Record


        OIDRegisterEntry unimarc = new OIDRegisterEntry("unimarc",
                "{1,2,840,10003,5,1}", "UNIMarc Record", null);
        reg.register_oid(unimarc);

        OIDRegisterEntry e = reg.lookupByName("unimarc");
        System.out.println("e="+e.getStringValue());

//oid.usmarc={1,2,840,10003,5,10}
//name.usmarc=US Marc Record

//oid.marc21={1,2,840,10003,5,10}
//name.marc21=Marc21 Record
         OIDRegisterEntry marc21 = new OIDRegisterEntry("marc21",
                "{1,2,840,10003,5,10}", "Marc21 Record", null);
        reg.register_oid(marc21);

//oid.sutrs={1,2,840,10003,5,101}
//name.sutrs=Simple Unstructured Text Record
        OIDRegisterEntry sutrs = new OIDRegisterEntry("sutrs",
                "{1,2,840,10003,5,101}", "Simple Unstructured Text Record", null);
        reg.register_oid(sutrs);

//oid.charset_utf8={1,0,10646,1,0,8}
//name.charset_utf8=UTF-8
        OIDRegisterEntry utf8 = new OIDRegisterEntry("charset_utf8",
        "{1,0,10646,1,0,8}", "UTF-8", null );
        reg.register_oid(utf8);

 





//      requested_syntax = reg.lookupByOID("marc21");
//      if (requested_syntax == null) {
//         System.out.println("Unsupported preferredRecordSyntax="+)
//      }

      // Create a Z39.50 origin.
      search = new Z3950Origin();

      Properties p = new Properties();
      // "ServiceHost" : Host to connect to
      p.setProperty("ServiceHost", serviceHost);         // set target
      // "ServicePort" : Port on which to connect
      p.setProperty("ServicePort", servicePort);         // set port
      // "default_record_syntax" : Default record syntax to ask for
      p.setProperty("default_record_syntax", recordType);
      // "record_syntax" : Default record syntax for the query.
      p.setProperty("record_syntax", recordType);
      // "default_element_set_name" : Default Element set to use (brief records)
      p.setProperty("default_element_set_name", elementSetName);
      // "service_short_name" : Short name for the target
      p.setProperty("service_short_name", "jisis");
      // "service_long_name", : Long name for the target
      p.setProperty("service_long_name", "jisis");

      /**
       * --------------------------------------------------------------
       * Authentication properties
       * "service_auth_type" : What kind of authentication to use
       *                         (0=none,1=anonymous,2=open string,3=idpass)
       *    "service_user_principal" : username or open string
       *    "service_user_group" : group
       *    "service_user_credentials" : password
       * --------------------------------------------------------------
       
       */
      if (password != null && password.length()>0) {
         p.setProperty("service_auth_type", "3");
         p.put("service_user_principal",userName);
	 p.put("service_user_credentials",password);

      }

      //p.setProperty("charset", "charset_utf8");

      search.init(p);
     


   }

   public Searchable getConnection() {
      return search;
   }

   public String getDatabase() {
      return dataBase;
   }
}
