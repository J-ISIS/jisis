/*
 * Leader.java
 *
 * Created on August 3, 2002, 7:06 PM
 */

package org.unesco.jisis.z3950;

/**
 *
 * @author  Siddarth1
 */



public class Leader {
    
    private String availType = "acdefgijkmoprts"; // s added because, of cfcf.pdf d:\biju\biju\important\
    
    private String availBlvl = "abcdms";
    
    private String availRecStatus = "acdnp";
    
    /**
     * The record length. L/00-04*/
    private int recordLength;
    
    /**
     * The status. L/05*/
    private char recordStatus;
    
    /**
     * Type of record. L/06*/
    private char type;
    
    /**
     * bibliographic level. L/07*/
    private char blvl;
    
    /**
     * type of control.  L/08.*/
    private char ctrl;
    
    /**
     * Character coding scheme. L/09.*/
    private char charCodingScheme;
    
    /**
     * The indicator count. L/10.*/
    private char indicatorCount;
    
    /**
     * The subfield code length. L/11 */
    private char subfieldCodeLength;
    
    /**
     * The base address of data. L/12-16*/
    private int baseAddressOfData;
    
    /**
     * Implementation defined. L/17*/
    private char enclvl;
    
    /**
     * Descriptive Cataloging Form . L/18*/
    private char desc;
    
    /**
     * Link entry requirement L/19*/
    private char lnkReq;
    
    /**
     * Entry map */
    private char[] entryMap;
    
    
    java.text.DecimalFormat df = new java.text.DecimalFormat("00000");
    
    /**
     * default constructor
     * @throws MarcException */
    public Leader() throws MarcException{
        //good for nothing...
    }
    
    /**
     * constructor with string as argument
     * @param It takes string as argument
     * @throws MarcException */
    public Leader(String s) throws MarcException{
        this(s.toCharArray());
    }
    
    /**
     * constructor with character array as argument
     * @throws MarcException
     * @exception MarcException if it is invalid leader
     * @see MarcException class  */
    public Leader(char[] leader) throws MarcException{
        if(leader == null){
            throw new MarcException(" Invalid Leader ");
        }
        if(leader.length != 24 ){
            throw new MarcException("Invalid Leader - length != 24 ");
        }
        try{
            
            recordLength = Integer.parseInt(new String(leader,0,5));
            recordStatus = leader[5];
            type = leader[6];
            blvl = leader[7];
           // System.out.println("blvl is: "+blvl);
            ctrl = leader[8];
            charCodingScheme = leader[9];
            indicatorCount = leader[10];
            subfieldCodeLength = leader[11];
            baseAddressOfData = Integer.parseInt( new String(leader,12,5));
            enclvl = leader[17];
            desc = leader[18];
            lnkReq = leader[19];
            entryMap = new String(leader,20,4).toCharArray();
            
        }catch(Exception exp){
            throw new MarcException("MEx - "+exp.getMessage());
        }
        
    }
    
    /**
     * sets the record length
     * @param It takes record length as argument    */
    public void setRecordLength(int recordLength) {
        this.recordLength = recordLength;
    }
    
    /**
     *returns the record length
     *@return It returns record length  */
    public int getRecordLength() {
        return recordLength;
    }
    
    /**
     * sets the record status
     * @param It takes record status as argument  */
    public void setRecordStatus(char recordStatus) {
        this.recordStatus = recordStatus;
    }
    
    /**
     * returns the record status
     * @returns It returns record status */
    public char getRecordStatus() {
        return recordStatus;
    }
    
    /**
     * sets the type of record
     * @param It takes type of record as argument   */
    public void setTypeOfRecord(char typeOfRecord) {
        this.type = typeOfRecord;
    }
    
    /**
     * returns the type of record
     * @return It returns type of record  */
    public char getTypeOfRecord() {
        return type;
    }
    
    /**
     * sets the bibliographic level of record
     * @param It takes bibliographic level of record as argument  */
    public void setBlvl(char blvl){
        this.blvl = blvl;
    }
    
    /**
     * returns the bibliographic level of record
     * @return It returns bibliographic level of record */
    public char getBlvl(){
        return blvl;
    }
    
    /**
     * sets the Type of control of record
     * @param It takes type of control of record */
    public void setTypeOfCtrl(char ctrl){
        this.ctrl = ctrl;
    }
    
    /**
     * returns the type of control of record
     * @return It returns type of control of record */
    public char getTypeOfCtrl(){
        return ctrl;
    }
    
    /**
     * sets the character coding scheme of record
     * @param It takes character coding scheme of record as argument */
    public void setCharCodingScheme(char charCodingScheme) {
        this.charCodingScheme = charCodingScheme;
    }
    
    /**
     * returns the character coding scheme of record
     * @return It returns character coding scheme of record */
    public char getCharCodingScheme() {
        return charCodingScheme;
    }
    
    /**
     * sets the indicator count for the record
     * @param It takes indicator count as argument */
    public void setIndicatorCount(char indicatorCount) {
        this.indicatorCount = indicatorCount;
    }
    
    /**
     * returns the indicator count for the record
     * @return It returns indicator count of record */
    public char getIndicatorCount() {
        return indicatorCount;
    }
    
    /**
     * sets the subfield code length
     * @param It takes subfield code length as argument  */
    public void setSubfieldCodeLength(char subfieldCodeLength) {
        this.subfieldCodeLength = subfieldCodeLength;
    }
    
    /**
     * returns the subfield code length
     * @return It returns subfield code length  */
    public char getSubfieldCodeLength() {
        return subfieldCodeLength;
    }
    
    /**
     * returns the Base address of data
     * @return It returns base address of data */
    public int getBaseAddressOfData() {
        return baseAddressOfData;
    }
    
    /**
     * sets the Base address of data
     * @param It takes base address of data as argument  */
    public void setBaseAddressOfData(int baseAddressOfData) {
        this.baseAddressOfData = baseAddressOfData;
    }
    
    /**
     * sets the implementation defined
     * @param It takes implementation defined as argument  */
    public void setEncLvl(char enclvl){
        this.enclvl = enclvl;
    }
    
    /**
     * returns the implementation defined
     * @return It returns implementation defined  */
    public char getEncLvl(){
        return enclvl;
    }
    
    /**
     * sets the entry map
     * @param It takes entry map as argument */
    public void setEntryMap(char[] entryMap) {
        this.entryMap = entryMap;
    }
    
    /**
     * returns the entry map
     * @return It returns entry map */
    public char[] getEntryMap() {
        return entryMap;
    }
    
    public String toString() {
        return new StringBuffer()
        .append(df.format(recordLength).toString())
        .append(recordStatus)
        .append(type)
        .append(blvl)
        .append(ctrl)
        .append(charCodingScheme)
        .append(indicatorCount)
        .append(subfieldCodeLength)
        .append(df.format(baseAddressOfData).toString())
        .append(enclvl)
        .append(desc)
        .append(lnkReq)
        .append(entryMap)
        .toString();
    }
    
    /**
     * It validates the various fields in the leader
     * @return It returns true when it is valid leader else it returns false  */
    public boolean isValidLeader(){
        if("adcnp".indexOf(recordStatus) == -1)
            return false;
        if("acdefgijkmoprt".indexOf(type) == -1)
            return false;
        if("abcdms".indexOf(blvl) == -1)
            return false;
        if(" a".indexOf(ctrl) == -1)
            return false;
        if(" a".indexOf(charCodingScheme) == -1)
            return false;
        if(" 12345678uz".indexOf(enclvl) == -1)
            return false;
        if(" aiu".indexOf(desc) == -1)
            return false;
        if(" r".indexOf(lnkReq) == -1)
            return false;
        
        return true;
    }
    
    /**this is main method   	  */
    public static void main(String args[]){
        try{
            String s = ""+new Leader("01041eam  2200265 a 4500").isValidLeader();
            //System.out.println("01041cam  2200265 a 4500");
            //System.out.println(s);
        }catch(MarcException me){
            //System.out.println("me "+me);
        }
    }
    
    public char getDesc() {
        return this.desc;
    }
    public void setDesc(char desc){
        this.desc = desc;
    }
}