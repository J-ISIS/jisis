package org.unesco.jisis.jisisutils;



/**
 * This class encapsulates various utilities for manipulating byte arrays
 * which contain primitive values, such as integers, strings, etc.
 *
 * @author Stan Bailes
 */
public class ByteUtil {
    public static char[] hexMap = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F'
    };

    

    public static boolean boolProperty(String s) {

        return Boolean.getBoolean(s);
    }

    public static byte[] bytes(String s) {

        byte[] buf = s.getBytes();
        return buf;
    }

    public static char[] bytesToChars(byte[] bbuf, int boff, char[] cbuf,
                                    int coff, int clen) {

        int clim = coff + clen;

        while (coff < clim) {
            int c1 = bbuf[boff++] & 0xff;
            int c2 = bbuf[boff++] & 0xff;

            cbuf[coff++] = (char) ((c1 << 8) | c2);
        }
        return cbuf;
    }

    public static void charsToBytes(char[] cbuf, int coff, byte[] bbuf,
                                    int boff, int clen) {

        int clim = coff + clen;

        while (coff < clim) {
            char c = cbuf[coff++];

            bbuf[boff++] = (byte) (c >> 8);
            bbuf[boff++] = (byte) (c & 0xff);
        }
    }

    public static String hexBytes(byte[] buf) {

        if (buf == null) {
            return "<null>";
        }

        return hexBytes(buf, 0, buf.length);
    }

    public static String hexBytes(byte[] buf, int off, int len) {

        if (buf == null) {
            return "<null>";
        }

        StringBuffer sb = new StringBuffer();

        for (int i = off; i < off + len; i++) {
            if (i >= buf.length) {
                sb.append("###### OVERRUN ######");

                break;
            }

            byte b = buf[i];

            sb.append(hexMap[(b >> 4) & 0xf]);
            sb.append(hexMap[b & 0xf]);
        }

        return sb.toString();
    }

    public static String hexInts(byte[] buf) {

        return hexInts(buf, 0, buf.length);
    }

    public static String hexInts(byte[] buf, int off, int len) {

        if (buf == null) {
            return "<null>";
        }

        StringBuffer sb = new StringBuffer();

        for (int i = off; i < off + len; i++) {
            if (i >= buf.length) {
                sb.append("###### OVERRUN ######");

                break;
            }

            byte b = buf[i];

            sb.append(hexMap[(b >> 4) & 0xf]);
            sb.append(hexMap[b & 0xf]);

            if ((((i + 1) - off) % 4) == 0) {
                sb.append(' ');
            }
        }

        return sb.toString();
    }

    public static int intProperty(String s, int defVal) {

        String v = System.getProperty(s);

        return (v == null)
               ? defVal
               : Integer.parseInt(v);
    }

    /**
     * Write a double value as height bytes (MSB first into the buffer
     * @param buf the buffer
     * @param pos the byte offset of the first value
     * @param val the value to write to the buffer
     *
     */
    public static final void putDouble(byte[] buf, int pos, double val) {

        ByteUtil.putLong(buf, pos, Double.doubleToLongBits(val));
    }

    /**
     * Write a float value as four bytes (MSB first into the buffer
     * @param buf the buffer
     * @param pos the byte offset of the first value
     * @param val the value to write to the buffer
     *
     */
    public static final void putFloat(byte[] buf, int pos, float val) {

        ByteUtil.putInt(buf, pos, Float.floatToIntBits(val));
    }

    /**
     * Write an integer value as four bytes (MSB first) into the buffer
     *
     * @param buf the buffer
     * @param pos the byte offset of the first value
     * @param val the value to write to the buffer
     */
    public static final void putInt(byte[] buf, int pos, int val) {

        buf[pos++] = (byte) ((val >>> 24) & 0xff);
        buf[pos++] = (byte) ((val >>> 16) & 0xff);
        buf[pos++] = (byte) ((val >>> 8) & 0xff);
        buf[pos]   = (byte) ((val) & 0xff);
    }

    /**
     * Write an long value as eight bytes (MSB first) into the buffer
     *
     * @param buf the buffer
     * @param pos the byte offset of the first value
     * @param val the value to write to the buffer
     */
    public static final void putLong(byte[] buf, int pos, long val) {

        buf[pos++] = (byte) ((val >>> 56) & 0xff);
        buf[pos++] = (byte) ((val >>> 48) & 0xff);
        buf[pos++] = (byte) ((val >>> 40) & 0xff);
        buf[pos++] = (byte) ((val >>> 32) & 0xff);
        buf[pos++] = (byte) ((val >>> 24) & 0xff);
        buf[pos++] = (byte) ((val >>> 16) & 0xff);
        buf[pos++] = (byte) ((val >>> 8) & 0xff);
        buf[pos]   = (byte) ((val) & 0xff);
    }

    /**
     * Write an short value as two bytes (MSB first) into the buffer
     *
     * @param buf the buffer
     * @param pos the byte offset of the first value
     * @param val the value to write to the buffer
     */
    public static final void putShort(byte[] buf, int pos, short val) {

        buf[pos++] = (byte) ((val >>> 8) & 0xff);
        buf[pos]   = (byte) ((val) & 0xff);
    }

    public static final void putString(byte[] buf, int pos, String s) {

        int len = s.length();

        for (int i = 0; i < len; i++) {
            buf[pos++] = (byte) s.charAt(i);
        }
    }

    public static String strBytes(byte[] buf) {

        if (buf == null) {
            return "<null>";
        }

        return strBytes(buf, 0, buf.length);
    }

    public static String strBytes(byte[] buf, int offset, int len) {

        if ((buf == null) || (len == 0)) {
            return "<null>";
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len; i++) {
            if ((i % 16) == 0 && ((len - i > 12) || (i > 0))) {
                if (i > 0) {
                    sb.append(' ');
                    sb.append(' ');

                    for (int j = (i - 1) & ~15; j < i; j++) {
                        byte c = buf[offset + j];

                        if ((c >= 0x20) && (c <= 0x7f)) {
                            sb.append((char) c);
                        } else {
                            sb.append('.');
                        }
                    }
                }

                sb.append('\n');
            }

            byte b = buf[offset + i];

            sb.append(hexMap[(b >> 4) & 0xf]);
            sb.append(hexMap[b & 0xf]);
            sb.append(' ');
        }

        if (len > 16) {
            for (int j = len; (j & 15) != 0; j++) {
                sb.append("   ");
            }
        }

        sb.append("  ");

        for (int j = (len - 1) & ~15; j < len; j++) {
            byte c = buf[offset + j];

            if ((c >= 0x20) && (c <= 0x7f)) {
                sb.append((char) c);
            } else {
                sb.append('.');
            }
        }

        return sb.toString();
    }

    /**
     * To hell with character encodings ;-)
     */
    public static byte[] strCharsAsBytes(String s) {

        byte[] buf  = new byte[s.length() * 2];
        char[] cbuf = s.toCharArray();

        charsToBytes(cbuf, 0, buf, 0, s.length());

        return buf;
    }

    public static String strProperty(String s, String defVal) {

        String v = System.getProperty(s);

        return (v == null)
               ? defVal
               : v;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Get the eight-byte double stored at the specified location in the
     * buffer.
     *
     * @param buf the buffer from which the short is read.
     * @param pos the position in the buffer.
     */
    public static final double getDouble(byte[] buf, int pos) {

        return Double.longBitsToDouble(ByteUtil.getLong(buf, pos));
    }

    /**
     * Get the four-byte float stored at the specified location in the
     * buffer.
     *
     * @param buf the buffer from which the short is read.
     * @param pos the position in the buffer.
     */
    public static final float getFloat(byte[] buf, int pos) {

        return Float.intBitsToFloat(ByteUtil.getInt(buf, pos));
    }

    /**
     * Get the four-byte integer stored at the specified location in the
     * buffer.
     *
     * @param buf the buffer from which the integer is read.
     * @param pos the position in the buffer.
     */
    public static final int getInt(byte[] buf, int pos) {

        return ((buf[pos] & 0xff) << 24) + ((buf[pos + 1] & 0xff) << 16)
               + ((buf[pos + 2] & 0xff) << 8) + (buf[pos + 3] & 0xff);
    }

    /**
     * Get the eight-byte long stored at the specified location in the
     * buffer.
     *
     * @param buf the buffer from which the long is read.
     * @param pos the position in the buffer.
     */
    public static final long getLong(byte[] buf, int pos) {

        return ((long) (buf[pos] & 0xff) << 56)
               + ((long) (buf[pos + 1] & 0xff) << 48)
               + ((long) (buf[pos + 2] & 0xff) << 40)
               + ((long) (buf[pos + 3] & 0xff) << 32)
               + ((long) (buf[pos + 4] & 0xff) << 24)
               + ((long) (buf[pos + 5] & 0xff) << 16)
               + ((long) (buf[pos + 6] & 0xff) << 8)
               + ((long) buf[pos + 7] & 0xff);
    }

    /**
     * Get the two-byte short stored at the specified location in the
     * buffer.
     *
     * @param buf the buffer from which the short is read.
     * @param pos the position in the buffer.
     */
    public static final short getShort(byte[] buf, int pos) {

        return (short) (((buf[pos] & 0xff) << 8) + (buf[pos + 1] & 0xff));
    }
}
