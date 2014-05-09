package protocol.chord;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import protocol.chord.FingerTable.Finger;

public class Node implements NodeIF, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient static final String handlerName = "Node";
	private transient static final int refreshFingersAfter = 2000;//ms
	private transient static final int stabilizeAfter = 5000;//ms
	private transient static final int checkPredecessorAfter = 5000;//ms
	//
	private String address;
	//TODO: public static final int port = 6565;
	private int port = 6565;
	//public Identifier id;
	private transient Node predecessor;
	
	public transient FingerTable fingerTable;
	public Node successor;
	private transient static final int successorListMaxLength = Identifier.maxLength;
	
	//
	private transient List<Node> successorList;

	private transient WebServer webServer;
	private transient ServiceInterface serviceObject;
	private transient Set<Callback> nodeCallbacks;
	
	private transient Thread stabilizationThread = new Thread ()
	{
		public void run ()
		{
			while (stabilizationThread == Thread.currentThread())
			{
				Node.this.stabilize();
				try {
					Thread.sleep(stabilizeAfter);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	private transient Thread fingerFixingThread = new Thread ()
	{
		public void run ()
		{
			while (fingerFixingThread == Thread.currentThread())
			{
				Node.this.fixFingers();
				try {
					Thread.sleep(refreshFingersAfter);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private transient Thread predecessorCheckingThread = new Thread ()
	{
		public void run ()
		{
			while (predecessorCheckingThread == Thread.currentThread())
			{
				if (!Node.this.checkPredecessor())
					Node.this.predecessor = null;
				try {
					Thread.sleep(checkPredecessorAfter);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	public Node ()
	{
		this.address = null;
		this.predecessor = null;
		//initialize the finger table
		this.fingerTable = new FingerTable(Identifier.maxLength);
		//this.keys = new HashMap<Identifier, Set<Node>>();
		this.successorList = new LinkedList<Node>();
		this.nodeCallbacks = new HashSet<Callback>();
	}
	
	public boolean start (String eAddress, int ePort, String myAddress, int newPort, ServiceInterface serviceObject)
	{
		if (newPort < 0)
                    return false;
                Node eNode = eAddress == null || ePort < 0 ? null : Node.toNode(eAddress, ePort);
                this.port = newPort;
                System.out.println("joining using : " + (eNode == null ? "null" : eNode));
		try {
                    this.address = myAddress;//Inet4Address.getLocalHost().getHostAddress();
                } catch (Exception ex) {
                    return false;
                }
		System.out.println("address:port <= " + this.address + ":" + this.port);
                this.serviceObject = serviceObject;
                System.out.println("id <= " + this.getIdentifier());
		BigInteger maxId = BigInteger.ONE.shiftLeft(Identifier.maxLength);
		BigInteger baseId = this.getIdentifier().toBigInteger();
		BigInteger offset = BigInteger.ONE;
		BigInteger nodeId = baseId.add(offset).mod(maxId);
		for (int i = 0; i < Identifier.maxLength; i++)
		{
			this.fingerTable.fingers[i].start = Identifier.toIdentifier(nodeId.toString());
			this.fingerTable.fingers[i].node = this;
			offset = offset.multiply(BigInteger.valueOf(2L));
			nodeId = baseId.add(offset).mod(maxId);
			this.fingerTable.fingers[i].interval = Identifier.toIdentifier(nodeId.toString());
		}
		this.successor = Node.toNode(this.address, this.port);
                System.out.print("starting XML-RPC server...");
		if (!this.startServer() || !this.join(eNode))
		{
			this.stop();
			return false;
		}
                System.out.println("DONE");
		stabilizationThread.start();
		fingerFixingThread.start();
		predecessorCheckingThread.start();
		return true;
	}
	
        public String getAddress ()
        {
            return this.address;
        }
        
        public int getPort ()
        {
            return this.port;
        }
        
	public void stop ()
	{
		this.stopServer();
		stabilizationThread = null;
		fingerFixingThread = null;
		predecessorCheckingThread = null;
	}
	
/*	
   public void start (Node eNode) throws Exception
	{
		this.startServer();
		this.join(eNode);
	}
 */
//	private boolean deleteKeys (Identifier from, Identifier to)
//	{
//		Set<Identifier> ids = this.keys.keySet();
//		Iterator<Identifier> itr = ids.iterator();
//		while (itr.hasNext())
//		{
//			Identifier id = itr.next();
//			if (id.between(from, to, true, false))
//			{
//				itr.remove();
//			}
//		}
//		return true;
//	}

	public Identifier getIdentifier ()
	{
		//TODO: change the hash function
//            return Identifier.toIdentifier(new BigInteger(this.port + "").mod(BigInteger.ONE.shiftLeft(Identifier.maxLength)));
		return Identifier.toIdentifier(Identifier.hash((this.address == null ? this.address : "") + ":" + this.port));
	}
	
        @Override
	public Node getSuccessor ()
	{
		return this.successor;
	}

	private void setSuccessor (Node node)
	{
		node = node == null ? this : node;
//		if (this.fingerTable.fingers[0].node != node)
//			System.out.println("sucessor <= " + node.getIdentifier());
		this.fingerTable.fingers[0].node = node;
		this.successor = Node.toNode(node.address, node.port);
		if (!this.equals(node))
		{
                    this.useSuccessorList(node);
		}
                //System.out.println("setting this  = " + this + " node = " + node + " now " + (this.getSuccessor() != null ? this.getSuccessor() : "null"));
	}
	
        @Override
	public Node getPredecessor ()
	{
//		if (this.predecessor == null)
//			return null;
		return this.predecessor;
	}

	private boolean setPredecessor (Node pNode)
	{
		this.predecessor = pNode;
//		if (this.predecessor != pNode)
//			System.out.println("predecessor <= " + this.predecessor.getIdentifier());
		return true;
	}
	
        @Override
	public List<Node> getSuccessorList() {
		return this.successorList;
	}
	
	private void useSuccessorList (Node sNode)
	{
		if (sNode == null || this.equals(sNode))
			return;
		List<Node> sList = null;
		try
		{
			sList = new LinkedList<Node>(sNode.getRemoteInterface().getSuccessorList());
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		sList.add(0, sNode);
		//remove all the nodes after and including current identifier (loop only once)
		Iterator<Node> itr = sList.iterator();
		int nSuccessor = 0;
		while (itr.hasNext())
		{
			if (itr.next().equals(this))
			{
				itr.remove();
				break;
			}
			nSuccessor++;
			if (nSuccessor >= successorListMaxLength)
				break;
		}
		while (itr.hasNext())
		{
			itr.next();
			itr.remove();
		}
                List<Node> oldList = this.successorList;
                List<Node> newList = sList;
		this.successorList = sList;
                if (!oldList.equals(newList))
                {
//                    System.out.println("OLD LIST : " + oldList);
//                    System.out.println("NEW LIST : " + newList);
                    for (Callback cb : this.nodeCallbacks)
                    {
                            cb.onSuccessorListChanged(oldList, newList);
                    }
                }
//                newList.clear();
	}

	private boolean join (Node eNode)
	{
            boolean success = false;
//            System.out.println("trying to join using " + (eNode == null ? "null" : eNode.getIdentifier()));
		
		if (eNode != null)
		{
			try
			{
//				System.out.println("testing...");
//				Node a = eNode.getRemoteInterface().testObj();
//				System.out.println("list = " + (a == null ? "null" : a));
				Node sNode = eNode.findSuccessor(this.getIdentifier());
//				System.out.println("tested...");
				this.setSuccessor(sNode);
				//TODO: debug
				this.fingerTable.print();
				success = true;
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		else
		{
			success = true;
		}
//		System.out.println("joining the chord..." + (success ? "DONE" : "FAILED"));
                return success;
	}
	
	private void stabilize ()
	{
		Node sNode = null;
		Node pNode = null;
		//iterate and find a successor from the successor list
		Iterator<Node> itr = this.successorList.iterator();
//		System.out.println("slist = " + this.successorList);
		while (itr.hasNext())
		{
			sNode = itr.next();
//			if (this.equals(sNode))
//				continue;
			try
			{
//				System.out.println("snode is " + sNode);
//				sNode = this == sNode ? this : sNode.getRemoteInterface().fetch();
               pNode = this.equals(sNode) ? this.getPredecessor() : sNode.getRemoteInterface().getPredecessor();
				//pNode = sNode.fetch();
				//pNode = sNode.getPredecessor();
				break;
			} catch (Exception e)
			{
//				System.out.println("an error");
				e.printStackTrace();
				sNode = null;
				itr.remove();
			}
		}
		
		//found no successor
		if (sNode == null)
		{
//			System.out.println("couldnt find any active successor from the successor list");
			this.setSuccessor(this);
			this.setPredecessor(null);
			return;
		}
//		System.out.println("new successor node is " + sNode);

		//no more nodes in the ring (that I know of)
		if (this.equals(sNode))
		{
			return;
		}

		
		//check if a there is new node that should be my successor
		if (pNode != null && pNode.getIdentifier().between(Node.this, sNode))
		{
                    sNode = pNode;
		}
//                System.out.println("setting my succ as " + sNode);
                this.setSuccessor(sNode);
		
		//notify the successor of my presence
		try
		{
			sNode.getRemoteInterface().notify(this);
		} catch (Exception e){}
	}
	
	public boolean notify (Node n)
	{
		if (n == null)
                    return true;
                Node oldPreNode = this.predecessor;
		Node newPreNode = this.predecessor;
		
		if (this.predecessor == null || n.getIdentifier().between(this.predecessor, this))
		{
			oldPreNode = this.predecessor;
			newPreNode = n;
			this.predecessor = n;
			//TODO: debug
//			System.out.println("predecessor <= " + n.getIdentifier());
			//use the predecessor to update the finger table
			for (int i = Identifier.maxLength - 1; i >= 0; i--)
			{
				Finger finger = this.fingerTable.fingers[i];
				if (this.predecessor.getIdentifier().between(finger.start, finger.node))
				{
					if (i == 0)
						this.setSuccessor(this.predecessor);
					else
						finger.node = this.predecessor;
				}
			}
//			this.fingerTable.print();
		}
		
		//invoke the callback functions
		if ((newPreNode == null ^ oldPreNode == null) 
				|| ((newPreNode != null && oldPreNode != null) && !newPreNode.equals(oldPreNode)))
		{
			for (Callback cb : this.nodeCallbacks)
			{
				cb.onPredecessorChanged(oldPreNode, newPreNode);
			}
		}
		return true;
	}
	
	private void fixFingers ()
	{
		//range = [1, Identifier.maxLength - 1]
		int randomFingerIndex = (int) (Math.random() * (Identifier.maxLength - 1)) + 1;
//		Node oldNode = this.fingerTable.fingers[randomFingerIndex].node;
		this.fingerTable.fingers[randomFingerIndex].node = this.findSuccessor(this.fingerTable.fingers[randomFingerIndex].start);
//		Node newNode = this.fingerTable.fingers[randomFingerIndex].node;
		//TODO: debug
//		if (!oldNode.getIdentifier().equals(newNode.getIdentifier()))
//		{
//			System.out.println("updated finger " + randomFingerIndex + " from " + oldNode.getIdentifier() + " to " + newNode.getIdentifier());
//		}
		//TODO: debug
		//System.out.println("finger[" + randomFingerIndex + "] <= " + this.fingerTable.fingers[randomFingerIndex].node.getIdentifier());
	}
	
	private boolean checkPredecessor ()
	{
		if (this.predecessor == null)
			return false;
		if (this.predecessor == this)
			return true;
		boolean success = true;
		try
		{
			success = this.predecessor.getRemoteInterface().exists();
		} catch (Exception e)
		{
			success = false;
		}
		return success;
	}

	public boolean exists ()
	{
		return true;
	}
	
	public Node fetch ()
	{
		return this;
	}
	
	public Node findSuccessor (Identifier ident)
	{
                Node pNode = null;
                try
                {
                    pNode = this.getRemoteInterface().findPredecessor(ident);
                } catch (Exception exp) {}
                //System.out.println("predecessor(" + ident + ") <= " + (pNode == null ? "null" : pNode));
		//System.out.println("pNode = " + pNode.getIdentifier().id);
		Node sNode = pNode == null ? null : pNode.getSuccessor();
		//System.out.println("successor(" + ident + ") = " + sNode.getIdentifier());
		return sNode;
	}
        
	public Node findPredecessor (Identifier ident)
	{
		Node n = this;
//		Node sNode = n.getSuccessor();
//		if (sNode == null)
//			System.out.println("in findPredecessor node = " + n + " successor = " + (sNode == null ? "null" : sNode));
//                System.out.println("OUT node is " + n +  " succ : " + (n.getSuccessor() != null ? n.getSuccessor(): "null"));
//                System.out.println("before " + n + " < " + ident + " < " + n.getSuccessor() + " == ");
		boolean s = ident.between(n, n.getSuccessor(), true, false);
//                System.out.println(n + " < " + ident + " < " + n.getSuccessor() + " == " + s);
		while (!ident.between(n, n.getSuccessor(), true, false))
		{
//			System.out.println("node is " + n);
                        Node temp = null;
			if (n.equals(this))
			{
				temp = n.closestPrecedingFinger(ident);
				//System.out.println(temp.getIdentifier() + " is closer to " + ident + " then " + n.getIdentifier());
			}
			else
			{
				//System.out.println("remote finger table lookup...");
				temp = n.getRemoteInterface().closestPrecedingFinger(ident);
				//System.out.println("remote finger table lookup DONE...");
			}
			
			//if closest finger didnt change
			if (temp.getIdentifier().compareTo(n, "=="))
			{
				break;
			}
			n = temp;
		}
		//System.out.println("predecessor(" + ident + ") = " + n.getIdentifier());
		return n;
	}

	public Node closestPrecedingFinger (Identifier ident)
	{
		Node n = this;
		for (int i = Identifier.maxLength - 1; i >= 0; i--)
		{
			Finger finger = this.fingerTable.fingers[i];
			if (finger.node.getIdentifier().between(this, ident))
			{
				try
				{
					n = this.equals(finger.node) ? this : finger.node.getRemoteInterface().fetch();
					break;
				} catch (Exception e) {}
			}
		}
		return n;
//		Node n = this;
//		for (int i = Identifier.maxLength - 1; i >= 0; i--)
//		{
//			Finger finger = this.fingerTable.fingers[i];
//			if (finger.node.getIdentifier().between(this, ident))
//			{
//				n = finger.node;//.getRemoteInterface().fetch();
//				break;
//			}
//		}
//		return n;
	}
	
	public interface Callback {
		public void onPredecessorChanged (Node oldNode, Node newNode);
                public void onSuccessorListChanged (List<Node> oldList, List<Node> newList);
	}
	
	public void registerCallback (Callback cb)
	{
		this.nodeCallbacks.add(cb);
	}
	
	public void deregisterCallback (Callback cb)
	{
		this.nodeCallbacks.remove(cb);
	}

//	public static void main (String args[]) throws XmlRpcException, IOException
//	{
//		int po = 6;
//		po *= 5;
//		Node eNode = Node.toNode("localhost", 6565 + po);
//		Node lnode = null;
//		try {
//			lnode = new Node();
//			lnode.address = "localhost";
//			//lnode.port = 6565 + 0 + po;
//			lnode.port = 6565 + 0+ po;
//			eNode = null; 
//			if (!lnode.start(eNode, null))
//			{
//				System.out.println("Failed to start the node.");
//				return;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		while (true)
//		{
//			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//			System.out.print("$ ");
//			String cmd = reader.readLine();
//			if (cmd.equals("print finger table"))
//			{
//				lnode.fingerTable.print();
//			}
//			else if (cmd.equals("print snode"))
//			{
//				System.out.println("successor(" + lnode.getIdentifier() + ") = " + (lnode.getSuccessor() == null ? "none" : lnode.getSuccessor().getIdentifier()));
//			}
//			else if (cmd.equals("print pnode"))
//			{
//				System.out.println("predecessor(" + lnode.getIdentifier() + ") = " + (lnode.getPredecessor() == null ? "none" : lnode.getPredecessor().getIdentifier()));
//			}
//			else if (cmd.equals("print slist"))
//			{
//				System.out.println(lnode.getSuccessorList());
//			}
//			else if (cmd.equals("is stabilizing"))
//			{
//				System.out.println("stabilizing = " + lnode.stabilizationThread.isAlive());
//			}
//			else if (cmd.equals("is fixing fingers"))
//			{
//				System.out.println("stabilizing = " + lnode.fingerFixingThread.isAlive());
//			}
//			else if (cmd.equals("check remote node"))
//			{
//				System.out.println(eNode.getRemoteInterface().exists());
//			}
//		}
//	}
	
 	public static Node toNode (String address, int port)
	
        {
            Node n = new Node();
            n.address = address;
            n.port = port;
            return n;
        }

       
        
        @Override
	public boolean equals (Object o)
	{
		if (o == null)
			return false;
		try
		{
			return this.getIdentifier().equals(o);
		} catch (Exception e)
		{
			return false;
		}
	}
        
    public int hashCode ()
    {
    	return this.getIdentifier().hashCode();
    }
	
    @Override
	public String toString ()
	{
            return this.getIdentifier().toString();
	}
	
	/** Client ENDPOINT **/
	private NodeIF getRemoteInterface ()
	{
//		final NodeIF nodeInterface = null;
		NodeIF nodeInterface = null;
		try {
	    	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL("http://" + this.address + ":" + this.port + "/xmlrpc"));
		    config.setEnabledForExtensions(true);
	        config.setContentLengthOptional(false);
		config.setConnectionTimeout(5);
                XmlRpcClient client = new XmlRpcClient();
	        // set configuration
	        client.setConfig(config);
	        //client.setTypeFactory(new XmlRpcTypeNil(client));
		    //ClientFactory factory = new ClientFactory(client);
	        //nodeInterface = (NodeIF) factory.newInstance(NodeIF.class);
	        ClientFactory factory = new ClientFactory(client);
			nodeInterface = (NodeIF) factory.newInstance(NodeIF.class);
//	        System.out.println("connecting to " + this.port + "...");
//	        System.out.println("connected to " + nodeInterface2.fetch() + "...");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return nodeInterface;
	 }
	
	public Object getRemoteServiceInterface (Class serviceInterfaceClass)
	{
		Object serviceInterface = null;
		try {
	    	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL("http://" + this.address + ":" + this.port + "/xmlrpc"));
		    config.setEnabledForExtensions(true);
	        config.setContentLengthOptional(false);
		    XmlRpcClient client = new XmlRpcClient();
		    // use Commons HttpClient as transport
	        //client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
	        // set configuration
	        client.setConfig(config);	    
		    //client.setTypeFactory(new XmlRpcTypeNil(client));
	        ClientFactory factory = new ClientFactory(client);
	        serviceInterface = factory.newInstance(serviceInterfaceClass);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return serviceInterface;
	 }

	/** Server ENDPOINT **/

	/* For creating the server interface for RPC invocations */
	private class NodeRequestProcessorFactoryFactory implements RequestProcessorFactoryFactory {
		private final RequestProcessorFactory nodeFactory =
				new NodeRequestProcessorFactory();
		private final RequestProcessorFactory serviceFactory =
				new ServiceRequestProcessorFactory();
		private final NodeIF node;
		private final ServiceInterface service;
		
		public NodeRequestProcessorFactoryFactory(NodeIF node, ServiceInterface serviceObject) {
			this.node = node;
			this.service = serviceObject;
		}

		public RequestProcessorFactory getRequestProcessorFactory(Class aClass)
				throws XmlRpcException {
//                        System.out.println("server requested for class " + aClass);
			if (aClass.equals(NodeIF.class))
			{
				return nodeFactory;
			}
			else if (aClass.equals(serviceObject.getServiceInterface()))
			{
				return serviceFactory;
			}
			else
			{
				return null;
			}
		}

		private class NodeRequestProcessorFactory implements RequestProcessorFactory {
			public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest)
					throws XmlRpcException {
//				System.out.println("1: invoking method : " + xmlRpcRequest.getMethodName());
				return node;
			}
		}

		private class ServiceRequestProcessorFactory implements RequestProcessorFactory {
			public Object getRequestProcessor(XmlRpcRequest xmlRpcRequest)
					throws XmlRpcException {
//				System.out.println("2: invoking method : " + xmlRpcRequest.getMethodName());
//                            System.out.println("using service object");
				return service;
			}
		}
}
	

	private boolean startServer ()
	{
		boolean started = true;
		try
		{
                    //close any open web servers
                    this.stopServer();
                    webServer = new WebServer(this.port);
                    XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

                    PropertyHandlerMapping phm = new PropertyHandlerMapping();
                    phm.setRequestProcessorFactoryFactory(new NodeRequestProcessorFactoryFactory(this, this.serviceObject));
                    phm.addHandler(NodeIF.class.getName(), NodeIF.class);
                    if (this.serviceObject != null)
                    {
                            phm.addHandler(this.serviceObject.getServiceInterface().getName(), this.serviceObject.getServiceInterface());
                    }
                    xmlRpcServer.setHandlerMapping(phm);
                    XmlRpcServerConfigImpl serverConfig =
                          (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
                    serverConfig.setEnabledForExtensions(true);
                    serverConfig.setContentLengthOptional(false);
    //	        xmlRpcServer.setTypeFactory(new XmlRpcTypeNil(xmlRpcServer));
//                    System.out.println("a1 " + this.port );
                    webServer.start();
//                    System.out.println("a2" );
                } catch (Exception e) {
			e.printStackTrace();
			//System.err.println(e.toString());
			started = false;
		}
		return started;
	}
	
	private void stopServer ()
	{
		if (this.webServer == null)
			return;
		try
		{
			this.webServer.shutdown();
		} catch (Exception e) {}
	}
//	class XmlRpcTypeNil extends TypeFactoryImpl {
//		 
//		public XmlRpcTypeNil(XmlRpcController pController) {
//			super(pController);
//		}
//	 
//		public TypeParser getParser(XmlRpcStreamConfig pConfig, NamespaceContextImpl pContext, String pURI, String pLocalName) {
//			System.out.println("-----------------" + pLocalName);
//			if (NullSerializer.NIL_TAG.equals(pLocalName) || NullSerializer.EX_NIL_TAG.equals(pLocalName) )return new NullParser();
//			else return super.getParser(pConfig, pContext, pURI, pLocalName);
//		}
//	 
//		public TypeSerializer getSerializer(XmlRpcStreamConfig pConfig, Object pObject) throws SAXException {
//			System.out.println("++++++++++++");
//			System.out.println("pobject is " + (pObject == null ? "null" : pObject.toString()));
//			System.out.println("+++++++++++++++++++++++++++");
//			if (pObject instanceof XmlRpcTypeNil) return new NullSerializer();
//			else return super.getSerializer(pConfig, pObject);
//		}
//	}
	
}
