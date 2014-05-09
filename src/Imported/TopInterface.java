package Imported;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;


public class TopInterface {

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
		
		
	}
	public static void clientside() throws IOException{
		String filename="abc.txt";
		InetAddress address=InetAddress.getByName("10.252.221.39");		
		parent_thread x=new parent_thread();		
		x.PrimaryThread(filename, address,-1);
	}
	public static void sourceside(){
		Source_side s=new Source_side();
		try {
			s.source_sideFileDownloader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
