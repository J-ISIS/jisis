/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisisutils;

import org.openide.util.Exceptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 *
 * @author jc_dauphin
 */
public class ObjectFile {
    RandomAccessFile dataFile;
    String           sFileName;

    public ObjectFile(String sName) throws IOException {
        sFileName = sName;
        dataFile  = new RandomAccessFile(sName, "rw");
    }

    // returns file position object was written to.
    public synchronized long writeObject(Object obj) {
        if (obj == null) {
            throw new RuntimeException("writeObject null object!");
        }
        long pos = 0;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream    oos  = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            int datalen = baos.size();
            // append record
            pos = dataFile.length();
            dataFile.seek(pos);
            // write the length of the output
            dataFile.writeInt(datalen);
            dataFile.write(baos.toByteArray());
            baos = null;
            oos  = null;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return pos;
    }

    // Replace object at position lPos, should be same length.
    public synchronized long writeLong(long lPos, long value) throws IOException {
        dataFile.seek(lPos);
        dataFile.writeLong(value);
        return lPos;
    }

    // Replace object at position lPos, should be same length.
    public synchronized long writeLong(long value) throws IOException {
        // append record
        long pos = dataFile.length();
        dataFile.seek(pos);
        dataFile.writeLong(value);
        return pos;
    }

    public synchronized long readLong(long lPos) throws IOException {
        dataFile.seek(lPos);
        long value = dataFile.readLong();
        return value;
    }

    // Replace object at position lPos, should be same length.
    public synchronized long writeObject(long lPos, Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream    oos  = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        int datalen = baos.size();
        // replace record
        int prevDataLength = getObjectLength(lPos);
        assert datalen == prevDataLength : "object length should be the same for replacing";
        dataFile.seek(lPos);
        // write the length of the output
        dataFile.writeInt(datalen);
        dataFile.write(baos.toByteArray());
        baos = null;
        oos  = null;
        
        return lPos;
    }

    // get the current object length - restore or move?
    public synchronized int getObjectLength(long lPos) throws IOException {
        dataFile.seek(lPos);
        return dataFile.readInt();
    }

    public synchronized void readBytes(long lPos, byte[] data) throws IOException {
        dataFile.seek(lPos);
        dataFile.readFully(data);
    }

    public synchronized void writeBytes(long lPos, byte[] data) throws IOException {
        dataFile.seek(lPos);
        dataFile.write(data);
    }

    public synchronized Object readObject(long lPos) throws IOException, ClassNotFoundException {
        dataFile.seek(lPos);
        int datalen = dataFile.readInt();
        if (datalen > dataFile.length()) {
            throw new IOException("Data file is corrupted. datalen: " + datalen);
        }
        byte[] data = new byte[datalen];
        dataFile.readFully(data);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream    ois  = new ObjectInputStream(bais);
        Object               o    = ois.readObject();
        bais = null;
        ois  = null;
        data = null;
        
        return o;
    }

    public long length() throws IOException {
        return dataFile.length();
    }

    public void close() throws IOException {
        dataFile.close();
    }

    synchronized void rewriteObject(long filePointer, Serializable obj) throws IOException {
        writeObject(filePointer, obj);
    }

    synchronized void writeLongs(long lPos, long[] data) throws IOException {
        dataFile.seek(lPos);
        for (long value : data) {
            dataFile.writeLong(value);
        }
    }

    synchronized void readLongs(long lPos, int j, int len, long[] data) throws IOException {
        dataFile.seek(lPos);
        for (int i = 0; i < len; i++) {
            data[i + j] = dataFile.readLong();
        }
    }

}    // end of ObjectFile class

