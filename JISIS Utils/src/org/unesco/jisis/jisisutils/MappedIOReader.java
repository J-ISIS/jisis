/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *
 * @author jcdauphi
 */

/**
 * The FastExcel want to read excel more faster but memory use is more lower. So
 * we didn't read all file content into memory but use the Memory Mapped
 * File(for more information visit <a
 * href="http://en.wikipedia.org/wiki/Memory-mapped_file">
 * http://en.wikipedia.org/wiki/Memory-mapped_file</a>).That's fast and low
 * memory use. MappedIOReader is just a wrapper class to read Memory Mapped
 * File.<br/> Also we implement a slow version:<code>RandomAccessReader</code>
 * without Memory Mapped File.
 * BUG:when using MappedByteBuffer to open file,after closing file File.delete
 * method failure.<a herf="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id
 * =4715154">Cannot delete file if memory mapped with FileChannel.map </a>.<br/>
 * @see MappedByteBuffer
 * @see RandomAccessFile
 * @author <a href="guooscar@gmail.com">yAma</a>
 * 
 *         2008-11-22
 */
public class MappedIOReader implements Reader {
	/* Excel File */
	private File file;
	private RandomAccessFile raf;
	private FileChannel channel;
	private MappedByteBuffer mbb;

	/**
	 * Default constructor.
	 * 
	 * @param file
	 *            file to read.
	 */
	public MappedIOReader(File file) {
		this.file = file;
	}

	public void close() throws ReadException {
		try {
			channel.close();
			mbb.clear();
			raf.close();
			unmap(mbb);
		} catch (IOException e) {
			throw new ReadException(e);
		} catch (Exception e) {
			throw new ReadException(e);
		}
	}
	public void unmap(final Object buffer) throws Exception {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					Method getCleanerMethod = buffer.getClass()
					.getMethod("cleaner", new Class[0]);
					getCleanerMethod.setAccessible(true);
					sun.misc.Cleaner cleaner = (sun.misc.Cleaner)
					getCleanerMethod.invoke(buffer, new Object[0]);
					cleaner.clean();
				} catch (Exception e) {
					System.err.println("can not unmap MappedByteBuffer,"
							+e.getMessage());
				}
				return null;
			}
		});
	}

	public int position() throws ReadException {
		return mbb.position();
	}

	/**
	 * 
	 */
	public void open() throws ReadException {
		try {
			raf = new RandomAccessFile(file, "r");
			channel=raf.getChannel();
			mbb = channel.map(MapMode.READ_ONLY, 0, raf.length());
		} catch (IOException e) {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e1) {
					// give up;
				}
			}
			throw new ReadException(e);
		}
	}

	public void seek(int offset) throws ReadException {
		mbb.position(offset);
	}

	public byte read() throws ReadException {
		return mbb.get();
	}

	public int read(byte[] b) throws ReadException {
		mbb.get(b);
		return 1;
	}

	public int read(byte[] b, int offset, int length) throws ReadException {
		mbb.get(b, offset, length);
		return 1;
	}

	public long length() throws ReadException {
		try {
			return raf.length();
		} catch (IOException e) {
			try {
				raf.close();
			} catch (IOException e1) {
			}
			throw new ReadException(e);
		}
	}

}