/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

/**
 *
 * @author Mahmoud
 */
public class Indexer {

    static DBModule indexerDB;

    public static boolean isHTML(Path filePath) {
        String extension = "";
        String fileName = filePath.toString();

        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            extension = fileName.substring(i + 1);
        }
        return extension.equals("html");
    }

    /**
     *
     * @return @throws IOException
     */
    public static ArrayList<Path> getAllFileNames() throws IOException {
        ArrayList<Path> paths = new ArrayList<>();

        try (Stream<Path> filePathStream = Files.walk(Paths.get(System.getProperty("user.dir") + "/docs"))) {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath) && isHTML(filePath)) {
                    paths.add(filePath);
                    System.out.println(filePath);
                }
            });
        }
        return paths;
    }

    /**
     * Converts a tag from string format to its rank
     *
     * @param tag The HTML tag in string format
     * @return Returns the rank of HTML tag
     */
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

    /**
     * Checks if a tag is a header or a paragraph or body
     *
     * @param tag The HTML tag in string format
     * @return Returns true if the tag indicates the position of the element
     */
    public static boolean isGoodTag(String tag) {
        return tag.equals("h1") || tag.equals("h2") || tag.equals("h3")
                || tag.equals("h4") || tag.equals("h5") || tag.equals("h6")
                || tag.equals("p") || tag.equals("body") || tag.equals("#root");
    }

    /**
     * Indexes a single page
     *
     * @param doc The HTML document
     * @param docID The ID of the HTML document
     */
    public static void indexPage(Document doc, String docID) {
        Element title = doc.select("title").first();
        Element body = doc.select("body").first();
        Map wordsCount = new HashMap();

        //Indexing the page title
        if (title != null) {
            String[] words = title.text().split("[^a-zA-Z0-9]+");
            for (String word : words) {
                indexerDB.insertWord(word, docID, 0, 0);
            }
        }

        String pageText = body.text();
        String[] words = pageText.split("[^a-zA-Z0-9]+");

        //Indexing the words of the page
        for (int i = words.length - 1; i >= 0; i--) {
            //Don't index 1 letter words
            if (words[i].length() == 1) {
                continue;
            }

            if (!wordsCount.containsKey(words[i])) {
                wordsCount.put(words[i], 0);
            }

            Elements elemList = doc.getElementsContainingText(words[i]);
            int wordCount = (int) (wordsCount.get(words[i]));
            Element elem = elemList.get(elemList.size() - 1 - wordCount);

            //Loop until we find a header or paragraph tag
            while (!isGoodTag(elem.tagName())) {
                elem = elem.parent();
            }

            int tag = convertTag(elem.tag().getName());
            int place = i + 1;

            indexerDB.insertWord(words[i], docID, place, tag);
            wordsCount.put(words[i], wordCount + 1);
        }

    }

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        indexerDB = new DBModule();
        indexerDB.initDB();
        ArrayList<Path> filePaths = getAllFileNames();
        for (int i = 0; i < filePaths.size(); i++) {
            File document = new File(filePaths.get(i).toString());
            Document doc = Jsoup.parse(document, "UTF-8", "");
            indexPage(doc, Integer.toString(i));
        }
    }
}
