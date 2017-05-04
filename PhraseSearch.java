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

    DBModule searchEngineDB = new DBModule();
    private static PhraseSearch phraseSearcher = new PhraseSearch();
    
    private PhraseSearch() {
        searchEngineDB.initDB();
    }
    
    public static PhraseSearch getInstance() {
        return phraseSearcher;
    }

    public List<PhraseSearchResult> phraseSearch(String phrase) {
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

        List<PhraseSearchResult> finalResults = new ArrayList<>();
        for (PhraseSearchResult result1 : results) {
            if (result1.getDocNo() == 0) {
                continue;
            }
            int count = 1;
            int tag = result1.getTag();
            for (PhraseSearchResult result2 : results) {
                if (result1.getDocNo() == result2.getDocNo() && result1 != result2) {
                    count++;
                    tag = min(tag, min(result1.getTag(), result2.getTag()));
                    result2.setDocNo(0);
                }
            }
            finalResults.add(new PhraseSearchResult(result1.getDocNo(),
                    tag, count));
        }
        return finalResults;
    }

    public static void main(String[] args) {
        PhraseSearch phraseSearcher = getInstance();
        List<PhraseSearchResult> phraseMatches = phraseSearcher.phraseSearch("search results");
        System.out.println("Phrase found in:");
        for (PhraseSearchResult phraseMatch : phraseMatches) {
            System.out.println("Document No " + phraseMatch.getDocNo());
        }
        Ranker ranker = Ranker.getInstance();
        ranker.phraseRank(phraseMatches);
    }
}
