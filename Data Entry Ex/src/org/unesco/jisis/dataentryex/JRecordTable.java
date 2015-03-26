/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.record.IRecord;

/**
 *
 * @author jcd
 *
 * A table for record data entry and editing
 */
public class JRecordTable {

   static final int ACTION_NEW = 0;
   static final int ACTION_OPEN = 1;
   static final int ACTION_SAVE = 2;
   static final int ACTION_ADDROW = 3;
   static final int ACTION_DELROW = 4;
   JTable recordTable_;
   RecordTableDataModel model_;
   String m_sCurrDir = "";

   public JRecordTable(IDatabase db, WorksheetDef wks) {


      model_ = new RecordTableDataModel(db, wks);
      recordTable_ = new JTable();
      recordTable_.setAutoCreateColumnsFromModel(false);

     
      recordTable_.setModel(model_);

      createRecordTableColumns();


   }

   private void createRecordTableColumns() {
      for (int i = 0; i < model_.getColumnCount(); i++) {
         int w = model_.recordColumns[i].width_;
         TableColumn tc = new TableColumn(i, w, null, null);
         tc.setHeaderValue(model_.recordColumns[i].title_);

         recordTable_.addColumn(tc);
      }
   }

   public Component getAwtComponent() {
      return recordTable_;
   }

   public JTable getJTable() {
      return recordTable_;
   }

   protected void makeAction(int commandID) {
      switch (commandID) {
         case ACTION_NEW:
            recordTable_.editingStopped(null);
            model_.setDefaultData();
            recordTable_.tableChanged(
                    new TableModelEvent(model_));
            recordTable_.repaint();
            break;

         case ACTION_OPEN:
            try {
               recordTable_.editingStopped(null);
//                                FileDialog openDlg = new FileDialog(this,
//                                        "Open Table File", FileDialog.LOAD);
//                                openDlg.setFile("*.video");
//                                openDlg.setDirectory(m_sCurrDir);
//                                openDlg.show();
//                                // Verify selection
//                                String sFileName = openDlg.getFile();
//                                if (sFileName == null) // No selection
//                                        return;
//                                m_sCurrDir = openDlg.getDirectory();
//                                File fInput = new File(m_sCurrDir, sFileName);
//                                if (!fInput.exists())
//                                        return;
//
//                                FileInputStream fStream =
//                                        new FileInputStream(fInput);
//                                ObjectInput  stream  =
//                                        new  ObjectInputStream(fStream);
//                                Object obj = stream.readObject();
//                                stream.close();
//                                fStream.close();
//                                if (obj instanceof Vector)
//                                {
//                                        m_data.setData( (Vector)obj );
//                                        m_table.tableChanged(
//                                                new TableModelEvent(m_data));
//                                        m_table.repaint();
//                                }
            } catch (Exception e) {
               System.err.println("Read error: " + e.getMessage());
            }

            break;

         case ACTION_SAVE:
//                        try
//                        {
//                                m_table.editingStopped(null);
//                                FileDialog saveDlg = new FileDialog(this,
//                                        "Save Table File", FileDialog.SAVE);
//                                saveDlg.setFile("*.video");
//                                saveDlg.setDirectory(m_sCurrDir);
//                                saveDlg.show();
//                                // Verify selection
//                                String sFileName = saveDlg.getFile();
//                                if (sFileName == null) // No selection
//                                        return;
//                                m_sCurrDir = saveDlg.getDirectory();
//                                File fOutput = new File(m_sCurrDir, sFileName);
//
//                                FileOutputStream fStream =
//                                        new FileOutputStream(fOutput);
//                                ObjectOutput  stream  =
//                                        new  ObjectOutputStream(fStream);
//                                stream.writeObject(m_data.getData());
//                                stream.flush();
//                                stream.close();
//                                fStream.close();
//                        }
//                        catch (IOException e)
//                        {
//                                e.printStackTrace();
//                                System.err.println("Save error: "+e.getMessage());
//                        }
            break;

         case ACTION_ADDROW:
            int m = recordTable_.getSelectedRow();
            if (m < 0) {
               m = 0;
            }
            model_.addNewRow(m);
            recordTable_.tableChanged(
                    new TableModelEvent(model_, m, m, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
            break;

         case ACTION_DELROW:
            int n = recordTable_.getSelectedRow();
            if (model_.deleteRow(n)) {
               recordTable_.tableChanged(
                       new TableModelEvent(model_, n, n, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
            }
            break;
      }
   }

   void setData(IRecord rec) {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   // Action adapter
   class ButtonAdapter implements ActionListener {
      // Holds command's ID

      private int m_commandID;

      ButtonAdapter(int commandID) {
         m_commandID = commandID;
      }

      public void actionPerformed(ActionEvent e) {
         makeAction(m_commandID);
      }
   }
}       // end class MyTable

class MyChoiceRenderer extends DefaultTableCellRenderer {

   String m_choices[];

   public MyChoiceRenderer(String choices[]) {
      super();
      m_choices = choices;

   }

   public Component getTableCellRendererComponent(JTable table,
           Object value, boolean isSelected, int rowIndex, int colIndex) {
      if (value == null || !(value instanceof Integer)) {
         return this;    //Pavel 11/03/98
      }
      setForeground(isSelected ? Color.white : Color.black);
      setBackground(isSelected ? new Color(0, 0, 128) : Color.white);

      int index = ((Integer) value).intValue();
      if (index < 0 || index >= m_choices.length) {
         setText("");
      } else {
         setText(m_choices[index]);
      }
      return this;
   }

   protected void setValue(Object value) {
      if (value != null && (value instanceof Integer)) {
         int index = ((Integer) value).intValue();
         if (index < 0 || index >= m_choices.length) {
            setText("");
         } else {
            setText(m_choices[index]);
         }
      }
   }
}    // end class  MyChoiceRenderer

class MyChoiceEditor implements TableCellEditor {

   String m_choices[];
   JComboBox m_combo;
   List m_listeners = new ArrayList();

   public MyChoiceEditor(String choices[]) {
      m_choices = choices;
      m_combo = new JComboBox();
      for (int k = 0; k < m_choices.length; k++) {
         m_combo.addItem(m_choices[k]);
      }
   }

   // Methods from TableCellEditor interface
   public Component getTableCellEditorComponent(JTable table,
           Object value, boolean isSelected, int rowIndex, int colIndex) {
      if (value == null || !(value instanceof Integer)) {
         return m_combo;
      }

      int index = ((Integer) value).intValue();
      if (index >= 0 && index < m_choices.length) {
         m_combo.setSelectedIndex(index);
      } else {
         m_combo.setSelectedItem("");
      }
      return m_combo;
   }

   // Methods from CellEditor interface
   public void addCellEditorListener(CellEditorListener l) {
      m_listeners.add(l);
   }

   public void removeCellEditorListener(CellEditorListener l) {
      m_listeners.remove(l);
   }

   public Component getCellEditorComponent() {
      return m_combo;
   }

   public Object getCellEditorValue() {
      return new Integer(m_combo.getSelectedIndex());
   }

   public boolean isCellEditable(EventObject ev) {
      return true;
   }

   public boolean shouldSelectCell(EventObject ev) {
      return true;
   }

   public boolean stopCellEditing() {
      for (int k = 0; k < m_listeners.size(); k++) {
         CellEditorListener l = (CellEditorListener) m_listeners.get(k);
         l.editingStopped(new ChangeEvent(this));
      }
      return true;
   }

   public void cancelCellEditing() {
      for (int k = 0; k < m_listeners.size(); k++) {
         CellEditorListener l = (CellEditorListener) m_listeners.get(k);
         l.editingCanceled(new ChangeEvent(this));
      }
   }
}    // end class  MyChoiceEditor

