/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author jcdau
 */
public class SmallButton extends JButton implements MouseListener {
  protected Border m_raised;

  protected Border m_lowered;

  protected Border m_inactive;

  public SmallButton(Action act, String tip) {
    super((Icon) act.getValue(Action.SMALL_ICON));
    m_raised = new BevelBorder(BevelBorder.RAISED);
    m_lowered = new BevelBorder(BevelBorder.LOWERED);
    m_inactive = new EmptyBorder(2, 2, 2, 2);
    setBorder(m_inactive);
    setMargin(new Insets(1, 1, 1, 1));
    setToolTipText(tip);
    addActionListener(act);
    addMouseListener(this);
    setRequestFocusEnabled(false);
  }

  @Override
  public float getAlignmentY() {
    return 0.5f;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    setBorder(m_lowered);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    setBorder(m_inactive);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    setBorder(m_raised);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    setBorder(m_inactive);
  }
}
