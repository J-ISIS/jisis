/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;

import org.unesco.jisis.jisiscore.client.GuiGlobal;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.actions.Presenter;


public final class SelectHitSortFileAction extends AbstractAction implements Presenter.Toolbar  {

   Component comp_ = new HitSortFilePanel();

   public SelectHitSortFileAction() {
      super();
      GuiGlobal.setHitSortFileComponent(comp_);
      GuiGlobal.setEnabledHitSortFileComponent(false);
   }
   public void actionPerformed(ActionEvent e) {

      GuiGlobal.setHitSortFileName(((HitSortFilePanel)comp_).getSelectedHitSortFileName());
      System.out.println("HitsortFileName="+GuiGlobal.getHitSortFileName());
   }

   public Component getToolbarPresenter() {
       GuiGlobal.setEnabledHitSortFileComponent(false);
      return comp_;
   }

   
}
