/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

/**
 *
 * @author jcd
 */
import java.io.File;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.filechooser.*;

/**
 * This class is an extension of JFileChooser which provides a simple mechanism
 * for the user to choose a file. The DirectoryServiceFileChooser is similiar
 * to a standard file chooser except that its file hierarchy is populated from a
 * generic directory service rather than the operating system's file system.
 */
public class DirectoryServiceFileChooser extends JFileChooser {

   private JButton openButton;
   private JButton cancelButton;
   static private DirectoryService directoryService;

   /**
    *  A factory method that constructs the file chooser.
    */
   public static DirectoryServiceFileChooser createDirectoryServiceFileChooser(int location) {
      //DirectoryService directoryService;
      switch (location) {
         //until other directory services are
         //implemented, just default to sports directory service
         //  case ???
         //  case ???
         default:
            //directoryService = new DirectoryServiceSportsAdapter();
      }

      FileSystemView fileSystemView = new DirectoryServiceFileSystemView(directoryService);
      FileView fileView = new DirectoryServiceFileView(directoryService);

      return new DirectoryServiceFileChooser(fileSystemView,fileView, directoryService);

   }

   public DirectoryServiceFileChooser(FileSystemView fileSystemView,
                                      FileView fileView,
                                      DirectoryService directoryService) {
      super(fileSystemView);
      setFileView(fileView);
      setMultiSelectionEnabled(true);
      this.directoryService = directoryService;
      //allow for both files and directories to be chosen
      setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

      FileFilter leagueFilter =
              new DirectoryServiceFileFilter(directoryService, "Leagues");
      FileFilter teamFilter =
              new DirectoryServiceFileFilter(directoryService, "Teams");
      FileFilter playerFilter =
              new DirectoryServiceFileFilter(directoryService, "Players");

      addChoosableFileFilter(leagueFilter);
      addChoosableFileFilter(teamFilter);
      addChoosableFileFilter(playerFilter);
      setFileFilter(getAcceptAllFileFilter());
   }

   /**
    * Specifies whether to show or hide the OPEN and
    * CANCEL buttons associated with the file chooser.
    * This method will be added to the Java 1.3 SDK API.
    */
   public void setControlButtonsAreShown(boolean visibleFlag) {
      if (visibleFlag == false) {
         if (this.openButton == null && this.cancelButton == null) {
            obtainButtons(this);
         }
         this.openButton.setVisible(false);
         this.cancelButton.setVisible(false);
      } else if (visibleFlag = true) {
         if (this.openButton == null && this.cancelButton == null) {
            obtainButtons(this);
         }
         this.openButton.setVisible(true);
         this.cancelButton.setVisible(true);
      }
   }

   /**
    * Recursively goes through the file chooser UI until
    * it finds both the OPEN and CANCEL buttons.
    */
   private void obtainButtons(Container container) {
      Component comparray[] = container.getComponents();
      for (int i = 0; (this.openButton == null || this.cancelButton == null)
              && i < comparray.length; i++) {
         if (comparray[i] instanceof JButton) {
            JButton button = (JButton) comparray[i];
            if ("Open".equalsIgnoreCase(button.getText())) {
               this.openButton = button;
            } else if ("Cancel".equalsIgnoreCase(button.getText())) {
               this.cancelButton = button;
            }
         } else if ((comparray[i]) instanceof Container) {
            obtainButtons((Container) (comparray[i]));
         }

      }

   }

   /**
    * Returns a list of selected files. This implementation
    * was obtained from http://www.spindoczine.com/sbe/
    */
   @Override
   public File[] getSelectedFiles() {
      Container c1 = (Container) getComponent(3);
      JList list = null;
      while (c1 != null) {
         Container c = (Container) c1.getComponent(0);
         if (c instanceof JList) {
            list = (JList) c;
            break;
         }
         c1 = c;
      }
      Object[] entries = list.getSelectedValues();
      File[] files = new File[entries.length];
      for (int k = 0; k < entries.length; k++) {
         if (entries[k] instanceof File) {
            File f = (File) entries[k];
            files[k] = processFilePath(f);
         }
      }
      return files;
   }

   /**
    * Processes the File and reformats it to adhere to
    * the Directory Service naming conventions.
    */
   private File processFilePath(File file) {
      File root = directoryService.getRoot();
      int rootLength = root.getParent().length();
      String fileName = file.getAbsolutePath();
      String newName = fileName.substring(rootLength,
              fileName.length());
      return new File(newName);
   }
}
