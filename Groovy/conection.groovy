import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.ConnectionPool;
import org.unesco.jisis.jisiscore.common.Global;
import org.unesco.jisis.jisiscore.common.IDatabase;
import org.unesco.jisis.jisiscore.common.IRecord;
import org.unesco.jisis.jisiscore.client.nio.ConnectionNIO;


import org.unesco.jisis.jisiscore.client.IConnection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.NotifyDescriptor;

def connect() {
      logger_      = Logger.getLogger(ConnectionNIO.class.getName());
      Global.setLogger(logger_);
      username = "admin";
      password = "admin";
      port     = "1111";
      hostname = "localhost";

      def connection_ = new ConnectionNIO(hostname, Integer.valueOf(port), username, password);
      
      ConnectionPool.addConnection(connection_);
      def dbHomes_    = connection_.getDbHomes();
      
      dbNames_    = new Vector<Vector>();

         System.out.println("dbHomes_.length=" + dbHomes_.length);

         for (int i = 0; i < dbHomes_.length; i++) {
            System.out.println("i=" + i);

            Vector v = (Vector) connection_.getDbNames(dbHomes_[i]);
System.out.println("i=" + i);
            dbNames_.add(v);
         }
        

        
   }
   connect();