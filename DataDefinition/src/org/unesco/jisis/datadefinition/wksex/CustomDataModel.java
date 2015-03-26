/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.datadefinition.wksex;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jcd
 */
public class CustomDataModel extends AbstractTableModel {
// Constant node types
   public static String FIELD = "0";
   public static String SUBFIELD = "1";
// arrays to hold grid data
   public List listField;
   public List listDisplayField;

   public CustomDataModel() {
      super();
// Create instances of the vector data arrays
      listField = new ArrayList();
      listDisplayField = new ArrayList();
   }

   public int getColumnCount() {
// Return 0 because we handle our own columns
      return 0;
   }

   public int getRowCount() {
      return listDisplayField.size();
   }

   public Object getValueAt(int iRowIndex, int iColumnIndex) {
      if (iRowIndex >= 0 && iRowIndex < listDisplayField.size()) {
// Get the node we are referencing
         MyTableNode node = (MyTableNode) listDisplayField.get(iRowIndex);
         switch (iColumnIndex) {
            case 0:
               return node.tag;
            case 1:
               return node.subfieldCode;
            case 2:
               return node.repeatable;
         }
      }
      return "";
   }

   public void setValueAt(Object aValue, int iRowIndex, int iColumnIndex) {
// Get the node we are referencing
      MyTableNode node = (MyTableNode) listDisplayField.get(iRowIndex);
      switch (iColumnIndex) {
         case 0:
            node.tag = (Integer) aValue;
            break;
         case 1:
            node.subfieldCode = (String) aValue;
            break;
         case 2:
            node.repeatable = (Boolean) aValue;
            break;
      }
// Update the node
      listDisplayField.set(iRowIndex, node);
   }

   public synchronized int GetDisplayNode(int tag, String type) {
      MyTableNode node;
      for (int iCtr = 0; iCtr < listDisplayField.size(); iCtr++) {
         node = (MyTableNode) listDisplayField.get(iCtr);
         if (node.tag == tag
                 && node.type.equals(type)) {
            return iCtr;
         }
      }
      return -1;
   }

   public synchronized int GetShadowNode(int tag, String type) {
      MyTableNode node;
      for (int iCtr = 0; iCtr < listField.size(); iCtr++) {
         node = (MyTableNode) listField.get(iCtr);
         if (node.tag == tag
                 && node.type.equals(type)) {
            return iCtr;
         }
      }
      return -1;
   }
// Expand the specified parent node (FIELD node)
   public void ExpandParent(MyTableNode node) {
// Determine the parent offsets
      int iParent = GetDisplayNode(node.tag, FIELD);
      int iShadowParent = GetShadowNode(node.tag, FIELD);
      MyTableNode shadow = (MyTableNode) listField.get(iShadowParent);
      if (shadow.iChildren > 0) {
// Reinsert the children
         MyTableNode child;
         for (int iCtr = 0; iCtr < shadow.iChildren; iCtr++) {
// Insert items from the shadow parent back into the
// display parent record
            child = (MyTableNode) new MyTableNode((MyTableNode) listField.get(iShadowParent
                    + iCtr + 1), iCtr + 1);
            listDisplayField.set(iParent + iCtr + 1, child);
         }
// Update the parent record
         node.iChildren = shadow.iChildren;
         listDisplayField.set(iParent, node);
      }
   }
// Collapse the specified parent node
   public void CollapseParent(MyTableNode node) {
// Determine the parent offsets
      int iParent = GetDisplayNode(node.tag, FIELD);
// Remove any children
      for (int iCtr = node.iChildren; iCtr > 0; iCtr--) {
         listDisplayField.remove(iParent + iCtr);
      }
// Update the parent record
      node.iChildren = 0;
      listDisplayField.set(iParent, node);
   }
// Insert a new parent node into the tree
   public int InsertParent(int tag, 
                           String subfieldCode,
                           boolean repeatable,
                           String defaultValue,
                           String description,
                           String displayControl,
                           String helpMsg,
                           String pickList,
                           String valFormat) {
// Create a new child record
      MyTableNode node = new MyTableNode();
      node.type = FIELD;
      node.tag = tag;
      node.subfieldCode = subfieldCode;
      node.repeatable = repeatable;
      node.description = description;
      node.displayControl = displayControl;
      node.helpMsg = helpMsg;
      node.pickList = pickList;
      node.valFormat = valFormat;
      listDisplayField.add(node);
// Add data to the shadow data area
      node = new MyTableNode();
      node.type = FIELD;
      node.tag = tag;
      node.subfieldCode = subfieldCode;
      node.repeatable = repeatable;
      node.description = description;
      node.displayControl = displayControl;
      node.helpMsg = helpMsg;
      node.pickList = pickList;
      node.valFormat = valFormat;
      listField.add(node);

      return listDisplayField.size() - 1;
   }
// Insert a new child node into the tree within the
// specified parent node
   public void InsertChild(int iParent, int tag,
                           String subfieldCode,
                           boolean repeatable,
                           String defaultValue,
                           String description,
                           String displayControl,
                           String helpMsg,
                           String pickList,
                           String valFormat) {
// Get the node we are referencing
      MyTableNode parent = (MyTableNode) listDisplayField.get(iParent);
// Create a new child record
      MyTableNode node = (MyTableNode) new MyTableNode();
      node.type = SUBFIELD;
      node.tag = tag;
      node.subfieldCode = subfieldCode;
      node.repeatable = repeatable;
      node.description = description;
      node.displayControl = displayControl;
      node.helpMsg = helpMsg;
      node.pickList = pickList;
      node.valFormat = valFormat;
    
// Set the offset
      parent.iChildren++;
      parent.iActualChildren++;
      node.iParentOffset = parent.iChildren;
// Insert the new node
      if (iParent + parent.iChildren >= listDisplayField.size()) {
         listDisplayField.add(node);
      } else {
         listDisplayField.set(iParent + parent.iChildren, node);
      }
// Update the parent record
      listDisplayField.set(iParent, parent);
// Add data to the shadow data area, too
      node = (MyTableNode) new MyTableNode();
      node.type = SUBFIELD;
      node.tag = tag;
      node.subfieldCode = subfieldCode;
      node.repeatable = repeatable;
      node.description = description;
      node.displayControl = displayControl;
      node.helpMsg = helpMsg;
      node.pickList = pickList;
      node.valFormat = valFormat;
     
      int iShadowParent = GetShadowNode(parent.tag, FIELD);
      parent = (MyTableNode) listField.get(iShadowParent);
      parent.iChildren++;
      parent.iActualChildren++;
      node.iParentOffset = parent.iChildren;
      if (iParent + parent.iChildren >= listField.size()) {
         listField.add(node);
      } else {
         listField.set(iShadowParent+ parent.iChildren, node);
      }
      listField.set(iShadowParent, parent);
   }
}
