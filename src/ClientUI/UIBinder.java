/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientUI;
import FileDownloader.Source_side;
import FileDownloader.TopInterface;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import protocol.chord.filemanager.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import protocol.chord.Identifier;
import protocol.chord.Node;
/**
 *
 * @author WASEEM
 */


public class UIBinder {
    
FileManager BackScanner;

int OLDPORT=6565;
int NEWPORT=4559;
String CONNECTING_IP="";
int SEARCHPORT = 8888;
String MY_IP="";

Socket QuerySocket;

public void init(){        
    BackScanner =new FileManager();
    BackScanner.init(CONNECTING_IP,OLDPORT,MY_IP,NEWPORT);
    BackScanner.setUploadDirectory(null);    
    System.out.println("Connected to Chord");
    QuerySocket =new Socket();
    FileManagerInitProxy();
}

public void setUploadDirectory(String filePath){
    System.out.println("Upload Dir : "+filePath);
    BackScanner.setUploadDirectory(new File(filePath));
    Source_side.directory=filePath+"\\";    
}


public String[][] searchQueryServer(String FileName){
        System.out.println("Sending Search Query to server");
        try {
                QuerySocket=new Socket(CONNECTING_IP,SEARCHPORT);
            
                PrintWriter out = new PrintWriter(QuerySocket.getOutputStream(), true);
                out.println(FileName);
                BufferedReader in = new BufferedReader(new InputStreamReader(QuerySocket.getInputStream()));
                String[][] FileMap=new String[20][2];
                
                String NameHashPair=in.readLine();
                
                for(int i=0;i<FileMap.length && NameHashPair!=null;i++){
                    String read=NameHashPair;
                    FileMap[i]=read.split(",");
                    NameHashPair=in.readLine();
                }
            
            return FileMap;
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(UIBinder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UIBinder.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    
    return null;
}


public ArrayList<InetAddress> getIPAddress(String HashKey){    
    Set<Node> Nodes = BackScanner.whoHasFile(Identifier.toIdentifier(HashKey));
    Iterator iter = Nodes.iterator();
    ArrayList<InetAddress> IP=new ArrayList<InetAddress>(); 
    while(iter.hasNext()){
        Node N = (Node) iter.next();
            try {
                IP.add(InetAddress.getByName(N.getAddress()));
            } catch (UnknownHostException ex) {
                System.out.println("Cannot resolve Hostname . Bad Hostname in chord ");
            }
    }        
    return IP;
}

public void FileManagerInitProxy(){
    Thread FM =new Thread(new FileManagerInit());
    FM.start();
}

public void FileDownloaderProxy(String FileName,ArrayList<InetAddress> IP){
    Thread FM =new Thread(new FileDownloader(FileName, IP));
    FM.start();    
}


 class FileManagerInit implements Runnable {

        @Override
    public void run() {
        System.out.println("FileDownloader Thread Started ");
        TopInterface TI = new TopInterface();
        TI.sourceside();        
    }
}

 class FileDownloader implements Runnable {
    
    String FileName;
    ArrayList<InetAddress> IP;
            
    public FileDownloader(String FileName,ArrayList<InetAddress> IP){
        this.FileName=FileName;
        this.IP=IP;
    }
    
        @Override
    public void run() {
        System.out.println("FileDownloader Thread Started ");
        TopInterface TI = new TopInterface();        
            try {
                TI.clientside(FileName, IP);
            } catch (IOException ex) {
                System.out.println("Failed to initiate download thread");
            }
    }
    
}


public void killall(){
    BackScanner.stop();
    System.exit(1);        
}
 
 

}
