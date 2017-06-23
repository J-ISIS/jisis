/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dictionary;

/**
 *
 * @author jc_dauphin
 */
import java.awt.Cursor;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.index.DictionaryTerm;
import static org.unesco.jisis.dictionary.TermsTableDataSource.columnNames;
import org.unesco.jisis.jisisutils.distributed.DistributedTableDescription;
import org.unesco.jisis.jisisutils.distributed.IDistributedTableDataSource;

/**
 *
 * @author jc_dauphin
 */
public class TermsTableDataSource implements IDistributedTableDataSource {

    private final IDatabase db_;
    private final JFrame frame_;
    private DistributedTableDescription tableDescription_ = null;
    static String[] columnNames = {"iTerm", "Field", "Term", "Freq"};
    static Class[] columnClasses = {String.class, String.class, String.class, String.class};
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TermsTableDataSource.class);

    /**
     * Class for loading terms from index. It uses SwingWorker to allow the
     * wait cursor
     */
    private class RowLoader extends SwingWorker<Object[][], Void> {

        private final JFrame frame_;
        private final int from_;
        private final int to_;
        private final String[] columnNames_;
        private final IDatabase db_;
        private DbException dbException_;
        
        public RowLoader(JFrame frame, String[] columnNames, IDatabase db, int from, int to) {
            frame_ = frame;
            from_ = from;
            to_ = to;
            db_ = db;
            columnNames_ = columnNames;
            frame_.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
           
        }

        @Override
        public Object[][] doInBackground() {
            Object[][] data = null;
            try {
                List<DictionaryTerm> terms = db_.getDictionaryTermsChunck(from_, to_);
                int nRows = terms.size();
                data = new Object[nRows][columnNames_.length];
                int n = columnNames_.length;

                System.out.println("retrieveRows nFields=" + n);

                for (int i = 0; i < nRows; i++) {
                    DictionaryTerm term = terms.get(i);
                    String s = null;
                    for (int j = 0; j < n; j++) {
                        switch (j) {
                            case 0:
                                s = Integer.toString(from_ + i);
                                break;
                            case 1:
                                s = term.getField();
                                break;
                            case 2:
                                s = term.getText();
                                break;
                            case 3:
                                s = Integer.toString(term.getFreq());
                                break;
                        }
                        data[i][j] = s;
                    }
                }
                return data;
            } catch (DbException ex) {
                LOGGER.error("Error when retrieving term rows from=[{}] to=[{}]", ex);
                dbException_ = ex;
            }
            return data;
        }

        @Override
        public void done() {
            frame_.setCursor(Cursor.getDefaultCursor());
            if (dbException_ != null) {
                JOptionPane.showMessageDialog(frame_,
                        dbException_.getMessage(),
                        "Error getting terms from=" + from_ + " to=" + to_,
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public TermsTableDataSource(IDatabase db, JFrame frame) {
        db_ = db;
        frame_ = frame;
    }
    
    /**
     * get the table description
     * @return
     * @throws Exception 
     */
    @Override
    public DistributedTableDescription getTableDescription() throws Exception {

        long nRows = db_.getDictionaryTermsCount();

        System.out.println("DistributedTableDescription nRows=" + nRows);

        if (tableDescription_ == null) {
            tableDescription_ = new DistributedTableDescription(columnNames,
                    columnClasses, nRows);
        }

        return tableDescription_;
    }

    /**
     * Retrieve term data from the index
     * 
     * @param from
     * @param to
     * @return
     * @throws Exception 
     */
    public Object[][] retrieveRows(int from, int to) throws Exception {

        RowLoader rowLoader = new RowLoader(frame_, columnNames, db_, from, to);
        rowLoader.execute();
        Object[][] data = rowLoader.get();
        return data;
    }

    @Override
    public Object[][] retrieveRows(int from, int to, ProgressHandle progress) throws Exception {

        RowLoader rowLoader = new RowLoader(frame_, columnNames, db_, from, to);
        rowLoader.execute();
        Object[][] data = rowLoader.get();
        return data;
    }

    @Override
    public int[] sort(int sortColumn, boolean ascending, int[] selectedRows) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSelectedRowsAndColumns(int[] selectedRows, int[] selectedColumns)
            throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] getSelectedRows() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] getSelectedColumns() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getRowCount() {

        long nRow = 0;
        try {
            nRow = db_.getDictionaryTermsCount();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        return nRow;
    }

    @Override
    public boolean isZeroBased() {
        return true; // To get the last element
    }
}
