/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisisutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 *  A replacement for a Vector that uses an LRU cache and persists all
 *  entries greater than the cache size.
 *
 * a proxy Vector filled with “stub” entries, a hash table of cache entries,
 * a Least Recently Used (LRU) list, and an ObjectFile of serialized objects.
 *
 */
public class FileExtendibleVector implements Cloneable, Serializable {
    /** count of the number of these objects created. */
    static long lCount;

    /** The cache. */
    Hashtable<Integer, CacheEntry> cache_ = new Hashtable<Integer, CacheEntry>();

    /**
     * ObjectFile that stores the persisted objects.
     *   @see ObjectFile
     */
    ObjectFile objFile_ = null;

    /**
     * The Vector mirror. This will store only a
     *   VectorEntry object for each entry inserted.
     */
    Vector rows_ = new Vector();

    /** Current cache size. */
    int cacheSize_;

    /**
     * First entry in the Least Recently Used (LRU) linked list.  The first
     * entry represents the most recently accessed cache element.
     */
    CacheEntry firstCacheEntry_;

    /**
     * Last entry in the Least Recently Used (LRU) linked list. The last
     * entry represents the least recently accessed cache element.
     */
    CacheEntry lastCacheEntry_;

    /** Maximum size the cache is allowed to grow to. */
    int maxCacheSize_;

    /** Name of file where persistent objects are stored. */
    String sTmpName_;

    /**
     * Constructor that sets the cache size.  File is not opened unless
     * array grows greater than cache size.
     * @param maxCacheSize Maximum size the cache should be allowed to grow to.
     */
    public FileExtendibleVector(int maxCacheSize) {
        maxCacheSize_ = maxCacheSize;
    }
File temp;
    private final void openTempFile() throws IOException {

         temp = File.createTempFile("vatmp",".obf");
         temp.deleteOnExit();
         sTmpName_ = temp.getName();


        objFile_ = new ObjectFile(sTmpName_);
    }

    /**
     * Method to add an object to this Vector. We add the object to either the
     * cache or disk.
     * @param o The object to add to the array.
     */
    public final void add(Serializable o) throws IOException {
        StubEntry e = new StubEntry();
        rows_.add(e);
        if (cacheSize_ < maxCacheSize_) {
            e.inCache = true;
            CacheEntry ce = new CacheEntry();
            ce.o   = o;
            ce.key = new Integer(rows_.size() - 1);
            cache_.put(ce.key, ce);
            cacheSize_++;
            // add Cache Entry to LRU list
            if (lastCacheEntry_ != null) {
                // add to front of list
                ce.next               = firstCacheEntry_;
                firstCacheEntry_.prev = ce;
                firstCacheEntry_      = ce;
            } else {
                // empty list
                firstCacheEntry_ = lastCacheEntry_ = ce;
            }
        } else {
           // Cache is full, write object on file
            if (objFile_ == null) {
                openTempFile();
            }
            e.filePointer = objFile_.writeObject(o);
        }
    }

    /**
     * Vector method to access an element at a specific index.
     * We retrieve the object from either the cache or from the object file.
     * @param index - The vector index of the element to retrieve.
     * @returns     - The Object associated with this index.
     */
    public final Object get(int index) throws IndexOutOfBoundsException, IOException {
        if ((index < 0) || (index >= rows_.size())) {
            throw new IndexOutOfBoundsException("In " + index + " out of bounds.");
        }
        StubEntry e = (StubEntry) rows_.get(index);
        Object    o = null;
        if (e.inCache) {
            // get it
            CacheEntry ce = null;
            ce = (CacheEntry) cache_.get(new Integer(index));
            if (ce == null) {
                throw new IOException("Element at idx " + index + " is NULL!");
            }
            if (((ce != null) && (ce.o == null))) {
                throw new IOException("Cache Element's object at idx " + index + " NOT in cache!");
            }
            o = ce.o;
            if (ce != firstCacheEntry_) {
                // remove it from its current place in list
                if (ce.next != null) {
                    ce.next.prev = ce.prev;
                } else {    // this is the last one in the cache
                    lastCacheEntry_ = ce.prev;
                }
                ce.prev.next = ce.next;
                // move it to front of list
                ce.next               = firstCacheEntry_;
                ce.prev               = null;
                firstCacheEntry_.prev = ce;
                firstCacheEntry_      = ce;
            }
        } else {
            // this will now be in the cache
            e.inCache = true;
            // retrieve and put in cache
            try {
                o = objFile_.readObject(e.filePointer);
            } catch (ClassNotFoundException cnfe) {
                throw new IOException(cnfe.getMessage());
            }
            // *** Check if cache is FULL!
            if (cacheSize_ == maxCacheSize_) {
                // REMOVE LRU (tail of list).
                CacheEntry leastUsed = lastCacheEntry_;
                if (leastUsed.prev != null) {
                    leastUsed.prev.next  = null;
                    lastCacheEntry_      = leastUsed.prev;
                    lastCacheEntry_.next = null;
                } else {
                    // removing the only entry
                    firstCacheEntry_ = lastCacheEntry_ = null;
                }
                // add retrieved object to cache
                CacheEntry ce = new CacheEntry();
                ce.o   = o;
                ce.key = new Integer(index);
                cache_.put(ce.key, ce);
                // add Cache Entry to LRU list
                if (lastCacheEntry_ != null) {
                    // add to front of list
                    ce.next               = firstCacheEntry_;
                    firstCacheEntry_.prev = ce;
                    firstCacheEntry_      = ce;
                } else {
                    // empty list
                    firstCacheEntry_ = lastCacheEntry_ = ce;
                }
                // get StubEntry for one to remove
                StubEntry outStubEntry = (StubEntry) rows_.get(leastUsed.key.intValue());
                // get Object out of cache
                CacheEntry outCacheEntry = (CacheEntry) cache_.remove(leastUsed.key);
                if (outCacheEntry == null) {
                    throw new RuntimeException("Cache Entry at " + leastUsed.key + " is Null!");
                }
                if ((outCacheEntry != null) && (outCacheEntry.o == null)) {
                    throw new RuntimeException("Cache object at " + leastUsed.key + " is Null!");
                }
                Object outObject = outCacheEntry.o;
                outStubEntry.inCache = false;
                if (outStubEntry.filePointer == -1) {
                    // had gone right to cache
                    outStubEntry.filePointer = objFile_.writeObject((Serializable) outObject);
                } else {
                    // already in the file - size changed?
                    int                   iCurrentSize =
                        objFile_.getObjectLength(outStubEntry.filePointer);
                    ByteArrayOutputStream baos         = new ByteArrayOutputStream();
                    ObjectOutputStream    oos          = new ObjectOutputStream(baos);
                    oos.writeObject((Serializable) outObject);
                    oos.flush();
                    int datalen = baos.size();
                    if (datalen <= iCurrentSize) {
                        objFile_.rewriteObject(outStubEntry.filePointer, (Serializable) outObject);
                    } else {
                        outStubEntry.filePointer = objFile_.writeObject((Serializable) outObject);
                    }
                    baos      = null;
                    oos       = null;
                    outObject = null;
                }
            } else {
                CacheEntry ce = new CacheEntry();
                ce.o   = o;
                ce.key = new Integer(index);
                cache_.put(ce.key, ce);
                cacheSize_++;
                // add Cache Entry to LRU list
                if (lastCacheEntry_ != null) {
                    // add to front of list
                    ce.next               = firstCacheEntry_;
                    firstCacheEntry_.prev = ce;
                    firstCacheEntry_      = ce;
                } else {
                    // empty list
                    firstCacheEntry_ = lastCacheEntry_ = ce;
                }
            }
        }
        return o;
    }

    public final void set(int index, Serializable o) throws IOException {
        if ((index < 0) || (index >= rows_.size())) {
            throw new IndexOutOfBoundsException("In " + index + " out of bounds.");
        }
        // Be sure we got it in cache
        Object    oldObj = get(index);
        StubEntry e      = (StubEntry) rows_.get(index);
        if (!e.inCache) {
            throw new RuntimeException("Object should be in cache " + index);
        }
        CacheEntry ce = (CacheEntry) cache_.get(new Integer(index));
        if (ce == null) {
            throw new IOException("Element at idx " + index + " is NULL!");
        }
        if (((ce != null) && (ce.o == null))) {
            throw new IOException("Cache Element's object at idx " + index + " NOT in cache!");
        }
        ce.o = o;
        if (e.filePointer != -1) {
            // already in the file - size changed?
            int                   iCurrentSize = objFile_.getObjectLength(e.filePointer);
            ByteArrayOutputStream baos         = new ByteArrayOutputStream();
            ObjectOutputStream    oos          = new ObjectOutputStream(baos);
            oos.writeObject((Serializable) o);
            oos.flush();
            int datalen = baos.size();
            if (datalen <= iCurrentSize) {
                objFile_.rewriteObject(e.filePointer, (Serializable) o);
            } else {
                e.filePointer = objFile_.writeObject((Serializable) o);
            }
            baos = null;
            oos  = null;
            // outObject = null;
        }
    }

    public final Object remove(int index) throws IndexOutOfBoundsException, IOException {
        return null;
    }

    public final void copyTo(Object oArray[]) throws IOException {}

    public final void close() {}

    public final void finalize() {}

    public final int size() {
        return rows_.size();
    }

    /**
     * An inner class that represents an entry in the cache.
     */
    class CacheEntry {
        /**
         * Key that maps to the element. We use an Integer object
         *   that corresponds to the index in the Array.
         */
        Integer key;

        /** Object to store in cache. */
        Object o;

        /** References that support a doubly linked list. */
        CacheEntry prev, next;
    }

    /**
     * An inner class that represents a minimal entry in the Array.
     */
    class StubEntry {
        /** If entry on disk, this is where it was stored. */
        long filePointer = -1;

        /** flag that indicates if entry is in the cache or on disk. */
        boolean inCache;
    }
}
