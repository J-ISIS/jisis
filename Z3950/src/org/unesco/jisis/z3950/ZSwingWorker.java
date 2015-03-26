/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.z3950;

import com.k_int.IR.InformationFragment;
import com.k_int.IR.InformationFragmentSource;
import com.k_int.IR.PresentException;
import com.k_int.IR.RecordFormatSpecification;
import com.k_int.IR.SearchException;
import com.k_int.IR.SearchTask;
import com.k_int.IR.Searchable;
import com.k_int.IR.TimeoutExceededException;
import java.util.Hashtable;
import org.openide.util.Exceptions;

/**
 *
 * @author jcd
 *
 * A class to perform lengthy GUI-interacting tasks in a dedicated thread and
 * to  provide interim results while it is still working.
 */
// Class SwingWorker<T,V>
// T - the result type returned by this SwingWorker's doInBackground and get methods
// V - the type used for carrying out intermediate results by this SwingWorker's p
//     publish and process methods
// The first template argument, in this case, Integer, is what is returned by
// doInBackground(), and by get(). In this case it is the number of records
// retrieved
//
// The second template argument, in this case, Object, is what
// is published with the publish method. It is also the data
// type which is stored by the java.util.List that is the parameter
// for the process method, which recieves the information published
// by the publish method.
public class ZSwingWorker extends javax.swing.SwingWorker<Integer, Object> {

   final protected Hashtable searchProp;
   final private Z3950Connection conn;
   final private RecordFormatSpecification recordFormatSpecification;

   final private Informable informable;

   public ZSwingWorker(Hashtable searchProp, Informable informable) {

      this.searchProp = searchProp;
      this.informable = informable;

      this.conn = new Z3950Connection(searchProp);

      String serviceHost = String.valueOf(searchProp.get("ServiceHost"));
      String servicePort = String.valueOf(searchProp.get("ServicePort"));
      String dataBase = String.valueOf(searchProp.get("database"));
      String userName = String.valueOf(searchProp.get("UserName"));
      String password = String.valueOf(searchProp.get("Password"));
      String recordType = String.valueOf(searchProp.get("RecordType"));
      String elementSetName = String.valueOf(searchProp.get("ElementSetName"));
       String encoding = String.valueOf(searchProp.get("encoding"));

      recordFormatSpecification =
              new RecordFormatSpecification(recordType, null, elementSetName);

   }

   @Override

   /**
    * This is where all background activities should happen
    *
    * Returns items of the type given as the first template argument to the
    * SwingWorker class.
    */
   protected Integer doInBackground() {


      if (javax.swing.SwingUtilities.isEventDispatchThread()) {
         System.out.println(
                 "javax.swing.SwingUtilities.isEventDispatchThread()returned true.");
      }
      //ArrayList results = new ArrayList();
      int nRecords = 0;

      try {
         Searchable search = conn.getConnection();

         Z3950Query query = new Z3950Query(conn, searchProp);
         SearchTask searchTask = (SearchTask) search.createTask(query, null);
         try {
            searchTask.evaluate(50000); // run query for < 30 secs
         } catch (TimeoutExceededException ex) {
            Exceptions.printStackTrace(ex);
         } catch (SearchException ex) {
            Exceptions.printStackTrace(ex);
         }

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
         InformationFragment[] irr = null;

         try {
            irr = ifs.getFragment(1, minimalCount, recordFormatSpecification);
         } catch (PresentException ex) {
            Exceptions.printStackTrace(ex);
         }
         if (this.isCancelled()) return null;
         if (irr != null) {
            //results.addAll(Arrays.asList(irr));
            for (int j = 0; j < irr.length; j++) {
               publish(irr[j]);
            }
            nRecords += irr.length;
         }

         // Don't know why, but the 1st call to getFragment doesn't return the
         // requested number of records (which should be minimalCount)

         int start = irr.length + 1;
         for (int i = start; i < recordCount; i += minimalCount) {
            if (this.isCancelled()) return null;
            int count = Math.min(recordCount - i + 1, minimalCount);
            System.out.println("ifs.getFragment start=" + i + " count=" + count);
            irr = ifs.getFragment(i, count, recordFormatSpecification);
            System.out.println("irr.length=" + irr.length);
            //results.addAll(Arrays.asList(irr));
            for (int j = 0; j < irr.length; j++) {
               publish(irr[j]);
            }
             nRecords += irr.length;
         }
         System.out.println("Number of records retrieved: " + nRecords);


      } catch (Exception exception) {
         Exceptions.printStackTrace(exception);
      }
      return nRecords;
   }

// Successive calls to publish are coalesced into a java.util.List,
// This method is processing the java.util.List of items
// given as successive arguments to the publish method.
// which is what is received by process, which in this case, is used
// to update the result Table. Thus, the values passed to publish are
// records .
// Note that these calls are coalesced into a java.util.List.
// This list holds items of the type given as the
// second template parameter type to SwingWorker.
// Note that the get method below has nothing to do
// with the SwingWorker get method; it is the List's
// get method.
// This would be a good place to update a progress bar.
// Note, always use java.util.List here, or it will use the wrong list.
   @Override
   protected void process(java.util.List chunks) {
      if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
         System.out.println("javax.swing.SwingUtilities.isEventDispatchThread()returned false.");
      }
      // Pass the records retrieved to the caller
      informable.recordsRetrieved(chunks);
   }

   @Override
   protected void done() {
      System.out.println("doInBackground is complete");

      if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
         System.out.println("javax.swing.SwingUtilities.isEventDispatchThread()returned false.");
      }

      try {
         // Here, the SwingWorker's get method returns an item
         // of the same type as specified as the first type parameter
         // given to the SwingWorker class.
         Integer nRecords =  get();
         System.out.println("Number of records retrieved:"+nRecords);
      } catch (Exception e) {
         System.out.println("Caught an exception: " + e);
      }
   }
}
