package Imported;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


class Mythread extends Thread{
	String filename;
	InetAddress Addr;
	int PortNoDest;
	//int PortNoSrc;
	int StartIndex;
	int EndIndex;
	public Mythread(InetAddress add,int PDest,/*int PSrc,*/String fname,int SIndex,int EIndex){
		Addr=add;
		PortNoDest=PDest;
		//PortNoSrc=PSrc;
		StartIndex=SIndex;
		EndIndex=EIndex;
		filename=fname;
		
	}
	public void run()
	{
		try {
			parent_thread.SecondaryThread(Addr, PortNoDest, /*PortNoSrc,*/filename, StartIndex, EndIndex);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
public class parent_thread {


	
static final int RTT_MAX=100; // in milli seconds. After this time the primary thread stops waiting for
					//anymore secondary users which have the file.
static final int TIMEOUT_limit=200; // in milli seconds. After this time the connection is assumed to
						// be lost and the secondary thread closes and appends to 
						// Filename_UNFINSHED_DATABASE_67.txt
static final int LISTENPORT=2023; // This is the global port on which all datarequests are received.
						// ParentThread() continuously listens on this port.
int NextPort=LISTENPORT; // this is the available port which secondary
// This Thread listens on LISTENPORT and creates PrimaryThread() whenever a request comes which 
// is servicable by this node.
int ParentThread()
{
	return 1;
}
int getPort() // this has to be improved.
{
	NextPort++;
	return NextPort;
}

// For getting file, one PrimaryThread is created for each file.
//This thread spawns many SecondaryThreads. It checks for completeness of the work of
// each of secondary threads. It assigns and reassigns if necessary work to each of secondary threads.
int PrimaryThread(String filename, InetAddress address,int PortNoFirst) throws IOException
{
	ControlPacketFormation intrfc=new ControlPacketFormation();
	Socket soc=intrfc.SendDataRequest(filename, LISTENPORT, address);
	FileReceiveRequestFormat fresp=intrfc.parseDataResponse(soc);
	
	int StartOffset=0; //start and end both inclusive	
	int counter=fresp.filesize/(1+fresp.nodes.size());
//	int fRecvPort=4535; // Needs to be changed
	
	for(int i=0;i<fresp.nodes.size();i++)
	{
		Mythread sec;
		if(i==fresp.nodes.size()-1)
		{
			sec=new Mythread(fresp.nodes.get(i),LISTENPORT,filename,
					StartOffset,fresp.filesize);
		}
		else 
		{
			sec=new Mythread(fresp.nodes.get(i),LISTENPORT,filename,
				StartOffset,StartOffset+counter);
			
		}
		StartOffset+=counter+1;
		
		
		sec.start();
	}
	int status=postDataResponseSink(address, fresp, filename, 0, counter);
	if(status==counter)
		return 1;
	//Handle the case when not all was written.. node failure.
	
	return 1;
}
//  This function returns 1 if the job given was completed. Otherwise it returns 0.
static int SecondaryThread(InetAddress Addr,int PortNoDest,/*int PortNoSrc,*/String filename,
						int StartIndex,int EndIndex) throws IOException
{
	ControlPacketFormation intrfc=new ControlPacketFormation();
	Socket soc=intrfc.SendDataRequest(filename, PortNoDest, Addr);
	FileReceiveRequestFormat fresp=intrfc.parseDataResponse(soc);
	return postDataResponseSink(Addr,fresp,filename,StartIndex,EndIndex);
}
static int postDataResponseSink(InetAddress Addr, FileReceiveRequestFormat fresp,String filename,
		int StartIndex,int EndIndex) throws IOException 
{
	Socket soc=new Socket(Addr,fresp.portnumber);
	String packet=SendRecv.FormPacket_DataInit(StartIndex, EndIndex, filename);
	SendRecv.send(soc, packet, SendType.DATAINIT,0,0);
	int status=SendRecv.FileReceive(soc, filename);
	
	if (status==EndIndex-StartIndex)
		return 1;
	
	//Handle the case when not all was written.. node failure.
	
	return 0;
}
}
