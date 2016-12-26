/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.picklist.ValidationData;
import org.unesco.jisis.corelib.record.IRecord;

/**
 *
 * @author jc Dauphin
 */
public class FieldDialog extends JDialog {

   private int dialogStatus_;
   
   FieldDataEntryPanel fieldDataEntryPanel_;

   public static int VALIDATE = 0;
   public static int CANCEL = 1;
   public static int HELP = 2;

   public FieldDialog(DataEntryTopComponent topComponent,
           IDatabase db,
           WorksheetDef.WorksheetField wksField,
           IRecord record,
           List<PickListData> pickListDataList,
           List<ValidationData> validationDataList) {
      super();
      this.setMinimumSize(new Dimension(1000, 500));

      setupUI(topComponent,
              db,
              wksField,
              record,
              pickListDataList,
              validationDataList);

   }

   private void setupUI(DataEntryTopComponent topComponent,
           IDatabase db,
           WorksheetDef.WorksheetField wksField,
           IRecord record,
           List<PickListData> pickListDataList,
           List<ValidationData> validationDataList) {
      JPanel contentPanel = new JPanel(new MigLayout());
      fieldDataEntryPanel_  = 
              new FieldDataEntryPanel(topComponent, db, wksField, record, pickListDataList, validationDataList);
      //panel.setPreferredSize(new Dimension(1000, 480));
      contentPanel.add(fieldDataEntryPanel_, "grow, pushx, pushy, wrap");

      JButton validateBttn = new JButton("Validate");
      validateBttn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            dialogStatus_ = VALIDATE;
            dispose();
         }
      });

      //tag identifies the type of button
      contentPanel.add(validateBttn, "tag ok, span, split 3, sizegroup bttn");

      JButton cancelBttn = new JButton("Cancel");

      cancelBttn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            dialogStatus_ = CANCEL;
            dispose();
         }
      });

      //sizegroups set all members to the size of the biggest member
      contentPanel.add(cancelBttn, "tag cancel, sizegroup bttn");
//
//      JButton helpBttn = new JButton("Help");
//      helpBttn.addActionListener(new ActionListener() {
//         @Override
//         public void actionPerformed(ActionEvent e) {
//            dialogStatus_ = HELP;
//         }
//      });
//      contentPanel.add(helpBttn, "tag help, sizegroup bttn");

      
      setContentPane(contentPanel);

     
   }

   public FieldDialog(Frame mf, String title, boolean modal,
           DataEntryTopComponent topComponent,
           IDatabase db,
           IRecord record,
           WorksheetDef.WorksheetField wksField,
           List<PickListData> pickListDataList,
           List<ValidationData> validationDataList) {
      super(mf, title, modal);
      this.setSize(300, 200);

      setupUI(topComponent,
              db,
              wksField,
              record,
              pickListDataList,
              validationDataList);

      this.setVisible(true);
   }
   
   public int getDialogStatus() {
      return dialogStatus_;
   }
   
   public Object getFieldValue() {
      if (dialogStatus_ == VALIDATE){
         return fieldDataEntryPanel_.getFieldValue();
      }
      return null;
   }
}
