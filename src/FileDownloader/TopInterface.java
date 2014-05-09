package FileDownloader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;


public class TopInterface {
    
        /*
	public static void main(String []args) throws IOException
	{
		Scanner sc=new Scanner(System.in);
		int a=sc.nextInt();
		if (a==1){
			clientside();
		}
		else{
			sourceside();
		}
		
		
	}*/
    
	public  void clientside(String filename,ArrayList<InetAddress> address) throws IOException{
		//String filename="tmpPlzCmR.pdf";
		//ArrayList<InetAddress> address=new ArrayList<InetAddress>();
		//address.add(InetAddress.getByName("10.252.221.47"));		
		parent_thread x=new parent_thread(address);		
		x.PrimaryThread(filename,10); //10 is redundant
	}
        
	public  void sourceside(){
		Source_side s=new Source_side();
		try {
			s.source_sideFileDownloader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
