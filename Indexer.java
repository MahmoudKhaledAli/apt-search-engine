/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.io.*;
import java.nio.file.*;
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

    static DBModule searchEngineDB;
    static Stemmer stemmer;

    public static void insertWord(String word, int ID, int place, int tag) {
        for (int i = 0; i < word.length(); i++) {
            stemmer.add(word.charAt(i));
        }
        stemmer.stem();
        String wordStem = stemmer.toString();
        String sqlQuery = "INSERT INTO Indexer "
                + "VALUES ('" + word + "', '" + wordStem + "', " + place + ", "
                + tag + ", " + ID + ")";
        searchEngineDB.executeQuery(sqlQuery);
    }

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
            case "title":
                return 0;
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
                || tag.equals("p") || tag.equals("title")
                || tag.equals("body");
    }

    public static int indexElement(Element elem, int ID, int wordCount) {
        String text = elem.text().toLowerCase();

        String[] words = text.split("[^a-zA-Z0-9]+");
        for (String word : words) {
            int tagRank = convertTag(elem.tagName());
            insertWord(word, ID, wordCount++, tagRank);
        }
        return wordCount;
    }

    public static int indexTitle(Document doc, int ID) {
        String title = doc.title();
        int wordCount = 0;
        String[] words = title.split("[^a-zA-Z0-9]+");
        for (String word : words) {
            insertWord(word.toLowerCase(), ID, wordCount++, 0);
        }
        return wordCount;
    }

    public static boolean checkIfIndexed(int ID) {
        String countQuery = "SELECT indexed "
                + "FROM Crawler "
                + "WHERE ID = " + ID;

        int indexed = searchEngineDB.executeScalar(countQuery);
        System.out.println(Integer.toString(indexed));
        return indexed == 1;
    }

    public static void markAsIndexed(int ID) {
        String insertQuery = "UPDATE Crawler "
                + "SET indexed = 1 "
                + "WHERE ID = " + ID;
        searchEngineDB.executeQuery(insertQuery);
    }

    /**
     * Indexes a single page
     *
     * @param doc The HTML document
     */
    public static void indexPage(Document doc, int ID) {

        //Indexing the title of the page
        if (checkIfIndexed(ID)) {
            return;
        }
        indexTitle(doc, ID);

        //Starting from the body element to index the page
        Element body = doc.body();

        Elements h1 = body.getElementsByTag("h1");
        int wordCount = 0;
        for (Element elem : h1) {
            wordCount += indexElement(elem, ID, wordCount);
        }
        h1.remove();

        Elements h2 = body.getElementsByTag("h2");
        wordCount = 0;
        for (Element elem : h2) {
            wordCount += indexElement(elem, ID, wordCount);
        }
        h2.remove();

        Elements h3 = body.getElementsByTag("h3");
        wordCount = 0;
        for (Element elem : h3) {
            wordCount += indexElement(elem, ID, wordCount);
        }
        h3.remove();

        Elements h4 = body.getElementsByTag("h4");
        wordCount = 0;
        for (Element elem : h4) {
            wordCount += indexElement(elem, ID, wordCount);
        }
        h4.remove();

        Elements h5 = body.getElementsByTag("h5");
        wordCount = 0;
        for (Element elem : h5) {
            wordCount += indexElement(elem, ID, wordCount);
        }
        h5.remove();

        Elements h6 = body.getElementsByTag("h6");
        wordCount = 0;
        for (Element elem : h6) {
            wordCount += indexElement(elem, ID, wordCount);
        }
        h6.remove();

        String plainText = body.text();
        wordCount = 0;
        String[] words = plainText.split("[^a-zA-Z0-9]+");
        for (int i = 0; i < words.length; i++) {
            insertWord(words[i].toLowerCase(), ID, i, 7);

        }

        markAsIndexed(ID);
    }

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        searchEngineDB = new DBModule();
        searchEngineDB.initDB();
        stemmer = new Stemmer();

        ArrayList<Path> filePaths;
        while (true) {
            filePaths = getAllFileNames();
            for (int i = 0; i < filePaths.size(); i++) {
                File document = new File(filePaths.get(i).toString());
                Document doc = Jsoup.parse(document, "UTF-8", "");
                String docID = Paths.get(System.getProperty("user.dir") + "/docs").relativize(filePaths.get(i)).toString();
                int ID = Integer.parseInt(docID.replaceFirst("[.][^.]+$", ""));
                indexPage(doc, ID);
            }
        }
    }
}
