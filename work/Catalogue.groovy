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

import java.awt.Color;
import java.io.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

class pdfCatalogue {
   def  bf = BaseFont.createFont (BaseFont.HELVETICA,
                     BaseFont.CP1252,
                     BaseFont.NOT_EMBEDDED);
   // Establish a title font for all record titles.                
   def titleFont = new Font (Font.HELVETICA, 18, Font.BOLD, 
                 new Color (0, 0, 128));
                 
   def h1Font = new Font(Font.HELVETICA, 12, Font.BOLD,
              new Color(0, 0, 128));


   def process() {
   
      // Create an instance of the Document class
      Document doc = new Document ();

      PdfWriter writer;
      writer = PdfWriter.getInstance (doc,
                   new FileOutputStream ("asfaex.pdf"));
      writer.setViewerPreferences (PdfWriter.PageModeUseOutlines);

      doc.open ();
            
      initDocument(doc, writer);
     
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
         // Create a record chapter.
    
         doc.add (recordChapter(rec));
         rec = db_.getNext();
      }
      // Close 
      doc.close ();
      writer.close();
   }
   
   def initDocument(doc, writer) {
  
      // Establish a footer that shows the page number between a pair dashes.
      HeaderFooter footer = new HeaderFooter (new Phrase ("- "), new Phrase (" -"));
      footer.setAlignment (Element.ALIGN_CENTER);
      doc.setFooter (footer);

      // Create the title page.
      PdfContentByte cb = writer.getDirectContent ();

      cb.rectangle (doc.left (), doc.bottom (), (float)(doc.right () - doc.left ()),
                    (float)(doc.top ()-doc.bottom ()));

      cb.stroke ();

      cb.beginText ();

      cb.setFontAndSize (bf, 34);

      cb.showTextAligned (PdfContentByte.ALIGN_CENTER, "ASFA",
             (float)((doc.right ()-doc.left ()) / 2 + doc.leftMargin ()),
             (float)((doc.top ()-doc.bottom ()) / 2 + doc.topMargin ()),
             0);
            
      cb.setFontAndSize (bf, 12);
      cb.showTextAligned (PdfContentByte.ALIGN_CENTER,"The Aquatic Sciences and Fisheries Abstracts (ASFA) Bibliographic Database",
             (float)((doc.right ()-doc.left ()) / 2 + doc.leftMargin ()),
             (float)((doc.top ()-doc.bottom ()) / 2 + doc.topMargin ()-18),
             0);
           
      cb.endText ();

      // Create the Introduction chapter.

      Paragraph title = new Paragraph ("Introduction", titleFont);
      title.setAlignment (Element.ALIGN_CENTER);
      title.setSpacingAfter (18.0f);

      Chapter chapter = new Chapter (title, 0);
      chapter.setNumberDepth (0);

      Paragraph p = new Paragraph ("The Aquatic Sciences and Fisheries Abstracts (ASFA) Bibliographic Database is the" +
                                   "principal information product produced through the cooperative efforts of the international" +
                                   "network of ASFA Partners (http://www.fao.org/fishery/asfa/1,1/en) and FAO. " +
                                   "The database contains more than 1 million bibliographic references (or records) to the world's" +
                                   "aquatic science literature accessioned since 1971.");

      p.setAlignment (Element.ALIGN_JUSTIFIED);
      chapter.add (p);

      doc.add (chapter);
  
   }
  
   def recordChapter (rec) {
       // Create a record chapter.
      Paragraph title = new Paragraph ("Record "+rec.getMfn(), titleFont);
      title.setAlignment (Element.ALIGN_CENTER);
      title.setSpacingAfter (18.0f);
      Chapter chapter = new Chapter (title, 1);
      chapter.setNumberDepth (0);
      chapter.setBookmarkOpen (false);
      chapter.setBookmarkTitle (("Record "+rec.getMfn()));

      // Get the English Title (tag 220) 
      IField field =  rec.getField(220);
      chapter.add (new Paragraph ("English Title:", h1Font));
      Paragraph p = new Paragraph (field.getStringFieldValue());
      p.setAlignment (Element.ALIGN_JUSTIFIED);
      chapter.add (p);

      // Get the Original Title (tag 224) 
      field =  rec.getField(224);
      chapter.add (new Paragraph ("Original Title:", h1Font));
      p = new Paragraph (field.getStringFieldValue());
      p.setAlignment (Element.ALIGN_JUSTIFIED);
      chapter.add (p);
     
      // Get the Serial Title (tag 324) 
      field =  rec.getField(324);
      chapter.add (new Paragraph ("Serial Title:", h1Font));
      p = new Paragraph (field.getStringFieldValue());
      p.setAlignment (Element.ALIGN_JUSTIFIED);
      chapter.add (p);

      // Get the Abstact (tag 700) 
      field =  rec.getField(700);
      chapter.add (new Paragraph ("Abstract:", h1Font));
      p = new Paragraph (field.getStringFieldValue());
      p.setAlignment (Element.ALIGN_JUSTIFIED);
      chapter.add (p);
/*
   Image image = Image.getInstance ("mercury.gif");
   image.setAlignment (Image.ALIGN_MIDDLE);
   chapter.add (image);
 */
      // Get the Monographic Level Authors (tag 200)
      field = rec.getField(200);
      if (field != null) {
     
         int nocc = field.getOccurrenceCount();
         if (nocc>0) {
            chapter.add (new Paragraph ("Monographic Level Authors:", h1Font));
            List list = new List (false, 30);
            for (int i=0; i<nocc; i++) {
               list.add (new ListItem (field.getStringOccurrence(i)));
            }
            chapter.add (list);
         }
      }
       
      // Get the Corporate Authors (tag 210)
      field = rec.getField(210);
      if (field != null) {
         // A field has at least one occurrence
         int nocc = field.getOccurrenceCount();
         if (nocc>0) {
            chapter.add (new Paragraph ("Corporate Authors:", h1Font));
          
            List list = new List (false, 30);
            for (int i=0; i<nocc; i++) {
               StringOccurrence occ = field.getOccurrence(i);
               Subfield[] subfields = occ.getSubfields();
               for (int j=0; j<subfields.length; j++) {
            
                  list.add (new ListItem (subfields[j].getData()));
               }
            }
            chapter.add (list);
         }
     
      }
      return chapter;

   }
}

   def catalogue = new pdfCatalogue()
   catalogue.process()