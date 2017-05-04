/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mahmoud
 */
public class PhraseSearch {

    static DBModule searchEngineDB;

    public static List<PhraseSearchResult> phraseSearch(String phrase) {
        List<PhraseSearchResult> results = new ArrayList<>();
        String[] words = phrase.split("[^a-zA-Z0-9]+");
        
        String firstQuery = "";
        
        for (int i = 0; i < words.length; i++) {
            firstQuery += "SELECT DOCUMENT FROM INDEXER WHERE WORD = '" + words[i].toLowerCase() + "'";
            if (i != words.length - 1) {
               firstQuery += "\nINTERSECT\n"; 
            }
        }
        
        List<IndexerEntry> allPossibleDocs = searchEngineDB.executeIndexerReaderID(firstQuery);
        
        String secondQuery = "SELECT * FROM INDEXER WHERE WORD = '" + words[0].toLowerCase()
                + "' AND DOCUMENT IN (";
        
        for (int i = 0; i < allPossibleDocs.size(); i++) {
            secondQuery += allPossibleDocs.get(i).getDocument();
            if (i != allPossibleDocs.size() - 1) {
                secondQuery += ", ";
            }
        }
        
        secondQuery += ')';
        
        List<IndexerEntry> firstWordMatches = searchEngineDB.executeIndexerReader(secondQuery);

        boolean phraseFound = true;
        
        for (IndexerEntry firstWordMatch : firstWordMatches) {
            phraseFound = true;
            for (int i = 1; i < words.length; i++) {
                String sqlQuery = "SELECT * FROM INDEXER WHERE DOCUMENT = "
                        + Integer.toString(firstWordMatch.getDocument()) + " "
                        + "AND PLACE = " + Integer.toString(i + firstWordMatch.getPlace())
                        + " AND TAG = " + Integer.toString(firstWordMatch.getTag());
                List<IndexerEntry> nextWord = searchEngineDB.executeIndexerReader(sqlQuery);
                if (nextWord.isEmpty()) {
                    phraseFound = false;
                    break;
                }
                if (!nextWord.get(0).getWord().equals(words[i].toLowerCase())) {
                    phraseFound = false;
                    break;
                }
            }
            if (phraseFound) {
                results.add(new PhraseSearchResult(firstWordMatch.getDocument(),
                        firstWordMatch.getTag()));
            }
        }
        
        return results;
    }

    public static void main(String[] args) {
        searchEngineDB = new DBModule();
        searchEngineDB.initDB();
        List<PhraseSearchResult> phraseMatches = phraseSearch("visual form of recursion");
        System.out.println("Phrase found in:");
        for (PhraseSearchResult phraseMatch : phraseMatches) {
            System.out.println("Document No " + phraseMatch.getDocNo());
        }
    }
}
