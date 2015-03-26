/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.io.IOException;
import java.util.List;

import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author jc_dauphin
 */
//Panel with two lists. User can move data back and forth between lists.
class TwoListsPanel extends JPanel {
   public static final int LIST_WIDTH         = 150;
   public static final int LIST_HEIGHT        = 200;
   private boolean         m_selectionChanged = false;
   private MutableList     m_leftList;
   private MutableList     m_rightList;

   public TwoListsPanel(Object[] leftData, String leftTitle, Object[] rightData,
                        String rightTitle) {
      super(new BorderLayout(10, 10));

      setBorder(new EmptyBorder(10, 10, 10, 10));

      m_leftList = new MutableList(leftData);

      m_leftList.setCellRenderer(new LocaleListRenderer());

      JScrollPane spl = new JScrollPane(m_leftList);
      JPanel      p2l = new JPanel(new BorderLayout());

      p2l.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
      p2l.add(spl, BorderLayout.CENTER);
      p2l.add(new JLabel(leftTitle), BorderLayout.NORTH);
      add(p2l, BorderLayout.WEST);

      m_rightList = new MutableList(rightData);

      m_rightList.setCellRenderer(new LocaleListRenderer());

      JScrollPane spr = new JScrollPane(m_rightList);
      JPanel      p2r = new JPanel(new BorderLayout());

      p2r.setPreferredSize(new Dimension(LIST_WIDTH, LIST_HEIGHT));
      p2r.add(spr, BorderLayout.CENTER);
      p2r.add(new JLabel(rightTitle), BorderLayout.NORTH);
      add(p2r, BorderLayout.EAST);

      JPanel p2c = new JPanel();

      p2c.setLayout(new BoxLayout(p2c, BoxLayout.Y_AXIS));
      p2c.add(Box.createVerticalGlue());

      JButton btnToRight = new JButton(">>");

      btnToRight.setRequestFocusEnabled(false);
      btnToRight.addActionListener(new LeftToRightMover());
      p2c.add(btnToRight);
      p2c.add(Box.createVerticalStrut(10));

      JButton btnToLeft = new JButton("<<");

      btnToLeft.setRequestFocusEnabled(false);
      btnToLeft.addActionListener(new RightToLeftMover());
      p2c.add(btnToLeft);
      p2c.add(Box.createVerticalGlue());
      add(p2c, BorderLayout.CENTER);
   }

   public boolean selectionChanged() {
      return m_selectionChanged;
   }

   public void moveFromLeftToRight(Object obj) {
      if (obj == null) {
         return;
      }

      m_leftList.removeElement(obj);
      m_rightList.addElement(obj);
   }

   public void moveFromRightToLeft(Object obj) {
      if (obj == null) {
         return;
      }

      m_rightList.removeElement(obj);
      m_leftList.addElement(obj);
   }

   class LeftToRightMover implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
         Object[] values = m_leftList.getSelectedValues();

         for (int k = 0; k < values.length; k++) {
            m_leftList.removeElement(values[k]);
            m_rightList.addElement(values[k]);

            m_selectionChanged = true;
         }

         m_leftList.repaint();
         m_rightList.repaint();
      }
   }


   class RightToLeftMover implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
         Object[] values = m_rightList.getSelectedValues();

         for (int k = 0; k < values.length; k++) {
            m_rightList.removeElement(values[k]);
            m_leftList.addElement(values[k]);

            m_selectionChanged = true;
         }

         m_leftList.repaint();
         m_rightList.repaint();
      }
   }


   class LocaleListRenderer extends DefaultListCellRenderer {
      public Component getListCellRendererComponent(JList list, Object value, int index,
              boolean isSelected, boolean cellHasFocus) {
         if (value instanceof Locale) {
            value = ((Locale) value).getDisplayName();
         }

         return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
   }
}


class MutableList extends JList {
   private DefaultListModel m_model;

   public MutableList() {
      m_model = new DefaultListModel();

      setModel(m_model);
      installDnD();
   }

   public MutableList(Object[] arr) {
      m_model = new DefaultListModel();

      for (int k = 0; k < arr.length; k++) {
         m_model.addElement(arr[k]);
      }

      setModel(m_model);
      installDnD();
   }

   public MutableList(List<Object> v) {
      m_model = new DefaultListModel();

      for (int k = 0; k < v.size(); k++) {
         m_model.addElement(v.get(k));
      }

      setModel(m_model);
      installDnD();
   }

   public void addElement(Object obj) {
      m_model.addElement(obj);
      repaint();
   }

   public void removeElement(Object obj) {
      m_model.removeElement(obj);
      repaint();
   }

   public Object[] getData() {
      return m_model.toArray();
   }

   protected void installDnD() {
      setDragEnabled(true);
      setTransferHandler(new ListTransferHandler());

      DnDStarter starter = new DnDStarter();

      addMouseListener(starter);
      addMouseMotionListener(starter);
   }

   class DnDStarter extends MouseInputAdapter {
      public void mousePressed(MouseEvent e) {
         TransferHandler th = MutableList.this.getTransferHandler();

         th.exportAsDrag(MutableList.this, e, TransferHandler.MOVE);
      }
   }
}


class ArrayTransfer implements Transferable {
   public static DataFlavor FLAVOUR;

   static {
      try {
         FLAVOUR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   protected JComponent m_source;    // It is very important to know your source and block transfer to the same component
   protected Object[] m_arr;

   public ArrayTransfer(JComponent source, Object[] arr) {
      m_source = source;
      m_arr    = arr;
   }

   public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      if (!isDataFlavorSupported(flavor)) {
         throw new UnsupportedFlavorException(flavor);
      }

      return this;
   }

   public boolean isDataFlavorSupported(DataFlavor flavor) {
      return FLAVOUR.equals(flavor);
   }

   public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { FLAVOUR };
   }

   public JComponent getSource() {
      return m_source;
   }

   public Object[] getData() {
      return m_arr;
   }
}


class ListTransferHandler extends TransferHandler {
   public boolean importData(JComponent c, Transferable t) {
      if (!(c instanceof MutableList)) {
         return false;
      }

      MutableList list = (MutableList) c;

      try {
         Object obj = t.getTransferData(ArrayTransfer.FLAVOUR);

         if (!(obj instanceof ArrayTransfer)) {
            return false;
         }

         ArrayTransfer at = (ArrayTransfer) obj;

         if (c.equals(at.getSource())) {    // Can't transfer to itself
            return false;
         }

         Object[] arr = at.getData();

         for (int k = 0; k < arr.length; k++) {
            list.addElement(arr[k]);
         }
      } catch (Exception ex) {
         ex.printStackTrace();

         return false;
      }

      return true;
   }

   public boolean canImport(JComponent c, DataFlavor[] transferFlavors) {
      if (!(c instanceof MutableList)) {
         return false;
      }

      for (int k = 0; k < transferFlavors.length; k++) {
         if (transferFlavors[k].equals(ArrayTransfer.FLAVOUR)) {
            return true;
         }
      }

      return false;
   }

   public int getSourceActions(JComponent c) {
      if (!(c instanceof MutableList)) {
         return NONE;
      }

      return COPY_OR_MOVE;
   }

   protected Transferable createTransferable(JComponent c) {
      if (!(c instanceof MutableList)) {
         return null;
      }

      Object[] arr = ((JList) c).getSelectedValues();

      return new ArrayTransfer(c, arr);
   }

   protected void exportDone(JComponent source, Transferable t, int action) {
      if (!(source instanceof MutableList)) {
         return;
      }

      MutableList list = (MutableList) source;

      if (!((action == COPY_OR_MOVE) || (action == MOVE))) {
         return;
      }

      try {
         Object obj = t.getTransferData(ArrayTransfer.FLAVOUR);

         if (!(obj instanceof ArrayTransfer)) {
            return;
         }

         ArrayTransfer at = (ArrayTransfer) obj;

         if (!source.equals(at.getSource())) {
            return;
         }

         Object[] arr = at.getData();

         for (int k = 0; k < arr.length; k++) {
            list.removeElement(arr[k]);
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }
}
