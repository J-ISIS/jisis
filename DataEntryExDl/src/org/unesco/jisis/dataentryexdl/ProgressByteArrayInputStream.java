/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.io.ByteArrayInputStream;

/**
 *
 * @author jcd
 */
public class ProgressByteArrayInputStream extends ByteArrayInputStream {

   private final long size;
   private int totalWork;
  
   String name = "Read Progress";

   public ProgressByteArrayInputStream(byte[] bytes) {
      super(bytes);
      this.size = bytes.length;
      this.totalWork = bytes.length;
   }

   

   @Override
   public synchronized int read(byte[] b, int off, int len) {
      if (totalWork >= 0) {
         statusChanged(b.length);
         totalWork -= b.length;
      }
      return super.read(b, off, len);
   }
   
   private void statusChanged(int value) {
      System.out.println("ProgressByteArrayInputStream value="+value+" size= "+size+"totalWork="+totalWork);
//		if (notifier != null) {
//			notifier.statusChanged(status);
//		}
	}
   
}
