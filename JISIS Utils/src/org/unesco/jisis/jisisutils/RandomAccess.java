package org.unesco.jisis.jisisutils;

import java.io.IOException;


public abstract class RandomAccess {
    final private byte[] fmtBuf_ = new byte[8];


     /**
     * Read <tt>len</tt> bytes from location <tt>pos</tt> of the region
     * into the buffer <tt>buf</tt>, starting at <tt>offset</tt>.
     */
    public abstract void read(long pos, byte[] buf, int offset, int len)
            throws IOException;

    /*
     *  Write <tt>len</tt> bytes from position <tt>offset</tt> in buffer
     * <tt>buf</tt> to position <tt>pos</tt> bytes into the managed
     * area.
     */
    public abstract void write(long pos, byte[] buf, int offset, int len)
            throws IOException;

    public abstract void flush() throws IOException;

    /**
     * Return the size of the managed region.
     */
    public abstract long size();
    /**
     * Resize the managed region.
     */
    public abstract void resize(long newSize) throws IOException;

    public abstract void close() throws IOException;

   

    public int readByte(int pos) throws IOException {
        synchronized (fmtBuf_) {
            read(pos, fmtBuf_, 0, 1);

            return fmtBuf_[0] & 0xff;
        }
    }

    /**
     * Write a integer value to the specified position in the buffer
     */
    public int readInt(long pos) throws IOException {
        synchronized (fmtBuf_) {
            read(pos, fmtBuf_, 0, 4);

            return ByteUtil.getInt(fmtBuf_, 0);
        }
    }

    /**
     * Read a long value from the specified position in the buffer
     */
    public long readLong(long pos) throws IOException {
        synchronized (fmtBuf_) {
            read(pos, fmtBuf_, 0, 8);

            return ByteUtil.getLong(fmtBuf_, 0);
        }
    }

    

    public void writeByte(int pos, int val) throws IOException {
        synchronized (fmtBuf_) {
            fmtBuf_[0] = (byte) (val & 0xff);
            write(pos, fmtBuf_, 0, 1);
        }
    }

    /**
     * Write a integer value to the specified position in the buffer
     */
    public void writeInt(long pos, int val) throws IOException {
        synchronized (fmtBuf_) {
            ByteUtil.putInt(fmtBuf_, 0, val);
            write(pos, fmtBuf_, 0, 4);
        }
    }

    /**
     * Write a long value to the specified position in the buffer
     */
    public void writeLong(long pos, long val) throws IOException {
        synchronized (fmtBuf_) {
            ByteUtil.putLong(fmtBuf_, 0, val);
            write(pos, fmtBuf_, 0, 8);
        }
    }
}
