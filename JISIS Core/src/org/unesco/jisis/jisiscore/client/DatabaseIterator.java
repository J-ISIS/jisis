
package org.unesco.jisis.jisiscore.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.common.IDatabase;


/**
 *
 * @author sphilips
 */
public abstract class DatabaseIterator<E> implements Iterator<E>, Iterable<E> {
    
    protected int bufferSize = 50000;
    protected int position = 0;
    protected IDatabase db;
    protected Queue<E> buffer = new LinkedList<E>();
    
    /*  Abstract methods */
    protected abstract Collection<E> connect() throws DbException;

    
    // <editor-fold default="collapse" desc=" Constructors ">
    public DatabaseIterator(IDatabase db) { 
        this.db = db;
    }
    
    public DatabaseIterator(IDatabase db, int start) {
        this(db);
        this.position = start;
    }
    
    public DatabaseIterator(IDatabase db, int start, int buffersize) {
        this(db);
        this.position = start;
        this.bufferSize = buffersize;
    }
    // </editor-fold>
    
    // <editor-fold default="collapse" desc=" Getters ">
    public int getBufferSize() {
        return this.bufferSize;
    }
    
    public void setBufferSize(int buffersize) {
        this.bufferSize = buffersize;
    }
    
    public IDatabase getDatabase() {
        return this.db;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    // </editor-fold>    

    /* Implemented */
    protected void fetch() throws DbException {
        Collection<E> terms = this.connect();
        if (terms == null) return;
        this.buffer.addAll(terms);
        this.position += terms.size();
    }
    
    protected void fetch(int position, int bufferSize) throws DbException  {
        this.position = position;
        this.bufferSize = bufferSize;
        this.fetch();
    }
    
    protected void checkBuffer() throws DbException {
        if (this.buffer.isEmpty())
            this.fetch();
    }
    
    /* Iterator methods */
    public boolean hasNext() {
        try {
            this.checkBuffer();
        } catch (DbException e) {
            return false;
        }
        return !this.buffer.isEmpty();
    }

    public E next() throws NoSuchElementException {
        if (! this.hasNext())
            throw (new NoSuchElementException());
        return this.buffer.remove();
    }
    
    public void remove() throws UnsupportedOperationException {
        this.next();
    }
     
    
    public Iterator<E> iterator() {
        return this;
    }
}
