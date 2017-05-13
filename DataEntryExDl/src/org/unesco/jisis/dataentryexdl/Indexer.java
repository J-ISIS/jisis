/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author jc Dauphin
 * 
 * Using Indexer to index text files
Listing 1.1 shows the Indexer command-line program. It takes two arguments:
n A path to a directory where we store the Lucene index
n A path to a directory that contains the files we want to index
 * Indexer: traverses a file system and indexes .txt files
 */
public class Indexer {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new Exception("Usage: java " + Indexer.class.getName()
              + " <index dir> <data dir>");
    }
    File indexDir = new File(args[0]);


    File dataDir = new File(args[1]);


    long start = new Date().getTime();
    int numIndexed = index(indexDir, dataDir);
    long end = new Date().getTime();
    System.out.println("Indexing " + numIndexed + " files took "
            + (end - start) + " milliseconds");
  }
// open an index and start file directory traversal

  public static int index(File indexDir, File dataDir)
          throws IOException {
    if (!dataDir.exists() || !dataDir.isDirectory()) {
      throw new IOException(dataDir
              + " does not exist or is not a directory");
    }
     Directory fsd = FSDirectory.open(indexDir.toPath());

            StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
            standardAnalyzer.setMaxTokenLength(Integer.MAX_VALUE);

            IndexWriterConfig conf = new IndexWriterConfig(standardAnalyzer);

            conf.setMaxBufferedDocs(120);
            //((LogMergePolicy) conf.getMergePolicy()).setMergeFactor(120);

            IndexWriter writer = new IndexWriter(fsd, conf);

    
    
    indexDirectory(writer, dataDir);
    int numIndexed = writer.numDocs();
  
    writer.close();


    return numIndexed;
  }
// recursive method that calls itself when it finds a directory

  private static void indexDirectory(IndexWriter writer, File dir)
          throws IOException {
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      File f = files[i];
      if (f.isDirectory()) {
      } else if (f.getName().endsWith(".txt")) {


        indexFile(writer, f);
      }
    }
  }
// method to actually index a file using Lucene

  private static void indexFile(IndexWriter writer, File f)
          throws IOException {
    if (f.isHidden() || !f.exists() || !f.canRead()) {
      return;
    }
    System.out.println("Indexing " + f.getCanonicalPath());
    Document doc = new Document();
    doc.add(new TextField("contents", new FileReader(f)));


    doc.add(new StringField("filename", f.getCanonicalPath(),
            Field.Store.YES));


    writer.addDocument(doc);
  }
}