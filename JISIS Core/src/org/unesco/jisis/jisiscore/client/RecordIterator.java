/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisiscore.client;

import java.util.Collection;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.record.Record;


/**
 *
 * @author sphilips
 */
public class RecordIterator extends DatabaseIterator<Record> {

    public RecordIterator(IDatabase db) { 
        super(db);
    }
    
    public RecordIterator(IDatabase db, int start) {
        super(db, start);
    }
    
    public RecordIterator(IDatabase db, int start, int buffersize) {
        super(db, start, buffersize);
    }
    
    
    /* Database access */
    protected Collection<Record> connect() throws DbException {
        long count = this.db.getRecordsCount();
        if (this.position >= count) return null;
        int end = this.position+this.bufferSize;
        if (end >= count) end = (int) count;
        return  this.db.getRecordChunck(this.position, end);
    }

}
