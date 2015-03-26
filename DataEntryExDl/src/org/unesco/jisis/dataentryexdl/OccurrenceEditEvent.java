/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryexdl;

import org.unesco.jisis.corelib.record.IField;

/**
 * Java's support of interfaces provides a mechanism by which we can get the equivalent of callbacks. 
 * The trick is to define a simple interface that declares the method we wish to be invoked.
 * 
 * This gives us a grip on any objects of classes that implement the interface.
 * @author jcd
 */
public interface OccurrenceEditEvent {
     public void notifyCaller(IField field, int occurrence, String event);
}
