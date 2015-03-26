/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutil.history;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;


/**
 *
 * @author jcd
 */
/**
 * Controller (manager of models) for HistoryTextArea.
 *
 * @author Slava Pestov
 * @version $Id: HistoryText.java 16341 2009-10-14 10:05:51Z kpouer $
 */
public  class HistoryText {

     //{{{ Private members
    private final JTextComponent text;
    private HistoryModel historyModel;
    private int index;
    private String current;
    private JPopupMenu popup;
    private boolean instantPopups;
	//}}}
    //{{{ HistoryText constructor

    public HistoryText(JTextComponent text, String name) {
        this.text = text;
        setModel(name);
        index = -1;
    } //}}}

    //{{{ fireActionPerformed() method
    public void fireActionPerformed() {
    } //}}}

    //{{{ getIndex() mehtod
    public int getIndex() {
        return index;
    } //}}}

    //{{{ setIndex() mehtod
    public void setIndex(int index) {
        this.index = index;
    } //}}}

    //{{{ getModel() method
    /**
     * Returns the underlying history controller.
     *
     * @return
     * @since jEdit 4.3pre1
     */
    public HistoryModel getModel() {
        return historyModel;
    } //}}}

    //{{{ setModel() method
    /**
     * Sets the history list controller.
     *
     * @param name The model name
     * @since jEdit 4.3pre1
     */
    public void setModel(String name) {
        if (name == null) {
            historyModel = null;
        } else {
            historyModel = HistoryModel.getModel(name);
        }
        index = -1;
    } //}}}

    //{{{ setInstantPopups() method
    /**
     * Sets if selecting a value from the popup should immediately fire an ActionEvent.
     *
     * @param instantPopups
     */
    public void setInstantPopups(boolean instantPopups) {
        this.instantPopups = instantPopups;
    } //}}}

    //{{{ getInstantPopups() method
    /**
     * Returns if selecting a value from the popup should immediately fire an ActionEvent.
     *
     * @return
     */
    public boolean getInstantPopups() {
        return instantPopups;
    } //}}}

    //{{{ addCurrentToHistory() method
    /**
     * Adds the currently entered item to the history.
     */
    public void addCurrentToHistory() {
        if (historyModel != null) {
            historyModel.addItem(getText());
        }
        index = 0;
    } //}}}

    //{{{ doBackwardSearch() method
    public void doBackwardSearch() {
        if (historyModel == null) {
            return;
        }

        if (text.getSelectionEnd() != getDocument().getLength()) {
            text.setCaretPosition(getDocument().getLength());
        }

        int start = getInputStart();
        String t = getText().substring(0,
            text.getSelectionStart() - start);
        if (t == null) {
            historyPrevious();
            return;
        }

        for (int i = index + 1; i < historyModel.getSize(); i++) {
            String item = historyModel.getItem(i);
            if (item.startsWith(t)) {
                text.replaceSelection(item.substring(t.length()));
                text.select(getInputStart() + t.length(),
                    getDocument().getLength());
                index = i;
                return;
            }
        }

        text.getToolkit().beep();
    } //}}}

    //{{{ doForwardSearch() method
    public void doForwardSearch() {
        if (historyModel == null) {
            return;
        }

        if (text.getSelectionEnd() != getDocument().getLength()) {
            text.setCaretPosition(getDocument().getLength());
        }

        int start = getInputStart();
        String t = getText().substring(0,
            text.getSelectionStart() - start);
        if (t == null) {
            historyNext();
            return;
        }

        for (int i = index - 1; i >= 0; i--) {
            String item = historyModel.getItem(i);
            if (item.startsWith(t)) {
                text.replaceSelection(item.substring(t.length()));
                text.select(getInputStart() + t.length(),
                    getDocument().getLength());
                index = i;
                return;
            }
        }

        text.getToolkit().beep();
    } //}}}

    //{{{ historyPrevious() method
    public void historyPrevious() {
        if (historyModel == null) {
            return;
        }

        if (index == historyModel.getSize() - 1) {
            text.getToolkit().beep();
        } else if (index == -1) {
            current = getText();
            setText(historyModel.getItem(0));
            index = 0;
        } else {
            // have to do this because setText() sets index to -1
            int newIndex = index + 1;
            setText(historyModel.getItem(newIndex));
            index = newIndex;
        }
    } //}}}

    //{{{ historyNext() method
    public void historyNext() {
        if (historyModel == null) {
            return;
        }

        if (index == -1) {
            text.getToolkit().beep();
        } else if (index == 0) {
            setText(current);
        } else {
            // have to do this because setText() sets index to -1
            int newIndex = index - 1;
            setText(historyModel.getItem(newIndex));
            index = newIndex;
        }
    } //}}}

    //{{{ getDocument() method
    public Document getDocument() {
        return text.getDocument();
    } //}}}

    //{{{ getText() method
    /**
     * Subclasses can override this to provide funky history behavior, for JTextPanes and such.
     *
     * @return
     */
    public String getText() {
        return text.getText();
    } //}}}

    //{{{ setText() method
    /**
     * Subclasses can override this to provide funky history behavior, for JTextPanes and such.
     *
     * @param text
     */
    public void setText(String text) {
        this.index = -1;
        this.text.setText(text);
    } //}}}

    //{{{ getInputStart() method
    /**
     * Subclasses can override this to provide funky history behavior, for JTextPanes and such.
     *
     * @return
     */
    public int getInputStart() {
        return 0;
    } //}}}

    //{{{ showPopupMenu() method
    public void showPopupMenu(String t, int x, int y) {
        if (historyModel == null || historyModel.size() == 0) {
            return;
        }

        text.requestFocus();

        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
            popup = null;
            return;
        }

        popup = new JPopupMenu() {
            @Override
            public void setVisible(boolean b) {
                if (!b) {
                    popup = null;
                }
                super.setVisible(b);
            }
        };
        JMenuItem caption = new JMenuItem(java.util.ResourceBundle.getBundle("org/unesco/jisis/jisisutil/history/Bundle")
                                                    .getString("PREVIOUSLY_ENTERED_STRINGS"));
        caption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ListModelEditor().open(historyModel);
            }
        });
        popup.add(caption);
        popup.addSeparator();

        for (int i = 0; i < historyModel.getSize(); i++) {
            String item = historyModel.getItem(i);
            if (item.startsWith(t)) {
                JMenuItem menuItem = new JMenuItem(item);
                menuItem.setActionCommand(String.valueOf(i));
                menuItem.addActionListener(
                    new ActionHandler());
                popup.add(menuItem);
            }
        }

        MiscUtilities.showPopupMenu(popup, text, x, y, false);
    } //}}}

    //{{{ showPopupMenu() method
    public void showPopupMenu(boolean search) {
        if (search) {
            showPopupMenu(getText().substring(getInputStart(),
                text.getSelectionStart()), 0, text.getHeight());
        } else {
            showPopupMenu("", 0, text.getHeight());
        }
    } //}}}

   

    /**
     * 
     */
    class ActionHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            int ind = Integer.parseInt(evt.getActionCommand());
            if (ind == -1) {
                if (index != -1) {
                    setText(current);
                }
            } else {
                setText(historyModel.getItem(ind));
                index = ind;
            }
            if (instantPopups) {
                addCurrentToHistory();
                fireActionPerformed();
            }
        }
    } 
}
