/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jcd
 */
public class CustomDataModel extends AbstractTableModel {
// Constant node types
   public static String SERVER = "0";
   public static String RESOURCE = "1";
   // Arrays to hold grid data
   public List listrService;
   public List listDisplayService;

   public CustomDataModel() {
      super();
// Create instances of the  data arrays
      listrService = new ArrayList();
      listDisplayService = new ArrayList();
   }

   public int getColumnCount() {
// Return 0 because we handle our own columns
      return 0;
   }

   public int getRowCount() {
      return listDisplayService.size();
   }

   public Object getValueAt(int iRowIndex, int iColumnIndex) {
      if (iRowIndex >= 0 && iRowIndex < listDisplayService.size()) {
// Get the node we are referencing
         MyTableNode node = (MyTableNode) listDisplayService.get(iRowIndex);
         switch (iColumnIndex) {
            case 0:
               return node.nameString;
            case 1:
               return node.locationString;
            case 2:
               return node.statusString;
         }
      }
      return "";
   }

    @Override
   public void setValueAt(Object aValue,
           int iRowIndex, int iColumnIndex) {
// Get the node we are referencing
      MyTableNode node = (MyTableNode) listDisplayService.get(iRowIndex);
      switch (iColumnIndex) {
         case 0:
            node.nameString = (String) aValue;
            break;
         case 1:
            node.locationString = (String) aValue;
            break;
         case 2:
            node.typeString = (String) aValue;
            break;
      }
// Update the node
      listDisplayService.set(iRowIndex, node);
   }

   public synchronized int GetDisplayNode(
           String string, String type) {
      MyTableNode node;
      for (int iCtr = 0; iCtr < listDisplayService.size(); iCtr++) {
         node = (MyTableNode) listDisplayService.get(iCtr);
         if (node.nameString.equalsIgnoreCase(string)
                 && node.typeString.equals(type)) {
            return iCtr;
         }
      }
      return -1;
   }

   public synchronized int GetShadowNode(
           String string, String type) {
      MyTableNode node;
      for (int iCtr = 0; iCtr < listrService.size(); iCtr++) {
         node = (MyTableNode) listrService.get(iCtr);
         if (node.nameString.equalsIgnoreCase(string)
                 && node.typeString.equals(type)) {
            return iCtr;
         }
      }
      return -1;
   }
// Expand the specified parent node
   public void ExpandParent(MyTableNode node) {
// Determine the parent offsets
      int iParent = GetDisplayNode(node.nameString, SERVER);
      int iShadowParent = GetShadowNode(node.nameString, SERVER);
      MyTableNode shadow = (MyTableNode) listrService.get(iShadowParent);
      if (shadow.iChildren > 0) {
// Reinsert the children
         MyTableNode child;
         for (int iCtr = 0; iCtr < shadow.iChildren; iCtr++) {
// Insert items from the shadow parent back into the
// display parent record
            child = (MyTableNode) new MyTableNode((MyTableNode) listrService.get(iShadowParent
                    + iCtr + 1), iCtr + 1);
            // Insert Element at (iParent + iCtr + 1)
            listDisplayService.add(iParent + iCtr + 1, child);
         }
// Update the parent record
         node.iChildren = shadow.iChildren;
         listDisplayService.set(iParent, node);
      }
   }
// Collapse the specified parent node
   public void CollapseParent(MyTableNode node) {
// Determine the parent offsets
      int iParent = GetDisplayNode(node.nameString, SERVER);
// Remove any children
      for (int iCtr = node.iChildren; iCtr > 0; iCtr--) {
         listDisplayService.remove(iParent + iCtr);
      }
// Update the parent record
      node.iChildren = 0;
      listDisplayService.set(iParent, node);
   }
// Insert a new parent node into the tree
   public int InsertParent(String nameString,
           String locationString, String statusString) {
// Create a new child record
      MyTableNode node = (MyTableNode) new MyTableNode();
      node.typeString = SERVER;
      node.nameString = nameString;
      node.locationString = locationString;
      node.statusString = statusString;
      listDisplayService.add(node);
// Add data to the shadow data area
      node = (MyTableNode) new MyTableNode();
      node.typeString = SERVER;
      node.nameString = nameString;
      node.locationString = locationString;
      node.statusString = statusString;
      listrService.add(node);
      return listDisplayService.size() - 1;
   }
// Insert a new child node into the tree within the
// specified parent node
   public void InsertChild(int iParent, String nameString,
           String locationString, String statusString) {
// Get the node we are referencing
      MyTableNode parent = (MyTableNode) listDisplayService.get(iParent);
// Create a new child record
      MyTableNode node = (MyTableNode) new MyTableNode();
      node.typeString = RESOURCE;
      node.nameString = nameString;
      node.locationString = locationString;
      node.statusString = statusString;
// Set the offset
      parent.iChildren++;
      parent.iActualChildren++;
      node.iParentOffset = parent.iChildren;
// Insert the new node
      if (iParent + parent.iChildren >= listDisplayService.size()) {
         listDisplayService.add(node);
      } else {
         listDisplayService.add(iParent + parent.iChildren, node);
      }
// Update the parent record
      listDisplayService.set(iParent, parent);
// Add data to the shadow data area, too
      node = (MyTableNode) new MyTableNode();
      node.typeString = RESOURCE;
      node.nameString = nameString;
      node.locationString = locationString;
      node.statusString = statusString;
      int iShadowParent = GetShadowNode(parent.nameString, SERVER);
      parent = (MyTableNode) listrService.get(iShadowParent);
      parent.iChildren++;
      parent.iActualChildren++;
      node.iParentOffset = parent.iChildren;
      if (iParent + parent.iChildren >= listrService.size()) {
         listrService.add(node);
      } else {
         listrService.add(iShadowParent+parent.iChildren, node);
      }
      listrService.set(iShadowParent, parent);
   }
}
