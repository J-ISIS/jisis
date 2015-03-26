/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.z3950;

import com.k_int.IR.IRQuery;
import com.k_int.IR.InformationFragmentSource;
import com.k_int.IR.SearchException;
import com.k_int.IR.SearchTask;
import com.k_int.IR.TimeoutExceededException;
import java.util.Properties;


/**
 *
 * @author jcd
 */
public class JisisSearchTask extends SearchTask {
    /** The jzkit representation of the query to evaluate */
    private IRQuery q;

    /** The properties passed into to start the server */
    private Properties serverProps;

    /**
     * Constructer to create a search task.
     *
     * @param source
     *            The class that created this task.
     * @param q
     *            the query to evaluate.
     */
    public JisisSearchTask(JisisZ3950Searchable source, IRQuery q) {
        this.q = q;

        // REVISIT: We should get rid of these Server Props, and make
        // a singleton that holds the attribute map, the databases, and
        // other configuration information. Could be good to have it
        // similar to geoserver's ConfigInfo, and geoserver's config could
        // initialize zserver's configuration information, instead of
        // using these props files as we do now. Would be good to do this
        // the same time that we get rid of GeoSearchable.
        this.serverProps = source.getServerProps();

        // String attrMapFile = serverProps.getProperty("fieldmap");
        //attrMap = GeoProfile.getUseAttrMap(); // getAttrMap(attrMapFile);
    }


   @Override
   /**
   * Evaluate the query, waiting at most timeout milliseconds, returning the
   * search status. InformationFragmentSource object should be used to check
   * the final number of result records.
   * @return int - Task Status Code.
   */

   public int evaluate(int timeout) throws TimeoutExceededException, SearchException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   /** getTaskResultSet. Search tasks delagate the responsibility of managing a result set to an instance
   * of the InformationFragmentSource. Often, the SearchTask will implement InformationFragmentSource
   * itself, and return (this) as the realisation of the getTaskResultSet method. Other SearchTasks
   * may use some cache managing FragmentSource to wrapper the source result set and return that object.
   * This method returns a producer object that is used by consumers such as the result set enumeration
   * to list all the records found by a search task.
   */

   public InformationFragmentSource getTaskResultSet() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

}
