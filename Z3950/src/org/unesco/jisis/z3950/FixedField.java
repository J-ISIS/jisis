/*
 * FixedField.java
 *
 * Created on August 3, 2002, 7:01 PM
 */
package org.unesco.jisis.z3950;

/**
 *
 * @author  Siddarth1
 */
import java.util.Properties;

public class FixedField implements java.io.Serializable {

   private String leader = "";
   public char alph = ' ';
   public char audn = ' ';
   public char biog = ' ';
   public char blvl = ' ';
   public char conf = ' ';
   public String cont = "";
   public char ctrl = ' ';
   public String ctry = "";
   public char dtst = ' ';
   public String dates = "";
   public char dsc = ' ';
   public char elvl = ' ';
   public char entw = ' ';
   public char fest = ' ';
   public char file = ' ';
   public char form = ' ';
   public char freq = ' ';
   public char gpub = ' ';
   public String ills = "";
   public char issn = ' ';
   public String lang = "";
   public char litf = ' ';
   public char orig = ' ';
   public String proj = "";
   public char regl = ' ';
   public char srtp = ' ';
   public char s_l = ' ';
   public char spform = ' ';
   public char srce = ' ';
   public char tech = ' ';
   public String time = "";
   public char tmat = ' ';
   public char type = ' ';
   public String accm = "";
   public String comp = "";
   public String ltxt = "";
   public char fmus = ' ';

   public FixedField() {
   }

   public FixedField(Properties prop) {
      String temp;
      temp = prop.getProperty("alph");
      if (temp != null) {
         alph = temp.toCharArray()[0];
      }
      temp = prop.getProperty("audn");
      if (temp != null) {
         audn = temp.toCharArray()[0];
      }
      temp = prop.getProperty("biog");
      if (temp != null) {
         biog = temp.toCharArray()[0];
      }
      temp = prop.getProperty("blvl");
      if (temp != null) {
         blvl = temp.toCharArray()[0];
      }
      temp = prop.getProperty("conf");
      if (temp != null) {
         conf = temp.toCharArray()[0];
      }
      temp = prop.getProperty("cont");
      if (temp != null) {
         cont = temp;
      }
      temp = prop.getProperty("ctrl");
      if (temp != null) {
         ctrl = temp.toCharArray()[0];
      }
      temp = prop.getProperty("ctry");
      if (temp != null) {
         ctry = temp;
      }
      temp = prop.getProperty("dtst");
      if (temp != null) {
         dtst = temp.toCharArray()[0];
      }
      temp = prop.getProperty("dates");
      if (temp != null) {
         dates = temp;
      }
      temp = prop.getProperty("dsc");
      if (temp != null) {
         dsc = temp.toCharArray()[0];
      }
      temp = prop.getProperty("elvl");
      if (temp != null) {
         elvl = temp.toCharArray()[0];
      }
      temp = prop.getProperty("entw");
      if (temp != null) {
         entw = temp.toCharArray()[0];
      }
      temp = prop.getProperty("fest");
      if (temp != null) {
         fest = temp.toCharArray()[0];
      }
      temp = prop.getProperty("file");
      if (temp != null) {
         file = temp.toCharArray()[0];
      }
      temp = prop.getProperty("form");
      if (temp != null) {
         form = temp.toCharArray()[0];
      }
      temp = prop.getProperty("freq");
      if (temp != null) {
         freq = temp.toCharArray()[0];
      }
      temp = prop.getProperty("gpub");
      if (temp != null) {
         gpub = temp.toCharArray()[0];
      }
      temp = prop.getProperty("ills");
      if (temp != null) {
         ills = temp;
      }
      temp = prop.getProperty("issn");
      if (temp != null) {
         issn = temp.toCharArray()[0];
      }
      temp = prop.getProperty("lang");
      if (temp != null) {
         lang = temp;
      }
      temp = prop.getProperty("litf");
      if (temp != null) {
         litf = temp.toCharArray()[0];
      }
      temp = prop.getProperty("orig");
      if (temp != null) {
         orig = temp.toCharArray()[0];
      }
      temp = prop.getProperty("proj");
      if (temp != null) {
         proj = temp;
      }
      temp = prop.getProperty("regl");
      if (temp != null) {
         regl = temp.toCharArray()[0];
      }
      temp = prop.getProperty("srtp");
      if (temp != null) {
         srtp = temp.toCharArray()[0];
      }
      temp = prop.getProperty("s_l");
      if (temp != null) {
         s_l = temp.toCharArray()[0];
      }
      temp = prop.getProperty("spform");
      if (temp != null) {
         spform = temp.toCharArray()[0];
      }
      temp = prop.getProperty("srce");
      if (temp != null) {
         srce = temp.toCharArray()[0];
      }
      temp = prop.getProperty("tech");
      if (temp != null) {
         tech = temp.toCharArray()[0];
      }
      temp = prop.getProperty("time");
      if (temp != null) {
         time = temp;
      }
      temp = prop.getProperty("tmat");
      if (temp != null) {
         tmat = temp.toCharArray()[0];
      }
      temp = prop.getProperty("type");
      if (temp != null) {
         type = temp.toCharArray()[0];
      }


   }

   public void setAlph(char alph) {
      this.alph = alph;
   }

   public char getAlph() {
      return alph;
   }

   public void setAudn(char audn) {
      this.audn = audn;
   }

   public char getAudn() {
      return audn;
   }

   public void setBiog(char biog) {
      this.biog = biog;
   }

   public char getBiog() {
      return biog;
   }

   public void setBlvl(char blvl) {
      this.blvl = blvl;
   }

   public char getBlvl() {
      return blvl;
   }

   public void setConf(char conf) {
      this.conf = conf;
   }

   public char getConf() {
      return conf;
   }

   public void setCont(String cont) {
      this.cont = cont;
   }

   public String getCont() {
      return cont;
   }

   public void setCtrl(char ctrl) {
      this.ctrl = ctrl;
   }

   public char getCtrl() {
      return ctrl;
   }

   public void setCtry(String ctry) {
      this.ctry = ctry;
   }

   public String getCtry() {
      return ctry;
   }

   public void setDtst(char dtst) {
      this.dtst = dtst;
   }

   public char getDtst() {
      return dtst;
   }

   public void setDates(String dates) {
      this.dates = dates;
   }

   public String getDates() {
      return dates;
   }

   public void setDsc(char dsc) {
      this.dsc = dsc;
   }

   public char getDsc() {
      return dsc;
   }

   public void setElvl(char elvl) {
      this.elvl = elvl;
   }

   public char getElvl() {
      return elvl;
   }

   public void setEntw(char entw) {
      this.entw = entw;
   }

   public char getEntw() {
      return entw;
   }

   public void setFest(char fest) {
      this.fest = fest;
   }

   public char getFest() {
      return fest;
   }

   public void setFile(char file) {
      this.file = file;
   }

   public char getFile() {
      return file;
   }

   public void setForm(char form) {
      this.form = form;
   }

   public char getForm() {
      return form;
   }

   public void setFreq(char freq) {
      this.freq = freq;
   }

   public char getFreq() {
      return freq;
   }

   public void setGpub(char gpub) {
      this.gpub = gpub;
   }

   public char getGpub() {
      return gpub;
   }

   public void setIlls(String ills) {
      this.ills = ills;
   }

   public String getIlls() {
      return ills;
   }

   public void setIssn(char issn) {
      this.issn = issn;
   }

   public char getIssn() {
      return issn;
   }

   public void setLang(String lang) {
      this.lang = lang;
   }

   public String getLang() {
      return lang;
   }

   public void setLitf(char litf) {
      this.litf = litf;
   }

   public char getLitf() {
      return litf;
   }

   public void setOrig(char orig) {
      this.orig = orig;
   }

   public char getOrig() {
      return orig;
   }

   public void setProj(String proj) {
      this.proj = proj;
   }

   public String getProj() {
      return proj;
   }

   public void setRegl(char regl) {
      this.regl = regl;
   }

   public char getRegl() {
      return regl;
   }

   public void setSrtp(char srtp) {
      this.srtp = srtp;
   }

   public char getSrtp() {
      return srtp;
   }

   public void setS_L(char s_l) {
      this.s_l = s_l;
   }

   public char getS_L() {
      return s_l;
   }

   public void setSpform(char spform) {
      this.spform = spform;
   }

   public char getSpform() {
      return spform;
   }

   public void setSrce(char srce) {
      this.srce = srce;
   }

   public char getSrce() {
      return srce;
   }

   public void setTech(char tech) {
      this.tech = tech;
   }

   public char getTech() {
      return tech;
   }

   public void setTime(String time) {
      this.time = time;
   }

   public String getTime() {
      return time;
   }

   public void setTmat(char tmat) {
      this.tmat = tmat;
   }

   public char getTmat() {
      return tmat;
   }

   public void setType(char type) {
      this.type = type;
   }

   public char getType() {
      return type;
   }

   /** Getter for property accm.
    * @return Value of property accm.
    */
   public java.lang.String getAccm() {
      return accm;
   }

   /** Setter for property accm.
    * @param accm New value of property accm.
    */
   public void setAccm(java.lang.String accm) {
      this.accm = accm;
   }

   /** Getter for property comp.
    * @return Value of property comp.
    */
   public java.lang.String getComp() {
      return comp;
   }

   /** Setter for property comp.
    * @param comp New value of property comp.
    */
   public void setComp(java.lang.String comp) {
      this.comp = comp;
   }

   /** Getter for property ltxt.
    * @return Value of property ltxt.
    */
   public java.lang.String getLtxt() {
      return ltxt;
   }

   /** Setter for property ltxt.
    * @param ltxt New value of property ltxt.
    */
   public void setLtxt(java.lang.String ltxt) {
      this.ltxt = ltxt;
   }

   /** Getter for property fmus.
    * @return Value of property fmus.
    */
   public char getFmus() {
      return fmus;
   }

   /** Setter for property fmus.
    * @param fmus New value of property fmus.
    */
   public void setFmus(char fmus) {
      this.fmus = fmus;
   }

   public String getLeader() {
      return leader;
   }

   public void setLeader(String leader) {
      this.leader = leader;
   }
}
