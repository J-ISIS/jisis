/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultFocusManager;
import javax.swing.DefaultListModel;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author jcd
 */
public class SelectorDialog extends JDialog
{
  //the Add (>>) button
  private JButton btnAdd;
  //the Remove (<<) button
  private JButton btnRemove;
  //the Remove All button
  private JButton btnRemoveAll;
  //the OK button
  private JButton btnOk;
  //the Cancel button
  private JButton btnCancel;
  //the list containing the user selected files
  private JList list;
  //the default model for the list
  private DefaultListModel listModel =
                            new DefaultListModel();
  //true if user clicked OK; false otherwise
  private boolean isOK;
  //the file chooser component
  private DirectoryServiceFileChooser filechooser =
    DirectoryServiceFileChooser.createDirectoryServiceFileChooser(-1);

  public SelectorDialog(JFrame aFrame,
                               String aTitle,
                               boolean aModalFlag)
  {
    super(aFrame, aTitle, aModalFlag);
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        setOK(false);
        setVisible(false);
      }
    });
    createGUI();
    //don't display OPEN/CANCEL in the file chooser
    this.filechooser.setControlButtonsAreShown(false);
  }

/**
 * Returns the items in the list.
 */
  public File[] getSelectedItems()
  {
    File[] files = new File[listModel.size()];
    for (int i=0; i<listModel.size();i++)
    {
      files[i] = (File)listModel.elementAt(i);
    }
    return files;
  }


/**
 * Creates (and initializes) the gui components.
 */
  private void createGUI()
  {
    //vertical distance between buttons
    Dimension vBtnDist = new Dimension(0, 5);
    //horizontal distance between boxes
    Dimension hBoxDist = new Dimension(15, 0);

    filechooser.setPreferredSize(new Dimension(400,250));

    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
    centerPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

    //create box for Add, Remove, Remove All buttons
    Box buttonBox = Box.createVerticalBox();
    btnAdd = new JButton(">>");
    btnAdd.setToolTipText("Add selected file(s) to the list");
    btnAdd.addActionListener(new AddButtonListener());
    btnRemove = new JButton("<<");
    btnRemove.setToolTipText("Remove selected file(s) from the list");
    btnRemove.addActionListener(new RemoveButtonListener());
    btnRemoveAll = new JButton("<<<");
    btnRemoveAll.setToolTipText("Clear all selections in list");
    btnRemoveAll.addActionListener(new RemoveAllButtonListener());
    Dimension dimBtnRemoveAll = btnRemoveAll.getPreferredSize();
    //set the preferred sizes of the buttons
    Dimension preferredBtnSize = new Dimension(dimBtnRemoveAll);
    btnAdd.setPreferredSize(preferredBtnSize);
    btnRemove.setPreferredSize(preferredBtnSize);
    btnRemoveAll.setPreferredSize(preferredBtnSize);
    btnAdd.setMinimumSize(preferredBtnSize);
    btnRemove.setMinimumSize(preferredBtnSize);
    btnRemoveAll.setMinimumSize(preferredBtnSize);
    btnAdd.setMaximumSize(preferredBtnSize);
    btnRemove.setMaximumSize(preferredBtnSize);
    btnRemoveAll.setMaximumSize(preferredBtnSize);
    //add the buttons to the box
    buttonBox.add(btnAdd);
    buttonBox.add(Box.createRigidArea(vBtnDist));
    buttonBox.add(btnRemove);
    buttonBox.add(Box.createRigidArea(vBtnDist));
    buttonBox.add(btnRemoveAll);
    //add the button box as an accessory to the JFileChooser
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    JPanel cPanel = new JPanel();
    cPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,0));
    cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.Y_AXIS));
    cPanel.add(buttonBox);
    panel.add(cPanel, BorderLayout.CENTER);
    filechooser.setAccessory(panel);

    Box listBox = Box.createVerticalBox();
    listBox.add(Box.createRigidArea(new Dimension(0, 45)));


    //the list
    list = new JList(listModel);
    JScrollPane scrPane = new JScrollPane(list);
    scrPane.setPreferredSize(new Dimension((int)filechooser.getPreferredSize().getWidth() - 200,
                                      (int)filechooser.getPreferredSize().getHeight() - 100));
    scrPane.setMinimumSize(new Dimension((int)filechooser.getMinimumSize().getWidth() - 200,
                                      (int)filechooser.getMinimumSize().getHeight() - 100));
    scrPane.setMaximumSize(new Dimension((int)filechooser.getMaximumSize().getWidth() - 200,
                                      (int)filechooser.getMaximumSize().getHeight() - 100));
    //add list to box
    listBox.add(scrPane);
    listBox.add(Box.createRigidArea(vBtnDist));

    //create box for OK and Cancel buttons
    Box buttonBox2 = Box.createVerticalBox();
    btnOk = new JButton("OK");
    btnOk.setMnemonic('K');
    btnOk.addActionListener(new OkButtonListener());
    btnCancel = new JButton("Cancel");
    btnCancel.setMnemonic('C');
    btnCancel.addActionListener(new CancelButtonListener());
    //set OK button's preferred size based on size of Cancel button
    btnOk.setPreferredSize(btnCancel.getPreferredSize());
    btnOk.setMinimumSize(btnCancel.getMinimumSize());
    btnOk.setMaximumSize(btnCancel.getMaximumSize());
    //add the buttons to the box
    buttonBox2.add(Box.createRigidArea(new Dimension(0,45)));
    buttonBox2.add(btnOk);
    buttonBox2.add(Box.createRigidArea(vBtnDist));
    buttonBox2.add(btnCancel);
    buttonBox2.add(Box.createVerticalGlue());

    //add components to the center panel
    centerPanel.add(filechooser);
    centerPanel.add(Box.createRigidArea(hBoxDist));
    centerPanel.add(listBox);
    centerPanel.add(Box.createRigidArea(hBoxDist));
    centerPanel.add(buttonBox2);

    //The default tab order is left to right and top to bottom; however,
    //the default ordering was incorrect, so I extending the
    //DefaultFocusManager and overrode compareTabOrder() to always return
    //false.  If compareTabOrder() returns false then the focus manager uses
    //the ordering the components were added to the container - the behavior
    //that we want
    FocusManager.setCurrentManager(new DefaultFocusManager()
    {
      public boolean compareTabOrder(Component a, Component b)
      {
        return false;
      }
    });

    this.getContentPane().add(centerPanel, BorderLayout.CENTER);
    this.pack();
  }

/*******************************************************************************
  Determines whether the user chose the dialog's <i>OK</i> button.
  @return true if the user closed the dialog by selecting <i>OK</i>;
          false otherwise
*******************************************************************************/
  public boolean isOK()
  {
    return this.isOK;
  }

/*******************************************************************************
  Sets whether the user choose the dialog's <i>OK</i> button.
  @param ok true if the user closed the dialog by selecting <i>OK</i>;
            false otherwise
*******************************************************************************/
  private void setOK(boolean ok)
  {
    this.isOK = ok;
  }

  //action listener for the Add button
  class AddButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent anEvent)
    {
      File[] selectedFiles = filechooser.getSelectedFiles();
      for (int i=0; i<selectedFiles.length; i++)
      {
        if (!listModel.contains(selectedFiles[i]))
        {
          listModel.addElement(selectedFiles[i]);
        }
      }
    }
  }

  //action listener for the remove button
  class RemoveButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent anEvent)
    {
      int[] arrIndices = list.getSelectedIndices();
      for (int i=arrIndices.length-1; i>=0; --i)
      {
        listModel.removeElementAt(arrIndices[i]);
      }
    }
  }

  //action listener for the Remove All button
  class RemoveAllButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent anEvent)
    {
      listModel.removeAllElements();
    }
  }

  //action listener for the OK button
  class OkButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent anEvent)
    {
      setOK(true);
      setVisible(false);
    }
  }

  //action listener for the Cancel button
  class CancelButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent anEvent)
    {
      setOK(false);
      setVisible(false);
    }
  }
}
