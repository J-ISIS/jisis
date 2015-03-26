/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.io.Serializable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.record.IRecord;

/**
 *
 * @author jcd
 */
// Class for table unit
class FieldData implements Serializable {

   protected int tag_;
   protected String description_;
   protected int type_;
   protected Object content_;



   protected String defaultValue_;
   protected String displayControl_;
   protected String helpMsg_;
   protected String pickList_;
   protected String valFormat_;

   public FieldData(WorksheetDef.WorksheetField wksField, FieldDefinitionTable.FieldDefinition fdtEntry) {
      description_ = wksField.getDescription();
      displayControl_ = wksField.getDisplayControl();
      helpMsg_ = wksField.getHelpMessage();
      pickList_ = wksField.getPickList();
      tag_ = wksField.getTag();
      valFormat_ = wksField.getValidationFormat();

      type_ = fdtEntry.getType();

   }
    public Object getContent() {
      return content_;
   }

   public void setContent(Object content) {
      content_ = content;
   }

   public String getDescription() {
      return description_;
   }

   public void setDescription(String description) {
      description_ = description;
   }

   public int getTag() {
      return tag_;
   }

   public void setTag(int tag) {
      tag_ = tag;
   }

   public int getType() {
      return type_;
   }

   public void setType(int type) {
      type_ = type;
   }
}
