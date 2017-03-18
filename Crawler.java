import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;

import java.util.Set;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
public class Crawler {
	private Set<String> visitedPages;
	private List<String> pagesToVisit;
	private int maxPages = 100;
	
	public String getNextPage(){
		String nextPage = pagesToVisit.remove(0);
		while(visitedPages.contains(nextPage)){
			nextPage = pagesToVisit.remove(0);
		}
		visitedPages.add(nextPage);
		return nextPage;
	}
	public Crawler(String startLink, int maxPages){
		
		pagesToVisit = fileToList("toVisit.txt");
		visitedPages = fileToSet("visited.txt");
		pagesToVisit.add(startLink);
		this.maxPages = maxPages;
	}
	public void setToFile(Set<String> set,String filename){
		
		try(FileWriter fw = new FileWriter(filename, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw)){
				for(String link : set){
					out.println(link);
				}			    
			}
		catch (IOException e) {
			    //exception handling left as an exercise for the reader
				System.err.print(e.getMessage());
			}		
	}
	public void listToFile(List<String> list,String filename){
		
		try(FileWriter fw = new FileWriter(filename, false);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw)){
				for(String link : list){
					out.println(link);
				}			    
			}
		catch (IOException e) {
			    //exception handling left as an exercise for the reader
				System.err.print(e.getMessage());
			}		
	}
	public Set<String> fileToSet(String filename){
		Set<String> updatedPages = new HashSet<String>();
		File visited = new File(filename);
		if(!visited.exists()){
			return updatedPages;
		}			
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	updatedPages.add(line);
		    }
		}
		catch (IOException e) {		    
			System.err.print(e.getMessage());
		}
		
		return updatedPages;
	}
	
	public List<String> fileToList(String filename){
		List<String> updatedPages = new ArrayList<String>();
		File toVisit = new File(filename);
		if(!toVisit.exists()){
			return updatedPages;
		}			
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	updatedPages.add(line);
		    }
		}
		catch (IOException e) {		    
			System.err.print(e.getMessage());
		}
		
		return updatedPages;
	}
	public static void main(String[] args) throws IOException{
		Crawler myCrawler = new Crawler("https://en.wikipedia.org/wiki/Robots_exclusion_standard",100);
		myCrawler.crawl();
	}
	public void createSiteDirectory(String siteDirectory){
		File f = new File(siteDirectory);
		if(!f.exists())
			f.mkdir();
	}
	public void createSiteHTML(String path, String Data) throws IOException{		
		File f = new File(path);
		f.getParentFile().mkdirs(); 
		f.createNewFile();
		FileWriter fw = new FileWriter(f, true);
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		out.append(Data);
		out.close();
	}
	public String getHostName(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    String hostname = uri.getHost();
	    // to provide faultproof result, check if not null then return only hostname, without www.
	    if (hostname != null) {
	        return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
	    }
	    return hostname;
	}
	public void crawl(){
		try {
			while(!pagesToVisit.isEmpty() && visitedPages.size()<maxPages){
				String nextPage = getNextPage();
				if(nextPage == "")//to avoid empty strings
					continue;
				URL pageURL = new URL(nextPage);
				URLConnection c = pageURL.openConnection();
				String contentType = c.getContentType();
				//System.out.println(contentType);
				if(!contentType.contains("text/html; charset=UTF-8"))//only HTML docs
					continue;
				/*Connection connection =Jsoup.connect(nextPage);
				if(connection.response().statusCode() != 200)
					continue;
					*/
				Document doc = Jsoup.connect(nextPage).get();
				System.out.println("Now visiting: " + nextPage);
				createSiteHTML(getHostName(nextPage)+pageURL.getPath()+".html", doc.toString());
				Elements links = doc.getElementsByTag("a");
				for (Element link : links) {
					if(!link.attr("href").startsWith("#")){
						String linkHref = link.attr("abs:href");
						pagesToVisit.add(linkHref);
					}				  
				}
				setToFile(visitedPages, "visited.txt");
				listToFile(pagesToVisit, "toVisit.txt");
			}		
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
