import org.unesco.jisis.corelib.client.ClientDbProxy;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.StringOccurrence;
import org.unesco.jisis.corelib.record.Subfield;
import org.unesco.jisis.corelib.client.ConnectionNIO;

import org.unesco.jisis.corelib.common.IConnection;


classe GlobalChange {
  


   def process() {
   
     
      def username = "admin";
      def password = "admin";
      def port     = "1111";
      def hostname = "localhost";

      // Establish a connection to the server
      def connection_ = ConnectionNIO.connect(hostname, Integer.valueOf(port), username, password);
      
      // Create a Database object bind to this server     
      ClientDbProxy db_ = new ClientDbProxy(connection_)
      
      // Let's use DB ASFAEX defined on root DEF_HOME
      def dbHome = "DEF_HOME";
      def dbName = "ASFAEX"
      // Open the database   
      db_.getDatabase(dbHome, dbName)
      
      // Get first record   
      IRecord rec = db_.getFirst();
 
      // Iterate over the records     
      while (rec != null) {
         /// Perform Global Change
          // Get the Monographic Level Authors (tag 200)
          int nFields = rec.getFieldCount();
          for (int j=0; j<nFields; j++) {
              field = rec.getFieldByIndex(j);
              if (field != null) {
                 int itag = field.getTag();
                 int nocc = field.getOccurrenceCount();
                 for (int k=0; k<nocc; k++) {
                     String occ = field.getStringOccurrence(k);
                     // Right trim the occurrence
                     occ.replaceAll("\\s+$", "");
                     // Replace in field
                     field.setOccurrence(k, occ)
                    } // Loop on occurrences
               
             } if (field != null)
            
           } // Loop on fields
      
         
         rec = db_.getNext();
      }
      // Close 
     db_.close();
   }
   
  
  
  
}

   def change = GlobalChange()
   change.process()