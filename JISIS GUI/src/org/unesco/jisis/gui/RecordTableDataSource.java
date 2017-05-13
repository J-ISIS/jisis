/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.gui;

import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.jisisutils.distributed.DistributedTableDescription;
import org.unesco.jisis.jisisutils.distributed.IDistributedTableDataSource;


/**
 *
 * @author jc_dauphin
 */
public class RecordTableDataSource implements IDistributedTableDataSource {
   private IDatabase                   db_;
   private FieldDefinitionTable        fdt_;
   private DistributedTableDescription tableDescription_ = null;
   private long                        indexes_[];
   private long firstMfn_ = -1;


    public RecordTableDataSource(IDatabase db) {
        db_ = db;
        try {
            fdt_ = db_.getFieldDefinitionTable();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        IRecord record = null;
        try {
            record = db_.getFirst();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        firstMfn_ = record.getMfn();

        indexes_ = new long[0];    // for consistency
    }

   public void setIndexMap(long indexes[]) {
      indexes_ = indexes;
      if (indexes == null) {
         indexes_ = new long[0];    // for consistency
      }
      if (tableDescription_ != null) {
         int nRows = 0;
         if (indexes_.length > 0) {
            nRows = indexes_.length;
         } else {
            try {
               nRows = (int) db_.getRecordsCount();
            } catch (DbException ex) {
               Exceptions.printStackTrace(ex);
            }
         }
         tableDescription_.setRowCount(nRows);
      }
   }

   public IDatabase getRemoteDatabase() {
      return db_;
   }

    private DistributedTableDescription makeDistributedTableDescription() throws Exception {

        int nFields = fdt_.getFieldsCount() + 1;    // MFN column (+1)
        String[] columnNames = new String[nFields];
        Class[] columnClasses = new Class[nFields];
        columnNames[0] = "MFN";
        columnClasses[0] = String.class;
        for (int i = 1; i < nFields; i++) {
            columnNames[i] = fdt_.getFieldByIndex(i - 1).getName() + " (" + fdt_.getFieldByIndex(i - 1).getTag() + ")";
            FieldDefinition fld = fdt_.getFieldByIndex(i - 1);
            int type = fld.getType();
            switch (type) {
                case Global.FIELD_TYPE_ALPHABETIC:
                case Global.FIELD_TYPE_ALPHANUMERIC:
                case Global.FIELD_TYPE_NUMERIC:
                case Global.FIELD_TYPE_PATTERN:
                case Global.FIELD_TYPE_STRING:
                case Global.FIELD_TYPE_TIME:
                case Global.FIELD_TYPE_URL:
                case Global.FIELD_TYPE_DOC:
                case Global.FIELD_TYPE_DATE:
                    columnClasses[i] = String.class;
                    break;
                case Global.FIELD_TYPE_BLOB:
                    columnClasses[i] = Object.class;
            }
        }
        int nRows = 0;
        if (indexes_.length > 0) {
            nRows = indexes_.length;
        } else {
            nRows = (int) db_.getRecordsCount();
        }
        System.out.println("DistributedTableDescription nRows=" + nRows);
        DistributedTableDescription tableDescription = new DistributedTableDescription(columnNames, columnClasses, nRows);

        return tableDescription;
    }
   public DistributedTableDescription getTableDescription() throws Exception {
      if (tableDescription_ == null) {
         tableDescription_ = makeDistributedTableDescription();
         
      }
      return tableDescription_;
   }

   public Object[][] retrieveRows(int from, int to, ProgressHandle progress) throws Exception {
      List<Record> records = null;
      if (indexes_.length > 0) {
         int  n     = to - from + 1;
         long mfn[] = new long[n];
         for (int i = 0; i < n; i++) {
            mfn[i] = indexes_[from + i];
         }
         records = db_.getRecordChunck(mfn);
      } else {
         records = db_.getRecordChunck(from, to);
      }
      int nRows = records.size();
      System.out.println("retrieveRows nRows=" + nRows);
      Object[][] data = new Object[nRows][fdt_.getFieldsCount() + 1];
      int        n    = fdt_.getFieldsCount();
      System.out.println("retrieveRows nFields=" + n);
      for (int i = 0; i < nRows; i++) {
         Record record  = records.get(i);
         int    nFields = record.getFieldCount();
         data[i][0] = Long.toString(record.getMfn());
         for (int j = 0; j < nFields; j++) {
            IField fld    = record.getFieldByIndex(j);
            int    tag    = fld.getTag();
            int    fdtIdx = fdt_.findField(tag);
            if (fdtIdx == -1) {
               continue;
            }
            Object obj = fld.getFieldValue();
            data[i][fdtIdx + 1] = obj;
         }
         if (progress != null) {
            progress.setDisplayName("Retrieving record" + i + "/" + nRows);
         }
      }
      return data;
   }

   public int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns)
           throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int[] getSelectedRows() throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int[] getSelectedColumns() throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getRowCount() {
      long nRows = 0;
      try {
         if (indexes_.length > 0) {
            nRows = indexes_.length;
         } else {
            nRows = (int) db_.getRecordsCount();
         }
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      return nRows;
   }

    public boolean isZeroBased() {
        return false;
    }
}
