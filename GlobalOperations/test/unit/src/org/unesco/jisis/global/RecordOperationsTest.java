/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.global;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.record.Field;
import org.unesco.jisis.corelib.record.FieldFactory;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.Record;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.ADD_ONLY_IF_NOT_PRESENT;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.DELETE_FIELD_OCCURRENCE_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.DELETE_FIELD_SUBFIELD_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.DELETE_FIELD_TAG_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.FIELD_OCCURRENCE_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.FIELD_TAG_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.INSERT_BEFORE_POSITION_FLAG;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.INSERT_BEFORE_POSITION_VALUE;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.TEXT_TO_ADD;
import static org.unesco.jisis.global.RecordOperations.toHex;

/**
 *
 * @author jcd
 * 
 * Tag	Contents
 * 24	<An> Electric hygrometer apparatus for measuring water-vapour loss from plants in the field
 * 26	^aParis^bUnesco^cl965
 * 30	^ap. 247-257^billus.
 * 44	Methodology of plant eco-physiology: proceedings of the Montpellier Symposium
 * 50	Incl. Bibl.
 * 69	Paper On: <hygrometers><plant transpiration><moisture><water balance>
 * 70	Grieve, B.J.
 * 70	Went, F.W.

 */
public class RecordOperationsTest {
    
    static Record record_;
    
    public RecordOperationsTest() {
    }
    /**
     * 
     * @return 
     */
    private static Record buildTestRecord() {
         Record record = (Record) Record.createRecord();
        
       
        try {
            Field field24 = (Field) FieldFactory.makeField(24);
            field24.setFieldValue("<An> Electric hygrometer apparatus for measuring water-vapour loss from plants in the field");
            record.addField(field24);
            Field field26 = (Field) FieldFactory.makeField(26);
            field26.setFieldValue("^aParis^bUnesco^cl965");
            record.addField(field26);
              Field field30 = (Field) FieldFactory.makeField(30);
            field30.setFieldValue("^ap. 247-257^billus.");
            record.addField(field30);
              Field field44 = (Field) FieldFactory.makeField(44);
            field44.setFieldValue("Methodology of plant eco-physiology: proceedings of the Montpellier Symposium");
            record.addField(field44);
              Field field50 = (Field) FieldFactory.makeField(50);
            field50.setFieldValue("Incl. Bibl.");
            record.addField(field50);
            
              Field field69 = (Field) FieldFactory.makeField(69);
            field69.setFieldValue("Paper On: <hygrometers><plant transpiration><moisture><water balance>");
             record.addField(field69);
             
               Field field70 = (Field) FieldFactory.makeField(70);
            field70.setFieldValue("Grieve, B.J.%Went, F.W");
             record.addField(field70);
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        return record;
    }
    @BeforeClass
    public static void setUpClass() {
        record_ =  buildTestRecord();
        
       
       
        
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of addToRecord method, of class RecordOperations.
     */
    /**
     * Add Operation - Add field with a specified contents to a record (The parameters specify details on how to process)
     *
     * @param record - The record object
     * @param parameters - The parameters defining the rules
     * 
     * 1. InsertBeforePosition chekbox not cheked
     * --------------------------------------- 
     *  1.1 If the givenfield fieldTag does not exist, it is automatically created with the new text. 
     *  Note: The field will be created even if it is not in the FDT.
     * 
     *  1.2 If the given field fieldTag is already present and the option “add only if not present” is not checked:
     *  The new text will replace the full field value. It can contain subfieldTag delimiters as well as
     *  occurrences separated by the occurrence separator
     * 
     *  2. InsertBeforePosition chekbox cheked 
     *  ------------------------------------ 
     *  Selecting this checkbox it is possible to specify where in the existing field the new text should be 
     *  added. Moreover, it is  possible to specify which occurrence number should be taken into account.
     * 
     *  2.1 if the given field fieldTag does not exist, the field is automatically created with the new text
     *  (position and occurrence number are ignored). Note: The field will be created even if it is not in the
     *  FDT.
     * 
     *  2.2 If the given field fieldTag is already present, and the option “add only if not present” is not checked,
     *  the new text is added as follow:
     * 
     * 2.2.1 How the new text is inserted in an existing field:
     * --------------------------------------------------------
     *  The default position value is 1, which means beginning of field/occurrence
     * 
     *  In case the field/occurrence text length is greater than position, the new text is inserted in the
     *  specified position. Otherwise it is appended at the end of the field/occurrence.
     * 
     *  2.2.2 Occurrence Number is zero: 
     * ---------------------------------
     *  A not repeatable field is supposed to have a single occurrence. Therefore all occurrences will be
     *  treated as described in 2.2.1
     * 
     *  2.2.3 Occurrence Number >0: 
     * ----------------------------
     * a) If an occurrence number is specified (greater than zero), only the specified occurrence is treated 
     *     as described in 2.2.1.
     *
     * b) If the field has not enough occurrences, one new occurrence is created.
     *
     *
     *
     */
    @Test
    public void testAddToRecord() {
        System.out.println("addToRecord");
        testAddToRecordInsertBeforePosition(false);
        testAddToRecordInsertBeforePosition(true);
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(TEXT_TO_ADD, "New Occurrence 1");
        parameters.put(FIELD_TAG_KEY, 200);

        parameters.put(ADD_ONLY_IF_NOT_PRESENT, 0); // 1 or 0       
        parameters.put(INSERT_BEFORE_POSITION_FLAG, 1);     
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 1);
        parameters.put(FIELD_OCCURRENCE_KEY, 1);
      
        record_ = buildTestRecord();
              
        Record result = RecordOperations.addToRecord(record_, parameters);
        String value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add Non existing occurrence");      
        System.out.println("**INSERT BEFORE POSITION CHECKED");    
        System.out.println("*****************************************************");
        
        assertEquals(value, "New Occurrence 1");
        System.out.println(result.toString());
        
        /*========================================================================*/
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 5);
        parameters.put(FIELD_OCCURRENCE_KEY, 5);
        
        record_ = buildTestRecord();
         
        result = RecordOperations.addToRecord(record_, parameters);
        value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add Non existing occurrence pos=5 occ=5");
        System.out.println("**INSERT BEFORE POSITION CHECKED");
        System.out.println("*****************************************************");
        Record save = (Record) Record.newInstance(result);
        assertEquals(value, "New Occurrence 1");
        System.out.println(result.toString());
       
        /*====================================================================*/
        parameters.put(TEXT_TO_ADD, "^a");
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 1);
        parameters.put(FIELD_OCCURRENCE_KEY, 0);
        
        result = RecordOperations.addToRecord(result, parameters);
        value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add - insert '^a' pos=1 occ=0");
        System.out.println("**INSERT BEFORE POSITION CHECKED");
        System.out.println("*****************************************************");
        
        assertEquals(value, "^aNew Occurrence 1");
        System.out.println(result.toString());
        
        /*====================================================================*/
        parameters.put(TEXT_TO_ADD, "^a");
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 1);
        parameters.put(FIELD_OCCURRENCE_KEY, 1);
        
        result = (Record) Record.newInstance(save);
        result = RecordOperations.addToRecord(result, parameters);
        value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add - insert '^a' pos=1 occ=1");
        System.out.println("**INSERT BEFORE POSITION CHECKED");
        System.out.println("*****************************************************");
       
        assertEquals(value, "^aNew Occurrence 1");
        System.out.println(result.toString());
        
         /*====================================================================*/
        parameters.put(TEXT_TO_ADD, "^a");
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 1);
        parameters.put(FIELD_OCCURRENCE_KEY, 100);
        
        result = (Record) Record.newInstance(save);
        result = RecordOperations.addToRecord(result, parameters);
        value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add - insert '^a' pos=1 occ=100");
         System.out.println("*** '^a' occ doesn't exists then is added as a new occurrence "); 
        System.out.println("**INSERT BEFORE POSITION CHECKED");

        System.out.println("*****************************************************");
         assertEquals(value, "New Occurrence 1%^a");
        System.out.println(result.toString());
        
       /*====================================================================*/
        parameters.put(TEXT_TO_ADD, "AAAA ");
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 5);
        parameters.put(FIELD_OCCURRENCE_KEY, 0);
        
        result = (Record) Record.newInstance(save);
        result = RecordOperations.addToRecord(result, parameters);
        value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add - insert 'AAAA ' pos=5 occ=0");
        System.out.println("*** 'AAAA ' inserted at pos=5 \"New AAAA Occurrence 1\"");
        System.out.println("**INSERT BEFORE POSITION CHECKED");

        System.out.println("*****************************************************");
        assertEquals(value, "New AAAA Occurrence 1");
        System.out.println(result.toString());
        
        /*====================================================================*/
        parameters.put(TEXT_TO_ADD, "AAAA ");
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 17);
        parameters.put(FIELD_OCCURRENCE_KEY, 0);
        
        result = (Record) Record.newInstance(save);
        result = RecordOperations.addToRecord(result, parameters);
        value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add - insert 'AAAA ' pos=17 occ=0");
        System.out.println("*** 'AAAA ' inserted at pos=17 \"New Occurrence 1AAAA \"");
        System.out.println("**INSERT BEFORE POSITION CHECKED");

        System.out.println("*****************************************************");
        assertEquals(value, "New Occurrence 1AAAA ");
        System.out.println(result.toString());
        
           /*====================================================================*/
        parameters.put(TEXT_TO_ADD, "AAAA ");
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 100);
        parameters.put(FIELD_OCCURRENCE_KEY, 0);
        
        result = (Record) Record.newInstance(save);
        result = RecordOperations.addToRecord(result, parameters);
        value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add - insert 'AAAA ' pos=100 occ=0");
        System.out.println("*** 'AAAA ' inserted at pos=100 \"New Occurrence 1AAAA \"");
        System.out.println("**INSERT BEFORE POSITION CHECKED");

        System.out.println("*****************************************************");
        assertEquals(value, "New Occurrence 1AAAA ");
        System.out.println(result.toString());

    }
    
     public void testAddToRecordInsertBeforePosition(boolean InsertBeforePositionChecked) {
          Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(TEXT_TO_ADD, "PLAIN TEXT TO BE ADDED");
        parameters.put(FIELD_TAG_KEY, 200);

        parameters.put(ADD_ONLY_IF_NOT_PRESENT, 0); // 1 or 0

        if (InsertBeforePositionChecked) {
             parameters.put(INSERT_BEFORE_POSITION_FLAG, 1);
        } else {
             parameters.put(INSERT_BEFORE_POSITION_FLAG, 0);
        }
        parameters.put(INSERT_BEFORE_POSITION_VALUE, 0);
        parameters.put(FIELD_OCCURRENCE_KEY, 0);

        System.out.println("addToRecord");
      
        record_ = buildTestRecord();
       
        
        Record result = RecordOperations.addToRecord(record_, parameters);
        String value = null;
        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add Non existing Field with plain text");
        if (InsertBeforePositionChecked) {
             System.out.println("**INSERT BEFORE POSITION CHECKED");
        }
        System.out.println("*****************************************************");
        assertEquals(value, "PLAIN TEXT TO BE ADDED");
        System.out.println(result.toString());
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Add Only If Not Present checked");
        if (InsertBeforePositionChecked) {
             System.out.println("**INSERT BEFORE POSITION CHECKED");
        }
        System.out.println("*****************************************************");
        parameters.put(TEXT_TO_ADD, "PLAIN TEXT TO BE ADDED ONLY IF FIELLD 200 NOT PRESENT");
        parameters.put(ADD_ONLY_IF_NOT_PRESENT, 1);
        result = RecordOperations.addToRecord(result, parameters);

        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertEquals(value, "PLAIN TEXT TO BE ADDED");
        System.out.println(result.toString());
        
        System.out.println("***************************************************************");
        System.out.println("***Test Global Add Only If Not Present NOT checked - PLAIN TEXT");
        if (InsertBeforePositionChecked) {
             System.out.println("**INSERT BEFORE POSITION CHECKED");
        }
        System.out.println("***************************************************************");
        System.out.println("***If the given field fieldTag is already present and the option “add only if not present” is not checked:");
        System.out.println("***The new text will replace the full field value. It can contain subfieldTag delimiters as well as");
        System.out.println("***occurrences separated by the occurrence separator");
        parameters.put(TEXT_TO_ADD, "NEW TEXT TO BE ADDED - WILL REPLACE CONTENT IF FIELD 200 PRESENT");
        parameters.put(ADD_ONLY_IF_NOT_PRESENT, 0);
        result = RecordOperations.addToRecord(result, parameters);

        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertEquals(value, "NEW TEXT TO BE ADDED - WILL REPLACE CONTENT IF FIELD 200 PRESENT");
        System.out.println(result.toString());
        
         System.out.println("***************************************************************************");
        System.out.println("***Test Global Add Only If Not Present NOT checked - OCCURRENCES + SUBFIELDS");
        if (InsertBeforePositionChecked) {
             System.out.println("**INSERT BEFORE POSITION CHECKED");
        }
        System.out.println("****************************************************************************");
        System.out.println("***If the given field fieldTag is already present and the option “add only if not present” is not checked:");
        System.out.println("***The new text will replace the full field value. It can contain subfieldTag delimiters as well as");
        System.out.println("***occurrences separated by the occurrence separator");
        parameters.put(TEXT_TO_ADD, "^aoccurrence 1 subfield a^boccurrence 1 subfield b%^aoccurrence 2 subfield a^boccurrence 2 subfield b");
        parameters.put(ADD_ONLY_IF_NOT_PRESENT, 0);
        result = RecordOperations.addToRecord(result, parameters);

        try {
            value = result.getField(200).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertEquals(value, "^aoccurrence 1 subfield a^boccurrence 1 subfield b%^aoccurrence 2 subfield a^boccurrence 2 subfield b");
        System.out.println(result.toString());
        
     }

    /**
     * Test of deleteFromRecord method, of class RecordOperations.
     * 
     *   * Delete Operation - Delete data from the record (The parameters specify details on how to process)
     * 
     *  Parameters:
     *  ----------
     * 
     * Field Tag - fieldTag of the field to be deleted.
     * 
     * Subfield - Specify a subfield fieldTag identifier (one character) in order to limit the scope to that given 
     *             subfield fieldTag.
     * 
     * Occurrence - If greater than zero, only the specified occurrence is treated. If set to zero, 
     *               all occurrences will be treated.
     *
     */
    @Test
    public void testDeleteFromRecord() {
        Map<String, Object> parameters = new HashMap<String, Object>();
       
        System.out.println("deleteFromRecord");
        /**
         *     26	^aParis^bUnesco^cl965
         *     30	^ap. 247-257^billus.
         */
    
      
        Record record = buildTestRecord();
        
        parameters.put(DELETE_FIELD_TAG_KEY, 26);

        parameters.put(DELETE_FIELD_SUBFIELD_KEY, "a");
      
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 0);
        
        Record result = RecordOperations.deleteFromRecord(record, parameters);
        
        String value = null;
        try {
            value = result.getField((Integer) parameters.get(DELETE_FIELD_TAG_KEY)).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Field 26");
        System.out.println("**delete subfield 'a' from ^aParis^bUnesco^cl965");
        System.out.println("*****************************************************");
 
        assertEquals(value, "^bUnesco^cl965");
        System.out.println(result.toString());
              
        /*===========================================================================*/
        
        record = buildTestRecord();
        
        parameters.put(DELETE_FIELD_TAG_KEY, 26);
        parameters.put(DELETE_FIELD_SUBFIELD_KEY, "b");
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 0);
        
        result = RecordOperations.deleteFromRecord(record, parameters);
        
        try {
            value = result.getField((Integer) parameters.get(DELETE_FIELD_TAG_KEY)).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Field 26");
        System.out.println("**delete subfield 'b' from ^aParis^bUnesco^cl965");
        System.out.println("*****************************************************");
 
        assertEquals(value, "^aParis^cl965");
        System.out.println(result.toString());
       
        /*===========================================================================*/
        
        record = buildTestRecord();
        
        parameters.put(DELETE_FIELD_TAG_KEY, 26);
        parameters.put(DELETE_FIELD_SUBFIELD_KEY, "c");
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 0);
        
        result = RecordOperations.deleteFromRecord(record, parameters);
        
        try {
            value = result.getField((Integer) parameters.get(DELETE_FIELD_TAG_KEY)).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Field 26");
        System.out.println("**delete subfield 'c' from ^aParis^bUnesco^cl965");
        System.out.println("*****************************************************");
 
        assertEquals(value, "^aParis^bUnesco");
        System.out.println(result.toString());
        
         /*===========================================================================*/
        
        record = buildTestRecord();
        
        parameters.put(DELETE_FIELD_TAG_KEY, 70);
        parameters.put(DELETE_FIELD_SUBFIELD_KEY, null);
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 0);
        
        result = RecordOperations.deleteFromRecord(record, parameters);
        
        try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = result.findField((Integer) parameters.get(DELETE_FIELD_TAG_KEY));
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Delete Whole Field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W'");
        System.out.println("*****************************************************");
 
        assertEquals(value, null);
        System.out.println(result.toString());
        
        
        
          /*===========================================================================*/
        
        record = buildTestRecord();
        
        parameters.put(DELETE_FIELD_TAG_KEY, 70);
        parameters.put(DELETE_FIELD_SUBFIELD_KEY, null);
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 1);
        
        result = RecordOperations.deleteFromRecord(record, parameters);
        
        try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = result.findField((Integer) parameters.get(DELETE_FIELD_TAG_KEY));
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Delete 1st Occurence Field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "Went, F.W");
        System.out.println(result.toString());
        
         /*===========================================================================*/
        
        record = buildTestRecord();
        
        parameters.put(DELETE_FIELD_TAG_KEY, 70);
        parameters.put(DELETE_FIELD_SUBFIELD_KEY, null);
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 2);
        
        result = RecordOperations.deleteFromRecord(record, parameters);
        
        try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = result.findField((Integer) parameters.get(DELETE_FIELD_TAG_KEY));
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Delete 2nd Occurrence Field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "Grieve, B.J.");
        System.out.println(result.toString());
        
        /*===========================================================================*/
        /**
         * 
         */
        Map<String, Object> addParameters = new HashMap<String, Object>();
        addParameters.put(TEXT_TO_ADD,"^aSubfield a occ 3^bsubfield b occ 3^csubfield c occ 3");
        addParameters.put(FIELD_TAG_KEY, 70);

        addParameters.put(ADD_ONLY_IF_NOT_PRESENT, 0); // 1 or 0       
        addParameters.put(INSERT_BEFORE_POSITION_FLAG, 1);     
        addParameters.put(INSERT_BEFORE_POSITION_VALUE, 1);
        addParameters.put(FIELD_OCCURRENCE_KEY, 3);
      
        record_ = buildTestRecord();
                     
        Record result1 = RecordOperations.addToRecord(record_, addParameters);
        
        Record saveRecord =(Record) Record.newInstance(record_);
        value = null;
        try {
            value = result1.getField(70).getStringFieldValue();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        System.out.println("*****************************************************");
        System.out.println("***Test Add occurence with subfields to field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W^aSubfield a occ 3^bsubfield b occ 3^csubfield c occ 3'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "Grieve, B.J.%Went, F.W%^aSubfield a occ 3^bsubfield b occ 3^csubfield c occ 3");
        System.out.println(result1.toString());
        
        parameters.put(DELETE_FIELD_TAG_KEY, 70);
        parameters.put(DELETE_FIELD_SUBFIELD_KEY, "a");
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 3);
        
        result = RecordOperations.deleteFromRecord(result1, parameters);
        
        try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = result.findField((Integer) parameters.get(DELETE_FIELD_TAG_KEY));
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Delete subfield a from 3rd Occurrence Field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W^bsubfield b occ 3^csubfield c occ 3'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "Grieve, B.J.%Went, F.W%^bsubfield b occ 3^csubfield c occ 3");
        System.out.println(result.toString());
        
        /*===============================================================================*/
//        result1 = RecordOperations.addToRecord(record_, addParameters);
//        value = null;
//        try {
//            value = result1.getField(70).getStringFieldValue();
//        } catch (DbException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        System.out.println("*****************************************************");
//        System.out.println("***Test Add occurence with subfields to field 70");
//        System.out.println("** 'Grieve, B.J.%Went, F.W^aSubfield a occ 3^bsubfield b occ 3^csubfield c occ 3'");
//        System.out.println("*****************************************************");
// 
//        assertEquals(value, "Grieve, B.J.%Went, F.W%^aSubfield a occ 3^bsubfield b occ 3^csubfield c occ 3");
//        System.out.println(result1.toString());
//        
        parameters.put(DELETE_FIELD_TAG_KEY, 70);
        parameters.put(DELETE_FIELD_SUBFIELD_KEY, "b");
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 3);
        
        result1 = (Record) Record.newInstance(saveRecord);
        
        result = RecordOperations.deleteFromRecord(result1, parameters);
        
        try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = result.findField((Integer) parameters.get(DELETE_FIELD_TAG_KEY));
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Delete subfield b from 3rd Occurrence Field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W^bsubfield b occ 3^csubfield c occ 3'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "Grieve, B.J.%Went, F.W%^aSubfield a occ 3^csubfield c occ 3");
        System.out.println(result.toString());
        
        /*===============================================================================*/
          parameters.put(DELETE_FIELD_TAG_KEY, 70);
        parameters.put(DELETE_FIELD_SUBFIELD_KEY, "c");
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 3);
        
        result1 = (Record) Record.newInstance(saveRecord);
        result = RecordOperations.deleteFromRecord(result1, parameters);
        
        try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = result.findField((Integer) parameters.get(DELETE_FIELD_TAG_KEY));
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Delete subfield c from 3rd Occurrence Field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W^bsubfield b occ 3^csubfield c occ 3'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "Grieve, B.J.%Went, F.W%^aSubfield a occ 3^bsubfield b occ 3");
        System.out.println(result.toString());
        
         /*===============================================================================*/
        parameters.put(DELETE_FIELD_TAG_KEY, 70);
       
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 3);
        
        result1 = (Record) Record.newInstance(saveRecord);
        
       parameters.put(DELETE_FIELD_SUBFIELD_KEY, "a");
       result = RecordOperations.deleteFromRecord(result1, parameters);

       parameters.put(DELETE_FIELD_SUBFIELD_KEY, "b");
       result = RecordOperations.deleteFromRecord(result, parameters);

       parameters.put(DELETE_FIELD_SUBFIELD_KEY, "c");
       result = RecordOperations.deleteFromRecord(result, parameters);

        try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = result.findField((Integer) parameters.get(DELETE_FIELD_TAG_KEY));
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Delete subfield c from 3rd Occurrence Field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W^bsubfield b occ 3^csubfield c occ 3'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "Grieve, B.J.%Went, F.W%");
        System.out.println(result.toString());
        
          /*===============================================================================*/
        parameters.put(DELETE_FIELD_TAG_KEY, 70);
       
        parameters.put(DELETE_FIELD_OCCURRENCE_KEY, 0);
        
        result1 = (Record) Record.newInstance(saveRecord);
        
       parameters.put(DELETE_FIELD_SUBFIELD_KEY, "a");
       result = RecordOperations.deleteFromRecord(result1, parameters);

      

        try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = result.findField((Integer) parameters.get(DELETE_FIELD_TAG_KEY));
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Delete  Delete subfield a from all Occurrences Field 70");
        System.out.println("** 'Grieve, B.J.%Went, F.W^bsubfield b occ 3^csubfield c occ 3'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "Grieve, B.J.%Went, F.W%^bsubfield b occ 3^csubfield c occ 3");
        System.out.println(result.toString());
        
    }

    /**
     * Test of replaceInOccurrence method, of class RecordOperations.
     * * PERFORM THE REPLACE GLOBAL OPERATION TO THE RECORD (The parameters specify details on how to process)
     * 
     * Parameters
     * ----------
     * Text to find - The text to be replaced.
     * New text - Replacing text. If empty, the "text to find" value will be deleted.
     * 
     *  Options
     *  --------
     * 
     *  Case sensitive: will search the text as entered in the Text to find box. If this button is not checked,
     *  the text search will be case insensitive, e.g. ‘WATER’ is considered equal to ‘water’;
     * 
     *  Whole words only: will only replace the text when this is preceded and followed by spaces or is at the
     *  beginning or the end of the field (or subfield).
     * 
     *  Prompt on replace: will ask confirmation before each change.
     * 
     *  Scope
     *  -----
     *  If you want to replace text in certain fields only, enter the applicable fieldTag(s), up to a maximum of 10,
     *  in the Tags box separated by comma (e.g. 100,110,120). 
     * 
     *  If you want to replace text only in certain subfields, enter the applicable subfield code(s) in the 
     *  Subfields box (e.g. abc). 
     * 
     *  It is also possible to specify to which occurrences the change should be applied: 1,2,3 etc…
     * 
     * @param record
     * @param parameters
     * @return 
     */
    @Test
    public void testReplaceInOccurrence() throws Exception {
        System.out.println("replaceInOccurrence");
       
//        IField field = null;
//        char[] subfieldTags = null;
//        int iocc = 0;
//        String textToFind = "";
//        String replaceWith = "";
//        int caseSensitive = 0;
//        int wholeWordOnly = 0;
//        int promptOnReplace = 0;
//        RecordOperations.replaceInOccurrence(field, subfieldTags, iocc, textToFind, replaceWith, caseSensitive, wholeWordOnly, promptOnReplace);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of replaceInRecord method, of class RecordOperations.
     */
    @Test
    public void testReplaceInRecord() {
        System.out.println("replaceInRecord");
         String[] fieldTags = new String[]{"70"};      // At least a field tag
        String[] occurrences = null;    // maybe null
        char[] subfieldTags = null;     // maybe null
        String textToFind = "Grieve";
        String replaceWith = "GRIEVE";
        int caseSensitive = 1;    // 1 if case sensitive
        int wholeWordOnly = 0;    // 1 if whole word
        int promptOnReplace = 0;
        
        record_ = buildTestRecord();
        
         RecordOperations.replaceInRecord(record_,
                 fieldTags, occurrences,
                 subfieldTags, 
                 textToFind, replaceWith, caseSensitive, wholeWordOnly, promptOnReplace);
         String value = null;
           try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = record_.findField(70);
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        System.out.println("*****************************************************");
        System.out.println("***Test Global Replace");
        System.out.println("** 'GRIEVE, B.J.%Went, F.W'");
        System.out.println("*****************************************************");
 
        assertEquals(value, "GRIEVE, B.J.%Went, F.W");
        System.out.println(record_.toString());
        
        /*======================================================================*/
        
        textToFind =  "gRiEvE";
        replaceWith = "GRIEVE";
        caseSensitive = 0;    // case unsensitive (1 if case sensitive)
        
        
        record_ = buildTestRecord();
        
         RecordOperations.replaceInRecord(record_,
                 fieldTags, occurrences,
                 subfieldTags, 
                 textToFind, replaceWith, caseSensitive, wholeWordOnly, promptOnReplace);
         value = null;
           try {
            /**
             * Cannot use getField that always return a field even if it doesn't exist in the record 
             * In that case it returns an empty field connected to the tag
             */
            IField field = record_.findField(70);
            if (field == null) {
                value = null;
            } else {
                value = field.getStringFieldValue();
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        
       System.out.println("*****************************************************");
       System.out.println("***Test Global Replace");
       System.out.println("textToFind = \"gRrIeve\"");
       System.out.println("replaceWith = \"GRIEVE\"");
       System.out.println("caseSensitive = 0");
       System.out.println("** 'GRIEVE, B.J.%Went, F.W'");
       System.out.println("*****************************************************");

        assertEquals(value, "GRIEVE, B.J.%Went, F.W");
        System.out.println(record_.toString());
//        Record record = null;
//        String[] fieldTags = null;
//        String[] occurrences = null;
//        char[] subfieldTags = null;
//        String textToFind = "";
//        String replaceWith = "";
//        int caseSensitive = 0;
//        int wholeWordOnly = 0;
//        int promptOnReplace = 0;
//        Record expResult = null;
//        Record result = RecordOperations.replaceInRecord(record, fieldTags, occurrences, subfieldTags, textToFind, replaceWith, caseSensitive, wholeWordOnly, promptOnReplace);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

  
    
}
