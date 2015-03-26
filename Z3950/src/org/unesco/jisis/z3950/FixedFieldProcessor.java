/*
 * FixedFieldProcessor.java
 *
 * Created on January 13, 2006, 5:17 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.unesco.jisis.z3950;
import java.util.*; 
/**
 *
 * @author bhargavi
 */
public class FixedFieldProcessor {
    ArrayList fields;
    ArrayList subfields;
    ArrayList controlflds;
    Field f;
    SubField sf;
    ControlField cf;
    public FixedField fxld;
    public CatalogMaterialDescription cmd;
    boolean fieldstarted = false;
    public int presentMaterialType = 0;
    private String leader;
    /** Creates a new instance of FixedFieldProcessor */
    public FixedFieldProcessor() {
        fxld=new FixedField();
        controlflds=new java.util.ArrayList();
    }
//    public FixedField process(){
//
//    }
    public void startControlField(String tag, String data){
        
//        	System.out.println("Control Field :"+tag +" data: "+data);
        cf = new ControlField(tag, data);
        controlflds.add(cf);
        if(data == null) return;
        if(tag.equals("008") || tag.equals("006")){
            boolean six = ("006".equals(tag));
            
            OO8Processor pro = null;
            if(six)
                pro = OO8Processor.get006Processor(presentMaterialType);
            else
                pro = OO8Processor.get008Processor(presentMaterialType);
            //	System.out.println(pro);
            try{	pro.process(fxld,data); }catch(MarcException me){  }
/*            if(six){
                cf = new ControlField(tag,data);
            }*/
            
        }else{
            
        }
    }
    public void startLeader(Leader l){
       fxld.setLeader(l.toString());
        presentMaterialType = 0;
        
        char blvl = l.getBlvl();
       // System.out.println("Blvl at ffp: "+blvl);
        char typ = l.getTypeOfRecord();
        //	System.out.println("Blvl = "+blvl);
        switch(blvl){
            case 'a':
                presentMaterialType |= CatalogueConstants.MONOGRAPH_ANALYTIC;
                break;
            case 'b':
                presentMaterialType |= CatalogueConstants.SERIAL_ANALYTIC;
                break;
            case 's':
                presentMaterialType |= CatalogueConstants.SERIAL;
                break;
            case 'm':
                presentMaterialType |= CatalogueConstants.MONOGRAPH;
                break;
            case 'c':
                presentMaterialType |= CatalogueConstants.COLLECTION;
                break;
            case 'd':
                presentMaterialType |= CatalogueConstants.SUBUNIT;
                break;
        }
        //--- check if music ---
        switch(typ){
            case 'c':
            case 'd':
            case 'i':
            case 'j':
                presentMaterialType |= CatalogueConstants.MUSIC;
                break;
            case 'g':
            case 'k':
            case 'r':
                presentMaterialType |= CatalogueConstants.VISUAL_MATERIAL;
                break;
            case 'e':
            case 'f':
                presentMaterialType |= CatalogueConstants.MAPS;
                break;
            case 'm':
                presentMaterialType |= CatalogueConstants.COMPUTERFILES;
                break;
            case 'p':
                presentMaterialType |= CatalogueConstants.MIXED_MATERIAL;
                break;
            case 'a':
            case 't':
                presentMaterialType |= CatalogueConstants.BOOKS;
                break;
                //case
        }
        
        fxld.setType(l.getTypeOfRecord());
        fxld.setBlvl(l.getBlvl());
        fxld.setDsc(l.getDesc());
        fxld.setElvl(l.getEncLvl());
        fxld.setCtrl(l.getTypeOfCtrl());
        
        
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }
}
