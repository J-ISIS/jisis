import org.unesco.jisis.corelib.common.IConnection
import org.unesco.jisis.corelib.client.ConnectionNIO
import org.unesco.jisis.corelib.client.ClientDbProxy
import org.unesco.jisis.corelib.common.IDatabase
import org.unesco.jisis.corelib.record.IRecord
import org.unesco.jisis.corelib.record.IField
import org.unesco.jisis.corelib.record.StringOccurrence
import org.unesco.jisis.corelib.record.Subfield



import com.lowagie.text.*
import com.lowagie.text.pdf.*


def connect() {
    println("MoreData : Connect\n");

    Properties props = new Properties()
    File propsFile = new File('/opt/jisis_suite/work/moredata.properties');
    props.load(propsFile.newDataInputStream())

    username = props.getProperty('username') ;
    password =  props.getProperty('password');
    port     = props.getProperty('port');
    hostname = props.getProperty('hostname') ;
    stringToFind=props.getProperty('stringToFind') ;
    stringToReplace=props.getProperty('stringToReplace') ;

    // Establish a connection to the server
    def connection = ConnectionNIO.connect(hostname, Integer.valueOf(port), username, password);

    // Create a Database object bind to this server
    ClientDbProxy db = new ClientDbProxy(connection)

    dbHome = "DEF_HOME";
    dbName = "teste";

    // Open the database
    db.getDatabase(dbHome,dbName);

    /* Get first record */
    IRecord rec = db.getFirst();
    def existReplace = false;
	
	while (rec != null) {
        int nfields = rec.getFieldCount();
        for (int i=0; i<=nfields-1; i++) {

           
     // Get the ieme field
     IField fld = rec.getFieldByIndex(i);

      // Get the number of occurrences
      int nocc = fld.getOccurrenceCount();
      if (nocc>0) {
         for (j=0; j<nocc; j++) {
            // Get the jeme occurrence of the ieme field
            StringOccurrence occ = fld.getOccurrence(j);
             println 'MoreData : Get the <'+j+'> occurrence of the <'+i+'> field.';

             Subfield[] subfields = occ.getSubfields();

              // Iterate over the subfields
              for (int k=0; k<subfields.length; k++) {
                 Subfield sf = subfields[k];
                 // Get subfield code
                 String subfieldCode = sf.getSubfieldCode();
                 // Get subfield data

                 String stringOrigem = sf.getData();

                 if (stringOrigem.contains(stringToFind)){
                    stringDestino = stringOrigem.replaceAll(stringToFind, stringToReplace);
                    //sf.setData(stringDestino);
                  
					// Subfield separator could be set in a variable
					occ.setSubfieldOccurrence("^"+subfieldCode, j, stringDestino);
                    existReplace = true;

                 }
            }
        }
     }
   }
   
   if(existReplace){
        db.updateRecord(rec);
        existReplace = false;
    }
    rec = db.getNext();
}
  /* Close the database */
    db.close();
}

connect();



