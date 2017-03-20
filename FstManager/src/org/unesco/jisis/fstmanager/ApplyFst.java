/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.fstmanager;

/**
 *
 * @author jc_dauphin
 */
//~--- JDK imports ------------------------------------------------------------
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.index.ParsedFstEntry;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;

/**
 * DocBuilder object is responsible of building Lucene Documents, it creates a Document object, adds fields to
 * the document according to the entries specified in the FST.
 *
 * A document is a sequence of fields. A field is a named sequence of terms. A term is a string. The same
 * string in two different fields is considered a different term. Thus terms are represented as a pair of
 * strings, the first naming the field, and the second naming text within the field.
 */
public class ApplyFst {

    private final FieldSelectionTable fst_;
    /**
     * The Parsed FST entries
     */
    private ParsedFstEntry[] parsedFstEntries_  = {};
    private ISISFormatter[]     pftIL_    = {};
    private boolean fstIsValid_ = true;
    private final IDatabase db_;
    private int errorCount_ = 0;
    private final boolean isMaster_;

    /**
     * Constructor
     *
     * @param fst - Field Selection Table
     */
    public ApplyFst(final IDatabase db, final FieldSelectionTable fst, final boolean isMaster) {
       
        fst_ = fst;
        db_ = db;
        isMaster_ = isMaster;
       
        if (isMaster_) {

            int entryCount = fst.getEntriesCount();
            parsedFstEntries_ = new ParsedFstEntry[entryCount];
            errorCount_ = 0;
            for (int i = 0; i < entryCount; i++) {
                FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                int tag = entry.getTag();
                String name = entry.getName();
                String format = entry.getFormat();
                int teq = entry.getTechnique();
                parsedFstEntries_[i] = ParsedFstEntry.newParsedFstEntry(tag, name, teq, format);
                if (!parsedFstEntries_[i].IsPftValid()) {
                    fstIsValid_ = false;
                    errorCount_++;
                }
            }
            GuiGlobal.output("Number of parsing errors in the FST: " + errorCount_);
        } else {
            pftIL_ = parseFST(db, fst);

        }
    }

    public boolean fstIsValid() {
        return fstIsValid_;
    }

    /**
     * Create one Lucene Document for the record and add it to the index. - The MFN is stored as a Field in
     * the Document so that we can later access the matching records. - For other fields, we use the FST Field
     * ID and FST format to build the fields.
     *
     * @param record - record to be used for creating the Lucene Document
     */
    Document createDocument(final IRecord record) {
        
        
        if (isMaster_) {
            Document document = new Document();
            /**
             * Create a Field to store the MFN, without indexing it
             */
            document.add(new StringField("MFN",
                Long.toString(record.getMfn()),
                Field.Store.YES));

            GuiGlobal.output("*** MFN=" + record.getMfn() + " *** Terms Extracted by the FST");
            for (int i = 0; i < parsedFstEntries_.length; i++) {
                if ((parsedFstEntries_[i] == null) || parsedFstEntries_[i].invalidState()) {
                    GuiGlobal.output("*** Parsing Error *** Invalid format!");
                } else {
                    Field[] fields = parsedFstEntries_[i].extract(db_, record);
                    if (fields != null) {
                        int n = fields.length;
                        for (int j = 0; j < n; j++) {
                            String term = fields[j].stringValue();
                            /**
                             * Remove diacritics (~= accents) from a string. The case will not be altered. For
                             * instance, 'à' will be replaced by 'a'. Note that ligatures will be left as is.
                             * StringUtils.stripAccents(null) = null StringUtils.stripAccents("") = ""
                             * StringUtils.stripAccents("control") = "control"
                             * StringUtils.stripAccents("éclair") = "eclair"
                             *
                             */
                            String normalizedTerm = StringUtils.stripAccents(term);
                            normalizedTerm = StringUtils.upperCase(normalizedTerm);
                            fields[j].setStringValue(normalizedTerm);

                            document.add(fields[j]);
                            GuiGlobal.output(fields[j].toString());
                        }
                    }
                }
            }
            return document;
        } else {

            GuiGlobal.output("*** MFN=" + record.getMfn() + " *** Output produced by the FST:");

            for (int i = 0; i < fst_.getEntriesCount(); i++) {
                FieldSelectionTable.FstEntry entry = fst_.getEntryByIndex(i);
                pftIL_[i].setRecord(db_, record);
                pftIL_[i].eval();
                String fieldVal = pftIL_[i].getText();
                if (fieldVal == null) {
                    continue;
                }
                GuiGlobal.output(">>>FST ID [" + entry.getTag() + "]: ");
                GuiGlobal.output(fieldVal);
            }
            return null;
        }

       
    }
    
     /**
    *  Parse the reformatting FST to get the intermediate language (IL)
    *  For further execution on the individual records
    *
    * @param sourceDB
    * @param reformattingFST
    * @return
    */
    private static ISISFormatter[] parseFST(final IDatabase db, final FieldSelectionTable fst) {

      // Parse the reformatting FST to get the intermediate language (IL)
        // For further execution on the individual records
        ISISFormatter[] pftIL = null;
        pftIL = new ISISFormatter[fst.getEntriesCount()];
        for (int i = 0; i < fst.getEntriesCount(); i++) {
            FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
            int tag = entry.getTag();
            String pft = entry.getFormat();
            pftIL[i] = ISISFormatter.getFormatter(pft);
            if (pftIL[i] == null) {
                GuiGlobal.output(ISISFormatter.getParsingError());
                return null;
            } else if (pftIL[i].hasParsingError()) {
                GuiGlobal.output(ISISFormatter.getParsingError());
                return null;
            }
            // pftValid_ = !pftIL_.hasParsingError();
        }
        return pftIL;
    }

}
