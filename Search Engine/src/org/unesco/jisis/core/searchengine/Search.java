package org.unesco.jisis.core.searchengine;

//~--- non-JDK imports --------------------------------------------------------

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.logging.Level;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.unesco.jisis.corelib.searchengine.AnyTermFinder;
import org.unesco.jisis.corelib.searchengine.Scanner;
import org.unesco.jisis.corelib.searchengine.SearchParser;

public class Search {
   private Expr                result_;
   private String              idxPath_;
   private Query               query_;
   

   /**
    * Constructor for using an ISIS query
    * @param srcExpr
    * @param idxpath
    * @param fieldArr
    * @throws java.lang.Exception
    */
   public Search(String srcExpr, String idxpath, int[] fieldArr) throws Exception {
      System.out.println("srcExpr: " + srcExpr);

      /** Create the ISIS Search Language parser */
      SearchParser parser = new SearchParser(new Scanner(new StringReader(srcExpr)));

      /** Get a reference to the Intermediate Representation (IR) */
      result_  = (Expr) parser.parse().value;
      idxPath_ = idxpath;

      FieldList fl        = result_.getFieldList();
      FieldList fieldList = new FieldList();
      String[]  fields    = new String[fieldArr.length];

      for (int i = 0; i < fieldArr.length; i++) {
         fieldList.add(new Integer(fieldArr[i]));    // Tags
      }

      /** Translate IR into Lucene Query language */
      query_ = result_.toLucene(fieldList);

      System.out.println("Lucene query: " + query_);
   }

   public Search(String srcExpr, String idxpath, int[] fieldArr, boolean bLucene) throws Exception {
      System.out.println("Lucene srcExpr: " + srcExpr+ " Lucene Search="+bLucene);

      idxPath_ = idxpath;

      FieldList fieldList = new FieldList();
      String[]  fields    = new String[fieldArr.length];

      for (int i = 0; i < fieldArr.length; i++) {
         fieldList.add(new Integer(fieldArr[i]));    // Tags
      }

      String      defaultField = Integer.toString(fieldArr[0]);
      
      QueryParser queryParser  = new QueryParser(defaultField,
                      //new WhitespaceAnalyzer()); Causing pbs with phrases
               new KeywordAnalyzer());
              //new SimpleAnalyzer());
      queryParser.setLowercaseExpandedTerms(false);
      query_ = queryParser.parse(srcExpr);

      System.out.println("Parsed Lucene query: " + query_);
   }

   public long[] search() throws Exception {
      // dumpIndex();
      IndexReader reader   = IndexReader.open(idxPath_);
      Searcher    searcher = new IndexSearcher(reader);
      Hits        hits     = searcher.search(query_.rewrite(reader));
      long        docs[]   = new long[hits.length()];

      // logger.log(Level.INFO, "hits: " + hits.length());
      for (int i = 0; i < docs.length; i++) {
         docs[i] = Long.parseLong(hits.doc(i).get("MFN"));
      }

      reader.close();
      return docs;
   }

   public void dumpIndex() {
      try {
         IndexReader   reader  = IndexReader.open(idxPath_);
         int           numDocs = reader.maxDoc();
         List<Field> v       = new ArrayList<Field>();

         for (int i = 0; i < numDocs; i++) {
            TermFreqVector[] tfv = reader.getTermFreqVectors(i);
            int              n   = tfv.length;

            for (int j = 0; j < n; j++) {
               TermFreqVector tf = tfv[j];

               System.out.println("name=" + tf.getField());

               int nterms = tf.size();

               for (int k = 0; k < nterms; k++) {
                  System.out.println("term=" + tf.getTerms()[k] + " Freq="
                                     + tf.getTermFrequencies().toString());
               }
            }
//          Document doc = reader.document(i);
//          Enumeration fldEnum = doc.fields();
//          while (fldEnum.hasMoreElements()) {
//             Field field = (Field) fldEnum.nextElement();
//             v.add(field);
//             Global.output(field.toString());
//          }
         }
      } catch (IOException ex) {
         java.util.logging.Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   void debug() {
      FieldList fieldList = new FieldList();

      for (int i = 1; i < 5; i++) {
         fieldList.add(new Integer(i));
      }

      result_.debugTree(fieldList);
   }
}


//. $ count
class Counter {
   private int count;

   public Counter() {
      this.count = 1;
   }

   public Counter add() {
      this.count++;

      return this;
   }

   public int getCount() {
      return count;
   }
}


class FieldList {
   java.util.Vector fieldList;

   public FieldList() {
      this.fieldList = new java.util.Vector();
   }

   public FieldList(Integer f) {
      this.fieldList = new java.util.Vector();

      this.fieldList.add(f);
   }

   public FieldList add(Integer f) {
      this.fieldList.add(f);

      return this;
   }

   java.util.Enumeration getEnum() {
      return fieldList.elements();
   }
}


class Expr {
   public void debugTree(FieldList f) {}

   public void debugTree(int field) {}

   public Query toLucene(FieldList fieldList) {
      return null;
   }

   public Query toLucene(int field) {
      return null;
   }

   public Term getTerm(int field) {
      return null;
   }

   public FieldList getFieldList() {
      return null;
   }
}


class SearchTerm extends Expr {
   private String term;

   public SearchTerm(String term) {
      this.term = term;
   }

   public void debugTree(int field) {
      // System.out.println("Term v("+field+")="+term);
   }

   public Term getTerm(int field) {
      return new Term("" + field, term);
   }

   public Query toLucene(int field) {
      Query res = null;

      if (getTerm(field).text().contains("$")) {
         String termRep = term.replace('$', '*');
         // logger.log(Level.FINE, "term modified: " + term);
         Term t = new Term(new Integer(field).toString(), termRep);

         // logger.log(Level.FINE, "term: " + t);
         res = new WildcardQuery(t);
         // res = wcCard;

      } else if (term.startsWith("any ")) {
         String        termReplaced = term.replaceFirst("any ", "");
         AnyTermFinder at           = new AnyTermFinder(termReplaced);
         String        queryString  = at.findAnyTerm();
         // System.out.println("In any term found this string query " + queryString);
         //QueryParser qp = new QueryParser(new Integer(field).toString(), new StandardAnalyzer());
         QueryParser qp = new QueryParser(new Integer(field).toString(), new KeywordAnalyzer());
         
         
         try {
            res = qp.parse(queryString);
         } catch (ParseException ex) {
            ex.printStackTrace();
         }

      } else {
         res = new TermQuery(getTerm(field));
      }

      return res;
   }
}


class DotExpr extends Expr {
   private Expr left;
   private Expr right;
   private int  dotCount;

   public DotExpr(int dotCount, Expr left, Expr right) {
      this.dotCount = dotCount;
      this.left     = left;
      this.right    = right;
   }

   public void debugTree(int field) {
      // System.out.println("Start dot(.): field("+field+")");
      left.debugTree(field);
      right.debugTree(field);
   }

   public Query toLucene(int field) {
      Term        leftTerm    = left.getTerm(field);
      Term        rightTerm   = right.getTerm(field);
      PhraseQuery phraseQuery = new PhraseQuery();

      phraseQuery.setSlop(dotCount);
      phraseQuery.add(leftTerm);
      phraseQuery.add(rightTerm);

      return phraseQuery;
   }
}


class AmpExpr extends Expr {
   private Expr left;
   private Expr right;
   private int  ampCount;

   public AmpExpr(int ampCount, Expr left, Expr right) {
      this.ampCount = ampCount;
      this.left     = left;
      this.right    = right;
   }

   public void debugTree(int field) {
      // System.out.println("Start amp(&): field("+field+")");
      left.debugTree(field);
      right.debugTree(field);
   }

   public Query toLucene(int field) {
      Term        leftTerm    = left.getTerm(field);
      Term        rightTerm   = right.getTerm(field);
      PhraseQuery phraseQuery = new PhraseQuery();

      phraseQuery.setSlop(ampCount);
      phraseQuery.add(leftTerm);
      phraseQuery.add(rightTerm);

      return phraseQuery;
   }
}


class FExpr extends Expr {
   private Expr left;
   private Expr right;

   public FExpr(Expr left, Expr right) {
      this.left  = left;
      this.right = right;
   }

   public void debugTree(int field) {
      // System.out.println("Start F(F): field("+field+")");
      left.debugTree(field);
      right.debugTree(field);
   }

   public Query toLucene(int field) {
      Query        leftQ     = left.toLucene(field);
      Query        rightQ    = right.toLucene(field);
      BooleanQuery boolQuery = new BooleanQuery();

      boolQuery.add(leftQ, BooleanClause.Occur.MUST);
      boolQuery.add(rightQ, BooleanClause.Occur.MUST);

      return boolQuery;
   }
}


class GExpr extends Expr {
   private Expr left;
   private Expr right;

   public GExpr(Expr left, Expr right) {
      this.left  = left;
      this.right = right;
   }

   public void debugTree(int field) {
      // System.out.println("Start G(G): field("+field+")");
      left.debugTree(field);
      right.debugTree(field);
   }

   public Query toLucene(int field) {
      Query        leftQ     = left.toLucene(field);
      Query        rightQ    = right.toLucene(field);
      BooleanQuery boolQuery = new BooleanQuery();

      boolQuery.add(leftQ, BooleanClause.Occur.MUST);
      boolQuery.add(rightQ, BooleanClause.Occur.MUST);

      return boolQuery;
   }
}


class FdQual extends Expr {
   private Expr                expr;
   private FieldList           fieldList;
   

   public FdQual(Expr expr, FieldList fieldList) {
      this.expr      = expr;
      this.fieldList = fieldList;
   }

   public FdQual(Expr expr) {
      this(expr, null);
   }

   public void debugTree(FieldList fieldList) {
      if (this.fieldList == null) {
         this.fieldList = fieldList;
      }

      java.util.Enumeration en = fieldList.getEnum();

      while (en.hasMoreElements()) {
         int field = ((Integer) en.nextElement()).intValue();

         expr.debugTree(field);
      }
   }

   public Query toLucene(FieldList fieldList) {
     System.out.println("into toLucene");

      if (this.fieldList == null) {
         this.fieldList = fieldList;
      }

      BooleanQuery boolQuery = new BooleanQuery();
      Enumeration  en        = this.fieldList.getEnum();

      /** Search term in all field */
      while (en.hasMoreElements()) {
         int   field = ((Integer) en.nextElement()).intValue();
         Query query = expr.toLucene(field);

         System.out.println("query: " + query);
         boolQuery.add(query, BooleanClause.Occur.SHOULD);
      }

      return boolQuery;
   }

   public FieldList getFieldList() {
      return this.fieldList;
   }
}


class AndExpr extends Expr {
   private Expr left;
   private Expr right;

   public AndExpr(Expr left, Expr right) {
      this.left  = left;
      this.right = right;
   }

   public void debugTree(FieldList fieldList) {
      // System.out.println("Start AND");
      left.debugTree(fieldList);
      right.debugTree(fieldList);
   }

   public Query toLucene(FieldList fieldList) {
      Query        leftQ     = left.toLucene(fieldList);
      Query        rightQ    = right.toLucene(fieldList);
      BooleanQuery boolQuery = new BooleanQuery();

      boolQuery.add(leftQ, BooleanClause.Occur.MUST);
      boolQuery.add(rightQ, BooleanClause.Occur.MUST);

      return boolQuery;
   }
}


class NotExpr extends Expr {
   private Expr left;
   private Expr right;

   public NotExpr(Expr left, Expr right) {
      this.left  = left;
      this.right = right;
   }

   public void debugTree(FieldList fieldList) {
      // System.out.println("Start NOT");
      left.debugTree(fieldList);
      right.debugTree(fieldList);
   }

   public Query toLucene(FieldList fieldList) {
      Query        leftQ     = left.toLucene(fieldList);
      Query        rightQ    = right.toLucene(fieldList);
      BooleanQuery boolQuery = new BooleanQuery();

      boolQuery.add(leftQ, BooleanClause.Occur.SHOULD);
      boolQuery.add(rightQ, BooleanClause.Occur.MUST_NOT);

      return boolQuery;
   }
}


class OrExpr extends Expr {
   private Expr left;
   private Expr right;

   public OrExpr(Expr left, Expr right) {
      this.left  = left;
      this.right = right;
   }

   public void debugTree(FieldList fieldList) {
      // System.out.println("Start OR");
      left.debugTree(fieldList);
      right.debugTree(fieldList);
   }

   public Query toLucene(FieldList fieldList) {
      Query        leftQ     = left.toLucene(fieldList);
      Query        rightQ    = right.toLucene(fieldList);
      BooleanQuery boolQuery = new BooleanQuery();

      boolQuery.add(leftQ, BooleanClause.Occur.SHOULD);
      boolQuery.add(rightQ, BooleanClause.Occur.SHOULD);

      return boolQuery;
   }
}


class AnyTerm extends Expr {
   private String term;

   // private Expr right;
   public AnyTerm(String term) {
      this.term = term;
   }

   public void debugTree(int field) {
      // System.out.println("Term v("+field+")="+term);
   }

   public Term getTerm(int field) {
      return new Term("" + field, term);
   }

   public Query toLucene(FieldList fieldList) {
      // Query leftQ = new Query(getTerm(1));
//    leftQ.
      // BooleanQuery boolQuery = new BooleanQuery();
      // boolQuery.add(leftQ, BooleanClause.Occur.SHOULD);
//    term.replace('$', '*');
      // Term t = new Term("1", term);
//    System.out.println("il termine è: " + t);
      // WildcardQuery wcCard = new WildcardQuery(t);
      // wcCard.
      return null;
   }
}
