/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

/**
 *
 * @author jcd
 */

public class History {
 
   
 
    private List<String> list_ = new ArrayList<>();
    private int current_ = -1;
    
    
    private final Preferences loadedFrom_;
    private final int loadId_;
    private final String loadPrefix_;
 
    public History(Preferences prefNode, String prefix, int id ) {
        loadedFrom_ = prefNode;
        loadId_ = id;
        loadPrefix_ = prefix;
        load(prefNode, prefix, id);
        
    }
    
    /** Constructs a History with the specified id
    * and initializes it with all previously saved values.
    * @param prefNode the preference node that values will be loaded from
    * @param id an integer identifying this history whose values
    * to load
    */
    public History(Preferences prefNode, int id)
    {
        this(prefNode, "History", id);
    }


    private void load(Preferences prefNode, String prefix, int id ){
        String hist = prefNode.get(prefix+id,"\n\n\n\n");
        String[] loadedItems = hist.split("\n",-1);
        list_ = Arrays.asList(hist);
        
    }
     /** Saves this text fields history to the specified preference node
    * and id.
    * @param prefNode the preference node that values will be written to
    * @param id an integer identifying this history text field
    */
    public void save(Preferences prefNode, String prefix, int id){
        StringBuffer hist = new StringBuffer();
        for(String value : list_){
            hist.append(value).append("\n");
        }
       
        prefNode.put(prefix+id,hist.toString());
    }
    
     public void save() {
        save(loadedFrom_, loadPrefix_, loadId_);
     }
    
    public boolean hasBack() {
        return current_ > 0;
    }

    public boolean hasForward() {
        return current_ < list_.size() - 1;
    }

    public void add(String uri) {
        current_++;
        // clear forward history
        while (list_.size() > current_) {
            list_.remove(current_);
        }
        // add new uri
        list_.add(uri);
    }

    public boolean contains(String uri) {
        return list_.contains(uri);
    }

    public String back() {
        if (!hasBack()) {
            throw new IndexOutOfBoundsException();
        }
        current_--;
        return (String) list_.get(current_);
    }

    public String forward() {
        if (!hasForward()) {
            throw new IndexOutOfBoundsException();
        }
        current_++;
        return (String) list_.get(current_);
    }

    public String getCurrent() {
        if (current_ < 0) {
            return null;
        } else {
            return (String) list_.get(current_);
        }
    }

}
