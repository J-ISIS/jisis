/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.wizards.marc;

import org.openide.WizardDescriptor;

import java.awt.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.unesco.jisis.corelib.common.Global;

public final class ExpSelectWizardIterator implements WizardDescriptor.Iterator {
   // To invoke this wizard, copy-paste and run the following code, e.g. from
   // SomeAction.performAction():
   /*
    * WizardDescriptor.Iterator iterator = new expSelectWizardIterator();
    * WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
    * // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
    * // {1} will be replaced by WizardDescriptor.Iterator.name()
    * wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
    * wizardDescriptor.setTitle("Your wizard dialog title here");
    * Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
    * dialog.setVisible(true);
    * dialog.toFront();
    * boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
    * if (!cancelled) {
    * // do something
    * }
    */
   // Keep track of the panels and selected panel:
   private transient int                      index  = 0;
   private transient WizardDescriptor.Panel[] panels = null;

   protected WizardDescriptor.Panel[] createPanels() {
      return new WizardDescriptor.Panel[] {
         new ExpSelectFormatWizardPanel(), new ExpISO2709WizardPanel(), new ExpMarcXmlWizardPanel(),
         new ExpMODSWizardPanel(), new ExpDCWizardPanel()
      };
   }

   // And the list of step names:
   protected String[] createSteps() {
      return new String[] {
         "Select the Output Format", "Parameters"
      };
   }

   protected final int getIndex() {
      return index;
   }

   // Also the list of steps in the left pane:
   private transient String[] steps = null;

   protected final String[] getSteps() {
      if (steps == null) {
         steps = createSteps();
      }
      return steps;
   }

   private int getSelectedStepIndex(int i) {
      switch (i) {
      case 0 :
         // Always show the 1st "Export Format Selection" panel
         return 0;
      case 1 :
      case 2 :
      case 3 :
      case 4 :
         // Should have selected "Parameter" step for Panel indexes >0
         return 1;
      default :
         throw new NoSuchElementException();
      }
   }

   /**
    * Initialize panels representing individual wizard's steps and sets
    * various properties for them influencing wizard appearance.
    */
   private WizardDescriptor.Panel[] getPanels() {
      if (panels == null) {
         panels = createPanels();
         steps  = getSteps();
         for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) {    // assume Swing components
               JComponent jc = (JComponent) c;
               // Sets step number of a component
               // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
               jc.putClientProperty("WizardPanel_contentSelectedIndex",
                                    new Integer(getSelectedStepIndex(i)));
               // Sets steps names for a panel
               jc.putClientProperty("WizardPanel_contentData", steps);
               // Turn on subtitle creation on each step
               jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
               // Show steps on the left side with the image on the background
               jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
               // Turn on numbering of all steps
               jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
            }
         }
      }
      return panels;
   }

   // --- WizardDescriptor.Iterator METHODS: ---
   // Note that this is very similar to WizardDescriptor.Iterator, but with a
   // few more options for customization. If you e.g. want to make panels appear
   // or disappear dynamically, go ahead.

   /** Get the name of the current panel. */
// public String name() {
//      return NbBundle.getMessage(expSelectWizardIterator.class, "TITLE_x_of_y",
//      new Integer(index + 1), new Integer(getPanels().length));
//  }
   public String name() {
      switch (index) {
         case 0:
            // Always show the 1st "Export Format Selection" panel
            return "1";
         case 1:
            // Should have selected ISO2709 in First panel
            return "1.1";
         case 2:
            // Should have selected MARCXML in First panel
            return "1.2";
         case 3:
            // Should have selected MODS in First panel
            return "1.3";
         case 4:
            // Should have selected DC in First panel
            return "1.4";
         default:
            throw new NoSuchElementException();
      }
   }

   private boolean showing(int index) throws NoSuchElementException {
      ExpSelectFormatWizardPanel selectWizardPanel = (ExpSelectFormatWizardPanel) panels[0];
      int format = selectWizardPanel.getFormat();
      switch (index) {
         case 0:
            // Always show the 1st "Export Format Selection" panel
            return true;
         case 1:
            // Should have selected ISO2709 in First panel
            return (format == Global.FORMAT_ISO2709)
                    ? true
                    : false;
         case 2:
            return (format == Global.FORMAT_MARCXML)
                    ? true
                    : false;
         case 3:
            return (format == Global.FORMAT_MODS)
                    ? true
                    : false;
         case 4:
            return (format == Global.FORMAT_DUBLIN_CORE)
                    ? true
                    : false;
         default:
            throw new NoSuchElementException();
      }
   }

   public boolean hasNext() {
      for (int i = index + 1; i < panels.length; i++) {
         if (showing(i)) {
            return true;
         }
      }
      return false;
   }

   public boolean hasPrevious() {
      return index > 0;
   }

   public void nextPanel() {
      index++;
      while (!showing(index)) {
         index++;
      }
      if (index == 1) {
         // User finished intro panel, list of panels may have changed:
         fireChangeEvent();
      }
   }

   public void previousPanel() {
      index--;
      while (!showing(index)) {
         index--;
      }
   }

   public WizardDescriptor.Panel current() {
      return getPanels()[index];
   }

   // If nothing unusual changes in the middle of the wizard, simply:
// public void addChangeListener(ChangeListener l) {
// }
//
// public void removeChangeListener(ChangeListener l) {
// }
   // If something changes dynamically (besides moving between panels), e.g.
   // the number of panels changes in response to user input, then uncomment
   // the following and call when needed: fireChangeEvent();
   private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);    // or can use ChangeSupport in NB 6.0

   public final void addChangeListener(ChangeListener l) {
      synchronized (listeners) {
         listeners.add(l);
      }
   }

   public final void removeChangeListener(ChangeListener l) {
      synchronized (listeners) {
         listeners.remove(l);
      }
   }

   protected final void fireChangeEvent() {
      Iterator<ChangeListener> it;
      synchronized (listeners) {
         it = new HashSet<ChangeListener>(listeners).iterator();
      }
      ChangeEvent ev = new ChangeEvent(this);
      while (it.hasNext()) {
         it.next().stateChanged(ev);
      }
   }
}
