/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dbdisplayfont;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A dialog box that lets the user choose a font.
 * <p>An average choose font dialog is like this:</p>
 * <img src="doc-files/font_pre.png">
 * <p>If you want you can hide the sample text area:</p>
 * <img src="doc-files/font.png">
 * <p>It can also show a custom accessory component, with wich you can control
 *     the functioning of the dialog box:</p>
 * <img src="doc-files/font_access.png">
 * <p>In this case the accessory component is a list that changes the sample text.</p>
 * <P><DL>
 * <DT><B>License:</B></DT>
 * <DD><pre>
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
 * <pre></DD></DL>
 *
 * @version 1.0
 * @author Roberto Mariottini
 */
public final class JFontChooser extends JComponent {

   /** <code>showDialog</code> returns this value if the user has approved (chosen) a font.
    *  @see #showDialog */
   public static final int APPROVE_OPTION = 0;
   /** <code>showDialog</code> returns this value if the user has not chosen a font.
    *  @see #showDialog */
   public static final int CANCEL_OPTION = 1;
   /** Action command used to notify that the user has approved (chosen) a font.
    *  @see #addActionListener */
   public static final String APPROVE_SELECTION = "ApproveSelection";
   /** Action command used to notify that the user has not chosen a font.
    *  @see #addActionListener */
   public static final String CANCEL_SELECTION = "CancelSelection";
   /**
    * Name of the property used to notify that the user has changed the selected font name in the name list.
    * This property is of type <code>String</code>.
    * @see #addPropertyChangeListener
    */
   public static final String FONT_NAME_CHANGED_PROPERTY = "FontNameChangedProperty";
   /**
    * Name of the property used to notify that the user has changed the selected font style in the style list.
    * This property is of type <code>Integer</code>, and can assume the following values:
    * <code>Font.PLAIN</code>, <code>Font.BOLD</code>, <code>Font.ITALIC</code>, <code>Font.BOLD | Font.ITALIC</code>.
    * @see #addPropertyChangeListener
    */
   public static final String FONT_STYLE_CHANGED_PROPERTY = "FontStyleChangedProperty";
   /**
    * Name of the property used to notify that the user has changed the selected font size in the size list or
    * in the size text field. This property is of type <code>Integer</code>.
    * @see #addPropertyChangeListener */
   public static final String FONT_SIZE_CHANGED_PROPERTY = "FontSizeChangedProperty";

   protected boolean succeeded_ = false;

   /**
    * Construct a font chooser with the specified fonts, optionally showing a sample to the user.
    * The sample text is a default text, you can change it by calling <code>setSampleText</code>.
    * @param fontNames the font family names to show to the user.
    * @param showSample true to show a sample of the selected font to the user.
    * @see #setSampleText
    */
   public JFontChooser(String[] fontNames, boolean showSample) {
      setLayout(new BorderLayout());

      JPanel centerPanel = new JPanel(new BorderLayout());

      // Uncomment one of the two lines below to use a standard layout manager
      // instead of my MeshLayout manager. The result is ugly.
      //JPanel listsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));/*
      JPanel listsPanel = new JPanel(new GridLayout(0, 3));/*
      //    net.mariottini.layout.MeshLayout mesh = new net.mariottini.layout.MeshLayout(0, 3, 0);
      //    mesh.setExpandColumn(0);
      //    JPanel listsPanel = new JPanel(mesh);//*/

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      panel.add(new JLabel("Family name:"), BorderLayout.NORTH);
      fontList = new JList(fontNames);
      fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      fontList.setVisibleRowCount(7);
      fontList.setSelectedIndex(0);
      panel.add(new JScrollPane(fontList), BorderLayout.CENTER);
      listsPanel.add(panel);

      panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));
      panel.add(new JLabel("Style:"), BorderLayout.NORTH);
      styleList = new JList(STYLE_NAMES);
      styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      styleList.setVisibleRowCount(7);
      styleList.setSelectedIndex(0);
      panel.add(new JScrollPane(styleList), BorderLayout.CENTER);
      listsPanel.add(panel);

      panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));
      panel.add(new JLabel("Size:"), BorderLayout.NORTH);
      JPanel sizePanel = new JPanel(new BorderLayout());
      sizeText = new JTextField(SIZES[0].toString(), 4);
      sizePanel.add(sizeText, BorderLayout.NORTH);
      sizeList = new JList(SIZES);
      sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      sizeList.setVisibleRowCount(6);
      sizePanel.add(new JScrollPane(sizeList), BorderLayout.CENTER);
      panel.add(sizePanel, BorderLayout.CENTER);
      listsPanel.add(panel);

      centerPanel.add(listsPanel, BorderLayout.NORTH);

      samplePanel = new JPanel(new BorderLayout());
      samplePanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));
      samplePanel.add(new JLabel("Sample:"), BorderLayout.NORTH);
      sampleLabel = new JLabel(DEFAULT_SAMPLE_TEXT, JLabel.CENTER);
      sampleLabel.setMinimumSize(new Dimension(64, 48));
      sampleLabel.setOpaque(true);
      sampleLabel.setBackground(sizeList.getBackground());
      sampleLabel.setBorder(sizeText.getBorder());
      samplePanel.add(sampleLabel, BorderLayout.CENTER);
      samplePanel.setVisible(showSample);
      centerPanel.add(samplePanel, BorderLayout.CENTER);

      add(centerPanel, BorderLayout.CENTER);

      accessoryPanel = new JPanel(new BorderLayout());
      accessoryPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 8));
      accessoryComponent = new JLabel("Accessory");
      accessoryComponent.setOpaque(true);
      accessoryComponent.setBackground(sizeList.getBackground());
      accessoryPanel.add(accessoryComponent, BorderLayout.CENTER);
      accessoryPanel.setVisible(false);
      add(accessoryPanel, BorderLayout.EAST);

      JPanel southPanel = new JPanel(new BorderLayout());
      southPanel.add(new JLabel(), BorderLayout.CENTER);
      JPanel buttonsPanel = new JPanel();
      ActionListener actionListener = new ButtonActionListener();
      JButton button = new JButton("OK");
      button.addActionListener(actionListener);
      button.setActionCommand(APPROVE_SELECTION);
      buttonsPanel.add(button);
      button = new JButton("Cancel");
      button.addActionListener(actionListener);
      button.setActionCommand(CANCEL_SELECTION);
      buttonsPanel.add(button);
      southPanel.add(buttonsPanel, BorderLayout.EAST);
      add(southPanel, BorderLayout.SOUTH);

      //* Fix list size (optional)
      Dimension d = fontList.getPreferredSize();
      d.width += 6;
      fontList.setPreferredSize(d);
      d = styleList.getPreferredSize();
      d.width += 6;
      styleList.setPreferredSize(d);
      d = sizeList.getPreferredSize();
      d.width += 6;
      sizeList.setPreferredSize(d);
      //*/

      // Fix sample size
      Dimension pref = sampleLabel.getPreferredSize();
      Dimension min = sampleLabel.getMinimumSize();
      pref.width += 16;
      pref.height += 12;
      if (pref.width < min.width) {
         pref.width = min.width;
      }
      if (pref.height < min.height) {
         pref.height = min.height;
      }
      sampleLabel.setPreferredSize(pref);

      // set listener
      SampleListener pl = new SampleListener();
      fontList.addListSelectionListener(pl);
      styleList.addListSelectionListener(pl);
      sizeList.addListSelectionListener(pl);
      sizeList.addListSelectionListener(new SizeListListener());
      sizeText.getDocument().addDocumentListener(new SizeTextListener());
      sizeText.addFocusListener(new SizeTextFocusListener());
      sizeList.setSelectedIndex(5);
   }

   /**
    * Construct a font chooser with the specified fonts, showing a sample to the user.
    * The sample text is a default text, you can change it by calling <code>setSampleText</code>.
    * @param fontNames the font family names to show to the user.
    * @see #setSampleText
    */
   public JFontChooser(String[] fontNames) {
      this(fontNames, true);
   }
// succeeded_ = true;
//       dispose();
//    }

    public boolean succeeded() {
      return succeeded_;
   }
   /**
    * Construct a font chooser with all the available fonts, optionally showing a sample to the user.
    * The sample text is a default text, you can change it by calling <code>setSampleText</code>.
    * The font list is acquired by calling GraphicsEnvironment.getAvailableFontFamilyNames().
    * @param showSample true to show a sample of the selected font to the user.
    * @see #setSampleText
    */
   public JFontChooser(boolean showSample) {
      this(genv.getAvailableFontFamilyNames(), showSample);
   }

   /**
    * Construct a font chooser with all the available fonts, showing a sample to the user.
    * The sample text is a default text, you can change it by calling <code>setSampleText</code>.
    * The font list is acquired by calling GraphicsEnvironment.getAvailableFontFamilyNames().
    * @see #setSampleText
    */
   public JFontChooser() {
      this(genv.getAvailableFontFamilyNames(), true);
   }

   /**
    * change the font names to show in the font list.
    * @param fontNames the font family names to show to the user.
    */
   public void setFontNames(String[] fontNames) {
      fontList.setListData(fontNames);
      fontList.setSelectedIndex(0);
      fontList.ensureIndexIsVisible(0);
   }

   /**
    * Set the string to use as a sample for the fonts. Pass <code>null</code> to
    * use the currently selected font family name as sample.
    * @param text the sample string, or <code>null</code> to use the font family name as sample.
    */
   public void setSampleText(String text) {
      sampleText = text;
      if (sampleText == null) {
         sampleLabel.setText(fontList.getSelectedValue().toString());
         return;
      }
      sampleLabel.setText(sampleText);

      // Fix sample size
      sampleLabel.setPreferredSize(null);
      Dimension pref = sampleLabel.getPreferredSize();
      pref.width += 16;
      pref.height += 12;
      Dimension min = sampleLabel.getMinimumSize();
      if (pref.width < min.width) {
         pref.width = min.width;
      }
      if (pref.height < min.height) {
         pref.height = min.height;
      }
      sampleLabel.setPreferredSize(pref);
   }

   /**
    * Shows/hides the sample panel.
    * @param visible true to show it, false to hide it.
    */
   public void setSampleVisible(boolean visible) {
      samplePanel.setVisible(visible);
   }

   /**
    * Returns the sample panel visibility status.
    * @return true if it's visible, false if it's hidden.
    */
   public boolean isSampleVisible() {
      return samplePanel.isVisible();
   }

   /**
    * Returns the component used to show the font sample. This function is provided
    * to customize the appearance of the component, in particular foreground and
    * background colors. There's no way to change this component, to customize deeper
    * the sample panel hide it and use the accessory component instead.
    * @return the sample component.
    * @see #setAccessory
    */
   public JComponent getSampleComponent() {
      return sampleLabel;
   }

   /**
    * Returns the accessory component.
    * @return the accessory component.
    * @see #setAccessory
    */
   public JComponent getAccessory() {
      return accessoryComponent;
   }

   /**
    * Sets the accessory component. The accessory component is used to customize
    * the dialog. You can use a single component or a container with many
    * components of your choice to extend the functionality of the dialog.
    * Register the appropriate listeners to react to user input.
    * The accessory component will become part of the dialog, and will be disposed
    * along with it: remove it before disposal if you want to preserve it.
    * @param newAccessory the accessory component to use, or <code>null</code> to
    *        hide and remove any accessory component previously set.
    * @see #addActionListener
    * @see #addPropertyChangeListener
    * @see #FONT_NAME_CHANGED_PROPERTY
    * @see #FONT_STYLE_CHANGED_PROPERTY
    * @see #FONT_SIZE_CHANGED_PROPERTY
    */
   public void setAccessory(JComponent newAccessory) {
      accessoryPanel.removeAll();
      accessoryComponent = newAccessory;
      if (accessoryComponent == null) {
         accessoryPanel.setVisible(false);
         return;
      }
      accessoryPanel.add(accessoryComponent, BorderLayout.CENTER);
      accessoryPanel.setVisible(true);
   }

   /**
    * Adds an action listener to this component. The <code>actionPerformed</code>
    * method will be called when the user press one of the two buttons "OK" and "Cancel".
    * The two different action command used are <code>APPROVE_SELECTION</code> and <code>CANCEL_SELECTION</code>.
    * @param listener the listener to add to the listeners list.
    * @see #APPROVE_SELECTION
    * @see #CANCEL_SELECTION
    */
   public void addActionListener(ActionListener listener) {
      actionListeners.add(listener);
   }

   /**
    * Remove an action listener from this component.
    * @param listener the listener to remove from the list.
    * @see #addActionListener
    */
   public void removeActionListener(ActionListener listener) {
      actionListeners.remove(listener);
   }

   /**
    * Called when the user approves a font. Call this method to close the dialog
    * and approve the currently selected font.
    */
   public void approveSelection() {
      ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, APPROVE_SELECTION);
      dispatchActionEvent(event);
   }

   /**
    * Called when the user cancels the operation. Call this method to close the
    * dialog without selecting a font.
    */
   public void cancelSelection() {
      ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, CANCEL_SELECTION);
      dispatchActionEvent(event);
   }

   /**
    * Returns the currently selected font. The Font object returned comprehends
    * the style and size chosen by the user.
    * @return the currently selected font.
    */
   public Font getSelectedFont() {
      return sampleLabel.getFont();
   }

   /**
    * Sets the currently selected font. The dialog will try to change the listboxes
    * selections according to the font set.
    * @param font the font to select.
    */
   public void setSelectedFont(Font font) {
      fontList.setSelectedValue(font.getFamily(), true);
      styleList.setSelectedIndex(font.getStyle());
      int size = font.getSize();
      int index = Arrays.binarySearch(SIZES, new Integer(size));
      if (index >= 0) {
         sizeList.setSelectedIndex(index);
         sizeList.ensureIndexIsVisible(index);
      } else {
         sizeText.setText(String.valueOf(size));
      }
   }

   /**
    * Show a modal "Choose Font" dialog.
    * @param parent the parent component, or null to use a default root frame as parent.
    * @return <code>APPROVE_OPTION</code> if the user chose a font, <code>CANCEL_OPTION</code>
    *         if the user canceled the operation.
    * @see #APPROVE_OPTION
    * @see #CANCEL_OPTION
    */
   public int showDialog(Component parent) {
      return showDialog(parent, "Choose Font");
   }

   /**
    * Show a modal "Choose Font" dialog with the specified title.
    * @param parent the parent component, or null to use a default root frame as parent.
    * @param title the title for the dialog.
    * @return <code>APPROVE_OPTION</code> if the user chose a font, <code>CANCEL_OPTION</code>
    *         if the user canceled the operation.
    * @see #APPROVE_OPTION
    * @see #CANCEL_OPTION
    */
   public int showDialog(Component parent, String title) {
      return showDialog(parent, title, true);
   }

   /**
    * Show a "Choose Font" dialog with the specified title and modality.
    * @param parent the parent component, or null to use a default root frame as parent.
    * @param title the title for the dialog.
    * @param modal true to show a modal dialog, false to show a non-modal dialog (in this case the
    *        function will return immediately after making visible the dialog).
    * @return <code>APPROVE_OPTION</code> if the user chose a font, <code>CANCEL_OPTION</code>
    *         if the user canceled the operation. <code>CANCEL_OPTION</code> is
    *         always returned for a non-modal dialog, use an ActionListener to
    *         be notified when the user approves/cancels the dialog.
    * @see #APPROVE_OPTION
    * @see #CANCEL_OPTION
    * @see #addActionListener
    */
   public int showDialog(Component parent, String title, boolean modal) {
      final int[] result = new int[]{CANCEL_OPTION};
      while (parent != null && !(parent instanceof Window)) {
         parent = parent.getParent();
      }
      final JDialog d;
      if (parent instanceof Frame) {
         d = new JDialog((Frame) parent, title, modal);
      } else if (parent instanceof Dialog) {
         d = new JDialog((Dialog) parent, title, modal);
      } else {
         d = new JDialog();
         d.setTitle(title);
         d.setModal(modal);
      }
      final ActionListener[] listener = new ActionListener[1];
      listener[0] = new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(APPROVE_SELECTION)) {
               result[0] = APPROVE_OPTION;
               succeeded_ = true;
            }
            removeActionListener(listener[0]);
            d.setContentPane(new JPanel());
            d.setVisible(false);
            d.dispose();
         }
      };
      addActionListener(listener[0]);
      d.setComponentOrientation(getComponentOrientation());
      d.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
      d.getContentPane().add(this, BorderLayout.CENTER);
      d.pack();
      d.setLocationRelativeTo(parent);
      d.setVisible(true);
      return result[0];
   }
   private static final GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
   private static final String DEFAULT_SAMPLE_TEXT = "AaBbCcDdEe123456";
   private static final String[] STYLE_NAMES = {"Plain", "Bold", "Italic", "Bold italic"};
   private static final int[] STYLE_VALUES = {Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC};
   private static final Integer[] SIZES = {new Integer(8), new Integer(9), new Integer(10), new Integer(11), new Integer(12), new Integer(14), new Integer(16), new Integer(18), new Integer(20), new Integer(22), new Integer(24), new Integer(26), new Integer(28), new Integer(36), new Integer(48), new Integer(72)};
   private java.util.List actionListeners = new ArrayList();
   private JList fontList;
   private JList styleList;
   private JList sizeList;
   private JTextField sizeText;
   private JPanel samplePanel;
   private JLabel sampleLabel;
   private String sampleText = DEFAULT_SAMPLE_TEXT;
   private JPanel accessoryPanel;
   private JComponent accessoryComponent;

   private void dispatchActionEvent(ActionEvent event) {
      for (int i = 0; i < actionListeners.size(); ++i) {
         ((ActionListener) actionListeners.get(i)).actionPerformed(event);
      }
   }

   private final class ButtonActionListener implements ActionListener {

      public void actionPerformed(ActionEvent e) {
         if (e.getActionCommand().equals(APPROVE_SELECTION)) {
            approveSelection();
         } else {
            cancelSelection();
         }
      }
   }

   private final class SizeListListener implements ListSelectionListener {

      public void valueChanged(ListSelectionEvent e) {
         if (!e.getValueIsAdjusting()) {
            String val = sizeList.getSelectedValue().toString();
            if (!sizeText.getText().equals(val)) {
               sizeText.setText(val);
            }
         }
      }
   }

   private final class SizeTextFocusListener extends FocusAdapter {

      public void focusLost(FocusEvent e) {
         try {
            Integer val = new Integer(sizeText.getText());
         } catch (NumberFormatException nfe) {
            sizeText.setText(sizeList.getSelectedValue().toString());
         }
      }
   }

   private final class SizeTextListener implements DocumentListener {

      public void changedUpdate(DocumentEvent e) {
         updateList();
      }

      public void insertUpdate(DocumentEvent e) {
         updateList();
      }

      public void removeUpdate(DocumentEvent e) {
         updateList();
      }

      private void updateList() {
         try {
            Integer val = new Integer(sizeText.getText());
            sizeList.setSelectedValue(val, true);
            updateSample();
         } catch (NumberFormatException nfe) {
         }
      }
   }

   private final class SampleListener implements ListSelectionListener {

      public void valueChanged(ListSelectionEvent e) {
         if (!e.getValueIsAdjusting()) {
            updateSample();
         }
      }
   }
   private String oldFamily = null;
   private Integer oldSize = null;
   private int oldStyle = -1;

   private void updateSample() {
      String familyName = (String) fontList.getSelectedValue();
      if (familyName == null) {
         return;
      }

      Integer size = null;
      try {
         size = new Integer(sizeText.getText());
      } catch (Exception nfe) {
         size = (Integer) sizeList.getSelectedValue();
      }

      int style = STYLE_VALUES[styleList.getSelectedIndex()];

      if (!familyName.equals(oldFamily) || !size.equals(oldSize) || style != oldStyle) {
         if (sampleText == null && !familyName.equals(oldFamily)) {
            sampleLabel.setText(familyName);
         }

         if (!familyName.equals(oldFamily)) {
            notifyPropertyChange(FONT_NAME_CHANGED_PROPERTY, oldFamily, familyName);
         }
         if (!size.equals(oldSize)) {
            notifyPropertyChange(FONT_SIZE_CHANGED_PROPERTY, oldSize, size);
         }
         if (style != oldStyle) {
            notifyPropertyChange(FONT_STYLE_CHANGED_PROPERTY, new Integer(oldStyle), new Integer(style));
         }

         sampleLabel.setFont(new Font(familyName, style, size.intValue()));

         oldFamily = familyName;
         oldSize = size;
         oldStyle = style;
      }
   }

   private void notifyPropertyChange(String property, Object oldValue, Object newValue) {
      PropertyChangeEvent evt = new PropertyChangeEvent(this, property, oldValue, newValue);
      PropertyChangeListener[] listeners = getPropertyChangeListeners();
      for (int i = 0; i < listeners.length; ++i) {
         listeners[i].propertyChange(evt);
      }
   }
}
