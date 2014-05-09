package protocol.chord.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import protocol.chord.Identifier;
import protocol.chord.Node;
import protocol.chord.ServiceInterface;

public class FileManager {

	private Node node;
	private static final int connectionAttempts = 5;
	private static final int tryConnectingAfter = 5000;
	//	private Map<Identifier, HashSet<Node>> keys;
	private ArrayList<Map<Identifier, HashSet<Node>>> keys1;
	private static final int maxHashBytes = 1024;
	private File uploadDir;
	private FileManagerImpl remoteHandler;
	private NodeCallbackImpl nodeCb;
	private Thread dirUploadThread;
	
	public FileManager ()
	{
		this.node = new Node();
		//		this.keys = new HashMap<Identifier, HashSet<Node>>();
		this.keys1 = new ArrayList<Map<Identifier, HashSet<Node>>>(Identifier.maxLength + 1);
		for (int i = 0; i < Identifier.maxLength + 1; i++)
			this.keys1.add(new HashMap<Identifier, HashSet<Node>>());
		this.uploadDir = null;
		this.remoteHandler = new FileManagerImpl();
		this.nodeCb = new NodeCallbackImpl();
		this.node.registerCallback(this.nodeCb);
	}

	//change to public boolean init (String address)
	public boolean init (String eAddress, int ePort, String myAddress, int newPort)
	{
		//try to connect to the p2p network
		boolean started = false;
		for (int i = 0; i < connectionAttempts; i++)
		{
			System.out.println((i + 1) + ": Trying to connect to the p2p network...");
			if (node.start(eAddress, ePort, myAddress, newPort, (ServiceInterface) this.remoteHandler))
			{
				started = true;
				break;
			}
			System.out.println("will try to reconnect after " + tryConnectingAfter/1000 + " seconds...");
			try {
				Thread.sleep(tryConnectingAfter);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!started)
		{
			System.out.println("failed to connect to the p2p network.");
			return false;
		}
		else
		{
			System.out.println("successfully connected to the p2p network.");
			System.out.println("succ out in finger table : " + (node.getSuccessor() == null ? "null" : node.getSuccessor()));
		}
		return true;
	}

	public boolean setUploadDirectory (final File f)
	{
		if (f != null && !f.isDirectory())
			return false;
		//throw new IllegalArgumentException((f == null ? "null" : f.getAbsolutePath()) + " is not a directory.");
		System.out.println("new upload dir : " + (f == null ? "null" : f.getAbsolutePath()));
		this.uploadDir = f;
		//upload the directory's information to the p2p network
		dirUploadThread = new Thread ()
		{
			@Override
			public void run ()
			{
				File[] files = null;
				try
				{
					files = f.listFiles();
				} catch (Exception e)
				{
					return;
				}
				
				for (File file : files)
				{
					if (dirUploadThread != Thread.currentThread())
						break;
					FileManager.this.putFile(file);
				}
			}
		};
		dirUploadThread.start();
		return true;
	}

	private boolean putFile (File f)
	{
		if (!f.exists() || !f.isFile())
                    return false;
                System.out.println("putting file " + f.getName() + "...");
		boolean success = false;
		Identifier ident = FileManager.getFileIdentifier(f);
		Map<String, HashSet<Node>> map = new HashMap<String, HashSet<Node>>();
		HashSet<Node> nSet = new HashSet<Node>();
		nSet.add(node);
		map.put(ident.toString(), nSet);
		Node sNode = node.findSuccessor(ident);//.getSuccessor();
		if (sNode != null)
		{
			if (node.equals(sNode))
			{
//				System.out.println("sending add request " + map + " to node(" + sNode + ")");
				this.remoteHandler.addKeys(map);
			}
			else
			{
				try {
//					System.out.println("sending add request " + map + " to node(" + sNode + ")");
					((FileManagerIF) sNode.getRemoteServiceInterface(FileManagerIF.class)).addKeys(map);
				} catch (Exception e) {}
			}
			success = true;
		}
		return success;
	}
        
        public Node getChordNode(){
            return this.node;
        }

	public Set<Node> whoHasFile (Identifier ident)
	{
		if (ident == null)
			return null;
		Set<Node> fNodes = null;
		try
		{
			Node sNode = node.findSuccessor(ident);			
			fNodes = sNode == null ? null : 
				(sNode.equals(node) ? this.remoteHandler.getFileOwners(ident) : ((FileManagerIF) sNode.getRemoteServiceInterface(FileManagerIF.class)).getFileOwners(ident));
		} catch (Exception e) {}
		return fNodes;
	}

	public String getFileName (Identifier ident, Node n)
	{
		if (ident == null || n == null)
			return null;
		String fileName = null;
		try
		{
			if (n.equals(node))
				fileName = this.remoteHandler.getFileName(ident);
			else
				fileName = ((FileManagerIF) n.getRemoteServiceInterface(FileManagerIF.class)).getFileName(ident);
		} catch (Exception e) {}
		return fileName;
	}

	private void printKeys ()
	{
		int nSegs = this.keys1.size();
		for (int i = 0 ; i < nSegs; i++)
		{
			if (i == 0)
				System.out.println("keys for my own segment =>");
			else
				System.out.println("keys for segment : " + i + " =>");
			System.out.println(this.keys1.get(i));
		}
	}

	private static Map<Identifier, HashSet<Node>> toLocalKeys (Map<String, HashSet<Node>> keys)
	{
		Map<Identifier, HashSet<Node>> localKeys = new HashMap<Identifier, HashSet<Node>>();
		Set<String> ids = keys.keySet();
		for (String id : ids)
		{
			localKeys.put(Identifier.toIdentifier(id), keys.get(id));
		}
		return localKeys;
	}

	private static Map<String, HashSet<Node>> toRemoteKeys (Map<Identifier, HashSet<Node>> keys)
	{
		Map<String, HashSet<Node>> remoteKeys = new HashMap<String, HashSet<Node>>();
		Set<Identifier> ids = keys.keySet();
		for (Identifier id : ids)
		{
			remoteKeys.put(id.toString(), keys.get(id));
		}
		return remoteKeys;
	}

	private Map<String, HashSet<Node>> getKeys (Identifier from, Identifier to)
	{
		Map<String, HashSet<Node>> result = new HashMap<String, HashSet<Node>>();
//		synchronized (FileManager.this.keys1)
//		{
			Iterator<Map<Identifier, HashSet<Node>>> iterator = FileManager.this.keys1.iterator();
			while(iterator.hasNext())
			{
				Map<Identifier, HashSet<Node>> keys = iterator.next();
				Set<Identifier> ids = keys.keySet();
				for (Identifier id : ids)
				{
					if (id.between(from, to, true, false))
					{
						result.put(id.toString(), keys.get(id));
					}
				}
			}
//		}
		return result;
	}

	private boolean removeKeys (Identifier from, Identifier to)
	{
		boolean myKeysChanged = false;
//		synchronized (FileManager.this.keys1)
//		{
			ArrayList<Map<Identifier, HashSet<Node>>> segs = FileManager.this.keys1;
			int nSegs = segs.size();
			//TODO: change to i < nSegs
			for (int i = 0; i < 1; i++)
			{
				Map<Identifier, HashSet<Node>> keys = segs.get(i);
				Set<Identifier> ids = keys.keySet();
				Iterator<Identifier> itr = ids.iterator();
				while (itr.hasNext())
				{
					Identifier id = itr.next();
//					System.out.println("in here");
					if (id.between(from, to, true, false))
					{
						itr.remove();
//						System.out.println("removed some keys/transfered the responsibility i = " + i);

						myKeysChanged = myKeysChanged || i == 0;
//						System.out.println("myKeysChanged in " + myKeysChanged);
					}
				}
//			}
		}
//		System.out.println("myKeysChanged out " + myKeysChanged);
		if (myKeysChanged)
		{
//			System.out.println("removed some keys/transfered the responsibility");
			FileManager.this.propogateKeys();
		}
		return true;
	}

	private void propogateKeys ()
	{
		//now propogate these keys to all the successors in the list
		List<Node> succList = node.getSuccessorList();
		int i = 0;
		for (Node sNode : succList)
		{
//			System.out.println("propagating keys " + this.keys1.get(0) + " to " + sNode + " DISTANCE = " + (i + 1));
			try
			{
				((FileManagerIF) sNode.getRemoteServiceInterface(FileManagerIF.class)).setKeys(FileManager.toRemoteKeys(this.keys1.get(0)), i + 1);
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
			i++;
		}
	}

	private class NodeCallbackImpl implements protocol.chord.Node.Callback
	{
		@Override
		public void onPredecessorChanged(Node oldNode, Node newNode) {
			Node fromNode = oldNode == null ? node : oldNode;
			Node toNode = newNode;
			System.out.println("in predecessor changed " + fromNode + " tobide = :=" + toNode);
			if (fromNode == null || toNode == null)
				return;
			System.out.println("predecessor changed from " + fromNode + " to " + toNode);
			Map<String, HashSet<Node>> copyKeys = FileManager.this.getKeys(fromNode.getIdentifier(), toNode.getIdentifier());
			//nothing to copy
			if (copyKeys.isEmpty())
				return;
			//copy the keys to the predecessor
			try
			{
				System.out.println("keys are being copied from " + FileManager.this.node.getIdentifier() + " to " + newNode.getIdentifier());
//				System.out.println(copyKeys);
				boolean success = ((FileManagerIF) newNode.getRemoteServiceInterface(FileManagerIF.class)).addKeys(copyKeys);
				if (success)
				{
					FileManager.this.removeKeys(fromNode.getIdentifier(), toNode.getIdentifier());
				}
			} catch (Exception e) {}
		}

		@Override
		public void onSuccessorListChanged(List<Node> oldList, List<Node> newList) {
			Node[] oldSuccNodes = new Node[oldList.size()];
			Node[] newSuccNodes = new Node[newList.size()];
			oldList.toArray(oldSuccNodes);
			newList.toArray(newSuccNodes);
			for (int i = 0; i < newSuccNodes.length ; i++)
			{
				boolean succChanged = i >= oldSuccNodes.length || oldSuccNodes[i] != newSuccNodes[i];
				if (succChanged)
				{
					try
					{
						((FileManagerIF) newSuccNodes[i].getRemoteServiceInterface(FileManagerIF.class)).setKeys(FileManager.toRemoteKeys(FileManager.this.keys1.get(0)), i + 1);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}

	}

	private class FileManagerImpl implements ServiceInterface, FileManagerIF
	{
		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public boolean addKeys (Map<String, HashSet<Node>> altKeys)
		{
			Map<Identifier, HashSet<Node>> addKeys = FileManager.toLocalKeys(altKeys);
//			synchronized (FileManager.this.keys1)
//			{
//				System.out.println("received a add request " + (altKeys == null ? "null" : altKeys));
				Map<Identifier, HashSet<Node>> myKeys = FileManager.this.keys1.get(0);
				Set<Identifier> ids = addKeys.keySet();
				for (Identifier ident : ids)
				{
					HashSet<Node> oNodes = null;//, altKeys.get(keyId));
					if ((oNodes = myKeys.get(ident)) == null)
						myKeys.put(ident, oNodes = new HashSet<Node>());
					oNodes.addAll(addKeys.get(ident));
				}
//			}
			FileManager.this.propogateKeys();
			return true;
		}

		@Override
		public boolean setKeys (Map<String, HashSet<Node>> altKeys, int distance)
		{
//			synchronized (FileManager.this.keys1)
//			{
				if (altKeys == null || distance < 1 || distance > FileManager.this.keys1.size())
				{
					return false;
					//throw new IllegalArgumentException("does not maintain so far away keys. allowed distance <= " + FileManager.this.keys1.size());
				}
				System.out.println("received a set request " + (altKeys == null ? "null" : " DISTANCE = " + distance));
//				System.out.println("received a set request " + (altKeys == null ? "null" : altKeys + " DISTANCE = " + distance));
				Map<Identifier, HashSet<Node>> newKeys = FileManager.toLocalKeys(altKeys);
				FileManager.this.keys1.set(distance, newKeys);
//			}
			return true;
		}

                @Override
                public Map<String, HashSet<Node>> getAllKeys ()
                {
                        Map<String, HashSet<Node>> result = new HashMap<String, HashSet<Node>>();
        //		synchronized (FileManager.this.keys1)
        //		{
                                Iterator<Map<Identifier, HashSet<Node>>> iterator = FileManager.this.keys1.iterator();
                                while(iterator.hasNext())
                                {
                                        Map<Identifier, HashSet<Node>> keys = iterator.next();
                                        result.putAll(FileManager.toRemoteKeys(keys));
                                }
        //		}
                        return result;
                }

                @Override
		public HashSet<Node> getFileOwners (Identifier ident)
		{
			//                	System.out.println("received getFileOwners for " + ident);
			//	 		System.out.println("received getFileOwners for " + ident);
			HashSet<Node> oNodes = new HashSet<Node>();
//			synchronized (FileManager.this.keys1)
//			{
				ArrayList<Map<Identifier, HashSet<Node>>> allKeys = FileManager.this.keys1;
				for (Map<Identifier, HashSet<Node>> keys : allKeys)
				{
					HashSet<Node> nodes = null;
					if ((nodes = keys.get(ident)) != null)
					{
						oNodes.addAll(nodes);
					}
//				}
			}
			//                            System.out.println("returning " + oNodes);
			return oNodes;
		}

		@Override
		public String getFileName (Identifier ident)
		{
			//TODO: can make this better
			//cache the file names with their identifiers
			if (FileManager.this.uploadDir == null)
				return null;
			File[] files = FileManager.this.uploadDir.listFiles();
			for (File f : files)
			{
				if (!f.isFile())
                                    continue;
                                Identifier fIdent = null;
				if ((fIdent = FileManager.getFileIdentifier(f)) != null && fIdent.equals(ident))
					return f.getName();
			}
			return null;
		}

		@Override
		public Class<?> getServiceInterface() {
			return FileManagerIF.class;
		}

	}

	private static Identifier getFileIdentifier (File f)
	{
		if (!f.exists())
			return null;
//		return Identifier.toIdentifier(f.getName().substring(0, f.getName().indexOf('.')));
		//TODO: uncomment this
		 		byte[] hashBytes = null;
		 		
		 		try {
		 			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			 		FileInputStream fis = new FileInputStream(f);
			        byte[] data = new byte[1024];
			        int read = 0;
			        int bytesRead = 0;
			        while (bytesRead < maxHashBytes && (read = fis.read(data)) != -1) {
			            sha1.update(data, 0, read);
			            bytesRead++;
			        };
			        hashBytes = sha1.digest();
				} catch (Exception e) {}
		
		 		return Identifier.toIdentifier(new BigInteger(1, hashBytes).mod(BigInteger.ONE.shiftLeft(Identifier.maxLength)));
	}

	public void execute (String cmd) throws Exception
	{
                    FileManager fMgr = this;
                    System.out.print("> ");
                    String[] bits = cmd.split(" ");
                    if (cmd.equals("print ft"))
                    {
                            fMgr.node.fingerTable.print();
                    }
                    else if (cmd.equals("print snode"))
                    {
                            System.out.println("successor = " + fMgr.node.getSuccessor().getIdentifier());
                    }
                    else if (cmd.equals("print pnode"))
                    {
                            System.out.println("predecessor = " + (fMgr.node.getPredecessor() == null ? "none" : fMgr.node.getPredecessor().getIdentifier()));
                    }
                    else if (cmd.equals("print slist"))
                    {
                            System.out.println(fMgr.node.getSuccessorList());
                    }
                    else if (bits.length == 3 && bits[0].equals("set") && bits[1].equals("dir"))
                    {
                            if (!fMgr.setUploadDirectory(new File(bits[2])))
                                    System.out.println("fialed to set the upload dir " + bits[2]);
                    }
                    else if (cmd.equals("print keys"))
                    {
                            fMgr.printKeys();
                    }
                    else if (cmd.equals("print exists"))
                    {
                            fMgr.printKeys();
                    }
                    else if (bits.length == 3 && bits[0].equals("who") && bits[1].equals("has"))
                    {
                            System.out.println("nodes " + fMgr.whoHasFile(Identifier.toIdentifier(bits[2])) + " have " + bits[2]);
                    }
                    else if (bits.length == 5 && bits[0].equals("get") && bits[1].equals("fn"))
                    {
                            System.out.println(fMgr.getFileName(Identifier.toIdentifier(bits[2]), Node.toNode(bits[3], Integer.parseInt(bits[4]))));

                    }
                    else
                    {
                        throw new Exception();
                    }
                    // 			else if (bits.length == 3 && bits[0].equals("set") && bits[1].equals("uploaddir"))
                    // 			{
                    // 				fMgr.setUploadDirectory(new File(bits[2]));
                    // 			}
	}
        
        public void stop(){
            node.stop();
        }
}
