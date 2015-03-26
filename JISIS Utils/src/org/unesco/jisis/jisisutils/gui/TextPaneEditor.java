/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author jc_dauphin
 */

public class TextPaneEditor extends AbstractCellEditor implements TableCellEditor {

    private JTextPane holder = new JTextPane();
    private JTable parentTable;
    private boolean loading;
    protected LayoutUpdater layoutUpdater = new LayoutUpdater();

    public TextPaneEditor(JTable table) {
        parentTable = table;
        KeyStroke acceptKS = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK);

        holder.getInputMap().put(acceptKS, "accept");
        holder.getActionMap().put("accept", new AcceptChangesAction());
        holder.getDocument().addUndoableEditListener(layoutUpdater);
        //holder.setLineWrap(true);
        //holder.setWrapStyleWord(true);
    }

    /**
     * Returns true.
     *
     * @param e an event object
     *
     * @return true
     */
    public boolean isCellEditable(EventObject e) {
        if (e instanceof MouseEvent) {
            return ((MouseEvent) e).getClickCount() == 2;
        } else {
            return super.isCellEditable(e);
        }//else
    }

    /**
     * Returns the value contained in the editor.
     *
     * @return the value contained in the editor
     */
    public Object getCellEditorValue() {
        return holder.getText();
    }

    /**
     * Sets an initial <code>value</code> for the editor.  This will cause the editor to
     * <code>stopEditing</code> and lose any partially edited value if the editor is editing when
     * this method is called. <p>
     *
     * Returns the component that should be added to the client's <code>Component</code> hierarchy.
     * Once installed in the client's hierarchy this component will then be able to draw and receive
     * user input.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column) {
        loading = true;
        holder.setText(value != null ? (String) value : "");
        loading = false;
        return holder;
    }
     /**
     * Class to handle changing of holder content and fit height of editor to new content.
     * For perfomance trick.
     */
    protected class LayoutUpdater implements Runnable, UndoableEditListener {

        public void run() {
            int h = holder.getPreferredSize().height;
            parentTable.setRowHeight(parentTable.getEditingRow() , h);
        }

        /**
         * An undoable edit happened
         */
        public void undoableEditHappened(UndoableEditEvent e) {
            if (loading) return;
            SwingUtilities.invokeLater(this);
        }
    }

    /**
     * Action to process changes made by user in description field
     */
    protected class AcceptChangesAction extends AbstractAction {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            stopCellEditing();
        }
    }
}