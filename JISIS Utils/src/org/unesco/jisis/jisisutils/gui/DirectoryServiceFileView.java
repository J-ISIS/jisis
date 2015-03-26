/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

/**
 *
 * @author jcd
 */
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;

/**
 * A concrete class that is implemented to
 * provide the filechooser with ui information.
 */
public class DirectoryServiceFileView extends FileView {

   private Image iconLeague =
           createImage("/images/league.gif");
   private Image iconTeam =
           createImage("/images/team.gif");
   private Image iconPlayer =
           createImage("/images/player.gif");
   private DirectoryService directoryService;

   public DirectoryServiceFileView(DirectoryService directoryService) {
      this.directoryService = directoryService;
   }

   /**
    * Specifies whether or not the directory can be opened.  In order to open the
    * directory, the file must be a directory and the user must have the proper
    * permissions.
    */
   public Boolean isTraversable(File file) {
      boolean flag = directoryService.isTraversable(file);
      return new Boolean(flag);
   }

   /**
    * Returns a description of the type of the file, such as "Directory".
    */
   public String getTypeDescription(File file) {
      return getFileDescription(file);
   }

   /**
    * Returns a description of the file.  The description could be something of a
    * short abstract of the file's contents.
    */
   public String getDescription(File file) {
      return getFileDescription(file);
   }

   /**
    * Returns a String representation of the file.
    */
   private String getFileDescription(File file) {
      String desc = null;
      int type = directoryService.getType(file);
      switch (type) {
         case DirectoryService.TYPE_LEAGUE:
            desc = "League";
            break;
         case DirectoryService.TYPE_TEAM:
            desc = "Team";
            break;
         case DirectoryService.TYPE_PLAYER:
            desc = "Player";
            break;
      }
      return desc;
   }

   /**
    * Returns a String representation of the file.
    */
   public String getName(File file) {
      if (file != null) {
         String name = file.getName();
         return name.equals("") ? file.getPath()
                 : name;
      }
      return null;
   }

   /**
    * Returns an icon appropriate for the file.
    */
   public Icon getIcon(File file) {
      Image icon = null;
      int type = directoryService.getType(file);
      switch (type) {
         case DirectoryService.TYPE_LEAGUE:
            icon = this.iconLeague;
            break;
         case DirectoryService.TYPE_TEAM:
            icon = this.iconTeam;
            break;
         case DirectoryService.TYPE_PLAYER:
            icon = this.iconPlayer;
            break;
      }
      return (icon != null) ? new ImageIcon(icon) : null;
   }

   public Image createImage(String filePath) {
      Image image = null;
      try {
         BufferedInputStream in = new BufferedInputStream(getClass().
                 getResourceAsStream(filePath));
         byte[] imageData = new byte[5000];
         in.read(imageData);
         image = Toolkit.getDefaultToolkit().
                 createImage(imageData);
      } catch (IOException ioe) {
      }
      return image;
   }
}
