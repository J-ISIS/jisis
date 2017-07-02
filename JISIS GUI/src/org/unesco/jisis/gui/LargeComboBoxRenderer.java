/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author jcdau
 */

public class LargeComboBoxRenderer extends JLabel implements ListCellRenderer<String> {

    private final int wordWrapWidth;

    public LargeComboBoxRenderer(int wordWrapWidth) {
	this.wordWrapWidth = wordWrapWidth;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
	if (isSelected) {
	    setBackground(list.getSelectionBackground());
	    setForeground(list.getSelectionForeground());
	} else {
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	}

	setFont(list.getFont());
	setOpaque(true);

	// index is -1 when there is no pop-up
	if (index == -1) {
	    setText(value);
	} else {
	    setText(getHtmlWrappedText(value));
	}
        setToolTipText(getHtmlWrappedText(value));

	return this;
    }

    private String getHtmlWrappedText(String text) {
	StringBuilder sb = new StringBuilder(300);
	sb.append("<html>");
	sb.append("<p style=\"");
	sb.append(getParagraphStyle());
	sb.append("\">");
	sb.append(text);
	sb.append("</p>");
	sb.append("</html>");

	return sb.toString();
    }

    private String getParagraphStyle() {
	StringBuilder sb = new StringBuilder(100);
	sb.append("word-wrap: break-word;");
	sb.append("width: ");
	sb.append(wordWrapWidth);
	sb.append("px;");
	return sb.toString();
    }

}