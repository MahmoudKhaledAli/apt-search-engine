/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.sql.*;
import java.util.logging.*;

/**
 *
 * @author Mahmoud
 */
public class DBModule {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    static final String DB_URL = "jdbc:derby://localhost:1527/SearchEngineDB;create=true";

    public void initDB() {
        String createTableQuery = "create table \"APP\".Indexer "
                + "( "
                + "WORD VARCHAR(1000) not null, "
                + "DOCUMENT VARCHAR(1000) not null, "
                + "PLACE INTEGER not null, "
                + "TAG INTEGER default 7, "
                + "primary key (WORD, DOCUMENT, PLACE))";

        executeQuery(createTableQuery);

        createTableQuery = "create table \"APP\".Crawler "
                + "( "
                + "ID VARCHAR(1000) not null, "
                + "INDEXED BOOLEAN default false, "
                + "primary key (ID))";
        executeQuery(createTableQuery);
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        //Register JDBC driver
        Class.forName(JDBC_DRIVER);

        //Open a connection
        System.out.println("Connecting to a selected database...");
        Connection conn = DriverManager.getConnection(DB_URL);
        System.out.println("Connected database successfully...");
        return conn;
    }

    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public ResultSet executeReader(String sqlQuery) {

        Statement stmt = null;
        Connection conn = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            System.out.println(sqlQuery);
            return stmt.executeQuery(sqlQuery);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBModule.class.getName()).log(Level.SEVERE, null, ex);
            return null;
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

    public void executeQuery(String sqlQuery) {

        Statement stmt = null;
        Connection conn = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            System.out.println(sqlQuery);
            stmt.executeUpdate(sqlQuery);

        } catch (SQLTransactionRollbackException se) {

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBModule.class
                    .getName()).log(Level.SEVERE, null, ex);
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
