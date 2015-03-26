/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;


/**
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * ActionTableCellEditor wraps a given TableCellEditor. This class itself
 * implements TableCellEditor and delegates all methods to the given
 * TableCellEditor.
 * in getTableCellEditorComponent method, we add custom editor button to the
 * existing TableCellEditor component and return. it.
 * When custom editor button is clicked, we cancel the cell editing and call the
 * method editCell which is abstract.
 *
 * Subclasses override editCell method to show a dialog and call
 * JTable.setValueAt(...) when [OK] button of dialog is pressed.
 */
public abstract class AbstractActionTableCellEditor implements TableCellEditor, ActionListener{
    public final Icon DOTDOTDOT_ICON = new ImageIcon(getClass().getResource("dotdotdot.png"));

    protected TableCellEditor editor;
    private JButton customEditorButton = new JButton(DOTDOTDOT_ICON);

    protected JTable table;
    protected int row, column;

    protected Frame frame;

    public AbstractActionTableCellEditor(TableCellEditor editor){
        this.editor = editor;
        customEditorButton.addActionListener(this);

        // ui-tweaking
        customEditorButton.setFocusable(false);
        customEditorButton.setFocusPainted(false);
        customEditorButton.setMargin(new Insets(0, 0, 0, 0));
    }

    public Component getTableCellEditorComponent(JTable table, Object value
                    , boolean isSelected, int row, int column){
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(editor.getTableCellEditorComponent(table, value, isSelected, row, column));
        panel.add(customEditorButton, BorderLayout.EAST);
        this.table = table;
        this.row = row;
        this.column = column;
        return panel;
    }

    public Object getCellEditorValue(){
        return editor.getCellEditorValue();
    }

    public boolean isCellEditable(EventObject anEvent){
        return editor.isCellEditable(anEvent);
    }

    public boolean shouldSelectCell(EventObject anEvent){
        return editor.shouldSelectCell(anEvent);
    }

    public boolean stopCellEditing(){
        return editor.stopCellEditing();
    }

    public void cancelCellEditing(){
        editor.cancelCellEditing();
    }

    public void addCellEditorListener(CellEditorListener l){
        editor.addCellEditorListener(l);
    }

    public void removeCellEditorListener(CellEditorListener l){
        editor.removeCellEditorListener(l);
    }

    public final void actionPerformed(ActionEvent e){
        editor.cancelCellEditing();
        frame = frameForActionEvent(e);
        editCell(table, row, column);
    }

     public  Frame frameForActionEvent(ActionEvent e) {
     
            Component c = (Component) table;
            while (c != null) {
                if (c instanceof Frame) {
                    return (Frame) c;
                }
                c =  c.getParent();
            }
        
        return null;
    }
    protected abstract void editCell(JTable table, int row, int column);
}
