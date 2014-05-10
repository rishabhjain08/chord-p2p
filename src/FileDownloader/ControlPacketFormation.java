package FileDownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class ControlPacketFormation {
	
	Socket SendDataRequest(String filename,int portno, InetAddress addr)
	{
		Socket temp;
		try {
			temp = new Socket(addr,portno);
			String data=SendRecv.FormPacket_DataRequest(filename);
			SendRecv.send(temp, data,SendType.DATAREQUEST,0,0);
			return temp;
			
		} catch (IOException e) {
                        System.out.println("****************Socket not formed : SendDataRequest");
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return null;
	}
	// Destination port will be global port.
	//Source port will be the port on which reply will come.

	int SendFileResponse(int PortNo,InetAddress[] addr)
	{
		return 1;
	}
	int DataInitRequest(String filename,int StartOffset,int EndOffset)
	{
		return 1;
	}
	// filesize
	// ip addresses of all nodes which have it.
	FileReceiveRequestFormat parseDataResponse(Socket soc) throws IOException//String packet)
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
		String packet=in.readLine();
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
				add[i]=InetAddress.getByName(zx[5+i].substring(1));// it is because "\" is added while converting inetaddress to string
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new FileReceiveRequestFormat(add, filesize,portno);
	}
	
	String generateDataResponse(String filename, String directory,int portno) throws UnknownHostException
	{
		boolean yes=false;
		
		File file=new File(directory.concat(filename));
		int filesize=0;
		if(file.exists()){
			yes=true;
			filesize=(int) file.length();
			
		}
		
		
		
		ArrayList<InetAddress> addres=new ArrayList<InetAddress>();
		//**********************Testing*******************
		/*
		addres.add(InetAddress.getByName("10.252.221.47"));
		addres.add(InetAddress.getByName("10.252.221.47"));
		addres.add(InetAddress.getByName("10.252.221.47"));
		addres.add(InetAddress.getByName("10.252.221.47"));
		addres.add(InetAddress.getByName("10.252.221.47"));
		addres.add(InetAddress.getByName("10.252.221.47"));
		//***********************Testing****************
		 * 
		 */
		return SendRecv.FormPacket_DataResponse(yes,portno,filesize,addres);
	}	

	void StartFileSending(Socket soc,String filename) throws IOException // at source side
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
		String info=in.readLine();
		String [] param=info.split(" ");
		assert(SendType.fromInt(Integer.parseInt(param[0]))==SendType.DATAINIT);
		int OffsetStart=Integer.parseInt(param[1]);
		int OffsetEnd=Integer.parseInt(param[2]);
		assert(param[3].compareTo(filename)==0);
		SendRecv.send(soc, Source_side.directory.concat(filename),SendType.FILE, OffsetStart, OffsetEnd);
		
	}
}
