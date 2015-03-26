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
import javax.swing.filechooser.*;

/**
 * A FileFilter can be set on a file chooser to
 * keep unwanted files from appearing in the
 * directory listing.
 */
public class DirectoryServiceFileFilter extends FileFilter {

   private DirectoryService directoryService_;
   private String description_;

   public DirectoryServiceFileFilter(DirectoryService directoryService,
           String description) {
      this.directoryService_ = directoryService;
      this.description_ = description;
   }

   /**
    * Whether the given file is accepted by this filter.
    */
   public boolean accept(File file) {
      if (file != null) {
         return directoryService_.acceptFilter(file, description_);
      }
      return false;
   }

   /**
    * The description of this filter. For example: "Teams" or "Players".
    */
   public String getDescription() {
      return description_;
   }
}
