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
    
    public void tfidfRank(String []terms,List<CrawlerEntry> docs){
        //1- get all indexed documents
        //2- calculate the sum of the tf-idf value of each term for each document
        //3- sort documents based on final tf-idf value       
        
        
        double [] idf = new double[terms.length];
        String idfQuery;
        for(int i = 0;i<idf.length;i++){
            idfQuery="SELECT count(Distinct document) from INDEXER where word = '" + terms[i] + "'";
            
            double termOccurence = db.executeScalar(idfQuery) + 1;
            idf[i]=Math.log(docs.size()/termOccurence);
            //System.out.println(idf[i]);
        }
        for (CrawlerEntry doc : docs) {
            int termsCount=db.executeScalar("SELECT COUNT(*) from INDEXER WHERE document =" + doc.getID());
            double popularity = getPagePopularity(doc.getID());
            double tfidfSum = 0.0f;
            for(int j =0;j<terms.length;j++){
                double termFreq=db.executeScalar("SELECT COUNT(word) from INDEXER WHERE document =" + doc.getID()+" AND"
                        + " word = '" + terms[j]+"'");
                //System.out.println(termFreq);
                tfidfSum+=termFreq*idf[j]/termsCount;  
                //System.out.println(idf[j]);
            }
            
            doc.setRelevance(tfidfSum*popularity);
        }
        Collections.sort(docs);
    }
    public static void main(String[] args) {
        db = new DBModule();
        db.initDB();
        String indexedQuery = "SELECT * FROM Crawler WHERE Indexed = 1";
        List<CrawlerEntry> docs = db.executeCrawlerReader(indexedQuery);
        Ranker myRanker = new Ranker();
        String [] terms = {"yahoo","rank"};
        myRanker.tfidfRank(terms, docs);
        for(CrawlerEntry doc : docs){
            System.out.println(doc.getRelevance()+", " + doc.getID());
        }
    }
    
}
