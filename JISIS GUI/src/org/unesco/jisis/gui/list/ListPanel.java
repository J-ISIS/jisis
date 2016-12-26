/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui.list;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.collections.CollectionUtils;

/**
 *
 * @author jcd
 */

/*
 * A component with a list that can be filtered, sort, search and display in
 * different way
 */
public class ListPanel extends JPanel
        implements ActionListener, ListSelectionListener {

   class SortableListModel extends DefaultListModel {

      public void sort(Comparator comparator) {
         int numItems = getSize();
         Object[] a = new Object[numItems];
         for (int i = 0; i < numItems; i++) {
            a[i] = getElementAt(i);
         }

         if (comparator == null) {
            Arrays.sort(a);
         } else {
            Arrays.sort(a, comparator);
         }

         clear();
         for (int i = 0; i < a.length; i++) {
            addElement(a[i]);
         }
         fireContentsChanged(this, 0, numItems - 1);
      }
   }
   // All the items in the list
   List dataList = null;
   // Items after filtered 
   Collection filteredData = null;
   // List of Filters
   List filterList = new ArrayList();
   // List of Sorts
   List sortList = new ArrayList();
   SortSpec currentSort = null;
   FilterSpec currentFilter = null;
   Object selectedObj = null;
   SortableListModel displayModel =
           new SortableListModel();
   PropertyChangeSupport pcs =
           new PropertyChangeSupport(this);
   Box mainB = Box.createVerticalBox();
   JPanel sortPane, filterPane, searchPane;
   JComboBox filterCB = new JComboBox();
   JComboBox sortCB = new JComboBox();
   JTextField searchTF = new JTextField(12);
   JButton searchButton = new JButton("Search");
   JLabel totalLbl = new JLabel("<total>");
   JList displayList = new JList();
   DefaultComboBoxModel filterModel =
           new DefaultComboBoxModel();
   DefaultComboBoxModel sortModel =
           new DefaultComboBoxModel();
   JScrollPane js = new JScrollPane();

   public ListPanel() {
      add(createMainPane());
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == filterCB) {
         FilterSpec f = (FilterSpec) filterCB.getSelectedItem();
         currentFilter = f;
         refresh();
      }

      if (e.getSource() == sortCB) {
         SortSpec s = (SortSpec) sortCB.getSelectedItem();
         currentSort = s;
         refresh();
      }

      if ((e.getSource() == searchButton)
              || (e.getSource() == searchTF)) {
         search(currentSort);
      }
   }


   /*
    * Add this object to the list of existing data
    */
   public void add(Object obj) {
      dataList.add(obj);
      refresh();
   }

   public void addFilterSpec(FilterSpec f) {
      filterList.add(f);
   }

   public void addPropertyChangeListener(PropertyChangeListener p) {
      pcs.addPropertyChangeListener(p);
   }

   public void addSortSpec(SortSpec f) {
      sortList.add(f);
   }

   private JPanel createFilterPane() {
      JPanel p = new JPanel();
      p.add(new JLabel("Filter By "));
      p.add(filterCB);
      filterCB.addActionListener(this);
      filterCB.setModel(filterModel);
      return p;
   }

   private JPanel createListPane() {
      JPanel p = new JPanel();
      p.setLayout(new BorderLayout());
      js.setViewportView(displayList);
      js.setPreferredSize(new Dimension(130, 210));
      p.add(js, BorderLayout.NORTH);
      totalLbl.setForeground(java.awt.SystemColor.textInactiveText);
      totalLbl.setFont(new java.awt.Font("dialog", 0, 10));
      p.add(totalLbl, BorderLayout.SOUTH);
      displayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      displayList.addListSelectionListener(this);
      return p;
   }

   private JPanel createMainPane() {
      JPanel p = new JPanel();
      filterPane = createFilterPane();
      sortPane = createSortPane();
      searchPane = createSearchPane();
      mainB.add(filterPane);
      mainB.add(sortPane);
      mainB.add(createListPane());
      mainB.add(searchPane);
      p.add(mainB);
      return p;
   }

   private JPanel createSearchPane() {
      JPanel p2 = new JPanel();
      p2.add(searchTF);
      p2.add(searchButton);
      searchTF.addActionListener(this);
      searchButton.addActionListener(this);
      return p2;
   }

   private JPanel createSortPane() {
      JPanel p = new JPanel();
      p.add(new JLabel("Sort By "));
      p.add(sortCB);
      sortCB.addActionListener(this);
      sortCB.setModel(sortModel);
      return p;
   }

   /*
    * Delete this object from the list of existing data
    */
   public void delete(Object obj) {
      dataList.remove(obj);
      refresh();
   }

   public Object getSelected() {
      return displayList.getSelectedValue();
   }

   private void refresh() {
      // Filter the data
      if (currentFilter == null) {
         filteredData = dataList;
      } else {
         filteredData =
                 CollectionUtils.select(dataList, currentFilter);
      }
      displayModel.clear();
      Iterator i = filteredData.iterator();
      while (i.hasNext()) {
         displayModel.addElement(i.next());
      }
      totalLbl.setText("      total: " + displayModel.size());

      // Sort data        
      displayModel.sort(currentSort);
      if (currentSort != null) {
         displayList.setCellRenderer(currentSort);
      }
      displayList.setModel(displayModel);
   }

   private void refreshCurrent() {
      if (selectedObj != null) {
         displayList.setSelectedValue(selectedObj, true);
      }
      if (displayList.getSelectedValue() == null) {
         displayList.setSelectedIndex(0);
         selectedObj = displayList.getSelectedValue();
      }
   }

   public void refreshGUI() {
      if (filterList.size() == 0) {
         mainB.remove(filterPane);
      }
      Iterator i = filterList.iterator();
      while (i.hasNext()) {
         filterModel.addElement(i.next());
      }
      i = sortList.iterator();
      while (i.hasNext()) {
         sortModel.addElement(i.next());
      }
      if (sortList.size() == 0) {
         mainB.remove(sortPane);
         mainB.remove(searchPane);
      }
   }

   private void search(SortSpec s) {
      s.setSearch(searchTF.getText());
      Object o = CollectionUtils.find(filteredData, s);
      displayList.setSelectedValue(o, true);
   }

   public void setData(java.util.List newData) {
      dataList = newData;
      refresh();
   }

   /*
    * Update this object in the list of existing data
    */
   public void update(Object original, Object newObj) {
      dataList.remove(original);
      dataList.add(newObj);
      refresh();
   }

   public void setSelectedIndex(int i) {
      displayList.setSelectedIndex(i);
   }

   /**
    * Selected object is change, fire property change to let listener know
    */
   public void valueChanged(ListSelectionEvent e) {
      if (!e.getValueIsAdjusting()) {
         Object o = displayList.getSelectedValue();
         pcs.firePropertyChange(ListTabPanel.LIST_SELECTED,
                 selectedObj, o);
         selectedObj = o;
      }
   }
}
