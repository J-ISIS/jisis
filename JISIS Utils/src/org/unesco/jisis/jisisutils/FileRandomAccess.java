
/**
 *
 */
package org.unesco.jisis.jisisutils;


import java.io.IOException;
import java.io.RandomAccessFile;



/**
 * @author jc_dauphin
 *
 */
public class FileRandomAccess extends RandomAccess {
    long             maxSize_;
    long             pos_;
    RandomAccessFile ra_;

    
    public FileRandomAccess(RandomAccessFile ra, long maxSize) {
        maxSize_ = maxSize;
        ra_      = ra;
    }

    /**
     * Read <tt>len</tt> bytes from location <tt>pos</tt> of the region
     * into the buffer <tt>buf</tt>, starting at <tt>offset</tt>.
     */
    public void read(long p, byte[] b, int offset, int len)
            throws IOException {
        if (p != pos_) {
            ra_.seek(p);
            pos_ = p;
        }

        ra_.read(b, offset, len);
        pos_ += len;
    }

    /*
     *  Write <tt>len</tt> bytes from position <tt>offset</tt> in buffer
     * <tt>buf</tt> to position <tt>pos</tt> bytes into the managed
     * area.
     */
    public void write(long p, byte[] b, int offset, int len)
            throws IOException {
        checkCapacity(p + len);

        if (p != pos_) {
            ra_.seek(p);
            pos_ = p;
        }

        ra_.write(b, offset, len);
        pos_ += len;
    }

     public void flush() throws IOException {
        try {
            ra_.getFD().sync();
        } catch (Throwable t) {}
    }

     /**
     * Return the size of the managed region.
     */
    public long size() {
        try {
            return ra_.length();
        } catch (IOException ex) {
            return -1;
        }
    }

     /**
     * Resize the managed region.
     */
    public void resize(long newSize) throws IOException {
        ra_.setLength(newSize);
    }

     public void close() throws IOException {
        ra_.close();
    }

    final void checkCapacity(long t) throws IOException {
        if (t > ra_.length()) {
            if (t > maxSize_) {
                throw new IOException("full");
            }

            try {
                ra_.setLength(t);
            } catch (IOException e) {
                if (e.toString().indexOf("extended attributes") > 0) {
                    return;
                }

                throw e;
            }
        }
    }

}
