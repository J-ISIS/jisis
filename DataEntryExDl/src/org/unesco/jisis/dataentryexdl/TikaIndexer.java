///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.unesco.jisis.dataentryexdl;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.tika.config.TikaConfig;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.parser.AutoDetectParser;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.parser.Parser;
//import org.apache.tika.sax.BodyContentHandler;
//import org.xml.sax.ContentHandler;
//
///**
// *
// * @author jc Dauphin
// */
//public class TikaIndexer extends Indexer {
//
//  private boolean DEBUG = true; //1
//  static Set textualMetadataFields = new HashSet(); //2
//  static { //2
//    textualMetadataFields.add(Metadata.TITLE); //2
//    textualMetadataFields.add(Metadata.AUTHOR); //2
//    textualMetadataFields.add(Metadata.COMMENTS); //2
//    textualMetadataFields.add(Metadata.KEYWORDS); //2
//    textualMetadataFields.add(Metadata.DESCRIPTION); //2
//    textualMetadataFields.add(Metadata.SUBJECT); //2
//  }
//
//  public static void main(String[] args) throws Exception {
//    if (args.length != 2) {
//      throw new Exception("Usage: java " + TikaIndexer.class.getName()
//              + " <index dir> <data dir>");
//    }
////    TikaConfig config = TikaConfig.getDefaultConfig(); //3
////    List<String> parsers = new ArrayList(config.getParser().keySet()); //3
////    Collections.sort(parsers); //3
////    Iterator<String> it = parsers.iterator(); //3
////    System.out.println("Mime type parsers:"); //3
////    while (it.hasNext()) { //3
////      System.out.println(" " + it.next()); //3
////    } //3
////    System.out.println(); //3
//    File indexDir = new File(args[0]);
//    File dataDir = new File(args[1]);
//    long start = new Date().getTime();
//    TikaIndexer indexer = new TikaIndexer(indexDir, dataDir);
//    int numIndexed = Indexer.index(indexDir, dataDir);
//    long end = new Date().getTime();
//    System.out.println("Indexing " + numIndexed + " files took "
//            + (end - start) + " milliseconds");
//  }
//
//  public TikaIndexer(File indexDir, File dataDir) throws IOException {
//    //super(indexDir, dataDir);
//    super();
//  }
//
//  protected boolean acceptFile(File f) {
//    return true; //4
//  }
//
//  protected Document getDocument(File f) throws Exception {
//    Metadata metadata = new Metadata();
//    metadata.set(Metadata.RESOURCE_NAME_KEY, //5
//            f.getCanonicalPath());
//// If you know content type (eg because this document
//// was loaded from an HTTP server), then you should also
//// set Metadata.CONTENT_TYPE
//// If you know content encoding (eg because this
//// document was loaded from an HTTP server), then you
//// should also set Metadata.CONTENT_ENCODING
//    InputStream is = new FileInputStream(f);
//    Parser parser = new AutoDetectParser();
//    ContentHandler handler = new BodyContentHandler();
//    ParseContext parseContext = new ParseContext();
//    try {
//      parser.parse(is, handler, metadata, parseContext);
//    } finally {
//      is.close();
//    }
//    Document doc = new Document();
//    doc.add(new Field("contents", handler.toString(), Field.Store.NO, Field.Index.ANALYZED));
//    if (DEBUG) {
//      System.out.println(" all text: " + handler.toString());
//    }
//    for (String name : metadata.names()) { //6
//      String value = metadata.get(name);
//      if (textualMetadataFields.contains(name)) {
//        doc.add(new Field("contents", value, //7
//                Field.Store.NO, Field.Index.ANALYZED));
//      }
//      doc.add(new Field(name, value, Field.Store.YES, Field.Index.NO));
//      if (DEBUG) {
//        System.out.println(" " + name + ": " + value);
//      }
//    }
//    if (DEBUG) {
//      System.out.println();
//    }
//    return doc;
//  }
//}