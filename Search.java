/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import struct.Result;
import indexer.*;
import java.io.File;
import javax.servlet.ServletContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Mahmoud
 */
public class Search extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DBModule db = new DBModule();
        db.initDB();
        Query queryProc = Query.getInstance();
        PhraseSearch phraserSearcher = PhraseSearch.getInstance();
        Ranker ranker = Ranker.getInstance();
        String query = request.getParameter("query");
        // Set response content type
        response.setContentType("text/html");
        query = query.trim();
        request.setAttribute("query", query);
        String[] WordArray = query.split("[^a-zA-Z0-9]+");
        for (int i = 0; i < WordArray.length; i++) {
            WordArray[i] = WordArray[i].toLowerCase();
        }
        boolean phrase;

        List<Integer> docs = new ArrayList<>();

        if (query.charAt(0) == '\"' && query.charAt(query.length() - 1) == '\"') {
            phrase = true;
            query = query.substring(1, query.length() - 1);
            List<PhraseSearchResult> resultsPhrase = phraserSearcher.phraseSearch(query);
            ranker.phraseRank(resultsPhrase);
            for (int i = 0; i < resultsPhrase.size(); i++) {
                docs.add(resultsPhrase.get(i).getDocNo());
            }
        } else {
            phrase = false;
            List<Integer> resultsWords = queryProc.searchWords(query);
            int[] resultsWordsInt = Query.toIntArray(resultsWords);
            ranker.tfidfRank(WordArray, resultsWordsInt);
            for (int i = 0; i < resultsWords.size(); i++) {
                docs.add(resultsWordsInt[i]);
            }
        }

        String sqlQuery = "INSERT INTO QUERY VALUES ('" + query + "'," + "0" + ")";
        db.executeQuery(sqlQuery);
        sqlQuery = "UPDATE QUERY SET COUNT = COUNT + 1 WHERE QUERY = '" + query + "'";
        db.executeQuery(sqlQuery);

        List<Result> resultsJSP = new ArrayList<>();

        for (Integer doc : docs) {
            String dbQuery = "SELECT * FROM CRAWLER WHERE ID = " + doc;
            List<CrawlerEntry> docsInfo = db.executeCrawlerReader(dbQuery);
            String url = docsInfo.get(0).getDocID();
            int docNo = docsInfo.get(0).getID();
            Document docHTML = Jsoup.parse(getFile(docNo), "UTF-8");
            String pageTitle = docHTML.getElementsByTag("title").text();
            String snippet = getSnippet(query, docHTML, phrase);
            resultsJSP.add(new Result(url, pageTitle, snippet));
        }

        request.setAttribute("results", resultsJSP);
        RequestDispatcher view = request.getRequestDispatcher("results.jsp");
        view.forward(request, response);
    }

    private String getSnippet(String query, Document doc, boolean phrase) {
        try {
            String[] sentences = doc.getElementsByTag("body").text().split("\\.|\\?|!");
            String snippet = "";
            int count = 0;
            String[] words = query.split("[^a-zA-Z0-9]+");
            Stemmer stemmer = new Stemmer();
            if (!phrase) {
                for (String sentence : sentences) {
                    String sentenceLook = sentence.toLowerCase();
                    if (count > 1) {
                        break;
                    }
                    for (String word : words) {
                        for (int i = 0; i < word.length(); i++) {
                            stemmer.add(word.charAt(i));
                        }
                        stemmer.stem();
                        String stemmed = stemmer.toString();
                        if (sentenceLook.contains(stemmed.toLowerCase()) || sentenceLook.contains(word.toLowerCase())) {
                            if (sentence.length() <= 200) {
                                snippet += sentence;
                                snippet += ". ";
                                count++;
                                break;
                            } else {
                                int index1 = sentenceLook.indexOf(stemmed.toLowerCase());
                                if (index1 == -1) {
                                    index1 = sentenceLook.indexOf(word.toLowerCase());
                                }
                                String toAdd = sentence.substring(
                                        (index1 - 50 < 0) ? index1 : index1 - 50,
                                        (index1 + 100 >= sentence.length()) ? sentence.length() : index1 + 100);
                                int index2 = toAdd.lastIndexOf(' ');
                                toAdd = toAdd.substring(0, (index2 == -1) ? toAdd.length() : index2);
                                count = 2;
                                snippet += toAdd;
                                snippet += " ...";
                                break;
                            }
                        }
                    }
                }
            } else {
                for (String sentence : sentences) {
                    String sentenceLook = sentence.toLowerCase();
                    if (sentenceLook.contains(query.toLowerCase())) {
                        if (sentence.length() > 200) {
                            sentence = sentence.substring(0, 200);
                            int index = sentence.lastIndexOf(' ');
                            sentence = sentence.substring(0,
                                    (index != -1) ? index : 200);
                            sentence += " ..";
                        }
                        snippet = sentence;
                        snippet = snippet.replaceAll("((?i)" + query + ")", "<b>$1</b>");
                        return snippet + ".";
                    }
                }
            }
            for (String word : words) {
                for (int i = 0; i < word.length(); i++) {
                    stemmer.add(word.charAt(i));
                }
                stemmer.stem();
                String stemmed = stemmer.toString();
                snippet = snippet.replaceAll("((?i)" + word + ")", "<b>$1</b>");
                snippet = snippet.replaceAll("((?i)" + stemmed + ")", "<b>$1</b>");
            }
            return snippet;
        } catch (Exception e) {
            return "";
        }
    }

    private File getFile(int docID) {
        ServletContext context = getServletContext();
        return new File(context.getRealPath("docs/" + docID + ".html"));
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
