/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisiscore.common;

import org.openide.util.NbBundle;



import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.common.WorksheetDef.WorksheetField;

/**
 *
 * @author jc_dauphin
 */
public class WKSModelEx extends AbstractTableModel implements TableModel, Reorderable {
    static final public String columnName[] = {
        NbBundle.getMessage(WKSModelEx.class, "MSG_WksTagLabel"),
        NbBundle.getMessage(WKSModelEx.class, "MSG_WksDescription"),
        NbBundle.getMessage(WKSModelEx.class, "MSG_WksDisplay"),
        NbBundle.getMessage(WKSModelEx.class, "MSG_WksDefault"),
        NbBundle.getMessage(WKSModelEx.class, "MSG_WksHelpMessage"),
        NbBundle.getMessage(WKSModelEx.class, "MSG_WksValidation"),
        NbBundle.getMessage(WKSModelEx.class, "MSG_WksPickList")
    };
    static final private Class columnClasses[] = {
        Integer.class, String.class, String.class, String.class, String.class, String.class,
        String.class
    };
    static final public ColumnData wksColumns[] = {
        new ColumnData(NbBundle.getMessage(WKSModelEx.class, "MSG_WksTagLabel"), 30, JLabel.RIGHT),
        new ColumnData(NbBundle.getMessage(WKSModelEx.class, "MSG_WksDescription"), 150,
                       JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(WKSModelEx.class, "MSG_WksDisplay"), 30, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(WKSModelEx.class, "MSG_WksDefault"), 150, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(WKSModelEx.class, "MSG_WksHelpMessage"), 150,
                       JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(WKSModelEx.class, "MSG_WksValidation"), 150,
                       JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(WKSModelEx.class, "MSG_WksPickList"), 150, JLabel.LEFT)
    };
    /** The column indexes in the JTable view */
    public static final int TAG_COLUMN_INDEX         = 0;
    public static final int DESCRIPTION_COLUMN_INDEX = 1;
    public static final int DISPLAY_COLUMN_INDEX     = 2;
    public static final int DEFAULT_COLUMN_INDEX     = 3;
    public static final int HELP_COLUMN_INDEX        = 4;
    public static final int VALIDATION_COLUMN_INDEX  = 5;
    public static final int PICKLIST_COLUMN_INDEX    = 6;

    /** The data structure used to store the worksheet data */
    private WorksheetDef    workSheetDef_            = null;
    private boolean dataChanged_ = false;

    /** Creates a new instance of FDTModel */
    public WKSModelEx() {
        workSheetDef_ = new WorksheetDef();
        fireTableDataChanged();
    }

    public WKSModelEx(WorksheetDef workSheetDef) {
        workSheetDef_ = workSheetDef;
        fireTableDataChanged();
    }

    public void addRow(int tag, String desc, String display, String defVal, String hlpMsg,
                       String valid, String pickList) {
        addRow(tag, desc, display, defVal, hlpMsg, valid, pickList, false);
    }

    private void addRow(int tag, String desc, String display, String defVal, String hlpMsg,
                        String valid, String pickList, boolean renderOnly) {
        if ((workSheetDef_ != null) &&!renderOnly) {
            
                workSheetDef_.addField(tag, false, false,
                        Global.FIELD_TYPE_ALPHANUMERIC,
                        desc, display, defVal, hlpMsg, valid, pickList);
                tableDataChanged();
           
        }
    }

    public void removeAll() {
        workSheetDef_.removeAll();
        tableDataChanged();
    }

    public void removeRow(int rowIndex) {
        if (workSheetDef_ != null) {
            WorksheetDef.WorksheetField wf = workSheetDef_.getFieldByIndex(rowIndex);
            workSheetDef_.removeField(wf.getTag());
            tableDataChanged();
        }
    }

    public String[] getColumns() {
        return columnName;
    }

    public Class[] getColumnClasses() {
        return columnClasses;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 1;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (workSheetDef_ != null) {
            
                WorksheetDef.WorksheetField wf      = workSheetDef_.getFieldByIndex(rowIndex);
                int                         tag     = Integer.parseInt(getValueAt(rowIndex,
                                                          0).toString());
                String                      desc    = getValueAt(rowIndex, 1).toString();
                String                      display = getValueAt(rowIndex, 2).toString();
                String                      def     = getValueAt(rowIndex, 3).toString();
                String                      help    = getValueAt(rowIndex, 4).toString();
                String                      val     = getValueAt(rowIndex, 5).toString();
                String                      pick    = getValueAt(rowIndex, 6).toString();
                switch (columnIndex) {
                case TAG_COLUMN_INDEX: {
                    tag = Integer.parseInt(aValue.toString());
                    break;
                }
                case DESCRIPTION_COLUMN_INDEX : {
                    desc = aValue.toString();
                    break;
                }
                case DISPLAY_COLUMN_INDEX: {
                    display = aValue.toString();
                    break;
                }
                case DEFAULT_COLUMN_INDEX : {
                    def = aValue.toString();
                    break;
                }
                case HELP_COLUMN_INDEX : {
                    help = aValue.toString();
                    break;
                }
                case VALIDATION_COLUMN_INDEX : {
                    val = aValue.toString();
                    break;
                }
                case PICKLIST_COLUMN_INDEX : {
                    pick = aValue.toString();
                    break;
                }
                }
                workSheetDef_.addField(tag, false,false,
                        Global.FIELD_TYPE_ALPHANUMERIC,
                        desc, display, def, help, val, pick);
           
            tableDataChanged();
        }
    }

    public WorksheetDef getWorksheetDef() {
        return workSheetDef_;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (workSheetDef_ == null) {
            return null;
        }
        WorksheetDef.WorksheetField wf = workSheetDef_.getFieldByIndex(rowIndex);
        switch (columnIndex) {
        case TAG_COLUMN_INDEX:    // Tag
            return new Integer(wf.getTag());
        case DESCRIPTION_COLUMN_INDEX :    // Description
            return (wf.getDescription() == null)
                   ? ""
                   : wf.getDescription();
        case DISPLAY_COLUMN_INDEX :    // Display Control
            return (wf.getDisplayControl() == null)
                   ? ""
                   : wf.getDisplayControl();
        case DEFAULT_COLUMN_INDEX :    // Default value
            return (wf.getDefaultValue() == null)
                   ? ""
                   : wf.getDefaultValue();
        case HELP_COLUMN_INDEX :    // Help Message
            return (wf.getHelpMessage() == null)
                   ? ""
                   : wf.getHelpMessage();
        case VALIDATION_COLUMN_INDEX :    // Validation Format
            return (wf.getValidationFormat() == null)
                   ? ""
                   : wf.getValidationFormat();
        case PICKLIST_COLUMN_INDEX :    // Pick List
            return (wf.getPickList() == null)
                   ? ""
                   : wf.getPickList();
        }
        return "";
    }

    public int getRowCount() {
        return workSheetDef_.getFieldsCount();
    }

    public int getColumnCount() {
        return getColumns().length;
    }

    public String getColumnName(int columnIndex) {
        return getColumns()[columnIndex];
    }

    public Class getColumnClass(int columnIndex) {
        return getColumnClasses()[columnIndex];
    }

   @Override
   public void reorder(int fromIndex, int toIndex) {
      WorksheetField workSheetField = workSheetDef_.getFieldByIndex(fromIndex);
      workSheetDef_.addField(toIndex, workSheetField);
      if (toIndex < fromIndex) fromIndex++;
      workSheetDef_.removeFieldIndex(fromIndex);
     tableDataChanged();
   }
   
   private void tableDataChanged() {
      dataChanged_ = true;
      fireTableDataChanged();
   }
   public boolean hasTableDataChanged() {
      return dataChanged_;
   }

   public void resetChanged() {
      dataChanged_ = false;
   }
}
