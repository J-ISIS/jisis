/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisiscore.client;

import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.client.ClientDatabaseInfo;
import org.unesco.jisis.corelib.client.RemoteDatabase;
import org.unesco.jisis.corelib.common.*;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.index.DictionaryTerm;
import org.unesco.jisis.corelib.index.JisisDocument;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.picklist.ValidationData;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.corelib.server.HandleDbRequest;
import org.unesco.jisis.corelib.server.RecordLock;

public class ClientDatabaseProxy implements IDatabase, IDatabaseEx {

   private boolean databaseChanged_ = false;
   private long currentRecordMfn_ = -1L;
   private boolean indexChanged_ = false;
   private boolean fstChanged_ = false;
   private boolean fdtChanged_ = false;
   private boolean pftChanged_ = false;
   private boolean wksChanged_ = false;
   private boolean searchHistoryChanged_ = false;
   private boolean markedRecordsHistoryChanged_ = false;
   private boolean hitSortResultChanged_ = false;
   private final ArrayList<TopComponent> windows_;    // Windows opened
   private boolean hitSortFileChanged_ = false;
   private final ObservableEx dbChangeObservers_;
   private RemoteDatabase db_;
   /** Null means use the dfault font */
   private Font displayFont_ = null;

   private final List<SearchResult> searchResults_;
   private int searchNumber_ = 0;

   private final List<MarkedRecords> markedRecordsList_;
   private int markedRecordsSetNumber_ = 0;

   private List<HitSortResult> hitSortResults_;
   private int hitSortResultNumber_ = 0;
   
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HandleDbRequest.class);


    private ClientDatabaseProxy() {

        dbChangeObservers_ = new ObservableEx() {
        };
        windows_ = new ArrayList<TopComponent>();
        searchResults_ = new ArrayList<SearchResult>();
        markedRecordsList_ = new ArrayList<MarkedRecords>();

    }

    public ClientDatabaseProxy(IConnection connection) {
        dbChangeObservers_ = new ObservableEx() {
        };
        windows_ = new ArrayList<>();
        db_ = new RemoteDatabase(connection);
        searchResults_ = new ArrayList<SearchResult>();
        markedRecordsList_ = new ArrayList<MarkedRecords>();
    }

    public ClientDatabaseProxy(IConnection connection, int dbId) {
        dbChangeObservers_ = new ObservableEx() {
        };
        windows_ = new ArrayList<TopComponent>();
        db_ = new RemoteDatabase(connection, dbId);
        searchResults_ = new ArrayList<SearchResult>();
        markedRecordsList_ = new ArrayList<MarkedRecords>();
    }

   @Override
   public void addObserver(Observer newObserver) {
      dbChangeObservers_.addObserver(newObserver);
   }

   @Override
   public void deleteObserver(Observer observer) {
      dbChangeObservers_.deleteObserver(observer);
   }

   public void addWindow(TopComponent topComponent) {
      windows_.add(topComponent);
   }

   public void deleteWindow(TopComponent topComponent) {
      windows_.remove(topComponent);
   }
   
   public ArrayList<TopComponent> getTopComponents() {
      return windows_;
   }
   
   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ClientDatabaseProxy)) {
         return false;
         
      }
      ClientDatabaseProxy clientDatabaseProxy = (ClientDatabaseProxy) o;
      try {
         if (clientDatabaseProxy.getDatabaseName().equals(this.getDatabaseName()) 
                 &&
             clientDatabaseProxy.getConnection().getServer().equals(this.getConnection().getServer())
                 &&
              clientDatabaseProxy.getID() == this.getID()   
                 ) {
            return true;
         }
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      return false;
   }

   protected void changeNotify() {
      System.out.println("NotifyObservers");
      dbChangeObservers_.setChanged();
      dbChangeObservers_.notifyObservers();
   }

   public void setDatabaseChanged() {
      databaseChanged_ = true;
      indexChanged_ = true;
      changeNotify();
      databaseChanged_ = false;
      indexChanged_ = false;
   }

   public boolean databaseHasChanged() {
      return databaseChanged_;
   }

   public boolean fdtHasChanged() {
      return fdtChanged_;
   }

   public boolean fstHasChanged() {
      return fstChanged_;
   }

   public void setFstChanged() {
      fstChanged_ = true;
      changeNotify();
      fstChanged_ = false;
   }

   public void setFdtChanged() {
      fdtChanged_ = true;
      changeNotify();
      fdtChanged_ = false;
   }

   public void setIndexChanged() {
      indexChanged_ = true;
      changeNotify();
      indexChanged_ = false;
   }

   public boolean indexHasChanged() {
      return indexChanged_;
   }

   public void setPftChanged() {
      pftChanged_ = true;
      changeNotify();
      pftChanged_ = false;
   }

   public boolean pftHasChanged() {
      return pftChanged_;
   }

   public void setWksChanged() {
      wksChanged_ = true;
      changeNotify();
      wksChanged_ = false;
   }

   public boolean wksHasChanged() {
      return wksChanged_;
   }

   public void setHitSortFileChanged() {
      hitSortFileChanged_ = true;
      changeNotify();
      hitSortFileChanged_ = false;
   }

   public boolean hitSortFileHasChanged() {
      return hitSortFileChanged_;
   }

    public void setSearchHistoryChanged() {
      searchHistoryChanged_ = true;
      changeNotify();
      searchHistoryChanged_ = false;
   }

   public boolean searchHistoryHasChanged() {
      return searchHistoryChanged_;
   }

   public void setMarkedRecordsHistoryChanged() {
      markedRecordsHistoryChanged_ = true;
      changeNotify();
      markedRecordsHistoryChanged_ = false;
   }

   public boolean markedRecordsHasChanged() {
      return markedRecordsHistoryChanged_;
   }

   public void setHitSortResultChanged() {
      hitSortResultChanged_ = true;
      changeNotify();
      hitSortResultChanged_ = false;
   }

   public boolean hitSortResultHasChanged() {
      return hitSortResultChanged_;
   }


   @Override
   public void getDatabase(String dbHome, String dbName, int bulkWrite) throws DbException {
      db_.getDatabase(dbHome, dbName, bulkWrite);
      if (db_.getErrorMsg() != null) {
         GuiGlobal.outputErr(db_.getErrorMsg());
      }
      fillDatabaseInfo();
      /* register the DB for this connection */
      ConnectionPool.addDatabase(db_.getConnection(), this);
     
      getFirst();
   }

   @Override
   public boolean createDatabase(CreateDbParams createDbParam, int bulkWrite) throws DbException {
      boolean ret = db_.createDatabase(createDbParam, bulkWrite);
       if (db_.getErrorMsg() != null) {
         GuiGlobal.outputErr(db_.getErrorMsg());
      }
      return ret;
   }

   @Override
   public int getID() {
      int dbId = db_.getID();
      return dbId;
   }
   
   /**
    * Close all the TopComponents built for this database - This method only affects the GUI components
    * @return
    * @throws DbException 
    */
    public void closeTopComponents() throws DbException {
      /*
       *  We need to clone the array because the close function in the
       * TopComponent will remove it from the windows_ ArrayList
       */
      ArrayList<TopComponent> windowsClone = (ArrayList<TopComponent>) windows_.clone();
      for (int i = 0; i < windowsClone.size(); i++) {
         TopComponent topComponent = windowsClone.get(i);
         LOGGER.debug("Database: "+ db_.getClientDatabaseInfo().getDbName()+
                               "Close Window i="+i+" TopComponent="+topComponent.getName());
         boolean status = topComponent.close();
         if (!status) {
            LOGGER.error("CLIENT - TopComponent not Closed!!!");
         }
      }
      windows_.clear();
     
   }

   @Override
   public boolean close() throws DbException {
      /*
       *  We need to clone the array because the close function in the
       * TopComponent will remove it from the windows_ ArrayList
       */
      ArrayList<TopComponent> windowsClone = (ArrayList<TopComponent>) windows_.clone();
      for (int i = 0; i < windowsClone.size(); i++) {
         TopComponent topComponent = windowsClone.get(i);
         LOGGER.debug("Database: "+ db_.getClientDatabaseInfo().getDbName()+
                               "Close Window i="+i+" TopComponent="+topComponent.getName());
         boolean status = topComponent.close();
         if (!status) {
            LOGGER.error("CLIENT - TopComponent not Closed!!!");
         }
      }
      windows_.clear();
      /**
       * Close Database on server
       */
      boolean ret = db_.close();
      
       if (db_.getErrorMsg() != null) {
         GuiGlobal.outputErr(db_.getErrorMsg());
      }
     
      return ret;
   }

   private void resetFST() {
      ClientDatabaseInfo dbInfo = db_.getClientDatabaseInfo();
      String[] fstNames;
      try {
         fstNames = db_.getFstNames();
         dbInfo.setFstNames(fstNames);
         for (int i = 0; i < fstNames.length; i++) {
            FieldSelectionTable fst = db_.getFst(fstNames[i]);
            dbInfo.addFst(fstNames[i], fst);
         }
         FieldSelectionTable fst = db_.getFieldSelectionTable();
         dbInfo.setFst(fst);
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   private void updateDbInfoForPfts() {
      try {
         ClientDatabaseInfo dbInfo = db_.getClientDatabaseInfo();
         // Pfts
         String defaultPftName = db_.getDefaultPrintFormatName();
         dbInfo.setDefaultPrintFormatName(defaultPftName);
         String defaultPft = db_.getDefaultPrintFormatName();
         dbInfo.setDefaultPrintFormat(defaultPft);
         String[] pftNames = db_.getPrintFormatNames();
         dbInfo.setPrintFormatNames(pftNames);
         for (int i = 0; i < pftNames.length; i++) {
            String pft = db_.getPrintFormat(pftNames[i]);
            dbInfo.addPft(pftNames[i], pft);
         }
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   private void updateDbInfoForFsts() {
      // FSTs
      try {
         ClientDatabaseInfo dbInfo = db_.getClientDatabaseInfo();
         String[] fstNames = db_.getFstNames();
         dbInfo.setFstNames(fstNames);
         for (int i = 0; i < fstNames.length; i++) {
            FieldSelectionTable fst = db_.getFst(fstNames[i]);
            dbInfo.addFst(fstNames[i], fst);
         }
         FieldSelectionTable fst = db_.getFieldSelectionTable();
         dbInfo.setFst(fst);
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   private void updateDbInfoForWks() {
      try {
         ClientDatabaseInfo dbInfo = db_.getClientDatabaseInfo();
         // Worksheets
         String[] wksNames = db_.getWorksheetNames();
         dbInfo.setWorksheetNames(wksNames);
         for (int i = 0; i < wksNames.length; i++) {
            WorksheetDef wks = db_.getWorksheetDef(wksNames[i]);
            dbInfo.addWks(wksNames[i], wks);
         }
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   private void updateDbInfoForDb() {
      try {
         ClientDatabaseInfo dbInfo = db_.getClientDatabaseInfo();
         // Last MFN
         long lastMfn = db_.getLastMfn();
         dbInfo.setLastMfn(lastMfn);
         // Number of records
         long recordsCount = db_.getRecordsCount();
         dbInfo.setRecordsCount(recordsCount);
         // FDT
         FieldDefinitionTable fdt = db_.getFieldDefinitionTable();
         dbInfo.setFdt(fdt);
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }
   
   private long getDictionaryTermsCountThread() throws InterruptedException, ExecutionException {
          final Future termsCount = executor.submit(
              new Callable() {
              @Override
              public Object call() throws Exception {

                  long count = 0;
                  try {
                      count = db_.getDictionaryTermsCount();
                  } catch (DbException ex) {
                      Exceptions.printStackTrace(ex);
                  }
                  return count;
              }

          });
          
         
         return (long) termsCount.get();
   }

   private void updateDbInfoForIndex() {
      try {
         ClientDatabaseInfo dbInfo = db_.getClientDatabaseInfo();
         // Index Number of terms
        
         //long termsCount = db_.getDictionaryTermsCount();
         dbInfo.setTermCount(getDictionaryTermsCount());
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   private void fillDatabaseInfo() {
      updateDbInfoForPfts();
      updateDbInfoForFsts();
      updateDbInfoForWks();
      updateDbInfoForDb();
      updateDbInfoForIndex();
   }

   /*
    * -------------------------------------------------
    * Methods that change the server DB and Metadata
    * ------------------------------------------------
    */
    @Override
    public IRecord addNewRecord() throws DbException {
        IRecord record = db_.addNewRecord();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        long recordsCount = db_.getRecordsCount();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = record.getMfn();
        db_.getClientDatabaseInfo().setRecordsCount(recordsCount);
        // Note that setDatabaseChanged includes notifies for the DB and indexes */
        setDatabaseChanged();
        return record;
    }

   @Override
   public void resetDatabaseInfo() {
      updateDatabaseInfo();
   }

    /**
     * Add record is supposed to be called in batch processing At the end a call to resetClientDatabaseInfo
     * should be done to reset the cache.
     *
     * @param record
     * @return
     * @throws java.lang.Exception
     */
    @Override
    public Record addRecord(final Record record) throws Exception {
        Record rec = db_.addRecord(record);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = rec.getMfn();
        /**
         * addRecord adds a record to the database WITHOUT UPDATING THE INDEX
         * Thus just update DB info and not Index info
         */
//        updateDbInfoForDb();
//
//        setDatabaseChanged();
        return rec;
    }

    @Override
    public Record updateRecord(Record record) throws Exception {
        Record rec = db_.updateRecord(record);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = record.getMfn();
        updateDatabaseInfo();
        setDatabaseChanged();
        return rec;
    }
    
      @Override
    public Record updateRecordEx(Record record) throws Exception {
        Record rec = db_.updateRecordEx(record);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = record.getMfn();
          /**
         * updateRecordEx update a record to the database WITHOUT UPDATING THE INDEX
         * Thus just update DB info and not Index info
         */
//        updateDbInfoForDb();
//
//        setDatabaseChanged();
       
        return rec;
    }

    @Override
    public boolean deleteRecord(final long mfn) throws DbException {
        IRecord record = db_.getRecordCursor(mfn);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        record = db_.getPrev();
        if (record == null) {
            record = db_.getNext();
        }
        currentRecordMfn_ = (record == null) ? -1L : record.getMfn();
        boolean ret = db_.deleteRecord(mfn);

        updateDatabaseInfo();
        setDatabaseChanged();
        return ret;
    }

    @Override
    public boolean buildIndex() throws DbException {
        checkIndexFormatVersion();
        boolean ret = db_.buildIndex();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        updateDatabaseInfo();
        return ret;
    }

    @Override
    public boolean clearIndex() throws DbException {
        checkIndexFormatVersion();
        boolean ret = db_.clearIndex();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        updateDatabaseInfo();
        return ret;
    }

    private void updateDatabaseInfo() {
        updateDbInfoForDb();
        updateDbInfoForIndex();
    }

    @Override
    public boolean saveFieldDefinitionTable(final FieldDefinitionTable fdt) throws DbException {
        boolean ret = db_.saveFieldDefinitionTable(fdt);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (ret) {
            updateDbInfoForDb();
        }
        setFdtChanged();
        return ret;
    }

    @Override
    public boolean saveFieldSelectionTable(final FieldSelectionTable fst) throws DbException {
        final boolean ret = db_.saveFieldSelectionTable(fst);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (ret) {
            updateDbInfoForFsts();
        }
        setFstChanged();
        return ret;
    }

    @Override
    public void saveDefaultPrintFormat(final String format) throws DbException {
        db_.saveDefaultPrintFormat(format);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        updateDbInfoForPfts();
        setPftChanged();
    }

    @Override
    public boolean removePrintFormat(final String name) throws DbException {
        final boolean ret = db_.removePrintFormat(name);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (ret) {
            updateDbInfoForPfts();
            setPftChanged();
        }
        return ret;
    }

    @Override
    public boolean savePrintFormat(final String name, final String format) throws DbException {
        final boolean ret = db_.savePrintFormat(name, format);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (ret) {
            updateDbInfoForPfts();
        }
        setPftChanged();
        return ret;
    }

    @Override
    public boolean removeWorksheetDef(final String worksheetName) throws DbException {
        final boolean ret = db_.removeWorksheetDef(worksheetName);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (ret) {
            updateDbInfoForWks();
        }
        setWksChanged();
        return ret;
    }

    @Override
    public boolean saveWorksheetDef(final WorksheetDef wkDef) throws DbException {
        final boolean ret = db_.saveWorksheetDef(wkDef);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (ret) {
            updateDbInfoForWks();
        }
        setWksChanged();
        return ret;
    }
   
   public long getCurrentRecordMfn() {
       return currentRecordMfn_;
   }

   /*
    * --------------------------------------------------------
    * Getter methods that get their data from the cached data
    * -------------------------------------------------------
    */
    @Override
    public IConnection getConnection() {
        IConnection connection = db_.getConnection();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return connection;
    }

   @Override
   public String getDbHome() {
      return db_.getClientDatabaseInfo().getDbHome();
   }

   @Override
   public String getDbName() {
      return db_.getClientDatabaseInfo().getDbName();
   }

   @Override
   public String getDatabaseName() throws DbException {
      String dbName = db_.getClientDatabaseInfo().getDbName();
      return dbName;
   }

   @Override
   public long getRecordsCount() throws DbException {
      long recordsCount = db_.getRecordsCount();
      return recordsCount;
   }

   @Override
   public long getLastMfn() throws DbException {
      long lastMfn = db_.getLastMfn();
      return lastMfn;
   }

   @Override
    public long getDictionaryTermsCount() throws DbException {
        long termsCount = 0;
        if (checkIndexFormatVersion()) {
            try {
                termsCount = getDictionaryTermsCountThread();
                if (termsCount == 0) {
                    GuiGlobal.output(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_IndexEmptyOrNotExist"));

                }
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.error("Error when getting number of terms in the dictionary", ex);
            }
        }
        return termsCount;
    }

    @Override
    public FieldDefinitionTable getFieldDefinitionTable() throws DbException {
        FieldDefinitionTable fdt = null;

        fdt = db_.getFieldDefinitionTable();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }

        return fdt;
    }

    @Override
    public FieldSelectionTable getFieldSelectionTable() throws DbException {
        FieldSelectionTable fst = db_.getFieldSelectionTable();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return fst;
    }

    @Override
    public String getDefaultPrintFormat() throws DbException {
        String defaultPft = db_.getDefaultPrintFormat();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return defaultPft;
    }

   @Override
   public String getDefaultPrintFormatName() throws DbException {
      String defaultPftName = db_.getDefaultPrintFormatName();
      return defaultPftName;
   }

    @Override
    public String getPrintFormat(final String name) throws DbException {
        final String pft = db_.getPrintFormat(name);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return pft;
    }

    @Override
    public String getPrintFormatAnsi(final String name) throws DbException {
        final String pft = db_.getPrintFormatAnsi(name);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return pft;
    }

    @Override
    public String[] getPrintFormatNames() throws DbException {
        String[] pftNames = db_.getPrintFormatNames();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return pftNames;
    }

   @Override
   public WorksheetDef getWorksheetDef(final String name) throws DbException {
      WorksheetDef wks = db_.getWorksheetDef(name);
      return wks;
   }

   @Override
   public String[] getWorksheetNames() throws DbException {
      String[] wksNames = db_.getWorksheetNames();
      return wksNames;
   }

   /*
    * --------------------------------------------
    * Methods to get the records remotely
    * -------------------------------------------
    */
    @Override
    public IRecord getFirst() throws DbException {
        IRecord record = null;
        if (db_.getRecordsCount() == 0) {
           // Do nothing if database is empty
        } else {

            record = db_.getFirst();
            if (db_.getErrorMsg() != null) {
                GuiGlobal.outputErr(db_.getErrorMsg());
            }
        }
        currentRecordMfn_ = (record == null) ? 0 : record.getMfn();

        return record;
    }

    @Override
    public IRecord getLast() throws DbException {
        IRecord record = db_.getLast();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = (record == null) ? 0 : record.getMfn();

        return record;
    }

    @Override
    public IRecord getNext() throws DbException {
        IRecord record = db_.getNext();
        if (record == null) {
            GuiGlobal.output(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_NoMoreNextSequentialRecord"));
        } else {
            if (db_.getErrorMsg() != null) {
                GuiGlobal.outputErr(db_.getErrorMsg());
            }
            currentRecordMfn_ = record.getMfn();
        }
        return record;
    }

    @Override
    public IRecord getPrev() throws DbException {
        IRecord record = db_.getPrev();
         if (record == null) {
            GuiGlobal.output(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_NoMorePrevSequentialRecord"));
        } else {
            if (db_.getErrorMsg() != null) {
                GuiGlobal.outputErr(db_.getErrorMsg());
            }
            currentRecordMfn_ = record.getMfn();
        }
        return record;
    }

    @Override
    public IRecord getCurrent() throws DbException {
        IRecord record = db_.getCurrent();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (record != null) {
            currentRecordMfn_ = record.getMfn();
        }

        return record;
    }

    @Override
    public IRecord getRecord(long mfn) throws DbException {
        IRecord record = db_.getRecord(mfn);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (record != null) {
            currentRecordMfn_ = record.getMfn();
        }

        return record;
    }

    @Override
    public IRecord getRecordCursor(long mfn) throws DbException {
        IRecord record = db_.getRecordCursor(mfn);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (record != null) {
            currentRecordMfn_ = record.getMfn();
        }

        return record;
    }

    @Override
    public List<DictionaryTerm> getDictionaryTermsChunck(int from, int to) throws DbException {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            return Collections.<DictionaryTerm>emptyList();
        }
        List<DictionaryTerm> list = db_.getDictionaryTermsChunck(from, to);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

    @Override
    public List<DictionaryTerm> getTermSuggestions(String prefix, String[] fieldNames, int maxTerms) throws Exception {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            return Collections.<DictionaryTerm>emptyList();
        }
        List<DictionaryTerm> list = db_.getTermSuggestions(prefix, fieldNames, maxTerms);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

    @Override
    public List<DictionaryTerm> getTermSuggestions(JisisParams params) throws Exception {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            return Collections.<DictionaryTerm>emptyList();
        }
        List<DictionaryTerm> list = db_.getTermSuggestions(params);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

    @Override
    public List<DictionaryTerm> getDictionaryTermsChunckEx(String from, int n)
        throws DbException {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            return Collections.<DictionaryTerm>emptyList();
        }
        List<DictionaryTerm> list = db_.getDictionaryTermsChunckEx(from, n);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

    @Override
    public List<DictionaryTerm> getSortedDictionaryTermsChunck(int from, int to)
        throws DbException {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            return Collections.<DictionaryTerm>emptyList();
        }
        List<DictionaryTerm> list = db_.getSortedDictionaryTermsChunck(from, to);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

    @Override
    public void setDictionarySorting(int[] iSort) throws DbException {
        db_.setDictionarySorting(iSort);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
    }

    @Override
    public boolean reIndex() throws DbException {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            GuiGlobal.output(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_IndexFormatTooOld"));
            return ret;
        }
        ret = db_.reIndex();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        updateDatabaseInfo();
        setIndexChanged();
        return ret;
    }

    @Override
    public long[] search(String query) throws DbException {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            GuiGlobal.output(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_IndexFormatTooOld"));
            return null;
        }
        long[] mfn = db_.search(query);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        db_.getClientDatabaseInfo().setLastQuery(query);
        db_.getClientDatabaseInfo().setResultLastQuery(mfn);

        return mfn;
    }

    @Override
    public long[] searchLucene(String query) throws DbException {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            GuiGlobal.output(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_IndexFormatTooOld"));
            return null;
        }
        long[] mfn = db_.searchLucene(query);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        db_.getClientDatabaseInfo().setLastLuceneQuery(query);
        db_.getClientDatabaseInfo().setResultLastLuceneQuery(mfn);

        return mfn;
    }

    @Override
    public List<DictionaryTerm> getDictionaryTerms() throws DbException {
        boolean ret = db_.checkIndexFormatVersion();
        if (!ret && db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
            GuiGlobal.output(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_IndexFormatTooOld"));
            return Collections.<DictionaryTerm>emptyList();
        }
        List<DictionaryTerm> list = db_.getDictionaryTerms();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

   @Override
   public IndexInfo getIndexInfo() throws DbException {
      boolean ret = db_.checkIndexFormatVersion();
      if (!ret && db_.getErrorMsg() != null) {
         GuiGlobal.outputErr(db_.getErrorMsg());
         GuiGlobal.output(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_IndexFormatTooOld") );
         return null;
      }
      return db_.getIndexInfo();
   }

   @Override
   public String[] getFstNames()  {
      
       String[] fstNames = null;
       try {
           fstNames = db_.getFstNames();
       } catch (Exception ex) {
           Exceptions.printStackTrace(ex);
       }
      
      return fstNames;
   }

    @Override
    public boolean saveFst(String name, FieldSelectionTable fst) throws Exception {
        boolean ret = db_.saveFst(name, fst);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        if (ret) {
            db_.getClientDatabaseInfo().setFstNames(db_.getFstNames());
        }
        setFstChanged();
        return ret;
    }

    @Override
    public FieldSelectionTable getFst(String name) throws DbException {
        FieldSelectionTable fst = db_.getFst(name);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return fst;
    }

    @Override
    public boolean removeFst(String name) throws DbException {
        db_.removeFst(name);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        db_.getClientDatabaseInfo().getFst();
        setFstChanged();
        return true;
    }

    @Override
    public String getDefaultFstName() throws DbException {
        String name = db_.getDefaultFstName();
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return name;
    }

    @Override
    public List<Record> getRecordChunck(int from, int to) throws DbException {
        List<Record> list = db_.getRecordChunck(from, to);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

    @Override
    public List<Record> getRecordChunck(long[] mfnChunck) throws DbException {
        List<Record> list = db_.getRecordChunck(mfnChunck);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

    @Override
    public List<Record> getRecordChunck(long fromMfn, int nRecords) throws DbException {
        List<Record> list = db_.getRecordChunck(fromMfn, nRecords);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        return list;
    }

   /*
    * ------------------------------------------
    * Display Font to be used for this database
    * ------------------------------------------
    * */
   public void setDisplayFont(Font font) {
      displayFont_ = font;
   }

   public Font getDisplayFont() {
      return displayFont_;
   }

   @Override
   public String[] getImageFileNames() throws Exception {
      return db_.getImageFileNames();
   }

   @Override
   public boolean saveImage(String fileName, Image img) throws Exception {
      return db_.saveImage(fileName, img);
   }

   @Override
   public Image getImage(String fileName) throws DbException {
      return db_.getImage(fileName);
   }

   @Override
   public boolean removeImage(String fileName) throws DbException {
      return db_.removeImage(fileName);
   }

   @Override
   public String[] getCssFileNames() throws Exception {
      return db_.getCssFileNames();
   }

   @Override
   public boolean saveCss(String fileName, String content) throws Exception {
      return db_.saveCss(fileName, content);
   }

   @Override
   public String getCss(String fileName) throws DbException {
      return db_.getCss(fileName);
   }

   @Override
   public boolean removeCss(String name) throws DbException {
      return db_.removeCss(name);
   }

   @Override
   public String[] getJavaScriptFileNames() throws Exception {
      return db_.getJavaScriptFileNames();
   }

   @Override
   public boolean saveJavaScript(String fileName, String content) throws Exception {
      return db_.saveJavaScript(fileName, content);
   }

   @Override
   public String getJavaScript(String fileName) throws DbException {
      return db_.getJavaScript(fileName);
   }

   @Override
   public boolean removeJavaScript(String name) throws DbException {
      return db_.removeJavaScript(name);
   }

   @Override
   public String[] getAnyFileNames() throws Exception {
      return db_.getAnyFileNames();
   }

   @Override
   public boolean saveAny(String fileName, String content) throws Exception {
      return db_.saveAny(fileName, content);
   }

   @Override
   public String getAny(String fileName) throws DbException {
      return db_.getAny(fileName);
   }

   @Override
   public boolean removeAny(String name) throws DbException {
      return db_.removeAny(name);
   }

   @Override
   public String[] getStopWordsFileNames() throws Exception {
      return db_.getStopWordsFileNames();
   }

   @Override
   public boolean saveStopWords(String fileName, String content) throws Exception {
      return db_.saveStopWords(fileName, content);
   }

   @Override
   public String getStopWords(String fileName) throws DbException {
      return db_.getStopWords(fileName);
   }

   @Override
   public boolean removeStopWords(String name) throws DbException {
      return db_.removeStopWords(name);
   }

   @Override
   public String[] getGroovyFileNames() throws Exception {
      return db_.getGroovyFileNames();
   }

   @Override
   public boolean saveGroovy(String fileName, String content) throws Exception {
      return db_.saveGroovy(fileName, content);
   }

   @Override
   public String getGroovy(String fileName) throws DbException {
      return db_.getGroovy(fileName);
   }

   @Override
   public boolean removeGroovy(String fileName) throws DbException {
      return db_.removeGroovy(fileName);
   }
   
    @Override
   public List<String> getDocumentFileNames() throws Exception {
      return db_.getDocumentFileNames();
   }

   
    @Override
   public boolean saveDocument(String fileName, byte[] content) throws Exception {
      return db_.saveDocument(fileName, content);
   }

   @Override
   public byte[] getDocument(String fileName) throws DbException {
      return db_.getDocument(fileName);
   }

   @Override
   public boolean removeDocument(String fileName) throws DbException {
      return db_.removeDocument(fileName);
   }

   @Override
   public String getDbConfig() throws DbException {
      return db_.getDbConfig();
   }

   @Override
   public boolean saveDbConfig(String dbConfig) throws DbException {
      return db_.saveDbConfig(dbConfig);
   }
   @Override
    public int lockDatabase(UserInfo requester) throws DbException {
      return db_.lockDatabase(requester);
   }

   @Override
   public int unlockDatabase() throws DbException {
      return db_.unlockDatabase();
   }

   @Override
   public boolean isDatabaseLocked() throws DbException {
      return db_.isDatabaseLocked();
   }

   @Override
   public UserInfo databaseLockOwner() throws DbException {
      return db_.databaseLockOwner();
   }

   @Override
   public int lockRecord(long mfn, UserInfo requester) throws DbException {
      return db_.lockRecord(mfn, requester);
   }

   @Override
   public int unlockRecord(long mfn, UserInfo requester) throws DbException {
      return db_.unlockRecord(mfn, requester);
   }

   @Override
   public int getRecordLockStatus(long mfn, UserInfo requester) throws DbException {
      return db_.getRecordLockStatus(mfn, requester);
   }

   @Override
   public UserInfo recordLockOwner(long mfn) throws DbException {
      return db_.recordLockOwner(mfn);
   }

   @Override
   public int unlockAllRecords() throws DbException {
      return db_.unlockAllRecords();
   }

   @Override
   public List<RecordLock> getRecordLocks() throws DbException {
      return db_.getRecordLocks();
   }

   public List<SearchResult> getSearchResults() {
      return searchResults_;
   }

   public void addSearchResult(SearchResult searchResult) {
      searchNumber_++;
      searchResult.setSearchNumber(searchNumber_);
      searchResults_.add(searchResult);
      setSearchHistoryChanged();
   }

    public List<MarkedRecords> getMarkedRecordsList() {
      return markedRecordsList_;
   }

   public MarkedRecords addMarkedRecords(MarkedRecords markedRecords) {
      markedRecordsSetNumber_++;
      markedRecords.setMarkedSetNumber(markedRecordsSetNumber_);
      markedRecordsList_.add(markedRecords);
      setMarkedRecordsHistoryChanged();
      return markedRecords;
   }

    public List<HitSortResult> getHitSortResults() {
      return hitSortResults_;
   }

   public void addHitSortResult(HitSortResult hitSortResult) {
      hitSortResultNumber_++;
      hitSortResult.setHitSortNumber(hitSortResultNumber_);
      hitSortResults_.add(hitSortResult);
      setHitSortResultChanged();
   }

   @Override
   public List<ValidationData> getValidationData(String wksName) throws DbException {
      return db_.getValidationData(wksName);
      
   }

   @Override
   public boolean setValidationData(String wksName, List<ValidationData> list) throws DbException {
      return db_.setValidationData(wksName, list);
      
   }

   @Override
   public List<PickListData> getPickListData(String wksName) throws DbException {
      return db_.getPickListData(wksName);
   }

   @Override
   public boolean setPickListData(String wksName, List<PickListData> list) throws DbException {
      return db_.setPickListData(wksName, list);
   }

   @Override
   public DatabaseInfo getDatabaseInfo() {
      return db_.getDatabaseInfo();
   }

    @Override
    public FormattedRecord getRecordFmt(long mfn, String pftName) throws DbException {
        FormattedRecord formattedRecord = db_.getRecordFmt(mfn, pftName);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = (formattedRecord == null) ? -1L : formattedRecord.getMfn();
        return formattedRecord;
    }

    @Override
    public FormattedRecord getRecordCursorFmt(long mfn, String pftName) throws DbException {
        FormattedRecord formattedRecord = db_.getRecordCursorFmt(mfn, pftName);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = (formattedRecord == null) ? -1L : formattedRecord.getMfn();
        return formattedRecord;
    }

    @Override
    public FormattedRecord getFirstFmt(String pftName) throws DbException {
        FormattedRecord formattedRecord = db_.getFirstFmt(pftName);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = (formattedRecord == null) ? -1L : formattedRecord.getMfn();
        return formattedRecord;
    }

    @Override
    public FormattedRecord getNextFmt(String pftName) throws DbException {
        FormattedRecord formattedRecord = db_.getNextFmt(pftName);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = (formattedRecord == null) ? -1L : formattedRecord.getMfn();
        return formattedRecord;
    }

    @Override
    public FormattedRecord getPrevFmt(String pftName) throws DbException {
        FormattedRecord formattedRecord = db_.getPrevFmt(pftName);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = (formattedRecord == null) ? -1L : formattedRecord.getMfn();
        return formattedRecord;
    }

    @Override
    public FormattedRecord getLastFmt(String pftName) throws DbException {
        FormattedRecord formattedRecord = db_.getLastFmt(pftName);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = (formattedRecord == null) ? -1L : formattedRecord.getMfn();
        return formattedRecord;
    }

    @Override
    public FormattedRecord getCurrentFmt(String pftName) throws DbException {
        FormattedRecord formattedRecord = db_.getCurrentFmt(pftName);
        if (db_.getErrorMsg() != null) {
            GuiGlobal.outputErr(db_.getErrorMsg());
        }
        currentRecordMfn_ = (formattedRecord == null) ? -1L : formattedRecord.getMfn();
        return formattedRecord;
    }

   @Override
   public FormattedRecord formatRecord(Record record, String pftName) throws DbException {
      return db_.formatRecord(record, pftName);
      
   }

   @Override
   public List<JisisDocument> getPageOfSearchDoc(String query, int pageNumber, int documentsPerPage) throws DbException {
      db_.checkIndexFormatVersion();
      return db_.getPageOfSearchDoc(query, pageNumber, documentsPerPage);
   }

   @Override
   public int getSearchHitCount(String query) throws DbException {
      db_.checkIndexFormatVersion();
      return db_.getSearchHitCount(query);
   }

   @Override
   public boolean checkIndexFormatVersion() throws DbException {
      boolean b = db_.checkIndexFormatVersion();
       if (b) {
          // pass
      } else {
        
         GuiGlobal.outputErr(NbBundle.getMessage(ClientDatabaseProxy.class, "MSG_IndexFormatTooOld") );
      }
      return b;
   }
}
