/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Mahmoud
 */
public class Ranker {
    static DBModule db;
    public static double getPagePopularity(int docNo) {
        String query = "SELECT refCount"
                + "FROM Crawler "
                + "WHERE docID = " + "docNo";
        
        return db.executeScalar(query);        
    }
    public void setDB(DBModule db){
        this.db = db;
    }
    
    public static double getPhraseRelevance(String phrase, int docNo) {
        return 0.0d;
    }
    
    public static double getWordRelevance(String word, int docNo) {
        return 0.0d;
    }
    
    public void tfidfRank(String []terms){
        //1- get all indexed documents
        //2- calculate the sum of the tf-idf value of each term for each document
        //3- sort documents based on final tf-idf value
        
        
        String indexedQuery = "SELECT * FROM Crawler WHERE Indexed = 1";
        List<CrawlerEntry> docs = db.executeCrawlerReader(indexedQuery);
        double [] idf = new double[terms.length];
        String idfQuery;
        for(int i = 0;i<idf.length;i++){
            idfQuery="SELECT count(Distinct document) from Indexer where word = '" + terms[i] + "'";
            idf[i]=Math.log(docs.size()/(db.executeScalar(idfQuery) + 1));
        }
        for (CrawlerEntry doc : docs) {
            int termsCount=db.executeScalar("SELECT COUNT(*) from Indexer WHERE document =" + doc.getDocID());
            double popularity = getPagePopularity(doc.getID());
            double tfidfSum = 0.0f;
            for(int j =0;j<terms.length;j++){
                int termFreq=db.executeScalar("SELECT COUNT(*) from Indexer WHERE document =" + doc.getDocID()+" AND"
                        + "word = '" + terms[j]+"'")/termsCount;
                
                tfidfSum+=termFreq*idf[j];               
            }
            doc.setRelevance(tfidfSum*popularity);
        }
        Collections.sort(docs);
    }
    
}
