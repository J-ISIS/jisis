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

public interface DirectoryService {
  public final static int TYPE_LEAGUE = 0;
  public final static int TYPE_TEAM = 1;
  public final static int TYPE_PLAYER = 2;

/**
 * Returns the children of the parent directory.
 */
  public File[] getChildren(File aDir);

/**
 * Returns the root (in this case, Baseball).
 */
  public File getRoot();

/**
 * Returns the type (for example, Player).
 */
  public int getType(File aFile);

/**
 * Returns whether the file is traversable.
 */
  public boolean isTraversable(File aFile);

/**
 * Creates a new folder in the containing directory.
 */
  public File createNewFolder(File aContainingDir);

/**
 * Determines if the file should be displayed
 * based on the current filter.
 */
  public boolean acceptFilter(File aFile,
                          String aCurrentFilter);
}

