/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.z3950;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.DataField;

/**
 *
 * @author jcd
 */
public class Marc21 {

  
     static private boolean has880Data = false;


   private Marc21() {
    
   }

   private String getSubfieldData(String tag, char code, org.marc4j.marc.Record record) {
      org.marc4j.marc.DataField field = (DataField) record.getVariableField(tag);
      String data = "";
      if (field != null) {
         org.marc4j.marc.Subfield subfield = field.getSubfield(code);
         if (subfield != null) {
            data = subfield.getData();
         }
      }
      return data;
   }

   public static java.util.Hashtable getISBD(org.marc4j.marc.Record record, boolean onlyNoLinkDetails) {
        java.util.Hashtable htISBD = new java.util.Hashtable();
       
        if(record!=null){
            htISBD = getNoLinkDetailsWith880(record, htISBD);
            if(onlyNoLinkDetails) return htISBD;

            String mainEntry = null, uniformTitle = null, relatedNames = null, seriesAuthors = null, subjects = null;
            java.util.Hashtable htLinkingEntries = null;

            mainEntry = getMainEntry(record);
            if(!mainEntry.trim().equals("")) htISBD.put("MainEntry", mainEntry);

            uniformTitle = getUniformTitle(record);
            if(!uniformTitle.trim().equals("")) htISBD.put("UniformTitle", uniformTitle);

            relatedNames = getRelatedNames(record);
            //System.out.println("relatedNames##################"+relatedNames);
            if(!relatedNames.trim().equals(""))
                htISBD.put("RelatedNames", relatedNames);

            seriesAuthors = getSeriesAuthors(record);
            if(!seriesAuthors.trim().equals("")) htISBD.put("SeriesAuthors", seriesAuthors);

            subjects = getSubjects(record);
            if(!subjects.trim().equals("")) htISBD.put("Subjects", subjects);

            htLinkingEntries = getLinkingEntries(record);
            if(htLinkingEntries != null) htISBD.put("LinkingEntries", htLinkingEntries);
        }
        //System.out.println("Convertr.getISBD"+htISBD);
        return htISBD;
    }

    public static java.util.Hashtable getNoLinkDetailsWith880(org.marc4j.marc.Record record, java.util.Hashtable htISBD) {
        if(record!=null){
            String title = null, edition = null, publication = null, pubPlace = null, pub = null, pubYear = null, relatedTitle = null, physicalDescription = null, seriesTitle = null, isbn = null, issn = null;
            java.util.Hashtable notesHt = new java.util.Hashtable();

            title = getTitle(record);
            if(!title.trim().equals("")) htISBD.put("TITLE_SOR", title);

            edition = getEdition(record);
            if(!edition.trim().equals("")) htISBD.put("EDITION", edition);

            publication = getPublication(record);
            if(!publication.trim().equals("")) htISBD.put("PUBLISHER", publication);

            pubPlace = getPubPlace(record);
            if(!pubPlace.trim().equals("")) htISBD.put("Place", pubPlace);

            pub = getPub(record);
            if(!pub.trim().equals("")) htISBD.put("Publisher", pub);

            pubYear = getPubYear(record);
            if(!pubYear.trim().equals("")) htISBD.put("Year", pubYear);

            relatedTitle = getRelatedTitles(record);
            if(!relatedTitle.trim().equals("")) htISBD.put("RelatedTitle", relatedTitle);

            physicalDescription = getPhysicalDescription(record);
            if(!physicalDescription.trim().equals("")) htISBD.put("PHYSICAL_DESCRIPTION", physicalDescription);

            seriesTitle = getSeriesTitle(record);
            if(!seriesTitle.trim().equals("")) htISBD.put("SeriesTitle", seriesTitle);

            String notes = null;

            notes = getXXXNotes(record, "500");
            if(!notes.trim().equals("")) notesHt.put("GENERAL_NOTE", notes);

            notes = getXXXNotes(record, "502");
            if(!notes.trim().equals("")) notesHt.put("DISSERTATION_NOTE", notes);

            notes = getXXXNotes(record, "504");
            if(!notes.trim().equals("")) notesHt.put("BIBLIOGRAPHIC_NOTE", notes);

            notes = getXXXNotes(record, "505");
            if(!notes.trim().equals("")) notesHt.put("FORMATTED_CONTENTS_NOTE", notes);

            notes = getXXXNotes(record, "520");
            if(!notes.trim().equals("")) notesHt.put("SUMMARY_NOTE", notes);

            notes = getXXXNotes(record, "516");
            if(!notes.trim().equals("")) notesHt.put("TYPE_OF_COMPUTER_FILE_DATA_NOTE", notes);

            notes = getXXXNotes(record, "508");
            if(!notes.trim().equals("")) notesHt.put("CREATION_PRODUCTION_CREDITS_NOTE", notes);

            notes = getXXXNotes(record, "546");
            if(!notes.trim().equals("")) notesHt.put("LANGUAGE_NOTE", notes);

            notes = getXXXNotes(record, "538");
            if(!notes.trim().equals("")) notesHt.put("SYSTEM_DETAILS_NOTE", notes);

            notes = getXXXNotes(record, "530");
            if(!notes.trim().equals("")) notesHt.put("ADDITIONAL_PHYSICAL_FORM_AVAILABLE_NOTE", notes);

            if(notesHt.size()>0) htISBD.put("NOTES", notesHt);

            isbn = getISBN(record);
            if(!isbn.equals("")) htISBD.put("ISBN", isbn);

            issn = getISSN(record);
            if(!issn.equals("")) htISBD.put("ISSN", issn);
        }
        return htISBD;
    }

    public static String getMainEntry(org.marc4j.marc.Record record){
       String author = getFieldDataFormCMD(record, "100");
        if(author.equals(""))
            author = getFieldDataFormCMD(record, "110");
        if(author.equals(""))
            author = getFieldDataFormCMD(record, "111");
        return author;
        
    }

   public static String getFieldDataFormCMD(org.marc4j.marc.Record record, String tag) {
      String data = "";
      try {
         if (record.getVariableField(tag) != null) {
            DataField field = (DataField) record.getVariableFields(tag);
            if (field.getSubfield('a') != null) {
               data = field.getSubfield('a').getData();
            }
            List subfieldList = field.getSubfields();
            if (subfieldList != null && !subfieldList.isEmpty()) {
               for (int k = 0; k < subfieldList.size(); k++) {
                  org.marc4j.marc.Subfield subfield = (org.marc4j.marc.Subfield) subfieldList.get(k);
                  if (subfield.getCode() != 'a' && subfield.getCode() != 'e' && subfield.getCode() != '6') {
                     data += "-" + subfield.getData();
                  }
               }
            }
            if (field.getSubfield('6') != null) {
               String data880 = getParticular880Data(tag, field.getSubfield('6').getData(), record);
               if (!data880.equals("")) {
                  data += "<br>" + data880;
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return data;
   }

   public static String getUniformTitle(org.marc4j.marc.Record record) {
      String uniformTitle = "";
      try {
         if (record.getVariableField("240") != null) {
            DataField field = (DataField) record.getVariableField("240");
            if (field.getSubfield('a') != null) {
               uniformTitle = field.getSubfield('a').getData();
            }
            if (field.getSubfield('f') != null) {
               uniformTitle += " " + field.getSubfield('f').getData();
            }
            if (field.getSubfield('6') != null) {
               String data880 = getParticular880Data("240", field.getSubfield('6').getData(), record);
               if (!data880.equals("")) {
                  uniformTitle += "<br>" + data880;
               }
            }
         } else if (record.getVariableField("130") != null) {
            DataField field = (DataField) record.getVariableField("130");
            if (field.getSubfield('a') != null) {
               uniformTitle = field.getSubfield('a').getData();
            }
            if (field.getSubfield('f') != null) {
               uniformTitle += " " + field.getSubfield('f').getData();
            }
            if (field.getSubfield('6') != null) {
               String data880 = getParticular880Data("130", field.getSubfield('6').getData(), record);
               if (!data880.equals("")) {
                  uniformTitle += "<br>" + data880;
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return uniformTitle;

   }

   public static String getTitle(org.marc4j.marc.Record record) {
      String title = "";
      try {
         if (record.getVariableField("245") != null) {
            DataField field = (DataField) record.getVariableField("245");
            if (field.getSubfield('a') != null) {
               title = field.getSubfield('a').getData();
            }
            List field246List = record.getVariableFields("246");
            for (int i = 0; i < field246List.size(); i++) {
               DataField field246 = (DataField) field246List.get(i);
               if (field246.getSubfield('a') != null) {
                  title += " = " + field246.getSubfield('a').getData();
               }
            }
            if (field.getSubfield('n') != null) {
               title += " " + field.getSubfield('n').getData();
            }
            if (field.getSubfield('b') != null) {
               title += " : " + field.getSubfield('b').getData();
            }
            if (field.getSubfield('c') != null) {
               title += " / " + field.getSubfield('c').getData();
            }
            if (field.getSubfield('6') != null) {
               String data880 = getParticular880Data("245", field.getSubfield('6').getData(), record);
               if (!data880.equals("")) {
                  title += "<br>" + data880;
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return title;
   }

   public static String getEdition(org.marc4j.marc.Record record) {
      String edition = "";
      try {
         if (record.getVariableField("250") != null) {
            DataField field = (DataField) record.getVariableField("250");
            if (field.getSubfields() != null && field.getSubfields().size() > 0) {
               if (field.getSubfield('a') != null) {
                  edition = field.getSubfield('a').getData();
               }
               if (field.getSubfield('b') != null) {
                  edition += " " + field.getSubfield('b').getData();
               }
               if (field.getSubfield('6') != null) {
                  String data880 = getParticular880Data("250", field.getSubfield('6').getData(), record);
                  if (!data880.equals("")) {
                     edition += "<br>" + data880;
                  }
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return edition;

   }

   public static String getPublication(org.marc4j.marc.Record record) {
      String publication = "";
      try {
         List field260List = record.getVariableFields("260");
         for (int j = 0; j < field260List.size(); j++) {
            DataField field260 = (DataField) field260List.get(j);
            if (field260.getSubfields().size() > 0) {
               if (j != 0 && !publication.trim().equals("")) {
                  publication += " ; ";
               }
               if (field260.getSubfield('a') != null) {
                  publication += field260.getSubfield('a').getData();
               }
               if (field260.getSubfield('b') != null) {
                  if (!publication.trim().equals("")) {
                     publication += " : " + field260.getSubfield('b').getData();
                  } else {
                     publication += field260.getSubfield('b').getData();
                  }
               }
               if (field260.getSubfield('c') != null) {
                  if (!publication.trim().equals("")) {
                     publication += " , " + field260.getSubfield('c').getData();
                  } else {
                     publication += field260.getSubfield('c').getData();
                  }
               }
               if (field260.getSubfield('e') != null || field260.getSubfield('f') != null || field260.getSubfield('g') != null) {
                  publication += " (";
               }
               if (field260.getSubfield('e') != null) {
                  publication += field260.getSubfield('e').getData();
               }
               if (field260.getSubfield('f') != null) {
                  publication += " : " + field260.getSubfield('f').getData();
               }
               if (field260.getSubfield('g') != null) {
                  publication += " , " + field260.getSubfield('g').getData();
               }
               if (field260.getSubfield('e') != null || field260.getSubfield('f') != null || field260.getSubfield('g') != null) {
                  publication += ")";
               }
               if (field260.getSubfield('6') != null) {
                  String data880 = getParticular880Data("260", field260.getSubfield('6').getData(), record);
                  if (!data880.equals("")) {
                     publication += "<br>" + data880;
                  }
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return publication;
   }

   public static String getPubPlace(org.marc4j.marc.Record record) {
      String pubPlace = "";
      try {
         List field260List = record.getVariableFields("260");
         for (int j = 0; j < field260List.size(); j++) {
            DataField field260 = (DataField) field260List.get(j);
            if (field260.getSubfields().size() > 0) {
               if (field260.getSubfield('a') != null && !field260.getSubfield('a').getData().equals("")) {
                  if (j != 0 && !pubPlace.trim().equals("")) {
                     pubPlace += " ; ";
                  }
                  pubPlace += field260.getSubfield('a').getData();
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return pubPlace;
   }

   public static String getPub(org.marc4j.marc.Record record) {
      String pub = "";
      try {
         List field260List = record.getVariableFields("260");
         for (int j = 0; j < field260List.size(); j++) {
            DataField field260 = (DataField) field260List.get(j);
            if (field260.getSubfields().size() > 0) {
               if (field260.getSubfield('b') != null && !field260.getSubfield('b').getData().equals("")) {
                  if (j != 0 && !pub.trim().equals("")) {
                     pub += " ; ";
                  }
                  pub += field260.getSubfield('b').getData();
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return pub;
   }

   public static String getPubYear(org.marc4j.marc.Record record) {
      String pubYear = "";
      try {
         List field260List = record.getVariableFields("260");
         for (int j = 0; j < field260List.size(); j++) {
            DataField field260 = (DataField) field260List.get(j);
            if (field260.getSubfields().size() > 0) {
               if (field260.getSubfield('c') != null && !field260.getSubfield('c').getData().equals("")) {
                  if (j != 0 && !pubYear.trim().equals("")) {
                     pubYear += " ; ";
                  }
                  pubYear += field260.getSubfield('c').getData();
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return pubYear;
   }

   public static String getRelatedNames(org.marc4j.marc.Record record) {
      String relatedNames = "";
      try {

         List field700List = record.getVariableFields("700");
         for (int j = 0; j < field700List.size(); j++) {
            DataField field700 = (DataField) field700List.get(j);

            String data880 = getParticular880Data("700", field700.getSubfield('6').getData(), record);
            if (!relatedNames.trim().equals("")) {
               relatedNames += ";";
               if (!data880.equals("")) {
                  relatedNames += "<br>";
               }
               if (has880Data) {
                  relatedNames += "<br>";
                  has880Data = false;
               }
            }
            if (field700.getSubfield('a').getData() != null) {
               relatedNames += field700.getSubfield('a').getData();
            }
            List lisuvs = field700.getSubfields();
            if (lisuvs != null && !lisuvs.isEmpty()) {
               String jtauth = "";
               for (int k = 0; k < lisuvs.size(); k++) {
                  org.marc4j.marc.Subfield sfsub = (org.marc4j.marc.Subfield) lisuvs.get(k);
                  if (sfsub.getCode() != 'a' && sfsub.getCode() != 'e' && sfsub.getCode() != '6') {
                     relatedNames += "-" + sfsub.getData();
                  } else {
                     if (sfsub.getCode() == 'e') {
                        jtauth += "-" + sfsub.getData();
                     }
                  }
               }
               if (jtauth == null || jtauth.trim().equals("")) {
                  relatedNames += " jt. auth.";
               } else {
                  relatedNames += "-" + jtauth;
               }
            }
            if (!data880.equals("")) {
               relatedNames += "<br>" + data880;
               has880Data = true;
            }
         }
         List field710List = record.getVariableFields("710");

         for (int j = 0; j < field710List.size(); j++) {
            DataField field710 = (DataField) field710List.get(j);
            String data880 = getParticular880Data("710", field710.getSubfield('6').getData(), record);

            //System.out.println("field710data880: $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ "+data880);
            if (!relatedNames.trim().equals("")) {
               relatedNames += ";";
               if (!data880.equals("")) {
                  relatedNames += "<br>";
               }
               if (has880Data) {
                  relatedNames += "<br>";
                  has880Data = false;
               }
            }
            if (field710.getSubfield('a').getData() != null) {
               relatedNames += field710.getSubfield('a').getData();
            }
            List lisuvs = field710.getSubfields();
            if (lisuvs != null && lisuvs.size() != 0) {
               String jtauth = "";
               for (int k = 0; k < lisuvs.size(); k++) {
                  org.marc4j.marc.Subfield sfsub = (org.marc4j.marc.Subfield) lisuvs.get(k);
                  if (sfsub.getCode() != 'a' && sfsub.getCode() != 'e' && sfsub.getCode() != '6') {
                     relatedNames += "-" + sfsub.getData();
                  } else {
                     if (sfsub.getCode() == 'e') {
                        jtauth += "-" + sfsub.getData();
                     }
                  }
               }
               if (jtauth == null || jtauth.trim().equals("")) {
                  relatedNames += " jt. auth.";
               } else {
                  relatedNames += "-" + jtauth;
               }
            }
            if (!data880.equals("")) {
               relatedNames += "<br>" + data880;
               has880Data = true;
            }
         }
         List field711List = record.getVariableFields("711");
         for (int j = 0; j < field711List.size(); j++) {
            DataField field711 = (DataField) field711List.get(j);
            String data880 = getParticular880Data("711", field711.getSubfield('6').getData(), record);
            if (!relatedNames.trim().equals("")) {
               relatedNames += ";";
               if (!data880.equals("")) {
                  relatedNames += "<br>";
               }
               if (has880Data) {
                  relatedNames += "<br>";
                  has880Data = false;
               }
            }
            if (field711.getSubfield('a').getData() != null) {
               relatedNames += field711.getSubfield('a').getData();
            }
            List lisuvs = field711.getSubfields();
            if (lisuvs != null && lisuvs.size() != 0) {
               String jtauth = "";
               for (int k = 0; k < lisuvs.size(); k++) {
                  org.marc4j.marc.Subfield sfsub = (org.marc4j.marc.Subfield) lisuvs.get(k);
                  if (sfsub.getCode() != 'a' && sfsub.getCode() != 'e' && sfsub.getCode() != '6') {
                     relatedNames += "-" + sfsub.getData();
                  } else {
                     if (sfsub.getCode() == 'e') {
                        jtauth += "-" + sfsub.getData();
                     }
                  }
               }
               if (jtauth == null || jtauth.trim().equals("")) {
                  relatedNames += " jt. auth.";
               } else {
                  relatedNames += "-" + jtauth;
               }
            }
            if (!data880.equals("")) {
               relatedNames += "<br>" + data880;
               has880Data = true;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return relatedNames;
   }

   public static String getRelatedTitles(org.marc4j.marc.Record record) {
      String relatedTitles = "";
      try {
         List field740List = record.getVariableFields("740");
         for (int j = 0; j < field740List.size(); j++) {
            DataField field740 = (DataField) field740List.get(j);
            String data880 = getParticular880Data("740", field740.getSubfield('6').getData(), record);
            if (!relatedTitles.trim().equals("")) {
               relatedTitles += ";";
               if (!data880.equals("")) {
                  relatedTitles += "<br>";
               }
               if (has880Data) {
                  relatedTitles += "<br>";
                  has880Data = false;
               }
            }
            if (field740.getSubfield('a') != null) {
               relatedTitles += field740.getSubfield('a').getData();
            }
            if (!data880.equals("")) {
               relatedTitles += "<br>" + data880;
               has880Data = true;
            }
         }
         List field730List = record.getVariableFields("730");
         for (int j = 0; j < field730List.size(); j++) {
            DataField field730 = (DataField) field730List.get(j);
            String data880 = getParticular880Data("730", field730.getSubfield('6').getData(), record);
            if (!relatedTitles.trim().equals("")) {
               relatedTitles += ";";
               if (!data880.equals("")) {
                  relatedTitles += "<br>";
               }
               if (has880Data) {
                  relatedTitles += "<br>";
                  has880Data = false;
               }
            }
            if (field730.getSubfield('a') != null) {
               relatedTitles += field730.getSubfield('a').getData();
            }
            if (field730.getSubfield('f') != null) {
               relatedTitles += " : " + field730.getSubfield('f').getData();
            }
            if (!data880.equals("")) {
               relatedTitles += "<br>" + data880;
               has880Data = true;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return relatedTitles;
   }

   public static String getPhysicalDescription(org.marc4j.marc.Record record) {
      String physicalDescription = "";
      try {
         List field300List = record.getVariableFields("300");
         for (int j = 0; j < field300List.size(); j++) {
            DataField field300 = (DataField) field300List.get(j);
            if (!physicalDescription.equals("")) {
               physicalDescription += " ; ";
            }
            if (field300.getSubfield('a') != null) {
               physicalDescription += field300.getSubfield('a').getData();
            }
            if (field300.getSubfield('b') != null) {
               physicalDescription += " : " + field300.getSubfield('b').getData();
            }
            if (field300.getSubfield('c') != null) {
               physicalDescription += " ; " + field300.getSubfield('c').getData();
            }
            if (field300.getSubfield('6') != null) {
               String data880 = getParticular880Data("300", field300.getSubfield('6').getData(), record);
               if (!data880.equals("")) {
                  physicalDescription += "<br>" + data880;
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return physicalDescription;
   }

   public static String getSeriesAuthors(org.marc4j.marc.Record record) {
      String seriesAuthors = "";
      try {
         List field800List = record.getVariableFields("800");
         for (int j = 0; j < field800List.size(); j++) {
            DataField field800 = (DataField) field800List.get(j);
            String data880 = getParticular880Data("800", field800.getSubfield('6').getData(), record);
            if (!seriesAuthors.trim().equals("")) {
               seriesAuthors += ";";
               if (!data880.equals("")) {
                  seriesAuthors += "<br>";
               }
               if (has880Data) {
                  seriesAuthors += "<br>";
                  has880Data = false;
               }
            }
            if (field800.getSubfield('a') != null) {
               seriesAuthors += field800.getSubfield('a').getData();
            }
            if (!data880.equals("")) {
               seriesAuthors += "<br>" + data880;
               has880Data = true;
            }
         }
         List field810List = record.getVariableFields("810");
         for (int j = 0; j < field810List.size(); j++) {
            DataField field810 = (DataField) field810List.get(j);
            String data880 = getParticular880Data("810", field810.getSubfield('6').getData(), record);
            if (!seriesAuthors.trim().equals("")) {
               seriesAuthors += ";";
               if (!data880.equals("")) {
                  seriesAuthors += "<br>";
               }
               if (has880Data) {
                  seriesAuthors += "<br>";
                  has880Data = false;
               }
            }
            if (field810.getSubfield('a') != null) {
               seriesAuthors += field810.getSubfield('a').getData();
            }
            if (!data880.equals("")) {
               seriesAuthors += "<br>" + data880;
               has880Data = true;
            }
         }
         List field811List = record.getVariableFields("811");
         for (int j = 0; j < field811List.size(); j++) {
            DataField field811 = (DataField) field811List.get(j);
            String data880 = getParticular880Data("811", field811.getSubfield('6').getData(), record);
            if (!seriesAuthors.trim().equals("")) {
               seriesAuthors += ";";
               if (!data880.equals("")) {
                  seriesAuthors += "<br>";
               }
               if (has880Data) {
                  seriesAuthors += "<br>";
                  has880Data = false;
               }
            }
            if (field811.getSubfield('a') != null) {
               seriesAuthors += field811.getSubfield('a').getData();
            }
            if (!data880.equals("")) {
               seriesAuthors += "<br>" + data880;
               has880Data = true;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return seriesAuthors;
   }

   public static String getSeriesTitle(org.marc4j.marc.Record record) {
      String seriesTitle = "";
      try {
         List field440List = record.getVariableFields("440");
         for (int j = 0; j < field440List.size(); j++) {
            DataField field440 = (DataField) field440List.get(j);
            String data880 = getParticular880Data("440", field440.getSubfield('6').getData(), record);
            if (!seriesTitle.trim().equals("")) {
               seriesTitle += ";";
               if (!data880.equals("")) {
                  seriesTitle += "<br>";
               }
               if (has880Data) {
                  seriesTitle += "<br>";
                  has880Data = false;
               }
            }
            if (field440.getSubfield('a') != null) {
               seriesTitle += field440.getSubfield('a').getData();
            }
            if (field440.getSubfield('n') != null) {
               seriesTitle += " : " + field440.getSubfield('n').getData();
            }
            if (field440.getSubfield('p') != null) {
               seriesTitle += " : " + field440.getSubfield('p').getData();
            }
            if (field440.getSubfield('r') != null) {
               seriesTitle += " : " + field440.getSubfield('r').getData();
            }
            if (field440.getSubfield('x') != null) {
               seriesTitle += " , " + field440.getSubfield('x').getData();
            }
            if (!data880.equals("")) {
               seriesTitle += "<br>" + data880;
               has880Data = true;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return seriesTitle;
   }

   public static String getXXXNotes(org.marc4j.marc.Record record, String tag) {
      String notes = "";
      try {
         List fieldList = record.getVariableFields(tag);
         for (int j = 0; j < fieldList.size(); j++) {
            DataField field = (DataField) fieldList.get(j);
            if (field.getSubfields() != null && field.getSubfields().size() > 0) {
               String data880 = getParticular880Data(tag, field.getSubfield('6').getData(), record);
               if (!notes.equals("")) {
                  notes += ";";
                  if (!data880.equals("")) {
                     notes += "<br>";
                  }
                  if (has880Data) {
                     notes += "<br>";
                     has880Data = false;
                  }
               }
               if (field.getSubfield('a') != null) {
                  notes += field.getSubfield('a').getData();
               }
               if (!data880.equals("")) {
                  notes += "<br>" + data880;
                  has880Data = true;
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return notes;
   }

    public static String getSubjects(org.marc4j.marc.Record record) {
      String subjects = "";
      try {
         List fieldList = record.getVariableFields();
         for (int j = 0; j < fieldList.size(); j++) {
            DataField field = (DataField) fieldList.get(j);
            if (field.getTag().startsWith("6")) {
               String data880 = getParticular880Data(field.getTag(), field.getSubfield('6').getData(), record);
               if (!subjects.trim().equals("")) {
                  subjects += ";";
                  if (!data880.equals("")) {
                     subjects += "<br>";
                  }
                  if (has880Data) {
                     subjects += "<br>";
                     has880Data = false;
                  }
               }
               if (field.getSubfield('a') != null) {
                  subjects += field.getSubfield('a').getData();
               }
               List lisuvs = field.getSubfields('x');
               if (lisuvs != null && !lisuvs.isEmpty()) {
                  for (int k = 0; k < lisuvs.size(); k++) {
                     org.marc4j.marc.Subfield sfsub = (org.marc4j.marc.Subfield) lisuvs.get(k);
                     subjects += "-" + sfsub.getData();
                  }
               }
               lisuvs = field.getSubfields('y');
               if (lisuvs != null && !lisuvs.isEmpty()) {
                  for (int k = 0; k < lisuvs.size(); k++) {
                     org.marc4j.marc.Subfield sfsub = (org.marc4j.marc.Subfield) lisuvs.get(k);
                     subjects += "-" + sfsub.getData();
                  }
               }
               lisuvs = field.getSubfields('v');
               if (lisuvs != null && !lisuvs.isEmpty()) {
                  for (int k = 0; k < lisuvs.size(); k++) {
                     org.marc4j.marc.Subfield sfsub = (org.marc4j.marc.Subfield) lisuvs.get(k);
                     subjects += "-" + sfsub.getData();
                  }
               }
               lisuvs = field.getSubfields('z');
               if (lisuvs != null && !lisuvs.isEmpty()) {
                  for (int k = 0; k < lisuvs.size(); k++) {
                     org.marc4j.marc.Subfield sfsub = (org.marc4j.marc.Subfield) lisuvs.get(k);
                     subjects += "-" + sfsub.getData();
                  }
               }
               if (!data880.equals("")) {
                  subjects += "<br>" + data880;
                  has880Data = true;
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return subjects;
   }

    public static String getISBN(org.marc4j.marc.Record record){
        return getFieldDataFormCMD(record, "020");
    }

    public static String getISSN(org.marc4j.marc.Record record){
        return getFieldDataFormCMD(record, "022");
    }

    public static java.util.Hashtable getLinkingEntries(org.marc4j.marc.Record record){
        java.util.Hashtable htLinkingEntries = new java.util.Hashtable();
        try{
            htLinkingEntries.put("MainSeriesEntry", getLinkingEntry(record, "760"));
            htLinkingEntries.put("SubSeriesEntry", getLinkingEntry(record, "762"));
            htLinkingEntries.put("OriginalLanguageEntry", getLinkingEntry(record, "765"));
            htLinkingEntries.put("TranslationEntry", getLinkingEntry(record, "767"));
            htLinkingEntries.put("SupplementOrSpecialIssueEntry", getLinkingEntry(record, "770"));
            htLinkingEntries.put("SupplementParentEntry", getLinkingEntry(record, "772"));
            htLinkingEntries.put("HostItemEntry", getLinkingEntry(record, "773"));
            htLinkingEntries.put("ConstituentUnitEntry", getLinkingEntry(record, "774"));
            htLinkingEntries.put("OtherEditionEntry", getLinkingEntry(record, "775"));
            htLinkingEntries.put("AdditionalPhysicalFormEntry", getLinkingEntry(record, "776"));
            htLinkingEntries.put("IssuedWithEntry", getLinkingEntry(record, "777"));
            htLinkingEntries.put("PrecedingEntry", getLinkingEntry(record, "780"));
            htLinkingEntries.put("SucceedingEntry", getLinkingEntry(record, "785"));
            htLinkingEntries.put("DataSourceEntry", getLinkingEntry(record, "786"));
            htLinkingEntries.put("NonspecificRelationshipEntry", getLinkingEntry(record, "787"));
        }catch(Exception e){e.printStackTrace();}
        return htLinkingEntries;
    }

    public static String getLinkingEntry(org.marc4j.marc.Record record, String tag){
        String linkingEntry = "";
        try{
            List fieldList = record.getVariableFields(tag);
            if(fieldList.size()>0){
                for(int j=0; j<fieldList.size(); j++){
                    DataField field = (DataField)fieldList.get(j);
                    String data880 = getParticularSubField880Data(tag, 't', field.getSubfield('6').getData(), record);
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
                    if(field.getSubfield('t') != null)
                        linkingEntry += field.getSubfield('t').getData();
                    if(field.getSubfield('g') != null)
                        linkingEntry += " LOCALE_DEPENDENT_IN "+field.getSubfield('g').getData();
                    if(!data880.equals("")){
                        linkingEntry += "<br>"+data880;
                        has880Data = true;
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return linkingEntry;
    }

   public static String getParticularSubField880Data(String fieldTag, char subFieldTag, String subField6Data,
                                               org.marc4j.marc.Record record) {
      String data = "";
      try {
         if (subField6Data != null && !subField6Data.trim().equals("")) {
            subField6Data = subField6Data.substring(0, 6);
            //System.out.println("subField6Data..."+subField6Data);
            StringTokenizer sTField$6 = new StringTokenizer(subField6Data, "-");
            sTField$6.nextToken();
            String field880$6 = fieldTag + "-" + sTField$6.nextToken();
            List field880List = record.getVariableFields("880");
            for (int i = 0; i < field880List.size(); i++) {
               DataField field880 = (DataField) field880List.get(i);
               String subfld880data = field880.getSubfield('6').getData();
               subfld880data = subfld880data.substring(0, 6);
               if (subfld880data.equals(field880$6)) {
                  data += field880.getSubfield(subFieldTag).getData();
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return data;
   }

   public static String getParticular880Data(String fieldTag, String subField6Data, org.marc4j.marc.Record record) {
      String data = "";
      try {
         if (subField6Data != null && !subField6Data.trim().equals("")) {
            //System.out.println("%%%%%% subField6Data:"+subField6Data);
            subField6Data = subField6Data.substring(0, 6);
            //System.out.println("%%%%%% subField6Data:"+subField6Data);
            StringTokenizer sTField$6 = new StringTokenizer(subField6Data, "-");
            sTField$6.nextToken();
            String field880$6 = fieldTag + "-" + sTField$6.nextToken();
            List field880List = record.getVariableFields("880");
            for (int i = 0; i < field880List.size(); i++) {
               DataField field880 = (DataField) field880List.get(i);
               String str = field880.getSubfield('6').getData();

               str = str.substring(0, 6);

               if (str.equals(field880$6)) {
                  if (!data.trim().equals("")) {
                     data += "<br>";
                  }
                  data += field880.getSubfield('a').getData();
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return data;
   }

    public static String verifyXML(String marcxml){
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

   public static String marcToMarcXML(String marc) {
      String xml = "";
      try {

         InputStream input = new ByteArrayInputStream(marc.getBytes("UTF-8"));
         MarcReader reader = new MarcStreamReader(input);
         ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
         MarcWriter writer = new MarcXmlWriter(bos, true);
         while (reader.hasNext()) {
            // System.out.println("6");
            org.marc4j.marc.Record record = reader.next();
            writer.write(record);
         }
         writer.close();
         xml = bos.toString("UTF-8");

      } catch (Exception exp) {
         exp.printStackTrace();
      }

      return xml;
   }
   public static String marcXMLToMarc(String marcxml) {
      String marc = "";

      try {
         InputStreamReader input = new java.io.InputStreamReader(new ByteArrayInputStream(marcxml.getBytes("UTF-8")), "UTF-8");
         org.xml.sax.InputSource is = new org.xml.sax.InputSource(input);
//            java.io.InputStreamReader isr = new java.io.InputStreamReader()
         MarcXmlReader reader = new MarcXmlReader(is);
         ByteArrayOutputStream bos = new ByteArrayOutputStream(500);
         MarcWriter writer = new MarcStreamWriter(bos, "UTF-8");
         while (reader.hasNext()) {
            org.marc4j.marc.Record record = reader.next();

            writer.write(record);

         }
         writer.close();
         marc = bos.toString("UTF-8");
         char chrep = (char) 0;
         marc = marc.replace(chrep, ' ');
         //System.out.println("marc: "+marc);
      } catch (Exception exp) {
         exp.printStackTrace();
      }
      return marc;
   }
}
