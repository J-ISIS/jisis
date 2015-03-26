/*
 * AnyTermFinder.java
 *
 * Created on 10 settembre 2007, 9.55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.core.searchengine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author triverio
 */
public class AnyTermFinder {
    
    private String key;
    /** Creates a new instance of AnyTermFinder */
    public AnyTermFinder() {
    }

        /** Creates a new instance of AnyTermFinder */
    public AnyTermFinder(String key) {
        this.key = key;
    }
    
    
    public String findAnyTerm(){
        String terms = new String();
        Map anyCouples = this.readAnyTermFile();
        terms = (String) anyCouples.get(key);
        return terms;
    }

    private Map readAnyTermFile() {
        Map result = new HashMap();
        String str = "";
        String[] keyValue = new String[2];
         File f = new File("conf/anyFile.dat");
        try {
           
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis);
            // prendere il model da fdtFile
            BufferedReader in = new BufferedReader(isr);
            //BufferedReader in = new BufferedReader(new FileReader());
            //System.out.println("any file is : "  + (f.getAbsolutePath().toString()));
            while ((str = in.readLine()) != null) {  
                keyValue = str.split(":=");
               // System.out.println("adding key-values " + keyValue[0] + " - " + keyValue[1]);
                result.put(keyValue[0],keyValue[1]);
            }
            in.close();
            } catch (IOException e) {
                e.printStackTrace();
        } 
        return result;
    }


}
