/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisisutils;

import org.openide.util.Exceptions;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 *
 * @author jc_dauphin
 */
public class FileExtendibleArray {
    private Hashtable<Integer, PageBuffer> cache = new Hashtable<Integer, PageBuffer>();

    /**
     * this is the end of the linked list that referes to the least
     * recently used EmployeeProfile.
     */
    LinkedList lru = null;

    /**
     * This is the head of the linked list that refers to the most
     * recently used EmployeeProfile.
     */
    LinkedList mruList = null;

    /** The current size of the cache */
    private int        currentCacheSize;
    private ObjectFile file_;

    /** Maximum allowed pages in the cache */
    private int maxCacheSize;

    /** Number of pages allocated */
    private int pageCount_;
    private int pageSize_;

    /**
     *   Create a new, empty cache.
     */
    private FileExtendibleArray(int maxCacheSize, int pageSize, String fileName) throws IOException {
        pageSize_         = pageSize;
        this.maxCacheSize = maxCacheSize;
        currentCacheSize  = 0;
        pageCount_        = 0;
        file_             = new ObjectFile(fileName);
        mruList           = new LinkedList();
        clear();
    }
 /**
     *
     * @param maxCacheSize - maximum number of page buffers
     * @param pageSize - page size in number of longs
     *
     */
    public static FileExtendibleArray makeCache(int maxCacheSize, int pageSize, String fileName)
            throws IOException {
        return new FileExtendibleArray(maxCacheSize, pageSize, fileName);
    }

    /**
     * Look in the cache to find the PageBuffer which holds the page with
     * page number pageNumber
     *
     * @param pageNumber - Logical virtual memory page number
     * @return - PageBuffer or null if no such PageBuffer exists
     */
    private PageBuffer findPageBuffer(int pageNumber) {
        PageBuffer buf = cache.get(pageNumber);
        if (buf != null) {
            resetAge(buf);
        }
        return buf;
    }

    private synchronized void resetAge(PageBuffer buf) {
        synchronized (cache) {
            mruList.remove(buf.pageNumber_);
            mruList.addFirst(buf.pageNumber_);
        }
    }

    /**
     * Returns a reference to a free buffer. If necessary, it creates a free
     * buffer by displacing the oldest page (and writing it out)
     * @param pageNumber
     * @return
     */
    private synchronized PageBuffer fetchPageBuffer(int pageNumber) {
        PageBuffer buf = null;
        if (currentCacheSize < maxCacheSize) {
            // cache is not full
            buf = new PageBuffer(pageNumber);
            resetAge(buf);
            currentCacheSize++;
        } else {
            // Cache is full
            // Get the last recently used page
            int lruPageNo = (Integer) mruList.getLast();
            buf = cache.get(lruPageNo);
            long byteAddress = lruPageNo * pageSize_ * Long.SIZE;
            try {
                file_.writeLongs(byteAddress, buf.data_);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            mruList.removeLast();
            buf.reset(pageNumber);
            resetAge(buf);
        }
        return buf;
    }

    /**
     * Returns a reference to the PageBuffer which holds page pageNumber
     * if necessary the page is read from disk
     * @param pageNumber
     * @return
     */
    private synchronized PageBuffer readPageBuffer(int pageNumber) {
        PageBuffer buf = findPageBuffer(pageNumber);
        if (buf == null) {
            // Page is not in the real memory cache
            buf = fetchPageBuffer(pageNumber);

            readPage(buf);
            
        }
        return buf;
    }
    /**
     * The longs at positions i:i+len-1 of the file are read and placed
     * in the array a at location a[j]:a[j+len-1]
     *
     */
    public synchronized void read( int i, int len, int j, long[] a) {
       if (j+len > a.length) {
            throw new IllegalArgumentException("Cache:Read Invalid argument"
                    +"j="+j+" len="+len+" a.length="+a.length);
        }
        int        pageNo      = (int) (i / pageSize_);
        int        offset      = (int) (i % pageSize_);
        int k = len;
        int page = pageNo;
        while (k>0) {
           PageBuffer buf = readPageBuffer(page);
           int off = (page == pageNo) ? offset : 0;
           int m = Math.min(pageSize_-off, len);

           for (int ll=0; ll<m; ll++) {
              a[j+ll] = buf.data_[off+ll];
           }
           j += m;
           k -= m;
           page++;

        }
    }
    /**
     * The longs  placed at location a[i]:a[i+len-1] in the array a
     * are written at positions j:j+len-1 of the file
     *
     *
     */
    public synchronized void write( int i, int len, int j, long[] a) {
       if (j+len > a.length) {
            throw new IllegalArgumentException("Cache:Read Invalid argument"
                    +"j="+j+" len="+len+" a.length="+a.length);
        }
        int        pageNo      = (int) (i / pageSize_);
        int        offset      = (int) (i % pageSize_);
        int k = len;
        int page = pageNo;
        PageBuffer buf = null;
        while (k>0) {
           int off = (page == pageNo) ? offset : 0;
           int m = Math.min(pageSize_-off, len);
           if (m != pageSize_) {
              buf = readPageBuffer(page);
           } else {
              buf = fetchPageBuffer(page);
           }

           for (int ll=0; ll<m; ll++) {
              buf.data_[off+ll] = a[j+ll];
           }
           writePage(buf);
           j += m;
           k -= m;
           page++;

        }
    }

     private synchronized void readPage(PageBuffer buf) {
       long byteAddress = buf.pageNumber_*pageSize_*Long.SIZE;

      try {
         long fileLength = file_.length();
         if (byteAddress > fileLength ) {
            // Make empty pages

         }
         file_.readLongs(byteAddress, 0, pageSize_, buf.data_);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      }

    }

    private synchronized void writePage(PageBuffer buf) {
       long byteAddress = buf.pageNumber_*pageSize_*Long.SIZE;
      try {
         file_.writeLongs(byteAddress, buf.data_);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      }

    }
    
    /**
     * The index-th long is returned
     * @param index
     * @return
     */
    public synchronized long fetchValue(long index) throws IOException {
        
        int        pageNo      = (int) (index / pageSize_);
        int        offset      = (int) (index % pageSize_);
        PageBuffer buf           = null;
        buf = readPageBuffer(pageNo);
        long value = buf.data_[offset];
        return value;
    }

    public synchronized void storeValue(long index, long value) {
        
        int        pageNo      = (int) (index / pageSize_);
        int        offset      = (int) (index % pageSize_);
        PageBuffer buf           = null;
        buf = readPageBuffer(pageNo);
        buf.data_[offset] = value;
        writePage(buf);
        resetAge(buf);
        return;
    }

    /**
     *   Delete all the items from the cache.
     */
    public synchronized void clear() {
        cache            = new Hashtable();
        currentCacheSize = 0;
    }

    /**
     *   Returns the total size of the items in the cache.
     *   If all the items were added with the default size of 1,
     *   this is the number of items in the cache.
     *
     * @return
     *  The sum of the sizes of the items in the cache.
     */
    public synchronized int getCacheSize() {
        return currentCacheSize;
    }

    /**
     *   Returns the maxmimum number of items allowed in the cache.
     *
     * @return
     *  The maximum number of items allowed in the cache.
     */
    public synchronized int getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     *   Does the cache contain the given key?
     *
     * @param key
     *   The key used to refer to the object when <CODE>add()</CODE>
     *   was called.
     * @return
     *   true if the key was found.
     */
    public synchronized boolean containsKey(Object key) {
        return cache.containsKey(key);
    }

    /**
     * An inner class that represents an entry in the cache.
     */
    class PageBuffer {
        /** Object to store in cache. */
        long[] data_;

        /** Key that maps to the buffer */
        int pageNumber_;

        public PageBuffer(int pageNumber) {
            pageNumber_ = pageNumber;
            data_       = new long[pageSize_];
            Arrays.fill(data_, 0);
        }

        private void reset(int pageNumber) {
            pageNumber_ = pageNumber;
            Arrays.fill(data_, 0);
        }
    }
}
