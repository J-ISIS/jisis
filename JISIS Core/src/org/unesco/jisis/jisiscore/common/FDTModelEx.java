/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisiscore.common;

import org.openide.util.NbBundle;



import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;

/**
 *
 * @author jc_dauphin
 */
public class FDTModelEx extends AbstractTableModel implements TableModel {
    private FieldDefinitionTable fdt_         = null;
    static final public String   columnNames[] = {
        NbBundle.getMessage(FDTModelEx.class, "MSG_TagLabel"),
        NbBundle.getMessage(FDTModelEx.class, "MSG_NameLabel"),
        NbBundle.getMessage(FDTModelEx.class, "MSG_TypeLabel"),
        NbBundle.getMessage(FDTModelEx.class, "MSG_IndicatorsLabel"),
        NbBundle.getMessage(FDTModelEx.class, "MSG_RepeatLabel"),
        NbBundle.getMessage(FDTModelEx.class, "MSG_FirstSubfieldLabel"),
        NbBundle.getMessage(FDTModelEx.class, "MSG_SubfieldLabel")
    };

    /** Column Types */
    static final private Class     columnClasses[] = {
        Integer.class, String.class, Integer.class, 
        Boolean.class, Boolean.class,Boolean.class,
        String.class
    };
    static final public ColumnData fdtColumns[]    = {
        new ColumnData(NbBundle.getMessage(FDTModelEx.class, "MSG_TagLabel"), 50, JLabel.RIGHT),
        new ColumnData(NbBundle.getMessage(FDTModelEx.class, "MSG_NameLabel"), 350, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(FDTModelEx.class, "MSG_TypeLabel"), 50, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(FDTModelEx.class, "MSG_IndicatorsLabel"), 75, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(FDTModelEx.class, "MSG_RepeatLabel"), 50, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(FDTModelEx.class, "MSG_FirstSubfieldLabel"), 75, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(FDTModelEx.class, "MSG_SubfieldLabel"), 300, JLabel.LEFT)
    };

    /** Creates a new instance of FDTModel */
    public FDTModelEx() {
        fdt_ = new FieldDefinitionTable();
    }

    public FDTModelEx(FieldDefinitionTable fdt) {
        fdt_ = fdt;
    }

    public String[] getColumns() {
    return columnNames;
    }

    public Class[] getColumnClasses() {
        return columnClasses;
    }

    public int findField(int tag) {
        return fdt_.findField(tag);
    }

    public void addRow(int tag, String name, int type, boolean indicators, 
            boolean repeatable, boolean firstSubfield, String pattern) {
        addRow(tag, name, type, indicators, repeatable, firstSubfield, pattern, false);
    }

    public void addRow(int tag, String name, int type, boolean indicators,
            boolean repeatable, boolean firstSubfield, String pattern,
                       boolean renderOnly) {
        if ((fdt_ != null) &&!renderOnly) {
           
                /** setField either replace or add */
                fdt_.setField(tag, name, type, indicators,repeatable, firstSubfield, pattern);
           
        } 
    }

    public void removeRow(int rowIndex) {
        if (fdt_ != null) {
            fdt_.removeField(fdt_.getFieldByIndex(rowIndex).getTag());
        }
    }

    public void clear() {
       fdt_.clear();
    }

    public int getRowCount() {
        return fdt_.getFieldsCount();
    }

    public int getColumnCount() {
        return getColumns().length;
    }

   @Override
    public String getColumnName(int columnIndex) {
        return getColumns()[columnIndex];
    }

   @Override
    public Class getColumnClass(int columnIndex) {
        return getColumnClasses()[columnIndex];
    }

   @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        FieldDefinition fd = fdt_.getFieldByIndex(rowIndex);
        switch (columnIndex) {
        case 0 :    // Tag
            return new Integer(fd.getTag());
        case 1 :    // Name
            return (fd.getName() == null)
                   ? ""
                   : fd.getName();
        case 2 :    // Type
            return new Integer(fd.getType());
        case 3 :    // Indicators
            return new Boolean(fd.hasIndicators());
        case 4 :    // Rep
            return new Boolean(fd.isRepeatable());
          case 5 :    // First Subfield
            return new Boolean(fd.hasFirstSubfield());
        case 6 :    // Pattern
            return (fd.getSubfields() == null)
                   ? ""
                   : fd.getSubfields();
        }
        return "";
    }


    public FieldDefinitionTable getFieldDefinitionTable() {
        return fdt_;
    }
}
