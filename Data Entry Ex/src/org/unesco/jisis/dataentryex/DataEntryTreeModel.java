/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.util.Hashtable;
import java.util.List;
import javax.swing.tree.TreePath;
import org.openide.util.Exceptions;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.common.WorksheetDef.WorksheetField;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.picklist.ValidationData;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IOccurrence;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.corelib.record.StringOccurrence;
import org.unesco.jisis.corelib.record.Subfield;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;

/**
 *
 * @author jcd
 */
public class DataEntryTreeModel extends AbstractTreeModel {

   private DataEntryNode rootNode;
   private IDatabase db_;
   private IRecord record_ = null;
   private WorksheetDef wks_ = null;
   private boolean showEmptyFields_;
   private List<PickListData> pickListDataList_;
   private List<ValidationData> validationDataList_;

    private DataEntryTreeModel(IDatabase db, WorksheetDef wks,
            List<PickListData> pickListDataList, List<ValidationData> validationDataList) {
        db_ = db;
        wks_ = wks;
        record_ = null;

        pickListDataList_ = pickListDataList;
        validationDataList_ = validationDataList;

        buildTreeModelForEmptyRecord(wks);

    }

   private DataEntryTreeModel(IDatabase db, WorksheetDef wks, IRecord record, 
           List<PickListData>  pickListDataList, List<ValidationData> validationDataList, boolean showEmptyFields) {
      db_ = db;
      wks_ = wks;
      record_ = record;
      pickListDataList_ = pickListDataList;
      validationDataList_ = validationDataList;
      
      showEmptyFields_ = showEmptyFields;
       
      buildTreeModelForRecord(wks, record);
   }

   //-----------------------------------------------------------------
   // Public static factory methods
   //-----------------------------------------------------------------
    public static DataEntryTreeModel makeTreeModelForEmptyRecord(IDatabase db, WorksheetDef wks,
            List<PickListData>  pickListDataList, List<ValidationData> validationDataList) {
        DataEntryTreeModel treeModel = new DataEntryTreeModel(db, wks,
                pickListDataList, validationDataList);
        return treeModel;
    }

   public static DataEntryTreeModel makeTreeModelForRecord(IDatabase db, WorksheetDef wks, IRecord record, 
           List<PickListData>  pickListDataList, List<ValidationData> validationDataList, boolean showEmptyFields) {
      DataEntryTreeModel treeModel = new DataEntryTreeModel(db, wks, record, 
              pickListDataList, validationDataList, showEmptyFields);
      return treeModel;
   }

   private void buildTreeModelForRecord(WorksheetDef wks, IRecord record) {
      Hashtable<String, Object> nameMap = new Hashtable<String, Object>();
      nameMap.put("type", "rootNode");

      nameMap.put("repeatable", "");
      nameMap.put("defaultValue", "");
      nameMap.put("description", "");
      nameMap.put("displayControl", "");
      nameMap.put("helpMsg", "");
      nameMap.put("pickList", "");
      nameMap.put("valFormat", "");
      nameMap.put("data", "");

      rootNode = (record.getMfn()==0L) ? new DataEntryNode(wks.getName())
                                       : new DataEntryNode("MFN: " + record.getMfn());
      rootNode.setUserObject(nameMap);

      for (int i = 0; i < wks.getFieldsCount(); i++) {
         try {
            WorksheetDef.WorksheetField wksField = wks.getFieldByIndex(i);
            IField fld = record.getField(wksField.getTag());
            if (!showEmptyFields_ && fld.isEmpty()) {
               continue;
            }
            
            DataEntryNode[] nodes = buildTreelNodesForWksField(wksField, record);
            for (int j = 0; j < nodes.length; j++) {
               rootNode.add(nodes[j]);
            }
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
      }
   }

     private String fieldDefaultValue(String defaultValue) {
      try {

         if ((defaultValue != null) && (defaultValue.length() > 0)) {
            ISISFormatter formatter = ISISFormatter.getFormatter(defaultValue);
             if (formatter == null) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return null;
             } else if (formatter.hasParsingError()) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return null;
             }
           IRecord record = Record.createRecord();
            formatter.setRecord(db_, record);
            formatter.eval();
            String result = formatter.getText();

            return result;

         } else {
            return null;
         }
      } catch (RuntimeException re) {
         new DefaultFormattingException(re.getMessage()).displayWarning();
         return null;
      }
   }

   /**
    * Build Tree node(s) for a Worksheet Field from the record data. We have several nodes when
    * the record field has multiple occurrences. The worksheet subfields (if any) are added as sub-nodes and
    * filled with record data.
    *
    * @param wksField - The worksheet Field definition
    * @param record   - The record to accommodate for this worksheet field
    *
    * @return - The tree nodes resulting from combining the worksheet field
    *           definition with the record field
    */
   private DataEntryNode[] buildTreelNodesForWksField(WorksheetField wksField,
           IRecord record) {
      DataEntryNode[] nodes = null;
      try {
         // Get the field tag from the worksheet field definition
         int wksTag = wksField.getTag();
         // Get the record field identified by this tag
         IField fld = record.getField(wksTag);
         if (fld == null) {
            // If the field is not in the record, we build an empty tree node
            nodes = new DataEntryNode[1];
            nodes[0] = buildTreeNodes(wksField, false);
            return nodes;
          }
         /**
          * Let assume that we may have pickList and validation rules on the field
          * even if we have subfields
          */
          PickListData pickListData = null;
          for (PickListData pickList : pickListDataList_) {
              if (Integer.valueOf(pickList.getTag()) == wksField.getTag()
                      && pickList.getSubfieldCode().length() == 0) {
                  pickListData = pickList;
                  break;
              }
          }
          ValidationData validationData = null;
          for (ValidationData validation : validationDataList_) {
              if (Integer.valueOf(validation.getTag()) == wksField.getTag()
                      && validation.getSubfieldCode().length() == 0) {
                  validationData = validation;
                  break;
              }
          }

          int nOccurrences = fld.getOccurrenceCount();
          if (nOccurrences == 0) {
              nodes = new DataEntryNode[1];
              nodes[0] = buildTreeNodes(wksField, false);
              return nodes;
          }
          nodes = new DataEntryNode[nOccurrences];
          boolean noTyping = false;
          if (pickListData != null) {
              noTyping = pickListData.isNoType();
          }
         // Create a wks entry for each occurrence
         for (int i = 0; i < nOccurrences; i++) {
            IOccurrence occurrence = fld.getOccurrence(i);
            Hashtable fieldMap = new Hashtable<String, Object>();
            fieldMap.put("type", "fieldNode");
            fieldMap.put("tag", "" + wksField.getTag());
            fieldMap.put("subfieldCode", "");
            fieldMap.put("repeatable", (wksField.getRepeatable()) ? "true" : "false");
            fieldMap.put("defaultValue", wksField.getDefaultValue());
            fieldMap.put("fieldType", Global.fiedType(wksField.getType()));
            fieldMap.put("description", wksField.getDescription());
            fieldMap.put("displayControl", wksField.getDisplayControl());
            fieldMap.put("helpMsg", wksField.getHelpMessage());
           
            if (pickListData != null) fieldMap.put("pickList", pickListData);
            if (validationData != null) fieldMap.put("valFormat", validationData.getFormat());
            if (wksField.getType() == Global.FIELD_TYPE_BLOB) {
               fieldMap.put("data", occurrence.getValue());
            } else {
               fieldMap.put("data", (fld == null) ? "" : occurrence.toString());
            }
            //creating a node for this element
            DataEntryNode nodeRow = new DataEntryNode("tag " + wksField.getTag());
            nodeRow.setUserObject(fieldMap);
            //adding all worksheet subfields like subnodes.
            int nWksSubfields = wksField.getSubFieldsCount();
            boolean hasIndicators = false;
            for (int j = 0; j < nWksSubfields; j++) {
               WorksheetDef.WorksheetSubField wksSubfield = wksField.getSubFieldByIndex(j);
               DataEntryNode subNodeRow = new DataEntryNode(wksSubfield.getSubfieldCode());
               Hashtable<String, Object> subfieldMap = new Hashtable<String, Object>();
               subfieldMap.put("type", "subfieldNode");
               subfieldMap.put("tag", "" + wksField.getTag());
               subfieldMap.put("subfieldCode", wksSubfield.getSubfieldCode());
               subfieldMap.put("repeatable", (wksSubfield.getRepeatable()) ? "true" : "false");
               subfieldMap.put("defaultValue", wksSubfield.getDefaultValue());
               subfieldMap.put("fieldType", Global.fiedType(wksSubfield.getType()));
               subfieldMap.put("description", wksSubfield.getDescription());
               subfieldMap.put("displayControl", wksSubfield.getDisplayControl());
               subfieldMap.put("helpMsg", wksSubfield.getHelpMessage());
                pickListData = null;
                for (PickListData pickList : pickListDataList_) {
                    if (Integer.valueOf(pickList.getTag()) == wksSubfield.getTag()
                        && wksSubfield.getSubfieldCode().equals(pickList.getSubfieldCode())) {
                        pickListData = pickList;
                        break;
                    }
                }
               if (pickListData != null) {
                  subfieldMap.put("pickList", pickListData);
               }
                validationData = null;
                for (ValidationData validation : validationDataList_) {
                    if (Integer.valueOf(validation.getTag()) == wksSubfield.getTag()
                        && wksSubfield.getSubfieldCode().equals(validation.getSubfieldCode())) {
                        validationData = validation;
                        break;
                    }
                }
               if (validationData != null) {
                  subfieldMap.put("valFormat", validationData.getFormat());
               }

               if (wksSubfield.getSubfieldCode().equals("$ind1")) {
                  hasIndicators = true;
                  StringOccurrence occ = (StringOccurrence) occurrence;
                  // Get 1st subfield of occurrence
                  Subfield subfield = occ.getSubfield(0);
                  // If we have a 1st implicit subfield and indicators
                  // Then the 1st character in the subfield is the
                  // MARC "first indicator"
                  String descriptor1 = (subfield == null || subfield.getSubfieldCode() != '*'
                          || subfield.getData().length() <= 0) ? " "
                          : Character.toString(subfield.getData().charAt(0));

                  subfieldMap.put("data", descriptor1);
                  subNodeRow.setUserObject(subfieldMap);
                  nodeRow.add(subNodeRow);
               } else if (wksSubfield.getSubfieldCode().equals("$ind2")) {
                  //System.out.println(wksSubfield.toString());
                  StringOccurrence occ = (StringOccurrence) occurrence;
                  //System.out.println(occurrence.toString());
                  Subfield subfield = occ.getSubfield(0);
                  // If we have a 1st implicit subfield and indicators
                  // Then the 2nd character in the subfield is the
                  // MARC "second indicator"
                  String descriptor2 = (subfield == null || subfield.getSubfieldCode() != '*'
                          || subfield.getData().length() <= 1) ? " "
                          : Character.toString(subfield.getData().charAt(1));

                  subfieldMap.put("data", descriptor2);
                  subNodeRow.setUserObject(subfieldMap);
                  nodeRow.add(subNodeRow);
               } else if (wksSubfield.getSubfieldCode().charAt(0) == '*') {
                  Character subfieldCode = wksSubfield.getSubfieldCode().charAt(1);
                  String charCode = Global.SUBFIELD_SEPARATOR + Character.toString(subfieldCode);

                  String subfieldData = occurrence.getSubfield(charCode);
                  // We should have only one occurrence of 1st implicit subfield and
                  // this field may or maynot contain the indicators
                  // In that case, we should remove the indicators from the field value

                  if (hasIndicators && subfieldData != null && subfieldData.length() > 2) {
                     subfieldData = subfieldData.substring(2);
                  }

                  subfieldMap.put("data", (subfieldData == null) ? "" : subfieldData);
                  subNodeRow.setUserObject(subfieldMap);
                  nodeRow.add(subNodeRow);
               } else {

                  Character subfieldCode = wksSubfield.getSubfieldCode().charAt(1);
                  String charCode = Global.SUBFIELD_SEPARATOR + Character.toString(subfieldCode);
                  // Get all subfield occurrences
                  List<Subfield> subfieldOccurrences = ((StringOccurrence) occurrence).getSubfieldOccurrences(charCode);
                  if (subfieldOccurrences == null) {
                     subfieldMap.put("data", "");

                     subNodeRow.setUserObject(subfieldMap);
                     nodeRow.add(subNodeRow);
                  } else {
                     for (Subfield subfieldOccurrence : subfieldOccurrences) {
                        String subfieldData = subfieldOccurrence.getData();
                        DataEntryNode newNode = new DataEntryNode(wksSubfield.getSubfieldCode());

                        Hashtable<String, Object> subfieldMap1 = new Hashtable<String, Object>(subfieldMap);
                        subfieldMap1.put("data", (subfieldData == null) ? "" : subfieldData);

                        newNode.setUserObject(subfieldMap1);
                        nodeRow.add(newNode);
                     }
                  }
               }
            } // j -Loop on worksheet subfields for one field occurrence
            nodes[i] = nodeRow;
         } // i - Loop on Record field occurrences for this Worksheet Field Entry
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      return nodes;
   }

   private void buildTreeModelForEmptyRecord(WorksheetDef wks) {
      Hashtable<String, Object> nameMap = new Hashtable<String, Object>();
      nameMap.put("type", "rootNode");

      nameMap.put("repeatable", "");
      nameMap.put("defaultValue", "");
      nameMap.put("description", "");
      nameMap.put("displayControl", "");
      nameMap.put("helpMsg", "");
      nameMap.put("pickList", "");
      nameMap.put("valFormat", "");
      nameMap.put("data", "");

      rootNode = new DataEntryNode(wks.getName());
      rootNode.setUserObject(nameMap);

      for (int i = 0; i < wks.getFieldsCount(); i++) {
         WorksheetDef.WorksheetField wksField = wks.getFieldByIndex(i);

         DataEntryNode node = buildTreeNodes(wksField, true);
         rootNode.add(node);
      }
   }
   /**
    * Build a DataEntryNode for a subfield
    * @param wksField    Worksheet Field Info
    * @param wksSubfield Worksheet Subfield Info
    * @param newRecord   true for a new Record and false otherwise for an
    *                    existing record.
    * @return
    */
   public DataEntryNode buildTreeSubfieldNode(WorksheetDef.WorksheetField wksField,
           WorksheetDef.WorksheetSubField wksSubfield, boolean newRecord) {

        PickListData pickListData = null;
          for (PickListData pickList : pickListDataList_) {
              if (Integer.valueOf(pickList.getTag()) == wksField.getTag() &&
                  wksSubfield.getSubfieldCode().equals(pickList.getSubfieldCode())) {
                  pickListData = pickList;
                  break;
              }
          }
          ValidationData validationData = null;
          for (ValidationData validation : validationDataList_) {
              if (Integer.valueOf(validation.getTag()) == wksField.getTag()&&
                  wksSubfield.getSubfieldCode().equals(validation.getSubfieldCode())) {
                  validationData = validation;
                  break;
              }
          }

      DataEntryNode subNodeRow = new DataEntryNode(wksSubfield.getSubfieldCode());

      Hashtable<String, Object> subfieldMap = new Hashtable<String, Object>();
      subfieldMap.put("type", "subfieldNode");
      subfieldMap.put("tag", "" + wksField.getTag());
      subfieldMap.put("subfieldCode", wksSubfield.getSubfieldCode());
      subfieldMap.put("repeatable", (wksSubfield.getRepeatable()) ? "true" : "false");
      subfieldMap.put("defaultValue", wksSubfield.getDefaultValue());
      subfieldMap.put("fieldType", Global.fiedType(wksSubfield.getType()));
      subfieldMap.put("description", wksSubfield.getDescription());

      subfieldMap.put("displayControl", wksSubfield.getDisplayControl());
      subfieldMap.put("helpMsg", wksSubfield.getHelpMessage());
      if (pickListData != null) subfieldMap.put("pickList", pickListData);
      if (validationData != null) subfieldMap.put("valFormat", validationData.getFormat());
     
      String dataValue = "";
//         if (wksSubfield.getSubfieldCode().equals("$ind1") ||
//             wksSubfield.getSubfieldCode().equals("$ind2")) {
//            dataValue = "#";
//            fieldMap.put("data", "##");
//         }
      if (newRecord && wksSubfield.getDefaultValue() != null) {
         // This is a new record and we have a default value PFT
         String defaultValue = wksField.getDefaultValue();
         // Execute the PFT
         String value = fieldDefaultValue(defaultValue);
         if (value != null) {
            dataValue = value;
         }
      }
      subfieldMap.put("data", dataValue);
      subNodeRow.setUserObject(subfieldMap);
      return subNodeRow;
   }

  /**
   * Build an empty Tree node for a worksheet entry
   * @param wksField The worksheet entry
   * @param newRecord true if it's new record and false if it is an empty field
   *                  from an existing record
   * @return The DataEntryNode built from this worksheet entry
   */
   public DataEntryNode buildTreeNodes(WorksheetDef.WorksheetField wksField, boolean newRecord) {

        PickListData pickListData = null;
          for (PickListData pickList : pickListDataList_) {
              if (Integer.valueOf(pickList.getTag()) == wksField.getTag() && 
                  pickList.getSubfieldCode().length() == 0) {
                  pickListData = pickList;
                  break;
              }
          }
          ValidationData validationData = null;
          for (ValidationData validation : validationDataList_) {
              if (Integer.valueOf(validation.getTag()) == wksField.getTag()) {
                  validationData = validation;
                  break;
              }
          }

      Hashtable<String, Object> fieldMap = new Hashtable<String, Object>();
      fieldMap.put("type", "fieldNode");
      fieldMap.put("tag", "" + wksField.getTag());
      fieldMap.put("subfieldCode", "");
      fieldMap.put("repeatable", (wksField.getRepeatable()) ? "true" : "false");
      fieldMap.put("defaultValue", wksField.getDefaultValue());
      fieldMap.put("fieldType", Global.fiedType(wksField.getType()));
      fieldMap.put("description", wksField.getDescription());
      fieldMap.put("displayControl", wksField.getDisplayControl());
      fieldMap.put("helpMsg", wksField.getHelpMessage());
      if (pickListData != null) fieldMap.put("pickList", pickListData);
      if (validationData != null) fieldMap.put("valFormat", validationData.getFormat());
      String dataValue = "";
       
      if (newRecord && wksField.getDefaultValue() != null) {
         // This is a new record and we have a default value PFT
         String defaultValue = wksField.getDefaultValue();
         // Execute the PFT
         String value = fieldDefaultValue(defaultValue);
         if (value != null) {
            dataValue = value;
         }
      }
      fieldMap.put("data", dataValue);
      //creating a node for this element
      DataEntryNode nodeRow = new DataEntryNode("tag " + wksField.getTag());
      nodeRow.setUserObject(fieldMap);

      //adding all subfields like subnodes.

      int nSubfields = wksField.getSubFieldsCount();

      for (int i = 0; i < nSubfields; i++) {
         WorksheetDef.WorksheetSubField wksSubfield = wksField.getSubFieldByIndex(i);
         DataEntryNode subNodeRow = buildTreeSubfieldNode(wksField,wksSubfield,newRecord);
         nodeRow.add(subNodeRow);
      }

      return nodeRow;
   }

   /**WARNING!!!!! THESE 2 METHODS ALWAYS RETURN THE FIRST OCCURRENCE FOUND
    * SHOULD NOT BE USED!!!!!!!!!!!!!!!!!!!!!!!
    */
   /**
    * Get DataEntryNode with this tag
    * @param tag
    * @return
    */
   public DataEntryNode getFieldNode(int tag) {
      for (DataEntryNode node = (DataEntryNode) rootNode.getFirstChild(); node != null; node = (DataEntryNode) node.getNextSibling()) {
         Hashtable data = (Hashtable) node.getUserObject();
         int deTag = Integer.valueOf((String)data.get("tag"));
         if (deTag == tag) {
            return node;
         }
      }
      return null;
   }

   /**
    * Get DataEntryNode with this tag, and subfield
    * @param tag
    * @return
    */
   public DataEntryNode getSubfieldNode(int tag, String subfieldTag) {
      DataEntryNode tagNode = getFieldNode(tag);
      if (tagNode == null) {
         return null;
      }
      for (DataEntryNode node = (DataEntryNode) tagNode.getFirstChild();
              node != null; node = (DataEntryNode) node.getNextSibling()) {
         Hashtable data = (Hashtable) node.getUserObject();
         String subfieldCode  = (String)data.get("subfieldCode");
         if (subfieldCode.equals(subfieldTag)) {
            return node;
         }
      }
      return null;
   }

   @Override
   public Object getChild(Object parent, int index) {
      return ((DataEntryNode) parent).getChildAt(index);

   }

   @Override
   public int getChildCount(Object parent) {
      return ((DataEntryNode) parent).getChildCount();
   }

   @Override
   public int getIndexOfChild(Object parent, Object child) {
      return ((DataEntryNode) parent).getIndex((DataEntryNode) child);
   }

   @Override
   public Object getRoot() {
      return rootNode;
   }


   public DataEntryNode getDataEntryTreeRoot() {
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
