/*
 * Converter.java
 *
 * Created on January 6, 2006, 4:41 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.unesco.jisis.z3950;
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.marc4j.*;
import org.marc4j.marc.*;
/**
 *
 * @author bhargavi
 */
public class Converter {
    
    //NewGenLibImplementation newgenImpl=null;
    private String leadingTrailingCharacters=":/,;()[]^-#$%&~";
    private static final Converter SINGLETON = new Converter();
    public static Converter getInstance(){
        return SINGLETON;
    }
    
    /** Creates a new instance of Converter */
    private Converter() {
        // System.out.println("Contructor Converter");
        //newgenImpl= new NewGenLibImplementation();
    }
    
    public String marcToMarcXML(String marc) {
        String xml ="";
        try{

           // System.out.println("iso: "+marc);
            InputStream input = new ByteArrayInputStream(marc.getBytes("UTF-8"));
            MarcReader reader = new MarcStreamReader(input);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
            MarcWriter writer = new MarcXmlWriter(bos,true);
            while (reader.hasNext()) {
                // System.out.println("6");
                Record record = reader.next();
                writer.write(record);
            }
            writer.close();
            xml = bos.toString("UTF-8");
           
        }catch (Exception exp){exp.printStackTrace();}
        
       // xml = marcModelsToMarcXML(newgenImpl.getMarcModelsFromMarc(marc));
        return xml;
    }
    public String marcXMLToMarc(String marcxml) {
        String marc ="";
        // System.out.println("xml being converted: "+marcxml);
//        marcxml=verifyXML(marcxml);
        try{
            InputStreamReader input = new java.io.InputStreamReader(new ByteArrayInputStream(marcxml.getBytes("UTF-8")),"UTF-8");
            org.xml.sax.InputSource is = new org.xml.sax.InputSource(input);
//            java.io.InputStreamReader isr = new java.io.InputStreamReader()
            MarcXmlReader reader = new MarcXmlReader(is);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
            MarcWriter writer = new MarcStreamWriter(bos,"UTF-8");
            while (reader.hasNext()) {
                Record record = reader.next();
                
                writer.write(record);
                
            }
            writer.close();
            marc = bos.toString("UTF-8");
            char chrep = (char)0;
            marc=marc.replace(chrep,' ');
            //System.out.println("marc: "+marc);
        }catch (Exception exp){exp.printStackTrace();}
        return marc;
    }
    public CatalogMaterialDescription getMarcModelFromMarcRemoveSpecialCharacters(String marc){
        return this.getMarcModelFromMarc2(marc);
    }
     public CatalogMaterialDescription[] getMarcModelsFromMarcRemoveSpecialCharacters(String marc){
        return this.getMarcModelsFromMarc2(marc);
    }
    public CatalogMaterialDescription[] getMarcModelsFromMarc2(String marc){
        CatalogMaterialDescription[] cmd =null;
        try{
//            if(!marc.endsWith("")){
//                marc+="";
//            }
            InputStream input = new ByteArrayInputStream(marc.getBytes("UTF-8"));
            MarcReader reader = new MarcStreamReader(input);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
            MarcWriter writer = new MarcXmlWriter(bos,"UTF-8", true);
              writer.setConverter(new org.marc4j.converter.impl.UnicodeToAnsel());
            java.util.Vector veccmd = new java.util.Vector(1,1);
            while (reader.hasNext()) {
                //System.out.println("6");
                Record record = null;
                try{
                    record = reader.next();
                }catch (Exception exp){
                    exp.printStackTrace();
                }
                if(record!=null)
                    veccmd.addElement(record);
            }
            cmd = new CatalogMaterialDescription[veccmd.size()];
            for(int k=0;k<veccmd.size();k++){
                Record onerec =(Record)veccmd.elementAt(k);
                //System.out.println(onerec.toString());
                java.util.List li = onerec.getDataFields();
                java.util.ArrayList aldatafield = new java.util.ArrayList();
                for(int i=0;i<li.size();i++){
                    DataField daf = (DataField)li.get(i);
                    java.util.List lisf = daf.getSubfields();
                    java.util.ArrayList alsubfield = new java.util.ArrayList();
                    org.unesco.jisis.z3950.Field marcf = new org.unesco.jisis.z3950.Field(daf.getTag(), daf.getIndicator1(), daf.getIndicator2());
                    for(int j=0;j<lisf.size();j++){
                        //System.out.println(lisf.get(j).getClass().getName());
                        Subfield sf = (Subfield)lisf.get(j);
                        org.unesco.jisis.z3950.SubField marcsf = new org.unesco.jisis.z3950.SubField(sf.getCode(),removeLeadingTrailingSpecialCharacters(sf.getData()));
                        alsubfield.add(marcsf);
                    }
                    marcf.addSubField(alsubfield);
                    aldatafield.add(marcf);
                }
                String leaderstr = null;
                try{
                    leaderstr = onerec.getLeader().marshal();
                }catch (Exception expl){expl.printStackTrace();}
                org.unesco.jisis.z3950.Leader leadermm = new org.unesco.jisis.z3950.Leader(leaderstr);
                 org.unesco.jisis.z3950.FixedFieldProcessor ffp = new  org.unesco.jisis.z3950.FixedFieldProcessor();
                ffp.startLeader(leadermm);
                //System.out.println("ffp1: "+ffp.fxld);
                java.util.List licon = onerec.getControlFields();
                java.util.ArrayList alcontrolfield = new java.util.ArrayList();
                for(int i=0;i<licon.size();i++){
                    ControlField conf = (ControlField)licon.get(i);
                     org.unesco.jisis.z3950.ControlField cf = new  org.unesco.jisis.z3950.ControlField();
                    cf.setTag(conf.getTag());
                    cf.setData(conf.getData());
                    alcontrolfield.add(cf);
                    ffp.startControlField(cf.getTag(), cf.getData());
                }
                cmd[k]=new CatalogMaterialDescription();
                cmd[k].addControlField(alcontrolfield);
                cmd[k].setFixedField(ffp.fxld);
                cmd[k].addField(aldatafield);
            }
        }catch (Exception exp){exp.printStackTrace();}
        return cmd;
    }
    public String removeLeadingTrailingSpecialCharacters(String data){
        // System.out.println("Data ius: "+data);
        String returnStr="";
        char[] vals = data.toCharArray();
        for (int i = 0; i < vals.length; i++) {
            if(leadingTrailingCharacters.indexOf(vals[i])==-1){
                //  System.out.println("Started from first and it is fine");
                break;
            }else{
                //System.out.println("Started from first and data removed accordingly");
                vals[i]=' ';
            }
        }
        for (int i = (vals.length)-1; i >= 0; i--) {
            // System.out.println("entered into reverese checking");
            if(leadingTrailingCharacters.indexOf(vals[i])==-1){
                // System.out.println("Started from back and it is fine");
                break;
            }else{
                //System.out.println("Started from back and data removed accordingly");
                vals[i]=' ';
            }
        }
        returnStr=String.valueOf(vals).trim();
        //System.out.println("Return Data ius: "+returnStr);
        return returnStr;
    }
    public CatalogMaterialDescription getMarcModelFromMarc2(String marc){
        CatalogMaterialDescription cmd =null;
        //System.out.println(marc);
        try{
//            if(!marc.endsWith("")){
//                marc+="";
//            }
            
            InputStream input = new ByteArrayInputStream(marc.getBytes("UTF-8"));
            MarcReader reader = new MarcStreamReader(input);
            //  ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
            //MarcWriter writer = new MarcXmlWriter(bos,true);
            Record onerec =null;
            
            while (reader.hasNext()) {
                
                Record record = null;
                try{
                    
                    onerec = reader.next();
                    
                }catch (Exception exp){exp.printStackTrace();}
            }
            
            java.util.ArrayList aldatafield = new java.util.ArrayList();
            //System.out.println("onrec..."+(onerec==null));
            if(onerec!=null){
                //System.out.println("record...onerec if.");
                java.util.List li = onerec.getDataFields();
                for(int i=0;i<li.size();i++){
                    DataField daf = (DataField)li.get(i);
                    java.util.List lisf = daf.getSubfields();
                    java.util.ArrayList alsubfield = new java.util.ArrayList();
                    org.unesco.jisis.z3950.Field marcf = new org.unesco.jisis.z3950.Field(daf.getTag(), daf.getIndicator1(), daf.getIndicator2());
                    for(int j=0;j<lisf.size();j++){
                        //System.out.println(lisf.get(j).getClass().getName());
                        Subfield sf = (Subfield)lisf.get(j);
                        org.unesco.jisis.z3950.SubField marcsf = new org.unesco.jisis.z3950.SubField(sf.getCode(),removeLeadingTrailingSpecialCharacters(sf.getData()));
                        alsubfield.add(marcsf);
                    }
                    marcf.addSubField(alsubfield);
                    aldatafield.add(marcf);
                }
                String leaderstr = null;
                try{
                    leaderstr = onerec.getLeader().marshal();
                }catch (Exception expl){expl.printStackTrace();}
                org.unesco.jisis.z3950.Leader leadermm = new org.unesco.jisis.z3950.Leader(leaderstr);
                org.unesco.jisis.z3950.FixedFieldProcessor ffp = new org.unesco.jisis.z3950.FixedFieldProcessor();
                ffp.startLeader(leadermm);
                
                java.util.List licon = onerec.getControlFields();
                java.util.ArrayList alcontrolfield = new java.util.ArrayList();
                for(int i=0;i<licon.size();i++){
                    ControlField conf = (ControlField)licon.get(i);
                    org.unesco.jisis.z3950.ControlField cf = new org.unesco.jisis.z3950.ControlField();
                    cf.setTag(conf.getTag());
                    cf.setData(conf.getData());
                    alcontrolfield.add(cf);
                    ffp.startControlField(cf.getTag(), cf.getData());
                }
                cmd=new CatalogMaterialDescription();
                cmd.addControlField(alcontrolfield);
                cmd.setFixedField(ffp.fxld);
                cmd.addField(aldatafield);
            }
        }catch (Exception exp){exp.printStackTrace();}
        return cmd;
    }
    public CatalogMaterialDescription getMarcModelFromMarc(String marc){
        CatalogMaterialDescription cmd =null;
        System.out.println("GetMarcModelFromMarc");
        try{
//            if(!marc.endsWith("")){
//                marc+="";
//            }
            
            InputStream input = new ByteArrayInputStream(marc.getBytes("UTF-8"));
            org.marc4j.MarcReader reader = new org.marc4j.MarcStreamReader(input);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
            org.marc4j.MarcWriter writer = new org.marc4j.MarcStreamWriter(bos, "UTF8");

            org.marc4j.converter.impl.AnselToUnicode converter = new org.marc4j.converter.impl.AnselToUnicode();
            writer.setConverter(converter);

            org.marc4j.marc.Record onerec =null;
            
            while (reader.hasNext()) {
                    
                    org.marc4j.marc.Record record = reader.next();
                    org.marc4j.marc.Leader leader = record.getLeader();
                    leader.setCharCodingScheme('a');
                    writer.write(record);
                    
            }
            writer.close();
            byte[] rawRecord = bos.toByteArray();
            input = new ByteArrayInputStream(rawRecord);
            reader = new org.marc4j.MarcStreamReader(input);
            while (reader.hasNext()) {
                    onerec = reader.next();
            }
            System.out.println("Converted record:\n"+onerec.toString());
            
            java.util.ArrayList aldatafield = new java.util.ArrayList();
            //System.out.println("onrec..."+(onerec==null));
            if(onerec!=null){
                //System.out.println("record...onerec if.");
                java.util.List li = onerec.getDataFields();
                for(int i=0;i<li.size();i++){
                    DataField daf = (DataField)li.get(i);
                    java.util.List lisf = daf.getSubfields();
                    java.util.ArrayList alsubfield = new java.util.ArrayList();
                    org.unesco.jisis.z3950.Field marcf = new org.unesco.jisis.z3950.Field(daf.getTag(), daf.getIndicator1(), daf.getIndicator2());
                    for(int j=0;j<lisf.size();j++){
                        //System.out.println(lisf.get(j).getClass().getName());
                        Subfield sf = (Subfield)lisf.get(j);
                        org.unesco.jisis.z3950.SubField marcsf = new org.unesco.jisis.z3950.SubField(sf.getCode(),sf.getData());
                        alsubfield.add(marcsf);
                    }
                    marcf.addSubField(alsubfield);
                    aldatafield.add(marcf);
                }
                String leaderstr = null;
                try{
                    leaderstr = onerec.getLeader().marshal();
                }catch (Exception expl){expl.printStackTrace();}
                org.unesco.jisis.z3950.Leader leadermm = new org.unesco.jisis.z3950.Leader(leaderstr);
                org.unesco.jisis.z3950.FixedFieldProcessor ffp = new org.unesco.jisis.z3950.FixedFieldProcessor();
                ffp.startLeader(leadermm);
                
                java.util.List licon = onerec.getControlFields();
                java.util.ArrayList alcontrolfield = new java.util.ArrayList();
                for(int i=0;i<licon.size();i++){
                    org.marc4j.marc.ControlField conf = (org.marc4j.marc.ControlField)licon.get(i);
                    org.unesco.jisis.z3950.ControlField cf = new org.unesco.jisis.z3950.ControlField();
                    cf.setTag(conf.getTag());
                    cf.setData(conf.getData());
                    alcontrolfield.add(cf);
                    ffp.startControlField(cf.getTag(), cf.getData());
                }
                cmd=new CatalogMaterialDescription();
                cmd.addControlField(alcontrolfield);
                cmd.setFixedField(ffp.fxld);
                cmd.addField(aldatafield);
            }
        }catch (Exception exp){
           System.out.println("EXECPTION IN CONVERTER"+exp.getMessage());
           exp.printStackTrace();
        }
        return cmd;
        
        
    }
    public CatalogMaterialDescription[] getMarcModelsFromMarc(String marc){
        CatalogMaterialDescription[] cmd =null;
        try{
//            if(!marc.endsWith("")){
//                marc+="";
//            }
            InputStream input = new ByteArrayInputStream(marc.getBytes("UTF-8"));
            MarcReader reader = new MarcStreamReader(input);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
            MarcWriter writer = new MarcXmlWriter(bos,"UTF-8", true);
              writer.setConverter(new org.marc4j.converter.impl.UnicodeToAnsel());
            java.util.Vector veccmd = new java.util.Vector(1,1);
            while (reader.hasNext()) {
                //System.out.println("6");
                Record record = null;
                try{
                    record = reader.next();
                }catch (Exception exp){
                    exp.printStackTrace();
                }
                if(record!=null)
                    veccmd.addElement(record);
            }
            cmd = new CatalogMaterialDescription[veccmd.size()];
            for(int k=0;k<veccmd.size();k++){
                Record onerec =(Record)veccmd.elementAt(k);
                //System.out.println(onerec.toString());
                java.util.List li = onerec.getDataFields();
                java.util.ArrayList aldatafield = new java.util.ArrayList();
                for(int i=0;i<li.size();i++){
                    DataField daf = (DataField)li.get(i);
                    java.util.List lisf = daf.getSubfields();
                    java.util.ArrayList alsubfield = new java.util.ArrayList();
                    org.unesco.jisis.z3950.Field marcf = new org.unesco.jisis.z3950.Field(daf.getTag(), daf.getIndicator1(), daf.getIndicator2());
                    for(int j=0;j<lisf.size();j++){
                        //System.out.println(lisf.get(j).getClass().getName());
                        Subfield sf = (Subfield)lisf.get(j);
                        org.unesco.jisis.z3950.SubField marcsf = new org.unesco.jisis.z3950.SubField(sf.getCode(),sf.getData());
                        alsubfield.add(marcsf);
                    }
                    marcf.addSubField(alsubfield);
                    aldatafield.add(marcf);
                }
                String leaderstr = null;
                try{
                    leaderstr = onerec.getLeader().marshal();
                }catch (Exception expl){expl.printStackTrace();}
                org.unesco.jisis.z3950.Leader leadermm = new org.unesco.jisis.z3950.Leader(leaderstr);
                org.unesco.jisis.z3950.FixedFieldProcessor ffp = new org.unesco.jisis.z3950.FixedFieldProcessor();
                ffp.startLeader(leadermm);
                //System.out.println("ffp1: "+ffp.fxld);
                java.util.List licon = onerec.getControlFields();
                java.util.ArrayList alcontrolfield = new java.util.ArrayList();
                for(int i=0;i<licon.size();i++){
                    ControlField conf = (ControlField)licon.get(i);
                    org.unesco.jisis.z3950.ControlField cf = new org.unesco.jisis.z3950.ControlField();
                    cf.setTag(conf.getTag());
                    cf.setData(conf.getData());
                    alcontrolfield.add(cf);
                    ffp.startControlField(cf.getTag(), cf.getData());
                }
                cmd[k]=new org.unesco.jisis.z3950.CatalogMaterialDescription();
                cmd[k].addControlField(alcontrolfield);
                cmd[k].setFixedField(ffp.fxld);
                cmd[k].addField(aldatafield);
            }
        }catch (Exception exp){exp.printStackTrace();}
        return cmd;
        
    }
    public org.unesco.jisis.z3950.CatalogMaterialDescription getMarcModelFromMarcXML(String marcxml){
        org.unesco.jisis.z3950.CatalogMaterialDescription cmd =null;
        marcxml=verifyXML(marcxml);
        try{
            String marc = marcXMLToMarc(marcxml);
            cmd=this.getMarcModelFromMarc(marc);
        }catch (Exception exp){exp.printStackTrace();}
        return cmd;
    }
    public org.unesco.jisis.z3950.CatalogMaterialDescription[] getMarcModelsFromMarcXML(String marcxml){
        org.unesco.jisis.z3950.CatalogMaterialDescription[] cmd =null;
        try{
            String marc = marcXMLToMarc(marcxml);
            cmd=this.getMarcModelsFromMarc(marc);
        }catch (Exception exp){exp.printStackTrace();}
        return cmd;
    }
    
    public String marcModelToMARC(org.unesco.jisis.z3950.CatalogMaterialDescription cmd) {
        String marc="";
        org.marc4j.marc.MarcFactory mf = org.marc4j.marc.MarcFactory.newInstance();
        org.marc4j.marc.Record record = mf.newRecord();
        org.marc4j.marc.ControlField ctrlfld=mf.newControlField();
        ctrlfld.setTag("001");
        ctrlfld.setData("###86104385#");
        
        //System.out.println("l************************");
        org.marc4j.marc.Leader leaderm = mf.newLeader(cmd.getFixedField().getLeader());
        record.setLeader(leaderm);
        org.unesco.jisis.z3950.ControlField cfs[]=cmd.getControlField();
        if(cfs!=null){
            for(int i=0;i<cfs.length;i++){
                //System.out.println("1cf");
                org.unesco.jisis.z3950.ControlField  cf = cfs[i];
                org.marc4j.marc.ControlField cfm = mf.newControlField(cf.getTag(), cf.getData());
                record.addVariableField(cfm);
            }
        }
        record.addVariableField(ctrlfld) ;
        org.unesco.jisis.z3950.Field fields[] = cmd.getFields();
        for(int i=0;i<fields.length;i++){
            //System.out.println("2field");
            org.unesco.jisis.z3950.Field  f = fields[i];
            org.unesco.jisis.z3950.SubField sfields[] = f.getSubFields();
            if(sfields!=null){
                org.marc4j.marc.DataField df=mf.newDataField(f.getTag(), f.getIndicator1(), f.getIndicator2());
                for(int j=0;j<sfields.length;j++){
                    df.addSubfield(mf.newSubfield(sfields[j].getDelimiter(), sfields[j].getData().trim()));
                }
                record.addVariableField(df);
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
        MarcWriter writer = new MarcStreamWriter(bos,"UTF-8");
        //System.out.println("record is..."+(record==null)+"........"+(writer==null));
        try{
            writer.write(record);
        }catch(Exception e){e.printStackTrace();}
        writer.close();
        try{
            marc=bos.toString("UTF-8");
        }catch (Exception exp){}
        char chrep = (char)0;
        marc=marc.replace(chrep,' ');
        //System.out.println("marc in converter..."+marc);
        return marc;
    }
    
    public String marcModelsToMarcXML(org.unesco.jisis.z3950.CatalogMaterialDescription[] cmds){
        String marcXml="";
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
        MarcXmlWriter writer = new MarcXmlWriter(bos,"UTF-8",true);
        for (int k = 0; k < cmds.length; k++) {
            org.unesco.jisis.z3950.CatalogMaterialDescription cmd=cmds[k];
            
            org.marc4j.marc.MarcFactory mf = org.marc4j.marc.MarcFactory.newInstance();
            org.marc4j.marc.Record record = mf.newRecord();
            org.marc4j.marc.Leader leaderm = mf.newLeader(cmd.getFixedField().getLeader());
            leaderm.unmarshal(cmd.getFixedField().getLeader());
            record.setLeader(leaderm);
            org.unesco.jisis.z3950.ControlField cfs[]=cmd.getControlField();
            if(cfs!=null){
                for(int i=0;i<cfs.length;i++){
                    //System.out.println("1cf");
                    org.unesco.jisis.z3950.ControlField  cf = cfs[i];
                    org.marc4j.marc.ControlField cfm = mf.newControlField(cf.getTag(), cf.getData());
                    record.addVariableField(cfm);
                }
            }
            org.unesco.jisis.z3950.Field fields[] = cmd.getFields();
            for(int i=0;i<fields.length;i++){
                //System.out.println("2field");
                org.unesco.jisis.z3950.Field  f = fields[i];
                org.unesco.jisis.z3950.SubField sfields[] = f.getSubFields();
                
                if(sfields!=null){
                    org.marc4j.marc.DataField df=mf.newDataField(f.getTag(), f.getIndicator1(), f.getIndicator2());
                    
                    for(int j=0;j<sfields.length;j++){
                        df.addSubfield(mf.newSubfield(sfields[j].getDelimiter(), sfields[j].getData()));
                    }
                    record.addVariableField(df);
                }
            }
            
            //System.out.println("record is..."+(record==null)+"........"+(writer==null));
            writer.write(record);
            
            
            
        }
        writer.close();
        try{
            marcXml=bos.toString("UTF-8");
        }catch (Exception exp){exp.printStackTrace();}
        return marcXml;
    }
    
    public String marcModelToMarcXML(CatalogMaterialDescription cmd){
        String marcXml="";
        // Element elerecord = newgenImpl.marcModelToMarcXML(cmd);
        
       /* Namespace ns = Namespace.getNamespace("http://www.loc.gov/MARC21/slim");
        Element elerecord = new Element("record",ns);
        String leader=cmd.getLeader();
        
        Element elex = new Element("leader",ns);
        elex.setText(leader);
        elerecord.addContent(elex);
        
          newgenlib.marccomponent.marcmodel.ControlField[] cf=cmd.getControlField();
        for (int i = 0; i < cf.length; i++) {
            newgenlib.marccomponent.marcmodel.ControlField onecf = cf[i];
            elex = new Element("controlfield",ns);
            elex.setAttribute("tag",onecf.getTag());
            elex.setText(onecf.getData());
            elerecord.addContent(elex);
        }
        
        Field field[]=cmd.getFields();
        if(field != null && field.length >0){
            for (int i = 0; i < field.length; i++) {
                Field onefield = field[i];
                elex = new Element("datafield",ns);
                elex.setAttribute("tag",onefield.getTag());
                elex.setAttribute("ind1",String.valueOf(onefield.getIndicator1()));
                elex.setAttribute("ind2",String.valueOf(onefield.getIndicator2()));
        
                SubField[] sf = onefield.getSubFields();
                if(sf!=null){
                    for (int j = 0; j < sf.length; j++) {
                        SubField sfone = sf[j];
                        Element eley = new Element("subfield",ns);
                        eley.setAttribute("code",String.valueOf(sfone.getIdentifier()));
                        eley.setText(sfone.getData());
                        elex.addContent(eley);
                    }
                    elerecord.addContent(elex);
                }
            }
        }
        
        Element eleroot = new Element("collection",ns);
        eleroot.addContent(elerecord);
        Document doc = new Document(eleroot);
        marcXml=(new XMLOutputter()).outputString(doc);*/
        
        org.marc4j.marc.MarcFactory mf = org.marc4j.marc.MarcFactory.newInstance();
        org.marc4j.marc.Record record = mf.newRecord();
        //System.out.println("leader "+cmd.getFixedField().getLeader());
        // System.out.println("Leader is: "+cmd.getFixedField().getLeader());
        org.marc4j.marc.Leader leaderm = mf.newLeader(cmd.getFixedField().getLeader());
        leaderm.unmarshal(cmd.getFixedField().getLeader());
        record.setLeader(leaderm);
        org.unesco.jisis.z3950.ControlField cfs[]=cmd.getControlField();
        if(cfs!=null){
            for(int i=0;i<cfs.length;i++){
                //System.out.println("1cf");
                org.unesco.jisis.z3950.ControlField  cf = cfs[i];
                org.marc4j.marc.ControlField cfm = mf.newControlField(cf.getTag(), cf.getData());
                record.addVariableField(cfm);
            }
        }
        org.unesco.jisis.z3950.Field fields[] = cmd.getFields();
        for(int i=0;i<fields.length;i++){
            //System.out.println("2field");
            org.unesco.jisis.z3950.Field  f = fields[i];
            org.unesco.jisis.z3950.SubField sfields[] = f.getSubFields();
            
            if(sfields!=null){
                org.marc4j.marc.DataField df=mf.newDataField(f.getTag(), f.getIndicator1(), f.getIndicator2());
                
                for(int j=0;j<sfields.length;j++){
                    df.addSubfield(mf.newSubfield(sfields[j].getDelimiter(), sfields[j].getData()));
                }
                record.addVariableField(df);
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
        MarcXmlWriter writer = new MarcXmlWriter(bos,"UTF-8",true);
        //System.out.println("record is..."+(record==null)+"........"+(writer==null));
        writer.write(record);
        
        writer.close();
        try{
            marcXml=bos.toString("UTF-8");
        }catch (Exception exp){exp.printStackTrace();}
        return marcXml;
    }
    
    public java.util.Hashtable getDetails(String isoDump) {
        
        CatalogMaterialDescription cmd = this.getMarcModelFromMarc(isoDump);
        String title="";
        String remainderOfTitle="";
        String statementOfResponsibility="";
        String parallelTitles="";
        String varyingFormOfTitle="";
        String edition="";
        String publisher="";
        String physicalDescription="";
        String isbn="";
        String issn="";
        String bibnote="";
        String languagenote="";
        String sysDetNote="";
        String addtionFormNote="";
        String generalNote="";
        String summaryNote="";
        String dataNote="";
        String creditsNote="";
        String dissertaionNote="";
        String author="";
        Vector vecSubject=new Vector(1,1);
        Vector vecAddedEntries=new Vector(1,1);
        Vector vecSeries=new Vector(1,1);
        java.util.Hashtable hashISO=new java.util.Hashtable();
        String partSection="";
        String parallelTitle="",distinctiveTitle="",otherTitle="",coverTitle="",addedTitle="",captionTitle="",runningTitle="",spineTitle="";
        
        
        
        if(cmd!=null){
            Field[] datalist = cmd.getFields();
            //System.out.println("in getDetails..."+datalist.size());
            for(int i=0; i<datalist.length; i++) {
                Field daf = (Field)datalist[i];
                String tag = daf.getTag();
                
                if(tag.equals("245")) {
                    SubField[] subfieldList1=daf.getSubFields();
                    //System.out.println("subfield list.."+subfieldList1.size());
                    for(int sf=0;sf<subfieldList1.length;sf++){
                        SubField subf=(SubField)subfieldList1[sf];
                        char code=subf.getDelimiter();
                        //System.out.println("code is.."+String.valueOf(code));
                        if(subf.getDelimiter()=='a' ){
                            title =(new String(subf.getData()));
                        }else if(subf.getDelimiter()=='b' ){
                            remainderOfTitle=(new String(subf.getData()));
                        }else if(subf.getDelimiter()=='c' ){
                            statementOfResponsibility=(new String(subf.getData()));
                        }else if(subf.getDelimiter()=='n' ){
                            partSection=(new String(subf.getData()));
                        }
                    }
                    title=title+" "+partSection;
                    
                    //                org.marc4j.marc.Subfield sf = daf.getSubfield('a');
                    //                System.out.println("sf is   "+(sf==null));
                    //                if(sf!=null){
                    //                    System.out.println("titl is present");
                    //                    title=String.valueOf(sf.getData());
                    //                }
                    //                sf = daf.getSubfield('b');
                    //                if(sf!=null){
                    //                    remainderOfTitle=String.valueOf(sf.getData());
                    //                }
                    //                sf = daf.getSubfield('c');
                    //                if(sf!=null){
                    //                    statementOfResponsibility=String.valueOf(sf.getData());
                    //                }
                }
                if(tag.equals("100") && author.equals("")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        author=String.valueOf(sf.getData());
                    }
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e')
                                author+="-"+sfsub.getData();
                        }
                    }
                }
                if(tag.equals("110") && author.equals("")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        author=String.valueOf(sf.getData());
                    }
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e')
                                author+="-"+sfsub.getData();
                        }
                    }
                }
                if(tag.equals("111") && author.equals("")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        author=String.valueOf(sf.getData());
                    }
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e')
                                author+="-"+sfsub.getData();
                        }
                    }
                }
                if(tag.equals("246")){
                    SubField sf = daf.getSubField('a');
                    String ind2=String.valueOf(daf.getIndicator2());
                    String ptitle="";
                    String psor="";
                    if(sf!=null){
                        ptitle=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('b');
                    if(sf!=null){
                        psor=String.valueOf(sf.getData());
                    }
                    if(!psor.equals(""))
                        ptitle+="/ "+psor;
                    if(parallelTitles.equals(""))
                        parallelTitles=ptitle;
                    else
                        parallelTitles+=": "+ptitle;
                    
                    if(ind2.trim().equals("1")||ind2.trim().equals("0")){
                        if(parallelTitle.trim().equals("")){
                            parallelTitle=ptitle;
                        }else{
                            parallelTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("2")){
                        if(distinctiveTitle.trim().equals("")){
                            distinctiveTitle=ptitle;
                        }else{
                            distinctiveTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("3")){
                        //otherTitle
                        if(otherTitle.trim().equals("")){
                            otherTitle=ptitle;
                        }else{
                            otherTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("4")){
                        if(coverTitle.trim().equals("")){
                            coverTitle=ptitle;
                        }else{
                            coverTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("5")){
                        //addedTitle
                        if(addedTitle.trim().equals("")){
                            addedTitle=ptitle;
                        }else{
                            addedTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("6")){
                        //captionTitle
                        if(captionTitle.trim().equals("")){
                            captionTitle=ptitle;
                        }else{
                            captionTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("7")){
                        if(runningTitle.trim().equals("")){
                            runningTitle=ptitle;
                        }else{
                            runningTitle+=" , "+ptitle;
                        }
                    }else if(ind2.trim().equals("8")){
                        if(spineTitle.trim().equals("")){
                            spineTitle=ptitle;
                        }else{
                            spineTitle+=" , "+ptitle;
                        }
                    }
                    
                    
                    
                }
                if(tag.equals("247")){
                    SubField sf = daf.getSubField('a');
                    String ptitle="";
                    String psor="";
                    if(sf!=null){
                        ptitle=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('b');
                    if(sf!=null){
                        psor=String.valueOf(sf.getData());
                    }
                    if(!psor.equals(""))
                        ptitle+=": "+psor;
                    if(varyingFormOfTitle.equals(""))
                        varyingFormOfTitle=ptitle;
                    else
                        varyingFormOfTitle+="; "+ptitle;
                }
                if(tag.equals("250")) {
                    SubField sf = daf.getSubField('a');
                    String remed="";
                    if(sf!=null){
                        edition=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('b');
                    if(sf!=null){
                        remed=String.valueOf(sf.getData());
                    }
                    edition+=" "+remed;
                }
                if(tag.equals("260")) {
                    String pub="";
                    String pubyear="";
                    String pubplace="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        pubplace=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('b');
                    if(sf!=null){
                        pub=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('c');
                    if(sf!=null){
                        pubyear=String.valueOf(sf.getData());
                        hashISO.put("YEAR",pubyear);
                    }
                    publisher=pubplace;
                    if(!pub.equals(""))
                        publisher+=": "+pub;
                    if(!pubyear.equals(""))
                        publisher+=", "+pubyear;
                    
                }
                if(tag.equals("300")) {
                    String col="";
                    String dimen="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        col=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('c');
                    if(sf!=null){
                        dimen=String.valueOf(sf.getData());
                    }
                    physicalDescription=col;
                    if(!dimen.equals(""))
                        physicalDescription+="; "+dimen;
                }
                if(tag.equals("020")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(isbn.equals(""))
                            isbn+=String.valueOf(sf.getData());
                        else
                            isbn+="; "+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("022")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(issn.equals(""))
                            issn+=String.valueOf(sf.getData());
                        else
                            issn+="; "+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("504")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(bibnote.equals(""))
                            bibnote+=String.valueOf(sf.getData());
                        else
                            bibnote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("546")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(languagenote.equals(""))
                            languagenote+=String.valueOf(sf.getData());
                        else
                            languagenote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("538")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(sysDetNote.equals(""))
                            sysDetNote+=String.valueOf(sf.getData());
                        else
                            sysDetNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("530")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(addtionFormNote.equals(""))
                            addtionFormNote+=String.valueOf(sf.getData());
                        else
                            addtionFormNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("500")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(generalNote.equals(""))
                            generalNote+=String.valueOf(sf.getData());
                        else
                            generalNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("520") || tag.equals("510")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(summaryNote.equals(""))
                            summaryNote+=String.valueOf(sf.getData());
                        else
                            summaryNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("516")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(dataNote.equals(""))
                            dataNote+=String.valueOf(sf.getData());
                        else
                            dataNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("508")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(creditsNote.equals(""))
                            creditsNote+=String.valueOf(sf.getData());
                        else
                            creditsNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("502")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(dissertaionNote.equals(""))
                            dissertaionNote+=String.valueOf(sf.getData());
                        else
                            dissertaionNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("650") || tag.equals("651") || tag.equals("600")){
                    String subject="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null)
                        subject=String.valueOf(sf.getData());
                    SubField[] lisuvs = daf.getSubFields('x');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subject+="-"+sfsub.getData();
                        }
                    }
                    lisuvs = daf.getSubFields('y');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subject+="-"+sfsub.getData();
                        }
                    }
                    lisuvs = daf.getSubFields('v');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subject+="-"+sfsub.getData();
                        }
                    }
                    lisuvs = daf.getSubFields('z');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subject+="-"+sfsub.getData();
                        }
                    }
                    vecSubject.addElement(subject);
                }
                if(tag.equals("700") || tag.equals("710") || tag.equals("711")){
                    String subject="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null)
                        subject=String.valueOf(sf.getData());
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        String jtauth="";
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e')
                                subject+="-"+sfsub.getData();
                            else{
                                if(sfsub.getDelimiter()=='e')
                                    jtauth+="-"+sfsub.getData();
                            }
                        }
                        if(jtauth==null || jtauth.trim().equals("")){
                            subject+=" jt. auth.";
                        } else{
                            subject+="-"+jtauth;
                        }
                    }
                    vecAddedEntries.addElement(subject);
                }
                if(tag.equals("440")){
                    String subject="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null)
                        subject=String.valueOf(sf.getData());
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a')
                                subject+="-"+sfsub.getData();
                        }
                    }
                    vecSeries.addElement(subject);
                }
            }
            hashISO.put("AUTHOR", author);
            String titleArea = "";
            if(!title.equals("")){
                hashISO.put("TITLE", title);
                hashISO.put("TITLE_SOR", title);
                titleArea=title;
            }
            if(!remainderOfTitle.equals("")){
                hashISO.put("REMAINDER_OF_TITLE", remainderOfTitle);
                titleArea+=" : "+remainderOfTitle;
            }
            if(!statementOfResponsibility.equals("")){
                hashISO.put("STATEMENT_OF_RESPONSIBILITY", statementOfResponsibility);
                hashISO.put("TITLE_SOR", title+" / "+statementOfResponsibility);
                titleArea+=" / "+statementOfResponsibility;
            }
            hashISO.put("TITLE_AREA",titleArea);
            if(!parallelTitles.equals("")){
                hashISO.put("PARALLEL_TITLE_STR", parallelTitles);
            }
            if(!varyingFormOfTitle.equals("")){
                hashISO.put("VARYING_FORM_OF_TITLE", varyingFormOfTitle);
            }
            if(!edition.equals("")){
                hashISO.put("EDITION", edition);
            }
            if(!publisher.equals("")){
                hashISO.put("PUBLISHER", publisher);
            }
            if(!physicalDescription.equals("")){
                hashISO.put("PHYSICAL_DESCRIPTION", physicalDescription);
            }
            if(!isbn.equals("")){
                hashISO.put("ISBN", isbn);
            }
            if(!issn.equals("")){
                hashISO.put("ISSN", issn);
            }
            if(!bibnote.equals("")){
                hashISO.put("BIBLIOGRAPHIC_NOTE", bibnote);
            }
            if(!languagenote.equals("")){
                hashISO.put("LANGUAGE_NOTE", languagenote);
            }
            if(!sysDetNote.equals("")){
                hashISO.put("SYSTEM_DETAILS_NOTE", sysDetNote);
            }
            if(!addtionFormNote.equals("")){
                hashISO.put("ADDITIONAL_PHYSICAL_FORM_AVAILABLE_NOTE", addtionFormNote);
            }
            if(!generalNote.equals("")){
                hashISO.put("GENERAL_NOTE", generalNote);
            }
            if(!summaryNote.equals("")){
                hashISO.put("SUMMARY_NOTE", summaryNote);
            }
            if(!dataNote.equals("")){
                hashISO.put("TYPE_OF_COMPUTER_FILE_DATA_NOTE", dataNote);
            }
            if(!creditsNote.equals("")){
                hashISO.put("CREATION_PRODUCTION_CREDITS_NOTE", creditsNote);
            }
            if(!dissertaionNote.equals("")){
                hashISO.put("DISSERTATION_NOTE", dissertaionNote);
            }
            //parallelTitle="",distinctiveTitle="",otherTitle="",coverTitle="",addedTitle="",captionTitle="",runningTitle="",spineTitle="";
            if(!parallelTitle.equals("")){
                hashISO.put("PARALLEL_TITLE", parallelTitle);
            }
            if(!distinctiveTitle.equals("")){
                hashISO.put("DISTINCTIVE_TITLE", distinctiveTitle);
            }
            if(!otherTitle.equals("")){
                hashISO.put("OTHER_TITLE", otherTitle);
            }
            if(!coverTitle.equals("")){
                hashISO.put("COVER_TITLE", coverTitle);
            }
            if(!addedTitle.equals("")){
                hashISO.put("ADDEDTITLEPAGE_TITLE", addedTitle);
            }
            if(!captionTitle.equals("")){
                hashISO.put("CAPTION_TITLE", captionTitle);
            }
            if(!runningTitle.equals("")){
                hashISO.put("RUNNING_TITLE", runningTitle);
            }
            if(!spineTitle.equals("")){
                hashISO.put("SPINE_TITLE", spineTitle);
            }
            hashISO.put("SUBJECTS",vecSubject);
            hashISO.put("ADDED_ENTRIES",vecAddedEntries);
            hashISO.put("SERIES",vecSeries);
        }
        return hashISO;
    }
    
      public java.util.Hashtable getDetailsRemoveSpecialChars(String isoDump) {
        
//        CatalogMaterialDescription cmd = this.marc8ToUnicode(isoDump);
        CatalogMaterialDescription cmd = this.getMarcModelFromMarcRemoveSpecialCharacters(isoDump);
        String title="";
        String remainderOfTitle="";
        String statementOfResponsibility="";
        String parallelTitles="";
        String varyingFormOfTitle="";
        String edition="";
        String publisher="";
        String physicalDescription="";
        String isbn="";
        String issn="";
        String bibnote="";
        String languagenote="";
        String sysDetNote="";
        String addtionFormNote="";
        String generalNote="";
        String summaryNote="";
        String dataNote="";
        String creditsNote="";
        String dissertaionNote="";
        String author="";
        Vector vecSubject=new Vector(1,1);
        Vector vecAddedEntries=new Vector(1,1);
        Vector vecSeries=new Vector(1,1);
        java.util.Hashtable hashISO=new java.util.Hashtable();
        String parallelTitle="",distinctiveTitle="",otherTitle="",coverTitle="",addedTitle="",captionTitle="",runningTitle="",spineTitle="";
        
        
        
        if(cmd!=null){
            Field[] datalist = cmd.getFields();
            //System.out.println("in getDetails..."+datalist.size());
            for(int i=0; i<datalist.length; i++) {
                Field daf = (Field)datalist[i];
                String tag = daf.getTag();
                
                if(tag.equals("245")) {
                    SubField[] subfieldList1=daf.getSubFields();
                    //System.out.println("subfield list.."+subfieldList1.size());
                    for(int sf=0;sf<subfieldList1.length;sf++){
                        SubField subf=(SubField)subfieldList1[sf];
                        char code=subf.getDelimiter();
                        //System.out.println("code is.."+String.valueOf(code));
                        if(subf.getDelimiter()=='a' ){
                            title =(new String(subf.getData()));
                        }else if(subf.getDelimiter()=='b' ){
                            remainderOfTitle=(new String(subf.getData()));
                        }else if(subf.getDelimiter()=='c' ){
                            statementOfResponsibility=(new String(subf.getData()));
                        }
                    }
                    //                org.marc4j.marc.Subfield sf = daf.getSubfield('a');
                    //                System.out.println("sf is   "+(sf==null));
                    //                if(sf!=null){
                    //                    System.out.println("titl is present");
                    //                    title=String.valueOf(sf.getData());
                    //                }
                    //                sf = daf.getSubfield('b');
                    //                if(sf!=null){
                    //                    remainderOfTitle=String.valueOf(sf.getData());
                    //                }
                    //                sf = daf.getSubfield('c');
                    //                if(sf!=null){
                    //                    statementOfResponsibility=String.valueOf(sf.getData());
                    //                }
                }
                if(tag.equals("100") && author.equals("")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        author=String.valueOf(sf.getData());
                    }
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e')
                                author+="-"+sfsub.getData();
                        }
                    }
                }
                if(tag.equals("110") && author.equals("")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        author=String.valueOf(sf.getData());
                    }
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e')
                                author+="-"+sfsub.getData();
                        }
                    }
                }
                if(tag.equals("111") && author.equals("")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        author=String.valueOf(sf.getData());
                    }
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e')
                                author+="-"+sfsub.getData();
                        }
                    }
                }
                if(tag.equals("246")){
                    SubField sf = daf.getSubField('a');
                    String ind2=String.valueOf(daf.getIndicator2());
                    String ptitle="";
                    String psor="";
                    if(sf!=null){
                        ptitle=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('b');
                    if(sf!=null){
                        psor=String.valueOf(sf.getData());
                    }
                    if(!psor.equals(""))
                        ptitle+="/ "+psor;
                    if(parallelTitles.equals(""))
                        parallelTitles=ptitle;
                    else
                        parallelTitles+=": "+ptitle;
                    
                    if(ind2.trim().equals("1")||ind2.trim().equals("0")){
                        if(parallelTitle.trim().equals("")){
                            parallelTitle=ptitle;
                        }else{
                            parallelTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("2")){
                        if(distinctiveTitle.trim().equals("")){
                            distinctiveTitle=ptitle;
                        }else{
                            distinctiveTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("3")){
                        //otherTitle
                        if(otherTitle.trim().equals("")){
                            otherTitle=ptitle;
                        }else{
                            otherTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("4")){
                        if(coverTitle.trim().equals("")){
                            coverTitle=ptitle;
                        }else{
                            coverTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("5")){
                        //addedTitle
                        if(addedTitle.trim().equals("")){
                            addedTitle=ptitle;
                        }else{
                            addedTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("6")){
                        //captionTitle
                        if(captionTitle.trim().equals("")){
                            captionTitle=ptitle;
                        }else{
                            captionTitle+=" , "+ptitle;
                        }
                        
                    }else if(ind2.trim().equals("7")){
                        if(runningTitle.trim().equals("")){
                            runningTitle=ptitle;
                        }else{
                            runningTitle+=" , "+ptitle;
                        }
                    }else if(ind2.trim().equals("8")){
                        if(spineTitle.trim().equals("")){
                            spineTitle=ptitle;
                        }else{
                            spineTitle+=" , "+ptitle;
                        }
                    }
                    
                    
                    
                }
                if(tag.equals("247")){
                    SubField sf = daf.getSubField('a');
                    String ptitle="";
                    String psor="";
                    if(sf!=null){
                        ptitle=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('b');
                    if(sf!=null){
                        psor=String.valueOf(sf.getData());
                    }
                    if(!psor.equals(""))
                        ptitle+=": "+psor;
                    if(varyingFormOfTitle.equals(""))
                        varyingFormOfTitle=ptitle;
                    else
                        varyingFormOfTitle+="; "+ptitle;
                }
                if(tag.equals("250")) {
                    SubField sf = daf.getSubField('a');
                    String remed="";
                    if(sf!=null){
                        edition=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('b');
                    if(sf!=null){
                        remed=String.valueOf(sf.getData());
                    }
                    edition+=" "+remed;
                }
                if(tag.equals("260")) {
                    String pub="";
                    String pubyear="";
                    String pubplace="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        pubplace=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('b');
                    if(sf!=null){
                        pub=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('c');
                    if(sf!=null){
                        pubyear=String.valueOf(sf.getData());
                        hashISO.put("YEAR",pubyear);
                    }
                    publisher=pubplace;
                    if(!pub.equals(""))
                        publisher+=": "+pub;
                    if(!pubyear.equals(""))
                        publisher+=", "+pubyear;
                    
                }
                if(tag.equals("300")) {
                    String col="";
                    String dimen="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        col=String.valueOf(sf.getData());
                    }
                    sf = daf.getSubField('c');
                    if(sf!=null){
                        dimen=String.valueOf(sf.getData());
                    }
                    physicalDescription=col;
                    if(!dimen.equals(""))
                        physicalDescription+="; "+dimen;
                }
                if(tag.equals("020")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(isbn.equals(""))
                            isbn+=String.valueOf(sf.getData());
                        else
                            isbn+="; "+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("022")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(issn.equals(""))
                            issn+=String.valueOf(sf.getData());
                        else
                            issn+="; "+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("504")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(bibnote.equals(""))
                            bibnote+=String.valueOf(sf.getData());
                        else
                            bibnote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("546")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(languagenote.equals(""))
                            languagenote+=String.valueOf(sf.getData());
                        else
                            languagenote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("538")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(sysDetNote.equals(""))
                            sysDetNote+=String.valueOf(sf.getData());
                        else
                            sysDetNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("530")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(addtionFormNote.equals(""))
                            addtionFormNote+=String.valueOf(sf.getData());
                        else
                            addtionFormNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("500")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(generalNote.equals(""))
                            generalNote+=String.valueOf(sf.getData());
                        else
                            generalNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("520") || tag.equals("510")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(summaryNote.equals(""))
                            summaryNote+=String.valueOf(sf.getData());
                        else
                            summaryNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("516")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(dataNote.equals(""))
                            dataNote+=String.valueOf(sf.getData());
                        else
                            dataNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("508")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(creditsNote.equals(""))
                            creditsNote+=String.valueOf(sf.getData());
                        else
                            creditsNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("502")){
                    SubField sf = daf.getSubField('a');
                    if(sf!=null){
                        if(dissertaionNote.equals(""))
                            dissertaionNote+=String.valueOf(sf.getData());
                        else
                            dissertaionNote+="; \n"+String.valueOf(sf.getData());
                    }
                }
                if(tag.equals("650") || tag.equals("651") || tag.equals("600")){
                    String subject="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null)
                        subject=String.valueOf(sf.getData());
                    SubField[] lisuvs = daf.getSubFields('x');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subject+="-"+sfsub.getData();
                        }
                    }
                    lisuvs = daf.getSubFields('y');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subject+="-"+sfsub.getData();
                        }
                    }
                    lisuvs = daf.getSubFields('v');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subject+="-"+sfsub.getData();
                        }
                    }
                    lisuvs = daf.getSubFields('z');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subject+="-"+sfsub.getData();
                        }
                    }
                    vecSubject.addElement(subject);
                }
                if(tag.equals("700") || tag.equals("710") || tag.equals("711")){
                    String subject="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null)
                        subject=String.valueOf(sf.getData());
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        String jtauth="";
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e')
                                subject+="-"+sfsub.getData();
                            else{
                                if(sfsub.getDelimiter()=='e')
                                    jtauth+="-"+sfsub.getData();
                            }
                        }
                        if(jtauth==null || jtauth.trim().equals("")){
                            subject+=" jt. auth.";
                        } else{
                            subject+="-"+jtauth;
                        }
                    }
                    vecAddedEntries.addElement(subject);
                }
                if(tag.equals("440")){
                    String subject="";
                    SubField sf = daf.getSubField('a');
                    if(sf!=null)
                        subject=String.valueOf(sf.getData());
                    SubField[] lisuvs = daf.getSubFields();
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            if(sfsub.getDelimiter()!='a')
                                subject+="-"+sfsub.getData();
                        }
                    }
                    vecSeries.addElement(subject);
                }
            }
            hashISO.put("AUTHOR", author);
            String titleArea = "";
            if(!title.equals("")){
                hashISO.put("TITLE", title);
                hashISO.put("TITLE_SOR", title);
                titleArea=title;
            }
            if(!remainderOfTitle.equals("")){
                hashISO.put("REMAINDER_OF_TITLE", remainderOfTitle);
                titleArea+=" : "+remainderOfTitle;
            }
            if(!statementOfResponsibility.equals("")){
                hashISO.put("STATEMENT_OF_RESPONSIBILITY", statementOfResponsibility);
                hashISO.put("TITLE_SOR", title+" / "+statementOfResponsibility);
                titleArea+=" / "+statementOfResponsibility;
            }
            hashISO.put("TITLE_AREA",titleArea);
            if(!parallelTitles.equals("")){
                hashISO.put("PARALLEL_TITLE_STR", parallelTitles);
            }
            if(!varyingFormOfTitle.equals("")){
                hashISO.put("VARYING_FORM_OF_TITLE", varyingFormOfTitle);
            }
            if(!edition.equals("")){
                hashISO.put("EDITION", edition);
            }
            if(!publisher.equals("")){
                hashISO.put("PUBLISHER", publisher);
            }
            if(!physicalDescription.equals("")){
                hashISO.put("PHYSICAL_DESCRIPTION", physicalDescription);
            }
            if(!isbn.equals("")){
                hashISO.put("ISBN", isbn);
            }
            if(!issn.equals("")){
                hashISO.put("ISSN", issn);
            }
            if(!bibnote.equals("")){
                hashISO.put("BIBLIOGRAPHIC_NOTE", bibnote);
            }
            if(!languagenote.equals("")){
                hashISO.put("LANGUAGE_NOTE", languagenote);
            }
            if(!sysDetNote.equals("")){
                hashISO.put("SYSTEM_DETAILS_NOTE", sysDetNote);
            }
            if(!addtionFormNote.equals("")){
                hashISO.put("ADDITIONAL_PHYSICAL_FORM_AVAILABLE_NOTE", addtionFormNote);
            }
            if(!generalNote.equals("")){
                hashISO.put("GENERAL_NOTE", generalNote);
            }
            if(!summaryNote.equals("")){
                hashISO.put("SUMMARY_NOTE", summaryNote);
            }
            if(!dataNote.equals("")){
                hashISO.put("TYPE_OF_COMPUTER_FILE_DATA_NOTE", dataNote);
            }
            if(!creditsNote.equals("")){
                hashISO.put("CREATION_PRODUCTION_CREDITS_NOTE", creditsNote);
            }
            if(!dissertaionNote.equals("")){
                hashISO.put("DISSERTATION_NOTE", dissertaionNote);
            }
            //parallelTitle="",distinctiveTitle="",otherTitle="",coverTitle="",addedTitle="",captionTitle="",runningTitle="",spineTitle="";
            if(!parallelTitle.equals("")){
                hashISO.put("PARALLEL_TITLE", parallelTitle);
            }
            if(!distinctiveTitle.equals("")){
                hashISO.put("DISTINCTIVE_TITLE", distinctiveTitle);
            }
            if(!otherTitle.equals("")){
                hashISO.put("OTHER_TITLE", otherTitle);
            }
            if(!coverTitle.equals("")){
                hashISO.put("COVER_TITLE", coverTitle);
            }
            if(!addedTitle.equals("")){
                hashISO.put("ADDEDTITLEPAGE_TITLE", addedTitle);
            }
            if(!captionTitle.equals("")){
                hashISO.put("CAPTION_TITLE", captionTitle);
            }
            if(!runningTitle.equals("")){
                hashISO.put("RUNNING_TITLE", runningTitle);
            }
            if(!spineTitle.equals("")){
                hashISO.put("SPINE_TITLE", spineTitle);
            }
            hashISO.put("SUBJECTS",vecSubject);
            hashISO.put("ADDED_ENTRIES",vecAddedEntries);
            hashISO.put("SERIES",vecSeries);
        }
        return hashISO;
    }
    
    
    public java.util.Hashtable getISBD(String isoDump, boolean onlyNoLinkDetails) {
        java.util.Hashtable htISBD = new java.util.Hashtable();
        CatalogMaterialDescription cmd = this.getMarcModelFromMarc(isoDump);
        if(cmd!=null){
            htISBD = getNoLinkDetailsWith880(cmd, htISBD);
            if(onlyNoLinkDetails) return htISBD;
            
            String mainEntry = null, uniformTitle = null, relatedNames = null, seriesAuthors = null, subjects = null;
            java.util.Hashtable htLinkingEntries = null;
            
            mainEntry = getMainEntry(cmd);
            if(!mainEntry.trim().equals("")) htISBD.put("MainEntry", mainEntry);
            
            uniformTitle = getUniformTitle(cmd);
            if(!uniformTitle.trim().equals("")) htISBD.put("UniformTitle", uniformTitle);
            
            relatedNames = getRelatedNames(cmd);
            //System.out.println("relatedNames##################"+relatedNames);
            if(!relatedNames.trim().equals("")) 
                htISBD.put("RelatedNames", relatedNames);
            
            seriesAuthors = getSeriesAuthors(cmd);
            if(!seriesAuthors.trim().equals("")) htISBD.put("SeriesAuthors", seriesAuthors);
            
            subjects = getSubjects(cmd);
            if(!subjects.trim().equals("")) htISBD.put("Subjects", subjects);
            
            htLinkingEntries = getLinkingEntries(cmd);
            if(htLinkingEntries != null) htISBD.put("LinkingEntries", htLinkingEntries);
        }
        //System.out.println("Convertr.getISBD"+htISBD);
        return htISBD;
    }
    
    public java.util.Hashtable getNoLinkDetailsWith880(CatalogMaterialDescription cmd, java.util.Hashtable htISBD) {
        if(cmd!=null){
            String title = null, edition = null, publication = null, pubPlace = null, pub = null, pubYear = null, relatedTitle = null, physicalDescription = null, seriesTitle = null, isbn = null, issn = null;
            java.util.Hashtable notesHt = new java.util.Hashtable();
            
            title = getTitle(cmd);
            if(!title.trim().equals("")) htISBD.put("TITLE_SOR", title);
            
            edition = getEdition(cmd);
            if(!edition.trim().equals("")) htISBD.put("EDITION", edition);
            
            publication = getPublication(cmd);
            if(!publication.trim().equals("")) htISBD.put("PUBLISHER", publication);
            
            pubPlace = getPubPlace(cmd);
            if(!pubPlace.trim().equals("")) htISBD.put("Place", pubPlace);
            
            pub = getPub(cmd);
            if(!pub.trim().equals("")) htISBD.put("Publisher", pub);
            
            pubYear = getPubYear(cmd);
            if(!pubYear.trim().equals("")) htISBD.put("Year", pubYear);
            
            relatedTitle = getRelatedTitles(cmd);
            if(!relatedTitle.trim().equals("")) htISBD.put("RelatedTitle", relatedTitle);
            
            physicalDescription = getPhysicalDescription(cmd);
            if(!physicalDescription.trim().equals("")) htISBD.put("PHYSICAL_DESCRIPTION", physicalDescription);
            
            seriesTitle = getSeriesTitle(cmd);
            if(!seriesTitle.trim().equals("")) htISBD.put("SeriesTitle", seriesTitle);
            
            String notes = null;
            
            notes = getXXXNotes(cmd, "500");
            if(!notes.trim().equals("")) notesHt.put("GENERAL_NOTE", notes);
            
            notes = getXXXNotes(cmd, "502");
            if(!notes.trim().equals("")) notesHt.put("DISSERTATION_NOTE", notes);
            
            notes = getXXXNotes(cmd, "504");
            if(!notes.trim().equals("")) notesHt.put("BIBLIOGRAPHIC_NOTE", notes);
            
            notes = getXXXNotes(cmd, "505");
            if(!notes.trim().equals("")) notesHt.put("FORMATTED_CONTENTS_NOTE", notes);
            
            notes = getXXXNotes(cmd, "520");
            if(!notes.trim().equals("")) notesHt.put("SUMMARY_NOTE", notes);
            
            notes = getXXXNotes(cmd, "516");
            if(!notes.trim().equals("")) notesHt.put("TYPE_OF_COMPUTER_FILE_DATA_NOTE", notes);
            
            notes = getXXXNotes(cmd, "508");
            if(!notes.trim().equals("")) notesHt.put("CREATION_PRODUCTION_CREDITS_NOTE", notes);
            
            notes = getXXXNotes(cmd, "546");
            if(!notes.trim().equals("")) notesHt.put("LANGUAGE_NOTE", notes);
            
            notes = getXXXNotes(cmd, "538");
            if(!notes.trim().equals("")) notesHt.put("SYSTEM_DETAILS_NOTE", notes);
            
            notes = getXXXNotes(cmd, "530");
            if(!notes.trim().equals("")) notesHt.put("ADDITIONAL_PHYSICAL_FORM_AVAILABLE_NOTE", notes);
            
            if(notesHt.size()>0) htISBD.put("NOTES", notesHt);
            
            isbn = getISBN(cmd);
            if(!isbn.equals("")) htISBD.put("ISBN", isbn);
            
            issn = getISSN(cmd);
            if(!issn.equals("")) htISBD.put("ISSN", issn);
        }
        return htISBD;
    }
    
    public String getMainEntry(CatalogMaterialDescription cmd){
        String author = getFieldDataFormCMD(cmd, "100");
        if(author.equals(""))
            author = getFieldDataFormCMD(cmd, "110");
        if(author.equals(""))
            author = getFieldDataFormCMD(cmd, "111");
        return author;
    }
    
    public String getFieldDataFormCMD(CatalogMaterialDescription cmd, String tag){
        String data = "";
        try{
            if(cmd.getField(tag) != null){
                Field field = cmd.getField(tag);
                if(field.getSubFieldData('a') != null) data = field.getSubFieldData('a');
                SubField[] subFieldArray = field.getSubFields();
                if(subFieldArray != null && subFieldArray.length!=0){
                    for(int k=0; k<subFieldArray.length; k++){
                        SubField subField = (SubField)subFieldArray[k];
                        if(subField.getDelimiter()!='a' && subField.getDelimiter()!='e' && subField.getDelimiter()!='6')
                            data += "-"+subField.getData();
                    }
                }
                if(field.getSubFieldData('6') != null){
                    String data880 = getParticular880Data(tag, field.getSubFieldData('6'), cmd);
                    if(!data880.equals("")) data += "<br>"+data880;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return data;
    }
    
    public String getUniformTitle(CatalogMaterialDescription cmd){
        String uniformTitle = "";
        try{
            if(cmd.getField("240") != null){
                Field field = cmd.getField("240");
                if(field.getSubFieldData('a') != null) uniformTitle = field.getSubFieldData('a');
                if(field.getSubFieldData('f') != null) uniformTitle += " "+field.getSubFieldData('f');
                if(field.getSubFieldData('6') != null){
                    String data880 = getParticular880Data("240", field.getSubFieldData('6'), cmd);
                    if(!data880.equals("")) uniformTitle += "<br>"+data880;
                }
            }else if(cmd.getField("130") != null){
                Field field = cmd.getField("130");
                if(field.getSubFieldData('a') != null) uniformTitle = field.getSubFieldData('a');
                if(field.getSubFieldData('f') != null) uniformTitle += " "+field.getSubFieldData('f');
                if(field.getSubFieldData('6') != null){
                    String data880 = getParticular880Data("130", field.getSubFieldData('6'), cmd);
                    if(!data880.equals("")) uniformTitle += "<br>"+data880;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return uniformTitle;
    }
    
    public String getTitle(CatalogMaterialDescription cmd){
        String title = "";
        try{
            if(cmd.getField("245") != null){
                Field field = cmd.getField("245");
                if(field.getSubFieldData('a') != null)
                    title = field.getSubFieldData('a');
                Field[] field246Array = cmd.getFields("246");
                for(int i=0; i<field246Array.length; i++){
                    Field field246 = (Field)field246Array[i];
                    if(field246.getSubFieldData('a') != null)
                        title += " = "+field246.getSubFieldData('a');
                }
                if(field.getSubFieldData('n') != null)
                    title += " "+field.getSubFieldData('n');
                if(field.getSubFieldData('b') != null)
                    title += " : "+field.getSubFieldData('b');
                if(field.getSubFieldData('c') != null)
                    title += " / "+field.getSubFieldData('c');
                if(field.getSubFieldData('6') != null){
                    String data880 = getParticular880Data("245", field.getSubFieldData('6'), cmd);
                    if(!data880.equals("")) title += "<br>"+data880;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return title;
    }
    
    public String getEdition(CatalogMaterialDescription cmd){
        String edition = "";
        try{
            if(cmd.getField("250") != null){
                Field field = cmd.getField("250");
                if(field.getSubFields() != null && field.getSubFields().length>0){
                    if(field.getSubFieldData('a') != null)
                        edition = field.getSubFieldData('a');
                    if(field.getSubFieldData('b') != null)
                        edition += " "+field.getSubFieldData('b');
                    if(field.getSubFieldData('6') != null){
                        String data880 = getParticular880Data("250", field.getSubFieldData('6'), cmd);
                        if(!data880.equals("")) edition += "<br>"+data880;
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return edition;
    }
    
    public String getPublication(CatalogMaterialDescription cmd){
        String publication = "";
        try{
            Field[] field260Array = cmd.getFields("260");
            for(int j=0; j<field260Array.length; j++){
                Field field260 = (Field)field260Array[j];
                if(field260.getSubFields().length>0){
                    if(j!=0 && !publication.trim().equals("")) publication += " ; ";
                    if(field260.getSubFieldData('a') != null)
                        publication += field260.getSubFieldData('a');
                    if(field260.getSubFieldData('b') != null){
                        if(!publication.trim().equals(""))
                            publication += " : "+field260.getSubFieldData('b');
                        else publication += field260.getSubFieldData('b');
                    }
                    if(field260.getSubFieldData('c') != null){
                        if(!publication.trim().equals(""))
                            publication += " , "+field260.getSubFieldData('c');
                        else publication += field260.getSubFieldData('c');
                    }
                    if(field260.getSubFieldData('e') != null || field260.getSubFieldData('f') != null || field260.getSubFieldData('g') != null)
                        publication += " (";
                    if(field260.getSubFieldData('e') != null)
                        publication += field260.getSubFieldData('e');
                    if(field260.getSubFieldData('f') != null)
                        publication += " : "+field260.getSubFieldData('f');
                    if(field260.getSubFieldData('g') != null)
                        publication += " , "+field260.getSubFieldData('g');
                    if(field260.getSubFieldData('e') != null || field260.getSubFieldData('f') != null || field260.getSubFieldData('g') != null)
                        publication += ")";
                    if(field260.getSubFieldData('6') != null){
                        String data880 = getParticular880Data("260", field260.getSubFieldData('6'), cmd);
                        if(!data880.equals("")) publication += "<br>"+data880;
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return publication;
    }
    
    public String getPubPlace(CatalogMaterialDescription cmd){
        String pubPlace = "";
        try{
            Field[] field260Array = cmd.getFields("260");
            for(int j=0; j<field260Array.length; j++){
                Field field260 = (Field)field260Array[j];
                if(field260.getSubFields().length>0){
                    if(field260.getSubFieldData('a') != null && !field260.getSubFieldData('a').equals("")){
                        if(j!=0 && !pubPlace.trim().equals("")) pubPlace += " ; ";
                        pubPlace += field260.getSubFieldData('a');
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return pubPlace;
    }
    
    public String getPub(CatalogMaterialDescription cmd){
        String pub = "";
        try{
            Field[] field260Array = cmd.getFields("260");
            for(int j=0; j<field260Array.length; j++){
                Field field260 = (Field)field260Array[j];
                if(field260.getSubFields().length>0){
                    if(field260.getSubFieldData('b') != null && !field260.getSubFieldData('b').equals("")){
                        if(j!=0 && !pub.trim().equals("")) pub += " ; ";
                        pub += field260.getSubFieldData('b');
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return pub;
    }
    
    public String getPubYear(CatalogMaterialDescription cmd){
        String pubYear = "";
        try{
            Field[] field260Array = cmd.getFields("260");
            for(int j=0; j<field260Array.length; j++){
                Field field260 = (Field)field260Array[j];
                if(field260.getSubFields().length>0){
                    if(field260.getSubFieldData('c') != null && !field260.getSubFieldData('c').equals("")){
                        if(j!=0 && !pubYear.trim().equals("")) pubYear += " ; ";
                        pubYear += field260.getSubFieldData('c');
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return pubYear;
    }
    
    public String getRelatedNames(CatalogMaterialDescription cmd){
        String relatedNames = "";
        try{
            Field[] field700Array = cmd.getFields("700");
            for(int j=0; j<field700Array.length; j++){
                Field field700 = (Field)field700Array[j];
                String data880 = getParticular880Data("700", field700.getSubFieldData('6'), cmd);
                if(!relatedNames.trim().equals("")){
                    relatedNames += ";";
                    if(!data880.equals("")){
                        relatedNames += "<br>";
                    }
                    if(has880Data){
                        relatedNames += "<br>";
                        has880Data = false;
                    }
                }
                if(field700.getSubFieldData('a') != null)
                    relatedNames += field700.getSubFieldData('a');
                SubField[] lisuvs = field700.getSubFields();
                if(lisuvs!=null && lisuvs.length!=0){
                    String jtauth="";
                    for(int k=0;k<lisuvs.length;k++){
                        SubField sfsub = (SubField)lisuvs[k];
                        if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e' && sfsub.getDelimiter()!='6')
                            relatedNames += "-"+sfsub.getData();
                        else{
                            if(sfsub.getDelimiter()=='e')
                                jtauth += "-"+sfsub.getData();
                        }
                    }
                    if(jtauth==null || jtauth.trim().equals("")){
                        relatedNames += " jt. auth.";
                    } else{
                        relatedNames += "-"+jtauth;
                    }
                }
                if(!data880.equals("")){
                    relatedNames += "<br>"+data880;
                    has880Data = true;
                }
            }
            Field[] field710Array = cmd.getFields("710");
            for(int j=0; j<field710Array.length; j++){
                Field field710 = (Field)field710Array[j];
                String data880 = getParticular880Data("710", field710.getSubFieldData('6'), cmd);
                
                //System.out.println("field710data880: $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ "+data880);
                if(!relatedNames.trim().equals("")){
                    relatedNames += ";";
                    if(!data880.equals("")){
                        relatedNames += "<br>";
                    }
                    if(has880Data){
                        relatedNames += "<br>";
                        has880Data = false;
                    }
                }
                if(field710.getSubFieldData('a') != null)
                    relatedNames += field710.getSubFieldData('a');
                SubField[] lisuvs = field710.getSubFields();
                if(lisuvs!=null && lisuvs.length!=0){
                    String jtauth="";
                    for(int k=0;k<lisuvs.length;k++){
                        SubField sfsub = (SubField)lisuvs[k];
                        if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e' && sfsub.getDelimiter()!='6')
                            relatedNames += "-"+sfsub.getData();
                        else{
                            if(sfsub.getDelimiter()=='e')
                                jtauth += "-"+sfsub.getData();
                        }
                    }
                    if(jtauth==null || jtauth.trim().equals("")){
                        relatedNames += " jt. auth.";
                    } else{
                        relatedNames += "-"+jtauth;
                    }
                }
                if(!data880.equals("")){
                    relatedNames += "<br>"+data880;
                    has880Data = true;
                }
            }
            Field[] field711Array = cmd.getFields("711");
            for(int j=0; j<field711Array.length; j++){
                Field field711 = (Field)field711Array[j];
                String data880 = getParticular880Data("711", field711.getSubFieldData('6'), cmd);
                if(!relatedNames.trim().equals("")){
                    relatedNames += ";";
                    if(!data880.equals("")){
                        relatedNames += "<br>";
                    }
                    if(has880Data){
                        relatedNames += "<br>";
                        has880Data = false;
                    }
                }
                if(field711.getSubFieldData('a') != null)
                    relatedNames += field711.getSubFieldData('a');
                SubField[] lisuvs = field711.getSubFields();
                if(lisuvs!=null && lisuvs.length!=0){
                    String jtauth="";
                    for(int k=0;k<lisuvs.length;k++){
                        SubField sfsub = (SubField)lisuvs[k];
                        if(sfsub.getDelimiter()!='a' && sfsub.getDelimiter()!='e' && sfsub.getDelimiter()!='6')
                            relatedNames += "-"+sfsub.getData();
                        else{
                            if(sfsub.getDelimiter()=='e')
                                jtauth += "-"+sfsub.getData();
                        }
                    }
                    if(jtauth==null || jtauth.trim().equals("")){
                        relatedNames += " jt. auth.";
                    } else{
                        relatedNames += "-"+jtauth;
                    }
                }
                if(!data880.equals("")){
                    relatedNames += "<br>"+data880;
                    has880Data = true;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return relatedNames;
    }
    
    public String getRelatedTitles(CatalogMaterialDescription cmd){
        String relatedTitles = "";
        try{
            Field[] field740Array = cmd.getFields("740");
            for(int j=0; j<field740Array.length; j++){
                Field field740 = (Field)field740Array[j];
                String data880 = getParticular880Data("740", field740.getSubFieldData('6'), cmd);
                if(!relatedTitles.trim().equals("")){
                    relatedTitles += ";";
                    if(!data880.equals("")){
                        relatedTitles += "<br>";
                    }
                    if(has880Data){
                        relatedTitles += "<br>";
                        has880Data = false;
                    }
                }
                if(field740.getSubFieldData('a') != null)
                    relatedTitles += field740.getSubFieldData('a');
                if(!data880.equals("")){
                    relatedTitles += "<br>"+data880;
                    has880Data = true;
                }
            }
            Field[] field730Array = cmd.getFields("730");
            for(int j=0; j<field730Array.length; j++){
                Field field730 = (Field)field730Array[j];
                String data880 = getParticular880Data("730", field730.getSubFieldData('6'), cmd);
                if(!relatedTitles.trim().equals("")){
                    relatedTitles += ";";
                    if(!data880.equals("")){
                        relatedTitles += "<br>";
                    }
                    if(has880Data){
                        relatedTitles += "<br>";
                        has880Data = false;
                    }
                }
                if(field730.getSubFieldData('a') != null)
                    relatedTitles += field730.getSubFieldData('a');
                if(field730.getSubFieldData('f') != null)
                    relatedTitles += " : "+field730.getSubFieldData('f');
                if(!data880.equals("")){
                    relatedTitles += "<br>"+data880;
                    has880Data = true;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return relatedTitles;
    }
    
    public String getPhysicalDescription(CatalogMaterialDescription cmd){
        String physicalDescription = "";
        try{
            Field[] field300Array = cmd.getFields("300");
            for(int j=0; j<field300Array.length; j++){
                Field field300 = (Field)field300Array[j];
                if(!physicalDescription.equals("")) physicalDescription += " ; ";
                if(field300.getSubFieldData('a') != null)
                    physicalDescription += field300.getSubFieldData('a');
                if(field300.getSubFieldData('b') != null)
                    physicalDescription += " : "+field300.getSubFieldData('b');
                if(field300.getSubFieldData('c') != null)
                    physicalDescription += " ; "+field300.getSubFieldData('c');
                if(field300.getSubFieldData('6') != null){
                    String data880 = getParticular880Data("300", field300.getSubFieldData('6'), cmd);
                    if(!data880.equals("")) physicalDescription += "<br>"+data880;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return physicalDescription;
    }
    
    public String getSeriesAuthors(CatalogMaterialDescription cmd){
        String seriesAuthors = "";
        try{
            Field[] field800Array = cmd.getFields("800");
            for(int j=0; j<field800Array.length; j++){
                Field field800 = (Field)field800Array[j];
                String data880 = getParticular880Data("800", field800.getSubFieldData('6'), cmd);
                if(!seriesAuthors.trim().equals("")){
                    seriesAuthors += ";";
                    if(!data880.equals("")){
                        seriesAuthors += "<br>";
                    }
                    if(has880Data){
                        seriesAuthors += "<br>";
                        has880Data = false;
                    }
                }
                if(field800.getSubFieldData('a') != null)
                    seriesAuthors += field800.getSubFieldData('a');
                if(!data880.equals("")){
                    seriesAuthors += "<br>"+data880;
                    has880Data = true;
                }
            }
            Field[] field810Array = cmd.getFields("810");
            for(int j=0; j<field810Array.length; j++){
                Field field810 = (Field)field810Array[j];
                String data880 = getParticular880Data("810", field810.getSubFieldData('6'), cmd);
                if(!seriesAuthors.trim().equals("")){
                    seriesAuthors += ";";
                    if(!data880.equals("")){
                        seriesAuthors += "<br>";
                    }
                    if(has880Data){
                        seriesAuthors += "<br>";
                        has880Data = false;
                    }
                }
                if(field810.getSubFieldData('a') != null)
                    seriesAuthors += field810.getSubFieldData('a');
                if(!data880.equals("")){
                    seriesAuthors += "<br>"+data880;
                    has880Data = true;
                }
            }
            Field[] field811Array = cmd.getFields("811");
            for(int j=0; j<field811Array.length; j++){
                Field field811 = (Field)field811Array[j];
                String data880 = getParticular880Data("811", field811.getSubFieldData('6'), cmd);
                if(!seriesAuthors.trim().equals("")){
                    seriesAuthors += ";";
                    if(!data880.equals("")){
                        seriesAuthors += "<br>";
                    }
                    if(has880Data){
                        seriesAuthors += "<br>";
                        has880Data = false;
                    }
                }
                if(field811.getSubFieldData('a') != null)
                    seriesAuthors += field811.getSubFieldData('a');
                if(!data880.equals("")){
                    seriesAuthors += "<br>"+data880;
                    has880Data = true;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return seriesAuthors;
    }
    
    public String getSeriesTitle(CatalogMaterialDescription cmd){
        String seriesTitle = "";
        try{
            Field[] field440Array = cmd.getFields("440");
            for(int j=0; j<field440Array.length; j++){
                Field field440 = (Field)field440Array[j];
                String data880 = getParticular880Data("440", field440.getSubFieldData('6'), cmd);
                if(!seriesTitle.trim().equals("")){
                    seriesTitle += ";";
                    if(!data880.equals("")){
                        seriesTitle += "<br>";
                    }
                    if(has880Data){
                        seriesTitle += "<br>";
                        has880Data = false;
                    }
                }
                if(field440.getSubFieldData('a') != null)
                    seriesTitle += field440.getSubFieldData('a');
                if(field440.getSubFieldData('n') != null)
                    seriesTitle += " : "+field440.getSubFieldData('n');
                if(field440.getSubFieldData('p') != null)
                    seriesTitle += " : "+field440.getSubFieldData('p');
                if(field440.getSubFieldData('r') != null)
                    seriesTitle += " : "+field440.getSubFieldData('r');
                if(field440.getSubFieldData('x') != null)
                    seriesTitle += " , "+field440.getSubFieldData('x');
                if(!data880.equals("")){
                    seriesTitle += "<br>"+data880;
                    has880Data = true;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return seriesTitle;
    }
    
    public String getXXXNotes(CatalogMaterialDescription cmd, String tag){
        String notes = "";
        try{
            Field[] fieldArray = cmd.getFields(tag);
            for(int j=0; j<fieldArray.length; j++){
                Field field = (Field)fieldArray[j];
                if(field.getSubFields() != null && field.getSubFields().length>0){
                    String data880 = getParticular880Data(tag, field.getSubFieldData('6'), cmd);
                    if(!notes.equals("")){
                        notes += ";";
                        if(!data880.equals("")){
                            notes += "<br>";
                        }
                        if(has880Data){
                            notes += "<br>";
                            has880Data = false;
                        }
                    }
                    if(field.getSubFieldData('a') != null)
                        notes += field.getSubFieldData('a');
                    if(!data880.equals("")){
                        notes += "<br>"+data880;
                        has880Data = true;
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return notes;
    }
    
    public String getSubjects(CatalogMaterialDescription cmd){
        String subjects = "";
        try{
            Field[] fieldArray = cmd.getFields();
            for(int j=0; j<fieldArray.length; j++){
                Field field = (Field)fieldArray[j];
                if(field.getTag().startsWith("6")){
                    String data880 = getParticular880Data(field.getTag(), field.getSubFieldData('6'), cmd);
                    if(!subjects.trim().equals("")){
                        subjects += ";";
                        if(!data880.equals("")){
                            subjects += "<br>";
                        }
                        if(has880Data){
                            subjects += "<br>";
                            has880Data = false;
                        }
                    }
                    if(field.getSubFieldData('a') != null)
                        subjects += field.getSubFieldData('a');
                    SubField[] lisuvs = field.getSubFields('x');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subjects += "-"+sfsub.getData();
                        }
                    }
                    lisuvs = field.getSubFields('y');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subjects += "-"+sfsub.getData();
                        }
                    }
                    lisuvs = field.getSubFields('v');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subjects += "-"+sfsub.getData();
                        }
                    }
                    lisuvs = field.getSubFields('z');
                    if(lisuvs!=null && lisuvs.length!=0){
                        for(int k=0;k<lisuvs.length;k++){
                            SubField sfsub=(SubField)lisuvs[k];
                            subjects += "-"+sfsub.getData();
                        }
                    }
                    if(!data880.equals("")){
                        subjects += "<br>"+data880;
                        has880Data = true;
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return subjects;
    }
    
    public String getISBN(CatalogMaterialDescription cmd){
        return getFieldDataFormCMD(cmd, "020");
    }
    
    public String getISSN(CatalogMaterialDescription cmd){
        return getFieldDataFormCMD(cmd, "022");
    }
    
    public java.util.Hashtable getLinkingEntries(CatalogMaterialDescription cmd){
        java.util.Hashtable htLinkingEntries = new java.util.Hashtable();
        try{
            htLinkingEntries.put("MainSeriesEntry", getLinkingEntry(cmd, "760"));
            htLinkingEntries.put("SubSeriesEntry", getLinkingEntry(cmd, "762"));
            htLinkingEntries.put("OriginalLanguageEntry", getLinkingEntry(cmd, "765"));
            htLinkingEntries.put("TranslationEntry", getLinkingEntry(cmd, "767"));
            htLinkingEntries.put("SupplementOrSpecialIssueEntry", getLinkingEntry(cmd, "770"));
            htLinkingEntries.put("SupplementParentEntry", getLinkingEntry(cmd, "772"));
            htLinkingEntries.put("HostItemEntry", getLinkingEntry(cmd, "773"));
            htLinkingEntries.put("ConstituentUnitEntry", getLinkingEntry(cmd, "774"));
            htLinkingEntries.put("OtherEditionEntry", getLinkingEntry(cmd, "775"));
            htLinkingEntries.put("AdditionalPhysicalFormEntry", getLinkingEntry(cmd, "776"));
            htLinkingEntries.put("IssuedWithEntry", getLinkingEntry(cmd, "777"));
            htLinkingEntries.put("PrecedingEntry", getLinkingEntry(cmd, "780"));
            htLinkingEntries.put("SucceedingEntry", getLinkingEntry(cmd, "785"));
            htLinkingEntries.put("DataSourceEntry", getLinkingEntry(cmd, "786"));
            htLinkingEntries.put("NonspecificRelationshipEntry", getLinkingEntry(cmd, "787"));
        }catch(Exception e){e.printStackTrace();}
        return htLinkingEntries;
    }
    
    public String getLinkingEntry(CatalogMaterialDescription cmd, String tag){
        String linkingEntry = "";
        try{
            Field[] fieldArray = cmd.getFields(tag);
            if(fieldArray.length>0){
                for(int j=0; j<fieldArray.length; j++){
                    Field field = (Field)fieldArray[j];
                    String data880 = getParticularSubField880Data(tag, 't', field.getSubFieldData('6'), cmd);
                    if(!linkingEntry.trim().equals("")){
                        linkingEntry += ";";
                        if(!data880.equals("")){
                            linkingEntry += "<br>";
                        }
                        if(has880Data){
                            linkingEntry += "<br>";
                            has880Data = false;
                        }
                    }
                    if(field.getSubFieldData('t') != null)
                        linkingEntry += field.getSubFieldData('t');
                    if(field.getSubFieldData('g') != null)
                        linkingEntry += " LOCALE_DEPENDENT_IN "+field.getSubFieldData('g');
                    if(!data880.equals("")){
                        linkingEntry += "<br>"+data880;
                        has880Data = true;
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return linkingEntry;
    }
    
    public String getParticularSubField880Data(String fieldTag, char subFieldTag, String subField6Data, CatalogMaterialDescription cmd){
        String data = "";
        try{
            if(subField6Data != null && !subField6Data.trim().equals("")){
                subField6Data=subField6Data.substring(0,6);
                //System.out.println("subField6Data..."+subField6Data);
                StringTokenizer sTField$6 = new StringTokenizer(subField6Data, "-");
                sTField$6.nextToken();
                String field880$6 = fieldTag+"-"+sTField$6.nextToken();
                Field[] field880Array = cmd.getFields("880");
                for(int i=0; i<field880Array.length; i++){
                    Field field880 = (Field)field880Array[i];
                    String subfld880data=field880.getSubFieldData('6');
                    subfld880data=subfld880data.substring(0,6);
                    if(subfld880data.equals(field880$6))
                        data += field880.getSubFieldData(subFieldTag);
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return data;
    }
    
    public String getParticular880Data(String fieldTag, String subField6Data, CatalogMaterialDescription cmd){
        String data = "";
        try{
            if(subField6Data != null && !subField6Data.trim().equals("")){
                //System.out.println("%%%%%% subField6Data:"+subField6Data);
                subField6Data=subField6Data.substring(0,6);
                //System.out.println("%%%%%% subField6Data:"+subField6Data);
                StringTokenizer sTField$6 = new StringTokenizer(subField6Data, "-");
                sTField$6.nextToken();
                String field880$6 = fieldTag+"-"+sTField$6.nextToken();
                Field[] field880Array = cmd.getFields("880");
                for(int i=0; i<field880Array.length; i++){
                    Field field880 = (Field)field880Array[i];
                    String str=field880.getSubFieldData('6');
                    //System.out.println("%%%%%%880 $6 data:"+str);
                    str=str.substring(0,6);
                    //System.out.println("%%%%%%880 $6 data:"+str);
                    if(str.equals(field880$6)){
                        if(!data.trim().equals(""))
                            data += "<br>";
                        data += field880.getSubFieldData('a');
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return data;
    }
    
    public String verifyXML(String marcxml){
        String xml="";
        try{
            
            Document doc = (new SAXBuilder()).build(new StringReader(marcxml));
            xml=marcxml;
        }catch (Exception expx){
            char[] stchar = xml.toCharArray();
            for(int i=0;i<stchar.length;i++){
                if(stchar[i]=='&'){
                    if(stchar[i+1]=='#' && stchar[i+2]=='0' && stchar[i+3]==';'){
                        stchar[i]=' ';
                        stchar[i+1]=' ';
                        stchar[i+2]=' ';
                        stchar[i+3]=' ';
                        break;
                    }
                }
            }
            xml=new String(stchar);
        }
        return xml;
    }

    //Variables declaration
    private boolean has880Data = false;

   org.marc4j.marc.Record marc8ToUnicode(byte[] data) {

      org.marc4j.marc.Record marc4jRecord = null;
      System.out.println("GetMarcModelFromMarc");
      try {

         // Convert raw data in MARC-8 encoding to UNICODE UTF-8
         InputStream input = new ByteArrayInputStream(data);
         org.marc4j.MarcReader reader = new org.marc4j.MarcStreamReader(input);
         ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
         org.marc4j.MarcWriter writer = new org.marc4j.MarcStreamWriter(bos, "UTF8");

         org.marc4j.converter.impl.AnselToUnicode converter = new org.marc4j.converter.impl.AnselToUnicode();
         writer.setConverter(converter);

         while (reader.hasNext()) {

            org.marc4j.marc.Record record = reader.next();
            org.marc4j.marc.Leader leader = record.getLeader();
            leader.setCharCodingScheme('a');
            writer.write(record);

         }
         writer.close();
         byte[] rawRecord = bos.toByteArray();
         input = new ByteArrayInputStream(rawRecord);
         reader = new org.marc4j.MarcStreamReader(input);
        
         while (reader.hasNext()) {
            marc4jRecord = reader.next();
         }
         System.out.println("Converted record:\n" + marc4jRecord.toString());

//         java.util.ArrayList dataFields = new java.util.ArrayList();
//
//         if (marc4jRecord != null) {
//
//            java.util.List marc4jDataFields = marc4jRecord.getDataFields();
//            for (int i = 0; i < marc4jDataFields.size(); i++) {
//               org.marc4j.marc.DataField marc4jDataField =
//                       (org.marc4j.marc.DataField) marc4jDataFields.get(i);
//               java.util.List marc4jSubfields = marc4jDataField.getSubfields();
//               java.util.ArrayList subfields = new java.util.ArrayList();
//               org.unesco.jisis.z3950.Field dataField =
//                       new org.unesco.jisis.z3950.Field(marc4jDataField.getTag(),
//                       marc4jDataField.getIndicator1(),
//                       marc4jDataField.getIndicator2());
//               for (int j = 0; j < marc4jSubfields.size(); j++) {
//
//                  org.marc4j.marc.Subfield marc4jSubfield =
//                          (org.marc4j.marc.Subfield) marc4jSubfields.get(j);
//                  org.unesco.jisis.z3950.SubField subfield =
//                          new org.unesco.jisis.z3950.SubField(marc4jSubfield.getCode(), marc4jSubfield.getData());
//                  subfields.add(subfield);
//               }
//               dataField.addSubField(subfields);
//               dataFields.add(dataField);
//            }
//            String leaderString = null;
//            try {
//               leaderString = marc4jRecord.getLeader().marshal();
//            } catch (Exception expl) {
//               expl.printStackTrace();
//            }
//            org.unesco.jisis.z3950.Leader leader = new org.unesco.jisis.z3950.Leader(leaderString);
//            org.unesco.jisis.z3950.FixedFieldProcessor ffp = new org.unesco.jisis.z3950.FixedFieldProcessor();
//            ffp.startLeader(leader);
//
//            java.util.List marc4jControlFields = marc4jRecord.getControlFields();
//            java.util.ArrayList controlFields = new java.util.ArrayList();
//            for (int i = 0; i < marc4jControlFields.size(); i++) {
//               org.marc4j.marc.ControlField marc4jCtrlFld = (org.marc4j.marc.ControlField) marc4jControlFields.get(i);
//               org.unesco.jisis.z3950.ControlField cf = new org.unesco.jisis.z3950.ControlField();
//               cf.setTag(marc4jCtrlFld.getTag());
//               cf.setData(marc4jCtrlFld.getData());
//               controlFields.add(cf);
//               ffp.startControlField(cf.getTag(), cf.getData());
//            }
//            catMatDesc = new CatalogMaterialDescription();
//            catMatDesc.addControlField(controlFields);
//            catMatDesc.setFixedField(ffp.fxld);
//            catMatDesc.addField(dataFields);
//         }
      } catch (Exception exp) {
         System.out.println("EXECPTION IN CONVERTER" + exp.getMessage());
         exp.printStackTrace();
      }
      return marc4jRecord;


   }
   /**
    * Convert ISO2709 to
    * @param data
    * @return
    */
    org.marc4j.marc.Record unimarcEncodingToUnicode(byte[] data) {
      
      System.out.println("unimarcEncodingToUnicode");
      org.marc4j.marc.Record marc4jRecord = null;
      try {
         // Convert raw data in MARC-8 encoding to UNICODE UTF-8
         InputStream input = new ByteArrayInputStream(data);
         org.marc4j.MarcReader reader = new org.marc4j.MarcStreamReader(input);
         ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
         org.marc4j.MarcWriter writer = new org.marc4j.MarcStreamWriter(bos, "UTF8");

         org.marc4j.converter.impl.Iso5426ToUnicode converter = new org.marc4j.converter.impl.Iso5426ToUnicode();
         writer.setConverter(converter);
         while (reader.hasNext()) {
            org.marc4j.marc.Record record = reader.next();
            org.marc4j.marc.Leader leader = record.getLeader();
            // Repair missing leader position 9 value ("a" for MARC21 - UTF8).
            leader.setCharCodingScheme('a');
            writer.write(record);
         }
         writer.close();
         byte[] rawRecord = bos.toByteArray();
         input = new ByteArrayInputStream(rawRecord);
         reader = new org.marc4j.MarcStreamReader(input);
         
         while (reader.hasNext()) {
            marc4jRecord = reader.next();
         }
         System.out.println("Converted record:\n" + marc4jRecord.toString());


      } catch (Exception exp) {
         System.out.println("EXECPTION IN CONVERTER" + exp.getMessage());
         exp.printStackTrace();
      }
      return marc4jRecord;


   }

    org.marc4j.marc.Record convertToUnicode(byte[] data, org.marc4j.converter.CharConverter converter) {

      System.out.println("unimarcEncodingToUnicode");
      org.marc4j.marc.Record marc4jRecord = null;
      try {
         // Convert raw data in MARC-8 encoding to UNICODE UTF-8
         InputStream input = new ByteArrayInputStream(data);
         org.marc4j.MarcReader reader = new org.marc4j.MarcStreamReader(input);
         ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
         org.marc4j.MarcWriter writer = new org.marc4j.MarcStreamWriter(bos, "UTF8");


         writer.setConverter(converter);
         while (reader.hasNext()) {
            org.marc4j.marc.Record record = reader.next();
            org.marc4j.marc.Leader leader = record.getLeader();
            // Repair missing leader position 9 value ("a" for MARC21 - UTF8).
            leader.setCharCodingScheme('a');
            writer.write(record);
         }
         writer.close();
         byte[] rawRecord = bos.toByteArray();
         input = new ByteArrayInputStream(rawRecord);
         reader = new org.marc4j.MarcStreamReader(input);

         while (reader.hasNext()) {
            marc4jRecord = reader.next();
         }
         System.out.println("Converted record:\n" + marc4jRecord.toString());


      } catch (Exception exp) {
         System.out.println("EXECPTION IN CONVERTER" + exp.getMessage());
         exp.printStackTrace();
      }
      return marc4jRecord;


   }
    
}
