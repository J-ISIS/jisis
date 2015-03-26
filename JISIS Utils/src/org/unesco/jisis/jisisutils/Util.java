/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.jisisutils;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author jc_dauphin
 */
public class Util {
    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();

            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        return (path.delete());
    }
}
