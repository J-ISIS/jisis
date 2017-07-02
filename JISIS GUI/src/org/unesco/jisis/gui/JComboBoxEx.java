/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

/**
 *
 * @author jcdau
 */
public class JComboBoxEx extends JComboBox {
    
    public JComboBoxEx() {
        super();
        setPrototypeDisplayValue("Short");
        this.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                if (index == -1) {
                    this.setToolTipText(value.toString());
                    return this;
                }

                setToolTipText(value.toString());
                Rectangle textRect
                        = new Rectangle(this.getSize().width,
                                getPreferredSize().height);
                String shortText = SwingUtilities.layoutCompoundLabel(this,
                        getFontMetrics(getFont()),
                          value.toString(), null,
                        getVerticalAlignment(), getHorizontalAlignment(),
                        getHorizontalTextPosition(), getVerticalTextPosition(),
                        textRect, new Rectangle(), textRect,
                        getIconTextGap());
                setText(shortText);
                return this;
            }
        });
    }
    
    @Override
    public JToolTip createToolTip() {
        JMultiLineToolTip tip = new JMultiLineToolTip();
        tip.setComponent(this);
        tip.setFixedWidth(500);
        return tip;
    }
    
}
