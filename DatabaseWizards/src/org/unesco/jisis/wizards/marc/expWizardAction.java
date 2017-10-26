package org.unesco.jisis.wizards.marc;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Map;
import org.openide.util.Exceptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.importexport.ExportException;
import org.unesco.jisis.importexport.ImpExpTool;
import org.unesco.jisis.gui.Util;


// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
@ActionID(id = "org.unesco.jisis.wizards.marc.expWizardAction", category = "Database")
@ActionRegistration(displayName = "#Export_Wizard_Title", lazy = false)
@ActionReference(path = "Menu/Database", position = 500)
public final class expWizardAction extends CallableSystemAction {

  
   private IDatabase db_;

   @Override
    public void actionPerformed(ActionEvent evt) {

        if (!Util.isAdmin()) {
            return;
        }
        db_ = Util.getDatabaseToUse(evt);
        if (db_ == null) {
            return;
        }
        super.actionPerformed(evt);

    }

   @Override
   public void performAction() {

      WizardDescriptor.Iterator<WizardDescriptor>  iterator = new ExpSelectWizardIterator();
      WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
      // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
      // {1} will be replaced by WizardDescriptor.Iterator.name()
      wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
      wizardDescriptor.setTitle(NbBundle.
                   getMessage(expWizardAction.class, "Export_Wizard_Title"));
      Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
      dialog.setVisible(true);
      dialog.toFront();
      boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
      if (!cancelled) {
         finished(wizardDescriptor);
      }

   }
   private boolean validateISO2709(WizardDescriptor wd) {
        String isoFile         = (String) wd.getProperty("isoFile");


        if ( isoFile== null || isoFile.equals("")) {
           NotifyDescriptor nd =
                   new NotifyDescriptor.Message(NbBundle.
                   getMessage(ExpISO2709WizardPanel.class,"Empty_ISO_FileName"));
           DialogDisplayer.getDefault().notify(nd);
           return false;
        }
        int fromMFN            = (Integer) wd.getProperty("fromMFN");
        int toMFN              = (Integer) wd.getProperty("toMFN");
        int outputLineLength   = (Integer) wd.getProperty("outputLineLength");
        String reformattingFST = (String) wd.getProperty("reformattingFST");
        int renumberFromMFN    = (Integer) wd.getProperty("renumberFromMFN");
        int outputTagMFN       = (Integer) wd.getProperty("outputTagMFN");
        String encoding        = (String) wd.getProperty("encoding");

        int fieldTerminator    = (Integer) wd.getProperty("fieldTerminator");
        int recordTerminator   = (Integer) wd.getProperty("recordTerminator");
        int subfieldDelimiter  = (Integer) wd.getProperty("subfieldDelimiter");
        
         
//      int                 mfnSelectionOption = (Integer) parameters.get("mfnSelectionOption");
//      String              mfnRangesString    = (String) parameters.get("mfnRanges");
//     
//      int                 searchHistoryIndex = (Integer) parameters.get("searchHistoryIndex");
//      int                 markedHistoryIndex = (Integer) parameters.get("markedHistoryIndex");
        return true;
   }

   private void finished(WizardDescriptor wd) {

//      if (!validateISO2709(wd)) {
//         return;
//      }
      Map<String, Object> parameters = wd.getProperties();
      int format = (Integer) parameters.get("format");
      try {
         switch (format) {
            case Global.FORMAT_ISO2709:
                ImpExpTool.exportISO2709(db_, parameters);
                break;
            case Global.FORMAT_MARC:
               break;
            case Global.FORMAT_MARCXML:
               ImpExpTool.exportMarcXML(db_, parameters);
               break;
            case Global.FORMAT_MARC21:
               break;
            case Global.FORMAT_UNIMARC:
               break;
            case Global.FORMAT_MODS:
               ImpExpTool.exportMODS(db_, parameters);
               break;
            case Global.FORMAT_DUBLIN_CORE:
               ImpExpTool.exportDC(db_, parameters);
               break;
            case Global.FORMAT_CSV:
               break;
         }
        
      } catch (ExportException ex) {
         Exceptions.printStackTrace(ex);
      }

   }


   @Override
   public String getName() {
      return NbBundle.getMessage(expWizardAction.class, "MSG_expWizardTitle");
   }

   @Override
   public String iconResource() {
      return null;
   }

   @Override
   public HelpCtx getHelpCtx() {
      return HelpCtx.DEFAULT_HELP;
   }

   @Override
   protected boolean asynchronous() {
      return false;
   }
}

