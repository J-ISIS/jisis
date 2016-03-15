/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.FmtManager;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.PasteAction;
import org.openide.actions.RedoAction;
import org.openide.actions.UndoAction;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;
import static org.unesco.jisis.FmtManager.FmtDocumentEx.DEFAULT_FONT_SIZE;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.PrintFormat;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.FormattingException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.server.DbHandle;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;
import org.unesco.jisis.weboutput.WebOutputTopComponent;
import org.unesco.jisis.corelib.util.StringUtils;

//import org.openide.util.Utilities;
/**
 * Top component which displays something.
 */
final public class FmtManagerTopComponent extends TopComponent {

   private static FmtManagerTopComponent instance;
   /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
   private static final String PREFERRED_ID = "FmtManagerTopComponent";
   private static PrintFormat currentPft_;
   private ClientDatabaseProxy db_;
   
   private UndoManagerEx undoRedoManager_ = new UndoManagerEx();
      
   private WebOutputTopComponent out_;
   
   
   protected int m_xStart = -1;
   protected int m_xFinish = -1;
   
   protected FindDialog findDialog_;
   
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DbHandle.class);

   public FmtManagerTopComponent(IDatabase db) {

      if (db instanceof ClientDatabaseProxy) {
         db_ = (ClientDatabaseProxy) db;
      } else {
         throw new RuntimeException("FmtManagerTopComponent: Cannot cast DB to ClientDatabaseProxy");
      }

      /* Register this TopComponent as attached to this DB */
      db_.addWindow(this);
       if (Global.getApplicationFont() != null) {
           UIManager.getDefaults().get("TextPane.font");
           UIManager.getDefaults().put("TextPane.font", Global.getApplicationFont());
       }

      initComponents();

      //FmtDocumentEx doc = new FmtDocumentEx();
     
      
      /**
       * Add a caretListener to the editor. 
       *  1) Used to update cursor row and column when caret moves.
       *  2) Used to enable the Quote button
       */
      editorPane_.addCaretListener(new CaretListener() {
         public void caretUpdate(CaretEvent e) {
            int row = getRow(e.getDot(), (JTextComponent) e.getSource());
            int col = getColumn(e.getDot(), (JTextComponent) e.getSource());
            // Once we know the position of the line and the column, pass it to a helper function for updating the status bar.
            StatusDisplayer.getDefault().setStatusText("" + row + " | " + col, StatusDisplayer.IMPORTANCE_ANNOTATION);
            
            if (e.getDot() == e.getMark()) { // no selection
               btnQuote.setEnabled(false);
            } else {
               btnQuote.setEnabled(true);
            }
         }
      });
      /**
       * Focus Listener to save and restore the caret position when selection occurs
       * in another text component.
       */
      FocusListener focusListener = new FocusListener() {
         public void focusGained(FocusEvent e) {
            int len = editorPane_.getDocument().getLength();
            if (m_xStart>=0 && m_xFinish>=0 &&
                m_xStart<len && m_xFinish<len) {
               if (editorPane_.getCaretPosition()==m_xStart) {
                  editorPane_.setCaretPosition(m_xFinish);
                  editorPane_.moveCaretPosition(m_xStart);
               } else {
                  editorPane_.select(m_xStart, m_xFinish);
               }             
            }
            editorPane_.grabFocus();
         }

         public void focusLost(FocusEvent e) {
            m_xStart = editorPane_.getSelectionStart();
            m_xFinish = editorPane_.getSelectionEnd();
         }
      };
      editorPane_.addFocusListener(focusListener);

      hookActions();
          
     
      resetDefault();

      setName(NbBundle.getMessage(FmtManagerTopComponent.class, "CTL_FmtManagerTopComponent"));
      setToolTipText(NbBundle.getMessage(FmtManagerTopComponent.class, "HINT_FmtManagerTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
      try {

         this.setDisplayName("PFT Manager" + " (" + db.getDbHome() + "//" + db.getDatabaseName() + ")");
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      out_ = WebOutputTopComponent.findInstance();
   }
     public static int getRow(int pos, JTextComponent editor) {
        int rn = (pos==0) ? 1 : 0;
        if (pos == 0) return rn;
        try {
            int offs=pos;
            while( offs>=0) {
                offs=Utilities.getRowStart(editor, offs)-1;
                rn++;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return rn;
    }

    public static int getColumn(int pos, JTextComponent editor) {
        try {
            return pos-Utilities.getRowStart(editor, pos)+1;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return -1;
    }
   @Override
   public UndoRedo getUndoRedo() {
      return undoRedoManager_;
   }
   
   private String[] getPrintFormatNames() {

      String[] pftNames = null;
      try {
         pftNames = db_.getPrintFormatNames();
      } catch (DbException ex) {
         LOGGER.error("Error when getting PFT names", ex);
      }

      return pftNames;
   }
   
   public JTextPane getTextPane() {
      return editorPane_;
   }
   
   public void setSelection(int xStart, int xFinish, boolean moveUp) {
      if (moveUp) {
         editorPane_.setCaretPosition(xFinish);
         editorPane_.moveCaretPosition(xStart);
      } else {
         editorPane_.select(xStart, xFinish);
      }
      m_xStart = editorPane_.getSelectionStart();
      m_xFinish = editorPane_.getSelectionEnd();
       final FmtEditor ed = (FmtEditor) editorPane_;
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  
                  ed.grabFocus();
               }
            });
   }
   
   private void loadDocument(String data) {
      try {     
       
         /**
          * To avoid that each insertion will trigger a re-rendering of the display
          * swap in a blank document and then restore the original document 
          * after the inserts:
          * 
          **/ 
         FmtDocumentEx doc = new FmtDocumentEx();
         doc.setFontSize(DEFAULT_FONT_SIZE) ;
         sliderFontSize.setValue(DEFAULT_FONT_SIZE);
         Document blank = new FmtDocumentEx();
         editorPane_.setDocument(blank);
         // Use fast replace
         data = StringUtils.fastReplaceAll(data, "\r\n", "\n");
         doc.insertString(0, data, null);
         
         // Add the listeners
        
         

//    for (... iteration over large chunk of parsed text ...) {
//        ...
//        doc.insertString(offset, partOfText, attrsForPartOfText);
//        ...
//    }
       
         editorPane_.setDocument(doc);
         doc.addDocumentListener((FmtEditor) editorPane_);
         doc.addUndoableEditListener(undoRedoManager_);
         FmtEditor fmtEditor = (FmtEditor) editorPane_;
         fmtEditor.setDocumentUnchanged();
      } catch (BadLocationException ex) {
         Exceptions.printStackTrace(ex);
      }
      
      
        
     
   }

   private void resetDefault() {
      try {
         String pftName = db_.getDefaultPrintFormatName();
         String pftFormat = db_.getPrintFormat(pftName);
         currentPft_ = new PrintFormat(pftName, pftFormat);
         editorPane_.setName(pftName);
         loadDocument(pftFormat);
         //editFormat.setEditorKit(new NumberedEditorKit());
         String[] pftNames = getPrintFormatNames();
         cmbPFT.setModel(new DefaultComboBoxModel(pftNames));

         final FmtEditor ed = (FmtEditor) editorPane_;
         ed.setDocumentUnchanged();
         cmbPFT.setSelectedItem(pftName);
        
         undoRedoManager_.discardAllEdits();
         m_xStart = -1;
         m_xFinish = -1;
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               ed.setCaretPosition(0);
               ed.scrollRectToVisible(new Rectangle(0,0,1,1));
               ed.grabFocus();
            }
         });

      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }
   }


   /**
    * Hook standards edit actions
    */
   private void hookActions() {
      // hook into the netbeans Edit menu

      InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      
      CopyAction copyAction = SystemAction.get(CopyAction.class);
      KeyStroke keyCopy = (KeyStroke) copyAction.getValue(Action.ACCELERATOR_KEY);
      if (keyCopy == null) {
         keyCopy = KeyStroke.getKeyStroke("control C");
      }
      keys.put(keyCopy, copyAction);
      
      PasteAction pasteAction = SystemAction.get(PasteAction.class);
      KeyStroke keyPaste = (KeyStroke) pasteAction.getValue(Action.ACCELERATOR_KEY);
      if (keyPaste == null) {
         keyPaste = KeyStroke.getKeyStroke("control V");
      }
      keys.put(keyPaste, pasteAction);
      
      CutAction cutAction = SystemAction.get(CutAction.class);
      KeyStroke keyCut = (KeyStroke) cutAction.getValue(Action.ACCELERATOR_KEY);
      if (keyCut == null) {
         keyCut = KeyStroke.getKeyStroke("control X");
      }
      keys.put(keyCut, cutAction);
      
      // Bind the undo action to ctl-Z
      UndoAction undoAction = SystemAction.get(UndoAction.class);
       KeyStroke keyUndo = (KeyStroke) undoAction.getValue(Action.ACCELERATOR_KEY);
      if (keyUndo == null) {
         keyUndo = KeyStroke.getKeyStroke("control Z");
      }
      keys.put(keyUndo, undoAction);
      
      // Bind the redo action to ctl-Y
      RedoAction redoAction = SystemAction.get(RedoAction.class);
      KeyStroke keyRedo = (KeyStroke) redoAction.getValue(Action.ACCELERATOR_KEY);
      if (keyRedo == null) {
         keyRedo = KeyStroke.getKeyStroke("control Y");
      }
      keys.put(keyRedo, redoAction);

   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editorPanel = new javax.swing.JPanel();
        editorScrollPane = new javax.swing.JScrollPane();
        editorPane_ = new FmtEditor(this);
        ctrlPanel = new javax.swing.JPanel();
        crudPanel = new javax.swing.JPanel();
        btnNew = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        lbltSelectFormat = new javax.swing.JLabel();
        cmbPFT = new javax.swing.JComboBox();
        btnGenerateHTML = new javax.swing.JButton();
        btnSyntax = new javax.swing.JButton();
        btnQuote = new javax.swing.JButton();
        btnFindReplace = new javax.swing.JButton();
        btnConvert = new javax.swing.JButton();
        btnShowRecords = new javax.swing.JButton();
        sliderFontSize = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();

        TextLineNumber tln = new TextLineNumber(editorPane_, 5);
        editorScrollPane.setRowHeaderView( tln );
        editorPane_.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                editorPane_KeyTyped(evt);
            }
        });
        editorScrollPane.setViewportView(editorPane_);

        javax.swing.GroupLayout editorPanelLayout = new javax.swing.GroupLayout(editorPanel);
        editorPanel.setLayout(editorPanelLayout);
        editorPanelLayout.setHorizontalGroup(
            editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editorScrollPane)
                .addContainerGap())
        );
        editorPanelLayout.setVerticalGroup(
            editorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editorPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(editorScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE)
                .addContainerGap())
        );

        ctrlPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        crudPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(btnNew, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnNew.text")); // NOI18N
        btnNew.setFocusable(false);
        btnNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnSave.text")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        lbltSelectFormat.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lbltSelectFormat, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.lbltSelectFormat.text")); // NOI18N

        cmbPFT.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbPFT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPFTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout crudPanelLayout = new javax.swing.GroupLayout(crudPanel);
        crudPanel.setLayout(crudPanelLayout);
        crudPanelLayout.setHorizontalGroup(
            crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, crudPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbltSelectFormat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbPFT, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(btnNew, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        crudPanelLayout.setVerticalGroup(
            crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, crudPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(crudPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnDelete)
                        .addComponent(btnSave)
                        .addComponent(btnNew))
                    .addComponent(cmbPFT, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbltSelectFormat))
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(btnGenerateHTML, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnGenerateHTML.text")); // NOI18N
        btnGenerateHTML.setToolTipText(org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnGenerateHTML.toolTipText")); // NOI18N
        btnGenerateHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateHTMLActionPerformed(evt);
            }
        });

        btnSyntax.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        btnSyntax.setForeground(new java.awt.Color(51, 51, 255));
        org.openide.awt.Mnemonics.setLocalizedText(btnSyntax, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnSyntax.text")); // NOI18N
        btnSyntax.setToolTipText(org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnSyntax.toolTipText")); // NOI18N
        btnSyntax.setFocusable(false);
        btnSyntax.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSyntax.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSyntax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSyntaxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnQuote, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnQuote.text")); // NOI18N
        btnQuote.setToolTipText(org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnQuote.toolTipText")); // NOI18N
        btnQuote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuoteActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnFindReplace, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnFindReplace.text")); // NOI18N
        btnFindReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindReplaceActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnConvert, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnConvert.text")); // NOI18N
        btnConvert.setToolTipText(org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnConvert.toolTipText")); // NOI18N
        btnConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvertActionPerformed(evt);
            }
        });

        btnShowRecords.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowRecords, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnShowRecords.text")); // NOI18N
        btnShowRecords.setToolTipText(org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.btnShowRecords.toolTipText")); // NOI18N
        btnShowRecords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowRecordsActionPerformed(evt);
            }
        });

        sliderFontSize.setMaximum(40);
        sliderFontSize.setMinimum(10);
        sliderFontSize.setToolTipText(org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.sliderFontSize.toolTipText")); // NOI18N
        sliderFontSize.setValue(12);
        sliderFontSize.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        sliderFontSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderFontSizeStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FmtManagerTopComponent.class, "FmtManagerTopComponent.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout ctrlPanelLayout = new javax.swing.GroupLayout(ctrlPanel);
        ctrlPanel.setLayout(ctrlPanelLayout);
        ctrlPanelLayout.setHorizontalGroup(
            ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ctrlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(ctrlPanelLayout.createSequentialGroup()
                        .addComponent(crudPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnGenerateHTML)
                            .addComponent(btnConvert, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sliderFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(ctrlPanelLayout.createSequentialGroup()
                        .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnQuote, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnFindReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnShowRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSyntax, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ctrlPanelLayout.setVerticalGroup(
            ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ctrlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(ctrlPanelLayout.createSequentialGroup()
                        .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSyntax, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnGenerateHTML)
                                .addComponent(btnQuote)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnConvert)
                            .addComponent(btnFindReplace)
                            .addComponent(btnShowRecords)))
                    .addComponent(crudPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sliderFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ctrlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(editorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ctrlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(editorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

   private boolean checkFormatSaved() {
      FmtEditor ed = (FmtEditor) editorPane_;
      if (ed.documentHasChanged()) {
         NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                 "The Current Format was modified!\nDo you want to save it?",
                 NotifyDescriptor.YES_NO_CANCEL_OPTION,
                 NotifyDescriptor.QUESTION_MESSAGE);

         Object option = DialogDisplayer.getDefault().notify(nd);
         if (option == NotifyDescriptor.CANCEL_OPTION) {
            // Do nothing
            return false;

         } else if (option == NotifyDescriptor.YES_OPTION) {
            btnSaveActionPerformed(null);
         }
      }
      return true;
   }
   
   private boolean pftNameExist(String pftName) {
      String pftNames[]=null;
      try {
         pftNames = db_.getPrintFormatNames();
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      if (pftNames == null || pftNames.length == 0) {
         return false;
      }
      
      for (int i = 0; i < pftNames.length; i++) {
         if (pftNames[i].equals(pftName)) {
            try {
               if (pftName.equals("RAW") && db_.getPrintFormat(pftName).equals("")) {
                  return false;
               } else {
                  return true;
               }
            } catch (DbException ex) {
               Exceptions.printStackTrace(ex);
            }
         }
      }
      return false;
   }
   public static String getFormatName() {
      return currentPft_.getName();
   }

   public static String getFormat() {
      return currentPft_.getFormat();
   }
         private void editorPane_KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_editorPane_KeyTyped
            // TODO add your handling code here:
         }//GEN-LAST:event_editorPane_KeyTyped

   private void btnShowRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowRecordsActionPerformed

      currentPft_.setFormat(editorPane_.getText());

      try {
         String fmt = editorPane_.getText();
         if (!fmt.endsWith("\n")) {
            fmt = fmt + '\n';
         }
         ISISFormatter formatter = ISISFormatter.getFormatter(fmt);
         if (formatter == null) {
            GuiGlobal.output(ISISFormatter.getParsingError());
         } else if (formatter.hasParsingError()) {
            GuiGlobal.output(ISISFormatter.getParsingError());
         }

      } catch (RuntimeException re) {
         new FormattingException(re.getMessage()).displayWarning();
         return;
      }


      out_.setPft(db_, currentPft_.getName(), currentPft_.getFormat());
      out_.open();
      out_.repaint();
      out_.requestActive();
   }//GEN-LAST:event_btnShowRecordsActionPerformed

   private void btnConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvertActionPerformed
      // TODO add your handling code here:

      ConvertDlg dlg = new ConvertDlg(WindowManager.getDefault().getMainWindow(), true);
      dlg.setLocationRelativeTo(null);
      dlg.setVisible(true);
      if (!dlg.succeeded()) {
         return;
      }

      String encoding = dlg.getEncoding();
      String newStr = null;

      // Get the ansi format
      String ansiFmt = null;
      try {
         ansiFmt = db_.getPrintFormatAnsi(editorPane_.getName());
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }

      /**
      * Converting bytes to chars is called decoding
      * A Charset is created using the Charset.forName() method.
      * String charsetName = "ISO-8859-1";
      * Charset charset = Charset.forName( charsetName );
      * converting a Byte- Buffer to a CharBuffer using a Charset:
      * Charset charset = Charset.forName( charsetName );
      * CharsetDecoder decoder = charset.newDecoder();
      * CharBuffer charBuffer = decoder.decode( byteBuffer );
      */
      Charset charset = Charset.forName(encoding);
      CharsetDecoder decoder = charset.newDecoder();
      try {
         // Make a ByteBuffer from the byte array
         ByteBuffer byteBuffer = ByteBuffer.wrap(ansiFmt.getBytes());
         // converting the Byte-Buffer to a CharBuffer using the Charset
         // define by encoding
         CharBuffer charBuffer = decoder.decode(byteBuffer);
         newStr = charBuffer.toString();
         // Save as UTF8

      } catch (CharacterCodingException ex) {
         Exceptions.printStackTrace(ex);
      }

      loadDocument(newStr);
   }//GEN-LAST:event_btnConvertActionPerformed

   private void btnFindReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindReplaceActionPerformed
      if (findDialog_==null) {
           findDialog_ = new FindDialog(this, WindowManager.getDefault().getMainWindow(), false);
       }
      else {
           findDialog_.setSelectedIndex(0);
       }

      findDialog_.setVisible(true);
   }//GEN-LAST:event_btnFindReplaceActionPerformed

   private void btnQuoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuoteActionPerformed
      try {
         int xStart = editorPane_.getSelectionStart();
         int xFinish = editorPane_.getSelectionEnd();
         /**
          * To avoid that each insertion will trigger a re-rendering of the display
          * swap in a blank document and then restore the original document 
          * after the inserts:
          * 
          **/ 
        
         FmtDocumentEx doc = (FmtDocumentEx) editorPane_.getDocument();
         Document blank = new DefaultStyledDocument();
         editorPane_.setDocument(blank);
        
         doc.quoteLines(xStart, xFinish - xStart);
         
         editorPane_.setDocument(doc);

         int end = doc.getLastLineEndOffset(xStart, xFinish - xStart);
         editorPane_.setSelectionStart(end);
         editorPane_.setSelectionEnd(end);
         m_xStart = -1;
         m_xFinish = -1;
         editorPane_.grabFocus();
      } catch (BadLocationException ex) {
         Exceptions.printStackTrace(ex);
      }
   }//GEN-LAST:event_btnQuoteActionPerformed

   private void btnSyntaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSyntaxActionPerformed

      ISISFormatter formatter = null;

      String fmt = editorPane_.getText();
      if (!fmt.endsWith("\n"))
      fmt = fmt+'\n';
      formatter = ISISFormatter.getFormatter(fmt);
      if (formatter == null) {
         GuiGlobal.output(ISISFormatter.getParsingError());
      } else if (formatter.hasParsingError()) {
         GuiGlobal.output(ISISFormatter.getParsingError());
      } else {
         GuiGlobal.output(ISISFormatter.getParsingMsg());
      }
   }//GEN-LAST:event_btnSyntaxActionPerformed

   private void btnGenerateHTMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateHTMLActionPerformed
      try {

         HtmlFormatDialog dialog = new HtmlFormatDialog(WindowManager.getDefault().getMainWindow(), true);
         dialog.addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e) {
               //System.exit(0);
            }
         });
         dialog.setVisible(true);
         int res = dialog.getChoice();
         if (res == -1) {
            return;
         }
         FieldDefinitionTable fdt = db_.getFieldDefinitionTable();
         if (fdt == null) {
            return;
         }
         String s = null;
         if (res == HtmlFormatDialog.HTML_NORMAL_FORMAT) {
            s = HtmlFormat.normalHtmlFormat(fdt);
         } else if (res == HtmlFormatDialog.HTML_TABLE_FORMAT) {
            s = HtmlFormat.tableHtmlFormat(fdt);
         }
         loadDocument(s);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
   }//GEN-LAST:event_btnGenerateHTMLActionPerformed

   private void cmbPFTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPFTActionPerformed
      if (cmbPFT.getSelectedItem().toString().equals(currentPft_.getName())) {
         return;
      }
//      if (cmbPFT.getSelectedItem().toString().equals("RAW")) {
//         //cmbPFT.setSelectedItem(currentPft_.getName());
//         return;
//      }
      checkFormatSaved();

      final FmtEditor ed = (FmtEditor) editorPane_;
      try {

         currentPft_.setName(cmbPFT.getSelectedItem().toString());
         currentPft_.setFormat(db_.getPrintFormat(currentPft_.getName()));
         editorPane_.setName(currentPft_.getName());
         loadDocument(currentPft_.getFormat());

         undoRedoManager_.discardAllEdits();
         ed.setDocumentUnchanged();
         m_xStart = -1;
         m_xFinish = -1;
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               ed.setCaretPosition(0);
               ed.scrollRectToVisible(new Rectangle(0,0,1,1));
               ed.grabFocus();
            }
         });
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
   }//GEN-LAST:event_cmbPFTActionPerformed

   private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
      String label = NbBundle.getMessage(FmtManagerTopComponent.class, "MSG_DeletePftLabel");
      String title = NbBundle.getMessage(FmtManagerTopComponent.class, "MSG_DeletePftDialogTitle");

      NotifyDescriptor d =
              new NotifyDescriptor.Confirmation(label, title,
              NotifyDescriptor.OK_CANCEL_OPTION);
      String pftName = currentPft_.getName();
      if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
         try {

            db_.removePrintFormat(pftName);
            resetDefault();
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }

         String msg = NbBundle.getMessage(FmtManagerTopComponent.class,
                 "MSG_PftSuccessfullyDeleted", pftName);
         GuiGlobal.output(msg);
      }

   
   }//GEN-LAST:event_btnDeleteActionPerformed

   private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed

      try {
         currentPft_.setFormat(editorPane_.getText());
         db_.savePrintFormat(currentPft_.getName(), currentPft_.getFormat());
         String msg = NbBundle.getMessage(FmtManagerTopComponent.class,
                 "MSG_PftSuccessfullySaved", currentPft_.getName());
         GuiGlobal.output(msg);
         FmtEditor ed = (FmtEditor) editorPane_;
         ed.setDocumentUnchanged();
      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }//GEN-LAST:event_btnSaveActionPerformed

   private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed

      String label = NbBundle.getMessage(FmtManagerTopComponent.class, "MSG_NewFormat");
      String title = NbBundle.getMessage(FmtManagerTopComponent.class, "MSG_NewFormatTitle");

      NotifyDescriptor.InputLine d =
      new NotifyDescriptor.InputLine(label, title);
      if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
         if (pftNameExist(d.getInputText())) {
            String errorMsg = NbBundle.getMessage(FmtManagerTopComponent.class, "MSG_PftExist");
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
            return;
         }
         PrintFormat pft = new PrintFormat(d.getInputText(), "");

         try {
            /** Save the empty format on the server */
            db_.savePrintFormat(pft.getName(), pft.getFormat());

            /** Reload the list of format names from the server */
            String[] pftNames = getPrintFormatNames();
            /** Reset the model with the new list of format names */
            cmbPFT.setModel(new DefaultComboBoxModel(pftNames));
            cmbPFT.setSelectedItem(pft.getName());
            currentPft_.setName(cmbPFT.getSelectedItem().toString());
            currentPft_.setFormat(db_.getPrintFormat(currentPft_.getName()));
            loadDocument(currentPft_.getFormat());
            m_xStart = -1;
            m_xFinish = -1;
            final FmtEditor ed = (FmtEditor) editorPane_;
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  ed.setCaretPosition(0);
                  ed.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
                  ed.grabFocus();
               }
            });
         } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
         } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
         }
      }
   }//GEN-LAST:event_btnNewActionPerformed

    private void sliderFontSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderFontSizeStateChanged
       try {
           int fontSize = sliderFontSize.getValue();
           FmtDocumentEx doc = (FmtDocumentEx) editorPane_.getDocument();
           doc.setFontSize(fontSize);
           
           MutableAttributeSet attrs = editorPane_.getInputAttributes();
           
           StyleConstants.setFontSize(attrs, fontSize);
           
           doc.processChangedLines(0, doc.getLength()+1);
       } catch (BadLocationException ex) {
           Exceptions.printStackTrace(ex);
       }
    }//GEN-LAST:event_sliderFontSizeStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConvert;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnFindReplace;
    private javax.swing.JButton btnGenerateHTML;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnQuote;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnShowRecords;
    private javax.swing.JButton btnSyntax;
    private javax.swing.JComboBox cmbPFT;
    private javax.swing.JPanel crudPanel;
    private javax.swing.JPanel ctrlPanel;
    private javax.swing.JTextPane editorPane_;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JScrollPane editorScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lbltSelectFormat;
    private javax.swing.JSlider sliderFontSize;
    // End of variables declaration//GEN-END:variables

   /**
    * Gets default instance. Do not use directly: reserved for *.settings files only,
    * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
    * To obtain the singleton instance, use {@link #findInstance}.
    */
    public static synchronized FmtManagerTopComponent getDefault() {
        if (instance != null) {
            instance.close();
            instance = null;
        }

         ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();

        if (connectionInfo.getDefaultDatabase() != null && instance == null) {
            instance = new FmtManagerTopComponent(connectionInfo.getDefaultDatabase());
        }

        return instance;

    }

   /**
    * Obtain the FmtManagerTopComponent instance. Never call {@link #getDefault} directly!
    */
   public static synchronized FmtManagerTopComponent findInstance() {
      TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
      if (win == null) {
         Logger.getLogger(FmtManagerTopComponent.class.getName()).warning(
                 "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
         return getDefault();
      }
      if (win instanceof FmtManagerTopComponent) {
         return (FmtManagerTopComponent) win;
      }
      Logger.getLogger(FmtManagerTopComponent.class.getName()).warning(
              "There seem to be multiple components with the '" + PREFERRED_ID +
              "' ID. That is a potential source of errors and unexpected behavior.");
      return getDefault();
   }

   @Override
   public int getPersistenceType() {
      return TopComponent.PERSISTENCE_NEVER;
   }

  
 @Override
   public void componentClosed() {
      out_.close();
      
   }
 
   
   @Override
   public boolean canClose() {
      if (!checkFormatSaved()) {
         return false;
      }
      return true;
   }
  

   /** replaces this in object stream */
   @Override
   public Object writeReplace() {
      return new ResolvableHelper();
   }

   @Override
   protected String preferredID() {
      return PREFERRED_ID;
   }

   @Override
   protected void componentActivated() {
      editorPane_.grabFocus();
   }

   final static class ResolvableHelper implements Serializable {

      private static final long serialVersionUID = 1L;

      public Object readResolve() {
         return FmtManagerTopComponent.getDefault();
      }
   }

   /**
    * Showing/hiding busy cursor, before this funcionality was in Rave winsys,
    * the code is copied from that module.
    * It needs to be called from event-dispatching thread to work synch,
    * otherwise it is scheduled into that thread. */
   static void showBusyCursor(final boolean busy) {
      if (SwingUtilities.isEventDispatchThread()) {
         doShowBusyCursor(busy);
      } else {
         SwingUtilities.invokeLater(new Runnable() {

            public void run() {
               doShowBusyCursor(busy);
            }
         });
      }
   }

   private static void doShowBusyCursor(boolean busy) {
      JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
      if (busy) {
         RepaintManager.currentManager(mainWindow).paintDirtyRegions();
         mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         mainWindow.getGlassPane().setVisible(true);
         mainWindow.repaint();
      } else {
         mainWindow.getGlassPane().setVisible(false);
         mainWindow.getGlassPane().setCursor(null);
         mainWindow.repaint();
      }
   }
   
    /** Called when the selection is removed: disable cut/copy */
   void disableCutCopy() {
      CutAction cutAction = SystemAction.get(CutAction.class);
      CopyAction copyAction = SystemAction.get(CopyAction.class);
      cutAction.setEnabled(false);
      copyAction.setEnabled(false);
   }

   /** Called when a selection is detected by editorPane_ CaretListenerSelection */
   void enableCutCopy() {
      CutAction cutAction = SystemAction.get(CutAction.class);
      CopyAction copyAction = SystemAction.get(CopyAction.class);
      cutAction.setEnabled(true);
      copyAction.setEnabled(true);
   }

}
