/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Binder;

import java.util.Set;
import protocol.chord.Identifier;
import protocol.chord.filemanager.FileManager;

/**
 *
 * @author waseem
 */
public class FileManagerTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        FileManager mgr = new FileManager();
//        mgr.init(-1, 6565);
        mgr.setUploadDirectory(null);
        Set<protocol.chord.Node> nodes = mgr.whoHasFile(Identifier.toIdentifier("45454"));
        mgr.getFileName(Identifier.toIdentifier("45454"), nodes.iterator().next());        
    }
    
}
