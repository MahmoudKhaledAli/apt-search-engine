/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mahmoud
 */
public class DBModule {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    static final String DB_URL = "jdbc:derby://localhost:1527/Indexer";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        //STEP 2: Register JDBC driver
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        //STEP 3: Open a connection
        System.out.println("Connecting to a selected database...");
        conn = DriverManager.getConnection(DB_URL);
        System.out.println("Connected database successfully...");
        return conn;
    }

    public static void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public static void insertWord(String word, String docID, int place, int tag) {

        Statement stmt = null;
        Connection conn = null;
        
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            String sql = "INSERT INTO WORDS "
                    + "VALUES ('" + word + "', '" + docID + "', " + place + ", " + tag + ")";
            System.out.println(sql);
            stmt.executeUpdate(sql);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBModule.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    closeConnection(conn);
                }
            } catch(SQLException ex) {
                //do nothing
            }
            try {
                if (conn != null) {
                    closeConnection(conn);
                }
            } catch (SQLException se) {
            }//end finally try
        }//end try
    }
}
