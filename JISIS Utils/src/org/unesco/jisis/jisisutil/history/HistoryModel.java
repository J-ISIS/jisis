/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutil.history;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;


/**
 *
 * @author jcd
 */
/**
 * A history list. One history list can be used by several history text fields. Note that the list model
 * implementation is incomplete; no events are fired when the history model changes.
 *
 * @author Slava Pestov
 * @version $Id: HistoryModel.java 12504 2008-04-22 23:12:43Z ezust $
 */
public class HistoryModel extends DefaultListModel
    implements MutableListModel {

    //{{{ Private members

   private static final int HISTORY_MAX_DEFAULT = 50;
    private static int max = HISTORY_MAX_DEFAULT;

    private final String name;
    private static Map<String, HistoryModel> models;

    private static boolean modified;
    private static HistoryModelSaver saver;
	//}}}
    //{{{ HistoryModel constructor

    /**
     * Creates a new history list. Calling this is normally not necessary.
     *
     * @param name
     */
    public HistoryModel(String name) {
        this.name = name;
    } //}}}

    //{{{ addItem() method
    /**
     * Adds an item to the end of this history list, trimming the list to the maximum number of items if
     * necessary.
     *
     * @param text The item
     */
    public void addItem(String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        int index = indexOf(text);
        if (index != -1) {
            removeElementAt(index);
        }

        insertElementAt(text, 0);

        while (getSize() > max) {
            removeElementAt(getSize() - 1);
        }
    } //}}}

    //{{{ insertElementAt() method
    @Override
    public void insertElementAt(Object obj, int index) {
        modified = true;
        super.insertElementAt(obj, index);
    } //}}}

    //{{{ getItem() method
    /**
     * Returns an item from the history list.
     *
     * @param index The index
     * @return
     */
    public String getItem(int index) {
        return (String) elementAt(index);
    } //}}}

    //{{{ removeElement() method
    @Override
    public boolean removeElement(Object obj) {
        modified = true;
        return super.removeElement(obj);
    } //}}}

    //{{{ removeAllElements() method
    @Override
    public void removeAllElements() {
        modified = true;
        super.removeAllElements();
    } //}}}

    //{{{ getName() method
    /**
     * Returns the name of this history list. This can be passed to the HistoryTextField constructor.
     *
     * @return
     */
    public String getName() {
        return name;
    } //}}}

    //{{{ getModel() method
    /**
     * Returns a named model. If the specified model does not already exist, it will be created.
     *
     * @param name The model name
     * @return
     */
    public static HistoryModel getModel(String name) {
        if (models == null) {
            models = Collections.synchronizedMap(new HashMap<String, HistoryModel>());
        }

        HistoryModel model = models.get(name);
        if (model == null) {
            model = new HistoryModel(name);
            models.put(name, model);
        }

        return model;
    } //}}}

    //{{{ loadHistory() method
    public static void loadHistory() {
        if (saver != null) {
            models = saver.load(models);
        }
    } //}}}

    //{{{ saveHistory() method
    public static void saveHistory() {
        if (saver != null && modified && saver.save(models)) {
            modified = false;
        }
    } //}}}

    //{{{ setMax() method
    public static void setMax(int max) {
        HistoryModel.max = max;
    } //}}}

    //{{{ setSaver() method
    public static void setSaver(HistoryModelSaver saver) {
        HistoryModel.saver = saver;
    } //}}}

}
