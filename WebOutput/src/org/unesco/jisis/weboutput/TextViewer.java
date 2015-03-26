/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.weboutput;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author jcd
 */
public class TextViewer  extends JPanel {
    protected JTextArea textArea;
    
   public TextViewer() {

      textArea = new JTextArea();
      add("Center", new JScrollPane(textArea));
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
   }
    /** Request focus for the editor.
    */
  
   @Override
   public void requestFocus() {
      if (textArea != null) {
         textArea.requestFocus();
      } else {
         super.requestFocus();
      }
   }
  
 

  /** Insert text into the editor, replacing the current selection (if any).
    *
    * @param text The text to insert.
    */
  
  public synchronized void insertText(String text)
    {
    textArea.replaceSelection(text);
    }

  /** Set the editable state of the editor.
    *
    * @param flag If <b>true</b>, the editor will be editable, otherwise it
    * will be non-editable.
    */
  
  public void setEditable(boolean flag)
    {
    textArea.setEditable(flag);
   
    }

  /** Set the text in the editor.
    *
    * @param text The text to display in the editor.
    */
  
  public synchronized void setText(String text)
    {
    textArea.setText(text);
    }

  /** Get the text in the editor.
    *
    * @return The text currently in the editor.
    */
  
  public synchronized String getText()
    {
    return(textArea.getText());
    }

  /** Perform a <i>cut</i> operation on the editor. Removes the selected text
    * from the editor, and stores it in the system clipboard.
    */

  public void cut()
    {
    textArea.cut();
    }

  /** Perform a <i>copy</i> operation on the editor. Copies the selected text
    * from the editor to the system clipboard.
    */

  public void copy()
    {
    textArea.copy();
    }

}