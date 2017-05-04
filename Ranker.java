/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Mahmoud
 */
public class Ranker {

    static DBModule db = new DBModule();
    static Stemmer stemmer = new Stemmer();
    private static Ranker ranker = new Ranker();
    private Ranker() {
        db.initDB();
    };
    public static Ranker getInstance(){
        return ranker;
    }

    public long getPagePopularity(int docNo) {
        String query = "SELECT refCount "
                + "FROM CRAWLER "
                + "WHERE ID = " + docNo;

        return db.executeScalar(query);
    }

    public static double getPhraseRelevance(String phrase, int docNo) {
        return 0.0d;
    }

    public static double getWordRelevance(String word, int docNo) {
        return 0.0d;
    }

    public void tfidfRank(String[] query, int[] docNumbers) {
        String[] terms = new String[query.length];
        for (int i = 0; i < query.length; i++) {
            for (int j = 0; j < query[i].length(); j++) {
                stemmer.add(query[i].charAt(j));
            }
            stemmer.stem();
            String wordStem = stemmer.toString();
            terms[i] = wordStem;
        }
            

        List<docEntry> docs = new ArrayList<>();
        for (int i = 0; i < docNumbers.length; i++) {
            docEntry newEntry = new docEntry();
            newEntry.setID(docNumbers[i]);
            docs.add(newEntry);
        }
        double[] idf = new double[terms.length];
        String idfQuery;
        for (int i = 0; i < idf.length; i++) {
            idfQuery = "SELECT count(Distinct document) from INDEXER where word = '" + terms[i] + "'";

            double termOccurence = db.executeScalar(idfQuery) + 1;
            idf[i] = Math.log(docs.size() / termOccurence);
            
        }
        for (docEntry doc : docs) {
            int termsCount = db.executeScalar("SELECT COUNT(*) from INDEXER WHERE document =" + doc.getID());
            double popularity = getPagePopularity(doc.getID());
            double tfidfSum = 0.0f;
            for (int j = 0; j < terms.length; j++) {
                double termFreq = db.executeScalar("SELECT COUNT(word) from INDEXER WHERE document =" + doc.getID() + " AND"
                        + " word = '" + terms[j] + "'");
                int termTag = db.executeScalar("SELECT Tag from INDEXER WHERE document = " + doc.getID()+" "
                        + " AND word ='" + terms[j] + "'");
                double tagWeight = 1/(1+termTag);
                tfidfSum += termFreq * idf[j] * tagWeight / termsCount;
                
            }

            doc.setRelevance(tfidfSum * popularity);
        }
        Collections.sort(docs);
        for (int i = 0; i < docNumbers.length; i++) {
            docNumbers[i] = docs.get(i).getID();
        }
    }
    public void phraseRank(List<PhraseSearchResult> results) {
        db = new DBModule();
        db.initDB();
        for (PhraseSearchResult result : results) {
            result.setRank(getPagePopularity(result.getDocNo()));
        }
        Collections.sort(results);
    }

    public static void main(String[] args) {        
        String indexedQuery = "SELECT * FROM Crawler WHERE Indexed = 1";
        List<CrawlerEntry> docs = db.executeCrawlerReader(indexedQuery);
        int [] docNums = new int[docs.size()];
        for(int i=0;i<docs.size();i++){
            docNums[i] = docs.get(i).getID();
        }
        Ranker myRanker = Ranker.getInstance();
        String[] terms = {"yahoo", "ranking"};
      myRanker.tfidfRank(terms, docNums);
        for(int i=0;i<docs.size();i++){
            System.out.println(docNums[i]);
        }
    }

}
