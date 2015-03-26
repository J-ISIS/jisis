/*
 * Z3950ServerDialog.java
 */

package org.unesco.jisis.z3950;

import java.awt.BorderLayout;
import java.io.File;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.unesco.jisis.corelib.common.Global;



public class Z3950ServerDialog extends javax.swing.JDialog {
    Z3950ServerPanel panel=null;
    XMLUtility xmlUtility=null;
    Utilities utilities=null;
    String operation="";
    ArrayList catVect,locVect;
    /** Creates new form Z3950ServerDialog */
    public Z3950ServerDialog(java.awt.Frame parent,String operation,String title,ArrayList catVect,ArrayList locVect) {
        super(parent);
        initComponents();
        this.catVect=catVect;
        this.locVect=locVect;
        jPanel1.setLayout(new BorderLayout());
        panel=new Z3950ServerPanel(catVect,locVect);
        jPanel1.add(panel, BorderLayout.CENTER);
        xmlUtility=XMLUtility.getInstance();
        utilities=Utilities.getInstance();
        this.operation=operation;
        this.setTitle(title);
        this.setModal(true);
        //this.setSize(400,300);
        this.setLocation(utilities.getScreenLocation(this.getSize()));
    }
    public Z3950ServerDialog(javax.swing.JDialog parent,String operation,String title,ArrayList catVect,ArrayList locVect) {
        super(parent);
        initComponents();
         this.catVect=catVect;
        this.locVect=locVect;
        jPanel1.setLayout(new BorderLayout());
        panel=new Z3950ServerPanel(catVect,locVect);
        jPanel1.add(panel, BorderLayout.CENTER);
        this.operation=operation;
        this.setTitle(title);
        this.setModal(true);
        //this.setSize(400,300);
        this.setLocation(utilities.getScreenLocation(this.getSize()));
    }
    public void saveServerDetails()
    {
        try
        {
            String fileName=Global.getClientWorkPath();
            fileName=fileName.concat("/Z3950Servers.xml");
            File inFile = new File(fileName);
            if(inFile.exists())
            {
                FileInputStream fis = new FileInputStream(inFile);
//                FileChannel inChannel = fis.getChannel();
//                ByteBuffer buf = ByteBuffer.allocate((int)inChannel.size());
//                inChannel.read(buf);
//                inChannel.close();
//                String myString = new String(buf.array());
//                System.out.println("mystring:"+myString);
////                File outFile = new File(fileName);
////                FileWriter out = new FileWriter(outFile);
                SAXBuilder sb = new SAXBuilder();
                Document doc = null;
                try {
                    doc = sb.build(fis);
                } catch (Exception e) {
                    
                }
                org.jdom.Element root=doc.getRootElement();

//                System.out.println("root:"+root);
                org.jdom.Element elepan = (org.jdom.Element)panel.getServerDetails().clone();
                root.addContent(elepan);
//                System.out.println("panel:"+(new XMLOutputter()).outputString(elepan));
                XMLOutputter xout = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                xout.output(new Document((Element)root.clone()),new FileOutputStream(inFile));
            }
            else
            {
                org.jdom.Element root=new org.jdom.Element("ZServers");
                org.jdom.Element elepan = (org.jdom.Element)panel.getServerDetails().clone();
                root.addContent(elepan);
                XMLOutputter xout = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                xout.output(new Document((Element)root.clone()),new FileOutputStream(inFile));
//                System.out.println("######### file not exsits ##########");
            }
            
            
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    public void setServerDetails(String server)
    {
        panel.setServerDetails(server);
    }
            
    public void saveEditedServerDetails()
    {
         try
        {
            String fileName=Global.getClientWorkPath();
            fileName=fileName.concat("/Z3950Servers.xml");
            File inFile = new File(fileName);
            if(inFile.exists())
            {
                FileInputStream fis = new FileInputStream(inFile);
                SAXBuilder sb = new SAXBuilder();
                Document doc = null;
                try {
                    doc = sb.build(fis);
                } catch (Exception e) {
                    
                }
                org.jdom.Element root=doc.getRootElement();
                org.jdom.Element newRoot=new org.jdom.Element("ZServers");
                org.jdom.Element newEle=panel.getServerDetails();
                java.util.List childList=root.getChildren();
                String oldName=panel.oldName;
                for(int i=0;i<childList.size();i++)
                {
                    org.jdom.Element child=(org.jdom.Element)childList.get(i);
                    String name=child.getChildText("Name");
                    if(name.equalsIgnoreCase(oldName))
                    {
                        newRoot.addContent((org.jdom.Element)newEle.clone());
                    }
                    else
                    {
                        newRoot.addContent((org.jdom.Element)child.clone());
                    }
                }
                XMLOutputter xout = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                xout.output(new Document((Element)newRoot.clone()),new FileOutputStream(inFile));
            }
            
            
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public boolean validationCheck()
    {
        return panel.validationCheck();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jPanel1 = new javax.swing.JPanel();
      jPanel2 = new javax.swing.JPanel();
      btnOk = new javax.swing.JButton();
      btnHelp = new javax.swing.JButton();
      btnCancel = new javax.swing.JButton();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

      jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(0, 483, Short.MAX_VALUE)
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(0, 631, Short.MAX_VALUE)
      );

      jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      jPanel2.setPreferredSize(new java.awt.Dimension(100, 40));

      btnOk.setMnemonic('o');
      btnOk.setText("Ok");
      btnOk.setToolTipText("Ok");
      btnOk.setPreferredSize(new java.awt.Dimension(75, 26));
      btnOk.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnOkActionPerformed(evt);
         }
      });

      btnHelp.setMnemonic('h');
      btnHelp.setText("HELP");
      btnHelp.setToolTipText("Help");
      btnHelp.setPreferredSize(new java.awt.Dimension(75, 26));

      btnCancel.setMnemonic('c');
      btnCancel.setText("Cancel");
      btnCancel.setToolTipText("Cancel");
      btnCancel.setPreferredSize(new java.awt.Dimension(75, 26));
      btnCancel.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnCancelActionPerformed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .add(124, 124, 124)
            .add(btnOk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(5, 5, 5)
            .add(btnHelp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(5, 5, 5)
            .add(btnCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel2Layout.createSequentialGroup()
            .add(5, 5, 5)
            .add(btnOk, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         .add(jPanel2Layout.createSequentialGroup()
            .add(5, 5, 5)
            .add(btnHelp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         .add(jPanel2Layout.createSequentialGroup()
            .add(5, 5, 5)
            .add(btnCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
      );

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
         .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 487, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
// TODO add your handling code here:
        if(this.operation.equals("Add"))
        {
           if(validationCheck())
           {
               saveServerDetails();
               this.setVisible(false);
           }
           else
           {
               javax.swing.JOptionPane pane=new javax.swing.JOptionPane();
               pane.showMessageDialog(jPanel1,"Please Enter all fields(Port should be integer)");
           }
        }
        if(this.operation.equals("Modify"))
        {
            if(validationCheck())
            {
                saveEditedServerDetails();
                this.setVisible(false);
            }
            else
            {
               javax.swing.JOptionPane pane=new javax.swing.JOptionPane();
               pane.showMessageDialog(jPanel1,"Please Enter all fields(Port should be integer)");
            }
        }
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
// TODO add your handling code here:
        this.setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed
    
    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new Z3950ServerDialog(new javax.swing.JFrame(), true).setVisible(true);
//            }
//        });
//    }
    
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton btnCancel;
   private javax.swing.JButton btnHelp;
   private javax.swing.JButton btnOk;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   // End of variables declaration//GEN-END:variables
    
}
