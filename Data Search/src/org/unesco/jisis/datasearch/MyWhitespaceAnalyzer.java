/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.datasearch;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

/**
 *
 * @author jc_dauphin
 */

public final class MyWhitespaceAnalyzer extends Analyzer {
 

   @Override
   protected TokenStreamComponents createComponents(String string) {
      Tokenizer source = new MyWhitespaceTokenizer();
    
         return new TokenStreamComponents(source);

   }


}