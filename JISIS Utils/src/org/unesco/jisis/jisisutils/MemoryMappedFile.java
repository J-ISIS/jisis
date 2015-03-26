//
///**
// *
// */
//package org.unesco.jisis.jisisutils;
//
////~--- JDK imports ------------------------------------------------------------
//
///**
// * @author jc_dauphin
// *
// */
//import java.io.*;
//
//import java.lang.reflect.Method;
//
//import java.nio.*;
//import java.nio.channels.FileChannel;
//
//import java.security.*;
//
//import java.util.*;
//
////~--- classes ----------------------------------------------------------------
//
//public class MemoryMappedFile {
//
//  public static final int         MODE_READ_ONLY  = 0;
//  
//  public static final int         MODE_READ_WRITE = 1;
//  
//  // The file we are using for our data
//  private File                            file;
//  
//  // We open the file as a RandomAccessFile
//  public RandomAccessFile         raf;
//  
//  // We access the file through a MappedByteBuffer
//  private MappedByteBuffer        mbb;
//  
//  // The physical log
//  // private PhysicalLog physicalLog;
//  
//  // Are we currently in the middle of repairing?
//  private boolean                         repairing               = false;
//  
//  private int                                     access_mode             = MODE_READ_ONLY;
//  
//  private FileChannel                     channel;
//  
//  //Open an existing MemoryMappedFile
//  public MemoryMappedFile(File file) throws IOException {
//          this.file = file;
//          init(-1);
//  }
//  
//  // Open an existing MemoryMappedFile
//  public MemoryMappedFile(String filename) throws IOException {
//          this(new File(filename));
//  }
//  
//  private MemoryMappedFile() {
//  }
//  
//  // Create a new MemoryMappedFile
//  static public MemoryMappedFile create(File file, long size) throws IOException {
//          MemoryMappedFile cf = new MemoryMappedFile();
//          cf.file = file;
//          cf.init(size);
//          return cf;
//  }
//  
//  // Create a new MemoryMappedFile
//  static public MemoryMappedFile create(String filename, long size) throws IOException {
//          return create(new File(filename), size);
//  }
//  
//  // Accessors for the 'repairing' boolean
//  public boolean isRepairing() {
//          return repairing;
//  }
//  
//  public void setRepairing(boolean repairing) {
//          this.repairing = repairing;
//  }
//  
//  // Initialize a MemoryMappedFile.  If the file isn't already
//  // there, or it isn't long enough, write a 0 byte at
//  // the the last byte in the file, to make it that
//  // long.
//  private void init(long size) throws IOException {
//          raf = new RandomAccessFile(file, "rw");
//          
//          if (size > 0) {
//                  raf.seek(size - 1);
//                  int b = raf.read();
//                  if (b == -1) {
//                          raf.write((byte) 0);
//                  } else {
//                  }
//          }
//          
//          FileChannel fc = raf.getChannel();
//          mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size());
//  }
//  
//  // Read a byte from the file -- just get it from the
//  // underlying MappedByteBuffer
//  synchronized public byte get(int index) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          return mbb.get(index);
//  }
//  
//  // Read bytes from the file -- just get them from the
//  // underlying MappedByteBuffer
//  synchronized public MemoryMappedFile get(int index, byte dst[]) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          return get(index, dst, 0, dst.length);
//  }
//  
//  // Read bytes from the file -- just get them from the
//  // underlying MappedByteBuffer
//  synchronized public MemoryMappedFile get(int index, byte dst[], int offset, int length) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          mbb.position(index);
//          mbb.get(dst, offset, length);
//          return this;
//  }
//  
//  // Read bytes from the file -- just get them from the
//  // underlying MappedByteBuffer
//  synchronized public MemoryMappedFile get(int index, ByteBuffer dest) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          mbb.position(index);
//          mbb.limit(index + dest.remaining());
//          dest.put(mbb);
//          return this;
//  }
//  
//  // Read a char from the file -- just get it from the
//  // underlying MappedByteBuffer
//  synchronized public char getChar(int index) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          return mbb.getChar(index);
//  }
//  
//  // Read a double from the file -- just get it from the
//  // underlying MappedByteBuffer
//  synchronized public double getDouble(int index) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          return mbb.getDouble(index);
//  }
//  
//  // Read a float from the file -- just get it from the
//  // underlying MappedByteBuffer
//  synchronized public float getFloat(int index) throws MMTFileException {
//          if (mbb == null)
//                  throw new MMTFileException("Illegal operation: " + file + " is closed");
//          
//          return mbb.getFloat(index);
//  }
//  
//  // Read an integer from the file -- just get it from the
//  // underlying MappedByteBuffer
//  synchronized public int getInt(int index) throws MMTFileException {
//          if (mbb == null)
//                  throw new MMTFileException("Illegal operation: " + file + " is closed");
//          
//          return mbb.getInt(index);
//  }
//  
//  // Read a long from the file -- just get it from the
//  // underlying MappedByteBuffer
//  synchronized public long getLong(int index) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          return mbb.getLong(index);
//  }
//  
//  // Read a short from the file -- just get it from the
//  // underlying MappedByteBuffer
//  synchronized public short getShort(int index) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          return mbb.getShort(index);
//  }
//  
//  // Write a byte to the file -- mark it as dirty and
//  // then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile put(int index, byte b) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index);
//          mbb.put(index, b);
//          return this;
//  }
//  
//  // Write bytes to the file -- mark them as dirty and
//  // then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile put(int index, byte src[]) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          return put(index, src, 0, src.length);
//  }
//  
//  // Write bytes to the file -- mark them as dirty and
//  // then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile put(int index, byte src[], int offset, int length) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index, index + length - offset);
//          mbb.position(index);
//          mbb.put(src, offset, length);
//          return this;
//  }
//  
//  // Write bytes to the file -- mark them as dirty and
//  // then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile put(int index, ByteBuffer src) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index, index + src.remaining());
//          mbb.position(index);
//          mbb.put(src);
//          return this;
//  }
//  
//  // Write a char to the file -- mark the bytes as
//  // dirty and then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile putChar(int index, char value) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index, index + 2);
//          mbb.putChar(index, value);
//          return this;
//  }
//  
//  // Write a double to the file -- mark the bytes as
//  // dirty and then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile putDouble(int index, double value) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index, index + 8);
//          mbb.putDouble(index, value);
//          return this;
//  }
//  
//  // Write a float to the file -- mark the bytes as
//  // dirty and then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile putFloat(int index, float value) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index, index + 4);
//          mbb.putFloat(index, value);
//          return this;
//  }
//  
//  // Write an integer to the file -- mark the bytes as
//  // dirty and then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile putInt(int index, int value) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index, index + 4);
//          mbb.putInt(index, value);
//          return this;
//  }
//  
//  // Write a long to the file -- mark the bytes as
//  // dirty and then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile putLong(int index, long value) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index, index + 8);
//          mbb.putLong(index, value);
//          return this;
//  }
//  
//  // Write a short to the file -- mark the bytes as
//  // dirty and then write the byte to the underlying
//  // MappedByteBuffer
//  synchronized public MemoryMappedFile putShort(int index, short value) {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          markDirty(index, index + 2);
//          mbb.putShort(index, value);
//          return this;
//  }
//  
//  synchronized public String toString() {
//          if (mbb == null)
//                  throw new MemoryMappedFileException("Illegal operation: " + file + " is closed");
//          
//          return "Checkpointing " + mbb;
//  }
//  
//  // Mark a single byte as dirty
//  protected void markDirty(int index) {
//          markDirty(index, index + 1);
//  }
//  
//  // Mark a range of bytes as dirty.  Write them to the
//  // physical log.  @param start The start of the dirty
//  // region (inclusive) @param end The end of the dirty
//  // region (exclusive)
//  protected void markDirty(int start, int end) {
//          if (physicalLog == null) {
//                  long unique = System.currentTimeMillis();
//                  
//                  String suffix = ".ckpt." + unique + ".log";
//                  if (repairing) {
//                          suffix = ".repair" + suffix;
//                  }
//                  
//                  File plfile = new File(file + suffix);
//                  
//                  physicalLog = new PhysicalLog(plfile, "w");
//          }
//          
//          mbb.position(start);
//          mbb.limit(end);
//          
//          ByteBuffer subbuffer = mbb.slice();
//          
//          PhysicalLogEntry ple = new PhysicalLogEntry(start, end, subbuffer);
//          physicalLog.writeEntry(ple);
//          
//          mbb.limit(mbb.capacity());
//  }
//  
//  // Checkpoint the file.  Force all changes to disk,
//  // then close and delete the physical log.
//  synchronized public void checkpoint() {
//          long time = System.currentTimeMillis();
//          
//          System.out.print("Checkpointing ... ");
//          
//          // Force changes to the datafile to disk
//          mbb.force();
//          
//          // Close and delete the physical log
//          if (physicalLog != null) {
//                  physicalLog.close();
//                  physicalLog.delete();
//                  physicalLog = null;
//          }
//          
//          time = System.currentTimeMillis() - time;
//          System.out.println("done. (" + time + " ms)");
//  }
//  
//  // Close the file.  Checkpoint before closing.
//  synchronized public void close() throws IOException {
//          if (mbb == null)
//                  return;
//          
//          checkpoint();
//          
//          mbb = null;
//          raf.close();
//          file.setLastModified(System.currentTimeMillis());
//  }
//  
//}
