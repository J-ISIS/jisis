/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.datadefinition.wksex;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jcd
 */
public class CustomCellRenderer extends JLabel implements TableCellRenderer {

   private boolean isSelected;
   private boolean hasFocus;
   private CustomDataModel model;
    static final String FIELD_ICON_PATH = "org/unesco/jisis/datadefinition/wksex/server.gif";
    static final String FIELD_PLUS_ICON_PATH = "org/unesco/jisis/datadefinition/wksex/server+.gif";
    static final String FIELD_MINUS_ICON_PATH = "org/unesco/jisis/datadefinition/wksex/server-.gif";
    static final String SUBFIELD_ICON_PATH = "org/unesco/jisis/datadefinition/wksex/resource.gif";
    static final String SUBFIELD_LAST_ICON_PATH = "org/unesco/jisis/datadefinition/wksex/resource_last.gif";


   private ImageIcon[] images;

   public CustomCellRenderer(CustomDataModel model) {
      this.model = model;
// Create all of the images
      images = new ImageIcon[5];
      images[0] = new ImageIcon(ImageUtilities.loadImage(FIELD_ICON_PATH, true));

      images[1] = new ImageIcon(ImageUtilities.loadImage(FIELD_PLUS_ICON_PATH, true));
      images[2] = new ImageIcon(ImageUtilities.loadImage(FIELD_MINUS_ICON_PATH, true));
      images[3] = new ImageIcon(ImageUtilities.loadImage(SUBFIELD_ICON_PATH, true));
      images[4] = new ImageIcon(ImageUtilities.loadImage(SUBFIELD_LAST_ICON_PATH, true));
   }

   public Component getTableCellRendererComponent(
           JTable renderTable, Object value, boolean isSelected,
           boolean hasFocus, int iRowIndex, int iColumnIndex) {
      JisisTreeTable table = (JisisTreeTable) renderTable;
      this.hasFocus = hasFocus;
      this.isSelected = isSelected;
      if (iRowIndex < model.getRowCount()) {
// Get the node we are rendering
         MyTableNode node = (MyTableNode) model.listDisplayField.get(iRowIndex);
// Draw the correct text color depending on the
// selection state
         if (hasFocus || isSelected) {
            setForeground(Color.white);
         } else {
            setForeground(Color.black);
         }
// Draw the correct icon for this service
         if (iColumnIndex == 0) {
            if (node.type.equals(model.FIELD)) {
// Get a reference to the shadow parent
               int iShadowParent = model.GetShadowNode(
                       node.tag, model.FIELD);
               MyTableNode shadow = (MyTableNode) model.listField.get(iShadowParent);
               if (node.iChildren == 0 && shadow.iChildren == 0) {
                  setIcon(images[0]);
               } else if (node.iChildren == 0
                       && shadow.iChildren > 0) {
                  setIcon(images[1]);
               } else {
                  setIcon(images[2]);
               }
            } else {
// Get a reference to the parent node
               int iParent = iRowIndex - node.iParentOffset;
               MyTableNode parent = (MyTableNode) model.listDisplayField.get(iParent);
               if (iParent + parent.iChildren <= iRowIndex) {
                  setIcon(images[4]);
               } else {
                  setIcon(images[3]);
               }
            }
         } else {
            setIcon(null);
         }
// Draw the node text
         switch (iColumnIndex) {
            case 0:
               setText(""+node.tag);
               break;
            case 1:
               setText(node.subfieldCode);
               break;
            case 2:
               setText((node.repeatable) ? "%" : " ");
               break;
         }
      }
      return this;
   }
// This is a hack to paint the background. Normally, a JLabel can
// paint its own background, but, due to an apparent bug or
// limitation in the TreeCellRenderer, the paint method is
// required to handle this.
   public void paint(Graphics g) {
      Color bColor;
// Set the correct background color
      if (isSelected || hasFocus) {
         bColor = Color.red;
      } else {
         bColor = Color.white;
      }
      g.setColor(bColor);
// Draw a rectangle in the background of the cell
      g.fillRect(0, 0, getWidth(), getHeight() - 1);
      super.paint(g);
   }
}
