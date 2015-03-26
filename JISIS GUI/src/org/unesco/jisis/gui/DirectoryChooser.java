/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.gui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;
import org.openide.util.NbBundle;

/**
 *
 * @author jcd
 */

public class DirectoryChooser extends JFileChooser {


    /**
     * Constructor
     *
     */
    public DirectoryChooser() {
        super();
        this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    }

    /**
     * Constructor
     *
     * @see javax.swing.JFileChooser#JFileChooser(File)
     */
    public DirectoryChooser(File starttDir) {
        super(starttDir);
        super.setDialogTitle(NbBundle.getMessage(DirectoryChooser.class, "DirectoryChooser.MSG_Select_Directory"));
        this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    /**
     * Gets the selected file
     *
     * @see javax.swing.JFileChooser#getSelectedFile()
     */
    public File getSelectedFile() {
        File    file = super.getSelectedFile();

        if (file == null) {
            return file;
        }
        String  dirName = super.getSelectedFile().getAbsolutePath();
        String  fileSep = System.getProperty("file.separator", "\\");

        if (dirName.endsWith(fileSep + ".")) {
            dirName = dirName.substring(0, dirName.length() - 2);
            file = new File(dirName);
        }
        return file;
    }

    /**
     * Sets the currect Directory
     *
     * @see javax.swing.JFileChooser#setCurrentDirectory(File)
     */
    @Override
    public void setCurrentDirectory(File file) {
        // Let the action take place, then override the 'Name' field.
        // This is equivalent to the user typing &quot;.&quot; in the name field.
        super.setCurrentDirectory(file);
        FileChooserUI uifc = getUI();

        if (uifc instanceof BasicFileChooserUI) {
            ((BasicFileChooserUI) uifc).setFileName(".");
        }
    }
}
