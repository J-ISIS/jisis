/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.TreePathSupport;

/**
 *
 * @author jcd
 */
public class OutlineUtil {
/**
 * Save the expansion state of a tree.
 *
 * @param tree
 * @return expanded tree path as Enumeration
 */
   public static TreePath[] saveExpansionState(Outline tree) {

      TreePathSupport treePathSupport = tree.getOutlineModel().getTreePathSupport();

      return treePathSupport.getExpandedDescendants(new TreePath(tree.getOutlineModel().getRoot()));

   }


    /**
     * Restore the expansion state of a JTree.
     *
     * @param tree
     * @param enumeration an Enumeration of expansion state. You can get it using {@link #saveExpansionState(javax.swing.JTree)}.
     */

    public static void loadExpansionState(Outline tree, TreePath[] treeExpansion) {

        if (treeExpansion != null) {
            for (TreePath treePath : treeExpansion) {
                tree.expandPath(treePath);
            }
        }
    }


     public static void loadExpansionStateEx(Outline tree, TreePath[] treeExpansion) {

        DefaultMutableTreeNode newRoot = (DefaultMutableTreeNode) tree.getOutlineModel().getRoot();

        if (treeExpansion != null) {
           
            for (TreePath treePath : treeExpansion) {
               DefaultMutableTreeNode last = (DefaultMutableTreeNode) treePath.getLastPathComponent();
               TreePath newTreePath = new TreePath(newRoot);
               if (treePath.getPathCount() > 1) {
                  last.setParent(newRoot);
                  newTreePath = new TreePath( new Object[] { newRoot, last});
               }
               
               System.out.println("treePath**"+treePath.toString());
               System.out.println("newTreePath**"+newTreePath.toString());
               tree.expandPath(newTreePath);
            }
        }
    }

}
