/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author WASEEM
 */
public class TestQueryServer {

    /**
     * @param args the command line arguments
     */
    static int SEARCHPORT = 8888;
    static Socket QuerySocket;

    public static void main(String[] args) {
        try {
            // TODO code application logic here
            QuerySocket=new Socket("localhost",SEARCHPORT);
            
            PrintWriter out = new PrintWriter(QuerySocket.getOutputStream(), true);
                out.println("psycho");
                BufferedReader in = new BufferedReader(new InputStreamReader(QuerySocket.getInputStream()));
                String[][] FileMap=new String[20][2];
                
                String NameHashPair=in.readLine();
                
                for(int i=0;i<FileMap.length && NameHashPair!=null;i++){
                    String read=NameHashPair;
                    FileMap[i]=read.split(",");
                    NameHashPair=in.readLine();
                }
                
        } catch (UnknownHostException ex) {
            Logger.getLogger(TestQueryServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestQueryServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
