
import org.unesco.jisis.corelib.common.IConnection
import org.unesco.jisis.corelib.client.ConnectionNIO
import org.unesco.jisis.corelib.client.ClientDbProxy
import org.unesco.jisis.corelib.common.IDatabase
import org.unesco.jisis.corelib.record.IRecord
import org.unesco.jisis.corelib.record.IField
import org.unesco.jisis.corelib.record.StringOccurrence
import org.unesco.jisis.corelib.record.Subfield

def snippet1() {
// Initialize the server parameters
  username = "admin";
  password = "admin";
  port     = "1111";
  hostname = "localhost";
  // Establish a connection to the server
  def connection =  ConnectionNIO.connect(hostname,
              Integer.valueOf(port), username, password);   
  // Create a Database object bind to this server     
  ClientDbProxy db = new ClientDbProxy(connection)
  // Let's use DB ASFAEX on root defined by DEF_HOME
  dbHome = "DEF_HOME";
  dbName = "ASFAEX"
  // Open the database   
  db.getDatabase(dbHome, dbName)
  // Get first record   
  IRecord rec = db.getFirst();
  // Iterate over the records in the database until nomore
  while (rec != null) {
     // Process the record->
     println '\n========Record MFN: '+rec.getMfn()+' ==========='
     IField field = rec.getField(700);
     System.out.println(field.getFieldValue());
     // ….. 
      // Get the next sequential record in the mfn order
      rec = db.getNext();
   }
   
      // Close the database
   db.close();

}
snippet1();