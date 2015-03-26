/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dbdisplayfont;


/**
 *  Copyright © 2006, 2007 Roberto Mariottini. All rights reserved.
 *
 *  Permission is granted to anyone to use this software in source and binary forms
 *  for any purpose, with or without modification, including commercial applications,
 *  and to alter it and redistribute it freely, provided that the following conditions
 *  are met:
 *
 *  o  Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  o  The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *  o  Altered source versions must be plainly marked as such, and must not
 *     be misrepresented as being the original software.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 *  OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */



import java.awt.*;
import java.awt.event.*;
import java.awt.font.NumericShaper;
import javax.swing.*;
import javax.swing.event.*;

public final class JFontChooserTest extends JComponent
{
     private static final int[] shapers = { NumericShaper.ARABIC, NumericShaper.BENGALI,
  NumericShaper.DEVANAGARI, NumericShaper.EASTERN_ARABIC, NumericShaper.ETHIOPIC, NumericShaper.EUROPEAN,
  NumericShaper.GUJARATI, NumericShaper.GURMUKHI, NumericShaper.KANNADA, NumericShaper.KHMER, NumericShaper.LAO,
  NumericShaper.MALAYALAM, NumericShaper.MONGOLIAN, NumericShaper.MYANMAR, NumericShaper.ORIYA, NumericShaper.TAMIL,
  NumericShaper.TELUGU, NumericShaper.THAI, NumericShaper.TIBETAN, };
  private static final String[] shaperNames = { "ARABIC", "BENGALI", "DEVANAGARI", "EASTERN_ARABIC", "ETHIOPIC",
  "EUROPEAN", "GUJARATI", "GURMUKHI", "KANNADA", "KHMER", "LAO", "MALAYALAM", "MONGOLIAN", "MYANMAR",
  "ORIYA", "TAMIL", "TELUGU", "THAI", "TIBETAN", };

  final JFontChooser chooser4 = new JFontChooser();

  public JFontChooserTest() {

  }
  public  void showDialog()
  {
    try
    {


      // sample 4
      //final JFontChooser chooser4 = new JFontChooser();
      final char[] numbers = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
      NumericShaper shaper = NumericShaper.getShaper(shapers[5]);
      char[] temp =  numbers.clone();
      shaper.shape(temp, 0, temp.length);
      chooser4.setSampleText(new String(temp));
      final JList shaperList = new JList(shaperNames);
      shaperList.setSelectedIndex(5);
      shaperList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (! e.getValueIsAdjusting()) {
            NumericShaper shaper = NumericShaper.getShaper(shapers[shaperList.getSelectedIndex()]);
            char[] temp =  numbers.clone();
            shaper.shape(temp, 0, temp.length);
            chooser4.setSampleText(new String(temp));
          }
        }
      });
      JButton button = new JButton("Standard Java Fonts");
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
          chooser4.setFontNames(new String[] {"Dialog", "Monospaced", "Sans serif", "Serif"});
        }
      });
      JPanel accessoryPanel = new JPanel(new BorderLayout());
      accessoryPanel.add(new JLabel("Shape:"), BorderLayout.NORTH);
      accessoryPanel.add(new JScrollPane(shaperList), BorderLayout.CENTER);
      accessoryPanel.add(button, BorderLayout.SOUTH);
      chooser4.setAccessory(accessoryPanel);

      chooser4.showDialog(null);



    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  

   public  boolean  succeeded() {
      return chooser4.succeeded();
   }

   public  Font getSelectedFont() {
      return chooser4.getSelectedFont();
   }


}
