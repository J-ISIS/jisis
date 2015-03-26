/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisiscore.client;

import org.unesco.jisis.corelib.pft.ISISFormatter;

/**
 *
 * @author jcd
 */
public class ParsedSortFstEntry {
   
   public int tag_;
   public int technique_;

   /** Itermediate Language of the Parsed FST Format */
   ISISFormatter ilFmt_;

   public ParsedSortFstEntry(int tag, int technique, ISISFormatter ilFmt) {
      tag_       = tag;
      technique_ = technique;
      ilFmt_     = ilFmt;
   }
}