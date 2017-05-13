/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 *
 * @author jcdau
 */
public class SmallToggleButton extends JToggleButton implements ItemListener {
  protected Border raised;

  protected Border lowered;

  public SmallToggleButton(boolean selected, ImageIcon imgUnselected,
      ImageIcon imgSelected, String tip) {
    super(imgUnselected, selected);
    setHorizontalAlignment(CENTER);
    setBorderPainted(true);
    raised = new BevelBorder(BevelBorder.RAISED);
    lowered = new BevelBorder(BevelBorder.LOWERED);
    setBorder(selected ? lowered : raised);
    setMargin(new Insets(1, 1, 1, 1));
    setToolTipText(tip);
    setRequestFocusEnabled(false);
    setSelectedIcon(imgSelected);
    addItemListener(this);
  }

  @Override
  public float getAlignmentY() {
    return 0.5f;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    setBorder(isSelected() ? lowered : raised);
  }
}