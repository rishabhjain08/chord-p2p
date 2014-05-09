package Imported;

//import java.io.*; 
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;



public class SendRecv {

	static int MTU; // Maximum data present in packet.
	
//Opcode is according to SendType enumeration
	/*String FormPacket_FileTransfer()
	{
		return new String(SendType.FILE.toString());
	}
	*/
	static String FormPacket_DataRequest(String filename)
	{
		String packet=new String(SendType.DATAREQUEST.toString());
		packet.concat(filename);
		return packet;
	}
	static String FormPacket_DataResponse(boolean yes,int portno,int filesize, ArrayList<InetAddress> addres)
	{
		String packet=new String(SendType.DATARESPONSE.toString());
		packet=packet.concat(String.valueOf(yes));
		packet=packet.concat(String.valueOf(portno));
		if(yes)
		{			
			packet=packet+filesize;
			packet=packet.concat(String.valueOf(addres.size()));
			for(int i=0;i<addres.size();i++)
			{
				packet=packet.concat(addres.get(i).toString());
			}
		}
		
		return packet;
	}
	static String FormPacket_DataInit(int OffsetStart, int OffsetEnd,String filename)
	{
		String packet=new String(SendType.DATAINIT.toString());
		packet=packet+OffsetStart;
		packet=packet+OffsetEnd;
		packet=packet.concat(filename);
		
		return packet;
	}
	
	
	static int Filegetdata(StringBuilder data, BufferedReader reader,
			int OffsetStart, int OffsetEnd) {
		if (OffsetStart >= OffsetEnd)
			return -1;
		data.setLength(0);
		int bytesToSend = -1;
		char[] cbuf = new char[(int) (OffsetEnd - OffsetStart + 1)];
		try {
			bytesToSend = reader.read(cbuf, OffsetStart, OffsetEnd
					- OffsetStart + 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data.append(cbuf.toString().substring(0, bytesToSend));
		return bytesToSend;

	}

	static int sendfile(Socket soc, String filename,
			int OffsetStart, int OffsetEnd) {
		StringBuilder data = new StringBuilder();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));

			int sentBytes = 0;
			int curOffsetStart=OffsetStart;
			int curOffsetEnd=OffsetStart+MTU;
			while ((sentBytes = Filegetdata(data, reader, curOffsetStart,
					curOffsetEnd)) != -1) {
				sendcore(soc, data.toString());
				curOffsetStart += sentBytes;
				curOffsetEnd+=sentBytes;
				if(curOffsetEnd>OffsetEnd)
					curOffsetEnd=OffsetEnd;
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	// In case of 
				//FILE -> data is filename
				// DATAREQUEST-> appropriately formatted packet
				// DATARESPONSE-> appropriately formatted packet
				// DATAINIT -> appropriately formatted packet.
	static int send(Socket soc, String data, SendType datatype,
			int OffsetStart, int OffsetEnd) {
		switch (datatype) {
		case FILE:
			return sendfile(soc, data, OffsetStart, OffsetEnd);
			
		case DATAREQUEST:
			return sendcore(soc,data);
			
		case DATARESPONSE:
			return sendcore(soc,data);
			
		case DATAINIT:
			return sendcore(soc,data);			
		default:
			return -1;
		}
		
	}
	/*
	int send(String targetname, int portno, String data, SendType datatype,
			int OffsetStart, int OffsetEnd) {
		try {
			InetAddress target = InetAddress.getByName(targetname);
			return send(target, portno, data, datatype, OffsetStart, OffsetEnd);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	int send(Socket soc, String data, SendType datatype) {
		try {
			InetAddress target = InetAddress.getByName(targetname);
			return send(target, portno, data, datatype);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
*/
	int send(InetAddress target, int portno, String data, SendType datatype) {
		return 1;
	}
	static int sendcore(Socket soc, String packet)
	{

		try {
					
			PrintWriter out=new PrintWriter(soc.getOutputStream(),true);
			out.print(packet);		
		}
			catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}

	static int FileReceive(Socket soc,String filename) throws IOException
	{
		int bytessent=0;
		BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
		BufferedWriter out=new BufferedWriter(new FileWriter(filename));
		String temp=in.readLine();
		do 
		{
			out.write(temp);
			bytessent+=temp.length();
			temp=in.readLine();
		}while(temp!=null);
		// Handle timeouts .. 
		return bytessent;
	}

	
}