/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.datadefinition.wksex;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author jcd
 */
public class WksTreeNode extends DefaultMutableTreeNode {

        String displayName;
	private List<Object> tableRowValues;

	private WksTreeNode() {
		
	}
        public WksTreeNode(String label) {

		super();
                displayName = label;
		tableRowValues = new ArrayList<Object>();
	}

	public void setTableRowValues(List<Object> values) {
		tableRowValues = values;
	}

	public List<Object> getTableRowValues() {
		return tableRowValues;
	}
        /**
         * A DefaultMutableTreeNode may also hold a reference to a user object,
         * the use of which is left to the user.
         * Asking a DefaultMutableTreeNode  for its string representation with
         * toString() returns the string representation of its user object.
         * @return
         */
   @Override
        public String toString() {
           return displayName;
        }

}