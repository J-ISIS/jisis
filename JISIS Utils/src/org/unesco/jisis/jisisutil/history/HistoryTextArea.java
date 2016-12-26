/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutil.history;

import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

/**
 *
 * @author jcd
 */
public class HistoryTextArea extends JTextArea {

    //{{{ Private variables

    private final HistoryText controller;
	//}}}

    public HistoryTextArea(String name) {
        super(3, 15);
        controller = new HistoryText(this, name);
        setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            Collections.singleton(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
        setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            Collections.singleton(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.SHIFT_MASK)));
    } //}}}

    //{{{ getModel() method
    /**
     * Returns the underlying history controller.
     *
     * @return
     * @since jEdit 4.3pre1
     */
    public HistoryModel getModel() {
        return controller.getModel();
    } //}}}

    //{{{ setModel() method
    /**
     * Sets the history list controller.
     *
     * @param name The model name
     * @since jEdit 4.3pre1
     */
    public void setModel(String name) {
        controller.setModel(name);
    } //}}}

    //{{{ setInstantPopups() method
    /**
     * Sets if selecting a value from the popup should immediately fire an ActionEvent.
     *
     * @param instantPopups
     */
    public void setInstantPopups(boolean instantPopups) {
        controller.setInstantPopups(instantPopups);
    } //}}}

    //{{{ getInstantPopups() method
    /**
     * Returns if selecting a value from the popup should immediately fire an ActionEvent.
     *
     * @return
     */
    public boolean getInstantPopups() {
        return controller.getInstantPopups();
    } //}}}

    //{{{ addCurrentToHistory() method
    /**
     * Adds the currently entered item to the history.
     */
    public void addCurrentToHistory() {
        controller.addCurrentToHistory();
    } //}}}

    //{{{ setText() method
    /**
     * Sets the displayed text.
     *
     * @param text
     */
    @Override
    public void setText(String text) {
        super.setText(text);
        controller.setIndex(-1);
    } //}}}

	//{{{ Protected members
    //{{{ processKeyEvent() method
    @Override
    protected void processKeyEvent(KeyEvent evt) {
        if (!isEnabled()) {
            return;
        }

        if (evt.getID() == KeyEvent.KEY_PRESSED) {
            switch (evt.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    if (evt.isControlDown()) {
                        replaceSelection("\n");
                        evt.consume();
                    }
                    break;
                case KeyEvent.VK_TAB:
                    if (evt.isControlDown()) {
                        replaceSelection("\t");
                        evt.consume();
                    }
                    break;
                case KeyEvent.VK_PAGE_UP:
                    if (evt.isShiftDown()) {
                        controller.doBackwardSearch();
                    } else {
                        controller.historyPrevious();
                    }
                    evt.consume();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    if (evt.isShiftDown()) {
                        controller.doForwardSearch();
                    } else {
                        controller.historyNext();
                    }
                    evt.consume();
                    break;
                case KeyEvent.VK_UP:
                    if (evt.isAltDown()) {
                        controller.showPopupMenu(
                            evt.isShiftDown());
                        evt.consume();
                    }
                    break;
            }
        }

        if (!evt.isConsumed()) {
            super.processKeyEvent(evt);
        }
    } //}}}

    //{{{ processMouseEvent() method
    @Override
    protected void processMouseEvent(MouseEvent evt) {
        if (!isEnabled()) {
            return;
        }

        switch (evt.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                if (evt.isPopupTrigger()) {
                    controller.showPopupMenu(evt.isShiftDown());
                } else {
                    super.processMouseEvent(evt);
                }

                break;
            default:
                super.processMouseEvent(evt);
                break;
        }
    } //}}}

	//}}}
}
