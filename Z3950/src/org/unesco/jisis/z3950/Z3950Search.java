/*
 * Z3950Search.java
 *
 */
package org.unesco.jisis.z3950;

import java.util.*;
import com.k_int.IR.*;



public class Z3950Search {


   Z3950Connection conn = null;

   /**
   * It is *Essential* that the default_record_syntax and default_element_set_name properties
   * are set by the Searchable init method for this part of the framework to hang together properly!
   */
  public static final RecordFormatSpecification default_spec =
              new RecordFormatSpecification( new IndirectFormatProperty("default_record_syntax"),
                                             null,
                                             new IndirectFormatProperty("default_element_set_name"));
   /** Default record format, a full xml. */
   //new RecordFormatSpecification("xml","meta","f"));
   //new RecordFormatSpecification("html", null, "f");
   //RecordFormatSpecification brief_usmarc = new RecordFormatSpecification( "usmarc", null, "b" );

  /**
   * Constructor
   */
   public Z3950Search() {
   }

   public ArrayList getSearchResults(Hashtable searchProp) {

      ArrayList results = new ArrayList();
      try {

         String serviceHost = String.valueOf(searchProp.get("ServiceHost"));
         String servicePort = String.valueOf(searchProp.get("ServicePort"));
         String dataBase = String.valueOf(searchProp.get("database"));
         String userName = String.valueOf(searchProp.get("UserName"));
         String password = String.valueOf(searchProp.get("Password"));
         String recordType = String.valueOf(searchProp.get("RecordType"));
         String elementSetName = String.valueOf(searchProp.get("ElementSetName"));

         RecordFormatSpecification recordFormatSpecification =
                 new RecordFormatSpecification(recordType, null, elementSetName);

         conn = new Z3950Connection(searchProp);
         Searchable search = conn.getConnection();

         Z3950Query query = new Z3950Query(conn, searchProp);
         SearchTask searchTask = (SearchTask) search.createTask(query, null);
         searchTask.evaluate(50000);                // run query for < 30 secs
         System.out.println("Content-type: text/plain\n");
         System.err.println("Result: "
                 + searchTask.lookupPrivateStatusCode(searchTask.getPrivateTaskStatusCode()));


         InformationFragmentSource ifs = searchTask.getTaskResultSet();
         /****/
         int recordCount = ifs.getFragmentCount();
         System.out.println("=" + recordCount);
         int minimalCount = recordCount;
         if (minimalCount > 10) {
            minimalCount = 10;
         }
         /**
          * InformationFragment[] getFragment(int starting_fragment,
          *                                          int count,
          *                                          RecordFormatSpecification spec)
          *                            throws PresentException
          *
          * Position based range access to the result set. Implementation must
          * be 1 based: IE, First record in result set is 1 not 0.
          * Local mappings (e.g to vector) must account for this!

          */
         InformationFragment[] irr = ifs.getFragment(1, minimalCount, recordFormatSpecification);

         for (int i = 0; i < irr.length; i++) {
            results.add(irr[i]);
         }
         // Don't know why, but the 1st call to getFragment doesn't return the
         // requested number of records (which should be minimalCount)
         int start = irr.length + 1;
         for (int i = start; i < recordCount; i += minimalCount) {
            int count = Math.min(recordCount - i + 1, minimalCount);
            System.out.println("ifs.getFragment start=" + i + " count=" + count);
            irr = ifs.getFragment(i, count, recordFormatSpecification);
            System.out.println("irr.length=" + irr.length);
            for (int j = 0; j < irr.length; j++) {
               results.add(irr[j]);
            }
         }


         System.out.println("Number of records retrieved: " + results.size());


      } catch (Exception exception) {
         exception.printStackTrace();
      }
      return results;
   }
}
