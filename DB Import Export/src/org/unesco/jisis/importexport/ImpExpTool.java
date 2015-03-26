/*
 * ImpExpTool.java
 *
 * Created on February 3, 2007, 5:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



package org.unesco.jisis.importexport;

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
//import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NotImplementedException;
import org.openide.util.RequestProcessor;



import java.awt.EventQueue;

import java.io.*;

import java.util.*;
import javax.swing.SwingUtilities;


import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.marc4j.MarcXmlReader;
import org.marc4j.converter.CharConverter;
import org.marc4j.converter.impl.Iso6937ToUnicode;
import org.marc4j.converter.impl.UnicodeToAnsel;
import org.marc4j.converter.impl.UnicodeToIso5426;
import org.marc4j.converter.impl.UnicodeToIso6937;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.CreateDbParams;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.MfnRange;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.record.Field;
import org.unesco.jisis.corelib.record.FieldFactory;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.StringOccurrence;

import org.unesco.jisis.corelib.util.StringUtils;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;
import org.unesco.jisis.jisiscore.client.MarkedRecords;
import org.unesco.jisis.jisiscore.client.SearchResult;


/**
 *
 * @author jcd
 */
public class ImpExpTool {

    public final static int IMPORT_OPTION_LOAD = 0;
    public final static int IMPORT_OPTION_MERGE = 1;
    public final static int IMPORT_OPTION_UPDATE = 2;

   /**
     * Creates a new named RequestProcessor with defined throughput which can support interruption of the 
     * thread the processor runs in.
     * public RequestProcessor(String name,
     *           int throughput,
     *           boolean interruptThread)
     * 
     * Parameters:
     * name - the name to use for the request processor thread
     * throughput - the maximal count of requests allowed to run in parallel
     * interruptThread - true if RequestProcessor.Task.cancel() shall interrupt the thread
     */ 
    private final static RequestProcessor requestProcessor_ = new RequestProcessor("interruptible tasks", 1, true);
    
     protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ImpExpTool.class);

    /**
     * Creates a new instance of ImpExpTool
     */
    private ImpExpTool() {
    }

   //---------------------------------------------------------------------------
   // IMPORT METHODS
   //---------------------------------------------------------------------------

   /**
    * Import MARC records with format defined by ANSI/NISO Z39.2 and
    * ISO 2709:1996 standards
    *
    * @param parameters
    * @param targetDB
    */
    public static void importISO2709(final Map<String, Object> parameters, final IDatabase targetDB) {
        if (targetDB == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(ImpExpTool.class,
                "MSG_YouMustOpenTheDatabaseForImport"));
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        final File importFile = (File) parameters.get("impFile");
        final String encoding = (String) parameters.get("encoding");
        final int format = (Integer) parameters.get("format");
        final int inputLineLength = (Integer) parameters.get("inputLineLength");
        final int subfieldDelimiter
            = (Integer) parameters.get("subfieldDelimiter");
        final String reformattingFST = (String) parameters.get("reformattingFST");
        final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
        final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
        final int importOption = (Integer) parameters.get("importOption");
        final int leaderFields = (Integer) parameters.get("leaderFields");
        final boolean controlFieldsOption = (Boolean) parameters.get("controlFieldsOption");
        NotifyDescriptor.Confirmation cf = null;
        try {
            cf = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(ImpExpTool.class, "MSG_IsoFileWillBeImportedToDatabase")
                + targetDB.getDatabaseName() + "\n          Encoding: " + encoding
                + "\n       Line Length: " + inputLineLength + "\nSubfield Delimiter: "
                + subfieldDelimiter
                + "\n" + ((importOption == IMPORT_OPTION_LOAD) ? "Database content will be erased "
                    : (importOption == IMPORT_OPTION_MERGE) ? "Merge Option" : "Update Option"), NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        DialogDisplayer.getDefault().notify(cf);
        if ((cf.getValue() == NotifyDescriptor.CANCEL_OPTION)
            || (cf.getValue() == NotifyDescriptor.NO_OPTION)) {
            return;
        }

        GuiGlobal.output("Starting import");
        final Date start = new Date();
        try {
            Runnable importRun = new Runnable() {
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {

                            if (!controlFieldsOption) {
                                importISO2709TaskNoControlFields(parameters, targetDB);
                            } else {
                                importISO2709Task(parameters, targetDB);
                            }
                            // targetDB.close();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        Date end = new Date();
                        long recordCount = 0;
                        try {
                            targetDB.close();
                            targetDB.getDatabase(targetDB.getDbHome(), targetDB.getDbName(), Global.DATABASE_DURABILITY_WRITE);
                            recordCount = targetDB.getRecordsCount();
                        } catch (DbException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        if (!reformattingFST.equals("<none>")) {
                            GuiGlobal.output("ISO input records were reformatted according to FST " + reformattingFST);
                        }
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime())
                            + " milliseconds to import " + recordCount + " records from ISO file");
                        NotifyDescriptor d = new NotifyDescriptor.Message("Import is done!");
                        DialogDisplayer.getDefault().notify(d);
                    }
                }
            };
            requestProcessor_.post(importRun);
        } catch (Exception ex) {
            GuiGlobal.output("Error when importing ISO2709 data:\n " + ex.getMessage());
            LOGGER.error("Error when importing ISO2709 data:\n ", ex);
 
        }
    }

    /**
     * Import MARC records in MARCXML format
     *
     * @param targetDB
     * @param parameters
     */
    public static void importMarcXML(final IDatabase targetDB, final Map<String, Object> parameters) {
        if (targetDB == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(ImpExpTool.class,
                "MSG_YouMustOpenTheDatabaseForImport"));
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        final File importFile = (File) parameters.get("impFile");
        final String encoding = (String) parameters.get("encoding");
        final int format = (Integer) parameters.get("format");
        final int inputLineLength = 0; //(Integer) parameters.get("inputLineLength");
        final int subfieldDelimiter = 36; // Dollar
        //(Integer) parameters.get("subfieldDelimiter");
        final String reformattingFST = (String) parameters.get("reformattingFST");
        final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
        final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
        final int importOption = (Integer) parameters.get("importOption");
        final int leaderFields = (Integer) parameters.get("leaderFields");
        NotifyDescriptor.Confirmation cf = null;
        try {
            cf = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(ImpExpTool.class, "MSG_IsoFileWillBeImportedToDatabase")
                + targetDB.getDatabaseName() + "\n          Encoding: " + encoding
                + "\n       Line Length: " + inputLineLength + "\nSubfield Delimiter: "
                + subfieldDelimiter, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        DialogDisplayer.getDefault().notify(cf);
        if ((cf.getValue() == NotifyDescriptor.CANCEL_OPTION)
            || (cf.getValue() == NotifyDescriptor.NO_OPTION)) {
            return;
        }

        GuiGlobal.output("Starting import");
        final Date start = new Date();
        try {
            Runnable importRun = new Runnable() {
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            importMarcXMLTask(parameters, targetDB);
                            // targetDB.close();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        Date end = new Date();
                        long recordCount = 0;
                        try {
                            targetDB.close();
                            targetDB.getDatabase(targetDB.getDbHome(), targetDB.getDbName(), Global.DATABASE_DURABILITY_WRITE);
                            recordCount = targetDB.getRecordsCount();
                        } catch (DbException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        if (!reformattingFST.equals("<none>")) {
                            GuiGlobal.output("MARCXML input records were reformatted according to FST " + reformattingFST);
                        }
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime())
                            + " milliseconds to import " + recordCount + " from MARCXML file");
                        // Second Invocation, we are on the event queue now
                        NotifyDescriptor d = new NotifyDescriptor.Message("Import is done!");
                        DialogDisplayer.getDefault().notify(d);
                    }
                }
            };

            requestProcessor_.post(importRun);
        } catch (Exception ex) {
            GuiGlobal.output("Error when importing MARCXML data:\n " + ex.getMessage());
            LOGGER.error("Error when importing MARCXML data:\n ", ex);
        }

    }

    public static void importMODS(final IDatabase targetDB, final Map<String, Object> parameters) {
        if (targetDB == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(ImpExpTool.class,
                "MSG_YouMustOpenTheDatabaseForImport"));
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        final File importFile = (File) parameters.get("impFile");
        final String encoding = (String) parameters.get("encoding");
        final int format = (Integer) parameters.get("format");
        final int inputLineLength = 0; //(Integer) parameters.get("inputLineLength");
        final int subfieldDelimiter = 36; // Dollar
        //(Integer) parameters.get("subfieldDelimiter");
        final String reformattingFST = (String) parameters.get("reformattingFST");
        final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
        final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
        final int importOption = (Integer) parameters.get("importOption");
        final int leaderFields = (Integer) parameters.get("leaderFields");
        NotifyDescriptor.Confirmation cf = null;
        try {
            cf = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(ImpExpTool.class, "MSG_IsoFileWillBeImportedToDatabase")
                + targetDB.getDatabaseName() + "\n          Encoding: " + encoding
                + "\n       Line Length: " + inputLineLength + "\nSubfield Delimiter: "
                + subfieldDelimiter, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        DialogDisplayer.getDefault().notify(cf);
        if ((cf.getValue() == NotifyDescriptor.CANCEL_OPTION)
            || (cf.getValue() == NotifyDescriptor.NO_OPTION)) {
            return;
        }

        GuiGlobal.output("Starting import");
        final Date start = new Date();
        try {
            Runnable importRun = new Runnable() {
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            importMODSTask(parameters, targetDB);
                            // targetDB.close();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        Date end = new Date();
                          long recordCount = 0;
                        try {
                            targetDB.close();
                            targetDB.getDatabase(targetDB.getDbHome(), targetDB.getDbName(), Global.DATABASE_DURABILITY_WRITE);
                            recordCount = targetDB.getRecordsCount();
                        } catch (DbException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        if (!reformattingFST.equals("<none>")) {
                            GuiGlobal.output("MODS input records were reformatted according to FST " + reformattingFST);
                        }
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime())
                            + " milliseconds to import "+recordCount+" records from MODS file");
                        NotifyDescriptor d = new NotifyDescriptor.Message("Import is done!");
                        DialogDisplayer.getDefault().notify(d);
                    }
                }
            };

            requestProcessor_.post(importRun);
        } catch (Exception ex) {
             GuiGlobal.output("Error when importing MODS data:\n " + ex.getMessage());
             LOGGER.error("Error when importing MODS data:\n ", ex);
        }

    }
    
    public static void importDublinCore(final IDatabase targetDB, final Map<String, Object> parameters) {
        if (targetDB == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(ImpExpTool.class,
                "MSG_YouMustOpenTheDatabaseForImport"));
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        final File importFile = (File) parameters.get("impFile");
        final String encoding = (String) parameters.get("encoding");
        final int format = (Integer) parameters.get("format");
        final int inputLineLength = 0; //(Integer) parameters.get("inputLineLength");
        final int subfieldDelimiter = 36; // Dollar
        //(Integer) parameters.get("subfieldDelimiter");
        final String reformattingFST = (String) parameters.get("reformattingFST");
        final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
        final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
        final int importOption = (Integer) parameters.get("importOption");
        final int leaderFields = (Integer) parameters.get("leaderFields");
        NotifyDescriptor.Confirmation cf = null;
        try {
            cf = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(ImpExpTool.class, "MSG_IsoFileWillBeImportedToDatabase")
                + targetDB.getDatabaseName() + "\n          Encoding: " + encoding
                + "\n       Line Length: " + inputLineLength + "\nSubfield Delimiter: "
                + subfieldDelimiter, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        DialogDisplayer.getDefault().notify(cf);
        if ((cf.getValue() == NotifyDescriptor.CANCEL_OPTION)
            || (cf.getValue() == NotifyDescriptor.NO_OPTION)) {
            return;
        }

        GuiGlobal.output("Starting import");
        final Date start = new Date();
        try {
            Runnable importRun = new Runnable() {
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            importDublinCoreTask(parameters, targetDB);
                            // targetDB.close();                    
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        Date end = new Date();
                          long recordCount = 0;
                        try {
                            targetDB.close();
                            targetDB.getDatabase(targetDB.getDbHome(), targetDB.getDbName(), Global.DATABASE_DURABILITY_WRITE);
                            recordCount = targetDB.getRecordsCount();
                        } catch (DbException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        if (!reformattingFST.equals("<none>")) {
                            GuiGlobal.output("DC input records were reformatted according to FST " + reformattingFST);
                        }
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime())
                            + " milliseconds to import "+recordCount+" from DC file");
                        NotifyDescriptor d = new NotifyDescriptor.Message("Import is done!");
                        DialogDisplayer.getDefault().notify(d);

                    }
                }
            };

            requestProcessor_.post(importRun);
        } catch (Exception ex) {
            GuiGlobal.output("Error when importing DC data:\n " + ex.getMessage());
            LOGGER.error("Error when importing DC data:\n ", ex);
            Exceptions.printStackTrace(ex);
        }

    }

   public static void importMarc21(IDatabase db_, Map<String, Object> parameters) {
      throw new UnsupportedOperationException("Not yet implemented");
   }

    public static void importUnimarc(final IDatabase targetDB, final Map<String, Object> parameters) {

        if (targetDB == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(ImpExpTool.class,
                "MSG_YouMustOpenTheDatabaseForImport"));
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        final File importFile = (File) parameters.get("impFile");
        final String encoding = (String) parameters.get("encoding");
        final int format = (Integer) parameters.get("format");
        final int inputLineLength = (Integer) parameters.get("inputLineLength");
        final int subfieldDelimiter
            = (Integer) parameters.get("subfieldDelimiter");
        final String reformattingFST = (String) parameters.get("reformattingFST");
        final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
        final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
        final int importOption = (Integer) parameters.get("importOption");
        final int leaderFields = (Integer) parameters.get("leaderFields");
        NotifyDescriptor.Confirmation cf = null;
        try {
            cf = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(ImpExpTool.class, "MSG_IsoFileWillBeImportedToDatabase")
                + targetDB.getDatabaseName() + "\n          Encoding: " + encoding
                + "\n       Line Length: " + inputLineLength + "\nSubfield Delimiter: "
                + subfieldDelimiter
                + "\n" + ((importOption == IMPORT_OPTION_LOAD) ? "Database content will be erased "
                    : (importOption == IMPORT_OPTION_MERGE) ? "Merge Option" : "Update Option"), NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        DialogDisplayer.getDefault().notify(cf);
        if ((cf.getValue() == NotifyDescriptor.CANCEL_OPTION)
            || (cf.getValue() == NotifyDescriptor.NO_OPTION)) {
            return;
        }

        GuiGlobal.output("Starting import");
        final Date start = new Date();
        try {
            Runnable importRun = new Runnable() {
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            importUnimarcTask(parameters, targetDB);
                            // targetDB.close();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        Date end = new Date();
                          long recordCount = 0;
                        try {
                            targetDB.close();
                            targetDB.getDatabase(targetDB.getDbHome(), targetDB.getDbName(), Global.DATABASE_DURABILITY_WRITE);
                            recordCount = targetDB.getRecordsCount();
                        } catch (DbException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        if (!reformattingFST.equals("<none>")) {
                            GuiGlobal.output("UNIMARC input records were reformatted according to FST " + reformattingFST);
                        }
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime())
                            + " milliseconds to import "+recordCount+" records from UNIMARC file");
                        NotifyDescriptor d = new NotifyDescriptor.Message("Import is done!");
                        DialogDisplayer.getDefault().notify(d);
                    }
                }
            };
            requestProcessor_.post(importRun);
        } catch (Exception ex) {
             GuiGlobal.output("Error when importing UNIMARC data:\n " + ex.getMessage());
            LOGGER.error("Error when importing UNIMARC data:\n ", ex);
            Exceptions.printStackTrace(ex);
        }

    }
    /**
    * Thread Task for importing MARC Records in ISO 2709 format
    *
    * @param parameters
    * @param targetDB
    */
    private static void importUnimarcTask(final Map<String, Object> parameters, final IDatabase targetDB) {
        final File importFile = (File) parameters.get("importFile");
        final String encoding = (String) parameters.get("encoding");
        final int inputLineLength = (Integer) parameters.get("inputLineLength");
        final int subfieldDelimiter = (Integer) parameters.get("subfieldDelimiter");
        final String reformattingFST = (String) parameters.get("reformattingFST");
        final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
        final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
        final int importOption = (Integer) parameters.get("importOption");
        final boolean leaderFields = ((Integer) parameters.get("leaderFields") == 1) ? true : false;
        final MarcReader reader = new NioIsoReader(importFile, encoding,
            inputLineLength);
        FieldDefinitionTable fdt = null;
        String charToReplace = new Character((char) subfieldDelimiter).toString();
        String replacement = Global.SUBFIELD_SEPARATOR;

        ISISFormatter[] pftIL = null;

        FieldSelectionTable fst = null;
        try {
            fst = targetDB.getFst(reformattingFST);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
      // Parse the reformatting FST to get the intermediate language (IL)
        // For further execution on the individual records
        if (!reformattingFST.equals("<none>")) {
            if ((pftIL = parseReformattingFST(targetDB, reformattingFST)) == null) {
                return;
            }
        }
        CancellableProgress cancellable = new CancellableProgress();
        final ProgressHandle progress
            = ProgressHandleFactory.createHandle("Importing Data...", cancellable);
        progress.start();
        progress.switchToIndeterminate();
        long MFN = -1L;
        try {
            fdt = targetDB.getFieldDefinitionTable();

            // Loop on the MARC records
            while (reader.hasNext()) {
                if (Thread.interrupted() || cancellable.cancelRequested()) {
                    progress.finish();
                    break;
                }
                // Get the next MARC record
                Record record = null;
                try {
                    record = reader.next();
                } catch (Exception ex) {
                    progress.finish();
                    GuiGlobal.outputErr("Error when reading UNIMARC record, probably wrong line length");
                    GuiGlobal.outputErr(ex.toString());
                    break;
                }
                // Global.output("record: " + record.toString());

                // Create an empty ISIS record
                IRecord iRec = org.unesco.jisis.corelib.record.Record.createRecord();

                //assert(record.getControlFields().size() == 0);
                // Loop on MARC record fields to build ISIS Record
                Iterator it = record.getVariableFields().iterator();
                while (it.hasNext()) {
                    ControlField cf = (ControlField) it.next();
                    int iTag = Integer.parseInt(cf.getTag());
                    MFN = -1L;
                    if (inputTagMFN == iTag) {
                        String sMFN = cf.getData();
                        MFN = Long.valueOf(sMFN);
                        continue;
                    }
                    // getField creates the field if not found
                    IField field = iRec.getField(iTag);

                    //  All fields are supposed to have occurrences, a non repeatable
                    // field will have 1 occurrence
                    int nOccurrences = field.getOccurrenceCount();
                    String data = cf.getData();

                    data = Iso5426ToUnicode.convert(data);

                    // Replace external subfield delimiter by ISIS one
                    if (subfieldDelimiter != 94) {
                        data = StringUtils.replaceChar(data,
                            charToReplace.charAt(0), replacement.charAt(0));

                    }
                    field.setOccurrence(nOccurrences, data);

                    // getFieldByTag will add a new entry in the fdt if the tag is
                    // not found
                    FieldDefinition fd = fdt.getFieldByTag(iTag);
                    // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
                    // otc.println("**************\n" + iRec.toString());
                }
                // Build ISIS fields for the leader if needed
                if (leaderFields) {
                    org.marc4j.marc.Leader leader = record.getLeader();

                    for (int i = 0; i <= 23; i++) {
                        int iTag = 3000 + i;
                        /**
                         * getField creates the field if not found
                         */
                        IField field = iRec.getField(iTag);
                        /**
                         * getFieldByTag will add a new entry in the fdt if the tag is not found
                         */
                        FieldDefinition fd = fdt.getFieldByTag(iTag);
                        String sleader = leader.marshal();
                        field.setOccurrence(0, leader.marshal().substring(i, i + 1));
                    }
                }
                // We have now a complete ISIS record eqivalent to the MARC record
                if (!reformattingFST.equals("<none>")) {
                    // MARC fields are changed according to the fst
                    org.unesco.jisis.corelib.record.Record reformattedRec
                        = (org.unesco.jisis.corelib.record.Record) org.unesco.jisis.corelib.record.Record.createRecord();
                    // Build a new field from each FST entry
                    for (int i = 0; i < fst.getEntriesCount(); i++) {
                        FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                        int iTag = entry.getTag();
                        // getField creates the field if not found
                        Field field = (Field) reformattedRec.getField(iTag);
                        pftIL[i].setRecord(targetDB, iRec);
                        pftIL[i].eval();
                        String fieldVal = pftIL[i].getText();
                        if (fieldVal == null) {
                            continue;
                        }
                        // At this level, the new field may have occurrences and subfields
                        field.setFieldValue(fieldVal);
                    }
                    iRec = reformattedRec;
                }
                try {
                    if ((importOption == IMPORT_OPTION_UPDATE) && (MFN > 0 && MFN <= targetDB.getLastMfn())) {
                        iRec.setMfn(MFN);
                        iRec = targetDB.updateRecord((org.unesco.jisis.corelib.record.Record) iRec);
                    } else {

                        iRec = targetDB.addRecord((org.unesco.jisis.corelib.record.Record) iRec);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

                long mfn = iRec.getMfn();
                if (mfn % 10 == 0) {
                    progress.setDisplayName("Performing Import MFN:" + mfn);
                }
                iRec = null;
                record = null;
            }
            progress.finish();
        } catch (MarcException e) {
            String s = e.getMessage();
            if (s.equals("EOF")) {

                /**
                 * Continue, eof detected
                 */
                progress.finish();
            } else {
                progress.finish();
                throw new ImportException(e);
            }
        } catch (DbException dbe) {
            throw new ImportException(dbe);
        }
        progress.finish();
        try {
            /**
             * these instructions should be part of the thread as it must be executed once the DB is fully
             * loaded
             */
            targetDB.saveFieldDefinitionTable(fdt);
            targetDB.resetDatabaseInfo();
            long recCount = targetDB.getRecordsCount();
            long maxMFN = targetDB.getLastMfn();
            //System.out.println("ImpExpTool recCount=" + recCount + " maxMFN=" + maxMFN);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        // targetDB.
//        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Import completed",
//            NotifyDescriptor.INFORMATION_MESSAGE));
//        GuiGlobal.output("Last progress.finish()");
    }


   /**
    * Thread Task for importing MARC Records in ISO 2709 format
    * 
    * @param parameters
    * @param targetDB
    */
   private static void importISO2709Task(final Map<String, Object> parameters, final IDatabase targetDB) {
      final File      importFile = (File) parameters.get("importFile");
      final String    encoding = (String) parameters.get("encoding");
      final int       inputLineLength = (Integer) parameters.get("inputLineLength");
      final int       subfieldDelimiter = (Integer) parameters.get("subfieldDelimiter");
      final String    reformattingFST = (String) parameters.get("reformattingFST");
      final long      renumberFromMFN = (Long) parameters.get("renumberFromMFN");
      final int       inputTagMFN = (Integer) parameters.get("inputTagMFN");
      final int       importOption = (Integer) parameters.get("importOption");
      final boolean   leaderFields = ((Integer) parameters.get("leaderFields") == 1) ? true : false;
      final boolean   controlFieldsOption = (Boolean) parameters.get("controlFieldsOption");
      
      final MarcReader reader = new NioIsoReader(importFile, encoding,inputLineLength);
      FieldDefinitionTable fdt = null;
      String charToReplace = new Character((char) subfieldDelimiter).toString();
      String replacement = Global.SUBFIELD_SEPARATOR;

      ISISFormatter[] pftIL = null;

      long timeReading = 0, timeWriting = 0;

      FieldSelectionTable fst = null;
      try {
         fst = targetDB.getFst(reformattingFST);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      // Parse the reformatting FST to get the intermediate language (IL)
      // For further execution on the individual records
      if (!reformattingFST.equals("<none>")) {
         if ((pftIL = parseReformattingFST(targetDB, reformattingFST)) == null) {
            return;
         }
      }
      CancellableProgress cancellable = new CancellableProgress();
      final ProgressHandle progress =
              ProgressHandleFactory.createHandle("Importing Data...", cancellable);
      progress.start();
      progress.switchToIndeterminate();
      long MFN = -1L;
      int recordCount = 0;
      try {
         fdt = targetDB.getFieldDefinitionTable();

         // Loop on the MARC records
         while (reader.hasNext()) {
            if (Thread.interrupted() || cancellable.cancelRequested()) {
               progress.finish();
               break;
            }
            Date startReadingDate = new Date();
            // Get the next MARC record
             org.marc4j.marc.Record record;
             try {
                 record = reader.next();
             } catch (Exception ex) {
                 progress.finish();
                 GuiGlobal.outputErr("Error when reading ISO record, probably wrong line length");
                 GuiGlobal.outputErr(ex.toString());
                 break;
             }
            // Global.output("record: " + record.toString());

            // Create an empty ISIS record
            IRecord iRec = org.unesco.jisis.corelib.record.Record.createRecord();

            //assert(record.getControlFields().size() == 0);

            // Loop on MARC record fields to build ISIS Record
            Iterator it = record.getVariableFields().iterator();
            while (it.hasNext()) {
               ControlField cf = (ControlField) it.next();
               int iTag = Integer.parseInt(cf.getTag());
               MFN = -1L;
               if (inputTagMFN == iTag) {
                  String sMFN = cf.getData();
                  MFN = Long.valueOf(sMFN);
                  continue;
               }

               // getField creates the field if not found
               IField field = iRec.getField(iTag);

               
               //  All fields are supposed to have occurrences, a non repeatable
               // field will have 1 occurrence
               int nOccurrences = field.getOccurrenceCount();
               String data = cf.getData();
               if (data == null) {
                   continue;
               } 
             
               // Replace external subfield delimiter by ISIS one
               if (subfieldDelimiter != 94) {
                  data = StringUtils.replaceChar(data,
                          charToReplace.charAt(0), replacement.charAt(0));
                  
               }
               field.setOccurrence(nOccurrences, data);

               //getFieldByTag will add a new entry in the fdt if the tag is
               // not found
               
               FieldDefinition fd = fdt.getFieldByTag(iTag);
               // Update subfield codes if new subfield codes
               String fdtSubfields = fd.getSubfields();
               StringOccurrence stringOccurrence = (StringOccurrence) field.getOccurrence(nOccurrences);
               if (stringOccurrence != null && stringOccurrence.getSubfieldCount() > 0) {
                    org.unesco.jisis.corelib.record.Subfield[] subfields = stringOccurrence.getSubfields();
                    for (org.unesco.jisis.corelib.record.Subfield subfield : subfields) {
                        char code = subfield.getSubfieldCode();
                        if (code == '*') {
                            continue;
                        }
                        int index = fdtSubfields.indexOf(code);
                        if (index >= 0) {
                            continue;
                        }
                        fdtSubfields = new StringBuilder(fdtSubfields).append(code).toString();
                        char[] chars = fdtSubfields.toCharArray();
                        Arrays.sort(chars);
                        fdtSubfields = new String(chars);

                    }
                    fd.setSubfields(fdtSubfields);
                }
               // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
               // otc.println("**************\n" + iRec.toString());
            }
            // Build ISIS fields for the leader if needed
            if (leaderFields) {
               org.marc4j.marc.Leader leader = record.getLeader();

               for (int i = 0; i <= 23; i++) {
                  int iTag = 3000 + i;
                  /** getField creates the field if not found */
                  IField field = iRec.getField(iTag);
                  /**
                   * getFieldByTag will add a new entry in the fdt if the tag is
                   * not found
                   */
                  FieldDefinition fd = fdt.getFieldByTag(iTag);
                  String sleader = leader.marshal();
                  field.setOccurrence(0, leader.marshal().substring(i, i + 1));
               }
            }
            // We have now a complete ISIS record eqivalent to the MARC record
            if (!reformattingFST.equals("<none>")) {
               // MARC fields are changed according to the fst
               org.unesco.jisis.corelib.record.Record jRec =
                       (org.unesco.jisis.corelib.record.Record) org.unesco.jisis.corelib.record.Record.createRecord();
               // Build a new field from each FST entry
               for (int i = 0; i < fst.getEntriesCount(); i++) {
                  FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                  int iTag = entry.getTag();
                  // getField creates the field if not found
                  Field field = (Field) jRec.getField(iTag);
                  pftIL[i].setRecord(targetDB, iRec);
                  pftIL[i].eval();
                  String fieldVal = pftIL[i].getText();
                  if (fieldVal == null) {
                     continue;
                  }
                  // At this level, the new field may have occurrences and subfields
                  field.setFieldValue(fieldVal);
               }
               iRec = jRec;
            }
             Date endReadingDate = new Date();
             timeReading += (endReadingDate.getTime() - startReadingDate.getTime());
             Date startWritingDate = new Date();
            try {
               if ((importOption == IMPORT_OPTION_UPDATE) && (MFN>0 && MFN <= targetDB.getLastMfn())) {
                  iRec.setMfn(MFN);
                  iRec = targetDB.updateRecord((org.unesco.jisis.corelib.record.Record) iRec);
               } else {

                  iRec = targetDB.addRecord((org.unesco.jisis.corelib.record.Record) iRec);
               }
             } catch (Exception ex) {
                 progress.finish();
                 Exceptions.printStackTrace(ex);
             }
            Date endWritingDate = new Date();
             timeWriting += (endWritingDate.getTime() - startWritingDate.getTime());
            final long mfn = iRec.getMfn();
             if (mfn % 100 == 0) {
                 SwingUtilities.invokeLater(new Runnable() {

                     public void run() {
                         progress.setDisplayName("Performing Import MFN:" + mfn);
                     }
                 });

             }
             recordCount++;
            iRec = null;
            record = null;
         }
         progress.finish();
        
      } catch (MarcException e) {
         String s = e.getMessage();
         if (s.equals("EOF")) {

            /** Continue, eof detected */
            progress.finish();
         } else {
            progress.finish();
            throw new ImportException(e);
         }
      } catch (DbException dbe) {
         throw new ImportException(dbe);
      }
      progress.finish();
      try {

         /**
          * these instructions should be part of the thread as it must be executed
          * once the DB is fully loaded
          */
         targetDB.saveFieldDefinitionTable(fdt);
         targetDB.resetDatabaseInfo();
         long recCount = targetDB.getRecordsCount();
         long maxMFN = targetDB.getLastMfn();
         //System.out.println("ImpExpTool recCount=" + recCount + " maxMFN=" + maxMFN);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
       // targetDB.
//       GuiGlobal.output("Time elapsed for reading ISO file:" + timeReading + " milliseconds");
//       GuiGlobal.output("Time elapsed for writing records:" + timeWriting + " milliseconds");
//       DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Import completed",
//           NotifyDescriptor.INFORMATION_MESSAGE));
//       GuiGlobal.output("Last progress.finish()");
   }
   
   /**
    * Thread Task for importing MARC Records in ISO 2709 format
    * 
    * @param parameters
    * @param targetDB
    */
   private static void importISO2709TaskNoControlFields(final Map<String, Object> parameters, final IDatabase targetDB) {
      final File      importFile = (File) parameters.get("importFile");
      final String    encoding = (String) parameters.get("encoding");
      final int       inputLineLength = (Integer) parameters.get("inputLineLength");
      final int       subfieldDelimiter = (Integer) parameters.get("subfieldDelimiter");
      final String    reformattingFST = (String) parameters.get("reformattingFST");
      final long      renumberFromMFN = (Long) parameters.get("renumberFromMFN");
      final int       inputTagMFN = (Integer) parameters.get("inputTagMFN");
      final int       importOption = (Integer) parameters.get("importOption");
      final boolean   leaderFields = ((Integer) parameters.get("leaderFields") == 1) ? true : false;
      final boolean   controlFieldsOption = (Boolean) parameters.get("controlFieldsOption");
      
      final MarcReader reader = new NoControlFieldsNioIsoReader(importFile, encoding,inputLineLength);
      FieldDefinitionTable fdt = null;
      String charToReplace = new Character((char) subfieldDelimiter).toString();
      String replacement = Global.SUBFIELD_SEPARATOR;

      ISISFormatter[] pftIL = null;

      long timeReading = 0, timeWriting = 0;

      FieldSelectionTable fst = null;
      try {
         fst = targetDB.getFst(reformattingFST);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      // Parse the reformatting FST to get the intermediate language (IL)
      // For further execution on the individual records
      if (!reformattingFST.equals("<none>")) {
         if ((pftIL = parseReformattingFST(targetDB, reformattingFST)) == null) {
            return;
         }
      }
      CancellableProgress cancellable = new CancellableProgress();
      final ProgressHandle progress =
              ProgressHandleFactory.createHandle("Importing Data...", cancellable);
      progress.start();
      progress.switchToIndeterminate();
      long MFN = -1L;
      try {
         fdt = targetDB.getFieldDefinitionTable();

         // Loop on the MARC records
         while (reader.hasNext()) {
            if (Thread.interrupted() || cancellable.cancelRequested()) {
               progress.finish();
               break;
            }
            Date startReadingDate = new Date();
            // Get the next MARC record
            NoControlFieldsRecord record = null;
             try {
                 record = (NoControlFieldsRecord) reader.next();
             } catch (Exception ex) {
                  progress.finish();
                  GuiGlobal.outputErr("Error when reading ISO record, probably wrong line length");
                  GuiGlobal.outputErr(ex.toString());
                  break;
             }
            // Global.output("record: " + record.toString());

            // Create an empty ISIS record
            IRecord iRec = org.unesco.jisis.corelib.record.Record.createRecord();

            //assert(record.getControlFields().size() == 0);

            // Loop on MARC record fields to build ISIS Record
            Iterator it = record.getVariableFields().iterator();
            while (it.hasNext()) {
               ControlField cf = (ControlField) it.next();
               int iTag = Integer.parseInt(cf.getTag());
               MFN = -1L;
               if (inputTagMFN == iTag) {
                  String sMFN = cf.getData();
                  MFN = Long.valueOf(sMFN);
                  continue;
               }

               // getField creates the field if not found
               IField field = iRec.getField(iTag);

               
               //  All fields are supposed to have occurrences, a non repeatable
               // field will have 1 occurrence
               int nOccurrences = field.getOccurrenceCount();
               String data = cf.getData();
               if (data == null) {
                   continue;
               } 
               // Replace external subfield delimiter by ISIS one
               if (subfieldDelimiter != 94) {
                  data = StringUtils.replaceChar(data,
                          charToReplace.charAt(0), replacement.charAt(0));
                  
               }
               field.setOccurrence(nOccurrences, data);

               //getFieldByTag will add a new entry in the fdt if the tag is
               // not found
               
               FieldDefinition fd = fdt.getFieldByTag(iTag);
               // Update subfield codes if new subfield codes
               String fdtSubfields = fd.getSubfields();
               StringOccurrence stringOccurrence = (StringOccurrence) field.getOccurrence(nOccurrences);
               if (stringOccurrence != null && stringOccurrence.getSubfieldCount() > 0) {
                    org.unesco.jisis.corelib.record.Subfield[] subfields = stringOccurrence.getSubfields();
                    if (subfields == null) {
                        System.out.println("NO SUBFIELDS ! stringOccurrence="+stringOccurrence);
                   } else {
                       for (org.unesco.jisis.corelib.record.Subfield subfield : subfields) {
                           char code = subfield.getSubfieldCode();
                           if (code == '*') {
                               continue;
                           }
                           int index = fdtSubfields.indexOf(code);
                           if (index >= 0) {
                               continue;
                           }
                           fdtSubfields = new StringBuilder(fdtSubfields).append(code).toString();
                           char[] chars = fdtSubfields.toCharArray();
                           Arrays.sort(chars);
                           fdtSubfields = new String(chars);

                       }
                       fd.setSubfields(fdtSubfields);
                   }
                }
                    
               // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
               // otc.println("**************\n" + iRec.toString());
            }
            // Build ISIS fields for the leader if needed
            if (leaderFields) {
               org.marc4j.marc.Leader leader = record.getLeader();

               for (int i = 0; i <= 23; i++) {
                  int iTag = 3000 + i;
                  /** getField creates the field if not found */
                  IField field = iRec.getField(iTag);
                  /**
                   * getFieldByTag will add a new entry in the fdt if the tag is
                   * not found
                   */
                  FieldDefinition fd = fdt.getFieldByTag(iTag);
                  String sleader = leader.marshal();
                  field.setOccurrence(0, leader.marshal().substring(i, i + 1));
               }
            }
            // We have now a complete ISIS record eqivalent to the MARC record
            if (!reformattingFST.equals("<none>")) {
               // MARC fields are changed according to the fst
               org.unesco.jisis.corelib.record.Record jRec =
                       (org.unesco.jisis.corelib.record.Record) org.unesco.jisis.corelib.record.Record.createRecord();
               // Build a new field from each FST entry
               for (int i = 0; i < fst.getEntriesCount(); i++) {
                  FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                  int iTag = entry.getTag();
                  // getField creates the field if not found
                  Field field = (Field) jRec.getField(iTag);
                  pftIL[i].setRecord(targetDB, iRec);
                  pftIL[i].eval();
                  String fieldVal = pftIL[i].getText();
                  if (fieldVal == null) {
                     continue;
                  }
                  // At this level, the new field may have occurrences and subfields
                  field.setFieldValue(fieldVal);
               }
               iRec = jRec;
            }
             Date endReadingDate = new Date();
             timeReading += (endReadingDate.getTime() - startReadingDate.getTime());
             Date startWritingDate = new Date();
            try {
               if ((importOption == IMPORT_OPTION_UPDATE) && (MFN>0 && MFN <= targetDB.getLastMfn())) {
                  iRec.setMfn(MFN);
                  iRec = targetDB.updateRecord((org.unesco.jisis.corelib.record.Record) iRec);
               } else {

                  iRec = targetDB.addRecord((org.unesco.jisis.corelib.record.Record) iRec);
               }
             } catch (Exception ex) {
                 progress.finish();
                 Exceptions.printStackTrace(ex);
             }
            Date endWritingDate = new Date();
             timeWriting += (endWritingDate.getTime() - startWritingDate.getTime());
            final long mfn = iRec.getMfn();
             if (mfn % 100 == 0) {
                 SwingUtilities.invokeLater(new Runnable() {

                     public void run() {
                         progress.setDisplayName("Performing Import MFN:" + mfn);
                     }
                 });

             }
            iRec = null;
            record = null;
         }
         progress.finish();
        
      } catch (MarcException e) {
         String s = e.getMessage();
         if (s.equals("EOF")) {

            /** Continue, eof detected */
            progress.finish();
         } else {
            progress.finish();
            throw new ImportException(e);
         }
      } catch (DbException dbe) {
         throw new ImportException(dbe);
      }
      progress.finish();
      try {

         /**
          * these instructions should be part of the thread as it must be executed
          * once the DB is fully loaded
          */
         targetDB.saveFieldDefinitionTable(fdt);
         targetDB.resetDatabaseInfo();
         long recCount = targetDB.getRecordsCount();
         long maxMFN = targetDB.getLastMfn();
         //System.out.println("ImpExpTool recCount=" + recCount + " maxMFN=" + maxMFN);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
       // targetDB.
//       GuiGlobal.output("Time elapsed for reading ISO file:" + timeReading + " milliseconds");
//       GuiGlobal.output("Time elapsed for writing records:" + timeWriting + " milliseconds");
//       DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Import completed",
//           NotifyDescriptor.INFORMATION_MESSAGE));
//       GuiGlobal.output("Last progress.finish()");
   }

   /**
    * Thread Task for importing MARC records in MARCXML format
    *
    * @param parameters
    * @param targetDB
    */
   private static void importMarcXMLTask(final Map<String, Object> parameters, final IDatabase targetDB) {
      final File importFile = (File) parameters.get("importFile");
      final String encoding = (String) parameters.get("encoding");
      /**
       * These 2 parameters doesn't need to be specify by the user 
       * inputLineLength is not used
       * subfieldDelimiter is set to $ (36) because marc4j use it as subfield
       * delimiter.
       * 
       */
      final int inputLineLength = 0; //(Integer) parameters.get("inputLineLength");
      final int subfieldDelimiter = 36; //(Integer) parameters.get("subfieldDelimiter");
      final String reformattingFST = (String) parameters.get("reformattingFST");
      final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
      final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
      final int importOption = (Integer) parameters.get("importOption");
      final boolean leaderFields = ((Integer) parameters.get("leaderFields") == 1) ? true : false;


      FieldDefinitionTable fdt = null;
      String charToReplace = new Character((char) subfieldDelimiter).toString();
      String replacement = Global.SUBFIELD_SEPARATOR;

      ISISFormatter[] pftIL = null;



      FieldSelectionTable fst = null;
      try {
         fst = targetDB.getFst(reformattingFST);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      // Parse the reformatting FST to get the intermediate language (IL)
      // For further execution on the individual records
      if (!reformattingFST.equals("<none>")) {
         if ((pftIL = parseReformattingFST(targetDB, reformattingFST)) == null) {
            return;
         }
      }
      CancellableProgress cancellable = new CancellableProgress();
      final ProgressHandle progress =
              ProgressHandleFactory.createHandle("Importing Data...", cancellable);
      progress.start();
      progress.switchToIndeterminate();
      long MFN = -1L;

      InputStream in = null;
      try {
         in = new FileInputStream(importFile);
         final MarcReader reader = new MarcXmlReader(in);
         fdt = targetDB.getFieldDefinitionTable();
         while (reader.hasNext()) {
            if (Thread.interrupted() || cancellable.cancelRequested()) {
               progress.finish();
               break;
            }
             Record record = null;
             try {
                 record = reader.next();
             } catch (Exception ex) {
                 progress.finish();
                 GuiGlobal.outputErr("Error when reading MarcXML record");
                 GuiGlobal.outputErr(ex.toString());
                 break;
             }
            // Global.output("record: " + record.toString());
            IRecord iRec = org.unesco.jisis.corelib.record.Record.createRecord();
//          System.out.println("CONTROL FIELDS NUMBER" + record.getControlFields().size());
            Iterator it = record.getControlFields().iterator();
            while (it.hasNext()) {
               ControlField cf = (ControlField) it.next();
               int iTag = Integer.parseInt(cf.getTag());
                MFN = -1L;
               if (inputTagMFN == iTag) {
                  String sMFN = cf.getData();
                  MFN = Long.valueOf(sMFN);
                  continue;
               }
               /** getField creates the field if not found */
               IField field = iRec.getField(iTag);
               /**
                * All fields are supposed to have occurrences, a non repetetive
                * field will have 1 occurrence
                */
               int nOccurrences = field.getOccurrenceCount();
               String data = cf.getData();

               /**
                * Replace external subfield delimiter by ISIS one
                */
               if (subfieldDelimiter != 94) {
                  data = StringUtils.replaceChar(data,
                          charToReplace.charAt(0), replacement.charAt(0));
                  //data = data.replaceAll(charToReplace, replacement);
               }
               field.setOccurrence(nOccurrences, data);
               /**
                * getFieldByTag will add a new entry in the fdt if the tag is
                * not found
                */
                FieldDefinition fd = fdt.getFieldByTag(iTag);
                // Update subfield codes if new subfield codes
                String fdtSubfields = fd.getSubfields();
                StringOccurrence stringOccurrence = (StringOccurrence) field.getOccurrence(nOccurrences);
                if (stringOccurrence != null && stringOccurrence.getSubfieldCount() > 0) {
                    org.unesco.jisis.corelib.record.Subfield[] subfields = stringOccurrence.getSubfields();
                    for (org.unesco.jisis.corelib.record.Subfield subfield : subfields) {
                        char code = subfield.getSubfieldCode();
                        if (code == '*') {
                            continue;
                        }
                        int index = fdtSubfields.indexOf(code);
                        if (index >= 0) {
                            continue;
                        }
                        fdtSubfields = new StringBuilder(fdtSubfields).append(code).toString();
                        char[] chars = fdtSubfields.toCharArray();
                        Arrays.sort(chars);
                        fdtSubfields = new String(chars);

                    }
                    fd.setSubfields(fdtSubfields);
                }
               // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
               // otc.println("**************\n" + iRec.toString());
               }
            it = record.getDataFields().iterator();
            while (it.hasNext()) {
               DataField df = (DataField) it.next();
               int iTag = Integer.parseInt(df.getTag());

               StringBuffer sb = new StringBuffer();

               sb.append(df.getIndicator1());
               sb.append(df.getIndicator2());
               Iterator i = df.getSubfields().iterator();
               while (i.hasNext()) {
                  org.marc4j.marc.Subfield sf = (org.marc4j.marc.Subfield) i.next();
                  sb.append(sf.toString());
               }
               String data = sb.toString();
               /** getField creates the field if not found */
               IField field = iRec.getField(iTag);
               /**
                * All fields are supposed to have occurrences, a non repetetive
                * field will have 1 occurrence
                */
               int nOccurrences = field.getOccurrenceCount();

               /**
                * Replace external subfield delimiter by ISIS one
                */
               if (subfieldDelimiter != 94) {
                  data = StringUtils.replaceChar(data,
                          charToReplace.charAt(0), replacement.charAt(0));
                  // replaceAll uses regular expressions and some characters
                  // are meta characters
                  //data = data.replaceAll(charToReplace, replacement);
               }
               field.setOccurrence(nOccurrences, data);
               /**
                * getFieldByTag will add a new entry in the fdt if the tag is
                * not found
                */
                 FieldDefinition fd = fdt.getFieldByTag(iTag);
                 String fdtSubfields = fd.getSubfields();
                 StringOccurrence stringOccurrence = (StringOccurrence) field.getOccurrence(nOccurrences);
                 if (stringOccurrence != null && stringOccurrence.getSubfieldCount() > 0) {
                     org.unesco.jisis.corelib.record.Subfield[] subfields = stringOccurrence.getSubfields();
                     for (org.unesco.jisis.corelib.record.Subfield subfield : subfields) {
                         char code = subfield.getSubfieldCode();
                         if (code == '*') {
                             continue;
                         }
                         int index = fdtSubfields.indexOf(code);
                         if (index >= 0) {
                             continue;
                         }
                         fdtSubfields = new StringBuilder(fdtSubfields).append(code).toString();
                         char[] chars = fdtSubfields.toCharArray();
                         Arrays.sort(chars);
                         fdtSubfields = new String(chars);

                     }
                     fd.setSubfields(fdtSubfields);
                     fd.setIndicators(true);
               // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
                     // otc.println("**************\n" + iRec.toString());
                 }
             }
            if (leaderFields) {
               org.marc4j.marc.Leader leader = record.getLeader();
               for (int i = 0; i <= 23; i++) {
                  int iTag = 3000 + i;
                  /** getField creates the field if not found */
                  IField field = iRec.getField(iTag);
                  /**
                   * getFieldByTag will add a new entry in the fdt if the tag is
                   * not found
                   */
                  FieldDefinition fd = fdt.getFieldByTag(iTag);
                  String sleader = leader.marshal();
                  field.setOccurrence(0, leader.marshal().substring(i, i + 1));
               }
            }
            // We have now a complete ISIS record eqivalent to the MARC record
            if (!reformattingFST.equals("<none>")) {
               // MARC fields are changed according to the fst
               org.unesco.jisis.corelib.record.Record jRec =
                       (org.unesco.jisis.corelib.record.Record) org.unesco.jisis.corelib.record.Record.createRecord();
               // Build a new field from each FST entry
               for (int i = 0; i < fst.getEntriesCount(); i++) {
                  FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                  int iTag = entry.getTag();
                  // getField creates the field if not found
                  Field field = (Field) jRec.getField(iTag);
                  pftIL[i].setRecord(targetDB, iRec);
                  pftIL[i].eval();
                  String fieldVal = pftIL[i].getText();
                  if (fieldVal == null) {
                     continue;
                  }
                  // At this level, the new field may have occurrences and subfields
                  field.setFieldValue(fieldVal);
               }
               iRec = jRec;
            }
            try {
               if ((importOption == IMPORT_OPTION_UPDATE) && (MFN>0 && MFN <= targetDB.getLastMfn())) {
                  iRec.setMfn(MFN);
                  iRec = targetDB.updateRecord((org.unesco.jisis.corelib.record.Record) iRec);
               } else {

                  iRec = targetDB.addRecord((org.unesco.jisis.corelib.record.Record) iRec);
               }
            } catch (Exception ex) {
               Exceptions.printStackTrace(ex);
            }
            
            long mfn = iRec.getMfn();
            if (mfn % 10 == 0) {
               progress.setDisplayName("Performing Import MFN:" + mfn);
            }
            iRec = null;
            record = null;
         }
         progress.finish();
      } catch (FileNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } catch (MarcException e) {
         String s = e.getMessage();
         if (s.equals("EOF")) {
            /** Continue, eof detected */
            progress.finish();
         } else {
            throw new ImportException(e);
         }
      } catch (DbException dbe) {
         throw new ImportException(dbe);
      } finally {
         try {
            in.close();
         } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
         }
      }

      try {

         /**
          * these instructions should be part of the thread as it must be executed
          * once the DB is fully loaded
          */
         targetDB.saveFieldDefinitionTable(fdt);
         targetDB.resetDatabaseInfo();
         long recCount = targetDB.getRecordsCount();
         long maxMFN = targetDB.getLastMfn();
         //System.out.println("ImpExpTool recCount=" + recCount + " maxMFN=" + maxMFN);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      // targetDB.
//      DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Import completed",
//              NotifyDescriptor.INFORMATION_MESSAGE));
//      GuiGlobal.output("Last progress.finish()");
   }

   /**
    * Thread Task for importing MODS records - The input is pre-processed using
    * a stylesheet that transform XML MODS data to MARCXML.
    * The library of congress provides a stylesheet that transform MODS to
    * MARCXML
    * 
    * http://www.loc.gov/standards/marcxml/xslt/MODS2MARC2121slim.xsl
    *
    * @param parameters
    * @param targetDB
    */
   private static void importMODSTask(final Map<String, Object> parameters, final IDatabase targetDB) {
      final File importFile = (File) parameters.get("importFile");
      final String encoding = (String) parameters.get("encoding");
      /**
       * These 2 parameters doesn't need to be specify by the user 
       * inputLineLength is not used
       * subfieldDelimiter is set to $ (36) because marc4j use it as subfield
       * delimiter.
       * 
       */
      final int inputLineLength = 0; //(Integer) parameters.get("inputLineLength");
      final int subfieldDelimiter = 36; //(Integer) parameters.get("subfieldDelimiter");
      final String reformattingFST = (String) parameters.get("reformattingFST");
      final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
      final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
      final int importOption = (Integer) parameters.get("importOption");
      final boolean leaderFields = ((Integer) parameters.get("leaderFields") == 1) ? true : false;
      final String stylesheetUrl = "http://www.loc.gov/standards/marcxml/xslt/MODS2MARC21slim.xsl";


      FieldDefinitionTable fdt = null;
      String charToReplace = new Character((char) subfieldDelimiter).toString();
      String replacement = Global.SUBFIELD_SEPARATOR;

      ISISFormatter[] pftIL = null;



      FieldSelectionTable fst = null;
      try {
         fst = targetDB.getFst(reformattingFST);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      // Parse the reformatting FST to get the intermediate language (IL)
      // For further execution on the individual records
      if (!reformattingFST.equals("<none>")) {
         if ((pftIL = parseReformattingFST(targetDB, reformattingFST)) == null) {
            return;
         }
      }
      CancellableProgress cancellable = new CancellableProgress();
      final ProgressHandle progress =
              ProgressHandleFactory.createHandle("Importing Data...", cancellable);
      progress.start();
      progress.switchToIndeterminate();
      long MFN = -1L;

      InputStream in = null;
      try {
         in = new FileInputStream(importFile);
         final MarcReader reader = new MarcXmlReader(in, stylesheetUrl);
         fdt = targetDB.getFieldDefinitionTable();
         while (reader.hasNext()) {
            if (Thread.interrupted() || cancellable.cancelRequested()) {
               progress.finish();
               break;
            }
             Record record = null;
             try {
                 record = reader.next();
             } catch (Exception ex) {
                 progress.finish();
                 GuiGlobal.outputErr("Error when reading MODS record");
                 GuiGlobal.outputErr(ex.toString());
                 break;
             }
            // Global.output("record: " + record.toString());
            IRecord iRec = org.unesco.jisis.corelib.record.Record.createRecord();
//          System.out.println("CONTROL FIELDS NUMBER" + record.getControlFields().size());
            Iterator it = record.getControlFields().iterator();
            while (it.hasNext()) {
               ControlField cf = (ControlField) it.next();
               int iTag = Integer.parseInt(cf.getTag());
                MFN = -1L;
               if (inputTagMFN == iTag) {
                  String sMFN = cf.getData();
                  MFN = Long.valueOf(sMFN);
                  continue;
               }
               /** getField creates the field if not found */
               IField field = iRec.getField(iTag);
               /**
                * All fields are supposed to have occurrences, a non repetetive
                * field will have 1 occurrence
                */
               int nOccurrences = field.getOccurrenceCount();
               String data = cf.getData();

               /**
                * Replace external subfield delimiter by ISIS one
                */
               if (subfieldDelimiter != 94) {
                  data = StringUtils.replaceChar(data,
                          charToReplace.charAt(0), replacement.charAt(0));
                  //data = data.replaceAll(charToReplace, replacement);
               }
               field.setOccurrence(nOccurrences, data);
               /**
                * getFieldByTag will add a new entry in the fdt if the tag is
                * not found
                */
               FieldDefinition fd = fdt.getFieldByTag(iTag);
               // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
               // otc.println("**************\n" + iRec.toString());
               }
            it = record.getDataFields().iterator();
            while (it.hasNext()) {
               DataField df = (DataField) it.next();
               int iTag = Integer.parseInt(df.getTag());

               StringBuffer sb = new StringBuffer();

               sb.append(df.getIndicator1());
               sb.append(df.getIndicator2());
               Iterator i = df.getSubfields().iterator();
               while (i.hasNext()) {
                  org.marc4j.marc.Subfield sf = (org.marc4j.marc.Subfield) i.next();
                  sb.append(sf.toString());
               }
               String data = sb.toString();
               /** getField creates the field if not found */
               IField field = iRec.getField(iTag);
               /**
                * All fields are supposed to have occurrences, a non repetetive
                * field will have 1 occurrence
                */
               int nOccurrences = field.getOccurrenceCount();

               /**
                * Replace external subfield delimiter by ISIS one
                */
               if (subfieldDelimiter != 94) {
                  data = StringUtils.replaceChar(data,
                          charToReplace.charAt(0), replacement.charAt(0));
                  // replaceAll uses regular expressions and some characters
                  // are meta characters
                  //data = data.replaceAll(charToReplace, replacement);
               }
               field.setOccurrence(nOccurrences, data);
               /**
                * getFieldByTag will add a new entry in the fdt if the tag is
                * not found
                */
               FieldDefinition fd = fdt.getFieldByTag(iTag);
               // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
               // otc.println("**************\n" + iRec.toString());
               }
            if (leaderFields) {
               org.marc4j.marc.Leader leader = record.getLeader();
               for (int i = 0; i <= 23; i++) {
                  int iTag = 3000 + i;
                  /** getField creates the field if not found */
                  IField field = iRec.getField(iTag);
                  /**
                   * getFieldByTag will add a new entry in the fdt if the tag is
                   * not found
                   */
                  FieldDefinition fd = fdt.getFieldByTag(iTag);
                  String sleader = leader.marshal();
                  field.setOccurrence(0, leader.marshal().substring(i, i + 1));
               }
            }
            // We have now a complete ISIS record eqivalent to the MARC record
            if (!reformattingFST.equals("<none>")) {
               // MARC fields are changed according to the fst
               org.unesco.jisis.corelib.record.Record jRec =
                       (org.unesco.jisis.corelib.record.Record) org.unesco.jisis.corelib.record.Record.createRecord();
               // Build a new field from each FST entry
               for (int i = 0; i < fst.getEntriesCount(); i++) {
                  FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                  int iTag = entry.getTag();
                  // getField creates the field if not found
                  Field field = (Field) jRec.getField(iTag);
                  pftIL[i].setRecord(targetDB, iRec);
                  pftIL[i].eval();
                  String fieldVal = pftIL[i].getText();
                  if (fieldVal == null) {
                     continue;
                  }
                  // At this level, the new field may have occurrences and subfields
                  field.setFieldValue(fieldVal);
               }
               iRec = jRec;
            }
            try {
               if ((importOption == IMPORT_OPTION_UPDATE) && (MFN>0 && MFN <= targetDB.getLastMfn())) {
                  iRec.setMfn(MFN);
                  iRec = targetDB.updateRecord((org.unesco.jisis.corelib.record.Record) iRec);
               } else {

                  iRec = targetDB.addRecord((org.unesco.jisis.corelib.record.Record) iRec);
               }
            } catch (Exception ex) {
               Exceptions.printStackTrace(ex);
            }
            
            long mfn = iRec.getMfn();
            if (mfn % 10 == 0) {
               progress.setDisplayName("Performing Import MFN:" + mfn);
            }
            iRec = null;
            record = null;
         }
         progress.finish();
      } catch (FileNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } catch (MarcException e) {
         String s = e.getMessage();
         if (s.equals("EOF")) {
            /** Continue, eof detected */
            progress.finish();
         } else {
            throw new ImportException(e);
         }
      } catch (DbException dbe) {
         throw new ImportException(dbe);
      } finally {
         try {
            in.close();
         } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
         }
      }

      try {

         /**
          * these instructions should be part of the thread as it must be executed
          * once the DB is fully loaded
          */
         targetDB.saveFieldDefinitionTable(fdt);
         targetDB.resetDatabaseInfo();
         long recCount = targetDB.getRecordsCount();
         long maxMFN = targetDB.getLastMfn();
         //System.out.println("ImpExpTool recCount=" + recCount + " maxMFN=" + maxMFN);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      // targetDB.
//      DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Import completed",
//              NotifyDescriptor.INFORMATION_MESSAGE));
//      GuiGlobal.output("Last progress.finish()");
   }
   
   /**
    * Thread Task for importing Dublin Core records - The input is pre-processed using
    * a stylesheet that transform XML DC data to MARCXML.
    * The library of congress provides a stylesheet that transform MODS to
    * MARCXML
    * 
    * http://www.loc.gov/standards/marcxml/xslt/DC2MARC2121slim.xsl
    *
    * @param parameters
    * @param targetDB
    */
   private static void importDublinCoreTask(final Map<String, Object> parameters, final IDatabase targetDB) {
      final File importFile = (File) parameters.get("importFile");
      final String encoding = (String) parameters.get("encoding");
      /**
       * These 2 parameters doesn't need to be specify by the user 
       * inputLineLength is not used
       * subfieldDelimiter is set to $ (36) because marc4j use it as subfield
       * delimiter.
       * 
       */
      final int inputLineLength = 0; //(Integer) parameters.get("inputLineLength");
      final int subfieldDelimiter = (int) '$'; //(Integer) parameters.get("subfieldDelimiter");
      final String reformattingFST = (String) parameters.get("reformattingFST");
      final long renumberFromMFN = (Long) parameters.get("renumberFromMFN");
      final int inputTagMFN = (Integer) parameters.get("inputTagMFN");
      final int importOption = (Integer) parameters.get("importOption");
      final boolean leaderFields = ((Integer) parameters.get("leaderFields") == 1) ? true : false;
      final String stylesheetUrl = "http://www.loc.gov/standards/marcxml/xslt/DC2MARC21slim.xsl";


      FieldDefinitionTable fdt = null;
      String charToReplace = new Character((char) subfieldDelimiter).toString();
      String replacement = Global.SUBFIELD_SEPARATOR;

      ISISFormatter[] pftIL = null;

      FieldSelectionTable fst = null;
      try {
         fst = targetDB.getFst(reformattingFST);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      // Parse the reformatting FST to get the intermediate language (IL)
      // For further execution on the individual records
      if (!reformattingFST.equals("<none>")) {
         if ((pftIL = parseReformattingFST(targetDB, reformattingFST)) == null) {
            return;
         }
      }
      CancellableProgress cancellable = new CancellableProgress();
      final ProgressHandle progress =
              ProgressHandleFactory.createHandle("Importing Data...", cancellable);
      progress.start();
      progress.switchToIndeterminate();
      long MFN = -1L;

      InputStream in = null;
      try {
         in = new FileInputStream(importFile);
         final MarcReader reader = new MarcXmlReader(in, stylesheetUrl);
         fdt = targetDB.getFieldDefinitionTable();
         while (reader.hasNext()) {
            if (Thread.interrupted() || cancellable.cancelRequested()) {
               progress.finish();
               break;
            }
            Record record = null;
             try {
                 record = reader.next();
             } catch (Exception ex) {
                 progress.finish();
                 GuiGlobal.outputErr("Error when reading DC record");
                 GuiGlobal.outputErr(ex.toString());
                 break;
             }
            // Global.output("record: " + record.toString());
            IRecord iRec = org.unesco.jisis.corelib.record.Record.createRecord();
//          System.out.println("CONTROL FIELDS NUMBER" + record.getControlFields().size());
            Iterator it = record.getControlFields().iterator();
            while (it.hasNext()) {
               ControlField cf = (ControlField) it.next();
               int iTag = Integer.parseInt(cf.getTag());
                MFN = -1L;
               if (inputTagMFN == iTag) {
                  String sMFN = cf.getData();
                  MFN = Long.valueOf(sMFN);
                  continue;
               }
               /** getField creates the field if not found */
               IField field = iRec.getField(iTag);
               /**
                * All fields are supposed to have occurrences, a non repetetive
                * field will have 1 occurrence
                */
               int nOccurrences = field.getOccurrenceCount();
               String data = cf.getData();

               /**
                * Replace external subfield delimiter by ISIS one
                */
               if (subfieldDelimiter != 94) {
                  data = StringUtils.replaceChar(data,
                          charToReplace.charAt(0), replacement.charAt(0));
                  //data = data.replaceAll(charToReplace, replacement);
               }
               field.setOccurrence(nOccurrences, data);
               /**
                * getFieldByTag will add a new entry in the fdt if the tag is
                * not found
                */
               FieldDefinition fd = fdt.getFieldByTag(iTag);
               // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
               // otc.println("**************\n" + iRec.toString());
               }
            it = record.getDataFields().iterator();
            while (it.hasNext()) {
               DataField df = (DataField) it.next();
               int iTag = Integer.parseInt(df.getTag());

               StringBuffer sb = new StringBuffer();

               sb.append(df.getIndicator1());
               sb.append(df.getIndicator2());
               Iterator i = df.getSubfields().iterator();
               while (i.hasNext()) {
                  org.marc4j.marc.Subfield sf = (org.marc4j.marc.Subfield) i.next();
                  sb.append(sf.toString());
               }
               String data = sb.toString();
               /** getField creates the field if not found */
               IField field = iRec.getField(iTag);
               /**
                * All fields are supposed to have occurrences, a non repetetive
                * field will have 1 occurrence
                */
               int nOccurrences = field.getOccurrenceCount();

               /**
                * Replace external subfield delimiter by ISIS one
                */
               if (subfieldDelimiter != 94) {
                  data = StringUtils.replaceChar(data,
                          charToReplace.charAt(0), replacement.charAt(0));
                  // replaceAll uses regular expressions and some characters
                  // are meta characters
                  //data = data.replaceAll(charToReplace, replacement);
               }
               field.setOccurrence(nOccurrences, data);
               /**
                * getFieldByTag will add a new entry in the fdt if the tag is
                * not found
                */
               FieldDefinition fd = fdt.getFieldByTag(iTag);
               // iRec.getField(Integer.parseInt(cf.getTag())).setValue(cf.getData());
               // otc.println("**************\n" + iRec.toString());
               }
            if (leaderFields) {
               org.marc4j.marc.Leader leader = record.getLeader();
               for (int i = 0; i <= 23; i++) {
                  int iTag = 3000 + i;
                  /** getField creates the field if not found */
                  IField field = iRec.getField(iTag);
                  /**
                   * getFieldByTag will add a new entry in the fdt if the tag is
                   * not found
                   */
                  FieldDefinition fd = fdt.getFieldByTag(iTag);
                  String sleader = leader.marshal();
                  field.setOccurrence(0, leader.marshal().substring(i, i + 1));
               }
            }
            // We have now a complete ISIS record eqivalent to the MARC record
            if (!reformattingFST.equals("<none>")) {
               // MARC fields are changed according to the fst
               org.unesco.jisis.corelib.record.Record jRec =
                       (org.unesco.jisis.corelib.record.Record) org.unesco.jisis.corelib.record.Record.createRecord();
               // Build a new field from each FST entry
               for (int i = 0; i < fst.getEntriesCount(); i++) {
                  FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                  int iTag = entry.getTag();
                  // getField creates the field if not found
                  Field field = (Field) jRec.getField(iTag);
                  pftIL[i].setRecord(targetDB, iRec);
                  pftIL[i].eval();
                  String fieldVal = pftIL[i].getText();
                  if (fieldVal == null) {
                     continue;
                  }
                  // At this level, the new field may have occurrences and subfields
                  field.setFieldValue(fieldVal);
               }
               iRec = jRec;
            }
            try {
               if ((importOption == IMPORT_OPTION_UPDATE) && (MFN>0 && MFN <= targetDB.getLastMfn())) {
                  iRec.setMfn(MFN);
                  iRec = targetDB.updateRecord((org.unesco.jisis.corelib.record.Record) iRec);
               } else {

                  iRec = targetDB.addRecord((org.unesco.jisis.corelib.record.Record) iRec);
               }
            } catch (Exception ex) {
               Exceptions.printStackTrace(ex);
            }
            
            long mfn = iRec.getMfn();
            if (mfn % 10 == 0) {
               progress.setDisplayName("Performing Import MFN:" + mfn);
            }
            iRec = null;
            record = null;
         }
         progress.finish();
      } catch (FileNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } catch (MarcException e) {
         String s = e.getMessage();
         if (s.equals("EOF")) {
            /** Continue, eof detected */
            progress.finish();
         } else {
            throw new ImportException(e);
         }
      } catch (DbException dbe) {
         throw new ImportException(dbe);
      } finally {
         try {
            in.close();
         } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
         }
      }

      try {

         /**
          * these instructions should be part of the thread as it must be executed
          * once the DB is fully loaded
          */
         targetDB.saveFieldDefinitionTable(fdt);
         targetDB.resetDatabaseInfo();
         long recCount = targetDB.getRecordsCount();
         long maxMFN = targetDB.getLastMfn();
         //System.out.println("ImpExpTool recCount=" + recCount + " maxMFN=" + maxMFN);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      // targetDB.
//      DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Import completed",
//              NotifyDescriptor.INFORMATION_MESSAGE));
//      GuiGlobal.output("Last progress.finish()");
   }


   /**
    * Create a new Empty DB whith name "dbName" in directory defined by "dbHome"
    *
    * Empty FDT, FST, WKS, PFT will be created.
     * @param sourceDB
    * @param dbHome
    * @param dbName
    */
   public static void createEmptyDB(IDatabase sourceDB, String dbHome, String dbName) {
      CreateDbParams dbp = new CreateDbParams(dbHome, dbName);
      dbp.setFieldDefinitionTable(new FieldDefinitionTable());
      WorksheetDef wks = new WorksheetDef("Default worksheet");
      dbp.setDefaultWorkSheet(wks);
      dbp.setDefaultPft("defDispFormat", "");
      dbp.setFieldSelectionTable(new FieldSelectionTable());
      try {
         
         /* Note that createDatabase shut down the databases */
         sourceDB.createDatabase(dbp, Global.DATABASE_BULK_WRITE);
      } catch (DbException ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      } catch (Exception ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      }
      NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(ImpExpTool.class,
                              "MSG_CreateDatabaseWithPlainOldIsisFdtFst"));
      DialogDisplayer.getDefault().notify(d);
   }


   /**
    * Create a new Empty DB whith name "dbName" in directory defined by "dbHome"
    * The FDT, FST, WKS, PFT are plain old ISIS files
    * @param dbHome
    * @param dbName
    * @param fdtFile
    * @param fstFile
    * @param encoding
    * @return
    */
   public static CreateDbParams createNewDbParm(String dbHome, String dbName, String fdtFile,
           String fstFile, String encoding) {
      CreateDbParams dbp   = new CreateDbParams(dbHome, dbName);
      Date           start = new Date();
      dbp.setFieldDefinitionTable(ImpExpUtil.importOldFdt(fdtFile, encoding));
      Date end = new Date();
      GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to build FDT");
//    //create data entry worksheet
      WorksheetDef wks = new WorksheetDef("Default worksheet");
//    ArrayList wksData = wksModel.getData();
//    for (int i = 0; i < wksData.size(); i++) {
//        HashMap field = (HashMap) wksData.get(i);
//        String[] columns = wksModel.getColumns();
//        try {
//            int tag = Integer.parseInt(field.get(columns[0]).toString());
//            String desc=field.get(columns[1]).toString();
//            String defVal=field.get(columns[2]).toString();
//            String hlpMsg=field.get(columns[3]).toString();
//            String valid=field.get(columns[4]).toString();
//            String pickList=field.get(columns[5]).toString();
//            System.out.println(tag + " "+desc+" "+defVal+ " " +hlpMsg + " " +valid+" "+pickList);
//            wks.addField(tag, desc, defVal, hlpMsg, valid, pickList);
//        } catch (NumberFormatException ex) {
//            throw new org.openide.util.NotImplementedException(ex.getMessage());
//        }
//    }
//    wks.setRecordValidationFormat(recValFormat);
      dbp.setDefaultWorkSheet(wks);
      StringBuilder bufFormat = new StringBuilder();
      int          nFields   = dbp.getFieldDefinitionTable().getFieldsCount();
      for (int i = 0; i < nFields; i++) {
         bufFormat.append("V")
                  .append(Integer.toString(dbp.getFieldDefinitionTable().getFieldByIndex(i).getTag()))
                  .append(",/");
      }
      dbp.setDefaultPft("defDispFormat", bufFormat.toString());
      start = new Date();
      dbp.setFieldSelectionTable(ImpExpUtil.importOldFst(fstFile, encoding));
      end = new Date();
      GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to build FST");
      return dbp;
   }

   /**
    * Create a new DB from the info provided in a CreateDbParams object
     * @param sourceDB
    * @param dbp
    * @throws NotImplementedException
    */
   public static void createNewDb(IDatabase sourceDB, CreateDbParams dbp) throws NotImplementedException {
      Date start = new Date();
      try {
         
         /* Note that createDatabase shut down the databases */
         sourceDB.createDatabase(dbp, Global.DATABASE_BULK_WRITE);
      } catch (DbException ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      } catch (Exception ex) {
         throw new org.openide.util.NotImplementedException(ex.getMessage());
      }
      Date end = new Date();
      GuiGlobal.output(Long.toString(end.getTime() - start.getTime())
                    + " milliseconds to create Database files");
      NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(ImpExpTool.class,
                              "MSG_CreateDatabaseWithPlainOldIsisFdtFst"));
      DialogDisplayer.getDefault().notify(d);
   }

   

   private static MfnRange[] getMfnRanges(IDatabase db, int mfnSelectionOption,
           String mfnRangesString, int searchHistoryIndex, int markedHistoryIndex) {
      MfnRange[] mfnRanges = null;
      try {
         long from = db.getFirst().getMfn();
         long to = db.getLast().getMfn();

         List<Long> mfns = null;
         switch (mfnSelectionOption) {
            case Global.MFNS_OPTION_ALL:

               mfnRanges = new MfnRange[1];
               mfnRanges[0] = new MfnRange(from, to);
               break;
            case Global.MFNS_OPTION_RANGE:
               mfnRanges = Global.parseMfns(mfnRangesString);
               break;
            case Global.MFNS_OPTION_MARKED:
              if (markedHistoryIndex < 0) {
                  break;
               }
               List<MarkedRecords> markedRecordsList = ((ClientDatabaseProxy) db).getMarkedRecordsList();
               MarkedRecords markedRecords = markedRecordsList.get(markedHistoryIndex);
               mfns = markedRecords.getMfns();
               mfnRanges = new MfnRange[mfns.size()];
               for (int i = 0; i < mfns.size(); i++) {
                  long mfn = mfns.get(i);
                  mfnRanges[i] = new MfnRange(mfn, mfn);
               }
               break;
            case Global.MFNS_OPTION_SEARCH:
               List<SearchResult> searchResults = ((ClientDatabaseProxy) db).getSearchResults();
               SearchResult searchResult = searchResults.get(searchHistoryIndex);
               mfns = searchResult.getMfns();
               mfnRanges = new MfnRange[mfns.size()];
               for (int i = 0; i < mfns.size(); i++) {
                  long mfn = mfns.get(i);
                  mfnRanges[i] = new MfnRange(mfn, mfn);
               }
               break;
         }
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      return mfnRanges;
   }
 //-----------------------------------------------------------------------------
   // EXPORT METHODS
   //---------------------------------------------------------------------------
   /**
    * Export DB content as MARC records in format 2709 using a reformatting FST
    *
    * @param sourceDB
    * @param parameters
    * @return
    * @throws org.unesco.jisis.importexport.ExportException
    */
   private static File reformatExportISO2709(IDatabase sourceDB, Map<String, Object> parameters)
           throws ExportException {
      File                exportFile         = (File) parameters.get("selectedFile");
      String              isoFile            = (String) parameters.get("isoFile");
      int                 mfnSelectionOption = (Integer) parameters.get("mfnSelectionOption");
      String              mfnRangesString    = (String) parameters.get("mfnRanges");
      int                 outputLineLength   = (Integer) parameters.get("outputLineLength");
      String              reformattingFST    = (String) parameters.get("reformattingFST");
      int                 renumberFromMFN    = (Integer) parameters.get("renumberFromMFN");
      int                 outputTagMFN       = (Integer) parameters.get("outputTagMFN");
      String              encoding           = (String) parameters.get("encoding");
      int                 fieldTerminator    = (Integer) parameters.get("fieldTerminator");
      int                 recordTerminator   = (Integer) parameters.get("recordTerminator");
      int                 subfieldDelimiter  = (Integer) parameters.get("subfieldDelimiter");
      int                 searchHistoryIndex = (Integer) parameters.get("searchHistoryIndex");
      int                 markedHistoryIndex = (Integer) parameters.get("markedHistoryIndex");
      ISISFormatter[]     pftIL              = null;
      FieldSelectionTable fst                = null;

      if (reformattingFST.equals("<none>")) {
         return null;
      }
      // Parse the reformatting FST to get the intermediate language (IL)
      // For further execution on the individual records

      if ((pftIL = parseReformattingFST(sourceDB, reformattingFST)) == null) {
         return null;
      }
       try {
          fst = sourceDB.getFst(reformattingFST);
       } catch (DbException ex) {
          throw new ExportException(ex);
       }
      
      String dbname;
      try {
         dbname = sourceDB.getDatabaseName();
      } catch (DbException ex) {
         throw new ExportException(ex);
      }
      exportFile = new File(exportFile.getAbsolutePath());
      MarcFactory factory = MarcFactory.newInstance();
      try {
         MyMarcStreamWriter writer = new MyMarcStreamWriter(new FileOutputStream(exportFile),
                                        encoding);
         if (fieldTerminator != -1) {
            writer.setFieldTerminator(fieldTerminator);
         }
         if (recordTerminator != -1) {
            writer.setRecordTerminator(recordTerminator);
         }
         if (subfieldDelimiter != -1) {
            writer.setSubfieldDelimiter(subfieldDelimiter);
         }
          long outputMFN = (renumberFromMFN == -1)
                          ? 0
                          : renumberFromMFN - 1;
         IRecord    iRec;
         String     leader    = "000000000000000000004500";
         MfnRange[] mfnRanges = getMfnRanges(sourceDB, mfnSelectionOption, mfnRangesString,
                                             searchHistoryIndex, markedHistoryIndex);
        
         for (MfnRange r : mfnRanges) {
            long startMFN = r.getFirst();
            long endMFN   = r.getLast();
            for (long k = startMFN; k <= endMFN; k++) {
               iRec = sourceDB.getRecordCursor(k);
               if (iRec == null) {
                   GuiGlobal.outputErr("Record with MFN "+k+" not found and skipped");
                   continue;
               }
               org.marc4j.marc.Record record = factory.newRecord(leader);
               // System.out.println("exporting the record " + iRec.toString());
               // Add the Field for the MFN (option "Output Tag containing MFN"
               if (outputTagMFN != -1) {
                  outputMFN++;
                  long   mfn = (renumberFromMFN == -1)
                               ? iRec.getMfn()
                               : outputMFN;
                  String tag = Integer.toString(outputTagMFN);
                  if (tag.length() == 1) {
                     tag = "00" + tag;
                  } else if (tag.length() == 2) {
                     tag = "0" + tag;
                  }
                  record.addVariableField(factory.newControlField(tag, Long.toString(mfn)));
               }
               for (int i = 0; i < fst.getEntriesCount(); i++) {
                  FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                  pftIL[i].setRecord(sourceDB, iRec);
                  pftIL[i].eval();
                  String fieldVal = pftIL[i].getText();
                  if (fieldVal == null) {
                     continue;
                  }
                   // At this level, the new field may have occurrences and subfields
                  Field field = (Field) FieldFactory.makeField(entry.getTag());
                   /**
                    * Each line produced by the format ( or each element, if the FST specifies indexing
                    * techniques 2, 3, or 4) will generate a new occurrence of the output field.
                    *
                    */
                   StringTokenizer st = new StringTokenizer(fieldVal, Global.NEWLINE, false);
                   int l = 0;
                   while (st.hasMoreTokens()) {
                       field.setOccurrence(l, st.nextToken());
                       l++;
                   }                  
                  convertIsis2IsoField(record, factory, field, subfieldDelimiter);
                  
               }
               //System.out.println(record.toString());
               if (outputLineLength == 0) {
                  writer.write(record);
               } else {
                  writer.writeAsLines(record, outputLineLength);
               }
            }
         }
         writer.close();
      } catch (DbException ex) {
         throw new ExportException(ex);
      } catch (IOException ioex) {
         throw new ExportException(ioex);
      }
      NotifyDescriptor d = new NotifyDescriptor.Message("Exported to file: "
                              + exportFile.getAbsolutePath(), NotifyDescriptor.INFORMATION_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return exportFile;
   }

   private static void convertIsis2IsoField(org.marc4j.marc.Record record, MarcFactory factory, IField field,
                                             int subfieldDelimiter) {
      
      String tag = Integer.toString(field.getTag());
      if (tag.length() == 1) {
         tag = "00" + tag;
      } else if (tag.length() == 2) {
         tag = "0" + tag;
      }
      //Add a field for each occurrence
      int nOccurrences = field.getOccurrenceCount();
      for (int j = 0; j < nOccurrences; j++) {
         String fieldVal = (String) field.getOccurrenceValue(j);
         if (subfieldDelimiter != -1) {
            fieldVal = StringUtils.replaceChar(fieldVal, 94,subfieldDelimiter);
         }
         record.addVariableField(factory.newControlField(tag, fieldVal));
      }
   }

    public static void exportISO2709(final IDatabase sourceDB, final Map<String, Object> parameters) {
        try {
            GuiGlobal.output("Starting ISO 2709 export");
            final Date start = new Date();
            Runnable exportRun = new Runnable() {
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            exportISO2709Task(sourceDB, parameters);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        Date end = new Date();
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime())
                            + " milliseconds to export DB on ISO file");
                        NotifyDescriptor d = new NotifyDescriptor.Message("Export is done!");
                        DialogDisplayer.getDefault().notify(d);
                    }
                }
            };

            requestProcessor_.post(exportRun);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
   /**
    * Export DB content as MARC records in format 2709
    *
    * @param sourceDB
    * @param parameters
    * @return
    * @throws ExportException
    */
    public static File exportISO2709Task(IDatabase sourceDB, Map<String, Object> parameters)
        throws ExportException {
        File exportFile = (File) parameters.get("selectedFile");
        String isoFile = (String) parameters.get("isoFile");
        int mfnSelectionOption = (Integer) parameters.get("mfnSelectionOption");
        String mfnRangesString = (String) parameters.get("mfnRanges");
        int outputLineLength = (Integer) parameters.get("outputLineLength");
        String reformattingFST = (String) parameters.get("reformattingFST");
        int renumberFromMFN = (Integer) parameters.get("renumberFromMFN");
        int outputTagMFN = (Integer) parameters.get("outputTagMFN");
        String encoding = (String) parameters.get("encoding");
        int fieldTerminator = (Integer) parameters.get("fieldTerminator");
        int recordTerminator = (Integer) parameters.get("recordTerminator");
        int subfieldDelimiter = (Integer) parameters.get("subfieldDelimiter");
        int searchHistoryIndex = (Integer) parameters.get("searchHistoryIndex");
        int markedHistoryIndex = (Integer) parameters.get("markedHistoryIndex");
        String dbName;

        CancellableProgress cancellable = new CancellableProgress();
        final ProgressHandle progress
            = ProgressHandleFactory.createHandle("Exporting Data...", cancellable);
        progress.start();
        progress.switchToIndeterminate();

        try {
            if (!reformattingFST.equals("<none>")) {
                exportFile = reformatExportISO2709(sourceDB, parameters);
            } else {

                dbName = sourceDB.getDatabaseName();

                exportFile = new File(exportFile.getAbsolutePath());
                MarcFactory factory = MarcFactory.newInstance();
                MyMarcStreamWriter writer = new MyMarcStreamWriter(new FileOutputStream(exportFile),
                    encoding);

                if (encoding.equals("MARC-8") || encoding.equals("ISO5426")
                    || encoding.equals("ISO6937")) {
                    CharConverter converter;
                    if (encoding.equals("MARC-8") || encoding.equals("MARC8")) {
                        converter = new UnicodeToAnsel();
                    } else if (encoding.equalsIgnoreCase("Unimarc") || encoding.equals("ISO5426")) {
                        converter = new UnicodeToIso5426();

                    } else {
                        converter = new UnicodeToIso6937();

                    }
                    writer.setConverter(converter);
                    //writer.setUnicodeNormalization(true);
                }

                if (fieldTerminator != -1) {
                    writer.setFieldTerminator(fieldTerminator);
                }
                if (recordTerminator != -1) {
                    writer.setRecordTerminator(recordTerminator);
                }
                if (subfieldDelimiter != -1) {
                    writer.setSubfieldDelimiter(subfieldDelimiter);
                }
                long outputMFN = (renumberFromMFN == -1)
                    ? 0
                    : renumberFromMFN - 1;
                IRecord iRec;
                /*
                 * ---------------------------------------------
                 * The leader is 24 characters
                 *    0:4  Record length including the record length and record terminator
                 *    5:5  Status
                 *    6:6  Type of record
                 *    7:9  Implementation Defined Position
                 *   10:10 Indicator count
                 *   11:11 Identifier length
                 *   12:16 Base address of data
                 *   17:19 Implementation Defined Positions
                 *   20:23 Entry Map
                 *         Below:
                 *           4 in 20:20 is the length of the length-of-field portion
                 *             of each directory entry.
                 *         5 in 21:21 is the length of the starting-character-position of
                 *         each entry
                 *         0 in 22:22 is the length of the implementation-defined portion
                 *          of each entry.
                 *         0 in 23:23 is reserved for future use.
                 * ----------------------------------------------------------
                 */

                MfnRange[] mfnRanges = getMfnRanges(sourceDB, mfnSelectionOption, mfnRangesString,
                    searchHistoryIndex, markedHistoryIndex);
                boolean cancelled = false;
                for (MfnRange r : mfnRanges) {
                    if (Thread.interrupted() || cancelled) {
                        break;
                    }
                    long startMFN = r.getFirst();
                    long endMFN = r.getLast();
                    for (long k = startMFN; k <= endMFN; k++) {
                        if (Thread.interrupted() || cancellable.cancelRequested()) {
                            progress.finish();
                            cancelled = true;
                            break;
                        }
                        iRec = sourceDB.getRecordCursor(k);
                        if (iRec == null) {
                            GuiGlobal.outputErr("Record with MFN " + k + " not found and skipped");
                            continue;
                        }
                        // Export only if record with MFN k exists
                        if (iRec != null) {
                            String leader = "000000000000000000004500";
                            int nFields = iRec.getFieldCount();

                            for (int i = 0; i < nFields; i++) {
                                int itag = iRec.getFieldByIndex(i).getTag();
                                if (itag >= 3000 && itag <= 3023) {
                                    String fieldVal = iRec.getFieldByIndex(i).getStringFieldValue();
                                    if (fieldVal != null && fieldVal.length() > 0) {
                                        leader = StringUtils.replaceCharAt(leader,
                                            itag - 3000, fieldVal.charAt(0));

                                    }
                                }
                            }
                            if (encoding.equalsIgnoreCase("UTF-8")) {
                                leader = StringUtils.replaceCharAt(leader, 9, 'a');
                            }
                            org.marc4j.marc.Record record = factory.newRecord(leader);
                  // System.out.println("exporting the record " + iRec.toString());
                            // Add the Field for the MFN (option "Output Tag containing MFN")
                            if (outputTagMFN != -1) {
                                outputMFN++;
                                long mfn = (renumberFromMFN == -1)
                                    ? iRec.getMfn()
                                    : outputMFN;
                                String tag = Integer.toString(outputTagMFN);
                                if (tag.length() == 1) {
                                    tag = "00" + tag;
                                } else if (tag.length() == 2) {
                                    tag = "0" + tag;
                                }
                                record.addVariableField(factory.newControlField(tag, Long.toString(mfn)));
                            }
                            // Add the record fields occurrence by occurrence

                            for (int i = 0; i < nFields; i++) {
                                IField field = iRec.getFieldByIndex(i);
                                if (field.getTag() >= 1000) {
                                    // Skip fields with more than 3 digits
                                    continue;
                                }
                                convertIsis2IsoField(record, factory, field, subfieldDelimiter);
                            }
                            // System.out.println(record.toString());
                            if (outputLineLength == 0) {
                                writer.write(record);
                            } else {
                                writer.writeAsLines(record, outputLineLength);
                            }
                        }
                    } // MFN Loop
                } // Range Loop
                writer.close();
            }
            progress.finish();
        } catch (DbException ex) {
            progress.finish();
            throw new ExportException(ex);
        } catch (IOException ioex) {
            progress.finish();
            throw new ExportException(ioex);
        }
        NotifyDescriptor d = new NotifyDescriptor.Message("Exported to file: " + exportFile.getAbsolutePath(), NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        return exportFile;
    }

   /**
    *
    * @param sourceDB
    * @param parameters
    * @return
    * @throws ExportException
    */
    public static File exportMODS(final IDatabase sourceDB, final Map<String, Object> parameters)
        throws ExportException {
        final File exportFileParm = (File) parameters.get("selectedFile");
        String modsFile = (String) parameters.get("modsFile");

        try {
//          final String dbname = sourceDB.getDatabaseName();
//                     final File exportFile = new File(exportFileParm.getAbsolutePath());
//
//                     String stylesheetUrl = "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3-4.xsl";
//                     Source stylesheet = new StreamSource(stylesheetUrl);
//                     Result result = null;
//                     try {
//                        result = new StreamResult(new FileOutputStream(exportFile));
//                     } catch (FileNotFoundException ex) {
//                        Exceptions.printStackTrace(ex);
//                     }
//         final MyMarcXmlWriter writer = new MyMarcXmlWriter(result, stylesheet);
//                     writer.setIndent(true);
//                     writer.setConverter(new AnselToUnicode());

            final String dbname = sourceDB.getDatabaseName();
            final File exportFile = new File(exportFileParm.getAbsolutePath());

            String stylesheetUrl = "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3-4.xsl";
            Source stylesheet = new StreamSource(stylesheetUrl);
            Result result = null;
            try {
                result = new StreamResult(new FileOutputStream(exportFile));
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            final MyMarcXmlWriter writer = new MyMarcXmlWriter(result, stylesheet);
            writer.setConverter(new AnselToUnicode());
            writer.setIndent(true);
            GuiGlobal.output("Starting MODS export");
            final Date start = new Date();

            Runnable exportRun = new Runnable() {
                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            exportMarcXMLTask(writer, sourceDB, parameters);
                        } catch (Exception ex) {
                            if (writer != null) {
                                writer.close();
                            }
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);                           
                        }
                    } else {
                        // Second Invocation, we are on the event queue now (EDT)
                        writer.close();
                        Date end = new Date();
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to export DB on MODS file");
                         NotifyDescriptor d = new NotifyDescriptor.Message("Export is done!");
                            DialogDisplayer.getDefault().notify(d);
                    }
                }
            };

            requestProcessor_.post(exportRun);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return exportFileParm;
    }
    public static File exportDC(final IDatabase sourceDB, final Map<String, Object> parameters)
        throws ExportException {
        final File exportFileParm = (File) parameters.get("selectedFile");
        final String marcxml2RDFDC = "http://www.loc.gov/standards/marcxml/xslt/MARC21slim2RDFDC.xsl";
        final String marcxml2OAIDC = "http://www.loc.gov/standards/marcxml/xslt/MARC21slim2OAIDC.xsl";
        final String marcxml2SRWDC = "http://www.loc.gov/standards/marcxml/xslt/MARC21slim2SRWDC.xsl";

        String modsFile = (String) parameters.get("modsFile");

        try {
            final String dbname = sourceDB.getDatabaseName();
            final File exportFile = new File(exportFileParm.getAbsolutePath());

            int dcOption = (Integer) parameters.get("dcOption");
            String stylesheetUrlTmp = marcxml2OAIDC;
            if (dcOption == Global.DUBLIN_CORE_RDF) {
                stylesheetUrlTmp = marcxml2RDFDC;
            } else if (dcOption == Global.DUBLIN_CORE_SRW) {
                stylesheetUrlTmp = marcxml2SRWDC;
            }
            final String stylesheetUrl = stylesheetUrlTmp;

            Source stylesheet = new StreamSource(stylesheetUrl);
            Result result = null;
            try {
                result = new StreamResult(new FileOutputStream(exportFile));
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            final MyMarcXmlWriter writer = new MyMarcXmlWriter(result, stylesheet);
            writer.setConverter(new AnselToUnicode());

            GuiGlobal.output("Starting DC export");
            final Date start = new Date();

            Runnable exportRun = new Runnable() {

                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            exportMarcXMLTask(writer, sourceDB, parameters);
                        } catch (Exception ex) {
                            if (writer != null) {
                                writer.close();
                            }
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        writer.close();
                        Date end = new Date();
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to export DB on DC file");
                        NotifyDescriptor d = new NotifyDescriptor.Message("Export is done!");
                        DialogDisplayer.getDefault().notify(d);

                    }
                }
            };

            requestProcessor_.post(exportRun);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return exportFileParm;
    }
   /**
    *  Parse the reformatting FST to get the intermediate language (IL)
    *  For further execution on the individual records
    *
    * @param sourceDB
    * @param reformattingFST
    * @return
    */
   private static ISISFormatter[] parseReformattingFST(IDatabase sourceDB, String reformattingFST) {
      // Parse the reformatting FST to get the intermediate language (IL)
      // For further execution on the individual records
      ISISFormatter[] pftIL = null;
      FieldSelectionTable fst = null;
      try {
         fst = sourceDB.getFst(reformattingFST);
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
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      return pftIL;
   }

    private static void convertIsis2MarcField(org.marc4j.marc.Record record, MarcFactory factory, IField field) {
        String fieldVal = field.getStringFieldValue();
        if (fieldVal != null) {
            int itag = field.getTag();
            String tag = Integer.toString(itag);
            //System.out.println("tag=" + itag + "\n$$$$$$$:" + field.getStringFieldValue());
            if (tag.length() == 1) {
                tag = "00" + tag;
            } else if (tag.length() == 2) {
                tag = "0" + tag;
            }
            if (itag == 1) {
                // Special control field
                record.addVariableField(factory.newControlField(tag, fieldVal));
            } else if (itag > 1 && itag < 10) {
            // Control fields are identified by tags 002 through 009
                // They may have several occurences !
                int noccurrences = field.getOccurrenceCount();
                for (int j = 0; j < noccurrences; j++) {
                    StringOccurrence occurrence = (StringOccurrence) field.getOccurrence(j);
                    fieldVal = occurrence.getValue();
                    record.addVariableField(factory.newControlField(tag, fieldVal));
                }
            } else if (itag > 9 && itag < 1000) {
            // Data field are identified by rags 010 through 999
                // Loop on the number of occurrences

                int noccurrences = field.getOccurrenceCount();
                for (int j = 0; j < noccurrences; j++) {
                    // Create a field for each occurrence
                    DataField dataFld = factory.newDataField();
                    dataFld.setIndicator1(' ');
                    dataFld.setIndicator2(' ');
                    dataFld.setTag(tag);
                    StringOccurrence occurrence = (StringOccurrence) field.getOccurrence(j);
                    int n = occurrence.getSubfieldCount();
                    // If number of subfields equal 1 and subfield code equal "*"
                    // Loop on the subfields
                    for (int ks = 0; ks < n; ks++) {
                        org.unesco.jisis.corelib.record.Subfield subfield
                            = occurrence.getSubfield(ks);
                        if (subfield.getSubfieldCode() == '*') {
                            // Could be indicators or data    
                            if (subfield.getData().length() == 2 && n > 1) {
                                // First subfield without delimiter-code pair
                                // This field is supposed to contain the field
                                // indicators
                                String indicators = subfield.getData();

                                dataFld.setIndicator1(indicators.charAt(0));
                                dataFld.setIndicator2(indicators.charAt(1));
                                continue; // process next subfield
                            }
                        }
                        Subfield marc4jSubfield = factory.newSubfield();
                        marc4jSubfield.setData(subfield.getData());
                        marc4jSubfield.setCode(subfield.getSubfieldCode());
                        dataFld.addSubfield(marc4jSubfield);

                    } // Loop on subfields
                    record.addVariableField(dataFld);
                } // Loop on occurrences           
            }
        }
    }

   private static File exportMarcXMLTask(MarcWriter writer, IDatabase sourceDB, Map<String, Object> parameters) throws ExportException {
      ISISFormatter[] pftIL = null;
      FieldSelectionTable fst = null;




      File exportFile = (File) parameters.get("selectedFile");
      String marcXmlFile = (String) parameters.get("marcXmlFile");
      int mfnSelectionOption = (Integer) parameters.get("mfnSelectionOption");
      String mfnRangesString = (String) parameters.get("mfnRanges");
      String reformattingFST = (String) parameters.get("reformattingFST");
      int renumberFromMFN = (Integer) parameters.get("renumberFromMFN");
      int outputTagMFN = (Integer) parameters.get("outputTagMFN");
//      String encoding = (String) parameters.get("encoding");
//      int fieldTerminator = (Integer) parameters.get("fieldTerminator");
//      int recordTerminator = (Integer) parameters.get("recordTerminator");
//      int subfieldDelimiter = (Integer) parameters.get("subfieldDelimiter");
      int searchHistoryIndex = (Integer) parameters.get("searchHistoryIndex");
      int markedHistoryIndex = (Integer) parameters.get("markedHistoryIndex");


      CancellableProgress  cancellable       = new CancellableProgress();
      final ProgressHandle progress          =
         ProgressHandleFactory.createHandle("Exporting Data...", cancellable);
      progress.start();
      progress.switchToIndeterminate();

      // Parse the reformatting FST to get the intermediate language (IL)
      // For further execution on the individual records
      if (!reformattingFST.equals("<none>")) {
         if ((pftIL = parseReformattingFST(sourceDB, reformattingFST)) == null) {
            return null;
         }
      }
      try {
           fst = sourceDB.getFst(reformattingFST);
       } catch (DbException ex) {
          throw new ExportException(ex);
       }

      try {
         IRecord iRec;
         long outputMFN = (renumberFromMFN == -1)
                          ? 0
                          : renumberFromMFN - 1;
         MarcFactory factory = MarcFactory.newInstance();
         
         MfnRange[] mfnRanges = getMfnRanges(sourceDB, mfnSelectionOption, mfnRangesString,
                 searchHistoryIndex, markedHistoryIndex);
         boolean cancelled = false;
         // Loop over the mfn's ranges
         for (MfnRange r : mfnRanges) {
            if (Thread.interrupted() || cancelled) {
               break;
            }
            long startMFN = r.getFirst();
            long endMFN   = r.getLast();
            for (long k = startMFN; k <= endMFN; k++) {
               if (Thread.interrupted() || cancellable.cancelRequested()) {
                  progress.finish();
                  cancelled = true;
                  break;
               }
               //System.out.println("Output mfn=" + k);
               iRec = sourceDB.getRecordCursor(k);
                if (iRec == null) {
                    GuiGlobal.outputErr("Record with MFN " + k + " not found and skipped");
                    continue;
                }
               int nFields = iRec.getFieldCount();
               // Build the record leader, we assume that:
               // leader/00 -> 3000
               // leader/01 -> 3001
               // .......
               // leader/23 -> 3023
               String leader = "000000a00000000000004500";
               for (int i = 0; i < nFields; i++) {
                  int itag = iRec.getFieldByIndex(i).getTag();
                  if (itag >= 3000 && itag <= 3023) {
                     String fieldVal = iRec.getFieldByIndex(i).getStringFieldValue();
                     if (fieldVal != null && fieldVal.length() > 0) {
                        leader = StringUtils.replaceCharAt(leader,
                                itag - 3000, fieldVal.charAt(0));

                     }
                  }
               }
               // System.out.println("exporting the record " + iRec.toString());
               org.marc4j.marc.Record record = factory.newRecord(leader);

               // reassign an MFN to output records starting from outputMFN
               // if renumberFromMFN is not specified, the output records retain
               // their original MFN.
               if (outputTagMFN != -1) {
                  outputMFN++;
                  long mfn = (renumberFromMFN == -1)
                          ? iRec.getMfn()
                          : outputMFN;
                  String tag = Integer.toString(outputTagMFN);
                  if (tag.length() == 1) {
                     tag = "00" + tag;
                  } else if (tag.length() == 2) {
                     tag = "0" + tag;
                  }
                  record.addVariableField(factory.newControlField(tag, Long.toString(mfn)));
               }
               if (!reformattingFST.equals("<none>")) {
                  // Output of fields is driven by the fst
                  for (int i = 0; i < fst.getEntriesCount(); i++) {
                     FieldSelectionTable.FstEntry entry = fst.getEntryByIndex(i);
                     pftIL[i].setRecord(sourceDB, iRec);
                     pftIL[i].eval();
                     String fieldVal = pftIL[i].getText();
                     if (fieldVal == null) {
                        continue;
                     }
                      // At this level, the new field may have occurrences and subfields
                      Field field = (Field) FieldFactory.makeField(entry.getTag());

                      /**
                       * Each line produced by the format ( or each element, if the FST specifies indexing
                       * techniques 2, 3, or 4) will generate a new occurrence of the output field.
                       *
                       */
                      StringTokenizer st = new StringTokenizer(fieldVal, Global.NEWLINE, false);
                      int l = 0;
                      while (st.hasMoreTokens()) {
                          field.setOccurrence(l, st.nextToken());
                          l++;
                      }

                      convertIsis2MarcField(record, factory, field);

                   }
               } else {

                  // Loop on the number of fields
                  for (int i = 0; i < nFields; i++) {
                     IField field = iRec.getFieldByIndex(i);
                     if (field.getTag() >= 1000) {
                        // Skip fields with more than 3 digits
                        continue;
                     }
                     convertIsis2MarcField(record, factory, field);

                  } // Loop on fields
               }
               // System.out.println(record.toString());
               writer.write(record);
            }
         }
         progress.finish();

      } catch (DbException ex) {
         progress.finish();
         throw new ExportException(ex);

      }
      NotifyDescriptor d = new NotifyDescriptor.Message("Exported to file: " + exportFile.getAbsolutePath(), NotifyDescriptor.INFORMATION_MESSAGE);
      DialogDisplayer.getDefault().notify(d);
      return exportFile;
   }

    public static File exportMarcXML(final IDatabase sourceDB, final Map<String, Object> parameters)
        throws ExportException {

        final File exportFileParm = (File) parameters.get("selectedFile");
        try {

            final String dbname = sourceDB.getDatabaseName();
            final File exportFile = new File(exportFileParm.getAbsolutePath());

            final MarcWriter writer = new MyMarcXmlWriter(new FileOutputStream(exportFile), true);

            GuiGlobal.output("Starting MARCXML export");
            final Date start = new Date();

            Runnable exportRun = new Runnable() {

                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        try {
                            exportMarcXMLTask(writer, sourceDB, parameters);
                        } catch (Exception ex) {
                            if (writer != null) {
                                writer.close();
                            }
                            Exceptions.printStackTrace(ex);
                        } finally {
                            EventQueue.invokeLater(this);
                        }
                    } else {
                        // Second Invocation, we are on the event queue now
                        if (writer != null) {
                            writer.close();
                        }
                        Date end = new Date();
                        GuiGlobal.output(Long.toString(end.getTime() - start.getTime()) + " milliseconds to export DB on MARCXML file");
                        NotifyDescriptor d = new NotifyDescriptor.Message("Export is done!");
                        DialogDisplayer.getDefault().notify(d);
                    }
                }
            };
            requestProcessor_.post(exportRun);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return exportFileParm;
    }



   

   static class CancellableProgress implements Cancellable {
      private boolean cancelled = false;

      public boolean cancel() {
         cancelled = true;
         return true;
      }

      public boolean cancelRequested() {
         return cancelled;
      }
   }

   
}
