/*
 * Field.java
 *
 * Created on August 3, 2002, 5:35 PM
 */

package org.unesco.jisis.z3950;

/**
 *
 * @author  Siddarth1
 */
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;

/**
 * <p>
 * For sorting we are having a inner class a comparator only for sorting(using Arrays).
 * </P>
 */
public class Field implements java.io.Serializable{
    
    String tag;
    char i1 ;
    char i2;
    SubField[] msf;
    
    /** Assumed a synchronized comparison in an applet.(screen), we are making this a singletone.*/
    public static FieldComparator FC = null;
    
    /**
     * default constructor	*/
    public Field(){
    }
    
    /**
     * A constructor with char[] as tag and 2 indicators
     */
    public Field(char[] tag, char i1, char i2){
        this( new String(tag),i1,i2);
    }
    
    /**
     * constructor  with tag,indicator1,indicator as arguments
     * @throws MarcException */
    public Field(String tag,char i1,char i2) {
        //System.out.println("Field Tag in field class"+tag);
        //System.out.println("Field ind1 in field class"+i1);
        //System.out.println("Field ind2 in field class"+i2);
        // A one time creation.
        if(FC == null)
            FC = new FieldComparator();
//        System.out.println("indicators are: "+i1+"***"+i2);
//        if( (!MarcValidator.isValidIndicator(i1)) || (!MarcValidator.isValidIndicator(i2)) )
//            throw new MarcException(" FLD : Invalid Indicator");
//        if( tag.length() != 3)
//            throw new MarcException("FLD : Invalid Tag Length "+tag.length());
        
        this.tag=tag;
        this.i1=i1;
        this.i2=i2;
        
    }
    
    public String getTag(){
        return tag;
    }
    
    public void setTag(String tag){
        this.tag = tag;
    }
    /*To Return Indicator*/
    public char getIndicator1(){
        return i1;
    }
    
    public char getIndicator2(){
        return i2;
    }
    
    public void setIndicator1(char i1){
        this.i1 = i1;
    }
    
    public void setIndicator2(char i2){
        this.i2 = i2;
    }
    
    /**
     * It sets the subfields
     * @param It takes array of subfields as argument */
    public void setSubFields(SubField[] msf){
        this.msf=msf;
    }
    
    public SubField[] getSubFields(){
        return msf;
    }
    
    /**
     * To get the subfield object for the Character id.
     * @returns Subfield object.
     */
    public SubField getSubField(char id){
        if(msf == null) return null;
        for(int i=0;i<msf.length;i++){
            
            if(msf[i].getDelimiter() == id){
                return msf[i];
            }
            
        }// end of for
        return null;
    }
    public SubField[] getSubFields(char id){
        Vector vecsubs = new Vector(1,1);
        if(msf == null) return null;
        for(int i=0;i<msf.length;i++){
            
            if(msf[i].getDelimiter() == id){
                vecsubs.addElement(msf[i]);
            }
            
        }// end of for
        SubField[] subfs = new SubField[vecsubs.size()];
        for (int i = 0; i < vecsubs.size(); i++) {
            subfs[i]=(SubField)vecsubs.elementAt(i);
        }
        return subfs;
    }    
    public void removeSubField(char id){
        
        
        int index = -1;
        for(int i =0 ; i< msf.length; i++){
            if(msf[i].getDelimiter() == id){
                index = i;
                break;
            }
        }
        //System.out.println(index);
        if(index == -1) return;
        
        SubField nsf[] = new SubField[msf.length -1];
        if(index == 0){
            System.arraycopy(msf,1,nsf,0,nsf.length);
        }else{
            System.arraycopy(msf,0,nsf,0,index);
            if(index  < nsf.length)
                System.arraycopy(msf,(index+1),nsf,index,(nsf.length - index));
        }
        testprintarray(msf);
        this.msf = null;
        this.msf = nsf;
        nsf = null;
        testprintarray(msf);
    }
    public void testprintarray(SubField arr[]){
        for(int i =0 ; i < arr.length ; i++){
           // System.out.println("");
            //System.out.print(arr[i].getIdentifier()+",");
            //System.out.println();
        }
    }
    
    public String getSubFieldData(char id){
        for(int i=0;i<msf.length;i++){
            
            if(msf[i].getDelimiter() == id){
                
                return msf[i].getData();
            }
        }// end of for
        return null;
    }
    
    public void appendSubField(SubField sf){
        
        if(sf == null) return;
        if(msf == null){
            msf = new SubField[1];
            msf[0] = sf;
            return;
        }
        
        SubField temp[] = new SubField[msf.length +1];
        System.arraycopy(msf,0,temp,0,msf.length);
        
        temp[msf.length] = sf;
        msf = null;
        msf = temp;
        temp = null;
        
        
        
    }
    
    public int length(){
        int i = 5;
        for (int k = 0; k< msf.length; k++)
            i+=msf[k].length();
        return i;
    }
    
    
    
    public void addSubField(ArrayList al){
        if(al == null) return;
        if(al.size() < 1) return;
        Object[] a = al.toArray();
        msf = new SubField[a.length];
        for(int i=0; i< a.length; i++){
            msf[i] =(SubField)a[i];
        }//end of for
    }
    
    
    /**<p> A comparator for the Field class
     * @author - biju.
     * <p>*/
    protected class FieldComparator implements Comparator{
        FieldComparator(){
        }
        
        public boolean equals(Object c){
            return false;
        }
        public int compare(Object obj1 , Object obj2){
            //blindly assumed that here only filed objects will be there.
            // this is a clumsy approch, casting unnecessarly wastes time, look appendix of Thinking In Java(B.Eckle).
            Field fld1 = (Field)obj1, fld2 = (Field)obj2;
            
            int tag1 = Integer.parseInt(fld1.getTag());
            int tag2 = Integer.parseInt(fld2.getTag());
            
            return (int)( (tag1 - tag2) / Math.abs(tag1 - tag2) );
        }
    }//FieldComparator.
    
}