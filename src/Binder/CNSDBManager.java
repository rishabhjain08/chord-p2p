/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Binder;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import protocol.chord.Node;

/**
 *
 * @author WASEEM
 */
public class CNSDBManager {
    
    private String DBURL = "jdbc:mysql://localhost:3306/";
    private String DBName = "hashnamedb";
    private String Driver = "com.mysql.jdbc.Driver";
    private String UserName = "root"; 
    private String Password = "";

    
    public String MainTable="filemaps";
    
    private Connection ActiveDBConnection=null;


    public boolean connect() {
        boolean success=false;
        try{            
            Class.forName(this.Driver).newInstance();
            this.ActiveDBConnection = DriverManager.getConnection(this.DBURL+this.DBName,this.UserName,this.Password);                        
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Database Connection Error!!"+e.getMessage());            
        }
        return success;       
    }

    public boolean syncDB(Map<String, String> NetworkMap){
        
        //Statement LockDB ;
        Statement DoStuff ;
        //Statement UnLockDB ;        
        
        try {
            //LockDB = this.ActiveDBConnection.createStatement();
            DoStuff = this.ActiveDBConnection.createStatement();
            //UnLockDB = this.ActiveDBConnection.createStatement();

            
            //LockDB.execute("LOCK TABLES "+this.MainTable+" WRITE");
            
            /*Write UPDATE Query here*/
            DoStuff.executeQuery("");
            
            //UnLockDB.execute("UNLOCK TABLES ");
                     
        } catch (SQLException ex) {
            Logger.getLogger(CNSDBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public boolean Execute(String SQL) {
        try {
            //Statement UnLockDB = this.ActiveDBConnection.createStatement();
            //Statement UnLockDB = this.ActiveDBConnection.createStatement();

            //LockDB.execute("LOCK TABLES "+this.MainTable+" WRITE");
            Statement RawStatement = this.ActiveDBConnection.createStatement();
            RawStatement.execute(SQL);
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }
    
    public ResultSet fetchData(String SQL) {
        try {
            Statement RawStatement = this.ActiveDBConnection.createStatement();
            ResultSet rs = RawStatement.executeQuery(SQL);            
            
            return rs;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }
    
    public boolean disconnect(){
        boolean success=false;
        
        try {
            this.ActiveDBConnection.close();
            success=true;
        } catch (SQLException ex) {
            Logger.getLogger(CNSDBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }
     
    
}
