/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Test;
import Binder.CNSDBManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author WASEEM
 */
public class TestDBConn {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        CNSDBManager DBM = new CNSDBManager();
        if(DBM.connect()){
         System.out.println("Connection Successful");   
        }
            if (DBM.Execute("INSERT INTO FILEMAPS(FILENAME,HASHKEY) VALUES('ff','hh')")){
             System.out.println("Statement Executed");                   
            }            
        
        if(DBM.disconnect()){
         System.out.println("DB disconnected");               
        }
        
    }
}
