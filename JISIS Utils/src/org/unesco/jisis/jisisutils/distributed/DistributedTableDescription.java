/**
 *
 */



package org.unesco.jisis.jisisutils.distributed;

/**
 * @author  jc_dauphin
 */
public class DistributedTableDescription {
    private Class[]  columnClasses_;
    private String[] columnNames_;
    private long     rowCount_;

    /**
     * Constructor for DistributedTableDescription.
     * @exception If the columnNames or columnClasses array are null or of differing
     * lengths.
     */
    public DistributedTableDescription(String[] columnNames, Class[] columnClasses, long rowCount) throws Exception {
        if ((columnNames == null) || (columnClasses == null) || (columnNames.length != columnClasses.length)) {
            throw new Exception(
                "Either the columnNames array or the columnClasses array is null or the lengths of the arrays are not equal.");
        }

        columnNames_   = columnNames;
        columnClasses_ = columnClasses;
        rowCount_      = rowCount;
    }

    /**
     * Returns an array of the column names.
     */
    public String[] getColumnNames() {
        return columnNames_;
    }

    /**
     * Returns an array of the column classes.
     */
    public Class[] getColumnClasses() {
        return columnClasses_;
    }

    /**
     * Returns the row count.
     */
    public int getRowCount() {     
        return (int) rowCount_;
    }

    public void setRowCount(long rowCount) {
        rowCount_ = rowCount;
    }

    /**
     * Returns the column count.
     */
    public int getColumnCount() {
        return columnNames_.length;
    }
}
