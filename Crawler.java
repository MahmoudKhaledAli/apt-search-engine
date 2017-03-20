package indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class Crawler {

	DBModule searchEngineDB;
	private static Set<String> visitedPages;
	private static List<String> pagesToVisit;
	private static int maxPages = 100;
	private Object visitedLock = new Object();
	private Object toVisitLock = new Object();
	private static int docNumber = 0;

	public String getNextPage() {

		boolean invalidLink = false;
		String nextPage = "";
		do {

			synchronized (toVisitLock) {
				if (pagesToVisit.isEmpty()) {
					return "-";
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
				URLConnection c = pageURL.openConnection();
				String contentType = c.getContentType();

				if (contentType == null) {
					invalidLink = true;

				} else if (!contentType.contains("text/html;"))// only HTML docs
				{
					invalidLink = true;

				} else
					invalidLink = false;

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} while (invalidLink);

		return nextPage;
	}

	public Crawler(String startLink, int maxPage) {

		pagesToVisit = fileToList("toVisit.txt");
		visitedPages = fileToSet("visited.txt");
		pagesToVisit.add(startLink);
		maxPages = maxPage;
		docNumber = visitedPages.size();

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

	}

	public void setToFile(Set<String> set, String filename) {

		try (FileWriter fw = new FileWriter(filename, false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {

			for (String link : set) {
				out.println(link);
			}

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
		Set<String> updatedPages = new HashSet<String>();
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
		List<String> updatedPages = new ArrayList<String>();
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
				"https://en.wikipedia.org/wiki/Robots_exclusion_standard", 100);
		myCrawler1.init(5);
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

				if (nextPage == "-")
					continue;

				Document doc = Jsoup.connect(nextPage).userAgent("*").get();
				System.out.println("Thread " + Thread.currentThread().getName()
						+ " - Now visiting: " + nextPage);

				Elements links = doc.getElementsByTag("a");
				for (Element link : links) {
					if (!link.attr("href").startsWith("#")) {
						String linkHref = link.attr("abs:href");
						if (linkHref == "") {
							System.out.println();
							continue;
						}
						synchronized (toVisitLock) {
							if (!pagesToVisit.contains(linkHref)) {
								System.out.println("Thread "
										+ Thread.currentThread().getName()
										+ " - fetched a new link. ");
							}

						}

					}
				}
				synchronized (visitedLock) {
					visitedPages.add(nextPage);
					setToFile(visitedPages, "visited.txt");
					if (visitedPages.size() > maxPages)
						break;
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
