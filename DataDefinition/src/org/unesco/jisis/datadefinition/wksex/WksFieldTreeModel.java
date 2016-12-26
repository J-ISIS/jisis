/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.datadefinition.wksex;

import java.util.Properties;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.WorksheetDef;

/**
 *
 * @author jcd
 */
public class WksFieldTreeModel extends AbstractTreeModel {

   WksTreeNode rootNode;

   public WksFieldTreeModel(String worksheetName, WorksheetDef wks, FieldDefinitionTable fdt) {
      // Create the Tree Table root
      Properties nameMap = new Properties();
      nameMap.setProperty("nodeType", "rootNode");
      nameMap.setProperty("fieldType", "");
      nameMap.setProperty("indicators", "");
      nameMap.setProperty("repeatable", "");
      nameMap.setProperty("firstSubfield", "");
      nameMap.setProperty("defaultValue", "");
      nameMap.setProperty("description", "");
      nameMap.setProperty("displayControl", "");
      nameMap.setProperty("size", "");
      nameMap.setProperty("helpMsg", "");
      nameMap.setProperty("pickList", "");
      nameMap.setProperty("valFormat", "");

      rootNode = new WksTreeNode(worksheetName);
      rootNode.setUserObject(nameMap);

      for (int i = 0; i < wks.getFieldsCount(); i++) {
         WorksheetDef.WorksheetField wksField = wks.getFieldByIndex(i);
         /**
          * Check if the wks field has not been deleted from the FDT
          */
         int found = fdt.findField(wksField.getTag());
         if (found == -1) {
             continue;
         }
         FieldDefinition fdtEntry = fdt.getFieldByTag(wksField.getTag());
         WksTreeNode node = fillModelFromWksField(wksField, fdtEntry);
         rootNode.add(node);
      }

   }

   public WksTreeNode fillModelFromField(WorksheetDef.WorksheetField wksField, FieldDefinitionTable.FieldDefinition fdtEntry) {

      Properties nameMap = new Properties();
      nameMap.setProperty("nodeType", "fieldNode");
      nameMap.setProperty("tag", "" + wksField.getTag());
      nameMap.setProperty("subfieldCode", "");
      nameMap.setProperty("description", wksField.getDescription());
      nameMap.setProperty("fieldType", Global.fiedType(fdtEntry.getType()));
      nameMap.setProperty("indicators", (fdtEntry.hasIndicators()) ? "true" : "false" );
      nameMap.setProperty("repeatable", (fdtEntry.isRepeatable()) ? "true" : "false" );
      nameMap.setProperty("firstSubfield", (fdtEntry.hasFirstSubfield()) ? "true" : "false" );
      nameMap.setProperty("defaultValue", wksField.getDefaultValue());
      
      nameMap.setProperty("displayControl", wksField.getDisplayControl());
      nameMap.setProperty("size", wksField.getSize());
      nameMap.setProperty("helpMsg", wksField.getHelpMessage());
      nameMap.setProperty("pickList", wksField.getPickList());
      nameMap.setProperty("valFormat", wksField.getValidationFormat());
      //creating a node for this element
      WksTreeNode nodeRow = new WksTreeNode("tag "+wksField.getTag());
      nodeRow.setUserObject(nameMap);

      if (fdtEntry.hasIndicators()) {
         WksTreeNode subNodeRow = new WksTreeNode("ind1");
         Properties data = new Properties(nameMap);
         data.setProperty("nodeType", "indicatorNode");
         data.setProperty("subfieldCode", "$ind1");
         data.setProperty("description", "Indicator 1");
         data.setProperty("indicators", "false" );
         data.setProperty("repeatable", "false" );
         data.setProperty("firstSubfield", "false");
    
         data.setProperty("defaultValue", "");
         data.setProperty("displayControl", "");
         data.setProperty("size", "");
         data.setProperty("helpMsg", "");
         data.setProperty("pickList", "");
         data.setProperty("valFormat", "");
         
         subNodeRow.setUserObject(data);
         nodeRow.add(subNodeRow);

         subNodeRow = new WksTreeNode("ind2");
         data = new Properties(nameMap);
         data.setProperty("nodeType", "indicatorNode");
         data.setProperty("subfieldCode", "$ind2");
         data.setProperty("description", "Indicator 2");
         data.setProperty("indicators", "false" );
         data.setProperty("repeatable", "false" );
         data.setProperty("firstSubfield", "false");
         
         data.setProperty("defaultValue", "");
         data.setProperty("displayControl", "");
         data.setProperty("size", "");
         data.setProperty("helpMsg", "");
         data.setProperty("pickList", "");
         data.setProperty("valFormat", "");
         
         subNodeRow.setUserObject(data);
         nodeRow.add(subNodeRow);
      }
      if (fdtEntry.hasFirstSubfield()) {
         char subfieldCode = '*';
         WksTreeNode subNodeRow = new WksTreeNode("$"+subfieldCode);
         Properties data = new Properties(nameMap);
         data.setProperty("nodeType", "firstSubfieldNode");
         data.setProperty("subfieldCode", "$"+subfieldCode);
         data.setProperty("description", "V"+data.getProperty("tag")+Global.SUBFIELD_SEPARATOR+subfieldCode);
         data.setProperty("indicators", "false" );
         data.setProperty("repeatable", "false" );
         data.setProperty("firstSubfield", "false");
         
          data.setProperty("defaultValue", "");
         data.setProperty("displayControl", "");
         data.setProperty("size", "");
         data.setProperty("helpMsg", "");
         data.setProperty("pickList", "");
         data.setProperty("valFormat", "");
         
         subNodeRow.setUserObject(data);
      
         nodeRow.add(subNodeRow);
      }
      //adding all subfields like subnodes.
      String subfieldCodes = fdtEntry.getSubfields();
      
      for (int i = 0; i < subfieldCodes.length(); i++) {
         char subfieldCode = subfieldCodes.charAt(i);

         WksTreeNode subNodeRow = new WksTreeNode("$"+subfieldCode);

         Properties data = new Properties(nameMap);
         data.setProperty("nodeType", "subfieldNode");
         data.setProperty("subfieldCode", "$"+subfieldCode);
         data.setProperty("indicators", "false" );
         data.setProperty("repeatable", "false" );
         data.setProperty("firstSubfield", "false");
         data.setProperty("description", "V"+data.getProperty("tag")+Global.SUBFIELD_SEPARATOR+subfieldCode);
         data.setProperty("fieldType", Global.fiedType(Global.FIELD_TYPE_ALPHANUMERIC));
         
         data.setProperty("defaultValue", "");
         data.setProperty("displayControl", "");
         data.setProperty("size", "");
         data.setProperty("helpMsg", "");
         data.setProperty("pickList", "");
         data.setProperty("valFormat", "");
         
         subNodeRow.setUserObject(data);
        
         nodeRow.add(subNodeRow);
      }

      return nodeRow;
   }

   public WksTreeNode fillModelFromWksField(WorksheetDef.WorksheetField wksField, FieldDefinitionTable.FieldDefinition fdtEntry) {

      Properties nameMap = new Properties();
      nameMap.setProperty("nodeType", "fieldNode");
      nameMap.setProperty("tag", "" + wksField.getTag());
      nameMap.setProperty("subfieldCode", "");
      nameMap.setProperty("description", wksField.getDescription());
      nameMap.setProperty("fieldType", Global.fiedType(wksField.getType()));
      nameMap.setProperty("indicators", (fdtEntry.hasIndicators()) ? "true" : "false" );
      nameMap.setProperty("repeatable", (fdtEntry.isRepeatable()) ? "true" : "false" );
      nameMap.setProperty("firstSubfield", (fdtEntry.hasFirstSubfield()) ? "true" : "false" );
      nameMap.setProperty("defaultValue", wksField.getDefaultValue());

      nameMap.setProperty("displayControl", wksField.getDisplayControl());
      nameMap.setProperty("size", wksField.getSize());
      nameMap.setProperty("helpMsg", wksField.getHelpMessage());
      nameMap.setProperty("pickList", wksField.getPickList());
      nameMap.setProperty("valFormat", wksField.getValidationFormat());
      //creating a node for this element
      WksTreeNode nodeRow = new WksTreeNode("tag "+wksField.getTag());
      nodeRow.setUserObject(nameMap);


      //adding all subfields like subnodes.
      int nSubfields = wksField.getSubFieldsCount();

      for (int i = 0; i < nSubfields; i++) {
         WorksheetDef.WorksheetSubField wksSubfield = wksField.getSubFieldByIndex(i);

         String subfieldCode = wksSubfield.getSubfieldCode();
         WksTreeNode subNodeRow = new WksTreeNode((subfieldCode.startsWith("$") ? "" : "$") + subfieldCode);

         Properties data = new Properties(nameMap);
         data.setProperty("nodeType", "subfieldNode");
         data.setProperty("subfieldCode", (subfieldCode.startsWith("$") ? "" : "$") + subfieldCode);
         data.setProperty("description", wksSubfield.getDescription());
         data.setProperty("fieldType", Global.fiedType(wksSubfield.getType()));

         data.setProperty("repeatable", (wksSubfield.getRepeatable()) ? "true" : "false");
         data.setProperty("indicators", "false" );
         data.setProperty("firstSubfield", "false");

         data.setProperty("defaultValue", wksSubfield.getDefaultValue());

         data.setProperty("displayControl", wksSubfield.getDisplayControl());
         data.setProperty("size", wksSubfield.getSize());
         data.setProperty("helpMsg", wksSubfield.getHelpMessage());
         data.setProperty("pickList", wksSubfield.getPickList());
         data.setProperty("valFormat", wksSubfield.getValidationFormat());
         subNodeRow.setUserObject(data);

         nodeRow.add(subNodeRow);
      }

      return nodeRow;
   }

   @Override
   public Object getChild(Object parent, int index) {
      return ((WksTreeNode) parent).getChildAt(index);

   }

   @Override
   public int getChildCount(Object parent) {
      return ((WksTreeNode) parent).getChildCount();
   }

   @Override
   public int getIndexOfChild(Object parent, Object child) {
      return ((WksTreeNode) parent).getIndex((WksTreeNode) child);
   }

   @Override
   public Object getRoot() {
      return rootNode;
   }

   @Override
   public boolean isLeaf(Object node) {
      int child_count = getChildCount(node);
      if (child_count == 0) {
         return true;
      } else {
         return false;
      }

   }

   public void valueForPathChanged(TreePath path, Object newValue) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

  
}
