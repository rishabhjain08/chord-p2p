package FileDownloader;


public enum SendType {
	FILE, // data will be filename.
	DATAREQUEST, // data will be the string to be sent
	DATARESPONSE, // data will be the string to be sent.
	DATAINIT;
	
	public static SendType fromInt(int x)
	{
	switch(x)
	{
	case 0: return FILE;
	case 1: return DATAREQUEST;
	case 2: return DATARESPONSE;
	case 3: return DATAINIT;
	
	}
	return null;
	}
}
