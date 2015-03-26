/*
 * NGLUtilities.java
 *
 * Created on October 10, 2007, 6:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.unesco.jisis.z3950;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author root
 */
public class Utilities {

   private final static Utilities instance = new Utilities();
   private String userId = "1";
   private String libraryId = "1";
   private String libraryName = "";
   private String serverIp = "";
   private String serverPort = "";
   private java.util.Date nGLDate = new java.util.Date();
   private java.util.Locale nGLlocale = null;
   private int catalogueRecordAutoSavetime = 50000;
   private Frame NGLFrame = null;
   private Hashtable htLibIds = null;
   private Hashtable htLibNames = null;

   /** Make the class not instantiable */
   private Utilities() {

      this.nGLlocale = Locale.getDefault();
      htLibIds = new Hashtable();
      htLibNames = new Hashtable();
   }

   public static Utilities getInstance() {
      return instance;
   }

   public Point getScreenLocation(Dimension size) {
      double width = size.getWidth();
      double height = size.getHeight();

      Dimension dsys = Toolkit.getDefaultToolkit().getScreenSize();
      double syswidth = dsys.getWidth();
      double sysheight = dsys.getHeight();

      java.awt.Dimension dimen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
      double swidth = dimen.getWidth();
      double sheight = dimen.getHeight();
      double px = (swidth - width) / 2;
      double py = (sheight - height) / 2;

      java.awt.Point pret = new java.awt.Point((int) px, (int) py);
      return pret;
   }

   public String getClientRoot() {
      String path = "";
      try {

         String os = System.getProperty("os.name");
         System.out.println("Os name Identified");
         if (os.toUpperCase().indexOf("WINDOWS") != -1) {
            path = "C:";
         } else if (os.toUpperCase().indexOf("LINUX") != -1) {
            path = "/usr";
         } else {

            path = "C:";
         }


      } catch (Exception e) {
         e.printStackTrace();
      }


      return path;


   }

   public String getDefaultFolderPath() {
      String path = "";
      try {

         String os = System.getProperty("os.name");
         System.out.println("Os name Identified");
         if (os.toUpperCase().indexOf("WINDOWS") != -1) {
            java.io.File file = new java.io.File("C:/MARCDictionary");
            if (!file.exists()) {
               file.mkdir();
            }

            path = "C:/MARCDictionary";
         } else if (os.toUpperCase().indexOf("LINUX") != -1) {
            java.io.File file = new java.io.File("/usr/MARCDictionary");
            if (!file.exists()) {
               file.mkdir();
            }

            path = "/usr/MARCDictionary";
         } else {
            java.io.File file = new java.io.File("C:/MARCDictionary");
            if (!file.exists()) {
               file.mkdir();
            }
            path = "C:/MARCDictionary";
         }


      } catch (Exception e) {
         e.printStackTrace();
      }


      return path;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getLibraryId() {
      return libraryId;
   }

   public void setLibraryId(String libraryId) {
      this.libraryId = libraryId;
   }

   public String getLibraryName() {
      return libraryName;
   }

   public void setLibraryName(String libraryName) {
      this.libraryName = libraryName;
   }

   public String getServerIp() {
      return serverIp;
   }

   public void setServerIp(String serverIp) {
      this.serverIp = serverIp;
   }

   public String getServerPort() {
      return serverPort;
   }

   public void setServerPort(String serverPort) {
      this.serverPort = serverPort;
   }

   public java.util.Locale getNGLlocale() {
      return nGLlocale;
   }

   public void setNGLlocale(java.util.Locale nGLlocale) {
      this.nGLlocale = nGLlocale;
   }

   public java.util.Date getNGLDate() {
      return nGLDate;
   }

   public void setNGLDate(java.util.Date nGLDate) {
      this.nGLDate = nGLDate;
   }

   public Frame getNGLFrame() {
      return NGLFrame;
   }

   public void setNGLFrame(Frame NGLFrame) {
      this.NGLFrame = NGLFrame;
   }

   public String getLibraryId(String libraryName) {
      String libId = "";

      if (htLibIds != null) {
         libId = htLibIds.get(libraryName).toString();
      }

      return libId;
   }

   public String getLibraryName(String libraryId) {
      String libName = "";

      if (htLibNames != null) {
         libName = htLibNames.get(libraryId).toString();
      }

      return libName;
   }

   public java.util.ArrayList getLibraryNames() {
      java.util.ArrayList list = new ArrayList();

      if (htLibNames != null) {
         list.addAll(htLibNames.values());
      }

      return list;
   }

   public java.util.ArrayList getLibraryIds() {
      java.util.ArrayList list = new ArrayList();
      list.add("Lib1");
      list.add("Lib2");
      list.add("Lib3");
      if (htLibIds != null) {
         list.addAll(htLibIds.values());
      }

      return list;
   }
//   public void getAllLibraries(){
//     org.jdom.Element root=org.verus.ngl.utilities.NGLXMLUtility.getInstance().getRequestDataRoot("LoginHandler","1");
//     String xmlReq=org.verus.ngl.utilities.NGLXMLUtility.getInstance().generateXML(root);
//     String xmlResponse = org.verus.ngl.client.components.ServletConnector.getInstance().sendRequest(xmlReq);
//     System.out.println("The xml response is "+xmlResponse);
//     org.jdom.Element response = org.verus.ngl.utilities.NGLXMLUtility.getInstance().getRootElementFromXML(xmlResponse);
//     List listLib = response.getChildren("Library");
//     for(int i=0 ; i< listLib.size() ; i++){
//         org.jdom.Element singleLib = (org.jdom.Element)listLib.get(i);
//         String name = singleLib.getChildText("Name");
//         String id = singleLib.getChildText("Id");
//         htLibIds.put(name,id);
//         htLibNames.put(id,name);
//     }
//     
//   } 

   public List<String> getAllLibraryNames() {
      List<String> vecLibNames = new ArrayList<String>();
      vecLibNames.addAll(this.htLibNames.values());
      return vecLibNames;
   }

   public List<String> getAllLibraryIds() {
      List<String> vecLibIds = new ArrayList<String>();
      vecLibIds.addAll(this.htLibIds.values());
      return vecLibIds;
   }

   public int getCatalogueRecordAutoSavetime() {
      return catalogueRecordAutoSavetime;
   }

   public void setCatalogueRecordAutoSavetime(int catalogueRecordAutoSavetime) {
      this.catalogueRecordAutoSavetime = catalogueRecordAutoSavetime;
   }
}
