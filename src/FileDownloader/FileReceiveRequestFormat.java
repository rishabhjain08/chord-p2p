package FileDownloader;

import java.net.InetAddress;
import java.util.ArrayList;


public class FileReceiveRequestFormat {

	ArrayList<InetAddress> nodes;
	int filesize;  //bytes
	int portnumber; // portnum of the original source
	
	
	public FileReceiveRequestFormat(InetAddress[] arr,int fsize,int portnum)
	{
		nodes=new ArrayList<InetAddress>();
		portnumber=portnum;
		filesize=fsize;
		if(arr==null)
			return;
		for (int i=0;i<arr.length;i++)
		{
			nodes.add(arr[i]);			
		}
		
		
	}
	public void addNodes(InetAddress[] arr)
	{
		for (int i=0;i<arr.length;i++)
			nodes.add(arr[i]);
	}
	public void SetFileSize(int fsize)
	{
		filesize=fsize;
	}
}
