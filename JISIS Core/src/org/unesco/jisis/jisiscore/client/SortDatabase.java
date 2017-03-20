/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisiscore.client;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.*;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.index.ParsedFstEntry;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.sorting.BalancedMergeSort;
import org.unesco.jisis.corelib.sorting.JisisSortRecordComparator;
import org.unesco.jisis.corelib.sorting.JisisSortRecordInfo;
import org.unesco.jisis.corelib.util.StringUtils;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;

/**
 *
 * @author jc_dauphin
 */




public class SortDatabase implements Runnable {
   /** The database involved */
   private IDatabase db_;

   /** The sort key info from the dialog */
   private KeyInfo[] keys_;

   /** The FieldSelectionTable objects built from the KeyInfo fst data */
   private FieldSelectionTable[] fst_ = null;    // Array of FSTs for the sorting keys

   /** The Parsed FST entries, one per sort key */
   private ParsedSortKey[]     parsedSortKeys_;
   protected MfnRange[]        mfnRange_   = null;
   private int                 errorCount_ = 0;
   private boolean             pftValid_;
   private File                tempFile_ = null;

   private BufferedWriter out_       = null;
   private Date           start_    = null;
   private long           timeDB_    = 0;
   private long           timeExec_  = 0;
   private long           timeWrite_ = 0;
   private Date           startTime_, endTime_;

   private CancellableProgress cancellable_;
   public static final int    HEADING_PROCESSING_INDICATOR0 = 0;
   public static final int    HEADING_PROCESSING_INDICATOR1 = 1;
   public static final int    HEADING_PROCESSING_INDICATOR2 = 2;
   public static final int    HEADING_PROCESSING_INDICATOR3 = 3;
   
     private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SortDatabase.class);

   /**
    * Constructor
    * @param db
    * @param keys
    */
   public SortDatabase(IDatabase db, KeyInfo[] keys) {
      db_   = db;
      keys_ = keys;
      int nKeys = keys_.length;
      if (nKeys == 0) {
         return;
      }
      errorCount_ = 0;
      pftValid_   = true;
      // Array of FSTs
      fst_ = new FieldSelectionTable[nKeys];
      // Array of Parsed FSTs
      parsedSortKeys_ = new ParsedSortKey[nKeys];
      List<ParsedFstEntry> parsedSortFstEntries;
       for (int i = 0; i < nKeys; i++) {    // For each sort key
           // Get or Build the FieldSelectionTable object
           String s = keys_[i].fst_;
           if (s.startsWith("@")) {
               // A predefined FST name is preceded by an at sign "@"
               String fstName = s.substring(1);
               try {
                   fst_[i] = db_.getFst(fstName);
               } catch (DbException ex) {
                   errorCount_++;
                   LOGGER.error("Error when getting FST [{}]", fstName, ex);
                   GuiGlobal.outputErr("Error when getting FST [" + fstName + "] " + ex);
               }
           } else {
               // FST is defined in the string
               if ((fst_[i] = parseString(s)) == null) {
                   // Error
                   errorCount_++;
                   LOGGER.error("Error when parsing FST data part of sort key (Field ID technique Format) [{}]", s);
                   GuiGlobal.outputErr("Error when parsing FST data part of sort key [" + s + "]");
                   return;
               }
           }
           // Parse the format part of the FST
           int nEntries = fst_[i].getEntriesCount();
           parsedSortFstEntries = new ArrayList<>();
           for (int j = 0; j < nEntries; j++) {
               FieldSelectionTable.FstEntry entry = fst_[i].getEntryByIndex(j);
               int tag = entry.getTag();
               String name = entry.getName();
               String format = entry.getFormat();
               int technique = entry.getTechnique();
               try {

                   /**
                    * Parse the pft and build an intermediate language structure for execution
                    */
               // String s = String.format("| %5d | %1d | %s",tag, teq, pft);
                   // Global.output(s);
                   ParsedFstEntry parsedFstEntry = ParsedFstEntry.newParsedFstEntry(tag, name, technique,
                       format);
                   // ISISFormatter isisFormatter = ISISFormatter.getFormatter(format);
                   parsedSortFstEntries.add(parsedFstEntry);
                   pftValid_ = !parsedFstEntry.invalidState();
                   if (!pftValid_) {
                       errorCount_++;
                   }
               } catch (Exception e) {
                   parsedSortFstEntries.add(null);
                   pftValid_ = false;
                   errorCount_++;
               }
           }
           parsedSortKeys_[i] = new ParsedSortKey(keys_[i].length_, parsedSortFstEntries);
       }
      // Global.output("Number of parsing errors in the FST: "+errorCount_);
   }

   public int getErrorCount() {
      return errorCount_;
   }
   
   public ParsedSortKey[]  getParsedSortKey() {
      return parsedSortKeys_;
   }

   /**
    * This method parse the Sort Key FST data string for building a FeieldSelectionTable object
    * 
    * The string is first split into FST lines separated by* " + ", then each line is split into 3 parts:
    * field ID, indexing technique, and the extraction format. Finally the extraction format is parsed to
    * check the syntax.
    * 
    * A FST is either one entry or several entries separated by a "+" character
    * surrounded by spaces, i.e. " + ".
    * An entry is defined by a field identifier followed by by the indexing
    * technique, followed by the format and separated by at least a space.
    * @param s - The FST data part of the sort key   
    * @return  - A FieldSelectionTable object with 1 or more than one 1 FST entries
    *            or NULL in case they are parsing errors 
    */
    private FieldSelectionTable parseString(String s) {
       /**
        * zero or several spaces followed by + and followed by zero or several spaces
        * \s	A whitespace character: [ \t\n\x0B\f\r]
        */
        String[] lines = s.split("\\s*\\+\\s*");
        FieldSelectionTable fst = new FieldSelectionTable();
        boolean hasErrors = false;
       for (String line : lines) {
           /**
            * Split on one or several spaces
            */
           String[] tokens = line.split("\\s+");
           if (tokens.length < 3) {
               NotifyDescriptor d = new NotifyDescriptor.Message("We should have field ID, indexing Tech and format surrounded by space!" + "\nError on FST line:\n" + line);
               DialogDisplayer.getDefault().notify(d);
               hasErrors = true;
               continue;
           }
           int fieldID = 0;
           try {
               fieldID = Integer.parseInt(tokens[0]);
           } catch (NumberFormatException pe) {
               NotifyDescriptor d = new NotifyDescriptor.Message("Error on FST line, Invalid field ID:\n" + line);
               DialogDisplayer.getDefault().notify(d);
               hasErrors = true;
               continue;
           }
           int tech = 0;
           try {
               tech = Integer.parseInt(tokens[1]);
           } catch (NumberFormatException pe) {
               NotifyDescriptor d = new NotifyDescriptor.Message("Error on FST line, Invalid Technique:\n" + line);
               DialogDisplayer.getDefault().notify(d);
               hasErrors = true;
               continue;
           }
           String format = "";
           for (int j = 2; j < tokens.length; j++) {
               format += tokens[j];
               format += " ";
           }
           hasErrors = false;
           try {
               ISISFormatter il = ISISFormatter.getFormatter(format);
               if (il == null) {
                   GuiGlobal.output(ISISFormatter.getParsingError());
                   hasErrors = true;
               } else if (il.hasParsingError()) {
                   GuiGlobal.output(ISISFormatter.getParsingError());
                   hasErrors = true;
               }
           } catch (Exception e) {
               NotifyDescriptor d = new NotifyDescriptor.Message("Error on FST line, Invalid Format:\n" + line);
               DialogDisplayer.getDefault().notify(d);
               hasErrors = true;
               continue;
           }
           FieldSelectionTable.FstEntry e = new FieldSelectionTable.FstEntry(fieldID, "", tech, format);
           try {
               fst.addEntryAlways(e);
           } catch (DbException ex) {
               Exceptions.printStackTrace(ex);
           }
       }
        return (hasErrors)
                ? null
                : fst;
    }

   public void setMfnRange(MfnRange[] mfnRange) {
      mfnRange_ = mfnRange;
   }

   static class CancellableProgress implements Cancellable {
      private boolean cancelled = false;

      @Override
      public boolean cancel() {
         cancelled = true;
         return true;
      }

      public boolean cancelRequested() {
         return cancelled;
      }
   }
   public File getTempFile() {
      return tempFile_;
   }
    private void writeLine(long mfn, String sCombination, String sortKey) throws IOException {

      
      endTime_ = new Date();
      timeExec_ += (endTime_.getTime() - startTime_.getTime());
      startTime_ = new Date();
      String sMfn = String.format("%09d", mfn);
      out_.write(sMfn);
    
      out_.write(sCombination);
      out_.write(sortKey);
     
      out_.newLine();
      endTime_ = new Date();
      timeWrite_ += (endTime_.getTime() - startTime_.getTime());
   }
   private void writeLine(String sMfn, String sTermIndex, String s, int len) throws IOException {

      if (s.length() > len) {
         // Truncate                }
         s = s.substring(0, len);
      } else {
         // padd with blanks
         s = StringUtils.padRight(s, len);
      }
      endTime_ = new Date();
      timeExec_ += (endTime_.getTime() - startTime_.getTime());
      startTime_ = new Date();
      out_.write(sMfn);
      out_.write(sTermIndex);
      out_.write(s);
      out_.newLine();
      endTime_ = new Date();
      timeWrite_ += (endTime_.getTime() - startTime_.getTime());
   }
   /**
    * Extract the sort keys for this record using the FST format
    * @param record
    * @return - List of List of Lucene Fields extracted for this
    *           particular record. One list for each sort key
    */
   private synchronized List<List<Field>> extractSortKeys(IRecord record) {

      List<List<Field>> luceneFieldList = new ArrayList<List<Field>>();
     
      int sortKeyCount = parsedSortKeys_.length;
      // Loop on the number of sort keys
      for (int k = 0; k < sortKeyCount; k++) {      
         /**
          * The List of FST parsed entries for this sort key . We have several
          * entries if the FST format has several lines separated by "+"
          */
         List<ParsedFstEntry> parsedSortFstEntries = parsedSortKeys_[k].parsedSortFstEntries_;

         // Loop on the FST entries for this sort key
         List<org.apache.lucene.document.Field> fieldList =
                 new ArrayList<org.apache.lucene.document.Field>();
          for (ParsedFstEntry parsedSortFstEntry : parsedSortFstEntries) {
              /**
               * We re-use the index parsing - extract from the record the terms produced by the FST entry. We
               * get back Lucene fields !!
               */
              Field[] fields = parsedSortFstEntry.extract(db_, record);
              if (fields != null && fields.length > 0) {
                  fieldList.addAll(Arrays.asList(fields));
              }

          }
          /**
           * If first sort key is absent, record will be skpped We create a dummy field for other sort keys
           */
          if (k > 0 && fieldList.isEmpty()) {
              /**
               * No data was extracted, fields may be absent
               */
              // Add a dummy blank field
              Field field = new org.apache.lucene.document.Field("dummy", "", new FieldType(TextField.TYPE_STORED));
              fieldList.add(field);
          }
          luceneFieldList.add(fieldList);
      }
      return luceneFieldList;
   }
   
   private synchronized void removeFilingInformation(List<List<Field>> luceneFieldList) {
      for (List<Field> list : luceneFieldList) {
         for (Field field : list) {
            String value = field.stringValue();
            // Get rid of <...>
            value = value.replaceAll("\\<.*?>","");
            // Left Trim
            value = value.replaceAll("^\\s+", "");
            // Convert to Upper Case
            Locale locale = Locale.getDefault();
            value = value.toUpperCase(locale);
            field.setStringValue(value);
         }
      }
   }
   /**
    * Make the sort key combinations and write them on the Hit sort File
    * @param mfn
    * @param luceneFieldList - List of List of Lucene Fields extracted for this
    * particular record. One list for each sort key
    * @throws IOException 
    */
    private synchronized void combineSortKeys(long mfn, List<List<Field>> luceneFieldList) throws IOException {

      int combination = 0;
       String sCombination = null;
      int sortKeyCount = parsedSortKeys_.length;
      List<Field> sortKeyFields1 = luceneFieldList.get(0);
      if (sortKeyFields1.isEmpty()) {
         // If no 1st key data, then skip this record
      } else {
         int headingProcessingIndicator = keys_[0].headingIndicator_;
         int len1 = keys_[0].length_;
         int n1 = (headingProcessingIndicator == HEADING_PROCESSING_INDICATOR0)
               || (headingProcessingIndicator == HEADING_PROCESSING_INDICATOR2) 
                 ? 1 : sortKeyFields1.size();         
         for (int j1 = 0; j1 < n1; j1++) {
            String key1 = sortKeyFields1.get(j1).stringValue();
            // Truncate or pad with blanks
            key1 = (key1.length() > len1) ? key1.substring(0, len1)
                    : StringUtils.padRight(key1, len1);
            if (sortKeyCount > 1) {
               //if more than 1 key combine with 2nd key
               List<Field> sortKeyFields2 = luceneFieldList.get(1);
               headingProcessingIndicator = keys_[1].headingIndicator_;
               int len2 = keys_[1].length_;
                int n2 = (headingProcessingIndicator == HEADING_PROCESSING_INDICATOR0)
                      || (headingProcessingIndicator == HEADING_PROCESSING_INDICATOR2) 
                 ? 1 : sortKeyFields2.size();    
              
               for (int j2 = 0; j2 < n2; j2++) {
                  String key2 = sortKeyFields2.get(j2).stringValue();
                  // Truncate or pad with blanks
                  key2 = (key2.length() > len2)? key2.substring(0, len2)
                          : StringUtils.padRight(key2, len2);
                  if (sortKeyCount > 2) {
                     // if more than 2 sort keys then combine with third key
                     List<Field> sortKeyFields3 = luceneFieldList.get(2);
                     headingProcessingIndicator = keys_[2].headingIndicator_;
                     int len3 = keys_[2].length_;
                     
                      int n3 = (headingProcessingIndicator == HEADING_PROCESSING_INDICATOR0)
                            || (headingProcessingIndicator == HEADING_PROCESSING_INDICATOR2) 
                                ? 1 : sortKeyFields3.size();    
                     for (int j3 = 0; j3 < n3; j3++) {
                        String key3 = sortKeyFields3.get(j3).stringValue();
                         // Truncate or pad with blanks
                        key3 = (key3.length() > len3) ? key3.substring(0, len3)
                                : StringUtils.padRight(key3, len3);
                       
                        if (sortKeyCount > 3) {
                           // If more than 3 sort keys, then combine with last key
                           List<Field> sortKeyFields4 = luceneFieldList.get(3);
                           headingProcessingIndicator = keys_[3].headingIndicator_;
                           int len4 = keys_[3].length_;
                            int n4 = (headingProcessingIndicator == HEADING_PROCESSING_INDICATOR0)
                                  || (headingProcessingIndicator == HEADING_PROCESSING_INDICATOR2) 
                                  ? 1 : sortKeyFields4.size();    
                         
                           for (int j4 = 0; j4 < n4; j4++) {
                              String key4 = sortKeyFields4.get(j4).stringValue();
                              // Truncate or pad with blanks
                              key4 = (key4.length() > len4) ? key4.substring(0, len4)
                                      : StringUtils.padRight(key4, len4);
                              // Four sort keys
                              StringBuilder sb = new StringBuilder();
                              sb.append(key1).append(key2).append(key3).append(key4);
                              sCombination = String.format("%03d%03d%03d%03d", j1,j2,j3,j4);
                              writeLine(mfn, sCombination, sb.toString());
                              combination++;
                           } // j4 loop
                        } else { // sortKeyCount = 3
                           // Three sort keys
                           sCombination = String.format("%03d%03d%03d   ", j1,j2,j3);
                           StringBuilder sb = new StringBuilder();
                           sb.append(key1).append(key2).append(key3);
                           writeLine(mfn, sCombination, sb.toString());
                           combination++;
                        }

                     }       // j3 loop
                  } else {          // sortKeyCount = 2 
                     // Two sort keys
                     sCombination = String.format("%03d%03d      ", j1,j2);
                     StringBuilder sb = new StringBuilder();
                     sb.append(key1).append(key2);
                     writeLine(mfn, sCombination, sb.toString());
                     combination++;
                  }
               }             // j2 loop
            } else {              // sortKeyCount = 1
               // Only one key
               sCombination = String.format("%03d         ", j1);
               writeLine(mfn, sCombination, key1);
               combination++;
            }
         }                   // j1 loop
      }
   }
   /**
    * Create the file to be sorted
    * @throws IOException 
    */
   private synchronized void prepareSorting() throws IOException {
      cancellable_ = new CancellableProgress();
      final ProgressHandle progress = ProgressHandleFactory.createHandle("Extracting Data...",
                                         cancellable_);
      progress.start();
      progress.switchToIndeterminate();
      out_       = null;
      start_     = new Date();
      timeDB_    = 0;
      timeExec_  = 0;
      timeWrite_ = 0;
      
      try {
         // Create temporary file.
         File   dir    = new File(Global.getClientWorkPath());
         String dbName = db_.getDatabaseName();
         // prefix of temp file should be at least 3 characters
         if (dbName.length() < 3) {
            dbName = StringUtils.paddingString(dbName, 3, 'X', false);
         }
         tempFile_ = File.createTempFile(dbName, Global.HIT_SORT_FILE_EXT, dir);
         // Delete temp file when program exits.
         // tempFile.deleteOnExit();
         out_ = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile_), "UTF8"));
         // Loop on the mfn ranges
         for (int i = 0; i < mfnRange_.length; i++) {
            long first = mfnRange_[i].getFirst();
            long last  = mfnRange_[i].getLast();
            // Loop on the mfns for this range
            for (long j = first; j <= last; j++) {
               if (cancellable_.cancelRequested()) {
                  progress.finish();
                  break;
               }
               startTime_ = new Date();
               IRecord iRec = db_.getRecordCursor(j);
               if (iRec == null) {
                   continue;
               }
               endTime_ = new Date();
               timeDB_  += (endTime_.getTime() - startTime_.getTime());
               if (iRec == null) {
                  continue;
               }
               long mfn = iRec.getMfn();            
               /**
                * Phase 1
                * -------
                * Store in a List of List all the tems extracted for all the
                * sort keys
                */
               List<List<Field>> luceneFieldList = extractSortKeys(iRec);
               
               /**
                * Remove filing information <...>
                */
               removeFilingInformation(luceneFieldList);
              
               /**
                * Phase 2:
                * -------
                * 
                * Generate the hit sort file combinations
                * 
                */
                combineSortKeys(mfn, luceneFieldList);
              
               progress.setDisplayName("Extracting Data MFN:" + Long.toString(iRec.getMfn()));
            }
         }
      } catch (Exception e) {
         System.err.println("Error writing to file");
         Exceptions.printStackTrace(e);
      } finally {
         if (out_ != null) {
            out_.close();
         }
         Date end = new Date();
         GuiGlobal.output(Long.toString(timeDB_) + " milliseconds to read DB");
         GuiGlobal.output(Long.toString(timeExec_) + " milliseconds to interpret FST");
         GuiGlobal.output(Long.toString(timeWrite_) + " milliseconds to write temp");
         GuiGlobal.output(Long.toString(end.getTime() - start_.getTime())
                       + " milliseconds to prepare file to sort");
         progress.finish();
      }
   }

   public void run() {
      ProgressHandle progress = null;
      try {
         prepareSorting();
         if (cancellable_.cancelRequested()) {
            return;
         }
         JisisSortRecordInfo ri = new JisisSortRecordInfo(new JisisSortRecordComparator());
         cancellable_ = new CancellableProgress();
         progress     = ProgressHandleFactory.createHandle("Sorting Data...", cancellable_);
         progress.start();
         progress.switchToIndeterminate();
         BalancedMergeSort.execute(tempFile_.getAbsolutePath(), ri);
         NotifyDescriptor d = new NotifyDescriptor.Message("Sorting is done");
         DialogDisplayer.getDefault().notify(d);
      } catch (FileNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      } finally {
         progress.finish();
      }
     
   }
}
