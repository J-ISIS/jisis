/*
 * FieldComparator.java
 *
 * Created on 14 settembre 2007, 14.33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.jisiscore.client;

import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author triverio
 */
public class FieldComparator implements Comparator{
    
    private int tag;
  
    private static final Logger LOGGER = LoggerFactory.getLogger( FieldComparator.class);
    /** Creates a new instance of FieldComparator */
    public FieldComparator(int tag) {
        this.tag = tag;
    }
    
    
    public int compare(Object rec1, Object rec2) {
        int result = 0;
//        String field1 = "";
//        String field2 = "";
//        int field_int_1 = 0;
//        int field_int_2 = 0;
//        System.out.println(this.tag);
//        try {
//            int type = ((RecordClient)rec1).getField(this.tag).getType();
//            if (type ==3) {
//                field_int_1 = new Integer(((RecordClient) rec1).getField(this.tag).getFieldValue()).intValue();
//                field_int_2 = new Integer(((RecordClient) rec2).getField(this.tag).getFieldValue()).intValue();
//                if( field_int_1>field_int_2){
//                    result =  1;
//                } else if( field_int_1<field_int_2)
//                    result =  -1;
//                else
//                    result =  0;
//            } else {
//                logger.log(Level.INFO,"Record: " + ((RecordClient)rec1).getMfn());
//                field1 = ((RecordClient) rec1).getField(this.tag).getFieldValue();
//                field2 = ((RecordClient) rec2).getField(this.tag).getFieldValue();
//                if (field1!=null && field2!=null) {
//                    if( field1.compareTo(field2)>0){
//                        result =  1;
//                    } else if( field1.compareTo(field2)>0 )
//                        result =  -1;
//                    else
//                        result =  0;
//                }
//            }
//        } catch (DbException ex) {
//            ex.printStackTrace();
//        }
        
        throw new UnsupportedOperationException("FieldComparator Not supported yet.");
//        return result;
    }
    
}
