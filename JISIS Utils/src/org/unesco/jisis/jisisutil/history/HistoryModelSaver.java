/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutil.history;

import java.util.Map;

/**
 *
 * @author jcd
 */

public interface HistoryModelSaver {

    Map<String, HistoryModel> load(Map<String, HistoryModel>  models);

    boolean save(Map<String, HistoryModel> models);
}
