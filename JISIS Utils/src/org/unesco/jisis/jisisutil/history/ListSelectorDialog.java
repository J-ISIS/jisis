/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutil.history;

/**
 *
 * @author jcd
 */
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import org.jdesktop.swingx.JXList;

/**
 * A dialog to present the user with a list of items, that the user can make a
 * selection from, or cancel the selection.
 *
 
 */
public class ListSelectorDialog extends JDialog {

   /**
    * for serialization
    */
   private static final long serialVersionUID = 906147926840288895L;
   /**
    * Click to choose the currently selected property
    */
   protected JButton okButton_ = new JButton(java.util.ResourceBundle.getBundle("org/unesco/jisis/jisisutil/history/Bundle").getString("OK"));
   /**
    * Click to cancel the property selection
    */
   protected JButton cancelButton_ = new JButton(java.util.ResourceBundle.getBundle("org/unesco/jisis/jisisutil/history/Bundle").getString("CANCEL"));
  
   /**
    * The list component
    */
   protected JXList jxList_;
   /**
    * Whether the selection was made or cancelled
    */
   protected int result_;
   /**
    * Signifies an OK property selection
    */
   public static final int APPROVE_OPTION = 0;
   /**
    * Signifies a cancelled property selection
    */
   public static final int CANCEL_OPTION = 1;
   /**
    * The current regular expression.
    */
   protected String patternRegEx_ = ".*";

   /**
    * Create the list selection dialog.
    *
    * @param parentFrame the parent frame of the dialog
    * @param userList the JList component the user will select from
    */
   public ListSelectorDialog(Frame parentFrame, String title, JXList jxList) {

      super(parentFrame, title, true);
      jxList_ = jxList;

      /**
       * Allow selection by double click on a single element or last element
       * of MULTIPLE_INTERVAL_SELECTION
       */
      MouseListener xjListListener = new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent event) {
            
            if (event.getClickCount() == 2) {
                  result_ = APPROVE_OPTION;
                  setVisible(false);              
            }
         }
      };
      jxList_.addMouseListener(xjListListener);

      cancelButton_.setMnemonic(java.util.ResourceBundle.getBundle("org/unesco/jisis/jisisutil/history/Bundle")
                                  .getString("CANCEL_MNEMONIC").charAt(0));
      cancelButton_.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            result_ = CANCEL_OPTION;
            setVisible(false);
         }
      });
      okButton_.setMnemonic(java.util.ResourceBundle.getBundle("org/unesco/jisis/jisisutil/history/Bundle")
                                  .getString("OK_MNEMONIC").charAt(0));
      okButton_.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            result_ = APPROVE_OPTION;
            setVisible(false);
         }
      });
     

      Container c = getContentPane();
      c.setLayout(new BorderLayout());
      //    setBorder(BorderFactory.createTitledBorder("Select a property"));
      Box b1 = new Box(BoxLayout.X_AXIS);
      b1.add(okButton_);
      b1.add(Box.createHorizontalStrut(10));
     
      b1.add(Box.createHorizontalStrut(10));
      b1.add(cancelButton_);
      c.add(b1, BorderLayout.SOUTH);
      c.add(new JScrollPane(jxList_), BorderLayout.CENTER);

      getRootPane().setDefaultButton(okButton_);

      pack();

      // make sure, it's not bigger than the screen
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      int width = getWidth() > screen.getWidth()
              ? (int) screen.getWidth() : getWidth();
      int height = getHeight() > screen.getHeight()
              ? (int) screen.getHeight() : getHeight();
      setSize(800, 600);
      setResizable(true);
   }

   /**
    * Pops up the modal dialog and waits for cancel or a selection.
    *
    * @return either APPROVE_OPTION, or CANCEL_OPTION
    */
   public int showDialog() {

      result_ = CANCEL_OPTION;
      int[] origSelected = jxList_.getSelectedIndices();
      setVisible(true);
      if (result_ == CANCEL_OPTION) {
         jxList_.setSelectedIndices(origSelected);
      }
      return result_;
   }

  
   /**
    * Tests out the list selector from the command line.
    *
    * @param args ignored
    */
   public static void main(String[] args) {

      try {
         DefaultListModel lm = new DefaultListModel();
         lm.addElement("one");
         lm.addElement("two");
         lm.addElement("three");
         lm.addElement("four");
         lm.addElement("five");
         JXList jl = new JXList(lm);
         final ListSelectorDialog jd = new ListSelectorDialog(null,"Select Items", jl);
         int result = jd.showDialog();
         if (result == ListSelectorDialog.APPROVE_OPTION) {
            System.err.println("Fields Selected");
            int[] selected = jl.getSelectedIndices();
            for (int i = 0; i < selected.length; i++) {
               System.err.println("" + selected[i]
                       + " " + lm.elementAt(selected[i]));
            }
         } else {
            System.err.println("Cancelled");
         }
         System.exit(0);
      } catch (Exception ex) {
         ex.printStackTrace();
         System.err.println(ex.getMessage());
      }
   }
}