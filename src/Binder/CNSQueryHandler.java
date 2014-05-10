/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Binder;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author WASEEM
 */
public class CNSQueryHandler {

    /**
     * @param args the command line arguments
     */
    private static int PORT=8888;
    private static int BACKLOG=100;    
    private static boolean serverActive=true;
    private static CNSDBManager QueryDB;
    
    public static void main(String[] args) {
        // TODO code application logic here
        
            ServerSocket QueryServer = null;
            
            String line;
            
            //DataInputStream  is;
            PrintWriter os;
            
            Socket ClientSocket = null;
            QueryDB=new CNSDBManager();
            try{
                QueryServer=new ServerSocket(PORT,BACKLOG);
                QueryDB.connect();
            }
            catch(Exception e){
              System.out.println("QueryServer failed to initialize"+e.getMessage());  
            }
            System.out.println("Query Server Active . Yay !!");
            while(serverActive){                
                try {
                    ClientSocket = QueryServer.accept();
                    //is = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
                    //is=new DataInputStream(ClientSocket.getInputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
                    String FileQuery = in.readLine();
                    
                    System.out.println("Query Server Searching . Yay !!");
                   
                    
                    //TODO: Do lazy handling
                    String SQL="SELECT * FROM FILEMAPS WHERE FILENAME LIKE " +"'%"+FileQuery+"%' ORDER BY HashKey";
                    System.out.println("SQL STATEMENT : " + SQL);
                    ResultSet RES = QueryDB.fetchData(SQL);
                    System.out.println("Found Something , Yay !!");                    
                try {
                    ///Parse ResultSet and send to client 
                    System.out.println("Fetched A LOT OF DATA FROM SERVER");
                    os=new PrintWriter(ClientSocket.getOutputStream(),true);
                    while(RES.next()){
                        System.out.println("("+RES.getString(2)+","+RES.getString(3)+")");
                        os.println(RES.getString(2)+","+RES.getString(3));                        
                    }
                    os.flush();
                    os.close();
                    in.close();                    
                    ClientSocket.close();                    
                } catch (SQLException ex) {
                    System.out.println("Error Fetching results" + ex.getMessage());
                }
                    
                    
                    
                    /*Handle Query*/
                }
                catch (IOException ex) {
                    Logger.getLogger(CNSQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }            
            QueryDB.disconnect();            
    }

    
    
    public void  kill(){
        serverActive=false;
    }
    
    
}
