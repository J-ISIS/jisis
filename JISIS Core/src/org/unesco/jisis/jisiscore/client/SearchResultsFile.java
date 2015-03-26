/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisiscore.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author jcd
 */
public class SearchResultsFile {
    private static final int DB_NAME_MAX_SIZE = 128;
    private static final int QUERY_MAX_SIZE = 512;
    
      private static final int DIRECTORY_MAX_SIZE = 100;
      
      private static final int MAGIC_NUMBER = 13;
   
    class DirectoryEntry {

        byte[] dbName_;
        int searchNumber_;
        byte[] query_;
        long resultsCount_; // Number of hits (MFNs of records)
        long pos_;          // Starting byte position in file

        public DirectoryEntry() {
            dbName_ = new byte[DB_NAME_MAX_SIZE];
            searchNumber_ = -1;
            query_ = new byte[QUERY_MAX_SIZE];
            resultsCount_ = 0; // Number of hits (MFNs of records)
            pos_ = -1;
        }

        public DirectoryEntry(String dbName, int searchNumber, String query, long resultsCount, long pos) {
            dbName_ = new byte[DB_NAME_MAX_SIZE];
            byte[] bytes = dbName.getBytes();
            System.arraycopy(bytes, 0, dbName_, 0, bytes.length);
            searchNumber_ = searchNumber;
            query_ = new byte[QUERY_MAX_SIZE];
            bytes = query.getBytes();
            System.arraycopy(bytes, 0, query_, 0, bytes.length);
            resultsCount_ = resultsCount;
            pos_ = pos;
        }

        public void read() throws IOException {
            randomAccessFile_.readFully(dbName_);
            searchNumber_ = randomAccessFile_.readInt();
            randomAccessFile_.readFully(query_);
            resultsCount_ = randomAccessFile_.readLong();
            pos_ = randomAccessFile_.readLong();
        }

        public void write() throws IOException {
            randomAccessFile_.write(dbName_);
            randomAccessFile_.writeInt(searchNumber_);
            randomAccessFile_.write(query_);
            randomAccessFile_.writeLong(resultsCount_);
            randomAccessFile_.writeLong(pos_);
        }

    }
    
    public class Directory {
        DirectoryEntry[] directoryEntries_ ;
        
        public Directory() {
           directoryEntries_= new DirectoryEntry[DIRECTORY_MAX_SIZE];
        }
        public void read() throws IOException {
            randomAccessFile_.seek(SearchResultsFile.getHeaderSize());
            for (int i = 0; i<header_.searchSetCount_; i++) {
                directoryEntries_[i].read();
            }
        }
         public void write() throws IOException {
            randomAccessFile_.seek(SearchResultsFile.getHeaderSize());
            for (int i = 0; i<header_.searchSetCount_; i++) {
                directoryEntries_[i].write();
            }
        }
         
        
    }
    
    public class Header {
        int magicNumber_;
        int searchSetCount_;
        long lastPos_;
        
        public  Header(int magicNumber, int searchSetCount, long lastPos) {
            magicNumber_ = magicNumber;
            searchSetCount_ = searchSetCount;
            lastPos_ = lastPos;
        }
        
       
        public void read() throws IOException {
            randomAccessFile_.seek(0);
            magicNumber_ = randomAccessFile_.readInt();
            searchSetCount_ = randomAccessFile_.readInt();
            lastPos_ = randomAccessFile_.readLong();
        }
        
         public void write() throws IOException {
            randomAccessFile_.seek(0);
            randomAccessFile_.writeInt(magicNumber_);
            randomAccessFile_.writeInt(searchSetCount_);
            randomAccessFile_.writeLong(lastPos_);
        }
    }
    
    protected Header header_;
    
    protected Directory directory_;
    
    RandomAccessFile randomAccessFile_;
    
    
    public static int getHeaderSize() {
        int size = (Integer.SIZE / Byte.SIZE)
            + (Integer.SIZE / Byte.SIZE)
            + (Long.SIZE / Byte.SIZE);
        return size;
    }

    /**
     * Return size in bytes
     *
     * @return
     */
    public static int getDirectoryEntrySize() {
        int size = DB_NAME_MAX_SIZE
            + (Integer.SIZE / Byte.SIZE)
            + QUERY_MAX_SIZE
            + (Long.SIZE / Byte.SIZE)
            + (Long.SIZE / Byte.SIZE);
        return size;

    }

    public static int getDirectorySize() {
        int size = DIRECTORY_MAX_SIZE * getDirectoryEntrySize();
        return size;
    }
    
    public static long getDataPos() {
        int pos = getHeaderSize() + getDirectorySize();
        return pos;
    }
    
    public void open(String filePath) throws FileNotFoundException, IOException {
        randomAccessFile_ = new RandomAccessFile(filePath, "rw");
        /**
         * Read header
         */
         header_.read();
         
         directory_.read();


    }
    
    public void create(String filePath) throws FileNotFoundException, IOException {
         randomAccessFile_ = new RandomAccessFile(filePath, "rw");
        
         int magicNumber = MAGIC_NUMBER;
         int searchSetCount = 0;
         long lastPos = getDataPos();
         header_ = new Header(magicNumber, searchSetCount, lastPos);
         
         header_.write();
         directory_ = new Directory();
         
         directory_.write();
    }

    
}
