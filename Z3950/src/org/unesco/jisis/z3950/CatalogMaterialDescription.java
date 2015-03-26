/*
 * CatalogMaterialDescription.java
 *
 * Created on August 3, 2002, 7:13 PM
 */

package org.unesco.jisis.z3950;

/**
 *
 * @author  Siddarth1
 */

public class CatalogMaterialDescription implements java.io.Serializable{
    
    /** Constant for representing Marc Gui*/
    public static final char MARC = 'M';
    
    /** constant for representing Non Marc GUI */
    public static final char NONMARC = 'N';
    
    /** For storing the GUI of entry */
    public char gui;
    
    /** Field object collection */
    public Field[] field;
    
    /** Fixed field object */
    public FixedField fld;
    
    /** ControlField object */
    public ControlField[] cf;
    
    public CatalogMaterialDescription(){
        this(MARC,null,null);
    }
    
    public CatalogMaterialDescription(char gui, Field[] field, FixedField fld){
        this.gui = gui;
        this.field = field;
        this.fld = fld;
        sortFields();
    }
    
    /**<p> For setting the Gui from which the catalog material description was created.
     * <br><code>default&nbsp; - &nbsp; MARC</code>
     * </p> */
    public void setGUI(char gui){
        if( gui != CatalogMaterialDescription.NONMARC)
            this.gui=CatalogMaterialDescription.MARC;
    }
    
    /**
     * A getter Method for geting the Gui.
     * @returns - The Gui used for editing .
     */
    public char getGUI(){
        return gui;
    }
    
    public void setFields(Field[] field){
        if( (field != null) && (field.length > 0 ) ){
            this.field=field;
        }else{
            this.field = null;
        }
    }
    
    public Field[] getFields(){
        return field;
    }
    
    public void setFixedField(FixedField fld){
        this.fld = fld;
    }
    
    public FixedField getFixedField(){
        return fld;
    }
    
    public void setControlFields(ControlField[] cf){
        this.cf = cf;
    }
    
    public ControlField[] getControlField(){
        return cf;
    }
    
    /**
     * <p>This method will generate Field[] from the given array list which contains <code>Field</code>.<br>
     * @params - A java.util.ArrayList that contains Field objects inside.
     * @exception - MarcException.
     * </p>
     */
    public void addField(java.util.ArrayList a) {
        if(a == null)
            return;
        
        try{
            Object[] f = a.toArray();
            field = new Field[f.length];
            for(int i=0; i< f.length; i++){
                field[i] = (Field)f[i];
            }//end of for
        }catch(Exception exp){
            
        }
    }
    
    public void addField(Field f){
        appendField(f);
    }
    
    public void appendField(Field f){
        
        if(f == null) return;
        
        int fldlen = 0;
        int len = 1;
        if(field != null){
            fldlen = field.length;
            len = field.length +1;
        }
        Field temp[] = new Field[len];
        
        try{
            if(field != null)
                System.arraycopy(field,0,temp,0,field.length);
        }catch(Exception e){
            e.printStackTrace();
            // System.out.println("exception in append field method"+e);
        }
        
        temp[fldlen] = f;
        field = null;
        field = temp;
        temp = null;
        
    }
    /**
     * <p>This method will generate Field[] from the given array list which contains <code>Field</code>.<br>
     * @params - A java.util.ArrayList that contains Field objects inside.
     * @exception - MarcException.
     * </p>
     */
    public void addControlField(java.util.ArrayList a) {
        if(a == null)
            return;
        
        try{
            Object[] f = a.toArray();
            this.cf = new ControlField[f.length];
            for(int i=0; i< f.length; i++){
                cf[i] = (ControlField)f[i];
            }//end of for
        }catch(Exception exp){
            
        }
    }
    
    
    /**<p>
     * This Method returns a boolean value representing the gui for editing.
     * </p>
     * @returns - true if GUI was MARC.
     */
    public boolean isGUIMarc(){
        return (CatalogMaterialDescription.MARC == gui);
    }
    
    
    public Field getField(String fieldTag){
        
        if(fieldTag == null) return null;
        if(field == null) return null;
        
         /*
          *	binary search requires underlying collection to be sorted.
          * int i = java.util.Arrays.binarySearch(field,new Field(fieldTag,'1','1'),Field.FC);
          */
        boolean found = false;
        int i = 0;
        
        //---- log n----
        for(i =0 ; i < field.length ; i++){
            if(fieldTag.equals(field[i].getTag())){
                found = true;
                break;
            }
        }
        
        if(!found)
            i = -1;
        //----
        
        if(i < 0) return null;
        return field[i];
        
        
    }
    public Field[] getFields(String fieldTag){
        
        if(fieldTag == null) return null;
        if(field == null) return null;
        
         /*
          *	binary search requires underlying collection to be sorted.
          * int i = java.util.Arrays.binarySearch(field,new Field(fieldTag,'1','1'),Field.FC);
          */
        boolean found = false;
        int i = 0;
        
        //---- log n----
        java.util.Vector obj=new java.util.Vector(1,1);
        for(i =0 ; i < field.length ; i++){
            if(fieldTag.equals(field[i].getTag())){
                found = true;
                obj.addElement(field[i]);
                
            }
        }
        Field[] fields=new Field[obj.size()];
        if(found){
            
            for(int k=0;k<obj.size();k++){
                fields[k]=(Field)obj.get(k);
            }
        }
        
        //----
        
        return fields;
        
        
    }
    public String getTitleSlashResponsibility() {
        String s1 = "";
        String s2 = "";
        Field f = getField("245");
        if(f != null){
            s1 = f.getSubFieldData('a');
            if(s1.charAt(s1.length()-1) == '/'){
                s1 = s1.substring(0,s1.length()-1);
                if(s1 == null){
                    s1 = "_";
                }
            }
            
            s2 = f.getSubFieldData('c');
            if(s2 == null){
                s2 = "_";
            }
        }
        
        return (s1+"/"+s2);
    }
    
    
    public int getRecNo(){
        
        return 100;
    }
    public void setLeader(String leader){
        this.leader=leader;
    }
    public String getLeader(){
        
        return this.leader;
    }
    
    /**Use the quick sort by <B>Sun</B*/
    protected void sortFields(){
        
        if(field == null) return;
        //java.util.Arrays.sort(field,Field.FC);
    }
    
    String leader;
    
}