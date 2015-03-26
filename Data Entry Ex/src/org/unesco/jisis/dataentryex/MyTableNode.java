/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

/**
 *
 * @author jcd
 */
public class MyTableNode {

   public int iParentOffset;
   public int iChildren; // Number of visible children
   public int iActualChildren; // Number of children that the
                               // node actually has
   public String typeString;
   public String nameString;
   public String locationString;
   public String statusString;

   public MyTableNode() {
      typeString = "";
      nameString = "";
      locationString = "";
      statusString = "";
      iParentOffset = 0;
      iChildren = 0;
      iActualChildren = 0;
   }

   public MyTableNode(MyTableNode node, int iOffset) {
      this.typeString = new String(node.typeString);
      this.nameString = new String(node.nameString);
      this.locationString = new String(node.locationString);
      this.statusString = new String(node.statusString);
      this.iParentOffset = iOffset;
      this.iChildren = node.iChildren;
      this.iActualChildren = node.iActualChildren;
   }
}
