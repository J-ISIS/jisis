/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

/**
 *
 * @author jcd
 */
import java.io.*;
import javax.swing.filechooser.*;

/**
 * This class provides a view into a generic
 * directory service.
 */
public class DirectoryServiceFileSystemView extends FileSystemView {

   private DirectoryService directoryService;

   public DirectoryServiceFileSystemView(DirectoryService directoryService) {

      this.directoryService = directoryService;
   }

   /**
    * Creates a new folder with a default folder name.
    * This method calls into the currently opened
    * directory service to obtain the directory service
    * system specific way to create a new folder.
    */
   public File createNewFolder(File containingDir)
           throws IOException {
      File f = directoryService.createNewFolder(containingDir);
      return f;
   }

   /**
    * Returns all root partitions on this system.
    */
   public File[] getRoots() {
      File[] files = new File[1];
      files[0] = directoryService.getRoot();
      return files;
   }

   /**
    * Returns whether a file is hidden or not.
    * In the current system, there is no concept of
    * a hidden file.
    */
   public boolean isHiddenFile(File f) {
      return false;
   }

   /**
    * Determines if the file is a root partition.
    */
   public boolean isRoot(File f) {
      File root = directoryService.getRoot();
      String rootName = root.getName();
      String fName = f.getName();
      return (rootName.equals(fName));
   }

   /**
    * Returns the user's home directory.
    * The concept of a user's home directory
    * does not directly map to the Directory Service,
    * so, as a result, the root is always returned.
    */
   public File getHomeDirectory() {
      return directoryService.getRoot();
   }

   /**
    * Gets the list of files. Invoked internally
    * by the file chooser when the directory
    * is changed.
    */
   public File[] getFiles(File dir,
           boolean useFileHiding) {
      File[] files =
              directoryService.getChildren(dir);
      return files;
   }
}
