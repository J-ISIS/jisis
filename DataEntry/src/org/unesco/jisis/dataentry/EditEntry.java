/*
 * EditEntry.java
 *
 * Created on July 12, 2006, 8:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



package org.unesco.jisis.dataentry;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.FormattingException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.gui.ListSelectorDialog;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;


/**
 *
 * @author rustam
 * 
 * Focus events are fired whenever a component gains or loses the keyboard focus.
 * This is true whether the change in focus occurs through the mouse, the 
 * keyboard, or programmatically.
 * 
 * An EditEntry instance is a FocusListener on the RepeatableField objects
 */
public class EditEntry extends JPanel implements ActionListener, FocusListener {
   static final String                 ADD_PATH    = "org/unesco/jisis/dataentry/plus.gif";
   static final String                 DEL_PATH    = "org/unesco/jisis/dataentry/minus.gif";
   static final String                 PICK_LIST_PATH    = "org/unesco/jisis/dataentry/PickList.png";
   private IField                      fld_        = null;
   private WorksheetDef.WorksheetField wksFld_     = null;
   private WorksheetDef                wksDef_     = null;
   private IRecord                     rec_        = null;
   private IDatabase                   db_         = null;
   private FieldDefinitionTable        fdt_        = null;
   private Object                      fieldValue_ = null;
   private List<RepeatableField> fieldEntries_   = null;
   private PickListData pickListData_;
   private ComponentOrientation orientation_ = ComponentOrientation.LEFT_TO_RIGHT;
   private boolean recordModified = false;

   /** Creates a new instance of EditEntry */
   public EditEntry(WorksheetDef wd, WorksheetDef.WorksheetField wf, IField f, 
                    PickListData pickListData, IRecord rec,
                    IDatabase db, FieldDefinitionTable fdt) {
      fld_    = f;
      wksFld_ = wf;
      wksDef_ = wd;
      pickListData_ = pickListData;
      rec_    = rec;
      db_     = db;
      fdt_    = fdt;
      //FieldFactory.setDatabase(db);
      fieldEntries_ = new ArrayList<RepeatableField>();
      recordModified = false;
      redraw();
   }

   public void actionPerformed(ActionEvent e) {
      String actCmd = e.getActionCommand();
      if (actCmd.equals("actDel")) {
         try {
            DeleteButton de = (DeleteButton) e.getSource();
            if (fld_.getOccurrenceCount() > 0) {
               fld_.removeOccurrence(de.getID());
               recordModified = true;
               redraw();
            }
         } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
         }
         this.applyComponentOrientation(orientation_);
         // new NotImplemented().displayWarning();
      } else if (actCmd.equals("actAdd")) {
         try {
            if (fld_.getType() == Global.FIELD_TYPE_BLOB) {
               RepeatableField rp = new RepeatableField(0);
               Object value = rp.getValue();
               fld_.setOccurrence(fld_.getOccurrenceCount(), (byte[]) value);
            } else {
               String val = "";
               if (wksFld_.getDefaultValue() != null) {
                  String defaultValue = wksFld_.getDefaultValue();
                  // Execute the PFT
                  String value = fieldDefaultValue(defaultValue);
                  if (value != null) {
                     val = value;
                  }
               }

               fld_.setOccurrence(fld_.getOccurrenceCount(), val);
            }
            recordModified = true;
            redraw();
            this.applyComponentOrientation(orientation_);
         } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
         }
      } else if (actCmd.equals("actPickList")) {
         if (pickListData_ != null) {
            List<String> labels = pickListData_.getLabels();
            List<String> codes = pickListData_.getCodes();
            JXList jxList = new JXList((String[]) labels.toArray(new String[labels.size()]));
            ColorHighlighter colorHighlighter = new ColorHighlighter( HighlightPredicate.ROLLOVER_ROW, Color.CYAN, Color.WHITE); 
            jxList.addHighlighter(colorHighlighter);
            jxList.setRolloverEnabled(true);
            if (pickListData_.isMultiChoice()) {
               jxList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            } else {
               jxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            final ListSelectorDialog jd = new ListSelectorDialog(WindowManager.getDefault().getMainWindow(),
                    pickListData_.getDialogTitle(), jxList);
             jd.setLocationRelativeTo(null);
            int result = jd.showDialog();
            if (result == ListSelectorDialog.APPROVE_OPTION) {
               try {
                  int[] selected = jxList.getSelectedIndices();
                  PickListButton pickListButton = (PickListButton) e.getSource();
                  StringBuilder sb = new StringBuilder();
                 
                  if (pickListData_.isRepeat()) {
                     // Build a new field occurrence from each selected item 
                     for (int i = 0; i < selected.length; i++) {
                        sb = new StringBuilder();
                        if (pickListData_.isLtGt()) {
                           sb.append("<");
                        } else if (pickListData_.isSlashSlash()) {
                            sb.append("/");
                        }
                        if (pickListData_.isFirstDescribe()) {
                           // The first is what the user sees on the list. 
                           // The second is what it will be really inserted in the
                           // field. This is useful to mask codes with 
                           // human-readable descriptions.
                           sb.append(codes.get(selected[i]));
                        } else {
                           sb.append(labels.get(selected[i]));
                        }
                         if (pickListData_.isLtGt()) {
                           sb.append(">");
                        } else if (pickListData_.isSlashSlash()) {
                            sb.append("/");
                        }
                        Object obj = fld_.getOccurrenceValue(pickListButton.getID());
                        boolean startOnCurrentOccurrence = false;
                        if (obj == null){
                           startOnCurrentOccurrence = true;
                        } else if (obj instanceof String) {
                           String s = (String) obj;
                           if (s.equals("") || s.length() == 0 || s.trim().length() == 0) {
                              startOnCurrentOccurrence = true;
                           }
                        }
                        if (startOnCurrentOccurrence) {
                           // Current occurrence is empty thus start from there
                           fld_.setOccurrence(pickListButton.getID(), sb.toString());
                        } else {
                           fld_.setOccurrence(fld_.getOccurrenceCount(), sb.toString());
                        }
                     }
                  } else {
                     // We work on the current occurrence
                     if (pickListData_.isAdd()) {
                        //New selected items' text will be added to the text already in the field.
                        Object obj = fld_.getOccurrenceValue(pickListButton.getID());
                        if (obj == null ||  ((String) obj).length()==0) {
                           // Do nothing
                        } else {
                           // Add a blank
                           sb.append((String) obj).append(" ");
                        }
                     
                     }
                     for (int i = 0; i < selected.length; i++) {
                         if (pickListData_.isLtGt()) {
                           sb.append("<");
                        } else if (pickListData_.isSlashSlash()) {
                            sb.append("/");
                        }
                        if (pickListData_.isFirstDescribe()) {
                           // The first is what the user sees on the list. 
                           // The second is what it will be really inserted in the
                           // field. This is useful to mask codes with 
                           // human-readable descriptions.
                           sb.append(codes.get(selected[i]));
                        } else {
                           sb.append(labels.get(selected[i]));
                        }
                         if (pickListData_.isLtGt()) {
                           sb.append(">");
                        } else if (pickListData_.isSlashSlash()) {
                            sb.append("/");
                        }
                        if (i < selected.length - 1) {
                           sb.append(" ");
                        }
                     }
                     fld_.setOccurrence(pickListButton.getID(), sb.toString());
                  }
                   recordModified = true;
                  redraw();
               } catch (DbException ex) {
                  Exceptions.printStackTrace(ex);
               }

            } else {
               System.err.println("Cancelled");
            }
         }

      }
   }

   public void setOrientation(ComponentOrientation orientation) {
      orientation_ = orientation;
   }

   void setJTextPanetFont(Font font) {
      int n = fieldEntries_.size();

      for (int i = 0; i < n; ++i) {
         RepeatableField rf = fieldEntries_.get(i);
         rf.setJTextPaneFont(font);
         rf.updateUI();
      }

   }

   private boolean fieldValidation() {
      try {
         String valFormat = wksFld_.getValidationFormat();
         if ((valFormat != null) && (valFormat.length() > 0)) {
            ISISFormatter formatter = ISISFormatter.getFormatter(valFormat);
            if (formatter == null) {
               GuiGlobal.output(ISISFormatter.getParsingError());
               return false;
            } else if (formatter.hasParsingError()) {
               GuiGlobal.output(ISISFormatter.getParsingError());
               return false;
            }

            formatter.setRecord(db_, rec_);
            formatter.eval();
            String result = formatter.getText();
            if (result.length() > 0) {
               new ValidationFailedException(result).displayWarning();
               return false;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } catch (RuntimeException re) {
         new FormattingException(re.getMessage()).displayWarning();
         return false;
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

   /**
    * Called just after the listened-to component gets the focus.
    * @param e
    */
   public void focusGained(FocusEvent e) {
      if (fld_.hasOccurrences()) {
         RepeatableField source = (RepeatableField) e.getSource();
         fieldValue_ = source.getValue();
      } else {
         JTextPane source = (JTextPane) e.getSource();
         fieldValue_ = source.getText();
      }
   }
   /**
    * Called just after the listened-to component loses the focus.
    * @param e 
    */

   public void focusLost(FocusEvent e) {
      try {
         if (fld_.hasOccurrences()) {
            RepeatableField source = (RepeatableField) e.getSource();
            switch (source.getType()) {
               case Global.FIELD_TYPE_ALPHABETIC:
               case Global.FIELD_TYPE_ALPHANUMERIC:
               case Global.FIELD_TYPE_NUMERIC:
               case Global.FIELD_TYPE_PATTERN:
               case Global.FIELD_TYPE_STRING:
               case Global.FIELD_TYPE_BOOLEAN:
               case Global.FIELD_TYPE_CHAR:
               case Global.FIELD_TYPE_BYTE:
               case Global.FIELD_TYPE_SHORT:
               case Global.FIELD_TYPE_INT:
               case Global.FIELD_TYPE_FLOAT:
               case Global.FIELD_TYPE_LONG:
               case Global.FIELD_TYPE_DOUBLE:
               case Global.FIELD_TYPE_DATE:
               case Global.FIELD_TYPE_TIME:
               case Global.FIELD_TYPE_DOC:
               case Global.FIELD_TYPE_URL:   

                  String newValue = source.getText();
                  //if (!fieldValue_.equals(newValue) && fieldValidation() && recordValidation()) {
                     fld_.setOccurrence(source.getID(), source.getText());
                  //}
                  break;
               case Global.FIELD_TYPE_BLOB:

                  fld_.setOccurrence(source.getID(), source.getValue());
                  break;
            }
         } else {
            // No Occurrences
            JTextArea source = (JTextArea) e.getSource();
            String newValue = source.getText();
            if (!fieldValue_.equals(newValue) && fieldValidation() && recordValidation()) {
               fld_.setFieldValue(source.getText());
            }
         }
      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }
   }

   private boolean recordValidation() {
      try {
         String valFormat = wksDef_.getRecordValidationFormat();
         if ((valFormat != null) && (valFormat.length() > 0)) {
            ISISFormatter formatter = ISISFormatter.getFormatter(valFormat);
             if (formatter == null) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return false;
             } else if (formatter.hasParsingError()) {
                 GuiGlobal.output(ISISFormatter.getParsingError());
                 return false;
             }
            
            formatter.setRecord(db_, rec_);
            formatter.eval();
            String result = formatter.getText();
            if (result.length() > 0) {
               new ValidationFailedException(result).displayWarning();
               return false;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } catch (RuntimeException re) {
         new FormattingException(re.getMessage()).displayWarning();
         return false;
      }
   }

   private void redraw() {
      // fieldEntries will contain a List of RepeatableField with data for this
      // specific field
      fieldEntries_.clear();
      ClientDatabaseProxy database = (ClientDatabaseProxy) db_;
       //------------------------------------------
      // Build an array of the occurrences values
      //-------------------------------------------
      Object values[] = { "" };
      if (fld_ == null || fld_.getOccurrenceCount() == 0) {
         // The field is empty, just one occurrence
         values[0] = "";
         if (wksFld_.getDefaultValue() != null && wksFld_.getDefaultValue().length()>0) {
            String defaultValue = wksFld_.getDefaultValue();
            // Execute the PFT
            String val = fieldDefaultValue(defaultValue);
            if (val != null) {
               values[0] = val;
               try {
                  fld_.setFieldValue(val);
               } catch (DbException ex) {
                  Exceptions.printStackTrace(ex);
               }
            }
         }

      } else {
         // We are updating a field that already contains value(s)
         if (!fld_.hasOccurrences()) {
            // Only one occurrence, put the data in the 1st value item
            values[0] = fld_.getFieldValue();
         } else {
            // More than one occurrence, get number of occurrences
            int      repCount = fld_.getOccurrenceCount();
            // Allocate an array of objects that will contain references to
            // the occurrence data
            Object[] repData  = new Object[repCount];
            for (int i = 0; i < repCount; i++) {
               repData[i] = fld_.getOccurrenceValue(i);
            }
            values = repData;
         }
      }
      int tag  = wksFld_.getTag();
      
      
      // Get the field type from the FDT
      FieldDefinition fieldDefinition = fdt_.getFieldByTag(tag);
      int type = fieldDefinition.getType();
      boolean isRepeatitive = fieldDefinition.isRepeatable();
      
      //---------------------------------------
      // The Field Data Entry Panel (EditEntry)
      //---------------------------------------
      // 1) Remove components from the JPanel container
      this.removeAll();
      // 2) Create a border around the field panel
      this.setBorder(BorderFactory.createEtchedBorder());
      /**
       *  The container has 2 components:
       *     - The field label on the left
       *     - The field occurrences panel on the right
       */
    
      // Create a grid bag layout manager instance
      this.setLayout(new GridBagLayout());
      // And an associate constraints object
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      // initialize the constraints parameters
      gridBagConstraints.gridx  = 0; // Column 0
      gridBagConstraints.gridy  = 0; // Row 0
      //gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
      // Get the worksheet Field label
      StringBuilder sb = new StringBuilder();
      sb.append("<html><b>").append(wksFld_.getDescription()).append("</b><br>")
                         .append("(").append(wksFld_.getTag()).append(")")
                         .append("</html>");
      JLabel fieldDesc = new JLabel(sb.toString());
      fieldDesc.setPreferredSize(new Dimension(150, 50));
      this.add(fieldDesc, gridBagConstraints);
      
     
      //-------------------------------------------
      // Build the Field occurences panel
      //-------------------------------------------
      // Create the panel to contain all occurrences
      JPanel fieldPanel = new JPanel();
      // panel.setPreferredSize(new Dimension(300, 25));
      fieldPanel.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      // initialize the constraints parameters
      // Grid cell location where the component will be placed
      gbc.gridy  = 1; // Row 1
      gbc.gridx  = 1; // Column 1
      gbc.anchor = GridBagConstraints.FIRST_LINE_START;
      gbc.fill   = GridBagConstraints.NONE;
      // The insets constraint adds an invisible exterior padding around the
      // component
      gbc.insets = new Insets(5, 5, 5, 5);
      
      Dimension buttonDimension = new Dimension(20, 20);
     
      switch (type) {
         case Global.FIELD_TYPE_ALPHABETIC:
         case Global.FIELD_TYPE_ALPHANUMERIC:
         case Global.FIELD_TYPE_NUMERIC:
         case Global.FIELD_TYPE_PATTERN:
         case Global.FIELD_TYPE_STRING:
         case Global.FIELD_TYPE_BOOLEAN:
         case Global.FIELD_TYPE_CHAR:
         case Global.FIELD_TYPE_BYTE:
         case Global.FIELD_TYPE_SHORT:
         case Global.FIELD_TYPE_INT:
         case Global.FIELD_TYPE_FLOAT:
         case Global.FIELD_TYPE_LONG:
         case Global.FIELD_TYPE_DOUBLE:
         case Global.FIELD_TYPE_DATE:
         case Global.FIELD_TYPE_TIME:
         case Global.FIELD_TYPE_DOC:
         case Global.FIELD_TYPE_URL:
            // Loop on the field occurence data
            for (int i = 0; i < values.length; i++) {
               // We create a RepeatableField object
               RepeatableField repeatableField = new RepeatableField((String) values[i], i);
               // Change the font if needed
               if (database.getDisplayFont() != null) {
                  repeatableField.setJTextPaneFont(database.getDisplayFont());
               }
               fieldEntries_.add(repeatableField);
//            TextDataEntryDocument doc = new TextDataEntryDocument();
//            doc.addDocumentListener(fieldEntry);
//            fieldEntry.setDocument(doc);
//            fieldEntry.setValue((String)value[i]);
               repeatableField.addFocusListener(this);
               // fieldEntry.setPreferredSize(new Dimension(325, 100));
               // Add a scroll bar to the JTextPane derived Repeatable component
               JScrollPane scrollPane = new JScrollPane(repeatableField);
               scrollPane.setPreferredSize(new Dimension(650, 50));
               // fieldEntry.setPreferredSize(new Dimension(100, 50));
               gbc.gridx = 1; // Column 1 for the occurrence
               // Add the scrollable JTextPane derived Repeatable component
               fieldPanel.add(scrollPane /*
                        * fieldEntry
                        */, gbc);
               if (isRepeatitive && fld_.getOccurrenceCount()>1 && i>0) {
                  // Create the Delete (-) button
                  DeleteButton btnDel = new DeleteButton(i);
                  btnDel.setIcon(new ImageIcon(ImageUtilities.loadImage(DEL_PATH, true)));
                  btnDel.setActionCommand("actDel");
                  btnDel.addActionListener(this);
                  btnDel.setPreferredSize(buttonDimension);
                  btnDel.setToolTipText(NbBundle.getMessage(EditEntry.class, "MSG_Delete_Occurrence"));
                  gbc.gridx = 2; // Column 2 for the Delete button
                  fieldPanel.add(btnDel, gbc);
               } else {
                  gbc.gridx = 2; // Fill the gap of the Delete button
                  fieldPanel.add(Box.createHorizontalStrut(buttonDimension.width), gbc);
               }
               if (pickListData_!= null)  {

                  PickListButton btnPick = new PickListButton(i, tag);
                  btnPick.setIcon(new ImageIcon(ImageUtilities.loadImage(PICK_LIST_PATH, true)));
                  btnPick.setActionCommand("actPickList");
                  btnPick.addActionListener(this);
                  btnPick.setPreferredSize(buttonDimension);

                  btnPick.setToolTipText(NbBundle.getMessage(EditEntry.class, "MSG_Access_PickList"));

                  gbc.gridx = 0; // Column 0 for the PickList button
                  fieldPanel.add(btnPick, gbc);
                  if (pickListData_.isNoType()) {
                     repeatableField.setEditable(false);
                     repeatableField.setBackground(Color.LIGHT_GRAY);
                  }
               } else {
                  gbc.gridx = 0; // Fill the gap of the PickList button
                  fieldPanel.add(Box.createHorizontalStrut(buttonDimension.width), gbc);
               }

               gbc.gridy++; // Increment the row 
            }
            // Put the occurrence panel in column 3
            gridBagConstraints.gridx = 2;
            this.add(fieldPanel, gridBagConstraints);
         

            if (isRepeatitive) {              
               gridBagConstraints.gridwidth = 1;
               gridBagConstraints.gridy++;
               // Put the Add button at the same level than the Occurrence Panel
               gridBagConstraints.gridx = 2;  // Column
               // Create the Add(+) occurrence button
               JButton btnAdd = new JButton();
               btnAdd.setIcon(new ImageIcon(ImageUtilities.loadImage(ADD_PATH, true)));
               btnAdd.setActionCommand("actAdd");
               btnAdd.addActionListener(this);
               btnAdd.setPreferredSize(buttonDimension);

               btnAdd.setToolTipText(NbBundle.getMessage(EditEntry.class, "MSG_Add_Occurrence"));
               gridBagConstraints.anchor = GridBagConstraints.LINE_START;
               this.add(btnAdd, gridBagConstraints);
               gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
            }
             // Increment the row for the Help Message
            if ((wksFld_.getHelpMessage() != null ) && (wksFld_.getHelpMessage().length()>0))  {
               gridBagConstraints.gridy++;
               gridBagConstraints.gridx = 1;
               gridBagConstraints.gridwidth = 3; // Spans across 3 columns
               JLabel helpMsg = new JLabel(wksFld_.getHelpMessage());
               helpMsg.setPreferredSize(new Dimension(300, 50));
               this.add(helpMsg, gridBagConstraints);
               gridBagConstraints.gridwidth = 1;
            }
            break;
         case Global.FIELD_TYPE_BLOB:
            for (int i = 0; i < values.length; i++) {
               RepeatableField fieldEntry = new RepeatableField(i);
               if (database.getDisplayFont() != null) {
                  fieldEntry.setJTextPaneFont(database.getDisplayFont());
               }
               fieldEntries_.add(fieldEntry);
               fieldEntries_.add(fieldEntry);
               byte[] bytes = null;
               if (values[i] instanceof String) {
                  bytes = ((String) values[i]).getBytes();
               } else {
                  bytes = (byte[]) values[i];
               }
               fieldEntry.setValue(bytes);

               fieldEntry.addFocusListener(this);
               // fieldEntry.setPreferredSize(new Dimension(325, 100));
               JScrollPane scrollPane = new JScrollPane(fieldEntry);
               scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
               scrollPane.setPreferredSize(new Dimension(650, 250));
               // fieldEntry.setPreferredSize(new Dimension(100, 50));
               gbc.gridx = 1; // Column 1
               fieldPanel.add(scrollPane /*
                        * fieldEntry
                        */, gbc);
               DeleteButton btnDel = new DeleteButton(i);
               btnDel.setIcon(new ImageIcon(ImageUtilities.loadImage(DEL_PATH, true)));
               btnDel.setActionCommand("actDel");
               btnDel.addActionListener(this);
               btnDel.setPreferredSize(new Dimension(25, 25));
               gbc.gridx++;
               fieldPanel.add(btnDel, gbc);
               gbc.gridy++;
            }
            gridBagConstraints.gridx++;
            this.add(fieldPanel, gridBagConstraints);
            gridBagConstraints.gridy++;
            gridBagConstraints.gridx = 2;
            // Create the Add(+) occurrence button
            JButton btnAdd = new JButton();
            btnAdd.setIcon(new ImageIcon(ImageUtilities.loadImage(ADD_PATH, true)));
            btnAdd.setActionCommand("actAdd");
            btnAdd.addActionListener(this);
            btnAdd.setPreferredSize(new Dimension(25, 25));
            this.add(btnAdd, gridBagConstraints);
            break;
         default:
            JTextArea fieldEntry = new JTextArea();
            fieldEntry.setText((values == null)
                    ? ""
                    : values.toString());
            JScrollPane scrollPane = new JScrollPane(fieldEntry);
            // fieldEntry.setPreferredSize(new Dimension(350, 100));
            scrollPane.setPreferredSize(new Dimension(650, 100));
            fieldEntry.addFocusListener(this);
            gridBagConstraints.gridx++;
            this.add(scrollPane /*
                     * fieldEntry
                     */, gridBagConstraints);
            break;
      }
      this.updateUI();
   }

    public void refresh() {

      int ncomponents = this.getComponentCount();
      for (int i = 0 ; i < ncomponents ; ++i) {
         Component c = this.getComponent(i);

         if (c instanceof RepeatableField) {
            RepeatableField rf = (RepeatableField) c;
            rf.updateUI();
         }

      }
    }
    
    public boolean isModified() {
       
       for (RepeatableField repeatableField : fieldEntries_) {
         if (repeatableField.isModified()) return true;  
       }
       return (recordModified) ? true : false;
       
    }
}
