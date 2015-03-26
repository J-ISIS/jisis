/*
 * DbNamesModel.java
 *
 * Created on June 21, 2006, 11:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.wizards.dbcreate;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author rustam
 */
public class DbNamesModel implements ListModel {
    
    private List<String> _dbNames = new ArrayList<String>();
            
    /** Creates a new instance of DbNamesModel */
    public DbNamesModel(Enumeration dbNamesEnum) {
        while (dbNamesEnum != null && dbNamesEnum.hasMoreElements()) {
            _dbNames.add((String)dbNamesEnum.nextElement());
        }
    }

    public int getSize() {
        return _dbNames.size();
    }

    public Object getElementAt(int index) {
        return _dbNames.get(index);
    }

    public void addListDataListener(ListDataListener l) {
    }

    public void removeListDataListener(ListDataListener l) {
    }
    
}
