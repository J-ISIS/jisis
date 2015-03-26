/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxEditor;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractComboBoxEditor implements ComboBoxEditor {
    // these are abstract
    public abstract Component getEditorComponent();
    public abstract void setItem(Object anObject);
    public abstract Object getItem();
    public abstract void selectAll();

    // these we'll implement.
    List<ActionListener> listenerList=null;
    public void addActionListener(ActionListener l) {
	if (listenerList==null)
	    listenerList=new LinkedList<ActionListener>();
	listenerList.add(0, l);
    }
    public void removeActionListener(ActionListener l) {
	if (listenerList==null) return; // nothing to do
	listenerList.remove(l);
    }
    // useful for subclasses
    protected void fireActionEvent() {
	fireActionEvent(ActionEvent.ACTION_PERFORMED, "comboBoxEdited");
    }
    protected void fireActionEvent(int id, String command) {
	if (listenerList==null) return; // nothing to do
	ActionEvent actionEvent=null;
	for (Iterator<ActionListener> it=listenerList.iterator();
	     it.hasNext(); ) {
	    if (actionEvent==null)
		actionEvent = new ActionEvent(this, id, command);
	    it.next().actionPerformed(actionEvent);
	}
    }
}
