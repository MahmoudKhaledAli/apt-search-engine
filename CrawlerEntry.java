/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.sql.Time;
import java.sql.Timestamp;

/**
 *
 * @author Mahmoud
 */
public class CrawlerEntry {

    private int ID;
    private String docID;
    private java.sql.Timestamp LastCrawled;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public Timestamp getLastCrawled() {
        return LastCrawled;
    }

    public void setLastCrawled(Timestamp LastCrawled) {
        this.LastCrawled = LastCrawled;
    }

}
