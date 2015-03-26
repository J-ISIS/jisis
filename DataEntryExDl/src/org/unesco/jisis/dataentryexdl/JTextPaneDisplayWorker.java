/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;

/**
 *
 * @author jcd
 */
public class JTextPaneDisplayWorker extends SwingWorker<Document, String> {

   private final RepeatableField textPane_;
   private final String text_;
   private final DefaultStyledDocument document_;
   private final AttributeSet attr_;
   private final int size_;
   private int processedSize_;

   public JTextPaneDisplayWorker(RepeatableField textPane, String text) {
      textPane_ = textPane;
      text_ = text;
      size_ = text.length();
      processedSize_ = 0;
      document_ = (DefaultStyledDocument) textPane.getDocument();

      Element e = document_.getDefaultRootElement();
      // Copy attribute Set
      attr_ = e.getAttributes().copyAttributes();    

   }
//runs on a background thread.

   private final static int BUFFER_SIZE =  8192;
   @Override
   protected Document doInBackground() throws Exception {
     
      BufferedReader br = new BufferedReader(new StringReader(text_));
      char[] buffer = new char[BUFFER_SIZE];
     
      int len;
      processedSize_ = 0;
      while ((len = br.read(buffer, 0, BUFFER_SIZE)) != -1) {
         publish(new String(buffer, 0, len));
         processedSize_ += len;
         // update the progress
         setProgress((processedSize_ ) * 100 / size_);
      }
      br.close();

      return document_;
   }

   //runs on EDT, allowed to update gui
   @Override
   protected void process(List<String> textChunks) {
      
         System.out.println("process() Entry textChunks size=" + textChunks.size());
         System.out.println("process() Entry 1st chunk size=" + textChunks.get(0).length());
         for (String textChunk: textChunks) {  
           try { 
             document_.insertString(document_.getLength(), textChunk, attr_); // only updates a small part  
           } catch (BadLocationException e) {  
             e.printStackTrace();  
           }  
         }  
  
            
        System.out.println("process() Exit");

   }

  
}
