/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;



/**
 *
 * @author jcd
 */


/* ImageFilter.java is used by FileChooserDemo2.java. */
public class ImageFilter extends FileFilter {

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = ImagesUtil.getExtension(f);
        if (extension != null) {
            if (extension.equals(ImagesUtil.tiff) ||
                extension.equals(ImagesUtil.tif) ||
                extension.equals(ImagesUtil.gif) ||
                extension.equals(ImagesUtil.jpeg) ||
                extension.equals(ImagesUtil.jpg) ||
                extension.equals(ImagesUtil.png)) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Images";
    }
}

