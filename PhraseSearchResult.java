/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

/**
 *
 * @author Mahmoud
 */
public class PhraseSearchResult {

    int docNo;
    int tag;
    int count;
    double rank;

    public PhraseSearchResult(int docNo, int tag) {
        this.docNo = docNo;
        this.tag = tag;
    }

    public PhraseSearchResult(int docNo, int tag, int count) {
        this.docNo = docNo;
        this.tag = tag;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public int getDocNo() {
        return docNo;
    }

    public void setDocNo(int docNo) {
        this.docNo = docNo;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

}
