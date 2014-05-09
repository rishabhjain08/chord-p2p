/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Binder;


import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JOptionPane;
import protocol.chord.Identifier;
import protocol.chord.Node;
import protocol.chord.filemanager.FileManagerIF;
import protocol.chord.filemanager.FileManager;

/*
 *
 * @author WASEEM
 */

public class CentralNameServer {

    /*
     * @param args the command line arguments
     */
    
    private static int OLDPORT=-1;
    private static int NEWPORT=6565;
    private static boolean ActiveCNS=true;
            
    public static void main(String[] args) throws SQLException {
        // TODO code application logic here
        
        FileManager BackScanner = new FileManager();
        String MY_IP="10.193.7.35";//JOptionPane.showInputDialog("Enter your IP");
        String MY_PORT="6565";//JOptionPane.showInputDialog("Enter your Port");
        NEWPORT=Integer.parseInt(MY_PORT);
        BackScanner.init(null,OLDPORT,MY_IP,NEWPORT);
        BackScanner.setUploadDirectory(null);//No uploads from Name Server
        CNSDBManager DBM=new CNSDBManager();
        
        if(DBM.connect()){
            Node protocolNode=BackScanner.getChordNode();                
            Node succ = protocolNode.getSuccessor();            
            while(true){     
            if(succ!=null && ActiveCNS){
                
                System.out.println("Finally I am not lonely !!");
                while(ActiveCNS){      
                    try{
                        FileManagerIF succFM =(FileManagerIF) succ.getRemoteServiceInterface(protocol.chord.filemanager.FileManagerIF.class);
                        Map<String, HashSet<Node>> KEY = succFM.getAllKeys();
                        System.out.println("**************Fetched Something + "+KEY.size());
                        
                        for(Map.Entry<String,HashSet<Node>> entry : KEY.entrySet()){
                            //System.out.println("delete previous entries");
                            //DBM.Execute("DELETE FROM "+DBM.MainTable+" WHERE HashKey = "+entry.getKey());

                            Iterator I = entry.getValue().iterator();

                            while(I.hasNext()){
                                Node N  = (Node) I.next();
                                FileManagerIF F = (FileManagerIF) N.getRemoteServiceInterface(protocol.chord.filemanager.FileManagerIF.class);
                                String SQL="INSERT INTO "+ DBM.MainTable + "(FileName,HashKey)"+" VALUES( '"+F.getFileName(Identifier.toIdentifier( entry.getKey()))+"','"+entry.getKey() + "')";
                                System.out.println("SQL QUERY : : "+SQL);
                                DBM.Execute(SQL);
                            }
                        }
                        
                    
                    
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                                System.out.println("Can't sleep");                            
                        }
                }
                catch (Exception e){
                    System.out.println("Caught a bad one today ! "+e.getMessage());
                    succ = protocolNode;
                }
                    
                                        //Doubt : Should I use Remote Interface Here                     
                    while(true){    
                    System.out.println("my succ for node " + succ + " is ?");
                    //+ (nextSucc == null ? "null" : nextSucc));    
                    Node nextSucc=succ.findSuccessor(succ.getIdentifier());
                    System.out.println("my next succ for node " + nextSucc + " is ?");                    
                    if(nextSucc!=null && !succ.equals(nextSucc)){
                        succ=nextSucc;
                        break;
                    }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                System.out.println("Can't sleep");
                            }
                    }

                } 
            }
            else{
                    try {                       
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        System.out.println("Can't Sleep");
                    }
            }                
            succ = protocolNode.getSuccessor();
        }
        }
        else{
            System.out.println("Can't initiate CNS due to non-responsive DB");
        }
        
    }
    
    public void kill(){
        ActiveCNS=false;        
    }
    
    
}
