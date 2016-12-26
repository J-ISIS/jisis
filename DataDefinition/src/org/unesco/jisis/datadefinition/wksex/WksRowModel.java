/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.datadefinition.wksex;

import java.util.Properties;
import javax.swing.JComboBox;
import org.netbeans.swing.outline.RowModel;

/**
 *
 * @author jcd
 */
public class WksRowModel implements RowModel {

   public Class getColumnClass(int column) {
      switch (column) {
         case 0:
             return String.class;
         case 1:
            return Boolean.class;
         case 2:
            return Boolean.class;
         case 3:
            return Boolean.class;
         case 4:
            // Type of field
            return JComboBox.class;
             
          case 7:   // Display control
               return JComboBox.class;
         case 5:
         case 6:
        
         case 8:
         case 9:
         case 10:
            return String.class;
         default:
            assert false;
      }
      return null;
   }

   public int getColumnCount() {
      return 11;
   }

   public String getColumnName(int column) {
      switch (column) {
         case 0:
            return "Node Type";
         case 1:
            return "ind";
         case 2:
            return "rep";
         case 3:
            return "1stSubfield";
         case 4:
            return "Type";
         case 5:
            return "prompt";


         case 6:
            return "defaultValue";

         case 7:
            return "displayControl";
         case 8:
            return "helpMsg";
         case 9:
            return "pickList";
         case 10:
            return "valFormat";


         default:
            assert false;
      }
      return null;

   }

   public int getColumnIndex(String key) {
      // Column 0 is for the tree node
      if (key.equals("nodeType")) {
         return 1;
      }

       if (key.equals("indicators")) {
         return 2;
      }
      
      if (key.equals("repeatable")) {
         return 3;
      }

      if (key.equals("firstSubfield")) {
         return 4;
      }
      if (key.equals("fieldType")) {
         return 5;
      }
      if (key.equals("description")) {
         return 6;
      }

      if (key.equals("defaultValue")) {
         return 7;
      }


      if (key.equals("displayControl")) {
         return 8;
      }

      if (key.equals("helpMsg")) {
         return 9;
      }

      if (key.equals("pickList")) {
         return 10;
      }

      if (key.equals("valFormat")) {
         return 11;
      }
      return -1;
   }

   @Override
   public Object getValueFor(Object node, int column) {
      WksTreeNode n = (WksTreeNode) node;
      Properties nameMap = (Properties) n.getUserObject();
      //System.out.println("column="+column+" data="+nameMap.toString());
      switch (column) {
         case 0:
            return nameMap.getProperty("nodeType");
         case 1:
            return (new Boolean(nameMap.getProperty("indicators").equals("true")));
         
         case 2:

            return (new Boolean(nameMap.getProperty("repeatable").equals("true")));
          case 3:

            return (new Boolean(nameMap.getProperty("firstSubfield").equals("true")));
            case 4:
            return nameMap.getProperty("fieldType");
         case 5:
            return nameMap.getProperty("description");
         case 6:
            return nameMap.getProperty("defaultValue");

         case 7:
            return nameMap.getProperty("displayControl");
         case 8:
            return nameMap.getProperty("helpMsg");
         case 9:
            return nameMap.getProperty("pickList");
         case 10:
            return nameMap.getProperty("valFormat");


         default:
            assert false;
      }
      return null;
   }

   @Override
   public boolean isCellEditable(Object node, int column) {
       WksTreeNode wksNode = (WksTreeNode) node;
      Properties nameMap = (Properties) wksNode.getUserObject();
      if (nameMap.getProperty("nodeType").equals("rootNode")) {

         return false;
      }
      if (nameMap.getProperty("nodeType").equals("subfieldNode") &&
              (nameMap.getProperty("subfieldCode").equals("$ind1") ||
               nameMap.getProperty("subfieldCode").equals("$ind2"))) {
         return false;
      }
       switch (column) {
         case 0:
            return false; //"nodeType"
         case 1:
            if (nameMap.getProperty("nodeType").equals("fieldNode") && wksNode.getChildCount() > 0) {
                return false;
            }
            return false; //"indicators"
         case 2:
            return true; // "repeatable"
         case 3:
            return false; // "firstSubfield"
         case 4:
            return true; // "field/subfield type"
         case 5:
            return true; // "description"
         case 6:
            return true; //"defaultValue"

         case 7:
            return true; //"displayControl"
         case 8:
            return true; //"helpMsg"
         case 9:
            return true; //"pickList"
         case 10:
            return true; //"valFormat"


         default:
            assert false;
      }
      
      return false;
   }

   @Override
   public void setValueFor(Object node, int column, Object value) {
       WksTreeNode n = (WksTreeNode) node;
      Properties nameMap = (Properties) n.getUserObject();
      //System.out.println("column="+column+" data="+nameMap.toString());
      String val = null;
      switch (column) {
         case 0:
            return; //"nodeType"
         
         case 1:
            nameMap.setProperty("indicators", ((Boolean) value).toString());
            return; //"indicators"

         case 2:
            nameMap.setProperty("repeatable", ((Boolean) value).toString());
            return; // "repeatable"*

         case 3:
            nameMap.setProperty("firstSubfield", ((Boolean) value).toString());
            return; // "firstSubfield"

         case 4:
            val = (String) value;
            nameMap.setProperty("fieldType", val);
            return;
         case 5:
            val = (String) value;
            nameMap.setProperty("description", val);
            return;

         case 6:
            val = (String) value;
            nameMap.setProperty("defaultValue", val);
            return;

         case 7:
            val = (String) value;
            nameMap.setProperty("displayControl", val);
            return;

         case 8:
            val = (String) value;
            nameMap.setProperty("helpMsg", val);
            return;
            
         case 9:
            val = (String) value;
            nameMap.setProperty("pickList", val);
            return;
            
         case 10:
            val = (String) value;
            nameMap.setProperty("valFormat", val);
            return;

         default:
            assert false;
      }
     
    
   }
}
