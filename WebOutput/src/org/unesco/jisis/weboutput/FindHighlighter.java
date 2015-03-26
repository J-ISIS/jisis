/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.weboutput;

import java.awt.Color;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 *
 * @author jcd
 */
public class FindHighlighter {
   // A private subclass of the default highlight painter
   class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

      public MyHighlightPainter(Color color) {
         super(color);
      }
   }
   // An instance of the private subclass of the default highlight painter
   Highlighter.HighlightPainter myHighlightPainter = new MyHighlightPainter(Color.red);

   // Creates highlights around all occurrences of pattern in textComp
   public void highlight(JTextComponent textComp, String pattern) {
      // First remove all old highlights
      removeHighlights(textComp);

      try {
         Highlighter hilite = textComp.getHighlighter();
         Document doc = textComp.getDocument();
         String text = doc.getText(0, doc.getLength());
         int pos = 0;

         // Search for pattern
         // see I have updated now its not case sensitive 
         while ((pos = text.toUpperCase().indexOf(pattern.toUpperCase(), pos)) >= 0) {
            // Create highlighter using private painter and apply around pattern
            hilite.addHighlight(pos, pos + pattern.length(), myHighlightPainter);
            pos += pattern.length();
         }
      } catch (BadLocationException e) {
      }
   }
   // Removes only our private highlights

   public void removeHighlights(JTextComponent textComp) {
      Highlighter hilite = textComp.getHighlighter();
      Highlighter.Highlight[] hilites = hilite.getHighlights();
      for (int i = 0; i < hilites.length; i++) {
         if (hilites[i].getPainter() instanceof MyHighlightPainter) {
            hilite.removeHighlight(hilites[i]);
         }
      }

   }
}
