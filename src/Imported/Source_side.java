package Imported;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class Source_side {
	int GlobalPort=2343;
	String directory="";
	
	 class MiniServer extends Thread{

	    private Socket socket = null;

	    public MiniServer(Socket socket) {

	        super("MiniServer");
	        this.socket = socket;

	    }

	    public void run(){
	    	try {
	    		ControlPacketFormation cpf=new ControlPacketFormation();
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				String packet=in.readLine();
				String [] parsed=packet.split(" ");
				SendType typ=SendType.fromInt(Integer.parseInt(parsed[0]));
				switch(typ) {
				case FILE: 
					System.out.println("File Sent at wrong Port");
					return;
				case DATAREQUEST: 
					String filename=parsed[1]; // No spaces in filename.
					int portno=1245; // this needs to be changed.
					String xx=cpf.generateDataResponse(filename,directory,portno);
					if (xx==null)
						return;
					SendRecv.send(socket, xx, SendType.DATARESPONSE,0,0);
					ServerSocket soc=new ServerSocket(portno); //
					socket=soc.accept(); //Data Init
					cpf.StartFileSending(socket,filename);
					break;									
				
				case DATARESPONSE: 
					System.out.println("DataResponse Sent at wrong port");
					return;
				
				case DATAINIT : 
					System.out.println("DataInit Sent at wrong port");
					return;
				
					
				
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            
	    }
	            //implement your methods here

	}	
	

	void source_sideFileDownloader ()throws IOException {

        ServerSocket serverSocket = null;

        boolean listeningSocket = true;
        
        serverSocket = new ServerSocket(GlobalPort);
        

        while(listeningSocket){
            Socket clientSocket = serverSocket.accept();
            MiniServer mini = new MiniServer(clientSocket);
            mini.start();
        }
        serverSocket.close();       
    }
	
	FileReceiveRequestFormat parseDataResponse(String packet)
	{
		String [] zx=packet.split(" ");
		assert(SendType.fromInt(Integer.parseInt(zx[0]))==SendType.DATARESPONSE);
		boolean yes=zx[1].matches("1");
		if (!yes)
			return null;
		int portno=Integer.parseInt(zx[2]);
		int filesize=Integer.parseInt(zx[3]);
		int numAddresses=Integer.parseInt(zx[4]);
		if (numAddresses==0)
			return new FileReceiveRequestFormat(null,filesize,portno);
		InetAddress[] add=new InetAddress[numAddresses];
		
		for(int i=0;i<numAddresses;i++)
		{
			try {
				add[i]=InetAddress.getByName(zx[5+i]);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new FileReceiveRequestFormat(add, filesize,portno);
	}
	// do search in local repository to find out the file.
	
	

}
