/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import static indexer.Indexer.stemmer;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author Dell
 */
public class Query {

    DBModule db = new DBModule();
    Stemmer stemmer = new Stemmer();
    private static Query queryProcessor = new Query();

    private Query() {
        db.initDB();
    }

    public static Query getInstance() {
        return queryProcessor;
    }

    public List<Integer> searchWords(String Words) {
        String[] WordArray = Words.split("[^a-zA-Z0-9]+");
        String query = "";
        for (int i = 0; i < WordArray.length; i++) {
            for (int j = 0; j < WordArray[i].length(); j++) {
                stemmer.add(WordArray[i].charAt(j));
            }
            stemmer.stem();
            String wordStem = stemmer.toString();
            query += "SELECT DISTINCT (DOCUMENT) FROM INDEXER WHERE STEM='" + wordStem + "'";
            if (i != WordArray.length - 1) {
                query += " INTERSECT ";
            }
        }
        List<IndexerEntry> list = db.executeIndexerReaderID(query);
        List<Integer> list2 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            list2.add(list.get(i).getDocument());
        }
        return list2;

    }

    public static int[] toIntArray(final Collection<Integer> data) {
        int[] result;
        // null result for null input
        if (data == null) {
            result = null;
            // empty array for empty collection
        } else if (data.isEmpty()) {
            result = new int[0];
        } else {
            final Collection<Integer> effective;
            // if data contains null make defensive copy
            // and remove null values
            if (data.contains(null)) {
                effective = new ArrayList<Integer>(data);
                while (effective.remove(null)) {
                }
                // otherwise use original collection
            } else {
                effective = data;
            }
            result = new int[effective.size()];
            int offset = 0;
            // store values
            for (final Integer i : effective) {
                result[offset++] = i.intValue();
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Query query = new Query();
        List<Integer> results = query.searchWords("google");
        Ranker ranker = Ranker.getInstance();
        String[] WordArray = "google".split("[^a-zA-Z0-9]+");
        int[] resultsList = toIntArray(results);
        ranker.tfidfRank(WordArray, resultsList);
        
        for (int i = 0; i < resultsList.length; i++) {
            
            System.out.println(resultsList[i]);
        }
    }

}
