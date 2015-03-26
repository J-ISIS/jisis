/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.global;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.record.FieldFactory;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.corelib.record.StringOccurrence;
import org.unesco.jisis.corelib.record.Subfield;
import org.unesco.jisis.corelib.util.StringUtils;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.ADD_ONLY_IF_NOT_PRESENT;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.DELETE_FIELD_OCCURRENCE_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.DELETE_FIELD_SUBFIELD_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.DELETE_FIELD_TAG_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.FIELD_OCCURRENCE_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.FIELD_TAG_KEY;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.INSERT_BEFORE_POSITION_FLAG;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.INSERT_BEFORE_POSITION_VALUE;
import static org.unesco.jisis.global.GlobalOperationsTopComponent.TEXT_TO_ADD;

/**
 *
 * @author jcd
 */
public class RecordOperations {
    
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GlobalOperationsTopComponent.class);
 
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
    public static Record addToRecord(Record record, Map<String, Object> parameters) {
        try {
            String newText = (String) parameters.get(TEXT_TO_ADD);
            int tag = (Integer) parameters.get(FIELD_TAG_KEY);

            int addOnlyIfNotPresent = (Integer) parameters.get(ADD_ONLY_IF_NOT_PRESENT); // 1 or 0

            int insertBeforePosition = (Integer) parameters.get(INSERT_BEFORE_POSITION_FLAG);
            int position = (Integer) parameters.get(INSERT_BEFORE_POSITION_VALUE);
            int ioccurrence = (Integer) parameters.get(FIELD_OCCURRENCE_KEY);

            IField field = record.findField(tag);
            if (field == null) {
                // Field doesn't exist in record, note that getField will create the field
                field = record.getField(tag);
                // Change full field value with replaceWith that may have subfieldTag delimiters and
                // occurrences separator
                field.setFieldValue(newText);
            } else {
                // The given field fieldTag is already present 
                if (addOnlyIfNotPresent == 1) {
                    return record;
                }
                if (insertBeforePosition == 0) {
                      // InsertBeforePosition chekbox **NOT CHECKED**
                    // Change full field value with replaceWith that may have subfieldTag delimiters and
                    // occurrences separator                  
                    field.setFieldValue(newText);
                } else {
                    // InsertBeforePosition chekbox **CHECKED**
                    int occurrenceCount = field.getOccurrenceCount();
                    if (ioccurrence == 0) {
                        // Process all occurrences
                        if (position == 0) {
                            // Position not set, then replace field with new text
                            field.setFieldValue(newText);
                        } else {
                            // position should be >0

                            for (int i = 0; i < occurrenceCount; i++) {
                                String occurrence = field.getStringOccurrence(i);
                                StringBuilder sb = new StringBuilder(occurrence);
                                if (position > occurrence.length()) {
                                    // Append to the end
                                    sb.append(newText);
                                } else {
                                    sb.insert(position - 1, newText);
                                }
                                field.setOccurrence(i, sb.toString());

                            }
                        }
                    } else {
                        // Occurrence Number > 0 (ioccurrence is 1 based)
                        if (ioccurrence > 0 && ioccurrence <= occurrenceCount) {
                            // Treat only the specified occurrence
                            String occurrence = field.getStringOccurrence(ioccurrence - 1);
                            StringBuilder sb = new StringBuilder(occurrence);
                            if (position > occurrence.length()) {
                                // Append to the end
                                sb.append(newText);
                            } else {
                                sb.insert(position - 1, newText);
                            }
                            field.setOccurrence(ioccurrence - 1, sb.toString());
                        } else {
                            // Add a new occurrence to the field
                            field.setOccurrence(occurrenceCount, newText);
                        }
                    }

                }
            }
            record.setField(field);

        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        return record;

    }
    
    /**
     * Delete Operation - Delete data from the record (The parameters specify details on how to process)
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
     * @param record
     * @param parameters
     * @return The modified record
     */
    
    public static Record deleteFromRecord(Record record, Map<String, Object> parameters) {
        try {

            int fieldTag = (Integer) parameters.get(DELETE_FIELD_TAG_KEY);

            String subfieldTag = (String) parameters.get(DELETE_FIELD_SUBFIELD_KEY);

            int ioccurrence = (Integer) parameters.get(DELETE_FIELD_OCCURRENCE_KEY);

            IField field = record.findField(fieldTag);
            if (field == null) {
                // Field doesn't exist in record, return
                return record;

            }
            // Get the number of occurrences
            int occurrenceCount = field.getOccurrenceCount();

            if (occurrenceCount > 0) {
                if (ioccurrence == 0) {
                    // Treat all occurrences
                    if (subfieldTag == null || subfieldTag.equals(" ") || subfieldTag.equals("")) {
                        // Delete the full field !
                        record.removeField(fieldTag);
                    } else {
                        // remove subfieldTag from all occurrences
                        // Iterate over the occurrences
                        for (int j = 0; j < occurrenceCount; j++) {
                            // Get a reference to the jeme occurrence of the ieme field  
                            StringOccurrence occ = (StringOccurrence) field.getOccurrence(j);
                            // Get the subfields, an occurrence has at least one subfieldTag
                            // The data without a subfieldTag delimiter code pair has a dummy
                            // subfieldTag code char “*”    
                            Subfield[] subfields = occ.getSubfields();
                            // Iterate over the subfields
                            for (Subfield subfield : subfields) {
                                // Get the subfieldTag delimiter code
                                char code = subfield.getSubfieldCode();
                                if (code == subfieldTag.charAt(0)) {
                                    // Delete subfieldTag from occurrence
                                    occ.removeSubfieldOccurrence(Global.SUBFIELD_SEPARATOR+subfieldTag, 0);
                                }
                            }
                        }
                    }
                } else {
                    // Treat a single occurrence
                    // Occurrence Number > 0 is 1 based
                    if (ioccurrence > 0 && ioccurrence <= occurrenceCount) {
                        // Treat only the specified occurrence
                         if (subfieldTag == null || subfieldTag.equals(" ") || subfieldTag.equals("")) {
                            // Delete the whole occurrence
                            field.removeOccurrence(ioccurrence - 1);
                        } else {
                            StringOccurrence occurrence = (StringOccurrence) field.getOccurrence(ioccurrence - 1);
                        // Get the subfields, an occurrence has at least one subfieldTag
                            // The data without a subfieldTag delimiter code pair has a dummy
                            // subfieldTag code char “*”  

                            char subfieldCode = subfieldTag.charAt(0);
                            Subfield[] subfields = occurrence.getSubfields();
                            // Iterate over the subfields
                            for (Subfield subfield : subfields) {
                                // Get the subfieldTag delimiter code
                                char code = subfield.getSubfieldCode();
                                if (code == subfieldCode) {
                                    // Delete subfieldTag from occurrence
                                    occurrence.removeSubfieldOccurrence(Global.SUBFIELD_SEPARATOR + subfieldCode, 0);
                                }
                            }
                            field.setOccurrence(ioccurrence - 1, occurrence);
                        }
                    }
                }
            }
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        return record;
    }
    
     public static void replaceInOccurrence(IField field, char[] subfieldTags, int iocc, String textToFind, String replaceWith,
        int caseSensitive, int wholeWordOnly, int promptOnReplace) throws DbException {

        if (null != subfieldTags && subfieldTags.length > 0) {
            // Only replace in subfields, then get the subfields 
            StringOccurrence occ = (StringOccurrence) field.getOccurrence(iocc);
            Subfield[] subfields = occ.getSubfields();

            for (char subfieldTag : subfieldTags) {
                // Loop on record field subfields to identify subfields with code subfieldTag 
                for (Subfield sub : subfields) {
                    if (subfieldTag == sub.getSubfieldCode()) {
                        // This subfield has the right delimiter code
                        String data = sub.getData();
                        data = StringUtils.replaceAllEx(data, textToFind, replaceWith, caseSensitive, wholeWordOnly);
                        sub.setData(data);                        
                    }
                }
            }
            occ.setSubfields(subfields);          
        } else {
            // DO replace in the occurrence whatever the subfields
            String occurrence = field.getStringOccurrence(iocc);
            occurrence = StringUtils.replaceAllEx(occurrence, textToFind, replaceWith, caseSensitive, wholeWordOnly);
            field.setOccurrence(iocc, occurrence);
        }        
    }
    
    /**
     * PERFORM THE REPLACE GLOBAL OPERATION TO THE RECORD (The parameters specify details on how to process)
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
     
     public static Record replaceInRecord(Record record, String[] fieldTags, String[] occurrences, 
        char[] subfieldTags, String textToFind, String replaceWith, int caseSensitive, int wholeWordOnly,
        int promptOnReplace) {

        boolean allOccurrences = false;
        if (occurrences != null) {
            if (occurrences.length == 1 && occurrences[0].equals("0")) {
                /**
                 * allOccurrences is true if we have a have a single value 0 as occurrence number which means
                 * all occurrences
                 */
                allOccurrences = true;
            }
        }

        // Loop on the fields
        for (String fieldTag : fieldTags) {
            IField field;
            try {
                int itag = Integer.parseInt(fieldTag);
                field = record.findField(itag);
                if (field == null) {
                    // Field doesn't exist in record, continue to next field
                    continue;
                }
                /**
                 * Check if the field contains the text to replace, so that we don't waste time to
                 * try to replace.
                 */
                String fieldValue = field.getStringFieldValue();
                if (StringUtils.findEx(fieldValue, textToFind, caseSensitive, wholeWordOnly) == -1) {
                    continue; // Field doesn't contain the text to replace
                }
                /**
                 * Create a deep copy of the field (not a reference)
                 */
                IField newField = FieldFactory.newInstance(field);
                if (allOccurrences) {
                    // Replacement on all occurrences
                    int occurrenceCount = newField.getOccurrenceCount();
                    for (int i = 0; i < occurrenceCount; i++) {
                        replaceInOccurrence(newField, subfieldTags, i, textToFind, replaceWith,
                            caseSensitive, wholeWordOnly, promptOnReplace);
                    }
                } else if (occurrences == null) {
                     // DO replace in the field whatever the occurrences and subfields
                    fieldValue = newField.getStringFieldValue();

                    fieldValue = StringUtils.replaceAllEx(fieldValue, textToFind, replaceWith, caseSensitive, wholeWordOnly);
                    newField.setFieldValue(fieldValue);
                } else if (occurrences.length >= 1) {
                    // Replacement on specified occurrences
                    for (String occurrence : occurrences) {
                        int iocc = Integer.parseInt(occurrence) - 1;
                        replaceInOccurrence(newField, subfieldTags, iocc, textToFind, replaceWith,
                            caseSensitive, wholeWordOnly, promptOnReplace);
                    }
                } else {
                   LOGGER.error("Invalid State in globalReplaceInRecord");
                }
                if (promptOnReplace == 1) {
                   String s = field.getStringFieldValue();
                   s = StringUtils.hilightAllCaseInsensitive(s,textToFind);
                    String message = NbBundle.getMessage(RecordOperations.class,
                        "MSG_FIELD_REPLACE_PROMPT", record.getMfn(), field.getTag(), s);
                    NotifyDescriptor d
                        = new NotifyDescriptor.Confirmation(message,
                            NotifyDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                        // Update Field in record
                        record.setField(newField);
                    }
                } else {
                    // Update Field in record
                    record.setField(newField);
                }

            } catch (DbException ex) {
                LOGGER.error("Error when attempting to replace field {} on record with MFN {}"
                    , new Object[]{ fieldTag, record.getMfn()}, ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
           
        }
        
        return record;

    }
    
      public static String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes()));
    }
}
