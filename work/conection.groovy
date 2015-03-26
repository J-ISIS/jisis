import org.unesco.jisis.corelib.client.ClientDbProxy;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.client.ConnectionNIO;

import org.unesco.jisis.corelib.common.IConnection;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;


def connect() {
     
      username = "admin";
      password = "admin";
      port     = "1111";
      hostname = "localhost";

      // Establish a connection to the server
      def connection_ = ConnectionNIO.connect(hostname, Integer.valueOf(port), username, password);
      
      // Get the dbHomes defined on this server
      def dbHomes_    = connection_.getDbHomes();
      
      // Get the DB names for these dbHomes
      dbNames_    = new Vector<Vector>();
      System.out.println("dbHomes_.length=" + dbHomes_.length);

      for (int i = 0; i < dbHomes_.length; i++) {
          System.out.println("i=" + i);

          Vector v = (Vector) connection_.getDbNames(dbHomes_[i]);

          dbNames_.add(v);
      }
    
      // Create a Database object bind to this server     
      ClientDbProxy db_ = new ClientDbProxy(connection_)
      // Let's use DB ASFAEX defined on root DEF_HOME
      dbHome = "DEF_HOME";
      dbName = "ASFAEX"
      // Open the database   
      db_.getDatabase(dbHome, dbName)
      
      // Get first record   
      IRecord rec = db_.getFirst();
      println(rec.toString());
      pdfFirstRecord(rec);
      result = rec.toString();
         
      
   }
   def pdfFirstRecord(IRecord record) {
      Document document = new Document();
      PdfWriter.getInstance (document, new FileOutputStream ("FirstRecord.pdf"));
      document.open ();
      
      Paragraph p = new Paragraph (record.toString());
      p.setAlignment (Element.ALIGN_JUSTIFIED);
      document.add (p);

      document.close ();
   }

   connect();