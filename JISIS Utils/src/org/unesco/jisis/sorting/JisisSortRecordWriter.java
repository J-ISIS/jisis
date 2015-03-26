/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.sorting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * @author jc_dauphin
 */
public class JisisSortRecordWriter extends BufferedWriter
        implements RecordWriter {

   public JisisSortRecordWriter(File file) throws IOException {

      super(new OutputStreamWriter(new FileOutputStream(file),"UTF8"));
   }

   public JisisSortRecordWriter(String fileName) throws IOException {
      super(new OutputStreamWriter(new FileOutputStream(fileName),"UTF8"));
   }

   public void writeRecord(Record r) throws IOException {
      write(r.toString(), 0, r.toString().length());
      newLine();

   }

   @Override
   public void finalize() throws IOException {
      close();
   }
}
