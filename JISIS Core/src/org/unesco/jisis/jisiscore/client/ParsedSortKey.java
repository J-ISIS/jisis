/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisiscore.client;

import java.util.List;
import org.unesco.jisis.corelib.index.ParsedFstEntry;

/**
 *
 * @author jcd
 */
public class ParsedSortKey {
   /** Length of key */
   public int length_;

   /** The parsed FST */
   public List<ParsedFstEntry> parsedSortFstEntries_;

   public ParsedSortKey(int length, List<ParsedFstEntry> parsedSortFstEntries) {
      length_               = length;
      parsedSortFstEntries_ = parsedSortFstEntries;
   }
}