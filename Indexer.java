/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
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

    public static void initDB() {
        String createTableQuery = "create table \"APP\".Indexer "
                + "( "
                + "WORD VARCHAR(1000) not null, "
                + "DOCUMENT VARCHAR(1000) not null, "
                + "PLACE INTEGER not null, "
                + "TAG INTEGER default 7, "
                + "primary key (WORD, DOCUMENT, PLACE))";

        indexerDB.executeQuery(createTableQuery);
    }

    public static void insertWord(String word, String docID, int place, int tag) {

        String sqlQuery = "INSERT INTO Indexer "
                + "VALUES ('" + word + "', '" + docID + "', "
                + place + ", " + tag + ")";
        indexerDB.executeQuery(sqlQuery);
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
                || tag.equals("p") || tag.equals("title");
    }

    /**
     * Indexes a single page
     *
     * @param doc The HTML document
     * @param docID The ID of the HTML document
     */
    public static void indexPage(Document doc, String docID) {
        int wordCount = 0;

        //Indexing the title of the page
        String title = doc.title();
        String[] words = title.split("[^a-zA-Z0-9]+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 1) {
                insertWord(words[i].toLowerCase(), docID, wordCount++, 0);
            }
        }

        //Starting from the body element to index the page
        Element body = doc.body();
        Stack<Element> elemStack = new Stack<>();
        elemStack.push(body);
        while (!elemStack.isEmpty()) {
            Element elem = elemStack.peek();
            elemStack.pop();
            //get list of children
            Elements childrenList = elem.children();
            //reverse list to ensure correct order of words
            Collections.reverse(childrenList);
            for (Element child : childrenList) {
                if (isGoodTag(child.tagName()) || child.children().isEmpty()) {
                    //if it's a position tag or no more tags after it
                    //then add its text
                    String text = child.text();
                    words = text.split("[^a-zA-Z0-9]+");
                    for (String word : words) {
                        if (word.length() > 1) {
                            int tagRank = convertTag(child.tagName());
                            insertWord(word.toLowerCase(), docID, wordCount++, tagRank);
                        }
                    }
                } else {
                    String text = child.ownText();
                    //to get the text that is contained in this element only
                    words = text.split("[^a-zA-Z0-9]+");
                    for (String word : words) {
                        if (word.length() > 1) {
                            int tagRank = convertTag(child.tagName());
                            insertWord(word.toLowerCase(), docID, wordCount++, tagRank);
                        }
                    }
                    //add the element to the list of elements to be visited
                    elemStack.push(child);
                }
            }
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
        initDB();

        ArrayList<Path> filePaths = getAllFileNames();
        for (int i = 0; i < filePaths.size(); i++) {
            File document = new File(filePaths.get(i).toString());
            Document doc = Jsoup.parse(document, "UTF-8", "");
            indexPage(doc, Integer.toString(i));
        }
    }
}
