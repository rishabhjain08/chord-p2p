package protocol.chord;

import java.util.List;

public interface NodeIF {

	public Node findPredecessor (Identifier ident);
//	public Node findSuccessor (Identifier ident);
	//public Node remote_findSuccessor (Identifier ident);
	public Node closestPrecedingFinger (Identifier ident);
	public boolean exists ();
	public Node getPredecessor ();
	public Node getSuccessor ();
	//TODO: change return type to void
//	public boolean setPredecessor (Node pNode);
	public Node fetch ();
	//TODO: change return type to void
	public boolean notify (Node n);
	public List<Node> getSuccessorList();
	//public Node remote_closestPrecedingFinger (Identifier ident);
}
