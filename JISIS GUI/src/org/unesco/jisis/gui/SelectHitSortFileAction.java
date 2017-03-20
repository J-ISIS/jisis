/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;


import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.actions.Presenter;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;


public final class SelectHitSortFileAction extends AbstractAction implements Presenter.Toolbar  {

   Component comp_ = new HitSortFilePanel();

   public SelectHitSortFileAction() {
      super();
      GuiGlobal.setHitSortFileComponent(comp_);
      GuiGlobal.setEnabledHitSortFileComponent(false);
   }
   @Override
   public void actionPerformed(ActionEvent e) {

      GuiGlobal.setHitSortFileName(((HitSortFilePanel)comp_).getSelectedHitSortFileName());
      System.out.println("HitsortFileName="+GuiGlobal.getHitSortFileName());
   }

   @Override
   public Component getToolbarPresenter() {
       GuiGlobal.setEnabledHitSortFileComponent(false);
      return comp_;
   }

   
}
