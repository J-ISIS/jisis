/*
 * ControlField.java
 *
 * Created on August 3, 2002, 7:03 PM
 */

package org.unesco.jisis.z3950;

/**
 *
 * @author  Siddarth1
 */
import java.io.Serializable;

public class ControlField implements Serializable{
    
    
    String tag;
    String data;
    
    public ControlField(){
        this(null,null);
    }
    public ControlField(String tag, String data){
        this.tag = tag;
        this.data = data;
    }
    
    public void setTag(String tag){
        this.tag = tag;
    }
    
    public String getTag(){
        return tag;
    }
    
    public void setData(String data){
        this.data = data;
    }
    
    public String getData(){
        return data;
    }
}