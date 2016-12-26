package org.unesco.jisis.wizards.connopen;

import javax.swing.JPanel;
import org.openide.util.NbBundle;

public final class connOpenVisualPanel1 extends JPanel {
    
    /** Creates new form connOpenVisualPanel1 */
    public connOpenVisualPanel1() {
        initComponents();
        lblUsername.setVisible(true);
        lblPassword.setVisible(true);
        txtUsername.setVisible(true);
        txtPassword.setVisible(true);
    }
    
    public String getName() {
        return NbBundle.getMessage(ConnectionOpenWizardAction.class, "CTL_ConnOpenWizardStepOne");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblHostName = new javax.swing.JLabel();
        lblPort = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        txtHostName = new javax.swing.JTextField();
        txtPort = new javax.swing.JTextField();
        txtUsername = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();

        org.openide.awt.Mnemonics.setLocalizedText(lblHostName, org.openide.util.NbBundle.getMessage(connOpenVisualPanel1.class, "connOpenVisualPanel1.lblHostName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblPort, org.openide.util.NbBundle.getMessage(connOpenVisualPanel1.class, "connOpenVisualPanel1.lblPort.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblUsername, org.openide.util.NbBundle.getMessage(connOpenVisualPanel1.class, "connOpenVisualPanel1.lblUsername.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblPassword, org.openide.util.NbBundle.getMessage(connOpenVisualPanel1.class, "connOpenVisualPanel1.lblPassword.text")); // NOI18N

        txtHostName.setText(org.openide.util.NbBundle.getMessage(connOpenVisualPanel1.class, "connOpenVisualPanel1.txtHostName.text")); // NOI18N

        txtPort.setText(org.openide.util.NbBundle.getMessage(connOpenVisualPanel1.class, "connOpenVisualPanel1.txtPort.text")); // NOI18N

        txtUsername.setText(org.openide.util.NbBundle.getMessage(connOpenVisualPanel1.class, "connOpenVisualPanel1.txtUsername.text")); // NOI18N

        txtPassword.setText(org.openide.util.NbBundle.getMessage(connOpenVisualPanel1.class, "connOpenVisualPanel1.txtPassword.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUsername)
                    .addComponent(lblPassword)
                    .addComponent(lblPort)
                    .addComponent(lblHostName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtPassword)
                    .addComponent(txtHostName, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                    .addComponent(txtUsername)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(104, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHostName)
                    .addComponent(txtHostName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPort)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(186, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblHostName;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JTextField txtHostName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPort;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
    
    public String getHostName() {
        return txtHostName.getText();
    }
    
    public String getPort() {
        return txtPort.getText();
    }
    
    public String getUsername() {
        return txtUsername.getText();
    }
    
    public String getPassword() {
        return new String(txtPassword.getPassword());
    }
    
}

