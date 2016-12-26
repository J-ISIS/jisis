package org.unesco.jisis.database.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.PrintCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.client.ConnectionInfo;
import org.unesco.jisis.corelib.client.ConnectionPool;
import org.unesco.jisis.corelib.common.FormattedRecord;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.PrintFormat;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.DefaultDBNotFoundException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.corelib.exceptions.RecordNotFoundException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.fxbrowser.SwingFXWebView;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;
import org.unesco.jisis.jisiscore.client.MarkedRecords;
import org.unesco.jisis.jisiscore.common.AsyncCallback;

interface CallBack {

    void notifyDatabaseChanged();
}

/**
 * Top component which displays something.
 */
public class DbViewTopComponent extends TopComponent implements Observer {

   private static DbViewTopComponent instance;
   /**
    * path to the icon used by the component and its open action
    */
   static final String ICON_PATH = "org/unesco/jisis/database/explorer/view.png";
   private static final String PREFERRED_ID = "DbViewTopComponent";
   private ClientDatabaseProxy db_ = null;
   private PrintFormat currentPft_;
   //private Browser newWebBrowser_ = null;
   private long currentMfn_ = 0;
   private ArrayList<Long> markedRecords_ = new ArrayList<Long>();
   private SwingFXWebView swingFXWebView = null;
   
   private DbChangedControl poll_;
   private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DbViewTopComponent.class);


 
   //private IRecord _rec = null;
   public DbViewTopComponent(IDatabase db) {


      if (db instanceof ClientDatabaseProxy) {
         db_ = (ClientDatabaseProxy) db;
      } else {
         throw new RuntimeException("RecordDataBrowserTopComponent: Cannot cast DB to ClientDatabaseProxy");
      }
      /* Register this TopComponent as attached to this DB */
      db_.addWindow(this);
        /* Add this TopComponent as Observer to DB changes */
      db_.addObserver((Observer) this);
     
      try {
         long recCount = db_.getRecordsCount();
         long maxMFN = db_.getLastMfn();
//         System.out.println("DbViewTopComponent recCount=" + recCount +
//                 " maxMFN=" + maxMFN);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
      initComponents();

      putClientProperty("print.printable", Boolean.TRUE); // NOI18N
      webPanel.setPreferredSize(new Dimension(700, 500));

      if (swingFXWebView == null) {
         swingFXWebView = new SwingFXWebView();
         webPanel.add(swingFXWebView, BorderLayout.CENTER);
      }

      putClientProperty("print.printable", Boolean.TRUE); // NOI18N

 
      setName(NbBundle.getMessage(DbViewTopComponent.class, "CTL_DbViewTopComponent"));
      setToolTipText(NbBundle.getMessage(DbViewTopComponent.class, "HINT_DbViewTopComponent"));
      setIcon(ImageUtilities.loadImage(ICON_PATH, true));

//      btnFirst.setIcon(new ImageIcon(ImageUtilities.loadImage(DOUBLE_LEFT_PATH, true)));
//      btnPrev.setIcon(new ImageIcon(ImageUtilities.loadImage(ONE_LEFT_PATH, true)));
//      btnNext.setIcon(new ImageIcon(ImageUtilities.loadImage(ONE_RIGHT_PATH, true)));
//      btnLast.setIcon(new ImageIcon(ImageUtilities.loadImage(DOUBLE_RIGHT_PATH, true)));

      getPfts();
      try {
         long maxMFN = db_.getLastMfn();
         txtMaxMfn.setText(maxMFN + "");
         setName(NbBundle.getMessage(DbViewTopComponent.class, "CTL_DbViewTopComponent") 
                 + " (" +db.getDbHome()+"//"+ db_.getDatabaseName() + ")");
         displayFirstRecord();

      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }
      
//       try {
//           poll_ = new DbChangedControl(db_, this, new CallBack() {
//
//               @Override
//               public void notifyDatabaseChanged() {
//
//                   final NotifyDescriptor d
//                       = new NotifyDescriptor.Message(NbBundle.getMessage(DbViewTopComponent.class,
//                           "MSG_DATABASE_CHANGED_BY_ANOTHER_PROCESS"));
//                   DialogDisplayer.getDefault().notify(d);
//
//               }
//
//           });
//           poll_.notifyIfDbChanged();
//       } catch (DbException ex) {
//           Exceptions.printStackTrace(ex);
//       }
      
      associateLookup(new JisisPrintNode().getLookup());
   }

   private  void createAndShowMenu(final JComponent component, final AbstractButton moreButton) {
      JPopupMenu menuMark = new JPopupMenu();
      JMenuItem clearMarks = new JMenuItem(NbBundle.getMessage(DbViewTopComponent.class, "MSG_ClearMarkedRecords"));
      ActionListener clearMarkedRecordsListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (markedRecords_.isEmpty()) {
               return;
            }
            int n = markedRecords_.size();
            markedRecords_.clear();
            chkMarkRecord.setSelected(false);
            String msg = n+" "+NbBundle.getMessage(DbViewTopComponent.class,
                    "MSG_MarkedRecordsCleared");
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg);
            DialogDisplayer.getDefault().notify(d);
         }
      };
      clearMarks.addActionListener(clearMarkedRecordsListener);
      menuMark.add(clearMarks);
      JMenuItem saveMarksMenuItem = new JMenuItem(NbBundle.getMessage(DbViewTopComponent.class, "MSG_SaveMarkedRecords"));
      ActionListener saveMarksListener = new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (markedRecords_.isEmpty()) {
               return;
            }
            String markedSetName = "marked";
            MarkedRecords markedRecords = new MarkedRecords(0, db_.getDbName(), markedSetName, markedRecords_);
            db_.addMarkedRecords(markedRecords);
             int n = markedRecords_.size();
             String msg = n+" "+NbBundle.getMessage(DbViewTopComponent.class,
                    "MSG_MarkedRecordsSaved");
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg);
            DialogDisplayer.getDefault().notify(d);
         }
      };
      saveMarksMenuItem.addActionListener(saveMarksListener);
      menuMark.add(saveMarksMenuItem);
      menuMark.show(component, 0, component.getHeight());
   }


   private void getPfts() {
      try {
         String[] pftNames = db_.getPrintFormatNames();
         boolean userRawPft = false;
         for (String name : pftNames) {
            if (name.equals("RAW")) {
               userRawPft = true;
               break;
            }
         }
         String pftName = "RAW";
         String pftFormat = "";
         currentPft_ = new PrintFormat(pftName, pftFormat);
         cmbSelectPft.setModel(new DefaultComboBoxModel(pftNames));

      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }
   }


   public void printPage() {
       
      //newWebBrowser_.executeJavascript("window.print();");
   }
   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      mainPanel = new javax.swing.JPanel();
      ctrlPanel = new javax.swing.JPanel();
      jToolBar1 = new javax.swing.JToolBar();
      lblMFN = new javax.swing.JLabel();
      txtMFN = new javax.swing.JTextField();
      jSeparator1 = new javax.swing.JToolBar.Separator();
      btnFirst = new javax.swing.JButton();
      jSeparator3 = new javax.swing.JToolBar.Separator();
      btnPrev = new javax.swing.JButton();
      jSeparator4 = new javax.swing.JToolBar.Separator();
      btnNext = new javax.swing.JButton();
      jSeparator5 = new javax.swing.JToolBar.Separator();
      btnLast = new javax.swing.JButton();
      jSeparator6 = new javax.swing.JToolBar.Separator();
      lblFormat = new javax.swing.JLabel();
      cmbSelectPft = new javax.swing.JComboBox();
      jSeparator2 = new javax.swing.JToolBar.Separator();
      lblMaxMfn = new javax.swing.JLabel();
      txtMaxMfn = new javax.swing.JTextField();
      jToolBar2 = new javax.swing.JToolBar();
      btnMarkMenu = new javax.swing.JButton();
      chkMarkRecord = new javax.swing.JCheckBox();
      webPanel = new javax.swing.JPanel();

      setLayout(new java.awt.BorderLayout());

      ctrlPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      ctrlPanel.setPreferredSize(new java.awt.Dimension(100, 100));

      jToolBar1.setFloatable(false);
      jToolBar1.setRollover(true);

      org.openide.awt.Mnemonics.setLocalizedText(lblMFN, org.openide.util.NbBundle.getMessage(DbViewTopComponent.class, "LBL_MFN")); // NOI18N
      jToolBar1.add(lblMFN);

      txtMFN.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
      txtMFN.setText("1");
      txtMFN.setToolTipText(org.openide.util.NbBundle.getMessage(DbViewTopComponent.class, "HINT_MFN")); // NOI18N
      txtMFN.setMaximumSize(new java.awt.Dimension(100, 100));
      txtMFN.setPreferredSize(new java.awt.Dimension(40, 20));
      txtMFN.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            txtMFNActionPerformed(evt);
         }
      });
      txtMFN.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyPressed(java.awt.event.KeyEvent evt) {
            txtMFNKeyPressed(evt);
         }
      });
      jToolBar1.add(txtMFN);
      jToolBar1.add(jSeparator1);

      btnFirst.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/database/explorer/2leftarrow.png"))); // NOI18N
      btnFirst.setToolTipText(org.openide.util.NbBundle.getMessage(DbViewTopComponent.class, "HINT_First")); // NOI18N
      btnFirst.setPreferredSize(new java.awt.Dimension(30, 25));
      btnFirst.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnFirstActionPerformed(evt);
         }
      });
      jToolBar1.add(btnFirst);
      jToolBar1.add(jSeparator3);

      btnPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/database/explorer/1leftarrow.png"))); // NOI18N
      btnPrev.setToolTipText(org.openide.util.NbBundle.getMessage(DbViewTopComponent.class, "HINT_Prev")); // NOI18N
      btnPrev.setPreferredSize(new java.awt.Dimension(30, 25));
      btnPrev.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnPrevActionPerformed(evt);
         }
      });
      jToolBar1.add(btnPrev);
      jToolBar1.add(jSeparator4);

      btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/database/explorer/1rightarrow.png"))); // NOI18N
      btnNext.setToolTipText(org.openide.util.NbBundle.getMessage(DbViewTopComponent.class, "HINT_Next")); // NOI18N
      btnNext.setPreferredSize(new java.awt.Dimension(30, 25));
      btnNext.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnNextActionPerformed(evt);
         }
      });
      jToolBar1.add(btnNext);
      jToolBar1.add(jSeparator5);

      btnLast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/unesco/jisis/database/explorer/2rightarrow.png"))); // NOI18N
      btnLast.setToolTipText(org.openide.util.NbBundle.getMessage(DbViewTopComponent.class, "HINT_Last")); // NOI18N
      btnLast.setPreferredSize(new java.awt.Dimension(30, 25));
      btnLast.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnLastActionPerformed(evt);
         }
      });
      jToolBar1.add(btnLast);
      jToolBar1.add(jSeparator6);

      org.openide.awt.Mnemonics.setLocalizedText(lblFormat, "Format:");
      jToolBar1.add(lblFormat);

      cmbSelectPft.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
      cmbSelectPft.setMaximumSize(new java.awt.Dimension(150, 32767));
      cmbSelectPft.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            cmbSelectPftActionPerformed(evt);
         }
      });
      jToolBar1.add(cmbSelectPft);
      jToolBar1.add(jSeparator2);

      org.openide.awt.Mnemonics.setLocalizedText(lblMaxMfn, org.openide.util.NbBundle.getMessage(DbViewTopComponent.class, "LBL_Max")); // NOI18N
      jToolBar1.add(lblMaxMfn);

      txtMaxMfn.setEditable(false);
      txtMaxMfn.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
      txtMaxMfn.setMaximumSize(new java.awt.Dimension(100, 2147483647));
      txtMaxMfn.setPreferredSize(new java.awt.Dimension(40, 20));
      jToolBar1.add(txtMaxMfn);

      jToolBar2.setFloatable(false);
      jToolBar2.setRollover(true);

      org.openide.awt.Mnemonics.setLocalizedText(btnMarkMenu, "Mark Menu");
      btnMarkMenu.setFocusable(false);
      btnMarkMenu.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      btnMarkMenu.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      btnMarkMenu.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnMarkMenuActionPerformed(evt);
         }
      });
      jToolBar2.add(btnMarkMenu);

      org.openide.awt.Mnemonics.setLocalizedText(chkMarkRecord, "Mark This Record");
      chkMarkRecord.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            chkMarkRecordActionPerformed(evt);
         }
      });
      jToolBar2.add(chkMarkRecord);

      javax.swing.GroupLayout ctrlPanelLayout = new javax.swing.GroupLayout(ctrlPanel);
      ctrlPanel.setLayout(ctrlPanelLayout);
      ctrlPanelLayout.setHorizontalGroup(
         ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(ctrlPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 616, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(50, Short.MAX_VALUE))
      );
      ctrlPanelLayout.setVerticalGroup(
         ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(ctrlPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(ctrlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(14, Short.MAX_VALUE))
      );

      webPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
      webPanel.setLayout(new java.awt.BorderLayout());

      javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
      mainPanel.setLayout(mainPanelLayout);
      mainPanelLayout.setHorizontalGroup(
         mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(webPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 914, Short.MAX_VALUE)
               .addComponent(ctrlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 912, Short.MAX_VALUE))
            .addContainerGap())
      );
      mainPanelLayout.setVerticalGroup(
         mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(mainPanelLayout.createSequentialGroup()
            .addComponent(ctrlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(webPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE))
      );

      add(mainPanel, java.awt.BorderLayout.CENTER);
   }// </editor-fold>//GEN-END:initComponents

  

    private void txtMFNKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMFNKeyPressed

       if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
          long mfn = Long.parseLong(txtMFN.getText());
          try {
             displayRecord(db_.getRecordCursorFmt(mfn, currentPft_.getName()));
          } catch (DbException ex) {
             new RecordNotFoundException(ex).displayWarning();
          }
       }
    }//GEN-LAST:event_txtMFNKeyPressed

    private void btnLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastActionPerformed

       try {
          displayRecord(db_.getLastFmt(currentPft_.getName()));
       } catch (DbException ex) {
          new RecordNotFoundException(ex).displayWarning();
       }
    }//GEN-LAST:event_btnLastActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed

       try {
          displayRecord(db_.getNextFmt(currentPft_.getName()));
       } catch (DbException ex) {
          new RecordNotFoundException(ex).silent();
       }
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed

       try {
          displayRecord(db_.getPrevFmt(currentPft_.getName()));
       } catch (DbException ex) {
          new RecordNotFoundException(ex).silent();
       }
    }//GEN-LAST:event_btnPrevActionPerformed

   private void displayFirstRecord() {
      
      try {
         Date start = new Date();

         FormattedRecord fmtRecord = db_.getFirstFmt(currentPft_.getName());

         Date end = new Date();

         LOGGER.debug(Long.toString(end.getTime() - start.getTime())
                 + " milliseconds to get first formatted record");

         start = new Date();
         displayRecord(fmtRecord);
         end = new Date();
         LOGGER.debug(Long.toString(end.getTime() - start.getTime())
                 + " milliseconds to display first record");


      } catch (DbException ex) {
         new RecordNotFoundException(ex).displayWarning();
      }
   }
    private void btnFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirstActionPerformed
       displayFirstRecord();
    }//GEN-LAST:event_btnFirstActionPerformed

    private void txtMFNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMFNActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_txtMFNActionPerformed

    private void cmbSelectPftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSelectPftActionPerformed

        try {
            // TODO add your handling code here:
            String fmtName = cmbSelectPft.getSelectedItem().toString();
            String fmt = "";

            ISISFormatter formatter = null;
             fmt = db_.getPrintFormat(fmtName);
             if (fmt == null || fmt.equals("")) {
            
                fmt = "";
            } else {
                fmt = db_.getPrintFormat(fmtName);
                formatter = ISISFormatter.getFormatter(fmt);
                if (formatter == null) {
                    GuiGlobal.output(ISISFormatter.getParsingError());
                    return;
                } else if (formatter.hasParsingError()) {
                    GuiGlobal.output(ISISFormatter.getParsingError());
                    return;
                }
            }
            currentPft_.setName(fmtName);
            currentPft_.setFormat(fmt);
            currentPft_.setFormatter(formatter);

        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            displayRecord(db_.getRecordFmt(currentMfn_, currentPft_.getName()));
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }

}//GEN-LAST:event_cmbSelectPftActionPerformed

    private void chkMarkRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkMarkRecordActionPerformed
       if (chkMarkRecord.isSelected()) {
          markedRecords_.add(currentMfn_);
       } else {
          int index = markedRecords_.indexOf(currentMfn_);
          if (index >= 0) {
             markedRecords_.remove(index);
          }
       }
    }//GEN-LAST:event_chkMarkRecordActionPerformed

    private void btnMarkMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkMenuActionPerformed
       createAndShowMenu((JComponent) evt.getSource(), btnMarkMenu);

    }//GEN-LAST:event_btnMarkMenuActionPerformed

   private void dumpField(IRecord rec, int tag, String encoding) {
      String us = null;
      byte[] bS = null;
      char quote = '"';
      int ndx;

      try {
         IField fld = rec.getField(tag);
         us = fld.getStringFieldValue();
         try {
            bS = us.getBytes(encoding);
         } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
         }


         System.out.println(quote + us + quote + "number of chars=" + us.length());
         System.out.println(
                 " Field coded as " + encoding +
                 "bS length: " + bS.length + ".");
         if (!encoding.startsWith("UTF-16")) {
            for (ndx = 0; ndx < bS.length; ndx++) {
               System.out.print(Integer.toHexString(bS[ndx]) + "   ");
               System.out.print("\n");
            }
         } else {
            for (ndx = 0; ndx < bS.length; ndx++) {
               System.out.print(Integer.toHexString(bS[ndx++]) + " ");
               System.out.print(Integer.toHexString(bS[ndx]) + "   ");
               System.out.print("\n");
            }
         }
         System.out.print("\n");

      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }
   }

    private static File temp = null;
   private URL makeTempFile(String content) {
      if (temp == null) {
         try {
            // Create temp file.
            temp = File.createTempFile("record"+currentMfn_+"-", ".html", new File(Global.getClientTempPath()));
            System.out.println("Temp creation Record url="+temp.toString());
         } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
         }
         // Delete temp file when program exits.
         temp.deleteOnExit();
      }
      try {

         // Write to temp file
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp),
                         "UTF8"));

         out.write(content);
         out.close();
      } catch (IOException e) {
      }
      URL url = null;
      try {
         url = temp.toURI().toURL();
      } catch (MalformedURLException ex) {
         Exceptions.printStackTrace(ex);
      }
      System.out.println("Record url="+url.toString());
      return url;
   }

//   @Override
//    protected void componentActivated() {
//        super.componentActivated();
//        final String OUTPUT_ID = "output";
//        TopComponent outputWindow = WindowManager.getDefault().findTopComponent(OUTPUT_ID);
//        if (outputWindow != null && outputWindow.isOpened()) {
//
//            outputWindow.requestActive();
//        }
//    }
//    
//    @Override
//    protected void componentDeactivated() {
//         
//      final String OUTPUT_ID = "output";
//      TopComponent outputWindow = WindowManager.getDefault().findTopComponent(OUTPUT_ID);
//        if (outputWindow != null && outputWindow.isOpened()) {
//
//            //outputWindow.requestActive();
//        }
//        super.componentDeactivated();     
//    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        if (currentMfn_ != db_.getCurrentRecordMfn()) {
            try {
               long mfn = db_.getCurrentRecordMfn();
               FormattedRecord formattedRecord = db_.getRecordCursorFmt(mfn, currentPft_.getName());
                displayRecord(formattedRecord);
            } catch (DbException ex) {
                new RecordNotFoundException(ex).displayWarning();
            }
        }
    }
 
   private void doDisplayRecord(final String content) {

      Runnable displayRun = new Runnable() {
         public void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
               try {
                  swingFXWebView.loadContent(content);
               } catch (Exception ex) {
                  Exceptions.printStackTrace(ex);
               } finally {
                  // clear status text
                  StatusDisplayer.getDefault().setStatusText(""); // NOI18N
                  // clear wait cursor
                  SwingUtilities.invokeLater(this);
               }

            }
         }
      };
      RequestProcessor.Task loadTask = RequestProcessor.getDefault().post(displayRun);
      loadTask.waitFinished();
   }

   private void displayRecord(FormattedRecord formattedRecord) {

      if (formattedRecord == null) {
         return;
      }
      
      final String content = formattedRecord.getRecord();
     
      if (content == null) {
         return;
      }
      long mfn = formattedRecord.getMfn();
//      System.out.println("HTML:");
//      System.out.println(content);
     
      final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
      StatusDisplayer.getDefault().setStatusText("Starting browser display ...");
      RepaintManager.currentManager(frame).paintDirtyRegions();
      
      doDisplayRecord(content);    
      
      txtMFN.setText(mfn + "");
      currentMfn_ = mfn;
      int index = markedRecords_.indexOf(currentMfn_);
      chkMarkRecord.setSelected((index >= 0) ? true : false);   
   }




//   private void mashup(String xhtmlContent) {
//
//      DOMParser parser = new DOMParser();
//      InputSource in = new InputSource(new StringReader(xhtmlContent));
//      try {
//         parser.parse(in);
//      } catch (SAXException ex) {
//         Exceptions.printStackTrace(ex);
//      } catch (IOException ex) {
//         Exceptions.printStackTrace(ex);
//      }
//      Document doc = parser.getDocument();
//      
//
//      printElements(doc);
//      printElementAttributes(doc);
//   }
//   static void printElements(Document doc) {
//      NodeList nl = doc.getElementsByTagName("*");
//      Node n;
//      for (int i = 0; i < nl.getLength(); i++) {
//         n = nl.item(i);
//         System.out.println("xml element="+n.getNodeName() + " ");
//      }
//      
//      System.out.println();
//   }
//
//   static void printElementAttributes(Document doc) {
//      NodeList nl = doc.getElementsByTagName("*");
//      Element e;
//      Node n;
//      NamedNodeMap nnm;
//      String attrname;
//      String attrval;
//      int i, len;
//      len = nl.getLength();
//      for (int j = 0; j < len; j++) {
//         e = (Element) nl.item(j);
//         System.out.println("xml attribute="+e.getTagName() + ":");
//         nnm = e.getAttributes();
//         if (nnm != null) {
//            for (i = 0; i < nnm.getLength(); i++) {
//               n = nnm.item(i);
//               attrname = n.getNodeName();
//               attrval = n.getNodeValue();
//               System.out.print(" " + attrname + " = " + attrval);
//            }
//         }
//
//
//      }
//
//
//      System.out.println();
//   }


   public void setRecordByMFN(long MFN) {
      try {
         FormattedRecord fmtRecord = db_.getRecordCursorFmt(MFN, currentPft_.getName());
         displayRecord(fmtRecord);
      } catch (DbException ex) {
         Exceptions.printStackTrace(ex);
      }

   }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton btnFirst;
   private javax.swing.JButton btnLast;
   private javax.swing.JButton btnMarkMenu;
   private javax.swing.JButton btnNext;
   private javax.swing.JButton btnPrev;
   private javax.swing.JCheckBox chkMarkRecord;
   private javax.swing.JComboBox cmbSelectPft;
   private javax.swing.JPanel ctrlPanel;
   private javax.swing.JToolBar.Separator jSeparator1;
   private javax.swing.JToolBar.Separator jSeparator2;
   private javax.swing.JToolBar.Separator jSeparator3;
   private javax.swing.JToolBar.Separator jSeparator4;
   private javax.swing.JToolBar.Separator jSeparator5;
   private javax.swing.JToolBar.Separator jSeparator6;
   private javax.swing.JToolBar jToolBar1;
   private javax.swing.JToolBar jToolBar2;
   private javax.swing.JLabel lblFormat;
   private javax.swing.JLabel lblMFN;
   private javax.swing.JLabel lblMaxMfn;
   private javax.swing.JPanel mainPanel;
   private javax.swing.JTextField txtMFN;
   private javax.swing.JTextField txtMaxMfn;
   private javax.swing.JPanel webPanel;
   // End of variables declaration//GEN-END:variables

   /**
    * Gets default instance. Do not use directly: reserved for *.settings files only,
    * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
    * To obtain the singleton instance, use {@link findInstance}.
    */
   public static synchronized DbViewTopComponent getDefault() throws DefaultDBNotFoundException {

      if (instance != null) {
         instance.close();
         instance = null;
      }
      ConnectionInfo connectionInfo = ConnectionPool.getDefaultConnectionInfo();
      if (connectionInfo.getDefaultDatabase() != null && instance == null) {
         instance = new DbViewTopComponent(connectionInfo.getDefaultDatabase());
      }
      return instance;
   }

   /**
    * Obtain the DbViewTopComponent instance. Never call {@link #getDefault} directly!
    */
   public static synchronized DbViewTopComponent findInstance() throws DefaultDBNotFoundException {
      TopComponent win = getDefault();
      if (win == null) {
         ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find DbView component. It will not be located properly in the window system.");
         return getDefault();
      }
      if (win instanceof DbViewTopComponent) {
         return (DbViewTopComponent) win;
      }
      ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
      return getDefault();
   }

   @Override
   public int getPersistenceType() {
      return TopComponent.PERSISTENCE_NEVER;
   }

//   @Override
//   protected  void 	componentActivated() {
//
//       this.toFront();
//       JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
//       frame.getRootPane().updateUI();
//
//       IRecord rec = null;
//      try {
//         rec = db_.getRecord(currentMfn_);
//      } catch (DbException ex) {
//         Exceptions.printStackTrace(ex);
//      }
//       displayRecord(rec);
//       super.componentActivated();
//   }
  

    @Override
   public void componentClosed() {
      
       //newWebBrowser_.dispose();
      db_.deleteWindow(this);
      /**
       * This instance was set as observer 
       * We need to remove it
       */
      db_.deleteObserver(this);
      
      if (poll_ != null) {
          poll_.shutDownNow();
      }
   }

   /** replaces this in object stream */
   /*
   public Object writeReplace() {
   return new ResolvableHelper();
   }
    */
    @Override
   protected String preferredID() {
      return PREFERRED_ID;
   }
   /*
   final static class ResolvableHelper implements Serializable {
   private static final long serialVersionUID = 1L;
   public Object readResolve() {
   return DbViewTopComponent.getDefault();
   }
   }
    */

 
   // We are observer for the changes in the DB and Format changes in the
   // Fmt Manager
   @Override
   public void update(Observable arg0, Object arg1) {
      /**
       * First, update pfts if any change
       */
      if (db_.pftHasChanged()) {
         // Get back pfts from the server
         getPfts();
         String pftName = currentPft_.getName();
         cmbSelectPft.setSelectedItem(pftName);
      }
      if (db_.databaseHasChanged()) {
         try {
            long maxMFN = db_.getLastMfn();
            txtMaxMfn.setText(maxMFN + "");
            // Be sure to be on the record displayed, because another window
            // may have change the record cursor
            IRecord record = db_.getRecordCursor(currentMfn_);
            displayRecord(db_.getCurrentFmt(currentPft_.getName()));
         } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
         }
      }
      
   }

    class JisisPrintCookie implements PrintCookie {

       @Override
       public void print() {
           JOptionPane.showMessageDialog(null, "I am printing...");
           printPage();
       }

   }
   class JisisPrintNode  extends AbstractNode {
       public JisisPrintNode() {
           super(Children.LEAF);
           CookieSet cookies = getCookieSet();
           cookies.add(new JisisPrintCookie());
       }
   }
   
   
    
    class DbChangedControl {

        private final ClientDatabaseProxy db_;
        private long recordsCount_;
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private final CallBack callBack_;
        private final TopComponent topComponent_;

        public DbChangedControl(ClientDatabaseProxy db, TopComponent topComponent, CallBack callBack) throws DbException {
            db_ = db;
            callBack_ = callBack;
            topComponent_ = topComponent;

            recordsCount_ = db_.getRecordsCount();

        }

        public void notifyIfDbChanged() {
            final Runnable notify = new Runnable() {
                @Override
                public void run() {
                    long recordsCount = 0;
                    try {
                        recordsCount = db_.getRecordsCount();

                        if (recordsCount != recordsCount_) {

                            recordsCount_ = recordsCount;
                            long maxMFN = db_.getLastMfn();
                            txtMaxMfn.setText(maxMFN + "");
                            /**
                             * Inform only if current TopComponent is data viewer
                             */
                            if (topComponent_.isFocusOwner()) {
                                callBack_.notifyDatabaseChanged();
                            }

                        }
                    } catch (DbException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            };
            final ScheduledFuture<?> notifyHandle = scheduler.scheduleAtFixedRate(notify,
                10, // initialDelay - the time to delay first execution 
                10, // period - the period between successive executions
                SECONDS);

            scheduler.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        notifyHandle.cancel(true);
                    }
                },
                60 * 60 * 24, // the time from now to delay execution
                SECONDS);
        }
        
        public void shutDownNow() {
            scheduler.shutdownNow();
        }
    }
}
