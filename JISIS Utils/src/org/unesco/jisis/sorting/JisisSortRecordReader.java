/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.sorting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author jc_dauphin
 */
public class JisisSortRecordReader extends BufferedReader
                                   implements RecordReader{

   public JisisSortRecordReader(File file) throws IOException {
      super(new InputStreamReader(new FileInputStream(file),"UTF8"));
   }
   public JisisSortRecordReader(String fileName) throws IOException{
       super(new InputStreamReader(new FileInputStream(fileName),"UTF8"));
   }
   public Record readRecord() throws IOException {
      String s = readLine();
      return (s == null) ? null : new  JisisSortRecord(s);
   }

   public void finalize() throws IOException {
     close();
   }


}
