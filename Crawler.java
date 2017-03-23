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
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class Crawler {

    DBModule searchEngineDB;
    private static Set<String> visitedPages;
    private static List<String> pagesToVisit;
    private static Set<String> exclusions;
    private static Set<String> allowed;
    private static int maxPages = 100;
    private final Object visitedLock;
    private final Object toVisitLock;
    private static int docNumber = 0;

    public String getNextPage() {

        boolean invalidLink = false;
        String nextPage = "";
        do {

            synchronized (toVisitLock) {
                if (pagesToVisit.isEmpty()) {
                    continue;
                }
                nextPage = pagesToVisit.remove(0);
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
                if(!isRobotSafe(pageURL)){
                    System.out.println("robot disallowed");
                    invalidLink = true;
                    continue;
                }
                URLConnection c = pageURL.openConnection();
                String contentType = c.getContentType();

                if (contentType == null) {
                    invalidLink = true;

                } else if (!contentType.contains("text/html;"))// only HTML docs
                {
                    invalidLink = true;

                } else {
                    invalidLink = false;
                }

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } while (invalidLink);
        
        synchronized (visitedLock) {
                visitedPages.add(nextPage);
            }
        return nextPage;
    }

    public boolean isRobotSafe(URL pageURL) {

        String pageRobots = pageURL.getProtocol() + "://" + pageURL.getHost() + "/robots.txt";
        String userAgent = "";
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new URL(pageRobots).openStream()))) {
            String line = reader.readLine();
            if(line == null)
                return true;
            while (line != null) {
                line = line.trim();
                if(line.startsWith("User-agent:")){
                    userAgent = line.substring(line.indexOf(":") + 1).trim();
                }
                else if(line.startsWith("Allow:") && userAgent.equals("*")){
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if(pageURL.getPath().equals(line)){
                        return true;
                    }                   
                }
                else if(line.startsWith("Disallow:") && userAgent.equals("*")){
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if(line.equals("/"))
                        return false;
                    if(pageURL.getPath().equals(line)){
                        return false;
                    }
                }
                line = reader.readLine();
            }
            return true;
        } catch (IOException exc) {
            // quit
            return true;
        }
        
    }

    public Crawler(String startLink, int maxPage) {
        this.visitedLock = new Object();
        this.toVisitLock = new Object();

        pagesToVisit = fileToList("toVisit.txt");
        visitedPages = fileToSet("visited.txt");
        pagesToVisit.add(startLink);
        maxPages = maxPage;
        docNumber = visitedPages.size();
        String deleteQuery = "DELETE from \"APP\".Crawler";
        /*if(docNumber == 0){
			searchEngineDB.executeQuery(deleteQuery);
		}*/

        searchEngineDB = new DBModule();
        searchEngineDB.initDB();
    }

    class crawlerThread extends Thread {

        public void run() {
            crawl();
        }

        public crawlerThread(String threadName) {
            super(threadName);
        }
    }

    public void init(int threads) {
        ExecutorService poolManager = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < threads; i++) {
            poolManager.submit(new crawlerThread("T" + i));
        }

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

    public void run() {
        this.crawl();
    }

    public static void main(String[] args) throws IOException {
        Crawler myCrawler1 = new Crawler(
                "https://www.tutorialspoint.com/java/java_thread_synchronization.htm", 500);
        myCrawler1.init(50);
    }

    public void createSiteDirectory(String siteDirectory) {
        File f = new File(siteDirectory);
        if (!f.exists()) {
            f.mkdir();
        }
    }

    public void createSiteHTML(String path, String Data) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileWriter fw = new FileWriter(f, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);
        out.append(Data);
        out.close();
        System.out.println("Thread " + Thread.currentThread().getName()
                + " - Now saving: " + "docs/" + docNumber + ".html");
    }

    public void insertIntoDB(String path, int ID) {
        String countQuery = "SELECT count(*)" + "FROM Crawler " + "WHERE ID = "
                + Integer.toString(ID);

        int count = searchEngineDB.executeScalar(countQuery);

        if (count == 0) {
            String insertQuery = "INSERT INTO Crawler " + "values("
                    + Integer.toString(ID) + ", '" + path + "', 0)";
            searchEngineDB.executeQuery(insertQuery);
        } else if (count == 1) {

            String insertQuery = "UPDATE Crawler " + "SET indexed = 0 "
                    + "WHERE ID = " + Integer.toString(ID);
            searchEngineDB.executeQuery(insertQuery);
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

                if ("-".equals(nextPage)) {
                    continue;
                }

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
                    setToFile(visitedPages, "visited.txt");

                }
                synchronized (toVisitLock) {
                    listToFile(pagesToVisit, "toVisit.txt");
                    createSiteHTML("docs/" + (++docNumber) + ".html",
                            doc.toString());
                    insertIntoDB(nextPage, docNumber);
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

}
