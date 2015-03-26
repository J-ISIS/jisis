
package org.unesco.jisis.jisiscore.client;

import java.util.Collection;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.index.DictionaryTerm;


/**
 *
 * @author sphilips
 */
public class DictionaryIterator extends DatabaseIterator<DictionaryTerm> {

    public DictionaryIterator(IDatabase db) { 
        super(db);
    }
    
    public DictionaryIterator(IDatabase db, int start) {
        super(db, start);
    }
    
    public DictionaryIterator(IDatabase db, int start, int buffersize) {
        super(db, start, buffersize);
    }
    
    
    /* Database access */
    protected Collection<DictionaryTerm> connect() throws DbException {
        long count = this.db.getDictionaryTermsCount();
        if (this.position >= count) return null;
        int end = this.position+this.bufferSize;
        if (end >= count) end = (int) count-1;
        return this.db.getDictionaryTermsChunck(this.position, end);
    }

}
