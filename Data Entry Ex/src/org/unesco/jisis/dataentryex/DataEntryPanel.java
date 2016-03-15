/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DataEntryPanel.java
 *
 * Created on 2 juin 2010, 17:27:39
 */
package org.unesco.jisis.dataentryex;

import org.unesco.jisis.jisisutil.history.ListSelectorDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.picklist.ValidationData;
import org.unesco.jisis.corelib.record.*;
import org.unesco.jisis.gui.*;
import org.unesco.jisis.jisisutils.gui.JTableScrolling;
import org.unesco.jisis.jisisutils.gui.OutlineUtil;

/**
 *
 * @author jcd
 */
public class DataEntryPanel extends javax.swing.JPanel implements CellEditorListener {

    static final int ACTION_NEW = 0;
    static final int ACTION_OPEN = 1;
    static final int ACTION_SAVE = 2;
    static final int ACTION_ADDROW = 3;
    static final int ACTION_DELROW = 4;

    protected String wksName_ = null;
    private WorksheetDef wks_ = null;
    private FieldDefinitionTable fdt_ = null;
    private Outline outline_;
    private DataEntryTreeModel treeModel_;
    private OutlineModel outlineModel_;
    private DataEntryRowModel rowModel_;
    private IRecord record_ = null;
    private IDatabase db_ = null;
    private OutlineTopComponent topComponent_ = null;

    String m_sCurrDir = "";

    protected JPopupMenu occurrencePopupMenu_;
    protected JPopupMenu subfieldPopupMenu_;
    protected Action expandAction_;
    protected TreePath clickedPath_;

    protected String cellInitialValue_ = null;
    
    protected int selectedRow_ = -1;
    protected int selectedColumn_ = -1;

    protected List<PickListData> pickListDataList_;
    protected List<ValidationData> validationDataList_;

    /**
     * Creates new form DataEntryPanel
     */
    private DataEntryPanel() {
        initComponents();

    }

    public DataEntryPanel(OutlineTopComponent topComponent, IDatabase db, WorksheetDef wks,
        List<PickListData> pickListDataList, List<ValidationData> validationDataList) {
        topComponent_ = topComponent;
        db_ = db;
        wks_ = wks;
        wksName_ = wks.getName();
        pickListDataList_ = pickListDataList;
        validationDataList_ = validationDataList;
        try {
            fdt_ = db.getFieldDefinitionTable();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }

        initComponents();

        // Set the Outline TreeTable
        treeModel_ = DataEntryTreeModel.makeTreeModelForEmptyRecord(db, wks,
            pickListDataList_, validationDataList_);
        rowModel_ = new DataEntryRowModel();

        outlineModel_ = DefaultOutlineModel.createOutlineModel(treeModel_, rowModel_);

        outline_ = new Outline() {

            @Override
            public boolean getScrollableTracksViewportHeight() {
                if (getParent() instanceof JViewport) {
                    return getParent().getHeight() > getPreferredSize().height;
                } else {
                    return false;
                }
            }

            @Override
            /**
             * Occupy the full width window
             */
            public boolean getScrollableTracksViewportWidth() {
                if (getParent() instanceof JViewport) {
                    return getParent().getWidth() > getPreferredSize().width;
                } else {
                    return false;
                }
            }

            @Override
            /**
             * Override to choose the blob celleditor in case the field occurrence is of blob type
             */
            public TableCellEditor getCellEditor(int row, int col) {
                // TableCellEditor for this cell
                TableCellEditor tce = super.getCellEditor(row, col);
                if (col == 4) {
                    // TableCellEditor for cells on Data column
                    DataEntryNode dataEntryNode = (DataEntryNode) outline_.getValueAt(row, 0);
                    Hashtable data = (Hashtable) dataEntryNode.getUserObject();
                    String nodeType = (String) data.get("type");
                    String fieldType = (String) data.get("fieldType");
                    // The selected node should be a data entry field node
                    if ("fieldNode".equals(nodeType)
                        && fieldType.equals(Global.fiedType(Global.FIELD_TYPE_BLOB))) {
                        // Change the TableCellEditor for BLOB
                        tce = getDataEntryActionEditor(row);

                    }
                }
                return tce;
            }
        };

        outline_.setRootVisible(true);
        outline_.setModel(outlineModel_);

        setDataEntryOutlineColumns();

        outlineScrollPane.setViewportView(outline_);
        outlineScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        DataEntryNode root = (DataEntryNode) treeModel_.getRoot();

        outline_.setRowSelectionInterval(0, 0);

        outline_.setRenderDataProvider(new RenderData());

        GuiUtils.TweakJTable(outline_);

        // List Selection Listeners
        outline_.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                     
//                    boolean en = outline.getSelectedRow() != -1 && tblAvail.getSelectedRow() != -1;
//                    btnAddField.setEnabled(en);
//                    btnRemoveField.setEnabled(outline.getSelectedRow() != -1 && outline.getRowCount() > 1);
//                    btnClearWks.setEnabled(outline.getRowCount() > 1);
                }
            });
        
        ListSelectionModel cellSelectionModel = outline_.getSelectionModel();
        cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (outline_.getSelectedRow() != -1) {
                        selectedRow_ = outline_.getSelectedRow();
                    }
                    if (outline_.getSelectedColumn() != -1) {
                        selectedColumn_ = outline_.getSelectedColumn();
                    }
                }

            }

        });

//      Action action = new AbstractAction() {
//
//         public void actionPerformed(ActionEvent e) {
//            TableCellListener tcl = (TableCellListener) e.getSource();
//            System.out.println("Row   : " + tcl.getRow());
//            System.out.println("Column: " + tcl.getColumn());
//            System.out.println("Old   : " + tcl.getOldValue());
//            System.out.println("New   : " + tcl.getNewValue());
//         }
//      };
//      TableCellListener tcl = new TableCellListener(outline_, action);
        buildPopupMenu();
        outline_.add(occurrencePopupMenu_);
        outline_.addMouseListener(new PopupTrigger());

        outline_.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent ev) {
//                outline.setSelectionBackground(focusSelectionBackground);
//                outline.setSelectionForeground(focusSelectionForeground);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent ev) {
//                outline.setSelectionBackground(SheetCell.getNoFocusSelectionBackground());
//                outline.setSelectionForeground(SheetCell.getNoFocusSelectionForeground());
            }

        });

        changeKeyProcessing();
    }

    /**
     * Change default key binding for the Data
     */
    private void changeKeyProcessing() {
        /*
         *  true if the editor should get the focus when keystrokes cause the editor to be activated
        */
        outline_.setSurrendersFocusOnKeystroke(true);
        /**
         * Be sure to save the data when the user has typed something into one of the text fields in 
         * the JTable, and then click outside (thus removing focus from the JTable)
         */
        outline_.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        Action nextRowAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Enter key was pressed, get current row and col
                int selectedRow = selectedRow_;
                int selectedCol = selectedColumn_;
                if (selectedCol != 4) {
                    // Return if we are not on data column
                    return;
                }
                // First, cancel current data cell editing so that data is committed
                if (outline_.isEditing()) {

                    outline_.getCellEditor(selectedRow, selectedCol).stopCellEditing();
                }
                // Get next editable data row 
                final int nextRow = getNextEditableRow();
                outline_.setRowSelectionInterval(nextRow, nextRow);
                outline_.editCellAt(nextRow, selectedCol);
                // Show the root node if we are on 1st field
                final int row1 = (selectedRow == outline_.getRowCount() - 1) ? 0 : nextRow - 1;
                final int row2 = (nextRow == outline_.getRowCount() - 1) ? nextRow : nextRow + 1;

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JTableScrolling.makeRowsVisible(outline_, row1, row2);

                    }
                });

//            Rectangle rect = outline_.getCellRect(visibleRow, column, true);
//            outline_.scrollRectToVisible(rect);
                // Get the component that is handling the editing session.
                Component editorComp = outline_.getEditorComponent();
            // editorComp is a JPanel with a JTextField and the Edit Button
                // We need to focus on the JTextField to get the cursor
                if (editorComp instanceof JPanel) {
                    for (Component c : ((JPanel) editorComp).getComponents()) {
                        if (c instanceof JTextField) {
                            c.requestFocusInWindow();
                            ((JTextField) c).setCaretPosition(0);
                        }
                    }

                }
            }
        };

        Action prevRowAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Enter key was pressed, get current row and col
                int selectedRow = selectedRow_;
                int selectedCol = selectedColumn_;
                if (selectedCol != 4) {
                    // Return if we are not on data column
                    return;
                }
                // First, cancel current data cell editing so that data is committed
                if (outline_.isEditing()) {

                    outline_.getCellEditor(selectedRow, selectedCol).stopCellEditing();
                }
                // Get next editable data row 
                final int prevRow = getPrevEditableRow();
                outline_.setRowSelectionInterval(prevRow, prevRow);
                outline_.editCellAt(prevRow, selectedCol);
                final int row1 = (prevRow <= 2) ? 0 : prevRow - 1;

                final int row2 = (prevRow == outline_.getRowCount() - 1) ? prevRow : prevRow + 1;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JTableScrolling.makeRowsVisible(outline_, row1, row2);

                    }
                });

//             Rectangle rect = outline_.getCellRect(visibleRow, column, true);
//            outline_.scrollRectToVisible(rect);
                // Get the component that is handling the editing session.
                Component editorComp = outline_.getEditorComponent();
            // editorComp is a JPanel with a JTextField and the Edit Button
                // We need to focus on the JTextField to get the cursor
                if (editorComp instanceof JPanel) {
                    for (Component c : ((JPanel) editorComp).getComponents()) {
                        if (c instanceof JTextField) {
                            c.requestFocusInWindow();
                            ((JTextField) c).setCaretPosition(0);
                        }
                    }

                }
            }
        };

        Action pickListAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Enter key was pressed, get current row and col
                int row = selectedRow_;
                int column = selectedColumn_;
                if (column != 4) {
                    // Return if we are not on data column
                    return;
                }
                if (!pickList()) {
                    return;
                }
                // First, cancel current data cell editing so that data is committed
                if (outline_.isEditing()) {

                    outline_.getCellEditor(row, column).stopCellEditing();
                }
                processPickList();
                outline_.setColumnSelectionInterval(4, 4);
                outline_.setRowSelectionInterval(row, row);
                // Get the component that is handling the editing session.
                outline_.editCellAt(row, column);
                Component editorComp = outline_.getEditorComponent();
            // editorComp is a JPanel with a JTextField and the Edit Button
                // We need to focus on the JTextField to get the cursor
                if (editorComp instanceof JPanel) {
                    for (Component c : ((JPanel) editorComp).getComponents()) {
                        if (c instanceof JTextField) {
                            JTextField jtextField = (JTextField) c;
                            jtextField.requestFocusInWindow();
                            jtextField.setCaretPosition(jtextField.getText().length());
                        }
                    }

                }
            }
        };

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
        outline_.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enterKey, "Enter");
        outline_.getActionMap().put("Enter", nextRowAction);

        KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        outline_.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(downKey, "down");
        outline_.getActionMap().put("down", nextRowAction);

        KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        outline_.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(upKey, "up");
        outline_.getActionMap().put("up", prevRowAction);

        KeyStroke f2Key = KeyStroke.getKeyStroke("F2");
        outline_.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(f2Key, "picklist");
        outline_.getActionMap().put("picklist", pickListAction);

    }

    /**
     * Listens for cells that has been edited. When a cell has been edited, this function will run.
     */
    @Override
    public void editingStopped(ChangeEvent e) {
        TableCellEditor editor = (TableCellEditor) e.getSource();
        Object obj = editor.getCellEditorValue();
        if (obj instanceof String) {
            String newValue = (String) obj;
            if (!newValue.isEmpty() && !newValue.equals(cellInitialValue_)) {
                topComponent_.setRecordChangedFlag(true);
                cellInitialValue_ = newValue;
                TreePath[] treeExpansion = OutlineUtil.saveExpansionState(outline_);
                treeModel_.fireStructureChanged();
                OutlineUtil.loadExpansionState(outline_, treeExpansion);
            }
        } else {
            // Take the blob reference address for comparison
            String newValue = obj.toString();
            if (!newValue.isEmpty() && !newValue.equals(cellInitialValue_)) {
                topComponent_.setRecordChangedFlag(true);
                TreePath[] treeExpansion = OutlineUtil.saveExpansionState(outline_);
                treeModel_.fireStructureChanged();
                OutlineUtil.loadExpansionState(outline_, treeExpansion);
            }

        }
    }

    /**
     * Listens for cells where editing has been canceled (cell data has not been changed).
     */
    @Override
    public void editingCanceled(ChangeEvent e) {
        System.out.println("Editing of a cell has been canceled.");
    }

    private boolean pickList() {

        DataEntryNode selNode = getSelectedNode();
        Hashtable map = (Hashtable) selNode.getUserObject();
        PickListData pickListData = (PickListData) map.get("pickList");
        return (pickListData == null) ? false : true;
    }

   //private static DataEntryDlgTableCellEditor dataEntryActionEditor = null;
    public TableCellEditor getDataEntryActionEditor(int row) {
        DataEntryDlgTableCellEditor dataEntryActionEditor = null;
        if (dataEntryActionEditor == null) {
            DefaultCellEditor editor = null;

            JTextField textField = new JTextField();
            textField.setBorder(BorderFactory.createEmptyBorder());
            if (Global.getApplicationFont() != null) {
                textField.setFont(Global.getApplicationFont());
            }
            editor = new DefaultCellEditor(textField) {

                @Override
                public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                    final Component c = super.getTableCellEditorComponent(
                        table,
                        value, // edit the text field of Cell
                        isSelected,
                        row,
                        column);
//               if (value instanceof String) {
//                  cellInitialValue_ = (String) value;
//
//               } else {
//                  cellInitialValue_ = value.toString();
//               }
                    //System.out.println("Initial vale="+value);
                    return c;
                }
            };
            editor.setClickCountToStart(1);
            editor.addCellEditorListener(this);
            dataEntryActionEditor = new DataEntryDlgTableCellEditor(editor);
            dataEntryActionEditor.addCellEditorListener(this);
        }

        return dataEntryActionEditor;
    }

    public void completeEdit() {
        Component co = outline_.getEditorComponent();
        if (co != null) {
            TableCellEditor tce = outline_.getCellEditor();
            tce.stopCellEditing();
        }
    }

    private void buildPopupMenu() {
        occurrencePopupMenu_ = new JPopupMenu();
        expandAction_ = new AbstractAction("Expand") {
            public void actionPerformed(ActionEvent e) {
                if (clickedPath_ == null) {
                    return;
                }
                if (outline_.isExpanded(clickedPath_)) {
                    outline_.collapsePath(clickedPath_);
                } else {
                    outline_.expandPath(clickedPath_);
                }
            }
        };
        occurrencePopupMenu_.add(expandAction_);
        occurrencePopupMenu_.addSeparator();

        Action addOccurrenceAction = new AbstractAction("Add Occurrence") {

            @Override
            public void actionPerformed(ActionEvent e) {
                outline_.repaint();
                DataEntryNode selNode = getSelectedNode();
                Hashtable data = (Hashtable) selNode.getUserObject();
                String nodeType = (String) data.get("type");
                // The selected node should be a data entry field node
                if (!("fieldNode".equals(nodeType))) {
                    return;
                }
                // Save Expansion state
                TreePath[] treeExpansion = OutlineUtil.saveExpansionState(outline_);
                int tag = Integer.valueOf((String) data.get("tag"));
                // Get the worksheet entry for this tag
                WorksheetDef.WorksheetField worksheetField = wks_.getFieldByTag(tag);
            // Build a new data entry field node with possibly subfield as
                // children. Data is empty.
                DataEntryNode newNode = treeModel_.buildTreeNodes(worksheetField, true);
                DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
                int index = treeModel_.getIndexOfChild(root, selNode);
                root.insert(newNode, index + 1);

                treeModel_.fireStructureChanged();
                OutlineUtil.loadExpansionState(outline_, treeExpansion);

            }
        };

        occurrencePopupMenu_.add(addOccurrenceAction);

        Action deleteOccurrenceAction = new AbstractAction("Delete Occurrence") {

            @Override
            public void actionPerformed(ActionEvent e) {
                outline_.repaint();
                DataEntryNode selNode = getSelectedNode();
                Hashtable data = (Hashtable) selNode.getUserObject();
                String nodeType = (String) data.get("type");
                // The selected node should be a data entry field node
                if (!("fieldNode".equals(nodeType))) {
                    return;
                }
                // Save Expansion state
                TreePath[] treeExpansion = OutlineUtil.saveExpansionState(outline_);
                DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
                int index = treeModel_.getIndexOfChild(root, selNode);
            // If it remains only 1 occurrence, we cannot delete this
                // occurrence. We can only clear the data
                int tag = Integer.valueOf((String) data.get("tag"));
                int nOccurrences = getFieldOccurrenceCount(tag);
                if (nOccurrences == 1) {
                    data.put("data", "");
                    for (Enumeration en = selNode.children(); en.hasMoreElements();) {
                        DataEntryNode node = (DataEntryNode) en.nextElement();
                        data = (Hashtable) node.getUserObject();
                        data.put("data", "");
                    }
                    treeModel_.fireStructureChanged();
                } else {
                    root.remove(index);
                    treeModel_.fireStructureChanged();
                    outline_.setRowSelectionInterval(index, index);
                    OutlineUtil.loadExpansionState(outline_, treeExpansion);
                }
            }
        };

        occurrencePopupMenu_.add(deleteOccurrenceAction);

        Action clearOccurrenceAction = new AbstractAction("Clear Occurrence") {

            @Override
            public void actionPerformed(ActionEvent e) {
                outline_.repaint();
                DataEntryNode selNode = getSelectedNode();
                Hashtable data = (Hashtable) selNode.getUserObject();
                String nodeType = (String) data.get("type");
                // The selected node should be a data entry field node
                if (!("fieldNode".equals(nodeType))) {
                    return;
                }
                // Save Expansion state
                TreePath[] treeExpansion = OutlineUtil.saveExpansionState(outline_);
                data.put("data", "");
                for (Enumeration en = selNode.children(); en.hasMoreElements();) {
                    DataEntryNode node = (DataEntryNode) en.nextElement();
                    data = (Hashtable) node.getUserObject();
                    data.put("data", "");
                }

                treeModel_.fireStructureChanged();
                OutlineUtil.loadExpansionState(outline_, treeExpansion);

            }
        };

        occurrencePopupMenu_.add(clearOccurrenceAction);

        subfieldPopupMenu_ = new JPopupMenu();
        Action addSubfieldAction = new AbstractAction("Add Subfield Occurrence") {
            @Override
            public void actionPerformed(ActionEvent e) {
                outline_.repaint();
                DataEntryNode selNode = getSelectedNode();
                Hashtable data = (Hashtable) selNode.getUserObject();
                String nodeType = (String) data.get("type");
                // The selected node should be a data entry field node
                if (!("subfieldNode".equals(nodeType))) {
                    return;
                }
                // Save Expansion state
                TreePath[] treeExpansion = OutlineUtil.saveExpansionState(outline_);
                int tag = Integer.valueOf((String) data.get("tag"));
                String subfieldCode = (String) data.get("subfieldCode");
                // Get the worksheet entry for this tag
                WorksheetDef.WorksheetField worksheetField = wks_.getFieldByTag(tag);
                WorksheetDef.WorksheetSubField wksSubfield = worksheetField.getSubFieldByCode(subfieldCode);
                // Get Parent Field tag for this subfield
                DataEntryNode fieldNode = (DataEntryNode) selNode.getParent();
                // Get child index of the selected subfield
                int index = treeModel_.getIndexOfChild(fieldNode, selNode);
                DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
                int fieldIndex = treeModel_.getIndexOfChild(root, fieldNode);
                // Build a new data entry subfield node
                DataEntryNode newNode = treeModel_.buildTreeSubfieldNode(worksheetField, wksSubfield, true);
            // Build a new data entry subfield node with possibly subfield as
                // children. Data is empty.
                fieldNode.insert(newNode, index + 1);
                treeModel_.fireStructureChanged();

                TreePath path = new TreePath(new Object[]{root, fieldNode});
                outline_.expandPath(path);
                System.out.println("rowCount=" + outline_.getRowCount() + " index=" + index + " fieldIndex=" + fieldIndex);
                // Because of the root line we have +3
                outline_.setRowSelectionInterval(fieldIndex + index + 3, fieldIndex + index + 3);
                OutlineUtil.loadExpansionState(outline_, treeExpansion);

            }
        };

        subfieldPopupMenu_.add(addSubfieldAction);

        Action deleteSubfieldAction = new AbstractAction("Delete Subfield Occurrence") {

            public void actionPerformed(ActionEvent e) {
                outline_.repaint();
                DataEntryNode selNode = getSelectedNode();
                Hashtable data = (Hashtable) selNode.getUserObject();
                String nodeType = (String) data.get("type");
                // The selected node should be a data entry field node
                if (!("subfieldNode".equals(nodeType))) {
                    return;
                }
                // Save Expansion state
                TreePath[] treeExpansion = OutlineUtil.saveExpansionState(outline_);
                int tag = Integer.valueOf((String) data.get("tag"));
                String subfieldCode = (String) data.get("subfieldCode");
                // Get the worksheet entry for this tag
                WorksheetDef.WorksheetField worksheetField = wks_.getFieldByTag(tag);
                WorksheetDef.WorksheetSubField wksSubfield = worksheetField.getSubFieldByCode(subfieldCode);
                // Get Parent Field tag for this subfield
                DataEntryNode fieldNode = (DataEntryNode) selNode.getParent();
                // Get child index of the selected subfield
                int index = treeModel_.getIndexOfChild(fieldNode, selNode);
                DataEntryNode root = (DataEntryNode) treeModel_.getRoot();

                int fieldIndex = treeModel_.getIndexOfChild(root, fieldNode);

            // If it remains only 1 occurrence, we cannot delete this
                // occurrence. We can only clear the data
                int nSubfieldOccurrences = getSubfieldOccurrenceCount(fieldNode, subfieldCode);
                int offset = 0;
                if (nSubfieldOccurrences == 1) {
                    data.put("data", "");
                    // Be sure to update the field content
                    rowModel_.setValueFor(selNode, 3, "");
                    for (Enumeration en = selNode.children(); en.hasMoreElements();) {
                        DataEntryNode node = (DataEntryNode) en.nextElement();
                        data = (Hashtable) node.getUserObject();
                        data.put("data", "");
                        // Be sure to update the field content
                        rowModel_.setValueFor(node, 3, "");
                    }

                    treeModel_.fireStructureChanged();
                    offset = 2;
                } else {
                    DataEntryNode node = (DataEntryNode) fieldNode.getChildAt(index);
                    // Be sure to update the field content
                    rowModel_.setValueFor(node, 3, "");
                    fieldNode.remove(index);

                    treeModel_.fireStructureChanged();
                    offset = 1;

                }

                TreePath path = new TreePath(new Object[]{root, fieldNode});
                outline_.expandPath(path);
                System.out.println("rowCount=" + outline_.getRowCount() + " index=" + index + " fieldIndex=" + fieldIndex + " offset=" + offset);
                outline_.setRowSelectionInterval(fieldIndex + index + offset, fieldIndex + index + offset);
                OutlineUtil.loadExpansionState(outline_, treeExpansion);

            }
        };

        subfieldPopupMenu_.add(deleteSubfieldAction);

    }

    /**
     * Find out next editable row
     *
     * @return row index of next editable row
     */
    private int getNextEditableRow() {
        DataEntryNode root = (DataEntryNode) treeModel_.getRoot();

        int rowSelected = selectedRow_;
        DataEntryNode selNode = (DataEntryNode) outline_.getValueAt(rowSelected, 0);

        int nextRow = (rowSelected == outline_.getRowCount() - 1) ? 1 : rowSelected + 1;
        DataEntryNode nextNode = (DataEntryNode) outline_.getValueAt(nextRow, 0);

        Hashtable data = (Hashtable) nextNode.getUserObject();
        String nodeType = (String) data.get("type");

        if (nodeType.equals("subfieldNode")) {
            // We are on a subfield node
            return nextRow;
        } else if (nodeType.equals("fieldNode")) {

            if (nextNode.getChildCount() > 0) {
             // FieldNode not editable but subfields are
                // Expand the node
                TreePath treePath = new TreePath(new Object[]{root, nextNode});
                outline_.expandPath(treePath);
                nextRow++;
            } else {
                // we are on an editable field without subfields
            }

        }

        return nextRow;

    }

    /**
     * Find out next editable row
     *
     * @return row index of next editable row
     */
    private int getPrevEditableRow() {
        DataEntryNode root = (DataEntryNode) treeModel_.getRoot();

        int rowSelected = selectedRow_;
        DataEntryNode selNode = (DataEntryNode) outline_.getValueAt(rowSelected, 0);
        Hashtable selData = (Hashtable) selNode.getUserObject();
        String selNodeType = (String) selData.get("type");

      // If we are on editable field just after root or on subfield of a
        // non editable field
        int prevRow = (rowSelected == 1 && selNodeType.equals("fieldNode")
            || (rowSelected == 2 && selNodeType.equals("subfieldNode")))
                ? outline_.getRowCount() - 1 : rowSelected - 1;
        DataEntryNode prevNode = (DataEntryNode) outline_.getValueAt(prevRow, 0);

        Hashtable prevData = (Hashtable) prevNode.getUserObject();
        String prevNodeType = (String) prevData.get("type");

        if (prevNodeType.equals("subfieldNode")) {
            // We are on a subfield node
            return prevRow;
        } else if (prevNodeType.equals("fieldNode")) {
            /**
             * We are on a field node. If current row is a subfield row, we need to go up one row up
             */

            if (selNodeType.equals("subfieldNode") && prevRow != outline_.getRowCount() - 1) {
                prevRow--;
                prevNode = (DataEntryNode) outline_.getValueAt(prevRow, 0);
                prevData = (Hashtable) prevNode.getUserObject();
                prevNodeType = (String) prevData.get("type");
            }
            if (prevNode.getChildCount() > 0) {
              // FieldNode not editable but subfields are
                // Expand the node
                TreePath treePath = new TreePath(new Object[]{root, prevNode});
                outline_.expandPath(treePath);
                prevRow += prevNode.getChildCount();
            } else {
                // we are on an editable field without subfields
            }

        }

        return prevRow;

    }

    private class ExpandedField {

        public String name_;
        public int index_;

        public ExpandedField(String name, int index) {
            name_ = name;
            index_ = index;
        }
    }

    private List<ExpandedField> getExpandedFieldIndexes() {
        DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
        int nChildFields = root.getChildCount();
        ArrayList<ExpandedField> expandedFields = new ArrayList<ExpandedField>();
        for (int i = 0; i < nChildFields; i++) {
            DataEntryNode node = (DataEntryNode) root.getChildAt(i);
            TreePath treePath = new TreePath(new Object[]{root, node});
            if (outline_.isExpanded(treePath)) {
                ExpandedField expandedField = new ExpandedField(node.displayName, i);
                expandedFields.add(expandedField);
            }
        }
        return expandedFields;
    }

    private int getFieldNodeWithName(String name) {

        int index = -1;
        DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
        int nChildFields = root.getChildCount();
        for (int i = 0; i < nChildFields; i++) {
            DataEntryNode node = (DataEntryNode) root.getChildAt(i);
            if (node.displayName.equals(name)) {
                index = root.getIndex(node);
                break;
            }
        }
        return index;
    }

    private void expandFields(List<ExpandedField> expandedFieldIndexes) {
        DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
        for (ExpandedField expandedField : expandedFieldIndexes) {
            int index = expandedField.index_;
            if (expandedField.index_ >= root.getChildCount()) {
                // Try to find a node for the same field
                index = getFieldNodeWithName(expandedField.name_);
                if (index == -1) {
                    continue;
                }
            }
            DataEntryNode node = (DataEntryNode) root.getChildAt(index);
            if (!(node.displayName.equals(expandedField.name_))) {
                // Try to find a node for the same field
                index = getFieldNodeWithName(expandedField.name_);
                if (index == -1) {
                    continue;
                }
                node = (DataEntryNode) root.getChildAt(index);
            }
            TreePath treePath = new TreePath(new Object[]{root, node});
            outline_.expandPath(treePath);
        }
    }

    public DataEntryNode getSelectedNode() {
        return ((DataEntryNode) outline_.getValueAt(selectedRow_, 0));
    }

    public int getFieldOccurrenceCount(int tag) {

        int fieldOccurrenceCount = 0;
        DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
        for (DataEntryNode node = (DataEntryNode) root.getFirstChild(); node != null; node = (DataEntryNode) node.getNextSibling()) {
            Hashtable data = (Hashtable) node.getUserObject();
            int nodeTag = Integer.valueOf((String) data.get("tag"));
            if (nodeTag == tag) {
                fieldOccurrenceCount++;
            }
        }
        return fieldOccurrenceCount;
    }

    public int getSubfieldOccurrenceCount(DataEntryNode fieldNode, String subfieldTag) {

        int subfieldOccurrenceCount = 0;

        for (DataEntryNode node = (DataEntryNode) fieldNode.getFirstChild(); node != null; node = (DataEntryNode) node.getNextSibling()) {
            Hashtable data = (Hashtable) node.getUserObject();
            String subfieldCode = (String) data.get("subfieldCode");
            if (subfieldCode.equals(subfieldTag)) {
                subfieldOccurrenceCount++;
            }
        }
        return subfieldOccurrenceCount;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outlineScrollPane = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());
        add(outlineScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane outlineScrollPane;
    // End of variables declaration//GEN-END:variables

    private void setDataEntryOutlineColumns() {
      // Disable auto resizing
        //outline_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnModel tcm = outline_.getColumnModel();
        ICellHighlight cmp = new ICellHighlight() {

            public boolean shouldHighlight(JTable tbl, Object value,
                int row, int column) {
                if (row > 0) {
                    DataEntryNode node = (DataEntryNode) outline_.getValueAt(row, 0);
                    Hashtable data = (Hashtable) node.getUserObject();
                    String nodeType = (String) data.get("type");
                    if (nodeType.equals("fieldNode")) {
                        return true;
                    }
                }
                return false;
            }
        };

        for (int i = 0; i < outline_.getColumnCount(); i++) {
            TableColumn tc = tcm.getColumn(i);
            if (i == 0) {
                // Tree node
                tc.setPreferredWidth(200);
            } else if (i == 1) {
                // Subfield
                tc.setPreferredWidth(50);
            } else if (i == 2) {
                // Repetition checkbox
                tc.setPreferredWidth(50);
            } else if (i == 3) {
                // Prompt
                tc.setPreferredWidth(300);
                tc.setCellRenderer(new HighlightRenderer(cmp,
                    null,
                    Color.pink, Color.black,
                    Color.pink.darker(), Color.white));

            } else if (i == 4) {
                // Data
                tc.setPreferredWidth(600);
                tc.setCellRenderer(new DefaultTableCellRenderer());
//            MultiLineTableCellRenderer textAreaRenderer = new MultiLineTableCellRenderer();
//            tc.setCellRenderer(textAreaRenderer);
//            MultiLineTableCellEditor textEditor = new MultiLineTableCellEditor();
//            tc.setCellEditor(textEditor);

                DefaultCellEditor editor = null;
                EditorDlgActionTableCellEditor actionEditor = null;
                JTextField textField = new JTextField();
                if (Global.getApplicationFont() != null) {
                    textField.setFont(Global.getApplicationFont());
                }
                textField.setBorder(BorderFactory.createEmptyBorder());
                editor = new DefaultCellEditor(textField) {

                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value,
                        boolean isSelected, int row, int column) {
                        final Component c = super.getTableCellEditorComponent(
                            table,
                            value, // edit the text field of Cell
                            isSelected,
                            row,
                            column);

                        cellInitialValue_ = (String) value;
                        //System.out.println("Initial vale="+value);
                        return c;
                    }
                };

                editor.setClickCountToStart(1);
                editor.addCellEditorListener(this);
                actionEditor = new EditorDlgActionTableCellEditor(editor);

                tc.setCellEditor(actionEditor);
            } else if (i == 5) {
                // PickList Button
                tc.setPreferredWidth(75);
                JTableButtonRenderer buttonRenderer = new JTableButtonRenderer();
                buttonRenderer.setForeground(Color.blue);
                buttonRenderer.setBackground(Color.lightGray);
                tc.setCellRenderer(buttonRenderer);

                TableCellEditor editor = new JTableButtonEditor(new JButton());
                tc.setCellEditor(editor);
            }
        }
    }

    void setRecord(IRecord record, boolean showEmptyFields) {
        List<ExpandedField> treeExpansionIndexes = null;
        if (record_ != null) {
            // Save Expansion state
            treeExpansionIndexes = getExpandedFieldIndexes();
        }
        record_ = record;
        treeModel_ = DataEntryTreeModel.makeTreeModelForRecord(db_, wks_, record,
            pickListDataList_, validationDataList_, showEmptyFields);

        outlineModel_ = DefaultOutlineModel.createOutlineModel(treeModel_, rowModel_);
      // Listen to changes in the OutlineModel
//      outlineModel_.addTableModelListener(new TableModelListener() {
//
//         public void tableChanged(TableModelEvent e) {
//            switch (e.getType()) {
////               case TableModelEvent.DELETE:
////               case TableModelEvent.INSERT:
//               case TableModelEvent.UPDATE:
//                  topComponent_.setRecordChangedFlag(true);
//               default:
//
//            }
//
//         }
//      });

        outline_.setModel(outlineModel_);

        setDataEntryOutlineColumns();
        treeModel_.fireTreeStructureChanged(new TreePath(treeModel_.getRoot()));
        if (treeExpansionIndexes != null) {
            expandFields(treeExpansionIndexes);
        }
    }

    void newRecord() {
        record_ = null;
        treeModel_ = DataEntryTreeModel.makeTreeModelForEmptyRecord(db_, wks_,
            pickListDataList_, validationDataList_);

        outlineModel_ = DefaultOutlineModel.createOutlineModel(treeModel_, rowModel_);
        // Listen to changes in the OutlineModel
//      outlineModel_.addTableModelListener(new TableModelListener() {
//         public void tableChanged(TableModelEvent e) {
//            switch (e.getType()) {
////               case TableModelEvent.DELETE:
////               case TableModelEvent.INSERT:
//               case TableModelEvent.UPDATE:
//                  topComponent_.setRecordChangedFlag(true);
//               default:
//
//            }
//         }
//      });

        outline_.setModel(outlineModel_);
        setDataEntryOutlineColumns();
        treeModel_.fireTreeStructureChanged(new TreePath(treeModel_.getRoot()));

    }

    public void changeWorksheet(WorksheetDef wks, IRecord record, boolean showEmptyFields,
        List<PickListData> pickListDataList, List<ValidationData> validationDataList) {
        wks_ = wks;
        wksName_ = wks.getName();
        record_ = record;
        pickListDataList_ = pickListDataList;
        validationDataList_ = validationDataList;
        treeModel_ = DataEntryTreeModel.makeTreeModelForRecord(db_, wks_, record_,
            pickListDataList_, validationDataList_, showEmptyFields);

        outlineModel_ = DefaultOutlineModel.createOutlineModel(treeModel_, rowModel_);

        outline_.setModel(outlineModel_);
        setDataEntryOutlineColumns();
        treeModel_.fireTreeStructureChanged(new TreePath(treeModel_.getRoot()));
    }

    /**
     * Build a Record object from the Outline TreeTable and current record if we are updating.
     *
     * We use a TreeMap to store the fields before builing the Record object
     *
     * @return - The record object
     */
    public IRecord getRecord() {

        Record newRecord = null;
        try {
         // Build a treemap to get fields in increasing tag orders and
            // to build occurrences in a single string

            TreeMap<Integer, Object> fieldTreeMap = new TreeMap<Integer, Object>();
         //-------------------------------------------------
            // 1) Populate the TreeMap with the worksheet data
            //-------------------------------------------------
            // Loop on the field nodes
            for (DataEntryNode node = (DataEntryNode) treeModel_.getDataEntryTreeRoot().getFirstChild();
                node != null; node = (DataEntryNode) node.getNextSibling()) {

                Hashtable nodeData = (Hashtable) node.getUserObject();
                int tag = Integer.valueOf((String) nodeData.get("tag"));
                Object obj = nodeData.get("data");
                if (obj instanceof String) {
                    String value = (String) obj;

                    String fieldValue = (String) fieldTreeMap.get(new Integer(tag));
                    if (fieldValue == null) {
                        // 1st occurrence
                        fieldTreeMap.put(new Integer(tag), value);
                    } else {
                        // Other occurrences
                        fieldValue += Global.REPETITION_SEPARATOR;
                        fieldValue += value;
                        fieldTreeMap.put(new Integer(tag), fieldValue);
                    }
                } else {
               // obj Should be a blob
                    // Any occurrences already there?
                    ArrayList fieldValues = null;
                    fieldValues = (ArrayList) fieldTreeMap.get(new Integer(tag));
                    if (fieldValues == null) {
                        // 1st occurrence
                        fieldValues = new ArrayList();
                    }
                    fieldValues.add(obj);
                    fieldTreeMap.put(new Integer(tag), fieldValues);
                }
            }
         //---------------------------------------------------------------------
            // 2) Add to the TreeMap the record field data which are not in the
            //    the worksheet tree table
            //---------------------------------------------------------------------
            if (record_ != null) {
            // We are updating an existing record, tranfer in the TreeMap the
                // fields not defined in the worksheet if any
                int nFields = record_.getFieldCount();
                for (int i = 0; i < nFields; i++) { // Loop on the record fields
                    IField field = record_.getFieldByIndex(i);
                    int tag = field.getTag();
                    // Be sure the tag exists in the fdt
                    int ifind = fdt_.findField(tag);
                    if (ifind < 0) {
                        continue; // Skip
                    }
                    Object obj = fieldTreeMap.get(new Integer(tag));
                    if (obj == null) {
                        // This field was not in the worksheet
                        if (field.getType() == Global.FIELD_TYPE_BLOB) {
                            ArrayList fieldValues = new ArrayList();
                            for (int k = 0; k < field.getOccurrenceCount(); i++) {
                                fieldValues.add(field.getOccurrenceValue(k));
                            }
                            fieldTreeMap.put(new Integer(tag), fieldValues);
                        } else {
                            String fieldValue = field.getStringFieldValue();
                            fieldTreeMap.put(new Integer(tag), fieldValue);
                        }
                    }

                }
            }

            //Build the new record
            newRecord = (Record) Record.createRecord();
            if (record_ != null) {
                // We are updating the record, transfer the MFN
                newRecord.setMfn(record_.getMfn());
            }
            Set<Map.Entry<Integer, Object>> set = fieldTreeMap.entrySet();

            for (Map.Entry<Integer, Object> entry : set) {
                Integer tag = entry.getKey();
                Object obj = entry.getValue();
                IField field = FieldFactory.makeField(tag);
                if (obj instanceof String) {
                    String fieldValue = (String) obj;

                    field.setType(Global.FIELD_TYPE_ALPHANUMERIC);
                    field.setFieldValue(fieldValue);
                    newRecord.addField(field);
                } else {
                    field.setType(Global.FIELD_TYPE_BLOB);
                    ArrayList values = (ArrayList) obj;
                    for (int k = 0; k < values.size(); k++) {
                        field.setOccurrence(field.getOccurrenceCount(), (byte[]) values.get(k));
                    }
                    newRecord.addField(field);
                }

            }
            // At this level, the new field may have occurrences and subfields
//         String fieldVal = nodeData.get("data");
//         field.setFieldValue(fieldVal);

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return newRecord;
    }

    public void processPickList() {
        Object obj;
       

        // We are on a picklist button 
        DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
        DataEntryNode selNode = (DataEntryNode) outline_.getValueAt(selectedRow_, 0);
        Hashtable map = (Hashtable) selNode.getUserObject();
        String nodeType = (String) map.get("type");
        String subfieldCode = (String) map.get("subfieldCode");
        // The selected node should be a data entry field node
        if ("fieldNode".equals(nodeType) || ("subfieldNode".equals(nodeType))) {
            TreePath path = new TreePath(new Object[]{root, selNode});
            if (path == null) {
                return;
            }

            clickedPath_ = path;
            PickListData pickListData = (PickListData) map.get("pickList");
            if (pickListData != null) {
                List<String> labels = pickListData.getLabels();
                List<String> codes = pickListData.getCodes();
                final JXList jxList = new JXList((String[]) labels.toArray(new String[labels.size()]));
                ColorHighlighter colorHighlighter = new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, Color.CYAN, Color.WHITE);
                jxList.addHighlighter(colorHighlighter);
                jxList.setRolloverEnabled(true);

                if (pickListData.isMultiChoice()) {
                    jxList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                } else {
                    jxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                }
                final ListSelectorDialog jd = new ListSelectorDialog(WindowManager.getDefault().getMainWindow(),
                    pickListData.getDialogTitle(), jxList);
                jd.setLocationRelativeTo(null);
                int result = jd.showDialog();
                if (result == ListSelectorDialog.APPROVE_OPTION) {

                    // Save Expansion state
                    TreePath[] treeExpansion = OutlineUtil.saveExpansionState(outline_);
                    int tag = Integer.valueOf((String) map.get("tag"));
                    // Get the worksheet entry for this tag
                    WorksheetDef.WorksheetField worksheetField = wks_.getFieldByTag(tag);

                    int[] selected = jxList.getSelectedIndices();

                    StringBuilder sb = new StringBuilder();
                    int index = treeModel_.getIndexOfChild(root, selNode);
                    int iocc = 0;

                    if (pickListData.isRepeat()) {
                        // Build a new field occurrence from each selected item 
                        for (int i = 0; i < selected.length; i++) {
                            sb = new StringBuilder();
                            if (pickListData.isLtGt()) {
                                sb.append("<");
                            } else if (pickListData.isSlashSlash()) {
                                sb.append("/");
                            }
                            if (pickListData.isFirstDescribe()) {
                                // The first is what the user sees on the list. 
                                // The second is what it will be really inserted in the
                                // field. This is useful to mask codes with 
                                // human-readable descriptions.
                                sb.append(codes.get(selected[i]));
                            } else {
                                sb.append(labels.get(selected[i]));
                            }
                            if (pickListData.isLtGt()) {
                                sb.append(">");
                            } else if (pickListData.isSlashSlash()) {
                                sb.append("/");
                            }
                            if (map.get("data") == null || ((String) map.get("data")).length() == 0) {
                                // Current field occurrence is empty thus start from there
                                map.put("data", sb.toString());
                                // if the selected field has subfields, we fill the subfields
                                populateSubfields(sb.toString(), selNode);

                            } else {

                                // We create a new occurrence for this PickList item
                                // Build a new data entry field node with possibly subfield as
                                // children. Data is empty.
                                DataEntryNode newNode = treeModel_.buildTreeNodes(worksheetField, false);
                                Hashtable newNodeMap = (Hashtable) newNode.getUserObject();
                                newNodeMap.put("data", sb.toString());
                                // if the selected field has subfields, we fill the subfields
                                populateSubfields(sb.toString(), newNode);
                                root = (DataEntryNode) treeModel_.getRoot();

                                root.insert(newNode, index + 1 + iocc);
                                iocc++;
                            }
                        }
                    } else {
                        // We work on the current occurrence
                        if (pickListData.isAdd()) {
                            //New selected items' text will be added to the text already in the field.
                            obj = map.get("data");
                            if (obj == null || ((String) obj).length() == 0) {
                                // Do nothing
                            } else {
                                // Add a blank
                                sb.append((String) obj).append(" ");
                            }
                        }
                        // Loop on the picklist selected elements
                        for (int i = 0; i < selected.length; i++) {
                            if (pickListData.isLtGt()) {
                                sb.append("<");
                            } else if (pickListData.isSlashSlash()) {
                                sb.append("/");
                            }
                            if (pickListData.isFirstDescribe()) {
                                // The first is what the user sees on the list. 
                                // The second is what it will be really inserted in the
                                // field. This is useful to mask codes with 
                                // human-readable descriptions.
                                sb.append(codes.get(selected[i]));
                            } else {
                                sb.append(labels.get(selected[i]));
                            }
                            if (pickListData.isLtGt()) {
                                sb.append(">");
                            } else if (pickListData.isSlashSlash()) {
                                sb.append("/");
                            }
                            if (i < selected.length - 1) {
                                sb.append(" ");
                            }
                        }
                        map.put("data", sb.toString());
                        populateSubfields(sb.toString(), selNode);
                    }
                    // Be sure to update the field content
                    rowModel_.setValueFor(selNode, 3, sb.toString());
                    //recordModified = true;
                    treeModel_.fireStructureChanged();
                    OutlineUtil.loadExpansionState(outline_, treeExpansion);
                }

                /**
                 * We need to reset the selected column so that when the user expand or collapse a node, the
                 * selected column becomes 0 and don't stay on 5 (PickList button). This would result in
                 * presenting again the PickList dialog when the user expand or collapse a node
                 */
                outline_.setColumnSelectionInterval(0, outline_.getColumnCount() - 2);
            }

        
        }

        topComponent_.setRecordChangedFlag(true);
    }

    private void populateSubfields(String fieldOccurrenceValue, DataEntryNode selNode) {
        // if the selected field has subfields, we fill the subfields
        StringOccurrence occ = new StringOccurrence();
        occ.setValue(fieldOccurrenceValue);
        int nSubfields = selNode.getChildCount();
        for (int j = 0; j < nSubfields; j++) { // Loop on subfields
            DataEntryNode subfieldNode = (DataEntryNode) selNode.getChildAt(j);

            Hashtable subfieldMap = (Hashtable) subfieldNode.getUserObject();
            String subfieldCode = (String) subfieldMap.get("subfieldCode");

            boolean hasIndicators = false;
            if (subfieldCode.equals("$ind1")) {
                hasIndicators = true;
                // Get 1st subfield of occurrence
                Subfield subfield = occ.getSubfield(0);
               // If we have a 1st implicit subfield and indicators
                // Then the 1st character in the subfield is the
                // MARC "first indicator"
                String descriptor1 = (subfield == null || subfield.getSubfieldCode() != '*'
                    || subfield.getData().length() <= 0) ? " "
                        : Character.toString(subfield.getData().charAt(0));
                subfieldMap.put("data", descriptor1);
            } else if (subfieldCode.equals("$ind2")) {
                Subfield subfield = occ.getSubfield(0);
               // If we have a 1st implicit subfield and indicators
                // Then the 2nd character in the subfield is the
                // MARC "second indicator"
                String descriptor2 = (subfield == null || subfield.getSubfieldCode() != '*'
                    || subfield.getData().length() <= 1) ? " "
                        : Character.toString(subfield.getData().charAt(1));
                subfieldMap.put("data", descriptor2);

            } else if (subfieldCode.charAt(1) == '*') {
                Character subfieldCodeCharacter = subfieldCode.charAt(1);
                String charCode = Global.SUBFIELD_SEPARATOR + Character.toString(subfieldCodeCharacter);

                String subfieldData = occ.getSubfield(charCode);
               // We should have only one occurrence of 1st implicit subfield and
                // this field may or maynot contain the indicators
                // In that case, we should remove the indicators from the field value

                if (hasIndicators && subfieldData != null && subfieldData.length() > 2) {
                    subfieldData = subfieldData.substring(2);
                }

                subfieldMap.put("data", (subfieldData == null) ? "" : subfieldData);

            } else {

                Character subfieldCodeCharacter = subfieldCode.charAt(1);
                String charCode = Global.SUBFIELD_SEPARATOR + Character.toString(subfieldCodeCharacter);
                // Get all subfield occurrences
                List<Subfield> subfieldOccurrences = ((StringOccurrence) occ).getSubfieldOccurrences(charCode);
                if (subfieldOccurrences == null) {
                    subfieldMap.put("data", "");
                } else {
                    String subfieldData = subfieldOccurrences.get(0).getData();
                    subfieldMap.put("data", (subfieldData == null) ? "" : subfieldData);
                }
            }
        }
    }

    class PopupTrigger extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                int x = e.getX();
                int y = e.getY();
                if (selectedRow_ == -1) {
                    return;
                }
                DataEntryNode root = (DataEntryNode) treeModel_.getRoot();
                DataEntryNode selNode = (DataEntryNode) outline_.getValueAt(selectedRow_, 0);
                Hashtable data = (Hashtable) selNode.getUserObject();
                String nodeType = (String) data.get("type");
                String subfieldCode = (String) data.get("subfieldCode");
                // The selected node should be a data entry field node
                if ("fieldNode".equals(nodeType)) {
                    TreePath path = new TreePath(new Object[]{root, selNode});
                    if (path == null) {
                        return;
                    }
                    if (outline_.isExpanded(path)) {
                        expandAction_.putValue(Action.NAME, "Collapse");
                    } else {
                        expandAction_.putValue(Action.NAME, "Expand");
                    }
                    occurrencePopupMenu_.show(outline_, x, y);
                    clickedPath_ = path;

                } else if ("subfieldNode".equals(nodeType)) {
                    if (!subfieldCode.equals("$ind1") && !subfieldCode.equals("$ind2")
                        && !subfieldCode.equals("$*")) {
                        TreePath path = new TreePath(new Object[]{root, selNode});
                        if (path == null) {
                            return;
                        }

                        subfieldPopupMenu_.show(outline_, x, y);
                        clickedPath_ = path;
                    }
                }
            }
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {

                
                int col = outline_.getSelectedColumn();
                int row = outline_.getSelectedRow();
                if (col != 5) {
                    return;
                }
                // PickList button was clicked
                processPickList();
                outline_.setRowSelectionInterval(row, row);
                outline_.setColumnSelectionInterval(col, col);

            }
        }

    }

    private class RenderData implements RenderDataProvider {

        @Override
        public java.awt.Color getBackground(Object o) {
            DataEntryNode dataEntryNode = (DataEntryNode) o;
            Hashtable dataMap = (Hashtable) dataEntryNode.getUserObject();
            //System.out.println("column="+column+" data="+nameMap.toString());

            if (dataMap.get("type").equals("rootNode")
                || (dataMap.get("type").equals("fieldNode") && dataEntryNode.getChildCount() > 0)) {

                return UIManager.getColor("ScrollBar.thumb");
            }
            return null;
        }

        @Override
        public String getDisplayName(Object o) {
            return null;
        }

        @Override
        public java.awt.Color getForeground(Object o) {

            return null;
        }

        @Override
        public javax.swing.Icon getIcon(Object o) {
            return null;

        }

        @Override
        public String getTooltipText(Object o) {

            return null;
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }
    }
}
