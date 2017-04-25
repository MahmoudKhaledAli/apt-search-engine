package indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import java.util.Date;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import jdk.nashorn.internal.objects.NativeArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class Crawler {

    DBModule searchEngineDB;
    private static Set<String> visitedPages;
    private static List<String> pagesToVisit;

    private static Set<String> disallowed = new HashSet<String>();
    private static Set<String> allowed = new HashSet<String>();
    private static Set<String> robots = new HashSet<String>();

    private static int maxPages = 100;
    private Object visitedLock = new Object();
    private Object toVisitLock = new Object();
    private Object DBLock = new Object();
    private Object timeLock = new Object();
    private Object robotsLock = new Object();
    private static int docNumber = 0;   

    public String getNextPage() {

        boolean invalidLink = false;
        String nextPage = "";
        do {
            synchronized (toVisitLock) {
                if (pagesToVisit.isEmpty()) {
                    invalidLink = true;
                    continue;
                }
                nextPage = pagesToVisit.remove(0);
                if (nextPage.equals("")) {
                    invalidLink = true;
                    continue;
                }
            }

            URL pageURL;
            synchronized (visitedLock) {
                if (visitedPages.contains(nextPage)) {
                    invalidLink = true;
                    continue;
                }
            }

            try {
                pageURL = new URL(nextPage);
                URLConnection c = pageURL.openConnection();
                String contentType = c.getContentType();

                if (contentType == null) {
                    invalidLink = true;
                    continue;

                } else if (!contentType.contains("text/html;"))// only HTML docs
                {
                    invalidLink = true;
                    continue;

                } else {
                    invalidLink = false;
                }
                if (!isRobotSafe(pageURL)) {
                    System.out.println("robot disallowed: " + pageURL.toString());
                    invalidLink = true;
                    continue;
                }

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } while (invalidLink);

//        synchronized (visitedLock) {
//            visitedPages.add(nextPage);
//
//        }
        return nextPage;
    }

    public boolean isRobotSafe(URL pageURL) {
        synchronized (robotsLock) {
            if (robots.contains(pageURL.getHost())) {
                if (allowed.contains(pageURL.toString())) {
                    return true;
                } else if (disallowed.contains(pageURL.getProtocol() + "://" + pageURL.getHost() + "/")) {
                    return false;
                } else if (disallowed.contains(pageURL.toString())) {
                    return false;
                }
                return true;
            }
            String mainPage = pageURL.getProtocol() + "://" + pageURL.getHost();
            String pageRobots = mainPage + "/robots.txt";
            String userAgent = "";

            boolean safe = true;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new URL(pageRobots).openStream()))) {
                String line = reader.readLine();
                if (line == null) {
                    return true;
                }
                while (line != null) {
                    line = line.trim();
                    if (line.startsWith("User-agent:")) {
                        userAgent = line.substring(line.indexOf(":") + 1).trim();
                    } else if (line.startsWith("Allow:") && userAgent.equals("*")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        allowed.add(mainPage + line);
                        if (pageURL.getPath().equals(line)) {
                            safe = true;
                        }
                    } else if (line.startsWith("Disallow:") && userAgent.equals("*")) {
                        line = line.substring(line.indexOf(":") + 1).trim();
                        disallowed.add(mainPage + line);
                        if (line.equals("/")) {
                            safe = false;
                        }
                        if (pageURL.getPath().equals(line)) {
                            safe = false;
                        }
                    }
                    line = reader.readLine();
                }
                robots.add(pageURL.getHost());
                return safe;
            } catch (IOException exc) {
                // quit
                return safe;
            }
        }

    }

    public Crawler(String[] startLink, int maxPage) {

        pagesToVisit = fileToList("toVisit.txt");
        visitedPages = fileToSet("visited.txt");
        for (int i = 0; i < startLink.length; i++) {
            pagesToVisit.add(startLink[i]);
        }
        String countQuery = "SELECT count(*) from \"APP\".Crawler ";
        maxPages = maxPage;

//        String deleteQuery = "DELETE from \"APP\".Crawler";
//        if(docNumber == 0){
//			searchEngineDB.executeQuery(deleteQuery);
//		}
        searchEngineDB = new DBModule();
        searchEngineDB.initDB();
        docNumber = searchEngineDB.executeScalar(countQuery);
    }

    class crawlerThread extends Thread {

        public void run() {
            crawl();
        }

        public crawlerThread(String threadName) {
            super(threadName);
        }
    }

    class crawlerNoModifyFreqThread extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000 * 60 * 60 * 24 * 2);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }                
                synchronized (DBLock) {                    
                        addToVisitNoModifyFromDB();                    
                }      

            }
        }

        public crawlerNoModifyFreqThread(String threadName) {
            super(threadName);
        }
    }

    class crawlerModifyFreqThread extends Thread {

        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000 * 60 * 60 * 2);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }                
                synchronized (DBLock) {
                    try {
                        addToVisitModifyFromDB();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }      

            }
        }

        public crawlerModifyFreqThread(String threadName) {
            super(threadName);
        }
    }

    public void init(int threads) throws InterruptedException {
        ExecutorService poolManager = Executors.newFixedThreadPool(100);
        for (int i = 0; i < threads; i++) {
            poolManager.execute(new crawlerThread("T" + i));

        }
        poolManager.execute(new crawlerModifyFreqThread("Modify"));
        poolManager.execute(new crawlerNoModifyFreqThread("No-Modify"));
        poolManager.shutdown();
        try {
            poolManager.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Done");

    }

    public void setToFile(Set<String> set, String filename) {

        try (FileWriter fw = new FileWriter(filename, false);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {

            set.forEach((link) -> {
                out.println(link);
            });

        } catch (IOException e) {
            // exception handling left as an exercise for the reader
            System.err.print(e.getMessage());
        }

    }

    public void listToFile(List<String> list, String filename) {

        try (FileWriter fw = new FileWriter(filename, false);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {

            for (String link : list) {
                out.println(link);
            }

        } catch (IOException e) {
            // exception handling left as an exercise for the reader
            System.err.print(e.getMessage());
        }

    }

    public static Set<String> fileToSet(String filename) {

        Set<String> updatedPages = new HashSet<>();

        File visited = new File(filename);
        if (!visited.exists()) {
            return updatedPages;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                updatedPages.add(line);
            }
        } catch (IOException e) {
            System.err.print(e.getMessage());
        }

        return updatedPages;
    }

    public static List<String> fileToList(String filename) {

        List<String> updatedPages = new ArrayList<>();

        File toVisit = new File(filename);
        if (!toVisit.exists()) {
            return updatedPages;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                updatedPages.add(line);
            }
        } catch (IOException e) {
            System.err.print(e.getMessage());
        }

        return updatedPages;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String[] links = {
            "https://en.wikipedia.org/wiki/PageRank"
        };

        Crawler myCrawler1 = new Crawler(links, 200);
        myCrawler1.init(5);

    }

    public void createSiteDirectory(String siteDirectory) {
        File f = new File(siteDirectory);
        if (!f.exists()) {
            f.mkdir();
        }
    }

    public void createSiteHTML(int path, String Data) throws IOException {
        File f = new File("docs/" + path + ".html");
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileWriter fw = new FileWriter(f, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);
        out.append(Data);
        out.close();
        System.out.println("Thread " + Thread.currentThread().getName()
                + " - Now saving: " + "docs/" + path + ".html");
    }

    public int getOldDocNo(String path) {
        String countQuery = "SELECT count(*)" + "FROM Crawler " + "WHERE docID = '"
                + path + "'";

        int count = searchEngineDB.executeScalar(countQuery);

        if (count == 0) {
            return 0;
        } else {
            String selectQuery = "SELECT ID " + "FROM Crawler " + "WHERE docID = '"
                    + path + "'";
            return searchEngineDB.executeScalar(selectQuery);
        }
    }
    
    void incrementReference(String link)
    {
        String incQuery = "UPDATE Crawler "
                + "SET refCount = refCount + 1"
                + "WHERE docID = '" + link + "'";
        searchEngineDB.executeQuery(incQuery);
    }

    public void insertIntoDB(String path, int ID, long lastModified) {
        String countQuery = "SELECT count(*)" + "FROM Crawler " + "WHERE ID = "
                + Integer.toString(ID);

        int count = searchEngineDB.executeScalar(countQuery);

        if (count == 0) {

            java.util.Date today = new java.util.Date();
            java.sql.Timestamp currentTime = new java.sql.Timestamp(today.getTime());
            String insertQuery = "INSERT INTO Crawler(ID,docID,INDEXED,LastCrawled,LastModified) " + "values("
                    + Integer.toString(ID) + ", '" + path + "', 0, '"
                    + currentTime + "', " + lastModified + ")";
            searchEngineDB.executeQuery(insertQuery);
        } else if (count == 1) {
            java.util.Date today = new java.util.Date();
            java.sql.Timestamp currentTime = new java.sql.Timestamp(today.getTime());
            String insertQuery = "UPDATE Crawler " + "SET indexed = 0, "
                    + "LastCrawled = '" + currentTime + "', LastModified = " + lastModified
                    + " WHERE ID = " + Integer.toString(ID);
            searchEngineDB.executeQuery(insertQuery);
        }

    }

    public void addToVisitNoModifyFromDB() {
        String getQuery = "SELECT * From Crawler "
                + "WHERE LastModified = 0 AND "
                + "{fn TIMESTAMPDIFF(SQL_TSI_DAY,LastCrawled ,CURRENT_TIMESTAMP )}>=2";
        List<CrawlerEntry> crawlerEntries = searchEngineDB.executeCrawlerReader(getQuery);
        //System.out.println("No modify refreshing: " + crawlerEntries.size() + " links");
        for (CrawlerEntry entry : crawlerEntries) {
            String link = entry.getDocID();
            synchronized (toVisitLock) {
                pagesToVisit.add(0, link);
            }
            synchronized (visitedLock) {
                visitedPages.remove(link);
            }

        }
    }

    public void addToVisitModifyFromDB() throws MalformedURLException, IOException {
        String getQuery = "SELECT * From Crawler "
                + "WHERE LastModified != 0 ";
        URL pageURL;
        List<CrawlerEntry> crawlerEntries = searchEngineDB.executeCrawlerReader(getQuery);
        //System.out.println("Modify refreshing: " + crawlerEntries.size() + " links");
        for (CrawlerEntry entry : crawlerEntries) {
            String link = entry.getDocID();
            pageURL = new URL(link);
            URLConnection c = pageURL.openConnection();
            if (c.getLastModified() > entry.getLastModified()) {
                synchronized (toVisitLock) {
                    pagesToVisit.add(0, link);
                }
                synchronized (visitedLock) {
                    visitedPages.remove(link);
                }
            }

        }
    }

    public String getHostName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String hostname = uri.getHost();
        // to provide faultproof result, check if not null then return only
        // hostname, without www.
        if (hostname != null) {
            return hostname.startsWith("www.") ? hostname.substring(4)
                    : hostname;
        }
        return hostname;
    }

    public void crawl() {
        while (true) {
            try {

                String nextPage = getNextPage();

                Document doc = Jsoup.connect(nextPage).userAgent("*").get();
                System.out.println("Thread " + Thread.currentThread().getName()
                        + " - Now visiting: " + nextPage);

                Elements links = doc.getElementsByTag("a");
                for (Element link : links) {
                    if (!link.attr("href").startsWith("#")) {
                        String linkHref = link.attr("abs:href");

                        if ("".equals(linkHref)) {

                            continue;
                        }
                        synchronized(visitedLock){
                            if(visitedPages.contains(linkHref)){
                                incrementReference(linkHref);                               
                            }
                        }
                        
                        synchronized (toVisitLock) {
                            if (!pagesToVisit.contains(linkHref)) {
                                pagesToVisit.add(linkHref);
                            }
                        }

                    }
                }
                
                synchronized (visitedLock) {
                    if (docNumber >= maxPages) {
                        break;
                    }
                    visitedPages.add(nextPage);
                    setToFile(visitedPages, "visited.txt");

                }
                synchronized (toVisitLock) {                    
                        listToFile(pagesToVisit, "toVisit.txt");
                }
                synchronized (DBLock) {
                        int oldNo = getOldDocNo(nextPage);
                        URL pageURL = new URL(nextPage);
                        URLConnection c = pageURL.openConnection();
                        long lastModified = c.getLastModified();
                        if (oldNo == 0) {
                            createSiteHTML((++docNumber),
                                    doc.toString());
                            insertIntoDB(nextPage, docNumber, lastModified);
                        } else {
                            createSiteHTML(oldNo,
                                    doc.toString());
                            insertIntoDB(nextPage, oldNo, lastModified);
                        }
                }
                    

                

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }
}
