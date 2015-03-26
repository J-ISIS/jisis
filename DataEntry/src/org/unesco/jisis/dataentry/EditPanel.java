/*
 * EditPanel.java
 *
 * Created on July 12, 2006, 7:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentry;


import java.awt.*;
import java.util.List;
import javax.swing.JPanel;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.picklist.ValidationData;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.jisiscore.client.GuiGlobal;


/**
 *
 * @author rustam
 */
public class EditPanel extends JPanel {

   private ComponentOrientation orientation_ = ComponentOrientation.LEFT_TO_RIGHT;
   private IDatabase db_  = null;
   private FieldDefinitionTable fdt_ = null;
   private WorksheetDef wks_ = null;
   private boolean showEmptyFields_ = true;
   private List<PickListData> pickListDataList_;
   
   private List<ValidationData> validationDataList_;
   
    
    /** Creates a new instance of EditPanel */
    public EditPanel(IDatabase db, FieldDefinitionTable fdt, WorksheetDef wks,
             List<PickListData> pickListDataList,
             List<ValidationData> validationDataList,
             IRecord rec, boolean showEmptyFields) {

        db_  = db;
        fdt_ = fdt;
        wks_ = wks;
        pickListDataList_   = pickListDataList;
        validationDataList_ = validationDataList;
        showEmptyFields_    = showEmptyFields;
        redraw(rec);
    }

   

    public void setOrientation(ComponentOrientation orientation) {

        orientation_ = orientation;
        JPanel editPanel = (JPanel) this.getComponent(0);
        int ncomponents = editPanel.getComponentCount();
        for (int i = 0; i < ncomponents; ++i) {
            Component c = editPanel.getComponent(i);
            if (c instanceof EditEntry) {
                EditEntry ee = (EditEntry) c;
                ee.setOrientation(orientation);
            }
        }
    }

     private String fieldDefaultValue(String defaultValue) {
      try {

         if ((defaultValue != null) && (defaultValue.length() > 0)) {
            ISISFormatter formatter = ISISFormatter.getFormatter(defaultValue);
             if (formatter == null) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return null;
             } else if (formatter.hasParsingError()) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return null;
             }
           IRecord record = Record.createRecord();
            formatter.setRecord(db_, record);
            formatter.eval();
            String result = formatter.getText();

            return result;

         } else {
            return null;
         }
      } catch (RuntimeException re) {
         new DefaultFormattingException(re.getMessage()).displayWarning();
         return null;
      }
   }

    
    private void redraw(IRecord rec) {

        try {
          //Set the layout for EditPanel
          this.setLayout(new FlowLayout(FlowLayout.LEFT));
          // Create the editor panael
          JPanel editorPanel = new JPanel();
          editorPanel.setLayout(new GridBagLayout());
          GridBagConstraints gbc = new GridBagConstraints();
          gbc.insets = new Insets(0, 0, 10, 0);
          gbc.fill = GridBagConstraints.HORIZONTAL;
          // Get number of field in current worksheet  
          int fieldCount = wks_.getFieldsCount();
          for (int i = 0; i < fieldCount; i++) {
             WorksheetDef.WorksheetField wf = wks_.getFieldByIndex(i);
             // Note: an empty Field with 0 occurrences is created in the record
             // if the field is not present in the record
             IField f = rec.getField(wf.getTag());
             int type = fdt_.getFieldByTag(wf.getTag()).getType();
             f.setType(type);
             if (rec.getMfn() == 0) {
                // This is a new record, inject the default value in the
                // field if any
//                if (wf.getDefaultValue() != null) {
//                   String defaultValue = wf.getDefaultValue();
//                   // Execute the PFT
//                   String value = fieldDefaultValue(defaultValue);
//                   if (value != null) {
//                      f.setFieldValue(value);
//                   }
//                }
             } else if (!showEmptyFields_ && f.isEmpty()) {
                continue;

             }
             // Note: we send a reference to the field in the record. If the
             // field is edited, it will be updated when a field occurrence 
             // loose the focus
             // 
             PickListData pickListData = null;
             for (PickListData pickList : pickListDataList_) {
                if (Integer.valueOf(pickList.getTag()) == wf.getTag()) {
                   pickListData = pickList;
                   break;
                }
             }
             EditEntry ee = new EditEntry(wks_, wf, f, pickListData, rec, db_, fdt_);
             gbc.gridy++;
             editorPanel.add(ee, gbc);
          }
          this.add(editorPanel);
       } catch (DbException ex) {
          new GeneralDatabaseException(ex).displayWarning();
       }
    }

    public void refresh() {

        JPanel editPanel = (JPanel) this.getComponent(0);
        int ncomponents = editPanel.getComponentCount();
        for (int i = 0; i < ncomponents; ++i) {
            Component c = editPanel.getComponent(i);
            if (c instanceof EditEntry) {
                EditEntry ee = (EditEntry) c;
                ee.refresh();
            }
        }
    }
    
     public boolean isRecordModified() {

        boolean edited = false;
        JPanel editPanel = (JPanel) this.getComponent(0);
        int ncomponents = editPanel.getComponentCount();
        for (int i = 0; i < ncomponents; ++i) {
            Component c = editPanel.getComponent(i);
            if (c instanceof EditEntry) {
                EditEntry ee = (EditEntry) c;
                if (ee.isModified()) {
                   return true;
                }
            }
        }
        return edited;
    }

    void applyFont(Font font) {

        JPanel editPanel = (JPanel) this.getComponent(0);
        int ncomponents = editPanel.getComponentCount();
        for (int i = 0; i < ncomponents; ++i) {
            Component c = editPanel.getComponent(i);
            if (c instanceof EditEntry) {
                EditEntry ee = (EditEntry) c;
                ee.setJTextPanetFont(font);
            }
        }
    }

    
}
