/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisisutils;

import org.openide.util.Exceptions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Arrays;

/**
 *
 * @author jc_dauphin
 */
public class VArray {
    private FileExtendibleVector cache;
    private transient int         pageSize_;

    /**
     *    Create a new, empty VArray.
     */
    private VArray(int maxCacheSize, int pageSize) throws IOException {
        pageSize_ = pageSize;
        cache     = new FileExtendibleVector(maxCacheSize);
    }

    /**
     *
     *    @param maxCacheSize - maximum number of page buffers
     *    @param pageSize - page size in number of longs
     *
     */
    public static VArray makeVArray(int maxCacheSize, int pageSize, String fileName)
            throws IOException {
        return new VArray(maxCacheSize, pageSize);
    }

    /**
     *  The index-th long is returned
     *  @param index
     *  @return
     */
    public synchronized long fetchValue(long index) throws IOException {
        int        pageNo = (int) (index / pageSize_);
        int        offset = (int) (index % pageSize_);
        PageBuffer buf    = null;
        buf = (PageBuffer) cache.get(pageNo);
        long value = buf.getData()[offset];
        return value;
    }

    public synchronized void storeValue(long index, long value) {
        try {
            int        pageNo = (int) (index / pageSize_);
            int        offset = (int) (index % pageSize_);
            PageBuffer buf    = null;
            if (pageNo >= cache.size()) {
                // Add new pages with zeros
                buf = new PageBuffer(pageNo, pageSize_);
                for (int i = cache.size(); i <= pageNo; i++) {
                    buf.setPageNumber(i);
                    cache.add(buf);
                }
            }
            buf                  = (PageBuffer) cache.get(pageNo);
            buf.getData()[offset] = value;
            cache.set(pageNo, buf);
        } catch (IndexOutOfBoundsException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String args[]) {
        try {
            VArray va = makeVArray(50, 10,
                                   "c:\\NetBeansProjects\\jisis NetBeans Project\\jisis\\vatest");
            for (int i = 0; i < 100000; i++) {
                va.storeValue(i, i);
            }
            for (int i = 0; i < 100000; i++) {
                long k = va.fetchValue(i);
                System.out.println("fetchValue i=" + i + " value=" + k);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}

/**
 *     An inner class that represents an entry in the cache.
 */
class PageBuffer implements Serializable {
    /** Object to store in cache. */
    private transient long[] data_;

    /** Key that maps to the buffer */
    private transient int pageNumber_;
    private transient int pageSize_;

    public PageBuffer(int pageNumber, int pageSize) {
        pageNumber_ = pageNumber;
        pageSize_   = pageSize;
        data_       = new long[pageSize];
        Arrays.fill(data_, 0);
    }

    public long[] getData() {
        return data_;
    }

    public int getPageNumber() {
        return pageNumber_;
    }

    public void setPageNumber(int pageNumber) {
        pageNumber_ = pageNumber;
    }

    private void reset(int pageNumber) {
        pageNumber_ = pageNumber;
        Arrays.fill(data_, 0);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(pageNumber_);
        s.writeInt(pageSize_);
        for (int i = 0; i < pageSize_; i++) {
            s.writeLong(data_[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        pageNumber_ = s.readInt();
        pageSize_   = s.readInt();
        data_       = new long[pageSize_];
        for (int i = 0; i < pageSize_; i++) {
            data_[i] = s.readLong();
        }
    }
}
