/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mahmoud
 */
public class PhraseSearch {

    static DBModule searchEngineDB;

    List<Integer> phraseSearch(String phrase) {
        List<Integer> docs = new ArrayList<>();
        String[] words = phrase.split("[^a-zA-Z0-9]+");

        String sqlQuery = "SELECT * FROM INDEXER WHERE WORD = '" + words[0].toLowerCase() + "'";

        List<IndexerEntry> firstWordMatches = searchEngineDB.executeIndexerReader(sqlQuery);

        for (IndexerEntry firstWordMatch : firstWordMatches) {
            for (int i = 1; i < words.length; i++) {
                sqlQuery = "SELECT * FROM INDEXER WHERE DOCUMENT = "
                        + Integer.toString(firstWordMatch.getDocument()) + " "
                        + "AND PLACE = " + Integer.toString(i + firstWordMatch.getPlace())
                        + " AND TAG = " + Integer.toString(firstWordMatch.getTag());
                List<IndexerEntry> nextWord = searchEngineDB.executeIndexerReader(sqlQuery);
                if (nextWord.isEmpty()) {
                    break;
                }
                if (!nextWord.get(0).getWord().equals(words[i].toLowerCase())) {
                    break;
                }
            }
            docs.add(firstWordMatch.getDocument());
        }

        return docs;
    }

    public static void main(String[] args) {
        searchEngineDB = new DBModule();
        searchEngineDB.initDB();
        PhraseSearch searcher = new PhraseSearch();
        List<Integer> phrases = searcher.phraseSearch("Cartoon illustrating the basic principle of PageRank");
    }
}
