/*
 * UserDB.java
 *
 * Created on June 21, 2006, 9:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.jisiscore;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import java.io.File;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.UserInfo;
import org.unesco.jisis.corelib.users.UserDbHandle;

/**
 *
 * @author rustam
 */
public class UserDB {
//    
//    private static ArrayList dbs_ = new ArrayList();
//    private static int defaultDb_ = 0;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JavaISIS.class);
  
    
//    public static void addDatabase(RemoteDatabase db) {
//        dbs_.add(db);
//        setDefaultDatabase(db);
//    }
//    
//    public static ArrayList getDatabases() {
//        return dbs_;
//    }
//    
//    public static void setDefaultDatabase(RemoteDatabase db) {
//        defaultDb_ = dbs_.indexOf(db);
//    }
//    
//    public static RemoteDatabase getDefaultDatabase() throws DefaultDBNotFoundException {
//        RemoteDatabase db = null;
//        
//        if (dbs_.size()>0) {
//            db = (RemoteDatabase) dbs_.get(defaultDb_);
//        } else {
//            //throw new DefaultDBNotFoundException();
//        }
//        return db;
//    }
//    
//    public static void closeDatabase(RemoteDatabase db) throws DbException {
//        int dbIndex = dbs_.indexOf(db);
//        dbs_.remove(dbIndex);
//        defaultDb_ = 0;
//        db.close();
//    }
    
    public static void createUserDatabase() throws Exception {
       LOGGER.info("Entering UserDB createUserDatabase()");
       
       
        File dataDirectory = new File("userspace/");
        
        com.sleepycat.je.Database db = null;
        /* Open a transactional Oracle Berkeley DB Environment. */
        
        String dbName = "users";
//
        File dbDir = new File(dataDirectory,dbName);
//
//		dbDir.mkdir();
//
//		File dbCtlFile = new File(dbDir,"i.dbc");
//
//		dbCtlFile.createNewFile();
//
//		//8. create data folder
//
        File idataFile = new File(dbDir,"idata");
//
//
//		idataFile.mkdir();
        
        //2. create database file
        
        EnvironmentConfig envConfig =new EnvironmentConfig();
        
        envConfig.setAllowCreate(true);
        
        envConfig.setReadOnly(false);
        
        envConfig.setTransactional(true);
        
        Environment env = new Environment(idataFile,envConfig);
        
        DatabaseConfig dbConfig = new DatabaseConfig();
        
        dbConfig.setAllowCreate(true);
        
        dbConfig.setReadOnly(false);
        
        dbConfig.setSortedDuplicates(false);
        
        dbConfig.setTransactional(true);
        
        db = env.openDatabase(null,"config",dbConfig);
        
        DatabaseEntry theKey = new DatabaseEntry();
        
        DatabaseEntry theData = new DatabaseEntry();
        
        theKey.setData("dbname".getBytes());
        
        theData.setData(dbName.getBytes());
        
        db.put(null,theKey,theData);
        
        UserInfo user = new UserInfo("admin","admin", null);
        user.setIsAdmin(true);
        try {
            UserDbHandle userDbHandler = new UserDbHandle();
            userDbHandler.updateUser(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Users count="+db.count());
        db.close();

    }
    
    
    
}
