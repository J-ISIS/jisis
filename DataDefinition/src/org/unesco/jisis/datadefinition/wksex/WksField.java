/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.datadefinition.wksex;

/**
 *
 * @author jcd
 */
public class WksField {

   public int iParentOffset;
   public int iChildren; // Number of visible children
   public int iActualChildren; // Number of children that the
   // node actually has
   public static final int ROOT_NODE = 0;
   public static final int FIELD_NODE = 1;
   public static final int SUBFIELD_NODE = 2;
   public static final int EMPTY_NODE = 3;
   //XXX deleteme - string version of the avoid constants debug output:
   public static final String[] types = new String[]{
      "rootNode", "fieldNode", "subfieldNode", "emptyNode"
   };
   public String type;
   public int tag;
   public String subfieldCode;
   public boolean repeatable;
   protected String defaultValue;
   protected String description;
   protected String displayControl;
   protected String size;
   protected String helpMsg;
   protected String pickList;
   protected String valFormat;

   public WksField() {
      type = types[EMPTY_NODE];
      tag = 0;
      subfieldCode = "";
      repeatable = false;
      defaultValue = "";
      description = "";
      displayControl = "";
      size = "";
      helpMsg = "";
      pickList = "";
      valFormat = "";

      iParentOffset = 0;
      iChildren = 0;
      iActualChildren = 0;
   }

   public WksField(MyTableNode node, int iOffset) {
      type = new String(node.type);
      tag = node.tag;
      subfieldCode = new String(node.subfieldCode);
      repeatable = node.repeatable;
      defaultValue = new String(node.defaultValue);
      description = new String(node.description);
      displayControl = new String(node.displayControl);
      size = new String(node.size);
      helpMsg = new String(node.helpMsg);
      pickList = new String(node.pickList);
      valFormat = new String(node.valFormat);

      this.iParentOffset = iOffset;
      this.iChildren = node.iChildren;
      this.iActualChildren = node.iActualChildren;
   }
}
