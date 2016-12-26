/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.swing.outline.RowModel;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.gui.DataWithIcon;

/**
 *
 * @author jcd
 */
public class FieldDataEntryRowModel implements RowModel
{

   public Class getColumnClass(int column) {
      switch (column) {
        
         case 0:
             return Boolean.class;
            
         case 2:
            return JButton.class;
         case 1:
         case 3:
        
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
            return String.class;
         default:
            assert false;
      }
      return null;
   }

   public int getColumnCount() {
      return 4;
   }

   public String getColumnName(int column) {
      switch (column) {
         
         case 0:
            return "Rep";
         case 1:
            return "Prompt";
         case 3:
            return "Data";
         case 2:
            return "Pick";
         default:
            assert false;
      }
      return null;

   }

   public final static int TREENODE_COLUMN_INDEX = 0;
   public final static int REPEATABLE_COLUMN_INDEX = 0;
   public final static int PROMPT_COLUMN_INDEX = 1;
   public final static int PICKLIST_COLUMN_INDEX = 2;
   public final static int DATA_COLUMN_INDEX = 3;
   
   public int getColumnIndex(String key) {
      // Column 0 is for the tree node
      

      if (key.equals("repeatable")) {
         return REPEATABLE_COLUMN_INDEX+1;
      }
      if (key.equals("prompt")) {
         return PROMPT_COLUMN_INDEX+1;
      }

      if (key.equals("data")) {
         return DATA_COLUMN_INDEX+1;
      }
       if (key.equals("picklist")) {
         return PICKLIST_COLUMN_INDEX+1;
      }

      return -1;
   }

   @Override
   public Object getValueFor(Object node, int column) {
      DataEntryNode dataEntryNode = (DataEntryNode) node;
      Hashtable nameMap = (Hashtable) dataEntryNode.getUserObject();
      //System.out.println("column="+column+" data="+nameMap.toString());
      switch (column) {
         
         case REPEATABLE_COLUMN_INDEX:
            return (nameMap.get("repeatable").equals("true"));
         case PROMPT_COLUMN_INDEX:
            return nameMap.get("description");
         case DATA_COLUMN_INDEX:
            return nameMap.get("data");
         case PICKLIST_COLUMN_INDEX:
            if (nameMap.get("type").equals("rootNode")) {
              return ""; 
            }
            Object obj = nameMap.get("pickList");
            if (obj != null && obj instanceof PickListData) {
              

               return new DataWithIcon("Pick", new ImageIcon(getClass().getResource("new.png")));
            }
            return "";
         //return nameMap.get("picklist");

         default:
            assert false;
      }
      return null;
   }

   @Override
   public boolean isCellEditable(Object node, int column) {
      DataEntryNode dataEntryNode = (DataEntryNode) node;
      Hashtable nameMap = (Hashtable) dataEntryNode.getUserObject();
      // Disable editing for root and fields with subfields
      if (nameMap.get("type").equals("rootNode") ||
              (nameMap.get("type").equals("fieldNode") && dataEntryNode.getChildCount() > 0)) {
         
         return false;
      }
       PickListData pickListData;
      switch (column) {
         
         case REPEATABLE_COLUMN_INDEX:
            return false; // "repeatable"
         case PROMPT_COLUMN_INDEX:
            
            return false; // "description"
         case DATA_COLUMN_INDEX:
            /**
             * Check that if we have a pick list then we don't gave a "notype" parameter
             * 
             */
            pickListData = (PickListData) nameMap.get("pickList");
            if (pickListData != null) {
               if (pickListData.isNoType()) {
                  return false;
               }
            }
            return true; //"data"
          case PICKLIST_COLUMN_INDEX:
             pickListData = (PickListData) nameMap.get("pickList");
            if (pickListData != null)
               return true; //"picklist"
             return false;

         default:
            assert false;
      }

      return false;
   }

   @Override
   public void setValueFor(Object node, int column, Object value) {

      DataEntryNode dataEntryNode = (DataEntryNode) node;
      Hashtable map = (Hashtable) dataEntryNode.getUserObject();

      switch (column) {
        
         case REPEATABLE_COLUMN_INDEX:

            return; // "repeatable"
         case PROMPT_COLUMN_INDEX:

            return; // "description"
         case DATA_COLUMN_INDEX:
            // Data
            if (value instanceof String) {
               String val = (String) value;
               map.put("data", val);

               if (map.get("type").equals("subfieldNode")) {
                  // Subfield updated, Update the field with the new value
                  DataEntryNode fieldNode = (DataEntryNode) dataEntryNode.getParent();
                  Hashtable fieldMap = (Hashtable) fieldNode.getUserObject();
                  // Rebuild the field data from the individual subfields
                  int nSubfields = fieldNode.getChildCount();
                  StringBuffer sb = new StringBuffer();
                  for (int i = 0; i < nSubfields; i++) {
                     DataEntryNode subfieldNode = (DataEntryNode) fieldNode.getChildAt(i);
                     Hashtable subfieldMap = (Hashtable) subfieldNode.getUserObject();
                     String subfieldCode = (String) subfieldMap.get("subfieldCode");
                     String data = (String) subfieldMap.get("data");
                     if (subfieldCode.equals("$ind1")) {
                        if (data.length() > 0) {
                           sb.append(data.charAt(0));
                        } else {
                           sb.append(" ");
                        }

                     } else if (subfieldCode.equals("$ind2")) {
                        if (data.length() > 0) {
                           sb.append(data.charAt(0));
                        } else {
                           sb.append(" ");
                        }

                     } else if (data.length() > 0) {
                        if (subfieldCode.charAt(1) == '*') {
                           // Implicit 1st subfield just get the data
                           sb.append(subfieldMap.get("data"));
                        } else {
                           sb.append(Global.SUBFIELD_SEPARATOR);
                           sb.append(subfieldCode.charAt(1));
                           sb.append(subfieldMap.get("data"));
                        }

                     }
                  }
                  fieldMap.put("data", sb.toString());
               }
            } else {
               // Blob
               map.put("data", value);

            }
            return;
            
          case PICKLIST_COLUMN_INDEX:   // Picklist

              
               return;
         default:
            assert false;
      }


   }
}
