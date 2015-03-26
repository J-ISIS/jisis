/*
 * Z3950ServerPanel.java
 */

package org.unesco.jisis.z3950;

import java.util.*;

public class Z3950ServerPanel extends javax.swing.JPanel {

   XMLUtility xmlUtility = null;
   String oldName = "";
   ArrayList catVect, locVect;

   /** Creates new form Z3950ServerPanel */
   public Z3950ServerPanel(ArrayList catVect, ArrayList locVect) {
      initComponents();
      this.catVect = catVect;
      this.locVect = locVect;
      xmlUtility = XMLUtility.getInstance();
      setItems();
   }

   private void setItems() {
      ArrayList categories = new ArrayList();
      ArrayList locations = new ArrayList();
      int flag = 0, i, j;

      for (i = 0; i < catVect.size(); i++) {
         flag = 0;
         for (j = 0; j < categories.size(); j++) {
            if (catVect.get(i).equals(categories.get(j))) {
               flag = 1;
            }
         }
         if (flag == 0) {
            categories.add(catVect.get(i));
         }
      }


      for (i = 0; i < categories.size(); i++) {
         cmbCategory.addItem(categories.get(i));
      }

      for (i = 0; i < locVect.size(); i++) {
         flag = 0;
         for (j = 0; j < locations.size(); j++) {
            if (locVect.get(i).equals(locations.get(j))) {
               flag = 1;
            }
         }
         if (flag == 0) {
            locations.add(locVect.get(i));
         }
      }
      for (i = 0; i < locations.size(); i++) {
         cmbLocation.addItem(locations.get(i));
      }

      cmbRecordType.addItem("marc21");
      cmbRecordType.addItem("unimarc");
      cmbRecordType.addItem("opac");
      cmbRecordType.addItem("sutrs");
      cmbRecordType.addItem("xml");

      cmbElementSetName.addItem(" ");
      cmbElementSetName.addItem("b");
      cmbElementSetName.addItem("B");
      cmbElementSetName.addItem("dc");
      cmbElementSetName.addItem("f");
      cmbElementSetName.addItem("F");
      cmbElementSetName.addItem("marcxml");
      cmbElementSetName.addItem("mods");

      cmbType.addItem("Z3950");
      cmbType.addItem("SRU/W");

      cmbEncoding.addItem("UTF-8 (UNICODE)");
      cmbEncoding.addItem("MARC-8 (MARC21)");

      cmbEncoding.addItem("ISO-5426 (UNIMARC)");
      cmbEncoding.addItem("ISO-6937 (UNIMARC)");
      cmbEncoding.addItem("ISO-8859-1 (Latin 1)");



   }

   public org.jdom.Element getServerDetails() {
      org.jdom.Element root = new org.jdom.Element("ZServer");
      org.jdom.Element name = new org.jdom.Element("Name");
      name.setText(txtName.getText());
      root.addContent(name);
      org.jdom.Element category = new org.jdom.Element("Category");
      category.setText(cmbCategory.getSelectedItem().toString());
      root.addContent(category);
      org.jdom.Element type = new org.jdom.Element("Type");
      type.setText(cmbType.getSelectedItem().toString());
      root.addContent(type);
      org.jdom.Element zurl = new org.jdom.Element("Zurl");
      org.jdom.Element baseurl = new org.jdom.Element("BaseURL");
      baseurl.setText(txtBaseUrl.getText());
      zurl.addContent(baseurl);
      org.jdom.Element port = new org.jdom.Element("Port");
      port.setText(txtPort.getText());
      zurl.addContent(port);
      org.jdom.Element database = new org.jdom.Element("DataBase");
      database.setText(txtDatabase.getText());
      zurl.addContent(database);
      org.jdom.Element recordType = new org.jdom.Element("RecordType");
      recordType.setText(cmbRecordType.getSelectedItem().toString());
      zurl.addContent(recordType);
      org.jdom.Element encoding = new org.jdom.Element("Encoding");
      encoding.setText(cmbEncoding.getSelectedItem().toString());
      zurl.addContent(encoding);
      org.jdom.Element elementSetName = new org.jdom.Element("ElementSetName");
      elementSetName.setText(cmbElementSetName.getSelectedItem().toString());
      zurl.addContent(elementSetName);
      org.jdom.Element userName = new org.jdom.Element("UserName");
      userName.setText(txtUserName.getText());
      zurl.addContent(userName);
      org.jdom.Element password = new org.jdom.Element("Password");
      password.setText(txtPassword.getText());
      zurl.addContent(password);
      
      root.addContent(zurl);

      org.jdom.Element location = new org.jdom.Element("Location");
      location.setText(cmbLocation.getSelectedItem().toString());
      root.addContent(location);
      org.jdom.Element syntax = new org.jdom.Element("Syntax");
      syntax.setText("XML");
      root.addContent(syntax);
      org.jdom.Element active = new org.jdom.Element("Active");
      active.setText("YES");
      root.addContent(active);
      return root;

   }

   public void setServerDetails(String server) {

      org.jdom.Element root = xmlUtility.getRootElementFromXML(server);
      txtName.setText(root.getChildText("Name"));
      oldName = root.getChildText("Name");
      cmbCategory.setSelectedItem(root.getChildText("Category"));
      cmbType.setSelectedItem(root.getChildText("Type"));
      org.jdom.Element zurl = root.getChild("Zurl");
      txtBaseUrl.setText(zurl.getChildText("BaseURL"));
      txtPort.setText(zurl.getChildText("Port"));
      cmbRecordType.setSelectedItem(zurl.getChildText("RecordType"));
      cmbElementSetName.setSelectedItem(zurl.getChildText("ElementSetName"));
      txtDatabase.setText(zurl.getChildText("DataBase"));
      cmbEncoding.setSelectedItem(zurl.getChildText("Encoding"));
      txtUserName.setText(zurl.getChildText("UserName"));
      txtPassword.setText(zurl.getChildText("Password"));
      cmbLocation.setSelectedItem(root.getChildText("Location"));

   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jPanel1 = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();
      txtName = new javax.swing.JTextField();
      jLabel2 = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      txtBaseUrl = new javax.swing.JTextField();
      jLabel5 = new javax.swing.JLabel();
      txtPort = new javax.swing.JTextField();
      jLabel6 = new javax.swing.JLabel();
      txtDatabase = new javax.swing.JTextField();
      jLabel7 = new javax.swing.JLabel();
      cmbCategory = new javax.swing.JComboBox();
      cmbType = new javax.swing.JComboBox();
      cmbLocation = new javax.swing.JComboBox();
      jLabel8 = new javax.swing.JLabel();
      txtUserName = new javax.swing.JTextField();
      jLabel9 = new javax.swing.JLabel();
      txtPassword = new javax.swing.JTextField();
      jSeparator1 = new javax.swing.JSeparator();
      jLabel10 = new javax.swing.JLabel();
      cmbRecordType = new javax.swing.JComboBox();
      jLabel11 = new javax.swing.JLabel();
      cmbElementSetName = new javax.swing.JComboBox();
      jSeparator2 = new javax.swing.JSeparator();
      jLabel12 = new javax.swing.JLabel();
      cmbEncoding = new javax.swing.JComboBox();

      setLayout(new java.awt.BorderLayout());

      jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

      jLabel1.setText("Name:");

      txtName.setColumns(15);

      jLabel2.setText("Category:");

      jLabel3.setText("Type:");

      jLabel4.setText("Base Z39.50 URL:");

      txtBaseUrl.setColumns(15);

      jLabel5.setText("Port:");

      txtPort.setColumns(15);
      txtPort.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            txtPortFocusLost(evt);
         }
      });

      jLabel6.setText("Database:");

      txtDatabase.setColumns(15);

      jLabel7.setText("Location:");

      cmbCategory.setEditable(true);

      cmbLocation.setEditable(true);

      jLabel8.setText("User Name:");

      jLabel9.setText("Password:");

      jLabel10.setText("Record Type:");

      jLabel11.setText("Element Set Name:");

      jLabel12.setText("Encoding:");

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(54, 54, 54)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel1Layout.createSequentialGroup()
                  .add(36, 36, 36)
                  .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(jLabel8)
                     .add(jLabel9))
                  .add(18, 18, 18)
                  .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, txtUserName)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, txtPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 139, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
               .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                  .add(jPanel1Layout.createSequentialGroup()
                     .add(5, 5, 5)
                     .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(jLabel4)
                        .add(jLabel10)
                        .add(jLabel11))
                     .add(18, 18, 18)
                     .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(txtBaseUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 223, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(cmbElementSetName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(cmbRecordType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                  .add(jPanel1Layout.createSequentialGroup()
                     .add(48, 48, 48)
                     .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1))
                     .add(18, 18, 18)
                     .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(cmbType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 173, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(cmbCategory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(cmbLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)))
            .add(77, 77, 77))
         .add(jPanel1Layout.createSequentialGroup()
            .add(100, 100, 100)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(jLabel5)
               .add(jLabel6)
               .add(jLabel12))
            .add(18, 18, 18)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
               .add(txtPort)
               .add(txtDatabase)
               .add(cmbEncoding, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(236, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(20, 20, 20)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel1))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(cmbType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel3))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(cmbCategory, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel2))
            .add(4, 4, 4)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(cmbLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel7))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(txtBaseUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel4))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(txtPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel5))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(txtDatabase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel6))
            .add(14, 14, 14)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(cmbEncoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel12))
            .add(18, 18, 18)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(cmbRecordType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .add(jLabel10))
            .add(18, 18, 18)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel11)
               .add(cmbElementSetName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
            .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(10, 10, 10)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(jLabel8)
               .add(txtUserName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(18, 18, 18)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
               .add(jLabel9)
               .add(txtPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(56, 56, 56))
      );

      add(jPanel1, java.awt.BorderLayout.LINE_START);
   }// </editor-fold>//GEN-END:initComponents

    private void txtPortFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPortFocusLost
// TODO add your handling code here:
       javax.swing.JOptionPane pane = new javax.swing.JOptionPane();

    }//GEN-LAST:event_txtPortFocusLost

   public boolean portValidation() {
      int flag = 0;
      String st = txtPort.getText();
      int len = st.length();
      if (len != 0) {
         for (int i = 0; i < len; i++) {
            char ch = st.charAt(i);
            if (ch >= '0' && ch <= '9') {
               flag++;
            }
         }
         if (flag != len) {
            return false;
         } else {
            return true;
         }

      } else {
         return false;
      }
   }

   public boolean validationCheck() {
      if (portValidation()) {
         if (txtName.getText().equals("") || cmbCategory.getSelectedItem().equals("")
                 || cmbType.getSelectedItem().equals("")
                 || txtBaseUrl.getText().equals("")
                 || txtPort.getText().equals("")
                 || txtDatabase.getText().equals("")
                 || cmbLocation.getSelectedItem().equals("")) {
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox cmbCategory;
   private javax.swing.JComboBox cmbElementSetName;
   private javax.swing.JComboBox cmbEncoding;
   private javax.swing.JComboBox cmbLocation;
   private javax.swing.JComboBox cmbRecordType;
   private javax.swing.JComboBox cmbType;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel10;
   private javax.swing.JLabel jLabel11;
   private javax.swing.JLabel jLabel12;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JLabel jLabel6;
   private javax.swing.JLabel jLabel7;
   private javax.swing.JLabel jLabel8;
   private javax.swing.JLabel jLabel9;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JSeparator jSeparator1;
   private javax.swing.JSeparator jSeparator2;
   private javax.swing.JTextField txtBaseUrl;
   private javax.swing.JTextField txtDatabase;
   private javax.swing.JTextField txtName;
   private javax.swing.JTextField txtPassword;
   private javax.swing.JTextField txtPort;
   private javax.swing.JTextField txtUserName;
   // End of variables declaration//GEN-END:variables
}
