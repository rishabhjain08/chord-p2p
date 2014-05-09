package protocol.chord.filemanager;

import java.util.HashSet;
import java.util.Map;
import protocol.chord.Identifier;
import protocol.chord.Node;

public interface FileManagerIF {
    
	//TODO: change return type to void
	public boolean exists ();
        public boolean addKeys (Map<String, HashSet<Node>> altKeys);
        public boolean setKeys (Map<String, HashSet<Node>> skeys, int distance);
        public Map<String, HashSet<Node>> getAllKeys ();
 	public HashSet<Node> getFileOwners (Identifier ident);
 	public String getFileName (Identifier ident);
}
