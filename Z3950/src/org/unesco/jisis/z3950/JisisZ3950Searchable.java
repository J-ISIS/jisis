/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.z3950;

import com.k_int.IR.IRQuery;
import com.k_int.IR.SearchTask;
import java.util.Observer;
import java.util.Properties;


/**
 *
 * @author jcd
 */
public class JisisZ3950Searchable implements com.k_int.IR.Searchable {

   /** Standard logging instance for class */
//    private static final Logger LOGGER = Logger.getLogger(
//            "org.vfny.geoserver.zserver");
    private Properties properties = null;

    /**
     * no argument constructo.
     */
    public JisisZ3950Searchable() {
//        LOGGER.finer("created GeoSearchable object");
    }

    /**
     * inits the server with these properties.
     *
     * @param p the properties to init with.
     */
   @Override
    public void init(Properties p) {
        this.properties = p;
    }

    /**
     * destroy the searchable object. Shut down the searchable object
     * entirely. Release all held resources, make the object ready for  GC.
     * Try to release in here instead of on finalize.
     */
   @Override
    public void destroy() {
    }

    /**
     * Provide information about the type of Searchable  object this
     * realisation is
     *
     * @return the type of searchable object.
     */
   @Override
    public int getManagerType() {
        return com.k_int.IR.Searchable.SPECIFIC_SOURCE;
    }







 /**
     * Create a SearchTask. Evaluate the query with the Tasks evaluate method.
     *
     * @param q The query to get results for.
     * @param user_data not currently used, needed to implement interface.
     *
     * @return the search task with q as the query.
     */
   @Override
    public SearchTask createTask(IRQuery q, Object user_data) {
        return this.createTask(q, user_data, null);
    }

    /**
     * Create the search task.   Evaluate the query with  the Tasks evaluate
     * method.
     *
     * @param q The query to get results for.
     * @param user_data not used (use null).
     * @param observers not implemented (use null).
     *
     * @return the search task with q as the query.
     */
    public SearchTask createTask(IRQuery q, Object user_data,
        Observer[] observers) {
        /* Implementation notes: If there is a need for user_data,
         * then create a new constructor for GeoSearchTask that uses it.
         * As for observers, check out SearchTask, which GeoSearchTask
         * extends. */

        SearchTask retval = new JisisSearchTask(this, q);

        return retval;
    }

    /**
     * gets the properties of the server.
     *
     * @return a property class holding the server's information.
     */
    public Properties getServerProps() {
        return properties;
    }

}
