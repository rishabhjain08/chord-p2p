package FileDownloader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

class Mythread extends Thread {
	parent_thread pth;
	String filename;
	InetAddress Addr;
	ArrayList<Boolean> NodesTried;
	// int PortNoDest;
	// int PortNoSrc;
	int StartIndex;
	private int EndIndex;
	private int current_Source_index;
	static String Directory = "C:\\Users\\WASEEM\\Desktop\\P2PDownload\\";

	// this thread doesn't  make new thread.All work is done in constructor alone. It should be called at last. It is the primary thread.
	public Mythread (Socket soc,parent_thread pthread,int add_index,ArrayList<InetAddress> add_list, FileReceiveRequestFormat fresp,
			String filename, int StartIndex, int EndIndex,String dir) throws IOException
	{		
		this(pthread,add_index,add_list,filename,StartIndex,EndIndex,dir);
		postDataResponseSink( soc,add_list.get(add_index), fresp,filename, StartIndex,EndIndex);
		
	}

	public Mythread(parent_thread pth,int add_index, ArrayList<InetAddress> add_list,
			String fname, int SIndex, int EIndex, String dir) {
		
		this.pth=pth;
		NodesTried=new ArrayList<Boolean>();
		Addr = add_list.get(add_index);
		// PortNoDest=PDest;
		// PortNoSrc=PSrc;
		current_Source_index = add_index;
		for (int x = 0; x < add_list.size(); x++) {
			NodesTried.add(false);

		}
		NodesTried.set(add_index, true); // we have used it.

		StartIndex = SIndex;
		EndIndex = EIndex;
		filename = fname;
		if (dir != null)
			Mythread.Directory = dir;

	}

	public void run() {
		try {
			SecondaryThread(Addr, /* PortNoDest, *//* PortNoSrc, */filename,
					StartIndex, EndIndex);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getNextAddressindex() {
		this.current_Source_index = (1 + current_Source_index)
				% (this.NodesTried.size());
		if (NodesTried.get(current_Source_index))
			return -1;
		NodesTried.set(current_Source_index, true);
		return this.current_Source_index;
	}

	int SecondaryThread(InetAddress Addr,/* int PortNoSrc, */
			String filename, int StartIndex, int EndIndex) throws IOException {
		ControlPacketFormation intrfc = new ControlPacketFormation();
		Socket soc = intrfc.SendDataRequest(filename, parent_thread.LISTENPORT,
				Addr);
                FileReceiveRequestFormat fresp;
                if(soc!=null)
                    fresp = intrfc.parseDataResponse(soc);
                else
                    fresp =null;
                
		//soc.close();
		return postDataResponseSink(soc,Addr, fresp, filename, StartIndex, EndIndex);
	}

	int postDataResponseSink(Socket soc,InetAddress Addr, FileReceiveRequestFormat fresp,
			String filename, int StartIndex, int EndIndex)  throws IOException{
		
		//Socket soc=null;
		//try {
			//soc = new Socket(Addr, parent_thread.LISTENPORT);
			
		//} catch (IOException e) {
			// TODO Auto-generated catch block
		//	try {
			//	sleep(500);
			//	soc=new Socket(Addr,fresp.portnumber);
	//		} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
	//			e1.printStackTrace();
	//		}
			
		//e.printStackTrace();	
	//	}
            if(soc==null){
                int nextaddressindex1 = getNextAddressindex();
		if (nextaddressindex1 == -1)
			return 0;
                return SecondaryThread(pth.getAddress(nextaddressindex1),
					filename, StartIndex, EndIndex);
            }
		
		String packet = SendRecv.FormPacket_DataInit(StartIndex, EndIndex,
				filename);
		SendRecv.send(soc, packet, SendType.DATAINIT, 0, 0);
		int status = SendRecv.FileReceive(soc,
				Mythread.Directory.concat(filename),StartIndex, EndIndex);

		if (status == EndIndex - StartIndex + 1)
			return 1;

		// Handle the case when not all was written.. node failure.
		int nextaddressindex = getNextAddressindex();
		if (nextaddressindex == -1)
			return 0;
		else {
			return SecondaryThread(pth.getAddress(nextaddressindex),
					filename, StartIndex + status, EndIndex);
		}
	}

}

public class parent_thread {

	static final int RTT_MAX = 100; // in milli seconds. After this time the
									// primary thread stops waiting for
	// anymore secondary users which have the file.
	static final int TIMEOUT_limit = 200; // in milli seconds. After this time
											// the connection is assumed to
	// be lost and the secondary thread closes and appends to
	// Filename_UNFINSHED_DATABASE_67.txt
	static final int LISTENPORT = 2023; // This is the global port on which all
										// datarequests are received.
	// ParentThread() continuously listens on this port.
	int NextPort = LISTENPORT; // this is the available port which secondary
	// This Thread listens on LISTENPORT and creates PrimaryThread() whenever a
	// request comes which
	// is servicable by this node.
	static final int RecvTimeout = 200000; // this is the time after which thread will
									// call another file bearing person.

	private ArrayList<InetAddress> FileContainingnodes;

	public parent_thread(ArrayList<InetAddress> addroot) {
		FileContainingnodes=new ArrayList<InetAddress>();
		this.FileContainingnodes.addAll(addroot);
	}

	public InetAddress getAddress(int index) {
		return FileContainingnodes.get(index);
	}

	int getPort() // this has to be improved.
	{
		NextPort++;
		return NextPort;
	}

	// For getting file, one PrimaryThread is created for each file.
	// This thread spawns many SecondaryThreads. It checks for completeness of
	// the work of
	// each of secondary threads. It assigns and reassigns if necessary work to
	// each of secondary threads.
	int PrimaryThread(String filename, int PortNoFirst) throws IOException {
		ControlPacketFormation intrfc = new ControlPacketFormation();
                Socket soc=null;
                
                for(int z=0;z<this.FileContainingnodes.size();z++){
                    InetAddress a= this.FileContainingnodes.get(z);
                    soc = intrfc.SendDataRequest(filename, LISTENPORT,
				a);
                    if (soc==null){
                        this.FileContainingnodes.remove(a);
                    }
                    else
                        break;
                }
                if(soc==null)
                    return -1;
                 
                 
		FileReceiveRequestFormat fresp = intrfc.parseDataResponse(soc);
		//soc.close();
		//this.FileContainingnodes.addAll(fresp.nodes); was there earlier

		int StartOffset = 0; // start and end both inclusive
		int counter = fresp.filesize / (this.FileContainingnodes.size());
		// int fRecvPort=4535; // Needs to be changed

		for (int i = 0; i < fresp.nodes.size()-1; i++) {
			Mythread sec;
//			if (i == fresp.nodes.size() - 1) {
//				sec = new Mythread(this,i + 1, this.FileContainingnodes, filename,
//						StartOffset, fresp.filesize-1, null);
//			} else {
				sec = new Mythread(this,i + 1, this.FileContainingnodes, filename,
						StartOffset, StartOffset + counter, null);

	//		}
			StartOffset += counter + 1;
			sec.start();
		}
		Mythread fin = new Mythread(soc,this,0,this.FileContainingnodes,
				  fresp, filename, StartOffset, fresp.filesize - 1,null);
		
		/*
		 * int status = postDataResponseSink(this.FileContainingnodes.get(0),
		 * fresp, filename, StartOffset, fresp.filesize - 1); if (status ==
		 * counter) return 1; // Handle the case when not all was written.. node
		 * failure.
		 */
		return 1;
	}
	// This function returns 1 if the job given was completed. Otherwise it
	// returns 0.

}
