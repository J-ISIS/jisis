/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RowModel;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.gui.DataWithIcon;

/**
 *
 * @author jcd
 */
public class DataEntryRowModel implements RowModel
{
    
    
   private final DataEntryPanel dataEntryPanel_;
  

   public DataEntryRowModel(DataEntryPanel dataEntryPanel) {
      dataEntryPanel_ = dataEntryPanel;
   }

   @Override
   public Class getColumnClass(int column) {
      switch (column) {
         case 0:
             return String.class;
         case 1:
             return Boolean.class;
            
         case 4:
            return JButton.class;
         case 2:
         case 3:
        
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
            return String.class;
         default:
            assert false;
      }
      return null;
   }

   @Override
   public int getColumnCount() {
      return 5;
   }

   @Override
   public String getColumnName(int column) {
      switch (column) {
         case 0:            
            return "Subf";
         case 1:
            return "Rep";
         case 2:
            return "Prompt";


         case 3:
            return "Data";
         case 4:
            return "Pick";

 
         default:
            assert false;
      }
      return null;

   }

   public int getColumnIndex(String key) {
      // Column 0 is for the tree node
      if (key.equals("subfield")) {
         return 1;
      }

      if (key.equals("repeatable")) {
         return 2;
      }
      if (key.equals("prompt")) {
         return 3;
      }

      if (key.equals("data")) {
         return 4;
      }
       if (key.equals("picklist")) {
         return 4;
      }

      return -1;
   }

   @Override
   public Object getValueFor(Object node, int column) {
      DataEntryNode dataEntryNode = (DataEntryNode) node;
      Hashtable nameMap = (Hashtable) dataEntryNode.getUserObject();
      //System.out.println("column="+column+" data="+nameMap.toString());
      switch (column) {
         case 0:
            return nameMap.get("subfieldCode");
         case 1:
            return (nameMap.get("repeatable").equals("true"));
         case 2:
            return nameMap.get("description");
         case 3:
            return nameMap.get("data");
         case 4:
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
         case 0:
            return false; //"subfieldCode"
         case 1:
            return false; // "repeatable"
         case 2:
            
            return false; // "description"
         case 3:
            pickListData = (PickListData) nameMap.get("pickList");
            if (pickListData != null) {
               if (pickListData.isNoType()) {
                  return false;
               }
            }
            return true; //"data"
          case 4:
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
         case 0:

            return; //"subfieldCode"
         case 1:

            return; // "repeatable"
         case 2:

            return; // "description"
         case 3:
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
            dataEntryPanel_.tableDataChanged();
            return;
            
          case 4:   // Picklist

              
               return;
           

         default:
            assert false;
      }


   }
}
