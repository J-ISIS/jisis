/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisiscore.common;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 *
 * @author jc_dauphin
 */
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.exceptions.DbException;

public class FSTModelEx extends AbstractTableModel implements TableModel {

    static final public String columnName[] = {
        NbBundle.getMessage(FSTModelEx.class, "MSG_IdLabel"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_Name"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_TechLabel"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_FmtLabel")
    };
    static final private Class columnClasses[] = {
        Integer.class, String.class, JComboBox.class, String.class
    };
    static final public ColumnData fstColumns[] = {
        new ColumnData(NbBundle.getMessage(FSTModelEx.class, "MSG_IdLabel"), 10, JLabel.RIGHT),
        new ColumnData(NbBundle.getMessage(FSTModelEx.class, "MSG_Name"), 150, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(FSTModelEx.class, "MSG_TechLabel"), 50, JLabel.LEFT),
        new ColumnData(NbBundle.getMessage(FSTModelEx.class, "MSG_FmtLabel"), 550, JLabel.LEFT)
    };
    static final public String techniques[] = {
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique0"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique1"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique2"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique3"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique4"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique5"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique6"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique7"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique8"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique9"),
        NbBundle.getMessage(FSTModelEx.class, "MSG_IndexTechnique10")
    };

    /**
     * The column indexes in the JTable view
     */
    public static final int ID_COLUMN_INDEX = 0;
    public static final int NAME_COLUMN_INDEX = 1;
    public static final int TECHNIQUE_COLUMN_INDEX = 2;
    public static final int PFT_COLUMN_INDEX = 3;

    /**
     * The data structure for storing fst data
     */
    private FieldSelectionTable fst_ = null;

    /**
     * Creates a new instance of FDTModel
     */
    public FSTModelEx() {
        fst_ = new FieldSelectionTable();
    }

    public FSTModelEx(FieldSelectionTable fst) {
        fst_ = fst;
    }

    public void setStoreRecordInIndex(int value) {
        fst_.setStoreRecordInIndex(value);
    }

    public int getStoreRecordInIndex() {
        return fst_.getStoreRecordInIndex();
    }

    public void setMakeCatchallField(int value) {
        fst_.setMakeCatchallField(value);
    }

    public int getMakeCatchallField() {
        return fst_.getMakeCatchallField();
    }

    public void addRow(int tag, String name, int technique, String format) {
        addRow(tag, name, technique, format, false);
    }

    public void addRow(int tag, String name, int technique, String format, boolean renderOnly) {
        if ((fst_ != null) && !renderOnly) {
            try {
                /**
                 * Note that addEntry removes the entry if it already exists before adding the entry.
                 */
                fst_.addEntryAlways(tag, name, technique, format);
            } catch (DbException ex) {
                Exceptions.printStackTrace(ex);
            }

            fireTableDataChanged();

        }
    }

    public void removeRow(int rowIndex) {
        if (fst_ != null) {
            FieldSelectionTable.FstEntry fe = fst_.getEntryByIndex(rowIndex);
            fst_.deleteEntry(rowIndex);
            // fst_.removeEntry(fe.getTag(), fe.getTechnique());
            fireTableDataChanged();
        }
    }

    public String[] getColumns() {
        return columnName;
    }

    public Class[] getColumnClasses() {
        return columnClasses;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex != 4);

    }

    private int findTechnique(Object obj) {
        String s = (String) obj;
        int ifound = -1;
        for (int i = 0; i < techniques.length; i++) {
            if (s.equals(techniques[i])) {
                ifound = i;
                break;
            }
        }
        return ifound;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // System.out.println("FstModelEx setValueAt row=" + Integer.toString(rowIndex));
        if (fst_ != null) {
            int tag = Integer.parseInt(getValueAt(rowIndex, ID_COLUMN_INDEX).toString());
            String name = getValueAt(rowIndex, NAME_COLUMN_INDEX).toString();
            int tech = findTechnique(getValueAt(rowIndex, TECHNIQUE_COLUMN_INDEX));
            String format = getValueAt(rowIndex, PFT_COLUMN_INDEX).toString();
            //System.out.println("FstModelEx setValueAt  format1=" + format);
            switch (columnIndex) {
                case ID_COLUMN_INDEX:
                    tag = Integer.parseInt(aValue.toString());
                    break;

                case NAME_COLUMN_INDEX:
                    name = aValue.toString();

                    break;
                case TECHNIQUE_COLUMN_INDEX:
                    /* Change the Combo box String into a code */
                    tech = findTechnique(aValue);
                    if (tech == -1) {
                        throw new RuntimeException("FSTModelEx: Invalid Index " + aValue.toString()
                            + " ComboBox Techniques");
                    }
                    //System.out.println("FstModelEx setValueAt  tech=" + tech);
                    break;

                case PFT_COLUMN_INDEX:
                    format = aValue.toString();
                    //System.out.println("FstModelEx setValueAt  format2=" + format);
                    break;

                case 4:
                // saveButtons_.set(rowIndex, (JButton) aValue);
            }

            //System.out.println("FstModelEx setValueAt  forma3t=" + format);
            fst_.updateEntry(rowIndex, tag, name, tech, format);

            fireTableCellUpdated(rowIndex, columnIndex);

        }

    }

    public FieldSelectionTable getFieldSelectionTable() {
        return fst_;
    }

    @Override
    public int getRowCount() {
        return fst_.getEntriesCount();
    }

    @Override
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        // System.out.println("FstModelEx getValueAt row=" + Integer.toString(rowIndex));
        FieldSelectionTable.FstEntry fe = fst_.getEntryByIndex(rowIndex);
        switch (columnIndex) {
            case ID_COLUMN_INDEX:    // Tag (ID in fact that can be different from tag)
                return fe.getTag();
            case NAME_COLUMN_INDEX:              // Format
                return (fe.getName() == null)
                    ? ""
                    : fe.getName();
            case TECHNIQUE_COLUMN_INDEX:    // Technique
                int tech = fe.getTechnique();
                if ((tech < 0) || (tech >= techniques.length)) {
                    throw new RuntimeException("FSTModelEx::getValue Invalid Index " + tech
                        + " ComboBox Techniques");
                }
                return techniques[tech];
            case PFT_COLUMN_INDEX:              // Format
                return (fe.getFormat() == null)
                    ? ""
                    : fe.getFormat();
            case 4:              // Save button
                return "Save";    // saveButtons_.get(rowIndex);
        }
        return "";
    }
}
