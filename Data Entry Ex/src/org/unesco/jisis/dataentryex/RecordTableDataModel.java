/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryex;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.jisiscore.common.ColumnData;

/**
 *
 * @author jcd
 */
public class RecordTableDataModel extends AbstractTableModel {
   private final IDatabase db_;
   private FieldDefinitionTable fdt_;
   private final WorksheetDef wks_;
static final public String   columnNames[] = {
        NbBundle.getMessage(RecordTableDataModel.class, "MSG_TagLabel"),
        NbBundle.getMessage(RecordTableDataModel.class, "MSG_NameLabel"),
        NbBundle.getMessage(RecordTableDataModel.class, "MSG_TypeLabel"),
        NbBundle.getMessage(RecordTableDataModel.class, "MSG_FieldLabel")
    };

    /** Column Types */
    static final private Class     columnClasses[] = {
        Integer.class, String.class, Integer.class, Object.class
    };
    static final public ColumnData recordColumns[]    = {
        new ColumnData(NbBundle.getMessage(RecordTableDataModel.class, "MSG_TagLabel"), 75, JLabel.RIGHT),
        new ColumnData(NbBundle.getMessage(RecordTableDataModel.class, "MSG_NameLabel"), 150, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(RecordTableDataModel.class, "MSG_TypeLabel"), 50, JLabel.RIGHT),
        new ColumnData(NbBundle.getMessage(RecordTableDataModel.class, "MSG_FieldLabel"), 500, JLabel.LEFT)
       
    };

   List<FieldData> fieldData;

   public RecordTableDataModel(IDatabase db, WorksheetDef wks) {
      db_ = db;
      wks_ = wks;
      try {
         fdt_ = db_.getFieldDefinitionTable();
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }

      setDefaultData();
   }

   public List<FieldData> getData() {
      return fieldData;
   }

   public void setData(List<FieldData> fieldData) {
      this.fieldData = fieldData;
   }

   public void setDefaultData() {
      fieldData = new ArrayList<FieldData>();
      int fieldCount = wks_.getFieldsCount();

      for (int i = 0; i < fieldCount; i++) {
         WorksheetDef.WorksheetField wf = wks_.getFieldByIndex(i);
         //IField f = rec.getField(wf.getTag());
//                int type = fdt_.getFieldByTag(wf.getTag()).getType();
//                f.setType(type);
         FieldData fd = new FieldData(wf, fdt_.getFieldByTag(wf.getTag()));
         fieldData.add(fd);


      }


   }

   @Override
   public int getRowCount() {
      return fieldData.size();
   }

   @Override
   public int getColumnCount() {
      return recordColumns.length;
   }

   @Override
   public String getColumnName(int column) {
      return recordColumns[column].title_;
   }

   @Override
   public boolean isCellEditable(int nRow, int nCol) {
      // We can only change the content
      return (nCol == 3) ? true : false;
   }

   @Override
   public Object getValueAt(int nRow, int nCol) {
      if (nRow < 0 || nRow >= getRowCount()) {
         return "";
      }
      FieldData row = (FieldData) fieldData.get(nRow);
      switch (nCol) {
         case 0:
            return row.getTag();
         case 1:
            return row.getDescription();
         case 2:
            return row.getType();
         case 3:
            return row.getContent();
       
      }
      return "";
   }

   @Override
   public void setValueAt(Object obj, int nRow, int nCol) {
      if (nRow < 0 || nRow >= getRowCount()) {
         return;
      }
      FieldData row = (FieldData) fieldData.get(nRow);
      switch (nCol) {
         case 0:
            
            break;
         case 1:
            
            break;
         case 2:
           
            break;
         case 3:
            row.setContent(obj);
           
      }
   }

   public void addNewRow(int nRow) {
      int tag = fieldData.get(nRow).getTag();
      WorksheetDef.WorksheetField wf = wks_.getFieldByTag(tag);
         //IField f = rec.getField(wf.getTag());
//                int type = fdt_.getFieldByTag(wf.getTag()).getType();
//                f.setType(type);
         FieldData fd = new FieldData(wf, fdt_.getFieldByTag(tag));
     
      if (nRow >= 0 || nRow < getRowCount()) {
         fieldData.set(nRow, fd);
      } else {
         fieldData.add(fd);
      }
   }

   public boolean deleteRow(int nRow) {
      if (nRow < 0 || nRow >= getRowCount()) {
         return false;
      }
      fieldData.remove(nRow);
      return true;
   }
}        
