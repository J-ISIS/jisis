/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisiscore.common;

/**
 *
 * @author jcd
 * @param <T>
 */
public interface AsyncCallback<T> {

    void onFailure(Throwable caught);

    void onSuccess(T result);
    
    void onCancel();
}
