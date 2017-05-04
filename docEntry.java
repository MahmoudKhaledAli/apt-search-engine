/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

/**
 *
 * @author Kahla
 */
public class docEntry implements Comparable<docEntry>{
    private int ID;
    private double relevance;
    public int getID(){
        return ID;
    }
    public void setID(int id){
        ID=id;
    }
    public double getRelevance(){
        return relevance;
    }
    public void setRelevance(double rel){
        this.relevance = rel;
    }
    @Override
    public int compareTo(docEntry o) {
        
        if(this.relevance>o.relevance)
            return -1;
        else if(this.relevance<o.relevance)
            return 1;
        return 0;
    }
}
