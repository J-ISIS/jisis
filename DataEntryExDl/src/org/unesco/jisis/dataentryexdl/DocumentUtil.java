/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

/**
 *
 * @author jc Dauphin
 */
public class DocumentUtil {

  public static byte[] getBytesFromFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    // Get the size of the file
    long length = file.length();

    if (length > Integer.MAX_VALUE) {
      // File is too large
    }

    // Create the byte array to hold the data
    byte[] bytes = new byte[(int) length];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length
            && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
      throw new IOException("Could not completely read file " + file.getName());
    }

    // Close the input stream and return bytes
    is.close();
    return bytes;
  }

  public static String getMimeTypeFromFile(File file) throws IOException {
    FileInputStream is = null;
    String mimeType = "Unknown";
    try {

      is = new FileInputStream(file);

      ContentHandler contenthandler = new BodyContentHandler();
      Metadata metadata = new Metadata();
      metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
      Parser parser = new AutoDetectParser();
      // OOXMLParser parser = new OOXMLParser();
      ParseContext parseContext = new ParseContext();
      parser.parse(is, contenthandler, metadata, parseContext);
      mimeType = metadata.get(Metadata.CONTENT_TYPE);
      System.out.println("Mime: " + metadata.get(Metadata.CONTENT_TYPE));
      System.out.println("Title: " + metadata.get(Metadata.TITLE));
      System.out.println("Author: " + metadata.get(Metadata.AUTHOR));
      System.out.println("content: " + contenthandler.toString());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (is != null) {
        is.close();
      }
    }
    return mimeType;

  }
}
