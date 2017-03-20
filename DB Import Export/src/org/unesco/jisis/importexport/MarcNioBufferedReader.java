/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.importexport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.marc4j.MarcException;
import org.marc4j.marc.Leader;
import org.marc4j.marc.MarcFactory;
import org.openide.util.Exceptions;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;

/**
 *
 * @author jcd
 */
public class MarcNioBufferedReader {

    private static final int DEFAULT_LINE_LENGTH = 80;
    private byte[] lineBuf_ = null;
    private int lineLength_ = DEFAULT_LINE_LENGTH;
    
      /** Default Field Terminator */
    private final static int FIELD_TERMINATOR = 30;    /* 35 ICOMOS! */

    /** Default Record Terminator */
    private final static int      RECORD_TERMINATOR = 29;
    
     private boolean                firstRecord_      = true;

    /**
     * True if file has lines with cr/lf
     */
    private boolean bReadLines = true;

    private static final int BUFFER_SIZE = 2048 * 10;
    private final ByteBuffer byteBuffer_ = ByteBuffer.allocateDirect(BUFFER_SIZE);
    
     private int                    recordTerminator_ = RECORD_TERMINATOR;
    private int                    fieldTerminator_  = FIELD_TERMINATOR;

    /**
     * Mind two's complement arithmetic, especially when doing IO! In Java, bytes are signed, but the "bytes"
     * you read off an InputStream are unsigned.
     */
    private FileChannel channel_;
    
    MarcFactory factory_;
    
    
    

    MarcNioBufferedReader(FileChannel channel, int lineLength) {
        channel_ = channel;
        /* Be sure to fill buffer when starting */
        byteBuffer_.position(BUFFER_SIZE);
        if (lineLength == 0) {
            bReadLines = false;
            lineLength = DEFAULT_LINE_LENGTH;
        }
        lineLength_ = lineLength;
        lineBuf_ = new byte[lineLength_];
        
         factory_     = MarcFactory.newInstance();
    }

    void getBytes(byte[] buf) {
        for (int i = 0; i < buf.length; i++) {
            buf[i] = getByteFromBuf();
        }
    }

    boolean getBytesLeader(byte[] buf) {
        // Skip leading blanklines
        int c;

        while ((c = getByteFromBuf()) == '\n' || c == '\r') {
            // Skip blank lines
        }
        if (c == -1) {
            return false;
        }
        buf[0] = (byte) c;
        for (int i = 1; i < buf.length; i++) {
            buf[i] = getByteFromBuf();
        }
        return true;
    }

    byte getByteFromBuf() {
        byte b = 0;
        if (!byteBuffer_.hasRemaining()) {
            if (fillBuffer() == -1) {
                return -1;
            }
        }
        b = byteBuffer_.get();
        return b;
    }

    private int fillBuffer() {
        int nRead = -1;
        try {
            /* Reset read position in buffer to 0 */
            byteBuffer_.rewind();
            /* Read bytes from channel */
            nRead = channel_.read(byteBuffer_);
            if ((nRead == -1) || (nRead == 0)) {
                return -1;
            }
            byteBuffer_.limit(nRead);
            byteBuffer_.rewind();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return nRead;
    }

    /**
     * Read nbytes characters
     * 
     * if characters after nbytes characters are '\n' or '\r'
     * @param nbytes
     * @return 
     */
    private byte[] getLine(int nbytes) {
        int c;
        int p = 0;
        try {
            while ((c = getByte()) != -1) {
                if (bReadLines) {
                    if ((c == '\n') && (p == nbytes)) {
                        // end of line lf, line is read
                        break;
                    }
                    if ((c == '\r') && (p == nbytes)) {
                        // end of line cr, swallow the character
                        continue;
                    }
                } else {
                    // ISO file is not splitted in lines
                    if ((p == nbytes)) {
                        // We have read exactly nbytes character,
                        break;
                    }
                }
               
                if (p > lineBuf_.length) {
                    throw new RuntimeException("Buffer Line Length exceeded :" + lineBuf_.length);
                }
                lineBuf_[p] = (byte) c;
                p++;
                
            }
        } catch (Exception ioe) {
            c = -1;
        }
        if ((c == -1) && (p == 0)) {
            return null;
        } else {
            byte[] data = new byte[p];
            // System.out.println("p=" + p);
            System.arraycopy(lineBuf_, 0, data, 0, p);
            return data;
        }
    }

    private int getByte() {
        return getByteFromBuf() & 0xFF;
    }

    /**
     * 
     * @return 
     */
    boolean hasRemaining() {
        if (byteBuffer_.hasRemaining()) {
            return true;
        }
        if (fillBuffer() == -1) {
            return false;
        }
        if (byteBuffer_.hasRemaining()) {
            return true;
        }
        return false;
    }
    
     private Leader parseLeader(byte[] leaderData) throws IOException {
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(leaderData));
        Leader            ldr = factory_.newLeader();
        char[]            tmp = new char[5];
        isr.read(tmp);
        try {
            ldr.setRecordLength(Integer.parseInt(new String(tmp)));
        } catch (NumberFormatException e) {
            throw new MarcException("unable to parse record length", e);
        }
        ldr.setRecordStatus((char) isr.read());
        ldr.setTypeOfRecord((char) isr.read());
        tmp = new char[2];
        isr.read(tmp);
        ldr.setImplDefined1(tmp);
        ldr.setCharCodingScheme((char) isr.read());
        try {
            ldr.setIndicatorCount(Integer.parseInt(String.valueOf((char) isr.read())));
        } catch (NumberFormatException e) {
            throw new MarcException("unable to parse indicator count", e);
        }
        try {
            ldr.setSubfieldCodeLength(Integer.parseInt(String.valueOf((char) isr.read())));
        } catch (NumberFormatException e) {
            throw new MarcException("unable to parse subfield code length", e);
        }
        tmp = new char[5];
        isr.read(tmp);
        try {
            ldr.setBaseAddressOfData(Integer.parseInt(new String(tmp)));
        } catch (NumberFormatException e) {
            throw new MarcException("unable to parse base address of data", e);
        }
        tmp = new char[3];
        isr.read(tmp);
        ldr.setImplDefined2(tmp);
        tmp = new char[4];
        isr.read(tmp);
        ldr.setEntryMap(tmp);
        isr.close();
        return ldr;
    }
    
    /**
     *
     * @return
     */
    byte[] getRecord() {
        byte[] buf = null;
        Leader ldr;
        
          byte[] bytesLeader = new byte[24];
   // A tag shall contain 3 alphabetic or numeric ASCII characters
   byte[] bytesTag = new byte[3];
   // The length of the field, we assume it is 4 bytes, better approach
   // would be to take it from the Entry Map in the leader
   byte[] bytesLength = new byte[4];
   // The offset from the base address of the start pf the field,
   // we assume it is 5 bytes, better approach would be to take it from
   // the Entry Map in the leader
   byte[] bytesStart = new byte[5];

        /**
         * The leader is always at the beginning of a line and
         * cannot contain cr/lf
         * Leader is 24 Ascii characters (or bytes)
         */
        try {
            if (getBytesLeader(bytesLeader)) {
               throw new MarcException("EOF");
            }
            ldr = parseLeader(bytesLeader);
        } catch (BufferUnderflowException bue) {
            //System.out.println("Abnormal EOF lenght_=" + length_);
            throw new MarcException("EOF");
        } catch (IOException e) {
            throw new MarcException("error parsing leader with data: " + new String(bytesLeader), e);
        }
        // Full Record Length, include 24 bytes for leader and 1 byte for record
        // terminator
        int rlen = ldr.getRecordLength();
        // Allocate a byte buffer for the full MARC record
        buf = new byte[rlen];
        // Copy the leader
        System.arraycopy(bytesLeader, 0, buf, 0, bytesLeader.length);
        
        int    offset = bytesLeader.length;
        byte[] b      = null;
        while (offset < rlen) {
            // Bytes to read
            int nbytes = (offset == bytesLeader.length)
                         ? Math.min(lineLength_ - bytesLeader.length, rlen - offset)
                         : Math.min(lineLength_, rlen - offset);
            //assert nbytes > 0;
            b = getLine(nbytes);
            if (b == null) {
                break;
            }
            // System.out.println("rlen="+rlen+" offset="+offset+" b.length="+b.length);
            System.arraycopy(b, 0, buf, offset, b.length);
            offset += b.length;
            // Don't remember why I introduced this code!
//            if ((buf[offset - 2] == fieldTerminator_) && (buf[offset - 1] == recordTerminator_)) {
//                break;
//            }
        }
        b = null;
        
        if (firstRecord_) {
            // Adjust the RT and FT
            if ((buf[offset - 1] != recordTerminator_)) {
                GuiGlobal.output("Was expecting as RT: " + recordTerminator_);
                GuiGlobal.output("Changed to : " + (int) buf[offset - 1]);
                recordTerminator_ = buf[offset - 1];
            }
            if ((buf[offset - 2] != fieldTerminator_)) {
                GuiGlobal.output("Was expecting as FT: " + fieldTerminator_);
                GuiGlobal.output("Changed to : " + (int) buf[offset - 2]);
                fieldTerminator_ = buf[offset - 2];
            }
        } else {

            /** Check that we are at the end of a record */
            if ((buf[offset - 2] == fieldTerminator_) && (buf[offset - 1] == recordTerminator_)) {
                // Do nothing, we are at end of record
            } else {
                GuiGlobal.output("Error** Was expecting as FT: " + fieldTerminator_);
                GuiGlobal.output("Found : " + (int) buf[offset - 2]);
                GuiGlobal.output("Error** Was expecting as RT: " + recordTerminator_);
                GuiGlobal.output("Found : " + (int) buf[offset - 1]);
                // try to recover so that we are at beginning of record
                while ((b = getLine(lineLength_)) != null) {
                    int len = b.length;
                    if (len < 2) {
                        continue;
                    }
                    if ((b[len - 2] == fieldTerminator_) && (b[len - 1] == recordTerminator_)) {
                        break;
                    }
                }
            }
        }
        return buf;
    }

}

