/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * UI.java
 *
 * Created on May 8, 2014, 2:03:44 AM
 */
package ClientUI;

import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author WASEEM
 */

public class UI extends javax.swing.JFrame {
    
    UIBinder Binder ;
    /** Creates new form UI */
    public UI() {
        initComponents();
        Binder = new UIBinder();  
        String CONNECTING_IP="10.193.7.35";//JOptionPane.showInputDialog("Enter a connecting IP ADDRESS");        
        Binder.CONNECTING_IP=CONNECTING_IP;
        String OldPort="6565";//JOptionPane.showInputDialog("Enter a connecting port number");
        Binder.OLDPORT=Integer.parseInt(OldPort);        
        String MY_IP="10.193.7.35";//JOptionPane.showInputDialog("Enter your IP");
        Binder.MY_IP=MY_IP;                
        String NewPort="6566";//JOptionPane.showInputDialog("Enter your unique port number");
        Binder.NEWPORT=Integer.parseInt(NewPort);        
        Binder.init();        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        FileInput = new javax.swing.JTextField();
        Search = new javax.swing.JButton();
        Download = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        ResultTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        command_field = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        Options = new javax.swing.JMenu();
        SetUploadDirectory = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");
        jPopupMenu1.add(jMenuItem1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        FileInput.setFont(new java.awt.Font("Comic Sans MS", 3, 12));
        FileInput.setText("Type any filename here :)");
        getContentPane().add(FileInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, 290, 50));

        Search.setText("SEARCH");
        Search.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SearchMouseClicked(evt);
            }
        });
        Search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SearchKeyTyped(evt);
            }
        });
        getContentPane().add(Search, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 40, 100, 50));

        Download.setText("Download");
        Download.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                DownloadMouseClicked(evt);
            }
        });
        getContentPane().add(Download, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 40, 80, 50));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 570, 10));

        ResultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Serial No", "FileName", "SourceIP"
            }
        ));
        ResultTable.setColumnSelectionAllowed(true);
        ResultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ResultTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(ResultTable);
        ResultTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        ResultTable.getColumnModel().getColumn(0).setResizable(false);
        ResultTable.getColumnModel().getColumn(1).setResizable(false);
        ResultTable.getColumnModel().getColumn(2).setResizable(false);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 107, 570, 340));

        jLabel1.setFont(new java.awt.Font("Palatino Linotype", 1, 12)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Connected to Server XXYYZZ");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 0, 190, 30));

        command_field.setText("Command please!!!");
        command_field.setToolTipText("Command please!!!");
        command_field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                command_fieldActionPerformed(evt);
            }
        });
        command_field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                command_fieldKeyReleased(evt);
            }
        });
        getContentPane().add(command_field, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 450, 590, 30));

        Options.setText("Options");

        SetUploadDirectory.setText("SetUploadDirectory");
        SetUploadDirectory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SetUploadDirectoryMouseClicked(evt);
            }
        });
        SetUploadDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetUploadDirectoryActionPerformed(evt);
            }
        });
        Options.add(SetUploadDirectory);

        jMenuBar1.add(Options);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SearchMouseClicked
        // TODO add your handling code here:
        String [][] RES=Binder.searchQueryServer(FileInput.getText());
        System.out.println("FileName,HashName");        
        for (String[] R :RES){
            System.out.println(R[0]+","+R[1]);
        }
        
        DefaultTableModel TM = (DefaultTableModel) this.ResultTable.getModel(); 
        for(int i=0;i<TM.getRowCount();i++)
           TM.removeRow(i);        
        
        for(int i=0;i<RES.length;i++){
            String Data[]=new String[3];
            Data[0]=""+(i+1);
            Data[1]=RES[i][0];
            Data[2]=RES[i][1];   
            if(Data[1]!=null && Data[2]!=null)
                TM.addRow(Data);
        }    
        
    }//GEN-LAST:event_SearchMouseClicked

    private void SearchKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SearchKeyTyped
        // TODO add your handling code here:
        
    }//GEN-LAST:event_SearchKeyTyped

    private void ResultTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ResultTableMouseClicked
        // TODO add your handling code here:
        
        
    }//GEN-LAST:event_ResultTableMouseClicked

    private void SetUploadDirectoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SetUploadDirectoryMouseClicked
        // TODO add your handling code here:
        System.out.println("File Dialog pops up");
        JFileChooser FileChooser =new JFileChooser();
        int returnVal = FileChooser.showOpenDialog(null);        
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: ");
                    
            Binder.setUploadDirectory(FileChooser.getSelectedFile().getName());
        }
    }//GEN-LAST:event_SetUploadDirectoryMouseClicked

    private void SetUploadDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetUploadDirectoryActionPerformed
        // TODO add your handling code here:
        System.out.println("File Dialog pops up");
        JFileChooser FileChooser =new JFileChooser();
        FileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = FileChooser.showOpenDialog(this);        
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: ");                    
            Binder.setUploadDirectory(FileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_SetUploadDirectoryActionPerformed

    private void DownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_DownloadMouseClicked
        // TODO add your handling code here:
        if(this.ResultTable.getSelectedRow()!=-1){
            String FileName="";
            String HashKey = "";            
            FileName = (String) this.ResultTable.getValueAt(this.ResultTable.getSelectedRow(), 1);
            HashKey = (String) this.ResultTable.getValueAt(this.ResultTable.getSelectedRow(), 2);
            ArrayList<InetAddress> IP =  Binder.getIPAddress(HashKey);            
            
            System.out.println(" FILE DOWNLOADING STARTED ");
            System.out.println(" FILE NAME : "+FileName);
            for (InetAddress I : IP){
                System.out.println(" IP ADDRESS : "+I.toString());                
            }
            
            Binder.FileDownloaderProxy(FileName, IP);
            
        }        
    }//GEN-LAST:event_DownloadMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        Binder.killall();
    }//GEN-LAST:event_formWindowClosing

private void command_fieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_command_fieldActionPerformed
//    command_field
}//GEN-LAST:event_command_fieldActionPerformed

private void command_fieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_command_fieldKeyReleased
    if (evt.getKeyChar() == KeyEvent.VK_ENTER)
    {
            try {
                Binder.BackScanner.execute(command_field.getText());
            } catch (Exception ex) {
                try {
                    Binder.BackScanner.getChordNode().execute(command_field.getText());
                } catch (Exception ex1) {
                    Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
    }
}//GEN-LAST:event_command_fieldKeyReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {                
                new UI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Download;
    private javax.swing.JTextField FileInput;
    private javax.swing.JMenu Options;
    private javax.swing.JTable ResultTable;
    private javax.swing.JButton Search;
    private javax.swing.JMenuItem SetUploadDirectory;
    private javax.swing.JTextField command_field;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
