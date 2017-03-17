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
    static final String DB_URL = "jdbc:derby://localhost:1527/SearchEngineDB;create=true";

    public void initDB() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = getConnection();
            String createTableQuery = "create table \"APP\".Indexer "
                    + "("
                    + "	WORD VARCHAR(1000) not null, "
                    + "	DOCUMENT VARCHAR(1000) not null, "
                    + "	PLACE INTEGER not null, "
                    + "	TAG INTEGER default 7, "
                    + "	primary key (WORD, DOCUMENT, PLACE))";
            stmt = conn.createStatement();
            stmt.execute(createTableQuery);
            closeConnection(conn);
        } catch (SQLException se) {
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        //STEP 2: Register JDBC driver
        Class.forName(JDBC_DRIVER);

        //STEP 3: Open a connection
        System.out.println("Connecting to a selected database...");
        conn = DriverManager.getConnection(DB_URL);
        System.out.println("Connected database successfully...");
        return conn;
    }

    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public void insertWord(String word, String docID, int place, int tag) {

        Statement stmt = null;
        Connection conn = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            String sql = "INSERT INTO WORDS "
                    + "VALUES ('" + word + "', '" + docID + "', "
                    + place + ", " + tag + ")";
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
            } catch (SQLException ex) {
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
