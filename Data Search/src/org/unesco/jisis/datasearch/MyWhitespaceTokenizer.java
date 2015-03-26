/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.datasearch;

import java.io.Reader;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.unesco.jisis.corelib.common.Lucene;


/**
 *
 * @author jc_dauphin
 */

public class MyWhitespaceTokenizer extends CharTokenizer {
  /** Construct a new WhitespaceTokenizer. */
  public MyWhitespaceTokenizer(Reader in) {
    super(Lucene.MATCH_VERSION, in);
  }

  /** Collects only characters which do not satisfy
   * {@link Character#isWhitespace(char)}.*/
   @Override
  protected boolean isTokenChar(int i) {
    return !Character.isWhitespace(i);
  }

 
}