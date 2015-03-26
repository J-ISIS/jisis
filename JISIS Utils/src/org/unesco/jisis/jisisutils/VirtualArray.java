/**
 *
 */



package org.unesco.jisis.jisisutils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Virtual array implementation, the header is not in the cache, we read
 * and write array elements through the cache
 * @author jc_dauphin
 *
 */
public class VirtualArray {
//    private byte[]           buf             = null;
//    private CacheManager     cache_          = null;
//    private long             currentElement_ = 0;
//    private File             f_              = null;
//    private String           fileName_       = null;
//    private FileRandomAccess file_           = null;
//    private int              headerSize_     = 0;
//    private VAHeader         header_;
//
//    public void createPersistentVA(String fileName, int elementSize, int cacheSize) {
//        try {
//            f_ = new File(fileName);
//            if (!f_.createNewFile()) {
//                System.err.println("createPersistentVA -- File already exists: " + fileName);
//                throw new Exception();
//            }
//            fileName_ = fileName;
//            RandomAccessFile ra = new RandomAccessFile(f_, "rw");
//            file_  = new FileRandomAccess(ra, 1024 * 1024 * 1024 * 1024);
//            cache_ = new CacheManager(elementSize, cacheSize);
//            cache_.connect(file_);
//            header_.eofPos_       = 0;
//            header_.elementSize_  = elementSize;
//            header_.elementCount_ = 0;
//            Object obj = (Object) header_;
//            byte[] h   = (byte[]) obj;
//            headerSize_ = h.length;
//            buf         = new byte[header_.elementSize_];
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Exception: " + e);
//        }
//    }
//
//    public void createTemporaryVA(int elementSize, int cacheSize, String tempDir) {
//        try {
//            // change path
//            File tmp = new File(tempDir);
//            // Make a file object for VA-....tmp, in the default temp directory
//            f_        = File.createTempFile("VA", "tmp");
//            fileName_ = f_.getCanonicalPath();
//            RandomAccessFile ra = new RandomAccessFile(f_, "rw");
//            file_  = new FileRandomAccess(ra, 1024 * 1024 * 1024 * 1024);
//            cache_ = new CacheManager(elementSize, cacheSize);
//            cache_.connect(file_);
//            header_.eofPos_       = 0;
//            header_.elementSize_  = elementSize;
//            header_.elementCount_ = 0;
//            Object obj = (Object) header_;
//            byte[] h   = (byte[]) obj;
//            headerSize_ = h.length;
//            buf         = new byte[header_.elementSize_];
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Exception: " + e);
//        }
//    }
//
//    // allow the array to grow
//    private void grow(long numToAdd) {}
//
//    // Use this function to do the something like a memset()...
//    public void nullOut() {}
//
//    public void openPersistentVA(String fileName, int elementSize, int cacheSize) {
//        try {
//            f_        = new File(fileName);
//            fileName_ = fileName;
//            RandomAccessFile ra = new RandomAccessFile(f_, "rw");
//            file_  = new FileRandomAccess(ra, 1024 * 1024 * 1024 * 1024);
//            cache_ = new CacheManager(elementSize, cacheSize);
//            cache_.connect(file_);
//            header_.eofPos_       = 0;
//            header_.elementSize_  = elementSize;
//            header_.elementCount_ = 0;
//            Object obj = (Object) header_;
//            byte[] h   = (byte[]) obj;
//            headerSize_ = h.length;
//            buf         = new byte[header_.elementSize_];
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Exception: " + e);
//        }
//    }
//
//    /**
//     * From the Cacheable class, return the underlying data object.
//     * When used as a <tt>Block</tt>, we typically use the <tt>read()</tt>
//     * and <tt>write</tt> methods to access the underlying data...
//     */
//    public final Object getElement(long i) {
//        long pos = headerSize_ + (i - 1) * header_.elementSize_;
//        try {
//            cache_.read(pos, buf);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        currentElement_ = i;
//        return buf;
//    }
//
//    /**
//     * Return a reference to the array containing the data for this
//     * block, and create a new buffer, so that the reference returned
//     * can be used in a subsequent <code>setData</code> call to another
//     * buffer.  This allows the contents of one block to be moved to
//     * another block with a minimum of array copying.
//     */
//    public byte[] getElementAndReset() {
//        byte[] r = buf;
//        buf = new byte[r.length];
//        // setDirty(true);
//        return r;
//    }
//
//    /**
//     * From the Cacheable class, set the underlying data object.
//     * When used as a <tt>Block</tt>, we typically use the <tt>read()</tt>
//     * and <tt>write</tt> methods to access the underlying data...
//     */
//    public void setElement(Object obj, long i) {
//        long   pos = headerSize_ + (i - 1) * header_.elementSize_;
//        byte[] r   = (byte[]) obj;
//        if (r.length != buf.length) {
//            throw new RuntimeException("Bad buffer length!");
//        }
//        buf = r;
//        try {
//            cache_.write(pos, buf);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        currentElement_ = i;
//        // setDirty(true);
//    }
//
//    private class VAHeader {
//        private int  elementCount_ = 0;
//        private int  elementSize_  = 8;
//        private long eofPos_       = 0;
//        private int  version_      = 1;
//    }
}
