/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.datasearch;

import org.apache.lucene.analysis.util.CharTokenizer;


/**
 *
 * @author jc_dauphin
 */

public class MyWhitespaceTokenizer extends CharTokenizer {
  /** Construct a new WhitespaceTokenizer. */
  public MyWhitespaceTokenizer() {
    super();
  }

  /** Collects only characters which do not satisfy
   * {@link Character#isWhitespace(char)}
     * @param i.*/
   @Override
  protected boolean isTokenChar(int i) {
    return !Character.isWhitespace(i);
  }

 
}