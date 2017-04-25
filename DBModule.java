/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import javafx.scene.chart.PieChart.Data;

/**
 *
 * @author Mahmoud
 */
public class DBModule {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    static final String DB_URL = "jdbc:derby://localhost:1527/SearchEngineDB;create=true";

    public void initDB() {

        String createTableQuery = "create table \"APP\".Crawler "
                + "( "
                + "ID INTEGER not null, "
                + "docID VARCHAR(1000) not null, "
                + "INDEXED INTEGER default 0, "
                + "LastCrawled TIMESTAMP not null, "
                + "LastModified BIGINT not null, "
                + "refCount BIGINT default 0,"
                + "primary key (ID))";
        executeQuery(createTableQuery);

        createTableQuery = "create table \"APP\".Indexer"
                + "( WORD VARCHAR(1000) not null, STEM VARCHAR (1000) not null,\n"
                + "PLACE INTEGER not null, TAG INTEGER default 7,\n"
                + "DOCUMENT INTEGER CONSTRAINT word_foreign_key REFERENCES Crawler ON DELETE CASCADE,\n"
                + "primary key (WORD, DOCUMENT, PLACE, TAG))";

        executeQuery(createTableQuery);
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        //Register JDBC driver
        Class.forName(JDBC_DRIVER);

        //Open a connection
        Connection conn = DriverManager.getConnection(DB_URL);
        return conn;
    }

    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public List<IndexerEntry> executeIndexerReader(String sqlQuery) {

        Statement stmt = null;
        Connection conn = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            System.out.println(sqlQuery);
            ResultSet rs = stmt.executeQuery(sqlQuery);
            List<IndexerEntry> list = new ArrayList<>();
            while (rs.next()) {
                IndexerEntry data = new IndexerEntry();
                data.setWord(rs.getString("word"));
                data.setStem(rs.getString("stem"));
                data.setPlace(rs.getInt("place"));
                data.setTag(rs.getInt("tag"));
                data.setDocument(rs.getInt("document"));
                list.add(data);
            }
            return list;
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

    public List<CrawlerEntry> executeCrawlerReader(String sqlQuery) {

        Statement stmt = null;
        Connection conn = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            System.out.println(sqlQuery);
            ResultSet rs = stmt.executeQuery(sqlQuery);
            List<CrawlerEntry> list = new ArrayList<>();
            while (rs.next()) {
                CrawlerEntry data = new CrawlerEntry();
                data.setID(rs.getInt("ID"));
                data.setDocID(rs.getString("docID"));
                data.setLastCrawled(rs.getTimestamp("LastCrawled"));
                data.setLastModified(rs.getLong("LastModified"));
                list.add(data);
            }
            return list;
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

    public int executeScalar(String sqlQuery) {

        Statement stmt = null;
        Connection conn = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            System.out.println(sqlQuery);
            ResultSet rs = stmt.executeQuery(sqlQuery);
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBModule.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
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
