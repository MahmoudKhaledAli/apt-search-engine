/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Mahmoud
 */
public class Indexer {

    /**
     * @param args the command line arguments
     */
    
    //Converts a tag from a string to a rank in integers
    public static int convertTag(String tag) {
        switch (tag) {
            case "h1":
                return 1;
            case "h2":
                return 2;
            case "h3":
                return 3;
            case "h4":
                return 4;
            case "h5":
                return 5;
            case "h6":
                return 6;
            default:
                return 7;
        }
    }
    
    //checks if a tag is a header or a paragraph or body
    public static boolean isGoodTag(String tag) {
        if (tag.equals("h1")) {
            return true;
        } else if (tag.equals("h2")) {
            return true;
        } else if (tag.equals("h3")) {
            return true;
        } else if (tag.equals("h4")) {
            return true;
        } else if (tag.equals("h5")) {
            return true;
        } else if (tag.equals("h6")) {
            return true;
        } else if (tag.equals("p")) {
            return true;
        } else if (tag.equals("body")) {
            return true;
        } else if (tag.equals("#root")) {
            return true;
        }
        return false;
    }
    
    //to index a single document
    public static void indexPage(Document doc, String docID) {
        Element title = doc.select("title").first();
        Element body = doc.select("body").first();
        Map wordsCount = new HashMap();
        
        //Indexing the page title
        if (title != null) {
            String[] words = title.text().split("[^a-zA-Z0-9']+");
            for (String word : words) {
                DBModule.insertWord(word, docID, 0, 0);
            }
        }

        
        String pageText = body.text();
        String[] words = pageText.split("[^a-zA-Z0-9']+");
        
        //Indexing the words of the page
        for (int i = words.length - 1; i >= 0; i--) {
            if (!wordsCount.containsKey(words[i])) {
                wordsCount.put(words[i], 0);
            }
            Elements elemList = doc.getElementsContainingText(words[i]);
            Element elem = elemList.get(elemList.size() - (int) (wordsCount.get(words[i])) - 1);
            while (!isGoodTag(elem.tagName())) {
                elem = elem.parent();
            }
            DBModule.insertWord(words[i], docID, i + 1, convertTag(elem.tag().getName()));
            System.out.println(elem.tag().getName());
            wordsCount.put(words[i], (int) (wordsCount.get(words[i])) + 1);
        }

    }


    public static void main(String[] args) throws IOException {
        // TODO code application logic here

        File document = new File("Hello.html");
        Document doc = Jsoup.parse(document, "UTF-8", "");
        indexPage(doc, "1");
    }
}
