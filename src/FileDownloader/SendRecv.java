package FileDownloader;

//import java.io.*; 
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.*;
import java.util.ArrayList;



public class SendRecv {

	static int MTU=1300; // Maximum data present in packet.
	
//Opcode is according to SendType enumeration
	/*String FormPacket_FileTransfer()
	{
		return new String(SendType.FILE.toString());
	}
	*/
	static String FormPacket_DataRequest(String filename)
	{
		String packet=new String(SendType.DATAREQUEST.ordinal()+" ");
		packet=packet.concat(filename + "\n");
		
		return packet;
	}
	static String FormPacket_DataResponse(boolean yes,int portno,int filesize, ArrayList<InetAddress> addres)
	{
		String packet=new String(SendType.DATARESPONSE.ordinal()+" ");
		int tru=yes?1:0;
		packet=packet.concat(tru+" ");
		packet=packet.concat(String.valueOf(portno));
		if(yes)
		{			
			packet=packet+" "+filesize;
			packet=packet.concat(" "+String.valueOf(addres.size()));
			for(int i=0;i<addres.size();i++)
			{
				packet=packet.concat(" "+addres.get(i).toString());
			}
		}
		packet=packet+"\n";
		
		return packet;
	}
	static String FormPacket_DataInit(int OffsetStart, int OffsetEnd,String filename)
	{
		String packet=new String(SendType.DATAINIT.ordinal()+"");
		packet=packet+" "+OffsetStart;
		packet=packet+" "+ OffsetEnd;
		packet=packet.concat(" "+ filename+ "\n");
		
		return packet;
	}
	
	
	static int Filegetdata(StringBuilder data, BufferedReader reader,
			int OffsetStart, int OffsetEnd) {
		if (OffsetStart > OffsetEnd)
			return -1;
		data.setLength(0);
		int bytesToSend = -1;
		char[] cbuf = new char[(int) (OffsetEnd - OffsetStart + 1)];
		try {
			bytesToSend = reader.read(cbuf, 0, OffsetEnd
					- OffsetStart + 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		data=data.append(cbuf);
		return bytesToSend;

	}

	static int sendfile(Socket soc, String filename,
			int OffsetStart, int OffsetEnd) throws IOException {
		
		try {
			FileInputStream fis=new FileInputStream(filename);
			fis.getChannel().position(OffsetStart);
			//reader = new BufferedReader(new InputStreamReader(fis));
			

			int sentBytes = 0;
			int curOffsetStart=OffsetStart;
			byte []container=new byte[MTU];
			int curOffsetEnd=MTU<OffsetEnd-OffsetStart+1?OffsetStart+MTU-1:OffsetEnd;
			while ((sentBytes = fis.read(container,0, curOffsetEnd-curOffsetStart+1)) != -1) {
				sendcore(soc, container, curOffsetEnd-curOffsetStart+1);
				curOffsetStart += sentBytes;
				curOffsetEnd+=sentBytes;
				if(curOffsetEnd>OffsetEnd)
					curOffsetEnd=OffsetEnd;
				if(curOffsetStart>OffsetEnd)
					return 1;
			}
			fis.close();
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
			int OffsetStart, int OffsetEnd) throws IOException {
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
			out.flush();
		}
			catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}
	static int sendcore(Socket soc,byte [] packet,int length)
	{
		
		try {
			OutputStream out=soc.getOutputStream();
			out.write(packet,0,length);
			return 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	static int FileReceive(Socket soc,String filename,int StartIndex,int EndIndex) throws IOException
	{
		soc.setSoTimeout(parent_thread.RecvTimeout);
		int bytesrecvd=0;
		byte readbuffer[]=new byte[MTU];
		InputStream in = soc.getInputStream();
		RandomAccessFile raf=new RandomAccessFile(filename,"rw");
		raf.seek(StartIndex);
		
		
		int x=in.read(readbuffer);
		
		do 
		{		
			raf.write(readbuffer,0,x);						
			bytesrecvd+=x;
			if(bytesrecvd==EndIndex-StartIndex+1)
				break;
			x=in.read(readbuffer);
		}while(x!=-1);
		// Handle timeouts .. 
		raf.close();
		return bytesrecvd;
	}

	
}