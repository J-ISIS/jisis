/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.sorting;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author jc_dauphin
 */
public class JisisSortRecordInfo implements RecordInformation {
   private Comparator comp_;

   public JisisSortRecordInfo(Comparator comp) {
      comp_ = comp;
   }
   public Comparator getComparator() {
      return comp_;
   }

   public RecordReader newRecordReader(File f) throws IOException {
      return new JisisSortRecordReader(f);
   }

   public RecordWriter newRecordWriter(File f) throws IOException {
      return new JisisSortRecordWriter(f);
   }

}
