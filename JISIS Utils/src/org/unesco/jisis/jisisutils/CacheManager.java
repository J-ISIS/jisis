/**
 *  A simple cache manager that uses write through
 */



package org.unesco.jisis.jisisutils;

import org.openide.util.Exceptions;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author   jc_dauphin
 */
public class CacheManager {
    private final static int INVALID_NPOS = -1;
    private int              cacheSize_   = 0;
    private CacheEntry[]     cache_       = null;
    protected ObjectFile     file_        = null;
    private int              nUsed_       = 0;    // Number of slots being used.


    public static CacheManager makeCacheManager(int cacheSize, String fileName) throws IOException {
       return new CacheManager(cacheSize, fileName);
    }


    private CacheManager(int cacheSize, String fileName) throws IOException {
        cacheSize_ = cacheSize;
        nUsed_     = 0;
        cache_     = new CacheEntry[cacheSize_];
        file_      = new ObjectFile(fileName);
    }





     /////////////////////////////////////////////////////////////////////
     //  Private helper functions

    /**
     * get the Least Recently Used cache slot
     * @return
     */
    private int LRU() {
        int islot    = 0;
        int maxCount = cache_[0].useCounts_;
        for (int i = 1; i < nUsed_; i++) {
            if (cache_[i].useCounts_ > maxCount) {
                maxCount = cache_[islot = i].useCounts_;
            }
        }
        return islot;
    }

    private int ageAndFindSlot(long pos) {
        int islot = INVALID_NPOS;
        for (int i = 0; i < nUsed_; i++) {
            if (cache_[i].diskAddress_ == pos) {
                islot = i;
            }
            cache_[i].useCounts_++;    // Age the blocks
        }
        return islot;
    }

    private int getFreeSlot() {
        int islot;
        if (nUsed_ < cacheSize_) {
            islot = nUsed_++;    // Found an unused slot.
        } else {
            // No free slots; get the Least Recently Used block
            islot = LRU();
        }
        return islot;
    }

    public void erase() throws IOException {
        cache_ = null;
        nUsed_ = 0;
    }

    public void invalidate() {
        nUsed_ = 0;
    }

    /**
     * Reads a data object at byte offset pos through the cache.
     * @param pos
     * @param data
     * @return
     * @throws Exception
     */
    public void read(long pos, Serializable obj) throws IOException {
        if (obj == null) {
            throw new IllegalArgumentException("CacheManager:read null data object");
        }
        if ((pos < 0) || (pos > file_.length())) {
            throw new IllegalArgumentException("CacheManager:read invalid file offset");
        }
        // Search in the cache
        int islot = ageAndFindSlot(pos);
        if (islot == INVALID_NPOS) {
            // Not in cache; we'll have to read it in from disk.
            // Get a free slot.
            islot = getFreeSlot();
            try {
                cache_[islot].obj_ = file_.readObject(pos);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            cache_[islot].diskAddress_ = pos;
        } else {
            // data is in cache
            cache_[islot].useCounts_ = 0;
            obj                      = (Serializable) cache_[islot].obj_;
        }
    }

    /**
     * Write obj at offset pos on file through the cache. We use a
     * write-through-cache strategy so that the disk and cache copies
     * are always the same.
     *
     * @param pos - byte offset on file
     * @param obj - data object to write
     * @return    - The file byte offset where the object was written
     * @throws java.io.IOException
     */
    public long write(long pos, Serializable obj) throws IOException {
        if (obj == null) {
            throw new IllegalArgumentException("CacheManager: null data object");
        }
        long fsize = file_.length();
        if ((pos < 0) || (pos > fsize)) {
            throw new IllegalArgumentException("CachManager: invalid offset");
        }
        int islot = ageAndFindSlot(pos);
        if (islot == INVALID_NPOS) {
            // Not in cache; find a free slot.
            islot = getFreeSlot();
        }
        cache_[islot].diskAddress_ = pos;
        cache_[islot].useCounts_   = 0;
        cache_[islot].obj_         = obj;
        file_.writeObject(pos, obj);
        return pos;
    }

    /**
     * Append obj at the end of file through the cache. We use a
     * write-through-cache strategy so that the disk and cache copies
     * are always the same.
     *
     * @param obj - data object to write
     * @return    - The file byte offset where the object was written
     * @throws java.io.IOException
     */
    public long write(Serializable obj) throws IOException {
        if (obj == null) {
            throw new IllegalArgumentException("CacheManager: null data object");
        }
        long pos = file_.writeObject(obj);
        // Not in cache; find a free slot.
        int islot = getFreeSlot();
        cache_[islot].diskAddress_ = pos;
        cache_[islot].useCounts_   = 0;
        cache_[islot].obj_         = obj;
        return pos;
    }

    class CacheEntry {
        long diskAddress_;

        /** The data Object */
        Object obj_;
        int    useCounts_;
    }
}
